/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
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
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.IterationStatus;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

@ExtendWith(SpringExtension.class)
public class JiraIterationKPIServiceTest {

	@InjectMocks
	JiraIterationKPIServiceTestImpl jiraKPIService;

	@Mock
	private CustomApiConfig customApiConfig;

	private Map<String, String> aggregationCriteriaMap;
	@Mock
	private JiraIterationServiceR jiraService;

	private static List<JiraIssueCustomHistory> getJiraIssueCustomHistories() {
		JiraIssueCustomHistory issueCustomHistory = new JiraIssueCustomHistory();
		issueCustomHistory.setStoryID("DTS-123");
		List<JiraIssueCustomHistory> jiraIssueCustomHistories = new ArrayList<>();
		jiraIssueCustomHistories.add(issueCustomHistory);
		return jiraIssueCustomHistories;
	}

	private static List<JiraIssue> getJiraIssues() {
		JiraIssue jiraIssue = new JiraIssue();
		jiraIssue.setNumber("123");
		List<JiraIssue> jiraIssues = new ArrayList<>();
		jiraIssues.add(jiraIssue);
		return jiraIssues;
	}

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
	public void testCreateIterationKpiData_WhenEstimationCriteriaIsNotEmpty() {
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setEstimationCriteria(CommonConstant.STORY_POINT);
		List<IterationKpiModalValue> modalValues = new ArrayList<>();
		assertNotNull(jiraKPIService.createIterationKpiData("label", fieldMapping, 10, 13D, 9.5D, modalValues));
	}

	@Test
	public void testCreateIterationKpiData_WhenEstimationCriteriaIsEmpty() {
		FieldMapping fieldMapping = mock(FieldMapping.class);
		when(fieldMapping.getEstimationCriteria()).thenReturn(null);
		List<IterationKpiModalValue> modalValues = new ArrayList<>();
		assertNotNull(jiraKPIService.createIterationKpiData("label", fieldMapping, 10, 13D, 9.5D, modalValues));
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
		List<JiraIssue> jiraIssues = getJiraIssues();
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

	@Test
	public void testGetJiraIssuesFromBaseClass_WithNoParam() {
		List<JiraIssue> jiraIssues = getJiraIssues();
		when(jiraService.getJiraIssuesForCurrentSprint()).thenReturn(jiraIssues);
		assertNotNull(jiraIssues);
	}

	public static class JiraIterationKPIServiceTestImpl extends JiraIterationKPIService {

		@Override
		public String getQualifierType() {
			return null;
		}

		@Override
		public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node filteredNode)
				throws ApplicationException {
			return null;
		}

		@Override
		public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
				KpiRequest kpiRequest) {
			return null;
		}

	}

	@Test
	public void testCreateIterationKpiDataToGetTheDelayedItemCount() {
		String label = "Delayed Items";
		Integer issueCount = 5;
		Double individualDelayedPercentage = 20.0;
		List<IterationKpiModalValue> modalValues = new ArrayList<>();
		IterationKpiModalValue modalValue = new IterationKpiModalValue();
		modalValue.setIssueId("DTS-123");
		modalValue.setIssueStatus("Delayed");
		modalValues.add(modalValue);
		IterationKpiData iterationKpiData = jiraKPIService.createIterationKpiDataToGetTheDelayedItemCount(label, issueCount, modalValues,individualDelayedPercentage);
		assertNotNull(iterationKpiData);
		assertEquals(label, iterationKpiData.getLabel());
		assertEquals(Double.valueOf(issueCount), iterationKpiData.getValue());
		assertEquals(modalValues, iterationKpiData.getModalValues());
		assertEquals(individualDelayedPercentage, iterationKpiData.getValue1());
	}
}