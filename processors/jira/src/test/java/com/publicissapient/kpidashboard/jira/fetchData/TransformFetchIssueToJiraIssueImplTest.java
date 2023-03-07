package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.StatusCategory;
import com.atlassian.jira.rest.client.api.domain.*;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.jira.adapter.helper.JiraRestClientFactory;
import com.publicissapient.kpidashboard.jira.client.sprint.SprintClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import com.publicissapient.kpidashboard.jira.util.AdditionalFilterHelper;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


@RunWith(MockitoJUnitRunner.class)
public class TransformFetchIssueToJiraIssueImplTest {

    @Mock
    private JiraIssueRepository jiraIssueRepository;

    @Mock
    private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

    @Mock
    private JiraProcessorRepository jiraProcessorRepository;

    @Mock
    private AccountHierarchyRepository accountHierarchyRepository;

    @Mock
    private JiraProcessorConfig jiraProcessorConfig;

    @Mock
    private SprintClient sprintClient;

    @Mock
    private JiraRestClientFactory jiraRestClientFactory;

    @Mock
    private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

    @Mock
    private HierarchyLevelService hierarchyLevelService;

    @Mock
    private AdditionalFilterHelper additionalFilterHelper;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private KanbanJiraIssueRepository kanbanJiraRepo;

    @Mock
    private ToolCredentialProvider toolCredentialProvider;

    @Mock
    private AesEncryptionService aesEncryptionService;

    @Mock
    private JiraCommon jiraCommon;

    @Mock
    private CreateAccountHierarchy accountHierarchy;

    @InjectMocks
    TransformFetchedIssueToJiraIssueImpl transformFetchedIssueToJiraIssue;

    protected static final String QUERYDATEFORMAT = "yyyy-MM-dd HH:mm";

    private static final String CONTENTS = "contents";
    private static final String COMPLETED_ISSUES = "completedIssues";
    private static final String PUNTED_ISSUES = "puntedIssues";
    private static final String COMPLETED_ISSUES_ANOTHER_SPRINT = "issuesCompletedInAnotherSprint";
    private static final String ADDED_ISSUES = "issueKeysAddedDuringSprint";
    private static final String NOT_COMPLETED_ISSUES = "issuesNotCompletedInCurrentSprint";
    private static final String KEY = "key";
    private static final String ENTITY_DATA = "entityData";
    private static final String PRIORITYID = "priorityId";
    private static final String STATUSID = "statusId";

    private static final String TYPEID = "typeId";

    @Test
    public void convertToJiraIssue() throws URISyntaxException {

    }


}
