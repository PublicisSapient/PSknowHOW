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

package com.publicissapient.kpidashboard.bamboo.processor;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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
import org.json.simple.parser.ParseException;
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

import com.publicissapient.kpidashboard.bamboo.client.BambooClient;
import com.publicissapient.kpidashboard.bamboo.config.BambooConfig;
import com.publicissapient.kpidashboard.bamboo.factory.BambooClientFactory;
import com.publicissapient.kpidashboard.bamboo.model.BambooProcessor;
import com.publicissapient.kpidashboard.bamboo.repository.BambooProcessorRepository;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.DeploymentStatus;
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
 * This class provides Processor Task that fetches Build information from Bamboo
 * with the help of scheduler
 */
@Component
@Slf4j
public class BambooProcessorJobExecuter extends ProcessorJobExecutor<BambooProcessor> {

	private static final String BUILD = "build";
	private boolean executionStatus = true;
	private int failureCount;
	private int newBuildCount;

	@Autowired
	private BambooProcessorRepository bambooProcessorRepository;

	@Autowired
	private BuildRepository buildRepository;

	@Autowired
	private BambooConfig bambooConfig;

	@Autowired
	private ProcessorToolConnectionService processorToolConnectionService;

	@Autowired
	private AesEncryptionService aesEncryptionService;

	@Autowired
	private ProjectBasicConfigRepository projectConfigRepository;

	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Autowired
	private BambooClientFactory bambooClientFactory;

	@Autowired
	private DeploymentRepository deploymentRepository;
	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	/**
	 * Initializes and calls the base parameterized constructor of
	 * {@link ProcessorJobExecutor}
	 *
	 * @param taskScheduler
	 *            gets the configured scheduler from the properties file
	 */
	@Autowired
	public BambooProcessorJobExecuter(TaskScheduler taskScheduler) {
		super(taskScheduler, ProcessorConstants.BAMBOO);
	}

	/**
	 * Provides a base processor instance of {@link BambooProcessor}
	 */
	@Override
	public BambooProcessor getProcessor() {
		return BambooProcessor.prototype();
	}

	/**
	 * Provides the ProcessorRepository instance for Bamboo to do CRUD operations on
	 * collection processor
	 */
	@Override
	public ProcessorRepository<BambooProcessor> getProcessorRepository() {
		return bambooProcessorRepository;
	}

	/**
	 * Gets the Cron expression from the properties file
	 */
	@Override
	public String getCron() {
		return bambooConfig.getCron();
	}

	/**
	 * Iterates over the fetched build jobs and check it this build is already exist
	 * or not. adds only new builds to the build collections.
	 *
	 * @param buildsByJobMap
	 *            maps a {@link ObjectId} to a set of {@link Build}s.
	 * @return count of new build info added in db
	 */
	private int addNewBuildsInfoToDb(BambooClient bambooClient, List<Build> activeBuildJobs,
			Map<ObjectId, Set<Build>> buildsByJobMap, ProcessorToolConnection bambooserver, ObjectId processorId) {
		int count = 0;
		List<Build> buildsToSave = new ArrayList<>();
		for (Build buildInfo : nullSafe(buildsByJobMap.get(bambooserver.getId()))) {
			if (isNewBuild(bambooserver.getId(), buildInfo.getNumber())) {
				Build build = bambooClient.getBuildDetailsFromServer(buildInfo.getBuildUrl(), bambooserver.getUrl(),
						bambooserver);
				if (null != build) {
					build.setProcessorId(processorId);
					build.setBasicProjectConfigId(bambooserver.getBasicProjectConfigId());
					build.setProjectToolConfigId(bambooserver.getId());
					build.setBuildJob(buildInfo.getBuildJob());
					buildsToSave.add(build);
					count++;
					log.info("Saving build info for jobName {}, jobId: {}, buildNumber() : {} in DB.",
							bambooserver.getJobName(), bambooserver.getId(), buildInfo.getNumber());
				}
			}
		}
		if (CollectionUtils.isNotEmpty(buildsToSave)) {
			activeBuildJobs.addAll(buildsToSave);
			buildRepository.saveAll(buildsToSave);
		}
		log.info("Added {} new builds in the DB.", count);
		return count;
	}

