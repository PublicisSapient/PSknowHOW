package com.publicissapient.kpidashboard.jira.service;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface FetchKanbanReleaseData {
    ProjectRelease processReleaseInfo(ProjectConfFieldMapping projectConfig, KerberosClient krb5Client);
}
