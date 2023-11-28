/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.repotools.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.repotools.RepoToolsClient;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolsProvider;
import com.publicissapient.kpidashboard.apis.repotools.repository.RepoToolsProviderRepository;
import com.publicissapient.kpidashboard.apis.util.RestAPIUtils;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.generic.Processor;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;

@RunWith(MockitoJUnitRunner.class)
public class RepoToolsConfigServiceImplTest {

    ProjectToolConfig projectToolConfig = new ProjectToolConfig();
    ProjectToolConfig projectToolConfig1 = new ProjectToolConfig();
    Connection connection = new Connection();
    String toolName;
    ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();

    @InjectMocks
    private RepoToolsConfigServiceImpl repoToolsConfigService;

    @Mock
    private ProjectToolConfigRepository projectToolConfigRepository;

    @Mock
    private RepoToolsProviderRepository repoToolsProviderRepository;

    @Mock
    private CustomApiConfig customApiConfig;

    @Mock
    private RestAPIUtils restAPIUtils;

    @Mock
    private ConfigHelperService configHelperService;

    @Mock
    private ProcessorRepository processorRepository;

    @Mock
    private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

    @Mock
    private AesEncryptionService aesEncryptionService;

    RepoToolsClient repoToolsClient = Mockito.mock(RepoToolsClient.class);

    @Before
    public void setUp() {

        toolName = "RepoTool";
        projectToolConfig.setId(new ObjectId("5fa0023dbb5fa781ccd5ac2c"));
        projectToolConfig.setToolName(toolName);
        projectToolConfig.setConnectionId(new ObjectId("5fb3a6412064a35b8069930a"));
        projectToolConfig.setBasicProjectConfigId(new ObjectId("5fb364612064a31c9ccd517a"));
        projectToolConfig.setBranch("test1");
        projectToolConfig.setRepositoryName("testHttpUrl");

        projectToolConfig1.setId(new ObjectId("5fa0023dbb5fa781ccd5ac2c"));
        projectToolConfig1.setToolName(toolName);
        projectToolConfig1.setConnectionId(new ObjectId("5fb3a6412064a35b8069930a"));
        projectToolConfig1.setBasicProjectConfigId(new ObjectId("5fb364612064a31c9ccd517a"));
        projectToolConfig1.setBranch("test1");
        projectToolConfig1.setRepositoryName("testRepo2");

        connection.setUsername("test1");
        connection.setAccessToken("testToken");
        connection.setEmail("testEmail");
        connection.setType(toolName);
        connection.setSshUrl("testSshUrl");
        connection.setHttpUrl("testHttpUrl.git");
        connection.setRepoToolProvider("github");

        projectBasicConfig.setId(new ObjectId("5fb364612064a31c9ccd517a"));
        projectBasicConfig.setProjectName("testProj");


    }


    @Test
    public void testConfigureRepoToolsProject() {
        when(repoToolsProviderRepository.findByToolName(anyString())).thenReturn(new RepoToolsProvider());
		when(customApiConfig.getRepoToolAPIKey()).thenReturn("repoToolAPIKey");
		when(configHelperService.getProjectConfig(projectToolConfig.getBasicProjectConfigId().toString()))
				.thenReturn(projectBasicConfig);
        when(repoToolsProviderRepository.findByToolName(anyString()))
                .thenReturn(new RepoToolsProvider());
        when(customApiConfig.getRepoToolURL()).thenReturn("http://example.com/");
        when(restAPIUtils.decryptPassword(anyString())).thenReturn("decryptedApiKey");
        repoToolsConfigService.configureRepoToolProject(projectToolConfig, connection, Collections.singletonList("branchName"));
        verify(repoToolsProviderRepository, Mockito.times(1)).findByToolName("github");

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
        repoToolsConfigService.triggerScanRepoToolProject(Arrays.asList("5fb364612064a31c9ccd517a"));
        verify(processorRepository, Mockito.times(1)).findByProcessorName("RepoTool");
    }

}