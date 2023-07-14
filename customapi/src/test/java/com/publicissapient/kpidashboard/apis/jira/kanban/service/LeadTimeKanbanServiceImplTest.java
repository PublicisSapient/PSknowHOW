package com.publicissapient.kpidashboard.apis.jira.kanban.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
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
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;

@RunWith(MockitoJUnitRunner.class)
public class LeadTimeKanbanServiceImplTest {
	private static final String LEAD_TIME = "Lead Time";
	private static final String OPEN_TO_TRIAGE = "Open - Triage";
	private static final String TRIAGE_TO_COMPLETE = "Triage - Complete";
	private static final String COMPLETE_TO_LIVE = "Complete - Live";
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	LeadTimeKanbanServiceImpl leadTimeKanbanService;
	@Mock
	KanbanJiraIssueHistoryRepository kanbanJiraIssueHistoryRepository;
	@Mock
	private CommonService commonService;
	private List<KanbanIssueCustomHistory> jiraIssueCustomHistories = new ArrayList<>();
	private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private List<DataCount> trendValues = new ArrayList<>();
	private Map<String, List<DataCount>> trendValueMap = new LinkedHashMap<>();
	private Map<String, List<String>> maturityRangeMap = new HashMap<>();
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private List<AccountHierarchyDataKanban> accountHierarchyDataKanbanList = new ArrayList<>();
	private KpiRequest kpiRequest;

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("/json/default/kanban_kpi_request.json");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi53");
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setDuration("WEEKS");
		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyDataKanbanList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();
		KanbanIssueCustomHistoryDataFactory issueHistoryFactory = KanbanIssueCustomHistoryDataFactory.newInstance();
		jiraIssueCustomHistories = issueHistoryFactory
				.getKanbanIssueCustomHistoryDataListByTypeName(Arrays.asList("Story", "Defect", "Issue"));

		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335368249794a18e8a4479f"));
		projectConfig.setProjectName("Kanban Project");
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/kanban/kanban_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);

		setTreadValuesDataCount();

		maturityRangeMap.put("LeadTime", new ArrayList<>(Arrays.asList("-60,60-45,45-30,30-10,10-")));
		maturityRangeMap.put("Open-Triage", new ArrayList<>(Arrays.asList("-30,30-20,20-10,10-5,5-")));
		maturityRangeMap.put("Triage-Complete", new ArrayList<>(Arrays.asList("-20,20-10,10-7,7-3,3-")));
		maturityRangeMap.put("Complete-Live", new ArrayList<>(Arrays.asList("-30,30-15,15-5,5-2,2-")));

		kpiWiseAggregation.put("kanban_Lead_Time", "average");

	}

	private void setTreadValuesDataCount() {
		List<DataCount> dataCountList = new ArrayList<>();
		DataCount dataCountValue = new DataCount();
		dataCountValue.setData(String.valueOf(5L));
		dataCountValue.setValue(5L);
		dataCountList.add(dataCountValue);
		DataCount dataCount = setDataCountValues("Kanban Project", "3", "4", dataCountList);
		trendValues.add(dataCount);
		trendValueMap.put(LEAD_TIME, trendValues);
		trendValueMap.put(OPEN_TO_TRIAGE, trendValues);
		trendValueMap.put(TRIAGE_TO_COMPLETE, trendValues);
		trendValueMap.put(COMPLETE_TO_LIVE, trendValues);
	}

	private DataCount setDataCountValues(String data, String maturity, Object maturityValue, Object value) {
		DataCount dataCount = new DataCount();
		dataCount.setData(data);
		dataCount.setMaturity(maturity);
		dataCount.setMaturityValue(maturityValue);
		dataCount.setValue(value);
		return dataCount;
	}

	@Test
	public void testLeadTimeKanban() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyDataKanbanList, "hierarchyLevelOne", 4);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRAKANBAN.name()))
				.thenReturn(kpiRequestTrackerId);
		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(kanbanJiraIssueHistoryRepository.findIssuesByCreatedDateAndType(any(), any(), any(), any()))
				.thenReturn(jiraIssueCustomHistories);
		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		when(cacheService.getFullKanbanHierarchyLevel()).thenReturn(hierachyLevelFactory.getHierarchyLevels());
		when(configHelperService.calculateCriteria()).thenReturn(kpiWiseAggregation);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);

		try {
			KpiElement kpiElement = leadTimeKanbanService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			List<DataCountGroup> dataCountGroups = (List<DataCountGroup>) kpiElement.getTrendValueList();
			dataCountGroups.stream().forEach(cycle -> {
				String cycleFilter = cycle.getFilter();
				switch (cycleFilter) {
				case LEAD_TIME:
					assertThat("Lead Time :", cycle.getValue().size(), equalTo(1));
					break;
				case OPEN_TO_TRIAGE:
					assertThat("Open to Triage Value :", cycle.getValue().size(), equalTo(1));
					break;
				case TRIAGE_TO_COMPLETE:
					assertThat("Triage to Complete Value :", cycle.getValue().size(), equalTo(1));
					break;
				case COMPLETE_TO_LIVE:
					assertThat("Complete to Live Value :", cycle.getValue().size(), equalTo(1));
					break;
				default:
					break;
				}
			});
		} catch (ApplicationException enfe) {

		}
	}

	@Test
	public void testGetQualifierType() {
		assertThat(leadTimeKanbanService.getQualifierType(), equalTo("LEAD_TIME_KANBAN"));
	}

	@Test
	public void testCalculateKPIMetrics() {
		assertThat(leadTimeKanbanService.calculateKPIMetrics(null), equalTo(0L));
	}

}
