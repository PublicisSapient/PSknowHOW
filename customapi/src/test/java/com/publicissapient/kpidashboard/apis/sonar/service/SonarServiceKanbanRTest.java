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

package com.publicissapient.kpidashboard.apis.sonar.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
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
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.sonar.factory.SonarKPIServiceFactory;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;

/**
 * 
 */
@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ SonarKPIServiceFactory.class })
public class SonarServiceKanbanRTest {

	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	FilterHelperService filterHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@InjectMocks
	private SonarServiceKanbanR sonarService;
	@Mock
	private CustomApiConfig customApiSetting;
	@Mock
	private CacheService cacheService;
	@Mock
	private CommonService commonService;
	@Mock
	private SonarViolationsKanbanServiceImpl sonarViolationsService;
	@Mock
	private UserAuthorizedProjectsService authorizedProjectsService;
	@SuppressWarnings("rawtypes")
	@Mock
	private List<SonarKPIService> services;
	private KpiElement cqKpiElement;
	private KpiElement svKpiElement;
	private KpiElement tdKpiElement;
	private KpiElement tcKpiElement;
	private List<AccountHierarchyDataKanban> ahdList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private String[] projectKey;
	private Set<String> projects;

	private Map<String, SonarKPIService<?, ?, ?>> sonarServiceCache = new HashMap<>();

	@Mock
	private SonarKPIServiceFactory sonarKPIServiceFactory;

	private KpiRequest kpiRequest;
	private List<AccountHierarchyDataKanban> accountHierarchyKanbanDataList = new ArrayList<>();
	private List<HierarchyLevel> hierarchyLevels = new ArrayList<>();

	@Before
	public void setup() {
		sonarKPIServiceFactory.initMyServiceCache();

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("/json/default/kanban_kpi_request.json");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi64");
		kpiRequest.setLabel("PROJECT");

		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyKanbanDataList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();
		sonarKPIServiceFactory.initMyServiceCache();

		cqKpiElement = setKpiElement(KPICode.CODE_QUALITY_KANBAN.getKpiId(), "Code Quality");
		tdKpiElement = setKpiElement(KPICode.SONAR_TECH_DEBT_KANBAN.getKpiId(), "TechDebt");
		tcKpiElement = setKpiElement(KPICode.UNIT_TEST_COVERAGE_KANBAN.getKpiId(), "TestCoverage");
		svKpiElement = setKpiElement(KPICode.SONAR_VIOLATIONS_KANBAN.getKpiId(), "Sonar Violations");

		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		hierarchyLevels = hierachyLevelFactory.getHierarchyLevels();

	}

	private KpiElement setKpiElement(String kpiId, String kpiName) {
		KpiElement kpiElement = new KpiElement();
		kpiElement.setKpiId(kpiId);
		kpiElement.setKpiName(kpiName);
		return kpiElement;
	}

