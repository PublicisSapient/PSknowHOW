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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
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
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

@RunWith(MockitoJUnitRunner.class)
public class MeanTimeToRecoverServiceImplTest {

	@InjectMocks
	private MeanTimeToRecoverServiceImpl meanTimeToRecoverService;

	@Mock
	private ConfigHelperService configHelperService;

	@Mock
	private JiraIssueRepository jiraIssueRepository;

	@Mock
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Mock
	private CustomApiConfig customApiSetting;
	@Mock
	private CacheService cacheService;

	@Mock
	private FilterHelperService filterHelperService;

	private KpiRequest kpiRequest;
	KpiElement kpiElement;
	private Map<String, Object> filterLevelMap;
	private Map<String, String> kpiWiseAggregation = new HashMap<>();

	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private List<JiraIssue> jiraIssueList = new ArrayList<>();
	private List<JiraIssueCustomHistory> issueCustomHistoryList = new ArrayList<>();

	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.MEAN_TIME_TO_RECOVER.getKpiId());
		kpiRequest.setLabel("PROJECT");

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		// when(cacheService.cacheAccountHierarchyData()).thenReturn(accountHierarchyDataList);

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		jiraIssueList = jiraIssueDataFactory.getJiraIssues();
		JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory
				.newInstance("/json/default/iteration/jira_issue_custom_history_new_structure.json");
		issueCustomHistoryList = jiraIssueHistoryDataFactory.getJiraIssueCustomHistory();

		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectConfig.setProjectName("Scrum Project");

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setFieldMappingMap(fieldMappingMap);
		// when(configHelperService.getFieldMapping(projectConfig.getId())).thenReturn(fieldMapping);
		// set aggregation criteria kpi wise
		kpiWiseAggregation.put("MEAN_TIME_TO_RECOVER", "sum");

	}

	@After
	public void cleanup() {
		jiraIssueRepository.deleteAll();

	}

	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);
		when(jiraIssueCustomHistoryRepository.findIssuesByCreatedDateAndType(any(), any(), any(), any()))
				.thenReturn(issueCustomHistoryList);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		Map<String, Object> meanTimeToRecoverDataListMap = meanTimeToRecoverService.fetchKPIDataFromDb(leafNodeList, null, null,
				kpiRequest);
		assertNotNull(meanTimeToRecoverDataListMap);
	}
	
	@Test
	public void testQualifierType() {
		String kpiName = KPICode.MEAN_TIME_TO_RECOVER.name();
		String type = meanTimeToRecoverService.getQualifierType();
		assertThat("KPI NAME: ", type, equalTo(kpiName));
	}

	@Test
	public void getMeanTimeToRecoverForJiraData_weekWise() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(jiraIssueRepository.findIssuesWithBoolean(Mockito.anyMap(), Mockito.anyString(),Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyString())).thenReturn(jiraIssueList);
		when(jiraIssueCustomHistoryRepository.findIssuesByCreatedDateAndType(any(), any(), any(), any()))
				.thenReturn(issueCustomHistoryList);
		when(meanTimeToRecoverService.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		try {
			KpiElement kpiElement = meanTimeToRecoverService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Mean Time to Recover TrendValue :", ((List<DataCount>) kpiElement.getTrendValueList()).size(),
					equalTo(1));
		} catch (Exception exception) {
		}
	}

	@Test
	public void getMeanTimeToRecoverForJiraData_monthWise() throws ApplicationException {
		Map<String, Object> durationFilter = new LinkedHashMap<>();
		durationFilter.put(Constant.DURATION, CommonConstant.MONTH);
		durationFilter.put(Constant.COUNT, 20);
		kpiElement = kpiRequest.getKpiList().get(0);
		kpiElement.setFilterDuration(durationFilter);
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		// Correct usage of argument matcher
		when(jiraIssueRepository.findIssuesWithBoolean(Mockito.anyMap(), Mockito.anyString(),Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyString())).thenReturn(jiraIssueList);
		when(jiraIssueCustomHistoryRepository.findIssuesByCreatedDateAndType(any(), any(), any(), any()))
				.thenReturn(issueCustomHistoryList);
		when(meanTimeToRecoverService.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		try {
			KpiElement kpiElement = meanTimeToRecoverService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Mean Time to Recover TrendValue :", ((List<DataCount>) kpiElement.getTrendValueList()).size(),
					equalTo(1));
		} catch (Exception exception) {
		}
	}

	@Test
	public void testFetchKPIDataFromDbData_BadScenario() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);
//		when(jiraIssueRepository.findIssuesWithBoolean(Mockito.anyMap(), Mockito.anyString(),Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ArrayList<>());
		when(jiraIssueCustomHistoryRepository.findIssuesByCreatedDateAndType(any(), any(), any(), any()))
				.thenReturn(new ArrayList<>());
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		Map<String, Object> meanTimeToRecoverDataListMap = meanTimeToRecoverService.fetchKPIDataFromDb(leafNodeList, null, null,
				kpiRequest);
		assertNotNull(meanTimeToRecoverDataListMap);
	}
	
	@Test
	public void getMeanTimeToRecoverForJiraData_BadScenario() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(jiraIssueRepository.findIssuesWithBoolean(Mockito.anyMap(), Mockito.anyString(),Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ArrayList<>());
		when(jiraIssueCustomHistoryRepository.findIssuesByCreatedDateAndType(any(), any(), any(), any()))
				.thenReturn(new ArrayList<>());
		when(meanTimeToRecoverService.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		try {
			KpiElement kpiElement = meanTimeToRecoverService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Mean Time to Recover TrendValue :", ((List<DataCount>) kpiElement.getTrendValueList()).size(),
					equalTo(0));
		} catch (Exception exception) {
		}
	}


}