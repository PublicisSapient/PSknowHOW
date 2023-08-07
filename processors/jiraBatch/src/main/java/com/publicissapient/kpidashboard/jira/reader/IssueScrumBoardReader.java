package com.publicissapient.kpidashboard.jira.reader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.jira.client.JiraClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.model.ReadData;
import com.publicissapient.kpidashboard.jira.service.JiraCommonService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@StepScope
public class IssueScrumBoardReader implements ItemReader<ReadData> {

	@Autowired
	FetchProjectConfiguration fetchProjectConfiguration;

	@Autowired
	JiraClient jiraClient;

	@Autowired
	JiraCommonService jiraCommonService;

	@Autowired
	JiraIssueRepository jiraIssueRepository;

	@Autowired
	ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Autowired
	JiraProcessorConfig jiraProcessorConfig;

	private Iterator<Map.Entry<String, List<ProjectConfFieldMapping>>> projectConfMapIterator;
	private Iterator<BoardDetails> boardIterator;
	private Iterator<Issue> issueIterator;
	private Iterator<ProjectConfFieldMapping> projectConfigIterator;
	int pageSize = 30;
	private ProjectConfFieldMapping projectConfFieldMapping;
	int pageNumber = 0;
	String boardId = "";
	List<Issue> issues = new ArrayList<>();

	public void initializeReader() {
		log.info("**** Jira Issue fetch for Scrum started * * *");
		pageSize = jiraProcessorConfig.getPageSize();
		Map<String, List<ProjectConfFieldMapping>> projConfFieldMappingByUrl = new LinkedHashMap<>();
		projConfFieldMappingByUrl = fetchProjectConfiguration.fetchConfiguration(false);
		if (MapUtils.isNotEmpty(projConfFieldMappingByUrl)) {
			projectConfMapIterator = projConfFieldMappingByUrl.entrySet().iterator();
		}
	}

	@Override
	public ReadData read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		if (null == projectConfMapIterator) {
			log.info("Gathering data for batch");
			initializeReader();
		}
		ReadData readData = null;
		if (projectConfigIterator == null || !projectConfigIterator.hasNext()) {
			if (projectConfMapIterator.hasNext()) {
				Map.Entry<String, List<ProjectConfFieldMapping>> entry = projectConfMapIterator.next();
				projectConfigIterator = entry.getValue().iterator();
			}

		}
		if (boardIterator == null || !boardIterator.hasNext()) {
			if (projectConfigIterator.hasNext()) {
				projectConfFieldMapping = projectConfigIterator.next();
				if (CollectionUtils.isNotEmpty(projectConfFieldMapping.getProjectToolConfig().getBoards())) {
					boardIterator = projectConfFieldMapping.getProjectToolConfig().getBoards().iterator();
				}
			}
		}
		if (issueIterator == null || !issueIterator.hasNext()) {
			KerberosClient krb5Client = null;
			ProcessorJiraRestClient client = jiraClient.getClient(projectConfFieldMapping, krb5Client);
			if (null == issueIterator || issues.size() < pageSize) {
				if (boardIterator.hasNext()) {
					BoardDetails boardDetails = boardIterator.next();
					boardId = boardDetails.getBoardId();
					pageNumber = 1200;
					fetchIssues(krb5Client, client);
				}

			} else {
				fetchIssues(krb5Client, client);
			}

			if (CollectionUtils.isNotEmpty(issues)) {
				issueIterator = issues.iterator();
			}
		}
		if (null != issueIterator && issueIterator.hasNext()) {
			Issue issue = issueIterator.next();
			readData = new ReadData();
			readData.setIssue(issue);
			readData.setProjectConfFieldMapping(projectConfFieldMapping);
		}
		if ((null == projectConfMapIterator) || (!projectConfigIterator.hasNext() && !boardIterator.hasNext()
				&& (!issueIterator.hasNext() && issues.size() < pageSize))) {
			log.info("Data of all projects has been fetched");
			readData = null;
		}
		return readData;

	}

	private void fetchIssues(KerberosClient krb5Client, ProcessorJiraRestClient client) {
		log.info("Reading data for project : {} boardid : {} , page No : {}", projectConfFieldMapping.getProjectName(),
				boardId, pageNumber / pageSize);
		issues = jiraCommonService.fetchIssueBasedOnBoard(projectConfFieldMapping, client, krb5Client, pageNumber,
				boardId);
		pageNumber += pageSize;
	}

}
