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

import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;

/**
 * Repository for {@link KanbanJiraIssue} with custom methods.
 */
@Repository
public interface KanbanJiraIssueRepoCustom {

	/**
	 * Finds KanbanFeature objects for given filters and date range.
	 * 
	 * @param mapOfFilters
	 *            filters to apply
	 * @param dateFrom
	 *            start date selected
	 * @param dateTo
	 *            end date selected
	 * @return list of KanbanFeature
	 */
	List<KanbanJiraIssue> findIssuesByType(Map<String, List<String>> mapOfFilters, String dateFrom, String dateTo);

	/**
	 * Finds KanbanFeature objects for given filters and date.
	 * 
	 * @param mapOfFilters
	 *            filters to apply
	 * @param uniqueProjectMap
	 *            project specific check
	 * @param dateFrom
	 *            start date
	 * @param dateTo
	 *            end date
	 * @param range
	 *            either range or less
	 * @return list of KanbanFeature
	 */
	List<KanbanJiraIssue> findIssuesByDateAndType(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String dateFrom, String dateTo, String range);

	/**
	 * Finds KanbanFeature objects for given filters and date and jira status have
	 * as per mapStatusCriteria
	 *
	 * @param mapOfFilters
	 * @param uniqueProjectMap
	 * @param dateFrom
	 * @param dateTo
	 * @param range
	 * @return
	 */
	List<KanbanJiraIssue> findIssuesByDateAndTypeAndStatus(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String dateFrom, String dateTo, String range,
			String mapStatusCriteria);

	/**
	 * Find costOfDelay by type list.
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @return list of feature
	 */
	List<KanbanJiraIssue> findCostOfDelayByType(Map<String, List<String>> mapOfFilters);

	/**
	 * unset fields based on basic project config id.
	 * 
	 * @param basicProjectConfigId
	 * @param fieldsToUnset
	 */
	void updateByBasicProjectConfigId(String basicProjectConfigId, List<String> fieldsToUnset);

}
