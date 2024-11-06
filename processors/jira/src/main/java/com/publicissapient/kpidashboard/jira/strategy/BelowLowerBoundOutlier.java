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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

/**
 * Strategy to find outliers below the lower bound in a given set of sprint
 * issues.
 * 
 * @author shunaray
 */
public class BelowLowerBoundOutlier implements OutlierStrategy {

	/**
	 * Finds outliers below the lower bound for the given project configuration ID
	 * and sprint issues map.
	 *
	 * @param basicProjectConfigId
	 *            the project configuration ID
	 * @param projectSprintIssuesMap
	 *            the map of project sprint issues
	 * @return a map of sprint issues that are considered outliers
	 */
	@Override
	public Map<String, List<String>> findOutliers(ObjectId basicProjectConfigId,
			Map<ObjectId, Map<String, List<String>>> projectSprintIssuesMap) {
		Map<String, List<String>> sprintIssueMap = projectSprintIssuesMap.get(basicProjectConfigId);

		if (sprintIssueMap == null) {
			return Collections.emptyMap();
		}

		List<Integer> issueCounts = new ArrayList<>();
		for (List<String> issues : sprintIssueMap.values()) {
			issueCounts.add(issues.size());
		}

		if (issueCounts.isEmpty()) {
			return Collections.emptyMap();
		}

		Collections.sort(issueCounts);
		// Calculate Q1, Q3, and IQR
		double q1 = calculatePercentile(issueCounts, 25);
		double q3 = calculatePercentile(issueCounts, 75);
		double iqr = q3 - q1;

		// Determine the lower bound
		double lowerBound = q1 - 1.5 * iqr;

		// Find outliers below the lower bound and return sprint issue map
		Map<String, List<String>> outliers = new HashMap<>();
		for (Map.Entry<String, List<String>> entry : sprintIssueMap.entrySet()) {
			if (entry.getValue().size() < lowerBound) {
				outliers.put(entry.getKey(), entry.getValue());
			}
		}

		return outliers;
	}

	/**
	 * Calculates the percentile value from a sorted list of integers.
	 *
	 * @param sortedList
	 *            the sorted list of integers
	 * @param percentile
	 *            the percentile to calculate
	 * @return the calculated percentile value
	 */
	private double calculatePercentile(List<Integer> sortedList, double percentile) {
		int index = (int) Math.ceil(percentile / 100.0 * sortedList.size()) - 1;
		return sortedList.get(index);
	}
}