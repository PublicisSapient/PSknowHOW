package com.publicissapient.kpidashboard.jira.processor;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.model.ReadData;

@RunWith(MockitoJUnitRunner.class)
public class IssueKanbanProcessorTest {

	@Mock
	private KanbanJiraIssueProcessor jiraIssueProcessor;

	@Mock
	private KanbanJiraIssueHistoryProcessor jiraIssueHistoryProcessor;

	@Mock
	private KanbanJiraIssueAccountHierarchyProcessor jiraIssueAccountHierarchyProcessor;

	@Mock
	private KanbanJiraIssueAssigneeProcessor jiraIssueAssigneeProcessor;

	@InjectMocks
	private IssueKanbanProcessor issueKanbanProcessor;


	@Test
	public void testProcessWhenSprintFetchIsFalse() throws Exception {
		// Arrange

		ReadData readData = new ReadData();
		ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().projectName("xyz").build();
		readData.setProjectConfFieldMapping(projectConfFieldMapping);
		readData.setSprintFetch(false);
		KanbanJiraIssue jiraIssue = new KanbanJiraIssue();
		when(jiraIssueProcessor.convertToKanbanJiraIssue(any(), any(), any())).thenReturn(jiraIssue);
		KanbanAccountHierarchy accountHierarchy = KanbanAccountHierarchy.builder()
				.basicProjectConfigId(new ObjectId("63bfa0f80b28191677615735")).build();
		Set<KanbanAccountHierarchy> accountHierarchies = new HashSet<>();
		accountHierarchies.add(accountHierarchy);
		when(jiraIssueAccountHierarchyProcessor.createKanbanAccountHierarchy(any(), any()))
				.thenReturn(accountHierarchies);
		AssigneeDetails assigneeDetails = new AssigneeDetails();
		when(jiraIssueAssigneeProcessor.createKanbanAssigneeDetails(any(), any())).thenReturn(assigneeDetails);

		// Act
		CompositeResult result = issueKanbanProcessor.process(readData);

		// Assert
		assertEquals(jiraIssue, result.getKanbanJiraIssue());
		assertEquals(accountHierarchies, result.getKanbanAccountHierarchies());
		assertEquals(assigneeDetails, result.getAssigneeDetails());
	}
}