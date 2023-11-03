package com.publicissapient.kpidashboard.apis.jira.kanban.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
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
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.KanbanJiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;

@RunWith(MockitoJUnitRunner.class)
public class OpenTicketAgingByPriorityServiceImplTest {

	private static final String P1 = "p1,P1 - Blocker, blocker, 1, 0, p0, Urgent";
	private static final String P2 = "p2, critical, P2 - Critical, 2, High";
	private static final String P3 = "p3, P3 - Major, major, 3, Medium";
	private static final String P4 = "p4, P4 - Minor, minor, 4, Low";
	private static final String RANGE_TICKET_LIST = "rangeTickets";

	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	List<KanbanJiraIssue> kanbanJiraIssueList = new ArrayList<>();
	@Mock
	KanbanJiraIssueRepository kanbanJiraIssueRepository;
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@InjectMocks
	OpenTicketAgingByPriorityServiceImpl openTicketAgingByPriorityService;
	@Mock
	private FilterHelperService flterHelperService;
	private List<AccountHierarchyDataKanban> accountHierarchyDataKanbanList = new ArrayList<>();
	private Map<String, List<DataCount>> trendValueMap = new HashMap<>();
	private List<DataCount> trendValues = new ArrayList<>();
	@Mock
	private CustomApiConfig customApiConfig;

	@Mock
	private CommonService commonService;
	private KpiRequest kpiRequest;

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("/json/default/kanban_kpi_request.json");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi997");
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setDuration("WEEKS");

		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyDataKanbanList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/kanban/kanban_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);

		List<String> jiraTicketClosedStatus = fieldMapping.getJiraTicketClosedStatus();
		jiraTicketClosedStatus.add(fieldMapping.getJiraLiveStatusOTA());
		jiraTicketClosedStatus.addAll(fieldMapping.getJiraTicketRejectedStatus());

		KanbanJiraIssueDataFactory kanbanJiraIssueDataFactory = KanbanJiraIssueDataFactory.newInstance();
		kanbanJiraIssueList = kanbanJiraIssueDataFactory.getKanbanJiraIssueDataListByTypeNameandStatus(
				fieldMapping.getTicketCountIssueType(), jiraTicketClosedStatus);
		kanbanJiraIssueRepository.saveAll(kanbanJiraIssueList);

		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335368249794a18e8a4479f"));
		projectConfig.setProjectName("Kanban Project");
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);

		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);
		setTreadValuesDataCount();
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
		dataCountValue.setSProjectName("Kanban Project");
		dataCountValue.setDate("2019-03-12");
		dataCountValue.setHoverValue(new HashMap<>());
		dataCountValue.setValue(5L);
		dataCountList.add(dataCountValue);
		DataCount dataCount = new DataCount();
		dataCount.setData("Kanban Project");
		dataCount.setValue(dataCountList);
		trendValues.add(dataCount);
		trendValueMap.put("Overall", trendValues);
		trendValueMap.put("P1", trendValues);
		trendValueMap.put("P2", trendValues);
		trendValueMap.put("P4", trendValues);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetTicketAgingByPriority() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyDataKanbanList, "hierarchyLevelOne", 4);

		when(customApiConfig.getTotalDefectCountAgingXAxisRange())
				.thenReturn(new ArrayList<>(Arrays.asList("0-1", "1-3", "3-6", "6-12", ">12")));
		when(kanbanJiraIssueRepository.findIssuesByDateAndTypeAndStatus(any(), any(), any(), any(), any(), any()))
				.thenReturn(kanbanJiraIssueList);

		when(customApiConfig.getpriorityP1()).thenReturn(P1);
		when(customApiConfig.getpriorityP2()).thenReturn(P2);
		when(customApiConfig.getpriorityP3()).thenReturn(P3);
		when(customApiConfig.getpriorityP4()).thenReturn(P4);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRAKANBAN.name()))
				.thenReturn(kpiRequestTrackerId);
		when(openTicketAgingByPriorityService.getKanbanRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		try {
			KpiElement kpiElement = openTicketAgingByPriorityService.getKpiData(kpiRequest,
					kpiRequest.getKpiList().get(0), treeAggregatorDetail);

			((List<DataCountGroup>) kpiElement.getTrendValueList()).forEach(dc -> {

				String priority = dc.getFilter();
				switch (priority) {
				case "P1":
					assertThat("Ticket Priority Count Value :", dc.getValue().size(), equalTo(1));
					break;
				case "P2":
					assertThat("Ticket Priority Count Value :", dc.getValue().size(), equalTo(1));
					break;
				case "P3":
					assertThat("Ticket Priority Count Value :", dc.getValue().size(), equalTo(1));
					break;
				case "P4":
					assertThat("Ticket Priority Count Value :", dc.getValue().size(), equalTo(1));
					break;
				case "MISC":
					assertThat("Ticket Priority Count Value :", dc.getValue().size(), equalTo(1));
					break;

				default:
					break;
				}

			});

		} catch (ApplicationException enfe) {

		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyDataKanbanList, "hierarchyLevelOne", 4);
		List<Node> leafNodeList = treeAggregatorDetail.getMapOfListOfProjectNodes()
				.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);
		String startDate = LocalDate.now().minusMonths(15).toString();
		String endDate = LocalDate.now().toString();
		when(kanbanJiraIssueRepository.findIssuesByDateAndTypeAndStatus(any(), any(), any(), any(), any(), any()))
				.thenReturn(kanbanJiraIssueList);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		Map<String, Object> defectDataListMap = openTicketAgingByPriorityService.fetchKPIDataFromDb(leafNodeList,
				startDate, endDate, kpiRequest);
		assertThat("Total Defects issue list :",
				((List<KanbanJiraIssue>) defectDataListMap.get(RANGE_TICKET_LIST)).size(), equalTo(172));
	}

	@Test
	public void testCalculateKPIMetrics() {
		assertThat("Total Aging value :", openTicketAgingByPriorityService.calculateKPIMetrics(null), equalTo(0L));
	}

	@Test
	public void testGetQualifierType() {
		assertThat("Kpi Name :", openTicketAgingByPriorityService.getQualifierType(),
				equalTo("OPEN_TICKET_AGING_BY_PRIORITY"));
	}

}
