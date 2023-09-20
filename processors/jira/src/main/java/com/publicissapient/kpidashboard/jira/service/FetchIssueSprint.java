package com.publicissapient.kpidashboard.jira.service;

import java.util.List;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface FetchIssueSprint {

    List<Issue> fetchIssuesSprintBasedOnJql(ProjectConfFieldMapping projectConfig,
                                            ProcessorJiraRestClient client, int pageNumber, String sprintId);
}
