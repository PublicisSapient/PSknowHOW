/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.jira.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.model.jira.IterationStatus;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * @author anisingh4
 */
@ExtendWith(SpringExtension.class)
public class JiraKPIServiceTest {

	@InjectMocks
	JiraKpiServiceTestImpl jiraKPIService;

	@Mock
	private CustomApiConfig customApiConfig;

	private Map<String, String> aggregationCriteriaMap;
	@Mock
	private JiraServiceR jiraService;

	@Before
	public void init() {
		MockitoAnnotations.openMocks(this);

		aggregationCriteriaMap = new HashMap<>();
		aggregationCriteriaMap.put("kpi1", Constant.PERCENTILE);
		aggregationCriteriaMap.put("kpi2", Constant.MEDIAN);
		aggregationCriteriaMap.put("kpi3", Constant.AVERAGE);
		aggregationCriteriaMap.put("kpi4", Constant.SUM);
	}


	private List<Map<String, Long>> createAggregationInputData1() {
		List<Map<String, Long>> aggregatedValueList = new ArrayList<>();
		Map<String, Long> aggregatedValuesMap1 = new HashMap<>();
		aggregatedValuesMap1.put("Bug", 1L);
		Map<String, Long> aggregatedValuesMap2 = new HashMap<>();
		aggregatedValuesMap2.put("Bug", 4L);
		Map<String, Long> aggregatedValuesMap3 = new HashMap<>();
		aggregatedValuesMap3.put("Bug", 3L);
		Map<String, Long> aggregatedValuesMap4 = new HashMap<>();
		aggregatedValuesMap4.put("Bug", 0L);
		Map<String, Long> aggregatedValuesMap5 = new HashMap<>();
		aggregatedValuesMap5.put("Bug", 2L);
		Map<String, Long> aggregatedValuesMap6 = new HashMap<>();
		aggregatedValuesMap5.put("Bug", 6L);

		aggregatedValueList.add(aggregatedValuesMap1);
		aggregatedValueList.add(aggregatedValuesMap2);
		aggregatedValueList.add(aggregatedValuesMap3);
		aggregatedValueList.add(aggregatedValuesMap4);
		aggregatedValueList.add(aggregatedValuesMap5);
		aggregatedValueList.add(aggregatedValuesMap6);
		return aggregatedValueList;
	}

	public static class JiraKpiServiceTestImpl extends JiraKPIService {

		@Override
		public String getQualifierType() {
			return null;
		}

		@Override
		public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
				TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
			return null;
		}

		@Override
		public Object calculateKPIMetrics(Object o) {
			return null;
		}

