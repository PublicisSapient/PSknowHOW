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

package com.publicissapient.kpidashboard.azurerepo.processor;

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

import com.publicissapient.kpidashboard.azurerepo.config.AzureRepoConfig;
import com.publicissapient.kpidashboard.azurerepo.constants.AzureRepoConstants;
import com.publicissapient.kpidashboard.azurerepo.customexception.FetchingCommitException;
import com.publicissapient.kpidashboard.azurerepo.model.AzureRepoModel;
import com.publicissapient.kpidashboard.azurerepo.model.AzureRepoProcessor;
import com.publicissapient.kpidashboard.azurerepo.processor.service.AzureRepoClient;
import com.publicissapient.kpidashboard.azurerepo.repository.AzureRepoProcessorRepository;
import com.publicissapient.kpidashboard.azurerepo.repository.AzureRepoRepository;
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

import lombok.extern.slf4j.Slf4j;

/**
 * AzureRepoProcessorJobExecutor represents a class which holds all the
 * configuration and Azure Repo execution process.
 * 
 * @see AzureRepoProcessor
 */
@Slf4j
@Component
public class AzureRepoProcessorJobExecutor extends ProcessorJobExecutor<AzureRepoProcessor> {

	private final AzureRepoProcessorRepository azureRepoProcessorRepo;

	private final AzureRepoConfig azureRepoConfig;

	private final ProjectToolConfigRepository toolConfigRepository;

	private final AzureRepoRepository azureRepoRepository;

	private final AzureRepoClient azureRepoClient;

	private final ProcessorItemRepository<ProcessorItem> processorItemRepository;

	private final CommitRepository commitsRepo;
	private final ConnectionRepository connectionsRepository;
	private final ProcessorToolConnectionService processorToolConnectionService;
	private final ProjectBasicConfigRepository projectConfigRepository;
	boolean executionStatus = true;
	private MergeRequestRepository mergReqRepo;
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@Autowired
	protected AzureRepoProcessorJobExecutor(TaskScheduler taskScheduler, // NOSONAR
			AzureRepoProcessorRepository azureRepoProcessorRepo, AzureRepoConfig azureRepoConfig,
			ProjectToolConfigRepository toolConfigRepository, AzureRepoRepository azureRepoRepository,
			AzureRepoClient azureRepoClient, ProcessorItemRepository<ProcessorItem> processorItemRepository,
			CommitRepository commitsRepo, ConnectionRepository connectionsRepository,
			ProcessorToolConnectionService processorToolConnectionService,
			ProjectBasicConfigRepository projectConfigRepository, MergeRequestRepository mergReqRepo,
			ProcessorExecutionTraceLogService processorExecutionTraceLogService,
			ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository) {
		super(taskScheduler, ProcessorConstants.AZUREREPO);
		this.azureRepoProcessorRepo = azureRepoProcessorRepo;
		this.azureRepoConfig = azureRepoConfig;
		this.toolConfigRepository = toolConfigRepository;
		this.azureRepoRepository = azureRepoRepository;
		this.azureRepoClient = azureRepoClient;
		this.processorItemRepository = processorItemRepository;
		this.commitsRepo = commitsRepo;
		this.connectionsRepository = connectionsRepository;
		this.processorToolConnectionService = processorToolConnectionService;
		this.projectConfigRepository = projectConfigRepository;
		this.processorExecutionTraceLogService = processorExecutionTraceLogService;
		this.mergReqRepo = mergReqRepo;
		this.processorExecutionTraceLogRepository = processorExecutionTraceLogRepository;
	}

	/**
	 * Gets the cron.
	 *
	 * @return the cron
	 */
	@Override
	public String getCron() {
		return azureRepoConfig.getCron();
	}

