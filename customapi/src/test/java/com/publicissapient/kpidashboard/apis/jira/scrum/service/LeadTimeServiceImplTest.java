package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;

@RunWith(MockitoJUnitRunner.class)
public class LeadTimeServiceImplTest {

	private static final String STORY_HISTORY_DATA = "storyHistoryData";
	private static final String LEAD_TIME = "Lead Time";
	private static final String INTAKE_TO_DOR = "Intake - DoR";
	private static final String DOR_TO_DOD = "DoR - DoD";
	private static final String DOD_TO_LIVE = "DoD - Live";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	LeadTimeServiceImpl leadTimeService;
	@Mock
	ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	CustomApiConfig customApiSetting;
	@Mock
	FieldMappingRepository fieldMappingRepository;
	@Mock
	private CommonService commonService;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private List<DataCount> trendValues = new ArrayList<>();
	private Map<String, List<DataCount>> trendValueMap = new LinkedHashMap<>();
	private Map<String, List<String>> maturityRangeMap = new HashMap<>();
	private KpiRequest kpiRequest;
	private KpiElement kpiElement;
	private List<JiraIssueCustomHistory> jiraIssueCustomHistories = new ArrayList<>();

	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi3");
		kpiRequest.setLabel("PROJECT");

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);
		filterLevelMap.put("SPRINT", Filters.SPRINT);

		kpiWiseAggregation.put("kpi3", "sum");

		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectConfig.setProjectName("Scrum Project");
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);

		JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory.newInstance();

		jiraIssueCustomHistories = jiraIssueHistoryDataFactory.getJiraIssueCustomHistory();

		kpiWiseAggregation.put("kpi3", "percentile");

		setTreadValuesDataCount();

		maturityRangeMap.put("LeadTime", new ArrayList<>(Arrays.asList("-60,60-45,45-30,30-10,10-")));
		maturityRangeMap.put("Intake-DoR", new ArrayList<>(Arrays.asList("-30,30-20,20-10,10-5,5-")));
		maturityRangeMap.put("DoR-DoD", new ArrayList<>(Arrays.asList("-20,20-10,10-7,7-3,3-")));
		maturityRangeMap.put("DoD-Live", new ArrayList<>(Arrays.asList("-30,30-15,15-5,5-2,2-")));

		kpiWiseAggregation.put(LEAD_TIME, "average");

	}

	@After
	public void cleanup() {
		jiraIssueCustomHistoryRepository.deleteAll();
	}

	private void setTreadValuesDataCount() {
		List<DataCount> dataCountList = new ArrayList<>();
		DataCount dataCountValue = new DataCount();
		dataCountValue.setData(String.valueOf(5L));
		dataCountValue.setValue(5L);
		dataCountList.add(dataCountValue);
		DataCount dataCount = setDataCountValues("Scrum Project", "3", "4", dataCountList);
		trendValues.add(dataCount);
		trendValueMap.put(LEAD_TIME, trendValues);
		trendValueMap.put(INTAKE_TO_DOR, trendValues);
		trendValueMap.put(DOR_TO_DOD, trendValues);
		trendValueMap.put(DOD_TO_LIVE, trendValues);
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
	public void testGetQualifierType() {
		assertThat(leadTimeService.getQualifierType(), equalTo("LEAD_TIME"));
	}

	@Test
	public void testCalculateKPIMetrics() {
		assertThat(leadTimeService.calculateKPIMetrics(null), equalTo(0L));
	}

	@Test
	public void testFetchKPIDataFromDBData() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);
		when(customApiSetting.getSprintCountForFilters()).thenReturn(5);
		String startDate = LocalDate.now().minusDays(customApiSetting.getSprintCountForFilters() * 14L)
				.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String endDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(jiraIssueCustomHistoryRepository.findIssuesByCreatedDateAndType(any(), any(), any(), any()))
				.thenReturn(jiraIssueCustomHistories);

		Map<String, Object> resultListMap = leadTimeService.fetchKPIDataFromDb(leafNodeList, startDate, endDate,
				kpiRequest);
		List<JiraIssueCustomHistory> dataMap = (List<JiraIssueCustomHistory>) resultListMap.get(STORY_HISTORY_DATA);
		assertThat("Lead Time Data :", dataMap.size(), equalTo(92));
	}

	@Test
	public void testGetDorToDod() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(jiraIssueCustomHistoryRepository.findIssuesByCreatedDateAndType(any(), any(), any(), any()))
				.thenReturn(jiraIssueCustomHistories);
		when(configHelperService.calculateCriteria()).thenReturn(kpiWiseAggregation);
		String kpiRequestTrackerId = "Jira-Excel-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(leadTimeService.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);

		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);

		try {
			KpiElement kpiElement = leadTimeService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			List<DataCountGroup> dataCountGroups = (List<DataCountGroup>) kpiElement.getTrendValueList();
			dataCountGroups.stream().forEach(cycle -> {
				String cycleFilter = cycle.getFilter();
				switch (cycleFilter) {
				case LEAD_TIME:
					assertThat("LeadTime :", cycle.getValue().size(), equalTo(1));
					break;
				case INTAKE_TO_DOR:
					assertThat("Intake to DoR Value :", cycle.getValue().size(), equalTo(1));
					break;
				case DOR_TO_DOD:
					assertThat("DoR to DoD Value :", cycle.getValue().size(), equalTo(1));
					break;
				case DOD_TO_LIVE:
					assertThat("DoD to Live Value :", cycle.getValue().size(), equalTo(1));
					break;
				default:
					break;
				}
			});
		} catch (ApplicationException enfe) {

		}

	}
}
