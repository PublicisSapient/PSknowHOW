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

package com.publicissapient.kpidashboard.apis.jira.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.factory.JiraKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.jira.kanban.service.NetOpenTicketCountByRCAServiceImpl;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;

/**
 *
 * @author pkum34
 *
 */

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ JiraKPIServiceFactory.class })
public class JiraServiceKanbanRTest {

	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	FilterHelperService filterHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@InjectMocks
	private JiraServiceKanbanR jiraServiceKanbanR;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private UserAuthorizedProjectsService authorizedProjectsService;
	@Mock
	private CacheService cacheService;
	@Mock
	private NetOpenTicketCountByRCAServiceImpl rcaServiceImpl;
	@SuppressWarnings("rawtypes")
	@Mock
	private List<JiraKPIService> services;
	private List<AccountHierarchyDataKanban> accountHierarchyDataList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private List<DataCount> dataCountRCAList = new ArrayList<>();
	private String[] projectKey;
	private Set<String> projects;

	private KpiElement ticketRcaElement;

	private Map<String, JiraKPIService<?, ?, ?>> jiraServiceCache = new HashMap<>();

	@Mock
	private JiraKPIServiceFactory jiraKPIServiceFactory;
	private KpiRequestFactory kpiRequestFactory;
	private List<HierarchyLevel> hierarchyLevels = new ArrayList<>();

	@Before
	public void setup() {
		kpiRequestFactory = KpiRequestFactory.newInstance();
		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();

		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		hierarchyLevels = hierachyLevelFactory.getHierarchyLevels();

		jiraKPIServiceFactory.initMyServiceCache();

		setMockProjectConfig();
		setMockFieldMapping();

		setRcaKpiElement();
		ticketRcaElement = setKpiElement(KPICode.NET_OPEN_TICKET_COUNT_BY_RCA.getKpiId(), "TICKET_RCA");
	}

	private void setRcaKpiElement() {

		ticketRcaElement = setKpiElement(KPICode.NET_OPEN_TICKET_COUNT_BY_RCA.getKpiId(), "TICKET_RCA");

		DataCount dataCount1 = setDataCount(2, "Coding Issue");
		DataCount dataCount2 = setDataCount(1, "Functionality Not Clear");

		dataCountRCAList.add(dataCount1);
		dataCountRCAList.add(dataCount2);

		ticketRcaElement.setValue(dataCountRCAList);
	}

	private KpiElement setKpiElement(String kpiId, String kpiName) {

		KpiElement kpiElement = new KpiElement();
		kpiElement.setKpiId(kpiId);
		kpiElement.setKpiName(kpiName);

		return kpiElement;
	}

	private DataCount setDataCount(int count, String data) {

		DataCount dataCount = new DataCount();
		dataCount.setCount(count);
		dataCount.setData(data);

		return dataCount;

	}

	private void setMockProjectConfig() {

		ProjectBasicConfig projectOne = new ProjectBasicConfig();
		projectOne.setId(new ObjectId("5b674d58f47cae8935b1b26f"));
		projectOne.setProjectName("Alpha_Project1_Name");

		ProjectBasicConfig projectTwo = new ProjectBasicConfig();
		projectTwo.setId(new ObjectId("5b719d06a500d00814bfb2b9"));
		projectTwo.setProjectName("Alpha_Project2_Name");

		ProjectBasicConfig projectThree = new ProjectBasicConfig();
		projectThree.setId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		projectThree.setProjectName("Beta_Project1_Name");

		projectConfigList.add(projectOne);
		projectConfigList.add(projectTwo);
		projectConfigList.add(projectThree);

	}

