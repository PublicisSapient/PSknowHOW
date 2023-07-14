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
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.SonarHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
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
import com.publicissapient.kpidashboard.common.repository.sonar.SonarHistoryRepository;

/**
 * @author prigupta8
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SonarViolationsServiceImplTest {

	private static Tool tool1;
	private static Tool tool2;
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	SonarViolationsServiceImpl svServiceImpl;
	@Mock
	SonarHistoryRepository sonarHistoryRepository;
	@Mock
	private CustomApiConfig customApiConfig;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private Map<ObjectId, Map<String, List<Tool>>> toolMap = new HashMap<>();
	private Map<String, List<Tool>> toolGroup = new HashMap<>();
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private List<DataCount> vioList;

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

	@After
	public void cleanup() {

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetViolations() throws Exception {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(customApiConfig.getSonarWeekCount()).thenReturn(5);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(), anyLong()))
				.thenReturn(sonarHistoryData);
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.SONAR_VIOLATIONS.name(),
				Arrays.asList("-390", "390-309", "309-221", "221-140", "140-"));
		try {
			KpiElement kpiElement = svServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			List<DataCount> dataList = (List<DataCount>) kpiElement.getValue();
			assertSame((dataList.get(0).getData()), vioList.get(0).getData());
			assertSame((dataList.get(0).getCount()), vioList.get(0).getCount());
		} catch (Exception enfe) {

		}
	}

	/**
	 * agg criteria percentile
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testGetViolations2() throws Exception {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(customApiConfig.getSonarWeekCount()).thenReturn(5);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(), anyLong()))
				.thenReturn(sonarHistoryData);
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.SONAR_VIOLATIONS.name(),
				Arrays.asList("-390", "390-309", "309-221", "221-140", "140-"));
		// set aggregation criteria kpi wise
		kpiWiseAggregation.put(KPICode.SONAR_VIOLATIONS.name(), "percentile");
		try {
			KpiElement kpiElement = svServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			List<DataCount> dataList = (List<DataCount>) kpiElement.getValue();
			assertSame((dataList.get(0).getData()), vioList.get(0).getData());
			assertThat("Count :", dataList.get(0).getCount(), equalTo(2));
		} catch (Exception exception) {

		}
	}

	/**
	 * agg criteria median
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testGetViolations3() throws Exception {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(customApiConfig.getSonarWeekCount()).thenReturn(5);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(), anyLong()))
				.thenReturn(sonarHistoryData);
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.SONAR_VIOLATIONS.name(),
				Arrays.asList("-390", "390-309", "309-221", "221-140", "140-"));
		// set aggregation criteria kpi wise
		kpiWiseAggregation.put(KPICode.SONAR_VIOLATIONS.name(), "median");

		try {
			KpiElement kpiElement = svServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			List<DataCount> dataList = (List<DataCount>) kpiElement.getValue();
			assertSame((dataList.get(0).getData()), vioList.get(0).getData());
			assertThat("Count :", dataList.get(0).getCount(), equalTo(2));
		} catch (Exception enfe) {

		}
	}

	/**
	 * agg criteria average
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testGetViolations4() throws Exception {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(customApiConfig.getSonarWeekCount()).thenReturn(5);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(), anyLong()))
				.thenReturn(sonarHistoryData);
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.SONAR_VIOLATIONS.name(),
				Arrays.asList("-390", "390-309", "309-221", "221-140", "140-"));
		// set aggregation criteria kpi wise
		kpiWiseAggregation.put(KPICode.SONAR_VIOLATIONS.name(), "average");
		String kpiRequestTrackerId = "Excel-Sonar-5be544de025de212549176a9";

		try {
			KpiElement kpiElement = svServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			List<DataCount> dataList = (List<DataCount>) kpiElement.getValue();
			assertSame((dataList.get(0).getData()), vioList.get(0).getData());
			assertSame((dataList.get(0).getCount()), vioList.get(0).getCount());
		} catch (Exception enfe) {

		}
	}

	/**
	 * agg criteria sum
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testGetViolations5() throws Exception {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(customApiConfig.getSonarWeekCount()).thenReturn(5);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(), anyLong()))
				.thenReturn(sonarHistoryData);
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.SONAR_VIOLATIONS.name(),
				Arrays.asList("-390", "390-309", "309-221", "221-140", "140-"));
		// set aggregation criteria kpi wise
		kpiWiseAggregation.put(KPICode.SONAR_VIOLATIONS.name(), "sum");

		try {
			KpiElement kpiElement = svServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			Map<String, Object> dataMap = (Map<String, Object>) kpiElement.getValue();
			assertSame(((List<DataCount>) dataMap.get(Constant.AGGREGATED_VALUE)).get(0).getData(),
					vioList.get(0).getData());
			assertThat("Count :", ((List<DataCount>) dataMap.get(Constant.AGGREGATED_VALUE)).get(0).getCount(),
					equalTo(96));
		} catch (Exception enfe) {

		}
	}

	/**
	 * agg criteria percentile
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testGetViolations6() throws Exception {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(customApiConfig.getSonarWeekCount()).thenReturn(5);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(), anyLong()))
				.thenReturn(sonarHistoryData);
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.SONAR_VIOLATIONS.name(),
				Arrays.asList("-390", "390-309", "309-221", "221-140", "140-"));
		// set aggregation criteria kpi wise
		kpiWiseAggregation.put(KPICode.SONAR_VIOLATIONS.name(), "percentile");

		try {
			KpiElement kpiElement = svServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			List<DataCount> dataList = (List<DataCount>) kpiElement.getValue();
			assertSame((dataList.get(0).getData()), vioList.get(0).getData());
			assertThat("Count :", dataList.get(0).getCount(), equalTo(20));
		} catch (Exception enfe) {

		}
	}

	@Test
	public void testGetViolations7() throws Exception {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(customApiConfig.getSonarWeekCount()).thenReturn(5);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(), anyLong()))
				.thenReturn(sonarHistoryData);
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.SONAR_VIOLATIONS.name(),
				Arrays.asList("-390", "390-309", "309-221", "221-140", "140-"));
		try {
			KpiElement kpiElement = svServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat(((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(0));
		} catch (Exception enfe) {

		}
	}

	@Test
	public void testCalculateKPIMetrics() {
		assertThat(svServiceImpl.calculateKPIMetrics(new HashMap<>()), equalTo(0L));
	}

	@Test
	public void testGetQualifierType() {
		assertThat(svServiceImpl.getQualifierType(), equalTo("SONAR_VIOLATIONS"));
	}

	@Test
	public void getSonarKpiDataTest() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);

		svServiceImpl.getSonarKpiData(projectList, treeAggregatorDetail.getMapTmp(), kpiElement);
	}

}
