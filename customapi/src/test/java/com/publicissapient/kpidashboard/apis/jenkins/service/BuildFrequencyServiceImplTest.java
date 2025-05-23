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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.BuildDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.jira.service.SprintDetailsServiceImpl;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.BuildFrequencyInfo;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

@RunWith(MockitoJUnitRunner.class)
public class BuildFrequencyServiceImplTest {

	Map<String, List<Tool>> toolGroup = new HashMap<>();
	@Mock
	KpiDataCacheService kpiDataCacheService;
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	CustomApiConfig customApiConfig;
	@Mock
	private SprintDetailsServiceImpl sprintDetailsService;
	@InjectMocks
	BuildFrequencyServiceImpl buildFrequencyServiceImpl;
	private Map<String, Object> filterLevelMap;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private Map<ObjectId, Map<String, List<Tool>>> toolMap = new HashMap<>();
	private List<Build> buildList = new ArrayList<>();
	@Mock
	private CommonService commonService;

	private KpiRequest kpiRequest;
	private KpiElement kpiElement;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

	private List<DataCount> trendValues = new ArrayList<>();
	private Map<String, List<DataCount>> trendValueMap = new LinkedHashMap<>();
	private Build build;
	private BuildFrequencyInfo buildFrequencyInfo;

	@Before
	public void setup() {

		setTreadValuesDataCount();

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi172");
		kpiRequest.setLabel("PROJECT");
		kpiElement = kpiRequest.getKpiList().get(0);

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		BuildDataFactory buildDataFactory = BuildDataFactory.newInstance("/json/non-JiraProcessors/build_details.json");
		buildList = buildDataFactory.getbuildDataList();
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
		fieldMappingList.forEach(fieldMapping -> {
			fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		});

		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);
		build = new Build();
		getBuildFrequencyInfo();
		SprintDetails sprintDetails = new SprintDetails();
		sprintDetails.setCompleteDate(LocalDateTime.now().minusDays(1).toString());
		sprintDetails.setSprintID("40345_Scrum Project_6335363749794a18e8a4479b");
		sprintDetails.setBasicProjectConfigId(new ObjectId("6335363749794a18e8a4479b"));
		when(sprintDetailsService.getSprintDetailsByIds(anyList())).thenReturn(Arrays.asList(sprintDetails));
	}

	private void getBuildFrequencyInfo() {
		buildFrequencyInfo = new BuildFrequencyInfo();
		buildFrequencyInfo.addBuildJobNameList("JobFolder");
		buildFrequencyInfo.addWeeks("Week1");
		buildFrequencyInfo.addBuildJobNameList("BuildJob");
	}

	private void setTreadValuesDataCount() {
		DataCount dataCount = setDataCountValues("KnowHow", "3", "4", new DataCount());
		trendValues.add(dataCount);
		trendValueMap.put("OverAll", trendValues);
		trendValueMap.put("UI_Build (KnowHow) ", trendValues);
		trendValueMap.put("API_Build (KnowHow) ", trendValues);
	}

	private DataCount setDataCountValues(String data, String maturity, Object maturityValue, Object value) {
		DataCount dataCount = new DataCount();
		dataCount.setData(data);
		dataCount.setMaturity(maturity);
		dataCount.setMaturityValue(maturityValue);
		dataCount.setValue(value);
		return dataCount;
	}

	@Test
	public void testGetBuildFrquency() throws Exception {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		when(kpiDataCacheService.fetchBuildFrequencyData(any(), any(), any(), any())).thenReturn(buildList);

		try {
			when(customApiConfig.getJenkinsWeekCount()).thenReturn(5);

			KpiElement kpiElement = buildFrequencyServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Build Frequency :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(3));
		} catch (Exception enfe) {
		}
	}

	@Test
	public void testGetBuildFrquencyPort() throws Exception {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		kpiRequest.setLabel("PORT");
		when(kpiDataCacheService.fetchBuildFrequencyData(any(), any(), any(), any())).thenReturn(buildList);

		try {
			when(customApiConfig.getJenkinsWeekCount()).thenReturn(5);

			KpiElement kpiElement = buildFrequencyServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Build Frequency :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(3));
		} catch (Exception enfe) {
		}
	}

	@Test
	public void testGetBuildFrquency2() throws Exception {
		Map<String, Node> mapTmp = new HashMap<>();
		List<Node> leafNodeList = new ArrayList<>();

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				leafNodeList.addAll(v);
			}
		});

		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);

		String kpiRequestTrackerId = "Excel-Jenkins-5be544de025de212549176a9";
		try {
			KpiElement kpiElement = buildFrequencyServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Build Frequency :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(3));
		} catch (Exception enfe) {

		}
	}

	@Test
	public void testBuildFrquencyNegativeScenario() throws Exception {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		LocalDateTime weekDay = LocalDateTime.now();
		while (weekDay.getDayOfWeek() != DayOfWeek.MONDAY) {
			weekDay = weekDay.minusDays(1);
		}
		long weekDayTime = weekDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

		buildList.get(0).setJobFolder("");
		buildList.get(0).setStartTime(weekDayTime);
		buildList.get(1).setStartTime(weekDayTime);
		when(kpiDataCacheService.fetchBuildFrequencyData(any(), any(), any(), any())).thenReturn(buildList);
		String kpiRequestTrackerId = "Excel-Jenkins-5be544de025de212549176a9";
		try {
			when(customApiConfig.getJenkinsWeekCount()).thenReturn(5);
			when(cacheService.getFromApplicationCache(any())).thenReturn(kpiRequestTrackerId);

			KpiElement kpiElement = buildFrequencyServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Build Frequency :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(0));
		} catch (Exception enfe) {
		}
	}

	@Test
	public void testCalculateMaturity() {
		String maturity = buildFrequencyServiceImpl.calculateMaturity(new ArrayList<>(), "", "1");
		assertThat("Total Builds: ", maturity, equalTo(null));
	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<ObjectId, List<Build>> buildList = new HashMap<ObjectId, List<Build>>();
		Map<String, List<Object>> subCategoryMap = new HashMap<>();
		Long count = buildFrequencyServiceImpl.calculateKPIMetrics(subCategoryMap);
	}

	@Test
	public void getQualifierType() {
		Map<String, Object> subCategoryMap = new HashMap<>();
		String qualifierType = buildFrequencyServiceImpl.getQualifierType();
	}

	@Test
	public void testCalculateThresholdValue() {
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setThresholdValueKPI172("8");
		Double result = buildFrequencyServiceImpl.calculateThresholdValue(fieldMapping);
		assertEquals(8.0, result);
	}

	@Test
	public void testCalculateKpiValue() {
		List<Long> valueList = Arrays.asList(10L, 20L, 30L, 40L);
		String kpiId = "kpi172";
		Long result = buildFrequencyServiceImpl.calculateKpiValue(valueList, kpiId);
		assertEquals(40L, result);
	}

	@Test
	public void testGetBuildFrquency_pipelineName_empty() throws Exception {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		buildList.forEach(build -> build.setPipelineName(null));
		when(kpiDataCacheService.fetchBuildFrequencyData(any(), any(), any(), any())).thenReturn(buildList);
		String kpiRequestTrackerId = "Excel-Jenkins-5be544de025de212549176a9";

		try {
			when(customApiConfig.getJenkinsWeekCount()).thenReturn(5);

			KpiElement kpiElement = buildFrequencyServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Build Frequency :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(3));
		} catch (Exception enfe) {
		}
	}
}
