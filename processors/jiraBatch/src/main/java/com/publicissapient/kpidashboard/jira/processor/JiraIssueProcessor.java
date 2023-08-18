package com.publicissapient.kpidashboard.jira.processor;

import org.codehaus.jettison.json.JSONException;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface JiraIssueProcessor {

	JiraIssue convertToJiraIssue(Issue currentPagedJiraRs, ProjectConfFieldMapping projectConfig, String boardId)
			throws JSONException;

	void cleanAllObjects();
}