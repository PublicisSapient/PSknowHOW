package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.*;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jira.adapter.atlassianbespoke.client.CustomAsynchronousIssueRestClient;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import io.atlassian.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
public class FetchIssueBasedOnBoardImpl implements FetchIssueBasedOnBoard {

    private static final String MSG_JIRA_CLIENT_SETUP_FAILED = "Jira client setup failed. No results obtained. Check your jira setup.";
    private static final String ERROR_MSG_401 = "Error 401 connecting to JIRA server, your credentials are probably wrong. Note: Ensure you are using JIRA user name not your email address.";
    private static final String ERROR_MSG_NO_RESULT_WAS_AVAILABLE = "No result was available from Jira unexpectedly - defaulting to blank response. The reason for this fault is the following : {}";
    private static final String NO_RESULT_QUERY = "No result available for query: {}";
    protected static final String QUERYDATEFORMAT = "yyyy-MM-dd HH:mm";

    @Autowired
    private JiraIssueRepository jiraIssueRepository;

    PSLogData psLogData = new PSLogData();

    @Autowired
    private JiraCommonService jiraCommonService;

    @Override
    public List<Issue> fetchIssueBasedOnBoard(Map.Entry<String, ProjectConfFieldMapping> entry, ProcessorJiraRestClient clientIncoming, KerberosClient krb5Client) {

        ProjectConfFieldMapping projectConfig = entry.getValue();

        boolean dataExist = false;
            dataExist = (jiraIssueRepository
                    .findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);
            psLogData.setKanban("false");

        return jiraCommonService.fetchIssueBasedOnBoard(entry,clientIncoming, krb5Client, dataExist);

    }
}