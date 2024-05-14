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

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.jira.cache.JiraProcessorCacheEvictor;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.service.JiraClientService;
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
    private JiraClientService jiraClientService;

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

    @Mock
    private ProcessorJiraRestClient client;

    @Mock
    private KerberosClient kerberosClient;

    @InjectMocks
    private JobListenerScrum jobListenerScrum;

    private JobExecution jobExecution;

    @Before
    public void setUp() {
        jobExecution = MetaDataInstanceFactory.createJobExecution();
        when(jiraClientService.isContainRestClient(null)).thenReturn(true);
        when(jiraClientService.getRestClientMap(null)).thenReturn(client);
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

    @Test
    public void testAfterJob_SuccessExecution() throws Exception {
        // Simulate a failed job
        jobExecution.setStatus(BatchStatus.STARTED);

        // Act
        jobListenerScrum.afterJob(jobExecution);

        verify(ongoingExecutionsService).markExecutionAsCompleted(null);
    }

    @Test
    public void testAfterJob_WithException() throws Exception {
        // Act
        jobListenerScrum.afterJob(null);

        verify(ongoingExecutionsService).markExecutionAsCompleted(null);
    }


}
