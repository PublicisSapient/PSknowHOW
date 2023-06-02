package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.model.jira.*;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import org.codehaus.jettison.json.JSONException;

import java.util.List;
import java.util.Set;

public interface TransformFetchedIssueToJiraIssue {

    List<JiraIssue> convertToJiraIssue(List<Issue> currentPagedJiraRs, ProjectConfFieldMapping projectConfig,
                                       boolean dataFromBoard, List<JiraIssueCustomHistory> jiraIssueHistoryToSave, Set<SprintDetails> sprintDetailsSet, Set<Assignee> assigneeSetToSave, List<JiraIssue> jiraIssuesToDelete, List<JiraIssueCustomHistory> jiraIssueHistoryToDelete, List<IssueBacklog> issueBacklogToSave, List<IssueBacklogCustomHistory> issueBacklogCustomHistoryToSave, List<IssueBacklog> issueBacklogToDelete, List<IssueBacklogCustomHistory> issueBacklogCustomHistoryToDelete) throws JSONException, InterruptedException;
}
