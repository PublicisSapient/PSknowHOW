package com.publicissapient.kpidashboard.jira.config;

import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface FetchProjectConfiguration {
	ProjectConfFieldMapping fetchConfiguration(String projectId);

}
