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

package com.publicissapient.kpidashboard.apis.common.service;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

/**
 * A Service to manage cache.
 *
 * @author prijain3
 */
@Component
public interface KpiDataCacheService {

	/**
	 * Evicts KPI Cache for given kpi - irrespective of projects.
	 *
	 * @param kpiId
	 *            kpi id
	 */
	void clearCache(String kpiId);

	/**
	 * Evicts KPI Cache for given project and kpi
	 *
	 * @param basicProjectConfigId
	 *            project basic config id
	 * @param kpiId
	 *            kpi id
	 */
	void clearCache(String basicProjectConfigId, String kpiId);

	void clearCacheForProject(String basicProjectConfigId);

	void clearCacheForSource(String source);

	List<String> getKpiBasedOnSource(String source);

	/**
	 * Fetches data from DB for the given project and sprints combination. Data is
	 * cached. Cache key - project basic config id and kpi id. Note: Data will be
	 * cached only if Filter is selected till Sprint level.
	 *
	 * @param kpiRequest
	 *            The KPI request object.
	 * @param basicProjectConfigId
	 *            The project config ID.
	 * @param sprintList
	 *            The list of sprint IDs.
	 * @param kpiId
	 *            The KPI ID.
	 * @return A map containing issues
	 */
	Map<String, Object> fetchIssueCountData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId);

	/**
	 * Fetches Sprint Predictability data from DB and caches the result
	 *
	 * @param kpiRequest
	 *            The KPI request object.
	 * @param basicProjectConfigId
	 *            The project config ID.
	 * @param sprintList
	 *            The list of sprint IDs.
	 * @param kpiId
	 *            The KPI ID.
	 * @return A map returns sprint wise jira issues list and project wise sprint
	 *         details
	 */
	Map<String, Object> fetchSprintPredictabilityData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId);

	/**
	 * Fetches DIR data from DB and caches the result
	 *
	 * @param kpiRequest
	 *            The KPI request object.
	 * @param basicProjectConfigId
	 *            The project config ID.
	 * @param sprintList
	 *            The list of sprint IDs.
	 * @param kpiId
	 *            The KPI ID.
	 * @return A map returns sprint wise jira issues list and defect list details
	 */
	Map<String, Object> fetchDefectInjectionRateData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId);

	/**
	 * Fetches DIR data from DB and caches the result
	 *
	 * @param kpiRequest
	 *            The KPI request object.
	 * @param basicProjectConfigId
	 *            The project config ID.
	 * @param sprintList
	 *            The list of sprint IDs.
	 * @param kpiId
	 *            The KPI ID.
	 * @return A map returns sprint wise jira issues list and defect list details
	 */
	Map<String, Object> fetchFirstTimePassRateData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId);

	/**
	 * Fetches DD data from DB and caches the result
	 *
	 * @param kpiRequest
	 *            The KPI request object.
	 * @param basicProjectConfigId
	 *            The project config ID.
	 * @param sprintList
	 *            The list of sprint IDs.
	 * @param kpiId
	 *            The KPI ID.
	 * @return A map returns sprint wise jira issues list and defect list details
	 */
	Map<String, Object> fetchDefectDensityData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId);

	/**
	 * Fetches Sprint velocity data from DB and caches the result
	 *
	 * @param kpiRequest
	 *            The KPI request object.
	 * @param basicProjectConfigId
	 *            The project config ID.
	 * @param kpiId
	 *            The KPI ID.
	 * @return A map returns sprint wise jira issues list and project wise sprint
	 *         details
	 */
	Map<String, Object> fetchSprintVelocityData(KpiRequest kpiRequest, ObjectId basicProjectConfigId, String kpiId);

	/**
	 * Fetches Build Frequency KPI data from DB and caches the result
	 *
	 * @param basicProjectConfigId
	 *            The project config ID.
	 * @param startDate
	 *            the start date
	 * @param endDate
	 *            the end date
	 * @param kpiId
	 *            the KPI id
	 * @return list of builds.
	 */
	List<Build> fetchBuildFrequencyData(ObjectId basicProjectConfigId, String startDate, String endDate, String kpiId);

	/**
	 * Fetches sprint capacity utilization kpi data from the database and caches the
	 * result.
	 *
	 * @param kpiRequest
	 *            The KPI request object.
	 * @param basicProjectConfigId
	 *            The project config ID.
	 * @param sprintList
	 *            The list of sprint IDs.
	 * @param kpiId
	 *            The KPI ID.
	 * @return A map containing estimate time, story list, sprint details, and
	 *         JiraIssue history.
	 */
	Map<String, Object> fetchSprintCapacityData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId);

	/**
	 * Fetches Scope Churn kpi data from the database and caches the result.
	 *
	 * @param kpiRequest
	 *            The KPI request object.
	 * @param basicProjectConfigId
	 *            The project config ID.
	 * @param sprintList
	 *            The list of sprint IDs.
	 * @param kpiId
	 *            The KPI ID.
	 * @return A map containing sprint details, total issues and scope change issue
	 *         history.
	 */
	Map<String, Object> fetchScopeChurnData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId);

	/**
	 * Fetches Commitment Reliability kpi data from the database and caches the
	 * result.
	 *
	 * @param kpiRequest
	 *            The KPI request object.
	 * @param basicProjectConfigId
	 *            The project config ID.
	 * @param sprintList
	 *            The list of sprint IDs.
	 * @param kpiId
	 *            The KPI ID.
	 * @return A map containing sprint details and total issues.
	 */
	Map<String, Object> fetchCommitmentReliabilityData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId);

	/**
	 * Fetches Cost of Delay kpi data from the database and caches the * result.
	 *
	 * @param basicProjectConfigId
	 *            The project config ID.
	 * @param kpiId
	 *            The KPI ID.
	 * @return A map containing Cost of delay data.
	 */
	Map<String, Object> fetchCostOfDelayData(ObjectId basicProjectConfigId, String kpiId);

	/**
	 * Fetches Release Frequency kpi data from the database and caches the * result.
	 *
	 * @param basicProjectConfigId
	 *            The project config ID.
	 * @param kpiId
	 *            The KPI ID.
	 * @return list of project releases.
	 */
	List<ProjectRelease> fetchProjectReleaseData(ObjectId basicProjectConfigId, String kpiId);

	/**
	 * Fetches PI Predictability KPI data from the database and caches the * result.
	 *
	 * @param basicProjectConfigId
	 *            The project config ID.
	 * @param kpiId
	 *            The KPI ID.
	 * @return list of Jira Issues.
	 */
	List<JiraIssue> fetchPiPredictabilityData(ObjectId basicProjectConfigId, String kpiId);

	/**
	 * Fetches Happiness Index data from DB and caches the result
	 *
	 * @param basicProjectConfigId
	 *            The project config ID.
	 * @param sprintList
	 *            The list of sprint IDs.
	 * @param kpiId
	 *            The KPI ID.
	 * @return A map returns sprint wise jira issues list and project wise sprint
	 *         details
	 */
	Map<String, Object> fetchHappinessIndexData(ObjectId basicProjectConfigId, List<String> sprintList, String kpiId);

	/**
	 * Fetches Created vs Resolved KPI data from the database and caches the result.
	 *
	 * @param kpiRequest
	 *            The KPI request object.
	 * @param basicProjectConfigId
	 *            The project config ID.
	 * @param sprintList
	 *            The list of sprint IDs.
	 * @param kpiId
	 *            The KPI ID.
	 * @return A map containing sprint details, Sub-tasks, Sub-task history.
	 */
	Map<String, Object> fetchCreatedVsResolvedData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId);

	/**
	 * Fetches DRR KPI data from the database and caches the result.
	 *
	 * @param kpiRequest
	 *            The KPI request object.
	 * @param basicProjectConfigId
	 *            The project config ID.
	 * @param sprintList
	 *            The list of sprint IDs.
	 * @param kpiId
	 *            The KPI ID.
	 * @return A map containing sprint details, Sub-tasks, Sub-task history.
	 */
	Map<String, Object> fetchDRRData(KpiRequest kpiRequest, ObjectId basicProjectConfigId, List<String> sprintList,
			String kpiId);
}
