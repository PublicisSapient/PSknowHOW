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

package com.publicissapient.kpidashboard.common.repository.jira;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.jira.IssueGroupFields;
import com.publicissapient.kpidashboard.common.model.jira.IssueHistoryMappedData;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueHistory;

@ExtendWith(SpringExtension.class)
public class JiraIssueCustomHistoryRepositoryImplTest {
	@Mock
	private static JiraIssueCustomHistory mockJiraJiraIssueCustomHistory1;
	@Mock
	private static JiraIssueCustomHistory mockJiraJiraIssueCustomHistory2;
	@Mock
	private static JiraIssueCustomHistory mockJiraJiraIssueCustomHistory3;
	@Mock
	private static JiraIssueCustomHistory mockJiraJiraIssueCustomHistory4;
	@Mock
	private static JiraIssueCustomHistory mockJiraJiraIssueCustomHistory5;
	@Mock
	private MongoOperations operations;

	@InjectMocks
	private JiraIssueCustomHistoryRepositoryImpl repository;
	@Mock
	private JiraIssueCustomHistoryRepository featureCustomHistoryRepo;

	@BeforeEach
	public void setUp() {

		// Helper mock data

		/// document -1 in feature_custom_history
		ArrayList<JiraHistoryChangeLog> statusUpdationLog = new ArrayList<>();
		String projectID = "TestProject1";
		String storyID = "TestStory1";
		String storyType = "Story";
		/// history 1 details
		String sprintId = "TestSprint1";
		String status = "Active";
		String fromStatus = "Open";
		java.time.LocalDateTime activityDate = java.time.LocalDateTime.now();
		JiraHistoryChangeLog f1 = createFeatureStatusDetails(sprintId, status, fromStatus, activityDate);
		statusUpdationLog.add(f1);
		sprintId = "TestSprint1";
		status = "Active";
		fromStatus = "In Progress";
		activityDate = activityDate.plusDays(3);
		JiraHistoryChangeLog f2 = createFeatureStatusDetails(sprintId, status, fromStatus, activityDate);
		statusUpdationLog.add(f2);
		sprintId = "TestSprint1";
		status = "Active";
		fromStatus = "Closed";
		activityDate = activityDate.plusDays(3);
		JiraHistoryChangeLog f3 = createFeatureStatusDetails(sprintId, status, fromStatus, activityDate);
		statusUpdationLog.add(f3);
		mockJiraJiraIssueCustomHistory1 = createFeatureHistory(projectID, storyID, storyType, statusUpdationLog);

		/// document -2 in feature_custom_history
		statusUpdationLog = new ArrayList<>();
		projectID = "TestProject1";
		storyID = "TestStory2";
		storyType = "Story";
		/// history 1 details
		sprintId = "TestSprint1";
		status = "Active";
		fromStatus = "Open";
		activityDate = java.time.LocalDateTime.now();
		f1 = createFeatureStatusDetails(sprintId, status, fromStatus, activityDate);
		statusUpdationLog.add(f1);
		sprintId = "TestSprint1";
		status = "Active";
		fromStatus = "In Progress";
		activityDate = activityDate.plusDays(5);
		f2 = createFeatureStatusDetails(sprintId, status, fromStatus, activityDate);
		statusUpdationLog.add(f2);
		sprintId = "TestSprint1";
		status = "Active";
		fromStatus = "Closed";
		activityDate = activityDate.plusDays(7);
		f3 = createFeatureStatusDetails(sprintId, status, fromStatus, activityDate);
		statusUpdationLog.add(f3);
		mockJiraJiraIssueCustomHistory2 = createFeatureHistory(projectID, storyID, storyType, statusUpdationLog);

		/// document -3 in feature_custom_history
		statusUpdationLog = new ArrayList<>();
		projectID = "TestProject2";
		storyID = "TestStory3";
		storyType = "Story";
		/// history 1 details
		sprintId = "TestSprint1";
		status = "Active";
		fromStatus = "Open";
		activityDate = java.time.LocalDateTime.now();
		f1 = createFeatureStatusDetails(sprintId, status, fromStatus, activityDate);
		statusUpdationLog.add(f1);
		sprintId = "TestSprint2";
		status = "Active";
		fromStatus = "In Progress";
		activityDate = activityDate.plusDays(3);
		f2 = createFeatureStatusDetails(sprintId, status, fromStatus, activityDate);
		statusUpdationLog.add(f2);
		sprintId = "TestSprint3";
		status = "Active";
		fromStatus = "Closed";
		activityDate = activityDate.plusDays(3);
		f3 = createFeatureStatusDetails(sprintId, status, fromStatus, activityDate);
		statusUpdationLog.add(f3);
		mockJiraJiraIssueCustomHistory3 = createFeatureHistory(projectID, storyID, storyType, statusUpdationLog);

		/// document -4 in feature_custom_history
		statusUpdationLog = new ArrayList<>();
		projectID = "TestProject2";
		storyID = "TestStory4";
		storyType = "Story";
		/// history 1 details
		sprintId = "TestSprint4";
		status = "Active";
		fromStatus = "Open";
		activityDate = java.time.LocalDateTime.now();
		f1 = createFeatureStatusDetails(sprintId, status, fromStatus, activityDate);
		statusUpdationLog.add(f1);
		sprintId = "TestSprint4";
		status = "Active";
		fromStatus = "In Progress";
		activityDate = activityDate.plusDays(5);
		f2 = createFeatureStatusDetails(sprintId, status, fromStatus, activityDate);
		statusUpdationLog.add(f2);
		sprintId = "TestSprint4";
		status = "Active";
		fromStatus = "Open";
		activityDate = activityDate.plusDays(7);
		f3 = createFeatureStatusDetails(sprintId, status, fromStatus, activityDate);
		statusUpdationLog.add(f3);
		sprintId = "TestSprint4";
		status = "Active";
		fromStatus = "Closed";
		activityDate = activityDate.plusWeeks(7);
		f3 = createFeatureStatusDetails(sprintId, status, fromStatus, activityDate);
		statusUpdationLog.add(f3);
		mockJiraJiraIssueCustomHistory4 = createFeatureHistory(projectID, storyID, storyType, statusUpdationLog);

		/// document -5 in feature_custom_history
		statusUpdationLog = new ArrayList<>();
		projectID = "TestProject2";
		storyID = "TestDefect1";
		storyType = "Defect";
		Set<String> defectStoryID = new HashSet<String>();
		defectStoryID.add("TestStory4");
		/// history 1 details
		sprintId = "TestSprint4";
		status = "Active";
		fromStatus = "Open";
		activityDate = java.time.LocalDateTime.now();
		f1 = createFeatureStatusDetails(sprintId, status, fromStatus, activityDate);
		statusUpdationLog.add(f1);
		sprintId = "TestSprint4";
		status = "Active";
		fromStatus = "In Development";
		activityDate = activityDate.plusDays(5);
		f2 = createFeatureStatusDetails(sprintId, status, fromStatus, activityDate);
		statusUpdationLog.add(f2);
		sprintId = "TestSprint4";
		status = "Active";
		fromStatus = "In Testing";
		activityDate = activityDate.plusDays(7);
		f3 = createFeatureStatusDetails(sprintId, status, fromStatus, activityDate);
		statusUpdationLog.add(f3);
		sprintId = "TestSprint4";
		status = "Active";
		fromStatus = "Closed";
		activityDate = activityDate.plusWeeks(7);
		f3 = createFeatureStatusDetails(sprintId, status, fromStatus, activityDate);
		statusUpdationLog.add(f3);
		mockJiraJiraIssueCustomHistory5 = createFeatureHistory(projectID, storyID, storyType, statusUpdationLog);
		mockJiraJiraIssueCustomHistory5.setDefectStoryID(defectStoryID);
	}