		@Override
		public Object fetchKPIDataFromDb(List leafNodeList, String startDate, String endDate, KpiRequest kpiRequest) {
			return null;
		}

	}
	@Test
	public void testPopulateValidationDataObject() {
		KpiElement kpiElement = new KpiElement();
		String requestTrackerId = "excel";
		String validationDataKey = "kpi14";
		Map<String, ValidationData> validationDataMap = new HashMap<>();
		List<String> storyIdList = new ArrayList<>();
		storyIdList.add("DTS23");
		List<String> storyPointList = new ArrayList<>();
		storyPointList.add("13");
		JiraIssue jiraIssue = new JiraIssue();
		jiraIssue.setNumber("DTS23");
		List<JiraIssue> jiraIssues = new ArrayList<>();
		jiraIssues.add(jiraIssue);
		jiraKPIService.populateValidationDataObject(kpiElement, requestTrackerId, validationDataKey, validationDataMap,
				storyIdList, jiraIssues, storyPointList);
		assertNotNull(kpiElement.getMapOfSprintAndData());
		assertFalse(validationDataMap.isEmpty());
	}
	@Test
	public void testPopulateIterationStatusData() {
		IterationStatus iterationStatus = new IterationStatus();
		iterationStatus.setIssueId("1");
		iterationStatus.setUrl("abc");
		iterationStatus.setTypeName("test");
		iterationStatus.setPriority("5");
		iterationStatus.setIssueDescription("Testing");
		iterationStatus.setIssueStatus("InProgress");
		iterationStatus.setDueDate("2022-01-01");
		iterationStatus.setDelay("2");
		List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
		List<IterationKpiModalValue> modalValues = new ArrayList<>();
		jiraKPIService.populateIterationStatusData(overAllmodalValues, modalValues, iterationStatus);
		assertNotNull(modalValues);
		assertNotNull(overAllmodalValues);
	}
	@Test
	public void testPopulateIterationDataForTestWithoutStory() {
		TestCaseDetails testCaseDetails = mock(TestCaseDetails.class);
		when(testCaseDetails.getNumber()).thenReturn("123");
		when(testCaseDetails.getName()).thenReturn("sprint1");
		List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
		jiraKPIService.populateIterationDataForTestWithoutStory(overAllmodalValues,testCaseDetails);
		assertNotNull(overAllmodalValues);
	}
	@Test
	public void testPopulateIterationDataForDefectWithoutStory() {
		JiraIssue jiraIssue = mock(JiraIssue.class);
		when(jiraIssue.getNumber()).thenReturn("123");
		when(jiraIssue.getName()).thenReturn("sprint1");
		when(jiraIssue.getUrl()).thenReturn("abc.com");
		List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
		jiraKPIService.populateIterationDataForDefectWithoutStory(overAllmodalValues, jiraIssue);
		assertNotNull(overAllmodalValues);
	}
	@Test
	public void testGetDevCompletionDate() {
		JiraHistoryChangeLog jiraHistoryChangeLog = new JiraHistoryChangeLog();
		jiraHistoryChangeLog.setChangedTo("2022-01-01");
		jiraHistoryChangeLog.setUpdatedOn(LocalDateTime.now());
		List<JiraHistoryChangeLog> filterStatusUpdationLog = new ArrayList<>();
		filterStatusUpdationLog.add(jiraHistoryChangeLog);
		List<String> fieldMapping = new ArrayList<>();
		fieldMapping.add("2022-01-01");
		JiraIssueCustomHistory issueCustomHistory = mock(JiraIssueCustomHistory.class);
		when(issueCustomHistory.getStatusUpdationLog()).thenReturn(filterStatusUpdationLog);
		assertNotNull(jiraKPIService.getDevCompletionDate(issueCustomHistory, fieldMapping));
	}
	@Test
	public void testGetInSprintStatusLogs() {
		JiraHistoryChangeLog jiraHistoryChangeLog = new JiraHistoryChangeLog();
		jiraHistoryChangeLog.setChangedTo("2022-01-01");
		jiraHistoryChangeLog.setUpdatedOn(LocalDateTime.now());
		List<JiraHistoryChangeLog> issueHistoryLogs = new ArrayList<>();
		issueHistoryLogs.add(jiraHistoryChangeLog);
		assertNotNull(
				jiraKPIService.getInSprintStatusLogs(issueHistoryLogs, LocalDate.now().minusDays(10), LocalDate.now()));
	}
	@Test
	public void testCreateIterationKpiData_WhenEstimationCriteriaIsNotEmpty() {
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setEstimationCriteria(CommonConstant.STORY_POINT);
		List<IterationKpiModalValue> modalValues = new ArrayList<>();
		assertNotNull(
				jiraKPIService.createIterationKpiData("label",fieldMapping,10,13D,9.5D,modalValues));
	}
	@Test
	public void testCreateIterationKpiData_WhenEstimationCriteriaIsEmpty() {
		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(null);
		List<IterationKpiModalValue> modalValues = new ArrayList<>();
		assertNotNull(
				jiraKPIService.createIterationKpiData("label",fieldMapping,10,13D,9.5D,modalValues));
	}
	@Test
	public void testGetSprintDetailsFromBaseClass() {
		SprintDetails sprintDetails = new SprintDetails();
		sprintDetails.setSprintID("sprint1");
		when(jiraService.getCurrentSprintDetails()).thenReturn(sprintDetails);
		assertNotNull(jiraKPIService.getSprintDetailsFromBaseClass());
	}
	@Test
	public void testGetJiraIssuesFromBaseClass() {
		JiraIssue jiraIssue = new JiraIssue();
		jiraIssue.setNumber("123");
		List<JiraIssue> jiraIssues = new ArrayList<>();
		when(jiraService.getJiraIssuesForCurrentSprint()).thenReturn(jiraIssues);
		assertNotNull(jiraKPIService.getJiraIssuesFromBaseClass(List.of("123")));
	}
	@Test
	public void testGetJiraIssuesCustomHistoryFromBaseClass() {
		List<JiraIssueCustomHistory> jiraIssueCustomHistories = getJiraIssueCustomHistories();
		when(jiraService.getJiraIssuesCustomHistoryForCurrentSprint()).thenReturn(jiraIssueCustomHistories);
		assertNotNull(jiraKPIService.getJiraIssuesCustomHistoryFromBaseClass(List.of("DTS-123")));
	}
	@Test
	public void testGetJiraIssuesCustomHistoryFromBaseClass_WithNoParam() {
		List<JiraIssueCustomHistory> jiraIssueCustomHistories = getJiraIssueCustomHistories();
		when(jiraService.getJiraIssuesCustomHistoryForCurrentSprint()).thenReturn(jiraIssueCustomHistories);
		assertNotNull(jiraKPIService.getJiraIssuesCustomHistoryFromBaseClass());
	}

	private static List<JiraIssueCustomHistory> getJiraIssueCustomHistories() {
		JiraIssueCustomHistory issueCustomHistory = new JiraIssueCustomHistory();
		issueCustomHistory.setStoryID("DTS-123");
		List<JiraIssueCustomHistory> jiraIssueCustomHistories = new ArrayList<>();
		jiraIssueCustomHistories.add(issueCustomHistory);
		return jiraIssueCustomHistories;
	}


}