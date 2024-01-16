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

package com.publicissapient.kpidashboard.githubaction.processor;

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
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.githubaction.config.GitHubActionConfig;
import com.publicissapient.kpidashboard.githubaction.customexception.FetchingBuildException;
import com.publicissapient.kpidashboard.githubaction.factory.GitHubActionClientFactory;
import com.publicissapient.kpidashboard.githubaction.model.GitHubActionProcessor;
import com.publicissapient.kpidashboard.githubaction.processor.adapter.GitHubActionClient;
import com.publicissapient.kpidashboard.githubaction.repository.GitHubProcessorRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * GitHubActionProcessorJobExecutor represents a class which holds all the
 * configuration and GitHub execution process.
 * 
 * @see GitHubActionProcessor
 */
@Slf4j
@Component
public class GitHubActionProcessorJobExecutor extends ProcessorJobExecutor<GitHubActionProcessor> {

	private static final String BUILD = "build";

	@Autowired
	private GitHubActionConfig gitHubActionConfig;

	@Autowired
	private GitHubProcessorRepository gitHubActionProcessorRepository;

	@Autowired
	private ProcessorToolConnectionService processorToolConnectionService;

	@Autowired
	private ProjectBasicConfigRepository projectConfigRepository;

	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@Autowired
	private GitHubActionClientFactory gitHubActionClientFactory;
	@Autowired
	private BuildRepository buildRepository;
	@Autowired
	private DeploymentRepository deploymentRepository;

	@Autowired
	public GitHubActionProcessorJobExecutor(TaskScheduler taskScheduler) {
		super(taskScheduler, ProcessorConstants.GITHUBACTION);
	}

	@Override
	public GitHubActionProcessor getProcessor() {
		return GitHubActionProcessor.prototype();
	}

	@Override
	public ProcessorRepository<GitHubActionProcessor> getProcessorRepository() {
		return gitHubActionProcessorRepository;
	}

	@Override
	public String getCron() {
		return gitHubActionConfig.getCron();
	}

