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

package com.publicissapient.kpidashboard.apis.zephyr.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.zephyr.factory.ZephyrKPIServiceFactory;
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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author pkum34
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class ZephyrServiceKanbanTest {
	private static final String TESTZEPHYR = "TEST_ZEPHYR";

	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();

	@Mock
	ConfigHelperService configHelperService;
	@Mock
	FilterHelperService filterHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@InjectMocks
	private ZephyrServiceKanban zephyrService;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private CacheService cacheService;
	@Mock
	private UserAuthorizedProjectsService authorizedProjectsService;
	@Mock
	private RegressionPercentageKanbanServiceImpl regressionPercentageKanbanServiceImpl;
	@SuppressWarnings("rawtypes")
	@Mock
	private List<ZephyrKPIService> services;
	private List<AccountHierarchyDataKanban> accountHierarchyDataKanbanList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private String[] projectKey;
	private Set<String> projects;
	private ZephyrKPIServiceFactory zephyrKPIServiceFactory;

	@Mock
	private TestService service;
	private KpiRequest kpiRequest;
	private List<AccountHierarchyDataKanban> accountHierarchyKanbanDataList = new ArrayList<>();
	private List<HierarchyLevel> hierarchyLevels = new ArrayList<>();
	String[] exampleStringList;


	@Before
	public void setup() throws ApplicationException {
		MockitoAnnotations.initMocks(this);
		List<ZephyrKPIService<?, ?, ?>> mockServices = Arrays.asList(service);
		zephyrKPIServiceFactory = ZephyrKPIServiceFactory.builder().services(mockServices).build();
		doReturn(TESTZEPHYR).when(service).getQualifierType();
		doReturn(new KpiElement()).when(service).getKpiData(any(), any(), any());
		zephyrKPIServiceFactory.initMyServiceCache();

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("/json/default/kanban_kpi_request.json");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi63");
		createKpiRequest("SONAR", 3, kpiRequest);
		kpiRequest.setLabel("PROJECT");


		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyKanbanDataList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();


		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		hierarchyLevels = hierachyLevelFactory.getHierarchyLevels();
		Map<String, HierarchyLevel> hierarchyMap = hierarchyLevels.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
		Map<String, Integer> map = new HashMap<>();
		hierarchyMap.entrySet().stream().forEach(k -> map.put(k.getKey(), k.getValue().getLevel()));
		when(filterHelperService.getHierarchyIdLevelMap(true)).thenReturn(map);
		exampleStringList = new String[]{"exampleElement", "exampleElement"};




	}

	@Test
	public void sonarViolationsTestProcess_cache() throws Exception {

		when(filterHelperService.getHierarachyLevelId(Mockito.anyInt(), anyString(), Mockito.anyBoolean()))
				.thenReturn("project");
		when(cacheService.getFromApplicationCache(eq(exampleStringList), eq(KPISource.ZEPHYRKANBAN.name()), eq(1), isNull()))
				.thenReturn(new ArrayList<KpiElement>());
		when(filterHelperService.getFilteredBuildsKanban(ArgumentMatchers.any(), Mockito.anyString()))
				.thenReturn(accountHierarchyKanbanDataList);
		when(authorizedProjectsService.getKanbanProjectKey(accountHierarchyKanbanDataList, kpiRequest))
				.thenReturn(exampleStringList);
		when(authorizedProjectsService.filterKanbanProjects(accountHierarchyKanbanDataList))
				.thenReturn(accountHierarchyKanbanDataList);

		try {
			List<KpiElement> resultList = zephyrService.process(kpiRequest);
		} catch (Exception e) {

		}

	}

	@Test
	public void sonarViolationsTestProcess() throws Exception {

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
			List<KpiElement> resultList = zephyrService.process(kpiRequest);
		} catch (Exception e) {

		}

	}

	private void createKpiRequest(String source, int level, KpiRequest kpiRequest) {
		List<KpiElement> kpiList = new ArrayList<>();
		addKpiElement(kpiList, TESTZEPHYR, TESTZEPHYR, "TechDebt", "", source);
		kpiRequest.setLevel(level);
		kpiRequest.setIds(new String[] { "Scrum Project_6335363749794a18e8a4479b" });
		kpiRequest.setKpiList(kpiList);
		kpiRequest.setRequestTrackerId();
		Map<String, List<String>> selectedMap = new HashMap<>();
		selectedMap.put("Project", Arrays.asList("Kanban Project_6335368249794a18e8a4479f"));
		selectedMap.put(CommonConstant.date, Arrays.asList("10"));
		kpiRequest.setSelectedMap(selectedMap);

	}

	private void addKpiElement(List<KpiElement> kpiList, String kpiId, String kpiName, String category, String kpiUnit,
			String source) {
		KpiElement kpiElement = new KpiElement();
		kpiElement.setKpiId(kpiId);
		kpiElement.setKpiName(kpiName);
		kpiElement.setKpiCategory(category);
		kpiElement.setKpiUnit(kpiUnit);
		kpiElement.setKpiSource(source);
		kpiElement.setGroupId(1);
		kpiElement.setMaxValue("500");
		kpiElement.setChartType("gaugeChart");
		kpiList.add(kpiElement);
	}
}