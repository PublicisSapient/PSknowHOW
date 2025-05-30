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

/** */
package com.publicissapient.kpidashboard.apis.sonar.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.apis.jira.service.SprintDetailsServiceImpl;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import org.bson.types.ObjectId;
import org.junit.After;
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
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.SonarHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;
import com.publicissapient.kpidashboard.common.repository.sonar.SonarHistoryRepository;

/**
 * @author prigupta8
 */
@RunWith(MockitoJUnitRunner.class)
public class SonarTechDebtServiceImplTest {

	private static Tool tool1;
	private static Tool tool2;
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private Map<String, List<DataCount>> trendValueMap = new HashMap<>();
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	SonarTechDebtServiceImpl stdServiceImpl;
	@Mock
	SonarHistoryRepository sonarHistoryRepository;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	CacheService cacheService;
	@Mock
	private CommonService commonService;
	@Mock
	private SprintDetailsServiceImpl sprintDetailsService;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private Map<ObjectId, Map<String, List<Tool>>> toolMap = new HashMap<>();
	private Map<String, List<Tool>> toolGroup = new HashMap<>();
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private List<DataCount> trendValues = new ArrayList<>();
	private KpiRequest kpiRequest;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

	private List<SonarHistory> sonarHistoryData = new ArrayList<>();

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi27");
		kpiRequest.setLabel("PROJECT");

		String kpiRequestTrackerId = "Excel-Sonar-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.SONAR.name()))
				.thenReturn(kpiRequestTrackerId);

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		SonarHistoryDataFactory sonarHistoryDataFactory = SonarHistoryDataFactory.newInstance();
		sonarHistoryData = sonarHistoryDataFactory.getSonarHistoryList();

		setMockFieldMapping();

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

		projectConfigList.add(projectBasicConfig);

		projectConfigList.forEach(projectConfigs -> {
			projectConfigMap.put(projectConfigs.getProjectName(), projectConfigs);
		});
		Mockito.when(cacheService.cacheProjectConfigMapData()).thenReturn(projectConfigMap);

