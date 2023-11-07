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

import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
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

@RunWith(MockitoJUnitRunner.class)
public class NetOpenTicketCountByRCAServiceImplTest {

	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	NetOpenTicketCountByRCAServiceImpl ticketRCAServiceImpl;
	@Mock
	KpiHelperService kpiHelperService;
	@Mock
	private CommonService commonService;
	private List<AccountHierarchyDataKanban> accountHierarchyDataKanbanList = new ArrayList<>();
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private Map<String, List<DataCount>> trendValueMap = new HashMap<>();
	private List<DataCount> trendValues = new ArrayList<>();
	private KpiRequest kpiRequest;

	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();


	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("/json/default/kanban_kpi_request.json");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi50");
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setDuration("WEEKS");

		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyDataKanbanList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();
		// set aggregation criteria kpi wise
		kpiWiseAggregation.put("ticketRCA", "sum");

		setTreadValuesDataCount();
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setJiraLiveStatusNORK("");
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

	private void setTreadValuesDataCount() {
		List<DataCount> dataCountList = new ArrayList<>();
		DataCount dataCountValue = new DataCount();
		dataCountValue.setData("5");
		dataCountValue.setSProjectName("Alpha_Project1_Name");
		dataCountValue.setDate("2019-03-12");
		dataCountValue.setHoverValue(new HashMap<>());
		dataCountValue.setValue(new HashMap());
		dataCountList.add(dataCountValue);
		DataCount dataCount = new DataCount();
		dataCount.setData("Alpha_Project1_Name");
		dataCount.setValue(dataCountList);
		trendValues.add(dataCount);
		trendValueMap.put("Overall", trendValues);
		trendValueMap.put("Code Issue", trendValues);
		trendValueMap.put("Environment Issue", trendValues);
		trendValueMap.put("Functionality Not Clear", trendValues);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetTicketRCA() throws ApplicationException {

		Map<String, Map<String, Map<String, Set<String>>>> projectWiseJiraHistoryRCAAndDateWiseIssueMap = prepareProjectWiseJiraHistoryByFieldAndDate();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyDataKanbanList, "hierarchyLevelOne", 4);

		when(kpiHelperService.computeProjectWiseJiraHistoryByFieldAndDate(anyMap(), anyString(), anyMap(), anyString()))
				.thenReturn(projectWiseJiraHistoryRCAAndDateWiseIssueMap);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRAKANBAN.name()))
				.thenReturn(kpiRequestTrackerId);
		when(ticketRCAServiceImpl.getKanbanRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);
		List<KanbanIssueCustomHistory> kanbanIssueCustomHistoryDataList = KanbanIssueCustomHistoryDataFactory
				.newInstance().getKanbanIssueCustomHistoryDataList();

		Map<String, List<String>> projectWiseDoneStatus = new HashMap<>();
		projectWiseDoneStatus.put("6335368249794a18e8a4479f", Arrays.asList("Closed"));
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("JiraIssueHistoryData", kanbanIssueCustomHistoryDataList);
		resultMap.put("projectWiseClosedStoryStatus", projectWiseDoneStatus);
		when(kpiHelperService.fetchJiraCustomHistoryDataFromDbForKanban(anyList(), anyString(), anyString(), any(),
				anyString(), anyMap())).thenReturn(resultMap);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		try {
			KpiElement kpiElement = ticketRCAServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			((List<DataCountGroup>) kpiElement.getTrendValueList()).forEach(dc -> {

				String rootCause = dc.getFilter();
				switch (rootCause) {
				case "Code Issue":
					assertThat("Ticket RCA Count Value :", dc.getValue().size(), equalTo(1));
					break;
				case "Environment Issue":
					assertThat("Ticket RCA Count Value :", dc.getValue().size(), equalTo(1));
					break;
				case "Functionality Not Clear":
					assertThat("Ticket RCA Count Value :", dc.getValue().size(), equalTo(1));
					break;

				default:
					break;
				}

			});
		} catch (ApplicationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetQualifierType() {
		assertThat("Kpi Name :", ticketRCAServiceImpl.getQualifierType(), equalTo("NET_OPEN_TICKET_COUNT_BY_RCA"));
	}

	private Map<String, Map<String, Map<String, Set<String>>>> prepareProjectWiseJiraHistoryByFieldAndDate() {
		Map<String, Map<String, Map<String, Set<String>>>> projectWiseJiraHistoryRCAAndDateWiseIssueMap = new HashMap<>();
		Map<String, Map<String, Set<String>>> jiraHistoryRCAAndDateWiseIssueMap = new HashMap<>();
		Map<String, Set<String>> dateWiseIssueMap = new HashMap<>();
		Set<String> ids = new HashSet<>();
		ids.add("TEST-11232");
		ids.add("TEST-11233");
		dateWiseIssueMap.put("2022-07-01", ids);
		dateWiseIssueMap.put("2022-07-02", ids);
		jiraHistoryRCAAndDateWiseIssueMap.put("Code Issue", dateWiseIssueMap);
		jiraHistoryRCAAndDateWiseIssueMap.put("Environment Issue", dateWiseIssueMap);
		jiraHistoryRCAAndDateWiseIssueMap.put("Coding", dateWiseIssueMap);
		jiraHistoryRCAAndDateWiseIssueMap.put("Functionality Not Clear", dateWiseIssueMap);
		projectWiseJiraHistoryRCAAndDateWiseIssueMap.put("6335368249794a18e8a4479f", jiraHistoryRCAAndDateWiseIssueMap);
		return projectWiseJiraHistoryRCAAndDateWiseIssueMap;
	}

}
