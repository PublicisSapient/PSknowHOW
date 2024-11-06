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

package com.publicissapient.kpidashboard.jira.strategy;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import lombok.Setter;

/**
 * Context class for executing the outlier detection strategy.
 * 
 * @author shunaray
 */
@Setter
public class OutlierContext {

	/**
	 * The strategy to be used for outlier detection.
	 */
	private OutlierStrategy strategy;

	/**
	 * Executes the outlier detection strategy.
	 *
	 * @param basicProjectConfigId
	 *            the ID of the basic project configuration
	 * @param projectSprintIssuesMap
	 *            the map of project sprint issues
	 * @return a map of outliers found
	 */
	public Map<String, List<String>> executeStrategy(ObjectId basicProjectConfigId,
			Map<ObjectId, Map<String, List<String>>> projectSprintIssuesMap) {
		return strategy.findOutliers(basicProjectConfigId, projectSprintIssuesMap);
	}
}