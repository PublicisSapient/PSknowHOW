package com.publicissapient.kpidashboard.azure.data;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JiraIssueDataFactory {
	private static final String FILE_PATH_JIRA_ISSUES = "/json/jira_issues_collection.json";
	private List<JiraIssue> jiraIssues;
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
			mapper.registerModule(new JavaTimeModule());
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}

		return mapper;
	}

	public List<JiraIssue> getJiraIssues() {
		return jiraIssues;
	}
}
