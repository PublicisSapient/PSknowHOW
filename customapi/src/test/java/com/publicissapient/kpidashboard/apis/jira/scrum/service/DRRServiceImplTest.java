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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.publicissapient.kpidashboard.apis.data.SprintWiseStoryDataFactory;
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
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;

@RunWith(MockitoJUnitRunner.class)
public class DRRServiceImplTest {

	private static final String REJECTED_DEFECT_DATA = "rejectedBugKey";
	private static final String CLOSED_DEFECT_DATA = "closedDefects";
	private static final String TOTAL_SPRINT_SUBTASK_DEFECTS = "totalSprintSubtaskDefects";
	private static final String SUB_TASK_BUGS_HISTORY = "SubTaskBugsHistory";
	public static final String SPRINT_WISE_SPRINT_DETAILS = "sprintWiseSprintDetails";
	public static final String TOTAL_DEFECT_LIST = "totalDefectList";
	public static final String STORY_LIST = "storyList";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	List<JiraIssue> canceledBugList = new ArrayList<>();
	List<JiraIssue> totalBugList = new ArrayList<>();
	List<JiraIssue> totalIssueList = new ArrayList<>();

	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	DRRServiceImpl dRRServiceImpl;
	@Mock
	CustomApiConfig customApiSetting;
	@Mock
	private KpiDataCacheService kpiDataCacheService;
	@Mock
	private KpiDataProvider kpiDataProvider;

	private Map<String, Object> filterLevelMap;
	private List<SprintWiseStory> sprintWiseStoryList = new ArrayList<>();
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private KpiRequest kpiRequest;
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private List<SprintDetails> sprintDetailsList = new ArrayList<>();
	private List<JiraIssueCustomHistory> jiraIssueCustomHistoryList = new ArrayList<>();
	@Mock
	private CommonService commonService;

	@Mock
	private FilterHelperService filterHelperService;

	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();

	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.DEFECT_REMOVAL_EFFICIENCY.getKpiId());
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setLevel(5);

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

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);
		filterLevelMap.put("SPRINT", Filters.SPRINT);

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();

		totalBugList = jiraIssueDataFactory.getBugs();
		totalIssueList = jiraIssueDataFactory.getJiraIssues();

		canceledBugList = totalBugList.stream().filter(bug -> bug.getStatus().equals("Closed"))
				.collect(Collectors.toList());
		SprintWiseStoryDataFactory sprintWiseStoryDataFactory = SprintWiseStoryDataFactory.newInstance();
		sprintWiseStoryList = sprintWiseStoryDataFactory.getSprintWiseStories();
		SprintDetailsDataFactory sprintDetailsDataFactory = SprintDetailsDataFactory.newInstance();
		sprintDetailsList = sprintDetailsDataFactory.getSprintDetails();
		JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory.newInstance();
		jiraIssueCustomHistoryList = jiraIssueHistoryDataFactory.getJiraIssueCustomHistory();

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);
		when(configHelperService.getFieldMapping(projectBasicConfig.getId())).thenReturn(fieldMapping);
		kpiWiseAggregation.put("defectRejectionRate", "percentile");
	}

	@After
	public void cleanup() {
	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<String, Object> filterComponentIdWiseDefectMap = new HashMap<>();
		filterComponentIdWiseDefectMap.put(REJECTED_DEFECT_DATA, canceledBugList);
		filterComponentIdWiseDefectMap.put(CLOSED_DEFECT_DATA, totalBugList);
		Double drrValue = dRRServiceImpl.calculateKPIMetrics(filterComponentIdWiseDefectMap);
		assertThat("DRR value :", drrValue, equalTo(80.0));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList, false);
		String startDate = leafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = leafNodeList.get(leafNodeList.size() - 1).getSprintFilter().getEndDate();

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(TOTAL_SPRINT_SUBTASK_DEFECTS, new ArrayList<>());
		resultListMap.put(SUB_TASK_BUGS_HISTORY, jiraIssueCustomHistoryList);
		resultListMap.put(SPRINT_WISE_SPRINT_DETAILS, sprintDetailsList);
		resultListMap.put(STORY_LIST, totalIssueList);
		resultListMap.put(REJECTED_DEFECT_DATA, new ArrayList<>());
		resultListMap.put(TOTAL_DEFECT_LIST, totalIssueList);
		when(filterHelperService.isFilterSelectedTillSprintLevel(5, false)).thenReturn(true);
		when(kpiDataCacheService.fetchDRRData(any(), any(), any(), any())).thenReturn(resultListMap);
		when(customApiSetting.getApplicationDetailedLogger()).thenReturn("Off");

		Map<String, Object> defectDataListMap = dRRServiceImpl.fetchKPIDataFromDb(leafNodeList, startDate, endDate,
				kpiRequest);
		assertThat("Rejects Defects value :", ((List<JiraIssue>) defectDataListMap.get(REJECTED_DEFECT_DATA)).size(),
				equalTo(0));
	}

	@Test
	public void testGetDRR() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("defectRejectionRate", Arrays.asList("-30", "30-10", "10-5", "5-2", "2-"));

		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);

		when(customApiSetting.getApplicationDetailedLogger()).thenReturn("on");
		when(customApiSetting.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiSetting.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiSetting.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiSetting.getpriorityP4()).thenReturn("p4-minor");

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(dRRServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(TOTAL_SPRINT_SUBTASK_DEFECTS, new ArrayList<>());
		resultListMap.put(SUB_TASK_BUGS_HISTORY, jiraIssueCustomHistoryList);
		resultListMap.put(SPRINT_WISE_SPRINT_DETAILS, sprintDetailsList);
		resultListMap.put(STORY_LIST, totalIssueList);
		resultListMap.put(REJECTED_DEFECT_DATA, new ArrayList<>());
		resultListMap.put(TOTAL_DEFECT_LIST, totalIssueList);
		when(filterHelperService.isFilterSelectedTillSprintLevel(5, false)).thenReturn(false);
		when(kpiDataProvider.fetchDRRData(any(), any(), any())).thenReturn(resultListMap);

		try {
			KpiElement kpiElement = dRRServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("DRR Value :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(1));
		} catch (ApplicationException exception) {

		}
	}

	@Test
	public void testGetQualifierType() {
		assertThat("Kpi Name :", dRRServiceImpl.getQualifierType(), equalTo(KPICode.DEFECT_REJECTION_RATE.name()));
	}
}
