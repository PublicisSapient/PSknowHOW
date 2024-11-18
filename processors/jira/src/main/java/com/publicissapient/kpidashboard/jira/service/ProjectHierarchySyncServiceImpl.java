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

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for synchronizing project hierarchies.
 *
 * @author shunaray
 */
@Service
@Slf4j
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
	public void syncScrumSprintHierarchy(ObjectId basicProjectConfigId) {
		List<String> distinctSprintIDs = jiraIssueRepository
				.findDistinctSprintIDsByBasicProjectConfigId(String.valueOf(basicProjectConfigId)).stream()
				.map(JiraIssue::getSprintID).toList();

		// Find nodeIds that are in accountHierarchy but not in jira issue sprintIDs
		List<String> nonMatchingNodeIds = accountHierarchyRepository
				.findNodeIdsByBasicProjectConfigIdAndNodeIdNotIn(basicProjectConfigId, distinctSprintIDs,
						CommonConstant.HIERARCHY_LEVEL_ID_SPRINT)
				.stream().map(AccountHierarchy::getNodeId).toList();

		if (CollectionUtils.isNotEmpty(nonMatchingNodeIds)) {
			log.info("Syncing sprint details of projectId {}. Deleting sprintID: {}", basicProjectConfigId,
					nonMatchingNodeIds);
			sprintRepository.deleteBySprintIDInAndBasicProjectConfigId(nonMatchingNodeIds, basicProjectConfigId);

			deleteNonMatchingEntries(basicProjectConfigId, nonMatchingNodeIds, CommonConstant.HIERARCHY_LEVEL_ID_SPRINT,
					false);
		}
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
	public void syncScrumReleaseHierarchy(ObjectId basicProjectConfigId,
										  List<AccountHierarchy> fetchedReleasedHierarchy) {
		List<String> distinctReleaseNodeIds = fetchedReleasedHierarchy.stream().map(AccountHierarchy::getNodeId)
				.distinct().toList();

		List<String> entriesToDelete = accountHierarchyRepository
				.findNodeIdsByBasicProjectConfigIdAndNodeIdNotIn(basicProjectConfigId, distinctReleaseNodeIds,
						CommonConstant.HIERARCHY_LEVEL_ID_RELEASE)
				.stream().map(AccountHierarchy::getNodeId).toList();

		if (CollectionUtils.isNotEmpty(entriesToDelete)) {
			deleteNonMatchingEntries(basicProjectConfigId, entriesToDelete, CommonConstant.HIERARCHY_LEVEL_ID_RELEASE,
					false);
		}
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
	public void syncKanbanReleaseHierarchy(ObjectId basicProjectConfigId,
										   List<KanbanAccountHierarchy> fetchedReleasedHierarchy) {
		List<String> distinctReleaseNodeIds = fetchedReleasedHierarchy.stream().map(KanbanAccountHierarchy::getNodeId)
				.distinct().toList();

		List<String> entriesToDelete = kanbanAccountHierarchyRepository
				.findNodeIdsByBasicProjectConfigIdAndNodeIdNotIn(basicProjectConfigId, distinctReleaseNodeIds,
						CommonConstant.HIERARCHY_LEVEL_ID_RELEASE)
				.stream().map(KanbanAccountHierarchy::getNodeId).toList();

		if (CollectionUtils.isNotEmpty(entriesToDelete)) {
			deleteNonMatchingEntries(basicProjectConfigId, entriesToDelete, CommonConstant.HIERARCHY_LEVEL_ID_RELEASE,
					true);
		}
	}

	/**
	 * Deletes entries from the account hierarchy or Kanban account hierarchy that
	 * do not match the provided list of distinct release node IDs.
	 *
	 * @param basicProjectConfigId
	 *            the ID of the basic project configuration
	 * @param nodeIdsToBeDeleted
	 *            the list of node IDs to delete
	 * @param hierarchyLevelId
	 *            the hierarchy level ID
	 * @param isKanban
	 *            flag indicating if the hierarchy is Kanban
	 */
	@Override
	public void deleteNonMatchingEntries(ObjectId basicProjectConfigId, List<String> nodeIdsToBeDeleted,
			String hierarchyLevelId, boolean isKanban) {
		if (isKanban) {
			log.info("Syncing Kanban {} hierarchy of projectId {}. Deleting node IDs: {}", hierarchyLevelId,
					basicProjectConfigId, nodeIdsToBeDeleted);
			kanbanAccountHierarchyRepository.deleteByBasicProjectConfigIdAndNodeIdIn(basicProjectConfigId,
					nodeIdsToBeDeleted, hierarchyLevelId);
		} else {
			log.info("Syncing Scrum {} hierarchy of projectId {}. Deleting node IDs: {}", hierarchyLevelId,
					basicProjectConfigId, nodeIdsToBeDeleted);
			accountHierarchyRepository.deleteByBasicProjectConfigIdAndNodeIdIn(basicProjectConfigId, nodeIdsToBeDeleted,
					hierarchyLevelId);
		}
	}

}
