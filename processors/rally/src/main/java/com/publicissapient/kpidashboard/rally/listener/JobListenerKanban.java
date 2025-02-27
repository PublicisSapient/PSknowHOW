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
package com.publicissapient.kpidashboard.rally.listener;

import static com.publicissapient.kpidashboard.rally.helper.RallyHelper.convertDateToCustomFormat;
import static com.publicissapient.kpidashboard.rally.util.JiraProcessorUtil.generateLogMessage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.rally.cache.RallyProcessorCacheEvictor;
import com.publicissapient.kpidashboard.rally.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.rally.config.RallyProcessorConfig;
import com.publicissapient.kpidashboard.rally.constant.RallyConstants;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.rally.service.JiraClientService;
import com.publicissapient.kpidashboard.rally.service.RallyCommonService;
import com.publicissapient.kpidashboard.rally.service.NotificationHandler;
import com.publicissapient.kpidashboard.rally.service.OngoingExecutionsService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;

import io.atlassian.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

/**
 * @author purgupta2
 */
@Component
@Slf4j
@JobScope
public class JobListenerKanban implements JobExecutionListener {

	@Autowired
	private NotificationHandler handler;

	@Value("#{jobParameters['projectId']}")
	private String projectId;

	@Autowired
	private FieldMappingRepository fieldMappingRepository;

	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

	@Autowired
	private RallyProcessorCacheEvictor rallyProcessorCacheEvictor;

	@Autowired
	private OngoingExecutionsService ongoingExecutionsService;

	@Autowired
	private RallyProcessorConfig rallyProcessorConfig;

	@Autowired
	private ProjectBasicConfigRepository projectBasicConfigRepo;

	@Autowired
	private RallyCommonService rallyCommonService;

	@Autowired
	JiraClientService jiraClientService;

	@Autowired
	KanbanJiraIssueRepository kanbanJiraIssueRepository;

	@Autowired
	FetchProjectConfiguration fetchProjectConfiguration;