	/**
	 * Null check safety for the builds collection
	 *
	 * @param builds
	 *            builds info
	 * @return builds or a new empty set
	 */
	private Set<Build> nullSafe(Set<Build> builds) {
		return builds == null ? new HashSet<>() : builds;
	}

	/**
	 * Checks if its a new build not present in repo projectToolConfigId and
	 * BuildNumber. projectToolConfigId refer only one job for tool.
	 *
	 * @param jobId
	 *            Bamboo jobId
	 * @param buildNumber
	 *            Bamboo build Number
	 * @return true if build not already present in repo
	 */
	private boolean isNewBuild(ObjectId jobId, String buildNumber) {
		return buildRepository.findByProjectToolConfigIdAndNumber(jobId, buildNumber) == null;
	}

	/**
	 * Runs when the scheduler is called and executes the business logic to get the
	 * Bamboo data to store in DB
	 */
	@Override
	public boolean execute(BambooProcessor processor) {
		long start = System.currentTimeMillis();
		int totalCount = 0;
		String uid = UUID.randomUUID().toString();
		MDC.put("processorExecutionUid", uid);
		ObjectId processorId = processor.getId();
		if (null != processorId) {
			MDC.put("processorId", processorId.toString());
			Set<ObjectId> bambooProcessorIds = new HashSet<>();
			bambooProcessorIds.add(processorId);

			List<ProjectBasicConfig> projectConfigList = getSelectedProjects();
			clearSelectedBasicProjectConfigIds();

			Map<Pair<ObjectId, String>, List<Deployment>> existingDeployJobs = getAllInformationfromDeployment(
					processorId);

			List<Build> activeBuildJobs = new ArrayList<>();
			List<Deployment> activeDeployJobs = new ArrayList<>();
			Set<ObjectId> nonExistentToolConfig = new HashSet<>();

			MDC.put("TotalSelectedProjectsForProcessing", String.valueOf(projectConfigList.size()));
			for (ProjectBasicConfig proBasicConfig : projectConfigList) {
				log.info("Fetching data for project : {}", proBasicConfig.getProjectName());
				List<ProcessorToolConnection> bambooJobList = processorToolConnectionService
						.findByToolAndBasicProjectConfigId(ProcessorConstants.BAMBOO, proBasicConfig.getId());
				checkNonExistingTool(bambooJobList, existingDeployJobs, nonExistentToolConfig);

				if (!CollectionUtils.isEmpty(bambooJobList)) {
					totalCount = bambooJobList.size();
					processEachBambooJobOnJobType(bambooJobList, existingDeployJobs, activeBuildJobs, activeDeployJobs,
							processorId, proBasicConfig);
				}
			}
			// Delete jobs that will be no longer collected because servers have
			// moved etc.
			deleteJobs(newBuildCount, activeDeployJobs, nonExistentToolConfig);
			long end = System.currentTimeMillis();
			MDC.put("processorStartTime", String.valueOf(start));
			MDC.put("processorEndTime", String.valueOf(end));
			MDC.put("executionTime", String.valueOf(end - start));
			MDC.put("failureCount", String.valueOf(failureCount));
			MDC.put("totalJobsCount", String.valueOf(totalCount));
			MDC.put("executionStatus", String.valueOf(executionStatus));
			log.info("Bamboo Processor execution completed.");
			MDC.clear();
		}
		return executionStatus;
	}

	@Override
	public boolean executeSprint(String sprintId) {
		return false;
	}

