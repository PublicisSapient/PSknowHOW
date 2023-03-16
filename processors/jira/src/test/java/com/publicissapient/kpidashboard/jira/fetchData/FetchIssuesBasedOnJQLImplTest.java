package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.StatusCategory;
import com.atlassian.jira.rest.client.api.domain.*;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.impl.ProcessorAsynchJiraRestClient;
import com.publicissapient.kpidashboard.jira.client.jiraissue.JiraIssueClientFactory;
import com.publicissapient.kpidashboard.jira.client.jiraissue.ScrumJiraIssueClientImpl;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.data.*;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import io.atlassian.util.concurrent.Promise;
import org.apache.commons.beanutils.BeanUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FetchIssuesBasedOnJQLImplTest {

    @Mock
    private JiraProcessorConfig jiraProcessorConfig;

    @Mock
    private JiraIssueRepository jiraIssueRepository;

    @Mock
    private ConnectionRepository connectionRepository;

    @Mock
    private ProjectToolConfigRepository toolRepository;

    @Mock
    private ProjectBasicConfigRepository projectConfigRepository;

    @Mock
    private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

    @Mock
    private JiraIssueClientFactory factory;

    @Mock
    private ScrumJiraIssueClientImpl scrumJiraIssueClient;

    @Mock
    private FieldMappingRepository fieldMappingRepository;

    @Mock
    SearchRestClient searchRestClient;

    @Mock
    Promise<SearchResult> promisedRs;

    @Mock
    JiraCommonService jiraCommonService;

    @Mock
    private ProcessorAsynchJiraRestClient restClient;

    @InjectMocks
    private FetchIssuesBasedOnJQLImpl fetchIssuesBasedOnJQL;


    JiraIssue jiraIssue;

    List<ProcessorExecutionTraceLog> tracelogs;

    List<ProjectToolConfig> projectToolConfigs;

    Optional<Connection> connection;

    List<ProjectBasicConfig> projectConfigsList;

    List<FieldMapping> fieldMappingList;

    List<Issue> issues=new ArrayList<>();

    private static final String PLAIN_TEXT_PASSWORD = "Test@123";

    SearchResult searchResult;


    @Before
    public void setup() throws URISyntaxException {
        jiraIssue=getMockJiraIssue();
        tracelogs=getMockProcessorExecutionTraceLog();
        projectToolConfigs=getMockProjectToolConfig();
        connection=getMockConnection();
        projectConfigsList=getMockProjectConfig();
        fieldMappingList=getMockFieldMapping();
        createIssue();
    }

    private List<ProcessorExecutionTraceLog> getMockProcessorExecutionTraceLog() {
        ProcessorExecutionTracelogDataFactory processorExecutionTraceLogDataFactory = ProcessorExecutionTracelogDataFactory
                .newInstance("/json/default/processor_execution_tracelog.json");
        return processorExecutionTraceLogDataFactory.getProcessorExecutionTracelog();
    }

    @Test
    public void fetchIssues() throws InterruptedException, JSONException, IOException, Exception {
        when(factory.getJiraIssueDataClient(any())).thenReturn(scrumJiraIssueClient);
        when(jiraIssueRepository.findTopByBasicProjectConfigId(any())).thenReturn(jiraIssue);
        when(processorExecutionTraceLogRepository.findAll()).thenReturn(tracelogs);
        when(toolRepository.findByToolNameAndBasicProjectConfigId(any(),any())).thenReturn(projectToolConfigs);
        when(connectionRepository.findById(any())).thenReturn(connection);
        when(projectConfigRepository.findAll()).thenReturn(projectConfigsList);
        when(jiraProcessorConfig.getPageSize()).thenReturn(30);
        when(jiraProcessorConfig.getMinsToReduce()).thenReturn(30L);
        when(jiraProcessorConfig.getStartDate()).thenReturn("2019-01-07 00:00");
        when(jiraProcessorConfig.getJiraCloudGetUserApi()).thenReturn("user/search?query=");
        when(jiraProcessorConfig.getJiraServerGetUserApi()).thenReturn("user/search?username=");
        when(jiraProcessorConfig.getAesEncryptionKey()).thenReturn("708C150A5363290AAE3F579BF3746AD5");
        when(jiraCommonService.decryptJiraPassword(any())).thenReturn(PLAIN_TEXT_PASSWORD);
        when(fieldMappingRepository.findAll()).thenReturn(fieldMappingList);
        when(jiraProcessorConfig.getThreadPoolSize()).thenReturn(3);
        when(searchRestClient.searchJql(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anySet()))
                .thenReturn(promisedRs);
        when(promisedRs.claim()).thenReturn(searchResult);
        Map.Entry<String, ProjectConfFieldMapping> entry = createProjectConfigMap().entrySet().iterator().next();
        fetchIssuesBasedOnJQL.fetchIssues(entry, restClient);

    }

    private JiraIssue getMockJiraIssue() {
        JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory
                .newInstance("/json/default/jira_issues.json");
        return jiraIssueDataFactory.findTopByBasicProjectConfigId("63c04dc7b7617e260763ca4e");
    }

    private  List<ProjectToolConfig> getMockProjectToolConfig() {
        ToolConfigDataFactory projectToolConfigDataFactory = ToolConfigDataFactory
                .newInstance("/json/default/project_tool_configs.json");
        return projectToolConfigDataFactory.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA,"63c04dc7b7617e260763ca4e");
    }

    private Optional<Connection> getMockConnection() {
        ConnectionsDataFactory connectionDataFactory = ConnectionsDataFactory
                .newInstance("/json/default/connections.json");
        return connectionDataFactory.findConnectionById("63f733a07af7ed784f088cd5");
    }

    private List<ProjectBasicConfig> getMockProjectConfig() {
        ProjectBasicConfigDataFactory projectConfigDataFactory = ProjectBasicConfigDataFactory
                .newInstance("/json/default/project_basic_configs.json");
        return projectConfigDataFactory.getProjectBasicConfigs();
    }

    private  List<FieldMapping> getMockFieldMapping() {
        FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
                .newInstance("/json/default/field_mapping.json");
        return fieldMappingDataFactory.getFieldMappings();
    }

    private Map<String, ProjectConfFieldMapping> createProjectConfigMap(){
        Map<String, ProjectConfFieldMapping> projectConfigMap = new HashMap<>();
        ProjectConfFieldMapping projectConfFieldMapping=ProjectConfFieldMapping.builder().build();
        ProjectBasicConfig projectConfig=projectConfigsList.get(2);
        try {
            BeanUtils.copyProperties(projectConfFieldMapping, projectConfig);
        } catch (IllegalAccessException | InvocationTargetException e) {

        }
        projectConfFieldMapping.setProjectBasicConfig(projectConfig);
        projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
        projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
        projectConfFieldMapping.setJira(getJiraToolConfig());
        projectConfFieldMapping.setJiraToolConfigId(projectToolConfigs.get(0).getId());
        projectConfFieldMapping.setFieldMapping(fieldMappingList.get(1));
        projectConfigMap.put(projectConfig.getProjectName(), projectConfFieldMapping);
        return projectConfigMap;
    }

    private JiraToolConfig getJiraToolConfig() {
        JiraToolConfig toolObj = new JiraToolConfig();
        try {
            BeanUtils.copyProperties(toolObj, projectToolConfigs.get(0));
        } catch (IllegalAccessException | InvocationTargetException e){

        }
        toolObj.setConnection(connection);
        return toolObj;
    }

    private void createIssue() throws URISyntaxException {
        BasicProject basicProj = new BasicProject(new URI("self"), "proj1", 1l, "project1");
        IssueType issueType1 = new IssueType(new URI("self"), 1l, "Story", false, "desc", new URI("iconURI"));
        IssueType issueType2 = new IssueType(new URI("self"), 2l, "Defect", false, "desc", new URI("iconURI"));
        Status status1 = new Status(new URI("self"), 1l, "Ready for Sprint Planning", "desc", new URI("iconURI"),
                new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
        BasicPriority basicPriority = new BasicPriority(new URI("self"), 1l, "priority1");
        Resolution resolution = new Resolution(new URI("self"), 1l, "resolution", "resolution");
        Map<String, URI> avatarMap = new HashMap<>();
        avatarMap.put("48x48", new URI("value"));
        User user1 = new User(new URI("self"), "user1", "user1", "userAccount", "user1@xyz.com", true, null, avatarMap,
                null);
        Map<String, String> map = new HashMap<>();
        map.put("customfield_12121", "Client Testing (UAT)");
        map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
        map.put("value", "Component");
        map.put("id", "20810");
        JSONObject value = new JSONObject(map);
        IssueField issueField = new IssueField("20810", "Component", null, value);
        List<IssueField> issueFields = Arrays.asList(issueField);
        Comment comment = new Comment(new URI("self"), "body", null, null, DateTime.now(), DateTime.now(),
                new Visibility(Visibility.Type.ROLE, "abc"), 1l);
        List<Comment> comments = Arrays.asList(comment);
        BasicVotes basicVotes = new BasicVotes(new URI("self"), 1, true);
        BasicUser basicUser = new BasicUser(new URI("self"), "basicuser", "basicuser", "accountId");
        Worklog worklog = new Worklog(new URI("self"), new URI("self"), basicUser, basicUser, null, DateTime.now(),
                DateTime.now(), DateTime.now(), 60, null);
        List<Worklog> workLogs = Arrays.asList(worklog);
        ChangelogItem changelogItem = new ChangelogItem(FieldType.JIRA, "field1", "from", "fromString", "to",
                "toString");
        ChangelogGroup changelogGroup = new ChangelogGroup(basicUser, DateTime.now(), Arrays.asList(changelogItem));

        Issue issue = new Issue("summary1", new URI("self"), "key1", 1l, basicProj, issueType1, status1, "story",
                basicPriority, resolution, new ArrayList<>(), user1, user1, DateTime.now(), DateTime.now(),
                DateTime.now(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, issueFields, comments,
                null, createIssueLinkData(), basicVotes, workLogs, null, Arrays.asList("expandos"), null,
                Arrays.asList(changelogGroup), null, new HashSet<>(Arrays.asList("label1")));
        Issue issue1 = new Issue("summary1", new URI("self"), "key1", 1l, basicProj, issueType2, status1, "Defect",
                basicPriority, resolution, new ArrayList<>(), user1, user1, DateTime.now(), DateTime.now(),
                DateTime.now(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, issueFields, comments,
                null, createIssueLinkData(), basicVotes, workLogs, null, Arrays.asList("expandos"), null,
                Arrays.asList(changelogGroup), null, new HashSet<>(Arrays.asList("label1")));
        issues.add(issue);
        issues.add(issue1);

        searchResult = new SearchResult(0, 10, 2, issues);

    }

    private List<IssueLink> createIssueLinkData() throws URISyntaxException {
        List<IssueLink> issueLinkList = new ArrayList<>();
        URI uri = new URI("https://testDomain.com/jira/rest/api/2/issue/12344");
        IssueLinkType linkType = new IssueLinkType("Blocks", "blocks", IssueLinkType.Direction.OUTBOUND);
        IssueLink issueLink = new IssueLink("IssueKey", uri, linkType);
        issueLinkList.add(issueLink);

        return issueLinkList;
    }

}
