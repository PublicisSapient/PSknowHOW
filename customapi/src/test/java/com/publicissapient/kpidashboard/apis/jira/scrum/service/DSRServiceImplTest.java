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
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
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
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
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
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

@RunWith(MockitoJUnitRunner.class)
public class DSRServiceImplTest {
	private final static String UATBUGKEY = "uatBugData";
	private final static String TOTALBUGKEY = "totalBugData";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	List<JiraIssue> uatBugList = new ArrayList<>();
	List<JiraIssue> totalBugList = new ArrayList<>();
	List<SprintWiseStory> sprintWiseStoryList = new ArrayList<>();
	@Mock
	JiraIssueRepository jiraIssueRepository;
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	DSRServiceImpl dsrServiceImpl;
	@Mock
	ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	FieldMappingRepository fieldMappingRepository;
	@Mock
	CustomApiConfig customApiSetting;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private KpiRequest kpiRequest;
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	@Mock
	private KpiHelperService kpiHelperService;
	@Mock
	private CommonService commonService;
	@Mock
	private FilterHelperService filterHelperService;

	private FieldMapping fieldMapping;

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.DEFECT_REMOVAL_EFFICIENCY.getKpiId());
		kpiRequest.setLabel("PROJECT");

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
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);

		kpiWiseAggregation.put("defectSeepageRate", "percentile");
		Map<String, List<String>> priority = new HashMap<>();
		priority.put("P3", Arrays.asList("P3 - Major"));
		when(customApiSetting.getPriority()).thenReturn(priority);
	}

	@After
	public void cleanup() {
		jiraIssueRepository.deleteAll();

	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<String, Object> filterComponentIdWiseDefectMap = new HashMap<>();
		filterComponentIdWiseDefectMap.put(UATBUGKEY, uatBugList);
		filterComponentIdWiseDefectMap.put(TOTALBUGKEY, totalBugList);
		Double dsrValue = dsrServiceImpl.calculateKPIMetrics(filterComponentIdWiseDefectMap);
		assertThat("DSR value :", dsrValue, equalTo(47.0));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);
		String startDate = leafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = leafNodeList.get(leafNodeList.size() - 1).getSprintFilter().getEndDate();
		when(jiraIssueRepository.findIssuesGroupBySprint(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(sprintWiseStoryList);

		when(jiraIssueRepository.findIssuesByType(anyMap())).thenReturn(totalBugList);
		fieldMappingMap.forEach((k, v) -> {
			FieldMapping v1 = v;
			v1.setIncludeRCAForKPI35(Arrays.asList("code issue"));
			v1.setDefectPriorityKPI35(Arrays.asList("P3"));
		});
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		Map<String, Object> defectDataListMap = dsrServiceImpl.fetchKPIDataFromDb(leafNodeList, startDate, endDate,
				kpiRequest);
		assertThat("Total Defects value :", ((List<JiraIssue>) (defectDataListMap.get(TOTALBUGKEY))).size(),
				equalTo(8));
	}

	@Test
	public void testGetDSR_UATLabels() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("defectSeepageRate", Arrays.asList("-30", "30-10", "10-5", "5-2", "2-"));

		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);

		when(jiraIssueRepository.findIssuesGroupBySprint(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(sprintWiseStoryList);

		when(jiraIssueRepository.findIssuesByType(anyMap())).thenReturn(totalBugList);

		fieldMappingMap.forEach((k, v) -> {
			FieldMapping v1 = v;
			v1.setJiraBugRaisedByIdentification(CommonConstant.LABELS);
			v1.setJiraBugRaisedByValue(Arrays.asList("JAVA", "UI"));
			v1.setExcludeUnlinkedDefects(false);
		});
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(dsrServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		try {
			KpiElement kpiElement = dsrServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertEquals("DSR Value :", 9, kpiElement.getExcelData().size());
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

		when(jiraIssueRepository.findIssuesGroupBySprint(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(sprintWiseStoryList);

		totalBugList.forEach(issue -> {
			issue.setDefectRaisedBy("UAT");
			issue.setUatDefectGroup(Arrays.asList("JAVA"));
		});
		when(jiraIssueRepository.findIssuesByType(anyMap())).thenReturn(totalBugList);
		fieldMappingMap.forEach((k, v) -> {
			FieldMapping v1 = v;
			v1.setJiraBugRaisedByIdentification(CommonConstant.CUSTOM_FIELD);
			v1.setJiraBugRaisedByValue(Arrays.asList("JAVA", "UI"));
			v1.setExcludeUnlinkedDefects(false);
		});
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(dsrServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		try {
			KpiElement kpiElement = dsrServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertEquals("DSR Value :", 9, kpiElement.getExcelData().size());
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

		when(jiraIssueRepository.findIssuesGroupBySprint(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(sprintWiseStoryList);

		when(jiraIssueRepository.findIssuesByType(anyMap())).thenReturn(totalBugList);

		fieldMappingMap.forEach((k, v) -> {
			FieldMapping v1 = v;
			v1.setJiraBugRaisedByIdentification(CommonConstant.LABELS);
			v1.setExcludeUnlinkedDefects(false);
		});
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(dsrServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		try {
			KpiElement kpiElement = dsrServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertEquals("DSR Value :", 9, kpiElement.getExcelData().size());
		} catch (ApplicationException enfe) {

		}
	}

	@Test
	public void testGetQualifierType() {
		assertThat("Kpi Name :", dsrServiceImpl.getQualifierType(), equalTo(KPICode.DEFECT_SEEPAGE_RATE.name()));
	}

}
