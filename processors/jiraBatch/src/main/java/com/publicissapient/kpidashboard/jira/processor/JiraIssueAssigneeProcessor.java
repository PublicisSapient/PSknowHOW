package com.publicissapient.kpidashboard.jira.processor;

import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface JiraIssueAssigneeProcessor {
	AssigneeDetails createAssigneeDetails(ProjectConfFieldMapping projectConfig, JiraIssue jiraIssue);
	
	void cleanAllObjects();
}
