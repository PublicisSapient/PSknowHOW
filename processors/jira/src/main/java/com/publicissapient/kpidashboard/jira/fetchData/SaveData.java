package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public interface SaveData {
    void saveData(List<JiraIssue> jiraIssuesToSave, List<JiraIssueCustomHistory> jiraIssueHistoryToSave, List<SprintDetails> sprintDetailsToSave, Set<AccountHierarchy> accountHierarchiesToSave, AssigneeDetails assigneeDetailsToSave);
}
