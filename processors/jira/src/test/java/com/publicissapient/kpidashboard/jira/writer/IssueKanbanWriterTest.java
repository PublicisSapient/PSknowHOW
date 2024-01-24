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

import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.item.Chunk;

import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
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
	public void testWriteWithEmptyValue() throws Exception {
		// Mock data
		CompositeResult compositeResult = new CompositeResult();
		Chunk<CompositeResult> compositeResults=new Chunk<>();
		compositeResults.add(compositeResult);
		// Invoke the method to be tested
		issueKanbanWriter.write(compositeResults);

	}

	// Helper methods to create mock data for testing
	private Chunk<CompositeResult> createMockKanbanCompositeResults() {
		CompositeResult compositeResult = new CompositeResult();
		compositeResult.setKanbanJiraIssue(createMockJiraItems().get(0));
		compositeResult.setKanbanAccountHierarchies((createMockAccountHierarchies()));
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

	private Set<KanbanAccountHierarchy> createMockAccountHierarchies() {
		KanbanAccountHierarchy kanbanAccountHierarchy = new KanbanAccountHierarchy();
		kanbanAccountHierarchy.setId(new ObjectId("63bfa0f80b28191677615735"));
		Set<KanbanAccountHierarchy> accountHierarchies = new HashSet<>();
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