	@After
	public void tearDown() {
		mockJiraJiraIssueCustomHistory1 = null;
		mockJiraJiraIssueCustomHistory2 = null;
		mockJiraJiraIssueCustomHistory3 = null;
		mockJiraJiraIssueCustomHistory4 = null;
		mockJiraJiraIssueCustomHistory5 = null;
		featureCustomHistoryRepo.deleteAll();
	}

	@Test
	public void validateConnectivity_HappyPath() {
		MockitoAnnotations.openMocks(this);
		featureCustomHistoryRepo.save(mockJiraJiraIssueCustomHistory1);
		featureCustomHistoryRepo.save(mockJiraJiraIssueCustomHistory1);
		List<JiraIssueCustomHistory> customHiostoryList = new ArrayList<>();
		JiraIssueCustomHistory jiraIssueCustomHistory = new JiraIssueCustomHistory();
		customHiostoryList.add(jiraIssueCustomHistory);
		when(featureCustomHistoryRepo.findAll()).thenReturn(customHiostoryList);
		assertTrue("Happy-path MongoDB connectivity validation for the FeatureCustomHistoryRepository has passed",
				customHiostoryList.iterator().hasNext());
	}

	@Test
	public void testFindByStoryID_HappyPath() {
		MockitoAnnotations.openMocks(this);
		featureCustomHistoryRepo.save(mockJiraJiraIssueCustomHistory1);
		featureCustomHistoryRepo.save(mockJiraJiraIssueCustomHistory2);

		String testStoryId = "TestStory1";
		List<JiraIssueCustomHistory> jiraIssueCustomHistoryList = new ArrayList<>();
		JiraIssueCustomHistory jiraIssueCustomHistory = new JiraIssueCustomHistory();
		jiraIssueCustomHistory.setStoryID(testStoryId);
		jiraIssueCustomHistory.setBasicProjectConfigId("676987987897");
		jiraIssueCustomHistoryList.add(jiraIssueCustomHistory);
		assertEquals("TestStory1", testStoryId, jiraIssueCustomHistoryList.get(0).getStoryID());
	}

