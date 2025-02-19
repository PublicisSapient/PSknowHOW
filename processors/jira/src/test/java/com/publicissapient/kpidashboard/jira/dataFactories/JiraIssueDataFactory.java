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

package com.publicissapient.kpidashboard.jira.dataFactories;

import java.io.IOException;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JiraIssueDataFactory {
	private static final String FILE_PATH_JIRA_ISSUES = "/json/default/jira_issues.json";
	private List<JiraIssue> jiraIssues;
	private List<SprintWiseStory> sprintWiseStories;
	private ObjectMapper mapper;

	private JiraIssueDataFactory() {
	}

	public static JiraIssueDataFactory newInstance(String filePath) {

		JiraIssueDataFactory factory = new JiraIssueDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
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

	private void createObjectMapper() {

		if (mapper == null) {
			mapper = new ObjectMapper();
			mapper.registerModule(new JodaModule());
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}

		// return mapper;
	}

	public List<JiraIssue> getJiraIssues() {
		return jiraIssues;
	}

	public List<AdditionalFilter> getAdditionalFilter() {
		return jiraIssues.get(0).getAdditionalFilters();
	}

	public JiraIssue findTopByBasicProjectConfigId(String basicProjectConfigId) {
		return jiraIssues.stream().filter(jiraIssue -> jiraIssue.getBasicProjectConfigId().equals(basicProjectConfigId))
				.findFirst().orElse(null);
	}
}
