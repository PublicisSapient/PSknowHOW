/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.cleanup;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.service.ProjectBasicConfigService;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.repository.zephyr.TestCaseDetailsRepository;

/**
 * @author anisingh4
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class AgileDataCleanUpServiceTest {

	@InjectMocks
	private AgileDataCleanUpService agileDataCleanUpService;

	@Mock
	private ProjectToolConfigRepository projectToolConfigRepository;

	@Mock
	private JiraIssueRepository jiraIssueRepository;

	@Mock
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Mock
	private KanbanJiraIssueRepository kanbanJiraIssueRepository;

	@Mock
	private KanbanJiraIssueHistoryRepository kanbanJiraIssueHistoryRepository;

	@Mock
	private AccountHierarchyRepository accountHierarchyRepository;

	@Mock
	private ProjectBasicConfigService projectBasicConfigService;

	@Mock
	private FilterHelperService filterHelperService;

	@Mock
	private CacheService cacheService;

	@Mock
	private TestCaseDetailsRepository testCaseDetailsRepository;

	@Mock
	private FieldMappingRepository fieldMappingRepository;

	@Mock
	private SprintRepository sprintRepository;

	@Mock
	private ProjectReleaseRepo projectReleaseRepo;

	@Mock
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepository;

	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@Test
	public void getToolCategory() {
		String actualResult = agileDataCleanUpService.getToolCategory();
		assertEquals(ProcessorType.AGILE_TOOL.toString(), actualResult);
	}

	@Test
	public void clean_Kanban() {
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/kanban/kanban_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);

		ProjectToolConfig projectToolConfig = new ProjectToolConfig();
		projectToolConfig.setId(new ObjectId("5e9e4593e4b0c8ece56710c3"));
		projectToolConfig.setBasicProjectConfigId(new ObjectId("6335368249794a18e8a4479f"));
		projectToolConfig.setToolName(ProcessorConstants.JIRA);

		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("6335368249794a18e8a4479f"));
		projectBasicConfig.setProjectName("Kanban Project");
		projectBasicConfig.setIsKanban(true);

		when(fieldMappingRepository.findByBasicProjectConfigId(Mockito.any())).thenReturn(fieldMapping);
		when(projectToolConfigRepository.findById(Mockito.anyString())).thenReturn(projectToolConfig);
		when(projectBasicConfigService.getProjectBasicConfigs(Mockito.anyString())).thenReturn(projectBasicConfig);

		doNothing().when(kanbanJiraIssueRepository).deleteByBasicProjectConfigId(Mockito.anyString());
		doNothing().when(kanbanJiraIssueHistoryRepository).deleteByBasicProjectConfigId(Mockito.anyString());
		doNothing().when(testCaseDetailsRepository).deleteByBasicProjectConfigId(Mockito.anyString());
		doNothing().when(projectReleaseRepo).deleteByConfigId(Mockito.any());
		doNothing().when(processorExecutionTraceLogRepository)
				.deleteByBasicProjectConfigIdAndProcessorName(Mockito.any(), Mockito.anyString());
		agileDataCleanUpService.clean("5e9e4593e4b0c8ece56710c3");

		verify(kanbanJiraIssueRepository, times(1)).deleteByBasicProjectConfigId("6335368249794a18e8a4479f");
		verify(kanbanJiraIssueHistoryRepository, times(1)).deleteByBasicProjectConfigId("6335368249794a18e8a4479f");
		verify(processorExecutionTraceLogRepository, times(1))
				.deleteByBasicProjectConfigIdAndProcessorName("6335368249794a18e8a4479f", ProcessorConstants.JIRA);
	}

	@Test
	public void clean_Scrum() {
		ProjectToolConfig projectToolConfig = new ProjectToolConfig();
		projectToolConfig.setId(new ObjectId("5e9e4593e4b0c8ece56710c3"));
		projectToolConfig.setBasicProjectConfigId(new ObjectId("5e9db8f1e4b0caefbfa8e0c7"));

		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("5e9db8f1e4b0caefbfa8e0c7"));
		projectBasicConfig.setIsKanban(false);
		projectToolConfig.setToolName(ProcessorConstants.JIRA);

		FieldMapping fieldMapping = new FieldMapping();
		when(fieldMappingRepository.findByBasicProjectConfigId(Mockito.any())).thenReturn(fieldMapping);
		when(projectToolConfigRepository.findById(Mockito.anyString())).thenReturn(projectToolConfig);
		doNothing().when(sprintRepository).deleteByBasicProjectConfigId(Mockito.any());
		when(projectBasicConfigService.getProjectBasicConfigs(Mockito.anyString())).thenReturn(projectBasicConfig);

		doNothing().when(testCaseDetailsRepository).deleteByBasicProjectConfigId(Mockito.anyString());
		doNothing().when(jiraIssueRepository).deleteByBasicProjectConfigId(Mockito.anyString());
		doNothing().when(processorExecutionTraceLogRepository)
				.deleteByBasicProjectConfigIdAndProcessorName(Mockito.any(), Mockito.anyString());
		agileDataCleanUpService.clean("5e9db8f1e4b0caefbfa8e0c7");

		verify(jiraIssueRepository, times(1)).deleteByBasicProjectConfigId("5e9db8f1e4b0caefbfa8e0c7");
		verify(jiraIssueCustomHistoryRepository, times(1)).deleteByBasicProjectConfigId("5e9db8f1e4b0caefbfa8e0c7");
		verify(sprintRepository, times(1)).deleteByBasicProjectConfigId(new ObjectId("5e9db8f1e4b0caefbfa8e0c7"));
		verify(processorExecutionTraceLogRepository, times(1))
				.deleteByBasicProjectConfigIdAndProcessorName("5e9db8f1e4b0caefbfa8e0c7", ProcessorConstants.JIRA);

	}

}