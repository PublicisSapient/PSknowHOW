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

package com.publicissapient.kpidashboard.zephyr.processor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.publicissapient.kpidashboard.common.model.zephyr.ZephyrTestCaseDTO;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.zephyr.client.ZephyrClient;
import com.publicissapient.kpidashboard.zephyr.config.ZephyrConfig;
import com.publicissapient.kpidashboard.zephyr.factory.ZephyrClientFactory;
import com.publicissapient.kpidashboard.zephyr.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.zephyr.model.ZephyrProcessor;
import com.publicissapient.kpidashboard.zephyr.processor.service.ZephyrDBService;
import com.publicissapient.kpidashboard.zephyr.repository.ZephyrProcessorRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * The Job executor class which starts the execution of zephyr processor.
 */
@Component
@Slf4j
public class ZephyrProcessorJobExecutor extends ProcessorJobExecutor<ZephyrProcessor> {
	private static final String PROCESSOR_EXECUTION_UID = "processorExecutionUid";
	private static final String PROCESSOR_START_TIME = "processorStartTime";
	private static final String PROCESSOR_END_TIME = "processorEndTime";
	private static final String EXECUTION_TIME = "executionTime";
	private static final String EXECUTION_STATUS = "executionStatus";

	@Autowired
	private ZephyrProcessorRepository zephyrProcessorRepository;

	@Autowired
	private ZephyrConfig zephyrConfig;

	@Autowired
	private ZephyrDBService zephyrDBService;

	@Autowired
	private ZephyrClientFactory zephyrClientFactory;

	@Autowired
	private ProcessorToolConnectionService processorToolConnectionService;

	@Autowired
	private FieldMappingRepository fieldMappingRepository;

	@Autowired
	private ProjectBasicConfigRepository projectConfigRepository;

	private boolean executionStatus = true;
	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	/**
	 * Instantiates a new ZEPHYR processor job executor.
	 *
	 * @param taskScheduler
	 *            the task scheduler
	 */
	@Autowired
	protected ZephyrProcessorJobExecutor(TaskScheduler taskScheduler) {
		super(taskScheduler, ProcessorConstants.ZEPHYR);
	}

	private static List<ZephyrTestCaseDTO> filterTestCasesBsdOnFldrPth(Set<String> folderPathList,
			List<ZephyrTestCaseDTO> totalTestCasesList, boolean cloud) {
		List<ZephyrTestCaseDTO> filteredTestCasesList = new ArrayList<>();
		if (CollectionUtils.isEmpty(folderPathList)) {
			return totalTestCasesList;
		}
		totalTestCasesList.stream().forEach(testCases -> {
			Optional<String> folderName = Optional.ofNullable(testCases.getFolder());
			Optional<String> createdOnDate = Optional.ofNullable(testCases.getCreatedOn());
			Optional<String> updatedOnDate = Optional.ofNullable(testCases.getUpdatedOn());
			LocalDateTime instant = LocalDateTime.now();
			LocalDateTime currentDateMinus15Months = instant.minusMonths(15);
			folderPathList.forEach(folderPath -> {
				if (cloud) {
					if ((folderName.isPresent() && folderName.get().contains(folderPath)) && ((updatedOnDate.isPresent()
							&& DateUtil.stringToLocalDateTime(updatedOnDate.get(), DateUtil.TIME_FORMAT_WITH_SEC_DATE)
							.isAfter(currentDateMinus15Months))
							|| (createdOnDate.isPresent() && DateUtil
							.stringToLocalDateTime(createdOnDate.get(), DateUtil.TIME_FORMAT_WITH_SEC_DATE)
							.isAfter(currentDateMinus15Months)))) {
						filteredTestCasesList.add(testCases);
					}
				} else {
					if ((folderName.isPresent() && folderName.get().contains(folderPath)) && ((updatedOnDate.isPresent()
							&& DateUtil.stringToLocalDateTime(updatedOnDate.get(), DateUtil.TIME_FORMAT_WITH_SEC)
							.isAfter(currentDateMinus15Months))
							|| (createdOnDate.isPresent()
							&& DateUtil.stringToLocalDateTime(createdOnDate.get(), DateUtil.TIME_FORMAT_WITH_SEC)
							.isAfter(currentDateMinus15Months)))) {
						filteredTestCasesList.add(testCases);
					}
				}
			});
		});
		return filteredTestCasesList;
	}

	@Override
	public ZephyrProcessor getProcessor() {
		return ZephyrProcessor.prototype();
	}

