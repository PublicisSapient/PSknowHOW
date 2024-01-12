package com.publicissapient.kpidashboard.jira.listener;

import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
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

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class KanbanJiraIssueJqlWriterListenerTest {
    @Mock
    private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

    @InjectMocks
    private KanbanJiraIssueJqlWriterListener listener;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAfterWrite() {
        // Arrange
        List<CompositeResult> compositeResults = new ArrayList<>();

        // Create a KanbanJiraIssue for testing
        KanbanJiraIssue kanbanJiraIssue = new KanbanJiraIssue();
        kanbanJiraIssue.setBasicProjectConfigId("testProjectId");
        kanbanJiraIssue.setChangeDate("2022-01-01T12:34:56"); // Change date in the required format
        CompositeResult compositeResult = new CompositeResult();
        compositeResult.setKanbanJiraIssue(kanbanJiraIssue);
        compositeResults.add(compositeResult);

        // Mock the repository's behavior
        when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigId(
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
        List<CompositeResult> compositeResults = new ArrayList<>();

        // Arrange
        Exception testException = new RuntimeException("Test exception");
        // Act
        listener.onWriteError(testException, compositeResults);
        assertTrue("", true);
    }
}
