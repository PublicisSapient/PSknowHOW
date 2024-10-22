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
package com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.data.AdditionalFilterCategoryFactory;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import org.bson.types.ObjectId;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.data.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.factory.JiraNonTrendKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.jira.scrum.service.IterationBurnupServiceImpl;
import com.publicissapient.kpidashboard.apis.jira.service.NonTrendKPIService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

@RunWith(MockitoJUnitRunner.Silent.class)
public class JiraIterationServiceRTest {

	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	KpiHelperService kpiHelperService;
	@Mock
	FilterHelperService filterHelperService;
	@Mock
	SprintRepository sprintRepository;
	@Mock
	JiraIssueRepository jiraIssueRepository;
	@Mock
	JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@InjectMocks
	private JiraIterationServiceR jiraServiceR;
	@Mock
	private CacheService cacheService;
	@Mock
	private IterationBurnupServiceImpl iterationBurnupService;
	@SuppressWarnings("rawtypes")
	@Mock
	private List<JiraIterationKPIService> services;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private String[] projectKey;
	private Set<String> projects;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private List<DataCount> dataCountRCAList = new ArrayList<>();
	private List<HierarchyLevel> hierarchyLevels = new ArrayList<>();
	private KpiElement ibKpiElement;
	private Map<String, JiraIterationKPIService> jiraServiceCache = new HashMap<>();
	private Map<String, AdditionalFilterCategory> additonalFilterMap;
	@Mock
	private JiraNonTrendKPIServiceFactory jiraKPIServiceFactory;
	@Mock
	private UserAuthorizedProjectsService authorizedProjectsService;

	private KpiRequest kpiRequest;

	@Before
	public void setup() throws ApplicationException {
		MockitoAnnotations.openMocks(this);
		List<NonTrendKPIService> mockServices = Arrays.asList(iterationBurnupService);
		JiraNonTrendKPIServiceFactory serviceFactory = JiraNonTrendKPIServiceFactory.builder().services(mockServices)
				.build();
		doReturn(KPICode.ITERATION_BURNUP.name()).when(iterationBurnupService).getQualifierType();
		doReturn(new KpiElement()).when(iterationBurnupService).getKpiData(any(), any(), any());
		serviceFactory.initMyServiceCache();

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance("/json/default/project_hierarchy_filter_data.json");
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		hierarchyLevels = hierachyLevelFactory.getHierarchyLevels();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);
		filterLevelMap.put("SPRINT", Filters.SPRINT);