		List<DataCount> dataCountList = new ArrayList<>();
		dataCountList.add(createDataCount("2022-07-26", 0l));
		dataCountList.add(createDataCount("2022-07-27", 35l));
		dataCountList.add(createDataCount("2022-07-28", 44l));
		dataCountList.add(createDataCount("2022-07-29", 0l));
		dataCountList.add(createDataCount("2022-07-30", 0l));
		dataCountList.add(createDataCount("2022-07-31", 12l));
		dataCountList.add(createDataCount("2022-08-01", 0l));
		DataCount dataCount = createDataCount(null, 0l);
		dataCount.setData("");
		dataCount.setValue(dataCountList);
		trendValues.add(dataCount);
		trendValueMap.put("Overall", trendValues);
		trendValueMap.put("BRANCH1->PR_10304", trendValues);

		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);
		SprintDetails sprintDetails = new SprintDetails();
		sprintDetails.setCompleteDate("2025-04-01T13:44:44.421Z");
		sprintDetails.setSprintID("40345_Scrum Project_6335363749794a18e8a4479b");
		when(sprintDetailsService.getSprintDetailsByIds(anyList())).thenReturn(Arrays.asList(sprintDetails));
	}

	private void setToolMap() {
		List<Tool> toolList = new ArrayList<>();

		ProcessorItem processorItem = new ProcessorItem();
		processorItem.setId(new ObjectId("5c0f32fe00a9b83a7cbc4f0c"));

		ProcessorItem processorItem1 = new ProcessorItem();
		processorItem1.setId(new ObjectId("5c0f32fe00a9b83a7cbc4f0d"));

		List<ProcessorItem> collectorItemFirstList = new ArrayList<>();
		collectorItemFirstList.add(processorItem);

		List<ProcessorItem> collectorItemSecondList = new ArrayList<>();
		collectorItemSecondList.add(processorItem1);

		tool1 = createTool("KEY1", "url1", "Sonar", "user1", "pass1", collectorItemFirstList);
		tool2 = createTool("KEY2", "url2", "Sonar", "user2", "pass2", collectorItemSecondList);

		toolList.add(tool1);
		toolList.add(tool2);

		toolGroup.put(Constant.TOOL_SONAR, toolList);

		toolMap.put(new ObjectId("6335363749794a18e8a4479b"), toolGroup);
	}

	private Tool createTool(String key, String url, String toolType, String username, String password,
			List<ProcessorItem> processorItems) {
		Tool tool = new Tool();
		// tool.setKey(key);
		tool.setTool(toolType);
		tool.setUrl(url);
		// tool.setUsername(username);
		// tool.setPassword(password);
		tool.setProcessorItemList(processorItems);
		return tool;
	}

	private void setMockFieldMapping() {

		FieldMapping projectOne = new FieldMapping();
		projectOne.setBasicProjectConfigId(new ObjectId("5b674d58f47cae8935b1b26f"));

		FieldMapping projectTwo = new FieldMapping();
		projectTwo.setBasicProjectConfigId(new ObjectId("5b719d06a500d00814bfb2b9"));

		FieldMapping projectThree = new FieldMapping();
		projectThree.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));

		fieldMappingList.add(projectOne);
		fieldMappingList.add(projectTwo);
		fieldMappingList.add(projectThree);
	}

	@After
	public void cleanup() {
	}

	@Test
	public void testGetTechDebt() throws Exception {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(customApiConfig.getSonarWeekCount()).thenReturn(5);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(), anyLong()))
				.thenReturn(sonarHistoryData);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.SONAR_TECH_DEBT.name(), Arrays.asList("-80", "80-60", "60-40", "40-20", "20-"));

		// set aggregation criteria kpi wise
		kpiWiseAggregation.put(KPICode.SONAR_TECH_DEBT.name(), "average");
		try {
			KpiElement kpiElement = stdServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Tech Debt :", ((List<DataCount>) kpiElement.getTrendValueList()).get(0).getData(), equalTo(4l));
		} catch (Exception enfec) {

		}
	}

	/**
	 * agg criteria sum
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTechDebt2() throws Exception {

		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(customApiConfig.getSonarWeekCount()).thenReturn(5);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(), anyLong()))
				.thenReturn(sonarHistoryData);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.SONAR_TECH_DEBT.name(), Arrays.asList("-80", "80-60", "60-40", "40-20", "20-"));
		// set aggregation criteria kpi wise
		kpiWiseAggregation.put("sonar", "sum");
		try {
			KpiElement kpiElement = stdServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			Long techDebt = (Long) ((Map<String, Object>) kpiElement.getValue()).get(Constant.AGGREGATED_VALUE);
			assertThat("Tech Debt :", techDebt, equalTo(4l));
		} catch (Exception enfe) {

		}
	}

	/**
	 * agg criteria median
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTechDebt3() throws Exception {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(customApiConfig.getSonarWeekCount()).thenReturn(5);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(), anyLong()))
				.thenReturn(sonarHistoryData);
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.SONAR_TECH_DEBT.name(), Arrays.asList("-80", "80-60", "60-40", "40-20", "20-"));
		// set aggregation criteria kpi wise
		kpiWiseAggregation.put(KPICode.SONAR_TECH_DEBT.name(), "median");
		try {
			KpiElement kpiElement = stdServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			Long techDebt = (Long) ((Map<String, Object>) kpiElement.getValue()).get(Constant.AGGREGATED_VALUE);
			assertThat("Tech Debt :", techDebt, equalTo(4l));
		} catch (Exception exception) {

		}
	}

	/**
	 * agg criteria percentile
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTechDebt4() throws Exception {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(customApiConfig.getSonarWeekCount()).thenReturn(5);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(), anyLong()))
				.thenReturn(sonarHistoryData);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.SONAR_TECH_DEBT.name(), Arrays.asList("-80", "80-60", "60-40", "40-20", "20-"));
		// set aggregation criteria kpi wise
		kpiWiseAggregation.put(KPICode.SONAR_TECH_DEBT.name(), "percentile");
		try {
			KpiElement kpiElement = stdServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			Long techDebt = (Long) ((Map<String, Object>) kpiElement.getValue()).get(Constant.AGGREGATED_VALUE);
			assertThat("Tech Debt :", techDebt, equalTo(4l));
		} catch (Exception enfe) {

		}
	}

	@Test
	public void testGetTechDebtSpeedyPercentileNull() throws Exception {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(customApiConfig.getSonarWeekCount()).thenReturn(5);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(), anyLong()))
				.thenReturn(sonarHistoryData);
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.SONAR_TECH_DEBT.name(), Arrays.asList("-80", "80-60", "60-40", "40-20", "20-"));
		// set aggregation criteria kpi wise
		kpiWiseAggregation.put(KPICode.SONAR_TECH_DEBT.name(), "percentile");

		try {
			KpiElement kpiElement = stdServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			Long techDebt = (Long) ((Map<String, Object>) kpiElement.getValue()).get(Constant.AGGREGATED_VALUE);
			assertThat("Tech Debt :", techDebt, equalTo(4l));
		} catch (Exception enfe) {

		}
	}

	@Test
	public void testGetTechDebtOther() throws Exception {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(customApiConfig.getSonarWeekCount()).thenReturn(5);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(), anyLong()))
				.thenReturn(sonarHistoryData);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.SONAR_TECH_DEBT.name(), Arrays.asList("-80", "80-60", "60-40", "40-20", "20-"));
		// set aggregation criteria kpi wise
		kpiWiseAggregation.put(KPICode.SONAR_TECH_DEBT.name(), "other");
		try {
			KpiElement kpiElement = stdServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			Long techDebt = (Long) ((Map<String, Object>) kpiElement.getValue()).get(Constant.AGGREGATED_VALUE);
			assertThat("Tech Debt :", techDebt, equalTo(4l));
		} catch (Exception exception) {

		}
	}

	@Test
	public void testGetTechDebt5() throws Exception {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.SONAR_TECH_DEBT.name(), Arrays.asList("-80", "80-60", "60-40", "40-20", "20-"));
		when(customApiConfig.getSonarWeekCount()).thenReturn(5);
		kpiRequest.setLabel("PORT");
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(), anyLong()))
				.thenReturn(sonarHistoryData);
		// set aggregation criteria kpi wise
		kpiWiseAggregation.put(KPICode.SONAR_TECH_DEBT.name(), "average");

		try {
			KpiElement kpiElement = stdServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			Long techDebt = (Long) ((Map<String, Object>) kpiElement.getValue()).get(Constant.AGGREGATED_VALUE);
			assertNull(techDebt);
		} catch (Exception enfe) {

		}
	}

	@Test
	public void testGetQualifierType() {
		String expectedValue = KPICode.SONAR_TECH_DEBT.name();
		String actualValue = stdServiceImpl.getQualifierType();
		assertEquals(expectedValue, actualValue);
	}

	@Test
	public void testCalculateKPIMetrics() {
		assertEquals(null, stdServiceImpl.calculateKPIMetrics(new HashMap<>()));
	}

	@Test
	public void testGetTechDebtValueWithDouble() {
		assertEquals(Long.valueOf(42), stdServiceImpl.getTechDebtValue(42.0));
		assertEquals(Long.valueOf(123), stdServiceImpl.getTechDebtValue("123"));
		assertEquals(Long.valueOf(456), stdServiceImpl.getTechDebtValue(456L));
		assertEquals(Long.valueOf(-1), stdServiceImpl.getTechDebtValue(null));
	}

	@Test
	public void testThresold() {
		assertEquals(Double.valueOf(0), stdServiceImpl.calculateThresholdValue(new FieldMapping()));
	}

	private DataCount createDataCount(String date, Long data) {
		DataCount dataCount = new DataCount();
		dataCount.setData(data.toString());
		dataCount.setSProjectName("PR_10304");
		dataCount.setDate(date);
		dataCount.setHoverValue(new HashMap<>());
		dataCount.setValue(Long.valueOf(data));
		return dataCount;
	}
}
