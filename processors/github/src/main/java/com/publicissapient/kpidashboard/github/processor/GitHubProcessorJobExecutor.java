/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.github.processor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.executor.ProcessorJobExecutor;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.model.scm.MergeRequests;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
import com.publicissapient.kpidashboard.common.repository.scm.CommitRepository;
import com.publicissapient.kpidashboard.common.repository.scm.MergeRequestRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.github.config.GitHubConfig;
import com.publicissapient.kpidashboard.github.constants.GitHubConstants;
import com.publicissapient.kpidashboard.github.customexception.FetchingCommitException;
import com.publicissapient.kpidashboard.github.model.GitHubProcessor;
import com.publicissapient.kpidashboard.github.model.GitHubProcessorItem;
import com.publicissapient.kpidashboard.github.processor.service.GitHubClient;
import com.publicissapient.kpidashboard.github.repository.GitHubProcessorItemRepository;
import com.publicissapient.kpidashboard.github.repository.GitHubProcessorRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * GitHubProcessorJobExecutor represents a class which holds all the
 * configuration and GitHub execution process.
 * 
 * @see GitHubProcessor
 */
@Slf4j
@Component
public class GitHubProcessorJobExecutor extends ProcessorJobExecutor<GitHubProcessor> {

	@Autowired
	private GitHubProcessorRepository gitHubProcessorRepository;

	@Autowired
	private GitHubConfig gitHubConfig;

	@Autowired
	private GitHubProcessorItemRepository gitHubProcessorItemRepository;

	@Autowired
	private GitHubClient gitHubClient;

	@Autowired
	private CommitRepository commitsRepo;

	@Autowired
	private MergeRequestRepository mergReqRepo;

	@Autowired
	private ProcessorToolConnectionService processorToolConnectionService;

	@Autowired
	private ProjectBasicConfigRepository projectConfigRepository;

	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	/**
	 * 
	 * The constructor.
	 * 
	 * @param taskScheduler
	 *            taskScheduler
	 */
	@Autowired
	protected GitHubProcessorJobExecutor(TaskScheduler taskScheduler) {
		super(taskScheduler, ProcessorConstants.GITHUB);
	}

	/**
	 * Gets the cron.
	 *
	 * @return the cron
	 */
	@Override
	public String getCron() {
		return gitHubConfig.getCron();
	}

	/**
	 * Creates the processor item.
	 *
	 * @param tool
	 *            the tool
	 * @param processorId
	 *            the processor id
	 * @return the processor item
	 */
	private GitHubProcessorItem createProcessorItem(ProcessorToolConnection tool, ObjectId processorId) {
		GitHubProcessorItem item = new GitHubProcessorItem();
		item.setToolConfigId(tool.getId());
		item.setProcessorId(processorId);
		item.setActive(Boolean.TRUE);
		item.getToolDetailsMap().put(GitHubConstants.URL, tool.getUrl());
		item.getToolDetailsMap().put(GitHubConstants.TOOL_BRANCH, tool.getBranch());
		item.getToolDetailsMap().put(GitHubConstants.SCM, tool.getToolName());
		item.getToolDetailsMap().put(GitHubConstants.OWNER, tool.getUsername());
		item.getToolDetailsMap().put(GitHubConstants.REPO_NAME, tool.getRepositoryName());
		item.getToolDetailsMap().put(GitHubConstants.REPO_BRANCH, tool.getBranch());
		return item;
	}

	/**
	 * Checks if is new commitDetails.
	 *
	 * @param gitHubProcessorItem
	 *            the git repo
	 * @param commitDetails
	 *            commitDetails
	 * @return true, if is new commit
	 */
	private boolean isNewCommit(GitHubProcessorItem gitHubProcessorItem, CommitDetails commitDetails) {
		CommitDetails dbCommit = commitsRepo.findByProcessorItemIdAndRevisionNumber(gitHubProcessorItem.getId(),
				commitDetails.getRevisionNumber());
		return dbCommit == null;
	}

