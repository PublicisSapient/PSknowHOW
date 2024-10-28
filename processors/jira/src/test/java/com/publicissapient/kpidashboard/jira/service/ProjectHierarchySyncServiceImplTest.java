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
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

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

	@Before
	public void setUp() {
		// Initialize mocks before each test
	}

	@Test
	public void scrumSprintHierarchySyncDeletesNonMatchingSprints() {
		ObjectId projectId = new ObjectId();
		List<String> distinctSprintIDs = List.of("Sprint1", "Sprint2");
		List<String> accountHierarchyList = List.of("Sprint1", "Sprint3");

		when(jiraIssueRepository.findDistinctSprintIDByBasicProjectConfigId(projectId)).thenReturn(distinctSprintIDs);
		when(accountHierarchyRepository.findDistinctNodeIdsByLabelNameAndBasicProjectConfigId(
				CommonConstant.HIERARCHY_LEVEL_ID_SPRINT, projectId)).thenReturn(accountHierarchyList);

		projectHierarchySyncServiceImpl.scrumSprintHierarchySync(projectId);

		verify(sprintRepository).deleteBySprintIDInAndBasicProjectConfigId(List.of("Sprint3"), projectId);
	}

	@Test
	public void scrumReleaseHierarchySyncDeletesNonMatchingReleases() {
		ObjectId projectId = new ObjectId();
		List<AccountHierarchy> fetchedReleasedHierarchy = List.of(AccountHierarchy.builder().nodeId("Release1").build(),
				AccountHierarchy.builder().nodeId("Release2").build());
		List<String> distinctReleaseNodeIds = List.of("Release1", "Release2");

		projectHierarchySyncServiceImpl.scrumReleaseHierarchySync(projectId, fetchedReleasedHierarchy);

		verify(accountHierarchyRepository).deleteByBasicProjectConfigIdAndNodeIdNotIn(projectId, distinctReleaseNodeIds,
				CommonConstant.HIERARCHY_LEVEL_ID_RELEASE);
	}

	@Test
	public void kanbanReleaseHierarchySyncDeletesNonMatchingReleases() {
		ObjectId projectId = new ObjectId();

		List<KanbanAccountHierarchy> fetchedReleasedHierarchy = List.of(
				KanbanAccountHierarchy.builder().nodeId("Release1").build(),
				KanbanAccountHierarchy.builder().nodeId("Release2").build());
		List<String> distinctReleaseNodeIds = List.of("Release1", "Release2");
		projectHierarchySyncServiceImpl.kanbanReleaseHierarchySync(projectId, fetchedReleasedHierarchy);

		verify(kanbanAccountHierarchyRepository).deleteByBasicProjectConfigIdAndNodeIdNotIn(projectId,
				distinctReleaseNodeIds, CommonConstant.HIERARCHY_LEVEL_ID_RELEASE);
	}

	@Test
	public void deleteNonMatchingEntriesDeletesNonMatchingEntriesForScrum() {
		ObjectId projectId = new ObjectId();
		List<String> distinctReleaseNodeIds = List.of("Node1", "Node2");

		projectHierarchySyncServiceImpl.deleteNonMatchingEntries(projectId, distinctReleaseNodeIds,
				CommonConstant.HIERARCHY_LEVEL_ID_RELEASE, false);

		verify(accountHierarchyRepository).deleteByBasicProjectConfigIdAndNodeIdNotIn(projectId, distinctReleaseNodeIds,
				CommonConstant.HIERARCHY_LEVEL_ID_RELEASE);
	}

	@Test
	public void deleteNonMatchingEntriesDeletesNonMatchingEntriesForKanban() {
		ObjectId projectId = new ObjectId();
		List<String> distinctReleaseNodeIds = List.of("Node1", "Node2");

		projectHierarchySyncServiceImpl.deleteNonMatchingEntries(projectId, distinctReleaseNodeIds,
				CommonConstant.HIERARCHY_LEVEL_ID_RELEASE, true);

		verify(kanbanAccountHierarchyRepository).deleteByBasicProjectConfigIdAndNodeIdNotIn(projectId,
				distinctReleaseNodeIds, CommonConstant.HIERARCHY_LEVEL_ID_RELEASE);
	}
}
