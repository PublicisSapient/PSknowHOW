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

package com.publicissapient.kpidashboard.azure.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.azure.adapter.helper.AzureRestClientFactory;
import com.publicissapient.kpidashboard.azure.client.azureissue.AzureIssueClient;
import com.publicissapient.kpidashboard.azure.client.azureissue.AzureIssueClientFactory;
import com.publicissapient.kpidashboard.azure.client.azureissue.ScrumAzureIssueClientImpl;
import com.publicissapient.kpidashboard.azure.client.sprint.SprintClientImpl;
import com.publicissapient.kpidashboard.azure.config.AzureProcessorConfig;
import com.publicissapient.kpidashboard.azure.model.AzureProcessor;
import com.publicissapient.kpidashboard.azure.model.AzureServer;
import com.publicissapient.kpidashboard.azure.processor.mode.ModeBasedProcessor;
import com.publicissapient.kpidashboard.azure.processor.mode.impl.online.OnlineDataProcessorImpl;
import com.publicissapient.kpidashboard.azure.repository.AzureProcessorRepository;
import com.publicissapient.kpidashboard.azure.util.AlphanumComparator;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;

@ExtendWith(SpringExtension.class)
public class AzureProcessorJobExecutorTest {

	private static final String PLAIN_TEXT_PASSWORD = "TestUser@123";

	private final ObjectId processorId = new ObjectId("5f0c1e1c204347d129590ef8");
	List<ModeBasedProcessor> list = new ArrayList<>();
	@InjectMocks
	AzureProcessorJobExecutor azureProcessorJobExecutor;
	@InjectMocks
	OnlineDataProcessorImpl onlineDataProcessor;
	@Mock
	AesEncryptionService aesEncryptionService;
	@Mock
	AzureIssueClient scrumJiraIssueClient;
	List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	List<ProjectToolConfig> projectToolConfigList;
	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	private AzureProcessorRepository issueProcessorRepository;
	@Mock
	private AzureProcessorConfig azureProcessorConfig;
	@Mock
	private FieldMappingRepository fieldMappingRepository;
	@Mock
	private AzureRestClientFactory azureRestClientFactory;
	@Mock
	private AlphanumComparator alphanumComparator;
	@Mock
	private AzureIssueClientFactory azureIssueClientFactory;
	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	@Spy
	private List<ModeBasedProcessor> modeBasedProcessors = new ArrayList<ModeBasedProcessor>();
	@Mock
	private ProjectToolConfigRepository toolRepository;
	@Mock
	private ConnectionRepository connectionRepository;
	@Mock
	private SprintClientImpl sprintClient;

	@BeforeEach
	public void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);

		projectToolConfigList = new ArrayList();
		scrumJiraIssueClient = new ScrumAzureIssueClientImpl();
		prepareProjectConfig();
		prepareToolConfig();
		AzureProcessor processor = new AzureProcessor();

	}

	@Test
	public void getCronTest() {
		Mockito.when(azureProcessorConfig.getCron()).thenReturn("* 0 0 0 0 0");
		Assert.assertEquals("* 0 0 0 0 0", azureProcessorJobExecutor.getCron());
	}

	@Test
	public void getProcessorTest() {
		Assert.assertNotNull(azureProcessorJobExecutor.getProcessor());
	}

	@Test
	public void getProcessorRepositoryTest() {
		Assert.assertEquals(issueProcessorRepository, azureProcessorJobExecutor.getProcessorRepository());
	}

	@Test
	public void execute() {
		AzureProcessor azureProcessor = new AzureProcessor();
		azureProcessor.setId(processorId);
		Mockito.when(projectConfigRepository.findAll()).thenReturn(projectConfigList);

		when(azureProcessorConfig.getThreadPoolSize()).thenReturn(3);

		boolean actualStatus = azureProcessorJobExecutor.execute(azureProcessor);
		boolean expectedStatus = true;
		assertEquals(expectedStatus, actualStatus);
	}

	private void prepareProjectConfig() throws JsonParseException, JsonMappingException, IOException {
		String filePath = "src/test/resources/onlinedata/azure/scrumprojectconfig.json";
		File file = new File(filePath);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		ProjectBasicConfig projectConfig = objectMapper.readValue(file, ProjectBasicConfig.class);
		projectConfigList.add(projectConfig);

	}

	private void prepareToolConfig() {
		ProjectToolConfig projectToolConfig = new ProjectToolConfig();
		projectToolConfig.setId(new ObjectId("5fb35501f8a7eb04100e7ec7"));
		projectToolConfig.setConnectionId(new ObjectId("5fb35501f8a7eb04100e7ec7"));
		projectToolConfig.setBasicProjectConfigId(new ObjectId());
		projectToolConfigList.add(projectToolConfig);
	}

	private List<ProjectToolConfig> prepareProjectToolConfig() {
		ProjectToolConfig tool = new ProjectToolConfig();
		tool.setToolName(ProcessorConstants.AZUREBOARDS);
		tool.setConnectionId(new ObjectId());
		tool.setQueryEnabled(false);
		tool.setBoardQuery("");
		tool.setProjectKey("Key");
		projectToolConfigList.add(tool);
		return projectToolConfigList;
	}

	private Optional<Connection> returnConnectionObject() {
		Connection connection = new Connection();
		connection.setOffline(false);
		connection.setBaseUrl("https://test.com/testUser/testProject");
		connection.setPassword(PLAIN_TEXT_PASSWORD);
		connection.setPat(PLAIN_TEXT_PASSWORD);
		connection.setOffline(true);
		return Optional.of(connection);
	}

	private AzureServer prepareAzureServer() {
		AzureServer azureServer = new AzureServer();
		azureServer.setPat("TestUser@123");
		azureServer.setUrl("https://test.com/testUser/testProject");
		azureServer.setApiVersion("5.1");
		azureServer.setUsername("");
		return azureServer;

	}
}