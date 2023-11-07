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

package com.publicissapient.kpidashboard.common.repository.jira;//NOPMD

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.ReleaseWisePI;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;

/**
 * Repository for FeatureCollector with custom methods.
 */
@Repository
public interface JiraIssueRepositoryCustom {// NOPMD
	// to avoid tooManyMethods

	/**
	 * Find defect count by rca list.
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @return the list
	 */
	List<JiraIssue> findDefectCountByRCA(Map<String, List<String>> mapOfFilters);

	/**
	 * Find issues group by sprint list.
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @param uniqueProjectMap
	 *            the unique project map
	 * @param filterToShowOnTrend
	 *            the filter to show on trend
	 * @param individualDevOrQa
	 *            the individual dev or qa
	 * @return key as sprint and list of Story id
	 */
	List<SprintWiseStory> findIssuesGroupBySprint(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String filterToShowOnTrend, String individualDevOrQa);

	List<SprintWiseStory> findIssuesAndTestDetailsGroupBySprint(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String filterToShowOnTrend, String individualDevOrQa,
			Map<String, Map<String, Object>> uniqueProjectMapNotIn);

	/**
	 * Find issue by story number list.
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @param storyNumber
	 *            the story number
	 * @param uniqueProjectMapFolder
	 *            the unique project map folder
	 * @return UAT defects or total defects based on uatDefect parameter.
	 */
	List<JiraIssue> findIssueByStoryNumber(Map<String, List<String>> mapOfFilters, List<String> storyNumber,
			Map<String, Map<String, Object>> uniqueProjectMapFolder);

	/**
	 * Find issues by sprint and type list.
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @param uniqueProjectMap
	 *            the unique project map
	 * @return list of feature
	 */
	List<JiraIssue> findIssuesBySprintAndType(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap);

	/**
	 * Find issues by sprint and type list.
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @param uniqueProjectMap
	 *            the unique project map
	 * @param uniqueProjectMapNotIn
	 *            for not in query
	 * @return list of feature
	 */
	List<JiraIssue> findIssuesBySprintAndType(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, Map<String, Map<String, Object>> uniqueProjectMapNotIn);

	/**
	 * Find issues by type list.
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @return list of feature
	 */
	List<JiraIssue> findIssuesByType(Map<String, List<String>> mapOfFilters);

	/**
	 * Find issues with boolean field
	 * 
	 * @param mapOfFilters
	 *            mapOfFilters
	 * @param fieldName
	 *            fieldName
	 * @param dateFrom
	 *            dateFrom
	 * @param dateTo
	 *            dateTo
	 * @return List<JiraIssue>
	 */
	List<JiraIssue> findIssuesWithBoolean(Map<String, List<String>> mapOfFilters, String fieldName, boolean flag, String dateFrom,
										  String dateTo);

	List<JiraIssue> findUnassignedIssues(String startDate, String endDate, Map<String, List<String>> mapOfFilters);

	/**
	 * Find stories by type list.
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @param uniqueProjectMap
	 *            the unique project map
	 * @param filterToShowOnTrend
	 *            the filter to show on trend
	 * @param individualDevOrQa
	 *            the individual dev or qa
	 * @return list of PredictabilityFeature
	 */
	List<SprintWiseStory> findStoriesByType(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String filterToShowOnTrend, String individualDevOrQa);

	/**
	 * Find defect linked with sprint list.
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @return list of defects not linked with stories but sprint is tagged
	 */
	List<JiraIssue> findDefectLinkedWithSprint(Map<String, List<String>> mapOfFilters);

	/**
	 * This method is used to find stories for a given list of sprints
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @param storyNumber
	 *            the story number
	 * @return list the feature
	 */
	List<JiraIssue> findStoriesBySprints(Map<String, List<String>> mapOfFilters, List<String> storyNumber);

	/**
	 * Find costOfDelay by type list.
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @return list of feature
	 */
	List<JiraIssue> findCostOfDelayByType(Map<String, List<String>> mapOfFilters);

	/**
	 * Updates multi objects that matches with basicProjectConfigId and unsets the
	 * fields provided.
	 * 
	 * @param basicProjectConfigId
	 *            config id
	 * @param fieldsToUnset
	 *            list of fields to unset
	 */
	void updateByBasicProjectConfigId(String basicProjectConfigId, List<String> fieldsToUnset);

	/**
	 * This method used to find issue based on Project id and used in Regression
	 * Automation
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @param uniqueProjectMap
	 *            the unique project map
	 * @return list the list
	 */
	List<JiraIssue> findNonRegressionTestCases(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap);

	/**
	 * Find defects without story link.
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @param uniqueProjectMapNotIn
	 *            for not in query
	 * @return list of feature
	 */

	List<JiraIssue> findDefectsWithoutStoryLink(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMapNotIn);

	/**
	 * Find issues filtered by map of filters, type name and defectStoryIds
	 * 
	 * @param typeName
	 *            story type
	 * @param defectStoryIds
	 *            stories of the defects
	 * @return list of jira issues
	 */
	List<JiraIssue> findByTypeNameAndDefectStoryIDIn(String typeName, List<String> defectStoryIds);

	List<JiraIssue> findIssueByNumber(Map<String, List<String>> mapOfFilters, Set<String> storyNumber,
			Map<String, Map<String, Object>> uniqueProjectMap);

	/**
	 * Finds Feature objects for given filters and date and jira status have as per
	 * mapStatusCriteria
	 *
	 * @param mapOfFilters
	 * @param uniqueProjectMap
	 * @param dateFrom
	 * @param dateTo
	 * @param range
	 * @return
	 */
	List<JiraIssue> findIssuesByDateAndTypeAndStatus(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String dateFrom, String dateTo, String range,
			String mapStatusCriteria, boolean isProductionDefect);

	List<JiraIssue> findIssueAndDescByNumber(List<String> storyNumber);

	/**
	 * find linked defects of given stories and filters
	 *
	 * @param mapOfFilters
	 * @param defectsStoryIds
	 * @param uniqueProjectMap
	 * @return
	 */
	List<JiraIssue> findLinkedDefects(Map<String, List<String>> mapOfFilters, Set<String> defectsStoryIds,

			Map<String, Map<String, Object>> uniqueProjectMap);

	/**
	 * Find issues filtered by map of filters, type name and defectStoryIds
	 * 
	 * @param mapOfFilters
	 *            filters
	 * @param uniqueProjectMap
	 *            project map filters
	 * @return list of jira issues
	 */
	List<JiraIssue> findIssuesByFilterAndProjectMapFilter(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap);

	List<JiraIssue> findByRelease(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap);

	/**
	 * find unique Release Version Name group by type name
	 * 
	 * @param mapOfFilters
	 * @param @return
	 */

	List<ReleaseWisePI> findUniqueReleaseVersionByUniqueTypeName(Map<String, List<String>> mapOfFilters);
}
