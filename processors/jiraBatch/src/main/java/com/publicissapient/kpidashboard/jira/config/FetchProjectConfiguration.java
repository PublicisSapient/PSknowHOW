package com.publicissapient.kpidashboard.jira.config;

import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface FetchProjectConfiguration {
	Map<String, List<ProjectConfFieldMapping>> fetchConfiguration(Boolean isKanban);

}
