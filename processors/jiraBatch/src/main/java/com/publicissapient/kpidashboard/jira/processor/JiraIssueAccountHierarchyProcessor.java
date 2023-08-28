package com.publicissapient.kpidashboard.jira.processor;

import java.util.Set;

import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface JiraIssueAccountHierarchyProcessor {

	Set<AccountHierarchy> createAccountHierarchy(JiraIssue jiraIssue, ProjectConfFieldMapping projectConfig, Set<SprintDetails> sprintDetailsSet);

	void cleanAllObjects();

}
