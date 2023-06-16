package com.publicissapient.kpidashboard.apis.data;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JiraIssueReleaseStatusDataFactory {
	private static final String FILE_PATH_JIRA_ISSUES = "/json/default/jira_issue_release_status.json";
	private List<JiraIssueReleaseStatus> jiraIssueReleaseStatusList;
	private ObjectMapper mapper = null;

	public JiraIssueReleaseStatusDataFactory() {
	}

	public static JiraIssueReleaseStatusDataFactory newInstance(String filePath) {

		JiraIssueReleaseStatusDataFactory factory = new JiraIssueReleaseStatusDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	public static JiraIssueReleaseStatusDataFactory newInstance() {

		return newInstance(null);
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_JIRA_ISSUES : filePath;

			jiraIssueReleaseStatusList = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<JiraIssueReleaseStatus>>() {
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

	public List<JiraIssueReleaseStatus> getJiraIssueReleaseStatusList() {
		return jiraIssueReleaseStatusList;
	}
}
