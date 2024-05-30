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

package com.publicissapient.kpidashboard.argocd.processor;

import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.EXECUTION_STATUS;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.EXECUTION_TIME;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.INSTANCE_URL;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.PROCESSOR_END_TIME;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.PROCESSOR_EXECUTION_UID;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.PROCESSOR_START_TIME;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.TOTAL_CONFIGURED_PROJECTS;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.TOTAL_SELECTED_PROJECTS_FOR_PROCESSING;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.TOTAL_UPDATED_COUNT;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.publicissapient.kpidashboard.argocd.client.ArgoCDClient;
import com.publicissapient.kpidashboard.argocd.config.ArgoCDConfig;
import com.publicissapient.kpidashboard.argocd.dto.Application;
import com.publicissapient.kpidashboard.argocd.dto.ApplicationsList;
import com.publicissapient.kpidashboard.argocd.dto.History;
import com.publicissapient.kpidashboard.argocd.dto.UserCredentialsDTO;
import com.publicissapient.kpidashboard.argocd.model.ArgoCDProcessor;
import com.publicissapient.kpidashboard.argocd.repository.ArgoCDProcessorRepository;
import com.publicissapient.kpidashboard.argocd.utils.ArgoCDUtils;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.DeploymentStatus;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.exceptions.ClientErrorMessageEnum;
import com.publicissapient.kpidashboard.common.executor.ProcessorJobExecutor;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.DeploymentRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;

import lombok.extern.slf4j.Slf4j;

/**
 * ProcessorJobExecutor that fetches Deployment details from ArgoCD
 * 
 * @see ProcessorJobExecutor
 * @see ArgoCDProcessor
 */

@Component
@Slf4j
public class ArgoCDProcessorJobExecutor extends ProcessorJobExecutor<ArgoCDProcessor> {

	@Autowired
	private ArgoCDProcessorRepository argoCDProcessorRepository;

	@Autowired
	private ArgoCDConfig argoCDConfig;

	@Autowired
	private ArgoCDClient argoCDClient;

	@Autowired
	private ProjectBasicConfigRepository projectConfigRepository;

	@Autowired
	private DeploymentRepository deploymentRepository;

	@Autowired
	private ProcessorToolConnectionService processorToolConnectionService;

	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Autowired
	private AesEncryptionService aesEncryptionService;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	public ArgoCDProcessorJobExecutor(TaskScheduler taskScheduler) {
		super(taskScheduler, ProcessorConstants.ARGOCD);

	}

	@Override
	public ArgoCDProcessor getProcessor() {
		return ArgoCDProcessor.buildProcessor();
	}

	@Override
	public ProcessorRepository<ArgoCDProcessor> getProcessorRepository() {
		return argoCDProcessorRepository;
	}

	@Override
	public String getCron() {
		return argoCDConfig.getCron();
	}

