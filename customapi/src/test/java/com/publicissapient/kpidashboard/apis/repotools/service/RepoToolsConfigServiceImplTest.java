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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.repotools.RepoToolsClient;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiBulkMetricResponse;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolsProvider;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolsStatusResponse;
import com.publicissapient.kpidashboard.apis.repotools.repository.RepoToolsProviderRepository;
import com.publicissapient.kpidashboard.apis.util.RestAPIUtils;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.generic.Processor;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorItemRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;

@RunWith(MockitoJUnitRunner.class)
public class RepoToolsConfigServiceImplTest {

	ProjectToolConfig projectToolConfig = new ProjectToolConfig();
	ProjectToolConfig projectToolConfig1 = new ProjectToolConfig();
	ProjectToolConfig projectToolConfig2 = new ProjectToolConfig();
	Connection connection = new Connection();
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
	private ProcessorRepository<Processor> processorRepository;

	@Mock
	private ProcessorItemRepository processorItemRepository;
	private RepoToolsProvider repoToolsProvider = new RepoToolsProvider();

	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Mock
	private AesEncryptionService aesEncryptionService;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private RepoToolsClient repoToolsClient;
	@Mock
	private ConnectionRepository connectionRepository;

	@Before
	public void setUp() {

		projectToolConfig.setId(new ObjectId("5fa0023dbb5fa781ccd5ac2c"));
		projectToolConfig.setToolName(Constant.TOOL_BITBUCKET);
		projectToolConfig.setConnectionId(new ObjectId("5fb3a6412064a35b8069930a"));
		projectToolConfig.setBasicProjectConfigId(new ObjectId("5fb364612064a31c9ccd517a"));
		projectToolConfig.setBranch("test1");
		projectToolConfig.setGitFullUrl("https://kumbl@bitbucket.org/thend/cass.git");
		projectToolConfig.setRepoSlug("cass");

		projectToolConfig1.setId(new ObjectId("5fa0023dbb5fa781ccd5ac2c"));
		projectToolConfig1.setToolName(Constant.TOOL_GITHUB);
		projectToolConfig1.setConnectionId(new ObjectId("5fb3a6412064a35b8069930a"));
		projectToolConfig1.setBasicProjectConfigId(new ObjectId("5fb364612064a31c9ccd517a"));
		projectToolConfig1.setBranch("test2");
		projectToolConfig1.setGitFullUrl("testRepo2");
		projectToolConfig1.setRepositoryName("testRepo2");
		projectToolConfig1.setRepoSlug("cass");

		projectToolConfig2.setId(new ObjectId("5fa0023dbb5fa781ccd5ac2c"));
		projectToolConfig2.setToolName(Constant.TOOL_GITLAB);
		projectToolConfig2.setConnectionId(new ObjectId("5fb3a6412064a35b8069930a"));
		projectToolConfig2.setBasicProjectConfigId(new ObjectId("5fb364612064a31c9ccd517a"));
		projectToolConfig2.setBranch("test3");
		projectToolConfig2.setRepositoryName("testRepo");
		projectToolConfig2.setGitLabID(Arrays.asList("12345"));
		projectToolConfig.setRepoSlug("cass");

		connection.setUsername("test1");
		connection.setAccessToken("testToken");
		connection.setEmail("testEmail");
		connection.setType(Constant.TOOL_BITBUCKET);
		connection.setBaseUrl("testSshUrl");
		connection.setApiEndPoint("testHttpUrl.git");
		connection.setRepoToolProvider("github");

		repoToolsProvider.setToolName("github");

		projectBasicConfig.setId(new ObjectId("5fb364612064a31c9ccd517a"));
		projectBasicConfig.setProjectName("testProj");

		when(customApiConfig.getRepoToolAPIKey()).thenReturn("repoToolAPIKey");
		when(customApiConfig.getRepoToolURL()).thenReturn("http://example.com/");
		when(configHelperService.getProjectConfig(projectToolConfig.getBasicProjectConfigId().toString()))
				.thenReturn(projectBasicConfig);
	}

