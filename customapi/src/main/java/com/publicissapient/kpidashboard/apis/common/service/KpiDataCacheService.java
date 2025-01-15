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

import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import org.bson.types.ObjectId;
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

	/**
	 * Fetches data from DB for the given project and sprints combination. Data is
	 * cached. Cache key - project basic config id and kpi id.
	 * Note: Data will be cached only if Filter is selected till Sprint level.
	 *
	 * @param kpiRequest
	 * @param basicProjectConfigId
	 * @param sprintList
	 * @param kpiId
	 * @return
	 */
	Map<String, Object> fetchIssueCountData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId);

	Map<String, Object> fetchSprintCapacityData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId);
}
