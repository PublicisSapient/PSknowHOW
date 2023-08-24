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

package com.publicissapient.kpidashboard.azurepipeline.processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

import com.publicissapient.kpidashboard.azurepipeline.config.AzurePipelineConfig;
import com.publicissapient.kpidashboard.azurepipeline.factory.AzurePipelineFactory;
import com.publicissapient.kpidashboard.azurepipeline.model.AzurePipelineProcessor;
import com.publicissapient.kpidashboard.azurepipeline.processor.adapter.AzurePipelineClient;
import com.publicissapient.kpidashboard.azurepipeline.repository.AzurePipelineProcessorRepository;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.executor.ProcessorJobExecutor;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.repository.application.DeploymentRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * ProcessorJobExecutor that fetches Build log information from AzurePipeline.
 */

@Component
@Slf4j
public class AzurePipelineProcessorJobExecutor extends ProcessorJobExecutor<AzurePipelineProcessor> {

	private static final String PROCESSOR_EXECUTION_UID = "processorExecutionUid";
	private static final String PROCESSOR_START_TIME = "processorStartTime";
	private static final String INSTANCE_URL = "instanceUrl";
	private static final String TOTAL_UPDATED_COUNT = "totalUpdatedCount";
	private static final String PROCESSOR_END_TIME = "processorEndTime";
	private static final String EXECUTION_TIME = "executionTime";
	private static final String EXECUTION_STATUS = "executionStatus";
	private static final String BUILD = "build";
	@Autowired
	AesEncryptionService aesEncryptionService;
	@Autowired
	private AzurePipelineProcessorRepository azurePipelineProcessorRepository;
	@Autowired
	private ProjectBasicConfigRepository projectConfigRepository;
	@Autowired
	private BuildRepository buildRepository;
	@Autowired
	private DeploymentRepository deploymentRepository;
	@Autowired
	private AzurePipelineConfig azurePipelineConfig;
	@Autowired
	private AzurePipelineClient azurePipelineClient;
	@Autowired
	private AzurePipelineFactory azurePipelineFactory;
	@Autowired
	private ProcessorToolConnectionService processorToolConnectionService;

	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	/**
	 * Provides AzurePipeline TaskScheduler.
	 * 
	 * @param taskScheduler
	 *            the task scheduler
	 */
	@Autowired
	public AzurePipelineProcessorJobExecutor(TaskScheduler taskScheduler) {
		super(taskScheduler, ProcessorConstants.AZUREPIPELINE);

	}

	/**
	 * Provides Processor.
	 * 
	 * @return the AzurePipelineProcessor
	 */
	@Override
	public AzurePipelineProcessor getProcessor() {
		return AzurePipelineProcessor.buildProcessor();
	}

	/**
	 * Provides Processor Repository.
	 * 
	 * @return the ProcessorRepository
	 *
	 */
	@Override
	public ProcessorRepository<AzurePipelineProcessor> getProcessorRepository() {
		return azurePipelineProcessorRepository;
	}

	/**
	 * Provides cron expression.
	 * 
	 * @return the cron expression
	 */
	@Override
	public String getCron() {
		return azurePipelineConfig.getCron();
	}

