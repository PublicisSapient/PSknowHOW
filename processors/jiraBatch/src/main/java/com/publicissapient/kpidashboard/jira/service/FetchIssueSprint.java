package com.publicissapient.kpidashboard.jira.service;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import java.util.List;

public interface FetchIssueSprint {

    List<Issue> fetchIssuesSprintBasedOnJql(ProjectConfFieldMapping projectConfig,
                                            ProcessorJiraRestClient client, int pageNumber, String sprintId);
}
