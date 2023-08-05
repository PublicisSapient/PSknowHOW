package com.publicissapient.kpidashboard.jira.model;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;

import lombok.Data;

@Data
public class CompositeResult {
	
	private JiraIssue jiraIssue;
	private JiraIssueCustomHistory jiraIssueCustomHistory;
	
}
