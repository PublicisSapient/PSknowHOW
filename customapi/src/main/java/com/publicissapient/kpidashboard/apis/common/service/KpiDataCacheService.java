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

import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;
import org.bson.types.ObjectId;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

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
	 * @param basicProjectConfigId
	 * @param sprintList
	 * @param kpiId
	 * @return
	 */
	Map<String, Object> fetchIssueCountData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId);

	/**
	 *
	 * @param basicProjectConfigId
	 * @param startDate
	 * @param endDate
	 * @param kpiId
	 * @return
	 */
	List<Build> fetchBuildFrequencydata(ObjectId basicProjectConfigId, String startDate, String endDate, String kpiId);

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

    @Cacheable(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.toString().concat('_').concat(#kpiId)")
    Map<String, Object> fetchCostOfDelayData(ObjectId basicProjectConfigId, String kpiId);

    @Cacheable(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.toString().concat('_').concat(#kpiId)")
    List<ProjectRelease> fetchProjectReleaseData(ObjectId basicProjectConfigId, String kpiId);
}
