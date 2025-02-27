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

package com.publicissapient.kpidashboard.rally.service;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

public interface OutlierSprintStrategy {
	/**
	 * Finds overlapping sprints.
	 *
	 * @param basicProjectConfigId
	 *          the ID of the basic project configuration
	 * @return a map of overlapping sprints
	 */
	Map<String, List<String>> execute(ObjectId basicProjectConfigId);

	/**
	 * Prints a table of sprint issues for outlier sprint email
	 *
	 * @param outlierSprintIssueMap
	 *          outlier map where the key is the sprint name and the value is a list
	 *          of issue keys
	 * @return a string representation of the sprint issues table
	 */
	String printSprintIssuesTable(Map<String, List<String>> outlierSprintIssueMap);
}
