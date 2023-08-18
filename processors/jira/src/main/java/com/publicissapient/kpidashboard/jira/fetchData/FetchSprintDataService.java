package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;

public interface FetchSprintDataService {
    boolean fetchSprintData(String sprintID, ProcessorJiraRestClient client, KerberosClient krb5Client);
}