	/**
	 * Checks if is new mergeRequests.
	 * 
	 * @param gitHubProcessorItem
	 *            the git repo
	 * @param mergeRequests
	 * @return true, if is new merge Request
	 */
	private boolean isNewMergeReq(GitHubProcessorItem gitHubProcessorItem, MergeRequests mergeRequests) {
		boolean isNewReq = false;
		MergeRequests mergReq = mergReqRepo.findByProcessorItemIdAndRevisionNumber(gitHubProcessorItem.getId(),
				mergeRequests.getRevisionNumber());
		if (mergReq == null) {
			isNewReq = true;
		} else if (!mergReq.getState().equals(mergeRequests.getState())) {
			mergeRequests.setId(mergReq.getId());
			mergReqRepo.save(mergeRequests);
		}
		return isNewReq;
	}

	/**
	 * Execute.
	 *
	 * @param processor
	 *            the processor
	 * @return boolean value
	 */
	@Override
	public boolean execute(GitHubProcessor processor) {
		boolean executionStatus = true;
		String uid = UUID.randomUUID().toString();
		MDC.put("GitHubProcessorJobExecutorUid", uid);

		long gitHubProcessorStartTime = System.currentTimeMillis();
		MDC.put("GitHubProcessorJobExecutorStartTime", String.valueOf(gitHubProcessorStartTime));

		int reposCount = 0;
		int commitsCount = 0;
		int mergReqCount = 0;

		List<ProjectBasicConfig> projectConfigList = getSelectedProjects();
		MDC.put("TotalSelectedProjectsForProcessing", String.valueOf(projectConfigList.size()));
		clearSelectedBasicProjectConfigIds();
		for (ProjectBasicConfig proBasicConfig : projectConfigList) {
			List<ProcessorToolConnection> githubJobsFromConfig = processorToolConnectionService
					.findByToolAndBasicProjectConfigId(ProcessorConstants.GITHUB, proBasicConfig.getId());
			for (ProcessorToolConnection tool : githubJobsFromConfig) {
				ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(
						proBasicConfig.getId().toHexString());
				try {
					processorExecutionTraceLog.setExecutionStartedAt(System.currentTimeMillis());
					GitHubProcessorItem gitHubProcessorItem = getGitHubProcessorItem(tool, processor.getId());
					boolean firstTimeRun = (gitHubProcessorItem.getLastUpdatedCommit() == null);

					List<CommitDetails> commitDetailList = gitHubClient.fetchAllCommits(gitHubProcessorItem,
							firstTimeRun, tool, proBasicConfig);
					updateAssigneeNameForCommits(proBasicConfig, processorExecutionTraceLog, gitHubProcessorItem,
							commitDetailList);
					List<CommitDetails> unsavedCommits = commitDetailList.stream()
							.filter(commit -> isNewCommit(gitHubProcessorItem, commit)).collect(Collectors.toList());
					unsavedCommits.forEach(commit -> commit.setProcessorItemId(gitHubProcessorItem.getId()));
					commitsRepo.saveAll(unsavedCommits);
					commitsCount += unsavedCommits.size();
					if (!commitDetailList.isEmpty()) {
						gitHubProcessorItem.setLastUpdatedCommit(commitDetailList.get(0).getRevisionNumber());
					}

					List<MergeRequests> mergeRequestsList = gitHubClient.fetchMergeRequests(gitHubProcessorItem,
							firstTimeRun, tool, proBasicConfig);

					updateAssigneeForMerge(proBasicConfig, processorExecutionTraceLog, gitHubProcessorItem,
							mergeRequestsList);
					List<MergeRequests> unsavedMergeRequests = mergeRequestsList.stream()
							.filter(mergReq -> isNewMergeReq(gitHubProcessorItem, mergReq))
							.collect(Collectors.toList());
					unsavedMergeRequests.forEach(mergReq -> mergReq.setProcessorItemId(gitHubProcessorItem.getId()));
					mergReqRepo.saveAll(unsavedMergeRequests);
					mergReqCount += unsavedMergeRequests.size();

					gitHubProcessorItem.setLastUpdatedTime(Calendar.getInstance().getTime());
					gitHubProcessorItemRepository.save(gitHubProcessorItem);
					reposCount++;
					processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
					processorExecutionTraceLog.setExecutionSuccess(true);
					processorExecutionTraceLog.setLastEnableAssigneeToggleState(proBasicConfig.isSaveAssigneeDetails());
					processorExecutionTraceLogService.save(processorExecutionTraceLog);
				} catch (FetchingCommitException exception) {
					executionStatus = false;
					processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
					processorExecutionTraceLog.setExecutionSuccess(executionStatus);
					processorExecutionTraceLog.setLastEnableAssigneeToggleState(false);
					processorExecutionTraceLogService.save(processorExecutionTraceLog);
					log.error(String.format("Error in processing %s", tool.getUrl()), exception);
				}
			}
		}

		MDC.put("RepoCount", String.valueOf(reposCount));
		MDC.put("CommitCount", String.valueOf(commitsCount));

		if (commitsCount > 0) {
			cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.BITBUCKET_KPI_CACHE);
		}
		if (mergReqCount > 0) {
			cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.BITBUCKET_KPI_CACHE);
		}

		long bitBucketProcessorEndTime = System.currentTimeMillis();
		MDC.put("GitHubProcessorJobExecutorEndTime", String.valueOf(bitBucketProcessorEndTime));

		MDC.put("TotalGitHubProcessorJobExecutorTime",
				String.valueOf(bitBucketProcessorEndTime - gitHubProcessorStartTime));
		log.info("GitHub processor execution finished at {}", bitBucketProcessorEndTime);
		MDC.put("executionStatus", String.valueOf(executionStatus));
		MDC.clear();
		return executionStatus;
	}

	private void updateAssigneeNameForCommits(ProjectBasicConfig proBasicConfig,
			ProcessorExecutionTraceLog processorExecutionTraceLog, GitHubProcessorItem gitHubProcessorItem,
			List<CommitDetails> commitDetailList) {
		if (proBasicConfig.isSaveAssigneeDetails() && !processorExecutionTraceLog.isLastEnableAssigneeToggleState()) {
			List<CommitDetails> updateAuthor = new ArrayList<>();
			commitDetailList.stream().forEach(commit -> {
				CommitDetails commitDetailsData = commitsRepo.findByProcessorItemIdAndRevisionNumber(
						gitHubProcessorItem.getId(), commit.getRevisionNumber());
				if (commitDetailsData != null) {
					commitDetailsData.setAuthor(commit.getAuthor());
					updateAuthor.add(commitDetailsData);
				}
			});
			commitsRepo.saveAll(updateAuthor);

		}
	}

	private void updateAssigneeForMerge(ProjectBasicConfig proBasicConfig,
			ProcessorExecutionTraceLog processorExecutionTraceLog, GitHubProcessorItem gitHubProcessorItem,
			List<MergeRequests> mergeRequestsList) {
		if (proBasicConfig.isSaveAssigneeDetails() && !processorExecutionTraceLog.isLastEnableAssigneeToggleState()) {
			List<MergeRequests> updateAuthor = new ArrayList<>();
			mergeRequestsList.stream().forEach(mergeRequests -> {
				MergeRequests mergeRequestData = mergReqRepo.findByProcessorItemIdAndRevisionNumber(
						gitHubProcessorItem.getId(), mergeRequests.getRevisionNumber());
				if (mergeRequestData != null) {
					mergeRequestData.setAuthor(mergeRequests.getAuthor());
					updateAuthor.add(mergeRequestData);
				}
			});
			mergReqRepo.saveAll(updateAuthor);
		}
	}

	private GitHubProcessorItem getGitHubProcessorItem(ProcessorToolConnection tool, ObjectId processorId) {
		List<GitHubProcessorItem> gitHubProcessorItemList = gitHubProcessorItemRepository
				.findByProcessorIdAndToolConfigId(processorId, tool.getId());
		GitHubProcessorItem gitHubProcessorItem;
		if (CollectionUtils.isNotEmpty(gitHubProcessorItemList)) {
			gitHubProcessorItem = gitHubProcessorItemList.get(0);
		} else {
			gitHubProcessorItem = gitHubProcessorItemRepository.save(createProcessorItem(tool, processorId));
		}
		return gitHubProcessorItem;
	}

	private ProcessorExecutionTraceLog createTraceLog(String basicProjectConfigId) {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.GITHUB);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		Optional<ProcessorExecutionTraceLog> existingTraceLogOptional = processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.GITHUB, basicProjectConfigId);
		existingTraceLogOptional.ifPresent(
				existingProcessorExecutionTraceLog -> processorExecutionTraceLog.setLastEnableAssigneeToggleState(
						existingProcessorExecutionTraceLog.isLastEnableAssigneeToggleState()));
		return processorExecutionTraceLog;
	}

	/**
	 * Gets the processor.
	 *
	 * @return the processor
	 */
	@Override
	public GitHubProcessor getProcessor() {
		return GitHubProcessor.prototype();
	}

	/**
	 * Gets the processor repository.
	 *
	 * @return the processor repository
	 */
	@Override
	public ProcessorRepository<GitHubProcessor> getProcessorRepository() {
		return gitHubProcessorRepository;
	}

	/**
	 * Cleans the cache in the Custom API
	 * 
	 * @param cacheEndPoint
	 *            the cache endpoint
	 * @param cacheName
	 *            the cache name
	 */
	private void cacheRestClient(String cacheEndPoint, String cacheName) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(gitHubConfig.getCustomApiBaseUrl());
		uriBuilder.path("/");
		uriBuilder.path(cacheEndPoint);
		uriBuilder.path("/");
		uriBuilder.path(cacheName);

		HttpEntity<?> entity = new HttpEntity<>(headers);

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, entity, String.class);
		} catch (RestClientException e) {
			log.error("[GITHUB-CUSTOMAPI-CACHE-EVICT]. Error while consuming rest service {}", e);
		}

		if (null != response && response.getStatusCode().is2xxSuccessful()) {
			log.info("[GITHUB-CUSTOMAPI-CACHE-EVICT]. Successfully evicted cache: {} ", cacheName);
		} else {
			log.error("[GITHUB-CUSTOMAPI-CACHE-EVICT]. Error while evicting cache: {}", cacheName);
		}

		clearToolItemCache(gitHubConfig.getCustomApiBaseUrl());
	}

	/**
	 * Return List of selected ProjectBasicConfig id if null then return all
	 * ProjectBasicConfig ids
	 * 
	 * @return List of projects
	 */
	private List<ProjectBasicConfig> getSelectedProjects() {
		List<ProjectBasicConfig> allProjects = projectConfigRepository.findAll();
		MDC.put("TotalConfiguredProject", String.valueOf(CollectionUtils.emptyIfNull(allProjects).size()));

		List<String> selectedProjectsBasicIds = getProjectsBasicConfigIds();
		if (CollectionUtils.isEmpty(selectedProjectsBasicIds)) {
			return allProjects;
		}
		return CollectionUtils.emptyIfNull(allProjects).stream().filter(
				projectBasicConfig -> selectedProjectsBasicIds.contains(projectBasicConfig.getId().toHexString()))
				.collect(Collectors.toList());
	}

	private void clearSelectedBasicProjectConfigIds() {
		setProjectsBasicConfigIds(null);
	}

}