	private void setMockFieldMapping() {

		FieldMapping projectOne = new FieldMapping();
		projectOne.setBasicProjectConfigId(new ObjectId("5b674d58f47cae8935b1b26f"));
		projectOne.setJiraDefectCountlIssueTypeKPI36(Arrays.asList("Story"));
		projectOne.setJiraDefectCountlIssueTypeKPI28(Arrays.asList("Story"));

		FieldMapping projectTwo = new FieldMapping();
		projectTwo.setBasicProjectConfigId(new ObjectId("5b719d06a500d00814bfb2b9"));
		projectTwo.setJiraDefectCountlIssueTypeKPI36(Arrays.asList("Story"));
		projectTwo.setJiraDefectCountlIssueTypeKPI28(Arrays.asList("Story"));

		FieldMapping projectThree = new FieldMapping();
		projectThree.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		projectThree.setJiraDefectCountlIssueTypeKPI36(Arrays.asList("Story"));
		projectThree.setJiraDefectCountlIssueTypeKPI28(Arrays.asList("Story"));

		fieldMappingList.add(projectOne);
		fieldMappingList.add(projectTwo);
		fieldMappingList.add(projectThree);

	}

	@After
	public void cleanup() {

	}

	/**
	 * Test of empty filtered account hierarchy list.
	 *
	 * @throws Exception
	 */
	// @Test(expected = EntityNotFoundException.class)
	public void TestProcess_emptyFilteredACH() throws Exception {

		KpiRequest kpiRequest = kpiRequestFactory.findKpiRequest("kpi14");
		kpiRequest.setLabel("PROJECT");
		when(filterHelperService.getFilteredBuildsKanban(kpiRequest, "PROJECT"))
				.thenThrow(EntityNotFoundException.class);

		jiraServiceKanbanR.process(kpiRequest);

	}

	// @Test(expected = Exception.class)
	public void TestProcessException() throws Exception {

		KpiRequest kpiRequest = kpiRequestFactory.findKpiRequest("kpi14");
		kpiRequest.setLabel("PROJECT");

		when(filterHelperService.getFilteredBuildsKanban(kpiRequest, "PROJECT")).thenThrow(ApplicationException.class);

		jiraServiceKanbanR.process(kpiRequest);

	}

	@Test
	public void TestProcess() throws Exception {

		KpiRequest kpiRequest = kpiRequestFactory.findKpiRequest("kpi14");
		kpiRequest.setLabel("PROJECT");

		@SuppressWarnings("rawtypes")
		JiraKPIService mcokAbstract = rcaServiceImpl;
		jiraServiceCache.put(KPICode.NET_OPEN_TICKET_COUNT_BY_STATUS.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TICKET_VELOCITY.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TICKET_COUNT_BY_PRIORITY.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.NET_OPEN_TICKET_COUNT_BY_RCA.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TICKET_OPEN_VS_CLOSE_BY_PRIORITY.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.LEAD_TIME_KANBAN.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.KANBAN_JIRA_TECH_DEBT.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TEAM_CAPACITY.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TICKET_THROUGHPUT.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.WIP_VS_CLOSED.name(), mcokAbstract);

		try (MockedStatic<JiraKPIServiceFactory> utilities = Mockito.mockStatic(JiraKPIServiceFactory.class)) {
			utilities
					.when((Verification) JiraKPIServiceFactory
							.getJiraKPIService(KPICode.NET_OPEN_TICKET_COUNT_BY_STATUS.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.TICKET_VELOCITY.name()))
					.thenReturn(mcokAbstract);
			utilities.when(
					(Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.TICKET_COUNT_BY_PRIORITY.name()))
					.thenReturn(mcokAbstract);
			utilities.when(
					(Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.NET_OPEN_TICKET_COUNT_BY_RCA.name()))
					.thenReturn(mcokAbstract);
			utilities
					.when((Verification) JiraKPIServiceFactory
							.getJiraKPIService(KPICode.TICKET_OPEN_VS_CLOSE_BY_PRIORITY.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.LEAD_TIME_KANBAN.name()))
					.thenReturn(mcokAbstract);
			utilities
					.when((Verification) JiraKPIServiceFactory
							.getJiraKPIService(KPICode.TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.KANBAN_JIRA_TECH_DEBT.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.TEAM_CAPACITY.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.TICKET_THROUGHPUT.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.WIP_VS_CLOSED.name()))
					.thenReturn(mcokAbstract);
		}

		Map<String, Integer> map = new HashMap<>();
		Map<String, HierarchyLevel> hierarchyMap = hierarchyLevels.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
		hierarchyMap.entrySet().stream().forEach(k -> map.put(k.getKey(), k.getValue().getLevel()));
		when(filterHelperService.getHierarchyIdLevelMap(false)).thenReturn(map);
		when(filterHelperService.getHierarchyIdLevelMap(true)).thenReturn(map);
		when(filterHelperService.getFirstHierarachyLevel()).thenReturn("hierarchyLevelOne");
		when(filterHelperService.getFilteredBuildsKanban(ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(accountHierarchyDataList);
		when(authorizedProjectsService.filterKanbanProjects(accountHierarchyDataList))
				.thenReturn(accountHierarchyDataList);
		List<KpiElement> resultList = jiraServiceKanbanR.process(kpiRequest);

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {

			case NET_OPEN_TICKET_COUNT_BY_STATUS:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("TOTAL_TICKET_COUNT_BY_STATUS"));
				break;

			case TICKET_VELOCITY:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("TICKET_VELOCITY"));
				break;

			case TICKET_COUNT_BY_PRIORITY:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("TICKET_COUNT_BY_PRIORITY"));
				break;

			case LEAD_TIME_KANBAN:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("LEAD_TIME_KANBAN"));
				break;

