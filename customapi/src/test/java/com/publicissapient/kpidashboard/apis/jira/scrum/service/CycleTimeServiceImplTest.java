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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.CycleTimeValidationData;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;

/** author @shi6 */
@RunWith(MockitoJUnitRunner.class)
public class CycleTimeServiceImplTest {
	private static final List<String> xAxisRange = Arrays.asList("< 16 Months", "< 3 Months", "< 1 Months", "< 2 Weeks",
			"< 1 Week");

	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	JiraServiceR jiraService;
	@Mock
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Mock
	private CustomApiConfig customApiConfig;
	@InjectMocks
	CycleTimeServiceImpl cycleTimeService;
	private KpiRequest kpiRequest;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private final Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	List<JiraIssueCustomHistory> totalJiraIssueHistoryList = new ArrayList<>();
	FieldMapping fieldMapping = null;

	@Before
	public void setUp() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi171");
		kpiRequest.setLabel("PROJECT");

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMapping.setJiraLiveStatusKPI171(Arrays.asList("Live"));
		fieldMapping.setJiraDodKPI171(Arrays.asList("Close", "Dropped"));
		fieldMapping.setStoryFirstStatusKPI171("Open");
		fieldMapping.setJiraDorKPI171(Arrays.asList("In Progress", "In Analysis"));
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);

		JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory
				.newInstance("/json/default/iteration/jira_issue_custom_history.json");
		totalJiraIssueHistoryList = jiraIssueHistoryDataFactory.getUniqueJiraIssueCustomHistory();
		// when(customApiConfig.getCycleTimeRange()).thenReturn(xAxisRange);

	}

	@Test
	public void testFetchKPIDataFromDb_positive_scenario() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList, false);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(jiraIssueCustomHistoryRepository.findByFilterAndFromStatusMapWithDateFilter(anyMap(), anyMap(), anyString(),
				anyString())).thenReturn(totalJiraIssueHistoryList);
		Map<String, Object> sprintDataListMap = cycleTimeService.fetchKPIDataFromDb(leafNodeList.get(0),
				LocalDate.now().minusMonths(6).toString(), LocalDate.now().toString(), kpiRequest);
		assertNotNull(sprintDataListMap);
	}

	@Test
	public void test_CycleTime_Positive() {
		List<CycleTimeValidationData> cycleTimeValidationDataList = new ArrayList<>();
		Set<String> issueTypes = totalJiraIssueHistoryList.stream().map(JiraIssueCustomHistory::getStoryType)
				.collect(Collectors.toSet());
		DataCount trendValue = new DataCount();
		cycleTimeService.getCycleTime(totalJiraIssueHistoryList, fieldMapping, cycleTimeValidationDataList,
				kpiRequest.getKpiList().get(0), trendValue);
		assertEquals(39, cycleTimeValidationDataList.size());
	}

	@Test
	public void testGetKpiData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		when(jiraIssueCustomHistoryRepository.findByFilterAndFromStatusMapWithDateFilter(anyMap(), anyMap(), anyString(),
				anyString())).thenReturn(totalJiraIssueHistoryList);
		String kpiRequestTrackerId = "Jira-Excel-5be544de025de212549176a9";
		// when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY
		// +
		// KPISource.JIRA.name()))
		// .thenReturn(kpiRequestTrackerId);

		KpiElement responseKpiElement = cycleTimeService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail.getMapOfListOfProjectNodes().get("project").get(0));
		assertNotNull(responseKpiElement);
		int size = ((List<IterationKpiValue>) ((DataCount) responseKpiElement.getTrendValueList()).getValue()).size();
		assertEquals(3, size);
	}

	@Test
	public void testGetQualifierType() {
		assertThat(cycleTimeService.getQualifierType(), equalTo("CYCLE_TIME"));
	}
}