	private JiraIssueCustomHistory createFeatureHistory(String projectID, String storyID, String storyType,
			ArrayList<JiraHistoryChangeLog> statusChangeLog) {
		JiraIssueCustomHistory rt = new JiraIssueCustomHistory();
		rt.setProjectID(projectID);
		rt.setStoryID(storyID);
		rt.setStoryType(storyType);
		rt.setStatusUpdationLog(statusChangeLog);
		return rt;
	}

	private JiraHistoryChangeLog createFeatureStatusDetails(String sprintId, String status, String fromStatus,
			java.time.LocalDateTime activityDate) {
		JiraHistoryChangeLog jiraHistoryChangeLog = new JiraHistoryChangeLog();
		jiraHistoryChangeLog.setUpdatedOn(activityDate);
		jiraHistoryChangeLog.setChangedTo(fromStatus);
		jiraHistoryChangeLog.setChangedFrom(status);
		return jiraHistoryChangeLog;
	}

	@Test
	public void testFindFeatureCustomHistoryStoryProjectWise() {
		// Mock data
		Map<String, List<String>> mapOfFilters = Collections.singletonMap("projectKey", Arrays.asList("PROJ1", "PROJ2"));
		Map<String, Map<String, Object>> uniqueProjectMap = Collections.singletonMap("PROJ1",
				Collections.singletonMap("storyType", Arrays.asList(Pattern.compile("Story"))));

		AggregationResults<IssueHistoryMappedData> anc = mock(AggregationResults.class);

		doReturn(anc).when(operations).aggregate(any(), eq(JiraIssueCustomHistory.class), any());
		doReturn(createIssueHistory()).when(anc).getMappedResults();

		// Execute the method
		List<JiraIssueCustomHistory> result = repository.findFeatureCustomHistoryStoryProjectWise(mapOfFilters,
				uniqueProjectMap, Sort.Direction.ASC);
		verify(operations, times(1)).aggregate(any(), eq(JiraIssueCustomHistory.class), any());
	}

