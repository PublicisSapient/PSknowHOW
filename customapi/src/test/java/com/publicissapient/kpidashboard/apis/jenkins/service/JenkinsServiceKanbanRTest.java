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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jenkins.factory.JenkinsKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsServiceKanbanRTest {

	private static String GROUP_PROJECT = "project";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	FilterHelperService filterHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@InjectMocks
	private JenkinsServiceKanbanR jenkinsServiceKanbanR;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private CacheService cacheService;
	@Mock
	private CodeBuildTimeKanbanServiceImpl codeBuildTimeKanbanServiceImpl;
	@Mock
	private UserAuthorizedProjectsService authorizedProjectsService;
	@SuppressWarnings("rawtypes")
	@Mock
	private List<JenkinsKPIService> services;
	private List<AccountHierarchyDataKanban> accountHierarchyDataKanbanList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private String[] projectKey;
	private Set<String> projects;
	private KpiElement buildKpiElement;
	private Map<String, JenkinsKPIService<?, ?, ?>> jenkinsServiceCache = new HashMap<>();
	@Mock
	private JenkinsKPIServiceFactory jenkinsKPIServiceFactory;

	@Before
	public void setup() throws EntityNotFoundException {

		jenkinsKPIServiceFactory.initMyServiceCache();

		KpiRequest kpiRequestJenkins = createKpiRequest(2, "Jenkins");

		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyDataKanbanList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();

		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335368249794a18e8a4479f"));
		projectConfig.setProjectName("Kanban Project");
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/kanban/kanban_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);

		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		List<HierarchyLevel> hierarchyLevels = hierachyLevelFactory.getHierarchyLevels();
		Map<String, Integer> map = new HashMap<>();
		Map<String, HierarchyLevel> hierarchyMap = hierarchyLevels.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
		hierarchyMap.entrySet().stream().forEach(k -> map.put(k.getKey(), k.getValue().getLevel()));
		when(filterHelperService.getHierarchyIdLevelMap(anyBoolean())).thenReturn(map);

		when(filterHelperService.getHierarachyLevelId(4, "project", true)).thenReturn("project");
		when(authorizedProjectsService.filterKanbanProjects(accountHierarchyDataKanbanList)).thenReturn(accountHierarchyDataKanbanList);

		buildKpiElement = setKpiElement(KPICode.CODE_BUILD_TIME_KANBAN.getKpiId(), "CODE_BUILD_TIME_KANBAN");

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
		JenkinsKPIService mcokAbstract = codeBuildTimeKanbanServiceImpl;
		jenkinsServiceCache.put(KPICode.CODE_BUILD_TIME_KANBAN.name(), mcokAbstract);

		try (MockedStatic<JenkinsKPIServiceFactory> utilities = Mockito.mockStatic(JenkinsKPIServiceFactory.class)) {
			utilities.when((MockedStatic.Verification) JenkinsKPIServiceFactory
					.getJenkinsKPIService(KPICode.CODE_BUILD_TIME_KANBAN.name())).thenReturn(mcokAbstract);
		}

		List<KpiElement> resultList = jenkinsServiceKanbanR.process(kpiRequest);

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {

			case CODE_BUILD_TIME_KANBAN:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("CODE_BUILD_TIME_KANBAN"));
				break;

			default:
				break;
			}

		});

	}

	@Test
	public void testProcess1() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(4, "Jenkins");

		@SuppressWarnings("rawtypes")
		JenkinsKPIService mcokAbstract = codeBuildTimeKanbanServiceImpl;
		jenkinsServiceCache.put(KPICode.CODE_BUILD_TIME_KANBAN.name(), mcokAbstract);

		when(filterHelperService.getFilteredBuildsKanban(kpiRequest, GROUP_PROJECT))
				.thenReturn(accountHierarchyDataKanbanList);
		when(authorizedProjectsService.getKanbanProjectKey(accountHierarchyDataKanbanList, kpiRequest))
				.thenReturn(projectKey);

		List<KpiElement> resultList;
		try (MockedStatic<JenkinsKPIServiceFactory> mockedStatic = mockStatic(JenkinsKPIServiceFactory.class)) {
			CodeBuildTimeKanbanServiceImpl mockService = mock(CodeBuildTimeKanbanServiceImpl.class);
			when(mockService.getKpiData(any(), any(), any())).thenReturn(buildKpiElement);
			mockedStatic.when(() -> JenkinsKPIServiceFactory.getJenkinsKPIService(eq(KPICode.CODE_BUILD_TIME_KANBAN.name())))
					.thenReturn(mockService);
			resultList = jenkinsServiceKanbanR.process(kpiRequest);
			mockedStatic.verify(() -> JenkinsKPIServiceFactory.getJenkinsKPIService(eq(KPICode.CODE_BUILD_TIME_KANBAN.name())));
		}

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {

			case CODE_BUILD_TIME_KANBAN:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("CODE_BUILD_TIME_KANBAN"));
				break;

			default:
				break;
			}

		});

	}

	@Test
	public void testProcessCachedData() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(4, "Jenkins");
		when(filterHelperService.getFilteredBuildsKanban(kpiRequest, GROUP_PROJECT))
				.thenReturn(accountHierarchyDataKanbanList);
		when(cacheService.getFromApplicationCache(Mockito.any(), Mockito.any(), Mockito.any(),
				any())).thenReturn(new ArrayList<>());

		List<KpiElement> resultList = jenkinsServiceKanbanR.process(kpiRequest);
		assertThat("Kpi list :", resultList.size(), equalTo(0));

	}

	private KpiRequest createKpiRequest(int level, String source) {
		KpiRequest kpiRequest = new KpiRequest();
		List<KpiElement> kpiList = new ArrayList<>();

		addKpiElement(kpiList, KPICode.CODE_BUILD_TIME_KANBAN.getKpiId(), KPICode.CODE_BUILD_TIME_KANBAN.name(),
				"Productivity", "mins", source);
		kpiRequest.setLevel(level);
		kpiRequest.setLabel("project");
		kpiRequest.setIds(new String[] { "Kanban Project_6335368249794a18e8a4479f" });
		kpiRequest.setKpiList(kpiList);
		Map<String, List<String>> selectedMap = new HashMap<>();
		selectedMap.put("Project", Arrays.asList("Kanban Project_6335368249794a18e8a4479f"));
		kpiRequest.setSelectedMap(selectedMap);
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