	private void processEachBambooJobOnJobType(List<ProcessorToolConnection> bambooJobList,
			Map<Pair<ObjectId, String>, List<Deployment>> existingDeployJobs, List<Build> activeBuildJobs,
			List<Deployment> activeDeployJobs, ObjectId processorId, ProjectBasicConfig proBasicConfig) {
		for (ProcessorToolConnection bambooJobConfig : bambooJobList) {
			String jobType = bambooJobConfig.getJobType();
			ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLogBamboo(
					bambooJobConfig.getBasicProjectConfigId().toHexString());
			processorExecutionTraceLog.setExecutionStartedAt(System.currentTimeMillis());
			MDC.put("bambooInstanceUrl", bambooJobConfig.getUrl());
			MDC.put("JobName", BUILD.equalsIgnoreCase(jobType) ? bambooJobConfig.getJobName()
					: bambooJobConfig.getDeploymentProjectId());
			bambooJobConfig.setPassword(decryptPassword(bambooJobConfig.getPassword()));
			try {
				BambooClient bambooClient = bambooClientFactory.getBambooClient(jobType);
				if (BUILD.equalsIgnoreCase(jobType)) {
					newBuildCount = processBuildJob(bambooClient, bambooJobConfig, processorExecutionTraceLog,
							activeBuildJobs, newBuildCount, processorId, proBasicConfig);
				} else {
					processDeployJob(bambooClient, existingDeployJobs, bambooJobConfig, processorExecutionTraceLog,
							activeDeployJobs, processorId, proBasicConfig);
				}

			} catch (MalformedURLException | ParseException rcp) {
				processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
				processorExecutionTraceLog.setExecutionSuccess(false);
				processorExecutionTraceLogService.save(processorExecutionTraceLog);
				log.error("Error getting jobs for: {}", bambooJobConfig.getUrl());
				failureCount++;
				executionStatus = false;
			} finally {
				MDC.remove("JobName");
				MDC.remove("bambooInstanceUrl");
			}
		}
	}

	private void checkNonExistingTool(List<ProcessorToolConnection> bambooJobList,
			Map<Pair<ObjectId, String>, List<Deployment>> existingDeployedJobs, Set<ObjectId> nonExistentToolConfig) {
		Map<ObjectId, List<ProcessorToolConnection>> collect = bambooJobList.stream()
				.collect(Collectors.groupingBy(ProcessorToolConnection::getId));
		existingDeployedJobs.keySet().forEach(key -> {
			if (!collect.containsKey(key.getLeft())) {
				nonExistentToolConfig.add(key.getLeft());
			}
		});
	}

	private Map<Pair<ObjectId, String>, List<Deployment>> getAllInformationfromDeployment(ObjectId processorId) {
		List<Deployment> allDeployments = deploymentRepository.findAll();
		return allDeployments.stream().filter(deployment -> deployment.getProcessorId().compareTo(processorId) == 0)
				.collect(Collectors
						.groupingBy(deployment -> Pair.of(deployment.getProjectToolConfigId(), deployment.getJobId())));
	}

	private void processDeployJob(BambooClient bambooClient,
			Map<Pair<ObjectId, String>, List<Deployment>> existingDeployJobs, ProcessorToolConnection bambooJobConfig,
			ProcessorExecutionTraceLog processorExecutionTraceLog, List<Deployment> activeJobs, ObjectId processorId,
			ProjectBasicConfig proBasicConfig) throws MalformedURLException, ParseException {
		Map<Pair<ObjectId, String>, Set<Deployment>> deployJobsFromBamboo = bambooClient
				.getDeployJobsFromServer(bambooJobConfig, proBasicConfig);

		Set<Deployment> deployments = addNewBambooDeploysJobsToDb(deployJobsFromBamboo, existingDeployJobs,
				proBasicConfig);
		Set<Deployment> saveDeployments = new HashSet<>();
		deployments.stream().forEach(deployment -> {
			if (checkDeploymentConditionsNotNull(deployment)) {
				saveDeployments.add(deployment);
			}
		});
		saveDeployJob(saveDeployments, processorId);
		activeJobs.addAll(saveDeployments);
		processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
		processorExecutionTraceLog.setExecutionSuccess(true);
		processorExecutionTraceLogService.save(processorExecutionTraceLog);
		log.info("Finished with total deployed activeJobs count: {}", activeJobs.size());
	}

