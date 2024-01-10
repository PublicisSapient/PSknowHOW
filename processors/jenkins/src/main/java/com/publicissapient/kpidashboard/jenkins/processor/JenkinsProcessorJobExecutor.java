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

package com.publicissapient.kpidashboard.jenkins.processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.generic.JobProcessorItem;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.repository.application.DeploymentRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.jenkins.config.JenkinsConfig;
import com.publicissapient.kpidashboard.jenkins.factory.JenkinsClientFactory;
import com.publicissapient.kpidashboard.jenkins.model.JenkinsProcessor;
import com.publicissapient.kpidashboard.jenkins.processor.adapter.JenkinsClient;
import com.publicissapient.kpidashboard.jenkins.repository.JenkinsProcessorRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * ProcessorJobExecutor that fetches Build log information from Jenkins.
 */

@Component
@Slf4j
public class JenkinsProcessorJobExecutor extends ProcessorJobExecutor<JenkinsProcessor> {

	private static final String BUILD = "build";
	@Autowired
	AesEncryptionService aesEncryptionService;
	@Autowired
	private JenkinsProcessorRepository jenkinsProcessorRepository;
	@Autowired
	private BuildRepository buildRepository;
	@Autowired
	private JenkinsConfig jenkinsConfig;
	@Autowired
	private JenkinsClientFactory jenkinsClientFactory;
	@Autowired
	private ProcessorToolConnectionService processorToolConnectionService;
	@Autowired
	private ProjectBasicConfigRepository projectConfigRepository;
	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	@Autowired
	private DeploymentRepository deploymentRepository;
	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	/**
	 * Provides Jenkins TaskScheduler.
	 *
	 * @param taskScheduler
	 *            the task scheduler
	 */
	@Autowired
	public JenkinsProcessorJobExecutor(TaskScheduler taskScheduler) {
		super(taskScheduler, ProcessorConstants.JENKINS);

	}

	/**
	 * Provides Processor.
	 *
	 * @return the JenkinsProcessor
	 */
	@Override
	public JenkinsProcessor getProcessor() {
		return JenkinsProcessor.buildProcessor();
	}

	/**
	 * Provides Processor Repository.
	 *
	 * @return the ProcessorRepository
	 *
	 */
	@Override
	public ProcessorRepository<JenkinsProcessor> getProcessorRepository() {
		return jenkinsProcessorRepository;
	}

	/**
	 * Provides cron expression.
	 *
	 * @return the cron expression
	 */
	@Override
	public String getCron() {
		return jenkinsConfig.getCron();
	}

