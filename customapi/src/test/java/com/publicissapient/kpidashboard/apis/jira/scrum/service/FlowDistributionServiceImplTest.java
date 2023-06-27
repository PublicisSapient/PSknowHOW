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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.After;
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
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;

@RunWith(MockitoJUnitRunner.class)
public class FlowDistributionServiceImplTest {
	@Mock
	CustomApiConfig customApiConfig;
	@Mock
	CacheService cacheService;
	List<JiraIssueCustomHistory> customHistoryList = new ArrayList<>();
	@InjectMocks
	private FlowDistributionServiceImpl flowDistributionService;
	@Mock
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	private KpiRequest kpiRequest;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

	@Before
	public void setUp() throws ApplicationException {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();

		kpiRequest = kpiRequestFactory.findKpiRequest("kpi146");
		kpiRequest.setLabel("PROJECT");
		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();

		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		customHistoryList = JiraIssueHistoryDataFactory.newInstance().getJiraIssueCustomHistory();
	}

	@Test
	public void getQualifierType() {
		assertThat(flowDistributionService.getQualifierType(), equalTo(KPICode.FLOW_DISTRIBUTION.name()));
	}

	@Test
	public void testGetKpiData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(customApiConfig.getFlowKpiMonthCount()).thenReturn(1);
		String kpiRequestTrackerId = "Jira-Excel-QADD-track001";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(jiraIssueCustomHistoryRepository.findByBasicProjectConfigIdIn(Mockito.any()))
				.thenReturn(customHistoryList);
		customHistoryList.get(0).setCreatedDate(DateTime.now());
		try {
			KpiElement kpiElement = flowDistributionService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertNotNull(kpiElement.getTrendValueList());

		} catch (ApplicationException enfe) {

		}
	}

	@Test
	public void testGetQualifierType() {
		assertThat(flowDistributionService.getQualifierType(), equalTo("FLOW_DISTRIBUTION"));
	}

	@After
	public void cleanup() {
		jiraIssueCustomHistoryRepository.deleteAll();

	}
}
