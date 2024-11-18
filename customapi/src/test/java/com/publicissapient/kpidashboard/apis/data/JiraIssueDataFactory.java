/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */

@Slf4j
public class JiraIssueDataFactory {
	private static final String FILE_PATH_JIRA_ISSUES = "/json/default/jira_issues.json";
	private List<JiraIssue> jiraIssues;
	private List<SprintWiseStory> sprintWiseStories;
	private ObjectMapper mapper = null;

	private JiraIssueDataFactory() {
	}

	public static JiraIssueDataFactory newInstance(String filePath) {

		JiraIssueDataFactory factory = new JiraIssueDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	public static JiraIssueDataFactory newInstance() {

		return newInstance(null);
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_JIRA_ISSUES : filePath;

			jiraIssues = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<JiraIssue>>() {
					});
		} catch (IOException e) {
			log.error("Error in reading kpi request from file = " + filePath, e);
		}
	}

	private ObjectMapper createObjectMapper() {

		if (mapper == null) {
			mapper = new ObjectMapper();
			mapper.registerModule(new JodaModule());
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}

		return mapper;
	}

	public List<JiraIssue> getJiraIssues() {
		return jiraIssues;
	}

	public List<JiraIssue> getBugs() {
		return jiraIssues.stream().filter(jiraIssue -> jiraIssue.getTypeName().equals("Bug"))
				.collect(Collectors.toList());
	}

	public List<JiraIssue> getStories() {
		return jiraIssues.stream().filter(jiraIssue -> jiraIssue.getTypeName().equals("Story"))
				.collect(Collectors.toList());
	}

	public List<JiraIssue> findIssueByTypeName(String typeName) {
		return jiraIssues.stream().filter(jiraIssue -> jiraIssue.getTypeName().equals(typeName))
				.collect(Collectors.toList());
	}

	public List<JiraIssue> findIssueInTypeNames(List<String> typeName) {
		return jiraIssues.stream().filter(jiraIssue -> typeName.contains(jiraIssue.getTypeName()))
				.collect(Collectors.toList());
	}

	public JiraIssue findIssueById(String id) {
		return jiraIssues.stream().filter(jiraIssue -> jiraIssue.getId().toHexString().equals(id)).findFirst()
				.orElse(null);
	}

	public List<JiraIssue> findIssueByStatus(String status) {
		return jiraIssues.stream().filter(jiraIssue -> jiraIssue.getStatus().equals(status))
				.collect(Collectors.toList());
	}

	public List<JiraIssue> findIssueByNumberList(List<String> ids) {
		return jiraIssues.stream().filter(jiraIssue -> ids.contains(jiraIssue.getNumber()))
				.collect(Collectors.toList());
	}

	public List<JiraIssue> findIssueByStatusInTypeNames(String status, List<String> typeName) {
		return jiraIssues.stream()
				.filter(jiraIssue -> typeName.contains(jiraIssue.getTypeName()) && jiraIssue.getState().equals(status))
				.collect(Collectors.toList());
	}

	public List<JiraIssue> findIssueByOriginalTypeName(List<String> typeName) {
		return jiraIssues.stream().filter(jiraIssue -> typeName.contains(jiraIssue.getOriginalType()))
				.collect(Collectors.toList());
	}

	public Set<String> getUniqueIssueTypes() {
		Set<String> types = new HashSet<>();
		jiraIssues.forEach(jiraIssue -> types.add(jiraIssue.getTypeName()));
		return types;
	}

	public List<JiraIssue> findAutomatedTestCases() {
		return jiraIssues.stream().filter(jiraIssue -> jiraIssue.getIsTestAutomated().equals("Yes"))
				.collect(Collectors.toList());
	}

	public List<SprintWiseStory> getSprintWiseStories() {

		sprintWiseStories = new ArrayList<>();
		List<JiraIssue> stories = getStories();

		Map<String, List<JiraIssue>> sprintWiseIssuesMap = stories.stream()
				.collect(Collectors.groupingBy(jiraIssue -> jiraIssue.getSprintID(), Collectors.toList()));

		sprintWiseIssuesMap.forEach((sprintId, sprintStories) -> {
			SprintWiseStory sprintWiseStory = new SprintWiseStory();
			sprintWiseStory.setSprint(sprintId);
			List<String> storiesIdsOfSprint = sprintStories.stream().map(JiraIssue::getSprintID)
					.collect(Collectors.toList());
			sprintWiseStory.setStoryList(storiesIdsOfSprint);
			JiraIssue firstIssue = sprintStories.size() > 0 ? sprintStories.get(0) : null;
			if (firstIssue != null) {
				sprintWiseStory.setSprintName(firstIssue.getSprintName());
				sprintWiseStory.setSSprintBeginDate(firstIssue.getSprintBeginDate());

			}
			sprintWiseStories.add(sprintWiseStory);
		});

		return sprintWiseStories;
	}

}