			case TICKET_OPEN_VS_CLOSE_BY_PRIORITY:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("TICKET_OPEN_RATE"));
				break;

			case NET_OPEN_TICKET_COUNT_BY_RCA:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("TICKET_RCA"));
				break;

			case TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("STORY_OPEN_RATE_BY_ISSUE_TYPE"));
				break;

			case TEAM_CAPACITY:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("TEAM_CAPACITY"));
				break;

			case KANBAN_JIRA_TECH_DEBT:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("KANBAN_JIRA_TECH_DEBT"));
				break;

			case TICKET_THROUGHPUT:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("TICKET_THROUGHPUT"));
				break;

			case WIP_VS_CLOSED:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("WIP_VS_CLOSED"));
				break;

			default:
				break;
			}

		});

	}

	@Test
	public void TestProcess1() throws Exception {

		KpiRequest kpiRequest = kpiRequestFactory.findKpiRequest("kpi14");
		kpiRequest.setLabel("PROJECT");

		@SuppressWarnings("rawtypes")
		JiraKPIService mcokAbstract = rcaServiceImpl;
		jiraServiceCache.put(KPICode.NET_OPEN_TICKET_COUNT_BY_STATUS.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TICKET_VELOCITY.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TICKET_COUNT_BY_PRIORITY.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.NET_OPEN_TICKET_COUNT_BY_RCA.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TICKET_OPEN_VS_CLOSE_BY_PRIORITY.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.LEAD_TIME_KANBAN.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.KANBAN_JIRA_TECH_DEBT.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TEAM_CAPACITY.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TICKET_THROUGHPUT.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.WIP_VS_CLOSED.name(), mcokAbstract);

		try (MockedStatic<JiraKPIServiceFactory> utilities = Mockito.mockStatic(JiraKPIServiceFactory.class)) {
			utilities
					.when((Verification) JiraKPIServiceFactory
							.getJiraKPIService(KPICode.NET_OPEN_TICKET_COUNT_BY_STATUS.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.TICKET_VELOCITY.name()))
					.thenReturn(mcokAbstract);
			utilities.when(
					(Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.TICKET_COUNT_BY_PRIORITY.name()))
					.thenReturn(mcokAbstract);
			utilities.when(
					(Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.NET_OPEN_TICKET_COUNT_BY_RCA.name()))
					.thenReturn(mcokAbstract);
			utilities
					.when((Verification) JiraKPIServiceFactory
							.getJiraKPIService(KPICode.TICKET_OPEN_VS_CLOSE_BY_PRIORITY.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.LEAD_TIME_KANBAN.name()))
					.thenReturn(mcokAbstract);
			utilities
					.when((Verification) JiraKPIServiceFactory
							.getJiraKPIService(KPICode.TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.KANBAN_JIRA_TECH_DEBT.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.TEAM_CAPACITY.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.TICKET_THROUGHPUT.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.WIP_VS_CLOSED.name()))
					.thenReturn(mcokAbstract);
		}

		// when(filterHelperService.getFilteredBuildsKanban(any())).thenReturn(ahdList);

		// when(mcokAbstract.getKpiData(Mockito.any(), Mockito.any(),
		// Mockito.any())).thenReturn(ticketRcaElement);

		List<KpiElement> resultList = jiraServiceKanbanR.process(kpiRequest);

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {

			case NET_OPEN_TICKET_COUNT_BY_RCA:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("TICKET_RCA"));
				break;

			default:
				break;
			}

		});

	}

	@Test
	public void TestProcessKpiException() throws Exception {

		KpiRequest kpiRequest = kpiRequestFactory.findKpiRequest("kpi14");
		kpiRequest.setLabel("PROJECT");

		@SuppressWarnings("rawtypes")
		JiraKPIService mcokAbstract = rcaServiceImpl;
		jiraServiceCache.put(KPICode.NET_OPEN_TICKET_COUNT_BY_STATUS.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TICKET_VELOCITY.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TICKET_COUNT_BY_PRIORITY.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.NET_OPEN_TICKET_COUNT_BY_RCA.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TICKET_OPEN_VS_CLOSE_BY_PRIORITY.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.LEAD_TIME_KANBAN.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.KANBAN_JIRA_TECH_DEBT.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TEAM_CAPACITY.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TICKET_THROUGHPUT.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.WIP_VS_CLOSED.name(), mcokAbstract);

		try (MockedStatic<JiraKPIServiceFactory> utilities = Mockito.mockStatic(JiraKPIServiceFactory.class)) {
			utilities
					.when((Verification) JiraKPIServiceFactory
							.getJiraKPIService(KPICode.NET_OPEN_TICKET_COUNT_BY_STATUS.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.TICKET_VELOCITY.name()))
					.thenReturn(mcokAbstract);
			utilities.when(
					(Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.TICKET_COUNT_BY_PRIORITY.name()))
					.thenReturn(mcokAbstract);
			utilities.when(
					(Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.NET_OPEN_TICKET_COUNT_BY_RCA.name()))
					.thenReturn(mcokAbstract);
			utilities
					.when((Verification) JiraKPIServiceFactory
							.getJiraKPIService(KPICode.TICKET_OPEN_VS_CLOSE_BY_PRIORITY.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.LEAD_TIME_KANBAN.name()))
					.thenReturn(mcokAbstract);
			utilities
					.when((Verification) JiraKPIServiceFactory
							.getJiraKPIService(KPICode.TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.KANBAN_JIRA_TECH_DEBT.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.TEAM_CAPACITY.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.TICKET_THROUGHPUT.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.WIP_VS_CLOSED.name()))
					.thenReturn(mcokAbstract);
		}

		when(filterHelperService.getFilteredBuildsKanban(ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(accountHierarchyDataList);
		when(authorizedProjectsService.getKanbanProjectKey(accountHierarchyDataList, kpiRequest))
				.thenReturn(projectKey);

		// when(authorizedProjectsService.getKanbanProjectNodesForRequest(ahdList)).thenReturn(projects);
		//
		// when(mcokAbstract.getKpiData(Mockito.any(), Mockito.any(),
		// Mockito.any())).thenThrow(ApplicationException.class);

		List<KpiElement> resultList = jiraServiceKanbanR.process(kpiRequest);

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {

			case NET_OPEN_TICKET_COUNT_BY_RCA:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("TICKET_RCA"));
				break;

			default:
				break;
			}

		});

	}

}