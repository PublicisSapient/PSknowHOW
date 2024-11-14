/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.jira.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.jira.dataFactories.AccountHierarchiesDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.KanbanJiraIssueDataFactory;

@RunWith(MockitoJUnitRunner.class)
public class ProjectHierarchySyncServiceImplTest {

	@InjectMocks
	private ProjectHierarchySyncServiceImpl projectHierarchySyncServiceImpl;

	@Mock
	private JiraIssueRepository jiraIssueRepository;

	@Mock
	private AccountHierarchyRepository accountHierarchyRepository;

	@Mock
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepository;

	@Mock
	private SprintRepository sprintRepository;

	List<AccountHierarchy> accountHierarchyList;
	List<JiraIssue> jiraIssueList;
	List<KanbanJiraIssue> kanbanJiraIssueList;

	@Before
	public void setUp() {
		AccountHierarchiesDataFactory accountHierarchiesDataFactory = AccountHierarchiesDataFactory
				.newInstance("/json/default/account_hierarchy.json");
		accountHierarchyList = accountHierarchiesDataFactory.getAccountHierarchies();
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance("/json/default/jira_issues.json");
		jiraIssueList = jiraIssueDataFactory.getJiraIssues();
		KanbanJiraIssueDataFactory kanbanJiraIssueDataFactory = KanbanJiraIssueDataFactory
				.newInstance("/json/default/kanban_jira_issue.json");
		kanbanJiraIssueList = kanbanJiraIssueDataFactory.getKanbanJiraIssues();

	}

	@Test
	public void scrumSprintHierarchySyncNoSprintsToDeleteFalseHit() {
		ObjectId projectId = new ObjectId();
		List<String> distinctSprintIDs = List.of("Sprint1", "Sprint2");
		List<String> nonMatchingNodeIds = List.of();

		when(jiraIssueRepository.findDistinctSprintIDsByBasicProjectConfigId(String.valueOf(projectId)))
				.thenReturn(jiraIssueList);
		// when(accountHierarchyRepository.findNodeIdsByBasicProjectConfigIdAndNodeIdNotIn(projectId,
		// distinctSprintIDs,
		// CommonConstant.HIERARCHY_LEVEL_ID_SPRINT)).thenReturn(accountHierarchyList);

		projectHierarchySyncServiceImpl.scrumSprintHierarchySync(projectId);

		verify(sprintRepository, never()).deleteBySprintIDInAndBasicProjectConfigId(nonMatchingNodeIds, projectId);
	}

	@Test
	public void syncScrumReleaseHierarchyDeletesNonMatchingReleasesFalseHit() {
		ObjectId projectId = new ObjectId();
		List<AccountHierarchy> fetchedReleasedHierarchy = List.of(AccountHierarchy.builder().nodeId("Release1").build(),
				AccountHierarchy.builder().nodeId("Release2").build());
		List<String> distinctReleaseNodeIds = List.of("Release1", "Release2");
		List<String> entriesToDelete = List.of("Release3");

		when(accountHierarchyRepository.findNodeIdsByBasicProjectConfigIdAndNodeIdNotIn(projectId,
				distinctReleaseNodeIds, CommonConstant.HIERARCHY_LEVEL_ID_RELEASE)).thenReturn(accountHierarchyList);

		projectHierarchySyncServiceImpl.syncScrumReleaseHierarchy(projectId, fetchedReleasedHierarchy);

		verify(accountHierarchyRepository).findNodeIdsByBasicProjectConfigIdAndNodeIdNotIn(projectId,
				distinctReleaseNodeIds, CommonConstant.HIERARCHY_LEVEL_ID_RELEASE);
	}

	@Test
	public void syncKanbanReleaseHierarchyNoReleasesToDeleteFalseHit() {
		ObjectId projectId = new ObjectId();
		List<KanbanAccountHierarchy> fetchedReleasedHierarchy = List.of(
				KanbanAccountHierarchy.builder().nodeId("Release1").build(),
				KanbanAccountHierarchy.builder().nodeId("Release2").build());
		List<String> distinctReleaseNodeIds = List.of("Release1", "Release2");
		List<KanbanAccountHierarchy> entriesToDelete = List.of();

		when(kanbanAccountHierarchyRepository.findNodeIdsByBasicProjectConfigIdAndNodeIdNotIn(projectId,
				distinctReleaseNodeIds, CommonConstant.HIERARCHY_LEVEL_ID_RELEASE)).thenReturn(entriesToDelete);

		projectHierarchySyncServiceImpl.syncKanbanReleaseHierarchy(projectId, fetchedReleasedHierarchy);

		verify(kanbanAccountHierarchyRepository).findNodeIdsByBasicProjectConfigIdAndNodeIdNotIn(projectId,
				distinctReleaseNodeIds, CommonConstant.HIERARCHY_LEVEL_ID_RELEASE);
	}


