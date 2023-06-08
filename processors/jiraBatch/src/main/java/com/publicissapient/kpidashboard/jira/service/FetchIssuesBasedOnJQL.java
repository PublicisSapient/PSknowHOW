package com.publicissapient.kpidashboard.jira.service;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface FetchIssuesBasedOnJQL {

	List<Issue> fetchIssues(Map.Entry<String, ProjectConfFieldMapping> entry, ProcessorJiraRestClient client,
			KerberosClient krb5Client) throws InterruptedException, FileNotFoundException, JSONException;

}
