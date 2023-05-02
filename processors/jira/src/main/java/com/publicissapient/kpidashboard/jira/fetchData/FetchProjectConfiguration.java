package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.common.executor.ProcessorJobExecutor;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;

import com.publicissapient.kpidashboard.jira.model.JiraProcessor;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;


import java.util.List;
import java.util.Map;

public interface FetchProjectConfiguration {
    Map<String, ProjectConfFieldMapping> fetchConfiguration();

}
