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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.azurepipeline.factory.AzurePipelineFactory;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.repository.application.DeploymentRepository;
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
import com.publicissapient.kpidashboard.azurepipeline.model.AzurePipelineJob;
import com.publicissapient.kpidashboard.azurepipeline.model.AzurePipelineProcessor;
import com.publicissapient.kpidashboard.azurepipeline.processor.adapter.AzurePipelineClient;
import com.publicissapient.kpidashboard.azurepipeline.repository.AzurePipelineJobRepository;
import com.publicissapient.kpidashboard.azurepipeline.repository.AzurePipelineProcessorRepository;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.executor.ProcessorJobExecutor;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

import lombok.extern.slf4j.Slf4j;

/**
 * ProcessorJobExecutor that fetches Build log information from AzurePipeline.
 */

@Component
@Slf4j
public class AzurePipelineProcessorJobExecutor extends ProcessorJobExecutor<AzurePipelineProcessor> {

	private static final String JOBNAME = "jobName";
	private static final String PROCESSOR_EXECUTION_UID = "processorExecutionUid";
	private static final String PROCESSOR_START_TIME = "processorStartTime";
	private static final String INSTANCE_URL = "instanceUrl";
	private static final String TOTAL_UPDATED_COUNT = "totalUpdatedCount";
	private static final String PROCESSOR_END_TIME = "processorEndTime";
	private static final String EXECUTION_TIME = "executionTime";
	private static final String EXECUTION_STATUS = "executionStatus";
	private static final String BUILD = "build";

	@Autowired
	private AzurePipelineProcessorRepository azurePipelineProcessorRepository;

	@Autowired
	private AzurePipelineJobRepository azurePipelineJobRepository;

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
	AesEncryptionService aesEncryptionService;

	@Autowired
	private ProcessorToolConnectionService processorToolConnectionService;

	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

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
		List<AzurePipelineJob> existingJobs = azurePipelineJobRepository.findByProcessorIdIn(udId);
		List<Deployment> deploymentJobs = deploymentRepository.findByProcessorIdIn(udId);

