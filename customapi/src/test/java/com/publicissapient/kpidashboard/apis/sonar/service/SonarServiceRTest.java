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
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.filter.service.AccountHierarchyServiceImpl;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
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
public class SonarServiceRTest {

	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	FilterHelperService filterHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@InjectMocks
	private SonarServiceR sonarService;
	@Mock
	private CustomApiConfig customApiSetting;
	@Mock
	private CacheService cacheService;
	@Mock
	private CommonService commonService;
	@Mock
	private AccountHierarchyServiceImpl accountHierarchyServiceImpl;
	@Mock
	private SonarViolationsServiceImpl sonarViolationsService;
	@Mock
	private UserAuthorizedProjectsService authorizedProjectsService;
	@Mock
	private TokenAuthenticationService tokenAuthenticationService;
	@SuppressWarnings("rawtypes")
	@Mock
	private List<SonarKPIService> services;
	private KpiElement cqKpiElement;
	private KpiElement svKpiElement;
	private KpiElement tdKpiElement;
	private KpiElement tcKpiElement;
	private Map<String, Object> filterLevelMap;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private String[] projectKey;
	private Set<String> projects;

	private Map<String, SonarKPIService<?, ?, ?>> sonarServiceCache = new HashMap<>();
	private KpiRequest kpiRequest;
	private KpiElement kpiElement;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private List<HierarchyLevel> hierarchyLevels = new ArrayList<>();

	@Mock
	private SonarKPIServiceFactory sonarKPIServiceFactory;

	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi38");
		kpiRequest.setLabel("PROJECT");
		kpiElement = kpiRequest.getKpiList().get(0);

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		sonarKPIServiceFactory.initMyServiceCache();

		tdKpiElement = setKpiElement(KPICode.SONAR_TECH_DEBT.getKpiId(), "TechDebt");
		tcKpiElement = setKpiElement(KPICode.UNIT_TEST_COVERAGE.getKpiId(), "TestCoverage");
		svKpiElement = setKpiElement(KPICode.SONAR_VIOLATIONS.getKpiId(), "Sonar Violations");

		projectConfigList.forEach(projectConfig -> {
			projectConfigMap.put(projectConfig.getProjectName(), projectConfig);
		});

		fieldMappingList.forEach(fieldMapping -> {
			fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		});

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

		KpiRequest kpiRequest = createKpiRequest("Sonar", 3);

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
			utilities.when((Verification) SonarKPIServiceFactory.getSonarKPIService(KPICode.SONAR_TECH_DEBT.name()))
					.thenReturn(mockAbstract);
			utilities.when((Verification) SonarKPIServiceFactory.getSonarKPIService(KPICode.UNIT_TEST_COVERAGE.name()))
					.thenReturn(mockAbstract);
			utilities.when((Verification) SonarKPIServiceFactory.getSonarKPIService(KPICode.SONAR_VIOLATIONS.name()))
					.thenReturn(mockAbstract);
		}

		String[] exampleStringList = { "exampleElement", "exampleElement" };
		when(authorizedProjectsService.getProjectKey(accountHierarchyDataList, kpiRequest))
				.thenReturn(exampleStringList);
		when(filterHelperService.getHierarachyLevelId(Mockito.anyInt(), anyString(), Mockito.anyBoolean()))
				.thenReturn("project");
		when(filterHelperService.getFirstHierarachyLevel()).thenReturn("hierarchyLevelOne");
		Map<String, Integer> map = new HashMap<>();
		Map<String, HierarchyLevel> hierarchyMap = hierarchyLevels.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
		hierarchyMap.entrySet().stream().forEach(k -> map.put(k.getKey(), k.getValue().getLevel()));
		when(filterHelperService.getHierarchyIdLevelMap(false)).thenReturn(map);
		when(filterHelperService.getFilteredBuilds(ArgumentMatchers.any(), Mockito.anyString()))
				.thenReturn(accountHierarchyDataList);
		when(authorizedProjectsService.filterProjects(accountHierarchyDataList)).thenReturn(accountHierarchyDataList);
		try {

			List<KpiElement> resultList = sonarService.process(kpiRequest);

		} catch (Exception e) {

		}

	}

	@Test
	public void sonarViolationsTestProcessCachedData() throws Exception {

		KpiRequest kpiRequest = createKpiRequest("Excel-Sonar", 2);

		List<KpiElement> resultList = sonarService.process(kpiRequest);
		assertThat("Kpi list :", resultList.size(), equalTo(3));

	}

	@Test
	public void testProcessCached() throws Exception {

		KpiRequest kpiRequest = createKpiRequest("Sonar", 5);

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
		List<KpiElement> resultList = sonarService.process(kpiRequest);

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {
			case SONAR_TECH_DEBT:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("SONAR_TECH_DEBT"));
				break;
			case UNIT_TEST_COVERAGE:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("UNIT_TEST_COVERAGE"));
				break;
			case SONAR_VIOLATIONS:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("SONAR_VIOLATIONS"));
				break;

			default:
				break;
			}

		});

	}

	private KpiRequest createKpiRequest(String source, int level) {
		KpiRequest kpiRequest = new KpiRequest();
		List<KpiElement> kpiList = new ArrayList<>();

		addKpiElement(kpiList, KPICode.SONAR_TECH_DEBT.getKpiId(), KPICode.SONAR_TECH_DEBT.name(), "TechDebt", "",
				source);
		addKpiElement(kpiList, KPICode.UNIT_TEST_COVERAGE.getKpiId(), KPICode.UNIT_TEST_COVERAGE.name(), "TestCoverage",
				"", source);
		addKpiElement(kpiList, KPICode.SONAR_VIOLATIONS.getKpiId(), KPICode.SONAR_VIOLATIONS.name(), "Sonar Violations",
				"", source);
		kpiRequest.setLevel(level);
		kpiRequest.setIds(new String[] { "Scrum Project_6335363749794a18e8a4479b" });
		kpiRequest.setKpiList(kpiList);
		kpiRequest.setRequestTrackerId();
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