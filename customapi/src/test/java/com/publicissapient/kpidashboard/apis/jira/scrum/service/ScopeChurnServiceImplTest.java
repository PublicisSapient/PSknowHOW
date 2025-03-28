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
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiDataProvider;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
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
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

@RunWith(MockitoJUnitRunner.class)
public class ScopeChurnServiceImplTest {

	@Mock
	JiraIssueRepository jiraIssueRepository;
	@Mock
	SprintRepository sprintRepository;
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	ScopeChurnServiceImpl scopeChurnService;
	@Mock
	CustomApiConfig customApiSetting;
	@Mock
	JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Mock
	private FilterHelperService filterHelperService;
	@Mock
	private KpiDataCacheService kpiDataCacheService;
	@Mock
	private KpiDataProvider kpiDataProvider;
	@Mock
	private CommonService commonService;

	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private KpiRequest kpiRequest;
	private List<SprintDetails> sprintDetailsList = new ArrayList<>();
	List<JiraIssue> totalIssueList = new ArrayList<>();
	private List<JiraIssueCustomHistory> jiraIssueCustomHistoryList = new ArrayList<>();

	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private Map<String, List<DataCount>> trendValueMap = new HashMap<>();
	private List<DataCount> trendValues = new ArrayList<>();

	private static final String TOTAL_ISSUE = "totalIssue";
	private static final String SPRINT_DETAILS = "sprintDetails";
	private static final String SCOPE_CHANGE_ISSUE_HISTORY = "scopeChangeIssuesHistories";

	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.SCOPE_CHURN.getKpiId());
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setLevel(5);

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();

		totalIssueList = jiraIssueDataFactory.getJiraIssues();

		SprintDetailsDataFactory sprintDetailsDataFactory = SprintDetailsDataFactory.newInstance();
		sprintDetailsList = sprintDetailsDataFactory.getSprintDetails();
		JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory.newInstance();
		jiraIssueCustomHistoryList = jiraIssueHistoryDataFactory.getJiraIssueCustomHistory();

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);

		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectBasicConfig.setIsKanban(true);
		projectBasicConfig.setProjectName("Scrum Project");
		projectBasicConfig.setProjectNodeId("Scrum Project_6335363749794a18e8a4479b");
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
	}

	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList, false);

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(SPRINT_DETAILS, sprintDetailsList);
		resultListMap.put(TOTAL_ISSUE, totalIssueList);
		resultListMap.put(SCOPE_CHANGE_ISSUE_HISTORY, jiraIssueCustomHistoryList);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(filterHelperService.isFilterSelectedTillSprintLevel(5, false)).thenReturn(false);
		when(kpiDataProvider.fetchScopeChurnData(any(), any(), any())).thenReturn(resultListMap);

		when(customApiSetting.getApplicationDetailedLogger()).thenReturn("on");
		Map<String, Object> defectDataListMap = scopeChurnService.fetchKPIDataFromDb(leafNodeList, null, null, kpiRequest);
		assertNotNull(defectDataListMap);
	}

	@Test
	public void testFetchKPIDataFromDbEmptyData_BadScenario() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList, false);

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(SPRINT_DETAILS, sprintDetailsList);
		resultListMap.put(TOTAL_ISSUE, new ArrayList<>());
		resultListMap.put(SCOPE_CHANGE_ISSUE_HISTORY, new ArrayList<>());
		when(filterHelperService.isFilterSelectedTillSprintLevel(5, false)).thenReturn(true);
		when(kpiDataCacheService.fetchScopeChurnData(any(), any(), any(), any())).thenReturn(resultListMap);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(customApiSetting.getApplicationDetailedLogger()).thenReturn("on");
		Map<String, Object> defectDataListMap = scopeChurnService.fetchKPIDataFromDb(leafNodeList, null, null, kpiRequest);
		assertNotNull(defectDataListMap);
	}

	@Test
	public void testGetData() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		when(customApiSetting.getApplicationDetailedLogger()).thenReturn("on");
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		String kpiRequestTrackerId = "Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(scopeChurnService.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(SPRINT_DETAILS, sprintDetailsList);
		resultListMap.put(TOTAL_ISSUE, totalIssueList);
		resultListMap.put(SCOPE_CHANGE_ISSUE_HISTORY, new ArrayList<>());
		when(filterHelperService.isFilterSelectedTillSprintLevel(5, false)).thenReturn(true);
		when(kpiDataCacheService.fetchScopeChurnData(any(), any(), any(), any())).thenReturn(resultListMap);

		try {
			KpiElement kpiElement = scopeChurnService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Scope churn value :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(2));
		} catch (Exception exception) {
		}
	}

	@Test
	public void testGetData_BadScenario() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		when(customApiSetting.getApplicationDetailedLogger()).thenReturn("on");
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		String kpiRequestTrackerId = "Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(scopeChurnService.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(SPRINT_DETAILS, sprintDetailsList);
		resultListMap.put(TOTAL_ISSUE, new ArrayList<>());
		resultListMap.put(SCOPE_CHANGE_ISSUE_HISTORY, new ArrayList<>());
		when(filterHelperService.isFilterSelectedTillSprintLevel(5, false)).thenReturn(true);
		when(kpiDataCacheService.fetchScopeChurnData(any(), any(), any(), any())).thenReturn(resultListMap);
		try {
			KpiElement kpiElement = scopeChurnService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Scope churn value :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(2));
		} catch (Exception exception) {
		}
	}

	@Test
	public void testExcelDataFetch() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		when(customApiSetting.getApplicationDetailedLogger()).thenReturn("off");
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(scopeChurnService.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(SPRINT_DETAILS, sprintDetailsList);
		resultListMap.put(TOTAL_ISSUE, totalIssueList);
		resultListMap.put(SCOPE_CHANGE_ISSUE_HISTORY, jiraIssueCustomHistoryList);
		when(filterHelperService.isFilterSelectedTillSprintLevel(5, false)).thenReturn(true);
		when(kpiDataCacheService.fetchScopeChurnData(any(), any(), any(), any())).thenReturn(resultListMap);

		try {
			KpiElement kpiElement = scopeChurnService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Scope churn value :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(1));
		} catch (Exception exception) {
		}
	}

	@Test
	public void testQualifierType() {
		String kpiName = KPICode.SCOPE_CHURN.name();
		String type = scopeChurnService.getQualifierType();
		assertThat("KPI NAME: ", type, equalTo(kpiName));
	}

	@After
	public void cleanup() {
		jiraIssueRepository.deleteAll();
		jiraIssueCustomHistoryRepository.deleteAll();
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
