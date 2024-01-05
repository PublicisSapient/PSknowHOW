//package com.publicissapient.kpidashboard.jira.processor;
//
//import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
//import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
//import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
//import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
//import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
//import com.publicissapient.kpidashboard.jira.model.CompositeResult;
//import com.publicissapient.kpidashboard.jira.model.ReadData;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.*;
//
//@RunWith(MockitoJUnitRunner.class)
//class IssueScrumProcessorTest {
//
//    @Mock
//    private JiraIssueProcessor jiraIssueProcessor;
//
//    @Mock
//    private JiraIssueHistoryProcessor jiraIssueHistoryProcessor;
//
//    @Mock
//    private JiraIssueAccountHierarchyProcessor jiraIssueAccountHierarchyProcessor;
//
//    @Mock
//    private JiraIssueAssigneeProcessor jiraIssueAssigneeProcessor;
//
//    @Mock
//    private SprintDataProcessor sprintDataProcessor;
//
//    @InjectMocks
//    private IssueScrumProcessor issueScrumProcessor;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testProcessWhenSprintFetchIsFalse() throws Exception {
//        // Arrange
//
//        ReadData readData = new ReadData();
//        readData.setSprintFetch(false);
//        readData.setBoardId("11826");
//        JiraIssue jiraIssue = new JiraIssue();
//        when(jiraIssueProcessor.convertToJiraIssue(any(), any(), any())).thenReturn(jiraIssue);
//        Set<SprintDetails> sprintDetailsSet = new HashSet<>();
//        when(sprintDataProcessor.processSprintData(any(), any(), any())).thenReturn(sprintDetailsSet);
//        Set<AccountHierarchy> accountHierarchies = new HashSet<>();
//        when(jiraIssueAccountHierarchyProcessor.createAccountHierarchy(any(), any(), any())).thenReturn(accountHierarchies);
//        AssigneeDetails assigneeDetails = new AssigneeDetails();
//        when(jiraIssueAssigneeProcessor.createAssigneeDetails(any(), any())).thenReturn(assigneeDetails);
//
//        // Act
//        CompositeResult result = issueScrumProcessor.process(readData);
//
//        // Assert
//        assertEquals(jiraIssue, result.getJiraIssue());
//        assertEquals(sprintDetailsSet, result.getSprintDetailsSet());
//        assertEquals(accountHierarchies, result.getAccountHierarchies());
//        assertEquals(assigneeDetails, result.getAssigneeDetails());
//        verify(jiraIssueProcessor, times(1)).convertToJiraIssue(any(), any(), any());
//        verify(sprintDataProcessor, times(1)).processSprintData(any(), any(), any());
//        verify(jiraIssueAccountHierarchyProcessor, times(1)).createAccountHierarchy(any(), any(), any());
//        verify(jiraIssueAssigneeProcessor, times(1)).createAssigneeDetails(any(), any());
//    }
//
//    @Test
//    void testProcessWhenSprintFetchIsTrue() throws Exception {
//        // Arrange
//        ReadData readData = new ReadData();
//        readData.setSprintFetch(true);
//        JiraIssue jiraIssue = new JiraIssue();
//        when(jiraIssueProcessor.convertToJiraIssue(any(), any(), any())).thenReturn(jiraIssue);
//
//        // Act
//        CompositeResult result = issueScrumProcessor.process(readData);
//
//        // Assert
//        assertEquals(jiraIssue, result.getJiraIssue());
//        verify(jiraIssueProcessor, times(1)).convertToJiraIssue(any(), any(), any());
//        verify(sprintDataProcessor, never()).processSprintData(any(), any(), any());
//        verify(jiraIssueAccountHierarchyProcessor, never()).createAccountHierarchy(any(), any(), any());
//        verify(jiraIssueAssigneeProcessor, never()).createAssigneeDetails(any(), any());
//    }
//}