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
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.common.repository.jira.SprintRepositoryCustom;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import com.publicissapient.kpidashboard.apis.jira.service.SprintVelocityServiceHelper;
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
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

@RunWith(MockitoJUnitRunner.class)
public class SprintVelocityServiceImplTest {

	private final static String SPRINTVELOCITYKEY = "sprintVelocityKey";
	private static final String SUBGROUPCATEGORY = "subGroupCategory";
	private static final String SPRINT_WISE_SPRINTDETAILS = "sprintWiseSprintDetailMap";
	private static final String PROJECT_WISE_CLOSED_STATUS_MAP = "projectWiseClosedStatusMap";
	private static final String PROJECT_WISE_TYPE_NAME_MAP = "projectWiseTypeNameMap";
	private static final String TOTAL_ISSUE_WITH_STORYPOINTS = "totalIssueWithStoryPoints";
	private static final String PREVIOUS_SPRINT_WISE_DETAILS = "previousSprintWiseDetails";
	private static final String PREVIOUS_SPRINT_VELOCITY = "previousSprintvelocity";
	private static final String PREVIOUS_SPRINT = "previousSprint";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	List<JiraIssue> totalIssueList = new ArrayList<>();
	List<JiraIssue> previousTotalIssueList = new ArrayList<>();
	@Mock
	JiraIssueRepository jiraIssueRepository;
	@Mock
	CacheService cacheService;
	@Mock
	SprintRepository sprintRepository;
	@Mock
	KpiHelperService kpiHelperService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	SprintVelocityServiceHelper velocityHelper;
	@InjectMocks
	SprintVelocityServiceImpl sprintVelocityServiceImpl;
	@Mock
	ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	FieldMappingRepository fieldMappingRepository;
	@Mock
	CustomApiConfig customApiConfig;
	private KpiRequest kpiRequest;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private List<SprintDetails> sprintDetailsList = new ArrayList<>();
	@Mock
	private CommonService commonService;

	@Mock
	private FilterHelperService filterHelperService;
	@Mock
	private SprintRepositoryCustom sprintRepositoryCustom;


	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.DEFECT_REMOVAL_EFFICIENCY.getKpiId());
		kpiRequest.setLabel("PROJECT");

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);
		filterLevelMap.put("SPRINT", Filters.SPRINT);

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();

		totalIssueList = jiraIssueDataFactory.getBugs();
		previousTotalIssueList = jiraIssueDataFactory.getStories();

		SprintDetailsDataFactory sprintDetailsDataFactory = SprintDetailsDataFactory.newInstance();

		sprintDetailsList = sprintDetailsDataFactory.getSprintDetails();

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

	}

//	@After
//	public void cleanup() {
//		jiraIssueRepository.deleteAll();
//
//	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<String, Object> filterComponentIdWiseDefectMap = new HashMap<>();

		filterComponentIdWiseDefectMap.put(SPRINTVELOCITYKEY, totalIssueList);
		Double velocityValue = sprintVelocityServiceImpl.calculateKPIMetrics(filterComponentIdWiseDefectMap);

		assertThat("Velocity value :", velocityValue, equalTo(8.0));
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
		resultListMap.put(SPRINTVELOCITYKEY, totalIssueList);
		resultListMap.put(SUBGROUPCATEGORY, "sprint");

		resultListMap.put(SPRINT_WISE_SPRINTDETAILS, sprintDetailsList);
		resultListMap.put(PREVIOUS_SPRINT_VELOCITY, previousTotalIssueList);
		resultListMap.put(PREVIOUS_SPRINT_WISE_DETAILS, new ArrayList<>());
		when(kpiHelperService.fetchSprintVelocityDataFromDb(any(), any(), any())).thenReturn(resultListMap);
		when(sprintRepositoryCustom.findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(anySet(), anyList(),anyLong()))
				.thenReturn(sprintDetailsList);
		when(customApiConfig.getSprintCountForFilters()).thenReturn(5);
		Map<String, Object> velocityListMap = sprintVelocityServiceImpl.fetchKPIDataFromDb(leafNodeList, startDate,
				endDate, kpiRequest);
		assertThat("Velocity value :", ((List<JiraIssue>) (velocityListMap.get(SPRINTVELOCITYKEY))).size(),
				equalTo(19));
	}

	@Test
	public void testGetSprintVelocity() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("sprintVelocity", Arrays.asList("-5", "5-25", "25-50", "50-75", "75-"));

		when(customApiConfig.getApplicationDetailedLogger()).thenReturn("On");
		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(SPRINTVELOCITYKEY, totalIssueList);
		resultListMap.put(SUBGROUPCATEGORY, "Sprint");
		resultListMap.put(SPRINT_WISE_SPRINTDETAILS, sprintDetailsList);
		resultListMap.put(PREVIOUS_SPRINT_VELOCITY, previousTotalIssueList);
		resultListMap.put(PREVIOUS_SPRINT_WISE_DETAILS, new ArrayList<>());
		Map<Pair<String, String>, Map<String, Double>> map = new HashMap<>();
		Map<String, Double> abc = new HashMap<>();
		abc.put("ABC", 1.0);
		map.put(Pair.of("6335363749794a18e8a4479b", "abc"), abc);

		resultListMap.put(TOTAL_ISSUE_WITH_STORYPOINTS, map);
		when(kpiHelperService.fetchSprintVelocityDataFromDb(any(), any(), any())).thenReturn(resultListMap);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(sprintVelocityServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(sprintRepository.findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(any(), any()))
				.thenReturn(sprintDetailsList);
		when(customApiConfig.getSprintCountForFilters()).thenReturn(5);
		try {
			KpiElement kpiElement = sprintVelocityServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			List<DataCount> dataCountList = (List<DataCount>) kpiElement.getTrendValueList();

			assertThat("Sprint Velocity trend value : ", dataCountList.size(), equalTo(1));
		} catch (ApplicationException enfe) {

		}
	}

	@Test
	public void testGetSprintVelocity_EmptySprintDetails_AzureCase() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("sprintVelocity", Arrays.asList("-5", "5-25", "25-50", "50-75", "75-"));

		when(customApiConfig.getApplicationDetailedLogger()).thenReturn("On");
		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(SPRINTVELOCITYKEY, totalIssueList);
		resultListMap.put(SUBGROUPCATEGORY, "Sprint");
		resultListMap.put(SPRINT_WISE_SPRINTDETAILS, new ArrayList<>());
		resultListMap.put(PREVIOUS_SPRINT_VELOCITY, previousTotalIssueList);
		resultListMap.put(PREVIOUS_SPRINT_WISE_DETAILS, new ArrayList<>());
		when(kpiHelperService.fetchSprintVelocityDataFromDb(any(), any(), any())).thenReturn(resultListMap);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(sprintVelocityServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(sprintRepository.findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(any(), any()))
				.thenReturn(sprintDetailsList);
		when(customApiConfig.getSprintCountForFilters()).thenReturn(5);
		try {
			KpiElement kpiElement = sprintVelocityServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			List<DataCount> dataCountList = (List<DataCount>) kpiElement.getTrendValueList();

			assertThat("Sprint Velocity trend value : ", dataCountList.size(), equalTo(1));
		} catch (ApplicationException enfe) {

		}
	}

}
