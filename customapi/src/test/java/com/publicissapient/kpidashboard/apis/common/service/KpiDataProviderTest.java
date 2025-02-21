package com.publicissapient.kpidashboard.apis.common.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.*;

import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
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
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiDataProvider;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.*;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;
import com.publicissapient.kpidashboard.common.model.jira.HappinessKpiData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.ReleaseWisePI;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.UserRatingData;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.HappinessKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepositoryCustom;

@RunWith(MockitoJUnitRunner.class)
public class KpiDataProviderTest {
	private static final String STORY_LIST = "stories";
	private static final String SPRINTSDETAILS = "sprints";
	private static final String JIRA_ISSUE_HISTORY_DATA = "JiraIssueHistoryData";
	private static final String ESTIMATE_TIME = "Estimate_Time";
	private static final String SPRINT_WISE_PREDICTABILITY = "predictability";

	private static final String SPRINT_WISE_SPRINT_DETAILS = "sprintWiseSprintDetailMap";

	private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();

	@InjectMocks
	KpiDataProvider kpiDataProvider;
	@Mock
	private HappinessKpiDataRepository happinessKpiDataRepository;

	@Mock
	private SprintRepositoryCustom sprintRepositoryCustom;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	private FilterHelperService filterHelperService;
	@Mock
	private KpiHelperService kpiHelperService;
	@Mock
	private SprintRepository sprintRepository;
	@Mock
	private JiraIssueRepository jiraIssueRepository;
	@Mock
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Mock
	private CapacityKpiDataRepository capacityKpiDataRepository;
	@Mock
	private BuildRepository buildRepository;
	@Mock
	CustomApiConfig customApiConfig;
	@Mock
	private ProjectReleaseRepo projectReleaseRepo;

	private Map<String, Object> filterLevelMap;
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private List<SprintDetails> sprintDetailsList = new ArrayList<>();
	private List<JiraIssue> totalIssueList = new ArrayList<>();
	List<JiraIssue> previousTotalIssueList = new ArrayList<>();
	private static final String SPRINT_VELOCITY_KEY = "sprintVelocityKey";
	private static final String SUB_GROUP_CATEGORY = "subGroupCategory";
	private static final String SPRINT_WISE_SPRINT_DETAIL_MAP = "sprintWiseSprintDetailMap";
	private static final String PREVIOUS_SPRINT_WISE_DETAILS = "previousSprintWiseDetails";
	private static final String PREVIOUS_SPRINT_VELOCITY = "previousSprintvelocity";

