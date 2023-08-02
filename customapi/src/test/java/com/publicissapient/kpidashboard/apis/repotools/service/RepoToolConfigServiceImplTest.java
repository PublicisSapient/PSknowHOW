package com.publicissapient.kpidashboard.apis.repotools.service;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.repotools.RepoToolsClient;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolConfig;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolsProvider;
import com.publicissapient.kpidashboard.apis.repotools.repository.RepoToolsProviderRepository;
import com.publicissapient.kpidashboard.apis.projectconfig.projecttoolconfig.service.ProjectToolConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.util.RestAPIUtils;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfigDTO;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class RepoToolConfigServiceImplTest {

    ProjectToolConfig projectToolConfig = new ProjectToolConfig();

    Connection connection = new Connection();
    String testId;
    String toolName;
    String toolType;


    @InjectMocks
    RepoToolsConfigServiceImpl repoToolsConfigService;

    @Mock
    private ProjectToolConfigServiceImpl projectToolConfigService;

    @Mock
    private RepoToolsProviderRepository repoToolsProviderRepository;

    @Mock
    private ProjectBasicConfigRepository projectBasicConfigRepository;

    @Mock
    CustomApiConfig customApiConfig;

    @Mock
    private RestAPIUtils restAPIUtils;

    @Mock
    private ConnectionRepository connectionRepository;

    @Mock
    private RestTemplate restTemplate;

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

    }


    @Test
    public void testConfigureRepoToolsProject() {
        // Mock the dependencies
        RepoToolsClient repoToolsClient = new RepoToolsClient();
        when(repoToolsProviderRepository.findByToolName(anyString())).thenReturn(new RepoToolsProvider());
        when(projectToolConfigService.getProjectToolConfigs(anyString(), anyString()))
                .thenReturn(Collections.singletonList(new ProjectToolConfigDTO()));
        when(restAPIUtils.decryptPassword(anyString())).thenReturn("decryptedApiKey");
        when(projectBasicConfigRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(new ProjectBasicConfig()));
        when(customApiConfig.getRepoToolURL()).thenReturn("http://localhost:8080/api");
        when(customApiConfig.getRepoToolAPIKey()).thenReturn("repoToolAPIKey");
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("response body", HttpStatus.OK);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponse);

        repoToolsConfigService.configureRepoToolProject(projectToolConfig, connection, Collections.singletonList("branchName"));

        verify(repoToolsClient, times(1)).enrollProjectCall(any(RepoToolConfig.class), anyString(), anyString());
    }


    @Test
    public void triggerScanRepoToolProject() {
    }

    @Test
    public void createProjectCode() {
    }

    @Test
    public void updateRepoToolProjectConfiguration() {
    }
}