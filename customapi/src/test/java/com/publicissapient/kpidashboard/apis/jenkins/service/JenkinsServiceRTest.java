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
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

		KpiRequest kpiRequestJenkins = createKpiRequest(2, "Jenkins");

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

		when(filterHelperService.getHierarachyLevelId(5, "project", false)).thenReturn("project");

		buildKpiElement = setKpiElement(KPICode.CODE_BUILD_TIME.getKpiId(), "CODE_BUILD_TIME");
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

	@Test
	public void testProcess() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(2, "Jenkins");

		@SuppressWarnings("rawtypes")
		JenkinsKPIService mcokAbstract = codeBuildTimeServiceImpl;
		jenkinsServiceCache.put(KPICode.CODE_BUILD_TIME.name(), mcokAbstract);

		try (MockedStatic<JenkinsKPIServiceFactory> utilities = Mockito.mockStatic(JenkinsKPIServiceFactory.class)) {
			utilities.when((Verification) JenkinsKPIServiceFactory.getJenkinsKPIService(KPICode.CODE_BUILD_TIME.name()))
					.thenReturn(mcokAbstract);
		}

		when(authorizedProjectsService.getProjectKey(accountHierarchyDataList, kpiRequest)).thenReturn(projectKey);
		when(authorizedProjectsService.getProjectNodesForRequest(accountHierarchyDataList)).thenReturn(projects);

		when(mcokAbstract.getKpiData(any(), any(), any())).thenReturn(buildKpiElement);

		List<KpiElement> resultList = jenkinsServiceR.process(kpiRequest);

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {

			case CODE_BUILD_TIME:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("CODE_BUILD_TIME"));
				break;

			default:
				break;
			}

		});

	}

	// level = sprint
	@Test
	public void testProcess1() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(5, "Jenkins");

		@SuppressWarnings("rawtypes")
		JenkinsKPIService mcokAbstract = codeBuildTimeServiceImpl;
		jenkinsServiceCache.put(KPICode.CODE_BUILD_TIME.name(), mcokAbstract);

		try (MockedStatic<JenkinsKPIServiceFactory> utilities = Mockito.mockStatic(JenkinsKPIServiceFactory.class)) {
			utilities.when((Verification) JenkinsKPIServiceFactory.getJenkinsKPIService(KPICode.CODE_BUILD_TIME.name()))
					.thenReturn(mcokAbstract);
		}

		when(mcokAbstract.getKpiData(any(), any(), any())).thenReturn(buildKpiElement);

		List<KpiElement> resultList = jenkinsServiceR.process(kpiRequest);

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {

			case CODE_BUILD_TIME:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("CODE_BUILD_TIME"));
				break;

			default:
				break;
			}

		});

	}

	@SuppressWarnings("unchecked")
	@Test(expected = HttpMessageNotWritableException.class)
	public void testProcessException() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(5, "Jenkins");

		@SuppressWarnings("rawtypes")
		JenkinsKPIService mcokAbstract = codeBuildTimeServiceImpl;
		jenkinsServiceCache.put(KPICode.CODE_BUILD_TIME.name(), mcokAbstract);

		try (MockedStatic<JenkinsKPIServiceFactory> utilities = Mockito.mockStatic(JenkinsKPIServiceFactory.class)) {
			utilities.when((Verification) JenkinsKPIServiceFactory.getJenkinsKPIService(KPICode.CODE_BUILD_TIME.name()))
					.thenReturn(mcokAbstract);
		}

		when(filterHelperService.getFilteredBuilds(any(), Mockito.anyString()))
				.thenThrow(HttpMessageNotWritableException.class);
		jenkinsServiceR.process(kpiRequest);

	}

	@Test
	public void testProcessCachedData() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(2, "Jenkins");

		when(cacheService.getFromApplicationCache(any(), any(), any(), ArgumentMatchers.anyList()))
				.thenReturn(new ArrayList<>());

		List<KpiElement> resultList = jenkinsServiceR.process(kpiRequest);
		assertThat("Kpi list :", resultList.size(), equalTo(1));

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

}