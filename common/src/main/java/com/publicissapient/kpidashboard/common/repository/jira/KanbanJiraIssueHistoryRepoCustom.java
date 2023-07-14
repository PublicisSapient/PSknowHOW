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

package com.publicissapient.kpidashboard.common.repository.jira;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;

/**
 * The interface Kanban feature history repo custom.
 */
@Repository
public interface KanbanJiraIssueHistoryRepoCustom {

	/**
	 * Find kanban feature custom history by status and date.
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @param uniqueProjectMap
	 *            the unique project map
	 * @param dateFrom
	 *            the date from
	 * @param dateTo
	 *            the date to
	 * @param mapStatusCriteria
	 *            the map status criteria
	 * @return list
	 */
	List<KanbanIssueCustomHistory> findIssuesByStatusAndDate(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String dateFrom, String dateTo,
			String mapStatusCriteria);

	/**
	 * This method find issue history by created date.
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @param uniqueProjectMap
	 *            the unique project map
	 * @param dateFrom
	 *            the date from
	 * @param dateTo
	 *            the date to
	 * @return list
	 */
	List<KanbanIssueCustomHistory> findIssuesByCreatedDateAndType(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String dateFrom, String dateTo);

	public List<KanbanIssueCustomHistory> findIssuesInWipByDate(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, Map<String, Map<String, Object>> uniqueWipProjectMap,
			String dateFrom, String dateTo);
}
