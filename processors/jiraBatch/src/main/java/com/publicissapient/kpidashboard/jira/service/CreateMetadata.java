package com.publicissapient.kpidashboard.jira.service;

import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface CreateMetadata {

    void collectMetadata(ProjectConfFieldMapping projectConfig, ProcessorJiraRestClient client);
}
