/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package com.publicissapient.kpidashboard.apis.jira.service.backlogdashboard;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.factory.JiraNonTrendKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.jira.scrum.service.FlowLoadServiceImpl;
import com.publicissapient.kpidashboard.apis.jira.service.NonTrendKPIService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueReleaseStatusRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

@RunWith(MockitoJUnitRunner.class)
public class JiraBacklogServiceRTest {
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	KpiHelperService kpiHelperService;
	@Mock
	FilterHelperService filterHelperService;
	List<KpiElement> mockKpiElementList = new ArrayList<>();
	@Mock
	SprintRepository sprintRepository;
	@Mock
	JiraIssueRepository jiraIssueRepository;
	@Mock
	JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	private JiraBacklogServiceR jiraServiceR;
	@Mock
	private CacheService cacheService;
	@Mock
	private FlowLoadServiceImpl flowLoadService;
	@SuppressWarnings("rawtypes")
	@Mock
	private List<JiraBacklogKPIService> services;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private String[] projectKey;
	private List<HierarchyLevel> hierarchyLevels = new ArrayList<>();
	private KpiElement ibKpiElement;
	private Map<String, JiraBacklogKPIService> jiraServiceCache = new HashMap<>();
	@Mock
	private JiraNonTrendKPIServiceFactory jiraKPIServiceFactory;
	@Mock
	private UserAuthorizedProjectsService authorizedProjectsService;
	@Mock
	private JiraIssueReleaseStatusRepository jiraIssueReleaseStatusRepository;
	@Mock
	private FlowLoadServiceImpl service;
	@Mock
	private CustomApiConfig customApiConfig;

	@Before
	public void setup() throws ApplicationException {
		MockitoAnnotations.openMocks(this);
		List<NonTrendKPIService> mockServices = Arrays.asList(service);
		JiraNonTrendKPIServiceFactory serviceFactory = JiraNonTrendKPIServiceFactory.builder().services(mockServices)
				.build();
		doReturn(KPICode.FLOW_LOAD.name()).when(service).getQualifierType();
		doReturn(new KpiElement()).when(service).getKpiData(any(), any(), any());
		serviceFactory.initMyServiceCache();

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance("/json/default/account_hierarchy_filter_data.json");
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		hierarchyLevels = hierachyLevelFactory.getHierarchyLevels();

		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectConfig.setProjectName("Scrum Project");
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);