	private boolean checkDeploymentConditionsNotNull(Deployment deployment) {
		if (deployment.getEnvName() == null || deployment.getStartTime() == null || deployment.getEndTime() == null
				|| deployment.getDeploymentStatus() == null) {
			log.error("deployments conditions not satisfied so that data is not saved in db {}", deployment);
			return false;
		} else {
			return true;
		}
	}

	private void saveDeployJob(Set<Deployment> deployments, ObjectId processorId) {
		if (null != deployments) {
			deployments.forEach(deployment -> {
				deployment.setProcessorId(processorId);
				deploymentRepository.save(deployment);
				log.info("Saving deploy info for jobName {}, jobId: {}, releaseNumber() : {} in DB.",
						deployment.getJobName(), deployment.getId(), deployment.getNumber());

			});

		}
	}

	private Set<Deployment> addNewBambooDeploysJobsToDb(
			Map<Pair<ObjectId, String>, Set<Deployment>> deployJobsFromBamboo,
			Map<Pair<ObjectId, String>, List<Deployment>> existingDeployJobs, ProjectBasicConfig proBasicConfig) {
		Set<Deployment> finalDataToSave = new HashSet<>();
		deployJobsFromBamboo.forEach((key, value) -> {

			if (existingDeployJobs.containsKey(key)) {
				finalDataToSave
						.addAll(checkForExistingEnvironmentRelease(key, value, existingDeployJobs, proBasicConfig));
			} else {
				// directly push all the values
				finalDataToSave.addAll(value);
			}

		});
		return finalDataToSave;

	}

	private Set<Deployment> checkForExistingEnvironmentRelease(Pair<ObjectId, String> key, Set<Deployment> value,
			Map<Pair<ObjectId, String>, List<Deployment>> existingDeployJobs, ProjectBasicConfig proBasicConfig) {
		Set<Deployment> deploy = new HashSet<>();
		value.forEach(deployment -> {
			List<Deployment> existingdeployments = existingDeployJobs.get(key);
			Map<String, List<Deployment>> collect = existingdeployments.stream()
					.collect(Collectors.groupingBy(Deployment::getEnvId));
			boolean present = false;
			if (collect.containsKey(deployment.getEnvId())) {
				present = checkForCombination(collect.get(deployment.getEnvId()), deployment);
			}
			if (!present) {
				deploy.add(deployment);
			}
			existingdeployments.forEach(deployments -> {
				if (proBasicConfig.isSaveAssigneeDetails() && deployments.getDeployedBy() == null
						&& deployment.getDeployedBy() != null) {
					deployments.setDeployedBy(deployment.getDeployedBy());
					deploy.add(deployments);
				}
			});
		});

		return deploy;
	}

	private boolean checkForCombination(List<Deployment> existingdeployments, Deployment deployment) {
		LocalDateTime bambooStart;
		LocalDateTime bambooEnd;
		if (!checkRepeatedJobs(existingdeployments, deployment)
				&& !(DeploymentStatus.IN_PROGRESS.equals(deployment.getDeploymentStatus()))) {
			List<Deployment> sortedOnEnd = existingdeployments.stream()
					.sorted((c1, c2) -> DateUtil.stringToLocalDateTime(c2.getEndTime(), DateUtil.TIME_FORMAT)
							.compareTo(DateUtil.stringToLocalDateTime(c1.getEndTime(), DateUtil.TIME_FORMAT)))
					.collect(Collectors.toList());
			LocalDateTime endDb = DateUtil.stringToLocalDateTime(sortedOnEnd.get(0).getEndTime(), DateUtil.TIME_FORMAT);
			LocalDateTime startDb = DateUtil.stringToLocalDateTime(sortedOnEnd.get(0).getStartTime(),
					DateUtil.TIME_FORMAT);
			try {
				bambooStart = DateUtil.stringToLocalDateTime(deployment.getStartTime(), DateUtil.TIME_FORMAT);
				bambooEnd = DateUtil.stringToLocalDateTime(deployment.getEndTime(), DateUtil.TIME_FORMAT);
			} catch (DateTimeParseException | NumberFormatException ex) {
				log.error("Exception while checking combination with dates " + ex);
				bambooStart = LocalDateTime.now();
				bambooEnd = LocalDateTime.now();
			}
			return !endDb.isBefore(bambooEnd) || (!startDb.isBefore(bambooStart));
		}
		return true;
	}

