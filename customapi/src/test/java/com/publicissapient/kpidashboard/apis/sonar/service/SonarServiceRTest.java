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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import org.bson.types.ObjectId;
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
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.AccountHierarchyServiceImpl;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.sonar.factory.SonarKPIServiceFactory;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import org.slf4j.Logger;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SonarServiceRTest {
	private static final String TESTSONAR = "TEST_SONAR";

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
	private UserAuthorizedProjectsService authorizedProjectsService;
	@Mock
	private TokenAuthenticationService tokenAuthenticationService;
	@Mock
	private List<SonarKPIService> services;

	@Mock
	private Logger logger;

	private Map<String, Object> filterLevelMap;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private KpiRequest kpiRequest;
	private KpiElement kpiElement;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private List<HierarchyLevel> hierarchyLevels = new ArrayList<>();

	private SonarKPIServiceFactory sonarKPIServiceFactory;
	@Mock
	private SonarKPIService<?, ?, ?> sonarKPIService;

	@Mock
	TestService service;

	@Before
	public void setup() throws IllegalAccessException, ApplicationException {
		MockitoAnnotations.initMocks(this);
		List<SonarKPIService<?, ?, ?>> mockServices = Arrays.asList(service);
		sonarKPIServiceFactory = SonarKPIServiceFactory.builder().services(mockServices).build();
		doReturn(TESTSONAR).when(service).getQualifierType();
		doReturn(new KpiElement()).when(service).getKpiData(any(), any(), any());

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();

		kpiRequest = kpiRequestFactory.findKpiRequest("kpi38");
		createKpiRequest("SONAR", 3, kpiRequest);
		kpiRequest.setLabel("PROJECT");
		kpiElement = kpiRequest.getKpiList().get(0);

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		sonarKPIServiceFactory.initMyServiceCache();

		projectConfigList.forEach(projectConfig -> {
			projectConfigMap.put(projectConfig.getProjectName(), projectConfig);
		});

		fieldMappingList.forEach(fieldMapping -> {
			fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		});

		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		hierarchyLevels = hierachyLevelFactory.getHierarchyLevels();

	}

	/**
	 * Test of empty filtered account hierarchy list.
	 */
	@Test
	public void testProcessEmptyFilteredACH() {
		createKpiRequest("Sonar", 6, kpiRequest);
		sonarService.process(kpiRequest);
	}

	/**
	 * Test of empty filtered account hierarchy list.
	 */
	@Test
	public void testProcessEmptyFilteredException() {

		createKpiRequest("Sonar", 3, kpiRequest);
		sonarService.process(kpiRequest);
	}

	@Test
	public void sonarViolationsTestProcess() throws Exception {

		String kpiRequestTrackerId = "Excel-Sonar-5be544de025de212549176a9";
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
		// Create a mock SonarKPIService

		try {

			List<KpiElement> resultList = sonarService.process(kpiRequest);

		} catch (Exception e) {

		}

	}

	@Test
	public void sonarViolationsTestProcess_Excel() {
		String kpiRequestTrackerId = "Excel-Sonar-5be544de025de212549176a9";
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
		when(cacheService.getFromApplicationCache(eq(exampleStringList), eq(KPISource.ZEPHYRKANBAN.name()), eq(1), isNull()))
				.thenReturn(new ArrayList<KpiElement>());
		try {
			List<KpiElement> resultList = sonarService.process(kpiRequest);
		} catch (Exception e) {

		}
	}

	@Test
	public void sonarViolationsTestProcessCachedData() {

		createKpiRequest("Excel-Sonar", 2, kpiRequest);

		List<KpiElement> resultList = sonarService.process(kpiRequest);
		assertThat("Kpi list :", resultList.size(), equalTo(1));

	}

	private void createKpiRequest(String source, int level, KpiRequest kpiRequest) {
		List<KpiElement> kpiList = new ArrayList<>();
		addKpiElement(kpiList, TESTSONAR, TESTSONAR, "TechDebt", "", source);
		kpiRequest.setLevel(level);
		kpiRequest.setIds(new String[] { "Scrum Project_6335363749794a18e8a4479b" });
		kpiRequest.setKpiList(kpiList);
		kpiRequest.setRequestTrackerId();
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