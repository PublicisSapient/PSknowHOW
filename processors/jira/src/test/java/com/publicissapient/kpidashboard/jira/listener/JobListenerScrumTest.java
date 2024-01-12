package com.publicissapient.kpidashboard.jira.listener;

import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.jira.cache.JiraProcessorCacheEvictor;
import com.publicissapient.kpidashboard.jira.service.JiraCommonService;
import com.publicissapient.kpidashboard.jira.service.NotificationHandler;
import com.publicissapient.kpidashboard.jira.service.OngoingExecutionsService;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JobListenerScrumTest {

    @Mock
    private NotificationHandler handler;

    @Mock
    private FieldMappingRepository fieldMappingRepository;

    @Mock
    private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

    @Mock
    private JiraProcessorCacheEvictor jiraProcessorCacheEvictor;

    @Mock
    private OngoingExecutionsService ongoingExecutionsService;

    @Mock
    private ProjectBasicConfigRepository projectBasicConfigRepository;

    @Mock
    private JiraCommonService jiraCommonService;

    @InjectMocks
    private JobListenerScrum jobListenerScrum;

    private JobExecution jobExecution;

    @Before
    public void setUp() {
        jobExecution = MetaDataInstanceFactory.createJobExecution();
    }

    @Test
    public void testAfterJob_FailedExecution() throws Exception {
        FieldMapping fieldMapping=new FieldMapping();
        fieldMapping.setNotificationEnabler(true);
        when(fieldMappingRepository.findByProjectConfigId(null)).thenReturn(fieldMapping);
        ProjectBasicConfig projectBasicConfig= ProjectBasicConfig.builder().projectName("xyz").build();
        when(projectBasicConfigRepository.findByStringId(null)).thenReturn(Optional.ofNullable(projectBasicConfig));
        when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdIn(anyString(), any()))
                .thenReturn(Collections.singletonList(new ProcessorExecutionTraceLog()));
        when(jiraCommonService.getApiHost()).thenReturn("xyz");
        StepExecution stepExecution=jobExecution.createStepExecution("xyz");
        stepExecution.setStatus(BatchStatus.FAILED);
        stepExecution.addFailureException(new Throwable("Exception"));
        // Simulate a failed job
        jobExecution.setStatus(BatchStatus.FAILED);

        // Act
        jobListenerScrum.afterJob(jobExecution);

        verify(ongoingExecutionsService).markExecutionAsCompleted(null);
    }

    @Test
    public void testBeforeJob(){
        jobListenerScrum.beforeJob(jobExecution);
    }


}
