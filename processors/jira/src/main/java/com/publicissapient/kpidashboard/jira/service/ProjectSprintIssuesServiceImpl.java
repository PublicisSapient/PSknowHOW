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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.jira.strategy.FindOutliersBelowLowerBoundStrategy;
import com.publicissapient.kpidashboard.jira.strategy.OutlierContext;

/**
 * Service implementation for tracking project sprint issues during processor
 * run.
 *
 * @author shunary
 */
@Service
public class ProjectSprintIssuesServiceImpl implements ProjectSprintIssuesService {

	/**
	 * A map to store project sprint issues. The key is the project
	 * ID, and the value is another map where the key is the sprint name and the
	 * value is a list of issue keys.
	 */
	private final Map<ObjectId, Map<String, List<String>>> projectSprintIssuesMap = new HashMap<>();
	private final OutlierContext outlierContext = new OutlierContext();

	/**
	 * Adds an issue to the project sprint issues map.
	 *
	 * @param basicProjectConfigId
	 *            the project configuration ID
	 * @param sprintName
	 *            the name of the sprint
	 * @param issueKey
	 *            the key of the issue
	 */
	@Override
	public void addIssue(ObjectId basicProjectConfigId, String sprintName, String issueKey) {
		projectSprintIssuesMap.computeIfAbsent(basicProjectConfigId, k -> new HashMap<>())
				.computeIfAbsent(sprintName, k -> new ArrayList<>()).add(issueKey);
	}

	/**
	 * Removes a project from the project sprint issues map.
	 *
	 * @param basicProjectConfigId
	 *            the project configuration ID
	 */
	@Override
	public void removeProject(ObjectId basicProjectConfigId) {
		projectSprintIssuesMap.remove(basicProjectConfigId);
	}

	/**
	 * Retrieves the sprint issue map for a given project.
	 *
	 * @param basicProjectConfigId
	 *            the project configuration ID
	 * @return the sprint issue map for the project
	 */
	@Override
	public Map<String, List<String>> getSprintIssueMapForProject(ObjectId basicProjectConfigId) {
		return projectSprintIssuesMap.getOrDefault(basicProjectConfigId, new HashMap<>());
	}

	/**
	 * Prints a table of sprint issues.
	 *
	 * @param outlierSprintIssueMap
	 *            the map containing sprint names and their corresponding issue keys
	 * @return a formatted string representing the sprint issues table
	 */
	@Override
	public String printSprintIssuesTable(Map<String, List<String>> outlierSprintIssueMap) {
		StringBuilder formattedString = new StringBuilder();
		formattedString.append("<table border='1'>");
		formattedString.append("<tr><th>Sprint Name</th><th>Issue Tagged</th></tr>");

		for (Map.Entry<String, List<String>> entry : outlierSprintIssueMap.entrySet()) {
			formattedString.append("<tr>");
			formattedString.append("<td>").append(entry.getKey()).append("</td>");
			formattedString.append("<td>").append(String.join(", ", entry.getValue())).append("</td>");
			formattedString.append("</tr>");
		}

		formattedString.append("</table>");
		return formattedString.toString();
	}

	/**
	 * Finds outliers below the lower bound in the sprint issue map for a given
	 * project.
	 *
	 * @param basicProjectConfigId
	 *            the project configuration ID
	 * @return a map of sprints with issue counts below the lower bound
	 */
	@Override
	public Map<String, List<String>> findOutliersBelowLowerBound(ObjectId basicProjectConfigId) {
		outlierContext.setStrategy(new FindOutliersBelowLowerBoundStrategy());
		return outlierContext.executeStrategy(basicProjectConfigId, projectSprintIssuesMap);
	}

}
