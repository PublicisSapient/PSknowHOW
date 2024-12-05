/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 
 *    http://www.apache.org/licenses/LICENSE-2.0
 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.jira.scrum.service.release;

import static com.publicissapient.kpidashboard.apis.util.ReleaseKpiHelper.getTicketEstimate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueReleaseStatusDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.model.ReleaseSpecification;
import com.publicissapient.kpidashboard.apis.jira.service.releasedashboard.JiraReleaseKPIService;
import com.publicissapient.kpidashboard.apis.jira.service.releasedashboard.JiraReleaseServiceR;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

@RunWith(MockitoJUnitRunner.class)
public class ReleaseBurnUpServiceImplTest {
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	private ReleaseBurnUpServiceImpl releaseBurnUpService;
	@Mock
	private JiraReleaseServiceR jiraService;
	@Mock
	private JiraReleaseKPIService jiraReleaseKPIService;

	@Mock
	private JiraIssueRepository jiraIssueRepository;

	private KpiRequest kpiRequest;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private List<JiraIssue> jiraIssues = new ArrayList<>();
	private List<JiraIssue> jiraIssues2 = new ArrayList<>();
	private List<JiraIssueCustomHistory> jiraIssuesCustomHistory = new ArrayList<>();
	private List<JiraIssueReleaseStatus> jiraIssueReleaseStatusList;
	private final Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private final Map<ObjectId, FieldMapping> fieldMappingMap2 = new HashMap<>();
	private final Map<ObjectId, FieldMapping> fieldMappingMap3 = new HashMap<>();

