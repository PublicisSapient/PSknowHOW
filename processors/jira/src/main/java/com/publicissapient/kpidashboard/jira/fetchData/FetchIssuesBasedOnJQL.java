package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import org.codehaus.jettison.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public interface FetchIssuesBasedOnJQL {

    String QUERYDATEFORMAT = "yyyy-MM-dd HH:mm";
    public List<Issue> fetchIssues(Map.Entry<String, ProjectConfFieldMapping> entry) throws InterruptedException, FileNotFoundException, JSONException;

}
