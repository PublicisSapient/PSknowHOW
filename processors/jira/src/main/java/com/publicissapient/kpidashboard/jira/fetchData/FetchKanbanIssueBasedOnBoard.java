package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import java.util.List;
import java.util.Map;

public interface FetchKanbanIssueBasedOnBoard {
    List<Issue> fetchIssueBasedOnBoard(Map.Entry<String, ProjectConfFieldMapping> entry, ProcessorJiraRestClient clientIncoming, KerberosClient krb5Client);
}
