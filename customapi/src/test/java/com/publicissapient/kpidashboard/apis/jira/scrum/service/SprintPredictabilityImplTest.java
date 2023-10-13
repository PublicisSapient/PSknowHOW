package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepositoryCustom;

@RunWith(MockitoJUnitRunner.class)
public class SprintPredictabilityImplTest {

	private static final String SPRINT_WISE_PREDICTABILITY = "predictability";

	private static final String SPRINT_WISE_SPRINT_DETAILS = "sprintWiseSprintDetailMap";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMapForActualEstimation = new HashMap<>();
	List<JiraIssue> sprintWiseStoryList = new ArrayList<>();
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private KpiRequest kpiRequest;
	private Map<String, Object> filterLevelMap;
	private Set<ObjectId> basicProjectConfigObjectIds = new HashSet<>();
	private List<SprintDetails> sprintDetailsList = new ArrayList<>();
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	List<String> sprintStatusList = new ArrayList<>();

	@Mock
	private JiraIssueRepository jiraIssueRepository;

	@Mock
	private SprintRepository sprintRepository;

	@Mock
	private CacheService cacheService;

	@Mock
	private ConfigHelperService configHelperService;

	@Mock
	private FilterHelperService filterHelperService;

	@Mock
	private KpiHelperService kpiHelperService;

	@InjectMocks
	private SprintPredictabilityImpl sprintPredictability;

	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;

	@Mock
	private FieldMappingRepository fieldMappingRepository;

	@Mock
	private CustomApiConfig customApiSetting;

	@Mock
	private CommonService commonService;

