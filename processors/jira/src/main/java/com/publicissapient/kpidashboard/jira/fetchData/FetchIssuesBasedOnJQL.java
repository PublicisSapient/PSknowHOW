package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import org.codehaus.jettison.json.JSONException;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

public interface FetchIssuesBasedOnJQL {

    List<Issue> fetchIssues(Map.Entry<String, ProjectConfFieldMapping> entry, ProcessorJiraRestClient client) throws InterruptedException, FileNotFoundException, JSONException;

}
