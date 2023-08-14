package com.publicissapient.kpidashboard.jira.processor;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface JiraIssueHistoryProcessor {
	JiraIssueCustomHistory convertToJiraIssueHistory(Issue issue, ProjectConfFieldMapping projectConfig,
			JiraIssue jiraIssue);

	void cleanAllObjects();
}