	private boolean checkRepeatedJobs(List<Deployment> existingdeployments, Deployment deployment) {
		boolean repeat = false;
		for (Deployment existingDeployment : existingdeployments) {
			repeat = existingDeployment.getStartTime().equalsIgnoreCase(deployment.getStartTime());
			repeat = repeat && existingDeployment.getEndTime().equalsIgnoreCase(deployment.getEndTime());
			repeat = repeat && existingDeployment.getNumber().equalsIgnoreCase(deployment.getNumber());
			repeat = repeat && existingDeployment.getDeploymentStatus().equals(deployment.getDeploymentStatus());
			if (repeat) {
				break;
			}
		}

		return repeat;

	}

	/**
	 * fetched builds from server and saved in db only it is not present in db.
	 *
	 * @param bambooClient
	 * @param bambooJobConfig
	 * @param processorExecutionTraceLog
	 * @param activeBuildJobs
	 * @param newBuildCount
	 * @param processorId
	 * @param proBasicConfig
	 * @return
	 * @throws MalformedURLException
	 * @throws ParseException
	 */
	private int processBuildJob(BambooClient bambooClient, ProcessorToolConnection bambooJobConfig,
			ProcessorExecutionTraceLog processorExecutionTraceLog, List<Build> activeBuildJobs, int newBuildCount,
			ObjectId processorId, ProjectBasicConfig proBasicConfig) throws MalformedURLException, ParseException {
		Map<ObjectId, Set<Build>> buildsByJobMap = bambooClient.getJobsFromServer(bambooJobConfig, proBasicConfig);
		log.info("Fetched builds By Job map of size: {}", buildsByJobMap.size());
		int updatedJobCount = addNewBuildsInfoToDb(bambooClient, activeBuildJobs, buildsByJobMap, bambooJobConfig,
				processorId);
		processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
		processorExecutionTraceLog.setExecutionSuccess(true);
		processorExecutionTraceLogService.save(processorExecutionTraceLog);
		log.info("Finished with activeJobs count: {}", activeBuildJobs.size());
		return newBuildCount + updatedJobCount;
	}

	private String decryptPassword(String encryptedPassword) {
		return aesEncryptionService.decrypt(encryptedPassword, bambooConfig.getAesEncryptionKey());
	}

	private void deleteJobs(int newBuildCount, List<Deployment> activeDeployJobs, Set<ObjectId> nonExistentToolConfig) {
		if (newBuildCount > 0 || !activeDeployJobs.isEmpty()) {
			cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.JENKINS_KPI_CACHE);
		}
		if (!nonExistentToolConfig.isEmpty()) {
			nonExistentToolConfig
					.forEach(toolObject -> deploymentRepository.deleteDeploymentByProjectToolConfigId(toolObject));
		}
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

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(bambooConfig.getCustomApiBaseUrl());
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
			log.error("[BAMBOO-CUSTOMAPI-CACHE-EVICT]. Error while consuming rest service {}", e);
		}

		if (null != response && response.getStatusCode().is2xxSuccessful()) {
			log.info("[BAMBOO-CUSTOMAPI-CACHE-EVICT]. Successfully evicted cache: {} ", cacheName);
		} else {
			log.error("[BAMBOO-CUSTOMAPI-CACHE-EVICT]. Error while evicting cache: {}", cacheName);
		}
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

	private ProcessorExecutionTraceLog createTraceLogBamboo(String basicProjectConfigId) {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.BAMBOO);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		Optional<ProcessorExecutionTraceLog> existingTraceLogOptional = processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.BAMBOO, basicProjectConfigId);
		existingTraceLogOptional.ifPresent(
				existingProcessorExecutionTraceLog -> processorExecutionTraceLog.setLastEnableAssigneeToggleState(
						existingProcessorExecutionTraceLog.isLastEnableAssigneeToggleState()));

		return processorExecutionTraceLog;
	}

}