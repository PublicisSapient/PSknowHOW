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

package com.publicissapient.kpidashboard.sonar.processor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
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
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.sonar.SonarDetails;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
import com.publicissapient.kpidashboard.common.repository.sonar.SonarDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.sonar.SonarHistoryRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.sonar.config.SonarConfig;
import com.publicissapient.kpidashboard.sonar.factory.SonarClientFactory;
import com.publicissapient.kpidashboard.sonar.model.SonarProcessor;
import com.publicissapient.kpidashboard.sonar.model.SonarProcessorItem;
import com.publicissapient.kpidashboard.sonar.processor.adapter.SonarClient;
import com.publicissapient.kpidashboard.sonar.repository.SonarProcessorItemRepository;
import com.publicissapient.kpidashboard.sonar.repository.SonarProcessorRepository;
import com.publicissapient.kpidashboard.sonar.util.SonarProcessorUtils;
import com.publicissapient.kpidashboard.sonar.util.SonarUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * SonarProcessorJobExecutor that fetches sonar metrics information from Sonar.
 *
 */
@Component
@Slf4j
public class SonarProcessorJobExecutor extends ProcessorJobExecutor<SonarProcessor> {

	private static final String KEY = "key";
	private static final String INSTANCE_URL = "instanceUrl";
	private static final String PROCESSOR_END_TIME = "processorEndTime";
	private static final String EXECUTION_TIME = "executionTime";
	private static final String EXECUTION_STATUS = "executionStatus";
	private static final String PROCESSOR_EXECUTION_UID = "processorExecutionUid";
	private static final String PROCESSOR_START_TIME = "processorStartTime";
	private static final String PROJECT_KEY = "projectKey";
	private static final String SONAR_URL = "sonarUrl";
	private static final String TOTAL_FETCHED_PROJECTS = "totalFetchedProjects";

	@Autowired
	private SonarProcessorRepository sonarProcessorRepository;

	@Autowired
	private SonarProcessorItemRepository sonarProcessorItemRepository;

	@Autowired
	private SonarDetailsRepository sonarDetailsRepository;

	@Autowired
	private SonarClientFactory sonarClientFactory;

	@Autowired
	private SonarConfig sonarConfig;

	@Autowired
	private SonarHistoryRepository sonarHistoryRepository;

	@Autowired
	private AesEncryptionService aesEncryptionService;

	@Autowired
	private ProcessorToolConnectionService processorToolConnectionService;

	@Autowired
	private ProjectBasicConfigRepository projectConfigRepository;

	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Autowired
	private ToolCredentialProvider toolCredentialProvider;

	/**
	 * Instantiate SonarProcessorJobExecutor.
	 *
	 * @param scheduler
	 *            the task scheduler
	 */
	@Autowired
	public SonarProcessorJobExecutor(TaskScheduler scheduler) {
		super(scheduler, ProcessorConstants.SONAR);

	}

	/**
	 * Provides SonarProcessor.
	 *
	 * @return the SonarProcessor
	 *
	 */
	@Override
	public SonarProcessor getProcessor() {
		return SonarProcessor.getSonarConfig(sonarConfig.getMetrics());
	}

	/**
	 * Provides ProcessorRepository.
	 *
	 * @return the ProcessorRepository
	 */
	@Override
	public ProcessorRepository<SonarProcessor> getProcessorRepository() {
		return sonarProcessorRepository;
	}

	/**
	 * Provides cron expression.
	 *
	 * @return the cron expression from Sonar setting
	 */
	@Override
	public String getCron() {
		return sonarConfig.getCron();
	}

