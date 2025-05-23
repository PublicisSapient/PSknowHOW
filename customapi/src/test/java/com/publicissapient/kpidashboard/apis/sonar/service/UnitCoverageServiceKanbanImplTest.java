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

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.data.KanbanIssueCustomHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.SonarHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;
import com.publicissapient.kpidashboard.common.repository.sonar.SonarHistoryRepository;

/**
 * @author shichand0
 */
@RunWith(MockitoJUnitRunner.class)
public class UnitCoverageServiceKanbanImplTest {

	private static Tool tool1;
	private static Tool tool2;
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	UnitCoverageKanbanServiceimpl stdServiceImpl;
	@Mock
	CacheService cacheService;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private SonarHistoryRepository sonarHistoryRepository;
	@Mock
	private CommonService commonService;
	private List<AccountHierarchyDataKanban> ahdList = new ArrayList<>();
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private Map<ObjectId, Map<String, List<Tool>>> toolMap = new HashMap<>();
	private Map<String, List<Tool>> toolGroup = new HashMap<>();
	private Map<String, List<DataCount>> trendValueMap = new HashMap<>();

	private KpiRequest kpiRequest;
	private List<KanbanIssueCustomHistory> jiraIssueCustomHistories = new ArrayList<>();
	private List<SonarHistory> sonarHistoryData = new ArrayList<>();
	private List<AccountHierarchyDataKanban> accountHierarchyDataKanbanList = new ArrayList<>();

	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("/json/default/kanban_kpi_request.json");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi64");
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setDuration("WEEKS");
		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyDataKanbanList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();

		KanbanIssueCustomHistoryDataFactory issueHistoryFactory = KanbanIssueCustomHistoryDataFactory.newInstance();
		jiraIssueCustomHistories = issueHistoryFactory
				.getKanbanIssueCustomHistoryDataListByTypeName(Arrays.asList("Story", "Defect", "Issue"));

		SonarHistoryDataFactory sonarHistoryDataFactory = SonarHistoryDataFactory.newInstance();
		sonarHistoryData = sonarHistoryDataFactory.getSonarHistoryList();

		DateTime date = new DateTime("2018-07-19", DateTimeZone.UTC);
		projectConfigList.forEach(projectConfig -> {
			projectConfigMap.put(projectConfig.getProjectName(), projectConfig);
		});
		Mockito.when(cacheService.cacheProjectConfigMapData()).thenReturn(projectConfigMap);
		fieldMappingList.forEach(fieldMapping -> {
			fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		});

		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);
		setToolMap();
		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		when(cacheService.getFullKanbanHierarchyLevel()).thenReturn(hierachyLevelFactory.getHierarchyLevels());
	}

	private void setToolMap() {
		List<Tool> toolList = new ArrayList<>();

		ProcessorItem processorItem = new ProcessorItem();
		processorItem.setId(new ObjectId("5c0f32fe00a9b83a7cbc4f0c"));

		ProcessorItem processorItem1 = new ProcessorItem();
		processorItem1.setId(new ObjectId("5c0f32fe00a9b83a7cbc4f0d"));

		List<ProcessorItem> processorItems = new ArrayList<>();
		processorItems.add(processorItem);

		List<ProcessorItem> processorItemArrayList = new ArrayList<>();
		processorItemArrayList.add(processorItem1);

		tool1 = createTool("KEY1", "url1", "Sonar", "user1", "pass1", processorItems);
		tool2 = createTool("KEY2", "url2", "Sonar", "user2", "pass2", processorItemArrayList);

		toolList.add(tool1);
		toolList.add(tool2);

		toolGroup.put(Constant.TOOL_SONAR, toolList);

		toolMap.put(new ObjectId("6335368249794a18e8a4479f"), toolGroup);
	}

	private Tool createTool(String key, String url, String toolType, String username, String password,
			List<ProcessorItem> processorItems) {
		Tool tool = new Tool();
		tool.setTool(toolType);
		tool.setUrl(url);
		tool.setProcessorItemList(processorItems);
		return tool;
	}

	@Test
	public void testCalculateKPIMetrics() {
		assertThat(stdServiceImpl.calculateKPIMetrics(null), equalTo(null));
	}

	@Test
	public void testUnitCoverage() throws Exception {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyDataKanbanList, "hierarchyLevelOne", 4);
		String kpiRequestTrackerId = "Excel-Sonar-5be544de025de212549176a9";
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(), anyLong()))
				.thenReturn(sonarHistoryData);

		try {
			KpiElement kpiElement = stdServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			((List<DataCountGroup>) kpiElement.getTrendValueList()).forEach(data -> {
				String projectName = data.getFilter();
				switch (projectName) {
					case "Overall" :
						assertThat("Coverage:", data.getValue().size(), equalTo(1));
						break;

					case "ENGINEERING.KPIDASHBOARD.PROCESSORS->origin/develop->DA_10304" :
						assertThat("Coverage:", data.getValue().size(), equalTo(1));
						break;
				}
			});
		} catch (Exception enfe) {

		}
	}

	@Test
	public void testCoverageEmptyCollectorItem() throws Exception {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyDataKanbanList, "hierarchyLevelOne", 4);
		String kpiRequestTrackerId = "Excel-Sonar-5be544de025de212549176a9";
		// when(customApiConfig.getSonarWeekCount()).thenReturn(5);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);
		// when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY
		// +
		// KPISource.SONARKANBAN.name()))
		// .thenReturn(kpiRequestTrackerId);
		// when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(),
		// anyLong()))
		// .thenReturn(sonarHistoryData);

		try {
			KpiElement kpiElement = stdServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			Long coverage = (Long) ((Map<String, Object>) kpiElement.getValue()).get(Constant.AGGREGATED_VALUE);
			assertThat("Coverage :", coverage, equalTo(null));
		} catch (Exception enfe) {

		}
	}

	@Test
	public void testCoverage1() throws Exception {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyDataKanbanList, "hierarchyLevelOne", 4);
		String kpiRequestTrackerId = "Excel-Sonar-5be544de025de212549176a9";
		// when(customApiConfig.getSonarWeekCount()).thenReturn(5);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);
		// when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY
		// +
		// KPISource.SONARKANBAN.name()))
		// .thenReturn(kpiRequestTrackerId);
		// when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(),
		// anyLong()))
		// .thenReturn(sonarHistoryData);

		try {
			KpiElement kpiElement = stdServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			Long coverage = (Long) ((Map<String, Object>) kpiElement.getValue()).get(Constant.AGGREGATED_VALUE);
			assertNull(coverage);
		} catch (Exception enfe) {

		}
	}

	@Test
	public void getQualifierType() {
		assertThat(KPICode.UNIT_TEST_COVERAGE_KANBAN.name(), equalTo(stdServiceImpl.getQualifierType()));
	}

	@Test()
	public void testCoverageNoData() throws Exception {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyDataKanbanList, "hierarchyLevelOne", 4);
		String kpiRequestTrackerId = "Excel-Sonar-5be544de025de212549176a9";
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);
		when(configHelperService.getToolItemMap()).thenReturn(null);
		stdServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0), treeAggregatorDetail);
	}
}
