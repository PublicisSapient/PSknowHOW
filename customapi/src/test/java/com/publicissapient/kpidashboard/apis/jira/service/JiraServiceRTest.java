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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueReleaseStatusDataFactory;
import com.publicissapient.kpidashboard.apis.data.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.factory.JiraKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.jira.scrum.service.RCAServiceImpl;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueReleaseStatusRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

/**
 *
 * @author tauakram
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class JiraServiceRTest {
	private static final String TESTJIRA = "TEST_JIRA";

	private static String GROUP_PROJECT = "project";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	KpiHelperService kpiHelperService;
	@Mock
	FilterHelperService filterHelperService;
	List<KpiElement> mockKpiElementList = new ArrayList<>();
	@InjectMocks
	private JiraServiceR jiraServiceR;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private CacheService cacheService;
	@Mock
	private RCAServiceImpl rcaServiceImpl;
	@Mock
	ConfigHelperService configHelperService;
	@SuppressWarnings("rawtypes")
	@Mock
	private List<JiraKPIService> services;
	@Mock
	private JiraIssueRepository jiraIssueRepository;
	@Mock
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Mock
	private JiraIssueReleaseStatusRepository jiraIssueReleaseStatusRepository;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private String[] projectKey;
	private Set<String> projects;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private List<DataCount> dataCountRCAList = new ArrayList<>();
	private List<HierarchyLevel> hierarchyLevels = new ArrayList<>();
	private KpiElement rcaKpiElement;
	private Map<String, JiraKPIService<?, ?, ?>> jiraServiceCache = new HashMap<>();
	@Mock
	private UserAuthorizedProjectsService authorizedProjectsService;
	@Mock
	private TestService service;
	@Mock
	private SprintRepository sprintRepository;

	@Before
	public void setup() throws ApplicationException {
		MockitoAnnotations.openMocks(this);
		List<JiraKPIService<?, ?, ?>> mockServices = Arrays.asList(service);
		JiraKPIServiceFactory serviceFactory = JiraKPIServiceFactory.builder().services(mockServices).build();
		doReturn(TESTJIRA).when(service).getQualifierType();
		doReturn(new KpiElement()).when(service).getKpiData(any(), any(), any());
		serviceFactory.initMyServiceCache();

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		when(filterHelperService.getHierarachyLevelId(Mockito.anyInt(), anyString(), Mockito.anyBoolean()))
				.thenReturn("project");
		when(filterHelperService.getFilteredBuilds(ArgumentMatchers.any(), Mockito.anyString()))
				.thenReturn(accountHierarchyDataList);
		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		hierarchyLevels = hierachyLevelFactory.getHierarchyLevels();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);
		filterLevelMap.put("SPRINT", Filters.SPRINT);

		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectConfig.setProjectName("Scrum Project");
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMapping.setJiraSubTaskDefectType(Arrays.asList("Bug"));
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

	}

	@After
	public void cleanup() {

	}

	@Test(expected = Exception.class)
	public void testProcessException() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(4);
		kpiRequest.setSprintIncluded(null);

		when(filterHelperService.getFilteredBuilds(kpiRequest, GROUP_PROJECT)).thenThrow(ApplicationException.class);

		jiraServiceR.process(kpiRequest);

	}

	@Test
	public void TestProcess_pickFromCache() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(4);
		String[] exampleStringList = { "exampleElement", "exampleElement" };
		// when(cacheService.getFromApplicationCache(eq(exampleStringList),
		// eq(KPISource.JIRA.name()), eq(1), anyList()))
		// .thenReturn(new ArrayList<KpiElement>());
		when(cacheService.getFromApplicationCache(any(), Mockito.anyString(), any(), ArgumentMatchers.anyList()))
				.thenReturn(new ArrayList<KpiElement>());
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(true);
		when(kpiHelperService.getAuthorizedFilteredList(any(), any())).thenReturn(accountHierarchyDataList);
		when(kpiHelperService.getProjectKeyCache(any(), any())).thenReturn(kpiRequest.getIds());
		when(cacheService.cacheAccountHierarchyData()).thenReturn(accountHierarchyDataList);

		List<KpiElement> resultList = jiraServiceR.process(kpiRequest);

		assertEquals(0, resultList.size());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void TestProcess() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(4);
		Map<String, Integer> map = new HashMap<>();
		Map<String, HierarchyLevel> hierarchyMap = hierarchyLevels.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
		hierarchyMap.entrySet().stream().forEach(k -> map.put(k.getKey(), k.getValue().getLevel()));
		when(filterHelperService.getHierarchyIdLevelMap(false)).thenReturn(map);
		when(filterHelperService.getFilteredBuilds(kpiRequest, GROUP_PROJECT)).thenReturn(accountHierarchyDataList);
		when(authorizedProjectsService.getProjectKey(accountHierarchyDataList, kpiRequest)).thenReturn(projectKey);
		when(authorizedProjectsService.filterProjects(accountHierarchyDataList)).thenReturn(accountHierarchyDataList);
		when(filterHelperService.getFirstHierarachyLevel()).thenReturn("hierarchyLevelOne");
		List<KpiElement> resultList = jiraServiceR.process(kpiRequest);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testProcess1() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(5);

		@SuppressWarnings("rawtypes")
		JiraKPIService mcokAbstract = rcaServiceImpl;
		jiraServiceCache.put(KPICode.DEFECT_COUNT_BY_RCA.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.DEFECT_INJECTION_RATE.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.DEFECT_SEEPAGE_RATE.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.DEFECT_REJECTION_RATE.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.DEFECT_COUNT_BY_PRIORITY.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.DEFECT_REMOVAL_EFFICIENCY.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TECH_DEBT.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.ISSUE_COUNT.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.SPRINT_VELOCITY.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.LEAD_TIME.name(), mcokAbstract);
		jiraServiceCache.put(KPICode.TOTAL_DEFECT_COUNT.name(), mcokAbstract);

		try (MockedStatic<JiraKPIServiceFactory> utilities = Mockito.mockStatic(JiraKPIServiceFactory.class)) {
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.DEFECT_COUNT_BY_RCA.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.DEFECT_INJECTION_RATE.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.DEFECT_SEEPAGE_RATE.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.DEFECT_REJECTION_RATE.name()))
					.thenReturn(mcokAbstract);
			utilities.when(
					(Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.DEFECT_COUNT_BY_PRIORITY.name()))
					.thenReturn(mcokAbstract);
			utilities.when(
					(Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.DEFECT_REMOVAL_EFFICIENCY.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.TECH_DEBT.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.ISSUE_COUNT.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.SPRINT_VELOCITY.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.LEAD_TIME.name()))
					.thenReturn(mcokAbstract);
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.TOTAL_DEFECT_COUNT.name()))
					.thenReturn(mcokAbstract);
		}
		Map<String, Integer> map = new HashMap<>();
		Map<String, HierarchyLevel> hierarchyMap = hierarchyLevels.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
		hierarchyMap.entrySet().stream().forEach(k -> map.put(k.getKey(), k.getValue().getLevel()));
		when(filterHelperService.getHierarchyIdLevelMap(false)).thenReturn(map);
		when(cacheService.getFromApplicationCache(any(), any(), any(), any())).thenReturn(null);
		when(cacheService.cacheAccountHierarchyData()).thenReturn(accountHierarchyDataList);
		when(authorizedProjectsService.getProjectKey(accountHierarchyDataList, kpiRequest)).thenReturn(projectKey);
		when(authorizedProjectsService.filterProjects(any())).thenReturn(accountHierarchyDataList.stream()
				.filter(s -> s.getLeafNodeId().equalsIgnoreCase("38296_Scrum Project_6335363749794a18e8a4479b"))
				.collect(Collectors.toList()));
		when(filterHelperService.getFirstHierarachyLevel()).thenReturn("hierarchyLevelOne");
		when(cacheService.cacheFieldMappingMapData()).thenReturn(fieldMappingMap);
		when(kpiHelperService.getAuthorizedFilteredList(any(), any())).thenReturn(accountHierarchyDataList);
		when(kpiHelperService.getProjectKeyCache(any(), any())).thenReturn(kpiRequest.getIds());
//		when(mcokAbstract.getKpiData(any(), any(), any())).thenReturn(rcaKpiElement);

		List<KpiElement> resultList = jiraServiceR.process(kpiRequest);

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {

			case DEFECT_SEEPAGE_RATE:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("DEFECT_SEEPAGE_RATE"));
				break;

			default:
				break;
			}

		});

	}

	@SuppressWarnings("unchecked")
	@Test
	public void TestProcessExcel() throws Exception {

		KpiRequest kpiRequest = createKpiRequest1(6);

		@SuppressWarnings("rawtypes")
		JiraKPIService mcokAbstract = rcaServiceImpl;

		jiraServiceCache.put(KPICode.DEFECT_COUNT_BY_RCA.name(), mcokAbstract);
		try (MockedStatic<JiraKPIServiceFactory> utilities = Mockito.mockStatic(JiraKPIServiceFactory.class)) {
			utilities.when((Verification) JiraKPIServiceFactory.getJiraKPIService(KPICode.DEFECT_COUNT_BY_RCA.name()))
					.thenReturn(mcokAbstract);
		}

		Map<String, Integer> map = new HashMap<>();
		Map<String, HierarchyLevel> hierarchyMap = hierarchyLevels.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
		hierarchyMap.entrySet().stream().forEach(k -> map.put(k.getKey(), k.getValue().getLevel()));
		when(filterHelperService.getHierarchyIdLevelMap(false)).thenReturn(map);
		when(cacheService.getFromApplicationCache(any(), any(), any(), any())).thenReturn(null);
		when(cacheService.cacheAccountHierarchyData()).thenReturn(accountHierarchyDataList);
		when(authorizedProjectsService.getProjectKey(accountHierarchyDataList, kpiRequest)).thenReturn(projectKey);
		when(authorizedProjectsService.filterProjects(any())).thenReturn(accountHierarchyDataList.stream()
				.filter(s -> s.getLeafNodeId().equalsIgnoreCase("38296_Scrum Project_6335363749794a18e8a4479b"))
				.collect(Collectors.toList()));
		when(filterHelperService.getFirstHierarachyLevel()).thenReturn("hierarchyLevelOne");
		when(cacheService.cacheFieldMappingMapData()).thenReturn(fieldMappingMap);
		when(kpiHelperService.getAuthorizedFilteredList(any(), any())).thenReturn(accountHierarchyDataList);
		when(kpiHelperService.getProjectKeyCache(any(), any())).thenReturn(kpiRequest.getIds());
//		when(mcokAbstract.getKpiData(any(), any(), any())).thenReturn(rcaKpiElement);

		List<KpiElement> resultList = jiraServiceR.process(kpiRequest);

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {

			case DEFECT_COUNT_BY_RCA:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("DEFECT_COUNT_BY_RCA"));

				break;

			default:
				break;
			}

		});

	}

	private KpiRequest createKpiRequest(int level) {
		KpiRequest kpiRequest = new KpiRequest();
		List<KpiElement> kpiList = new ArrayList<>();

		addKpiElement(kpiList, KPICode.TEST_JIRA.getKpiId(), KPICode.TEST_JIRA.name(), "Category One", "");
		kpiRequest.setLevel(level);
		kpiRequest.setIds(new String[] { "Scrum Project_6335363749794a18e8a4479b" });
		kpiRequest.setKpiList(kpiList);
		kpiRequest.setRequestTrackerId();
		kpiRequest.setLabel("PROJECT");
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

	private KpiRequest createKpiRequest1(int level) {
		KpiRequest kpiRequest = new KpiRequest();
		List<KpiElement> kpiList = new ArrayList<>();

		KpiElement kpiElement = new KpiElement();
		kpiElement.setKpiId(KPICode.DEFECT_COUNT_BY_RCA.getKpiId());
		kpiElement.setKpiName(KPICode.DEFECT_COUNT_BY_RCA.name());
		kpiElement.setKpiCategory("Category One");
		kpiElement.setKpiUnit("");
		kpiElement.setKpiSource("Excel-Jira");

		kpiElement.setMaxValue("500");
		kpiElement.setChartType("gaugeChart");
		kpiList.add(kpiElement);

		kpiRequest.setLevel(level);
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setIds(new String[] { "Scrum Project_6335363749794a18e8a4479b" });
		kpiRequest.setKpiList(kpiList);
		kpiRequest.setRequestTrackerId();
		kpiRequest.setSprintIncluded(Arrays.asList());
		return kpiRequest;
	}

}