	@Test
	public void syncScrumReleaseHierarchyDeletesNonMatchingReleases() {
		ObjectId projectId = new ObjectId();
		List<AccountHierarchy> fetchedReleasedHierarchy = List.of(AccountHierarchy.builder().nodeId("Release1").build(),
				AccountHierarchy.builder().nodeId("Release2").build());
		List<String> distinctReleaseNodeIds = List.of("Release1", "Release2");

		projectHierarchySyncServiceImpl.syncScrumReleaseHierarchy(projectId, fetchedReleasedHierarchy);

		verify(accountHierarchyRepository).findNodeIdsByBasicProjectConfigIdAndNodeIdNotIn(projectId,
				distinctReleaseNodeIds, CommonConstant.HIERARCHY_LEVEL_ID_RELEASE);
	}

	@Test
	public void syncKanbanReleaseHierarchyDeletesNonMatchingReleases() {
		ObjectId projectId = new ObjectId();

		List<KanbanAccountHierarchy> fetchedReleasedHierarchy = List.of(
				KanbanAccountHierarchy.builder().nodeId("Release1").build(),
				KanbanAccountHierarchy.builder().nodeId("Release2").build());
		List<String> distinctReleaseNodeIds = List.of("Release1", "Release2");
		projectHierarchySyncServiceImpl.syncKanbanReleaseHierarchy(projectId, fetchedReleasedHierarchy);

		verify(kanbanAccountHierarchyRepository).findNodeIdsByBasicProjectConfigIdAndNodeIdNotIn(projectId,
				distinctReleaseNodeIds, CommonConstant.HIERARCHY_LEVEL_ID_RELEASE);
	}

	@Test
	public void deleteNonMatchingEntriesDeletesNonMatchingEntriesForScrum() {
		ObjectId projectId = new ObjectId();
		List<String> distinctReleaseNodeIds = List.of("Node1", "Node2");

		projectHierarchySyncServiceImpl.deleteNonMatchingEntries(projectId, distinctReleaseNodeIds,
				CommonConstant.HIERARCHY_LEVEL_ID_RELEASE, false);

		verify(accountHierarchyRepository).deleteByBasicProjectConfigIdAndNodeIdIn(projectId, distinctReleaseNodeIds,
				CommonConstant.HIERARCHY_LEVEL_ID_RELEASE);
	}

	@Test
	public void deleteNonMatchingEntriesDeletesNonMatchingEntriesForKanban() {
		ObjectId projectId = new ObjectId();
		List<String> distinctReleaseNodeIds = List.of("Node1", "Node2");

		projectHierarchySyncServiceImpl.deleteNonMatchingEntries(projectId, distinctReleaseNodeIds,
				CommonConstant.HIERARCHY_LEVEL_ID_RELEASE, true);

		verify(kanbanAccountHierarchyRepository).deleteByBasicProjectConfigIdAndNodeIdIn(projectId,
				distinctReleaseNodeIds, CommonConstant.HIERARCHY_LEVEL_ID_RELEASE);
	}

	@Test
	public void scrumSprintHierarchySyncNoSprintsToDeleteTrueHit() {
		ObjectId projectId = new ObjectId();
		List<String> nonMatchingNodeIds = List.of();

		when(jiraIssueRepository.findDistinctSprintIDsByBasicProjectConfigId(String.valueOf(projectId)))
				.thenReturn(jiraIssueList);
		when(accountHierarchyRepository.findNodeIdsByBasicProjectConfigIdAndNodeIdNotIn(projectId,
				jiraIssueList.stream().map(JiraIssue::getSprintID).toList(), CommonConstant.HIERARCHY_LEVEL_ID_SPRINT))
				.thenReturn(accountHierarchyList);

		projectHierarchySyncServiceImpl.scrumSprintHierarchySync(projectId);

		verify(sprintRepository, never()).deleteBySprintIDInAndBasicProjectConfigId(nonMatchingNodeIds, projectId);
	}

}
