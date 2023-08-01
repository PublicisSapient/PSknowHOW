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

package com.publicissapient.kpidashboard.gitlab.processor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.generic.Processor;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.model.scm.MergeRequests;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorItemRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
import com.publicissapient.kpidashboard.common.repository.scm.CommitRepository;
import com.publicissapient.kpidashboard.common.repository.scm.MergeRequestRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.gitlab.config.GitLabConfig;
import com.publicissapient.kpidashboard.gitlab.constants.GitLabConstants;
import com.publicissapient.kpidashboard.gitlab.customexception.FetchingCommitException;
import com.publicissapient.kpidashboard.gitlab.model.GitLabProcessor;
import com.publicissapient.kpidashboard.gitlab.model.GitLabRepo;
import com.publicissapient.kpidashboard.gitlab.processor.service.impl.GitLabClient;
import com.publicissapient.kpidashboard.gitlab.repository.GitLabProcessorRepository;
import com.publicissapient.kpidashboard.gitlab.repository.GitLabRepoRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * GitLabProcessorJobExecutor represents a class which holds all the
 * configuration and BitBucket execution process.
 *
 * @see GitLabProcessor
 */
@Slf4j
@Component
public class GitLabProcessorJobExecutor extends ProcessorJobExecutor<GitLabProcessor> {

	private final ProcessorToolConnectionService processorToolConnectionService;
	private GitLabConfig gitLabConfig;
	private ProcessorItemRepository<ProcessorItem> processorItemRepository;
	private GitLabRepoRepository gitLabRepository;
	private ProjectToolConfigRepository toolConfigRepository;
	private ConnectionRepository connectionsRepository;
	private CommitRepository commitRepository;
	private GitLabProcessorRepository gitLabProcessorRepository;
	private GitLabClient gitLabClient;
	private MergeRequestRepository mergReqRepo;

	private ProjectBasicConfigRepository projectConfigRepository;

	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	/**
	 * Instantiates a new Git lab processor job executor.
	 *
	 * @param scheduler
	 *            the scheduler
	 * @param gitLabProcessorRepository
	 *            the git lab processor repository
	 * @param gitLabConfig
	 *            the git lab config
	 * @param toolConfigRepository
	 *            the tool config repository
	 * @param gitLabRepository
	 *            the git lab repository
	 * @param gitLabClient
	 *            the git lab client
	 * @param processorItemRepository
	 *            the processor item repository
	 * @param commitsRepo
	 *            the commits repo
	 * @param connectionsRepository
	 *            connections Repository
	 * @param processorToolConnectionService
	 *            processorToolConnectionService
	 */
	@Autowired
	public GitLabProcessorJobExecutor(TaskScheduler scheduler, GitLabProcessorRepository gitLabProcessorRepository, // NOSONAR
			GitLabConfig gitLabConfig, ProjectToolConfigRepository toolConfigRepository,
			ConnectionRepository connectionsRepository, GitLabRepoRepository gitLabRepository,
			GitLabClient gitLabClient, ProcessorItemRepository<ProcessorItem> processorItemRepository,
			CommitRepository commitsRepo, ProcessorToolConnectionService processorToolConnectionService,
			MergeRequestRepository mergReqRepo, ProjectBasicConfigRepository projectConfigRepository,
			ProcessorExecutionTraceLogService processorExecutionTraceLogService,
			ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository) {
		super(scheduler, ProcessorConstants.GITLAB);
		this.gitLabProcessorRepository = gitLabProcessorRepository;
		this.gitLabConfig = gitLabConfig;
		this.toolConfigRepository = toolConfigRepository;
		this.gitLabRepository = gitLabRepository;
		this.gitLabClient = gitLabClient;
		this.processorItemRepository = processorItemRepository;
		this.connectionsRepository = connectionsRepository;
		this.commitRepository = commitsRepo;
		this.processorToolConnectionService = processorToolConnectionService;
		this.mergReqRepo = mergReqRepo;
		this.projectConfigRepository = projectConfigRepository;
		this.processorExecutionTraceLogService = processorExecutionTraceLogService;
		this.processorExecutionTraceLogRepository = processorExecutionTraceLogRepository;
	}

	private static void setLastUpdatedCommitAndTimeStamp(GitLabRepo gitRepo, List<CommitDetails> commitDetailList) {
		if (!commitDetailList.isEmpty()) {
			gitRepo.setLastUpdatedCommit(commitDetailList.get(0).getRevisionNumber());
			gitRepo.setLastCommitTimestamp(Long.toString(commitDetailList.get(0).getCommitTimestamp()));
		}
	}

