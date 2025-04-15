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
import static org.junit.Assert.assertEquals;
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
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
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
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;

@RunWith(MockitoJUnitRunner.class)
public class DSRServiceImplTest {
	private static final String UATBUGKEY = "uatBugData";
	private static final String TOTALBUGKEY = "totalBugData";
	private static final String SPRINTSTORIES = "storyData";
	private static final String PROJFMAPPING = "projectFieldMapping";
	public static final String STORY_LIST_WO_DROP = "storyList";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	List<JiraIssue> uatBugList = new ArrayList<>();
	List<JiraIssue> totalBugList = new ArrayList<>();
	List<SprintWiseStory> sprintWiseStoryList = new ArrayList<>();
	Map<String, List<String>> priority = new HashMap<>();

	@Mock
	CacheService cacheService;
	@Mock
	CustomApiConfig customApiConfig;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	private FilterHelperService filterHelperService;
	@Mock
	private CommonService commonService;
	@Mock
	private KpiDataCacheService kpiDataCacheService;
	@Mock
	private KpiDataProvider kpiDataProvider;
	@InjectMocks
	DSRServiceImpl dsrServiceImpl;

	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private KpiRequest kpiRequest;
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private FieldMapping fieldMapping;

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.DEFECT_REMOVAL_EFFICIENCY.getKpiId());
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setLevel(5);

		SprintWiseStoryDataFactory sprintWiseStoryDataFactory = SprintWiseStoryDataFactory.newInstance();
		sprintWiseStoryList = sprintWiseStoryDataFactory.getSprintWiseStories();

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);
		filterLevelMap.put("SPRINT", Filters.SPRINT);

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();

		totalBugList = jiraIssueDataFactory.getBugs();

		uatBugList = totalBugList.stream()
				.filter(f -> NormalizedJira.THIRD_PARTY_DEFECT_VALUE.getValue().equalsIgnoreCase(f.getDefectRaisedBy()))
				.collect(Collectors.toList());

		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectConfig.setProjectName("Scrum Project");
		projectConfig.setProjectNodeId("Scrum Project_6335363749794a18e8a4479b");
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);

		Mockito.when(cacheService.cacheProjectConfigMapData()).thenReturn(projectConfigMap);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);

		kpiWiseAggregation.put("defectSeepageRate", "percentile");
		priority.put("P3", Arrays.asList("P3 - Major"));
	}

	@After
	public void cleanup() {
	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<String, Object> filterComponentIdWiseDefectMap = new HashMap<>();
		filterComponentIdWiseDefectMap.put(UATBUGKEY, uatBugList);
		filterComponentIdWiseDefectMap.put(TOTALBUGKEY, totalBugList);
		Double dsrValue = dsrServiceImpl.calculateKPIMetrics(filterComponentIdWiseDefectMap);
		assertThat("DSR value :", dsrValue, equalTo(45.0));
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

		fieldMappingMap.forEach((k, v) -> {
			FieldMapping v1 = v;
			v1.setIncludeRCAForKPI35(Arrays.asList("code issue"));
			v1.setDefectPriorityKPI35(Arrays.asList("P3"));
		});

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(SPRINTSTORIES, sprintWiseStoryList);
		resultListMap.put(TOTALBUGKEY, totalBugList);
		resultListMap.put(PROJFMAPPING, fieldMappingMap);
		resultListMap.put(STORY_LIST_WO_DROP, new ArrayList<>());

		when(filterHelperService.isFilterSelectedTillSprintLevel(5, false)).thenReturn(true);
		when(kpiDataCacheService.fetchDSRData(any(), any(), any(), any())).thenReturn(resultListMap);

		Map<String, Object> defectDataListMap = dsrServiceImpl.fetchKPIDataFromDb(leafNodeList, startDate, endDate,
				kpiRequest);
		assertThat("Total Defects value :", ((List<JiraIssue>) (defectDataListMap.get(TOTALBUGKEY))).size(), equalTo(20));
	}

	@Test
	public void testGetDSR_UATLabels() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("defectSeepageRate", Arrays.asList("-30", "30-10", "10-5", "5-2", "2-"));

		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);

		Map<String, FieldMapping> projFieldMapping = new HashMap<>();
		fieldMappingMap.forEach((k, v) -> {
			v.setJiraBugRaisedByIdentification(CommonConstant.LABELS);
			v.setJiraBugRaisedByValue(Arrays.asList("JAVA", "UI"));
			v.setExcludeUnlinkedDefects(false);
			projFieldMapping.put(k.toString(), v);
		});
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(dsrServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(SPRINTSTORIES, sprintWiseStoryList);
		resultListMap.put(TOTALBUGKEY, totalBugList);
		resultListMap.put(PROJFMAPPING, projFieldMapping);
		resultListMap.put(STORY_LIST_WO_DROP, new ArrayList<>());

		when(filterHelperService.isFilterSelectedTillSprintLevel(5, false)).thenReturn(false);
		when(kpiDataProvider.fetchDSRData(any(), any(), any())).thenReturn(resultListMap);
		try {
			KpiElement kpiElement = dsrServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertEquals("DSR Value :", 28, kpiElement.getExcelData().size());
		} catch (ApplicationException enfe) {

		}
	}

	@Test
	public void testGetDSR_CustomField() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("defectSeepageRate", Arrays.asList("-30", "30-10", "10-5", "5-2", "2-"));

		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);

		totalBugList.forEach(issue -> {
			issue.setDefectRaisedBy("UAT");
			issue.setUatDefectGroup(Arrays.asList("JAVA"));
		});

		Map<String, FieldMapping> projFieldMapping = new HashMap<>();
		fieldMappingMap.forEach((k, v) -> {
			v.setJiraBugRaisedByIdentification(CommonConstant.CUSTOM_FIELD);
			v.setJiraBugRaisedByValue(Arrays.asList("JAVA", "UI"));
			v.setExcludeUnlinkedDefects(false);
			projFieldMapping.put(k.toString(), v);
		});
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(dsrServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(SPRINTSTORIES, sprintWiseStoryList);
		resultListMap.put(TOTALBUGKEY, totalBugList);
		resultListMap.put(PROJFMAPPING, projFieldMapping);
		resultListMap.put(STORY_LIST_WO_DROP, new ArrayList<>());

		when(filterHelperService.isFilterSelectedTillSprintLevel(5, false)).thenReturn(true);
		when(kpiDataCacheService.fetchDSRData(any(), any(), any(), any())).thenReturn(resultListMap);

		try {
			KpiElement kpiElement = dsrServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertEquals("DSR Value :", 28, kpiElement.getExcelData().size());
		} catch (ApplicationException enfe) {

		}
	}

	@Test
	public void testGetDSR_NOLabels() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("defectSeepageRate", Arrays.asList("-30", "30-10", "10-5", "5-2", "2-"));

		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);

		Map<String, FieldMapping> projFieldMapping = new HashMap<>();
		fieldMappingMap.forEach((k, v) -> {
			FieldMapping v1 = v;
			v1.setJiraBugRaisedByIdentification(CommonConstant.LABELS);
			v1.setExcludeUnlinkedDefects(false);
			projFieldMapping.put(k.toString(), v);
		});
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(dsrServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(SPRINTSTORIES, sprintWiseStoryList);
		resultListMap.put(TOTALBUGKEY, totalBugList);
		resultListMap.put(PROJFMAPPING, projFieldMapping);
		resultListMap.put(STORY_LIST_WO_DROP, new ArrayList<>());

		when(filterHelperService.isFilterSelectedTillSprintLevel(5, false)).thenReturn(true);
		when(kpiDataCacheService.fetchDSRData(any(), any(), any(), any())).thenReturn(resultListMap);
		try {
			KpiElement kpiElement = dsrServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertEquals("DSR Value :", 28, kpiElement.getExcelData().size());
		} catch (ApplicationException enfe) {

		}
	}

	@Test
	public void testGetQualifierType() {
		assertThat("Kpi Name :", dsrServiceImpl.getQualifierType(), equalTo(KPICode.DEFECT_SEEPAGE_RATE.name()));
	}
}
