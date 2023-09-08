package com.publicissapient.kpidashboard.jira.processor;

import java.util.Set;

import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface KanbanJiraIssueAccountHierarchyProcessor {
    Set<KanbanAccountHierarchy> createKanbanAccountHierarchy(KanbanJiraIssue kanbanJiraIssue, ProjectConfFieldMapping projectConfFieldMapping);

    void cleanAllObjects();
}
