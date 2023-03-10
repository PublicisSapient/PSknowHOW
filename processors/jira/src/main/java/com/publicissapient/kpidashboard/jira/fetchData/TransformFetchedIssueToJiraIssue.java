package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import org.codehaus.jettison.json.JSONException;

import java.util.List;
import java.util.Set;

public interface TransformFetchedIssueToJiraIssue {

    public List<JiraIssue> convertToJiraIssue(List<Issue> currentPagedJiraRs, ProjectConfFieldMapping projectConfig,
                                                Set<SprintDetails> setForCacheClean, boolean dataFromBoard) throws JSONException,InterruptedException;

}
