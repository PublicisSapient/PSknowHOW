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

/**
 * 
 */
package com.publicissapient.kpidashboard.apis.testexecution.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.jira.service.SprintDetailsService;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.service.ProjectBasicConfigService;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.Week;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.testexecution.KanbanTestExecution;
import com.publicissapient.kpidashboard.common.model.testexecution.TestExecution;
import com.publicissapient.kpidashboard.common.model.testexecution.TestExecutionData;
import com.publicissapient.kpidashboard.common.repository.application.KanbanTestExecutionRepository;
import com.publicissapient.kpidashboard.common.repository.application.TestExecutionRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

/**
 * @author sansharm13
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestExecutionDataServiceImplTest {

	TestExecutionData testExecutionData = new TestExecutionData();
	private MockMvc mockMvc;
	private FieldMapping fieldMapping;
	@InjectMocks
	private TestExecutionDataServiceImpl testExecutionDataServiceImpl;
	@Mock
	private TestExecutionRepository testExecutionRepository;
	@Mock
	private KanbanTestExecutionRepository kanbanTestExecutionRepo;
	@Mock
	private CacheService cacheService;
	@Mock
	private ProjectBasicConfigService projectBasicConfigService;
	@Mock
	private SprintDetailsService sprintDetailsService;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private ConfigHelperService configHelperService;

	/**
	 * initialize values to be used in testing
	 */
	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(testExecutionDataServiceImpl).build();
		testExecutionData.setBasicProjectConfigId("5fc0853c410df80001701321");
		testExecutionData.setProjectNodeId("KnowHOW_5fc0853c410df80001701321");
		testExecutionData.setProjectName("KnowHOW");
		testExecutionData.setTotalTestCases(500);
		testExecutionData.setExecutedTestCase(23);
		testExecutionData.setPassedTestCase(45);
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
	}

	/**
	 * scrum TestExecution data saving
	 */
	@Test
	public void testProcessTestExecutionData_scrum_success() {
		testExecutionData.setSprintName("abc");
		testExecutionData.setSprintId("3456");
		testExecutionData.setKanban(false);
		assertNotNull(testExecutionDataServiceImpl.processTestExecutionData(testExecutionData));
	}

	/**
	 * kanban TestExecution data saving
	 */
	@Test
	public void testProcessTestExecutionData_kanban_success() {
		testExecutionData.setExecutionDate("2021-02-16");
		testExecutionData.setKanban(true);
		assertNotNull(testExecutionDataServiceImpl.processTestExecutionData(testExecutionData));
	}

	/**
	 * testWrong date format
	 */
	@Test
	public void testverifyDateformat() {
		testExecutionData.setExecutionDate("20-02-16");
		testExecutionData.setKanban(true);
		try {
			testExecutionDataServiceImpl.processTestExecutionData(testExecutionData);
		} catch (IllegalArgumentException e) {
			final String msg = "Date Formate should be in yy-mm-dd";
			assertEquals(msg, e.getMessage());
		}
	}

	/**
	 * test kanban failures
	 */
	@Test
	public void testProcessTestExecutionData_kanban_failure() {
		testExecutionData.setExecutionDate("2020-02-16");
		testExecutionData.setKanban(true);
		assertNotNull(testExecutionDataServiceImpl.processTestExecutionData(testExecutionData));
	}

	/**
	 * test SCRUM failures
	 */
	@Test
	public void testProcessTestExecutionData_Scrum_failure() {
		testExecutionData.setKanban(false);
		assertNull(testExecutionDataServiceImpl.processTestExecutionData(testExecutionData));
	}

	@Test
	public void getTestExecutions_ScrumSuccess() {
		ProjectBasicConfig project = createScrumProject();
		when(customApiConfig.getSprintCountForFilters()).thenReturn(5);
		when(projectBasicConfigService.getProjectBasicConfigs(anyString())).thenReturn(project);
		when(sprintDetailsService.getSprintDetails(anyString())).thenReturn(Arrays.asList(createSprint()));
		when(testExecutionRepository.findBySprintIdIn(anyList()))
				.thenReturn(Arrays.asList(createTestExecutionKpiDbDataScrum()));
		when(configHelperService.getFieldMapping(any())).thenReturn(fieldMapping);
		List<TestExecutionData> testExecutions = testExecutionDataServiceImpl
				.getTestExecutions("5fba82843ab187639c1147bd");
		Assert.assertEquals(1, testExecutions.size());

	}

	@Test
	public void getTestExecutions_ScrumWithNoSavedData() {
		ProjectBasicConfig project = createScrumProject();
		when(customApiConfig.getSprintCountForFilters()).thenReturn(5);
		when(projectBasicConfigService.getProjectBasicConfigs(anyString())).thenReturn(project);
		when(sprintDetailsService.getSprintDetails(anyString())).thenReturn(Arrays.asList(createSprint()));
		when(testExecutionRepository.findBySprintIdIn(anyList())).thenReturn(new ArrayList<>());
		when(configHelperService.getFieldMapping(any())).thenReturn(fieldMapping);
		List<TestExecutionData> testExecutions = testExecutionDataServiceImpl
				.getTestExecutions("5fba82843ab187639c1147bd");
		Assert.assertEquals(1, testExecutions.size());
	}

	@Test
	public void getTestExecutions_KanbanSuccess() {
		ProjectBasicConfig project = createKanbanProject();
		when(projectBasicConfigService.getProjectBasicConfigs(anyString())).thenReturn(project);
		when(kanbanTestExecutionRepo.findByBasicProjectConfigId(anyString()))
				.thenReturn(Arrays.asList(createTestExecutionKpiDbDataKanban()));
		when(customApiConfig.getNumberOfPastDaysForKanbanTestExecution()).thenReturn(2);
		when(customApiConfig.getNumberOfFutureDaysForKanbanTestExecution()).thenReturn(2);
		List<TestExecutionData> testExecutions = testExecutionDataServiceImpl
				.getTestExecutions("5fba82843ab187639c1147bd");

		Assert.assertEquals(5, testExecutions.size());
	}

	@Test
	public void getTestExecutions_KanbanWithNoDataSaved() {
		ProjectBasicConfig project = createKanbanProject();
		when(projectBasicConfigService.getProjectBasicConfigs(anyString())).thenReturn(project);
		when(kanbanTestExecutionRepo.findByBasicProjectConfigId(anyString())).thenReturn(new ArrayList<>());
		when(customApiConfig.getNumberOfPastDaysForKanbanTestExecution()).thenReturn(2);
		when(customApiConfig.getNumberOfFutureDaysForKanbanTestExecution()).thenReturn(2);
		List<TestExecutionData> testExecutions = testExecutionDataServiceImpl
				.getTestExecutions("5fba82843ab187639c1147bd");

		Assert.assertEquals(5, testExecutions.size());
	}

	private ProjectBasicConfig createScrumProject() {
		ProjectBasicConfig project = new ProjectBasicConfig();
		project.setId(new ObjectId("5fba82843ab187639c1147bd"));
		project.setIsKanban(false);
		return project;
	}

	private ProjectBasicConfig createKanbanProject() {
		ProjectBasicConfig project = new ProjectBasicConfig();
		project.setId(new ObjectId("5fba82843ab187639c1147bd"));
		project.setIsKanban(true);
		return project;
	}

	private TestExecution createTestExecutionKpiDbDataScrum() {
		TestExecution testExecution = new TestExecution();
		testExecution.setId(new ObjectId("62d8ce66604a13533b68d615"));
		testExecution.setProjectName("health project");
		testExecution.setProjectId("health project_5fba82843ab187639c1147bd");
		testExecution.setExecutedTestCase(45);
		testExecution.setPassedTestCase(40);
		testExecution.setTotalTestCases(50);
		testExecution.setSprintId("25073_health project_5fba82843ab187639c1147bd");
		return testExecution;
	}

	private KanbanTestExecution createTestExecutionKpiDbDataKanban() {
		KanbanTestExecution testExecution = new KanbanTestExecution();
		testExecution.setId(new ObjectId("62d8ce66604a13533b68d615"));
		testExecution.setProjectName("health project");
		testExecution.setProjectNodeId("health project_5fba82843ab187639c1147bd");
		testExecution.setExecutedTestCase(45);
		testExecution.setPassedTestCase(40);
		testExecution.setTotalTestCases(50);
		Week currentWeek = DateUtil.getWeek(LocalDate.now());
		testExecution.setExecutionDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		return testExecution;
	}

	private SprintDetails createSprint() {
		SprintDetails sprint = new SprintDetails();
		sprint.setSprintID("25073_health project_5fba82843ab187639c1147bd");
		sprint.setSprintName("Test Sprint");
		sprint.setState("CLOSED");
		sprint.setStartDate("2022-05-30T13:04:54.523Z");
		sprint.setEndDate("2022-06-13T13:04:00.000Z");
		return sprint;
	}
}