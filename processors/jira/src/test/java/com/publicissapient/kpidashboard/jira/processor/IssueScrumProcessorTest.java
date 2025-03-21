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
import static org.mockito.Mockito.*;

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
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.model.ReadData;

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
		ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().projectName("xyz").build();
		readData.setProjectConfFieldMapping(projectConfFieldMapping);
		readData.setSprintFetch(false);
		JiraIssue jiraIssue = new JiraIssue();
		when(jiraIssueProcessor.convertToJiraIssue(any(), any(), any(), any())).thenReturn(jiraIssue);
		SprintDetails sprintDetails = new SprintDetails();
		sprintDetails.setSprintID("123");
		Set<SprintDetails> sprintDetailsSets = new HashSet<>();
		sprintDetailsSets.add(sprintDetails);
		when(sprintDataProcessor.processSprintData(any(), any(), any(), any())).thenReturn(sprintDetailsSets);
		ProjectHierarchy projectHierarchy = new ProjectHierarchy();
		projectHierarchy.setBasicProjectConfigId(new ObjectId("63bfa0f80b28191677615735"));
		Set<ProjectHierarchy> projectHierarchies = new HashSet<>();
		projectHierarchies.add(projectHierarchy);
		when(jiraIssueAccountHierarchyProcessor.createAccountHierarchy(any(), any(), any())).thenReturn(projectHierarchies);
		AssigneeDetails assigneeDetails = new AssigneeDetails();
		when(jiraIssueAssigneeProcessor.createAssigneeDetails(any(), any())).thenReturn(assigneeDetails);

		// Act
		CompositeResult result = issueScrumProcessor.process(readData);

		// Assert
		assertEquals(jiraIssue, result.getJiraIssue());
		assertEquals(sprintDetailsSets, result.getSprintDetailsSet());
		assertEquals(projectHierarchies, result.getProjectHierarchies());
		assertEquals(assigneeDetails, result.getAssigneeDetails());
	}
}
