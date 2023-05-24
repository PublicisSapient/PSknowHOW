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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.common.repository.jira.IssueBacklogCustomHistoryQueryRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;

@RunWith(MockitoJUnitRunner.class)
public class FlowDistributionServiceImplTest {
	@InjectMocks
	private FlowDistributionServiceImpl flowDistributionService;
	@Mock
	CustomDateRange customDateRange;
	@Mock
	CustomApiConfig customApiConfig;
	@Mock
	CacheService cacheService;
	@Mock
	private IssueBacklogCustomHistoryQueryRepository issueBacklogCustomHistoryQueryRepository;
	private KpiRequest kpiRequest;
	List<Node> leafNodeList = new ArrayList<>();
	TreeAggregatorDetail treeAggregatorDetail;
	List<JiraIssueCustomHistory> jiraHistoryDataList = new ArrayList<>();
	Map<String, Map<String, Integer>> dateTypeCountMap = new HashMap<>();

	@Before
	public void setUp() throws ApplicationException {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

		kpiRequest = kpiRequestFactory.findKpiRequest("kpi146");
		kpiRequest.setLabel("PROJECT");
		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();

		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		leafNodeList = new ArrayList<>();
		treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest, accountHierarchyDataList,
				new ArrayList<>(), "hierarchyLevelOne", 4);
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
			leafNodeList.addAll(v);
		});

		customDateRange = new CustomDateRange();
		customDateRange.setStartDate(LocalDate.now());
		customDateRange.setEndDate(LocalDate.now().minusDays(45));
		jiraHistoryDataList = JiraIssueHistoryDataFactory.newInstance().getJiraIssueCustomHistory();

	}

	@Test
	public void getQualifierType() {
		assertThat(flowDistributionService.getQualifierType(), equalTo(KPICode.FLOW_DISTRIBUTION.name()));
	}

	@Test
	public void getKpiData() throws ApplicationException {
		when(customApiConfig.getFlowKpiMonthCount()).thenReturn(12);
		String kpiRequestTrackerId = "Jira-Excel-QADD-track001";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		Map<String, Integer> typeCountMap = new HashMap<>();
		typeCountMap.put("Epic", 1);
		typeCountMap.put("Story", 3);

		String date = "2023-05-12";
		dateTypeCountMap.put(date, typeCountMap);
		when(issueBacklogCustomHistoryQueryRepository.getStoryTypeCountByDateRange(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(dateTypeCountMap);
		KpiElement responseKpiElement = flowDistributionService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail);

		assertNotNull(responseKpiElement);
		assertNotNull(responseKpiElement.getTrendValueList());
		assertEquals(responseKpiElement.getKpiId(), kpiRequest.getKpiList().get(0).getKpiId());
		List<DataCount> dataCounts = (List<DataCount>) responseKpiElement.getTrendValueList();
		assertEquals(dataCounts.get(0).getDate(), date);
	}
}
