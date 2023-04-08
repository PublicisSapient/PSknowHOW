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

package com.publicissapient.kpidashboard.apis.common.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.CapacityKpiDataDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KanbanIssueCustomHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiMasterDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.apis.data.SprintWiseStoryDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.MasterResponse;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.KpiMaster;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueSprint;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
import com.publicissapient.kpidashboard.common.repository.application.KpiMasterRepository;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.excel.KanbanCapacityRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.repository.kpivideolink.KPIVideoLinkRepository;

@RunWith(MockitoJUnitRunner.class)
public class KpiHelperServiceTest {

	private List<AccountHierarchyData> ahdList = new ArrayList<>();
	private List<AccountHierarchyDataKanban> ahdKanbanList = new ArrayList<>();
	@Mock
	private JiraIssueRepository jiraIssueRepository;

	@Mock
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Mock
	private KanbanJiraIssueHistoryRepository kanbanJiraIssueHistoryRepository;

	@Mock
	private KanbanCapacityRepository kanbanCapacityRepository;

	@Mock
	private CacheService cacheService;

	@Mock
	private FilterHelperService filterHelperService;

	@Mock
	private ConfigHelperService configHelperService;

	@Mock
	private CustomApiConfig customApiConfig;

	@Mock
	private CapacityKpiDataRepository capacityKpiDataRepository;

	@Mock
	private KPIVideoLinkRepository kpiVideoLinkRepository;

	@Mock
	private KpiMasterRepository kpiMasterRepository;

	@InjectMocks
	private KpiHelperService kpiHelperService;

	@Mock
	private SprintRepository sprintRepository;

	private List<SprintDetails> sprintDetailsList = new ArrayList<>();

	private List<JiraIssueCustomHistory> issueCustomHistoryList = new ArrayList<>();

	private List<SprintWiseStory> sprintWiseStoryList = new ArrayList<>();

	private List<JiraIssue> bugList = new ArrayList<>();

	private List<JiraIssue> issueList = new ArrayList<>();

	private List<CapacityKpiData> capacityKpiDataList = new ArrayList<>();

	private KpiRequestFactory kpiRequestFactory;

	private KpiRequestFactory kanbanKpiRequestFactory;

