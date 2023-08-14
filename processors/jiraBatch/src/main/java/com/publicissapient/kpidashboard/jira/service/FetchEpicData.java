package com.publicissapient.kpidashboard.jira.service;

import java.util.List;
import java.util.Map;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface FetchEpicData {

	List<Issue> fetchEpic(Map.Entry<String, ProjectConfFieldMapping> entry, String boardId,
			ProcessorJiraRestClient clientIncoming, KerberosClient krb5Client) throws InterruptedException;
}
