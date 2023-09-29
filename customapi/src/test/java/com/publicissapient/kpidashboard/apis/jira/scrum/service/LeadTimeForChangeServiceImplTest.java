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
import java.util.stream.Collectors;

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
import com.publicissapient.kpidashboard.apis.data.MergeRequestDataFactory;
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
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.scm.MergeRequests;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.scm.MergeRequestRepository;

@RunWith(MockitoJUnitRunner.class)
public class LeadTimeForChangeServiceImplTest {

	@InjectMocks
	private LeadTimeForChangeServiceImpl leadTimeForChangeService;

	@Mock
	private ConfigHelperService configHelperService;

	@Mock
	private MergeRequestRepository mergeRequestRepository;

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
	private Map<String, Object> filterLevelMap;

	private Map<String, String> kpiWiseAggregation = new HashMap<>();

	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private List<DataCount> dataCountList = new ArrayList<>();
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

	private List<JiraIssue> jiraIssueList = new ArrayList<>();

	private List<JiraIssueCustomHistory> issueCustomHistoryList = new ArrayList<>();

	private List<MergeRequests> mergeRequestsList = new ArrayList<>();

	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.LEAD_TIME_FOR_CHANGE.getKpiId());
		kpiRequest.setLabel("PROJECT");

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		when(cacheService.cacheAccountHierarchyData()).thenReturn(accountHierarchyDataList);

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		jiraIssueList = jiraIssueDataFactory.getJiraIssues();
		JiraIssueHistoryDataFactory issueHistoryFactory = JiraIssueHistoryDataFactory.newInstance();
		issueCustomHistoryList = issueHistoryFactory.getJiraIssueCustomHistory();

		MergeRequestDataFactory mergeRequestDataFactory = MergeRequestDataFactory.newInstance();
		mergeRequestsList = mergeRequestDataFactory.getMergeRequestList();

		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectConfig.setProjectName("Scrum Project");

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setFieldMappingMap(fieldMappingMap);
		when(configHelperService.getFieldMapping(projectConfig.getId())).thenReturn(fieldMapping);
		// set aggregation criteria kpi wise
		kpiWiseAggregation.put("LEAD_TIME_FOR_CHANGE", "sum");

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
		when(jiraIssueRepository.findByRelease(Mockito.any(), Mockito.any())).thenReturn(jiraIssueList);
		when(jiraIssueCustomHistoryRepository.findFeatureCustomHistoryStoryProjectWise(any(), any(), any()))
				.thenReturn(issueCustomHistoryList);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		Map<String, Object> leadTimeDataListMap = leadTimeForChangeService.fetchKPIDataFromDb(leafNodeList, null, null,
				kpiRequest);
		assertNotNull(leadTimeDataListMap);
	}

	@Test
	public void testQualifierType() {
		String kpiName = KPICode.LEAD_TIME_FOR_CHANGE.name();
		String type = leadTimeForChangeService.getQualifierType();
		assertThat("KPI NAME: ", type, equalTo(kpiName));
	}

	@Test
	public void getLeadTimeForChangeForJiraData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(jiraIssueRepository.findByRelease(Mockito.any(), Mockito.any())).thenReturn(jiraIssueList);
		when(jiraIssueCustomHistoryRepository.findFeatureCustomHistoryStoryProjectWise(any(), any(), any()))
				.thenReturn(issueCustomHistoryList);
		when(leadTimeForChangeService.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(customApiSetting.getJiraXaxisMonthCount()).thenReturn(8);
		try {
			KpiElement kpiElement = leadTimeForChangeService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Lead time for change TrendValue :", ((List<DataCount>) kpiElement.getTrendValueList()).size(),
					equalTo(1));
		} catch (Exception exception) {
		}
	}

	@Test
	public void getLeadTimeForChangeForRepoData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMapping.setLeadTimeConfigRepoTool(CommonConstant.REPO);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setFieldMappingMap(fieldMappingMap);
		when(configHelperService.getFieldMapping(new ObjectId("6335363749794a18e8a4479b"))).thenReturn(fieldMapping);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(jiraIssueRepository.findByRelease(Mockito.any(), Mockito.any())).thenReturn(jiraIssueList);
		when(jiraIssueCustomHistoryRepository.findFeatureCustomHistoryStoryProjectWise(any(), any(), any()))
				.thenReturn(issueCustomHistoryList);
		List<String> issueIdList = jiraIssueList.stream().map(JiraIssue::getNumber).collect(Collectors.toList());

		when(mergeRequestRepository.findMergeRequestListBasedOnBasicProjectConfigId(
				new ObjectId("6335363749794a18e8a4479b"), CommonUtils.convertTestFolderToPatternList(issueIdList),
				"master")).thenReturn(mergeRequestsList);
		when(leadTimeForChangeService.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(customApiSetting.getJiraXaxisMonthCount()).thenReturn(8);
		try {
			KpiElement kpiElement = leadTimeForChangeService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Lead time for change TrendValue :", ((List<DataCount>) kpiElement.getTrendValueList()).size(),
					equalTo(1));
		} catch (Exception exception) {
		}
	}
}
