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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.KanbanIssueCustomHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KanbanJiraIssueDataFactory;
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
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;

@RunWith(MockitoJUnitRunner.class)
public class NetOpenTicketCountByPriorityServiceImplTest {

	List<KanbanJiraIssue> kanbanJiraIssueDataList = new ArrayList<>();
	String P1 = "p1,p1-blocker,blocker, 1, 0, p0";
	String P2 = "p2, critical, p2-critical, 2";
	String P3 = "p3, p3-major, major, 3";
	String P4 = "p4, p4-minor, minor, 4, p5-trivial, 5,trivial";
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@InjectMocks
	NetOpenTicketCountByPriorityServiceImpl netOpenTicketCountByPriorityImpl;
	private List<AccountHierarchyDataKanban> accountHierarchyDataKanbanList = new ArrayList<>();
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private Map<String, List<DataCount>> trendValueMap = new HashMap<>();
	private List<DataCount> trendValues = new ArrayList<>();
	@Mock
	private CommonService commonService;
	private KpiRequest kpiRequest;

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("/json/default/kanban_kpi_request.json");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi50");
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setDuration("WEEKS");

		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyDataKanbanList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();

		KanbanJiraIssueDataFactory kanbanJiraIssueDataFactory = KanbanJiraIssueDataFactory.newInstance();
		kanbanJiraIssueDataList = kanbanJiraIssueDataFactory
				.getKanbanJiraIssueDataListByTypeName(Arrays.asList("Story"));

		setMockProjectConfig();
		setMockFieldMapping();

		projectConfigList.forEach(projectConfig -> {
			projectConfigMap.put(projectConfig.getProjectName(), projectConfig);
		});

		fieldMappingList.forEach(fieldMapping -> {
			fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		});

		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);

		// set aggregation criteria kpi wise
		kpiWiseAggregation.put("ticketCountByPriority", "sum");

		setTreadValuesDataCount();
		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		when(cacheService.getFullKanbanHierarchyLevel()).thenReturn(hierachyLevelFactory.getHierarchyLevels());


	}

	@After
	public void cleanup() {

	}

	private void setMockProjectConfig() {

		ProjectBasicConfig projectOne = new ProjectBasicConfig();
		projectOne.setId(new ObjectId("5b674d58f47cae8935b1b26f"));
		projectOne.setProjectName("Alpha_Project1_Name");

		projectConfigList.add(projectOne);
	}

	private void setMockFieldMapping() {

		FieldMapping projectOne = new FieldMapping();
		projectOne.setBasicProjectConfigId(new ObjectId("6335368249794a18e8a4479f"));
		projectOne.setTicketCountIssueType(Arrays.asList("Story"));

		FieldMapping projectTwo = new FieldMapping();
		projectTwo.setBasicProjectConfigId(new ObjectId("5b719d06a500d00814bfb2b9"));
		projectTwo.setTicketCountIssueType(Arrays.asList("Story"));

		FieldMapping projectThree = new FieldMapping();
		projectThree.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		projectThree.setTicketCountIssueType(Arrays.asList("Story"));

		fieldMappingList.add(projectOne);
		fieldMappingList.add(projectTwo);
		fieldMappingList.add(projectThree);
	}

	private void setTreadValuesDataCount() {
		List<DataCount> dataCountList = new ArrayList<>();
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
		trendValueMap.put("Overall", trendValues);
		trendValueMap.put("P1", trendValues);
		trendValueMap.put("P2", trendValues);
		trendValueMap.put("P4", trendValues);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetTicketCountByPriority() throws ApplicationException {

		Map<String, Map<String, Map<String, Set<String>>>> projectWiseJiraHistoryPriorityAndDateWiseIssueMap = prepareProjectWiseJiraHistoryByFieldAndDate();

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyDataKanbanList, "hierarchyLevelOne", 4);

		when(kpiHelperService.computeProjectWiseJiraHistoryByFieldAndDate(anyMap(), anyString(), anyMap(), anyString()))
				.thenReturn(projectWiseJiraHistoryPriorityAndDateWiseIssueMap);

		List<KanbanIssueCustomHistory> kanbanIssueCustomHistoryDataList = KanbanIssueCustomHistoryDataFactory
				.newInstance().getKanbanIssueCustomHistoryDataList();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("JiraIssueHistoryData", kanbanIssueCustomHistoryDataList);
		when(kpiHelperService.fetchJiraCustomHistoryDataFromDbForKanban(anyList(), anyString(), anyString(), any(),
				anyString(), anyMap())).thenReturn(resultMap);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRAKANBAN.name()))
				.thenReturn(kpiRequestTrackerId);
		when(netOpenTicketCountByPriorityImpl.getKanbanRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		try {
			KpiElement kpiElement = netOpenTicketCountByPriorityImpl.getKpiData(kpiRequest,
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
		} catch (ApplicationException e) {
			e.printStackTrace();
		}
	}

	private Map<String, Map<String, Map<String, Set<String>>>> prepareProjectWiseJiraHistoryByFieldAndDate() {
		Map<String, Map<String, Map<String, Set<String>>>> projectWiseJiraHistoryPriorityAndDateWiseIssueMap = new HashMap<>();
		Map<String, Map<String, Set<String>>> jiraHistoryPriorityAndDateWiseIssueMap = new HashMap<>();
		Map<String, Set<String>> dateWiseIssueMap = new HashMap<>();
		Set<String> ids = new HashSet<>();
		ids.add("TEST-11232");
		ids.add("TEST-11233");
		dateWiseIssueMap.put("2022-07-01", ids);
		dateWiseIssueMap.put("2022-07-02", ids);
		jiraHistoryPriorityAndDateWiseIssueMap.put("P1", dateWiseIssueMap);
		jiraHistoryPriorityAndDateWiseIssueMap.put("P2", dateWiseIssueMap);
		jiraHistoryPriorityAndDateWiseIssueMap.put("P3", dateWiseIssueMap);
		jiraHistoryPriorityAndDateWiseIssueMap.put("P4", dateWiseIssueMap);
		projectWiseJiraHistoryPriorityAndDateWiseIssueMap.put("6335368249794a18e8a4479f",
				jiraHistoryPriorityAndDateWiseIssueMap);
		return projectWiseJiraHistoryPriorityAndDateWiseIssueMap;
	}

	@Test
	public void testGetQualifierType() {
		assertThat(netOpenTicketCountByPriorityImpl.getQualifierType(), equalTo("TICKET_COUNT_BY_PRIORITY"));
	}

	@Test
	public void testCalculateKPIMetrics() {
		assertThat(netOpenTicketCountByPriorityImpl.calculateKPIMetrics(null), equalTo(0L));
	}

}
