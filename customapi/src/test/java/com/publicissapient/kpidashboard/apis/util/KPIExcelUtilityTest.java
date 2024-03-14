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

package com.publicissapient.kpidashboard.apis.util;

import static org.junit.Assert.assertNotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;

@ExtendWith(SpringExtension.class)
public class KPIExcelUtilityTest {
	@InjectMocks
	KPIExcelUtility excelUtility;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
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
		assertNotNull(modalValues);
		assertNotNull(overAllmodalValues);
	}
}