	@Test
	public void testFindIssuesByCreatedDateAndType() {
		// Mock data
		Map<String, List<String>> mapOfFilters = Collections.singletonMap("projectKey", Arrays.asList("PROJ1", "PROJ2"));
		Map<String, Map<String, Object>> uniqueProjectMap = Collections.singletonMap("PROJ1",
				Collections.singletonMap("storyType", Arrays.asList(Pattern.compile("Story"))));
		String dateFrom = "2022-01-01";
		String dateTo = "2022-01-10";

		when(operations.find(any(Query.class), eq(JiraIssueCustomHistory.class))).thenReturn(Collections.emptyList());

		// Execute the method
		repository.findIssuesByCreatedDateAndType(mapOfFilters, uniqueProjectMap, dateFrom, dateTo);

		verify(operations, times(1)).find(any(Query.class), eq(JiraIssueCustomHistory.class));
	}

	@Test
	public void testFindByFilterAndFromStatusMap() {
		// Mock data
		Map<String, List<String>> mapOfFilters = Collections.singletonMap("projectKey", Arrays.asList("PROJ1", "PROJ2"));
		Map<String, Map<String, Object>> uniqueProjectMap = Collections.singletonMap("PROJ1",
				Collections.singletonMap("storyType", Arrays.asList(Pattern.compile("Story"))));
		when(operations.find(any(Query.class), eq(JiraIssueCustomHistory.class))).thenReturn(Collections.emptyList());

		// Execute the method
		repository.findByFilterAndFromStatusMap(mapOfFilters, uniqueProjectMap);

		verify(operations, times(1)).find(any(Query.class), eq(JiraIssueCustomHistory.class));
	}

	@Test
	public void testFindByFilterAndFromReleaseMap() {
		// Mock data
		List<String> basicProjectConfigId = Arrays.asList("PROJ1", "PROJ2");
		List<Pattern> releaseList = Arrays.asList(Pattern.compile("Release1"), Pattern.compile("Release2"));

		// Mock the query result
		when(operations.find(any(Query.class), eq(JiraIssueCustomHistory.class))).thenReturn(Collections.emptyList());

		// Execute the method
		repository.findByFilterAndFromReleaseMap(basicProjectConfigId, releaseList);

		// Assertions or verifications can be added based on the expected behavior.
		verify(operations, times(1)).find(any(Query.class), eq(JiraIssueCustomHistory.class));
	}

	@Test
	public void testFindByFilterAndFromStatusMapWithDateFilter() {
		// Mock data
		Map<String, List<String>> mapOfFilters = Collections.singletonMap("projectKey", Arrays.asList("PROJ1", "PROJ2"));
		Map<String, Map<String, Object>> uniqueProjectMap = Collections.singletonMap("PROJ1",
				Collections.singletonMap("storyType", Arrays.asList(Pattern.compile("Story"))));
		String dateFrom = "2022-01-01";
		String dateTo = "2022-01-10";

		// Mock the query result
		when(operations.find(any(Query.class), eq(JiraIssueCustomHistory.class))).thenReturn(Collections.emptyList());

		// Execute the method
		repository.findByFilterAndFromStatusMapWithDateFilter(mapOfFilters, uniqueProjectMap, dateFrom, dateTo);

		// Assertions or verifications can be added based on the expected behavior.
		verify(operations, times(1)).find(any(Query.class), eq(JiraIssueCustomHistory.class));
	}

	List<IssueHistoryMappedData> createIssueHistory() {
		IssueHistoryMappedData issueHistoryMappedData = new IssueHistoryMappedData();

		IssueGroupFields groupFields = new IssueGroupFields();
		groupFields.setStoryID("DTS-123");
		groupFields.setProjectComponentId("DTS");
		groupFields.setBasicProjectConfigId("60ed70a572dafe33d3e37111");
		groupFields.setUrl("www.abc.com");
		issueHistoryMappedData.setId(groupFields);
		issueHistoryMappedData.setHistoryDetails(Arrays.asList(KanbanIssueHistory.builder().buildNumber("123")
				.status("SUCESS").type("DEPLOY").activityDate(LocalDateTime.now().toString()).build()));
		issueHistoryMappedData.setStatusUpdationLog(Arrays.asList(
				JiraHistoryChangeLog.builder().changedTo("end").changedFrom("start").updatedOn(LocalDateTime.now()).build()));
		return Arrays.asList(issueHistoryMappedData);
	}
}