	@Before
	public void setup() {
		AccountHierarchyFilterDataFactory factory = AccountHierarchyFilterDataFactory
				.newInstance();
		ahdList = factory.getAccountHierarchyDataList();

		AccountHierarchyKanbanFilterDataFactory kanbanFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		ahdKanbanList = kanbanFactory.getAccountHierarchyKanbanDataList();

		kpiRequestFactory = KpiRequestFactory.newInstance();
		kanbanKpiRequestFactory = KpiRequestFactory.newInstance();

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		FieldMappingDataFactory fieldMappingDataFactoryKanban = FieldMappingDataFactory
				.newInstance("/json/kanban/kanban_project_field_mappings.json");
		FieldMapping fieldMappingKanban = fieldMappingDataFactoryKanban.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMappingKanban.getBasicProjectConfigId(), fieldMappingKanban);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);

		sprintDetailsList = SprintDetailsDataFactory.newInstance().getSprintDetails();

		JiraIssueHistoryDataFactory issueHistoryFactory = JiraIssueHistoryDataFactory.newInstance();
		issueCustomHistoryList = issueHistoryFactory.getJiraIssueCustomHistory();

		SprintWiseStoryDataFactory storyFactory = SprintWiseStoryDataFactory.newInstance();
		sprintWiseStoryList = storyFactory.getSprintWiseStories();

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		bugList = jiraIssueDataFactory.getBugs();

		issueList = jiraIssueDataFactory.getJiraIssues();

		CapacityKpiDataDataFactory capacityKpiDataDataFactory = CapacityKpiDataDataFactory.newInstance();
		capacityKpiDataList = capacityKpiDataDataFactory.getCapacityKpiDataList();
	}

	@After
	public void cleanup() {

	}

	@Test
	public void testFetchDIRDataFromDb() throws ApplicationException {
		KpiRequest kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.DEFECT_INJECTION_RATE.getKpiId());
		when(jiraIssueRepository.findIssuesGroupBySprint(any(), any(), any(), any())).thenReturn(sprintWiseStoryList);

		when(jiraIssueCustomHistoryRepository.findFeatureCustomHistoryStoryProjectWise(any(), any()))
				.thenReturn(issueCustomHistoryList);
		when(jiraIssueRepository.findIssuesByType(any())).thenReturn(bugList);

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest, ahdList,
				new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);

		Map<String, Object> resultMap = kpiHelperService.fetchDIRDataFromDb(leafNodeList, kpiRequest);
		assertEquals(3, resultMap.size());
	}

	@Test
	public void testFetchQADDFromDb() throws ApplicationException {

		KpiRequest kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.DEFECT_DENSITY.getKpiId());
		when(jiraIssueRepository.findIssuesGroupBySprint(any(), any(), any(), any())).thenReturn(sprintWiseStoryList);
		when(jiraIssueCustomHistoryRepository.findFeatureCustomHistoryStoryProjectWise(any(), any()))
				.thenReturn(issueCustomHistoryList);
		when(jiraIssueRepository.findIssuesBySprintAndType(any(), any())).thenReturn(bugList);

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest, ahdList,
				new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);

		Map<String, Object> resultMap = kpiHelperService.fetchQADDFromDb(leafNodeList, kpiRequest);
		assertEquals(3, resultMap.size());
	}

	@Test
	public void testFetchSprintVelocityDataFromDb() throws ApplicationException {

		KpiRequest kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.SPRINT_VELOCITY.getKpiId());
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest, ahdList,
				new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);
		Map<String, Object> resultMap = kpiHelperService.fetchSprintVelocityDataFromDb(leafNodeList, kpiRequest);
		assertEquals(2, resultMap.size());
	}

	@Test
	public void testFetchSprintCapacityDataFromDb() throws ApplicationException {

		KpiRequest kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.SPRINT_CAPACITY_UTILIZATION.getKpiId());
		when(jiraIssueRepository.findIssuesBySprintAndType(any(), any())).thenReturn(issueList);

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest, ahdList,
				new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);

		List<JiraIssue> resultList = kpiHelperService.fetchSprintCapacityDataFromDb(leafNodeList);
		assertEquals(issueList.size(), resultList.size());
	}

	@Test
	public void testFetchCapacityDataFromDB() throws ApplicationException {

		KpiRequest kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.CAPACITY.getKpiId());

		when(capacityKpiDataRepository.findByFilters(any(), any())).thenReturn(capacityKpiDataList);

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest, ahdList,
				new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);

		List<CapacityKpiData> resultList = kpiHelperService.fetchCapacityDataFromDB(leafNodeList);
		assertEquals(4, resultList.size());
	}

	@Test
	public void testFetchTicketVelocityDataFromDb() throws ApplicationException {

		KpiRequest kpiRequest = kanbanKpiRequestFactory.findKpiRequest(KPICode.TICKET_VELOCITY.getKpiId());

		KanbanIssueCustomHistoryDataFactory issueHistoryFactory = KanbanIssueCustomHistoryDataFactory.newInstance();
		List<KanbanIssueCustomHistory> issueHistory = issueHistoryFactory.getKanbanIssueCustomHistoryDataList();
		when(kanbanJiraIssueHistoryRepository.findIssuesByStatusAndDate(any(), any(), any(), any(), any()))
				.thenReturn(issueHistory);

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), ahdKanbanList, "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);

		Map<String, Object> resultMap = kpiHelperService.fetchTicketVelocityDataFromDb(leafNodeList, "", "");
		assertEquals(2, resultMap.size());
	}

	@Test
	public void testFetchTeamCapacityDataFromDb() throws ApplicationException {

		KpiRequest kpiRequest = kanbanKpiRequestFactory.findKpiRequest(KPICode.TEAM_CAPACITY.getKpiId());

		when(kanbanCapacityRepository.findIssuesByType(any(), any(), any())).thenReturn(new ArrayList<>());

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), ahdKanbanList, "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);

		Map<String, Object> resultMap = kpiHelperService.fetchTeamCapacityDataFromDb(leafNodeList, "", "", kpiRequest,
				"");
		assertEquals(2, resultMap.size());
	}

	@Test
	public void testProcessStoryData() {
		List<JiraIssueSprint> storySprintDetails = new ArrayList<>();
		JiraIssueSprint jiraIssueSprint1 = new JiraIssueSprint();
		jiraIssueSprint1.setSprintId("sprintId");
		jiraIssueSprint1.setStatus("success");
		jiraIssueSprint1.setFromStatus("fromStatus");
		jiraIssueSprint1.setActivityDate(new DateTime(System.currentTimeMillis()));
		JiraIssueSprint jiraIssueSprint2 = new JiraIssueSprint();
		jiraIssueSprint2.setSprintId("sprintId");
		jiraIssueSprint2.setStatus("success");
		jiraIssueSprint2.setFromStatus("fromStatus");
		jiraIssueSprint2.setActivityDate(new DateTime(System.currentTimeMillis()));
		storySprintDetails.add(jiraIssueSprint1);
		storySprintDetails.add(jiraIssueSprint2);
		JiraIssueCustomHistory jiraIssueCustomHistory = new JiraIssueCustomHistory();
		jiraIssueCustomHistory.setStorySprintDetails(storySprintDetails);
		double result = kpiHelperService.processStoryData(jiraIssueCustomHistory, "fromStatus", "fromStatus");
		assertEquals(0.0, result, 0);
	}

	@Test
	public void testProcessStoryDataElseCondition() {
		List<JiraIssueSprint> storySprintDetails = new ArrayList<>();
		JiraIssueSprint jiraIssueSprint1 = new JiraIssueSprint();
		jiraIssueSprint1.setSprintId("sprintId");
		jiraIssueSprint1.setStatus("success");
		jiraIssueSprint1.setFromStatus("fromStatus");
		jiraIssueSprint1.setActivityDate(new DateTime(System.currentTimeMillis()));
		storySprintDetails.add(jiraIssueSprint1);
		JiraIssueCustomHistory jiraIssueCustomHistory = new JiraIssueCustomHistory();
		jiraIssueCustomHistory.setStorySprintDetails(storySprintDetails);
		double result = kpiHelperService.processStoryData(jiraIssueCustomHistory, "fromStatus", "fromStatus");
		assertEquals(0.0, result, 0);
	}

	@Test
	public void fetchKpiMasterList() {

		when(configHelperService.loadKpiMaster()).thenReturn(Arrays.asList(createKpiMaster()));
		MasterResponse masterResponse = kpiHelperService.fetchKpiMasterList();

		KpiMaster kpiMaster = masterResponse.getKpiList().get(0);
		assertEquals("kpi14", kpiMaster.getKpiId());
	}

	private KpiMaster createKpiMaster() {
		KpiMasterDataFactory kpiMasterDataFactory = KpiMasterDataFactory.newInstance();
		KpiMaster kpiMaster = kpiMasterDataFactory.getKpiList().get(0);
		return kpiMaster;
	}

	@Test
	public void testKpiResolution() {

		KpiRequest kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.AVERAGE_RESOLUTION_TIME.getKpiId());
		kpiHelperService.kpiResolution(kpiRequest.getKpiList());
	}

}
