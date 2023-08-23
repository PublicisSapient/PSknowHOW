package com.publicissapient.kpidashboard.jira.processor;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface KanbanJiraIssueHistoryProcessor {
    KanbanIssueCustomHistory convertToKanbanIssueHistory(Issue issue, ProjectConfFieldMapping projectConfFieldMapping, KanbanJiraIssue kanbanJiraIssue);

    void cleanAllObjects();
}
