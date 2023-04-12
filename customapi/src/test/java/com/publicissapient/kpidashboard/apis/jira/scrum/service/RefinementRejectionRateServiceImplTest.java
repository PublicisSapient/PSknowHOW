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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;

@RunWith(MockitoJUnitRunner.class)
public class RefinementRejectionRateServiceImplTest {
	private static final String READY_FOR_REFINEMENT_ISSUE = "Ready For Refinement";
	private static final String ACCEPTED_IN_REFINEMENT_ISSUE = "Accepted In Refinement";
	private static final String REJECTED_IN_REFINEMENT_ISSUE = "Rejected In Refinement";
	private static final String UNASSIGNED_JIRA_ISSUE = "Unassigned Jira Issue";
	private static final String UNASSIGNED_JIRA_ISSUE_HISTORY = "Unassigned Jira Issue History";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	List<JiraIssue> jiraIssueList = new ArrayList<>();
	List<JiraIssueCustomHistory> unassignedJiraHistoryDataList = new ArrayList<>();
	Map<String, Object> refinementList = new HashMap<>();
	List<Node> leafNodeList = new ArrayList<>();
	Map<String, List<Map<String, Object>>> projectWiseMap = new HashMap<>();
	TreeAggregatorDetail treeAggregatorDetail;
	Map<String, Object> defaultMap = new HashMap<>();
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@Mock
	CustomApiConfig customApiConfig;
	@InjectMocks
	RefinementRejectionRateServiceImpl refinementRejectionRateService;
	@Mock
	CustomDateRange customDateRange;
	private KpiRequest kpiRequest;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private KpiElement kpiElement;
	private Map<String, DateTime> jiraDateMap;
//	private FieldMapping fieldMappingData;

	@Before
	public void setup() throws ApplicationException {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi137");
		kpiRequest.setLabel("PROJECT");
		// List<KpiElement> kpiElementList = new ArrayList<>();
		// kpiElementList.add(new KpiElement());
		kpiElement = kpiRequest.getKpiList().get(0);
		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		leafNodeList = new ArrayList<>();
		treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest, accountHierarchyDataList,
				new ArrayList<>(), "hierarchyLevelOne", 10);
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {

			leafNodeList.addAll(v);
		});

		customDateRange = new CustomDateRange();
		customDateRange.setStartDate(LocalDate.now());
		customDateRange.setEndDate(LocalDate.now().minusDays(45));

		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);

		jiraIssueList = JiraIssueDataFactory.newInstance().getJiraIssues();
		unassignedJiraHistoryDataList = JiraIssueHistoryDataFactory.newInstance().getJiraIssueCustomHistory();

		refinementList = new HashMap<>();
		refinementList.put(UNASSIGNED_JIRA_ISSUE, jiraIssueList);
		refinementList.put(UNASSIGNED_JIRA_ISSUE_HISTORY, unassignedJiraHistoryDataList);

		defaultMap.put(READY_FOR_REFINEMENT_ISSUE, jiraIssueList);
		defaultMap.put(REJECTED_IN_REFINEMENT_ISSUE, jiraIssueList);
		defaultMap.put(ACCEPTED_IN_REFINEMENT_ISSUE, jiraIssueList);

		jiraDateMap = new HashMap<>();

		for (JiraIssue jiraissue:jiraIssueList) {
			jiraDateMap.put(jiraissue.getNumber(), LocalDateTime.now().toDateTime().minusDays((int) Math.random()))	;		
		}

		for (FieldMapping fieldMap:FieldMappingDataFactory.newInstance(null).getFieldMappings()) {
			fieldMappingMap.put(fieldMap.getBasicProjectConfigId(),fieldMap);
		}

		List<Map<String, Object>> dataList = new ArrayList<>();
		for (String key : defaultMap.keySet()) {
			Map<String, Object> dataMap = new HashMap<>();
			dataMap.put(key, defaultMap.get(key));
			dataList.add(dataMap);
		}

		projectWiseMap.put(leafNodeList.get(0).getId(), dataList);

		unassignedJiraHistoryDataList = JiraIssueHistoryDataFactory.newInstance().getJiraIssueCustomHistory();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {

		when(kpiHelperService.getUnAssignedIssueDataMap(leafNodeList, customDateRange.getStartDate().toString(),
				customDateRange.getEndDate().toString())).thenReturn(refinementList);

		Map<String, Object> responseRefinementList = refinementRejectionRateService.fetchKPIDataFromDb(leafNodeList,
				customDateRange.getStartDate().toString(), customDateRange.getEndDate().toString(), kpiRequest);
		assertNotNull(responseRefinementList);
		assertNotNull(responseRefinementList.get(UNASSIGNED_JIRA_ISSUE));
		assertNotNull(responseRefinementList.get(UNASSIGNED_JIRA_ISSUE_HISTORY));
		assertEquals(refinementList, responseRefinementList);
		assertEquals(refinementList.get(UNASSIGNED_JIRA_ISSUE), responseRefinementList.get(UNASSIGNED_JIRA_ISSUE));
		assertEquals(refinementList.get(UNASSIGNED_JIRA_ISSUE_HISTORY),
				responseRefinementList.get(UNASSIGNED_JIRA_ISSUE_HISTORY));
		assertEquals(((List<JiraIssue>) refinementList.get(UNASSIGNED_JIRA_ISSUE)).get(0).getNumber(),
				((List<JiraIssue>) responseRefinementList.get(UNASSIGNED_JIRA_ISSUE)).get(0).getNumber());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetKpiData() throws ApplicationException {
		when(customApiConfig.getBacklogDayCount()).thenReturn(35);
		when(customApiConfig.getBacklogWeekCount()).thenReturn(5);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(refinementRejectionRateService.fetchKPIDataFromDb(Mockito.any(), Mockito.anyString(),Mockito.anyString(),kpiRequest)).thenReturn(refinementList);
		
		KpiElement responseKpiElement = refinementRejectionRateService.getKpiData(kpiRequest,
				kpiRequest.getKpiList().get(0), treeAggregatorDetail);

		assertNotNull(responseKpiElement);
		assertNotNull(responseKpiElement.getTrendValueList());
		assertEquals(responseKpiElement.getKpiId(), kpiRequest.getKpiList().get(0).getKpiId());
		assertEquals(Arrays.asList(responseKpiElement.getTrendValueList()).size(),1);
	}

	@Test
	public void testGetQualifierType() {
		assertThat(refinementRejectionRateService.getQualifierType(), equalTo("REFINEMENT_REJECTION_RATE"));
	}
}
