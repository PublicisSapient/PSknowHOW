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

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiDataProvider;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.HappinessKpiData;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.UserRatingData;
import com.publicissapient.kpidashboard.common.repository.jira.HappinessKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

@RunWith(MockitoJUnitRunner.class)
public class HappinessIndexServiceImplTest {

	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private static final String SPRINT_DETAILS = "sprintDetails";
	private static final String HAPPINESS_INDEX_DETAILS = "happinessIndexDetails";
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	private SprintRepository sprintRepository;
	@Mock
	private HappinessKpiDataRepository happinessKpiDataRepository;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	KpiDataProvider kpiDataProvider;
	@Mock
	KpiDataCacheService kpiDataCacheService;
	@Mock
	private FilterHelperService filterHelperService;
	private KpiRequest kpiRequest;

	@InjectMocks
	private HappinessIndexServiceImpl happinessIndexImpl;

	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.HAPPINESS_INDEX_RATE.getKpiId());
		kpiRequest.setLabel("PROJECT");

		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectBasicConfig.setIsKanban(true);
		projectBasicConfig.setProjectName("Scrum Project");
		projectBasicConfig.setProjectNodeId("Scrum Project_6335363749794a18e8a4479b");
		projectConfigList.add(projectBasicConfig);

		projectConfigList.forEach(projectConfig -> {
			projectConfigMap.put(projectConfig.getProjectName(), projectConfig);
		});
		Mockito.when(cacheService.cacheProjectConfigMapData()).thenReturn(projectConfigMap);

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);
	}

	@Test
	public void testQualifierType() {
		String kpiName = KPICode.HAPPINESS_INDEX_RATE.name();
		String type = happinessIndexImpl.getQualifierType();
		assertEquals("KPI NAME : ", type, kpiName);
	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<String, Object> subCategoryMap = new HashMap<>();
		Double storyCount = happinessIndexImpl.calculateKPIMetrics(subCategoryMap);
		assertThat("Story List : ", storyCount, equalTo(0.0));
	}

	@Test
	public void getKpiDataEmptyTest() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(HAPPINESS_INDEX_DETAILS, Arrays.asList(new ArrayList<>()));
		resultListMap.put(SPRINT_DETAILS, new ArrayList<>());
		when(kpiDataProvider.fetchHappinessIndexDataFromDb(any())).thenReturn(resultListMap);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";

		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		KpiElement kpiElement = happinessIndexImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail);
		List<DataCount> dataCountList = (List<DataCount>) kpiElement.getTrendValueList();

		assertEquals("Story Count : ", 1, dataCountList.size());
	}

	@Test
	public void getKpiDataSuccessTest() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		SprintDetails sprintDetails = new SprintDetails();
		sprintDetails.setSprintID("38294_Scrum Project_6335363749794a18e8a4479b");
		sprintDetails.setBasicProjectConfigId(new ObjectId("6335363749794a18e8a4479b"));

		HappinessKpiData happinessKpiData = new HappinessKpiData();
		happinessKpiData.setSprintID("38294_Scrum Project_6335363749794a18e8a4479b");
		happinessKpiData.setBasicProjectConfigId(new ObjectId("6335363749794a18e8a4479b"));
		happinessKpiData.setUserRatingList(Arrays.asList(new UserRatingData(2, "uid", "uname")));

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";

		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(HAPPINESS_INDEX_DETAILS, Arrays.asList(happinessKpiData));
		resultListMap.put(SPRINT_DETAILS, Arrays.asList(sprintDetails));
		when(kpiDataProvider.fetchHappinessIndexDataFromDb(any())).thenReturn(resultListMap);
		KpiElement kpiElement = happinessIndexImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail);
		List<DataCount> dataCountList = (List<DataCount>) kpiElement.getTrendValueList();

		assertEquals("Story Count : ", 1, dataCountList.size());
	}
}