	/**
	 * Processes AzurePipeline build data.
	 * 
	 * @param processor
	 *            the azurePipeline processor instance
	 */
	@Override
	public boolean execute(AzurePipelineProcessor processor) {
		boolean executionStatus = true;
		long startTime = System.currentTimeMillis();
		String uid = UUID.randomUUID().toString();
		MDC.put(PROCESSOR_EXECUTION_UID, uid);
		MDC.put(PROCESSOR_START_TIME, String.valueOf(startTime));

		List<ProjectBasicConfig> projectConfigList = getSelectedProjects();
		MDC.put("TotalSelectedProjectsForProcessing", String.valueOf(projectConfigList.size()));
		clearSelectedBasicProjectConfigIds();

		Set<ObjectId> udId = new HashSet<>();
		udId.add(processor.getId());
		List<Deployment> deploymentJobs = deploymentRepository.findByProcessorIdIn(udId);
		List<Deployment> activeDeployJobs = new ArrayList<>();

		int count = 0;
		for (ProjectBasicConfig proBasicConfig : projectConfigList) {
			log.info("Fetching data for project : {}", proBasicConfig.getProjectName());
			List<ProcessorToolConnection> azurePipelineJobList = processorToolConnectionService
					.findByToolAndBasicProjectConfigId(ProcessorConstants.AZUREPIPELINE, proBasicConfig.getId());

			for (ProcessorToolConnection azurePipelineServer : azurePipelineJobList) {

				String instanceUrl = azurePipelineServer.getUrl();
				azurePipelineServer.setPat(decryptKey(azurePipelineServer.getPat()));
				MDC.put(INSTANCE_URL, instanceUrl);
				ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(
						proBasicConfig.getId().toHexString());
				try {
					processorExecutionTraceLog.setExecutionStartedAt(System.currentTimeMillis());
					azurePipelineClient = azurePipelineFactory.getAzurePipelineClient(azurePipelineServer.getJobType());
					long lastStartTimeOfJobs = lastStartTime(proBasicConfig, processorExecutionTraceLog, processor,
							azurePipelineServer, deploymentJobs);
					if (azurePipelineServer.getJobType().equalsIgnoreCase(BUILD)) {
						count = buildJobs(processor, startTime, count, azurePipelineServer, lastStartTimeOfJobs,
								proBasicConfig);
					} else {
						count = deployJobs(processor, startTime, deploymentJobs, activeDeployJobs, azurePipelineServer,
								lastStartTimeOfJobs, proBasicConfig);
					}
					log.info("Finished : {}", startTime);
					processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
					processorExecutionTraceLog.setExecutionSuccess(true);
					processorExecutionTraceLog.setLastEnableAssigneeToggleState(proBasicConfig.isSaveAssigneeDetails());
					processorExecutionTraceLogService.save(processorExecutionTraceLog);

				} catch (RestClientException exception) {
					executionStatus = false;
					processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
					processorExecutionTraceLog.setExecutionSuccess(executionStatus);
					processorExecutionTraceLogService.save(processorExecutionTraceLog);
					log.error(String.format("Error getting jobs for: %s", instanceUrl), exception);
				}

			}
		}
		MDC.put(TOTAL_UPDATED_COUNT, String.valueOf(count));

		if (count > 0) {
			cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.JENKINS_KPI_CACHE);
		}
		long endTime = System.currentTimeMillis();

