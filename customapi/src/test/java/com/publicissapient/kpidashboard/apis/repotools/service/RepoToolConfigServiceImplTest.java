package com.publicissapient.kpidashboard.apis.repotools.service;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.repotools.RepoToolsClient;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolConfig;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolsProvider;
import com.publicissapient.kpidashboard.apis.repotools.repository.RepoToolsProviderRepository;
import com.publicissapient.kpidashboard.apis.projectconfig.projecttoolconfig.service.ProjectToolConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.util.RestAPIUtils;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfigDTO;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.generic.Processor;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorItemRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.runner.RunWith;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class RepoToolConfigServiceImplTest {

    ProjectToolConfig projectToolConfig = new ProjectToolConfig();

    Connection connection = new Connection();
    String testId;
    String toolName;
    String toolType;

    ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();

    @Mock
    RestTemplate restTemplate;


    @InjectMocks
    @Spy
    private RepoToolsConfigServiceImpl repoToolsConfigService;

    @Mock
    private ProjectToolConfigServiceImpl projectToolConfigService;

    @Mock
    private ProjectToolConfigRepository projectToolConfigRepository;

    @Mock
    private RepoToolsProviderRepository repoToolsProviderRepository;

    @Mock
    private ProjectBasicConfigRepository projectBasicConfigRepository;

    @Mock
    private CustomApiConfig customApiConfig;

    @Mock
    private RestAPIUtils restAPIUtils;

    @Mock
    private ConfigHelperService configHelperService;

    @Mock
    private ProcessorItemRepository processorItemRepository;

    @Mock
    private ProcessorRepository processorRepository;

    @Mock
    private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

    @Mock
    private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;
    @Mock
    private RepoToolsClient repoToolsClient;

    @Before
    public void setUp() {

        toolName = "Test1";
        projectToolConfig.setId(new ObjectId("5fa0023dbb5fa781ccd5ac2c"));
        projectToolConfig.setToolName(toolName);
        projectToolConfig.setConnectionId(new ObjectId("5fb3a6412064a35b8069930a"));
        projectToolConfig.setBasicProjectConfigId(new ObjectId("5fb364612064a31c9ccd517a"));
        projectToolConfig.setBranch("test1");
        projectToolConfig.setJobName("testing1");

        toolType = "Repo_Tools";
        connection.setUsername("test1");
        connection.setAccessToken("testToken");
        connection.setEmail("testEmail");
        connection.setType(toolType);
        connection.setSshUrl("testSshUrl");
        connection.setHttpUrl("testHttpUrl");
        connection.setRepoToolProvider("github");

        projectBasicConfig.setId(new ObjectId("5fb364612064a31c9ccd517a"));
        projectBasicConfig.setProjectName("testProj");

        doReturn(mock(RepoToolsClient.class)).when(repoToolsConfigService).createRepoToolsClient();

    }


    @Test
    public void testConfigureRepoToolsProject() {
        RepoToolsClient repoToolsClient = new RepoToolsClient();
        when(repoToolsProviderRepository.findByToolName(anyString())).thenReturn(new RepoToolsProvider());
		when(customApiConfig.getRepoToolAPIKey()).thenReturn("repoToolAPIKey");
		when(configHelperService.getProjectConfig(projectToolConfig.getBasicProjectConfigId().toString()))
				.thenReturn(projectBasicConfig);
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("response body", HttpStatus.OK);
        when(restTemplate.exchange(eq(URI.create("http://example.com//beta/repositories/")), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponse);

        when(repoToolsProviderRepository.findByToolName(anyString()))
                .thenReturn(new RepoToolsProvider());

        when(customApiConfig.getRepoToolURL()).thenReturn("http://example.com/");
        when(restAPIUtils.decryptPassword(anyString())).thenReturn("decryptedApiKey");

        Whitebox.setInternalState(repoToolsConfigService, "repoToolsClient", repoToolsClient);

        int httpStatus = repoToolsConfigService.configureRepoToolProject(projectToolConfig, connection, Collections.singletonList("branchName"));

        Assert.assertEquals(HttpStatus.NOT_FOUND.value(), httpStatus);

    }


    @Test
    public void testTriggerScanRepoToolProject() {
        when(processorRepository.findByProcessorName(CommonConstant.REPO_TOOLS)).thenReturn(new Processor());
        when(projectToolConfigRepository.findByToolNameAndBasicProjectConfigId(CommonConstant.REPO_TOOLS,
                new ObjectId("5fb364612064a31c9ccd517a"))).thenReturn(Arrays.asList(projectToolConfig));
        when(processorExecutionTraceLogRepository
                .findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.REPO_TOOLS, "5fb364612064a31c9ccd517a")).thenReturn(Optional.of(new ProcessorExecutionTraceLog()));
        when(customApiConfig.getRepoToolURL()).thenReturn("http://example.com/");
        when(configHelperService.getProjectConfig(projectToolConfig.getBasicProjectConfigId().toString()))
                .thenReturn(projectBasicConfig);

        Whitebox.setInternalState(repoToolsConfigService, "repoToolsClient", repoToolsClient);

        int httpStatus = repoToolsConfigService.triggerScanRepoToolProject(Arrays.asList("5fb364612064a31c9ccd517a"));

        Assert.assertEquals(0, httpStatus);
    }

    @Test
    public void createProjectCode() {
    }

    @Test
    public void updateRepoToolProjectConfiguration() {
    }
}