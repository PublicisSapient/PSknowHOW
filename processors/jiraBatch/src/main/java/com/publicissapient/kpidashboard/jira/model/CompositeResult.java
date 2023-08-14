package com.publicissapient.kpidashboard.jira.model;

import java.util.Set;

import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;

import lombok.Data;

@Data
public class CompositeResult {
	
	private JiraIssue jiraIssue;
	private JiraIssueCustomHistory jiraIssueCustomHistory;
	private Set<AccountHierarchy> accountHierarchies;
	private AssigneeDetails assigneeDetails;
	
}