	private static void setLastCommitTime(ProjectBasicConfig proBasicConfig, GitLabRepo gitRepo,
			ProcessorExecutionTraceLog processorExecutionTraceLog) {
		if (proBasicConfig.isSaveAssigneeDetails() && !processorExecutionTraceLog.isLastEnableAssigneeToggleState()) {
			gitRepo.setLastCommitTimestamp(null);
		}
	}

	/**
	 * Gets the cron.
	 *
	 * @return the cron
	 */
	@Override
	public String getCron() {
		return gitLabConfig.getCron();
	}

	/**
	 * Adds the processor items.
	 *
	 * @param processor
	 *            the processor
	 */

	private void addProcessorItems(Processor processor) {
		List<ProjectToolConfig> tools = getToolConfigList();

		List<ProcessorItem> tobeSavedItems = new ArrayList<>(0);
		List<ObjectId> processorIds = new ArrayList<>(0);
		processorIds.add(processor.getId());
		List<ProcessorItem> processorItems = processorItemRepository.findByProcessorIdIn(processorIds);

		tools.forEach(tool -> {
			boolean isExists = isProcessorItemExist(tool, processorItems);
			if (!isExists) {
				tobeSavedItems.add(createProcessorItem(tool, processor.getId()));
			}
		});

		if (!tobeSavedItems.isEmpty()) {
			processorItemRepository.saveAll(tobeSavedItems);
		}
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
	private ProcessorItem createProcessorItem(ProjectToolConfig tool, ObjectId processorId) {

		Optional<Connection> connectionData = connectionsRepository.findById(tool.getConnectionId());

		ProcessorItem item = new ProcessorItem();

		item.setVersion((short) 2);
		item.setToolConfigId(tool.getId());
		item.setProcessorId(processorId);
		item.setActive(Boolean.TRUE);
		if (connectionData.isPresent()) {
			item.getToolDetailsMap().put(GitLabConstants.URL, connectionData.get().getBaseUrl());
			item.getToolDetailsMap().put(GitLabConstants.GITLAB_API, connectionData.get().getApiEndPoint());
			item.getToolDetailsMap().put(GitLabConstants.GITLAB_ACCESS_TOKEN, connectionData.get().getAccessToken());
		}
		item.getToolDetailsMap().put(GitLabConstants.TOOL_BRANCH, tool.getBranch());
		item.getToolDetailsMap().put(GitLabConstants.SCM, tool.getToolName());
		item.getToolDetailsMap().put(GitLabConstants.GIT_LAB_PROJECT_ID, tool.getProjectId());

		return item;
	}

	/**
	 * Checks if is processor item exist.
	 *
	 * @param tool
	 *            the tool
	 * @param processorItems
	 *            the processor items
	 * @return true, if is processor item exist
	 */
	private boolean isProcessorItemExist(ProjectToolConfig tool, List<ProcessorItem> processorItems) {
		boolean itemExists = false;
		Optional<Connection> connectionData = connectionsRepository.findById(tool.getConnectionId());
		for (ProcessorItem processorItem : processorItems) {
			String pattern = null;
			StringBuilder sb = new StringBuilder();
			Map<String, Object> options = processorItem.getToolDetailsMap();
			if (options.containsKey(GitLabConstants.SCM) && options.containsKey(GitLabConstants.URL)
					&& options.containsKey(GitLabConstants.TOOL_BRANCH)) {
				sb.append(options.get(GitLabConstants.SCM)).append(options.get(GitLabConstants.URL))
						.append(options.get(GitLabConstants.TOOL_BRANCH));
				pattern = sb.toString();
			}

			String match = new StringBuilder(tool.getToolName())
					.append(connectionData.isPresent() ? connectionData.get().getBaseUrl() : StringUtils.EMPTY)
					.append(tool.getBranch()).toString();
			if (match.equalsIgnoreCase(pattern) && tool.getId().equals(processorItem.getToolConfigId())) {
				itemExists = true;
				break;
			}
		}

		return itemExists;
	}

	/**
	 * Checks if is new commitDetails.
	 *
	 * @param gitRepo
	 *            the GitLab repo
	 * @return true, if is new commit
	 */
	private boolean isNewCommit(GitLabRepo gitRepo, CommitDetails commitDetails) {
		CommitDetails dbCommit = commitRepository.findByProcessorItemIdAndRevisionNumber(gitRepo.getId(),
				commitDetails.getRevisionNumber());
		return dbCommit == null;
	}

	private boolean isNewMergeReq(GitLabRepo gitRepo, MergeRequests mergeRequests) {
		MergeRequests mergReq = mergReqRepo.findByProcessorItemIdAndRevisionNumber(gitRepo.getId(),
				mergeRequests.getRevisionNumber());
		return mergReq == null;
	}

	private ProcessorExecutionTraceLog createTraceLog(String basicProjectConfigId) {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.GITLAB);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		Optional<ProcessorExecutionTraceLog> existingTraceLogOptional = processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.GITLAB, basicProjectConfigId);
		existingTraceLogOptional.ifPresent(
				existingProcessorExecutionTraceLog -> processorExecutionTraceLog.setLastEnableAssigneeToggleState(
						existingProcessorExecutionTraceLog.isLastEnableAssigneeToggleState()));
		return processorExecutionTraceLog;
	}

	/**
	 * Gets the active repos.
	 *
	 * @param processor
	 *            the processor
	 * @return the active repos
	 */
	private List<GitLabRepo> getActiveRepos(Processor processor) {
		return gitLabRepository.findActiveRepos(processor.getId());
	}

	/**
	 * Gets the tool config list.
	 *
	 * @return the tool config list
	 */
	private List<ProjectToolConfig> getToolConfigList() {
		return toolConfigRepository.findByToolName(GitLabConstants.TOOL_GITLAB);

	}

	/**
	 * Execute.
	 *
	 * @param processor
	 *            the processor
	 */
	@Override
	public boolean execute(GitLabProcessor processor) {
		boolean executionStatus = true;
		String uid = UUID.randomUUID().toString();
		MDC.put("GitLabProcessorJobExecutorUid", uid);

		long gitLabProcessorStartTime = System.currentTimeMillis();
		MDC.put("GitLabProcessorJobExecutorStartTime", String.valueOf(gitLabProcessorStartTime));

		List<ProjectBasicConfig> projectConfigList = getSelectedProjects();
		clearSelectedBasicProjectConfigIds();

		addProcessorItems(processor);
		int reposCount = 0;
		int commitsCount = 0;
		int mergReqCount = 0;

		List<GitLabRepo> gitLabRepos = getActiveRepos(processor);

		MDC.put("TotalSelectedProjectsForProcessing", String.valueOf(projectConfigList.size()));
		for (ProjectBasicConfig proBasicConfig : projectConfigList) {
			List<ProcessorToolConnection> gitLabDetails = processorToolConnectionService
					.findByToolAndBasicProjectConfigId(ProcessorConstants.GITLAB, proBasicConfig.getId());
			MDC.put("GitLabReposSize", String.valueOf(gitLabRepos.size()));
			for (GitLabRepo gitRepo : gitLabRepos) {
				for (ProcessorToolConnection entry : gitLabDetails) {
					ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(
							proBasicConfig.getId().toHexString());
					try {
						processorExecutionTraceLog.setExecutionStartedAt(System.currentTimeMillis());
						if (gitRepo.getToolConfigId().equals(entry.getId())) {
							setLastCommitTime(proBasicConfig, gitRepo, processorExecutionTraceLog);
							MDC.put("GitLabReposDataCollectionStarted",
									"GitLab Processor started collecting data for Url: " + entry.getUrl()
											+ " and branch : " + entry.getBranch());

							List<CommitDetails> commitDetailList = gitLabClient.fetchAllCommits(gitRepo, entry,
									proBasicConfig);
							updateAssigneeNameForCommit(proBasicConfig, gitRepo, processorExecutionTraceLog,
									commitDetailList);
							List<CommitDetails> unsavedCommits = commitDetailList.stream()
									.filter(commit -> isNewCommit(gitRepo, commit)).collect(Collectors.toList());
							unsavedCommits.forEach(commit -> commit.setProcessorItemId(gitRepo.getId()));
							commitRepository.saveAll(unsavedCommits);
							commitsCount += unsavedCommits.size();

							setLastUpdatedCommitAndTimeStamp(gitRepo, commitDetailList);
							List<MergeRequests> mergeRequestsList = gitLabClient.fetchAllMergeRequest(gitRepo, entry,
									proBasicConfig);
							updateAssigneeNameForMerge(proBasicConfig, gitRepo, processorExecutionTraceLog,
									mergeRequestsList);
							List<MergeRequests> unsavedMergeRequests = mergeRequestsList.stream()
									.filter(mergReq -> isNewMergeReq(gitRepo, mergReq)).collect(Collectors.toList());
							unsavedMergeRequests.forEach(mergReq -> mergReq.setProcessorItemId(gitRepo.getId()));
							mergReqRepo.saveAll(unsavedMergeRequests);
							mergReqCount += unsavedMergeRequests.size();
							gitRepo.setLastUpdatedTime(Calendar.getInstance().getTime());
							gitLabRepository.save(gitRepo);
							MDC.put("GitLabReposDataCollectionCompleted", "GitLab Processor collected data for Url: "
									+ entry.getUrl() + " and branch : " + entry.getBranch());
							reposCount++;
							processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
							processorExecutionTraceLog.setExecutionSuccess(true);
							processorExecutionTraceLog
									.setLastEnableAssigneeToggleState(proBasicConfig.isSaveAssigneeDetails());
							processorExecutionTraceLogService.save(processorExecutionTraceLog);
						}
					} catch (FetchingCommitException exception) {
						executionStatus = false;
						processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
						processorExecutionTraceLog.setExecutionSuccess(executionStatus);
						processorExecutionTraceLog.setLastEnableAssigneeToggleState(false);
						processorExecutionTraceLogService.save(processorExecutionTraceLog);
						log.error(String.format("Error in processing %s", gitRepo.getRepoUrl()), exception);
					}

				}
			}
		}

		MDC.put("RepoCount", String.valueOf(reposCount));
		MDC.put("CommitCount", String.valueOf(commitsCount));

		cache(commitsCount, mergReqCount);
		long gitLabProcessorEndTime = System.currentTimeMillis();
		MDC.put("GitLabProcessorJobExecutorEndTime", String.valueOf(gitLabProcessorEndTime));

		MDC.put("TotalGitLabProcessorJobExecutorTime",
				String.valueOf(gitLabProcessorEndTime - gitLabProcessorStartTime));
		log.info("GitLab processor execution finished at {}", gitLabProcessorEndTime);
		MDC.put("executionStatus", String.valueOf(executionStatus));
		MDC.clear();
		return executionStatus;
	}

	@Override
	public boolean executeSprint(String sprintId) {
		return false;
	}

	private void cache(int commitsCount, int mergReqCount) {
		if (commitsCount > 0) {
			cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.BITBUCKET_KPI_CACHE);
		}
		if (mergReqCount > 0) {
			cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.GITLAB_KPI_CACHE);
		}
	}

	private void updateAssigneeNameForMerge(ProjectBasicConfig proBasicConfig, GitLabRepo gitRepo,
			ProcessorExecutionTraceLog processorExecutionTraceLog, List<MergeRequests> mergeRequestsList) {
		if (proBasicConfig.isSaveAssigneeDetails() && !processorExecutionTraceLog.isLastEnableAssigneeToggleState()) {
			List<MergeRequests> updateAuthor = new ArrayList<>();
			mergeRequestsList.forEach(mergeRequests -> {
				MergeRequests dbMerge = mergReqRepo.findByProcessorItemIdAndRevisionNumber(gitRepo.getId(),
						mergeRequests.getRevisionNumber());
				if (dbMerge != null) {
					dbMerge.setAuthor(mergeRequests.getAuthor());
					updateAuthor.add(dbMerge);
				}
			});
			mergReqRepo.saveAll(updateAuthor);
		}
	}

	private void updateAssigneeNameForCommit(ProjectBasicConfig proBasicConfig, GitLabRepo gitRepo,
			ProcessorExecutionTraceLog processorExecutionTraceLog, List<CommitDetails> commitDetailList) {
		if (proBasicConfig.isSaveAssigneeDetails() && !processorExecutionTraceLog.isLastEnableAssigneeToggleState()) {
			List<CommitDetails> updateAuthor = new ArrayList<>();
			commitDetailList.stream().forEach(commitDetails -> {
				CommitDetails dbCommit = commitRepository.findByProcessorItemIdAndRevisionNumber(gitRepo.getId(),
						commitDetails.getRevisionNumber());
				if (dbCommit != null) {
					dbCommit.setAuthor(commitDetails.getAuthor());
					updateAuthor.add(dbCommit);
				}
			});
			commitRepository.saveAll(updateAuthor);
		}
	}

	/**
	 * Gets the processor.
	 *
	 * @return the processor
	 */
	@Override
	public GitLabProcessor getProcessor() {
		return GitLabProcessor.prototype();
	}

	/**
	 * Gets the processor repository.
	 *
	 * @return the processor repository
	 */
	@Override
	public ProcessorRepository<GitLabProcessor> getProcessorRepository() {
		return gitLabProcessorRepository;
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

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(gitLabConfig.getCustomApiBaseUrl());
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
			log.error("[GITLAB-CUSTOMAPI-CACHE-EVICT]. Error while consuming rest service {} ", e);
		}

		if (null != response && response.getStatusCode().is2xxSuccessful()) {
			log.info("[GITLAB-CUSTOMAPI-CACHE-EVICT]. Successfully evicted cache: {} ", cacheName);
		} else {
			log.error("[GITLAB-CUSTOMAPI-CACHE-EVICT]. Error while evicting  cache: {}", cacheName);
		}

		clearToolItemCache(gitLabConfig.getCustomApiBaseUrl());
	}

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
