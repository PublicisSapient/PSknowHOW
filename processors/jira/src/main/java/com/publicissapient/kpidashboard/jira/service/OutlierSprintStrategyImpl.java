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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OutlierSprintStrategyImpl implements OutlierSprintStrategy {

	@Autowired
	private SprintRepository sprintDetailsRepository;

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	/**
	 * Finds outlier sprints for a given project ID.
	 *
	 * @param basicProjectConfigId
	 *            the project configuration ID
	 * @return a map of SprintDetails to a list of issue numbers
	 */
	@Override
	public Map<String, List<String>> execute(ObjectId basicProjectConfigId) {

		List<SprintDetails> projectSprints = sprintDetailsRepository
				.findByBasicProjectConfigIdWithFieldsSorted(basicProjectConfigId);

		if (projectSprints.isEmpty()) {
			return Collections.emptyMap();
		}

		List<SprintDetails> overlappingSprints = new ArrayList<>();

		// Check for overlapping projectSprints
		for (int i = 0; i < projectSprints.size() - 1; i++) {
			SprintDetails currentSprint = projectSprints.get(i);
			SprintDetails nextSprint = projectSprints.get(i + 1);

			if (currentSprint.getEndDate() == null || nextSprint.getStartDate() == null) {
				continue; // Skip comparison if either date is null
			}

			LocalDateTime currentEndDate = DateUtil.stringToLocalDateTime(currentSprint.getEndDate(),
					DateUtil.TIME_FORMAT_WITH_SEC);
			LocalDateTime nextStartDate = DateUtil.stringToLocalDateTime(nextSprint.getStartDate(),
					DateUtil.TIME_FORMAT_WITH_SEC);

			if (!currentEndDate.toLocalDate().isEqual(nextStartDate.toLocalDate())
					&& currentEndDate.isAfter(nextStartDate)) {
				overlappingSprints.add(currentSprint);
				overlappingSprints.add(nextSprint);
				log.info("Overlapping sprints detected: {} and {} for projectId: {}", currentSprint.getSprintName(),
						nextSprint.getSprintName(), basicProjectConfigId);
			}
		}

		return getIssueTaggedToSprint(overlappingSprints, basicProjectConfigId);
	}

	/**
	 * Retrieves issues tagged to the given outlier sprints for a specific project
	 * ID.
	 *
	 * @param overlappingSprints
	 *            the list of overlapping sprints
	 * @param basicProjectConfigId
	 *            the project configuration ID
	 * @return a map of SprintDetails to a list of issue numbers
	 */
	private Map<String, List<String>> getIssueTaggedToSprint(List<SprintDetails> overlappingSprints,
			ObjectId basicProjectConfigId) {

		if (overlappingSprints.isEmpty()) {
			return Collections.emptyMap();
		}

		Set<String> sprintIds = overlappingSprints.stream().map(SprintDetails::getSprintID).collect(Collectors.toSet());

		// Retrieve issues associated with the outlier sprints
		List<JiraIssue> issues = jiraIssueRepository.findBySprintIDInAndBasicProjectConfigId(sprintIds,
				basicProjectConfigId.toString());

		// Group issues by sprint ID
		Map<String, List<String>> issuesBySprintId = issues.stream().collect(Collectors
				.groupingBy(JiraIssue::getSprintID, Collectors.mapping(JiraIssue::getNumber, Collectors.toList())));

		// Map outlier sprints to their respective issue numbers
		Map<String, List<String>> outlierSprintIssuesMap = new HashMap<>();
		for (SprintDetails sprint : overlappingSprints) {
			List<String> issueNumbers = issuesBySprintId.getOrDefault(sprint.getSprintID(), Collections.emptyList());
			outlierSprintIssuesMap.put(sprint.getSprintName(), issueNumbers);
		}

		return outlierSprintIssuesMap;
	}

	/**
	 * Prints a table of sprint issues for email format
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
}
