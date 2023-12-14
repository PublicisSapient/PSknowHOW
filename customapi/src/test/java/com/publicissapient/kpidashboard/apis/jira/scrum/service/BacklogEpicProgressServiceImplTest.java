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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
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
import com.publicissapient.kpidashboard.apis.data.JiraIssueReleaseStatusDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

/**
 * test file of release progress of backlog dashboard
 * 
 * @author shi6
 */
@RunWith(MockitoJUnitRunner.class)
public class BacklogEpicProgressServiceImplTest {
	private static final String TOTAL_ISSUES = "totalIssues";
	private static final String EPIC_LINKED = "epicLinked";
	private static final String RELEASE_JIRA_ISSUE_STATUS = "releaseJiraIssueStatus";
	private static final String TO_DO = "To Do";
	private static final String IN_PROGRESS = "In Progress";
	private static final String DONE = "Done";

	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	private JiraServiceR jiraService;
	@Mock
	JiraIssueRepository jiraIssueRepository;
	@InjectMocks
	private BacklogEpicProgressServiceImpl epicProgressService;
	private KpiRequest kpiRequest;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private List<JiraIssue> jiraIssueArrayList = new ArrayList<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private List<JiraIssueReleaseStatus> jiraIssueReleaseStatusList;
	private FieldMapping fieldMapping;

