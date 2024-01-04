/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

@RunWith(MockitoJUnitRunner.class)
public class IterationReadinessServiceImplTest {

	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	JiraServiceR jiraService;
	@InjectMocks
	IterationReadinessServiceImpl iterationReadinessService;
	private KpiRequest kpiRequest;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private final Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private List<SprintDetails> sprintDetailsList = new ArrayList<>();
	private List<String> sprintList = new ArrayList<>();
	private List<JiraIssue> issueList = new ArrayList<>();

	@Before
	public void setUp() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi161");
		kpiRequest.setLabel("PROJECT");

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		issueList = JiraIssueDataFactory.newInstance().getJiraIssues();

		sprintDetailsList = SprintDetailsDataFactory.newInstance().getSprintDetails();
		sprintList = sprintDetailsList.stream().map(SprintDetails::getSprintName).distinct()
				.collect(Collectors.toList());

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);

	}

	@Test
	public void getQualifierType() {
		assertThat(iterationReadinessService.getQualifierType(), equalTo(KPICode.ITERATION_READINESS_KPI.name()));
	}

	@Test
	public void testFetchKPIDataFromDb_NullData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(jiraService.getJiraIssuesForCurrentSprint()).thenReturn(issueList);
		when(jiraService.getFutureSprintsList()).thenReturn(new ArrayList<>());
		Map<String, Object> sprintDataListMap = iterationReadinessService.fetchKPIDataFromDb(leafNodeList, null, null,
				kpiRequest);
		assertNotNull(sprintDataListMap);
	}

	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(jiraService.getJiraIssuesForCurrentSprint()).thenReturn(issueList);
		when(jiraService.getFutureSprintsList()).thenReturn(sprintList);
		Map<String, Object> sprintDataListMap = iterationReadinessService.fetchKPIDataFromDb(leafNodeList, null, null,
				kpiRequest);
		assertNotNull(sprintDataListMap);
	}

	@Test
	public void testGetKpiData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		String kpiRequestTrackerId = "Jira-Excel-QADD-track001";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(jiraService.getJiraIssuesForCurrentSprint()).thenReturn(issueList);
		when(jiraService.getFutureSprintsList()).thenReturn(sprintList);
		try {
			KpiElement kpiElement = iterationReadinessService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertNotNull(kpiElement.getTrendValueList());

		} catch (ApplicationException enfe) {

		}
	}

}