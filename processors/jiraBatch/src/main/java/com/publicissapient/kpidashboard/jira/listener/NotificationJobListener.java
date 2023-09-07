package com.publicissapient.kpidashboard.jira.listener;

import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.jira.service.NotificationHandler;
import org.bson.types.ObjectId;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@JobScope
public class NotificationJobListener extends JobExecutionListenerSupport {

    @Autowired
    private NotificationHandler handler;

    private String projectId;

    @Autowired
    private FieldMappingRepository fieldMappingRepository;

    @Autowired
    public NotificationJobListener(@Value("#{jobParameters['projectId']}") String projectId) {
        this.projectId = projectId;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.FAILED) {
        	log.info("job failed : {}",jobExecution.getJobInstance().getJobName());
            // Send a notification here (e.g., email)
            FieldMapping fieldMapping = fieldMappingRepository.findByBasicProjectConfigId(new ObjectId(projectId));
            if(fieldMapping.getNotificationEnabler()=="On") {
                handler.sendEmailToProjectAdmin(jobExecution.getJobInstance().getJobName(), String.valueOf(jobExecution.getFailureExceptions()), projectId);
            } else {
                log.info("Notification is Off");
            }
        }
    }
}
