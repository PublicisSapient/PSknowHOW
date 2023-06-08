package com.publicissapient.kpidashboard.jira.service;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import java.util.Map;

public interface CreateJiraIssueHistory {
    JiraIssueCustomHistory createIssueCustomHistory(ProjectConfFieldMapping projectConfig, String issueId, JiraIssue jiraIssue, Issue issue,
                                                    FieldMapping fieldMapping, Map<String, IssueField> fields);
}
