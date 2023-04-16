package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

public interface CreateKanbanAccountHierarchy {

    Set<KanbanAccountHierarchy> createKanbanAccountHierarchy(List<KanbanJiraIssue> jiraIssueList,
                                                                    ProjectConfFieldMapping projectConfig);

}
