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

package com.publicissapient.kpidashboard.bitbucket.processor;

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

import com.publicissapient.kpidashboard.bitbucket.config.BitBucketConfig;
import com.publicissapient.kpidashboard.bitbucket.constants.BitBucketConstants;
import com.publicissapient.kpidashboard.bitbucket.customexception.FetchingCommitException;
import com.publicissapient.kpidashboard.bitbucket.factory.BitBucketClientFactory;
import com.publicissapient.kpidashboard.bitbucket.model.BitbucketProcessor;
import com.publicissapient.kpidashboard.bitbucket.model.BitbucketRepo;
import com.publicissapient.kpidashboard.bitbucket.processor.service.BitBucketClient;
import com.publicissapient.kpidashboard.bitbucket.repository.BitbucketProcessorRepository;
import com.publicissapient.kpidashboard.bitbucket.repository.BitbucketRepoRepository;
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

import lombok.extern.slf4j.Slf4j;

/**
 * BitBucketProcessorJobExecutor represents a class which holds all the
 * configuration and BitBucket execution process.
 * 
 * @see BitbucketProcessor
 */
@Slf4j
@Component
public class BitBucketProcessorJobExecutor extends ProcessorJobExecutor<BitbucketProcessor> {

	@Autowired
	private BitbucketProcessorRepository bitBucketProcessorRepo;

	@Autowired
	private BitBucketConfig bitBucketConfig;

	@Autowired
	private BitbucketRepoRepository bitBucketRepository;

	@Autowired
	private BitBucketClientFactory bitBucketClientFactory;

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

	@Autowired
	protected BitBucketProcessorJobExecutor(TaskScheduler taskScheduler) {
		super(taskScheduler, ProcessorConstants.BITBUCKET);
	}

