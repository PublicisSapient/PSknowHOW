package com.publicissapient.kpidashboard.jira.reader;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.jira.client.JiraClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.helper.JiraHelper;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.service.FetchIssueBasedOnBoard;
import com.publicissapient.kpidashboard.jira.service.JiraCommonService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IssueScrumBoardReader implements ItemReader<Issue> {

	@Autowired
	FetchProjectConfiguration fetchProjectConfiguration;

	@Autowired
	JiraClient jiraClient;

	@Autowired
	FetchIssueBasedOnBoard fetchIssueBasedOnBoard;

	@Autowired
	JiraIssueRepository jiraIssueRepository;

	@Autowired
	ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Autowired
	JiraProcessorConfig jiraProcessorConfig;

	@Autowired
	JiraCommonService jiraCommonService;

	private Map<String, List<ProjectConfFieldMapping>> projConfFieldMapping;
	private int boardIndex = 0;
	private List<Issue> issues;
	private int issueIndex = 0;
	private int startAt = 0;
	private ProcessorJiraRestClient client;

	@Override
	public Issue read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		log.info("**** Jira Issue fetch for Scrum started * * *");
		int pageSize = jiraProcessorConfig.getPageSize();
		if (null == projConfFieldMapping) {
			projConfFieldMapping = fetchProjectConfiguration.fetchConfiguration(false);
		}

		/**if (projConfFieldMapping.entrySet().iterator().hasNext()) {

			KerberosClient krb5Client = null;
			Map.Entry<String, List<ProjectConfFieldMapping>> entry = projConfFieldMapping.entrySet().iterator().next();
			client = jiraClient.getClient(entry, krb5Client);
			ProjectConfFieldMapping projectConfig = entry.getValue();
			boolean dataExist = false;
			dataExist = (jiraIssueRepository
					.findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);
			ProcessorExecutionTraceLog processorExecutionTraceLog = jiraCommonService.createTraceLog(projectConfig);
			String queryDate = JiraHelper.getDeltaDate(processorExecutionTraceLog.getLastSuccessfulRun());
			String userTimeZone = jiraCommonService.getUserTimeZone(projectConfig, krb5Client);

			List<BoardDetails> boardDetailsList = projectConfig.getProjectToolConfig().getBoards();
			if (CollectionUtils.isNotEmpty(boardDetailsList) && boardIndex < boardDetailsList.size()) {
				BoardDetails board = boardDetailsList.get(boardIndex);
				if (CollectionUtils.isEmpty(issues) || issueIndex >= issues.size()) {
					SearchResult searchResult = jiraCommonService.getIssues(board, projectConfig, queryDate,
							userTimeZone, startAt, dataExist);
					issues = JiraHelper.getIssuesFromResult(searchResult);
					issueIndex = 0;
					startAt += pageSize;
				}
				if (CollectionUtils.isNotEmpty(issues) && issueIndex < issues.size()) {
					Issue issue = issues.get(issueIndex);
					issueIndex++;
					return issue;
				}
				boardIndex++;
			}

		}**/

		return null;
	}

}
