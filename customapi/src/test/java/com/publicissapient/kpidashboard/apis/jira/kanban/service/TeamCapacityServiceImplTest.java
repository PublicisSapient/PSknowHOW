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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.data.KanbanCapacityDataFactory;
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
import com.publicissapient.kpidashboard.common.model.excel.KanbanCapacity;
import com.publicissapient.kpidashboard.common.repository.excel.KanbanCapacityRepository;

@RunWith(MockitoJUnitRunner.class)
public class TeamCapacityServiceImplTest {

	private static final String TICKET_LIST = "tickets";
	private static final String SUBGROUPCATEGORY = "subGroupCategory";
	List<KanbanCapacity> capacityList = new ArrayList<>();
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	KpiHelperService kpiHelperService;

	@Mock
	FilterHelperService filterHelperService;

	private List<AccountHierarchyDataKanban> accountHierarchyDataKanbanList = new ArrayList<>();
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private KpiRequest kpiRequest;

	@InjectMocks
	private TeamCapacityServiceImpl teamCapacityServiceImpl;

	@Mock
	private KanbanCapacityRepository kanbanCapacityRepository;

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("/json/default/kanban_kpi_request.json");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi58");
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setDuration("WEEKS");

		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyDataKanbanList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();

		KanbanCapacityDataFactory kanbanCapacityDataFactory = KanbanCapacityDataFactory.newInstance();
		capacityList = kanbanCapacityDataFactory.getKanbanCapacityDataList();

		kanbanCapacityRepository.saveAll(capacityList);
		// set aggregation criteria kpi wise
		kpiWiseAggregation.put("kanbanCapacity", "sum");

	}

	@After
	public void cleanup() {
		kanbanCapacityRepository.deleteAll();
	}

	@Test
	public void testGetTeamCapacity() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyDataKanbanList, "hierarchyLevelOne", 4);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRAKANBAN.name()))
				.thenReturn(kpiRequestTrackerId);
		when(teamCapacityServiceImpl.getKanbanRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		when(cacheService.getFullKanbanHierarchyLevel()).thenReturn(hierachyLevelFactory.getHierarchyLevels());
		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(TICKET_LIST, capacityList);
		resultListMap.put(SUBGROUPCATEGORY, "date");
		when(kpiHelperService.fetchTeamCapacityDataFromDb(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq(TICKET_LIST))).thenReturn(resultListMap);
		try {
			KpiElement kpiElement = teamCapacityServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Trend Value List Size is :", ((List<DataCount>) kpiElement.getTrendValueList()).size(),
					equalTo(1));
		} catch (ApplicationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCalculateKPIMetrics() {
		assertThat(teamCapacityServiceImpl.calculateKPIMetrics(null), equalTo(null));
	}

	@Test
	public void testGetQualifierType() {
		assertThat(teamCapacityServiceImpl.getQualifierType(), equalTo("TEAM_CAPACITY"));
	}
}
