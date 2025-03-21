/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static com.publicissapient.kpidashboard.apis.constant.Constant.P1;
import static com.publicissapient.kpidashboard.apis.constant.Constant.P2;
import static com.publicissapient.kpidashboard.apis.constant.Constant.P3;
import static com.publicissapient.kpidashboard.apis.constant.Constant.P4;
import static com.publicissapient.kpidashboard.apis.constant.Constant.P5;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.publicissapient.kpidashboard.apis.common.service.impl.CommonServiceImpl;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.SprintWiseStoryDataFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.LabelCount;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

/**
 * @author anisingh4
 */
@RunWith(MockitoJUnitRunner.class)
public class FirstTimePassRateServiceImplTest {

	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	@InjectMocks
	private FirstTimePassRateServiceImpl firstTimePassRateService;
	@Mock
	private ConfigHelperService configHelperService;
	@Mock
	private JiraIssueRepository jiraIssueRepository;
	@Mock
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Mock
	private KpiHelperService kpiHelperService;
	@Mock
	private CacheService cacheService;
	@Mock
	private CustomApiConfig customApiSetting;
	@Mock
	private JiraKPIService jiraKPIService;
	@Mock
	private CommonServiceImpl commonService;
	@Mock
	private FilterHelperService filterHelperService;
	@Mock
	KpiDataProvider kpiDataProvider;
	@Mock
	KpiDataCacheService kpiDataCacheService;
	@Mock
	private CustomApiConfig customApiConfig;
	private KpiRequest kpiRequest;
	private static final String FIRST_TIME_PASS_STORIES = "ftpStories";
	private static final String SPRINT_WISE_CLOSED_STORIES = "sprintWiseClosedStories";
	private static final String ISSUE_DATA = "Issue Data";
	public static final String DEFECT_FOR_EXCEL = "defect for Excel";
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private List<LabelCount> defectPriority = new ArrayList<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private Map<String, String> kpiWiseAggregation = new HashMap<>();

	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi82");
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

		/// set aggregation criteria kpi wise
		kpiWiseAggregation.put(KPICode.FIRST_TIME_PASS_RATE.getKpiId(), "average");
		when(configHelperService.calculateCriteria()).thenReturn(kpiWiseAggregation);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.FIRST_TIME_PASS_RATE.name(), Arrays.asList("-25", "25-50", "50-75", "75-90", "90-"));
		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);
		filterLevelMap.put("SPRINT", Filters.SPRINT);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		defectPriority = fieldMapping.getDefectPriorityKPI82();
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);
	}

	@Test
	public void getQualifierType() {
		String qualifierType = firstTimePassRateService.getQualifierType();
		assertEquals(KPICode.FIRST_TIME_PASS_RATE.name(), qualifierType);
	}

	@Test
	public void getKpiData() throws ApplicationException {

		SprintWiseStoryDataFactory sprintWiseStoryDataFactory = SprintWiseStoryDataFactory.newInstance();
		List<SprintWiseStory> storyData = sprintWiseStoryDataFactory.getSprintWiseStories();
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		List<JiraIssue> defectData = jiraIssueDataFactory.getBugs().stream().toList();

		List<JiraIssue> issues = jiraIssueDataFactory.getJiraIssues().stream()
				.filter(jiraIssue -> jiraIssue.getJiraStatus().equals("Closed") && jiraIssue.getTypeName().equals("Story"))
				.collect(Collectors.toList());
		Set<JiraIssue> stories = issues.stream().collect(Collectors.toSet());
		JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory.newInstance();
		List<JiraIssueCustomHistory> jiraIssueCustomHistories = jiraIssueHistoryDataFactory.getJiraIssueCustomHistory()
				.stream().filter(history -> stories.contains(history.getStoryID())).collect(Collectors.toList());

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(SPRINT_WISE_CLOSED_STORIES, storyData);
		resultListMap.put(FIRST_TIME_PASS_STORIES, new ArrayList<>());
		resultListMap.put(ISSUE_DATA, jiraIssueDataFactory.getJiraIssues());
		resultListMap.put(DEFECT_FOR_EXCEL, new HashSet(defectData));
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		when(kpiDataProvider.fetchFirstTimePassRateDataFromDb(eq(kpiRequest), any(), any())).thenReturn(resultListMap);
		try {
			KpiElement kpiElement = firstTimePassRateService.getKpiData(this.kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("First Time pass Rate Value size:", ((List<DataCount>) kpiElement.getTrendValueList()).size(),
					equalTo(1));

		} catch (ApplicationException enfe) {

		}
	}

	@Test
	public void calculateKPIMetrics() {
		assertThat(firstTimePassRateService.calculateKPIMetrics(new HashMap<>()), equalTo(0.0));
	}
}