	/**
	 * Processes sonar data from Sonar server.
	 *
	 * @param processor
	 *            the sonar processor
	 */
	@Override
	public boolean execute(SonarProcessor processor) {
		boolean executionStatus = true;
		long startTime = System.currentTimeMillis();
		String uid = UUID.randomUUID().toString();
		MDC.put(PROCESSOR_EXECUTION_UID, uid);
		MDC.put(PROCESSOR_START_TIME, String.valueOf(startTime));

		List<ProjectBasicConfig> projectConfigList = getSelectedProjects();
		clearSelectedBasicProjectConfigIds();
		AtomicReference<Integer> count = new AtomicReference<>(0);

		List<SonarProcessorItem> existingProcessorItems = sonarProcessorItemRepository
				.findByProcessorId(processor.getId());

		cleanUnusedProcessorItem(existingProcessorItems);

		for (ProjectBasicConfig proBasicConfig : projectConfigList) {

			List<ProcessorToolConnection> sonarServerList = processorToolConnectionService
					.findByToolAndBasicProjectConfigId(ProcessorConstants.SONAR, proBasicConfig.getId());

			for (ProcessorToolConnection sonar : sonarServerList) {
				ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(
						proBasicConfig.getId().toHexString());
				try {
					MDC.put(SONAR_URL, sonar.getUrl());
					processorExecutionTraceLog.setExecutionStartedAt(startTime);
					sonar.setPassword(decryptPassword(sonar.getPassword()));
					sonar.setAccessToken(decryptPassword(sonar.getAccessToken()));

					SonarClient sonarClient = sonarClientFactory.getSonarClient(sonar.getApiVersion());
					List<SonarProcessorItem> projects = sonarClient.getSonarProjectList(sonar);

					int projSize = ((CollectionUtils.isEmpty(projects)) ? 0 : projects.size());

					List<SonarProcessorItem> toBeEnabledJob = new ArrayList<>();
					setActiveSonarProjects(sonar, projects, toBeEnabledJob);

					// add only projects which are linked with toolconfigid
					addNewProjects(toBeEnabledJob, existingProcessorItems, processor);
					List<SonarProcessorItem> enableProjectList = sonarProcessorItemRepository
							.findEnabledProjectsForTool(processor.getId(), sonar.getId(), sonar.getUrl());
					int updatedCount = saveSonarDetails(enableProjectList, sonar, sonarClient,
							sonarConfig.getMetrics().get(0));
					count.updateAndGet(v -> v + updatedCount);

					saveSonarHistory(enableProjectList, sonar, sonarClient, sonarConfig.getMetrics().get(0));

					MDC.put(TOTAL_FETCHED_PROJECTS, String.valueOf(projSize));
					processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
					processorExecutionTraceLog.setExecutionSuccess(true);
					processorExecutionTraceLogService.save(processorExecutionTraceLog);
				} catch (Exception ex) {
					String errorMessage = "Exception in sonar project: url - " + sonar.getUrl() + ", user - "
							+ sonar.getUsername() + ", toolId - " + sonar.getId() + ", Exception is - "
							+ ex.getMessage();
					executionStatus = false;
					processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
					processorExecutionTraceLog.setExecutionSuccess(executionStatus);
					processorExecutionTraceLogService.save(processorExecutionTraceLog);
					log.error(errorMessage, ex);
				}
			}
		}

		if (count.get() > 0) {
			cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.SONAR_KPI_CACHE);
		}

		long endTime = System.currentTimeMillis();

