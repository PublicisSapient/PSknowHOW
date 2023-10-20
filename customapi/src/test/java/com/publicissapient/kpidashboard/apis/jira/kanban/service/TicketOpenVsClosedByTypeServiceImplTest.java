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

package com.publicissapient.kpidashboard.apis.jira.kanban.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
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
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.KanbanIssueCustomHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KanbanJiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;

@RunWith(MockitoJUnitRunner.class)
public class TicketOpenVsClosedByTypeServiceImplTest {

	List<KanbanJiraIssue> kanbanJiraIssueList = new ArrayList<>();
	List<KanbanIssueCustomHistory> historyClosedList = new ArrayList<>();
	@Mock
	KanbanJiraIssueRepository kanbanJiraIssueRepository;
	@Mock
	KanbanJiraIssueHistoryRepository kanbanJiraIssueHistoryRepository;
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	TicketOpenVsClosedByTypeServiceImpl storyOpenRateByIssueServiceImpl;
	@Mock
	private FilterHelperService flterHelperService;
	@Mock
	private CommonService commonService;
	private List<AccountHierarchyDataKanban> accountHierarchyDataKanbanList = new ArrayList<>();
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private List<String> filterCategory = new ArrayList<>();
	private KpiRequest kpiRequest;

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("/json/default/kanban_kpi_request.json");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi55");
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setDuration("WEEKS");

		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyDataKanbanList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();

		KanbanJiraIssueDataFactory kanbanJiraIssueDataFactory = KanbanJiraIssueDataFactory.newInstance();
		kanbanJiraIssueList = kanbanJiraIssueDataFactory.getKanbanJiraIssueDataList();
		kanbanJiraIssueRepository.saveAll(kanbanJiraIssueList);

		historyClosedList = KanbanIssueCustomHistoryDataFactory.newInstance().getKanbanIssueCustomHistoryDataList();

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/kanban/kanban_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335368249794a18e8a4479f"));
		projectConfig.setProjectName("Kanban Project");
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);
		filterCategory.add("Project");
		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);

		// set aggregation criteria kpi wise
		kpiWiseAggregation.put("storyOpenRateByIssue", "sum");

		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		when(cacheService.getFullKanbanHierarchyLevel()).thenReturn(hierachyLevelFactory.getHierarchyLevels());


	}

	@After
	public void cleanup() {

		kanbanJiraIssueRepository.deleteAll();

	}

	private void setMockProjectConfig() {

		ProjectBasicConfig projectOne = new ProjectBasicConfig();
		projectOne.setId(new ObjectId("5b674d58f47cae8935b1b26f"));
		projectOne.setProjectName("Alpha_Project1_Name");

		ProjectBasicConfig projectTwo = new ProjectBasicConfig();
		projectTwo.setId(new ObjectId("5b719d06a500d00814bfb2b9"));
		projectTwo.setProjectName("Alpha_Project2_Name");

		ProjectBasicConfig projectThree = new ProjectBasicConfig();
		projectThree.setId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		projectThree.setProjectName("Beta_Project1_Name");

		projectConfigList.add(projectOne);
		projectConfigList.add(projectTwo);
		projectConfigList.add(projectThree);

	}

	private void setMockFieldMapping() {

		FieldMapping projectOne = new FieldMapping();
		projectOne.setBasicProjectConfigId(new ObjectId("5b674d58f47cae8935b1b26f"));
		projectOne.setTicketCountIssueType(Arrays.asList("bug"));
		projectOne.setJiraTicketClosedStatus(Arrays.asList("CLOSED"));

		FieldMapping projectTwo = new FieldMapping();
		projectTwo.setBasicProjectConfigId(new ObjectId("5b719d06a500d00814bfb2b9"));
		projectTwo.setTicketCountIssueType(Arrays.asList("Story"));
		projectTwo.setJiraTicketClosedStatus(Arrays.asList("CLOSED"));

		FieldMapping projectThree = new FieldMapping();
		projectThree.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		projectThree.setTicketCountIssueType(Arrays.asList("Story"));
		projectThree.setJiraTicketClosedStatus(Arrays.asList("CLOSED"));

		fieldMappingList.add(projectOne);
		fieldMappingList.add(projectTwo);
		fieldMappingList.add(projectThree);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetStoryOpenRateIssueType() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyDataKanbanList, "hierarchyLevelOne", 4);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("storyOpenRateByIssue", Arrays.asList("-390", "390-309", "309-221", "221-140", "140-"));
		maturityRangeMap.put("ticketPriorityWeight", Arrays.asList("10", "7", "5", "3"));

		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRAKANBAN.name()))
				.thenReturn(kpiRequestTrackerId);
		when(storyOpenRateByIssueServiceImpl.getKanbanRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(kanbanJiraIssueRepository.findIssuesByDateAndType(any(), any(), any(), any(), any()))
				.thenReturn(kanbanJiraIssueList);
		when(kanbanJiraIssueHistoryRepository.findIssuesByStatusAndDate(any(), any(), any(), any(), any()))
				.thenReturn(historyClosedList);
		Map<String, List<DataCount>> trendMap = new HashMap<>();
		trendMap.put("Overall", new ArrayList<>());
		when(commonService.sortTrendValueMap(any())).thenReturn(trendMap);

		try {
			KpiElement kpiElement = storyOpenRateByIssueServiceImpl.getKpiData(kpiRequest,
					kpiRequest.getKpiList().get(0), treeAggregatorDetail);
			List<DataCount> response = (List<DataCount>) kpiElement.getTrendValueList();
			assertEquals(1, response.size());
		} catch (ApplicationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCalculateKPIMetrics() {
		assertThat(storyOpenRateByIssueServiceImpl.calculateKPIMetrics(null), equalTo(0L));
	}

	@Test
	public void testGetQualifierType() {
		assertThat("Kpi Name :", storyOpenRateByIssueServiceImpl.getQualifierType(),
				equalTo("TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE"));
	}
}
