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

package com.publicissapient.kpidashboard.jira.processor;

import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.BeanUtils;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.ProjectHierarchyService;
import com.publicissapient.kpidashboard.jira.dataFactories.AccountHierarchiesKanbanDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ConnectionsDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.HierachyLevelFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.KanbanJiraIssueDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ProjectBasicConfigDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ToolConfigDataFactory;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

@RunWith(MockitoJUnitRunner.class)
public class KanbanJiraIssueAccountHierarchyProcessorImplTest {

	List<HierarchyLevel> hierarchyLevelList;
	List<KanbanAccountHierarchy> accountHierarchyList;
	List<KanbanAccountHierarchy> accountHierarchies;
	List<ProjectToolConfig> projectToolConfigs;
	@Mock
	FieldMapping fieldMapping;
	Optional<Connection> connection;
	List<KanbanJiraIssue> kanbanJiraIssues;
	List<ProjectBasicConfig> projectConfigsList;
	@Mock
	private HierarchyLevelService hierarchyLevelService;
	@Mock
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo;
	@InjectMocks
	private KanbanJiraIssueAccountHierarchyProcessorImpl createKanbanAccountHierarchy;
	@Mock
	ProjectHierarchyService service;

	@Before
	public void setup() {
		hierarchyLevelList = getMockHierarchyLevel();
		accountHierarchyList = getMockAccountHierarchy();
		accountHierarchies = getMockAccountHierarchyByLabelNameAndBasicProjectConfigId();
		projectToolConfigs = getMockProjectToolConfig();
		fieldMapping = getMockFieldMapping();
		connection = getMockConnection();
		kanbanJiraIssues = getMockKanbanJiraIssue();
		projectConfigsList = getMockProjectConfig();
	}

	@Test
	public void createAccountHierarchy() {
		when(hierarchyLevelService.getFullHierarchyLevels(true)).thenReturn(hierarchyLevelList);
		Assert.assertEquals(
				1,
				createKanbanAccountHierarchy
						.createKanbanAccountHierarchy(kanbanJiraIssues.get(0), createProjectConfig())
						.size());
	}

	private List<HierarchyLevel> getMockHierarchyLevel() {
		HierachyLevelFactory hierarchyLevelFactory = HierachyLevelFactory
				.newInstance("/json/default/hierarchy_levels.json");
		return hierarchyLevelFactory.getHierarchyLevels();
	}

	private List<KanbanAccountHierarchy> getMockAccountHierarchy() {
		AccountHierarchiesKanbanDataFactory accountHierarchiesDataFactory = AccountHierarchiesKanbanDataFactory
				.newInstance("/json/default/account_hierarchy_kanban.json");
		return accountHierarchiesDataFactory.getAccountHierarchies();
	}

	private List<KanbanAccountHierarchy> getMockAccountHierarchyByLabelNameAndBasicProjectConfigId() {
		AccountHierarchiesKanbanDataFactory accountHierarchiesDataFactory = AccountHierarchiesKanbanDataFactory
				.newInstance("/json/default/account_hierarchy_kanban.json");
		return accountHierarchiesDataFactory
				.findByLabelNameAndBasicProjectConfigId(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, "6335368249794a18e8a4479f");
	}

	private List<KanbanJiraIssue> getMockKanbanJiraIssue() {
		KanbanJiraIssueDataFactory jiraIssueDataFactory = KanbanJiraIssueDataFactory
				.newInstance("/json/default/kanban_jira_issue.json");
		return jiraIssueDataFactory.getKanbanJiraIssues();
	}

	private List<ProjectBasicConfig> getMockProjectConfig() {
		ProjectBasicConfigDataFactory projectConfigDataFactory = ProjectBasicConfigDataFactory
				.newInstance("/json/default/project_basic_configs.json");
		return projectConfigDataFactory.getProjectBasicConfigs();
	}

	private ProjectConfFieldMapping createProjectConfig() {
		ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
		ProjectBasicConfig projectConfig = projectConfigsList.get(0);
		BeanUtils.copyProperties(projectConfig, projectConfFieldMapping);
		projectConfFieldMapping.setProjectBasicConfig(projectConfig);
		projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
		projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
		projectConfFieldMapping.setJira(getJiraToolConfig());
		projectConfFieldMapping.setJiraToolConfigId(projectToolConfigs.get(0).getId());
		projectConfFieldMapping.setFieldMapping(fieldMapping);

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
				"63c04dc7b7617e260763ca4e");
	}

	private Optional<Connection> getMockConnection() {
		ConnectionsDataFactory connectionDataFactory = ConnectionsDataFactory.newInstance("/json/default/connections.json");
		return connectionDataFactory.findConnectionById("5fd99f7bc8b51a7b55aec836");
	}

	private FieldMapping getMockFieldMapping() {
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/kanban_project_field_mappings.json");
		return fieldMappingDataFactory.findByBasicProjectConfigId("6335368249794a18e8a4479f");
	}
}
