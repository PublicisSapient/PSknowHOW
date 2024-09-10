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

package com.publicissapient.kpidashboard.jira.writer;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.publicissapient.kpidashboard.common.service.ProjectHierarchyService;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.item.Chunk;

import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;

@RunWith(MockitoJUnitRunner.class)
public class IssueKanbanWriterTest {

	@Mock
	private KanbanJiraIssueRepository kanbanJiraIssueRepository;

	@Mock
	private KanbanJiraIssueHistoryRepository kanbanJiraIssueHistoryRepository;

	@Mock
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepository;

	@Mock
	private AssigneeDetailsRepository assigneeDetailsRepository;

	@Mock
	private ProjectHierarchyService projectHierarchyService;

	@InjectMocks
	private IssueKanbanWriter issueKanbanWriter;

	@Test
	public void testWrite() throws Exception {
		// Mock data
		Chunk<CompositeResult> kanbanCompositeResults = createMockKanbanCompositeResults();

		// Invoke the method to be tested
		issueKanbanWriter.write(kanbanCompositeResults);

		// Verify interactions with repositories
		verify(kanbanJiraIssueRepository, times(1)).saveAll(createMockJiraItems());
	}

	@Test
	public void testWriteDuplicates() throws Exception {
		// Mock data
		Chunk<CompositeResult> kanbanCompositeResults = createDuplicateMockKanbanCompositeResults();

		// Invoke the method to be tested
		issueKanbanWriter.write(kanbanCompositeResults);

		// Verify interactions with repositories
		verify(kanbanJiraIssueRepository, times(1)).saveAll(verifyMockJiraItems());
	}

	// Helper methods to create mock data for testing
	private Chunk<CompositeResult> createMockKanbanCompositeResults() {
		CompositeResult compositeResult = new CompositeResult();
		compositeResult.setKanbanJiraIssue(createMockJiraItems().get(0));
		compositeResult.setProjectHierarchies((createMockAccountHierarchies()));
		compositeResult.setAssigneeDetails(createMockAssigneesToSave().get("0"));
		compositeResult.setKanbanIssueCustomHistory(createMockKanbanIssueCustomHistory().get(0));
		Chunk<CompositeResult> kanbanCompositeResults = new Chunk<>();
		kanbanCompositeResults.add(compositeResult);
		return kanbanCompositeResults;
	}

	private List<KanbanJiraIssue> createMockJiraItems() {
		KanbanJiraIssue kanbanJiraIssue = new KanbanJiraIssue();
		kanbanJiraIssue.setId(new ObjectId("63bfa0f80b28191677615735"));
		List<KanbanJiraIssue> jiraItems = new ArrayList<>();
		jiraItems.add(kanbanJiraIssue);
		// Create mock KanbanJiraIssue objects and add them to the list
		return jiraItems;
	}

	private List<KanbanIssueCustomHistory> createMockKanbanIssueCustomHistory() {
		KanbanIssueCustomHistory kanbanIssueCustomHistory = new KanbanIssueCustomHistory();
		kanbanIssueCustomHistory.setId(new ObjectId("63bfa0f80b28191677615735"));
		List<KanbanIssueCustomHistory> kanbanIssueCustomHistoryList = new ArrayList<>();
		kanbanIssueCustomHistoryList.add(kanbanIssueCustomHistory);
		// Create mock KanbanIssueCustomHistory objects and add them to the list
		return kanbanIssueCustomHistoryList;
	}

	private Set<ProjectHierarchy> createMockAccountHierarchies() {
		ProjectHierarchy kanbanAccountHierarchy = new ProjectHierarchy();
		kanbanAccountHierarchy.setId(new ObjectId("63bfa0f80b28191677615735"));
		Set<ProjectHierarchy> projectHierarchies = new HashSet<>();
		projectHierarchies.add(kanbanAccountHierarchy);
		// Create mock KanbanAccountHierarchy objects and add them to the set
		return projectHierarchies;
	}

	private Map<String, AssigneeDetails> createMockAssigneesToSave() {
		AssigneeDetails assigneeDetails = new AssigneeDetails();
		Assignee assignee = new Assignee();
		Set<Assignee> assignees = new HashSet<>();
		assignees.add(assignee);
		assigneeDetails.setBasicProjectConfigId("63bfa0f80b28191677615735");
		assigneeDetails.setAssignee(assignees);
		Map<String, AssigneeDetails> assigneesToSave = new HashMap<>();
		assigneesToSave.put("0", assigneeDetails);
		// Create mock AssigneeDetails objects and add them to the map
		return assigneesToSave;
	}

	// Helper methods to create Duplicate mock data for testing
	private Chunk<CompositeResult> createDuplicateMockKanbanCompositeResults() {
		CompositeResult compositeResult = new CompositeResult();
		CompositeResult compositeResultTwo = new CompositeResult();
		compositeResult.setKanbanJiraIssue(createDuplicateMockJiraItems().get(0));
		compositeResultTwo.setKanbanJiraIssue(createDuplicateMockJiraItems().get(1));
		compositeResult.setProjectHierarchies((createDuplicateMockAccountHierarchies()));
		compositeResultTwo.setProjectHierarchies((createDuplicateMockAccountHierarchies()));
		compositeResult.setAssigneeDetails(createDuplicateMockAssigneesToSave().get("0"));
		compositeResultTwo.setAssigneeDetails(createDuplicateMockAssigneesToSave().get("1"));
		compositeResult.setKanbanIssueCustomHistory(createDuplicateMockKanbanIssueCustomHistory().get(0));
		compositeResultTwo.setKanbanIssueCustomHistory(createDuplicateMockKanbanIssueCustomHistory().get(1));
		Chunk<CompositeResult> kanbanCompositeResults = new Chunk<>();
		kanbanCompositeResults.add(compositeResult);
		kanbanCompositeResults.add(compositeResultTwo);
		return kanbanCompositeResults;
	}

