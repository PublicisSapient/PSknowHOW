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
import java.util.Map;

import org.bson.types.ObjectId;

/**
 * Service interface for tracking project sprint issues during processor run
 *
 * @author shunaray
 */
public interface ProjectSprintIssuesService {

	/**
	 * Adds an issue to the specified sprint for a given project ID.
	 *
	 * @param basicProjectConfigId
	 *            the ID of the basic project configuration
	 * @param sprintName
	 *            the name of the sprint
	 * @param issueKey
	 *            the key of the issue to add
	 */
	void addIssue(ObjectId basicProjectConfigId, String sprintName, String issueKey);

	/**
	 * Removes a project based on the given project ID.
	 *
	 * @param basicProjectConfigId
	 *            the ID of the basic project configuration
	 */
	void removeProject(ObjectId basicProjectConfigId);

	/**
	 * Retrieves a map of sprint issues for a given project ID.
	 *
	 * @param basicProjectConfigId
	 *            the ID of the basic project configuration
	 * @return a map where the key is the sprint name and the value is a list of
	 *         issue keys
	 */
	Map<String, List<String>> getSprintIssueMapForProject(ObjectId basicProjectConfigId);

	/**
	 * Prints a table of sprint issues.
	 *
	 * @param outlierSprintIssueMap
	 *            outlier map where the key is the sprint name and the value is a
	 *            list of issue keys
	 * @return a string representation of the sprint issues table
	 */
	String printSprintIssuesTable(Map<String, List<String>> outlierSprintIssueMap);

	/**
	 * Finds outliers below the lower bound for a given project ID.
	 *
	 * @param basicProjectConfigId
	 *            the ID of the basic project configuration
	 * @return a map where the key is the sprint name and the value is a list of
	 *         issue keys that are outliers
	 */
	Map<String, List<String>> belowLowerBoundOutlier(ObjectId basicProjectConfigId);
}
