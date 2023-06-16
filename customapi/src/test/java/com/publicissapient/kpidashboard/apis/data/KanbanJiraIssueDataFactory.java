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
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sansharm13
 *
 */

@Slf4j
public class KanbanJiraIssueDataFactory {
	private static final String FILE_PATH = "/json/kanban/kanban_jira_issue.json";
	private List<KanbanJiraIssue> KanbanJiraIssueDataFactory;
	private ObjectMapper mapper = null;

	private KanbanJiraIssueDataFactory() {
	}

	public static KanbanJiraIssueDataFactory newInstance() {
		return newInstance(null);
	}

	public static KanbanJiraIssueDataFactory newInstance(String filePath) {

		KanbanJiraIssueDataFactory KanbanJiraIssueDataFactory = new KanbanJiraIssueDataFactory();
		KanbanJiraIssueDataFactory.createObjectMapper();
		KanbanJiraIssueDataFactory.init(filePath);
		return KanbanJiraIssueDataFactory;
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH : filePath;

			KanbanJiraIssueDataFactory = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
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

	public List<KanbanJiraIssue> getKanbanJiraIssueDataList() {
		return KanbanJiraIssueDataFactory;
	}

	public List<KanbanJiraIssue> getKanbanJiraIssueDataListByTypeName(List<String> typeName) {
		return KanbanJiraIssueDataFactory.stream().filter(f -> typeName.contains(f.getTypeName()))
				.collect(Collectors.toList());
	}

	public List<KanbanJiraIssue> getKanbanJiraIssueDataListByTypeNameandStatus(List<String> typeName,
			List<String> status) {
		return KanbanJiraIssueDataFactory.stream()
				.filter(f -> (typeName.contains(f.getTypeName())) && (status.contains(f.getStatus())))
				.collect(Collectors.toList());
	}
}
