package com.publicissapient.kpidashboard.jira.config;

import java.util.Map;

import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface FetchProjectConfiguration {
	Map<String, ProjectConfFieldMapping> fetchConfiguration(Boolean isKanban);

}
