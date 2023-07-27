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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueReleaseStatusRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

@RunWith(MockitoJUnitRunner.class)
public class RefinementRejectionRateServiceImplTest {
	private static final String UNASSIGNED_JIRA_ISSUE = "Unassigned Jira Issue";
	private static final String UNASSIGNED_JIRA_ISSUE_HISTORY = "Unassigned Jira Issue History";
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	List<JiraIssue> jiraIssueList = new ArrayList<>();
	List<JiraIssueCustomHistory> unassignedJiraHistoryDataList = new ArrayList<>();
	List<Node> leafNodeList = new ArrayList<>();
	TreeAggregatorDetail treeAggregatorDetail;

	@Mock
	ConfigHelperService configHelperService;
	@Mock
	CustomApiConfig customApiConfig;
	@InjectMocks
	RefinementRejectionRateServiceImpl refinementRejectionRateService;
	@Mock
	CustomDateRange customDateRange;
	@Mock
	private JiraIssueReleaseStatusRepository jiraIssueReleaseStatusRepository;
	@Mock
	private KpiHelperService kpiHelperService;
	@Mock
	private JiraServiceR jiraService;

	@Mock
	private JiraIssueRepository jiraIssueRepository;
	@Mock
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	private KpiRequest kpiRequest;

	@Before
	public void setup() throws ApplicationException {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

		kpiRequest = kpiRequestFactory.findKpiRequest("kpi139");
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
		configHelperService.setFieldMappingMap(fieldMappingMap);

		jiraIssueList = JiraIssueDataFactory.newInstance().getJiraIssues();
		unassignedJiraHistoryDataList = JiraIssueHistoryDataFactory.newInstance().getJiraIssueCustomHistory();

		for (FieldMapping fieldMap : FieldMappingDataFactory.newInstance(null).getFieldMappings()) {
			fieldMappingMap.put(fieldMap.getBasicProjectConfigId(), fieldMap);
		}

		unassignedJiraHistoryDataList = JiraIssueHistoryDataFactory.newInstance().getJiraIssueCustomHistory();

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {
		when(jiraIssueRepository.findUnassignedIssues(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap()))
				.thenReturn(jiraIssueList);
		when(jiraIssueCustomHistoryRepository.findByStoryIDInAndBasicProjectConfigIdIn(Mockito.anyList(),
				Mockito.anyList())).thenReturn(unassignedJiraHistoryDataList);
		when(jiraService.getJiraIssueReleaseForProject()).thenReturn(new JiraIssueReleaseStatus());
		Map<String, Object> responseRefinementList = refinementRejectionRateService.fetchKPIDataFromDb(leafNodeList,
				customDateRange.getStartDate().toString(), customDateRange.getEndDate().toString(), kpiRequest);
		assertNotNull(responseRefinementList);
		assertNotNull(responseRefinementList.get(UNASSIGNED_JIRA_ISSUE));
		assertNotNull(responseRefinementList.get(UNASSIGNED_JIRA_ISSUE_HISTORY));
		assertEquals(jiraIssueList, responseRefinementList.get(UNASSIGNED_JIRA_ISSUE));
		assertEquals(unassignedJiraHistoryDataList, responseRefinementList.get(UNASSIGNED_JIRA_ISSUE_HISTORY));
		assertEquals(jiraIssueList.get(0).getNumber(),
				((List<JiraIssue>) responseRefinementList.get(UNASSIGNED_JIRA_ISSUE)).get(0).getNumber());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetKpiData() throws ApplicationException {
		when(customApiConfig.getBacklogWeekCount()).thenReturn(5);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(jiraService.getJiraIssueReleaseForProject()).thenReturn(new JiraIssueReleaseStatus());
		KpiElement responseKpiElement = refinementRejectionRateService.getKpiData(kpiRequest,
				kpiRequest.getKpiList().get(0), treeAggregatorDetail);

		assertNotNull(responseKpiElement);
		assertNotNull(responseKpiElement.getTrendValueList());
		assertEquals(responseKpiElement.getKpiId(), kpiRequest.getKpiList().get(0).getKpiId());
		assertEquals(1, Arrays.asList(responseKpiElement.getTrendValueList()).size());

		List<DataCount> dataCounts = (List<DataCount>) responseKpiElement.getTrendValueList();
		for (DataCount dataCount : dataCounts) {
			for (DataCount values : new ArrayList<DataCount>((Collection<? extends DataCount>) dataCount.getValue())) {
				Assert.assertThat(values.getsSprintName(), StringContains.containsString("Week"));
			}

		}
	}

	@Test
	public void testGetQualifierType() {
		assertThat(refinementRejectionRateService.getQualifierType(), equalTo("REFINEMENT_REJECTION_RATE"));
	}
}
