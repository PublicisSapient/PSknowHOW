package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
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

import org.apache.commons.collections4.CollectionUtils;
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
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
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
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

@RunWith(MockitoJUnitRunner.class)
public class AverageResolutionTimeServiceImplTest {
	List<JiraIssue> totalIssueList = new ArrayList<>();
	List<JiraIssueCustomHistory> jiraIssueCustomHistoryList = new ArrayList<>();
	@InjectMocks
	AverageResolutionTimeServiceImpl averageResolutionTimeServiceImpl;
	@Mock
	JiraIssueRepository jiraIssueRepository;
	@Mock
	JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Mock
	CacheService cacheService;
	@Mock
	KpiHelperService kpiHelperService;
	@Mock
	ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	FieldMappingRepository fieldMappingRepository;
	@Mock
	CustomApiConfig customApiSetting;
	@Mock
	FilterHelperService filterHelperService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	CustomApiConfig customApiConfig;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private KpiRequest kpiRequest;
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private List<AccountHierarchyData> ahdList1 = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<DataCount> trendValues = new ArrayList<>();
	private Map<String, List<DataCount>> trendValueMap = new LinkedHashMap<>();
	private Map<String, List<String>> maturityRangeMap = new HashMap<>();
	private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	private CommonService commonService;

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("");
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.AVERAGE_RESOLUTION_TIME.getKpiId());
		kpiRequest.setLabel("PROJECT");

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);
		filterLevelMap.put("SPRINT", Filters.SPRINT);

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

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();

		totalIssueList = jiraIssueDataFactory.getJiraIssues();

		JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory.newInstance();

		jiraIssueCustomHistoryList = jiraIssueHistoryDataFactory.getJiraIssueCustomHistory();

		// set aggregation criteria kpi wise
		kpiWiseAggregation.put("average_Resolution_Time", "average");

		setTreadValuesDataCount();

	}

	@Test
	public void testgetCalculateKPIMetrics() {
		assertThat(averageResolutionTimeServiceImpl.calculateKPIMetrics(new HashMap<>()), equalTo(null));
	}

	@Test
	public void testGetAverageResolutionTime() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("averageResolutionTime", Arrays.asList("-30", "30-10", "10-5", "5-2", "2-"));

		when(jiraIssueRepository.findIssuesBySprintAndType(anyMap(), anyMap())).thenReturn(totalIssueList);
		when(jiraIssueCustomHistoryRepository.findByStoryIDInAndBasicProjectConfigIdIn(anyList(), anyList()))
				.thenReturn(jiraIssueCustomHistoryList);
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn("");
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);

		try {
			KpiElement kpiElement = averageResolutionTimeServiceImpl.getKpiData(kpiRequest,
					kpiRequest.getKpiList().get(0), treeAggregatorDetail);
			List<DataCountGroup> dataCountGroups = (List<DataCountGroup>) kpiElement.getTrendValueList();
			dataCountGroups.stream().forEach(avg -> {
				String avgFilter = avg.getFilter();
				switch (avgFilter) {
				case "Bug":
					assertThat("Bug Value :", avg.getValue().size(), equalTo(1));
					break;
				case "Story":
					assertThat("Story Value :", avg.getValue().size(), equalTo(1));
					break;
				case "Change request":
					assertThat("Change request Value :", avg.getValue().size(), equalTo(1));
					break;
				case "Epic":
					assertThat("Epic Value :", avg.getValue().size(), equalTo(1));
					break;
				default:
					break;
				}
			});
		} catch (ApplicationException enfe) {

		}

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
		List<String> storyIds = new ArrayList<>();
		CollectionUtils.emptyIfNull(totalIssueList).forEach(story -> storyIds.add(story.getNumber()));

		Map<String, Object> resultListMap = new HashMap<>();

		resultListMap.put("storyHistoryData", jiraIssueCustomHistoryList);
		resultListMap.put("stories", storyIds);
		resultListMap.put("jiraIssuesBySprintAndType", totalIssueList);
		resultListMap.put("projectFieldMapping", projectConfigList);

		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(jiraIssueRepository.findIssuesBySprintAndType(anyMap(), anyMap())).thenReturn(totalIssueList);
		when(jiraIssueCustomHistoryRepository.findByStoryIDInAndBasicProjectConfigIdIn(anyList(), anyList()))
				.thenReturn(jiraIssueCustomHistoryList);

		Map<String, Object> dbResultListMap = averageResolutionTimeServiceImpl.fetchKPIDataFromDb(leafNodeList,
				startDate, endDate, kpiRequest);
		List<JiraIssueCustomHistory> dataMap = (List<JiraIssueCustomHistory>) dbResultListMap.get("storyHistoryData");
		assertThat("Lead Time Data :", dataMap.size(),
				equalTo(((List<?>) resultListMap.get("storyHistoryData")).size()));
	}

	private void setTreadValuesDataCount() {
		List<DataCount> dataCountList = new ArrayList<>();
		DataCount dataCountValue = new DataCount();
		dataCountValue.setData(String.valueOf(5L));
		dataCountValue.setValue(5L);
		dataCountList.add(dataCountValue);
		DataCount dataCount = setDataCountValues("Scrum Project", "3", "4", dataCountList);
		trendValues.add(dataCount);
		trendValueMap.put("Overall", trendValues);
		trendValueMap.put("Bug", trendValues);
		trendValueMap.put("Change request", trendValues);
		trendValueMap.put("Enabler Story", trendValues);
		trendValueMap.put("Epic", trendValues);
		trendValueMap.put("Story", trendValues);
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
	public void testQualifierType() {
		String kpiName = KPICode.AVERAGE_RESOLUTION_TIME.name();
		String type = averageResolutionTimeServiceImpl.getQualifierType();
		assertThat("KPI Name : ", type, equalTo(kpiName));
	}

}
