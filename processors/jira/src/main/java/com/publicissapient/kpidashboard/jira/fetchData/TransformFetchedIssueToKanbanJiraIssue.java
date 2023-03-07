package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import org.codehaus.jettison.json.JSONException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TransformFetchedIssueToKanbanJiraIssue {

    public List<KanbanJiraIssue> convertToJiraIssue(List<Issue> currentPagedJiraRs,
                                                     ProjectConfFieldMapping projectConfig) throws JSONException;

}