	/**
	 * Executable method for processing the Deployment details from ArgoCD to
	 * PSKnowHow Database for the projects respectively
	 *
	 * @param processor
	 *            ArgoCD Processor
	 * @return boolean
	 */
	@Override
	public boolean execute(ArgoCDProcessor processor) {
		boolean executionStatus = true;
		long startTime = System.currentTimeMillis();
		String uid = UUID.randomUUID().toString();
		MDC.put(PROCESSOR_EXECUTION_UID, uid);
		MDC.put(PROCESSOR_START_TIME, String.valueOf(startTime));

		List<ProjectBasicConfig> projectConfigList = getSelectedProjects();
		MDC.put(TOTAL_SELECTED_PROJECTS_FOR_PROCESSING, String.valueOf(projectConfigList.size()));
		clearSelectedBasicProjectConfigIds();

		Set<ObjectId> udId = new HashSet<>();
		udId.add(processor.getId());
		List<Deployment> deploymentJobs = deploymentRepository.findByProcessorIdIn(udId);

		int count = 0;
		for (ProjectBasicConfig proBasicConfig : projectConfigList) {
			log.info("Fetching basic data for project : {}", proBasicConfig.getProjectName());
			List<ProcessorToolConnection> argoCDJobList = processorToolConnectionService
					.findByToolAndBasicProjectConfigId(ProcessorConstants.ARGOCD, proBasicConfig.getId());
			count = argoCDJobList.size();
			for (ProcessorToolConnection argoCDJob : argoCDJobList) {
				String baseUrl = argoCDJob.getUrl();
				UserCredentialsDTO cred = new UserCredentialsDTO(argoCDJob.getUsername(),
						decryptPassword(argoCDJob.getPassword()));
				MDC.put(INSTANCE_URL, baseUrl);
				ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(
						proBasicConfig.getId().toHexString());
				try {
					processorToolConnectionService.validateConnectionFlag(argoCDJob);
					processorExecutionTraceLog.setExecutionStartedAt(System.currentTimeMillis());
					String accessToken = argoCDClient.getAuthToken(baseUrl, cred);
					ApplicationsList listOfApplications = argoCDClient.getApplications(baseUrl, accessToken);
					if (null != listOfApplications && null != listOfApplications.getItems()) {
						for (Application applicationitem : listOfApplications.getItems()) {
							Application application = argoCDClient.getApplicationByName(baseUrl,
									applicationitem.getMetadata().getName(), accessToken);
							count += saveRevisionsInDbAndGetCount(application, deploymentJobs, argoCDJob,
									processor.getId());
						}
					}
					log.info("Finished ArgoCD Job started at :: {}", startTime);
					processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
					processorExecutionTraceLog.setExecutionSuccess(true);
					processorExecutionTraceLog.setLastEnableAssigneeToggleState(proBasicConfig.isSaveAssigneeDetails());
					processorExecutionTraceLogService.save(processorExecutionTraceLog);
				} catch (RestClientException exception) {
					isClientException(argoCDJob, exception);
					executionStatus = false;
					processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
					processorExecutionTraceLog.setExecutionSuccess(executionStatus);
					processorExecutionTraceLogService.save(processorExecutionTraceLog);
					log.error("Error getting ArgoCD jobs for ::" + baseUrl + " with exception :: ", exception);
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
		MDC.clear();
		log.info("ArgoCD Processor executed successfully.");
		return executionStatus;
	}

	/**
	 * 
	 * @param argoCDJob
	 *            argoCDJob
	 * @param exception
	 *            exception
	 */
	private void isClientException(ProcessorToolConnection argoCDJob, RestClientException exception) {
		if (exception instanceof HttpClientErrorException
				&& ((HttpClientErrorException) exception).getStatusCode().is4xxClientError()) {
			String errMsg = ClientErrorMessageEnum
					.fromValue(((HttpClientErrorException) exception).getStatusCode().value()).getReasonPhrase();
			processorToolConnectionService.updateBreakingConnection(argoCDJob.getConnectionId(), errMsg);
		}
	}

	/**
	 * @param sprintId
	 *            sprint Id
	 * @return boolean
	 */
	@Override
	public boolean executeSprint(String sprintId) {
		return false;
	}

	/**
	 * Returns the Decrypted value of String
	 * 
	 * @param encryptedValue
	 *            encrypted value of String
	 * @return String
	 */
	private String decryptPassword(String encryptedValue) {
		return aesEncryptionService.decrypt(encryptedValue, argoCDConfig.getAesEncryptionKey());
	}

	/**
	 * Return List of selected ProjectBasicConfig id if null then return all
	 * ProjectBasicConfig ids
	 * 
	 * @return List of ProjectBasicConfig
	 */
	private List<ProjectBasicConfig> getSelectedProjects() {
		List<ProjectBasicConfig> allProjects = projectConfigRepository.findAll();
		MDC.put(TOTAL_CONFIGURED_PROJECTS, String.valueOf(CollectionUtils.emptyIfNull(allProjects).size()));

		List<String> selectedProjectsBasicIds = getProjectsBasicConfigIds();
		if (CollectionUtils.isEmpty(selectedProjectsBasicIds)) {
			return allProjects;
		}
		return CollectionUtils.emptyIfNull(allProjects).stream().filter(
				projectBasicConfig -> selectedProjectsBasicIds.contains(projectBasicConfig.getId().toHexString()))
				.toList();
	}

	private void clearSelectedBasicProjectConfigIds() {
		setProjectsBasicConfigIds(null);
	}

	/**
	 * create Processor Trace log
	 * 
	 * @param basicProjectConfigId
	 *            basic Project Configuration Id
	 * @return ProcessorExecutionTraceLog
	 */
	private ProcessorExecutionTraceLog createTraceLog(String basicProjectConfigId) {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.ARGOCD);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		Optional<ProcessorExecutionTraceLog> existingTraceLogOptional = processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.ARGOCD, basicProjectConfigId);
		existingTraceLogOptional.ifPresent(
				existingProcessorExecutionTraceLog -> processorExecutionTraceLog.setLastEnableAssigneeToggleState(
						existingProcessorExecutionTraceLog.isLastEnableAssigneeToggleState()));
		return processorExecutionTraceLog;
	}

	/**
	 * saves the deployment nodes in database based on existing and new deployment
	 * nodes
	 * 
	 * @param application
	 *            ArgoCD Application
	 * @param exisitingEntries
	 *            Existing entries in Database
	 * @param argoCDJob
	 *            argoCD process tool connection
	 * @param processorId
	 *            processor Id
	 * @return int
	 */
	private int saveRevisionsInDbAndGetCount(Application application, List<Deployment> exisitingEntries,
			ProcessorToolConnection argoCDJob, ObjectId processorId) {
		Map<Pair<String, String>, Deployment> deployments = mapRevisionsToDeployment(application, argoCDJob,
				processorId);
		Map<Pair<String, String>, Deployment> exisitingDeployments = exisitingEntries.stream().collect(Collectors.toMap(
				deployment -> Pair.of(deployment.getEnvName(), deployment.getNumber()), deployment -> deployment));
		Set<Deployment> tobeSavedinDB = deployments.entrySet().stream()
				.filter(entry -> !exisitingDeployments.containsKey(entry.getKey())).map(Map.Entry::getValue)
				.collect(Collectors.toSet());
		tobeSavedinDB.forEach(deployment -> {
			deploymentRepository.save(deployment);
			log.info("Saving ArgoCD deployment info for application {} deployment number {}", deployment.getEnvName(),
					deployment.getNumber());
		});
		return tobeSavedinDB.size();
	}

	/**
	 * The method is responsible for mapping of ArgoCD attributes to Deployment
	 * Model
	 * 
	 * @param application
	 *            ArgoCD Application
	 * @param argoCDJob
	 *            argoCD process tool connection
	 * @param processorId
	 *            processor Id
	 * @return Map<Pair<String, String>, Deployment>
	 */
	private Map<Pair<String, String>, Deployment> mapRevisionsToDeployment(Application application,
			ProcessorToolConnection argoCDJob, ObjectId processorId) {
		Map<Pair<String, String>, Deployment> deployments = new HashMap<>();
		if (null != application.getStatus() && null != application.getStatus().getHistory()) {
			for (History history : application.getStatus().getHistory()) {
				Deployment deployment = new Deployment();
				deployment.setBasicProjectConfigId(argoCDJob.getBasicProjectConfigId());
				deployment.setProjectToolConfigId(argoCDJob.getId());
				deployment.setProcessorId(processorId);
				deployment.setJobId(argoCDJob.getDeploymentProjectId());
				deployment.setJobName(argoCDJob.getDeploymentProjectName());
				deployment.setEnvId(history.getRevision());
				deployment.setEnvName(application.getMetadata().getName());
				deployment.setDeploymentStatus(DeploymentStatus.SUCCESS);
				deployment.setStartTime(ArgoCDUtils.formatDate(history.getDeployStartedAt()));
				deployment.setEndTime(ArgoCDUtils.formatDate(history.getDeployedAt()));
				deployment.setDuration(
						ArgoCDUtils.calculateDuration(history.getDeployStartedAt(), history.getDeployedAt()));
				deployment.setNumber(history.getId());
				deployments.put(Pair.of(deployment.getEnvName(), deployment.getNumber()), deployment);
			}
		}
		return deployments;
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

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(argoCDConfig.getCustomApiBaseUrl());
		uriBuilder.path("/");
		uriBuilder.path(cacheEndPoint);
		uriBuilder.path("/");
		uriBuilder.path(cacheName);

		HttpEntity<?> entity = new HttpEntity<>(headers);

		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, entity, String.class);
		} catch (RestClientException e) {
			log.error("[ARGOCD-CUSTOMAPI-CACHE-EVICT]. Error while consuming rest service ", e);
		}

		if (null != response && response.getStatusCode().is2xxSuccessful()) {
			log.info("[ARGOCD-CUSTOMAPI-CACHE-EVICT]. Successfully evicted cache: {}", cacheName);
		} else {
			log.error("[ARGOCD-CUSTOMAPI-CACHE-EVICT]. Error while evicting cache: {}", cacheName);
		}

	}

}
