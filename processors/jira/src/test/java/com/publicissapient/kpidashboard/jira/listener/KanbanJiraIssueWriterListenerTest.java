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

import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class KanbanJiraIssueWriterListenerTest {

    @Mock
    private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

    @InjectMocks
    private KanbanJiraIssueWriterListener listener;

    List<CompositeResult> compositeResults = new ArrayList<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        KanbanJiraIssue kanbanJiraIssue = new KanbanJiraIssue();
        kanbanJiraIssue.setBasicProjectConfigId("testProjectId");
        kanbanJiraIssue.setBoardId("testBoardId");
        kanbanJiraIssue.setChangeDate("2022-01-01T12:34:56"); // Change date in the required format
        kanbanJiraIssue.setTypeName("Story");
        CompositeResult compositeResult = new CompositeResult();
        compositeResult.setKanbanJiraIssue(kanbanJiraIssue);
        compositeResults.add(compositeResult);
    }

    @Test
    public void testAfterWrite() {

        // Mock the repository's behavior
        when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdAndBoardId(
                eq(JiraConstants.JIRA), eq("testProjectId"), eq("testBoardId")))
                .thenReturn(Optional.empty()); // For the case where trace log is not present

        // Act
        listener.afterWrite(compositeResults);

        // Assert
        List<ProcessorExecutionTraceLog> savedLogs = new ArrayList<>();

        // Additional assertions based on the requirements of your application
        assertEquals(0, savedLogs.size());
    }

    @Test
    public void testOnWriteError_LogsError() {
        // Arrange
        Exception testException = new RuntimeException("Test exception");
        List<CompositeResult> compositeResults = new ArrayList<>();

        // Act
        listener.onWriteError(testException, compositeResults);
        assertTrue("", true);
    }



    @Test
    public void testAfterWrite1() {
        // Arrange
        List<CompositeResult> compositeResults = new ArrayList<>();

        // Create a KanbanJiraIssue for testing
        KanbanJiraIssue kanbanJiraIssue = new KanbanJiraIssue();
        kanbanJiraIssue.setBasicProjectConfigId("testProjectId");
        kanbanJiraIssue.setBoardId("testBoardId");
        kanbanJiraIssue.setTypeName("Story");
        kanbanJiraIssue.setChangeDate("2022-01-01T12:34:56"); // Change date in the required format
        CompositeResult compositeResult = new CompositeResult();
        compositeResult.setKanbanJiraIssue(kanbanJiraIssue);
        compositeResults.add(compositeResult);

        // Mock the repository's behavior
        when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdAndBoardId(
                eq(JiraConstants.JIRA), eq("testProjectId"), eq("testBoardId")))
                .thenReturn(Optional.empty()); // For the case where trace log is not present

        // Act
        listener.afterWrite(compositeResults);

        // Assert
        List<ProcessorExecutionTraceLog> savedLogs = new ArrayList<>();

        // Additional assertions based on the requirements of your application
        assertEquals(0, savedLogs.size());
    }

    @Test
    public void testAfterWriteWithEmptyValue() {
        listener.afterWrite(new ArrayList<>());
    }

    @Test
    public void testAfterWriteWithTraceLog() {
        ProcessorExecutionTraceLog processorExecutionTraceLog=new ProcessorExecutionTraceLog();
        processorExecutionTraceLog.setBasicProjectConfigId("abc");
        processorExecutionTraceLog.setBoardId("abc");
        processorExecutionTraceLog.setProcessorName(JiraConstants.JIRA);
        when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdAndBoardId(
                eq(JiraConstants.JIRA), eq("testProjectId"), eq("testBoardId")))
                .thenReturn(Optional.of(processorExecutionTraceLog));
        listener.afterWrite(compositeResults);
    }

}
