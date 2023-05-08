package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import org.codehaus.jettison.json.JSONException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

public interface TransformFetchedIssueToKanbanJiraIssue {

    List<KanbanJiraIssue> convertToJiraIssue(List<Issue> currentPagedJiraRs,
                                             ProjectConfFieldMapping projectConfig, boolean dataFromBoard, List<KanbanIssueCustomHistory> kanbanIssueHistoryToSave, Set<Assignee> assigneeSetToSave)// NOPMD
        // //NOSONAR
            throws JSONException;
}
