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

package com.publicissapient.kpidashboard.jiratest.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.jiratest.config.JiraTestProcessorConfig;
import com.publicissapient.kpidashboard.jiratest.model.JiraTestProcessor;
import com.publicissapient.kpidashboard.jiratest.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jiratest.processor.service.JiraTestService;
import com.publicissapient.kpidashboard.jiratest.repository.JiraTestProcessorRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * The Job executor class which starts the execution of zephyr processor.
 */
@Component
@Slf4j
public class JiraTestProcessorJobExecutor extends ProcessorJobExecutor<JiraTestProcessor> {
	private static final String PROCESSOR_EXECUTION_UID = "processorExecutionUid";
	private static final String PROCESSOR_START_TIME = "processorStartTime";
	private static final String PROCESSOR_END_TIME = "processorEndTime";
	private static final String EXECUTION_TIME = "executionTime";
	private static final String EXECUTION_STATUS = "executionStatus";
	@Autowired
	private ProcessorToolConnectionService processorToolConnectionService;

	@Autowired
	private FieldMappingRepository fieldMappingRepository;

	@Autowired
	private ProjectBasicConfigRepository projectConfigRepository;

	@Autowired
	private ConnectionRepository connectionRepository;

	@Autowired
	private ProjectToolConfigRepository toolRepository;

	@Autowired
	private JiraTestProcessorRepository jiraTestProcessorRepository;

	@Autowired
	private JiraTestProcessorConfig jiraTestProcessorConfig;

	@Autowired
	private JiraTestService jiraTestService;

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
	protected JiraTestProcessorJobExecutor(TaskScheduler taskScheduler) {
		super(taskScheduler, ProcessorConstants.JIRA_TEST);
	}

	@Override
	public JiraTestProcessor getProcessor() {
		return JiraTestProcessor.prototype();
	}

	@Override
	public ProcessorRepository<JiraTestProcessor> getProcessorRepository() {
		return jiraTestProcessorRepository;
	}

	@Override
	public String getCron() {
		return jiraTestProcessorConfig.getCron();
	}

	/**
	 * Method to process information from config around Zephyr and fetch test cases
	 * specific to project and folder wise and save it into test_case_details
	 * collections.
	 */
	@Override
	public boolean execute(JiraTestProcessor processor) {
		executionStatus = true;
		long start = System.currentTimeMillis();
		String uid = UUID.randomUUID().toString();

		MDC.put(PROCESSOR_EXECUTION_UID, uid);
		log.info("[JIRA TEST Processor]. Started at: {}", start);

		List<ProjectBasicConfig> projectList = getSelectedProjects();
		MDC.put("TotalSelectedProjectsForProcessing", String.valueOf(projectList.size()));
		clearSelectedBasicProjectConfigIds();

		AtomicReference<Integer> testCaseCount = new AtomicReference<>(0);

		for (ProjectBasicConfig project : projectList) {
			log.info("Fetching data for project : {}", project.getProjectName());

			List<ProcessorToolConnection> projectConfigList = processorToolConnectionService
					.findByToolAndBasicProjectConfigId(ProcessorConstants.JIRA_TEST, project.getId());
			ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(project.getId().toHexString());

			List<ProcessorToolConnection> processorToolConnectionList = projectConfigList.stream()
					.filter(projectConfig -> null != projectConfig.getConnectionId()).collect(Collectors.toList());

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
		log.info("JIRA TEST Processor execution complete.");
		MDC.clear();
		return executionStatus;

	}

	@Override
	public boolean executeSprint(String sprintId) {
		return false;
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

	private int collectTestCases(final ProjectConfFieldMapping projectConfig) {
		AtomicReference<Integer> testCaseCountTotal = new AtomicReference<>(0);
		if (projectConfig.getProjectKey() != null && projectConfig.getProcessorToolConnection() != null) {
			long storyDataStart = System.currentTimeMillis();
			MDC.put("storyDataStartTime", String.valueOf(storyDataStart));
			int count = jiraTestService.processesJiraIssues(projectConfig);
			testCaseCountTotal.set(count);
			MDC.put("JiraIssueCount", String.valueOf(count));
			long end = System.currentTimeMillis();
			MDC.put("storyDataEndTime", String.valueOf(end));
		}
		return testCaseCountTotal.get();
	}

	private ProcessorExecutionTraceLog createTraceLog(String basicProjectConfigId) {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.JIRA_TEST);
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
	public void cacheRestClient(final String cacheEndPoint, final String cacheName) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromHttpUrl(jiraTestProcessorConfig.getCustomApiBaseUrl());
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

		clearToolItemCache(jiraTestProcessorConfig.getCustomApiBaseUrl());
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
