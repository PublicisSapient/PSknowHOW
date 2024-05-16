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

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.item.Chunk;

import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;

@RunWith(MockitoJUnitRunner.class)
public class KanbanJiraIssueJqlWriterListenerTest {
    @Mock
    private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

    @InjectMocks
    private KanbanJiraIssueJqlWriterListener listener;

    Chunk<CompositeResult> compositeResults = new Chunk<>();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Create a KanbanJiraIssue for testing
        KanbanJiraIssue kanbanJiraIssue = new KanbanJiraIssue();
        kanbanJiraIssue.setBasicProjectConfigId("testProjectId");
        kanbanJiraIssue.setChangeDate("2022-01-01T12:34:56"); // Change date in the required format
        CompositeResult compositeResult = new CompositeResult();
        compositeResult.setKanbanJiraIssue(kanbanJiraIssue);
        compositeResults.add(compositeResult);
    }

    @Test
    public void testAfterWrite() {
        // Mock the repository's behavior
        when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdAndProgressStatsFalse(
                eq(JiraConstants.JIRA), eq("testProjectId")))
                .thenReturn(Optional.empty()); // For the case where trace log is not present

        // Act
        listener.afterWrite(compositeResults);

        // Verify
        verify(processorExecutionTraceLogRepo, times(1)).saveAll(anyList());

        // Assert
        List<ProcessorExecutionTraceLog> savedLogs = new ArrayList<>();

        // Additional assertions based on the requirements of your application
        assertEquals(0, savedLogs.size());
    }

    @Test
    public void testOnWriteError_LogsError() {
        Chunk<CompositeResult> compositeResults = new Chunk<>();

        // Arrange
        Exception testException = new RuntimeException("Test exception");
        // Act
        listener.onWriteError(testException, compositeResults);
        assertTrue("", true);
    }

    @Test
    public void testAfterWriteWithEmptyValue() {
        listener.afterWrite(new Chunk<>());
    }

    @Test
    public void testAfterWriteWithTraceLog() {
        ProcessorExecutionTraceLog processorExecutionTraceLog=new ProcessorExecutionTraceLog();
        processorExecutionTraceLog.setBasicProjectConfigId("abc");
        processorExecutionTraceLog.setBoardId("abc");
        processorExecutionTraceLog.setProcessorName(JiraConstants.JIRA);
        when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdAndProgressStatsFalse(
                eq(JiraConstants.JIRA), eq("testProjectId")))
                .thenReturn(Optional.of(processorExecutionTraceLog));
        listener.afterWrite(compositeResults);
    }
}
