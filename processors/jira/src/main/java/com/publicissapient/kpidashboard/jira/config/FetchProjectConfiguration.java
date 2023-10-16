package com.publicissapient.kpidashboard.jira.config;

import java.util.List;

import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface FetchProjectConfiguration {
	ProjectConfFieldMapping fetchConfiguration(String projectId);

	List<String> fetchBasicProjConfId(String toolName, boolean queryEnabled, boolean isKanban);

	ProjectConfFieldMapping fetchConfigurationBasedOnSprintId(String sprintId);
}
