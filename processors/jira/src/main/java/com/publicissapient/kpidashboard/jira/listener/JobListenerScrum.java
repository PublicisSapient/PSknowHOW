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

import static com.publicissapient.kpidashboard.jira.listener.JobListenerKanban.convertDateToCustomFormat;

import java.io.File;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bson.types.ObjectId;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.jira.cache.JiraProcessorCacheEvictor;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.service.NotificationHandler;
import com.publicissapient.kpidashboard.jira.service.OngoingExecutionsService;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

/**
 * @author pankumar8
 *
 */
@Component
@Slf4j
@JobScope
public class JobListenerScrum extends JobExecutionListenerSupport {

	@Autowired
	private NotificationHandler handler;

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
	public JobListenerScrum(@Value("#{jobParameters['projectId']}") String projectId) {
		this.projectId = projectId;
	}

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
		jiraProcessorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.JIRA_KPI_CACHE);

		if (jobExecution.getStatus() == BatchStatus.FAILED) {
			log.error("job failed : {} for the project : {}", jobExecution.getJobInstance().getJobName(), projectId);
			Throwable stepFaliureException=null;
			for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
				if (stepExecution.getStatus() == BatchStatus.FAILED) {
					stepFaliureException = stepExecution.getFailureExceptions().get(0);
					log.info("step execution failure reason :{}",stepFaliureException);
					break;
				}
			}
			try {
				sendNotification(stepFaliureException);
			} catch (UnknownHostException e) {
				log.error("AccessRequestController: Server Host name is not bind with Access Request mail ");
			}
			setExecutionSuccessFalse();
		}

		log.info("removing project with basicProjectConfigId {}", projectId);
		// Mark the execution as completed
		ongoingExecutionsService.markExecutionAsCompleted(projectId);
	}

	/**
	 *
	 * Gets api host
	 **/
	public String getApiHost() throws UnknownHostException {

		StringBuilder urlPath = new StringBuilder();
		if (StringUtils.isNotEmpty(jiraProcessorConfig.getUiHost())) {
			urlPath.append("http").append(':').append(File.separator + File.separator)
					.append(jiraProcessorConfig.getUiHost().trim());
		} else {
			throw new UnknownHostException("Api host not found in properties.");
		}

		return urlPath.toString();
	}

	private void sendNotification(Throwable stepFaliureException) throws UnknownHostException {
		FieldMapping fieldMapping = fieldMappingRepository.findByBasicProjectConfigId(new ObjectId(projectId));
		ProjectBasicConfig projectBasicConfig= projectBasicConfigRepo.findById(new ObjectId(projectId)).orElse(null);
		if (fieldMapping.getNotificationEnabler()) {
			handler.sendEmailToProjectAdmin(convertDateToCustomFormat(System.currentTimeMillis())+ " on " +getApiHost()+ " for " +projectBasicConfig.getProjectName(), ExceptionUtils.getStackTrace(stepFaliureException), projectId);
		} else {
			log.info("Notification Switch is Off for the project : {}. So No mail is sent to project admin", projectId);
		}
	}

	private void setExecutionSuccessFalse() {
		List<ProcessorExecutionTraceLog> procExecTraceLogs = processorExecutionTraceLogRepo
				.findByProcessorNameAndBasicProjectConfigIdIn(JiraConstants.JIRA, Arrays.asList(projectId));
		if (CollectionUtils.isNotEmpty(procExecTraceLogs)) {
			for (ProcessorExecutionTraceLog processorExecutionTraceLog : procExecTraceLogs) {
				processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
				processorExecutionTraceLog.setExecutionSuccess(false);
			}
			processorExecutionTraceLogRepo.saveAll(procExecTraceLogs);
		}
	}
}
