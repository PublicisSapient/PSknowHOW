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
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
	private KpiRequest kpiRequest;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private List<String> defectPriority = new ArrayList<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private Map<String, String> kpiWiseAggregation = new HashMap<>();

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi82");
		kpiRequest.setLabel("PROJECT");

		/// set aggregation criteria kpi wise
		kpiWiseAggregation.put(KPICode.FIRST_TIME_PASS_RATE.getKpiId(), "average");
		when(configHelperService.calculateCriteria()).thenReturn(kpiWiseAggregation);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.FIRST_TIME_PASS_RATE.name(),
				Arrays.asList("-25", "25-50", "50-75", "75-90", "90-"));
		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);
		filterLevelMap.put("SPRINT", Filters.SPRINT);

		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectConfig.setProjectName("Scrum Project");
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		defectPriority = fieldMapping.getDefectPriorityKPI82().stream().filter(a -> !a.equals("")).map(String::toUpperCase)
				.collect(Collectors.toList());
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

		List<JiraIssue> issues = jiraIssueDataFactory.getJiraIssues().stream().filter(
				jiraIssue -> jiraIssue.getJiraStatus().equals("Closed") && jiraIssue.getTypeName().equals("Story"))
				.collect(Collectors.toList());

		Set<String> stories = issues.stream().map(JiraIssue::getNumber).collect(Collectors.toSet());
		JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory.newInstance();
		List<JiraIssueCustomHistory> jiraIssueCustomHistories = jiraIssueHistoryDataFactory.getJiraIssueCustomHistory()
				.stream().filter(history -> stories.contains(history.getStoryID())).collect(Collectors.toList());

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		Map<String, List<String>> priorityMap = new HashMap<>();
		priorityMap.put(P1,
				Stream.of("p1", "P1 - Blocker", "blocker", "1", "0", "p0", "urgent").collect(Collectors.toList()));
		priorityMap.put(P2, Stream.of("p2", "critical", "P2 - Critical", "2", "high").collect(Collectors.toList()));
		priorityMap.put(P3, Stream.of("p3", "p3-major", "major", "3", "medium").collect(Collectors.toList()));
		priorityMap.put(P4, Stream.of("p4", "p4 - minor", "minor", "4", "low").collect(Collectors.toList()));
		priorityMap.put(P5, Stream.of("p5 - trivial", "5", "trivial").collect(Collectors.toList()));

		List<String> resultSet = Stream.of("p1", "P1 - Blocker", "blocker", "1", "0", "p0", "urgent", "p2", "critical",
				"P2 - Critical", "2", "high").collect(Collectors.toList());
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(customApiSetting.getPriority()).thenReturn(priorityMap);
		List<String> priorValue = new ArrayList<>();
		defectPriority.forEach(prior -> priorValue.addAll(priorityMap.get(prior)));
		when(jiraIssueRepository.findIssuesGroupBySprint(anyMap(), anyMap(), anyString(), anyString()))
				.thenReturn(storyData);
		when(jiraIssueRepository.findIssuesBySprintAndType(anyMap(), anyMap())).thenReturn(issues);
		when(jiraIssueCustomHistoryRepository.findByStoryIDIn(anyList())).thenReturn(jiraIssueCustomHistories);

		List<JiraIssue> defects = jiraIssueDataFactory.getJiraIssues().stream()
				.filter(issue -> issue.getTypeName().equals(NormalizedJira.DEFECT_TYPE.getValue()))
				.collect(Collectors.toList());

		when(jiraIssueRepository.findByTypeNameAndDefectStoryIDIn(anyString(), anyList())).thenReturn(defects);

		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequest.getRequestTrackerId());
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