	@Override
	public boolean execute(GitHubActionProcessor processor) {
		boolean executionStatus = true;
		long startTime = System.currentTimeMillis();
		String uid = UUID.randomUUID().toString();
		MDC.put("GitHubActionProcessorJobExecutorUid", uid);
		MDC.put("processorStartTime", String.valueOf(startTime));

		List<ProjectBasicConfig> projectConfigList = getSelectedProjects();
		MDC.put("TotalSelectedProjectsForProcessing", String.valueOf(projectConfigList.size()));
		clearSelectedBasicProjectConfigIds();
		int count = 0;

		Set<ObjectId> udId = new HashSet<>();
		udId.add(processor.getId());
		List<Deployment> deploymentJobs = deploymentRepository.findByProcessorIdIn(udId);

		for (ProjectBasicConfig proBasicConfig : projectConfigList) {
			log.info("Fetching data for project : {}", proBasicConfig.getProjectName());
			List<ProcessorToolConnection> githubActionJobsFromConfig = processorToolConnectionService
					.findByToolAndBasicProjectConfigId(ProcessorConstants.GITHUBACTION, proBasicConfig.getId());
			for (ProcessorToolConnection gitHubActions : githubActionJobsFromConfig) {
				String jobType = gitHubActions.getJobType();

				ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(
						proBasicConfig.getId().toHexString());

				try {
					log.info("Fetching jobs : {}", gitHubActions.getJobName());
					processorExecutionTraceLog.setExecutionStartedAt(System.currentTimeMillis());
					MDC.put("ProjectDataStartTime", String.valueOf(System.currentTimeMillis()));

					GitHubActionClient gitHubActionClient = gitHubActionClientFactory.getGitHubActionClient(jobType);
					if (BUILD.equalsIgnoreCase(jobType)) {
						processBuildJob(gitHubActionClient, gitHubActions, processor, processorExecutionTraceLog, count,
								proBasicConfig);
						MDC.put("totalUpdatedCount", String.valueOf(count));
					} else {
						processDeployJob(gitHubActionClient, gitHubActions, processor, proBasicConfig, deploymentJobs,
								processorExecutionTraceLog);
					}
				} catch (RestClientException | FetchingBuildException exception) {
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
		log.info("GitHubAction Processor execution finished");
		MDC.clear();
		return executionStatus;

	}

	@Override
	public boolean executeSprint(String sprintId) {
		return false;
	}

	private void processDeployJob(GitHubActionClient gitHubActionClient, ProcessorToolConnection gitHubActions,
			GitHubActionProcessor processor, ProjectBasicConfig projectBasicConfig, List<Deployment> deploymentJobs,
			ProcessorExecutionTraceLog processorExecutionTraceLog) throws FetchingBuildException {

		Map<Deployment, Set<Deployment>> deploymentsByJob = gitHubActionClient.getDeployJobsFromServer(gitHubActions,
				projectBasicConfig);
		if (MapUtils.isNotEmpty(deploymentsByJob)) {
			addNewDeploymentJobs(deploymentsByJob, deploymentJobs, processor);
		}

		MDC.put("ProjectDataEndTime", String.valueOf(System.currentTimeMillis()));
		processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
		processorExecutionTraceLog.setExecutionSuccess(true);
		processorExecutionTraceLogService.save(processorExecutionTraceLog);

	}

	private void addNewDeploymentJobs(Map<Deployment, Set<Deployment>> deploymentsByJob, List<Deployment> existingJobs,
			GitHubActionProcessor processor) {

		List<Deployment> newJobs = new ArrayList<>();
		for (Deployment job : deploymentsByJob.keySet()) {
			Deployment existing = null;
			if (!CollectionUtils.isEmpty(existingJobs) && existingJobs.contains(job)) {
				existing = existingJobs.get(existingJobs.indexOf(job));
			}

			if (existing == null) {
				job.setProcessorId(processor.getId());
				newJobs.add(job);
			}

		}

		log.info("new deployments added " + newJobs.size());

		if (!CollectionUtils.isEmpty(newJobs)) {
			deploymentRepository.saveAll(newJobs);
		}
	}

	private void processBuildJob(GitHubActionClient gitHubActionClient, ProcessorToolConnection gitHubActions,
			GitHubActionProcessor processor, ProcessorExecutionTraceLog processorExecutionTraceLog, int count,
			ProjectBasicConfig proBasicConfig) throws FetchingBuildException {

		Set<Build> buildsByJob = gitHubActionClient.getBuildJobsFromServer(gitHubActions, proBasicConfig);
		if (CollectionUtils.isNotEmpty(buildsByJob)) {

			int updatedJobs = addNewBuildDetails(buildsByJob, gitHubActions, processor.getId(), proBasicConfig);
			count += updatedJobs;
			log.info("Job updated for :{}", count);

		} else {
			log.error("Job Details not fetched for : {}, job : {}", gitHubActions.getUrl(), gitHubActions.getJobName());
		}
		MDC.put("ProjectDataEndTime", String.valueOf(System.currentTimeMillis()));
		processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
		processorExecutionTraceLog.setExecutionSuccess(true);
		processorExecutionTraceLog.setLastEnableAssigneeToggleState(proBasicConfig.isSaveAssigneeDetails());
		processorExecutionTraceLogService.save(processorExecutionTraceLog);

	}

	private int addNewBuildDetails(Set<Build> buildsByJob, ProcessorToolConnection gitHubActions, ObjectId processorId,
			ProjectBasicConfig proBasicConfig) {
		long start = System.currentTimeMillis();
		int count = 0;
		List<Build> buildsToSave = new ArrayList<>();
		Set<String> number = buildsByJob.stream().map(Build::getNumber).collect(Collectors.toSet());
		List<Build> buildData = buildRepository.findByProjectToolConfigIdAndNumberIn(gitHubActions.getId(), number);
		for (Build build : buildsByJob) {
			Build dBBuild = buildData.stream().filter(build1 -> build1.getNumber().equals(build.getNumber()))
					.findFirst().orElse(new Build());
			if (StringUtils.isEmpty(dBBuild.getNumber())) {
				build.setJobFolder(gitHubActions.getJobName());
				build.setProcessorId(processorId);
				build.setBasicProjectConfigId(gitHubActions.getBasicProjectConfigId());
				build.setProjectToolConfigId(gitHubActions.getId());
				build.setBuildJob(gitHubActions.getJobName());
				buildsToSave.add(build);
				count++;
			} else {

				if (proBasicConfig.isSaveAssigneeDetails() && dBBuild.getStartedBy() == null
						&& build.getStartedBy() != null) {
					dBBuild.setStartedBy(build.getStartedBy());
					buildsToSave.add(dBBuild);
				}
			}
		}

		if (CollectionUtils.isNotEmpty(buildsToSave)) {
			buildRepository.saveAll(buildsToSave);
		}
		log.info("New builds {} {}", start, count);
		return count;
	}

	private ProcessorExecutionTraceLog createTraceLog(String basicProjectConfigId) {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.GITHUBACTION);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		Optional<ProcessorExecutionTraceLog> existingTraceLogOptional = processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.GITHUBACTION, basicProjectConfigId);
		existingTraceLogOptional.ifPresent(
				existingProcessorExecutionTraceLog -> processorExecutionTraceLog.setLastEnableAssigneeToggleState(
						existingProcessorExecutionTraceLog.isLastEnableAssigneeToggleState()));

		return processorExecutionTraceLog;
	}

	private void clearSelectedBasicProjectConfigIds() {
		setProjectsBasicConfigIds(null);
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

	private void cacheRestClient(String cacheEndPoint, String cacheName) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(gitHubActionConfig.getCustomApiBaseUrl());
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

}
