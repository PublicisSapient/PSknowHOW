package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import java.util.List;
import java.util.Map;

public interface FetchIssuesBasedOnJQL {

    String QUERYDATEFORMAT = "yyyy-MM-dd HH:mm";
    public List<Issue> fetchIssues(Map.Entry<String, ProjectConfFieldMapping> entry) throws InterruptedException;
}
