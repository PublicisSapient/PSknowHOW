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

import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;

/**
 * Service interface for synchronizing project hierarchies.
 *
 * @author shunary
 */
public interface ProjectHierarchySyncService {
	/**
	 * Synchronizes the hierarchy for Scrum sprints.
	 *
	 * @param basicProjectConfigId
	 *            the ID of the basic project configuration
	 */
	void syncScrumSprintHierarchy(ObjectId basicProjectConfigId);

	/**
	 * Synchronizes the hierarchy for releases.
	 *
	 * @param basicProjectConfigId
	 *            the ID of the basic project configuration
	 * @param fetchedReleasedHierarchy
	 *            the list of fetched release hierarchies
	 */
	void syncReleaseHierarchy(ObjectId basicProjectConfigId, List<ProjectHierarchy> fetchedReleasedHierarchy);

	/**
	 * Deletes entries that do not match the given criteria.
	 *
	 * @param basicProjectConfigId
	 *            the ID of the basic project configuration
	 * @param distinctReleaseNodeIds
	 *            the list of distinct release node IDs
	 * @param hierarchyLevelId
	 *            the ID of the hierarchy level
	 */
	void deleteNonMatchingEntries(ObjectId basicProjectConfigId, List<String> distinctReleaseNodeIds,
			String hierarchyLevelId);
}
