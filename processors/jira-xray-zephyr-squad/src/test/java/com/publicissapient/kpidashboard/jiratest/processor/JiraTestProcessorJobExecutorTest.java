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

package com.publicissapient.kpidashboard.jiratest.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.jiratest.config.JiraTestProcessorConfig;
import com.publicissapient.kpidashboard.jiratest.model.JiraTestProcessor;
import com.publicissapient.kpidashboard.jiratest.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jiratest.processor.service.JiraTestService;
import com.publicissapient.kpidashboard.jiratest.repository.JiraTestProcessorRepository;

@ExtendWith(SpringExtension.class)
public class JiraTestProcessorJobExecutorTest {

	private final ObjectId PROCESSOR_ID = new ObjectId("5e16dc92f1aab3fbb1b198f3");
	@Mock
	RestTemplate restTemplate;
	@InjectMocks
	JiraTestProcessorJobExecutor jiraTestProcessorJobExecutor;
	JiraTestProcessor jiraTestProcessor = new JiraTestProcessor();
	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	private JiraTestProcessorRepository jiraTestProcessorRepository;
	@Mock
	private TaskScheduler taskScheduler;
	@Mock
	private JiraTestProcessorConfig jiraProcessorConfig;
	@Mock
	private ProcessorToolConnectionService processorToolConnectionService;
	@Mock
	private JiraTestService jiraTestService;
	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private ProjectConfFieldMapping projectConfFieldMapping;
	private ProjectBasicConfig projectBasicConfig;
	private List<ProcessorToolConnection> toolList = new ArrayList<>();
	private ProcessorToolConnection toolInfo;

	@BeforeEach
	public void setUp() throws Exception {

		toolInfo = new ProcessorToolConnection();
		toolInfo.setBasicProjectConfigId(new ObjectId("625fd013572701449a44b3de"));
		toolInfo.setToolName(ProcessorConstants.JIRA_TEST);
		toolInfo.setUrl("https://abc.com/jira");
		toolInfo.setApiEndPoint("/rest/atm/1.0");
		toolInfo.setUsername("test");
		toolInfo.setPassword("password");
		toolInfo.setProjectKey("testProjectKey");
		toolInfo.setConnectionId(new ObjectId("625d0d9d10ce157f45918b5c"));
		toolInfo.setCloudEnv(false);
		toolList.add(toolInfo);
		jiraTestProcessorJobExecutor = new JiraTestProcessorJobExecutor(taskScheduler);
		this.jiraTestProcessor = jiraTestProcessorJobExecutor.getProcessor();
		MockitoAnnotations.openMocks(this);
		projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("604092b52b424d5e90d39342"));
		projectBasicConfig.setIsKanban(true);
		projectBasicConfig.setProjectName("JIRA TEST Scrum");
		projectBasicConfig.setUpdatedAt("updatedAt");
		projectConfigList.add(projectBasicConfig);

		projectConfFieldMapping = new ProjectConfFieldMapping();
		projectConfFieldMapping.setProjectKey("XYZ");
		projectConfFieldMapping.setProjectName("JIRA TEST Scrum");
		projectConfFieldMapping.setBasicProjectConfigId(new ObjectId("625fd013572701449a44b3de"));
		projectConfFieldMapping.setKanban(false);
		projectConfFieldMapping.setProcessorToolConnection(toolInfo);

		Mockito.when(jiraProcessorConfig.getCustomApiBaseUrl()).thenReturn("http://localhost:8080/");
		PowerMockito.whenNew(RestTemplate.class).withNoArguments().thenReturn(restTemplate);
	}

	@Test
	public void getCronTest() {
		Mockito.when(jiraProcessorConfig.getCron()).thenReturn("* 0 0 0 0 0");
		Assert.assertEquals("* 0 0 0 0 0", jiraTestProcessorJobExecutor.getCron());
	}

	@Test
	public void getProcessorTest() {

		Assert.assertNotNull(jiraTestProcessorJobExecutor.getProcessor());
	}

	@Test
	public void getProcessorRepositoryTest() {
		Assert.assertEquals(jiraTestProcessorRepository, jiraTestProcessorJobExecutor.getProcessorRepository());
	}

	@Test
	public void execute() {
		JiraTestProcessor jiraProcessor = new JiraTestProcessor();
		jiraProcessor.setId(PROCESSOR_ID);
		Mockito.when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
		jiraTestProcessorJobExecutor.setProjectsBasicConfigIds(
				Arrays.asList("604092b52b424d5e90d39342", "604092b52b424d5e90d39343", "604092b52b424d5e90d39344"));
		when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any())).thenReturn(toolList);
		when(jiraTestService.processesJiraIssues(any())).thenReturn(10);
		when(projectConfigRepository.findById(any())).thenReturn(projectConfigList.stream().findFirst());
		Assert.assertEquals(true, jiraTestProcessorJobExecutor.execute(jiraProcessor));
		jiraTestProcessorJobExecutor.setProjectsBasicConfigIds(null);
	}

	@Test
	public void cacheRestClient() throws Exception { // NOSONAR
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = new ResponseEntity<>("Success", HttpStatus.OK);
		Mockito.when(restTemplate.exchange(ArgumentMatchers.any(URI.class), ArgumentMatchers.eq(HttpMethod.GET),
				ArgumentMatchers.eq(entity), ArgumentMatchers.eq(String.class))).thenReturn(response);
		jiraTestProcessorJobExecutor.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
				CommonConstant.TESTING_KPI_CACHE);
	}

	@Test
	public void cacheRestClientResponseNull() throws Exception { // NOSONAR
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		Mockito.when(restTemplate.exchange(new URI("http://localhost:8080/api/cache/clearCache/testingKpiCache"),
				HttpMethod.GET, entity, String.class)).thenReturn(null);
		jiraTestProcessorJobExecutor.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
				CommonConstant.TESTING_KPI_CACHE);
	}
}