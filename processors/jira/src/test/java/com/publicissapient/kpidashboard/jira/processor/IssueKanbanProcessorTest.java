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

package com.publicissapient.kpidashboard.jira.processor;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
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
		when(jiraIssueProcessor.convertToKanbanJiraIssue(any(), any(), any(), any())).thenReturn(jiraIssue);
		ProjectHierarchy projectHierarchy = new ProjectHierarchy();
		projectHierarchy.setBasicProjectConfigId(new ObjectId("63bfa0f80b28191677615735"));
		Set<ProjectHierarchy> accountHierarchies = new HashSet<>();
		accountHierarchies.add(projectHierarchy);
		when(jiraIssueAccountHierarchyProcessor.createKanbanAccountHierarchy(any(), any())).thenReturn(accountHierarchies);
		AssigneeDetails assigneeDetails = new AssigneeDetails();
		when(jiraIssueAssigneeProcessor.createKanbanAssigneeDetails(any(), any())).thenReturn(assigneeDetails);

		// Act
		CompositeResult result = issueKanbanProcessor.process(readData);

		// Assert
		assertEquals(jiraIssue, result.getKanbanJiraIssue());
		assertEquals(accountHierarchies, result.getProjectHierarchies());
		assertEquals(assigneeDetails, result.getAssigneeDetails());
	}
}