	/**
	 * Gets the cron.
	 *
	 * @return the cron
	 */
	@Override
	public String getCron() {
		return bitBucketConfig.getCron();
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
	private BitbucketRepo createProcessorItem(ProcessorToolConnection tool, ObjectId processorId) {
		BitbucketRepo item = new BitbucketRepo();
		item.setVersion((short) 2);
		item.setToolConfigId(tool.getId());
		item.setProcessorId(processorId);
		item.setActive(Boolean.TRUE);
		item.getToolDetailsMap().put(BitBucketConstants.URL, tool.getUrl());
		item.getToolDetailsMap().put(BitBucketConstants.TOOL_BRANCH, tool.getBranch());
		item.getToolDetailsMap().put(BitBucketConstants.SCM, tool.getToolName());
		item.getToolDetailsMap().put(BitBucketConstants.BITBUCKET_API, tool.getApiEndPoint());
		return item;
	}

	/**
	 * Checks if is new commitDetails.
	 *
	 * @param bitRepo
	 *            the bit repo
	 * @return true, if is new commit
	 */
	private boolean isNewCommit(BitbucketRepo bitRepo, CommitDetails commitDetails) {
		CommitDetails dbCommit = commitsRepo.findByProcessorItemIdAndRevisionNumber(bitRepo.getId(),
				commitDetails.getRevisionNumber());
		return dbCommit == null;
	}

	/**
	 * Checks if is new mergeRequests.
	 * 
	 * @param bitRepo
	 *            the bit repo
	 * @return true, if is new merge Request
	 */
	private boolean isNewMergeReq(BitbucketRepo bitRepo, MergeRequests mergeRequests) {
		MergeRequests mergReq = mergReqRepo.findByProcessorItemIdAndRevisionNumber(bitRepo.getId(),
				mergeRequests.getRevisionNumber());
		return mergReq == null;
	}

	/**
	 * Execute.
	 *
	 * @param processor
	 *            the processor
	 */
	@Override
	public boolean execute(BitbucketProcessor processor) {
		boolean executionStatus = true;
		String uid = UUID.randomUUID().toString();
		MDC.put("BitBucketProcessorJobExecutorUid", uid);

		long bitBucketProcessorStartTime = System.currentTimeMillis();
		MDC.put("BitBucketProcessorJobExecutorStartTime", String.valueOf(bitBucketProcessorStartTime));

		int reposCount = 0;
		int commitsCount = 0;
		int mergReqCount = 0;

		List<ProjectBasicConfig> projectConfigList = getSelectedProjects();
		MDC.put("TotalSelectedProjectsForProcessing", String.valueOf(projectConfigList.size()));
		clearSelectedBasicProjectConfigIds();
		for (ProjectBasicConfig proBasicConfig : projectConfigList) {
			List<ProcessorToolConnection> bitbucketJobsFromConfig = processorToolConnectionService
					.findByToolAndBasicProjectConfigId(ProcessorConstants.BITBUCKET, proBasicConfig.getId());
			for (ProcessorToolConnection tool : bitbucketJobsFromConfig) {
				ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(
						proBasicConfig.getId().toHexString());
				try {
					processorExecutionTraceLog.setExecutionStartedAt(System.currentTimeMillis());
					BitbucketRepo bitRepo = getBitbucketRepo(tool, processor.getId());
					if (proBasicConfig.isSaveAssigneeDetails()
							&& !processorExecutionTraceLog.isLastEnableAssigneeToggleState()) {
						bitRepo.setLastUpdatedCommit(null);
					}
					boolean firstTimeRun = (bitRepo.getLastUpdatedCommit() == null);
					MDC.put("BitbucketReposDataCollectionStarted",
							"Bitbucket Processor started collecting data for Url: " + tool.getUrl() + ", branch : "
									+ tool.getBranch() + " and repo : " + tool.getRepoSlug());
					BitBucketClient bitBucketClient = bitBucketClientFactory.getBitbucketClient(tool.isCloudEnv());
					List<CommitDetails> commitDetailList = bitBucketClient.fetchAllCommits(bitRepo, firstTimeRun, tool,
							proBasicConfig);
					updateAssigneeForCommit(proBasicConfig, processorExecutionTraceLog, bitRepo, commitDetailList);
					List<CommitDetails> unsavedCommits = commitDetailList.stream()
							.filter(commit -> isNewCommit(bitRepo, commit)).collect(Collectors.toList());
					unsavedCommits.forEach(commit -> commit.setProcessorItemId(bitRepo.getId()));
					commitsRepo.saveAll(unsavedCommits);
					commitsCount += unsavedCommits.size();
					if (!commitDetailList.isEmpty()) {
						bitRepo.setLastUpdatedCommit(commitDetailList.get(0).getRevisionNumber());
					}

					List<MergeRequests> mergeRequestsList = bitBucketClient.fetchMergeRequests(bitRepo, firstTimeRun,
							tool, proBasicConfig);
					updateAssigneeForMerge(proBasicConfig, processorExecutionTraceLog, bitRepo, mergeRequestsList);
					List<MergeRequests> unsavedMergeRequests = mergeRequestsList.stream()
							.filter(mergReq -> isNewMergeReq(bitRepo, mergReq)).collect(Collectors.toList());
					unsavedMergeRequests.forEach(mergReq -> mergReq.setProcessorItemId(bitRepo.getId()));
					mergReqRepo.saveAll(unsavedMergeRequests);
					mergReqCount += unsavedMergeRequests.size();

					bitRepo.setLastUpdatedTime(Calendar.getInstance().getTime());
					bitBucketRepository.save(bitRepo);
					MDC.put("BitbucketReposDataCollectionCompleted", "Bitbucket Processor collected data for Url: "
							+ tool.getUrl() + ", branch : " + tool.getBranch() + " and repo : " + tool.getRepoSlug());
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
		MDC.put("BitBucketProcessorJobExecutorEndTime", String.valueOf(bitBucketProcessorEndTime));

		MDC.put("TotalBitBucketProcessorJobExecutorTime",
				String.valueOf(bitBucketProcessorEndTime - bitBucketProcessorStartTime));
		log.info("Bitbucket processor execution finished at {}", bitBucketProcessorEndTime);
		MDC.put("executionStatus", String.valueOf(executionStatus));
		MDC.clear();
		return executionStatus;
	}
	@Override
	public boolean executeSprint(String sprintId) {
		return false;
	}

	private void updateAssigneeForCommit(ProjectBasicConfig proBasicConfig,
			ProcessorExecutionTraceLog processorExecutionTraceLog, BitbucketRepo bitRepo,
			List<CommitDetails> commitDetailList) {
		if (proBasicConfig.isSaveAssigneeDetails() && !processorExecutionTraceLog.isLastEnableAssigneeToggleState()) {
			List<CommitDetails> updateAuthor = new ArrayList<>();
			commitDetailList.stream().forEach(commitDetails -> {
				CommitDetails dbCommit = commitsRepo.findByProcessorItemIdAndRevisionNumber(bitRepo.getId(),
						commitDetails.getRevisionNumber());
				if (dbCommit != null) {
					dbCommit.setAuthor(commitDetails.getAuthor());
					updateAuthor.add(dbCommit);
				}
			});
			commitsRepo.saveAll(updateAuthor);
		}
	}

	private void updateAssigneeForMerge(ProjectBasicConfig proBasicConfig,
			ProcessorExecutionTraceLog processorExecutionTraceLog, BitbucketRepo bitRepo,
			List<MergeRequests> mergeRequestsList) {
		if (proBasicConfig.isSaveAssigneeDetails() && !processorExecutionTraceLog.isLastEnableAssigneeToggleState()) {
			List<MergeRequests> updateAuthor = new ArrayList<>();
			mergeRequestsList.forEach(mergeRequests -> {
				MergeRequests dbMerge = mergReqRepo.findByProcessorItemIdAndRevisionNumber(bitRepo.getId(),
						mergeRequests.getRevisionNumber());
				if (dbMerge != null) {
					dbMerge.setAuthor(mergeRequests.getAuthor());
					updateAuthor.add(dbMerge);
				}
			});
			mergReqRepo.saveAll(updateAuthor);
		}
	}

	private BitbucketRepo getBitbucketRepo(ProcessorToolConnection tool, ObjectId processorId) {
		List<BitbucketRepo> bitRepoList = bitBucketRepository.findByProcessorIdAndToolConfigId(processorId,
				tool.getId());
		BitbucketRepo bitRepo;
		if (CollectionUtils.isNotEmpty(bitRepoList)) {
			bitRepo = bitRepoList.get(0);
		} else {
			bitRepo = bitBucketRepository.save(createProcessorItem(tool, processorId));
		}
		return bitRepo;
	}

	private ProcessorExecutionTraceLog createTraceLog(String basicProjectConfigId) {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.BITBUCKET);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		Optional<ProcessorExecutionTraceLog> existingTraceLogOptional = processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.BITBUCKET, basicProjectConfigId);
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
	public BitbucketProcessor getProcessor() {
		return BitbucketProcessor.prototype();
	}

	/**
	 * Gets the processor repository.
	 *
	 * @return the processor repository
	 */
	@Override
	public ProcessorRepository<BitbucketProcessor> getProcessorRepository() {
		return bitBucketProcessorRepo;
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

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(bitBucketConfig.getCustomApiBaseUrl());
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
			log.error("[BITBUCKET-CUSTOMAPI-CACHE-EVICT]. Error while consuming rest service {}", e);
		}

		if (null != response && response.getStatusCode().is2xxSuccessful()) {
			log.info("[BITBUCKET-CUSTOMAPI-CACHE-EVICT]. Successfully evicted cache: {} ", cacheName);
		} else {
			log.error("[BITBUCKET-CUSTOMAPI-CACHE-EVICT]. Error while evicting cache: {}", cacheName);
		}

		clearToolItemCache(bitBucketConfig.getCustomApiBaseUrl());
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
