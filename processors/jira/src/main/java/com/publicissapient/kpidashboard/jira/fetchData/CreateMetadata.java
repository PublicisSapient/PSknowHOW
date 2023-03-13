package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface CreateMetadata {

    void collectMetadata(ProjectConfFieldMapping projectConfig);
}
