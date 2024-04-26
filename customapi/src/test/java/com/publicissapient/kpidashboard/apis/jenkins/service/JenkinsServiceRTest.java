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

package com.publicissapient.kpidashboard.apis.jenkins.service;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
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
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jenkins.factory.JenkinsKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsServiceRTest {

	private static String GROUP_PROJECT = "PROJECT";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	KpiHelperService kpiHelperService;
	@Mock
	FilterHelperService filterHelperService;
	@InjectMocks
	private JenkinsServiceR jenkinsServiceR;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private CacheService cacheService;
	@Mock
	private CodeBuildTimeServiceImpl codeBuildTimeServiceImpl;
	@Mock
	private UserAuthorizedProjectsService authorizedProjectsService;
	@SuppressWarnings("rawtypes")
	@Mock
	private List<JenkinsKPIService> services;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private String[] projectKey;
	private Set<String> projects;
	private KpiElement buildKpiElement;
	private Map<String, JenkinsKPIService<?, ?, ?>> jenkinsServiceCache = new HashMap<>();
	@Mock
	private JenkinsKPIServiceFactory jenkinsKPIServiceFactory;

	@Before
	public void setup() {

		KpiRequest kpiRequestJenkins = createKpiRequest(5, "Jenkins");

		jenkinsKPIServiceFactory.initMyServiceCache();

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

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

		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		List<HierarchyLevel> hierarchyLevels = hierachyLevelFactory.getHierarchyLevels();


		//when(authorizedProjectsService.getProjectKey(accountHierarchyDataList, kpiRequest)).thenReturn(projectKey);
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


		buildKpiElement = setKpiElement(KPICode.CODE_BUILD_TIME.getKpiId(), "CODE_BUILD_TIME");
	}

	// level = sprint
	@Test
	public void testProcess() throws Exception {

		when(kpiHelperService.isMandatoryFieldValuePresentOrNot(any(), any())).thenReturn(true);
		KpiRequest kpiRequest = createKpiRequest(4, "Jenkins");

		jenkinsServiceCache.put(KPICode.CODE_BUILD_TIME.name(), codeBuildTimeServiceImpl);
		when(filterHelperService.getFilteredBuilds(kpiRequest, "project")).thenReturn(accountHierarchyDataList);

		List<KpiElement> resultList;
		try (MockedStatic<JenkinsKPIServiceFactory> mockedStatic = mockStatic(JenkinsKPIServiceFactory.class)) {
			CodeBuildTimeServiceImpl mockService = mock(CodeBuildTimeServiceImpl.class);
			mockedStatic.when(() -> JenkinsKPIServiceFactory.getJenkinsKPIService(eq(KPICode.CODE_BUILD_TIME.name())))
					.thenReturn(mockService);
			when(mockService.getKpiData(any(), any(), any())).thenReturn(kpiRequest.getKpiList().get(0));
			resultList = jenkinsServiceR.process(kpiRequest);
			mockedStatic.verify(() -> JenkinsKPIServiceFactory.getJenkinsKPIService(eq(KPICode.CODE_BUILD_TIME.name())));
		}

		assertThat("Kpi Name :", resultList.get(0).getKpiName(), equalTo("CODE_BUILD_TIME"));
		assertThat("Kpi Name :", resultList.get(0).getResponseCode(), equalTo(CommonConstant.KPI_PASSED));
	}

	@Test
	public void testProcess_MandatoryFalse() throws Exception {

		when(kpiHelperService.isMandatoryFieldValuePresentOrNot(any(), any())).thenReturn(false);
		KpiRequest kpiRequest = createKpiRequest(4, "Jenkins");

		jenkinsServiceCache.put(KPICode.CODE_BUILD_TIME.name(), codeBuildTimeServiceImpl);
		when(filterHelperService.getFilteredBuilds(kpiRequest, "project")).thenReturn(accountHierarchyDataList);

		List<KpiElement> resultList;
		try (MockedStatic<JenkinsKPIServiceFactory> mockedStatic = mockStatic(JenkinsKPIServiceFactory.class)) {
			CodeBuildTimeServiceImpl mockService = mock(CodeBuildTimeServiceImpl.class);
			mockedStatic.when(() -> JenkinsKPIServiceFactory.getJenkinsKPIService(eq(KPICode.CODE_BUILD_TIME.name())))
					.thenReturn(mockService);
			resultList = jenkinsServiceR.process(kpiRequest);
			mockedStatic.verify(() -> JenkinsKPIServiceFactory.getJenkinsKPIService(eq(KPICode.CODE_BUILD_TIME.name())));
		}
		assertThat("Kpi Name :", resultList.get(0).getResponseCode(), equalTo(CommonConstant.MANDATORY_FIELD_MAPPING));
	}

	@Test
	public void testProcess_ApplicatioException() throws Exception {

		when(kpiHelperService.isMandatoryFieldValuePresentOrNot(any(), any())).thenReturn(true);
		KpiRequest kpiRequest = createKpiRequest(4, "Jenkins");

		jenkinsServiceCache.put(KPICode.CODE_BUILD_TIME.name(), codeBuildTimeServiceImpl);
		when(filterHelperService.getFilteredBuilds(kpiRequest, "project")).thenReturn(accountHierarchyDataList);

		List<KpiElement> resultList;
		try (MockedStatic<JenkinsKPIServiceFactory> mockedStatic = mockStatic(JenkinsKPIServiceFactory.class)) {
			CodeBuildTimeServiceImpl mockService = mock(CodeBuildTimeServiceImpl.class);
			mockedStatic.when(() -> JenkinsKPIServiceFactory.getJenkinsKPIService(eq(KPICode.CODE_BUILD_TIME.name())))
					.thenReturn(mockService);
			when(mockService.getKpiData(any(), any(), any())).thenThrow(ApplicationException.class);
			resultList = jenkinsServiceR.process(kpiRequest);
			mockedStatic.verify(() -> JenkinsKPIServiceFactory.getJenkinsKPIService(eq(KPICode.CODE_BUILD_TIME.name())));
		}

		assertThat("Kpi Name :", resultList.get(0).getResponseCode(), equalTo(CommonConstant.KPI_FAILED));
	}

	@Test
	public void testProcess_NullPointer() throws Exception {

		when(kpiHelperService.isMandatoryFieldValuePresentOrNot(any(), any())).thenReturn(true);
		KpiRequest kpiRequest = createKpiRequest(4, "Jenkins");

		jenkinsServiceCache.put(KPICode.CODE_BUILD_TIME.name(), codeBuildTimeServiceImpl);
		when(filterHelperService.getFilteredBuilds(kpiRequest, "project")).thenReturn(accountHierarchyDataList);

		List<KpiElement> resultList;
		try (MockedStatic<JenkinsKPIServiceFactory> mockedStatic = mockStatic(JenkinsKPIServiceFactory.class)) {
			CodeBuildTimeServiceImpl mockService = mock(CodeBuildTimeServiceImpl.class);
			when(mockService.getKpiData(any(), any(), any())).thenReturn(buildKpiElement);
			mockedStatic.when(() -> JenkinsKPIServiceFactory.getJenkinsKPIService(eq(KPICode.CODE_BUILD_TIME.name())))
					.thenReturn(mockService);
			when(mockService.getKpiData(any(), any(), any())).thenThrow(NullPointerException.class);
			resultList = jenkinsServiceR.process(kpiRequest);
			mockedStatic.verify(() -> JenkinsKPIServiceFactory.getJenkinsKPIService(eq(KPICode.CODE_BUILD_TIME.name())));
		}

		assertThat("Kpi Name :", resultList.get(0).getKpiName(), equalTo("CODE_BUILD_TIME"));
		assertThat("Kpi Name :", resultList.get(0).getResponseCode(), equalTo(CommonConstant.KPI_FAILED));
	}

	@SuppressWarnings("unchecked")
	@Test(expected = HttpMessageNotWritableException.class)
	public void testProcessException() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(4, "Jenkins");

		jenkinsServiceCache.put(KPICode.CODE_BUILD_TIME.name(), codeBuildTimeServiceImpl);

		try (MockedStatic<JenkinsKPIServiceFactory> utilities = mockStatic(JenkinsKPIServiceFactory.class)) {
			utilities.when((Verification) JenkinsKPIServiceFactory.getJenkinsKPIService(KPICode.CODE_BUILD_TIME.name()))
					.thenReturn(codeBuildTimeServiceImpl);
		}

		when(filterHelperService.getFilteredBuilds(any(), Mockito.anyString()))
				.thenThrow(HttpMessageNotWritableException.class);
		jenkinsServiceR.process(kpiRequest);

	}

	@Test
	public void testProcessCachedData() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(4, "Jenkins");
		when(filterHelperService.getFilteredBuilds(kpiRequest, "project")).thenReturn(accountHierarchyDataList);
		when(cacheService.getFromApplicationCache(any(), any(), any(), any()))
				.thenReturn(Arrays.asList(buildKpiElement));

		List<KpiElement> resultList = jenkinsServiceR.process(kpiRequest);
		assertThat("Kpi list :", resultList.size(), equalTo(1));

	}

	@Test
	public void processWithExposedApiToken() throws EntityNotFoundException {
		KpiRequest kpiRequest = createKpiRequest(4, "Jenkins");
		when(filterHelperService.getFilteredBuilds(kpiRequest, "project")).thenReturn(accountHierarchyDataList);
		when(cacheService.getFromApplicationCache(any(), any(), any(), any()))
				.thenReturn(Arrays.asList(buildKpiElement));
		List<KpiElement> resultList = jenkinsServiceR.processWithExposedApiToken(kpiRequest);
		assertEquals(1, resultList.size());
	}

	private KpiRequest createKpiRequest(int level, String source) {
		KpiRequest kpiRequest = new KpiRequest();
		List<KpiElement> kpiList = new ArrayList<>();

		addKpiElement(kpiList, KPICode.CODE_BUILD_TIME.getKpiId(), KPICode.CODE_BUILD_TIME.name(), "Productivity",
				"mins", source);
		kpiRequest.setLevel(level);
		kpiRequest.setLabel("project");
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

	private KpiElement setKpiElement(String kpiId, String kpiName) {

		KpiElement kpiElement = new KpiElement();
		kpiElement.setKpiId(kpiId);
		kpiElement.setKpiName(kpiName);

		return kpiElement;
	}

	@After
	public void cleanup() {

	}




}