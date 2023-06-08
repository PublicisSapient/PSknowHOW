package com.publicissapient.kpidashboard.jira.service;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

public interface CreateAssigneeDetails {
    AssigneeDetails createAssigneeDetails(ProjectConfFieldMapping projectConfig, Set<Assignee> assigneeSetToSave);
}
