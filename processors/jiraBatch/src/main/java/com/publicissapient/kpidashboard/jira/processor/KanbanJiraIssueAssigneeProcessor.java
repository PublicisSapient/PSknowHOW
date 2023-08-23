package com.publicissapient.kpidashboard.jira.processor;

import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface KanbanJiraIssueAssigneeProcessor {

    AssigneeDetails createKanbanAssigneeDetails(ProjectConfFieldMapping projectConfig, KanbanJiraIssue jiraIssue);

    void cleanAllObjects();
}
