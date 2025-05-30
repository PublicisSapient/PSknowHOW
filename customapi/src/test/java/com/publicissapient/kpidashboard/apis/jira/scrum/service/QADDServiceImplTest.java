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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiDataProvider;
import org.bson.types.ObjectId;
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
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

@RunWith(MockitoJUnitRunner.class)
public class QADDServiceImplTest {

	private static final String SUBGROUPCATEGORY = "subGroupCategory";
	private static final String SPRINT = "Sprint";
	private static final String STORY_POINTS_DATA = "storyPoints";
	@InjectMocks
	QADDServiceImpl qaddServiceImpl;
	@Mock
	JiraIssueRepository jiraIssueRepository;
	@Mock
	JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@Mock
	ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	FieldMappingRepository fieldMappingRepository;
	@Mock
	CustomApiConfig customApiSetting;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	KpiDataProvider kpiDataProvider;
	@Mock
	KpiDataCacheService kpiDataCacheService;
	private KpiRequest kpiRequest;
	private List<KpiElement> kpiElementList;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private List<JiraIssue> jiraStoryList = new ArrayList<>();
	private List<JiraIssue> defectList = new ArrayList<>();
	private List<SprintWiseStory> sprintWiseStoryList = new ArrayList<>();
	@Mock
	private CommonService commonService;

	@Mock
	private FilterHelperService filterHelperService;

	@Test
	public void testgetCalculateKPIMetrics() {
		assertThat(qaddServiceImpl.calculateKPIMetrics(new HashMap<>()), equalTo(null));
	}

	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi113");
		kpiRequest.setLabel("PROJECT");

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

		kpiWiseAggregation.put("cost_Of_Delay", "sum");

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		jiraStoryList = jiraIssueDataFactory.getStories();
		defectList = jiraIssueDataFactory.getBugs();

		SprintWiseStoryDataFactory sprintWiseStoryDataFactory = SprintWiseStoryDataFactory.newInstance();
		sprintWiseStoryList = sprintWiseStoryDataFactory.getSprintWiseStories();

		// set aggregation criteria kpi wise
		kpiWiseAggregation.put("qaDefectDensity", "average");
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("qaDefectDensity", Arrays.asList("-90", "90-60", "60-25", "25-10", "10-"));
	}

	@Test
	public void testGetQADD() {

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(STORY_POINTS_DATA, jiraStoryList);
		resultListMap.put("storyData", sprintWiseStoryList);
		resultListMap.put("defectData", defectList);
		resultListMap.put(SUBGROUPCATEGORY, SPRINT);
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		when(kpiDataProvider.fetchDefectDensityDataFromDb(eq(kpiRequest), any(), any())).thenReturn(resultListMap);
		try {
			assertNotNull(helper(resultListMap));
		} catch (ApplicationException e) {

		}
	}

	@Test
	public void testGetQADDByDefectNotLinkedToAnyStories() {

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(STORY_POINTS_DATA, jiraStoryList);
		resultListMap.put("storyData", sprintWiseStoryList);
		resultListMap.put("defectData", defectList);
		resultListMap.put(SUBGROUPCATEGORY, SPRINT);
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		when(kpiDataProvider.fetchDefectDensityDataFromDb(eq(kpiRequest), any(), any())).thenReturn(resultListMap);
		try {
			assertNotNull(helper(resultListMap));
		} catch (ApplicationException e) {

		}
	}

	@Test
	public void testGetQADDByDefectNotLinkedToStoriesOfSprint() {

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(STORY_POINTS_DATA, jiraStoryList);
		resultListMap.put("storyData", sprintWiseStoryList);
		resultListMap.put("defectData", defectList);
		resultListMap.put(SUBGROUPCATEGORY, SPRINT);
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		when(kpiDataProvider.fetchDefectDensityDataFromDb(eq(kpiRequest), any(), any())).thenReturn(resultListMap);
		try {
			assertNotNull(helper(resultListMap));
		} catch (ApplicationException e) {

		}
	}

	@Test
	public void testGetQADDByDefectNotRaisedByQA() {

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(STORY_POINTS_DATA, jiraStoryList);
		resultListMap.put("storyData", sprintWiseStoryList);
		resultListMap.put("defectData", defectList);
		resultListMap.put(SUBGROUPCATEGORY, SPRINT);
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		when(kpiDataProvider.fetchDefectDensityDataFromDb(eq(kpiRequest), any(), any())).thenReturn(resultListMap);
		try {
			assertNotNull(helper(resultListMap));
		} catch (ApplicationException e) {

		}
	}

	public Object helper(Map<String, Object> resultListMap) throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		String kpiRequestTrackerId = "Jira-Excel-QADD-track001";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		KpiElement kpiElement = qaddServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail);
		return kpiElement.getTrendValueList();
	}
}
