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

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

/**
 * Service implementation for synchronizing project hierarchies.
 */
@Service
public class ProjectHierarchySyncServiceImpl implements ProjectHierarchySyncService {

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private AccountHierarchyRepository accountHierarchyRepository;

	@Autowired
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepository;

	@Autowired
	private SprintRepository sprintRepository;

	/**
	 * Synchronizes the hierarchy of Scrum sprints by comparing the sprint IDs in
	 * Jira issues with those in the account hierarchy and deleting non-matching
	 * entries.
	 *
	 * @param basicProjectConfigId
	 *            the ID of the basic project configuration
	 */
	@Override
	public void scrumSprintHierarchySync(ObjectId basicProjectConfigId) {
		List<String> distinctSprintIDs = jiraIssueRepository
				.findDistinctSprintIDByBasicProjectConfigId(basicProjectConfigId);

		List<String> accountHierarchyList = accountHierarchyRepository
				.findDistinctNodeIdsByLabelNameAndBasicProjectConfigId(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT,
						basicProjectConfigId);
		// Find nodeIds that are in accountHierarchyList but not in distinctSprintIDs
		List<String> nonMatchingNodeIds = accountHierarchyList.stream()
				.filter(nodeId -> !distinctSprintIDs.contains(nodeId)).toList();

		sprintRepository.deleteBySprintIDInAndBasicProjectConfigId(nonMatchingNodeIds, basicProjectConfigId);

		deleteNonMatchingEntries(basicProjectConfigId, distinctSprintIDs, CommonConstant.HIERARCHY_LEVEL_ID_SPRINT,
				false);

	}

	/**
	 * Synchronizes the hierarchy of Scrum releases by comparing the release node
	 * IDs in the fetched release hierarchy with those in the account hierarchy and
	 * deleting non-matching entries.
	 *
	 * @param basicProjectConfigId
	 *            the ID of the basic project configuration
	 * @param fetchedReleasedHierarchy
	 *            the list of fetched release hierarchy
	 */
	@Override
	public void scrumReleaseHierarchySync(ObjectId basicProjectConfigId,
			List<AccountHierarchy> fetchedReleasedHierarchy) {
		List<String> distinctReleaseNodeIds = fetchedReleasedHierarchy.stream().map(AccountHierarchy::getNodeId)
				.distinct().toList();

		deleteNonMatchingEntries(basicProjectConfigId, distinctReleaseNodeIds,
				CommonConstant.HIERARCHY_LEVEL_ID_RELEASE, false);
	}

	/**
	 * Synchronizes the hierarchy of Kanban releases by comparing the release node
	 * IDs in the fetched release hierarchy with those in the Kanban account
	 * hierarchy and deleting non-matching entries.
	 *
	 * @param basicProjectConfigId
	 *            the ID of the basic project configuration
	 * @param fetchedReleasedHierarchy
	 *            the list of fetched release hierarchy
	 */
	@Override
	public void kanbanReleaseHierarchySync(ObjectId basicProjectConfigId,
			List<KanbanAccountHierarchy> fetchedReleasedHierarchy) {
		List<String> distinctReleaseNodeIds = fetchedReleasedHierarchy.stream().map(KanbanAccountHierarchy::getNodeId)
				.distinct().toList();

		deleteNonMatchingEntries(basicProjectConfigId, distinctReleaseNodeIds,
				CommonConstant.HIERARCHY_LEVEL_ID_RELEASE, true);
	}

	/**
	 * Deletes entries from the account hierarchy or Kanban account hierarchy that
	 * do not match the provided list of distinct release node IDs.
	 *
	 * @param basicProjectConfigId
	 *            the ID of the basic project configuration
	 * @param distinctReleaseNodeIds
	 *            the list of distinct release node IDs
	 * @param hierarchyLevelId
	 *            the hierarchy level ID
	 * @param isKanban
	 *            flag indicating if the hierarchy is Kanban
	 */
	@Override
	public void deleteNonMatchingEntries(ObjectId basicProjectConfigId, List<String> distinctReleaseNodeIds,
			String hierarchyLevelId, boolean isKanban) {
		if (isKanban) {
			kanbanAccountHierarchyRepository.deleteByBasicProjectConfigIdAndNodeIdNotIn(basicProjectConfigId,
					distinctReleaseNodeIds, hierarchyLevelId);
		} else {
			accountHierarchyRepository.deleteByBasicProjectConfigIdAndNodeIdNotIn(basicProjectConfigId,
					distinctReleaseNodeIds, hierarchyLevelId);
		}
	}

}
