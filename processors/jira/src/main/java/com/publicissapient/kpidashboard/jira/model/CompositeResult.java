package com.publicissapient.kpidashboard.jira.model;

import java.util.Set;

import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

import lombok.Data;

@Data
public class CompositeResult {

	private JiraIssue jiraIssue;
	private JiraIssueCustomHistory jiraIssueCustomHistory;
	private Set<ProjectHierarchy> projectHierarchies;
	private Set<SprintDetails> sprintDetailsSet;
	private AssigneeDetails assigneeDetails;
	private KanbanJiraIssue kanbanJiraIssue;
	private KanbanIssueCustomHistory kanbanIssueCustomHistory;

}
