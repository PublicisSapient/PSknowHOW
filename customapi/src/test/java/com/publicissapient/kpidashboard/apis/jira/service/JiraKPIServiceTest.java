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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import org.joda.time.DateTime;
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

import static org.junit.Assert.assertNotNull;

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

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		aggregationCriteriaMap = new HashMap<>();
		aggregationCriteriaMap.put("kpi1", Constant.PERCENTILE);
		aggregationCriteriaMap.put("kpi2", Constant.MEDIAN);
		aggregationCriteriaMap.put("kpi3", Constant.AVERAGE);
		aggregationCriteriaMap.put("kpi4", Constant.SUM);
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
		List<JiraIssueCustomHistory> jiraIssueCustomHistories = new ArrayList<>();
		JiraIssueCustomHistory issueCustomHistory = new JiraIssueCustomHistory();
		issueCustomHistory.setStoryID("1");
		issueCustomHistory.setCreatedDate(DateTime.now().now());
		jiraIssueCustomHistories.add(issueCustomHistory);
		List<JiraHistoryChangeLog> statusUpdationLog = new ArrayList<>();
		JiraHistoryChangeLog jiraHistoryChangeLog = new JiraHistoryChangeLog();
		jiraHistoryChangeLog.setChangedTo("In Development");
		jiraHistoryChangeLog.setUpdatedOn(LocalDateTime.now());
		statusUpdationLog.add(jiraHistoryChangeLog);
		issueCustomHistory.setStatusUpdationLog(statusUpdationLog);
		jiraKPIService.populateBackLogData(overAllmodalValues, modalValues, jiraIssue,jiraIssueCustomHistories,status);
		assertNotNull(modalValues);
		assertNotNull(overAllmodalValues);
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
}