package com.publicissapient.kpidashboard.jira.processor;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import java.util.Set;

public interface SprintDataProcessor {
    Set<SprintDetails> processSprintData(Issue issue, ProjectConfFieldMapping projectConfig, String boardId);
}