		kpiRequest = createKpiRequest(5);

		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectConfig.setProjectName("Scrum Project");
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);

		when(filterHelperService.getHierarachyLevelId(5, "sprint", false)).thenReturn("sprint");

		SprintDetailsDataFactory sprintDetailsDataFactory = SprintDetailsDataFactory.newInstance();
		List<SprintDetails> sprintDetails = sprintDetailsDataFactory.getSprintDetails();
		when(sprintRepository.findBySprintIDIn(anyList())).thenReturn(sprintDetails);

		AdditionalFilterCategoryFactory additionalFilterCategoryFactory = AdditionalFilterCategoryFactory.newInstance();
		List<AdditionalFilterCategory> additionalFilterCategoryList = additionalFilterCategoryFactory
				.getAdditionalFilterCategoryList();
		additonalFilterMap = additionalFilterCategoryList.stream()
				.collect(Collectors.toMap(AdditionalFilterCategory::getFilterCategoryId, x -> x));
		when(filterHelperService.getAdditionalFilterHierarchyLevel()).thenReturn(additonalFilterMap);
		when(cacheService.cacheSprintLevelData()).thenReturn(accountHierarchyDataList);
		when(kpiHelperService.getAuthorizedFilteredList(any(), any(), anyBoolean())).thenReturn(accountHierarchyDataList);
		when(kpiHelperService.getProjectKeyCache(any(), any(), anyBoolean())).thenReturn(kpiRequest.getIds());
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(true);
		when(cacheService.cacheSprintLevelData()).thenReturn(accountHierarchyDataList);

	}


	@Test
	public void testKPI() throws Exception {
		KpiRequest kpiRequest = createKpiRequest(5);
		when(kpiHelperService.isToolConfigured(any(), any(), any())).thenReturn(true);
		List<KpiElement> resultList = jiraServiceR.process(kpiRequest);

		MatcherAssert.assertThat("Kpi Name :", resultList.get(0).getResponseCode(), equalTo(CommonConstant.KPI_PASSED));

	}

	@Test
	public void TestProcess() throws Exception {
		when(kpiHelperService.getProjectKeyCache(any(), any(), anyBoolean())).thenReturn(kpiRequest.getIds());
		jiraServiceCache.put(KPICode.ITERATION_BURNUP.name(), iterationBurnupService);
		when(kpiHelperService.isToolConfigured(any(), any(), any())).thenReturn(true);
		List<KpiElement> resultList = jiraServiceR.process(kpiRequest);
		MatcherAssert.assertThat("Kpi Name :", resultList.get(0).getResponseCode(), equalTo(CommonConstant.KPI_PASSED));

	}

	@After
	public void cleanup() {

	}


	@Test
	public void TestProcess_pickFromCache() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(5);

		when(cacheService.getFromApplicationCache(any(), Mockito.anyString(), any(), ArgumentMatchers.anyList()))
				.thenReturn(new ArrayList<KpiElement>());

		List<KpiElement> resultList = jiraServiceR.process(kpiRequest);

		assertEquals(0, resultList.size());
	}



	@org.junit.Test
	public void TestProcess_ApplicationException() throws Exception {
		when(kpiHelperService.isToolConfigured(any(), any(), any())).thenReturn(true);
		when(iterationBurnupService.getKpiData(any(), any(), any())).thenThrow(ApplicationException.class);
		List<KpiElement> resultList = jiraServiceR.process(kpiRequest);
		MatcherAssert.assertThat("Kpi Name :", resultList.get(0).getResponseCode(), equalTo(CommonConstant.KPI_FAILED));
	}


	@Test
	public void TestProcess_NPException() throws Exception {
		when(kpiHelperService.isToolConfigured(any(), any(), any())).thenReturn(true);
		when(iterationBurnupService.getKpiData(any(), any(), any())).thenThrow(NullPointerException.class);
		List<KpiElement> resultList = jiraServiceR.process(kpiRequest);
		MatcherAssert.assertThat("Kpi Name :", resultList.get(0).getResponseCode(), equalTo(CommonConstant.KPI_FAILED));
	}

	@Test
	public void processWithExposedApiToken() throws EntityNotFoundException {
		KpiRequest kpiRequest = createKpiRequest(5);
		when(cacheService.getFromApplicationCache(any(), Mockito.anyString(), any(), ArgumentMatchers.anyList()))
				.thenReturn(new ArrayList<KpiElement>());
		List<KpiElement> resultList = jiraServiceR.processWithExposedApiToken(kpiRequest);
		assertEquals(0, resultList.size());
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

		addKpiElement(kpiList, KPICode.ITERATION_BURNUP.getKpiId(), KPICode.ITERATION_BURNUP.name(), "Iteration", "");
		kpiRequest.setLevel(level);
		kpiRequest.setIds(new String[] { "38296_Scrum Project_6335363749794a18e8a4479b" });
		kpiRequest.setKpiList(kpiList);
		kpiRequest.setRequestTrackerId();
		kpiRequest.setLabel("sprint");
		Map<String, List<String>> s = new HashMap<>();
		s.put("sprint", Arrays.asList("38296_Scrum Project_6335363749794a18e8a4479b"));
		kpiRequest.setSelectedMap(s);
		kpiRequest.setSprintIncluded(Arrays.asList("CLOSED", "ACTIVE"));
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
		kpiElement.setGroupId(1);
		kpiElement.setMaxValue("500");
		kpiElement.setChartType("gaugeChart");
		kpiList.add(kpiElement);
	}
}