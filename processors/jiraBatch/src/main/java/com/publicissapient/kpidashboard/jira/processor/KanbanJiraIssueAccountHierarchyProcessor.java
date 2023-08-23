package com.publicissapient.kpidashboard.jira.processor;

import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import java.util.Set;

public interface KanbanJiraIssueAccountHierarchyProcessor {
    Set<KanbanAccountHierarchy> createKanbanAccountHierarchy(KanbanJiraIssue kanbanJiraIssue, ProjectConfFieldMapping projectConfFieldMapping);

    void cleanAllObjects();
}
