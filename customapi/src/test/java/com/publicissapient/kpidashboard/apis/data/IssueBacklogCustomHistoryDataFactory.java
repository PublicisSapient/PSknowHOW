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
import com.publicissapient.kpidashboard.common.model.jira.IssueBacklogCustomHistory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IssueBacklogCustomHistoryDataFactory {
	private static final String FILE_PATH_JIRA_ISSUES = "/json/default/jira_issue_custom_history.json";
	private List<IssueBacklogCustomHistory> issueHistoryIssues = new ArrayList<>();
	private ObjectMapper mapper = null;

	private IssueBacklogCustomHistoryDataFactory() {
	}

	public static IssueBacklogCustomHistoryDataFactory newInstance(String filePath) {

		IssueBacklogCustomHistoryDataFactory factory = new IssueBacklogCustomHistoryDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	public static IssueBacklogCustomHistoryDataFactory newInstance() {

		return newInstance(null);
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_JIRA_ISSUES : filePath;

			issueHistoryIssues = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<IssueBacklogCustomHistory>>() {
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

	public List<IssueBacklogCustomHistory> getIssueBacklogCustomHistory() {
		return issueHistoryIssues;
	}

	public List<IssueBacklogCustomHistory> findIssueByTypeName(String typeName) {
		return issueHistoryIssues.stream().filter(jiraIssue -> jiraIssue.getStoryType().equals(typeName))
				.collect(Collectors.toList());
	}

	public List<IssueBacklogCustomHistory> findIssueInTypeNames(List<String> typeName) {
		return issueHistoryIssues.stream().filter(jiraIssue -> typeName.contains(jiraIssue.getStoryType()))
				.collect(Collectors.toList());
	}

	public IssueBacklogCustomHistory findIssueById(String id) {
		return issueHistoryIssues.stream().filter(jiraIssue -> jiraIssue.getId().toHexString().equals(id)).findFirst()
				.orElse(null);
	}

	// added for find unique jira issue custom history.
	public List<IssueBacklogCustomHistory> getUniqueIssueBacklogCustomHistory() {
		Set<String> uniqueStoryIds = new HashSet<>();
		return issueHistoryIssues.stream().filter(jiraHistoryIssue -> uniqueStoryIds.add(jiraHistoryIssue.getStoryID()))
				.collect(Collectors.toList());
	}

}
