/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.testng.Assert;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.TestCaseDetailsDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.model.CodeBuildTimeInfo;
import com.publicissapient.kpidashboard.apis.model.DSRValidationData;
import com.publicissapient.kpidashboard.apis.model.DeploymentFrequencyInfo;
import com.publicissapient.kpidashboard.apis.model.IssueKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.LeadTimeChangeData;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.SprintFilter;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolValidationData;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterConfig;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterValue;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;

@RunWith(MockitoJUnitRunner.class)
public class KPIExcelUtilityTest {

	@InjectMocks
	KPIExcelUtility excelUtility;
	private List<KPIExcelData> kpiExcelData;
	@Mock
	CustomApiConfig customApiConfig;
	private List<JiraIssue> jiraIssues;
	private List<TestCaseDetails> testCaseDetailsList;
	List<JiraIssue> storyList = new ArrayList<>();
	private DeploymentFrequencyInfo deploymentFrequencyInfo;

	@Before
	public void setup() {
		deploymentFrequencyInfo = Mockito.mock(DeploymentFrequencyInfo.class);
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		jiraIssues = jiraIssueDataFactory.getJiraIssues();
		testCaseDetailsList = TestCaseDetailsDataFactory.newInstance().getTestCaseDetailsList();
		storyList = jiraIssues.stream().filter(issue -> issue.getTypeName().equalsIgnoreCase("Story"))
				.collect(Collectors.toList());
		kpiExcelData = new ArrayList<>();
	}

	@Test
	public void populateFTPRExcelData_ValidData_PopulatesKPIExcelData() {

		List<String> storyIds = Arrays.asList("STORY1", "STORY2");
		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		Map<String, JiraIssue> issueData = jiraIssues.stream().collect(Collectors.toMap(JiraIssue::getNumber, x -> x));
		List<JiraIssue> defects = jiraIssues.stream().filter(i -> i.getTypeName().equalsIgnoreCase("Bug"))
				.collect(Collectors.toList());

		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.STORY_POINT);
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		// Act
		Node node = new Node();
		node.setSprintFilter(new SprintFilter("sprint-id", "TEST| KnowHOW|PI_10|Opensource_Scrum Project",
				LocalDateTime.now().toString(), LocalDateTime.now().toString()));

		excelUtility.populateFTPRExcelData(storyIds, jiraIssues, kpiExcelData, issueData, defects, customApiConfig,
				fieldMapping, node);

