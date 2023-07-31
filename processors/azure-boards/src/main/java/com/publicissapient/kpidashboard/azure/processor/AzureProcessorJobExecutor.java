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

package com.publicissapient.kpidashboard.azure.processor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.azure.adapter.helper.AzureRestClientFactory;
import com.publicissapient.kpidashboard.azure.config.AzureProcessorConfig;
import com.publicissapient.kpidashboard.azure.model.AzureProcessor;
import com.publicissapient.kpidashboard.azure.processor.mode.ModeBasedProcessor;
import com.publicissapient.kpidashboard.azure.repository.AzureProcessorRepository;
import com.publicissapient.kpidashboard.azure.util.AzureConstants;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.executor.ProcessorJobExecutor;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Collects {@link AzureProcessor} data from feature content source system.
 */
@Component
@Slf4j
public class AzureProcessorJobExecutor extends ProcessorJobExecutor<AzureProcessor> {

	@Autowired
	private ProjectBasicConfigRepository projectConfigRepository;

	@Autowired
	private AzureProcessorRepository issueProcessorRepository;

	@Autowired
	private AzureProcessorConfig azureProcessorConfig;

	@Autowired
	private List<ModeBasedProcessor> modeBasedProcessors;

	@Autowired
	private AzureRestClientFactory azureRestClientFactory;

	@Autowired
	public AzureProcessorJobExecutor(TaskScheduler taskScheduler) {
		super(taskScheduler, ProcessorConstants.AZURE);
	}

	@Override
	public AzureProcessor getProcessor() {
		return AzureProcessor.prototype();
	}

	@Override
	public ProcessorRepository<AzureProcessor> getProcessorRepository() {
		return issueProcessorRepository;
	}

	/**
	 * Gets current chronology setting, for the scheduler
	 */
	@Override
	public String getCron() {
		return azureProcessorConfig.getCron();
	}

	/**
	 * Gets called on a schedule to gather data from the feature content source
	 * system and update the repository with retrieved data.
	 * 
	 * @param azureProcessor
	 *            azureProcessor instance
	 */
	@Override
	public boolean execute(AzureProcessor azureProcessor) {
		boolean executionStatus = true;
		long start = System.currentTimeMillis();
		String uid = UUID.randomUUID().toString();
		MDC.put("processorExecutionUid", uid);
		MDC.put("processorStartTime", String.valueOf(start));
		List<ProjectBasicConfig> projectConfigList = getSelectedProjects();
		MDC.put("TotalSelectedProjectsForProcessing", String.valueOf(projectConfigList.size()));
		clearSelectedBasicProjectConfigIds();

		fetchIssueDetail(executionStatus, projectConfigList);

		long endTime = System.currentTimeMillis();
		MDC.put("processorEndTime", String.valueOf(endTime));
		MDC.put("executionTime", String.valueOf(endTime - start));
		MDC.put("executionStatus", String.valueOf(executionStatus));
		log.info("Azure execution completed");
		MDC.clear();
		return executionStatus;
	}

	@Override
	public boolean executeSprint(String sprintId) {
		return false;
	}

	private boolean fetchIssueDetail(boolean executionStatus, List<ProjectBasicConfig> projectConfigList) {
		AtomicReference<Integer> scrumIssueCount = new AtomicReference<>(0);
		AtomicReference<Integer> kanbanIssueCount = new AtomicReference<>(0);

		if (!modeBasedProcessors.isEmpty()) {
			try {
				modeBasedProcessors.parallelStream().forEach(modeBasedProcessor -> {
					Map<String, Integer> issueCountMap = modeBasedProcessor.validateAndCollectIssues(projectConfigList);
					scrumIssueCount.updateAndGet(v -> v + issueCountMap.get(AzureConstants.SCRUM_DATA));
					kanbanIssueCount.updateAndGet(v -> v + issueCountMap.get(AzureConstants.KANBAN_DATA));
				});
			} catch (RuntimeException e) {
				log.error("Got error while validateAndCollectIssues", e);
				MDC.put("error", e.getMessage());
				executionStatus = false;
			}
		}

		// Cleaning Cache
		if (scrumIssueCount.get() > 0) {
			azureRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
					CommonConstant.CACHE_ACCOUNT_HIERARCHY);
			azureRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.JIRA_KPI_CACHE);
		}
		if (kanbanIssueCount.get() > 0) {
			azureRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
					CommonConstant.CACHE_ACCOUNT_HIERARCHY_KANBAN);
			azureRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
					CommonConstant.JIRAKANBAN_KPI_CACHE);
		}
		return executionStatus;
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