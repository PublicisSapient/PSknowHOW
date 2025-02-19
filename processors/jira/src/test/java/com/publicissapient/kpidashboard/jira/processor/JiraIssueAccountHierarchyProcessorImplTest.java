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

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
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
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.ProjectHierarchyService;
import com.publicissapient.kpidashboard.jira.dataFactories.AccountHierarchiesDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ConnectionsDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.HierachyLevelFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ProjectBasicConfigDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ToolConfigDataFactory;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

@RunWith(MockitoJUnitRunner.class)
public class JiraIssueAccountHierarchyProcessorImplTest {

	List<HierarchyLevel> hierarchyLevelList;
	List<AccountHierarchy> accountHierarchyList;
	List<AccountHierarchy> accountHierarchies;
	List<ProjectToolConfig> projectToolConfigs;
	List<FieldMapping> fieldMappingList;
	Optional<Connection> connection;
	List<JiraIssue> jiraIssues;
	List<ProjectBasicConfig> projectConfigsList;
	@Mock
	private HierarchyLevelService hierarchyLevelService;
	@Mock
	private AccountHierarchyRepository accountHierarchyRepository;
	@Mock
	private SprintDetails sprintDetails;
	@InjectMocks
	private JiraIssueAccountHierarchyProcessorImpl createAccountHierarchy;
	@Mock
	private ProjectHierarchyService projectHierarchyService;

	@Before
	public void setup() {
		hierarchyLevelList = getMockHierarchyLevel();
		AccountHierarchiesDataFactory accountHierarchiesDataFactory = AccountHierarchiesDataFactory
				.newInstance("/json/default/account_hierarchy.json");
		accountHierarchyList = accountHierarchiesDataFactory.getAccountHierarchies();
		accountHierarchies = accountHierarchiesDataFactory
				.findByLabelNameAndBasicProjectConfigId(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, "63c04dc7b7617e260763ca4e");
		projectToolConfigs = getMockProjectToolConfig();
		fieldMappingList = getMockFieldMapping();
		connection = getMockConnection();
		jiraIssues = getMockJiraIssue();
		projectConfigsList = getMockProjectConfig();
		SprintDetailsDataFactory sprintDetailsDataFactory = SprintDetailsDataFactory.newInstance();
		sprintDetails = sprintDetailsDataFactory.getSprintDetails().get(0);
	}

	@Test
	public void createAccountHierarchy() {
		when(hierarchyLevelService.getFullHierarchyLevels(false)).thenReturn(hierarchyLevelList);
		Assert.assertEquals(
				2,
				createAccountHierarchy
						.createAccountHierarchy(jiraIssues.get(0), createProjectConfig(), getSprintDetails())
						.size());
	}

	@Test
	public void testCreateAccountHierarchy_Success() {
		when(hierarchyLevelService.getFullHierarchyLevels(false)).thenReturn(hierarchyLevelList);
		Map<String, List<ProjectHierarchy>> map = new HashMap<>();
		List<ProjectHierarchy> projectHierarchies = new ArrayList<>();
		projectHierarchies.add(new ProjectHierarchy());
		map.put("41409_NewJira_63c04dc7b7617e260763ca4e", projectHierarchies);
		when(projectHierarchyService.getProjectHierarchyMapByConfig(anyString())).thenReturn(map);
		Set<ProjectHierarchy> result =
				createAccountHierarchy.createAccountHierarchy(
						jiraIssues.get(0), createProjectConfig(), getSprintDetails());
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(2, result.size());
	}

	@Test
	public void testCreateAccountHierarchy_Success_1() {
		when(hierarchyLevelService.getFullHierarchyLevels(false)).thenReturn(hierarchyLevelList);
		Map<String, List<ProjectHierarchy>> map = new HashMap<>();
		List<ProjectHierarchy> projectHierarchies = new ArrayList<>();
		ProjectHierarchy projectHierarchy = new ProjectHierarchy();
		projectHierarchy.setNodeId("41409_NewJira_63c04dc7b7617e260763ca4e");
		projectHierarchy.setParentId("project_unique_003");
		projectHierarchies.add(projectHierarchy);
		map.put("41409_NewJira_63c04dc7b7617e260763ca4e", projectHierarchies);
		when(projectHierarchyService.getProjectHierarchyMapByConfig(anyString())).thenReturn(map);
		Set<ProjectHierarchy> result =
				createAccountHierarchy.createAccountHierarchy(
						jiraIssues.get(0), createProjectConfig(), getSprintDetails());
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(2, result.size());
	}

	private List<HierarchyLevel> getMockHierarchyLevel() {
		HierachyLevelFactory hierarchyLevelFactory = HierachyLevelFactory
				.newInstance("/json/default/hierarchy_levels.json");
		return hierarchyLevelFactory.getHierarchyLevels();
	}

	private Set<SprintDetails> getSprintDetails() {
		Set<SprintDetails> set = new HashSet<>();
		SprintDetails sprintDetails = new SprintDetails();
		sprintDetails.setSprintID("41409_NewJira_63c04dc7b7617e260763ca4e");
		sprintDetails.setOriginalSprintId("41409");
		sprintDetails.setState("ACTIVE");
		sprintDetails.setBasicProjectConfigId(new ObjectId("63c04dc7b7617e260763ca4e"));
		List<String> list = new ArrayList<>();
		list.add("41409");
		sprintDetails.setOriginBoardId(list);
		set.add(sprintDetails);
		return set;
	}

	private List<JiraIssue> getMockJiraIssue() {
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance("/json/default/jira_issues.json");
		return jiraIssueDataFactory.getJiraIssues();
	}

	private List<ProjectBasicConfig> getMockProjectConfig() {
		ProjectBasicConfigDataFactory projectConfigDataFactory = ProjectBasicConfigDataFactory
				.newInstance("/json/default/project_basic_configs.json");
		return projectConfigDataFactory.getProjectBasicConfigs();
	}

	private ProjectConfFieldMapping createProjectConfig() {
		ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
		ProjectBasicConfig projectConfig = projectConfigsList.get(2);
		BeanUtils.copyProperties(projectConfig, projectConfFieldMapping);
		projectConfFieldMapping.setProjectBasicConfig(projectConfig);
		projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
		projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
		projectConfFieldMapping.setJira(getJiraToolConfig());
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
				"63c04dc7b7617e260763ca4e");
	}

	private Optional<Connection> getMockConnection() {
		ConnectionsDataFactory connectionDataFactory = ConnectionsDataFactory.newInstance("/json/default/connections.json");
		return connectionDataFactory.findConnectionById("5fd99f7bc8b51a7b55aec836");
	}

	private List<FieldMapping> getMockFieldMapping() {
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/field_mapping.json");
		return fieldMappingDataFactory.getFieldMappings();
	}
}
