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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.bson.types.ObjectId;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author pankumar8
 */
@Component
@Slf4j
@JobScope
public class JobListenerKanban extends JobExecutionListenerSupport {

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
    public JobListenerKanban(@Value("#{jobParameters['projectId']}") String projectId) {
        this.projectId = projectId;
    }

    public static String convertDateToCustomFormat(long currentTimeMillis) {
        Date inputDate = new Date(currentTimeMillis);
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy, EEEE, hh:mm:ss a");

        String outputStr = outputFormat.format(inputDate);

        return outputStr;
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
        log.info("********In kanban JobExecution  listener - finishing job ********");
        jiraProcessorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT,
                CommonConstant.CACHE_ACCOUNT_HIERARCHY_KANBAN);
        jiraProcessorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.JIRAKANBAN_KPI_CACHE);
        try {
            // sending notification in case of job failure
            if (jobExecution.getStatus() == BatchStatus.FAILED) {
                log.error("job failed : {} for the project : {}", jobExecution.getJobInstance().getJobName(), projectId);
                setExecutionSuccessFalse();
                sendNotification();
            }
        } catch (Exception e) {
            log.error("An Exception has occured in kanban jobListener", e);
        } finally {
            log.info("removing project with basicProjectConfigId {}", projectId);
            // Mark the execution as completed
            ongoingExecutionsService.markExecutionAsCompleted(projectId);
        }
    }

    private void sendNotification() {
        FieldMapping fieldMapping = fieldMappingRepository.findByBasicProjectConfigId(new ObjectId(projectId));
        if (fieldMapping == null || fieldMapping.getNotificationEnabler()) {
            handler.sendEmailToProjectAdmin(convertDateToCustomFormat(System.currentTimeMillis()), projectId);
        } else {
            log.info("Notification Switch is Off for the project {}. No email is sent to project admin.", projectId);
        }
    }

    private void setExecutionSuccessFalse() {
        List<ProcessorExecutionTraceLog> procExecTraceLogs = processorExecutionTraceLogRepo
                .findByProcessorNameAndBasicProjectConfigIdIn(JiraConstants.JIRA, Arrays.asList(projectId));
        if (CollectionUtils.isNotEmpty(procExecTraceLogs)) {
            for (ProcessorExecutionTraceLog processorExecutionTraceLog : procExecTraceLogs) {
                processorExecutionTraceLog.setExecutionSuccess(false);
            }
            processorExecutionTraceLogRepo.saveAll(procExecTraceLogs);
        }
    }
}