	/**
	 * Processes Jenkins build data.
	 *
	 * @param processor
	 *            the jenkins processor instance
	 */
	@Override
	public boolean execute(JenkinsProcessor processor) {
		boolean executionStatus = true;
		long startTime = System.currentTimeMillis();
		String uid = UUID.randomUUID().toString();
		MDC.put("processorExecutionUid", uid);
		MDC.put("processorStartTime", String.valueOf(startTime));

		List<ProjectBasicConfig> projectConfigList = getSelectedProjects();
		MDC.put("TotalSelectedProjectsForProcessing", String.valueOf(projectConfigList.size()));
		clearSelectedBasicProjectConfigIds();
		int count = 0;
		for (ProjectBasicConfig proBasicConfig : projectConfigList) {
			log.info("Fetching data for project : {}", proBasicConfig.getProjectName());
			List<ProcessorToolConnection> jenkinsJobFromConfig = processorToolConnectionService
					.findByToolAndBasicProjectConfigId(ProcessorConstants.JENKINS, proBasicConfig.getId());

			for (ProcessorToolConnection jenkinsServer : jenkinsJobFromConfig) {
				String jobType = jenkinsServer.getJobType();
				jenkinsServer.setApiKey(decryptKey(jenkinsServer.getApiKey()));
				MDC.put(JobProcessorItem.INSTANCE_URL, jenkinsServer.getUrl());

				ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLogJenkins(
						proBasicConfig.getId().toHexString());

				try {
					log.info("Fetching jobs : {}", jenkinsServer.getJobName());
					processorExecutionTraceLog.setExecutionStartedAt(System.currentTimeMillis());
					MDC.put("ProjectDataStartTime", String.valueOf(System.currentTimeMillis()));

					JenkinsClient jenkinsClient = jenkinsClientFactory.getJenkinsClient(jobType);
					if (BUILD.equalsIgnoreCase(jobType)) {
						count += processBuildJob(jenkinsClient, jenkinsServer, processor, processorExecutionTraceLog,
								proBasicConfig);
						MDC.put("totalUpdatedCount", String.valueOf(count));
					} else {
						count += processDeployJob(jenkinsClient, jenkinsServer, processor, processorExecutionTraceLog);
						MDC.put("totalUpdatedCount", String.valueOf(count));
					}
				} catch (RestClientException exception) {
					executionStatus = false;
					processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
					processorExecutionTraceLog.setExecutionSuccess(executionStatus);
					processorExecutionTraceLogService.save(processorExecutionTraceLog);
					log.error(exception.getMessage(), exception);
				}
			}
		}

		if (count > 0) {
			cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.JENKINS_KPI_CACHE);
		}
		long endTime = System.currentTimeMillis();
		MDC.put("processorEndTime", String.valueOf(endTime));
		MDC.put("executionTime", String.valueOf(endTime - startTime));
		MDC.put("executionStatus", String.valueOf(executionStatus));
		log.info("Jenkins Processor execution finished");
		MDC.clear();
		return executionStatus;
	}
	@Override
	public boolean executeSprint(String sprintId) {
		return false;
	}

	private int processBuildJob(JenkinsClient jenkinsClient, ProcessorToolConnection jenkinsServer,
			JenkinsProcessor processor, ProcessorExecutionTraceLog processorExecutionTraceLog,
			ProjectBasicConfig proBasicConfig) {
		int buildCount = 0;
		Map<ObjectId, Set<Build>> buildsByJob = jenkinsClient.getBuildJobsFromServer(jenkinsServer, proBasicConfig);
		if (MapUtils.isNotEmpty(buildsByJob)) {

			int updatedJobs = addNewBuildDetails(buildsByJob, jenkinsServer, processor.getId(), proBasicConfig);
			buildCount += updatedJobs;
			log.info("build Job updated count :{}", buildCount);

		} else {
			log.error("Job Details not fetched for : {}, job : {}", jenkinsServer.getUrl(), jenkinsServer.getJobName());
		}
		MDC.put("ProjectDataEndTime", String.valueOf(System.currentTimeMillis()));
		processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
		processorExecutionTraceLog.setExecutionSuccess(true);
		processorExecutionTraceLog.setLastEnableAssigneeToggleState(proBasicConfig.isSaveAssigneeDetails());
		processorExecutionTraceLogService.save(processorExecutionTraceLog);
		return buildCount;
	}

	private int processDeployJob(JenkinsClient jenkinsClient, ProcessorToolConnection jenkinsServer,
			JenkinsProcessor processor, ProcessorExecutionTraceLog processorExecutionTraceLog) {
		int deployCount = 0;
		Map<String, Set<Deployment>> deploymentsByJob = jenkinsClient.getDeployJobsFromServer(jenkinsServer, processor);
		List<Deployment> existingDeployments = deploymentRepository
				.findByProjectToolConfigIdAndJobName(jenkinsServer.getId(), jenkinsServer.getJobName());
		if (MapUtils.isNotEmpty(deploymentsByJob)) {
			Set<Deployment> deployments = findNewDeployments(deploymentsByJob, existingDeployments);
			if (!saveDeployments(deployments)) {
				log.info("Deployments already present for job: {} ", jenkinsServer.getJobName());
			}
			deployCount = deployments.size();
			log.info("deploy Job updated count :{}", deployCount);
		} else {
			log.error("Deployments not fetched for : {}, job : {}", jenkinsServer.getUrl(), jenkinsServer.getJobName());
		}
		MDC.put("ProjectDataEndTime", String.valueOf(System.currentTimeMillis()));
		processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
		processorExecutionTraceLog.setExecutionSuccess(true);
		processorExecutionTraceLogService.save(processorExecutionTraceLog);
		return deployCount;
	}

	private Set<Deployment> findNewDeployments(Map<String, Set<Deployment>> deploymentsByJob,
			List<Deployment> existingDeployments) {
		Set<Deployment> newDeployments = new HashSet<>();
		deploymentsByJob.values().stream().forEach(deployments -> deployments.forEach(deployment -> {
			if (!existingDeployments.contains(deployment)) {
				newDeployments.add(deployment);
			}
		}));
		return newDeployments;
	}

	private boolean saveDeployments(Set<Deployment> deployments) {
		if (null != deployments && !deployments.isEmpty()) {
			deploymentRepository.saveAll(deployments);
			log.info("Finished with total deployments count : {}", deployments.size());
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Iterates over the enabled build jobs and adds new builds to the database.
	 *
	 * @param buildsByJob
	 *            the build by job
	 * @param proBasicConfig
	 * @return build count
	 */
	private int addNewBuildDetails(Map<ObjectId, Set<Build>> buildsByJob, ProcessorToolConnection jenkinsServer,
			ObjectId processorId, ProjectBasicConfig proBasicConfig) {
		long start = System.currentTimeMillis();
		int count = 0;
		List<Build> buildsToSave = new ArrayList<>();
		for (Build build : buildsByJob.values().iterator().next()) {
			Build buildData = buildRepository.findByProjectToolConfigIdAndNumber(jenkinsServer.getId(),
					build.getNumber());
			if (buildData == null) {
				build.setJobFolder(jenkinsServer.getJobName());
				build.setProcessorId(processorId);
				build.setBasicProjectConfigId(jenkinsServer.getBasicProjectConfigId());
				build.setProjectToolConfigId(jenkinsServer.getId());
				build.setBuildJob(jenkinsServer.getJobName());
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
		log.info("New builds {} {}", start, count);
		return count;
	}

	private String decryptKey(String encryptedKey) {
		return aesEncryptionService.decrypt(encryptedKey, jenkinsConfig.getAesEncryptionKey());
	}

	private ProcessorExecutionTraceLog createTraceLogJenkins(String basicProjectConfigId) {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.JENKINS);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		Optional<ProcessorExecutionTraceLog> existingTraceLogOptional = processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.JENKINS, basicProjectConfigId);
		existingTraceLogOptional.ifPresent(
				existingProcessorExecutionTraceLog -> processorExecutionTraceLog.setLastEnableAssigneeToggleState(
						existingProcessorExecutionTraceLog.isLastEnableAssigneeToggleState()));

		return processorExecutionTraceLog;
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

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(jenkinsConfig.getCustomApiBaseUrl());
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
			log.error("[JENKINS-CUSTOMAPI-CACHE-EVICT]. Error while consuming rest service {}", e);
		}

		if (null != response && response.getStatusCode().is2xxSuccessful()) {
			log.info("[JENKINS-CUSTOMAPI-CACHE-EVICT]. Successfully evicted cache: {} ", cacheName);
		} else {
			log.error("[JENKINS-CUSTOMAPI-CACHE-EVICT]. Error while evicting cache: {}", cacheName);
		}
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