	@Mock
	private JiraServiceR jiraKPIService;
	@Mock
	private SprintRepositoryCustom sprintRepositoryCustom;

	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.SPRINT_PREDICTABILITY.getKpiId());
		kpiRequest.setLabel("PROJECT");

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);
		filterLevelMap.put("SPRINT", Filters.SPRINT);

		JiraIssueDataFactory sprintWiseStoryDataFactory = JiraIssueDataFactory.newInstance();
		sprintWiseStoryList = sprintWiseStoryDataFactory.getStories();

		SprintDetailsDataFactory sprintDetailsDataFactory = SprintDetailsDataFactory.newInstance();

		sprintDetailsList = sprintDetailsDataFactory.getSprintDetails();

		basicProjectConfigObjectIds.add(new ObjectId("6335363749794a18e8a4479b"));
		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectConfig.setProjectName("Scrum Project");
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		FieldMapping fieldMappingWithActualEstimation = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingWithActualEstimation.setEstimationCriteria("Actual Estimation");
		fieldMappingMapForActualEstimation.put(fieldMapping.getBasicProjectConfigId(),
				fieldMappingWithActualEstimation);
		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);


		// set aggregation criteria kpi wise
		kpiWiseAggregation.put("defectRemovalEfficiency", "percentile");
		sprintStatusList.add(SprintDetails.SPRINT_STATE_CLOSED);
		sprintStatusList.add(SprintDetails.SPRINT_STATE_CLOSED.toLowerCase());

	}

	@After
	public void cleanup() {
		sprintWiseStoryList = null;
		jiraIssueRepository.deleteAll();

	}

	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);
		String startDate = leafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = leafNodeList.get(leafNodeList.size() - 1).getSprintFilter().getEndDate();

		when(sprintRepositoryCustom.findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(anySet(),
				anyList(),anyLong())).thenReturn(sprintDetailsList);

		when(jiraIssueRepository.findIssuesBySprintAndType(Mockito.any(), Mockito.any()))
				.thenReturn(sprintWiseStoryList);
        when(sprintRepositoryCustom.findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(basicProjectConfigObjectIds, sprintStatusList,5)).thenReturn(sprintDetailsList);
		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(SPRINT_WISE_PREDICTABILITY, sprintWiseStoryList);
		Map<ObjectId, List<SprintDetails>> expectedDuplicateIssues = new HashMap<>();
		expectedDuplicateIssues.put(new ObjectId("6335363749794a18e8a4479b"),sprintDetailsList.stream().collect(Collectors.toList()));

		resultListMap.put(SPRINT_WISE_SPRINT_DETAILS, sprintDetailsList);

		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		Map<String, Object> sprintWisePredictability = sprintPredictability.fetchKPIDataFromDb(leafNodeList, startDate,
				endDate, kpiRequest);
		assertThat("Sprint wise jira Issue  value :",
				((List<JiraIssue>) sprintWisePredictability.get(SPRINT_WISE_PREDICTABILITY)).size(), equalTo(23));
		assertThat("Sprint wise Sprint details value :",
				((List<SprintDetails>) sprintWisePredictability.get(SPRINT_WISE_SPRINT_DETAILS)).size(), equalTo(9));
	}

	@Test
	public void testGetSprintPredictability() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		when(sprintRepositoryCustom.findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(anySet(),
				anyList(),anyLong())).thenReturn(sprintDetailsList);

		when(jiraIssueRepository.findIssuesBySprintAndType(Mockito.any(), Mockito.any()))
				.thenReturn(sprintWiseStoryList);

		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(sprintPredictability.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		try {
			KpiElement kpiElement = sprintPredictability.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("DRE Value :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(1));
		} catch (Exception exception) {
		}
	}

	@Test
	public void testGetSprintPredictability_EmptySprintDetails_AzureCase() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		when(sprintRepository.findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(basicProjectConfigObjectIds,
				sprintStatusList)).thenReturn(sprintDetailsList);

		when(jiraIssueRepository.findIssuesBySprintAndType(Mockito.any(), Mockito.any()))
				.thenReturn(sprintWiseStoryList);

		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(sprintPredictability.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		try {
			KpiElement kpiElement = sprintPredictability.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Azure Value :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(0));
		} catch (Exception exception) {
		}
	}

	@Test
	public void testQualifierType() {
		String kpiName = KPICode.SPRINT_PREDICTABILITY.name();
		String type = sprintPredictability.getQualifierType();
		assertThat("KPI NAME: ", type, equalTo(kpiName));
	}

	@Test
	public void testGetSprintPredictabilityForActualEstimation() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		when(sprintRepository.findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(basicProjectConfigObjectIds,
				sprintStatusList)).thenReturn(sprintDetailsList);

		when(jiraIssueRepository.findIssuesBySprintAndType(Mockito.any(), Mockito.any()))
				.thenReturn(sprintWiseStoryList);

		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMapForActualEstimation);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(sprintPredictability.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		try {
			KpiElement kpiElement = sprintPredictability.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("DRE Value :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(0));
		} catch (Exception exception) {
		}
	}

	@Test
	public void testFetchKPIDataFromDbData1() throws ApplicationException{

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);
		String startDate = leafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = leafNodeList.get(leafNodeList.size() - 1).getSprintFilter().getEndDate();

		when(sprintRepositoryCustom.findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(anySet(),
				anyList(),anyLong())).thenReturn(sprintDetailsList);

		when(jiraIssueRepository.findIssuesBySprintAndType(Mockito.any(), Mockito.any()))
				.thenReturn(sprintWiseStoryList);
		when(sprintRepositoryCustom.findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(basicProjectConfigObjectIds, sprintStatusList,5)).thenReturn(sprintDetailsList);
		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(SPRINT_WISE_PREDICTABILITY, sprintWiseStoryList);
//		Map<ObjectId, List<SprintDetails>> expectedDuplicateIssues = new HashMap<>();
//		expectedDuplicateIssues.put(new ObjectId("6335363749794a18e8a4479b"),sprintDetailsList.stream().collect(Collectors.toList()));

		resultListMap.put(SPRINT_WISE_SPRINT_DETAILS, sprintDetailsList);

		Map<ObjectId, Set<String>> duplicateIssues = new HashMap<>();
		Set<String> set = new HashSet<>();
		set.add("6335363749794a18e8a4479b");
		duplicateIssues.put(new ObjectId("6335363749794a18e8a4479b"),set);
		when(kpiHelperService.getProjectWiseTotalSprintDetail(anyMap())).thenReturn(duplicateIssues);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		Map<String, Object> sprintWisePredictability = sprintPredictability.fetchKPIDataFromDb(leafNodeList, startDate,
				endDate, kpiRequest);
	}
}
