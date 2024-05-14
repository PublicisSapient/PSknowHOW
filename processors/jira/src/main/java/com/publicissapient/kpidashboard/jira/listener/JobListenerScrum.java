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
package com.publicissapient.kpidashboard.jira.listener;

import static com.publicissapient.kpidashboard.jira.helper.JiraHelper.convertDateToCustomFormat;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import com.publicissapient.kpidashboard.jira.service.JiraClientService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.jira.cache.JiraProcessorCacheEvictor;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.service.JiraCommonService;
import com.publicissapient.kpidashboard.jira.service.NotificationHandler;
import com.publicissapient.kpidashboard.jira.service.OngoingExecutionsService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 */
@Component
@Slf4j
@JobScope
public class JobListenerScrum implements JobExecutionListener {

	@Autowired
	private NotificationHandler handler;

	@Value("#{jobParameters['projectId']}")
	private String projectId;

	@Autowired
	private FieldMappingRepository fieldMappingRepository;

	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

	@Autowired
	private JiraProcessorCacheEvictor jiraProcessorCacheEvictor;

	@Autowired
	private OngoingExecutionsService ongoingExecutionsService;

	@Autowired
	private JiraProcessorConfig jiraProcessorConfig;

	@Autowired
	private ProjectBasicConfigRepository projectBasicConfigRepo;

	@Autowired
	private JiraCommonService jiraCommonService;

	@Autowired
	JiraClientService jiraClientService;

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
		log.info("********in scrum JobExecution listener - finishing job *********");
		jiraProcessorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT,
				CommonConstant.CACHE_ACCOUNT_HIERARCHY);
		jiraProcessorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT,
				CommonConstant.CACHE_SPRINT_HIERARCHY);
		jiraProcessorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.JIRA_KPI_CACHE);
		try {
			if (jobExecution.getStatus() == BatchStatus.FAILED) {
				log.error("job failed : {} for the project : {}", jobExecution.getJobInstance().getJobName(),
						projectId);
				Throwable stepFaliureException = null;
				for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
					if (stepExecution.getStatus() == BatchStatus.FAILED) {
						stepFaliureException = stepExecution.getFailureExceptions().get(0);
						break;
					}
				}
				setExecutionInfoInTraceLog(false,	stepFaliureException);
				sendNotification(stepFaliureException);
			} else {
				setExecutionInfoInTraceLog(true, null);
			}
		} catch (Exception e) {
			log.error("An Exception has occured in scrum jobListener", e);
		} finally {
			log.info("removing project with basicProjectConfigId {}", projectId);
			// Mark the execution as completed
			ongoingExecutionsService.markExecutionAsCompleted(projectId);
            if (jiraClientService.isContainRestClient(projectId)){
                try {
                    jiraClientService.getRestClientMap(projectId).close();
                } catch (IOException e) {
					throw new RuntimeException("Failed to close rest client",e);// NOSONAR
                }
                jiraClientService.removeRestClientMapClientForKey(projectId);
                jiraClientService.removeKerberosClientMapClientForKey(projectId);
            }
		}
	}

	private void sendNotification(Throwable stepFaliureException) throws UnknownHostException {
		FieldMapping fieldMapping = fieldMappingRepository.findByProjectConfigId(projectId);
		ProjectBasicConfig projectBasicConfig = projectBasicConfigRepo.findByStringId(projectId).orElse(null);
		if (fieldMapping == null || (fieldMapping.getNotificationEnabler() && projectBasicConfig != null)) {
			handler.sendEmailToProjectAdmin(
					convertDateToCustomFormat(System.currentTimeMillis()) + " on " + jiraCommonService.getApiHost()
							+ " for \"" + getProjectName(projectBasicConfig) + "\"",
					ExceptionUtils.getRootCauseMessage(stepFaliureException), projectId);
		} else {
			log.info("Notification Switch is Off for the project : {}. So No mail is sent to project admin", projectId);
		}
	}

	private static String getProjectName(ProjectBasicConfig projectBasicConfig) {
		return projectBasicConfig == null ? "" : projectBasicConfig.getProjectName();
	}


	private void setExecutionInfoInTraceLog(boolean status, Throwable stepFailureException) {
		List<ProcessorExecutionTraceLog> procExecTraceLogs = processorExecutionTraceLogRepo
				.findByProcessorNameAndBasicProjectConfigIdIn(JiraConstants.JIRA, Collections.singletonList(projectId));
		if (CollectionUtils.isNotEmpty(procExecTraceLogs)) {
			for (ProcessorExecutionTraceLog processorExecutionTraceLog : procExecTraceLogs) {
				processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
				processorExecutionTraceLog.setExecutionSuccess(status);
				if (stepFailureException != null) {
					String rootCauseMessage = (stepFailureException).toString();
					processorExecutionTraceLog.setErrorMessage("Step failed due to: " + rootCauseMessage);
				}
			}
			processorExecutionTraceLogRepo.saveAll(procExecTraceLogs);
		}
	}
}
