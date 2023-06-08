package com.publicissapient.kpidashboard.jira.service;

import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FetchIssuesBasedOnJQLImpl implements FetchIssuesBasedOnJQL {

	String QUERYDATEFORMAT = "yyyy-MM-dd HH:mm";
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
	private static final String MSG_JIRA_CLIENT_SETUP_FAILED = "Jira client setup failed. No results obtained. Check your jira setup.";
	private static final String ERROR_MSG_401 = "Error 401 connecting to JIRA server, your credentials are probably wrong. Note: Ensure you are using JIRA user name not your email address.";
	private static final String ERROR_MSG_NO_RESULT_WAS_AVAILABLE = "No result was available from Jira unexpectedly - defaulting to blank response. The reason for this fault is the following : {}";
	private static final String NO_RESULT_QUERY = "No result available for query: {}";

	@Autowired
	private JiraProcessorConfig jiraProcessorConfig;

	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	PSLogData psLogData = new PSLogData();

	private ProcessorJiraRestClient client;

	@Autowired
	private KanbanJiraIssueRepository kanbanJiraRepo;

	// @Autowired
	// private RabbitTemplate template;
	@Autowired
	private TransformFetchedIssueToJiraIssue transformFetchedIssue;

	@Autowired
	private JiraCommonService jiraCommonService;

	@Autowired
	ValidateData validateData;

	@Autowired
	private CreateAccountHierarchy createAccountHierarchy;

	@Autowired
	private FetchSprintReportImpl fetchSprintReport;

	@Autowired
	private SaveData saveData;

	@Autowired
	private CreateAssigneeDetails createAssigneeDetails;

	@Override
	public List<Issue> fetchIssues(Map.Entry<String, ProjectConfFieldMapping> entry,
			ProcessorJiraRestClient clientIncoming, KerberosClient krb5Client) throws JSONException {

		ProjectConfFieldMapping projectConfig = entry.getValue();
		boolean dataExist = false;
		dataExist = (jiraIssueRepository
				.findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);
		psLogData.setKanban("false");
		return jiraCommonService.fetchIssues(entry, clientIncoming, krb5Client, dataExist);
	}

}
