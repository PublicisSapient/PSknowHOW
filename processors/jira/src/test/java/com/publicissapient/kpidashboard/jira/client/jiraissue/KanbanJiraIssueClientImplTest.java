package com.publicissapient.kpidashboard.jira.client.jiraissue;

import com.atlassian.jira.rest.client.api.domain.*;
import com.publicissapient.kpidashboard.common.model.application.*;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.adapter.impl.OfflineAdapter;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraProcessor;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class KanbanJiraIssueClientImplTest {

    @InjectMocks
    private KanbanJiraIssueClientImpl kanbanJiraIssueClient;

    @Mock
    private KanbanJiraIssueRepository kanbanJiraRepo;

    @Mock
    private KanbanJiraIssueHistoryRepository kanbanIssueHistoryRepo;

    @Mock
    private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

    @Mock
    private JiraProcessorConfig jiraProcessorConfig;

    @Mock
    private OfflineAdapter jiraAdapter;

    @Mock
    private JiraProcessorRepository jiraProcessorRepository;

    @Mock
    private HierarchyLevelService hierarchyLevelService;

    @Mock
    private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo;

    @Test
    public void processesJiraIssuesTest() {
        KanbanJiraIssue kanbanJiraIssue = getKanbanJiraIssue();
        FieldMapping fieldMapping = new FieldMapping();
        fieldMapping.setBasicProjectConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
        String[] srs = new String[2];
        srs[0]="KnowHOW";
        srs[1]="KnowHOW1";
        fieldMapping.setJiraIssueTypeNames(srs);
        ProjectToolConfig projectToolConfig = new ProjectToolConfig();
        projectToolConfig.setBasicProjectConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
        BoardDetails board = new BoardDetails();
        board.setBoardId("1111");
        board.setBoardName("test board");
        List<BoardDetails> boardList = new ArrayList<>();
        boardList.add(board);
        projectToolConfig.setBoards(boardList);
        ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
        projectBasicConfig.setId(new ObjectId("632eb205e0fd283f9bb747ad"));
        JiraToolConfig jiraToolConfig = getJiraToolConfig(fieldMapping);
        ProjectConfFieldMapping projectConfFieldMapping = getProjectConfFieldMapping(fieldMapping, projectToolConfig,
                projectBasicConfig, jiraToolConfig);
        ArrayList<Version> alVersion = new ArrayList<>();
        Version version = null;
        alVersion.add(version);
        Set<String> stringSet = new HashSet<>();
        stringSet.add("Bug");
        stringSet.add("KnowHOW");
        Issue issue = new Issue("summary", null, "key", 121L, null,
                new IssueType(null, 11L, "Defect", true, "Description", null),
                new Status(null,null,"KnowHOW",null,null,null), "description",
                null, null,null,null,null, DateTime.now(), DateTime.now(),
                null,null,null,null, null,null,null,null,
                null,null,null,null,null,null,null,null,stringSet);
        Iterable<Issue> iterable = Arrays.asList(issue);
        SearchResult searchResult = new SearchResult(0,0,1,  iterable);
                when(kanbanJiraRepo.findTopByBasicProjectConfigId(Mockito.any())).thenReturn(kanbanJiraIssue);
        JiraProcessor jiraProcessor = new JiraProcessor();
        jiraProcessor.setId(new ObjectId("632eb205e0fd283f9bb747ad"));
        List<HierarchyLevel> hierarchyLevelList = new ArrayList<>();
        HierarchyLevel hierarchyLevel = new HierarchyLevel();
        hierarchyLevel.setHierarchyLevelId("121");
        hierarchyLevelList.add(hierarchyLevel);
        List<KanbanAccountHierarchy> kanbanAccountHierarchies = new ArrayList<>();
        KanbanAccountHierarchy kanbanAccountHierarchy = new KanbanAccountHierarchy();
        kanbanAccountHierarchy.setNodeId("121");
        kanbanAccountHierarchy.setPath("path");
        kanbanAccountHierarchies.add(kanbanAccountHierarchy);
        when(jiraProcessorConfig.getStartDate()).thenReturn("2022-09-28 10:22");
        when(jiraProcessorConfig.getPageSize()).thenReturn(2);
        when(jiraProcessorRepository.findByProcessorName(Mockito.anyString())).thenReturn(jiraProcessor);
        when(hierarchyLevelService.getFullHierarchyLevels(true)).thenReturn(hierarchyLevelList);
        when(kanbanAccountHierarchyRepo.findAll()).thenReturn(kanbanAccountHierarchies);
        when(kanbanJiraRepo.findTopByBasicProjectConfigId(Mockito.anyString())).thenReturn(getKanbanJiraIssue());
        when(kanbanJiraRepo.findByIssueIdAndBasicProjectConfigId(Mockito.anyString(), Mockito.anyString())).thenReturn(Arrays.asList(getKanbanJiraIssue()));
        Map<String, LocalDateTime> map = new HashMap<>();
        map.put("KnowHOW", LocalDateTime.now());
        doNothing().when(processorExecutionTraceLogService).save(Mockito.any());
        JiraAdapter jiraAdapter = new OfflineAdapter(jiraProcessorConfig, searchResult, alVersion);
        kanbanJiraIssueClient.processesJiraIssues(projectConfFieldMapping, jiraAdapter, true);

        verify(kanbanJiraRepo, times(1)).saveAll(Mockito.any());
        verify(kanbanIssueHistoryRepo, times(1)).saveAll(Mockito.any());
        verify(processorExecutionTraceLogService, times(1)).save(Mockito.any());
    }

    @Test
    public void purgeJiraIssuesTest() {
        ArrayList<KanbanIssueCustomHistory> kanbanIssueCustomHistories = new ArrayList<>();
        KanbanIssueCustomHistory kanbanIssueCustomHistory = new KanbanIssueCustomHistory();
        kanbanIssueCustomHistory.setBasicProjectConfigId("632eb205e0fd283f9bb747ad");
        kanbanIssueCustomHistory.setStoryID("DTS-121");
        kanbanIssueCustomHistories.add(kanbanIssueCustomHistory);
        ArrayList<KanbanJiraIssue> kanbanJiraIssue = new ArrayList<>();
        KanbanJiraIssue jiraIssue = new KanbanJiraIssue();
        jiraIssue.setBasicProjectConfigId("632eb205e0fd283f9bb747ad");
        kanbanJiraIssue.add(jiraIssue);
        when(kanbanIssueHistoryRepo.findByStoryIDAndBasicProjectConfigId(Mockito.anyString(), Mockito.anyString())).thenReturn(kanbanIssueCustomHistories);
        when(kanbanJiraRepo.findByIssueIdAndBasicProjectConfigId(Mockito.anyString(), Mockito.anyString())).thenReturn(kanbanJiraIssue);
        doNothing().when(kanbanJiraRepo).deleteAll(Mockito.any());
        doNothing().when(kanbanIssueHistoryRepo).deleteAll(Mockito.any());
        FieldMapping fieldMapping = new FieldMapping();
        fieldMapping.setBasicProjectConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
        String[] srs = new String[2];
        srs[0]="KnowHOW";
        srs[1]="KnowHOW1";
        fieldMapping.setJiraIssueTypeNames(srs);
        ProjectToolConfig projectToolConfig = new ProjectToolConfig();
        projectToolConfig.setBasicProjectConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
        ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
        projectBasicConfig.setId(new ObjectId("632eb205e0fd283f9bb747ad"));
        JiraToolConfig jiraToolConfig = getJiraToolConfig(fieldMapping);
        ProjectConfFieldMapping projectConfFieldMapping = getProjectConfFieldMapping(fieldMapping, projectToolConfig,
                projectBasicConfig, jiraToolConfig);
        Set<String> stringSet = new HashSet<>();
        stringSet.add("Bug");
        stringSet.add("KnowHOW");
        Issue issue = new Issue("summary", null, "key", 121L, null, null,
                new Status(null,null,"KnowHOW",null,null,null), "description",
                null, null,null,null,null, DateTime.now(), DateTime.now(),
                null,null,null,null, null,null,null,null,
                null,null,null,null,null,null,null,null,stringSet);
        List<Issue> issues = new ArrayList<>();
        issues.add(issue);
        kanbanJiraIssueClient.purgeJiraIssues(issues, projectConfFieldMapping);
        verify(kanbanJiraRepo, times(1)).deleteAll(kanbanJiraIssue);
        verify(kanbanIssueHistoryRepo, times(1)).deleteAll(kanbanIssueCustomHistories);
    }

    @Test
    public void setEstimateTest() {
        KanbanJiraIssue kanbanJiraIssue = getKanbanJiraIssue();
        Map<String, IssueField> map = new HashMap<>();
        IssueField issueField = null;
        map.put("111", issueField);
        FieldMapping fieldMapping = new FieldMapping();
        fieldMapping.setBasicProjectConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
        fieldMapping.setEstimationCriteria("Actual Estimation");
        String[] srs = new String[2];
        srs[0]="KnowHOW";
        srs[1]="KnowHOW1";
        fieldMapping.setJiraIssueTypeNames(srs);
        FieldMapping fieldMappingDup = new FieldMapping();
        fieldMappingDup.setBasicProjectConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
        fieldMappingDup.setEstimationCriteria("Actual Estimation");
        String[] srsDup = new String[2];
        srsDup[0]="KnowHOW";
        srsDup[1]="KnowHOW1";
        fieldMappingDup.setJiraIssueTypeNames(srs);
        fieldMappingDup.setJiraStoryPointsCustomField("customfield");
        kanbanJiraIssueClient.setEstimate(kanbanJiraIssue, map, fieldMapping, jiraProcessorConfig);
        kanbanJiraIssueClient.setEstimate(kanbanJiraIssue, map, fieldMappingDup, jiraProcessorConfig);
    }

    @Test
    public void setTestAutomatedFieldTest() {
        FieldMapping fieldMapping = new FieldMapping();
        fieldMapping.setBasicProjectConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
        String[] srs = new String[2];
        srs[0]="KnowHOW";
        srs[1]="KnowHOW1";
        fieldMapping.setJiraIssueTypeNames(srs);
        Map<String, IssueField> map = new HashMap<>();
        IssueField issueField = null;
        map.put("111", issueField);
        Set<String> stringSet = new HashSet<>();
        stringSet.add("Bug");
        stringSet.add("KnowHOW");
        Issue issue = new Issue("summary", null, "key", 121L, null, new IssueType(null, 11L, "Defect", true, "Description", null),
                new Status(null,null,"KnowHOW",null,null,null), "description",
                null, null,null,null,null, DateTime.now(), DateTime.now(),
                null,null,null,null, null,null,null,null,
                null,null,null,null,null,null,null,null,stringSet);
    }

    @Test
    public void setJiraAssigneeDetailsTest() {
        kanbanJiraIssueClient.setJiraAssigneeDetails(getKanbanJiraIssue(), null);
    }

    @Test
    public void setIssueTechStoryTypeTest() {
        FieldMapping fieldMapping = new FieldMapping();
        ArrayList<String> al = new ArrayList<>();
        al.add("Bug");
        al.add("Defect");
        fieldMapping.setJiraTechDebtValue(al);
        fieldMapping.setJiraTechDebtIdentification(JiraConstants.LABELS);
        Map<String, IssueField> map = new HashMap<>();
        IssueField issueField = null;
        map.put("111", issueField);
        Set<String> stringSet = new HashSet<>();
        stringSet.add("Bug");
        stringSet.add("KnowHOW");
        Issue issue = new Issue("summary", null, "key", 121L, null, null,
                new Status(null,null,"KnowHOW",null,null,null), "description",
                null, null,null,null,null, DateTime.now(), DateTime.now(),
                null,null,null,null, null,null,null,null,
                null,null,null,null,null,null,null,null,stringSet);
        kanbanJiraIssueClient.setIssueTechStoryType(fieldMapping, issue, getKanbanJiraIssue(), map);
    }

    @Test
    public void processJiraIssueDataTest() throws JSONException {
        Map<String, IssueField> map = new HashMap<>();
        IssueField issueField = null;
        map.put("111", issueField);
        FieldMapping fieldMapping = new FieldMapping();
        ArrayList<String> al = new ArrayList<>();
        al.add("Bug");
        al.add("Defect");
        fieldMapping.setJiraTechDebtValue(al);
        fieldMapping.setEnvImpacted("yes");
        fieldMapping.setJiraTechDebtIdentification(JiraConstants.LABELS);
        Issue issue = new Issue("summary", null, "key", 121L, null, null,
                new Status(null,null,"KnowHOW",null,null,null), "description",
                null, null,null,null,null, DateTime.now(), DateTime.now(),
                null,null,null,null, null,null,null,null,
                null,null,null,null,null,null,null,null,null);
        kanbanJiraIssueClient.processJiraIssueData(getKanbanJiraIssue(), issue, map, fieldMapping, jiraProcessorConfig);
    }

    @Test
    public void setDevicePlatform() {
        FieldMapping fieldMapping = new FieldMapping();
        ArrayList<String> al = new ArrayList<>();
        al.add("Bug");
        al.add("Defect");
        fieldMapping.setJiraTechDebtValue(al);
        fieldMapping.setJiraTechDebtIdentification(JiraConstants.LABELS);
        fieldMapping.setDevicePlatform("platform");
        Map<String, IssueField> map = new HashMap<>();
        IssueField issueField = null;
        map.put("111", issueField);
        kanbanJiraIssueClient.setDevicePlatform(fieldMapping, getKanbanJiraIssue(), map);
    }

    private static KanbanJiraIssue getKanbanJiraIssue() {
        KanbanJiraIssue kanbanJiraIssue = new KanbanJiraIssue();
        kanbanJiraIssue.setProcessorId(new ObjectId("632eb205e0fd283f9bb747ad"));
        kanbanJiraIssue.setIssueId("123");
        kanbanJiraIssue.setNumber("123");
        kanbanJiraIssue.setName("Jira");
        kanbanJiraIssue.setTypeId("123");
        kanbanJiraIssue.setTypeName("Defect");
        kanbanJiraIssue.setChangeDate("2021-07-26T10:22:12.0000000");
        kanbanJiraIssue.setSTeamID("KnowHOW");
        kanbanJiraIssue.setBasicProjectConfigId("5e9db8f1e4b0caefbfa8e0c7");
        return kanbanJiraIssue;
    }

    private static ProjectConfFieldMapping getProjectConfFieldMapping(FieldMapping fieldMapping, ProjectToolConfig projectToolConfig, ProjectBasicConfig projectBasicConfig, JiraToolConfig jiraToolConfig) {
        ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
        projectConfFieldMapping.setJira(getJiraToolConfig(fieldMapping));
        projectConfFieldMapping.setFieldMapping(fieldMapping);
        projectConfFieldMapping.setBasicProjectConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
        projectConfFieldMapping.setIssueCount(2);
        projectConfFieldMapping.setKanban(true);
        projectConfFieldMapping.setSprintCount(12);
        projectConfFieldMapping.setProjectName("KnowHOW");
        projectConfFieldMapping.setProjectToolConfig(projectToolConfig);
        projectConfFieldMapping.setJiraToolConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
        projectConfFieldMapping.setProjectBasicConfig(projectBasicConfig);
        projectConfFieldMapping.setBasicProjectConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
        return projectConfFieldMapping;
    }

    private static JiraToolConfig getJiraToolConfig(FieldMapping fieldMapping) {
        JiraToolConfig jiraToolConfig = new JiraToolConfig();
        jiraToolConfig.setBasicProjectConfigId("632eb205e0fd283f9bb747ad");
        jiraToolConfig.setProjectId("123");
        jiraToolConfig.setProjectKey("123");
        jiraToolConfig.setFieldMapping(fieldMapping);
        jiraToolConfig.setCreatedAt("2021-07-26T10:22:12.0000000");
        jiraToolConfig.setUpdatedAt("2021-07-26T10:22:12.0000000");
        jiraToolConfig.setQueryEnabled(true);
        jiraToolConfig.setBoardQuery("query");
        Connection connection = getConnectionObject();
        jiraToolConfig.setConnection(Optional.of(connection));
        BoardDetails boardDetails = new BoardDetails();
        boardDetails.setBoardName("knowHOW");
        boardDetails.setBoardId("123");
        ArrayList<BoardDetails> al = new ArrayList<>();
        al.add(boardDetails);
        jiraToolConfig.setBoards(al);
        return jiraToolConfig;
    }

    private static Connection getConnectionObject() {
        Connection connection = new Connection();
        connection.setType("Defect");
        connection.setConnectionName("DTS");
        connection.setCloudEnv(true);
        connection.setBaseUrl("url");
        connection.setUsername("KnowHOW");
        connection.setPassword("KnowHOW");
        connection.setApiEndPoint("url");
        connection.setConsumerKey("123");
        connection.setPrivateKey("123");
        connection.setApiKey("123");
        connection.setClientSecretKey("999");
        connection.setIsOAuth(true);
        connection.setClientId("111");
        connection.setTenantId("111");
        connection.setPat("pat");
        connection.setApiKeyFieldName("apiKey");
        connection.setAccessToken("accessToken");
        connection.setOffline(true);
        connection.setOfflineFilePath("offlineFilePath");
        connection.setCreatedAt("now");
        connection.setUpdatedAt("later");
        connection.setUpdatedBy("KnowHOW");
        connection.setConnPrivate(true);
        connection.setUpdatedBy("KnowHOW");
        ArrayList<String> alStrings = new ArrayList<>();
        alStrings.add("KnowHOW");
        connection.setConnectionUsers(alStrings);
        return connection;
    }
}
