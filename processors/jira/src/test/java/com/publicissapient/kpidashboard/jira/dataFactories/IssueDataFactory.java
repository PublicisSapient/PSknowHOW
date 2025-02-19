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
import com.fasterxml.jackson.datatype.joda.JodaModule;

import lombok.extern.slf4j.Slf4j;

/**
 * @author hiren babariya
 */
@Slf4j
public class IssueDataFactory {

	private static final String FILE_PATH_JIRA_ISSUES = "/json/default/issues.json";
	// private List<Issue> issues;
	private List<Object> issueObjects;
	private ObjectMapper mapper = null;

	private IssueDataFactory() {
	}

	public static IssueDataFactory newInstance(String filePath) {

		IssueDataFactory factory = new IssueDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	public static IssueDataFactory newInstance() {

		return newInstance(null);
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_JIRA_ISSUES : filePath;
			// Read the JSON file as a List<Object> and then convert each object to Issue
			issueObjects = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<Object>>() {
					});

			// Convert each object to Issue
			// issues = mapper.convertValue(issueObjects, new TypeReference<List<Issue>>() {
			// });
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

	public List<Object> getIssues() {
		return issueObjects;
	}
}