	private KpiRequest kpiRequest;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.ISSUE_COUNT.getKpiId());
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setLevel(5);

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);
		filterLevelMap.put("SPRINT", Filters.SPRINT);

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();

		totalIssueList = jiraIssueDataFactory.getJiraIssues();

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
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(configHelperService.getFieldMapping(any())).thenReturn(fieldMapping);
		/// set aggregation criteria kpi wise
		kpiWiseAggregation.put(KPICode.ISSUE_COUNT.name(), "sum");

		SprintDetailsDataFactory sprintDetailsDataFactory = SprintDetailsDataFactory.newInstance();
		sprintDetailsList = sprintDetailsDataFactory.getSprintDetails();
	}

	@After
	public void cleanup() {
		jiraIssueRepository.deleteAll();
	}

	@Test
	public void testFetchIssueCountDataFromDB() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		when(sprintRepository.findBySprintIDIn(any())).thenReturn(sprintDetailsList);
		when(jiraIssueRepository.findIssueByNumber(any(), any(), any())).thenReturn(totalIssueList);

		Map<ObjectId, List<String>> projectWiseSprints = new HashMap<>();
		treeAggregatorDetail.getMapOfListOfLeafNodes().get("sprint").forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			String sprint = leaf.getSprintFilter().getId();
			projectWiseSprints.putIfAbsent(basicProjectConfigId, new ArrayList<>());
			projectWiseSprints.get(basicProjectConfigId).add(sprint);
		});

		projectWiseSprints.forEach((basicProjectConfigId, sprintList) -> {
			Map<String, Object> result = kpiDataProvider.fetchIssueCountDataFromDB(kpiRequest, basicProjectConfigId,
					sprintList);
			assertThat("Total Stories : ", result.size(), equalTo(4));
		});
	}

	@Test
	public void testFetchBuildFrequencydata() {
		BuildDataFactory buildDataFactory = BuildDataFactory.newInstance("/json/non-JiraProcessors/build_details.json");
		List<Build> buildList = buildDataFactory.getbuildDataList();
		when(buildRepository.findBuildList(any(), any(), any(), any())).thenReturn(buildList);
		List<Build> list = kpiDataProvider.fetchBuildFrequencyData(new ObjectId(), "", "");
		assertThat(list.size(), equalTo(18));
	}

	@Test
	public void fetchSprintCapacityDataFromDb_shouldReturnCorrectData_whenValidInput() {
		List<String> sprintList = List.of("sprint1", "sprint2");
		ObjectId basicProjectConfigId = new ObjectId("6335363749794a18e8a4479b");
		String kpiId = "kpiId";

		when(sprintRepository.findBySprintIDIn(sprintList)).thenReturn(sprintDetailsList);
		when(jiraIssueRepository.findIssueByNumberOrParentStoryIdAndType(anySet(), Mockito.anyMap(),
				Mockito.eq(CommonConstant.NUMBER))).thenReturn(totalIssueList);
		when(jiraIssueRepository.findIssueByNumberOrParentStoryIdAndType(anySet(), Mockito.anyMap(),
				Mockito.eq(CommonConstant.PARENT_STORY_ID))).thenReturn(totalIssueList);
		when(jiraIssueCustomHistoryRepository.findByStoryIDInAndBasicProjectConfigIdIn(anyList(), anyList()))
				.thenReturn(new ArrayList<>());

		Map<String, Object> result = kpiDataProvider.fetchSprintCapacityDataFromDb(kpiRequest, basicProjectConfigId,
				sprintList);

		assertThat(result.get(ESTIMATE_TIME), equalTo(new ArrayList<>()));
		assertThat(((List<JiraIssue>) result.get(STORY_LIST)).size(), equalTo(totalIssueList.size() * 2));
		assertThat(result.get(SPRINTSDETAILS), equalTo(sprintDetailsList));
		assertThat(result.get(JIRA_ISSUE_HISTORY_DATA), equalTo(new ArrayList<>()));
	}

	@Test
	public void fetchSprintPredictabilityDataFromDb_shouldReturnCorrectData_whenValidInput() {
		List<String> sprintList = List.of("sprint1", "sprint2");
		ObjectId basicProjectConfigId = new ObjectId("6335363749794a18e8a4479b");
		String kpiId = "kpiId";
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		List<JiraIssue> sprintWiseStoryList = jiraIssueDataFactory.getStories();
		when(sprintRepositoryCustom.findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(anySet(), anyList(),
				anyLong())).thenReturn(sprintDetailsList);
		when(jiraIssueRepository.findIssuesBySprintAndType(Mockito.any(), Mockito.any())).thenReturn(sprintWiseStoryList);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		Map<ObjectId, Set<String>> duplicateIssues = new HashMap<>();
		Set<String> set = new HashSet<>();
		set.add("6335363749794a18e8a4479b");
		duplicateIssues.put(new ObjectId("6335363749794a18e8a4479b"), set);
		when(kpiHelperService.getProjectWiseTotalSprintDetail(anyMap())).thenReturn(duplicateIssues);
		Map<String, Object> result = kpiDataProvider.fetchSprintPredictabilityDataFromDb(kpiRequest, basicProjectConfigId,
				sprintList);
		assertThat(result.get(SPRINT_WISE_PREDICTABILITY), equalTo(sprintWiseStoryList));
	}

	@Test
	public void fetchHappinessIndexDataFromDb_shouldReturnCorrectData_whenValidInput() {
		SprintDetails sprintDetails = new SprintDetails();
		sprintDetails.setSprintID("38294_Scrum Project_6335363749794a18e8a4479b");
		sprintDetails.setBasicProjectConfigId(new ObjectId("6335363749794a18e8a4479b"));

		HappinessKpiData happinessKpiData = new HappinessKpiData();
		happinessKpiData.setSprintID("38294_Scrum Project_6335363749794a18e8a4479b");
		happinessKpiData.setBasicProjectConfigId(new ObjectId("6335363749794a18e8a4479b"));
		happinessKpiData.setUserRatingList(Arrays.asList(new UserRatingData(2, "uid", "uname")));

		Mockito.when(sprintRepository.findBySprintIDIn(Mockito.any())).thenReturn(Arrays.asList(sprintDetails));
		Mockito.when(happinessKpiDataRepository.findBySprintIDIn(Mockito.any()))
				.thenReturn(Arrays.asList(happinessKpiData));
		List<String> sprintList = List.of("sprint1", "sprint2");
		Map<String, Object> result = kpiDataProvider.fetchHappinessIndexDataFromDb(sprintList);
	}

	@Test
	public void fetchSprintVelocityDataFromDb_shouldReturnCorrectData_whenValidInput() {
		List<String> sprintList = List.of("sprint1", "sprint2");
		ObjectId basicProjectConfigId = new ObjectId("6335363749794a18e8a4479b");
		String kpiId = "kpiId";
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		totalIssueList = jiraIssueDataFactory.getBugs();
		previousTotalIssueList = jiraIssueDataFactory.getStories();

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(SPRINT_VELOCITY_KEY, totalIssueList);
		resultListMap.put(SUB_GROUP_CATEGORY, "sprint");
		resultListMap.put(SPRINT_WISE_SPRINT_DETAIL_MAP, sprintDetailsList);
		resultListMap.put(PREVIOUS_SPRINT_VELOCITY, previousTotalIssueList);
		resultListMap.put(PREVIOUS_SPRINT_WISE_DETAILS, new ArrayList<>());
		when(kpiHelperService.fetchSprintVelocityDataFromDb(any(), any(), any())).thenReturn(resultListMap);
		when(sprintRepositoryCustom.findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(anySet(), anyList(),
				anyLong())).thenReturn(sprintDetailsList);
		when(customApiConfig.getSprintCountForFilters()).thenReturn(5);

		Map<String, Object> result = kpiDataProvider.fetchSprintVelocityDataFromDb(kpiRequest, basicProjectConfigId);
		assertThat("Velocity value :", ((List<JiraIssue>) (result.get(SPRINT_VELOCITY_KEY))).size(), equalTo(20));
	}

	@Test
	public void testFetchScopeChurnData() {
		List<String> sprintList = List.of("sprint1", "sprint2");
		ObjectId basicProjectConfigId = new ObjectId("6335363749794a18e8a4479b");

		when(sprintRepository.findBySprintIDIn(Mockito.any())).thenReturn(sprintDetailsList);
		when(jiraIssueRepository.findIssueByNumber(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(totalIssueList);
		when(jiraIssueCustomHistoryRepository.findByStoryIDInAndBasicProjectConfigIdIn(Mockito.any(), Mockito.any()))
				.thenReturn(new ArrayList<>());

		Map<String, Object> result = kpiDataProvider.fetchScopeChurnData(kpiRequest, basicProjectConfigId, sprintList);
		assertNotNull(result);
	}

	@Test
	public void testFetchScopeChurnDataEmptyData() {
		List<String> sprintList = List.of("sprint1", "sprint2");
		ObjectId basicProjectConfigId = new ObjectId("6335363749794a18e8a4479b");

		when(sprintRepository.findBySprintIDIn(Mockito.any())).thenReturn(sprintDetailsList);
		when(jiraIssueRepository.findIssueByNumber(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(new ArrayList<>());
		when(jiraIssueCustomHistoryRepository.findByStoryIDInAndBasicProjectConfigIdIn(Mockito.any(), Mockito.any()))
				.thenReturn(new ArrayList<>());

		Map<ObjectId, FieldMapping> fieldMappingMap1 = new HashMap<>();
		fieldMappingMap.forEach((key, value) -> fieldMappingMap1.put(key, new FieldMapping()));
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap1);

		Map<String, Object> result = kpiDataProvider.fetchScopeChurnData(kpiRequest, basicProjectConfigId, sprintList);
		assertNotNull(result);
	}

	@Test
	public void testFetchCommitmentReliabilityData() {
		List<String> sprintList = List.of("sprint1", "sprint2");
		ObjectId basicProjectConfigId = new ObjectId("6335363749794a18e8a4479b");

		Map<ObjectId, Set<String>> duplicateIssues = new HashMap<>();
		fieldMappingMap.forEach((key, value) -> duplicateIssues.put(key, new HashSet<>()));

		when(sprintRepository.findBySprintIDIn(Mockito.any())).thenReturn(sprintDetailsList);
		when(jiraIssueRepository.findIssueByNumber(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(totalIssueList);
		when(kpiHelperService.getProjectWiseTotalSprintDetail(any())).thenReturn(duplicateIssues);

		Map<String, Object> result = kpiDataProvider.fetchCommitmentReliabilityData(kpiRequest, basicProjectConfigId,
				sprintList);
		assertNotNull(result);
	}

	@Test
	public void testFetchCostOfDelayData() {
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory.newInstance();
		List<JiraIssue> codList = jiraIssueDataFactory.getJiraIssues();
		List<JiraIssueCustomHistory> codHistoryList = jiraIssueHistoryDataFactory.getJiraIssueCustomHistory();
		codHistoryList.stream().map(JiraIssueCustomHistory::getStatusUpdationLog).forEach(f -> {
			f.forEach(g -> g.setUpdatedOn(LocalDateTime.now().minusDays(2)));
		});

		when(jiraIssueRepository.findIssuesByFilterAndProjectMapFilter(Mockito.any(), Mockito.any())).thenReturn(codList);
		when(jiraIssueCustomHistoryRepository.findByStoryIDInAndBasicProjectConfigIdIn(Mockito.any(), Mockito.any()))
				.thenReturn(codHistoryList);

		Map<String, Object> result = kpiDataProvider.fetchCostOfDelayData(new ObjectId("6335363749794a18e8a4479b"));
		assertThat("Data : ", result.size(), equalTo(3));
	}

	@Test
	public void testFetchCostOfDelayData2() {
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory.newInstance();
		List<JiraIssue> codList = jiraIssueDataFactory.getJiraIssues();
		List<JiraIssueCustomHistory> codHistoryList = jiraIssueHistoryDataFactory.getJiraIssueCustomHistory();
		codHistoryList.stream().map(JiraIssueCustomHistory::getStatusUpdationLog).forEach(f -> {
			f.forEach(g -> g.setUpdatedOn(LocalDateTime.now().minusDays(2)));
		});
		fieldMappingMap.forEach((key, value) -> {
			value.setClosedIssueStatusToConsiderKpi113(List.of("Closed"));
			value.setIssueTypesToConsiderKpi113(List.of("Story"));
		});

		when(jiraIssueRepository.findIssuesByFilterAndProjectMapFilter(Mockito.any(), Mockito.any())).thenReturn(codList);
		when(jiraIssueCustomHistoryRepository.findByStoryIDInAndBasicProjectConfigIdIn(Mockito.any(), Mockito.any()))
				.thenReturn(codHistoryList);

		Map<String, Object> result = kpiDataProvider.fetchCostOfDelayData(new ObjectId("6335363749794a18e8a4479b"));
		assertThat("Data : ", result.size(), equalTo(3));
	}

	@Test
	public void testFetchProjectReleaseData() {
		ProjectReleaseDataFactory projectReleaseDataFactory = ProjectReleaseDataFactory.newInstance();
		List<ProjectRelease> releaseList = projectReleaseDataFactory.findByBasicProjectConfigId("6335363749794a18e8a4479b");
		when(projectReleaseRepo.findByConfigIdIn(any())).thenReturn(releaseList);
		List<ProjectRelease> list = kpiDataProvider.fetchProjectReleaseData(new ObjectId("6335363749794a18e8a4479b"));
		assertThat("Total Release : ", list.size(), equalTo(1));
	}

	@Test
	public void testFetchPiPredictabilityData() {
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		List<JiraIssue> piWiseEpicList = jiraIssueDataFactory.getJiraIssues();

		List<ReleaseWisePI> releaseWisePIList = new ArrayList<>();
		ReleaseWisePI release1 = new ReleaseWisePI();
		release1.setBasicProjectConfigId("6335363749794a18e8a4479b");
		release1.setReleaseName(new ArrayList<>(Collections.singleton("KnowHOW v7.0.0")));
		release1.setUniqueTypeName("Story");
		releaseWisePIList.add(release1);

		ReleaseWisePI release2 = new ReleaseWisePI();
		release2.setBasicProjectConfigId("6335363749794a18e8a4479b");
		release2.setReleaseName(new ArrayList<>(Collections.singleton("KnowHOW PI-11")));
		release2.setUniqueTypeName("Epic");
		releaseWisePIList.add(release2);

		when(jiraIssueRepository.findUniqueReleaseVersionByUniqueTypeName(Mockito.any())).thenReturn(releaseWisePIList);
		when(jiraIssueRepository.findByRelease(Mockito.any(), Mockito.any())).thenReturn(piWiseEpicList);
		List<JiraIssue> list = kpiDataProvider.fetchPiPredictabilityData(new ObjectId("6335363749794a18e8a4479b"));
		assertThat("Total Release : ", list.size(), equalTo(48));
	}

	@Test
	public void testFetchCreatedVsResolvedData() {
		List<String> sprintList = List.of("sprint1", "sprint2");
		ObjectId basicProjectConfigId = new ObjectId("6335363749794a18e8a4479b");

		when(sprintRepository.findBySprintIDIn(Mockito.any())).thenReturn(sprintDetailsList);
		when(jiraIssueRepository.findIssueByNumber(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(totalIssueList);
		when(jiraIssueRepository.findLinkedDefects(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(new ArrayList<>());
		when(jiraIssueCustomHistoryRepository.findByStoryIDInAndBasicProjectConfigIdIn(Mockito.any(), Mockito.any()))
				.thenReturn(new ArrayList<>());

		Map<String, Object> result = kpiDataProvider.fetchCreatedVsResolvedData(kpiRequest, basicProjectConfigId,
				sprintList);
		assertThat("createdVsResolved value :", ((List<JiraIssue>) (result.get("createdVsResolvedKey"))).size(),
				equalTo(totalIssueList.size()));
	}

	@Test
	public void testFetchDIR() {
		List<String> sprintList = List.of("sprint1", "sprint2");
		ObjectId basicProjectConfigId = new ObjectId("6335363749794a18e8a4479b");
		SprintWiseStoryDataFactory sprintWiseStoryDataFactory = SprintWiseStoryDataFactory.newInstance();
		List<SprintWiseStory> storyData = sprintWiseStoryDataFactory.getSprintWiseStories();
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		List<JiraIssue> defectData = jiraIssueDataFactory.getBugs();
		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put("storyData", storyData);
		resultListMap.put("defectData", defectData);
		resultListMap.put("issueData", new ArrayList<>());
		when(kpiHelperService.fetchDIRDataFromDb(any(), any(), any())).thenReturn(resultListMap);
		Map<String, Object> defectDataListMap = kpiDataProvider.fetchDefectInjectionRateDataFromDb(kpiRequest, basicProjectConfigId, sprintList);
		assertThat("Total Story value :", ((List<JiraIssue>) (defectDataListMap.get("storyData"))).size(), equalTo(5));
		assertThat("Total Defects value :", ((List<JiraIssue>) (defectDataListMap.get("defectData"))).size(), equalTo(20));
	}

	@Test
	public void testFetchDD() {
		List<String> sprintList = List.of("sprint1", "sprint2");
		ObjectId basicProjectConfigId = new ObjectId("6335363749794a18e8a4479b");
		SprintWiseStoryDataFactory sprintWiseStoryDataFactory = SprintWiseStoryDataFactory.newInstance();
		List<SprintWiseStory> storyData = sprintWiseStoryDataFactory.getSprintWiseStories();
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		List<JiraIssue> defectData = jiraIssueDataFactory.getBugs();
		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put("storyData", storyData);
		resultListMap.put("defectData", defectData);
		resultListMap.put("storyPoints", new ArrayList<>());
		when(kpiHelperService.fetchQADDFromDb(any(), any(), any())).thenReturn(resultListMap);
		Map<String, Object> defectDataListMap = kpiDataProvider.fetchDefectDensityDataFromDb(kpiRequest, basicProjectConfigId, sprintList);
		assertThat("Total Story value :", ((List<JiraIssue>) (defectDataListMap.get("storyData"))).size(), equalTo(5));
		assertThat("Total Defects value :", ((List<JiraIssue>) (defectDataListMap.get("defectData"))).size(), equalTo(20));
	}

	@Test
	public void testFetchDRRData() {
		List<String> sprintList = List.of("sprint1", "sprint2");
		ObjectId basicProjectConfigId = new ObjectId("6335363749794a18e8a4479b");

		JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory.newInstance();
		List<JiraIssueCustomHistory> jiraIssueCustomHistoryList = jiraIssueHistoryDataFactory.getJiraIssueCustomHistory();

		when(sprintRepository.findBySprintIDIn(Mockito.any())).thenReturn(sprintDetailsList);
		when(jiraIssueCustomHistoryRepository.findByStoryIDInAndBasicProjectConfigIdIn(Mockito.any(), Mockito.any()))
				.thenReturn(jiraIssueCustomHistoryList);
		when(jiraIssueRepository.findIssueByNumber(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(totalIssueList);

		Map<String, Object> result = kpiDataProvider.fetchDRRData(kpiRequest, basicProjectConfigId,
				sprintList);
		assertThat("Rejects Defects value :", ((List<JiraIssue>) result.get("rejectedBugKey")).size(),
				equalTo(0));
	}
}