	@Test
	public void testConfigureRepoToolsProject() {

		repoToolsConfigService.configureRepoToolProject(projectToolConfig, connection,
				Collections.singletonList("branchName"));
		when(repoToolsClient.enrollProjectCall(any(), anyString(), anyString())).thenReturn(HttpStatus.OK.value());
		ServiceResponse service = repoToolsConfigService.configureRepoToolProject(projectToolConfig, connection,
				Collections.singletonList("branchName"));

		// Assert
		assertEquals(true, service.getSuccess());
	}

	@Test
	public void testTriggerScanRepoToolProject() {
		Processor processor = new Processor();
		processor.setProcessorName("GitHub");
		when(processorRepository.findByProcessorName("GitHub")).thenReturn(processor);
		when(projectToolConfigRepository.findByToolNameAndBasicProjectConfigId("GitHub",
				new ObjectId("5fb364612064a31c9ccd517a"))).thenReturn(Arrays.asList(projectToolConfig));
		when(customApiConfig.getRepoToolURL()).thenReturn("http://example.com/");
		when(repoToolsClient.triggerScanCall(anyString(), anyString(), anyString())).thenReturn(HttpStatus.OK.value());

		int result = repoToolsConfigService.triggerScanRepoToolProject("GitHub", "5fb364612064a31c9ccd517a");
		verify(processorRepository, Mockito.times(1)).findByProcessorName("GitHub");
		assertEquals(HttpStatus.OK.value(), result);
	}

	@Test
	public void testUpdateRepoToolProjectConfiguration() {
		List<ProjectToolConfig> projectToolConfigList = new ArrayList<>();
		projectToolConfigList.add(projectToolConfig);
		projectToolConfigList.add(projectToolConfig1);
		assertEquals(true, repoToolsConfigService.updateRepoToolProjectConfiguration(projectToolConfigList,
				projectToolConfig, "5fb364612064a31c9ccd517a"));
	}

	@Test
	public void testUpdateRepoToolProjectConfiguration2() {
		List<ProjectToolConfig> projectToolConfigList = new ArrayList<>();
		projectToolConfigList.add(projectToolConfig);
		when(configHelperService.getProjectConfig("5fb364612064a31c9ccd517a")).thenReturn(projectBasicConfig);
		when(customApiConfig.getRepoToolDeleteProjectUrl()).thenReturn("delete/project");
		repoToolsConfigService.updateRepoToolProjectConfiguration(projectToolConfigList, projectToolConfig,
				"5fb364612064a31c9ccd517a");
		verify(repoToolsClient, times(1)).deleteProject(anyString(), anyString());
	}

	@Test
	public void testUpdateRepoToolProjectConfiguration3() {
		List<ProjectToolConfig> projectToolConfigList = new ArrayList<>();
		projectToolConfigList.add(projectToolConfig);
		projectToolConfigList.add(projectToolConfig);
		projectToolConfigList.add(projectToolConfig);
		assertEquals(true, repoToolsConfigService.updateRepoToolProjectConfiguration(projectToolConfigList,
				projectToolConfig, "5fb364612064a31c9ccd517a"));
	}

	@Test
	public void testGetRepoToolKpiMetrics() {
		List<String> projectCode = Arrays.asList("code1", "code2", "code3");
		RepoToolKpiBulkMetricResponse repoToolKpiBulkMetricResponse = new RepoToolKpiBulkMetricResponse();
		repoToolsConfigService.getRepoToolKpiMetrics(projectCode, "repoToolKpi", "startDate", "endDate", "frequency");
		verify(repoToolsClient, times(1)).kpiMetricCall(anyString(), anyString(), any());
	}

	@Test
	public void testSaveRepoToolProjectTraceLog() {
		RepoToolsStatusResponse repoToolsStatusResponse = new RepoToolsStatusResponse();
		repoToolsStatusResponse.setProject("example_project_123");
		repoToolsStatusResponse.setStatus("SUCCESS");
		repoToolsStatusResponse.setRepositoryProvider("github");
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setLastEnableAssigneeToggleState(false);
		repoToolsConfigService.saveRepoToolProjectTraceLog(repoToolsStatusResponse);

		verify(processorExecutionTraceLogService, times(1)).save(any(ProcessorExecutionTraceLog.class));
	}

	@Test
	public void testUpdateConnection() {
		when(repoToolsProviderRepository.findByToolName("github")).thenReturn(repoToolsProvider);
		assertEquals(
				HttpStatus.OK.value(), repoToolsConfigService.updateRepoToolConnection(connection));
	}
}
