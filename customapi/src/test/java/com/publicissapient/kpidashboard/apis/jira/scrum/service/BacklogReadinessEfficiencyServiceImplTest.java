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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.backlogdashboard.JiraBacklogServiceR;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testng.Assert;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.jira.service.SprintVelocityServiceHelper;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

/**
 * Test class for @{BacklogReadinessEfficiencyServiceImpl}
 *
 * @author dhachuda
 *
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class BacklogReadinessEfficiencyServiceImplTest {

	@Mock
	CacheService cacheService;
	@InjectMocks
	BacklogReadinessEfficiencyServiceImpl backlogReadinessEfficiencyServiceImpl;
	@Mock
	private ConfigHelperService configHelperService;
	@Mock
	private KpiHelperService kpiHelperService;
	@Mock
	private JiraBacklogServiceR jiraService;
	@Mock
	private SprintVelocityServiceHelper velocityServiceHelper;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Mock
	private JiraIssueRepository jiraIssueRepository;
	@Mock
	private FilterHelperService filterHelperService;

	private KpiRequest kpiRequest;
	private List<JiraIssue> storyList = new ArrayList<>();
	private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private SprintDetails sprintDetails = new SprintDetails();
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private List<JiraIssueCustomHistory> jiraIssueCustomHistories = new ArrayList<>();

	private void setMockProjectConfig() {
		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectConfig.setProjectName("Scrum Project");
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);
	}

	private void setMockFieldMapping() {
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setFieldMappingMap(fieldMappingMap);
	}

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi138");
		kpiRequest.setLabel("PROJECT");
		List<String> sprintList = List.of("38296_Scrum Project_6335363749794a18e8a4479b");
		Map<String,List<String>> stringListMap=new HashMap<>();
		stringListMap.put("sprint",sprintList);
		kpiRequest.setSelectedMap(stringListMap);

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		setMockProjectConfig();
		setMockFieldMapping();
		sprintDetails = SprintDetailsDataFactory.newInstance().getSprintDetails().get(0);
		List<String> jiraIssueList = sprintDetails.getTotalIssues().stream().filter(Objects::nonNull)
				.map(SprintIssue::getNumber).distinct().collect(Collectors.toList());
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		storyList = jiraIssueDataFactory.findIssueByNumberList(jiraIssueList);

		JiraIssueHistoryDataFactory jiraIssueCustomHistoryDataFactory = JiraIssueHistoryDataFactory.newInstance();
		jiraIssueCustomHistories = jiraIssueCustomHistoryDataFactory.getJiraIssueCustomHistory();
	}

	@Test(expected = IllegalStateException.class)
	public void testGetKpiDataProject_closedSprint() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 4);

		List<SprintDetails> sprintDetailsList = new ArrayList<>();
		sprintDetailsList.add(sprintDetails);
		Map<String, Object> sprintVelocityStoryMap = new HashMap<>();
		sprintVelocityStoryMap.put("sprintVelocityKey", storyList);
		sprintVelocityStoryMap.put("sprintWiseSprintDetailMap", sprintDetailsList);

		when(kpiHelperService.fetchBackLogReadinessFromdb(any(), any())).thenReturn(sprintVelocityStoryMap);

		when(jiraIssueCustomHistoryRepository.findByStoryIDIn(any())).thenReturn(jiraIssueCustomHistories);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(jiraIssueRepository.findIssuesBySprintAndType(any(), any())).thenReturn(storyList);
		when(jiraService.getJiraIssueReleaseForProject()).thenReturn(new JiraIssueReleaseStatus());
		when(backlogReadinessEfficiencyServiceImpl.getBackLogStory(new ObjectId("6335363749794a18e8a4479b")))
				.thenReturn(storyList);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(velocityServiceHelper.calculateSprintVelocityValue(any(), any(), any())).thenReturn(10.0);
		doNothing().when(velocityServiceHelper).getSprintIssuesForProject(any(), any(), any());
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(backlogReadinessEfficiencyServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(customApiConfig.getSprintCountForBackLogStrength()).thenReturn(5);
		when(filterHelperService.getFilteredBuilds(any(),any())).thenReturn(accountHierarchyDataList);
		try {
			KpiElement kpiElement = backlogReadinessEfficiencyServiceImpl.getKpiData(kpiRequest,
					kpiRequest.getKpiList().get(0), treeAggregatorDetail.getMapOfListOfProjectNodes().get("project").get(0));
			assertNotNull((DataCount) kpiElement.getTrendValueList());

		} catch (ApplicationException enfe) {

		}

	}

	@Test
	public void testGetQualifierType() {
		Assert.assertEquals(backlogReadinessEfficiencyServiceImpl.getQualifierType(), "BACKLOG_READINESS_EFFICIENCY");
	}

	@Test
	public void testGetBackLogStory() {
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(jiraIssueRepository.findIssuesBySprintAndType(any(), any())).thenReturn(storyList);
		when(jiraService.getJiraIssueReleaseForProject()).thenReturn(new JiraIssueReleaseStatus());
		List<JiraIssue> backLogStory = backlogReadinessEfficiencyServiceImpl
				.getBackLogStory(new ObjectId("6335363749794a18e8a4479b"));
		Assert.assertEquals(backLogStory.size(), storyList.size());
	}

}