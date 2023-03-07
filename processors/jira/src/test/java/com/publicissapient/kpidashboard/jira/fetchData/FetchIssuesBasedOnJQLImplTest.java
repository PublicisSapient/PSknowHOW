package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
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
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.jira.adapter.helper.JiraRestClientFactory;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.client.jiraissue.JiraIssueClientFactory;
import com.publicissapient.kpidashboard.jira.client.jiraissue.KanbanJiraIssueClientImpl;
import com.publicissapient.kpidashboard.jira.client.jiraissue.ScrumJiraIssueClientImpl;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.data.*;
import com.publicissapient.kpidashboard.jira.model.JiraInfo;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.oauth.JiraOAuthClient;
import com.publicissapient.kpidashboard.jira.oauth.JiraOAuthProperties;
import io.atlassian.util.concurrent.Promise;
import org.apache.commons.beanutils.BeanUtils;
import org.codehaus.jettison.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FetchIssuesBasedOnJQLImplTest {

    @Mock
    private JiraProcessorConfig jiraProcessorConfig;

    @Mock
    private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

    @Mock
    private JiraIssueRepository jiraIssueRepository;

    @Mock
    private ToolCredentialProvider toolCredentialProvider;

    @Mock
    private AesEncryptionService aesEncryptionService;

    @Mock
    private ConnectionRepository connectionRepository;

    @Mock
    private ProjectToolConfigRepository toolRepository;

    @Mock
    private ProjectBasicConfigRepository projectConfigRepository;

    @Mock
    private JiraRestClientFactory jiraRestClientFactory;

    @Mock
    private JiraOAuthProperties jiraOAuthProperties;

    @Mock
    private JiraOAuthClient jiraOAuthClient;

    @Mock
    private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

    @Mock
    private KanbanJiraIssueRepository kanbanJiraRepo;

    @Mock
    private JiraIssueClientFactory factory;

    @Mock
    private KanbanJiraIssueClientImpl kanbanJiraIssueClient;

    @Mock
    private ScrumJiraIssueClientImpl scrumJiraIssueClient;

    @Mock
    private FieldMappingRepository fieldMappingRepository;

    @Mock
    ProcessorJiraRestClient client;

    @Mock
    SearchRestClient searchRestClient;

    @Mock
    Promise<SearchResult> promisedRs;

    @InjectMocks
    private FetchIssuesBasedOnJQLImpl fetchIssuesBasedOnJQL;


    JiraIssue jiraIssue;

    List<ProcessorExecutionTraceLog> tracelogs;

    List<ProjectToolConfig> projectToolConfigs;

    Optional<Connection> connection;

    List<ProjectBasicConfig> projectConfigsList;

    List<FieldMapping> fieldMappingList;
    private static final String PLAIN_TEXT_PASSWORD = "Purush@0699";

    ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();

    ProjectConfFieldMapping projectConfFieldMapping2 = ProjectConfFieldMapping.builder().build();


    @Before
    public void setup(){
        jiraIssue=getMockJiraIssue();
        tracelogs=getMockProcessorExecutionTraceLog();
        projectToolConfigs=getMockProjectToolConfig();
        connection=getMockConnection();
        projectConfigsList=getMockProjectConfig();
        fieldMappingList=getMockFieldMapping();
    }

    private List<ProcessorExecutionTraceLog> getMockProcessorExecutionTraceLog() {
        ProcessorExecutionTracelogDataFactory processorExecutionTraceLogDataFactory = ProcessorExecutionTracelogDataFactory
                .newInstance("/json/default/processor_execution_tracelog.json");
        return processorExecutionTraceLogDataFactory.getProcessorExecutionTracelog();
    }

    @Test
    public void fetchIssues() throws InterruptedException, JSONException {
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
        when(aesEncryptionService.decrypt(anyString(), anyString())).thenReturn(PLAIN_TEXT_PASSWORD);
        JiraInfo jiraInfo = JiraInfo.builder()
                .jiraConfigBaseUrl(projectConfFieldMapping.getJira().getConnection().get().getBaseUrl())
                .username(projectConfFieldMapping.getJira().getConnection().get().getUsername())
                .password(projectConfFieldMapping.getJira().getConnection().get().getPassword())
                .jiraConfigProxyUrl(null).jiraConfigProxyPort(null).build();

        JiraInfo jiraInfoOAuth = JiraInfo.builder().jiraConfigBaseUrl(jiraOAuthProperties.getJiraBaseURL())
                .jiraConfigAccessToken(jiraOAuthProperties.getAccessToken())
                .username(projectConfFieldMapping2.getJira().getConnection().get().getUsername())
                .password(projectConfFieldMapping2.getJira().getConnection().get().getPassword())
                .jiraConfigProxyUrl(null).jiraConfigProxyPort(null).build();
        when(fieldMappingRepository.findAll()).thenReturn(fieldMappingList);
        when(jiraProcessorConfig.getThreadPoolSize()).thenReturn(3);
        when(jiraRestClientFactory.getJiraClient(jiraInfo)).thenReturn(client);
        when(jiraRestClientFactory.getJiraClient(jiraInfoOAuth)).thenReturn(client);
        when(searchRestClient.searchJql(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anySet()))
                .thenReturn(promisedRs);
        SearchResult sr = Mockito.mock(SearchResult.class);
        when(promisedRs.claim()).thenReturn(sr);
        Map.Entry<String, ProjectConfFieldMapping> entry = createProjectConfigMap().entrySet().iterator().next();
        fetchIssuesBasedOnJQL.fetchIssues(entry);

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

}