	/**
	 * Adds the processor items.
	 *
	 * @param processor
	 *            the processor
	 * @param tools
	 *            the processor
	 */
	private void addProcessorItems(Processor processor, List<ProjectToolConfig> tools) {

		List<ObjectId> processorIds = new ArrayList<>(0);
		processorIds.add(processor.getId());
		List<ProcessorItem> processorItems = processorItemRepository.findByProcessorIdIn(processorIds);
		List<ProcessorItem> tobeSavedItems = new ArrayList<>(0);

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
		ProcessorItem item = new ProcessorItem();
		Optional<Connection> connection = connectionsRepository.findById(tool.getConnectionId());
		item.setVersion((short) 2);
		item.setToolConfigId(tool.getId());
		item.setProcessorId(processorId);
		item.setActive(Boolean.TRUE);

		item.getToolDetailsMap().put(AzureRepoConstants.URL,
				connection.isPresent() ? connection.get().getBaseUrl() : StringUtils.EMPTY);
		item.getToolDetailsMap().put(AzureRepoConstants.TOOL_BRANCH, tool.getBranch());
		item.getToolDetailsMap().put(AzureRepoConstants.SCM, tool.getToolName());
		item.getToolDetailsMap().put(AzureRepoConstants.REPOSITORY_NAME, tool.getRepoSlug());
		item.getToolDetailsMap().put(AzureRepoConstants.API_VERSION, tool.getApiVersion());

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
		for (ProcessorItem processorItem : processorItems) {
			String pattern = null;
			StringBuilder sb = new StringBuilder();
			Map<String, Object> options = processorItem.getToolDetailsMap();
			if (options.containsKey(AzureRepoConstants.SCM) && options.containsKey(AzureRepoConstants.URL)
					&& options.containsKey(AzureRepoConstants.TOOL_BRANCH)
					&& options.containsKey(AzureRepoConstants.REPOSITORY_NAME)) {
				sb.append(options.get(AzureRepoConstants.SCM)).append(options.get(AzureRepoConstants.URL))
						.append(options.get(AzureRepoConstants.TOOL_BRANCH))
						.append(options.get(AzureRepoConstants.REPOSITORY_NAME));
				pattern = sb.toString();
			}
			Optional<Connection> connection = connectionsRepository.findById(tool.getConnectionId());
			String match = new StringBuilder(tool.getToolName())
					.append(connection.isPresent() ? connection.get().getBaseUrl() : StringUtils.EMPTY)
					.append(tool.getBranch()).append(tool.getRepoSlug()).toString();
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
	 * @param azureRepo
	 *            the bit repo
	 * @param commitDetails
	 *            the commitDetails
	 * @return true, if is new commit
	 */
	private boolean isNewCommit(AzureRepoModel azureRepo, CommitDetails commitDetails) {
		CommitDetails dbCommit = commitsRepo.findByProcessorItemIdAndRevisionNumber(azureRepo.getId(),
				commitDetails.getRevisionNumber());
		return dbCommit == null;
	}

	private boolean isNewMergeReq(AzureRepoModel azureRepo, MergeRequests mergeRequests) {
		MergeRequests dbCommit = mergReqRepo.findByProcessorItemIdAndRevisionNumber(azureRepo.getId(),
				mergeRequests.getRevisionNumber());
		return dbCommit == null;
	}

	/**
	 * Gets the active repos.
	 *
	 * @param processor
	 *            the processor
	 * @return the active repos
	 */
	private List<AzureRepoModel> getActiveRepos(Processor processor) {
		return azureRepoRepository.findActiveRepos(processor.getId());
	}

	/**
	 * Gets the tool config list.
	 *
	 * @return the tool config list
	 */
	private List<ProjectToolConfig> getToolConfigList() {
		return toolConfigRepository.findByToolName(AzureRepoConstants.TOOL_AZUREREPO);
	}

	/**
	 * Execute.
	 *
	 * @param processor
	 *            the processor
	 */
	@Override
	public boolean execute(AzureRepoProcessor processor) {
		executionStatus = true;
		String uid = UUID.randomUUID().toString();
		MDC.put("AzureRepoProcessorJobExecutorUid", uid);

		long azureRepoProcessorStartTime = System.currentTimeMillis();
		MDC.put("AzureRepoProcessorJobExecutorStartTime", String.valueOf(azureRepoProcessorStartTime));
		List<ProjectToolConfig> tools = getToolConfigList();
		addProcessorItems(processor, tools);
		int reposCount = 0;
		int commitsCount = 0;
		int mergReqCount = 0;
		cleanUnusedProcessorItem(tools, processor);

		List<ProjectBasicConfig> projectConfigList = getSelectedProjects();
		MDC.put("TotalSelectedProjectsForProcessing", String.valueOf(projectConfigList.size()));
		clearSelectedBasicProjectConfigIds();

		List<AzureRepoModel> azurerepoRepos = getActiveRepos(processor);
		MDC.put("AzurerepoReposSize", String.valueOf(azurerepoRepos.size()));

		for (ProjectBasicConfig proBasicConfig : projectConfigList) {
			log.info("Fetching data for project : {}", proBasicConfig.getProjectName());
			List<ProcessorToolConnection> azureRepoInfo = processorToolConnectionService
					.findByToolAndBasicProjectConfigId(ProcessorConstants.AZUREREPO, proBasicConfig.getId());
			ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(
					proBasicConfig.getId().toHexString());
			try {
				if (CollectionUtils.isNotEmpty(azureRepoInfo)) {
					processorExecutionTraceLog.setExecutionStartedAt(System.currentTimeMillis());
					MDC.put("ProjectDataStartTime", String.valueOf(System.currentTimeMillis()));
					commitsCount = processRepoData(azurerepoRepos, azureRepoInfo, reposCount, proBasicConfig);
					mergReqCount = processMergeRequestData(azurerepoRepos, azureRepoInfo, reposCount, proBasicConfig);
					MDC.put("ProjectDataEndTime", String.valueOf(System.currentTimeMillis()));
					processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
					processorExecutionTraceLog.setExecutionSuccess(true);
					processorExecutionTraceLog.setLastEnableAssigneeToggleState(proBasicConfig.isSaveAssigneeDetails());
					processorExecutionTraceLogService.save(processorExecutionTraceLog);
				}
			} catch (Exception exception) {
				executionStatus = false;
				processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
				processorExecutionTraceLog.setExecutionSuccess(executionStatus);
				processorExecutionTraceLog.setLastEnableAssigneeToggleState(false);
				processorExecutionTraceLogService.save(processorExecutionTraceLog);
				log.error("Error while processing", exception);
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
		long azureRepoProcessorEndTime = System.currentTimeMillis();
		MDC.put("AzureRepoProcessorJobExecutorEndTime", String.valueOf(azureRepoProcessorEndTime));

		MDC.put("TotalAzureRepoProcessorJobExecutorTime",
				String.valueOf(azureRepoProcessorEndTime - azureRepoProcessorStartTime));
		log.info("Azurerepo processor execution finished at {}", azureRepoProcessorEndTime);
		MDC.put("executionStatus", String.valueOf(executionStatus));
		MDC.clear();
		return executionStatus;
	}

	@Override
	public boolean executeSprint(String sprintId) {
		return false;
	}

	/**
	 * 
	 * processRepoData
	 * 
	 * @param azurerepoRepos
	 *            azurerepoRepos
	 * @param azureRepoInfo
	 *            azureRepoInfo
	 * @param reposCount
	 *            reposCount
	 * @param projectBasicConfig
	 * @return executionStatus
	 */
	private int processRepoData(List<AzureRepoModel> azurerepoRepos, List<ProcessorToolConnection> azureRepoInfo,
			int reposCount, ProjectBasicConfig projectBasicConfig) {
		int commitsCount = 0;
		for (AzureRepoModel azureRepo : azurerepoRepos) {
			for (ProcessorToolConnection entry : azureRepoInfo) {
				ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
				try {
					if (azureRepo.getToolConfigId().equals(entry.getId())) {
						boolean firstTimeRun = (azureRepo.getLastUpdatedCommit() == null);
						if (projectBasicConfig.isSaveAssigneeDetails()
								&& !processorExecutionTraceLog.isLastEnableAssigneeToggleState()) {
							azureRepo.setLastUpdatedTime(null);
						}
						MDC.put("AzurerepoReposDataCollectionStarted",
								"Azurerepo Processor started collecting data for Url: " + entry.getUrl()
										+ " and branch : " + entry.getBranch());

						List<CommitDetails> commitDetailList = azureRepoClient.fetchAllCommits(azureRepo, firstTimeRun,
								entry, projectBasicConfig);
						if (projectBasicConfig.isSaveAssigneeDetails()
								&& !processorExecutionTraceLog.isLastEnableAssigneeToggleState()) {
							List<CommitDetails> updateAuthor = new ArrayList<>();
							commitDetailList.stream().forEach(commitDetails -> {
								CommitDetails dbCommit = commitsRepo.findByProcessorItemIdAndRevisionNumber(
										azureRepo.getId(), commitDetails.getRevisionNumber());
								if (dbCommit != null) {
									dbCommit.setAuthor(commitDetails.getAuthor());
									updateAuthor.add(dbCommit);
								}
							});
							commitsRepo.saveAll(updateAuthor);
						}
						List<CommitDetails> unsavedCommits = commitDetailList.stream()
								.filter(commit -> isNewCommit(azureRepo, commit)).collect(Collectors.toList());
						unsavedCommits.forEach(commit -> commit.setProcessorItemId(azureRepo.getId()));
						commitsRepo.saveAll(unsavedCommits);
						commitsCount += unsavedCommits.size();

						azureRepo.setLastUpdatedTime(Calendar.getInstance().getTime());
						if (!commitDetailList.isEmpty()) {
							azureRepo.setLastUpdatedCommit(commitDetailList.get(0).getRevisionNumber());
						}
						azureRepoRepository.save(azureRepo);
						MDC.put("AzurereppoReposDataCollectionCompleted", "Azurerepo Processor collected data for Url: "
								+ entry.getUrl() + " and branch : " + entry.getBranch());
						reposCount++;
					}
				} catch (FetchingCommitException exception) {
					log.error(String.format("Error in processing %s", entry.getUrl()), exception);
					executionStatus = false;
				}

			}
		}
		return commitsCount;

	}

	private int processMergeRequestData(List<AzureRepoModel> azurerepoRepos,
			List<ProcessorToolConnection> azureRepoInfo, int reposCount, ProjectBasicConfig proBasicConfig) {

		int mergReqCount = 0;
		for (AzureRepoModel azureRepo : azurerepoRepos) {
			for (ProcessorToolConnection entry : azureRepoInfo) {
				ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
				try {
					if (azureRepo.getToolConfigId().equals(entry.getId())) {
						boolean firstTimeRun = (azureRepo.getLastUpdatedCommit() == null);
						if (proBasicConfig.isSaveAssigneeDetails()
								&& !processorExecutionTraceLog.isLastEnableAssigneeToggleState()) {
							azureRepo.setLastUpdatedTime(null);
						}
						MDC.put("AzurerepoReposDataCollectionStarted",
								"Azurerepo Processor started collecting data for Url: " + entry.getUrl()
										+ " and branch : " + entry.getBranch());

						List<MergeRequests> mergeRequestsList = azureRepoClient.fetchAllMergeRequest(azureRepo,
								firstTimeRun, entry, proBasicConfig);
						if (proBasicConfig.isSaveAssigneeDetails()
								&& !processorExecutionTraceLog.isLastEnableAssigneeToggleState()) {
							List<MergeRequests> updateAuthor = new ArrayList<>();
							mergeRequestsList.forEach(mergeRequests -> {
								MergeRequests dbMerge = mergReqRepo.findByProcessorItemIdAndRevisionNumber(
										azureRepo.getId(), mergeRequests.getRevisionNumber());
								if (dbMerge != null) {
									dbMerge.setAuthor(mergeRequests.getAuthor());
									updateAuthor.add(dbMerge);
								}
							});
							mergReqRepo.saveAll(updateAuthor);
						}
						List<MergeRequests> unsavedMergeRequests = mergeRequestsList.stream()
								.filter(mergReq -> isNewMergeReq(azureRepo, mergReq)).collect(Collectors.toList());
						unsavedMergeRequests.forEach(mergReq -> mergReq.setProcessorItemId(azureRepo.getId()));
						mergReqRepo.saveAll(unsavedMergeRequests);
						mergReqCount += unsavedMergeRequests.size();
						azureRepo.setLastUpdatedTime(Calendar.getInstance().getTime());
						azureRepoRepository.save(azureRepo);
						MDC.put("AzurereppoReposDataCollectionCompleted", "Azurerepo Processor collected data for Url: "
								+ entry.getUrl() + " and branch : " + entry.getBranch());
						reposCount++;
					}
				} catch (FetchingCommitException exception) {
					log.error(String.format("Error in processing %s", entry.getUrl()), exception);
					executionStatus = false;
				}

			}
		}
		return mergReqCount;

	}

	/**
	 * Gets the processor.
	 *
	 * @return the processor
	 */
	@Override
	public AzureRepoProcessor getProcessor() {
		return AzureRepoProcessor.prototype();
	}

	private ProcessorExecutionTraceLog createTraceLog(String basicProjectConfigId) {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.AZUREREPO);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		Optional<ProcessorExecutionTraceLog> existingTraceLogOptional = processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.AZUREREPO, basicProjectConfigId);
		existingTraceLogOptional.ifPresent(existingProcessorExecutionTraceLog -> {
			processorExecutionTraceLog.setLastEnableAssigneeToggleState(
					existingProcessorExecutionTraceLog.isLastEnableAssigneeToggleState());
		});
		return processorExecutionTraceLog;
	}

	/**
	 * Gets the processor repository.
	 *
	 * @return the processor repository
	 */
	@Override
	public ProcessorRepository<AzureRepoProcessor> getProcessorRepository() {
		return azureRepoProcessorRepo;
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

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(azureRepoConfig.getCustomApiBaseUrl());
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
			log.error("[AZURE REPO-CUSTOMAPI-CACHE-EVICT]. Error while consuming rest service {}", e);
		}

		if (null != response && response.getStatusCode().is2xxSuccessful()) {
			log.info("[AZURE REPO-CUSTOMAPI-CACHE-EVICT]. Successfully evicted cache: {} ", cacheName);
		} else {
			log.error("[AZURE REPO-CUSTOMAPI-CACHE-EVICT]. Error while evicting cache: {}", cacheName);
		}

		clearToolItemCache(azureRepoConfig.getCustomApiBaseUrl());
	}

	/**
	 *
	 * @param tools
	 * @param processor
	 */
	private void cleanUnusedProcessorItem(List<ProjectToolConfig> tools, AzureRepoProcessor processor) {
		List<AzureRepoModel> azureRepoModels = getActiveRepos(processor);
		if (CollectionUtils.isEmpty(tools)) {
			CollectionUtils.emptyIfNull(azureRepoModels)
					.forEach(item -> azureRepoProcessorRepo.deleteById(item.getId()));
		} else {
			CollectionUtils.emptyIfNull(azureRepoModels).stream().forEach(item -> {
				boolean itemExists = tools.stream().anyMatch(t -> item.getProcessorId().equals(t.getId()));
				if (!itemExists) {
					azureRepoProcessorRepo.deleteById(item.getId());
				}
			});
		}
	}

	/**
	 * Return List of selected ProjectBasicConfig id if null then return all
	 * ProjectBasicConfig ids
	 * 
	 * @return List of ProjectBasicConfig
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