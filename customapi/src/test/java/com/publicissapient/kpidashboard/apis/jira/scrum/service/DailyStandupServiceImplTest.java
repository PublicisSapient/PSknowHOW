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

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueReleaseStatusDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.constant.Role;
import com.publicissapient.kpidashboard.common.model.application.AssigneeCapacity;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueReleaseStatusRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

@RunWith(MockitoJUnitRunner.class)
public class DailyStandupServiceImplTest {

	@Mock
	CacheService cacheService;
	@Mock
	private JiraIssueRepository jiraIssueRepository;
	@Mock
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Mock
	private ConfigHelperService configHelperService;
	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	private CapacityKpiDataRepository capacityKpiDataRepository;

	@Mock
	private FieldMappingRepository fieldMappingRepository;

	@Mock
	private JiraServiceR jiraService;

	@Mock
	private JiraIssueReleaseStatusRepository jiraIssueReleaseStatusRepository;

	@InjectMocks
	private DailyStandupServiceImpl dailyStandupService;

	private List<JiraIssue> storyList = new ArrayList<>();
	private List<JiraIssue> subTasks = new ArrayList<>();
	private List<JiraIssueCustomHistory> jiraIssueCustomHistoryList = new ArrayList<>();
	private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private SprintDetails sprintDetails = new SprintDetails();
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private JiraIssueReleaseStatus jiraReleasStatus;
	private KpiRequest kpiRequest;

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi154");
		kpiRequest.setLabel("PROJECT");

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
		subTasks = jiraIssueDataFactory.findIssueByOriginalTypeName(Arrays.asList("Sub-Task", "Task"));

		JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory.newInstance();
		jiraIssueCustomHistoryList = jiraIssueHistoryDataFactory.getJiraIssueCustomHistory();

		CapacityKpiData capacityKpiData = new CapacityKpiData();
		capacityKpiData.setBasicProjectConfigId(new ObjectId("6335363749794a18e8a4479b"));
		capacityKpiData.setCapacityPerSprint(12.0);