	@Before
	public void setUp() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi169");
		kpiRequest.setLabel("BACKLOG");
		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance("/json/default/account_hierarchy_filter_data_release.json");
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		jiraIssueArrayList = jiraIssueDataFactory.getJiraIssues();
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		JiraIssueReleaseStatusDataFactory jiraIssueReleaseStatusDataFactory = JiraIssueReleaseStatusDataFactory
				.newInstance("/json/default/jira_issue_release_status.json");
		jiraIssueReleaseStatusList = jiraIssueReleaseStatusDataFactory.getJiraIssueReleaseStatusList();
	}

	/*
	 * testing the sorting method
	 */
	@Test
	public void testSorting() {
		List<DataCount> dataCountList = createDataCountList();
		epicProgressService.sorting(dataCountList);
		assertThat(dataCountList).isSortedAccordingTo((dataCount1, dataCount2) -> {
			long sum1 = ((List<DataCount>) dataCount1.getValue()).stream()
					.filter(subfilter -> subfilter.getSubFilter().equalsIgnoreCase(TO_DO)
							|| subfilter.getSubFilter().equalsIgnoreCase(IN_PROGRESS))
					.mapToLong(a -> (long) a.getValue()).sum();

			long sum2 = ((List<DataCount>) dataCount2.getValue()).stream()
					.filter(subfilter -> subfilter.getSubFilter().equalsIgnoreCase(TO_DO)
							|| subfilter.getSubFilter().equalsIgnoreCase(IN_PROGRESS))
					.mapToLong(a -> (long) a.getValue()).sum();

			return Long.compare(sum1, sum2);
		});
	}

	/*
	 * when from db we get some data
	 */
	@Test
	public void testFetchKPIDataFromDbPositiveScenario() throws ApplicationException {
		List<Node> leafNodeList = new ArrayList<>();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.PROJECT) {
				leafNodeList.addAll(v);
			}
		});
		Set<JiraIssue> epic = jiraIssueArrayList.stream()
				.filter(jiraIssue -> jiraIssue.getTypeName().equalsIgnoreCase("Epic")).collect(Collectors.toSet());
		when(jiraIssueRepository.findNumberInAndBasicProjectConfigIdAndTypeName(anyList(), anyString(), anyString()))
				.thenReturn(epic);
		when(jiraService.getJiraIssueReleaseForProject()).thenReturn(jiraIssueReleaseStatusList.get(0));
		when(jiraService.getJiraIssuesForCurrentSprint()).thenReturn(jiraIssueArrayList);
		Map<String, Object> resultListMap = epicProgressService.fetchKPIDataFromDb(leafNodeList, "", "", kpiRequest);

		Assertions.assertThat(resultListMap).isNotEmpty();
		Assertions.assertThat(resultListMap.containsKey(TOTAL_ISSUES)).isTrue();
		Assertions.assertThat(((List<JiraIssue>) resultListMap.get(TOTAL_ISSUES)).size()).isGreaterThan(0);
		Assertions.assertThat(resultListMap.containsKey(EPIC_LINKED)).isTrue();
		Assertions.assertThat(((Set<JiraIssue>) resultListMap.get(EPIC_LINKED)).size()).isGreaterThan(0);
		Assertions.assertThat(resultListMap.containsKey(RELEASE_JIRA_ISSUE_STATUS)).isTrue();
	}

	/*
	 * when from db no data is found
	 */
	@Test
	public void testFetchKPIDataFromDbNegativeScenario() throws ApplicationException {
		// Create a list of leaf nodes with a valid project filter.
		List<Node> leafNodeList = new ArrayList<>();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.PROJECT) {
				leafNodeList.addAll(v);
			}
		});
		Map<String, Object> resultListMap = epicProgressService.fetchKPIDataFromDb(leafNodeList, "", "", kpiRequest);
		assertThat(((Set<JiraIssue>) resultListMap.get(EPIC_LINKED)).size()).isEqualTo(0);
	}

	@Test
	public void testGetStatusWiseCountListPositive() {
		Map.Entry<String, String> epicUrl = new AbstractMap.SimpleEntry<>("EPIC", "url");
		DataCount dataCount = epicProgressService.getStatusWiseCountList(jiraIssueArrayList,
				jiraIssueReleaseStatusList.get(0), epicUrl, fieldMapping);
		assertThat(dataCount.getData()).isEqualTo("44");
		assertThat(dataCount.getSize()).isEqualTo("60.0");
		DataCount toDoCount = ((List<DataCount>) dataCount.getValue()).get(0);
		assertThat(toDoCount.getValue()).isEqualTo(5L);
		assertThat(toDoCount.getSize()).isEqualTo(4.0);
		assertThat(toDoCount.getSubFilter()).isEqualTo(TO_DO);
		DataCount inProgressCount = ((List<DataCount>) dataCount.getValue()).get(1);
		assertThat(inProgressCount.getValue()).isEqualTo(0L);
		assertThat(inProgressCount.getSize()).isEqualTo(0.0);
		assertThat(inProgressCount.getSubFilter()).isEqualTo(IN_PROGRESS);

		DataCount doneCount = ((List<DataCount>) dataCount.getValue()).get(2);
		assertThat(doneCount.getValue()).isEqualTo(39L);
		assertThat(doneCount.getSize()).isEqualTo(56.0);
		assertThat(doneCount.getSubFilter()).isEqualTo(DONE);
	}

	/*
	 * get status wise count negative case when jiraIssue list is empty
	 */
	@Test
	public void testGetStatusWiseCountListNegative() {
		List<JiraIssue> jiraIssueList = null;
		Map.Entry<String, String> epicUrl = new AbstractMap.SimpleEntry<>("EPIC", "url");
		DataCount dataCount = epicProgressService.getStatusWiseCountList(jiraIssueList,
				jiraIssueReleaseStatusList.get(0), epicUrl, fieldMapping);
		assertThat(dataCount.getData()).isEqualTo("0");
	}

	/*
	 * creating epic wise story size map
	 */
	@Test
	public void testCreateDataCountGroupMapPositiveScenario() {
		jiraIssueArrayList.stream().filter(jiraIssue -> !jiraIssue.getTypeName().equalsIgnoreCase("Epic"))
				.forEach(jiraIssue -> jiraIssue.setEpicLinked("EPIC-2"));
		Set<JiraIssue> epicIssues = jiraIssueArrayList.stream()
				.filter(jiraIssue -> jiraIssue.getTypeName().equalsIgnoreCase("Epic")).collect(Collectors.toSet());

		List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
		Map<String, String> epicWiseSize = epicProgressService.createDataCountGroupMap(jiraIssueArrayList,
				jiraIssueReleaseStatusList.get(0), epicIssues, fieldMapping, iterationKpiValues);

		assertThat(epicWiseSize).hasSize(1);
		assertThat(epicWiseSize.get("-")).isEqualTo(null);
		assertThat(epicWiseSize.get("EPIC-2")).isEqualTo("57.0");
		assertThat(iterationKpiValues).hasSize(2);
		assertThat(iterationKpiValues.get(0).getValue()).hasSize(1);
	}

	/*
	 * testing complete epic progress kpi
	 */
	@Test
	public void getKpiData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		String kpiRequestTrackerId = "Jira-Excel-QADD-track001";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		Set<JiraIssue> epic = jiraIssueArrayList.stream()
				.filter(jiraIssue -> jiraIssue.getTypeName().equalsIgnoreCase("Epic")).collect(Collectors.toSet());
		when(jiraIssueRepository.findNumberInAndBasicProjectConfigIdAndTypeName(anyList(), anyString(), anyString()))
				.thenReturn(epic);
		jiraIssueArrayList.stream().filter(jiraIssue -> !jiraIssue.getTypeName().equalsIgnoreCase("Epic"))
				.forEach(jiraIssue -> jiraIssue.setEpicLinked("EPIC-1"));
		when(jiraService.getJiraIssuesForCurrentSprint()).thenReturn(jiraIssueArrayList);
		when(jiraService.getJiraIssueReleaseForProject()).thenReturn(jiraIssueReleaseStatusList.get(0));
		KpiElement kpiElement = epicProgressService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail);
		assertNotNull(kpiElement.getTrendValueList());
	}

	@Test
	public void getQualitfierType() {
		assertThat(epicProgressService.getQualifierType()).isEqualTo(KPICode.BACKLOG_EPIC_PROGRESS.name());
	}

	private List<DataCount> createDataCountList() {
		List<DataCount> fullDataCount = new ArrayList<>();
		List<DataCount> statusWiseDataCount1 = new ArrayList<>();
		DataCount toDodataCount = new DataCount(TO_DO, 4L, 3.0, null);
		DataCount inProgressDataCount = new DataCount(IN_PROGRESS, 4L, 3.0, null);
		DataCount doneDataCount = new DataCount(DONE, 4L, 3.0, null);
		statusWiseDataCount1.add(toDodataCount);
		statusWiseDataCount1.add(inProgressDataCount);
		statusWiseDataCount1.add(doneDataCount);
		DataCount fullDataCount1 = new DataCount();
		fullDataCount1.setData("8");
		fullDataCount1.setValue(statusWiseDataCount1);
		fullDataCount1.setKpiGroup("epic1");
		fullDataCount.add(fullDataCount1);

		List<DataCount> statusWiseDataCount2 = new ArrayList<>();
		DataCount toDodataCount2 = new DataCount(TO_DO, 0L, 0.0, null);
		DataCount inProgressDataCount2 = new DataCount(IN_PROGRESS, 6L, 23.0, null);
		DataCount doneDataCount2 = new DataCount(DONE, 4L, 8.0, null);
		statusWiseDataCount2.add(toDodataCount2);
		statusWiseDataCount2.add(inProgressDataCount2);
		statusWiseDataCount2.add(doneDataCount2);
		DataCount fullDataCount2 = new DataCount();
		fullDataCount2.setData("10");
		fullDataCount2.setValue(statusWiseDataCount2);
		fullDataCount2.setKpiGroup("epic2");
		fullDataCount.add(fullDataCount2);
		return fullDataCount;

	}

}