package com.publicissapient.kpidashboard.jira.processor;

import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.model.ReadData;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IssueScrumProcessorTest {

    @Mock
    private JiraIssueProcessor jiraIssueProcessor;

    @Mock
    private JiraIssueHistoryProcessor jiraIssueHistoryProcessor;

    @Mock
    private JiraIssueAccountHierarchyProcessor jiraIssueAccountHierarchyProcessor;

    @Mock
    private JiraIssueAssigneeProcessor jiraIssueAssigneeProcessor;

    @Mock
    private SprintDataProcessor sprintDataProcessor;

    @InjectMocks
    private IssueScrumProcessor issueScrumProcessor;

    @Test
    public void testProcessWhenSprintFetchIsFalse() throws Exception {
        // Arrange

        ReadData readData = new ReadData();
        ProjectConfFieldMapping projectConfFieldMapping= ProjectConfFieldMapping.builder().projectName("xyz").build();
        readData.setProjectConfFieldMapping(projectConfFieldMapping);
        readData.setSprintFetch(false);
        JiraIssue jiraIssue = new JiraIssue();
        when(jiraIssueProcessor.convertToJiraIssue(any(), any(), any())).thenReturn(jiraIssue);
        SprintDetails sprintDetails=new SprintDetails();
        sprintDetails.setSprintID("123");
        Set<SprintDetails> sprintDetailsSets = new HashSet<>();
        sprintDetailsSets.add(sprintDetails);
        when(sprintDataProcessor.processSprintData(any(), any(), any())).thenReturn(sprintDetailsSets);
        AccountHierarchy accountHierarchy=AccountHierarchy.builder().basicProjectConfigId(new ObjectId("63bfa0f80b28191677615735")).build();
        Set<AccountHierarchy> accountHierarchies = new HashSet<>();
        accountHierarchies.add(accountHierarchy);
        when(jiraIssueAccountHierarchyProcessor.createAccountHierarchy(any(), any(), any())).thenReturn(accountHierarchies);
        AssigneeDetails assigneeDetails = new AssigneeDetails();
        when(jiraIssueAssigneeProcessor.createAssigneeDetails(any(), any())).thenReturn(assigneeDetails);

        // Act
        CompositeResult result = issueScrumProcessor.process(readData);

        // Assert
        assertEquals(jiraIssue, result.getJiraIssue());
        assertEquals(sprintDetailsSets, result.getSprintDetailsSet());
        assertEquals(accountHierarchies, result.getAccountHierarchies());
        assertEquals(assigneeDetails, result.getAssigneeDetails());
    }
}