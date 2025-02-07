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
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
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
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
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
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.class)
public class CostOfDelayServiceImplTest {
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@InjectMocks
	CostOfDelayServiceImpl costOfDelayServiceImpl;
	@Mock
	ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	FieldMappingRepository fieldMappingRepository;
	@Mock
	CustomApiConfig customApiSetting;
	@Mock
	FilterHelperService filterHelperService;
	@Mock
	private KpiDataCacheService kpiDataCacheService;

	private Map<String, Object> filterLevelMap;
	private List<JiraIssue> codList = new ArrayList<>();
	private List<JiraIssueCustomHistory> codHistoryList = new ArrayList<>();
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private KpiRequest kpiRequest;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

	private static final String COD_DATA = "costOfDelayData";
	private static final String COD_DATA_HISTORY = "costOfDelayDataHistory";
	private static final String FIELD_MAPPING = "fieldMapping";

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi113");
		kpiRequest.setLabel("PROJECT");

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);

		kpiWiseAggregation.put("cost_Of_Delay", "sum");

		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectConfig.setProjectName("Scrum Project");
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory.newInstance();
		codList = jiraIssueDataFactory.getJiraIssues();
		codHistoryList = jiraIssueHistoryDataFactory.getJiraIssueCustomHistory();
		codHistoryList.stream().map(JiraIssueCustomHistory::getStatusUpdationLog).forEach(f -> {
			f.forEach(g -> g.setUpdatedOn(LocalDateTime.now().minusDays(2)));
		});

	}

	@After
	public void cleanup() {

	}

	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);
		String startDate = leafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = leafNodeList.get(leafNodeList.size() - 1).getSprintFilter().getEndDate();

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(COD_DATA, codList);
		resultListMap.put(COD_DATA_HISTORY, codHistoryList);
		resultListMap.put(FIELD_MAPPING, new HashMap<>());
		when(kpiDataCacheService.fetchCostOfDelayData(Mockito.any(), Mockito.any())).thenReturn(resultListMap);

		Map<String, Object> dataList = costOfDelayServiceImpl.fetchKPIDataFromDb(leafNodeList, startDate, endDate,
				kpiRequest);
		assertThat("Total Release : ", dataList.size(), equalTo(3));
	}

	@Test
	public void testGetKPIList() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(customApiSetting.getJiraXaxisMonthCount()).thenReturn(5);
		when(costOfDelayServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);

		Map<String, List<String>> closedStatusMap = new HashMap<>();
		closedStatusMap.put("6335363749794a18e8a4479b", List.of("closed"));
		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(COD_DATA, codList);
		resultListMap.put(COD_DATA_HISTORY, codHistoryList);
		resultListMap.put(FIELD_MAPPING, closedStatusMap);
		when(kpiDataCacheService.fetchCostOfDelayData(Mockito.any(), Mockito.any())).thenReturn(resultListMap);

		try {
			KpiElement kpiElement = costOfDelayServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("KpiElement : ", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(1));
		} catch (ApplicationException enfe) {

		}

	}

	@Test
	public void testGetStoryList() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(costOfDelayServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);

		Map<String, List<String>> closedStatusMap = new HashMap<>();
		closedStatusMap.put("6335363749794a18e8a4479b", List.of("closed"));
		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(COD_DATA, codList);
		resultListMap.put(COD_DATA_HISTORY, codHistoryList);
		resultListMap.put(FIELD_MAPPING, closedStatusMap);
		when(kpiDataCacheService.fetchCostOfDelayData(Mockito.any(), Mockito.any())).thenReturn(resultListMap);

		try {
			KpiElement kpiElement = costOfDelayServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("KpiElement : ", kpiElement.getValue(), equalTo(null));
		} catch (ApplicationException enfe) {

		}

	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<String, Object> subCategoryMap = new HashMap<>();
		Double epicCount = costOfDelayServiceImpl.calculateKPIMetrics(subCategoryMap);
		assertThat("epic List : ", epicCount, equalTo(null));
	}

	@Test
	public void testQualifierType() {
		String kpiName = KPICode.COST_OF_DELAY.name();
		String type = costOfDelayServiceImpl.getQualifierType();
		assertThat("KPI NAME : ", type, equalTo(kpiName));
	}

}
