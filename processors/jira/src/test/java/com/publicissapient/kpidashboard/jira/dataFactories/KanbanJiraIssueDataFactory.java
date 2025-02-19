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

package com.publicissapient.kpidashboard.jira.dataFactories;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sansharm13
 */
@Slf4j
public class KanbanJiraIssueDataFactory {
	private static final String FILE_PATH = "/json/default/kanban_jira_issue.json";
	private List<KanbanJiraIssue> kanbanJiraIssueDataFactory;
	private ObjectMapper mapper = null;

	private KanbanJiraIssueDataFactory() {
	}

	// public static KanbanJiraIssueDataFactory newInstance() {
	// return newInstance(null);
	// }

	public static KanbanJiraIssueDataFactory newInstance(String filePath) {

		KanbanJiraIssueDataFactory factory = new KanbanJiraIssueDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH : filePath;

			kanbanJiraIssueDataFactory = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<KanbanJiraIssue>>() {
					});
		} catch (IOException e) {
			log.error("Error in reading kpi request from file = " + filePath, e);
		}
	}

	private ObjectMapper createObjectMapper() {

		if (mapper == null) {
			mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}

		return mapper;
	}

	public KanbanJiraIssue findTopByBasicProjectConfigId(String basicProjectConfigId) {
		return kanbanJiraIssueDataFactory.stream()
				.filter(kanbanJiraIssue -> kanbanJiraIssue.getBasicProjectConfigId().equals(basicProjectConfigId)).findFirst()
				.orElse(null);
	}

	public List<AdditionalFilter> getAdditionalFilter() {
		return kanbanJiraIssueDataFactory.get(0).getAdditionalFilters();
	}

	public List<KanbanJiraIssue> getKanbanJiraIssues() {
		return kanbanJiraIssueDataFactory;
	}
}
