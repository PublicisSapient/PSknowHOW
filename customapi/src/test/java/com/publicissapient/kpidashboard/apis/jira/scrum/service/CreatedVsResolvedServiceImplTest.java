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
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiDataProvider;
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
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

@RunWith(MockitoJUnitRunner.class)
public class CreatedVsResolvedServiceImplTest {

	private final static String CREATED_VS_RESOLVED_KEY = "createdVsResolvedKey";
	private static final String SPRINT_WISE_SPRINTDETAILS = "sprintWiseSprintDetailMap";
	private static final String SUB_TASK_BUGS_HISTORY = "SubTaskBugsHistory";
	private static final String SPRINT_WISE_SUB_TASK_BUGS = "sprintWiseSubTaskBugs";
	public static final String STORY_LIST = "storyList";

	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	List<JiraIssue> totalIssueList = new ArrayList<>();
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	CreatedVsResolvedServiceImpl createdVsResolvedServiceImpl;
	@Mock
	CustomApiConfig customApiConfig;
	@Mock
	private FilterHelperService filterHelperService;
	@Mock
	private JiraServiceR jiraService;
	@Mock
	private KpiDataCacheService kpiDataCacheService;
	@Mock
	private KpiDataProvider kpiDataProvider;

	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private List<SprintDetails> sprintDetailsList = new ArrayList<>();
	private List<DataCount> trendValues = new ArrayList<>();
	private Map<String, List<DataCount>> trendValueMap = new LinkedHashMap<>();

	private KpiRequest kpiRequest;
	@Mock
	private CommonService commonService;

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.CREATED_VS_RESOLVED_DEFECTS.getKpiId());
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

		totalIssueList = jiraIssueDataFactory.getJiraIssues();

		SprintDetailsDataFactory sprintDetailsDataFactory = SprintDetailsDataFactory.newInstance();
		sprintDetailsList = sprintDetailsDataFactory.getSprintDetails();

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);
		when(configHelperService.getFieldMapping(projectBasicConfig.getId())).thenReturn(fieldMapping);

		// when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		// setDataCountList();
		kpiWiseAggregation.put("created_Vs_Resolved_Defects", "sum");
		setTreadValuesDataCount();

	}

	@After
	public void cleanup() {

	}

	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList , false);
		String startDate = leafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = leafNodeList.get(leafNodeList.size() - 1).getSprintFilter().getEndDate();
		when(customApiConfig.getApplicationDetailedLogger()).thenReturn("Off");

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(CREATED_VS_RESOLVED_KEY, totalIssueList);
		resultListMap.put(SPRINT_WISE_SUB_TASK_BUGS, new ArrayList<>());
		resultListMap.put(SUB_TASK_BUGS_HISTORY, new ArrayList<>());
		resultListMap.put(SPRINT_WISE_SPRINTDETAILS, sprintDetailsList);
		resultListMap.put(STORY_LIST, totalIssueList);
		when(filterHelperService.isFilterSelectedTillSprintLevel(5, false)).thenReturn(true);
		when(kpiDataCacheService.fetchCreatedVsResolvedData(any(), any(), any(), any())).thenReturn(resultListMap);

		Map<String, Object> createdVsResolvedListMap = createdVsResolvedServiceImpl.fetchKPIDataFromDb(leafNodeList,
				startDate, endDate, kpiRequest);
		assertThat("createdVsResolved value :",
				((List<JiraIssue>) (createdVsResolvedListMap.get(CREATED_VS_RESOLVED_KEY))).size(), equalTo(totalIssueList.size()));
	}

	@Test
	public void testGetCreatedVsResolved() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("sprintVelocity", Arrays.asList("-5", "5-25", "25-50", "50-75", "75-"));

		when(customApiConfig.getApplicationDetailedLogger()).thenReturn("On");

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(createdVsResolvedServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);

		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(CREATED_VS_RESOLVED_KEY, totalIssueList);
		resultListMap.put(SPRINT_WISE_SUB_TASK_BUGS, new ArrayList<>());
		resultListMap.put(SUB_TASK_BUGS_HISTORY, new ArrayList<>());
		resultListMap.put(SPRINT_WISE_SPRINTDETAILS, sprintDetailsList);
		resultListMap.put(STORY_LIST, totalIssueList);
		when(filterHelperService.isFilterSelectedTillSprintLevel(5, false)).thenReturn(false);
		when(kpiDataProvider.fetchCreatedVsResolvedData(any(), any(), any())).thenReturn(resultListMap);

		try {
			KpiElement kpiElement = createdVsResolvedServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			List<DataCountGroup> dataCountList = (List<DataCountGroup>) kpiElement.getTrendValueList();
			System.out.println(dataCountList);

			assertThat("Created Vs Resolved trend value : ", dataCountList.size(), equalTo(2));
		} catch (ApplicationException enfe) {

		}
	}

	@Test
	public void testGetCreatedVsResolved_EmptySprintDetails_AzureCase() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("sprintVelocity", Arrays.asList("-5", "5-25", "25-50", "50-75", "75-"));

		when(customApiConfig.getApplicationDetailedLogger()).thenReturn("On");

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(createdVsResolvedServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(CREATED_VS_RESOLVED_KEY, totalIssueList);
		resultListMap.put(SPRINT_WISE_SUB_TASK_BUGS, new ArrayList<>());
		resultListMap.put(SUB_TASK_BUGS_HISTORY, new ArrayList<>());
		resultListMap.put(SPRINT_WISE_SPRINTDETAILS, sprintDetailsList);
		resultListMap.put(STORY_LIST, totalIssueList);
		when(filterHelperService.isFilterSelectedTillSprintLevel(5, false)).thenReturn(true);
		when(kpiDataCacheService.fetchCreatedVsResolvedData(any(), any(), any(), any())).thenReturn(resultListMap);
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");

		try {
			KpiElement kpiElement = createdVsResolvedServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			List<DataCountGroup> dataCountList = (List<DataCountGroup>) kpiElement.getTrendValueList();

			assertThat("Created Vs Resolved trend value : ", dataCountList.size(), equalTo(2));
		} catch (ApplicationException enfe) {

		}
	}

	@Test
	public void testGetQualifierType() {
		assertThat(createdVsResolvedServiceImpl.getQualifierType(), equalTo("CREATED_VS_RESOLVED_DEFECTS"));
	}

	private void setTreadValuesDataCount() {
		List<DataCount> dataCountList = new ArrayList<>();
		DataCount dataCountValue = new DataCount();
		dataCountValue.setData(String.valueOf(5L));
		dataCountValue.setValue(5L);
		dataCountList.add(dataCountValue);
		DataCount dataCount = setDataCountValues("Scrum Project", "3", "4", dataCountList);
		trendValues.add(dataCount);
		trendValueMap.put("Tagged Defects", trendValues);
		trendValueMap.put("Defects Tagged After Sprint Start", trendValues);
	}

	private DataCount setDataCountValues(String data, String maturity, Object maturityValue, Object value) {
		DataCount dataCount = new DataCount();
		dataCount.setData(data);
		dataCount.setMaturity(maturity);
		dataCount.setMaturityValue(maturityValue);
		dataCount.setValue(value);
		return dataCount;
	}

}