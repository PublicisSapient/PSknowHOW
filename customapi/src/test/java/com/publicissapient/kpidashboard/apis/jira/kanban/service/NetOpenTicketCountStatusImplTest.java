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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.KanbanIssueCustomHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;

@RunWith(MockitoJUnitRunner.class)
public class NetOpenTicketCountStatusImplTest {

	private static final String PROJECT_WISE_CLOSED_STORY_STATUS = "projectWiseClosedStoryStatus";
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@Mock
	KanbanJiraIssueRepository kanbanJiraIssueRepository;
	@InjectMocks
	NetOpenTicketCountStatusImpl totalTicketCountImpl;
	private List<AccountHierarchyDataKanban> accountHierarchyDataKanbanList = new ArrayList<>();
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private Map<String, List<DataCount>> trendValueMap = new HashMap<>();
	private List<DataCount> trendValues = new ArrayList<>();
	@Mock
	private CommonService commonService;
	private KpiRequest kpiRequest;
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("/json/default/kanban_kpi_request.json");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi48");
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setDuration("WEEKS");

		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyDataKanbanList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();

		// set aggregation criteria kpi wise
		kpiWiseAggregation.put(KPICode.NET_OPEN_TICKET_COUNT_BY_STATUS.name(), "sum");
		setTreadValuesDataCount();
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setJiraLiveStatusNOSK("");
		fieldMapping.setJiraTicketClosedStatus(Collections.EMPTY_LIST);
		fieldMapping.setKanbanRCACountIssueType(Collections.EMPTY_LIST);
		fieldMapping.setTicketCountIssueType(Collections.EMPTY_LIST);
		fieldMapping.setStoryFirstStatus("");
		fieldMappingMap.put(new ObjectId("6335368249794a18e8a4479f"), fieldMapping);
		configHelperService.setFieldMappingMap(fieldMappingMap);
		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		when(cacheService.getFullKanbanHierarchyLevel()).thenReturn(hierachyLevelFactory.getHierarchyLevels());

	}

	@After
	public void cleanup() {

	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<String, Map<String, Map<String, Set<String>>>> filterComponentIdWiseDefectMap = new HashMap<>();
		Long stringLongMap = totalTicketCountImpl.calculateKPIMetrics(filterComponentIdWiseDefectMap);
		assertThat("ticket count :", stringLongMap, equalTo(0L));
	}

	private void setTreadValuesDataCount() {
		List<DataCount> dataCountList = new ArrayList<>();
		dataCountList.add(createDataCount("2022-07-27", "0", "Open"));
		dataCountList.add(createDataCount("2022-07-26", "0", "Open"));
		dataCountList.add(createDataCount("2022-07-25", "0", "Open"));
		dataCountList.add(createDataCount("2022-07-24", "0", "Open"));
		dataCountList.add(createDataCount("2022-07-23", "0", "Open"));
		dataCountList.add(createDataCount("2022-07-22", "0", "Open"));
		dataCountList.add(createDataCount("2022-07-21", "0", "Open"));

		List<DataCount> dataCountList1 = new ArrayList<>();
		dataCountList1.add(createDataCount("2022-07-27", "0", "In Analysis"));
		dataCountList1.add(createDataCount("2022-07-26", "0", "In Analysis"));
		dataCountList1.add(createDataCount("2022-07-25", "0", "In Analysis"));
		dataCountList1.add(createDataCount("2022-07-24", "0", "In Analysis"));
		dataCountList1.add(createDataCount("2022-07-23", "0", "In Analysis"));
		dataCountList1.add(createDataCount("2022-07-22", "0", "In Analysis"));
		dataCountList1.add(createDataCount("2022-07-21", "0", "In Analysis"));

		DataCount dataCountValue = new DataCount();
		dataCountValue.setData("5");
		dataCountValue.setSProjectName("Kanban Project");
		dataCountValue.setDate("2019-03-12");
		dataCountValue.setHoverValue(new HashMap<>());
		dataCountValue.setValue(new HashMap());

		dataCountList.add(dataCountValue);

		DataCount dataCount = new DataCount();
		dataCount.setData("Kanban Project");
		dataCount.setValue(dataCountList);
		trendValues.add(dataCount);
		trendValueMap.put("In Analysis", dataCountList1);
		trendValueMap.put("Open", dataCountList);
	}

	private DataCount createDataCount(String date, String data, String group) {
		DataCount dataCount = new DataCount();
		dataCount.setData(data);
		dataCount.setSProjectName("Kanban Project");
		dataCount.setSSprintID(data);
		dataCount.setSSprintName(data);
		dataCount.setDate(date);
		dataCount.setHoverValue(new HashMap<>());
		dataCount.setValue(Long.valueOf(data));
		dataCount.setKpiGroup(group);
		return dataCount;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testNetOpenTicketByStatus() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyDataKanbanList, "hierarchyLevelOne", 4);
		when(kpiHelperService.fetchJiraCustomHistoryDataFromDbForKanban(any(), any(), any(), any(), any(), anyMap()))
				.thenReturn(createResultMap());
		Map<String, Map<String, Map<String, Set<String>>>> projectWiseJiraHistoryStatusAndDateWiseIssueMap = prepareProjectWiseJiraHistoryByStatusAndDate();
		when(kpiHelperService.computeProjectWiseJiraHistoryByStatusAndDate(anyMap(), anyString(), anyMap()))
				.thenReturn(projectWiseJiraHistoryStatusAndDateWiseIssueMap);
		List<KanbanIssueCustomHistory> kanbanIssueCustomHistoryDataList = KanbanIssueCustomHistoryDataFactory
				.newInstance().getKanbanIssueCustomHistoryDataList();

		Map<String, List<String>> projectWiseDoneStatus = new HashMap<>();
		projectWiseDoneStatus.put("6335368249794a18e8a4479f", Arrays.asList("Closed"));
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("JiraIssueHistoryData", kanbanIssueCustomHistoryDataList);
		resultMap.put("projectWiseClosedStoryStatus", projectWiseDoneStatus);
		when(kpiHelperService.fetchJiraCustomHistoryDataFromDbForKanban(anyList(), anyString(), anyString(), any(),
				anyString(), anyMap())).thenReturn(resultMap);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRAKANBAN.name()))
				.thenReturn(kpiRequestTrackerId);
		when(totalTicketCountImpl.getKanbanRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		try {
			KpiElement kpiElement = totalTicketCountImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			((List<DataCountGroup>) kpiElement.getTrendValueList()).forEach(dc -> {

				String status = dc.getFilter();
				switch (status) {
				case "In Analysis":
					assertThat("Ticket Analysis Count Value :", dc.getValue().size(), equalTo(7));
					break;
				case "Open":
					assertThat("Ticket Open Count Value :", dc.getValue().size(), equalTo(8));
					break;
				default:
					break;
				}

			});
		} catch (ApplicationException e) {
			e.printStackTrace();
		}
	}

	private Map<String, Object> createResultMap() {
		Map<String, Object> resultMap = new HashMap<>();
		Map<String, List<String>> projectWiseClosedStatusMap = new HashMap<>();
		projectWiseClosedStatusMap.put("6335368249794a18e8a4479f", Arrays.asList("Closed"));
		resultMap.put(PROJECT_WISE_CLOSED_STORY_STATUS, projectWiseClosedStatusMap);
		return resultMap;
	}

	private Map<String, Map<String, Map<String, Set<String>>>> prepareProjectWiseJiraHistoryByStatusAndDate() {
		Map<String, Map<String, Map<String, Set<String>>>> projectWiseJiraHistoryStatusAndDateWiseIssueMap = new HashMap<>();
		Map<String, Map<String, Set<String>>> jiraHistoryStatusAndDateWiseIssueMap = new HashMap<>();
		Map<String, Set<String>> dateWiseIssueMap = new HashMap<>();
		Set<String> ids = new HashSet<>();
		ids.add("TEST-11232");
		ids.add("TEST-11233");
		dateWiseIssueMap.put("2022-07-01", ids);
		dateWiseIssueMap.put("2022-07-02", ids);
		jiraHistoryStatusAndDateWiseIssueMap.put("Open", dateWiseIssueMap);
		jiraHistoryStatusAndDateWiseIssueMap.put("In Analysis", dateWiseIssueMap);
		projectWiseJiraHistoryStatusAndDateWiseIssueMap.put("6335368249794a18e8a4479f",
				jiraHistoryStatusAndDateWiseIssueMap);
		return projectWiseJiraHistoryStatusAndDateWiseIssueMap;
	}

	@Test
	public void testGetQualifierType() {
		assertThat("Kpi Name :", totalTicketCountImpl.getQualifierType(), equalTo("NET_OPEN_TICKET_COUNT_BY_STATUS"));
	}

}
