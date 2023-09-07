package com.publicissapient.kpidashboard.jira.service;

import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;

public interface CreateJiraIssueReleaseStatus {
    void processAndSaveProjectStatusCategory(ProcessorJiraRestClient client, String basicProjectConfigId);
}