		MDC.put(PROCESSOR_END_TIME, String.valueOf(endTime));
		MDC.put(EXECUTION_TIME, String.valueOf(endTime - startTime));
		MDC.put(EXECUTION_STATUS, String.valueOf(executionStatus));
		log.info("AzurePipeline Processor execution finished");
		MDC.clear();
		return executionStatus;
	}

	@Override
	public boolean executeSprint(String sprintId) {
		return false;
	}

	private int deployJobs(AzurePipelineProcessor processor, long startTime, List<Deployment> deploymentJobs,
			List<Deployment> activeDeployJobs, ProcessorToolConnection azurePipelineServer, long lastStartTimeOfJobs,
			ProjectBasicConfig proBasicConfig) {

		Map<Deployment, Set<Deployment>> deploymentsByJob = azurePipelineClient.getDeploymentJobs(azurePipelineServer,
				lastStartTimeOfJobs, proBasicConfig);
		log.info("Fetched jobs : {}", startTime);
		activeDeployJobs.addAll(deploymentsByJob.keySet());
		return addNewDeploymentJobs(deploymentsByJob, deploymentJobs, processor, proBasicConfig, azurePipelineServer);
	}

	private int buildJobs(AzurePipelineProcessor processor, long startTime, int count,
			ProcessorToolConnection azurePipelineServer, long lastStartTimeOfJobs, ProjectBasicConfig proBasicConfig) {
		Map<ObjectId, Set<Build>> buildsByJob = azurePipelineClient.getInstanceJobs(azurePipelineServer,
				lastStartTimeOfJobs, proBasicConfig);
		log.info("Fetched jobs : {}", startTime);

		int updatedJobs = addNewBuilds(processor.getId(), azurePipelineServer, buildsByJob, proBasicConfig);
		count += updatedJobs;
		return count;
	}

	/**
	 * Gets the last start time from the list of azurepipeline builds in db
	 * 
	 * @param processor
	 *            the processor
	 * @return lastStartTimeOfBuilds
	 */
	private long getLastStartTimeOfJobs(AzurePipelineProcessor processor, ProcessorToolConnection azurePipelineServer,
			List<Deployment> deploymentJobs) {
		if (azurePipelineServer.getJobType().equalsIgnoreCase("Build")) {
			return getLastStartTimeOfBuilds(azurePipelineServer);
		} else {
			return getLastStartTimeOfReleases(processor, azurePipelineServer, deploymentJobs);
		}

	}

	private long getLastStartTimeOfReleases(AzurePipelineProcessor processor,
			ProcessorToolConnection azurePipelineServer, List<Deployment> deploymentJobs) {
		long lastStartTimeOfReleases = 0;
		for (Deployment releaseJob : deploymentJobs) {
			if (releaseJob.getProcessorId().equals(processor.getId())
					&& releaseJob.getJobName().equals(azurePipelineServer.getJobName())) {
				List<Deployment> releases = deploymentRepository.findByProjectToolConfigIdAndJobName(
						releaseJob.getProjectToolConfigId(), releaseJob.getJobName());
				if (!releases.isEmpty()) {
					try {
						List<Deployment> sortedOnStartDate = releases.stream().sorted((c1, c2) -> DateUtil
								.stringToLocalDateTime(c2.getStartTime(), DateUtil.TIME_FORMAT)
								.compareTo(DateUtil.stringToLocalDateTime(c1.getStartTime(), DateUtil.TIME_FORMAT)))
								.collect(Collectors.toList());
						lastStartTimeOfReleases = Math.max(lastStartTimeOfReleases, DateUtil
								.convertStringToLong(sortedOnStartDate.get(releases.size() - 1).getStartTime()));
					} catch (Exception e) {
						log.error(" error in calculating lastStartTimeOfReleases" + e);
					}
				}
			}
		}
		return lastStartTimeOfReleases;
	}

	private long getLastStartTimeOfBuilds(ProcessorToolConnection azurePipelineServer) {
		long lastStartTimeOfBuilds = 0;
		List<Build> builds = buildRepository.findByProjectToolConfigIdAndBuildJob(azurePipelineServer.getId(),
				azurePipelineServer.getJobName());
		if (!builds.isEmpty()) {
			builds.sort(
					(Build b1, Build b2) -> Long.valueOf(b1.getStartTime()).compareTo(Long.valueOf(b2.getStartTime())));
			lastStartTimeOfBuilds = Math.max(lastStartTimeOfBuilds, builds.get(builds.size() - 1).getStartTime());
		}
		return lastStartTimeOfBuilds;
	}

	private String decryptKey(String encryptedKey) {
		return aesEncryptionService.decrypt(encryptedKey, azurePipelineConfig.getAesEncryptionKey());
	}

	private ProcessorExecutionTraceLog createTraceLog(String basicProjectConfigId) {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.AZUREPIPELINE);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		Optional<ProcessorExecutionTraceLog> existingTraceLogOptional = processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.AZUREPIPELINE, basicProjectConfigId);
		existingTraceLogOptional.ifPresent(
				existingProcessorExecutionTraceLog -> processorExecutionTraceLog.setLastEnableAssigneeToggleState(
						existingProcessorExecutionTraceLog.isLastEnableAssigneeToggleState()));
		return processorExecutionTraceLog;
	}

	/**
	 * Iterates over the build jobs and adds new builds to the database.
	 *
	 * @param processorId
	 * @param azurePipelineServer
	 * @param buildsByJob
	 *            the build by job
	 * @param proBasicConfig
	 * @return adds new build
	 */
	private int addNewBuilds(ObjectId processorId, ProcessorToolConnection azurePipelineServer,
			Map<ObjectId, Set<Build>> buildsByJob, ProjectBasicConfig proBasicConfig) {
		long start = System.currentTimeMillis();
		int count = 0;
		List<Build> buildsToSave = new ArrayList<>();
		// process new builds in the order of their build numbers - this has
		// implication to handling of commits in BuildEventListener
		ArrayList<Build> builds = new ArrayList<>(nullSafe(buildsByJob.get(azurePipelineServer.getId())));
		builds.sort((Build b1, Build b2) -> Long.valueOf(b1.getStartTime()).compareTo(Long.valueOf(b2.getStartTime())));
		for (Build build : builds) {
			Build buildData = buildRepository.findByProjectToolConfigIdAndNumber(azurePipelineServer.getId(),
					build.getNumber());
			if (buildData == null) {
				build.setProcessorId(processorId);
				build.setBasicProjectConfigId(azurePipelineServer.getBasicProjectConfigId());
				build.setProjectToolConfigId(azurePipelineServer.getId());
				build.setBuildJob(azurePipelineServer.getJobName());
				buildsToSave.add(build);
				count++;
			} else {
				if (proBasicConfig.isSaveAssigneeDetails() && buildData.getStartedBy() == null
						&& build.getStartedBy() != null) {
					buildData.setStartedBy(build.getStartedBy());
					buildsToSave.add(buildData);
				}
			}
		}

		if (CollectionUtils.isNotEmpty(buildsToSave)) {
			buildRepository.saveAll(buildsToSave);
		}

		if (log.isInfoEnabled()) {
			log.info("New builds " + start + " " + count);
		}
		return count;
	}

	/**
	 * Checks if builds is null or not.
	 * 
	 * @param builds
	 *            the build list
	 * @return builds if not null
	 */
	private Set<Build> nullSafe(Set<Build> builds) {
		return builds == null ? new HashSet<>() : builds;
	}

	private int addNewDeploymentJobs(Map<Deployment, Set<Deployment>> deploymentsByJob, List<Deployment> existingJobs,
			AzurePipelineProcessor processor, ProjectBasicConfig proBasicConfig,
			ProcessorToolConnection azurePipelineServer) {
		long start = System.currentTimeMillis();
		int count = 0;
		List<Deployment> newJobs = new ArrayList<>();
		for (Deployment job : deploymentsByJob.keySet()) {
			Deployment existing = null;
			Deployment deploymentData = deploymentRepository
					.findByProjectToolConfigIdAndNumber(azurePipelineServer.getId(), job.getNumber());

			if (!CollectionUtils.isEmpty(existingJobs) && existingJobs.contains(job)) {
				existing = existingJobs.get(existingJobs.indexOf(job));
			}

			if (existing == null) {
				job.setProcessorId(processor.getId());
				newJobs.add(job);
				count++;
			}

			if (proBasicConfig.isSaveAssigneeDetails() && deploymentData != null && deploymentData.getDeployedBy() == null
					&& job.getDeployedBy() != null) {
				deploymentData.setDeployedBy(job.getDeployedBy());
				newJobs.add(deploymentData);
			}
		}

		if (!CollectionUtils.isEmpty(newJobs)) {
			deploymentRepository.saveAll(newJobs);
		}
		if (log.isInfoEnabled()) {
			log.info("New jobs " + start + " " + count);
		}
		return count;
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

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(azurePipelineConfig.getCustomApiBaseUrl());
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
			log.error("[AZUREPIPELINE-CUSTOMAPI-CACHE-EVICT]. Error while consuming rest service {}", e);
		}

		if (null != response && response.getStatusCode().is2xxSuccessful()) {
			log.info("[AZUREPIPELINE-CUSTOMAPI-CACHE-EVICT]. Successfully evicted cache: {} ", cacheName);
		} else {
			log.error("[AZUREPIPELINE-CUSTOMAPI-CACHE-EVICT]. Error while evicting cache: {}", cacheName);
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

	private long lastStartTime(ProjectBasicConfig proBasicConfig, ProcessorExecutionTraceLog processorExecutionTraceLog,
			AzurePipelineProcessor processor, ProcessorToolConnection azurePipelineServer,
			List<Deployment> deploymentJobs) {
		long lastStartTimeOfJobs;
		if (proBasicConfig.isSaveAssigneeDetails() && !processorExecutionTraceLog.isLastEnableAssigneeToggleState()) {
			lastStartTimeOfJobs = 0;
		} else {
			lastStartTimeOfJobs = getLastStartTimeOfJobs(processor, azurePipelineServer, deploymentJobs);
		}
		return lastStartTimeOfJobs;
	}
}
