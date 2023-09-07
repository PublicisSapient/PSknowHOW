package com.publicissapient.kpidashboard.jira.service;

import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.publicissapient.kpidashboard.common.model.jira.IssueBacklog;
import com.publicissapient.kpidashboard.common.model.jira.IssueBacklogCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import java.util.List;

public interface CreateIssueBacklog {
    void createIssueBacklogandIssueBacklogHistory(List<JiraIssue> jiraIssuesToSave, List<JiraIssueCustomHistory> jiraIssueHistoryToSave, List<JiraIssue> jiraIssuesToDelete, List<JiraIssueCustomHistory> jiraIssueHistoryToDelete, List<IssueBacklog> issueBacklogToSave, List<IssueBacklogCustomHistory> issueBacklogCustomHistoryToSave, List<IssueBacklog> issueBacklogToDelete, List<IssueBacklogCustomHistory> issueBacklogCustomHistoryToDelete, JiraIssue jiraIssue, JiraIssueCustomHistory jiraIssueHistory, IssueField sprint, String issueId, ProjectConfFieldMapping projectConfig, String issueNumber);
}
