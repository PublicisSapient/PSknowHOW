package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public interface CreateAccountHierarchy {

    Set<AccountHierarchy> createAccountHierarchy(List<JiraIssue> jiraIssueList, ProjectConfFieldMapping projectConfig);

}
