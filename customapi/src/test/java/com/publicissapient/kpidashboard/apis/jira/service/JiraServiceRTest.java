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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.factory.JiraKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.jira.scrum.service.RCAServiceImpl;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;

/**
 *
 * @author tauakram
 *
 */

@RunWith(MockitoJUnitRunner.Silent.class)
public class JiraServiceRTest {

	private static String GROUP_PROJECT = "project";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	KpiHelperService kpiHelperService;
	@Mock
	FilterHelperService filterHelperService;
	List<KpiElement> mockKpiElementList = new ArrayList<>();
	@InjectMocks
	@Spy
	private JiraServiceR jiraServiceR;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private CacheService cacheService;
	@Mock
	private RCAServiceImpl rcaServiceImpl;
	@SuppressWarnings("rawtypes")
	@Mock
	private List<JiraKPIService> services;
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
	private JiraKPIServiceFactory jiraKPIServiceFactory;
	@Mock
	private UserAuthorizedProjectsService authorizedProjectsService;

	@Before
	public void setup() {
		mockKpiElementList.add(rcaKpiElement);

		when(cacheService.getFromApplicationCache(any(), Mockito.anyString(), any(), ArgumentMatchers.anyList()))
				.thenReturn(mockKpiElementList);

		// jiraKPIServiceFactory.initMyServiceCache();

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
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
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);

		when(filterHelperService.getHierarachyLevelId(4, "project", false)).thenReturn("project");

		setRcaKpiElement();

	}

	private void setRcaKpiElement() {

		rcaKpiElement = setKpiElement("kpi36", "DEFECT_RCA");

		DataCount dataCount1 = setDataCount(2, "Coding Issue");
		DataCount dataCount2 = setDataCount(1, "Functionality Not Clear");

		dataCountRCAList.add(dataCount1);
		dataCountRCAList.add(dataCount2);

		rcaKpiElement.setValue(dataCountRCAList);
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

	@After
	public void cleanup() {

	}

	@Test(expected = Exception.class)
	public void testProcessException() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(4);

		when(filterHelperService.getFilteredBuilds(kpiRequest, GROUP_PROJECT)).thenThrow(ApplicationException.class);

		jiraServiceR.process(kpiRequest);

	}

	@Test
	public void TestProcess_pickFromCache() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(4);

		// checking only for RCA
		mockKpiElementList.add(rcaKpiElement);

		when(cacheService.getFromApplicationCache(any(), Mockito.anyString(), any(), ArgumentMatchers.anyList()))
				.thenReturn(mockKpiElementList);
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(true);

		List<KpiElement> resultList = jiraServiceR.process(kpiRequest);

		assertThat("Kpi Name :", resultList.get(0).getKpiName(), equalTo("DEFECT_COUNT_BY_RCA"));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void TestProcess() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(4);

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
		when(filterHelperService.getFilteredBuilds(kpiRequest, GROUP_PROJECT)).thenReturn(accountHierarchyDataList);
		when(authorizedProjectsService.getProjectKey(accountHierarchyDataList, kpiRequest)).thenReturn(projectKey);
		when(authorizedProjectsService.filterProjects(accountHierarchyDataList)).thenReturn(accountHierarchyDataList);
		when(filterHelperService.getFirstHierarachyLevel()).thenReturn("hierarchyLevelOne");
		List<KpiElement> resultList = jiraServiceR.process(kpiRequest);

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {

			case DEFECT_SEEPAGE_RATE:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("DEFECT_SEEPAGE_RATE"));
				break;

			case DEFECT_REJECTION_RATE:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("DEFECT_REJECTION_RATE"));
				break;

			case DEFECT_INJECTION_RATE:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("DEFECT_INJECTION_RATE"));
				break;

			case DEFECT_COUNT_BY_PRIORITY:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("DEFECT_COUNT_BY_PRIORITY"));
				break;

			case DEFECT_COUNT_BY_RCA:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("DEFECT_COUNT_BY_RCA"));

				break;

			case DEFECT_REMOVAL_EFFICIENCY:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("DEFECT_REMOVAL_EFFICIENCY"));
				break;

			case TECH_DEBT:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("TECH_DEBT"));
				break;

			case ISSUE_COUNT:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("ISSUE_COUNT"));

				break;
			case SPRINT_VELOCITY:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("SPRINT_VELOCITY"));
				break;

			case TOTAL_DEFECT_COUNT:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("TOTAL_DEFECT_COUNT"));
				break;

			case LEAD_TIME:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("LEAD_TIME"));
				break;

			default:
				break;
			}

		});

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
		when(filterHelperService.getFilteredBuilds(kpiRequest, GROUP_PROJECT)).thenReturn(accountHierarchyDataList);

		when(mcokAbstract.getKpiData(any(), any(), any())).thenReturn(rcaKpiElement);

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

		when(filterHelperService.getFilteredBuilds(kpiRequest, GROUP_PROJECT)).thenReturn(accountHierarchyDataList);

		when(mcokAbstract.getKpiData(any(), any(), any())).thenReturn(rcaKpiElement);

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

		addKpiElement(kpiList, KPICode.DEFECT_COUNT_BY_RCA.getKpiId(), KPICode.DEFECT_COUNT_BY_RCA.name(),
				"Category One", "");
		addKpiElement(kpiList, KPICode.DEFECT_INJECTION_RATE.getKpiId(), KPICode.DEFECT_INJECTION_RATE.name(),
				"Category One", "%");
		addKpiElement(kpiList, KPICode.DEFECT_COUNT_BY_PRIORITY.getKpiId(), KPICode.DEFECT_COUNT_BY_PRIORITY.name(),
				"Category One", "");
		addKpiElement(kpiList, KPICode.DEFECT_REJECTION_RATE.getKpiId(), KPICode.DEFECT_REJECTION_RATE.name(),
				"Category One", "%");
		addKpiElement(kpiList, KPICode.DEFECT_REMOVAL_EFFICIENCY.getKpiId(), KPICode.DEFECT_REMOVAL_EFFICIENCY.name(),
				"Category One", "%");
		addKpiElement(kpiList, KPICode.DEFECT_SEEPAGE_RATE.getKpiId(), KPICode.DEFECT_SEEPAGE_RATE.name(),
				"Category One", "%");
		addKpiElement(kpiList, KPICode.TECH_DEBT.getKpiId(), KPICode.TECH_DEBT.name(), "Category One", "Days");
		addKpiElement(kpiList, KPICode.TOTAL_DEFECT_COUNT.getKpiId(), KPICode.TOTAL_DEFECT_COUNT.name(), "Category One",
				"");
		addKpiElement(kpiList, KPICode.ISSUE_COUNT.getKpiId(), KPICode.ISSUE_COUNT.name(), "Category One", "SP");
		addKpiElement(kpiList, KPICode.SPRINT_VELOCITY.getKpiId(), KPICode.SPRINT_VELOCITY.name(), "Category One",
				"SP");
		addKpiElement(kpiList, KPICode.LEAD_TIME.getKpiId(), KPICode.LEAD_TIME.name(), "Category Two", "Days");
		kpiRequest.setLevel(level);
		kpiRequest.setIds(new String[] { "Scrum Project_6335363749794a18e8a4479b" });
		kpiRequest.setKpiList(kpiList);
		kpiRequest.setRequestTrackerId();
		kpiRequest.setLabel("PROJECT");
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
		kpiRequest.setIds(new String[] { "Scrum Project_6335363749794a18e8a4479b" });
		kpiRequest.setKpiList(kpiList);
		kpiRequest.setRequestTrackerId();
		return kpiRequest;
	}

}