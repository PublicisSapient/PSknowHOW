package com.publicissapient.kpidashboard.rally.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.rally.model.CompositeResult;
import com.publicissapient.kpidashboard.rally.model.HierarchicalRequirement;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.rally.model.ReadData;

@ExtendWith(MockitoExtension.class)
public class IssueScrumProcessorTest {

    @InjectMocks
    private IssueScrumProcessor issueScrumProcessor;

    @Mock
    private RallyIssueProcessor rallyIssueProcessor;

    @Mock
    private RallyIssueHistoryProcessor rallyIssueHistoryProcessor;

    @Mock
    private RallyIssueAccountHierarchyProcessor rallyIssueAccountHierarchyProcessor;

    @Mock
    private RallyIssueAssigneeProcessor rallyIssueAssigneeProcessor;

    @Mock
    private SprintDataProcessor sprintDataProcessor;

    private ReadData readData;
    private JiraIssue jiraIssue;
    private JiraIssueCustomHistory jiraIssueCustomHistory;
    private Set<SprintDetails> sprintDetails;
    private Set<ProjectHierarchy> projectHierarchies;
    private AssigneeDetails assigneeDetails;
    private ProjectConfFieldMapping projectConfFieldMapping;
    private HierarchicalRequirement hierarchicalRequirement;

    @BeforeEach
    public void setup() {
        readData = new ReadData();
        projectConfFieldMapping = new ProjectConfFieldMapping();
        hierarchicalRequirement = new HierarchicalRequirement();
        jiraIssue = new JiraIssue();
        jiraIssueCustomHistory = new JiraIssueCustomHistory();
        sprintDetails = new HashSet<>();
        projectHierarchies = new HashSet<>();
        assigneeDetails = new AssigneeDetails();

        projectConfFieldMapping.setProjectName("Test Project");
        readData.setProjectConfFieldMapping(projectConfFieldMapping);
        readData.setHierarchicalRequirement(hierarchicalRequirement);
        readData.setSprintFetch(false);
    }

    @Test
    public void testProcessWithValidData() throws Exception {
        when(rallyIssueProcessor.convertToJiraIssue(any(), any(), any(), any())).thenReturn(jiraIssue);
        when(rallyIssueHistoryProcessor.convertToJiraIssueHistory(any(), any(), any())).thenReturn(jiraIssueCustomHistory);
        when(sprintDataProcessor.processSprintData(any(), any(), any(), any())).thenReturn(sprintDetails);
        when(rallyIssueAccountHierarchyProcessor.createAccountHierarchy(any(), any(), any())).thenReturn(projectHierarchies);
        when(rallyIssueAssigneeProcessor.createAssigneeDetails(any(), any())).thenReturn(assigneeDetails);

        CompositeResult result = issueScrumProcessor.process(readData);

        assertNotNull(result);
        assertEquals(jiraIssue, result.getJiraIssue());
        assertEquals(jiraIssueCustomHistory, result.getJiraIssueCustomHistory());
        assertEquals(sprintDetails, result.getSprintDetailsSet());
        assertEquals(projectHierarchies, result.getProjectHierarchies());
        assertEquals(assigneeDetails, result.getAssigneeDetails());
    }

    @Test
    public void testProcessWithNullJiraIssue() throws Exception {
        when(rallyIssueProcessor.convertToJiraIssue(any(), any(), any(), any())).thenReturn(null);

        CompositeResult result = issueScrumProcessor.process(readData);

        assertNull(result);
    }

    @Test
    public void testProcessWithSprintFetch() throws Exception {
        readData.setSprintFetch(true);
        when(rallyIssueProcessor.convertToJiraIssue(any(), any(), any(), any())).thenReturn(jiraIssue);
        when(rallyIssueHistoryProcessor.convertToJiraIssueHistory(any(), any(), any())).thenReturn(jiraIssueCustomHistory);

        CompositeResult result = issueScrumProcessor.process(readData);

        assertNotNull(result);
        assertEquals(jiraIssue, result.getJiraIssue());
        assertEquals(jiraIssueCustomHistory, result.getJiraIssueCustomHistory());
        assertNull(result.getSprintDetailsSet());
        assertNull(result.getProjectHierarchies());
        assertNull(result.getAssigneeDetails());
    }

    @Test
    public void testProcessWithBoardId() throws Exception {
        readData.setBoardId("TEST-BOARD-1");
        when(rallyIssueProcessor.convertToJiraIssue(any(), any(), any(), any())).thenReturn(jiraIssue);
        when(rallyIssueHistoryProcessor.convertToJiraIssueHistory(any(), any(), any())).thenReturn(jiraIssueCustomHistory);
        when(sprintDataProcessor.processSprintData(any(), any(), any(), any())).thenReturn(sprintDetails);
        when(rallyIssueAccountHierarchyProcessor.createAccountHierarchy(any(), any(), any())).thenReturn(projectHierarchies);
        when(rallyIssueAssigneeProcessor.createAssigneeDetails(any(), any())).thenReturn(assigneeDetails);

        CompositeResult result = issueScrumProcessor.process(readData);

        assertNotNull(result);
        assertEquals(jiraIssue, result.getJiraIssue());
        assertEquals(jiraIssueCustomHistory, result.getJiraIssueCustomHistory());
        assertNull(result.getSprintDetailsSet());
        assertEquals(projectHierarchies, result.getProjectHierarchies());
        assertEquals(assigneeDetails, result.getAssigneeDetails());
    }
}
