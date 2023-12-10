package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static com.publicissapient.kpidashboard.apis.constant.Constant.P1;
import static com.publicissapient.kpidashboard.apis.constant.Constant.P2;
import static com.publicissapient.kpidashboard.apis.constant.Constant.P3;
import static com.publicissapient.kpidashboard.apis.constant.Constant.P4;
import static com.publicissapient.kpidashboard.apis.constant.Constant.P5;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FTPRServiceImplTest {

	@Mock
	CacheService cacheService;
	@Mock
	private JiraIssueRepository jiraIssueRepository;
	@Mock
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Mock
	private ConfigHelperService configHelperService;
	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;

	@Mock
	private FieldMappingRepository fieldMappingRepository;

	@Mock
	private CustomApiConfig customApiSetting;

	@Mock
	private KpiHelperService kpiHelperService;
	@InjectMocks
	private FTPRServiceImpl ftprService;
	@Mock
	private JiraServiceR jiraService;

	@Mock
	private SprintRepository sprintRepository;
	private SprintDetails sprintDetails = new SprintDetails();
	private KpiRequest kpiRequest;
	private List<JiraIssue> storyList = new ArrayList<>();
	private List<JiraIssueCustomHistory> jiraIssueCustomHistories = new ArrayList<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi133");
		kpiRequest.setLabel("PROJECT");

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		sprintDetails = SprintDetailsDataFactory.newInstance().getSprintDetails().get(0);

		List<String> jiraIssueList = sprintDetails.getTotalIssues().stream().filter(Objects::nonNull)
				.map(SprintIssue::getNumber).distinct().collect(Collectors.toList());
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		storyList = jiraIssueDataFactory.findIssueByNumberList(jiraIssueList);

		JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory.newInstance();
		jiraIssueCustomHistories = jiraIssueHistoryDataFactory.getJiraIssueCustomHistory().stream()
				.filter(history -> storyList.contains(history.getStoryID())).collect(Collectors.toList());

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMapping.setIncludeRCAForKPI135(Arrays.asList("coding"));
		fieldMapping.setJiraKPI82StoryIdentification(Arrays.asList("Story"));
		fieldMapping.setJiraDefectRejectionStatusKPI135("");
		fieldMapping.setResolutionTypeForRejectionKPI135(Arrays.asList("Invalid", "Duplicate", "Unrequired"));
		fieldMapping.setJiraIssueDeliverdStatusKPI82(Arrays.asList("Closed"));
		fieldMapping.setDefectPriorityKPI135(Arrays.asList("p2", "p1"));
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setFieldMappingMap(fieldMappingMap);

	}

	@Test
	public void getQualifierType() {
		String qualifierType = ftprService.getQualifierType();
		assertEquals(KPICode.FIRST_TIME_PASS_RATE_ITERATION.name(), qualifierType);
	}

	@Test
	public void testGetKpiDataProject() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(jiraService.getCurrentSprintDetails()).thenReturn(sprintDetails);
		when(sprintRepository.findBySprintID(any())).thenReturn(sprintDetails);
		when(jiraIssueRepository.findByNumberInAndBasicProjectConfigId(any(), any())).thenReturn(storyList);
		when(jiraIssueCustomHistoryRepository.findByStoryIDInAndBasicProjectConfigIdIn(anyList(), anyList()))
				.thenReturn(jiraIssueCustomHistories);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(ftprService.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);

		Map<String, List<String>> priorityMap = new HashMap<>();
		priorityMap.put(P1,
				Stream.of("p1", "P1 - Blocker", "blocker", "1", "0", "p0", "urgent").collect(Collectors.toList()));
		priorityMap.put(P2, Stream.of("p2", "critical", "P2 - Critical", "2", "high").collect(Collectors.toList()));
		priorityMap.put(P3, Stream.of("p3", "p3-major", "major", "3", "medium").collect(Collectors.toList()));
		priorityMap.put(P4, Stream.of("p4", "p4 - minor", "minor", "4", "low").collect(Collectors.toList()));
		priorityMap.put(P5, Stream.of("p5 - trivial", "5", "trivial").collect(Collectors.toList()));

		when(customApiSetting.getPriority()).thenReturn(priorityMap);

		try {
			KpiElement kpiElement = ftprService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertNotNull((DataCount) kpiElement.getTrendValueList());

		} catch (ApplicationException enfe) {

		}
	}
}
