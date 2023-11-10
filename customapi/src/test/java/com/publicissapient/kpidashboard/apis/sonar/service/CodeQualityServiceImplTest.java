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

/**
 *
 */
package com.publicissapient.kpidashboard.apis.sonar.service;

import static com.publicissapient.kpidashboard.common.constant.CommonConstant.HIERARCHY_LEVEL_ID_PROJECT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;
import com.publicissapient.kpidashboard.common.model.sonar.SonarMetric;
import com.publicissapient.kpidashboard.common.repository.sonar.SonarHistoryRepository;

/**
 * @author shi6
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CodeQualityServiceImplTest {

	private static Tool tool1;
	private static Tool tool2;
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	CodeQualityServiceImpl codeQualityService;
	@Mock
	SonarHistoryRepository sonarHistoryRepository;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	CacheService cacheService;
	@Mock
	private CommonService commonService;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private Map<ObjectId, Map<String, List<Tool>>> toolMap = new HashMap<>();
	private Map<String, List<Tool>> toolGroup = new HashMap<>();
	private KpiRequest kpiRequest;
	private KpiElement kpiElement;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

	private List<SonarHistory> sonarHistoryData = new ArrayList<>();

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi38");
		kpiRequest.setLabel("PROJECT");
		kpiElement = kpiRequest.getKpiList().get(0);

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		SonarHistoryDataFactory sonarHistoryDataFactory = SonarHistoryDataFactory.newInstance();
		sonarHistoryData = sonarHistoryDataFactory.getSonarHistoryList();

		projectConfigList.forEach(projectConfig -> {
			projectConfigMap.put(projectConfig.getProjectName(), projectConfig);
		});

		fieldMappingList.forEach(fieldMapping -> {
			fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		});

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
			List<ProcessorItem> collectorItemList) {
		Tool tool = new Tool();
		tool.setTool(toolType);
		tool.setUrl(url);
		tool.setProcessorItemList(collectorItemList);
		return tool;
	}

	@Test
	public void testGetSonarKpiData_NonEmptyInputs() throws ApplicationException {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> pList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);
		when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(), anyLong()))
				.thenReturn(sonarHistoryData);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(customApiConfig.getSonarMonthCount()).thenReturn(5);
		String kpiRequestTrackerId = "Jira-Excel-QADD-track001";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.SONAR.name()))
				.thenReturn(kpiRequestTrackerId);
		codeQualityService.getSonarKpiData(pList, treeAggregatorDetail.getMapTmp(), kpiElement);
		assertFalse(kpiElement.getExcelData().isEmpty());
		assertFalse(kpiElement.getExcelColumns().isEmpty());
	}

	@Test
	public void testGetSonarKpiData_EmptyInputs() throws ApplicationException {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> pList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);
		codeQualityService.getSonarKpiData(pList, treeAggregatorDetail.getMapTmp(), kpiElement);
		assertFalse(kpiElement.getExcelColumns().isEmpty());
	}

	@Test
	public void testPrepareEmptyJobWiseHistoryMap_EmptyInput() {
		List<SonarHistory> sonarHistoryList = new ArrayList<>();
		Long end = System.currentTimeMillis();
		Map<String, SonarHistory> historyMap = codeQualityService.prepareEmptyJobWiseHistoryMap(sonarHistoryList, end);
		assertTrue(historyMap.isEmpty());
	}

	@Test
	public void testPrepareEmptyJobWiseHistoryMap_DifferentMetricValues() {
		Long end = System.currentTimeMillis();
		Map<String, SonarHistory> historyMap = codeQualityService.prepareEmptyJobWiseHistoryMap(sonarHistoryData, end);
		List<SonarHistory> values = new ArrayList<>(historyMap.values());
		List<SonarMetric> collect = values.stream().flatMap(sonarHistory -> sonarHistory.getMetrics().stream())
				.collect(Collectors.toList());
		assertEquals(collect.get(0).getMetricName(), "sqale_rating");
	}

	@Test
	public void testGetKpiData_AggregatedValuesWithoutData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(new HashMap<>());
		KpiElement result = codeQualityService.getKpiData(kpiRequest, kpiElement, treeAggregatorDetail);
		assertTrue(((List<DataCount>) result.getTrendValueList()).size() == 0);
	}

	@Test
	public void testGetKpiData_AggregatedValuesWithData() throws ApplicationException {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		Map<String, List<DataCount>> trendMap = createTrendValue();
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendMap);
		List<Node> pList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);
		when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(), anyLong()))
				.thenReturn(sonarHistoryData);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(customApiConfig.getSonarMonthCount()).thenReturn(5);
		String kpiRequestTrackerId = "Jira-Excel-QADD-track001";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.SONAR.name()))
				.thenReturn(kpiRequestTrackerId);

		KpiElement result = codeQualityService.getKpiData(kpiRequest, kpiElement, treeAggregatorDetail);
		assertTrue(((List<DataCount>) result.getTrendValueList()).size() > 0);
	}

	@Test
	public void testGetSqualeRatingValue_DoubleInput() {
		// Test when the input is a Double
		Double input = 42.5;
		// Create an instance of your class containing the method
		Long result = codeQualityService.getSqualeRatingValue(input);
		assertEquals(Long.valueOf(42), result); // Expecting the double to be converted to Long
	}

	@Test
	public void testGetSqualeRatingValue_StringInput() {
		// Test when the input is a String representing a double
		String input = "42.5";
		// Create an instance of your class containing the method
		Long result = codeQualityService.getSqualeRatingValue(input);
		assertEquals(Long.valueOf(42), result); // Expecting the string to be converted to Long
	}

	// Test when the input is already a Long
	@Test
	public void testGetSqualeRatingValue_LongInput() {
		Long input = 42L;
		Long result = codeQualityService.getSqualeRatingValue(input);
		assertEquals(Long.valueOf(42), result); // Expecting the Long to remain the same
	}

	@Test
	public void testGetSqualeRatingValue_NullInput() {
		Object input = null;
		Long result = codeQualityService.getSqualeRatingValue(input);
		assertEquals(Long.valueOf(0), result);
	}

	@Test(expected = ClassCastException.class)
	public void testGetSqualeRatingValue_InvalidInput() {
		// Test when the input is of an unsupported type
		Object input = new Object(); // An unsupported type
		// Create an instance of your class containing the method
		Long result = codeQualityService.getSqualeRatingValue(input); // This should throw a ClassCastException
	}

	@Test
	public void testGetQualifier() {
		assertEquals(codeQualityService.getQualifierType(), KPICode.SONAR_CODE_QUALITY.name());
	}

	private Map<String, List<DataCount>> createTrendValue() {
		Map<String, List<DataCount>> trendMap = new HashMap<>();
		List<DataCount> dataCountList = new ArrayList<>();
		DataCount d1 = new DataCount();
		d1.setData("ABC");
		List<DataCount> dataCountValueList = new ArrayList<>();
		dataCountValueList.add(createDataCount());
		dataCountValueList.add(createDataCount());
		dataCountValueList.add(createDataCount());
		dataCountValueList.add(createDataCount());
		dataCountValueList.add(createDataCount());
		d1.setValue(dataCountValueList);
		d1.setMaturity("5");
		d1.setMaturityValue(1);
		dataCountList.add(d1);
		trendMap.put("Overall", dataCountList);
		trendMap.put("ENGINEERING.KPIDASHBOARD.PROCESSORS->master->KnowHOW", dataCountList);
		return trendMap;
	}

	private DataCount createDataCount() {
		DataCount dataCount = new DataCount();
		dataCount.setData("1");
		dataCount.setValue(1L);
		dataCount.setValue(1L);
		dataCount.setDate("Date");
		return dataCount;
	}

}