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

package com.publicissapient.kpidashboard.jira.tasklet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.BeanUtils;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.jira.client.JiraClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.dataFactories.ConnectionsDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ProjectBasicConfigDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ToolConfigDataFactory;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.service.CreateMetadata;
import com.publicissapient.kpidashboard.jira.service.JiraClientService;

@RunWith(MockitoJUnitRunner.class)
public class MetaDataTaskletTest {

	@Mock
	private FetchProjectConfiguration fetchProjectConfiguration;

	@Mock
	private JiraClient jiraClient;

	@Mock
	private JiraClientService jiraClientService;;

	@Mock
	CreateMetadata createMetadata;

	@Mock
	private StepContribution stepContribution;

	@Mock
	private ChunkContext chunkContext;

	@Mock
	private JiraProcessorConfig jiraProcessorConfig;

	List<ProjectToolConfig> projectToolConfigs;
	Optional<Connection> connection;
	List<ProjectBasicConfig> projectConfigsList;
	List<FieldMapping> fieldMappingList;

	@InjectMocks
	private MetaDataTasklet metaDataTasklet;

	@Before
	public void setUp() {
		// Mock any setup or common behavior needed before each test
		projectToolConfigs = getMockProjectToolConfig();
		connection = getMockConnection();
		projectConfigsList = getMockProjectConfig();
		fieldMappingList = getMockFieldMapping();
	}

	@Test
	public void testExecute() throws Exception {
		// Arrange

		when(fetchProjectConfiguration.fetchConfiguration(null)).thenReturn(createProjectConfigMap());

		when(jiraProcessorConfig.isFetchMetadata()).thenReturn(true);
		// Act
		RepeatStatus result = metaDataTasklet.execute(stepContribution, chunkContext);
		// Assert
		// verify(createMetadata, times(1)).collectMetadata(projectConfFieldMapping, null);
		assertEquals(RepeatStatus.FINISHED, result);
	}

	private ProjectConfFieldMapping createProjectConfigMap() {
		ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
		ProjectBasicConfig projectConfig = projectConfigsList.get(2);
		BeanUtils.copyProperties(projectConfig, projectConfFieldMapping);
		projectConfFieldMapping.setProjectBasicConfig(projectConfig);
		projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
		projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
		projectConfFieldMapping.setJira(getJiraToolConfig());
		projectConfFieldMapping.setProjectToolConfig(projectToolConfigs.get(0));
		projectConfFieldMapping.setJiraToolConfigId(projectToolConfigs.get(0).getId());
		projectConfFieldMapping.setFieldMapping(fieldMappingList.get(1));
		return projectConfFieldMapping;
	}

	private JiraToolConfig getJiraToolConfig() {
		JiraToolConfig toolObj = new JiraToolConfig();
		BeanUtils.copyProperties(projectToolConfigs.get(0), toolObj);
		toolObj.setConnection(connection);
		return toolObj;
	}

	private List<ProjectToolConfig> getMockProjectToolConfig() {
		ToolConfigDataFactory projectToolConfigDataFactory = ToolConfigDataFactory
				.newInstance("/json/default/project_tool_configs.json");
		return projectToolConfigDataFactory.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA,
				"63bfa0d5b7617e260763ca21");
	}

	private Optional<Connection> getMockConnection() {
		ConnectionsDataFactory connectionDataFactory = ConnectionsDataFactory.newInstance("/json/default/connections.json");
		return connectionDataFactory.findConnectionById("5fd99f7bc8b51a7b55aec836");
	}

	private List<ProjectBasicConfig> getMockProjectConfig() {
		ProjectBasicConfigDataFactory projectConfigDataFactory = ProjectBasicConfigDataFactory
				.newInstance("/json/default/project_basic_configs.json");
		return projectConfigDataFactory.getProjectBasicConfigs();
	}

	private List<FieldMapping> getMockFieldMapping() {
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/field_mapping.json");
		return fieldMappingDataFactory.getFieldMappings();
	}
}
