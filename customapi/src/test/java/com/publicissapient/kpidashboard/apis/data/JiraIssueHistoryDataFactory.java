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
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */

@Slf4j
public class JiraIssueHistoryDataFactory {
	private static final String FILE_PATH_JIRA_ISSUES = "/json/default/jira_issue_custom_history.json";
	private List<JiraIssueCustomHistory> jiraHistoryIssues = new ArrayList<>();
	private ObjectMapper mapper = null;

	private JiraIssueHistoryDataFactory() {
	}

	public static JiraIssueHistoryDataFactory newInstance(String filePath) {

		JiraIssueHistoryDataFactory factory = new JiraIssueHistoryDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	public static JiraIssueHistoryDataFactory newInstance() {

		return newInstance(null);
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_JIRA_ISSUES : filePath;

			jiraHistoryIssues = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<JiraIssueCustomHistory>>() {
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
			mapper.registerModule(new JavaTimeModule());
		}

		return mapper;
	}

	public List<JiraIssueCustomHistory> getJiraIssueCustomHistory() {
		return jiraHistoryIssues;
	}

	public List<JiraIssueCustomHistory> findIssueByTypeName(String typeName) {
		return jiraHistoryIssues.stream().filter(jiraIssue -> jiraIssue.getStoryType().equals(typeName))
				.collect(Collectors.toList());
	}

	public List<JiraIssueCustomHistory> findIssueInTypeNames(List<String> typeName) {
		return jiraHistoryIssues.stream().filter(jiraIssue -> typeName.contains(jiraIssue.getStoryType()))
				.collect(Collectors.toList());
	}

	public JiraIssueCustomHistory findIssueById(String id) {
		return jiraHistoryIssues.stream().filter(jiraIssue -> jiraIssue.getId().toHexString().equals(id)).findFirst()
				.orElse(null);
	}

	// added for find unique jira issue custom history.
	public List<JiraIssueCustomHistory> getUniqueJiraIssueCustomHistory() {
		Set<String> uniqueStoryIds = new HashSet<>();
		return jiraHistoryIssues.stream().filter(jiraHistoryIssue -> uniqueStoryIds.add(jiraHistoryIssue.getStoryID()))
				.collect(Collectors.toList());
	}

}
