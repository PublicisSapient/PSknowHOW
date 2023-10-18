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

package com.publicissapient.kpidashboard.jira.processor;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.jira.service.FetchSprintDataServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.context.ExecutionLogContext;
import com.publicissapient.kpidashboard.common.executor.ProcessorJobExecutor;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jira.adapter.helper.JiraRestClientFactory;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraProcessor;
import com.publicissapient.kpidashboard.jira.processor.mode.ModeBasedProcessor;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * Collects {@link JiraProcessor} data from feature content source system.
 */
@Component
@Slf4j
public class JiraProcessorJobExecutor extends ProcessorJobExecutor<JiraProcessor> {
	PSLogData psLogData = new PSLogData();
	@Autowired
	private ProjectBasicConfigRepository projectConfigRepository;

	@Autowired
	private JiraProcessorRepository issueProcessorRepository;

	@Autowired
	private JiraProcessorConfig jiraProcessorConfig;

	@Autowired
	private List<ModeBasedProcessor> modeBasedProcessors;

	@Autowired
	private JiraRestClientFactory jiraRestClientFactory;

	@Autowired
	public JiraProcessorJobExecutor(TaskScheduler taskScheduler) {
		super(taskScheduler, ProcessorConstants.JIRA);
	}

	@Autowired
	FetchSprintDataServiceImpl fetchSprintDataServiceImpl;

	@Override
	public JiraProcessor getProcessor() {
		return JiraProcessor.prototype();
	}

	@Override
	public ProcessorRepository<JiraProcessor> getProcessorRepository() {
		return issueProcessorRepository;
	}

	/**
	 * Gets current chronology setting, for the scheduler
	 */
	@Override
	public String getCron() {
		return jiraProcessorConfig.getCron();
	}

	/**
	 * Gets called on a schedule to gather data from the feature content source
	 * system and update the repository with retrieved data.
	 * 
	 * @param jiraProcessor
	 *            jiraProcessor instance
	 */
	@Override
	public boolean execute(JiraProcessor jiraProcessor) {
		boolean executionStatus = true;
		long start = System.currentTimeMillis();
		String uid = UUID.randomUUID().toString();
		List<ProjectBasicConfig> projectConfigList = getSelectedProjects();
		// change 2--
		if (ObjectUtils.isNotEmpty(getExecutionLogContext())
				&& (StringUtils.isNotEmpty(getExecutionLogContext().getRequestId()))) {
			// setting execution context as per user request
			getExecutionLogContext().setIsCron("false");
		} else {
			// setting execution context as per for cron job uuid
			ExecutionLogContext cronExecutionContext = new ExecutionLogContext();
			cronExecutionContext.setRequestId(uid);
			cronExecutionContext.setIsCron("true");
			setExecutionLogContext(cronExecutionContext);
		}
		psLogData.setProcessorStartTime(DateUtil.convertMillisToDateTime(start));
		log.info("Jira Processor Started", kv(CommonConstant.PSLOGDATA, psLogData));

		clearSelectedBasicProjectConfigIds();
		ExecutionLogContext executionLocalLogContext = getExecutionLogContext();
		fetchIssueDetail(executionStatus, projectConfigList, executionLocalLogContext);

		long endTime = System.currentTimeMillis();
		psLogData.setProcessorEndTime(DateUtil.convertMillisToDateTime(endTime));
		psLogData.setTimeTaken(String.valueOf(endTime - start));
		psLogData.setExecutionStatus(String.valueOf(executionStatus));
		ExecutionLogContext.updateContext(executionLocalLogContext);
		log.info("Jira execution completed", kv(CommonConstant.PSLOGDATA, psLogData));
		// Change 6-- clear Execution context
		ExecutionLogContext.getContext().destroy();
		destroyLogContext();
		MDC.clear();
		return executionStatus;
	}

	@Override
	public boolean executeSprint(String sprintId) {

		long start = System.currentTimeMillis();

		psLogData.setProcessorStartTime(DateUtil.convertMillisToDateTime(start));
		log.info("Jira Processor Started for sprint fetch", kv(CommonConstant.PSLOGDATA, psLogData));

		boolean executionStatus = fetchSprintDataServiceImpl.fetchSprintData(sprintId);

		long endTime = System.currentTimeMillis();
		psLogData.setProcessorEndTime(DateUtil.convertMillisToDateTime(endTime));
		psLogData.setTimeTaken(String.valueOf(endTime - start));
		psLogData.setExecutionStatus(String.valueOf(executionStatus));
		log.info("Jira execution completed for sprint fetch", kv(CommonConstant.PSLOGDATA, psLogData));

		MDC.clear();
		return executionStatus;

	}

	/**
	 * @param executionStatus
	 * @param projectConfigList
	 * @param executionLogContext
	 * @return
	 */
	private boolean fetchIssueDetail(boolean executionStatus, List<ProjectBasicConfig> projectConfigList,
			ExecutionLogContext executionLogContext) {
		AtomicReference<Integer> scrumIssueCount = new AtomicReference<>(0);
		AtomicReference<Integer> kanbanIssueCount = new AtomicReference<>(0);

		if (!modeBasedProcessors.isEmpty() && CollectionUtils.isNotEmpty(projectConfigList)) {
			try {
				modeBasedProcessors.parallelStream().forEach(modeBasedProcessor -> {
					modeBasedProcessor.setExecutionLogContext(executionLogContext);
					Map<String, Integer> issueCountMap = modeBasedProcessor.validateAndCollectIssues(projectConfigList);
					scrumIssueCount.updateAndGet(v -> v + issueCountMap.get(JiraConstants.SCRUM_DATA));
					kanbanIssueCount.updateAndGet(v -> v + issueCountMap.get(JiraConstants.KANBAN_DATA));
				});
			} catch (RuntimeException e) {
				log.error("Got error while validateAndCollectIssues", e);
				executionStatus = false;
			}
		}

		// Cleaning Cache
		if (scrumIssueCount.get() > 0 || executionStatus) {
			jiraRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
					CommonConstant.CACHE_ACCOUNT_HIERARCHY);
			jiraRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.JIRA_KPI_CACHE);
		}
		if (kanbanIssueCount.get() > 0 || executionStatus) {
			jiraRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
					CommonConstant.CACHE_ACCOUNT_HIERARCHY_KANBAN);
			jiraRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
					CommonConstant.JIRAKANBAN_KPI_CACHE);
		}
		return executionStatus;
	}

	private List<ProjectBasicConfig> getSelectedProjects() {
		List<ProjectBasicConfig> allProjects = projectConfigRepository.findAll();

		psLogData.setTotalConfiguredProject(String.valueOf(CollectionUtils.emptyIfNull(allProjects).size()));
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
