package com.publicissapient.kpidashboard.apis.common.service;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiDataProvider;
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
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KpiDataProviderTest {

	private static final String STORY_LIST = "stories";
	private static final String SPRINTSDETAILS = "sprints";
	private static final String JIRA_ISSUE_HISTORY_DATA = "JiraIssueHistoryData";
	private static final String ESTIMATE_TIME = "Estimate_Time";

	private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();

	@InjectMocks
	KpiDataProvider kpiDataProvider;

	@Mock
	ConfigHelperService configHelperService;
	@Mock
	private FilterHelperService filterHelperService;
	@Mock
	private SprintRepository sprintRepository;
	@Mock
	private JiraIssueRepository jiraIssueRepository;
	@Mock
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Mock
	private CapacityKpiDataRepository capacityKpiDataRepository;

	private Map<String, Object> filterLevelMap;
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private List<SprintDetails> sprintDetailsList = new ArrayList<>();
	private List<JiraIssue> totalIssueList = new ArrayList<>();

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

		when(sprintRepository.findBySprintIDIn(Mockito.any())).thenReturn(sprintDetailsList);
		when(jiraIssueRepository.findIssueByNumber(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(totalIssueList);

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
	public void fetchSprintCapacityDataFromDb_shouldReturnCorrectData_whenValidInput() {
		List<String> sprintList = List.of("sprint1", "sprint2");
		ObjectId basicProjectConfigId = new ObjectId("6335363749794a18e8a4479b");
		String kpiId = "kpiId";

		when(sprintRepository.findBySprintIDIn(sprintList)).thenReturn(sprintDetailsList);
		when(jiraIssueRepository.findIssueByNumberOrParentStoryIdAndType(Mockito.anySet(), Mockito.anyMap(),
				Mockito.eq(CommonConstant.NUMBER))).thenReturn(totalIssueList);
		when(jiraIssueRepository.findIssueByNumberOrParentStoryIdAndType(Mockito.anySet(), Mockito.anyMap(),
				Mockito.eq(CommonConstant.PARENT_STORY_ID))).thenReturn(totalIssueList);
		when(jiraIssueCustomHistoryRepository.findByStoryIDInAndBasicProjectConfigIdIn(Mockito.anyList(),
				Mockito.anyList())).thenReturn(new ArrayList<>());

		Map<String, Object> result = kpiDataProvider.fetchSprintCapacityDataFromDb(kpiRequest, basicProjectConfigId,
				sprintList, kpiId);

		assertThat(result.get(ESTIMATE_TIME), equalTo(new ArrayList<>()));
		assertThat(((List<JiraIssue>)result.get(STORY_LIST)).size(), equalTo(totalIssueList.size()*2));
		assertThat(result.get(SPRINTSDETAILS), equalTo(sprintDetailsList));
		assertThat(result.get(JIRA_ISSUE_HISTORY_DATA), equalTo(new ArrayList<>()));
	}

}