	/**
	 * Test of empty filtered account hierarchy list.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcessEmptyFilteredACH() throws Exception {

		KpiRequest kpiRequest = createKpiRequest("Sonar", 6);

		sonarService.process(kpiRequest);

	}

	/**
	 * Test of empty filtered account hierarchy list.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcessEmptyFilteredException() throws Exception {

		KpiRequest kpiRequest = createKpiRequest1("Sonar", 5);

		sonarService.process(kpiRequest);

	}

	@Test
	public void sonarViolationsTestProcess() throws Exception {

		String kpiRequestTrackerId = "Excel-Sonar-5be544de025de212549176a9";

		@SuppressWarnings("rawtypes")
		SonarKPIService mockAbstract = sonarViolationsService;
		sonarServiceCache.put(KPICode.SONAR_TECH_DEBT.name(), mockAbstract);
		sonarServiceCache.put(KPICode.UNIT_TEST_COVERAGE.name(), mockAbstract);
		sonarServiceCache.put(KPICode.SONAR_VIOLATIONS.name(), mockAbstract);

		try (MockedStatic<SonarKPIServiceFactory> utilities = Mockito.mockStatic(SonarKPIServiceFactory.class)) {
			utilities.when(
					(Verification) SonarKPIServiceFactory.getSonarKPIService(KPICode.SONAR_TECH_DEBT_KANBAN.name()))
					.thenReturn(mockAbstract);
			utilities.when(
					(Verification) SonarKPIServiceFactory.getSonarKPIService(KPICode.UNIT_TEST_COVERAGE_KANBAN.name()))
					.thenReturn(mockAbstract);
			utilities.when(
					(Verification) SonarKPIServiceFactory.getSonarKPIService(KPICode.SONAR_VIOLATIONS_KANBAN.name()))
					.thenReturn(mockAbstract);
		}

		String[] exampleStringList = { "exampleElement", "exampleElement" };
		when(filterHelperService.getHierarachyLevelId(Mockito.anyInt(), anyString(), Mockito.anyBoolean()))
				.thenReturn("project");
		when(filterHelperService.getFilteredBuildsKanban(ArgumentMatchers.any(), Mockito.anyString()))
				.thenReturn(accountHierarchyKanbanDataList);
		when(authorizedProjectsService.getKanbanProjectKey(accountHierarchyKanbanDataList, kpiRequest))
				.thenReturn(exampleStringList);
		when(filterHelperService.getFirstHierarachyLevel()).thenReturn("hierarchyLevelOne");
		Map<String, Integer> map = new HashMap<>();
		Map<String, HierarchyLevel> hierarchyMap = hierarchyLevels.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
		hierarchyMap.entrySet().stream().forEach(k -> map.put(k.getKey(), k.getValue().getLevel()));
		when(filterHelperService.getHierarchyIdLevelMap(false)).thenReturn(map);
		when(authorizedProjectsService.filterKanbanProjects(accountHierarchyKanbanDataList))
				.thenReturn(accountHierarchyKanbanDataList);

		try {
			List<KpiElement> resultList = sonarService.process(kpiRequest);
		} catch (Exception e) {

		}

	}

	@Test
	public void sonarViolationsTestProcessCachedData() throws Exception {

		KpiRequest kpiRequest = createKpiRequest("Excel-Sonar", 3);

		List<KpiElement> resultList = sonarService.process(kpiRequest);
		assertThat("Kpi list :", resultList.size(), equalTo(0));

	}

	private KpiRequest createKpiRequest(String source, int level) {
		KpiRequest kpiRequest = new KpiRequest();
		List<KpiElement> kpiList = new ArrayList<>();

		addKpiElement(kpiList, KPICode.CODE_QUALITY_KANBAN.getKpiId(), KPICode.CODE_QUALITY_KANBAN.name(),
				"Code Quality", "", source);
		addKpiElement(kpiList, KPICode.SONAR_TECH_DEBT_KANBAN.getKpiId(), KPICode.SONAR_TECH_DEBT_KANBAN.name(),
				"TechDebt", "", source);
		addKpiElement(kpiList, KPICode.UNIT_TEST_COVERAGE_KANBAN.getKpiId(), KPICode.UNIT_TEST_COVERAGE_KANBAN.name(),
				"TestCoverage", "", source);
		addKpiElement(kpiList, KPICode.SONAR_VIOLATIONS_KANBAN.getKpiId(), KPICode.SONAR_VIOLATIONS_KANBAN.name(),
				"Sonar Violations", "", source);
		kpiRequest.setLevel(level);
		kpiRequest.setIds(new String[] { "Kanban Project_6335368249794a18e8a4479f" });
		kpiRequest.setKpiList(kpiList);
		kpiRequest.setRequestTrackerId();
		Map<String, List<String>> selectedMap = new HashMap<>();
		selectedMap.put("Project", Arrays.asList("Kanban Project_6335368249794a18e8a4479f"));
		kpiRequest.setSelectedMap(selectedMap);
		return kpiRequest;
	}

	private KpiRequest createKpiRequest1(String source, int level) {
		KpiRequest kpiRequest = new KpiRequest();
		List<KpiElement> kpiList = new ArrayList<>();

		addKpiElement(kpiList, KPICode.CODE_QUALITY_KANBAN.getKpiId(), KPICode.CODE_QUALITY_KANBAN.name(),
				"Code Quality", "", source);
		kpiRequest.setLevel(level);
		kpiRequest.setIds(new String[] { "Kanban Project_6335368249794a18e8a4479f" });
		kpiRequest.setKpiList(kpiList);
		kpiRequest.setRequestTrackerId();
		Map<String, List<String>> selectedMap = new HashMap<>();
		selectedMap.put("Project", Arrays.asList("Kanban Project_6335368249794a18e8a4479f"));
		kpiRequest.setSelectedMap(selectedMap);
		return kpiRequest;
	}

	private void addKpiElement(List<KpiElement> kpiList, String kpiId, String kpiName, String category, String kpiUnit,
			String source) {
		KpiElement kpiElement = new KpiElement();
		kpiElement.setKpiId(kpiId);
		kpiElement.setKpiName(kpiName);
		kpiElement.setKpiCategory(category);
		kpiElement.setKpiUnit(kpiUnit);
		kpiElement.setKpiSource(source);

		kpiElement.setMaxValue("500");
		kpiElement.setChartType("gaugeChart");
		kpiList.add(kpiElement);
	}

}