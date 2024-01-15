package com.publicissapient.kpidashboard.jira.writer;

import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class IssueScrumWriterTest {

    @Mock
    private JiraIssueRepository kanbanJiraIssueRepository;

    @Mock
    private JiraIssueCustomHistoryRepository kanbanJiraIssueHistoryRepository;

    @Mock
    private AccountHierarchyRepository kanbanAccountHierarchyRepository;

    @Mock
    private AssigneeDetailsRepository assigneeDetailsRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private JiraProcessorConfig jiraProcessorConfig;

    @InjectMocks
    private IssueScrumWriter issueScrumWriter;

    @Test
    public void testWrite() throws Exception {
        // Mock data
        List<CompositeResult> kanbanCompositeResults = createMockScrumCompositeResults();
        //when(jiraProcessorConfig.getPageSize()).thenReturn(50);
        // Invoke the method to be tested
        issueScrumWriter.write(kanbanCompositeResults);

        // Verify interactions with repositories
        verify(kanbanJiraIssueRepository, times(1)).saveAll(createMockJiraItems());
    }

    // Helper methods to create mock data for testing
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