	private List<KanbanJiraIssue> createDuplicateMockJiraItems() {
		KanbanJiraIssue kanbanJiraIssue = new KanbanJiraIssue();
		kanbanJiraIssue.setId(new ObjectId("63bfa0f80b28191677615735"));
		kanbanJiraIssue.setNumber("123");
		kanbanJiraIssue.setBasicProjectConfigId("123");
		KanbanJiraIssue kanbanJiraIssueTwo = new KanbanJiraIssue();
		kanbanJiraIssueTwo.setId(new ObjectId("63bfa0f80b28191677615736"));
		kanbanJiraIssueTwo.setNumber("123");
		kanbanJiraIssueTwo.setBasicProjectConfigId("123");
		List<KanbanJiraIssue> jiraItems = new ArrayList<>();
		jiraItems.add(kanbanJiraIssue);
		jiraItems.add(kanbanJiraIssueTwo);
		// Create mock KanbanJiraIssue objects and add them to the list
		return jiraItems;
	}

	private List<KanbanJiraIssue> verifyMockJiraItems() {
		KanbanJiraIssue kanbanJiraIssue = new KanbanJiraIssue();
		kanbanJiraIssue.setId(new ObjectId("63bfa0f80b28191677615735"));
		kanbanJiraIssue.setNumber("123");
		kanbanJiraIssue.setBasicProjectConfigId("123");
		List<KanbanJiraIssue> jiraItems = new ArrayList<>();
		jiraItems.add(kanbanJiraIssue);
		// Create mock KanbanJiraIssue objects and add them to the list
		return jiraItems;
	}

	private List<KanbanIssueCustomHistory> createDuplicateMockKanbanIssueCustomHistory() {
		KanbanIssueCustomHistory kanbanIssueCustomHistory = new KanbanIssueCustomHistory();
		kanbanIssueCustomHistory.setId(new ObjectId("63bfa0f80b28191677615735"));
		List<KanbanIssueCustomHistory> kanbanIssueCustomHistoryList = new ArrayList<>();
		kanbanIssueCustomHistoryList.add(kanbanIssueCustomHistory);
		KanbanIssueCustomHistory kanbanIssueCustomHistoryTwo = new KanbanIssueCustomHistory();
		kanbanIssueCustomHistoryTwo.setId(new ObjectId("63bfa0f80b28191677615736"));
		kanbanIssueCustomHistoryList.add(kanbanIssueCustomHistoryTwo);
		// Create mock KanbanIssueCustomHistory objects and add them to the list
		return kanbanIssueCustomHistoryList;
	}

	private Map<String, AssigneeDetails> createDuplicateMockAssigneesToSave() {
		AssigneeDetails assigneeDetails = new AssigneeDetails();
		AssigneeDetails assigneeDetailsTwo = new AssigneeDetails();
		Assignee assignee = new Assignee();
		Set<Assignee> assignees = new HashSet<>();
		assignees.add(assignee);
		assigneeDetails.setBasicProjectConfigId("63bfa0f80b28191677615735");
		assigneeDetails.setAssignee(assignees);
		Assignee assigneeTwo = new Assignee();
		assigneeTwo.setAssigneeId("987");
		Set<Assignee> assigneesTwo = new HashSet<>();
		assigneesTwo.add(assigneeTwo);
		assigneeDetailsTwo.setBasicProjectConfigId("63bfa0f80b28191677615736");
		assigneeDetailsTwo.setAssignee(assigneesTwo);
		Map<String, AssigneeDetails> assigneesToSave = new HashMap<>();
		assigneesToSave.put("0", assigneeDetails);
		assigneesToSave.put("1", assigneeDetailsTwo);
		// Create mock AssigneeDetails objects and add them to the map
		return assigneesToSave;
	}

	private Set<ProjectHierarchy> createDuplicateMockAccountHierarchies() {
		ProjectHierarchy kanbanAccountHierarchy = new ProjectHierarchy();
		kanbanAccountHierarchy.setId(new ObjectId("63bfa0f80b28191677615735"));
		kanbanAccountHierarchy.setNodeId("123");
		Set<ProjectHierarchy> accountHierarchies = new HashSet<>();
		accountHierarchies.add(kanbanAccountHierarchy);
		ProjectHierarchy kanbanAccountHierarchyTwo = new ProjectHierarchy();
		kanbanAccountHierarchyTwo.setId(new ObjectId("63bfa0f80b28191677615736"));
		kanbanAccountHierarchyTwo.setNodeId("123");
		accountHierarchies.add(kanbanAccountHierarchyTwo);
		// Create mock KanbanAccountHierarchy objects and add them to the set
		return accountHierarchies;
	}
}
