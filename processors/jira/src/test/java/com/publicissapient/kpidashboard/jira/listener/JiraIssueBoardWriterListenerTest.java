package com.publicissapient.kpidashboard.jira.listener;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JiraIssueBoardWriterListenerTest {

    @Mock
    private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

    @InjectMocks
    private JiraIssueBoardWriterListener listener;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAfterWrite() {
        // Mock data
        List<CompositeResult> compositeResults = createMockScrumCompositeResults();

        // Mock repository behavior
        when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdAndBoardId(
                anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        // Invoke the method to be tested
        listener.afterWrite(compositeResults);

        // Verify interactions with the repository
        verify(processorExecutionTraceLogRepo, times(1)).findByProcessorNameAndBasicProjectConfigIdAndBoardId(
                eq(ProcessorConstants.JIRA), anyString(), anyString());
        verify(processorExecutionTraceLogRepo, times(1)).saveAll(anyList());
    }

    @Test
    public void testOnWriteError() {
        // Mock data
        Exception exception = new RuntimeException("Test exception");
        List<CompositeResult> compositeResults = createMockScrumCompositeResults();

        // Invoke the method to be tested
        listener.onWriteError(exception, compositeResults);

        // Verify log.error is called
        // You may need to use a mocking framework for logging or add a logging abstraction for testing
    }

    private List<CompositeResult> createMockScrumCompositeResults() {
        CompositeResult compositeResult = new CompositeResult();
        compositeResult.setJiraIssue(new ArrayList<>(createMockJiraItems()).get(0));
        compositeResult.setAccountHierarchies((createMockAccountHierarchies()));
        compositeResult.setAssigneeDetails(createMockAssigneesToSave().get("0"));
        compositeResult.setJiraIssueCustomHistory(createMockScrumIssueCustomHistory().get(0));
        SprintDetails sprintDetails=new SprintDetails();
        sprintDetails.setSprintID("1234");
        compositeResult.setSprintDetailsSet(new HashSet<>(Arrays.asList(sprintDetails)));
        List<CompositeResult> kanbanCompositeResults = new ArrayList<>();
        kanbanCompositeResults.add(compositeResult);
        return kanbanCompositeResults;
    }

    private Set<JiraIssue> createMockJiraItems() {
        JiraIssue kanbanJiraIssue = new JiraIssue();
        kanbanJiraIssue.setId(new ObjectId("63bfa0f80b28191677615735"));
        Set<JiraIssue> jiraItems = new HashSet<>();
        jiraItems.add(kanbanJiraIssue);
        // Create mock KanbanJiraIssue objects and add them to the list
        return jiraItems;
    }

    private List<JiraIssueCustomHistory> createMockScrumIssueCustomHistory() {
        JiraIssueCustomHistory kanbanIssueCustomHistory = new JiraIssueCustomHistory();
        kanbanIssueCustomHistory.setId(new ObjectId("63bfa0f80b28191677615735"));
        List<JiraIssueCustomHistory> kanbanIssueCustomHistoryList = new ArrayList<>();
        kanbanIssueCustomHistoryList.add(kanbanIssueCustomHistory);
        // Create mock KanbanIssueCustomHistory objects and add them to the list
        return kanbanIssueCustomHistoryList;
    }

    private Set<AccountHierarchy> createMockAccountHierarchies() {
        AccountHierarchy kanbanAccountHierarchy = new AccountHierarchy();
        kanbanAccountHierarchy.setId(new ObjectId("63bfa0f80b28191677615735"));
        Set<AccountHierarchy> accountHierarchies = new HashSet<>();
        accountHierarchies.add(kanbanAccountHierarchy);
        // Create mock KanbanAccountHierarchy objects and add them to the set
        return accountHierarchies;
    }

    private Map<String, AssigneeDetails> createMockAssigneesToSave() {
        AssigneeDetails assigneeDetails = new AssigneeDetails();
        assigneeDetails.setBasicProjectConfigId("63bfa0f80b28191677615735");
        Map<String, AssigneeDetails> assigneesToSave = new HashMap<>();
        assigneesToSave.put("0", assigneeDetails);
        // Create mock AssigneeDetails objects and add them to the map
        return assigneesToSave;
    }
}
