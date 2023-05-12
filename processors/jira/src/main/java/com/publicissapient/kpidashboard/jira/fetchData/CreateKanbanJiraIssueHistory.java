package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface CreateKanbanJiraIssueHistory {
    KanbanIssueCustomHistory createKanbanIssueCustomHistory(ProjectConfFieldMapping projectConfig, KanbanJiraIssue jiraIssue, Issue issue,
                                                      FieldMapping fieldMapping);
}
