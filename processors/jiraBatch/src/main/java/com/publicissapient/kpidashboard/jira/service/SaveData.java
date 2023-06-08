package com.publicissapient.kpidashboard.jira.service;

import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

public interface SaveData {

    void saveData(List<JiraIssue> jiraIssuesToSave, List<JiraIssueCustomHistory> jiraIssueHistoryToSave, List<SprintDetails> sprintDetailsToSave, Set<AccountHierarchy> accountHierarchiesToSave, AssigneeDetails assigneeDetailsToSave, List<KanbanJiraIssue> kanbanJiraIssues, List<KanbanIssueCustomHistory> kanbanIssueCustomHistoryList, Set<KanbanAccountHierarchy> kanbanAccountHierarchies);
}
