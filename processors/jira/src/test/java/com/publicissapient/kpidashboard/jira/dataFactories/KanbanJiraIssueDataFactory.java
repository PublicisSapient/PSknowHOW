package com.publicissapient.kpidashboard.jira.dataFactories;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
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
	private static final String FILE_PATH = "/json/default/kanban_jira_issue.json";
	private List<KanbanJiraIssue> kanbanJiraIssueDataFactory;
	private ObjectMapper mapper = null;

	private KanbanJiraIssueDataFactory() {
	}

//	public static KanbanJiraIssueDataFactory newInstance() {
//		return newInstance(null);
//	}

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

	public KanbanJiraIssue findTopByBasicProjectConfigId(String basicProjectConfigId){
		return kanbanJiraIssueDataFactory.stream().filter(kanbanJiraIssue -> kanbanJiraIssue.getBasicProjectConfigId().equals(basicProjectConfigId)).findFirst()
				.orElse(null);
	}

	public List<AdditionalFilter> getAdditionalFilter(){
		return kanbanJiraIssueDataFactory.get(0).getAdditionalFilters();
	}

	public List<KanbanJiraIssue> getKanbanJiraIssues(){
		return kanbanJiraIssueDataFactory;
	}
}



