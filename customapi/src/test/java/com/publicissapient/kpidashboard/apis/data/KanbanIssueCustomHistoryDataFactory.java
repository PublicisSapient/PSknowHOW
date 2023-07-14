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
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sansharm13
 *
 */
@Slf4j
public class KanbanIssueCustomHistoryDataFactory {
	private static final String FILE_PATH = "/json/kanban/kanban_issue_custom_history.json";
	private List<KanbanIssueCustomHistory> KanbanIssueCustomHistoryDataFactory;
	private ObjectMapper mapper = null;

	private KanbanIssueCustomHistoryDataFactory() {
	}

	public static KanbanIssueCustomHistoryDataFactory newInstance() {
		return newInstance(null);
	}

	public static KanbanIssueCustomHistoryDataFactory newInstance(String filePath) {

		KanbanIssueCustomHistoryDataFactory KanbanIssueCustomHistoryDataFactory = new KanbanIssueCustomHistoryDataFactory();
		KanbanIssueCustomHistoryDataFactory.createObjectMapper();
		KanbanIssueCustomHistoryDataFactory.init(filePath);
		return KanbanIssueCustomHistoryDataFactory;
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH : filePath;

			KanbanIssueCustomHistoryDataFactory = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<KanbanIssueCustomHistory>>() {
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

	public List<KanbanIssueCustomHistory> getKanbanIssueCustomHistoryDataList() {
		return KanbanIssueCustomHistoryDataFactory;
	}

	public List<KanbanIssueCustomHistory> getKanbanIssueCustomHistoryDataListByTypeName(List<String> typeName) {
		return KanbanIssueCustomHistoryDataFactory.stream().filter(f -> typeName.contains(f.getStoryType()))
				.collect(Collectors.toList());
	}

}