		when(capacityKpiDataRepository.findBySprintIDAndBasicProjectConfigId(any(), any())).thenReturn(capacityKpiData);
		JiraIssueReleaseStatusDataFactory jiraIssueReleaseStatusDataFactory = JiraIssueReleaseStatusDataFactory
				.newInstance("/json/default/jira_issue_release_status.json");
		jiraReleasStatus = createJiraReleasStatus(jiraIssueReleaseStatusDataFactory.getJiraIssueReleaseStatusList());

	}

	private JiraIssueReleaseStatus createJiraReleasStatus(List<JiraIssueReleaseStatus> jiraIssueReleaseStatusList) {

		JiraIssueReleaseStatus jiraIssueReleaseStatus = new JiraIssueReleaseStatus();
		jiraIssueReleaseStatus.setInProgressList(jiraIssueReleaseStatusList.get(0).getInProgressList());
		jiraIssueReleaseStatus.setClosedList(jiraIssueReleaseStatusList.get(0).getClosedList());
		jiraIssueReleaseStatus.setToDoList(jiraIssueReleaseStatusList.get(0).getToDoList());
		return jiraIssueReleaseStatus;
	}

	@Test
	public void getQualifierType() {
	}

	/*
	 * when DSV is called for closed sprin, the trendValueList should be empty
	 */
	@Test
	public void getKpiDataWithClosedSprint() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(jiraService.getCurrentSprintDetails()).thenReturn(sprintDetails);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		CapacityKpiData capacityKpiData = new CapacityKpiData();
		capacityKpiData.setBasicProjectConfigId(new ObjectId("6335363749794a18e8a4479b"));
		capacityKpiData.setCapacityPerSprint(12.0);

		KpiElement kpiData = dailyStandupService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail);
		assertNotNull(kpiData.getTrendValueList());

	}

	/*
	 * when DSV is called for an active sprint, with role not provided
	 */
	@Test
	public void getKpiDataWithActiveSprint() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		sprintDetails.setState(SprintDetails.SPRINT_STATE_ACTIVE);
		when(jiraService.getCurrentSprintDetails()).thenReturn(sprintDetails);
		when(jiraService.getJiraIssuesForCurrentSprint()).thenReturn(storyList);
		when(jiraService.getJiraIssuesCustomHistoryForCurrentSprint()).thenReturn(jiraIssueCustomHistoryList);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(jiraIssueRepository.findNumberInAndBasicProjectConfigIdAndTypeName(anyList(), anyString(), anyString()))
				.thenReturn(new HashSet<>(storyList));
		when(jiraIssueRepository.findByBasicProjectConfigIdAndParentStoryIdInAndOriginalTypeIn(anyString(), anySet(),
				anyList())).thenReturn(new HashSet<>(subTasks));
		when(jiraIssueReleaseStatusRepository.findByBasicProjectConfigId(anyString())).thenReturn(jiraReleasStatus);
		try {

			KpiElement kpiElement = dailyStandupService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertNotNull(kpiElement.getTrendValueList());
			List<DailyStandupServiceImpl.UserWiseCardDetail> trendValueList = (List<DailyStandupServiceImpl.UserWiseCardDetail>) kpiElement
					.getTrendValueList();
			List<DailyStandupServiceImpl.UserWiseCardDetail> unassigned = trendValueList.stream()
					.filter(issue -> issue.getRole().equalsIgnoreCase("Unassigned")).collect(Collectors.toList());
			Assert.assertEquals(unassigned.size(), 6);

		} catch (ApplicationException enfe) {

		}

	}

	/*
	 * when DSV is called for an active sprint, with role provided
	 */
	@Test
	public void getKpiDataWithActiveSprintAndCapcity() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		sprintDetails.setState(SprintDetails.SPRINT_STATE_ACTIVE);
		when(jiraService.getCurrentSprintDetails()).thenReturn(sprintDetails);
		when(jiraService.getJiraIssuesForCurrentSprint()).thenReturn(storyList);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(jiraIssueRepository.findByBasicProjectConfigIdAndParentStoryIdInAndOriginalTypeIn(anyString(), anySet(),
				anyList())).thenReturn(new HashSet<>(subTasks));
		when(jiraIssueReleaseStatusRepository.findByBasicProjectConfigId(anyString())).thenReturn(jiraReleasStatus);

		CapacityKpiData capacityKpiData = new CapacityKpiData();
		capacityKpiData.setBasicProjectConfigId(new ObjectId("6335363749794a18e8a4479b"));
		capacityKpiData.setCapacityPerSprint(12.0);

		List<AssigneeCapacity> capacityList = new ArrayList<>();
		AssigneeCapacity assigneeCapacity = new AssigneeCapacity();
		assigneeCapacity.setUserId("testUser1");
		assigneeCapacity.setUserName("testUser1");
		assigneeCapacity.setRole(Role.TESTER);
		assigneeCapacity.setAvailableCapacity(56.0);
		capacityList.add(assigneeCapacity);
		capacityKpiData.setAssigneeCapacity(capacityList);

		when(capacityKpiDataRepository.findBySprintIDAndBasicProjectConfigId(any(), any())).thenReturn(capacityKpiData);
		try {
			KpiElement kpiElement = dailyStandupService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertNotNull(kpiElement.getTrendValueList());
			List<DailyStandupServiceImpl.UserWiseCardDetail> trendValueList = (List<DailyStandupServiceImpl.UserWiseCardDetail>) kpiElement
					.getTrendValueList();
			List<DailyStandupServiceImpl.UserWiseCardDetail> unassigned = trendValueList.stream()
					.filter(issue -> issue.getRole().equalsIgnoreCase("Unassigned")).collect(Collectors.toList());
			Assert.assertEquals(unassigned.size(), 5);
			Assert.assertEquals(trendValueList.stream()
					.filter(issue -> issue.getRole().equalsIgnoreCase(Role.TESTER.getRoleValue()))
					.collect(Collectors.toList()).size(), 1);

		} catch (ApplicationException enfe) {

		}

	}

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
		fieldMapping.setJiraSubTaskIdentification(Arrays.asList("Sub-Task", "Task"));
		fieldMapping.setJiraDevDoneStatusKPI154(Arrays.asList("Ready for Testing", "Deployed"));
		fieldMapping.setJiraStatusStartDevelopmentKPI154(Arrays.asList("In Analysis", "In Development"));
		fieldMapping.setJiraQADoneStatusKPI154(Arrays.asList("In Testing"));
		fieldMapping.setJiraStatusForInProgressKPI119(
				Arrays.asList("In Analysis, In Development", "In Testing", "Ready for Testing", "Deployed"));
		fieldMapping.setJiraIterationCompletionStatusKPI154(Arrays.asList("Closed", "Dropped", "Live"));
		fieldMapping.setStoryFirstStatusKPI154(Arrays.asList("Open"));
		fieldMapping.setJiraOnHoldStatusKPI154(Arrays.asList("On Hold"));
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setFieldMappingMap(fieldMappingMap);
	}
}