	@Override
	public void beforeJob(JobExecution jobExecution) {
		// in future we can use this method to do something before job execution starts
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.batch.core.listener.JobExecutionListenerSupport#afterJob(
	 * org.springframework.batch.core.JobExecution)
	 */
	@Override
	public void afterJob(JobExecution jobExecution) {
		log.info("********In kanban JobExecution  listener - finishing job ********");
		rallyProcessorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT,
				CommonConstant.CACHE_ACCOUNT_HIERARCHY_KANBAN);
		rallyProcessorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT,
				CommonConstant.CACHE_ORGANIZATION_HIERARCHY);
		rallyProcessorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.CACHE_PROJECT_TOOL_CONFIG);
		rallyProcessorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.CACHE_PROJECT_HIERARCHY);
		rallyProcessorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.JIRAKANBAN_KPI_CACHE);
		try {
			// sending notification in case of job failure
			if (jobExecution.getStatus() == BatchStatus.FAILED) {
				log.error("job failed : {} for the project : {}", jobExecution.getJobInstance().getJobName(), projectId);
				Throwable stepFaliureException = null;
				for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
					if (stepExecution.getStatus() == BatchStatus.FAILED) {
						stepFaliureException = stepExecution.getFailureExceptions().get(0);
						break;
					}
				}
				setExecutionInfoInTraceLog(false, stepFaliureException);
				sendNotification(stepFaliureException);
			} else {
				setExecutionInfoInTraceLog(true, null);
			}
		} catch (Exception e) {
			log.error("An Exception has occured in kanban jobListener", e);
		} finally {
			log.info("removing project with basicProjectConfigId {}", projectId);
			// Mark the execution as completed
			ongoingExecutionsService.markExecutionAsCompleted(projectId);
//			if (jiraClientService.isContainRestClient(projectId)) {
//				try {
//					jiraClientService.getRestClientMap(projectId).close();
//				} catch (IOException e) {
//					throw new RuntimeException("Failed to close rest client", e); // NOSONAR
//				}
//				jiraClientService.removeRestClientMapClientForKey(projectId);
//				jiraClientService.removeKerberosClientMapClientForKey(projectId);
//			}
		}
	}

	private void sendNotification(Throwable stepFaliureException) throws UnknownHostException {
		FieldMapping fieldMapping = fieldMappingRepository.findByProjectConfigId(projectId);
		ProjectBasicConfig projectBasicConfig = projectBasicConfigRepo.findByStringId(projectId).orElse(null);
		if (fieldMapping == null || (fieldMapping.getNotificationEnabler() && projectBasicConfig != null)) {
			handler.sendEmailToProjectAdminAndSuperAdmin(
					convertDateToCustomFormat(System.currentTimeMillis()) + " on " + rallyCommonService.getApiHost() + " for \"" +
							getProjectName(projectBasicConfig) + "\"",
					generateLogMessage(stepFaliureException), projectId, RallyConstants.ERROR_NOTIFICATION_SUBJECT_KEY,
					RallyConstants.ERROR_MAIL_TEMPLATE_KEY);
		} else {
			log.info("Notification Switch is Off for the project : {}. So No mail is sent to project admin", projectId);
		}
	}

	private static String getProjectName(ProjectBasicConfig projectBasicConfig) {
		return projectBasicConfig == null ? "" : projectBasicConfig.getProjectName();
	}

	private void setExecutionInfoInTraceLog(boolean status, Throwable stepFailureException) {
		List<ProcessorExecutionTraceLog> procExecTraceLogs = processorExecutionTraceLogRepo
				.findByProcessorNameAndBasicProjectConfigIdIn(RallyConstants.JIRA, Collections.singletonList(projectId));
		if (CollectionUtils.isNotEmpty(procExecTraceLogs)) {
			for (ProcessorExecutionTraceLog processorExecutionTraceLog : procExecTraceLogs) {
				checkDeltaIssues(processorExecutionTraceLog, status);
				processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
				processorExecutionTraceLog.setExecutionSuccess(status);
				if (stepFailureException != null && processorExecutionTraceLog.isProgressStats()) {
					processorExecutionTraceLog.setErrorMessage(generateLogMessage(stepFailureException));
					processorExecutionTraceLog.setFailureLog(stepFailureException.getMessage());
				}
			}
			processorExecutionTraceLogRepo.saveAll(procExecTraceLogs);
		}
	}

	private void checkDeltaIssues(ProcessorExecutionTraceLog processorExecutionTraceLog, boolean status) {
		try {
			if (StringUtils.isNotEmpty(processorExecutionTraceLog.getFirstRunDate()) && status) {
				if (StringUtils.isNotEmpty(processorExecutionTraceLog.getBoardId())) {
					String query = "updatedDate>='" + processorExecutionTraceLog.getFirstRunDate() + "' ";
//					Promise<SearchResult> promisedRs = jiraClientService.getRestClientMap(projectId).getCustomIssueClient()
//							.searchBoardIssue(processorExecutionTraceLog.getBoardId(), query, 0, 0, RallyConstants.ISSUE_FIELD_SET);
//					SearchResult searchResult = promisedRs.claim();
//					if (searchResult != null && (searchResult.getTotal() != kanbanJiraIssueRepository
//							.countByBasicProjectConfigIdAndExcludeTypeName(projectId, RallyConstants.EPIC))) {
//						processorExecutionTraceLog.setDataMismatch(true);
//					}
				} else {
					ProjectConfFieldMapping projectConfig = fetchProjectConfiguration.fetchConfiguration(projectId);
					String issueTypes = Arrays.stream(projectConfig.getFieldMapping().getJiraIssueTypeNames())
							.map(array -> "\"" + String.join("\", \"", array) + "\"").collect(Collectors.joining(", "));
					StringBuilder query = new StringBuilder("project in (")
							.append(projectConfig.getProjectToolConfig().getProjectKey()).append(") and ");

					String userQuery = projectConfig.getJira().getBoardQuery().toLowerCase().split(RallyConstants.ORDERBY)[0];
					query.append(userQuery);
					query.append(" and issuetype in (").append(issueTypes).append(" ) and updatedDate>='")
							.append(processorExecutionTraceLog.getFirstRunDate()).append("' ");
					log.info("jql query :{}", query);
//					Promise<SearchResult> promisedRs = jiraClientService.getRestClientMap(projectId).getProcessorSearchClient()
//							.searchJql(query.toString(), 0, 0, RallyConstants.ISSUE_FIELD_SET);
//					SearchResult searchResult = promisedRs.claim();
//					if (searchResult != null && (searchResult.getTotal() != kanbanJiraIssueRepository
//							.countByBasicProjectConfigIdAndExcludeTypeName(projectId, CommonConstant.BLANK))) {
//						processorExecutionTraceLog.setDataMismatch(true);
//					}
				}
			}
		} catch (Exception e) {
			log.error("Some error occured while calculating dataMistch", e);
		}
	}
}