		List<AzurePipelineJob> activeBuildJobs = new ArrayList<>();
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
					long lastStartTimeOfJobs = getLastStartTimeOfJobs(processor, azurePipelineServer, existingJobs,
							deploymentJobs);
					if (azurePipelineServer.getJobType().equalsIgnoreCase(BUILD)) {
						count = buildJobs(processor, startTime, existingJobs, activeBuildJobs, count,
								azurePipelineServer, lastStartTimeOfJobs,proBasicConfig);
					} else {
						count = deployJobs(processor, startTime, deploymentJobs, activeDeployJobs, azurePipelineServer,
								lastStartTimeOfJobs,proBasicConfig);
					}
					log.info("Finished : {}", startTime);
					processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
					processorExecutionTraceLog.setExecutionSuccess(true);
					processorExecutionTraceLogService.save(processorExecutionTraceLog);

				} catch (RestClientException exception) {
					executionStatus = false;
					processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
					processorExecutionTraceLog.setExecutionSuccess(executionStatus);
					processorExecutionTraceLogService.save(processorExecutionTraceLog);
					log.error(String.format("Error getting jobs for: %s", instanceUrl), exception);
				} finally {
					if (azurePipelineServer.getJobType().equalsIgnoreCase(BUILD)) {
						removeDiscardedJobs(activeBuildJobs, existingJobs, processor.getId());
					} else {
						removeDiscardedDeploymentJobs(activeDeployJobs, deploymentJobs, processor.getId());
					}

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

	private int deployJobs(AzurePipelineProcessor processor, long startTime, List<Deployment> deploymentJobs,
			List<Deployment> activeDeployJobs, ProcessorToolConnection azurePipelineServer, long lastStartTimeOfJobs,
			ProjectBasicConfig proBasicConfig) {

		Map<Deployment, Set<Deployment>> deploymentsByJob = azurePipelineClient.getDeploymentJobs(azurePipelineServer,
				lastStartTimeOfJobs, proBasicConfig);
		log.info("Fetched jobs : {}", startTime);
		activeDeployJobs.addAll(deploymentsByJob.keySet());
		return addNewDeploymentJobs(deploymentsByJob, deploymentJobs, processor);

	}

	private int buildJobs(AzurePipelineProcessor processor, long startTime, List<AzurePipelineJob> existingJobs,
			List<AzurePipelineJob> activeBuildJobs, int count, ProcessorToolConnection azurePipelineServer,
			long lastStartTimeOfJobs,ProjectBasicConfig proBasicConfig) {
		Map<AzurePipelineJob, Set<Build>> buildsByJob = azurePipelineClient.getInstanceJobs(azurePipelineServer,
				lastStartTimeOfJobs,proBasicConfig);
		log.info("Fetched jobs : {}", startTime);
		activeBuildJobs.addAll(buildsByJob.keySet());
		addNewJobs(buildsByJob.keySet(), existingJobs, processor);
		saveJobs(processor, azurePipelineServer);

		int updatedJobs = addNewBuilds(findActiveJobs(processor, azurePipelineServer.getUrl()), buildsByJob);
		count += updatedJobs;
		return count;
	}

	/**
	 * This method save the jobs.
	 * 
	 * @param processor
	 *            processor
	 * @param azurePipelineServer
	 *            azurePipelineServer
	 */
	public void saveJobs(AzurePipelineProcessor processor, ProcessorToolConnection azurePipelineServer) {
		List<AzurePipelineJob> processorItems = azurePipelineJobRepository.findByProcessorId(processor.getId());
		List<AzurePipelineJob> toBeEnabledJob = new ArrayList<>();

		for (AzurePipelineJob azurePipelinJob : processorItems) {

			if (azurePipelineServer.getUrl().equals(azurePipelinJob.getToolDetailsMap().get(INSTANCE_URL))
					&& azurePipelineServer.getJobName().equals(azurePipelinJob.getToolDetailsMap().get(JOBNAME))) {
				AzurePipelineJob tmpAzurePipelineJob = azurePipelinJob;
				tmpAzurePipelineJob.setActive(true);
				tmpAzurePipelineJob.setVersion((short) 2);
				tmpAzurePipelineJob.setToolConfigId(azurePipelineServer.getId());
				toBeEnabledJob.add(tmpAzurePipelineJob);
			}
		}
		if (!CollectionUtils.isEmpty(toBeEnabledJob)) {
			azurePipelineJobRepository.saveAll(toBeEnabledJob);
		}
	}

	/**
	 * Gets the last start time from the list of azurepipeline builds in db
	 * 
	 * @param processor
	 *            the processor
	 * @param existingJobs
	 *            the existing processor items in db
	 * @return lastStartTimeOfBuilds
	 */
	private long getLastStartTimeOfJobs(AzurePipelineProcessor processor, ProcessorToolConnection azurePipelineServer,
			List<AzurePipelineJob> existingJobs, List<Deployment> deploymentJobs) {
		if (azurePipelineServer.getJobType().equalsIgnoreCase("Build")) {
			return getLastStartTimeOfBuilds(processor, azurePipelineServer, existingJobs);
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
					releases.sort((Deployment deployment1, Deployment deployment2) -> Long
							.valueOf(deployment1.getStartTime()).compareTo(Long.valueOf(deployment2.getStartTime())));
					lastStartTimeOfReleases = Math.max(lastStartTimeOfReleases,
							Long.parseLong(releases.get(releases.size() - 1).getStartTime()));
				}
			}
		}
		return lastStartTimeOfReleases;
	}

	private long getLastStartTimeOfBuilds(AzurePipelineProcessor processor, ProcessorToolConnection azurePipelineServer,
			List<AzurePipelineJob> existingJobs) {
		long lastStartTimeOfBuilds = 0;
		for (AzurePipelineJob job : existingJobs) {
			if (job.getProcessorId().equals(processor.getId())
					&& job.getJobName().equals(azurePipelineServer.getJobName())) {
				List<Build> builds = buildRepository.findByProcessorItemIdAndBuildJob(job.getId(), job.getJobName());
				if (!builds.isEmpty()) {
					builds.sort((Build b1, Build b2) -> Long.valueOf(b1.getStartTime())
							.compareTo(Long.valueOf(b2.getStartTime())));
					lastStartTimeOfBuilds = Math.max(lastStartTimeOfBuilds,
							builds.get(builds.size() - 1).getStartTime());
				}
			}
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
		return processorExecutionTraceLog;
	}

	/**
	 * Delete orphaned job processor items.
	 *
	 * @param activeJobs
	 *            the active AzurePipeline jobs
	 * @param existingJobs
	 *            the existing AzurePipeline jobs
	 * 
	 * @param processorId
	 *            the AzurePipeline processor id
	 */
	private void removeDiscardedJobs(List<AzurePipelineJob> activeJobs, List<AzurePipelineJob> existingJobs,
			ObjectId processorId) {

		List<AzurePipelineJob> deleteJobList = new ArrayList<>();
		for (AzurePipelineJob job : existingJobs) {
			if (job.getVersion() != null && job.getVersion() == 2) {
				continue;
			}

			if (!job.getProcessorId().equals(processorId)) {
				deleteJobList.add(job);
			}

			if (!activeJobs.contains(job)) {
				deleteJobList.add(job);
			}
		}
		if (!CollectionUtils.isEmpty(deleteJobList)) {
			azurePipelineJobRepository.deleteAll(deleteJobList);
		}
	}

	private void removeDiscardedDeploymentJobs(List<Deployment> activeJobs, List<Deployment> existingJobs,
			ObjectId processorId) {

		List<Deployment> deleteJobList = new ArrayList<>();
		for (Deployment job : existingJobs) {

			if (!job.getProcessorId().equals(processorId)) {
				deleteJobList.add(job);
			}

			if (!activeJobs.contains(job)) {
				deleteJobList.add(job);
			}
		}
		if (!CollectionUtils.isEmpty(deleteJobList)) {
			deploymentRepository.deleteAll(deleteJobList);
		}
	}

	/**
	 * Iterates over the enabled build jobs and adds new builds to the database.
	 *
	 * @param enabledJobs
	 *            the list of enabled AzurePipeline job
	 * @param buildsByJob
	 *            the build by job
	 * @return adds new build
	 */
	private int addNewBuilds(List<AzurePipelineJob> enabledJobs, Map<AzurePipelineJob, Set<Build>> buildsByJob) {
		long start = System.currentTimeMillis();
		int count = 0;
		for (AzurePipelineJob job : enabledJobs) {
			// process new builds in the order of their build numbers - this has
			// implication to handling of commits in BuildEventListener
			ArrayList<Build> builds = new ArrayList<>(nullSafe(buildsByJob.get(job)));
			builds.sort(
					(Build b1, Build b2) -> Long.valueOf(b1.getStartTime()).compareTo(Long.valueOf(b2.getStartTime())));
			for (Build build : builds) {
				if (isNewBuild(job, build)) {
					build.setProcessorItemId(job.getId());
					build.setBuildJob(job.getJobName());
					buildRepository.save(build);
					count++;
				}
			}
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

	/**
	 * Adds new AzurePipelineJobs to the database as disabled jobs.
	 * 
	 * @param jobs
	 *            the AzurePipeline jobs name
	 * @param existingJobs
	 *            the existing AzurePipeline jobs
	 * @param processor
	 *            the AzurePipeline processor
	 *
	 */
	private void addNewJobs(Set<AzurePipelineJob> jobs, List<AzurePipelineJob> existingJobs,
			AzurePipelineProcessor processor) {
		long start = System.currentTimeMillis();
		int count = 0;
		List<AzurePipelineJob> newJobs = new ArrayList<>();
		for (AzurePipelineJob job : jobs) {
			AzurePipelineJob existing = null;
			if (!CollectionUtils.isEmpty(existingJobs) && existingJobs.contains(job)) {
				existing = existingJobs.get(existingJobs.indexOf(job));
			}

			if (existing == null) {
				job.setProcessorId(processor.getId());
				job.setActive(true);
				job.setDesc(job.getJobName());
				newJobs.add(job);
				count++;
			}
		}
		// save all in one shot
		if (!CollectionUtils.isEmpty(newJobs)) {
			azurePipelineJobRepository.saveAll(newJobs);
		}
		if (log.isInfoEnabled()) {
			log.info("New jobs " + start + " " + count);
		}
	}

	/**
	 * Finds enabled Jobs.
	 * 
	 * @param processor
	 *            azurePipeline processor
	 * @param instanceUrl
	 *            azurePipeline build server url
	 * @return List<AzurePipelineJob>
	 */
	private List<AzurePipelineJob> findActiveJobs(AzurePipelineProcessor processor, String instanceUrl) {
		return azurePipelineJobRepository.findEnabledJobs(processor.getId(), instanceUrl);
	}

	/**
	 * Checks whether the build is new.
	 * 
	 * @param job
	 *            the AzurePipeline jobs
	 * @param build
	 *            the AzurePipeline build
	 * @return boolean
	 */
	private boolean isNewBuild(AzurePipelineJob job, Build build) {
		return buildRepository.findByProcessorItemIdAndNumber(job.getId(), build.getNumber()) == null;
	}

	private int addNewDeploymentJobs(Map<Deployment, Set<Deployment>> deploymentsByJob, List<Deployment> existingJobs,
			AzurePipelineProcessor processor) {
		long start = System.currentTimeMillis();
		int count = 0;
		List<Deployment> newJobs = new ArrayList<>();
		for (Deployment job : deploymentsByJob.keySet()) {
			Deployment existing = null;
			if (!CollectionUtils.isEmpty(existingJobs) && existingJobs.contains(job)) {
				existing = existingJobs.get(existingJobs.indexOf(job));
			}

			if (existing == null) {
				job.setProcessorId(processor.getId());
				newJobs.add(job);
				count++;
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

		clearToolItemCache(azurePipelineConfig.getCustomApiBaseUrl());
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
