package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import org.springframework.stereotype.Service;

public interface CreateMetadata {

    void collectMetadata(ProjectConfFieldMapping projectConfig, ProcessorJiraRestClient client);
}
