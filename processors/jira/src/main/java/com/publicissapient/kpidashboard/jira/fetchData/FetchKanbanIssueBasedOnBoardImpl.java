package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class FetchKanbanIssueBasedOnBoardImpl implements FetchKanbanIssueBasedOnBoard {

    PSLogData psLogData = new PSLogData();

    @Autowired
    private KanbanJiraIssueRepository kanbanJiraRepo;

    @Autowired
    private JiraCommonService jiraCommonService;

    @Override
    public List<Issue> fetchIssueBasedOnBoard(Map.Entry<String, ProjectConfFieldMapping> entry, ProcessorJiraRestClient clientIncoming, KerberosClient krb5Client) {

        ProjectConfFieldMapping projectConfig = entry.getValue();

        boolean dataExist = false;
            dataExist = (kanbanJiraRepo
                    .findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);
            psLogData.setKanban("true");

        return jiraCommonService.fetchIssueBasedOnBoard(entry,clientIncoming, krb5Client, dataExist, new HashSet<>());

    }

}
