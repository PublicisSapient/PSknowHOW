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

package com.publicissapient.kpidashboard.apis.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.KanbanJiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;

@RunWith(MockitoJUnitRunner.class)
public class KPIHelperUtilTest {

	List<KanbanJiraIssue> kanbanJiraIssueDataList = new ArrayList<>();
	List<JiraIssue> jiraIssueList = new ArrayList<>();
	String P1 = "p1,p1-blocker,blocker, 1, 0, p0";
	String P2 = "p2, critical, p2-critical, 2";
	String P3 = "p3, p3-major, major, 3";
	String P4 = "p4, p4-minor, minor, 4, p5-trivial, 5,trivial";
	@Mock
	JiraIssueRepository jiraIssueRepository;
	@Mock
	CustomApiConfig customApiConfig;
	@Mock
	KanbanJiraIssueRepository kanbanJiraIssueRepository;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private List<AccountHierarchyDataKanban> accountHierarchyKanbanDataList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	@InjectMocks
	private KPIHelperUtil kpiHelperUtil;
	private KpiRequest kpiRequestScrum;
	private KpiRequest kpiRequestKanban;

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequestScrum = kpiRequestFactory.findKpiRequest(KPICode.DEFECT_INJECTION_RATE.getKpiId());
		kpiRequestScrum.setLabel("PROJECT");

		kpiRequestFactory = KpiRequestFactory.newInstance("/json/default/kanban_kpi_request.json");
		kpiRequestKanban = kpiRequestFactory.findKpiRequest("kpi119");
		kpiRequestKanban.setLabel("PROJECT");

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyKanbanDataList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();

		KanbanJiraIssueDataFactory kanbanJiraIssueDataFactory = KanbanJiraIssueDataFactory.newInstance();
		kanbanJiraIssueDataList = kanbanJiraIssueDataFactory
				.getKanbanJiraIssueDataListByTypeName(Arrays.asList("Story"));

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		jiraIssueList = jiraIssueDataFactory.findIssueInTypeNames(Arrays.asList("Bug"));

		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		filterLevelMap = hierachyLevelFactory.getHierarchyLevels().stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));

	}

	@Test
	public void TestGetTreeLeafNodesGroupedByFilter() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequestScrum,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		assertThat("Root is", treeAggregatorDetail.getRoot().getGroupName(), equalTo(Filters.ROOT.name()));

		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((filter, leafNodeList) -> {

			Filters filters = Filters.getFilter(filter);
			switch (filters) {
			case SPRINT:
				assertThat("Number of leaf nodes at sprint level", leafNodeList.size(), equalTo(5));
				break;

			default:
				break;
			}

		});

	}

	@Test
	public void testGetLeafNodes() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequestScrum,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);

		assertThat("Number of total leaf nodes", leafNodeList.size(), equalTo(5));
	}

	@Test
	public void testGetLeafNodes2() {
		assertThat(KPIHelperUtil.getLeafNodes(null, new ArrayList<>()).size(), equalTo(0));
	}

	@Test
	public void testSetpriorityScrum() {

		when(customApiConfig.getpriorityP1()).thenReturn(P1);
		when(customApiConfig.getpriorityP2()).thenReturn(P2);
		when(customApiConfig.getpriorityP3()).thenReturn(P3);
		when(customApiConfig.getpriorityP4()).thenReturn(P4);

		Map<String, Long> priorityCountMap = kpiHelperUtil.setpriorityScrum(jiraIssueList, customApiConfig);
		assertThat("priority map size", priorityCountMap.size(), equalTo(4));
	}

	@Test
	public void testSetpriorityKanban() {

		when(customApiConfig.getpriorityP1()).thenReturn(P1);
		when(customApiConfig.getpriorityP2()).thenReturn(P2);
		when(customApiConfig.getpriorityP3()).thenReturn(P3);
		when(customApiConfig.getpriorityP4()).thenReturn(P4);

		Map<String, Long> priorityCountMap = kpiHelperUtil.setpriorityKanban(kanbanJiraIssueDataList, customApiConfig);
		assertThat("priority map size", priorityCountMap.size(), equalTo(2));
	}

	@After
	public void cleanup() {

	}
}