		when(filterHelperService.getHierarachyLevelId(4, "project", false)).thenReturn("project");

	}

	@Test(expected = Exception.class)
	public void testProcessException() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(6);
		kpiRequest.setSprintIncluded(null);

		jiraServiceR.process(kpiRequest);

	}

	@Test
	public void TestProcess_pickFromCache() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(5);
		when(kpiHelperService.getAuthorizedFilteredList(any(), any(), anyBoolean())).thenReturn(accountHierarchyDataList);
		when(kpiHelperService.getProjectKeyCache(any(), any(), anyBoolean())).thenReturn(kpiRequest.getIds());
		when(cacheService.cacheAccountHierarchyData()).thenReturn(accountHierarchyDataList);
		when(cacheService.getFromApplicationCache(any(), Mockito.anyString(), any(), ArgumentMatchers.anyList()))
				.thenReturn(new ArrayList<KpiElement>());
		List<KpiElement> resultList = jiraServiceR.process(kpiRequest);

		assertEquals(0, resultList.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void TestProcessWithApplicationException() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(4);

		@SuppressWarnings("rawtypes")
		JiraBacklogKPIService jiraKPIService = flowLoadService;
		jiraServiceCache.put(KPICode.FLOW_LOAD.name(), flowLoadService);

		try (MockedStatic<JiraNonTrendKPIServiceFactory> utilities = Mockito
				.mockStatic(JiraNonTrendKPIServiceFactory.class)) {
			utilities.when((MockedStatic.Verification) JiraNonTrendKPIServiceFactory
					.getJiraKPIService(KPICode.FLOW_LOAD.name())).thenReturn(jiraKPIService);
		}
		doThrow(ApplicationException.class).when(service).getKpiData(any(), any(), any());
		Map<String, Integer> map = new HashMap<>();
		Map<String, HierarchyLevel> hierarchyMap = hierarchyLevels.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
		hierarchyMap.entrySet().stream().forEach(k -> map.put(k.getKey(), k.getValue().getLevel()));
		when(cacheService.getFromApplicationCache(any(), any(), any(), any())).thenReturn(null);
		when(cacheService.cacheAccountHierarchyData()).thenReturn(accountHierarchyDataList);
		when(kpiHelperService.getAuthorizedFilteredList(any(), any(), anyBoolean())).thenReturn(accountHierarchyDataList);
		when(kpiHelperService.getProjectKeyCache(any(), any(), anyBoolean())).thenReturn(kpiRequest.getIds());
		List<KpiElement> resultList = jiraServiceR.process(kpiRequest);

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {

			case FLOW_LOAD:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("FLOW_LOAD"));
				break;

			default:
				break;
			}

		});

	}

	@SuppressWarnings("unchecked")
	@Test
	public void TestProcess2() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(4);

		@SuppressWarnings("rawtypes")
		JiraBacklogKPIService jiraKPIService = flowLoadService;
		jiraServiceCache.put(KPICode.FLOW_LOAD.name(), flowLoadService);

		try (MockedStatic<JiraNonTrendKPIServiceFactory> utilities = Mockito
				.mockStatic(JiraNonTrendKPIServiceFactory.class)) {
			utilities.when((MockedStatic.Verification) JiraNonTrendKPIServiceFactory
					.getJiraKPIService(KPICode.FLOW_LOAD.name())).thenReturn(jiraKPIService);
		}

		Map<String, Integer> map = new HashMap<>();
		Map<String, HierarchyLevel> hierarchyMap = hierarchyLevels.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
		hierarchyMap.entrySet().stream().forEach(k -> map.put(k.getKey(), k.getValue().getLevel()));
		when(cacheService.getFromApplicationCache(any(), any(), any(), any())).thenReturn(null);
		when(cacheService.cacheAccountHierarchyData()).thenReturn(accountHierarchyDataList);
		when(kpiHelperService.getAuthorizedFilteredList(any(), any(), anyBoolean())).thenReturn(accountHierarchyDataList);
		when(kpiHelperService.getProjectKeyCache(any(), any(), anyBoolean())).thenReturn(kpiRequest.getIds());
		List<KpiElement> resultList = jiraServiceR.process(kpiRequest);

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {

			case FLOW_LOAD:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("FLOW_LOAD"));
				break;

			default:
				break;
			}

		});

	}

	@Test
	public void processWithExposedApiToken() throws EntityNotFoundException {
		KpiRequest kpiRequest = createKpiRequest(5);
		when(cacheService.cacheAccountHierarchyData()).thenReturn(accountHierarchyDataList);
		List<KpiElement> resultList = jiraServiceR.processWithExposedApiToken(kpiRequest);
		assertEquals(0, resultList.size());
	}

	@Test
	public void testGetFutureSprintsList() throws NoSuchFieldException, IllegalAccessException {
		// Creating test data
		List<SprintDetails> futureSprintDetails = new ArrayList<>();
		futureSprintDetails.add(new SprintDetails("s1","sone","1","future",String.valueOf(LocalDate.of(2024, 3, 8)),String.valueOf(LocalDate.of(2024, 4, 8)),"",null,null,null,null,null,null,null,null,null,null,null));

		// Using reflection to set the private field futureSprintDetails
		Field field = JiraBacklogServiceR.class.getDeclaredField("futureSprintDetails");
		field.setAccessible(true);
		field.set(jiraServiceR, futureSprintDetails);
		when(customApiConfig.getSprintCountForBackLogStrength()).thenReturn(5); // Set the expected limit

		List<String> result = jiraServiceR.getFutureSprintsList();

		// Asserting the result
		assertEquals(1, result.size()); // Ensure correct number of sprints returned
	}

	@Test
	public void getJiraIssueReleaseForProject(){
		jiraServiceR.getJiraIssueReleaseForProject();
	}

	@Test
	public void getJiraIssuesForCurrentSprint(){
		jiraServiceR.getJiraIssuesForCurrentSprint();
	}

	@Test
	public void getJiraIssuesCustomHistoryForCurrentSprint(){
		jiraServiceR.getJiraIssuesCustomHistoryForCurrentSprint();
	}

	private KpiRequest createKpiRequest(int level) {
		KpiRequest kpiRequest = new KpiRequest();
		List<KpiElement> kpiList = new ArrayList<>();

		addKpiElement(kpiList, KPICode.FLOW_LOAD.getKpiId(), KPICode.FLOW_LOAD.name(), "Backlog", "");
		kpiRequest.setLevel(level);
		kpiRequest.setIds(new String[] { "Scrum Project_6335363749794a18e8a4479b" });
		kpiRequest.setKpiList(kpiList);
		kpiRequest.setRequestTrackerId();
		kpiRequest.setLabel("project");
		Map<String, List<String>> s = new HashMap<>();
		s.put("project", Arrays.asList("Scrum Project_6335363749794a18e8a4479b"));
		kpiRequest.setSelectedMap(s);
		kpiRequest.setSprintIncluded(Arrays.asList());
		return kpiRequest;
	}

	private void addKpiElement(List<KpiElement> kpiList, String kpiId, String kpiName, String category,
			String kpiUnit) {
		KpiElement kpiElement = new KpiElement();
		kpiElement.setKpiId(kpiId);
		kpiElement.setKpiName(kpiName);
		kpiElement.setKpiCategory(category);
		kpiElement.setKpiUnit(kpiUnit);
		kpiElement.setKpiSource("Jira");
		kpiElement.setMaxValue("500");
		kpiElement.setGroupId(1);
		kpiElement.setChartType("gaugeChart");
		kpiList.add(kpiElement);
	}

}