		// Assert
		assertEquals(2, kpiExcelData.size());
		assertEquals("TEST| KnowHOW|PI_10|Opensource_Scrum Project", kpiExcelData.get(0).getSprintName());
		assertEquals("TEST| KnowHOW|PI_10|Opensource_Scrum Project", kpiExcelData.get(1).getSprintName());
	}

	@Test
	public void populateLeadTimeForChangeExcelData_ValidData_PopulatesKPIExcelData() {
		// Arrange
		String projectName = "Project1";
		Map<String, List<LeadTimeChangeData>> leadTimeMapTimeWise = new HashMap<>();
		List<LeadTimeChangeData> leadTimeList = Arrays.asList(createLeadTime(), createLeadTime());

		leadTimeMapTimeWise.put("Week1", leadTimeList);
		List<KPIExcelData> kpiExcelData = new ArrayList<>();
		String leadTimeConfigRepoTool = CommonConstant.REPO;

		// Act
		excelUtility.populateLeadTimeForChangeExcelData(projectName, leadTimeMapTimeWise, kpiExcelData,
				leadTimeConfigRepoTool);

		// Assert
		assertEquals(2, kpiExcelData.size());
	}

	public static void populatePickupTimeExcelData(String projectName, List<Map<String, Double>> repoWiseMRList,
			List<String> repoList, List<String> branchList, List<KPIExcelData> kpiExcelData) {

		if (CollectionUtils.isNotEmpty(repoWiseMRList)) {
			for (int i = 0; i < repoWiseMRList.size(); i++) {
				Map<String, Double> repoWiseMap = repoWiseMRList.get(i);
				for (Map.Entry<String, Double> m : repoWiseMap.entrySet()) {
					KPIExcelData excelData = new KPIExcelData();
					excelData.setProject(projectName);
					excelData.setRepo(repoList.get(i));
					excelData.setBranch(branchList.get(i));
					excelData.setDaysWeeks(m.getKey());
					excelData.setPickupTime(m.getValue().toString());
					kpiExcelData.add(excelData);
				}
			}
		}
	}

	@Test
	public void populatePickupTimeExcelData_ValidData_PopulatesKPIExcelData() {
		// Arrange
		RepoToolValidationData repoToolValidationData = new RepoToolValidationData();
		repoToolValidationData.setProjectName("Project1");
		repoToolValidationData.setPickupTime(10.0d);
		repoToolValidationData.setDate("Week");
		repoToolValidationData.setRepoUrl("repoUrl");
		repoToolValidationData.setBranchName("master");
		repoToolValidationData.setDeveloperName("developer");
		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		// Act
		excelUtility.populatePickupTimeExcelData(Arrays.asList(repoToolValidationData), kpiExcelData);

		// Assert
		assertEquals(1, kpiExcelData.size());
	}

	@Test
	public void populatePRSizeExcelData_ValidData_PopulatesKPIExcelData() {
		// Arrange
		RepoToolValidationData repoToolValidationData = new RepoToolValidationData();
		repoToolValidationData.setProjectName("Project1");
		repoToolValidationData.setPrSize(10L);
		repoToolValidationData.setDate("Week");
		repoToolValidationData.setRepoUrl("repoUrl");
		repoToolValidationData.setBranchName("master");
		repoToolValidationData.setDeveloperName("developer");
		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		// Act
		excelUtility.populatePRSizeExcelData(Arrays.asList(repoToolValidationData), kpiExcelData);

		// Assert
		assertEquals(1, kpiExcelData.size());
	}

	@Test
	public void populateCodeCommit_ValidData_PopulatesKPIExcelData() {
		// Arrange
		RepoToolValidationData repoToolValidationData = new RepoToolValidationData();
		repoToolValidationData.setProjectName("Project1");
		repoToolValidationData.setCommitCount(10L);
		repoToolValidationData.setMrCount(2L);
		repoToolValidationData.setDate("Week");
		repoToolValidationData.setRepoUrl("repoUrl");
		repoToolValidationData.setBranchName("master");
		repoToolValidationData.setDeveloperName("developer");
		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		// Act
		excelUtility.populateCodeCommit(Arrays.asList(repoToolValidationData), kpiExcelData);

		// Assert
		assertEquals(1, kpiExcelData.size());
	}

	@Test
	public void populateCodeBuildTimeExcelData_ValidData_PopulatesKPIExcelData() {
		// Arrange
		CodeBuildTimeInfo codeBuildTimeInfo = new CodeBuildTimeInfo();
		codeBuildTimeInfo.setBuildJobList(Arrays.asList("Job1", "Job2"));
		codeBuildTimeInfo.setBuildStartTimeList(Arrays.asList("2022-01-01T10:00:00", "2022-01-02T11:00:00"));
		codeBuildTimeInfo.setBuildEndTimeList(Arrays.asList("2022-01-01T11:00:00", "2022-01-02T12:00:00"));
		codeBuildTimeInfo.setDurationList(Arrays.asList("1 hour", "1 hour"));
		codeBuildTimeInfo.setBuildUrlList(Arrays.asList("url1", "url2"));
		codeBuildTimeInfo.setBuildStatusList(Arrays.asList("SUCCESS", "FAILURE"));

		String projectName = "Project1";
		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		// Act
		excelUtility.populateCodeBuildTimeExcelData(codeBuildTimeInfo, projectName, kpiExcelData);

		// Assert
		assertEquals(2, kpiExcelData.size());
	}

	@Test
	public void populateRefinementRejectionExcelData_ValidData_PopulatesExcelDataList() {
		// Arrange
		List<KPIExcelData> excelDataList = new ArrayList<>();

		Map<String, Map<String, List<JiraIssue>>> weekAndTypeMap = new HashMap<>();
		Map<String, List<JiraIssue>> map = new HashMap<>();
		map.put("Type1", Arrays.asList(jiraIssues.get(0)));

		weekAndTypeMap.put("Week1", map);
		map.clear();
		map.put("Type2", Arrays.asList(jiraIssues.get(1)));
		weekAndTypeMap.put("Week2", map);

		Map<String, LocalDateTime> jiraDateMap = jiraIssues.stream()
				.collect(Collectors.toMap(JiraIssue::getNumber, x -> LocalDateTime.now()));

		// Act
		excelUtility.populateRefinementRejectionExcelData(excelDataList, jiraIssues, weekAndTypeMap, jiraDateMap);

		// Assert
		assertEquals(48, excelDataList.size());
	}

	@Test
	public void testPopulateDefectRelatedExcelData_DRE() {
		// Mock input parameters
		Set<String> set = new HashSet<>();
		set.add("A");
		set.add("B");

		jiraIssues.forEach(jira -> {
			AdditionalFilterConfig additionalFilterConfig = new AdditionalFilterConfig();
			additionalFilterConfig.setFilterId("sqd");
			additionalFilterConfig.setValues(set);
			List<AdditionalFilterConfig> additionalFilterConfigList = new ArrayList<>();
			additionalFilterConfigList.add(additionalFilterConfig);

			List<AdditionalFilterValue> additionalFilterValueList = new ArrayList<>();
			AdditionalFilterValue additionalFilterValue = new AdditionalFilterValue();
			additionalFilterValue.setValue("abc");
			additionalFilterValue.setValueId("abc12");
			additionalFilterValueList.add(additionalFilterValue);

			List<AdditionalFilter> additionalFilterConfigsList = new ArrayList<>();
			AdditionalFilter additionalFilter = new AdditionalFilter();
			additionalFilter.setFilterId("sqd");
			additionalFilter.setFilterValues(additionalFilterValueList);
			additionalFilterConfigsList.add(additionalFilter);

			jira.setAdditionalFilters(additionalFilterConfigsList);
		});
		Map<String, JiraIssue> bug = jiraIssues.stream().filter(issue -> issue.getTypeName().equalsIgnoreCase("Bug"))
				.collect(Collectors.toMap(JiraIssue::getNumber, x -> x));
		List<KPIExcelData> kpiExcelData = new ArrayList<>();
		String kpiId = KPICode.DEFECT_REMOVAL_EFFICIENCY.getKpiId();
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		// Call the method to populate data
		KPIExcelUtility.populateDefectRelatedExcelData("abc", bug, jiraIssues, kpiExcelData, kpiId, customApiConfig,
				storyList);

		// Assert the result based on your logic
		assertEquals(20, kpiExcelData.size());
		KPIExcelData excelData = kpiExcelData.get(0);

		Map<String, String> defectIdDetails = excelData.getDefectId();
		assertEquals(1, defectIdDetails.size());
		// Depending on your kpiId logic, assert the corresponding fields
		assertEquals(Constant.EXCEL_YES, excelData.getRemovedDefect());
	}

	@Test
	public void testPopulateDefectRelatedExcelData_DSR() {
		// Mock input parameters
		Map<String, JiraIssue> bug = jiraIssues.stream().filter(issue -> issue.getTypeName().equalsIgnoreCase("Bug"))
				.collect(Collectors.toMap(JiraIssue::getNumber, x -> x));
		List<KPIExcelData> kpiExcelData = new ArrayList<>();
		String kpiId = KPICode.DEFECT_SEEPAGE_RATE.getKpiId();
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		// Call the method to populate data
		KPIExcelUtility.populateDefectRelatedExcelData("abc", bug, jiraIssues, kpiExcelData, kpiId, customApiConfig,
				storyList);

		// Assert the result based on your logic
		assertEquals(20, kpiExcelData.size());
		KPIExcelData excelData = kpiExcelData.get(0);

		Map<String, String> defectIdDetails = excelData.getDefectId();
		assertEquals(1, defectIdDetails.size());
		// Depending on your kpiId logic, assert the corresponding fields
		// assertEquals(Constant.EXCEL_YES, excelData.getEscapedDefect());
	}

	@Test
	public void testPopulateDefectRelatedExcelData_DRR() {
		// Mock input parameters
		Map<String, JiraIssue> bug = jiraIssues.stream().filter(issue -> issue.getTypeName().equalsIgnoreCase("Bug"))
				.collect(Collectors.toMap(JiraIssue::getNumber, x -> x));
		List<KPIExcelData> kpiExcelData = new ArrayList<>();
		String kpiId = KPICode.DEFECT_REJECTION_RATE.getKpiId();
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		// Call the method to populate data
		KPIExcelUtility.populateDefectRelatedExcelData("abc", bug, jiraIssues, kpiExcelData, kpiId, customApiConfig,
				storyList);

		// Assert the result based on your logic
		assertEquals(20, kpiExcelData.size());
		KPIExcelData excelData = kpiExcelData.get(0);

		Map<String, String> defectIdDetails = excelData.getDefectId();
		assertEquals(1, defectIdDetails.size());
		// Depending on your kpiId logic, assert the corresponding fields
		assertEquals(Constant.EXCEL_YES, excelData.getRejectedDefect());
	}

	@Test
	public void testPopulateDefectRelatedExcelData_Negative() {
		// Mock input parameters
		Map<String, JiraIssue> bug = jiraIssues.stream().filter(issue -> issue.getTypeName().equalsIgnoreCase("Bug"))
				.collect(Collectors.toMap(JiraIssue::getNumber, x -> x));
		List<KPIExcelData> kpiExcelData = new ArrayList<>();
		String kpiId = KPICode.CYCLE_TIME.getKpiId();
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		// Call the method to populate data
		KPIExcelUtility.populateDefectRelatedExcelData("abc", bug, jiraIssues, kpiExcelData, kpiId, customApiConfig,
				storyList);

		// Assert the result based on your logic
		assertEquals(20, kpiExcelData.size());
		KPIExcelData excelData = kpiExcelData.get(0);

		Map<String, String> defectIdDetails = excelData.getDefectId();
		assertEquals(1, defectIdDetails.size());
		// Depending on your kpiId logic, assert the corresponding fields
		assertNull(excelData.getRemovedDefect());
	}

	@Test
	public void testPopulateInSprintAutomationExcelData() {
		// Mock input parameters
		String sprint = "YourSprint";
		List<TestCaseDetails> allTestList = new ArrayList<>();
		List<TestCaseDetails> automatedList = new ArrayList<>();
		Set<JiraIssue> linkedStories = new HashSet<>();
		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		// Mock a TestCaseDetails
		TestCaseDetails testCase1 = mock(TestCaseDetails.class);
		when(testCase1.getNumber()).thenReturn("TC123");
		when(testCase1.getDefectStoryID()).thenReturn(new HashSet<>(Arrays.asList("Story123")));

		// Mock another TestCaseDetails
		TestCaseDetails testCase2 = mock(TestCaseDetails.class);
		when(testCase2.getNumber()).thenReturn("TC456");
		when(testCase2.getDefectStoryID()).thenReturn(new HashSet<>(Arrays.asList("Story456")));

		// Add the mock TestCaseDetails to the allTestList and automatedList
		allTestList.add(testCase1);
		allTestList.add(testCase2);
		automatedList.add(testCase1);

		// Mock a JiraIssue (linked story)
		JiraIssue linkedStory = mock(JiraIssue.class);
		when(linkedStory.getNumber()).thenReturn("Story123");
		when(linkedStory.getUrl()).thenReturn("http://example.com/story123");

		// Add the mock JiraIssue to the linkedStories
		linkedStories.add(linkedStory);

		// Call the method to populate data
		KPIExcelUtility.populateInSprintAutomationExcelData(sprint, allTestList, automatedList, linkedStories,
				kpiExcelData);

		// Assert the result based on your logic
		assertEquals(2, kpiExcelData.size());

		KPIExcelData excelData = kpiExcelData.get(0);

		assertEquals(sprint, excelData.getSprintName());
		assertEquals("TC123", excelData.getTestCaseId());
		assertEquals(Constant.EXCEL_YES, excelData.getAutomated());

		Map<String, String> linkedStoriesMap = excelData.getLinkedStory();
		assertEquals(1, linkedStoriesMap.size());
		assertEquals("http://example.com/story123", linkedStoriesMap.get("Story123"));
	}

	@Test
	public void testPopulateOpenVsClosedExcelData() {
		// Create a mock of KanbanJiraIssue
		KanbanJiraIssue openIssue = mock(KanbanJiraIssue.class);
		when(openIssue.getNumber()).thenReturn("OPEN-1");
		// when(openIssue.getTypeName()).thenReturn("Bug");
		// when(openIssue.getPriority()).thenReturn("High");

		KanbanIssueCustomHistory closedIssue = mock(KanbanIssueCustomHistory.class);
		when(closedIssue.getStoryID()).thenReturn("CLOSED-1");
		// when(closedIssue.getStoryType()).thenReturn("Story");
		// when(closedIssue.getPriority()).thenReturn("Low");

		// Mock data
		List<KanbanJiraIssue> openIssues = Arrays.asList(openIssue);
		List<KanbanIssueCustomHistory> closedIssues = Arrays.asList(closedIssue);

		// Create an empty list to hold the KPIExcelData objects
		List<KPIExcelData> excelDataList = new ArrayList<>();

		// Call the method to be tested
		KPIExcelUtility.populateOpenVsClosedExcelData("2022-01-01", "ProjectX", openIssues, closedIssues, excelDataList,
				"KPI_ID");

		// Verify the results
		assertEquals(2, excelDataList.size());

		// Verify the first KPIExcelData object for open issue
		KPIExcelData openKPIExcelData = excelDataList.get(0);
		assertEquals("ProjectX", openKPIExcelData.getProject());
		assertEquals("2022-01-01", openKPIExcelData.getDayWeekMonth());
		assertEquals("OPEN-1", openKPIExcelData.getTicketIssue().keySet().iterator().next());

		// Verify the second KPIExcelData object for closed issue
		KPIExcelData closedKPIExcelData = excelDataList.get(1);
		assertEquals("ProjectX", closedKPIExcelData.getProject());
		assertEquals("2022-01-01", closedKPIExcelData.getDayWeekMonth());
		assertEquals("CLOSED-1", closedKPIExcelData.getTicketIssue().keySet().iterator().next());
	}

	@Test
	public void testPrepareExcelForKanbanCumulativeDataMap() {
		// Mock data
		String projectName = "ProjectX";
		String date = "2022-01-01";
		String kpiId = "NET_OPEN_TICKET_COUNT_BY_STATUS";

		Map<String, Map<String, Set<String>>> jiraHistoryFieldAndDateWiseIssueMap = new HashMap<>();
		Map<String, Set<String>> internalMap = new HashMap<>();
		internalMap.put(LocalDate.now().toString(), new HashSet<>(Arrays.asList("Issue1", "Issue2")));
		jiraHistoryFieldAndDateWiseIssueMap.put("FieldA", internalMap);

		Set<String> fieldValues = new HashSet<>(Arrays.asList("FieldA"));
		Set<KanbanIssueCustomHistory> kanbanJiraIssues = new HashSet<>(
				Arrays.asList(createKanbanIssue("Issue1", "FieldA", "2022-01-01"),
						createKanbanIssue("Issue2", "FieldB", "2022-01-01"), createKanbanIssue("Issue3", "FieldA", "2022-01-02")));
		List<KPIExcelData> excelDataList = new ArrayList<>();

		// Create a mock of YourClass and use it to call the method
		// when(KPIExcelUtility.checkEmptyURL(any(KanbanJiraIssue.class))).thenReturn("MockedURL");

		// Call the method to be tested
		KPIExcelUtility.prepareExcelForKanbanCumulativeDataMap(projectName, jiraHistoryFieldAndDateWiseIssueMap,
				fieldValues, kanbanJiraIssues, excelDataList, date, kpiId);

		// Verify the results
		assertEquals(2, excelDataList.size());
	}

	private KanbanIssueCustomHistory createKanbanIssue(String storyId, String field, String createdDate) {
		KanbanIssueCustomHistory issue = new KanbanIssueCustomHistory();
		issue.setStoryID(storyId);
		issue.setCreatedDate(createdDate);
		return issue;
	}

	private LeadTimeChangeData createLeadTime() {
		LeadTimeChangeData leadTimeChangeData = new LeadTimeChangeData();
		leadTimeChangeData.setLeadTime(2.0);
		leadTimeChangeData.setLeadTimeInDays("2");
		leadTimeChangeData.setDate(LocalDate.now().toString());
		leadTimeChangeData.setClosedDate(LocalDate.now().minusDays(1).toString());
		leadTimeChangeData.setReleaseDate(LocalDate.now().toString());
		leadTimeChangeData.setMergeID("123");
		leadTimeChangeData.setUrl("www.fhewjdh.com");
		return leadTimeChangeData;
	}

	@Test
	public void populateReleaseDefectRelatedExcelData_ValidData_PopulatesExcelDataList() {
		// Arrange
		List<KPIExcelData> excelDataList = new ArrayList<>();
		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.STORY_POINT);
		jiraIssues.get(0).setAggregateTimeOriginalEstimateMinutes(10);

		// Act
		KPIExcelUtility.populateReleaseDefectRelatedExcelData(jiraIssues, excelDataList, fieldMapping);

		// Assert
		assertEquals(48, excelDataList.size());
	}

	@Test
	public void populateReleaseDefectRelatedExcelData_WhenEstimationCriteriaIsNotStoryPoint_PopulatesExcelDataList() {
		// Arrange
		List<KPIExcelData> excelDataList = new ArrayList<>();
		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.JIRA_IN_PROGRESS_STATUS);
		jiraIssues.get(0).setAggregateTimeOriginalEstimateMinutes(10);

		// Act
		KPIExcelUtility.populateReleaseDefectRelatedExcelData(jiraIssues, excelDataList, fieldMapping);

		// Assert
		assertEquals(48, excelDataList.size());
	}

	@Test
	public void populateBacklogCountExcelData_ValidData_PopulatesExcelDataList() {
		// Arrange
		List<KPIExcelData> excelDataList = new ArrayList<>();

		jiraIssues.get(0).setCreatedDate("2022-01-01");
		jiraIssues.get(0).setUpdateDate("2022-04-01");

		// Act
		KPIExcelUtility.populateBacklogCountExcelData(jiraIssues, excelDataList);

		// Assert
		assertEquals(48, excelDataList.size());
	}

	@Test
	public void populateIterationKPI_ValidData() {

		IterationKpiModalValue jiraIssueModalObject = new IterationKpiModalValue();
		IterationKpiModalValue modelValue = new IterationKpiModalValue();
		IterationKpiModalValue iterationKpiModalValue = new IterationKpiModalValue();
		List<IterationKpiModalValue> overAllModalValues = new ArrayList<>();
		overAllModalValues.add(iterationKpiModalValue);
		List<IterationKpiModalValue> modalValues = new ArrayList<>();
		modalValues.add(modelValue);

		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.STORY_POINT);
		Map<String, IterationKpiModalValue> modalObjectMap = mock(Map.class);
		when(modalObjectMap.get(jiraIssues.get(0).getNumber())).thenReturn(jiraIssueModalObject);

		// Act
		KPIExcelUtility.populateIterationKPI(overAllModalValues, modalValues, jiraIssues.get(0), fieldMapping,
				modalObjectMap);
		assertNotNull(modalValues);
		assertEquals(2, modalValues.size());
		assertNotNull(overAllModalValues);
		assertEquals(2, overAllModalValues.size());
	}

	@Test
	public void populateIterationKPI_ValidData1() {

		IterationKpiModalValue jiraIssueModalObject = new IterationKpiModalValue();
		IterationKpiModalValue modelValue = new IterationKpiModalValue();
		IterationKpiModalValue iterationKpiModalValue = new IterationKpiModalValue();
		List<IterationKpiModalValue> overAllModalValues = new ArrayList<>();
		overAllModalValues.add(iterationKpiModalValue);
		List<IterationKpiModalValue> modalValues = new ArrayList<>();
		modalValues.add(modelValue);

		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.STORY_POINT);
		Map<String, IterationKpiModalValue> modalObjectMap = mock(Map.class);
		when(modalObjectMap.get(jiraIssues.get(0).getNumber())).thenReturn(jiraIssueModalObject);
		jiraIssues.get(0).setSprintName("");

		// Act
		KPIExcelUtility.populateIterationKPI(overAllModalValues, modalValues, jiraIssues.get(0), fieldMapping,
				modalObjectMap);
		assertNotNull(modalValues);
		assertEquals(2, modalValues.size());
		assertNotNull(overAllModalValues);
		assertEquals(2, overAllModalValues.size());
	}

	@Test
	public void populateIterationKPI_When_Actual_Estimation_ValidData() {

		IterationKpiModalValue jiraIssueModalObject = new IterationKpiModalValue();
		IterationKpiModalValue modelValue = new IterationKpiModalValue();
		IterationKpiModalValue iterationKpiModalValue = new IterationKpiModalValue();
		List<IterationKpiModalValue> overAllModalValues = new ArrayList<>();
		overAllModalValues.add(iterationKpiModalValue);
		List<IterationKpiModalValue> modalValues = new ArrayList<>();
		modalValues.add(modelValue);

		jiraIssues.get(0).setOriginalEstimateMinutes(480);
		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.ACTUAL_ESTIMATION);
		when(fieldMapping.getAdditionalFilterConfig()).thenReturn(List.of(new AdditionalFilterConfig()));
		Map modalObjectMap = mock(Map.class);
		when(modalObjectMap.get(jiraIssues.get(0).getNumber())).thenReturn(jiraIssueModalObject);

		// Act
		KPIExcelUtility.populateIterationKPI(overAllModalValues, modalValues, jiraIssues.get(0), fieldMapping,
				modalObjectMap);
		assertNotNull(modalValues);
		assertEquals(2, modalValues.size());
		assertNotNull(overAllModalValues);
		assertEquals(2, overAllModalValues.size());
	}

	@Test
	public void populateFlowKPI_ValidData_PopulatesExcelDataList() {
		Map<String, Integer> typeCountMap = new HashMap<>();
		typeCountMap.put("A", 1);
		Map<String, Map<String, Integer>> dateTypeCountMap = new HashMap<>();
		dateTypeCountMap.put("2022-01-01", typeCountMap);
		// Arrange
		List<KPIExcelData> excelDataList = new ArrayList<>();

		// Act
		KPIExcelUtility.populateFlowKPI(dateTypeCountMap, excelDataList);

		// Assert
		assertNotNull(excelDataList);
		assertEquals(1, excelDataList.size());
		assertEquals(1, excelDataList.get(0).getCount().size());
	}

	@Test
	public void populateDirExcelData_ValidData_PopulatesKPIExcelData() {
		List<JiraIssue> defects = new ArrayList<>();
		Set<String> set = new HashSet<String>();
		set.add("STORY1");
		jiraIssues.get(0).setDefectStoryID(set);
		jiraIssues.get(0).setNumber("STORY1");
		defects.add(jiraIssues.get(0));
		jiraIssues.get(1).setDefectStoryID(set);
		jiraIssues.get(1).setNumber("STORY2");
		jiraIssues.get(1).setAggregateTimeOriginalEstimateMinutes(15);
		defects.add(jiraIssues.get(1));
		List<String> storyIds = Arrays.asList("STORY1", "STORY2");
		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		Map<String, JiraIssue> issueData = defects.stream().collect(Collectors.toMap(JiraIssue::getNumber, x -> x));

		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.STORY_POINT);
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		// Act
		Node node = new Node();
		node.setSprintFilter(new SprintFilter("sprint-id", "TEST| KnowHOW|PI_10|Opensource_Scrum Project",
				LocalDateTime.now().toString(), LocalDateTime.now().toString()));

		KPIExcelUtility.populateDirExcelData(storyIds, defects, kpiExcelData, issueData, fieldMapping, customApiConfig,
				node);

		// Assert
		assertEquals(3, kpiExcelData.size());
		assertEquals("TEST| KnowHOW|PI_10|Opensource_Scrum Project", kpiExcelData.get(0).getSprintName());
		assertEquals("TEST| KnowHOW|PI_10|Opensource_Scrum Project", kpiExcelData.get(1).getSprintName());
	}

	@Test
	public void populateDefectDensityExcelData_ValidData_Actual_Estimation() {
		List<JiraIssue> defects = new ArrayList<>();
		Set<String> set = new HashSet<String>();
		set.add("STORY1");
		jiraIssues.get(0).setDefectStoryID(set);
		jiraIssues.get(0).setNumber("STORY1");
		defects.add(jiraIssues.get(0));
		// Arrange
		jiraIssues.get(1).setDefectStoryID(set);
		jiraIssues.get(1).setNumber("STORY2");
		jiraIssues.get(1).setAggregateTimeOriginalEstimateMinutes(15);
		defects.add(jiraIssues.get(1));
		List<String> storyIds = Arrays.asList("STORY1", "STORY2");
		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		Map<String, JiraIssue> issueData = defects.stream().collect(Collectors.toMap(JiraIssue::getNumber, x -> x));

		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.STORY_POINT);
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		// Act
		Node node = new Node();
		node.setSprintFilter(new SprintFilter("sprint-id", "TEST| KnowHOW|PI_10|Opensource_Scrum Project",
				LocalDateTime.now().toString(), LocalDateTime.now().toString()));

		KPIExcelUtility.populateDefectDensityExcelData(storyIds, defects, kpiExcelData, issueData, fieldMapping,
				customApiConfig, node);

		// Assert
		assertEquals(3, kpiExcelData.size());
		assertEquals("TEST| KnowHOW|PI_10|Opensource_Scrum Project", kpiExcelData.get(0).getSprintName());
		assertEquals("TEST| KnowHOW|PI_10|Opensource_Scrum Project", kpiExcelData.get(1).getSprintName());
	}

	@Test
	public void populateDefectDensityExcelData_ValidData() {
		List<JiraIssue> defects = new ArrayList<>();
		Set<String> set = new HashSet<String>();
		set.add("STORY1");
		set.add("STORY2");
		jiraIssues.get(0).setDefectStoryID(set);
		jiraIssues.get(0).setNumber("STORY1");
		jiraIssues.get(0).setAggregateTimeOriginalEstimateMinutes(5);
		defects.add(jiraIssues.get(0));
		jiraIssues.get(1).setDefectStoryID(set);
		jiraIssues.get(1).setNumber("STORY2");
		jiraIssues.get(1).setAggregateTimeOriginalEstimateMinutes(15);
		defects.add(jiraIssues.get(1));

		List<String> storyIds = Arrays.asList("STORY1", "STORY2");
		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		Map<String, JiraIssue> issueData = defects.stream().collect(Collectors.toMap(JiraIssue::getNumber, x -> x));

		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.ACTUAL_ESTIMATION);
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		// Act
		Node node = new Node();
		node.setSprintFilter(new SprintFilter("sprint-id", "TEST| KnowHOW|PI_10|Opensource_Scrum Project",
				LocalDateTime.now().toString(), LocalDateTime.now().toString()));

		KPIExcelUtility.populateDefectDensityExcelData(storyIds, defects, kpiExcelData, issueData, fieldMapping,
				customApiConfig, node);

		// Assert
		assertEquals(4, kpiExcelData.size());
		assertEquals("TEST| KnowHOW|PI_10|Opensource_Scrum Project", kpiExcelData.get(0).getSprintName());
		assertEquals("TEST| KnowHOW|PI_10|Opensource_Scrum Project", kpiExcelData.get(1).getSprintName());
	}

	@Test
	public void populateFTPRExcelData_NonNullJiraIssue() {
		List<JiraIssue> defects = new ArrayList<>();
		Set<String> set = new HashSet<>();
		set.add("STORY1");
		jiraIssues.get(0).setDefectStoryID(set);
		jiraIssues.get(0).setNumber("STORY1");
		defects.add(jiraIssues.get(0));
		jiraIssues.get(1).setDefectStoryID(set);
		jiraIssues.get(1).setNumber("STORY2");
		jiraIssues.get(1).setAggregateTimeOriginalEstimateMinutes(15);
		defects.add(jiraIssues.get(1));
		// Arrange
		String sprint = "Sprint1";
		List<String> storyIds = Arrays.asList("STORY1", "STORY2");
		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		Map<String, JiraIssue> issueData = defects.stream().collect(Collectors.toMap(JiraIssue::getNumber, x -> x));

		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.STORY_POINT);
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		// Act
		Node node = new Node();
		node.setSprintFilter(new SprintFilter("sprint-id", "TEST| KnowHOW|PI_10|Opensource_Scrum Project",
				LocalDateTime.now().toString(), LocalDateTime.now().toString()));

		excelUtility.populateFTPRExcelData(storyIds, jiraIssues, kpiExcelData, issueData, defects, customApiConfig,
				fieldMapping, node);
		// Assert
		assertEquals(3, kpiExcelData.size());
		assertEquals("TEST| KnowHOW|PI_10|Opensource_Scrum Project", kpiExcelData.get(0).getSprintName());
		assertEquals("TEST| KnowHOW|PI_10|Opensource_Scrum Project", kpiExcelData.get(1).getSprintName());
	}

	@Test
	public void testPopulateDefectSeepageRateExcelData() {
		Map<String, JiraIssue> totalBugList = new HashMap<>();
		totalBugList.put("STORY1", jiraIssues.get(0));

		List<DSRValidationData> dsrValidationDataList = new ArrayList<>();
		DSRValidationData dsrValidationData = new DSRValidationData();
		dsrValidationData.setIssueNumber("STORY1");
		dsrValidationData.setLabel("test");
		dsrValidationDataList.add(dsrValidationData);

		// Arrange
		String sprint = "Sprint1";
		List<KPIExcelData> kpiExcelData = new ArrayList<>();
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");

		// Act
		KPIExcelUtility.populateDefectSeepageRateExcelData(sprint, totalBugList, dsrValidationDataList, kpiExcelData,
				customApiConfig, storyList);
		// Assert
		assertEquals(1, kpiExcelData.size());
		assertEquals("Sprint1", kpiExcelData.get(0).getSprintName());
	}

	@Test
	public void populateDefectRelatedExcelData_ValidData_PopulatesKPIExcelData() {
		List<JiraIssue> defects = new ArrayList<>();
		Set<String> set = new HashSet<>();
		set.add("STORY1");
		jiraIssues.get(0).setDefectStoryID(set);
		jiraIssues.get(0).setNumber("STORY1");
		defects.add(jiraIssues.get(0));
		// Arrange
		String sprint = "Sprint1";
		List<String> storyIds = Arrays.asList("STORY1", "STORY2");
		List<KPIExcelData> kpiExcelData = new ArrayList<>();
		List<String> priority = new ArrayList<>();
		Map<String, List<String>> pr = new HashMap<>();
		priority.add("p4-minor");
		priority.add("4");
		priority.add("p4");
		priority.add("minor");
		priority.add("Low");
		pr.put("p4-minor", priority);
		customApiConfig.setPriority(pr);
		Map<String, JiraIssue> issueData = defects.stream().collect(Collectors.toMap(JiraIssue::getNumber, x -> x));
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		// Act
		KPIExcelUtility.populateDefectRelatedExcelData(sprint, defects, kpiExcelData, customApiConfig, storyList);

		// Assert
		assertEquals(1, kpiExcelData.size());
		assertEquals("Sprint1", kpiExcelData.get(0).getSprintName());
	}

	@Test
	public void testPopulateDefectRCAandStatusRelatedExcelData_ValidData() {
		List<JiraIssue> jiraIssue = new ArrayList<>();

		List<JiraIssue> createDuringIteration = new ArrayList<>();
		Set<String> set = new HashSet<String>();
		set.add("STORY1");
		jiraIssues.get(0).setDefectStoryID(set);
		jiraIssues.get(0).setNumber("STORY1");
		jiraIssues.get(0).setAggregateTimeOriginalEstimateMinutes(5);
		jiraIssue.add(jiraIssues.get(0));
		// Arrange
		String sprint = "Sprint1";
		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.ACTUAL_ESTIMATION);

		// Act
		KPIExcelUtility.populateDefectRCAandStatusRelatedExcelData(sprint, jiraIssue, createDuringIteration, kpiExcelData,
				fieldMapping);

		// Assert
		assertEquals(1, kpiExcelData.size());
		assertEquals("Sprint1", kpiExcelData.get(0).getSprintName());
	}

	@Test
	public void testPopulateCreatedVsResolvedExcelData_ValidData() {
		List<JiraIssue> jiraIssue = new ArrayList<>();
		Set<String> set = new HashSet<>();
		set.add("STORY1");
		jiraIssues.get(0).setDefectStoryID(set);
		jiraIssues.get(0).setNumber("STORY1");
		jiraIssues.get(0).setAggregateTimeOriginalEstimateMinutes(5);
		jiraIssue.add(jiraIssues.get(0));
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		Map<String, String> map = new HashMap<>();
		map.put(jiraIssues.get(0).getNumber(), jiraIssues.get(0).getStatus());
		List<JiraIssue> createdConditionStories = new ArrayList<>();
		createdConditionStories.add(jiraIssues.get(0));
		// Arrange
		String sprint = "Sprint1";
		List<KPIExcelData> kpiExcelData = new ArrayList<>();
		Map<String, JiraIssue> issueData = jiraIssue.stream().collect(Collectors.toMap(JiraIssue::getNumber, x -> x));
		// Act
		KPIExcelUtility.populateCreatedVsResolvedExcelData(sprint, issueData, createdConditionStories, map, kpiExcelData,
				customApiConfig, storyList);
		// Assert
		assertEquals(1, kpiExcelData.size());
		assertEquals("Sprint1", kpiExcelData.get(0).getSprintName());
	}

	@Test
	public void testPopulateBackLogData() {
		JiraIssue jiraIssue = new JiraIssue();
		jiraIssue.setTypeName("bug");
		jiraIssue.setUrl("abc");
		jiraIssue.setNumber("1");
		jiraIssue.setPriority("5");
		jiraIssue.setName("Testing");
		List<String> status = new ArrayList<>();
		status.add("In Development");
		List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
		List<IterationKpiModalValue> modalValues = new ArrayList<>();
		JiraIssueCustomHistory issueCustomHistory = new JiraIssueCustomHistory();
		issueCustomHistory.setStoryID("1");
		issueCustomHistory.setCreatedDate(DateTime.now().now());
		List<JiraHistoryChangeLog> statusUpdationLog = new ArrayList<>();
		JiraHistoryChangeLog jiraHistoryChangeLog = new JiraHistoryChangeLog();
		jiraHistoryChangeLog.setChangedTo("In Development");
		jiraHistoryChangeLog.setUpdatedOn(LocalDateTime.now());
		statusUpdationLog.add(jiraHistoryChangeLog);
		issueCustomHistory.setStatusUpdationLog(statusUpdationLog);
		KPIExcelUtility.populateBackLogData(overAllmodalValues, modalValues, jiraIssue, issueCustomHistory, status);
		Assert.assertNotNull(modalValues);
		Assert.assertNotNull(overAllmodalValues);
	}

	@Test
	public void testPopulateIssueModal() {

		IssueKpiModalValue jiraIssueModalObject = new IssueKpiModalValue();
		AdditionalFilterConfig config = new AdditionalFilterConfig();

		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.STORY_POINT);
		when(fieldMapping.getAdditionalFilterConfig()).thenReturn(List.of(config));
		Map<String, IssueKpiModalValue> modalObjectMap = mock(Map.class);
		when(modalObjectMap.get(jiraIssues.get(0).getNumber())).thenReturn(jiraIssueModalObject);

		// Act
		KPIExcelUtility.populateIssueModal(jiraIssues.get(0), fieldMapping, modalObjectMap);
		assertNotNull(modalObjectMap);
	}

	@Test
	public void testPopulateIssueModalOriginalEstimate() {

		IssueKpiModalValue jiraIssueModalObject = new IssueKpiModalValue();
		AdditionalFilterConfig config = new AdditionalFilterConfig();
		jiraIssues.get(0).setOriginalEstimateMinutes(480);
		jiraIssues.get(0).setRemainingEstimateMinutes(null);

		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.ACTUAL_ESTIMATION);
		when(fieldMapping.getAdditionalFilterConfig()).thenReturn(List.of(config));
		Map<String, IssueKpiModalValue> modalObjectMap = mock(Map.class);
		when(modalObjectMap.get(jiraIssues.get(0).getNumber())).thenReturn(jiraIssueModalObject);

		// Act
		KPIExcelUtility.populateIssueModal(jiraIssues.get(0), fieldMapping, modalObjectMap);
		assertNotNull(modalObjectMap);
	}

	@Test
	public void testPopulateReleasePlanExcelData() {

		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.STORY_POINT);

		excelUtility.populateReleasePlanExcelData(jiraIssues, kpiExcelData, fieldMapping);

		// Assert
		assertEquals(48, kpiExcelData.size());
	}

	@Test
	public void testPopulateReleasePlanExcelData2() {

		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.ACTUAL_ESTIMATION);

		jiraIssues.get(0).setAggregateTimeOriginalEstimateMinutes(480);

		excelUtility.populateReleasePlanExcelData(jiraIssues, kpiExcelData, fieldMapping);

		// Assert
		assertEquals(48, kpiExcelData.size());
	}

	@Test
	public void testPopulateIterationReadinessExcelData() {

		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.STORY_POINT);

		excelUtility.populateIterationReadinessExcelData(jiraIssues, kpiExcelData, fieldMapping);

		// Assert
		assertEquals(48, kpiExcelData.size());
	}

	@Test
	public void testPopulateIterationReadinessExcelData2() {

		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.ACTUAL_ESTIMATION);

		jiraIssues.get(0).setAggregateTimeOriginalEstimateMinutes(480);

		excelUtility.populateIterationReadinessExcelData(jiraIssues, kpiExcelData, fieldMapping);

		// Assert
		assertEquals(48, kpiExcelData.size());
	}

	@Test
	public void testPopulateReleaseDefectWithTestPhasesRelatedExcelData() {

		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		excelUtility.populateReleaseDefectWithTestPhasesRelatedExcelData(jiraIssues, kpiExcelData);

		// Assert
		assertEquals(48, kpiExcelData.size());
	}

	@Test
	public void testPopulateBacklogDefectCountExcelData() {

		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		excelUtility.populateBacklogDefectCountExcelData(jiraIssues, kpiExcelData);

		// Assert
		assertEquals(48, kpiExcelData.size());
	}

	@Test
	public void testPopulateReleaseBurnUpExcelData() {

		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.STORY_POINT);

		Map<String, LocalDate> issueWiseReleaseTagDateMap = new HashMap<>();
		Map<String, LocalDate> completeDateIssueMap = new HashMap<>();
		Map<String, LocalDate> devCompleteDateIssueMap = new HashMap<>();

		excelUtility.populateReleaseBurnUpExcelData(jiraIssues, issueWiseReleaseTagDateMap, completeDateIssueMap,
				devCompleteDateIssueMap, kpiExcelData, fieldMapping);

		// Assert
		assertEquals(48, kpiExcelData.size());
	}

	@Test
	public void testPopulateReleaseBurnUpExcelData2() {

		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.ACTUAL_ESTIMATION);

		jiraIssues.get(0).setAggregateTimeOriginalEstimateMinutes(480);
		Map<String, LocalDate> issueWiseReleaseTagDateMap = new HashMap<>();
		Map<String, LocalDate> completeDateIssueMap = new HashMap<>();
		Map<String, LocalDate> devCompleteDateIssueMap = new HashMap<>();

		excelUtility.populateReleaseBurnUpExcelData(jiraIssues, issueWiseReleaseTagDateMap, completeDateIssueMap,
				devCompleteDateIssueMap, kpiExcelData, fieldMapping);

		// Assert
		assertEquals(48, kpiExcelData.size());
	}

	@Test
	public void testPopulateDeploymentFrequencyExcelData() {
		// Setup mock data
		List<String> jobNameList = List.of("Job1", "Job2");
		List<String> monthList = List.of("Week1", "Week2");
		List<String> environmentList = List.of("Env1", "Env2");
		List<String> deploymentDateList = List.of("2022-01-01", "2022-01-02");
		String projectName = "projectName";
		Map<String, String> deploymentMapPipelineNameWise = new HashMap<>();
		deploymentMapPipelineNameWise.put("pipeline1", "ddd");
		when(deploymentFrequencyInfo.getJobNameList()).thenReturn(jobNameList);
		when(deploymentFrequencyInfo.getMonthList()).thenReturn(monthList);
		when(deploymentFrequencyInfo.getEnvironmentList()).thenReturn(environmentList);
		when(deploymentFrequencyInfo.getDeploymentDateList()).thenReturn(deploymentDateList);

		// Call the method
		KPIExcelUtility.populateDeploymentFrequencyExcelData(projectName, deploymentFrequencyInfo, kpiExcelData,
				deploymentMapPipelineNameWise);

		// Verify the results
		assertEquals(2, kpiExcelData.size());
		assertEquals("Job1", kpiExcelData.get(0).getJobName());
		assertEquals("Week1", kpiExcelData.get(0).getWeeks());
		assertEquals("Env1", kpiExcelData.get(0).getDeploymentEnvironment());
		assertEquals("Job2", kpiExcelData.get(1).getJobName());
		assertEquals("Week2", kpiExcelData.get(1).getWeeks());
		assertEquals("Env2", kpiExcelData.get(1).getDeploymentEnvironment());
	}

	@Test
	public void testPopulateDeploymentFrequencyExcelData_ValidData() {
		// Setup mock data
		List<String> jobNameList = Arrays.asList("Job1", "Job2");
		List<String> monthList = Arrays.asList("Week1", "Week2");
		List<String> environmentList = Arrays.asList("Env1", "Env2");
		List<String> deploymentDateList = List.of("2022-01-01", "2022-01-02");
		String projectName = "projectName";
		Map<String, String> deploymentMapPipelineNameWise = new HashMap<>();
		deploymentMapPipelineNameWise.put("pipeline1", "ddd");

		when(deploymentFrequencyInfo.getJobNameList()).thenReturn(jobNameList);
		when(deploymentFrequencyInfo.getMonthList()).thenReturn(monthList);
		when(deploymentFrequencyInfo.getEnvironmentList()).thenReturn(environmentList);
		when(deploymentFrequencyInfo.getDeploymentDateList()).thenReturn(deploymentDateList);

		// Call the method
		KPIExcelUtility.populateDeploymentFrequencyExcelData(projectName, deploymentFrequencyInfo, kpiExcelData,
				deploymentMapPipelineNameWise);

		// Verify the results
		assertEquals(2, kpiExcelData.size());
		assertEquals("Job1", kpiExcelData.get(0).getJobName());
		assertEquals("Week1", kpiExcelData.get(0).getWeeks());
		assertEquals("Env1", kpiExcelData.get(0).getDeploymentEnvironment());
		assertEquals("Job2", kpiExcelData.get(1).getJobName());
		assertEquals("Week2", kpiExcelData.get(1).getWeeks());
		assertEquals("Env2", kpiExcelData.get(1).getDeploymentEnvironment());
	}

	@Test
	public void testPopulateDeploymentFrequencyExcelData_EmptyData() {
		// Setup mock data
		List<String> jobNameList = Arrays.asList();
		List<String> monthList = Arrays.asList();
		List<String> environmentList = Arrays.asList();
		List<String> deploymentDateList = List.of();
		String projectName = "projectName";
		Map<String, String> deploymentMapPipelineNameWise = new HashMap<>();
		// Call the method
		KPIExcelUtility.populateDeploymentFrequencyExcelData(projectName, deploymentFrequencyInfo, kpiExcelData,
				deploymentMapPipelineNameWise);

		// Verify the results
		assertEquals(0, kpiExcelData.size());
	}
}
