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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.data.KanbanIssueCustomHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;

@RunWith(MockitoJUnitRunner.class)
public class TicketVelocityServiceImplTest {

	private static final String TICKETVELOCITYKEY = "ticketVelocityKey";
	private static final String SUBGROUPCATEGORY = "subGroupCategory";
	List<KanbanIssueCustomHistory> jiraHistoryList = new ArrayList<>();
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	KpiHelperService kpiHelperService;

	@Mock
	FilterHelperService filterHelperService;
	@InjectMocks
	TicketVelocityServiceImpl ticketVelocityServiceImpl;
	@Mock
	ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	FieldMappingRepository fieldMappingRepository;
	@Mock
	CustomApiConfig customApiConfig;
	@Mock
	private CommonService commonService;
	private KpiRequest kpiRequest;

	private List<AccountHierarchyDataKanban> accountHierarchyDataKanbanList = new ArrayList<>();
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("/json/default/kanban_kpi_request.json");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi49");
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setDuration("WEEKS");

		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyDataKanbanList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();

		KanbanIssueCustomHistoryDataFactory issueHistoryFactory = KanbanIssueCustomHistoryDataFactory.newInstance();
		jiraHistoryList = issueHistoryFactory
				.getKanbanIssueCustomHistoryDataListByTypeName(Arrays.asList("Story", "Defect", "Issue"));

	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<String, Object> filterComponentIdWiseDefectMap = new HashMap<>();
		filterComponentIdWiseDefectMap.put(TICKETVELOCITYKEY, jiraHistoryList);
		Double velocityValue = ticketVelocityServiceImpl.calculateKPIMetrics(filterComponentIdWiseDefectMap);
		assertThat("Velocity value :", velocityValue, equalTo(638.5));
	}

	@Test
	public void testFetchKPIDataFromDbNoData() throws ApplicationException {
		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(TICKETVELOCITYKEY, new ArrayList<KanbanJiraIssue>());
		when(kpiHelperService.fetchTicketVelocityDataFromDb(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(resultListMap);
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyDataKanbanList, "hierarchyLevelOne", 4);
		Map<String, Object> velocityListMap = ticketVelocityServiceImpl.fetchKPIDataFromDb(
				treeAggregatorDetail.getMapOfListOfProjectNodes().get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT), null,
				null, kpiRequest);
		assertThat("Velocity value :", ((List<KanbanJiraIssue>) (velocityListMap.get(TICKETVELOCITYKEY))).size(),
				equalTo(0));
	}

	@Test
	public void testGetTicketVelocityAggregationSum() throws ApplicationException {
		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(TICKETVELOCITYKEY, jiraHistoryList);
		resultListMap.put(SUBGROUPCATEGORY, "date");
		when(kpiHelperService.fetchTicketVelocityDataFromDb(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(resultListMap);
		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		when(cacheService.getFullKanbanHierarchyLevel()).thenReturn(hierachyLevelFactory.getHierarchyLevels());

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRAKANBAN.name()))
				.thenReturn(kpiRequestTrackerId);
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyDataKanbanList, "hierarchyLevelOne", 4);

		try {
			KpiElement kpiElement = ticketVelocityServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Velocity Value :",
					((List<DataCount>) ((List<DataCount>) kpiElement.getTrendValueList()).get(0).getValue()).size(),
					equalTo(7));
		} catch (ApplicationException enfe) {

		}

	}

	@Test
	public void testGetQualifierType() {
		assertThat("Kpi Name :", ticketVelocityServiceImpl.getQualifierType(), equalTo("TICKET_VELOCITY"));
	}

}