	@Before
	public void setUp() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi141");
		kpiRequest.setLabel("RELEASE");
		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance("/json/default/account_hierarchy_filter_data_release.json");
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory
				.newInstance("/json/default/iteration/jira_issues_new_structure.json");
		jiraIssues = jiraIssueDataFactory.getJiraIssues();
		jiraIssues2 = jiraIssueDataFactory.getJiraIssues();
		JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory
				.newInstance("/json/default/iteration/jira_issue_custom_history_new_structure.json");
		jiraIssuesCustomHistory = jiraIssueHistoryDataFactory.getJiraIssueCustomHistory();
		JiraIssueReleaseStatusDataFactory jiraIssueReleaseStatusDataFactory = JiraIssueReleaseStatusDataFactory
				.newInstance("/json/default/jira_issue_release_status.json");
		jiraIssueReleaseStatusList = jiraIssueReleaseStatusDataFactory.getJiraIssueReleaseStatusList();

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		FieldMapping fieldMapping2 = fieldMappingDataFactory.getFieldMappings().get(0);
		FieldMapping fieldMapping3 = fieldMappingDataFactory.getFieldMappings().get(1);
		fieldMapping2.setEstimationCriteria("");
		JiraIssueCustomHistory history = jiraIssuesCustomHistory.stream().findFirst()
				.orElse(new JiraIssueCustomHistory());
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		fieldMappingMap2.put(fieldMapping.getBasicProjectConfigId(), fieldMapping2);
		fieldMappingMap3.put(fieldMapping.getBasicProjectConfigId(), fieldMapping3);
		configHelperService.setFieldMappingMap(fieldMappingMap);
	}

	@Test
	public void getQualifierType() {
		assertThat(releaseBurnUpService.getQualifierType(), equalTo(KPICode.RELEASE_BURNUP.name()));
	}

	@Test
	public void getKpiData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		String kpiRequestTrackerId = "Jira-Excel-ADD-track001";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(jiraService.getJiraIssueReleaseForProject()).thenReturn(jiraIssueReleaseStatusList.get(0));
		when(jiraService.getJiraIssuesCustomHistoryForCurrentRelease()).thenReturn(jiraIssuesCustomHistory);
		when(jiraIssueRepository.findByNumberInAndBasicProjectConfigId(any(), any())).thenReturn(jiraIssues);
		when(jiraService.getReleaseList()).thenReturn(Collections.singletonList("AP v2.0.0"));
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		KpiElement kpiElement = releaseBurnUpService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail.getMapOfListOfLeafNodes().get("release").get(0));
		assertNotNull(kpiElement.getTrendValueList());
	}

	@Test
	public void getKpiData1() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		String kpiRequestTrackerId = "Jira-Excel-ADD-track001";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(jiraService.getJiraIssueReleaseForProject()).thenReturn(jiraIssueReleaseStatusList.get(0));
		when(jiraService.getJiraIssuesCustomHistoryForCurrentRelease()).thenReturn(jiraIssuesCustomHistory);
		when(jiraIssueRepository.findByNumberInAndBasicProjectConfigId(any(), any())).thenReturn(jiraIssues);
		when(jiraService.getReleaseList()).thenReturn(Collections.singletonList("AP v2.0.0"));
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap3);
		KpiElement kpiElement = releaseBurnUpService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail.getMapOfListOfLeafNodes().get("release").get(0));
		assertNotNull(kpiElement.getTrendValueList());
	}

	@Test
	public void getKpiData2() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		String kpiRequestTrackerId = "Jira-Excel-ADD-track001";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(jiraService.getJiraIssueReleaseForProject()).thenReturn(jiraIssueReleaseStatusList.get(0));
		when(jiraService.getJiraIssuesCustomHistoryForCurrentRelease()).thenReturn(jiraIssuesCustomHistory);

		jiraIssues2.forEach(a -> a.setAggregateTimeOriginalEstimateMinutes(0));
		when(jiraIssueRepository.findByNumberInAndBasicProjectConfigId(any(), any())).thenReturn(jiraIssues2);
		when(jiraService.getReleaseList()).thenReturn(Collections.singletonList("AP v2.0.0"));
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap2);
		KpiElement kpiElement = releaseBurnUpService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail.getMapOfListOfLeafNodes().get("release").get(0));
		assertNotNull(kpiElement.getTrendValueList());
	}

	@Test
	public void withNullStartEndDate() throws ApplicationException {
		accountHierarchyDataList = accountHierarchyDataList.stream().filter(data -> {
			data.getNode().stream().filter(node -> node.getGroupName().equalsIgnoreCase("release")).forEach(node -> {
				node.getProjectHierarchy().setBeginDate(null);
				node.getProjectHierarchy().setEndDate(null);
			});
			return true;
		}).collect(Collectors.toList());
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		String kpiRequestTrackerId = "Jira-Excel-ADD-track001";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(jiraService.getJiraIssueReleaseForProject()).thenReturn(jiraIssueReleaseStatusList.get(0));
		when(jiraService.getJiraIssuesCustomHistoryForCurrentRelease()).thenReturn(jiraIssuesCustomHistory);
		when(jiraIssueRepository.findByNumberInAndBasicProjectConfigId(any(), any())).thenReturn(jiraIssues);
		when(jiraService.getReleaseList()).thenReturn(Collections.singletonList("AP v2.0.0"));
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		KpiElement kpiElement = releaseBurnUpService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail.getMapOfListOfLeafNodes().get("release").get(0));
		assertNotNull(kpiElement.getTrendValueList());
	}

	@Test
	public void withJustStartDate() throws ApplicationException {
		accountHierarchyDataList = accountHierarchyDataList.stream().filter(data -> {
			data.getNode().stream().filter(node -> node.getGroupName().equalsIgnoreCase("release")).forEach(node -> {
				node.getProjectHierarchy().setBeginDate("2023-05-25T15:53:00.0000000");
				node.getProjectHierarchy().setEndDate(null);
				node.getProjectHierarchy().setReleaseState("Released");
			});
			return true;
		}).collect(Collectors.toList());
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		String kpiRequestTrackerId = "Jira-Excel-ADD-track001";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(jiraService.getJiraIssueReleaseForProject()).thenReturn(jiraIssueReleaseStatusList.get(0));
		when(jiraService.getJiraIssuesCustomHistoryForCurrentRelease()).thenReturn(jiraIssuesCustomHistory);
		when(jiraIssueRepository.findByNumberInAndBasicProjectConfigId(any(), any())).thenReturn(jiraIssues);
		when(jiraService.getReleaseList()).thenReturn(Collections.singletonList("AP v2.0.0"));
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		KpiElement kpiElement = releaseBurnUpService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail.getMapOfListOfLeafNodes().get("release").get(0));
		assertNotNull(kpiElement.getTrendValueList());
	}

	@Test
	public void getKpiData_bad_scenario() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		String kpiRequestTrackerId = "Jira-Excel-ADD-track001";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(jiraService.getJiraIssuesCustomHistoryForCurrentRelease()).thenReturn(new ArrayList<>());
		when(jiraIssueRepository.findByNumberInAndBasicProjectConfigId(any(), any())).thenReturn(new ArrayList<>());
		when(jiraService.getReleaseList()).thenReturn(Collections.singletonList("AP v2.0.0"));
		KpiElement kpiElement = releaseBurnUpService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail.getMapOfListOfLeafNodes().get("release").get(0));
		assertNotNull(kpiElement.getTrendValueList());
	}

	@Test
	public void test_prediction_Data() throws ApplicationException {
		accountHierarchyDataList = accountHierarchyDataList.stream().filter(data -> {
			data.getNode().stream().filter(node -> node.getGroupName().equalsIgnoreCase("release")).forEach(node -> {
				node.getProjectHierarchy().setBeginDate(null);
				node.getProjectHierarchy().setEndDate(null);
				node.getProjectHierarchy().setReleaseState("unreleased");
			});
			return true;
		}).collect(Collectors.toList());
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		String kpiRequestTrackerId = "Jira-Excel-ADD-track001";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(jiraService.getJiraIssueReleaseForProject()).thenReturn(jiraIssueReleaseStatusList.get(0));
		when(jiraService.getJiraIssuesCustomHistoryForCurrentRelease()).thenReturn(jiraIssuesCustomHistory);
		when(jiraIssueRepository.findByNumberInAndBasicProjectConfigId(any(), any())).thenReturn(jiraIssues);
		when(jiraService.getReleaseList()).thenReturn(Collections.singletonList("AP v2.0.0"));
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		KpiElement kpiElement = releaseBurnUpService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail.getMapOfListOfLeafNodes().get("release").get(0));
		assertNotNull(kpiElement.getTrendValueList());
	}

	@Test
	public void testGetAvgVelocity_withValidData() {
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		List<JiraIssue> jiraIssueList = jiraIssueDataFactory.getJiraIssues();
		FieldMapping fieldMapping = new FieldMapping();
		ReleaseSpecification releaseSpecification = new ReleaseSpecification();
		fieldMapping.setReleaseListKPI150(
				Arrays.asList("KnowHOW v9.0.0 (duration 62.0 days)", "KnowHOW v9.1.0 (duration 6.0 days)"));
		fieldMapping.setBasicProjectConfigId(new ObjectId("6335363749794a18e8a4479b"));
		List<String> releaseNames = Arrays.asList("KnowHOW v9.0.0", "KnowHOW v9.1.0");
		when(jiraIssueRepository.findByBasicProjectConfigIdAndReleaseVersionsReleaseNameIn(
				fieldMapping.getBasicProjectConfigId().toString(), releaseNames)).thenReturn(jiraIssueList);
		Map<String, Object> result = releaseBurnUpService.getAvgVelocity(fieldMapping, releaseSpecification);
		Assertions.assertNotNull(result);
	}

	@Test
	public void testGetTicketEstimate_StoryPointCriteria() {
		FieldMapping fieldMapping = mock(FieldMapping.class);
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		List<JiraIssue> jiraIssueList = jiraIssueDataFactory.getJiraIssues();
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.STORY_POINT);
		double result = getTicketEstimate(jiraIssueList, fieldMapping, 0.0);
		assertEquals(63.0, result, 0.01);
	}

	@Test
	public void testGetTicketEstimate_TimeCriteria() {
		FieldMapping fieldMapping = mock(FieldMapping.class);
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		List<JiraIssue> jiraIssueList = jiraIssueDataFactory.getJiraIssues();
		when(fieldMapping.getEstimationCriteria()).thenReturn("time");
		when(fieldMapping.getStoryPointToHourMapping()).thenReturn(1.0);
		double result = getTicketEstimate(jiraIssueList, fieldMapping, 0.0);
		assertEquals(0.0, result, 0.01);
	}

}