	@Override
	public ProcessorRepository<ZephyrProcessor> getProcessorRepository() {
		return zephyrProcessorRepository;
	}

	@Override
	public String getCron() {
		return zephyrConfig.getCron();
	}

	/**
	 * Method to process information from config around Zephyr and fetch test cases
	 * specific to project and folder wise and save it into test_case_details
	 * collections.
	 */
	@Override
	public boolean execute(ZephyrProcessor processor) {
		executionStatus = true;
		long start = System.currentTimeMillis();
		String uid = UUID.randomUUID().toString();

		MDC.put(PROCESSOR_EXECUTION_UID, uid);
		log.info("[ZEPHYR Processor]. Started at: {}", start);

		List<ProjectBasicConfig> projectList = getSelectedProjects();
		MDC.put("TotalSelectedProjectsForProcessing", String.valueOf(projectList.size()));
		clearSelectedBasicProjectConfigIds();

		AtomicReference<Integer> testCaseCount = new AtomicReference<>(0);

		for (ProjectBasicConfig project : projectList) {
			log.info("Fetching data for project : {}", project.getProjectName());

			List<ProcessorToolConnection> projectConfigList = processorToolConnectionService
					.findByToolAndBasicProjectConfigId(ProcessorConstants.ZEPHYR, project.getId());
			ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(project.getId().toHexString());

			List<ProcessorToolConnection> processorToolConnectionList = projectConfigList.stream()
					.filter(projectConfig -> null != projectConfig.getConnectionId() && !projectConfig.isOffline())
					.collect(Collectors.toList());

			if (CollectionUtils.isNotEmpty(processorToolConnectionList)) {
				List<ProjectConfFieldMapping> onlineProjectConfigMap = createProjectConfigMap(
						processorToolConnectionList);

				onlineProjectConfigMap.forEach(projectConfigMap -> {
					try {
						MDC.put("project", projectConfigMap.getProjectName());
						log.info("Data for project : {}", projectConfigMap.getProjectName());
						processorExecutionTraceLog.setExecutionStartedAt(System.currentTimeMillis());

						if (StringUtils.isNotBlank(projectConfigMap.getProjectKey())) {
							testCaseCount.updateAndGet(test -> test + collectTestCases(projectConfigMap));
						}
						processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
						processorExecutionTraceLog.setExecutionSuccess(true);
						processorExecutionTraceLogService.save(processorExecutionTraceLog);
					} catch (RestClientException e) {
						executionStatus = false;
						processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
						processorExecutionTraceLog.setExecutionSuccess(executionStatus);
						processorExecutionTraceLogService.save(processorExecutionTraceLog);
						log.error("Got exception while getting response from endpoint: {}", e);
						MDC.put("status", "Fail");
					} finally {
						MDC.remove("project");
					}
				});
				MDC.put("projectsToProcess", String.valueOf(onlineProjectConfigMap.size()));
				MDC.put("status", "Success");
			}
		}

		if (testCaseCount.get() > 0) {
			cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.TESTING_KPI_CACHE);
		}
		long end = System.currentTimeMillis();
		MDC.put(PROCESSOR_START_TIME, String.valueOf(start));
		MDC.put(PROCESSOR_END_TIME, String.valueOf(end));
		MDC.put(EXECUTION_TIME, String.valueOf(end - start));
		MDC.put(EXECUTION_STATUS, String.valueOf(executionStatus));
		log.info("ZEPHYR Processor execution complete.");
		MDC.clear();
		return executionStatus;

	}

	@Override
	public boolean executeSprint(String sprintId) {
		return false;
	}

	/**
	 * @param projectConfigMap
	 * @return projectTestCountMap
	 */
	private int collectTestCases(final ProjectConfFieldMapping projectConfigMap) {
		AtomicReference<Integer> testCaseCountTotal = new AtomicReference<>(0);
		ProcessorToolConnection processorToolConnection = projectConfigMap.getProcessorToolConnection();
		ZephyrClient zephyrClient = zephyrClientFactory.getClient(processorToolConnection.isCloudEnv());
		Set<String> folderPathList = getAllFolderPathList(processorToolConnection);
		if (CollectionUtils.isNotEmpty(folderPathList)) {
				AtomicReference<Integer> testCaseCountFolderWise = new AtomicReference<>(0);
				// get testCases folder wise
				getTestCaseAndProcess(projectConfigMap, testCaseCountFolderWise, processorToolConnection, zephyrClient,
						folderPathList);
				testCaseCountTotal.updateAndGet(test -> test + testCaseCountFolderWise.get());

		} else {
			// get all testCases
			getTestCaseAndProcess(projectConfigMap, testCaseCountTotal, processorToolConnection, zephyrClient, null);
		}
		return testCaseCountTotal.get();
	}

	private void getTestCaseAndProcess(ProjectConfFieldMapping projectConfigMap, AtomicReference<Integer> testCaseCount,
			ProcessorToolConnection processorToolConnection, ZephyrClient zephyrClient, Set<String> folderPathList) {
		boolean isTestCaseEmpty = false;
		do {
			final List<ZephyrTestCaseDTO> testCase = zephyrClient.getTestCase(testCaseCount.get(), projectConfigMap);
			if (CollectionUtils.isNotEmpty(testCase)) {
				List<ZephyrTestCaseDTO> filteredTestCasesList = filterTestCasesBsdOnFldrPth(folderPathList, testCase,
						processorToolConnection.isCloudEnv());
				zephyrDBService.processTestCaseInfoToDB(filteredTestCasesList, processorToolConnection,
						projectConfigMap.isKanban(), processorToolConnection.isCloudEnv());
				testCaseCount.updateAndGet(test -> test + testCase.size());
				log.info("{} test cases are fetched and {} are matching with the folderPath:{}", testCase.size(),
						filteredTestCasesList.size(), folderPathList);
			} else {
				isTestCaseEmpty = true;
			}
			log.debug("Loop testCase count {} ", testCaseCount);
		} while (testCaseCount.get() >= zephyrConfig.getPageSize() && !isTestCaseEmpty);
	}

	/**
	 * @param processorToolConnection
	 * @return folderPathList
	 */
	private Set<String> getAllFolderPathList(ProcessorToolConnection processorToolConnection) {
		Set<String> folderPathList = new HashSet<>();
		if (CollectionUtils.isNotEmpty(processorToolConnection.getInSprintAutomationFolderPath())) {
			folderPathList.addAll(processorToolConnection.getInSprintAutomationFolderPath());
		}
		if (CollectionUtils.isNotEmpty(processorToolConnection.getRegressionAutomationFolderPath())) {
			folderPathList.addAll(processorToolConnection.getRegressionAutomationFolderPath());
		}
		return folderPathList;
	}

	/**
	 * Gets project conf Field Mapping
	 *
	 * @param projectConfigList
	 *            {@link ProcessorToolConnection}
	 * @return {@link ProjectConfFieldMapping}
	 */
	private List<ProjectConfFieldMapping> createProjectConfigMap(
			final List<ProcessorToolConnection> projectConfigList) {
		List<ProjectConfFieldMapping> projectConfigMap = new ArrayList<>();
		for (ProcessorToolConnection projectConfig : projectConfigList) {
			ProjectConfFieldMapping projectConfFieldMapping = new ProjectConfFieldMapping();
			Optional<ProjectBasicConfig> projectBasicId = projectConfigRepository
					.findById(projectConfig.getBasicProjectConfigId());
			projectConfFieldMapping.setProcessorToolConnection(projectConfig);

			if (null != projectConfig.getProjectKey()) {
				projectConfFieldMapping.setProjectKey(projectConfig.getProjectKey());
			}
			if (projectBasicId.isPresent()) {
				if (null != projectBasicId.get().getProjectName()) {
					projectConfFieldMapping.setProjectName(projectBasicId.get().getProjectName());
				}

				projectConfFieldMapping.setBasicProjectConfigId(projectBasicId.get().getId());
				projectConfFieldMapping.setKanban(projectBasicId.get().getIsKanban());
			}
			projectConfigMap.add(projectConfFieldMapping);
		}
		return projectConfigMap;
	}

	private ProcessorExecutionTraceLog createTraceLog(String basicProjectConfigId) {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.ZEPHYR);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
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
	private void cacheRestClient(final String cacheEndPoint, final String cacheName) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(zephyrConfig.getCustomApiBaseUrl());
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
			log.error("[ZEPHYR-CUSTOMAPI-CACHE-EVICT]. Error while consuming rest service {}", e);
		}

		if (null != response && response.getStatusCode().is2xxSuccessful()) {
			log.info("[ZEPHYR-CUSTOMAPI-CACHE-EVICT]. Successfully evicted cache: {} ", cacheName);
		} else {
			log.error("[ZEPHYR-CUSTOMAPI-CACHE-EVICT]. Error while evicting cache: {}", cacheName);
		}

		clearToolItemCache(zephyrConfig.getCustomApiBaseUrl());
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
