package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface FetchEpicData {

    List<Issue> fetchEpic(Map.Entry<String, ProjectConfFieldMapping> entry, String boardId, ProcessorJiraRestClient clientIncoming, KerberosClient krb5Client) throws InterruptedException;
}
