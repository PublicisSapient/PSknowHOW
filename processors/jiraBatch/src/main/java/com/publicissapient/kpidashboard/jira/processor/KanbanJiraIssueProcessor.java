package com.publicissapient.kpidashboard.jira.processor;

import org.codehaus.jettison.json.JSONException;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface KanbanJiraIssueProcessor {
    KanbanJiraIssue convertToKanbanJiraIssue(Issue issue, ProjectConfFieldMapping projectConfFieldMapping, String boardId) throws JSONException;

    void cleanAllObjects();
}