		MDC.put(PROCESSOR_END_TIME, String.valueOf(endTime));
		MDC.put(EXECUTION_TIME, String.valueOf(endTime - startTime));
		MDC.put(EXECUTION_STATUS, String.valueOf(executionStatus));
		log.info("Sonar Processor execution completed");
		MDC.clear();
		return executionStatus;
	}

	@Override
	public boolean executeSprint(String sprintId) {
		return false;
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

	/**
	 * Update sonar projects active and set tool config.
	 *
	 * @param sonar
	 *            the Sonar server configuration details
	 * @param projects
	 *            the list of Sonar project configuration
	 * @param toBeEnabledJob
	 *            the enabled Sonar job
	 */
	private void setActiveSonarProjects(ProcessorToolConnection sonar, List<SonarProcessorItem> projects,
			List<SonarProcessorItem> toBeEnabledJob) {
		for (SonarProcessorItem sp : projects) {
			if (sonar.getUrl().equals(sp.getInstanceUrl()) && sonar.getProjectKey().equals(sp.getKey())) {
				sp.setActive(true);
				sp.setToolConfigId(sonar.getId());
				toBeEnabledJob.add(sp);
			}
		}
	}

	private ProcessorExecutionTraceLog createTraceLog(String basicProjectConfigId) {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.SONAR);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		return processorExecutionTraceLog;
	}

	/**
	 * Saves the latest sonar details.
	 *
	 * @param sonarProjects
	 *            the list of Sonar project
	 * @param sonarServer
	 *            the Sonar server configuration
	 * @param sonarClient
	 *            the Sonar client
	 * @param metrics
	 *            the metrics
	 * @return count of data saved in db
	 */
	private int saveSonarDetails(List<SonarProcessorItem> sonarProjects, ProcessorToolConnection sonarServer,
			SonarClient sonarClient, String metrics) {
		int count = 0;
		for (SonarProcessorItem project : sonarProjects) {

			SonarDetails sonarDetailsMetrics = getSonarDetails(sonarServer, sonarClient, metrics, project);
			SonarDetails existingSonarDetails = getExistingSonarData(project);
			if (sonarDetailsMetrics != null) {
				if (existingSonarDetails != null) {
					sonarDetailsMetrics.setId(existingSonarDetails.getId());
				}
				sonarDetailsMetrics.setProcessorItemId(project.getId());
				sonarDetailsRepository.save(sonarDetailsMetrics);
				MDC.put(PROJECT_KEY, project.getKey());
				count++;
			}
		}
		return count;
	}

	private SonarDetails getSonarDetails(ProcessorToolConnection sonarServer, SonarClient sonarClient, String metrics,
			SonarProcessorItem project) {
		SonarDetails sonarDetailsMetrics;

		if (sonarServer.isCloudEnv()) {
			sonarDetailsMetrics = sonarClient.getLatestSonarDetails(project,
					new HttpEntity<>(SonarProcessorUtils.getHeaders(sonarServer.getAccessToken())), metrics);
		} else if (!sonarServer.isCloudEnv() && sonarServer.isAccessTokenEnabled()) {
			sonarDetailsMetrics = sonarClient.getLatestSonarDetails(project,
					new HttpEntity<>(SonarProcessorUtils.getHeaders(sonarServer.getAccessToken(), true)), metrics);
		} else {
			sonarDetailsMetrics = sonarClient.getLatestSonarDetails(project,
					new HttpEntity<>(
							SonarProcessorUtils.getHeaders(sonarServer.getUsername(), sonarServer.getPassword())),
					metrics);
		}
		return sonarDetailsMetrics;
	}

	/**
	 * Saves sonar history data.
	 *
	 * @param sonarProjects
	 *            the list of Sonar project
	 * @param sonarServer
	 *            the Sonar server configuration
	 * @param sonarClient
	 *            the Sonar client
	 * @param metrics
	 *            the metrics
	 * @return the count of code quality history data
	 */
	private int saveSonarHistory(List<SonarProcessorItem> sonarProjects, ProcessorToolConnection sonarServer,
			SonarClient sonarClient, String metrics) {
		int cnt = 0;
		for (SonarProcessorItem ci : sonarProjects) {
			log.info("Looking for Job: {}", ci.getDesc() + " " + ci.getToolConfigId().toString());
			Date date = new Date(ci.getUpdatedTime());
			long diffInMillies = Math.abs(date.getTime() - new Date().getTime());
			long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
			if (ci.getToolDetailsMap().get(INSTANCE_URL) == null || ci.getToolDetailsMap().get(KEY) == null
					|| diff < 1) {
				continue;
			}

			List<SonarHistory> sonarHistoryList = getSonarHistory(sonarServer, sonarClient, metrics, ci);
			if (CollectionUtils.isNotEmpty(sonarHistoryList)) {
				sonarHistoryRepository.saveAll(sonarHistoryList);
				ci.setUpdatedTime(new Date().getTime());
				sonarProcessorItemRepository.save(ci);
				cnt++;
				log.info("Updated Job: {}", ci.getDesc());
			}
		}
		log.info("History Data saved : {}", cnt);
		return cnt;
	}

	private List<SonarHistory> getSonarHistory(ProcessorToolConnection sonarServer, SonarClient sonarClient,
			String metrics, SonarProcessorItem ci) {

		ToolCredential toolCredentials = SonarUtils.getToolCredentials(toolCredentialProvider, sonarServer);
		String username = toolCredentials.getUsername();
		String password = toolCredentials.getPassword();

		List<SonarHistory> sonarHistoryList;
		if (sonarServer.isCloudEnv()) {
			sonarHistoryList = sonarClient.getPastSonarDetails(ci,
					new HttpEntity<>(SonarProcessorUtils.getHeaders(password)), metrics);

		} else if (!sonarServer.isCloudEnv() && sonarServer.isAccessTokenEnabled()) {
			sonarHistoryList = sonarClient.getPastSonarDetails(ci,
					new HttpEntity<>(SonarProcessorUtils.getHeaders(password, true)), metrics);
		} else {
			sonarHistoryList = sonarClient.getPastSonarDetails(ci,
					new HttpEntity<>(SonarProcessorUtils.getHeaders(username, password)), metrics);
		}
		return sonarHistoryList;
	}

	/**
	 * Decrypt jira password as plain text
	 *
	 * @param encryptedPassword
	 *            encrypted password
	 * @return plain text password
	 */
	private String decryptPassword(String encryptedPassword) {
		return aesEncryptionService.decrypt(encryptedPassword, sonarConfig.getAesEncryptionKey());
	}

	/**
	 * Adds new project in processor item collection.
	 *
	 * @param sonarDetailsList
	 *            the list of Sonar project
	 * @param existingProjectList
	 *            the existing Sonar project
	 * @param processor
	 *            the processor
	 */
	private void addNewProjects(List<SonarProcessorItem> sonarDetailsList, List<SonarProcessorItem> existingProjectList,
			SonarProcessor processor) {
		long startTime = System.currentTimeMillis();
		int cnt = 0;
		List<SonarProcessorItem> newProjectList = new ArrayList<>();
		for (SonarProcessorItem sonarItem : sonarDetailsList) {
			if (!existingProjectList.contains(sonarItem)) {
				sonarItem.setProcessorId(processor.getId());
				sonarItem.setActive(true);
				sonarItem.setDesc(sonarItem.getProjectName());
				sonarItem.setKey(sonarItem.getKey());
				newProjectList.add(sonarItem);
				cnt++;
			}
		}

		if (!CollectionUtils.isEmpty(newProjectList)) {
			sonarProcessorItemRepository.saveAll(newProjectList);
		}
		log.info("New projects : {} , {} ", startTime, cnt);
	}

	/**
	 * Checks if sonar data
	 *
	 * @param project
	 *            the Sonar project
	 * @return boolean
	 */
	private SonarDetails getExistingSonarData(SonarProcessorItem project) {
		return sonarDetailsRepository.findByProcessorItemId(project.getId());
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

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(sonarConfig.getCustomApiBaseUrl());
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
			log.error("[SONAR-CUSTOMAPI-CACHE-EVICT]. Error while consuming rest service {}", e);
		}

		if (null != response && response.getStatusCode().is2xxSuccessful()) {
			log.info("[SONAR-CUSTOMAPI-CACHE-EVICT]. Successfully evicted cache: {} ", cacheName);
		} else {
			log.error("[SONAR-CUSTOMAPI-CACHE-EVICT]. Error while evicting cache: {}", cacheName);
		}

		clearToolItemCache(sonarConfig.getCustomApiBaseUrl());
	}

	/**
	 * Removes toolConfigId and make enabled to false for unused jobs.
	 *
	 * @param existingProcessorItems
	 *            the existing Sonar Project *
	 */
	private void cleanUnusedProcessorItem(List<SonarProcessorItem> existingProcessorItems) {

		List<ProcessorToolConnection> existingSonarTools = processorToolConnectionService
				.findByTool(ProcessorConstants.SONAR);

		List<SonarProcessorItem> abandonedProcessorItems = CollectionUtils.emptyIfNull(existingProcessorItems).stream()
				.filter(processorItem -> isToolInfoNotExists(processorItem, existingSonarTools))
				.collect(Collectors.toList());

		if (CollectionUtils.isNotEmpty(abandonedProcessorItems)) {
			inactivateProcessorItems(abandonedProcessorItems);
			sonarProcessorItemRepository.saveAll(abandonedProcessorItems);
		}
	}

	private void inactivateProcessorItems(List<SonarProcessorItem> abandonedProcessorItems) {
		abandonedProcessorItems.forEach(item -> {
			item.setActive(false);
			item.setToolConfigId(null);
			log.info("Remove ToolConfig id from Job: {}", item.getId());
		});
	}

	private boolean isToolInfoNotExists(SonarProcessorItem processorItem,
			List<ProcessorToolConnection> existingProcessorToolConnections) {
		if (CollectionUtils.isEmpty(existingProcessorToolConnections)) {
			return false;
		}

		return existingProcessorToolConnections.stream().noneMatch(
				processorToolConnection -> processorToolConnection.getUrl().equals(processorItem.getInstanceUrl())
						&& processorToolConnection.getProjectKey().equals(processorItem.getKey()));
	}

	private void clearSelectedBasicProjectConfigIds() {
		setProjectsBasicConfigIds(null);
	}
}
