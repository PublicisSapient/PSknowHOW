package com.publicissapient.kpidashboard.apis.data;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.publicissapient.kpidashboard.common.model.jira.IssueBacklog;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IssueBacklogDataFactory {
	private static final String FILE_PATH_JIRA_ISSUES = "/json/default/jira_issues.json";
	private List<IssueBacklog> issueBacklogs;
	private List<SprintWiseStory> sprintWiseStories;
	private ObjectMapper mapper = null;

	private IssueBacklogDataFactory() {
	}

	public static IssueBacklogDataFactory newInstance(String filePath) {

		IssueBacklogDataFactory factory = new IssueBacklogDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	public static IssueBacklogDataFactory newInstance() {

		return newInstance(null);
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_JIRA_ISSUES : filePath;

			issueBacklogs = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<IssueBacklog>>() {
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

	public List<IssueBacklog> getIssueBacklogs() {
		return issueBacklogs;
	}

	public List<IssueBacklog> findIssueByNumberList(List<String> ids) {
		return issueBacklogs.stream().filter(jiraIssue -> ids.contains(jiraIssue.getNumber()))
				.collect(Collectors.toList());
	}

}
