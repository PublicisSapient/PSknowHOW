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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiDataProvider;
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
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepositoryCustom;

@RunWith(MockitoJUnitRunner.class)
public class SprintPredictabilityImplTest {

	private static final String SPRINT_WISE_PREDICTABILITY = "predictability";

	private static final String SPRINT_WISE_SPRINT_DETAILS = "sprintWiseSprintDetailMap";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMapForActualEstimation = new HashMap<>();
	List<JiraIssue> sprintWiseStoryList = new ArrayList<>();
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private KpiRequest kpiRequest;
	private Map<String, Object> filterLevelMap;
	private Set<ObjectId> basicProjectConfigObjectIds = new HashSet<>();
	private List<SprintDetails> sprintDetailsList = new ArrayList<>();
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	List<String> sprintStatusList = new ArrayList<>();

	@Mock
	private JiraIssueRepository jiraIssueRepository;
	@Mock
	CustomApiConfig customApiConfig;

	@Mock
	private SprintRepository sprintRepository;

	@Mock
	private CacheService cacheService;

	@Mock
	private ConfigHelperService configHelperService;
	@Mock
	KpiDataProvider kpiDataProvider;
	@Mock
	KpiDataCacheService kpiDataCacheService;

	@Mock
	private FilterHelperService filterHelperService;

	@Mock
	private KpiHelperService kpiHelperService;

	@InjectMocks
	private SprintPredictabilityImpl sprintPredictability;

	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;

	@Mock
	private FieldMappingRepository fieldMappingRepository;

	@Mock
	private CustomApiConfig customApiSetting;

	@Mock
	private CommonService commonService;

	@Mock
	private JiraServiceR jiraKPIService;
	@Mock
	private SprintRepositoryCustom sprintRepositoryCustom;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();

	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.SPRINT_PREDICTABILITY.getKpiId());
		kpiRequest.setLabel("PROJECT");

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);
		filterLevelMap.put("SPRINT", Filters.SPRINT);

		JiraIssueDataFactory sprintWiseStoryDataFactory = JiraIssueDataFactory.newInstance();
		sprintWiseStoryList = sprintWiseStoryDataFactory.getStories();

		SprintDetailsDataFactory sprintDetailsDataFactory = SprintDetailsDataFactory.newInstance();

		sprintDetailsList = sprintDetailsDataFactory.getSprintDetails();

		basicProjectConfigObjectIds.add(new ObjectId("6335363749794a18e8a4479b"));
		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectConfig.setProjectName("Scrum Project");
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		FieldMapping fieldMappingWithActualEstimation = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingWithActualEstimation.setEstimationCriteria("Actual Estimation");
		fieldMappingMapForActualEstimation.put(fieldMapping.getBasicProjectConfigId(), fieldMappingWithActualEstimation);
		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);

		// set aggregation criteria kpi wise
		kpiWiseAggregation.put("defectRemovalEfficiency", "percentile");
		sprintStatusList.add(SprintDetails.SPRINT_STATE_CLOSED);
		sprintStatusList.add(SprintDetails.SPRINT_STATE_CLOSED.toLowerCase());
		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(SPRINT_WISE_PREDICTABILITY, sprintWiseStoryList);
		resultListMap.put(SPRINT_WISE_SPRINT_DETAILS, sprintDetailsList);
		when(kpiDataProvider.fetchSprintPredictabilityDataFromDb(eq(kpiRequest), any(), any())).thenReturn(resultListMap);

		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectBasicConfig.setIsKanban(true);
		projectBasicConfig.setProjectName("Scrum Project");
		projectBasicConfig.setProjectNodeId("Scrum Project_6335363749794a18e8a4479b");
		projectConfigList.add(projectBasicConfig);

		projectConfigList.forEach(projectConfigs -> {
			projectConfigMap.put(projectConfigs.getProjectName(), projectConfigs);
		});
		when(cacheService.cacheProjectConfigMapData()).thenReturn(projectConfigMap);
	}

	@After
	public void cleanup() {
		sprintWiseStoryList = null;
		jiraIssueRepository.deleteAll();
	}

	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList, false);
		String startDate = leafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = leafNodeList.get(leafNodeList.size() - 1).getSprintFilter().getEndDate();
		Map<ObjectId, List<SprintDetails>> expectedDuplicateIssues = new HashMap<>();
		expectedDuplicateIssues.put(new ObjectId("6335363749794a18e8a4479b"),
				sprintDetailsList.stream().collect(Collectors.toList()));
		Map<String, Object> sprintWisePredictability = sprintPredictability.fetchKPIDataFromDb(leafNodeList, startDate,
				endDate, kpiRequest);
		assertThat("Sprint wise jira Issue  value :",
				((List<JiraIssue>) sprintWisePredictability.get(SPRINT_WISE_PREDICTABILITY)).size(), equalTo(25));
		assertThat("Sprint wise Sprint details value :",
				((List<SprintDetails>) sprintWisePredictability.get(SPRINT_WISE_SPRINT_DETAILS)).size(), equalTo(7));
	}

	@Test
	public void testGetSprintPredictability() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(sprintPredictability.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		try {
			KpiElement kpiElement = sprintPredictability.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("DRE Value :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(1));
		} catch (Exception exception) {
		}
	}

	@Test
	public void testGetSprintPredictability_EmptySprintDetails_AzureCase() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(sprintPredictability.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		try {
			KpiElement kpiElement = sprintPredictability.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Azure Value :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(0));
		} catch (Exception exception) {
		}
	}

	@Test
	public void testQualifierType() {
		String kpiName = KPICode.SPRINT_PREDICTABILITY.name();
		String type = sprintPredictability.getQualifierType();
		assertThat("KPI NAME: ", type, equalTo(kpiName));
	}

	@Test
	public void testGetSprintPredictabilityForActualEstimation() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMapForActualEstimation);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(sprintPredictability.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		try {
			KpiElement kpiElement = sprintPredictability.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("DRE Value :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(0));
		} catch (Exception exception) {
		}
	}

	@Test
	public void testFetchKPIDataFromDbData1() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList, false);
		String startDate = leafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = leafNodeList.get(leafNodeList.size() - 1).getSprintFilter().getEndDate();

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(SPRINT_WISE_PREDICTABILITY, sprintWiseStoryList);
		resultListMap.put(SPRINT_WISE_SPRINT_DETAILS, sprintDetailsList);

		Map<ObjectId, Set<String>> duplicateIssues = new HashMap<>();
		Set<String> set = new HashSet<>();
		set.add("6335363749794a18e8a4479b");
		duplicateIssues.put(new ObjectId("6335363749794a18e8a4479b"), set);
		when(kpiDataProvider.fetchSprintPredictabilityDataFromDb(eq(kpiRequest), any(), any())).thenReturn(resultListMap);
		Map<String, Object> sprintWisePredictability = sprintPredictability.fetchKPIDataFromDb(leafNodeList, startDate,
				endDate, kpiRequest);
	}
}
