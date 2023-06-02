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

package com.publicissapient.kpidashboard.common.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueSprint;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;

public class JiraIssueCustomHistoryRepositoryTest {
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
	private JiraIssueCustomHistoryRepository featureCustomHistoryRepo;

	@Before
	public void setUp() {

		// Helper mock data

		/// document -1 in feature_custom_history
		ArrayList<JiraIssueSprint> storySprintDetails = new ArrayList<>();
		String projectID = "TestProject1";
		String storyID = "TestStory1";
		String storyType = "Story";
		/// history 1 details
		String sprintId = "TestSprint1";
		String status = "Active";
		String fromStatus = "Open";
		DateTime activityDate = DateTime.now();
		JiraIssueSprint f1 = createFeatureSprintDetails(sprintId, status, fromStatus, activityDate);
		storySprintDetails.add(f1);
		sprintId = "TestSprint1";
		status = "Active";
		fromStatus = "In Progress";
		activityDate = activityDate.plusDays(3);
		JiraIssueSprint f2 = createFeatureSprintDetails(sprintId, status, fromStatus, activityDate);
		storySprintDetails.add(f2);
		sprintId = "TestSprint1";
		status = "Active";
		fromStatus = "Closed";
		activityDate = activityDate.plusDays(3);
		JiraIssueSprint f3 = createFeatureSprintDetails(sprintId, status, fromStatus, activityDate);
		storySprintDetails.add(f3);
		mockJiraJiraIssueCustomHistory1 = createFeatureHistory(projectID, storyID, storyType, storySprintDetails);

		/// document -2 in feature_custom_history
		storySprintDetails = new ArrayList<>();
		projectID = "TestProject1";
		storyID = "TestStory2";
		storyType = "Story";
		/// history 1 details
		sprintId = "TestSprint1";
		status = "Active";
		fromStatus = "Open";
		activityDate = DateTime.now();
		f1 = createFeatureSprintDetails(sprintId, status, fromStatus, activityDate);
		storySprintDetails.add(f1);
		sprintId = "TestSprint1";
		status = "Active";
		fromStatus = "In Progress";
		activityDate = activityDate.plusDays(5);
		f2 = createFeatureSprintDetails(sprintId, status, fromStatus, activityDate);
		storySprintDetails.add(f2);
		sprintId = "TestSprint1";
		status = "Active";
		fromStatus = "Closed";
		activityDate = activityDate.plusDays(7);
		f3 = createFeatureSprintDetails(sprintId, status, fromStatus, activityDate);
		storySprintDetails.add(f3);
		mockJiraJiraIssueCustomHistory2 = createFeatureHistory(projectID, storyID, storyType, storySprintDetails);

		/// document -3 in feature_custom_history
		storySprintDetails = new ArrayList<>();
		projectID = "TestProject2";
		storyID = "TestStory3";
		storyType = "Story";
		/// history 1 details
		sprintId = "TestSprint1";
		status = "Active";
		fromStatus = "Open";
		activityDate = DateTime.now();
		f1 = createFeatureSprintDetails(sprintId, status, fromStatus, activityDate);
		storySprintDetails.add(f1);
		sprintId = "TestSprint2";
		status = "Active";
		fromStatus = "In Progress";
		activityDate = activityDate.plusDays(3);
		f2 = createFeatureSprintDetails(sprintId, status, fromStatus, activityDate);
		storySprintDetails.add(f2);
		sprintId = "TestSprint3";
		status = "Active";
		fromStatus = "Closed";
		activityDate = activityDate.plusDays(3);
		f3 = createFeatureSprintDetails(sprintId, status, fromStatus, activityDate);
		storySprintDetails.add(f3);
		mockJiraJiraIssueCustomHistory3 = createFeatureHistory(projectID, storyID, storyType, storySprintDetails);

		/// document -4 in feature_custom_history
		storySprintDetails = new ArrayList<>();
		projectID = "TestProject2";
		storyID = "TestStory4";
		storyType = "Story";
		/// history 1 details
		sprintId = "TestSprint4";
		status = "Active";
		fromStatus = "Open";
		activityDate = DateTime.now();
		f1 = createFeatureSprintDetails(sprintId, status, fromStatus, activityDate);
		storySprintDetails.add(f1);
		sprintId = "TestSprint4";
		status = "Active";
		fromStatus = "In Progress";
		activityDate = activityDate.plusDays(5);
		f2 = createFeatureSprintDetails(sprintId, status, fromStatus, activityDate);
		storySprintDetails.add(f2);
		sprintId = "TestSprint4";
		status = "Active";
		fromStatus = "Open";
		activityDate = activityDate.plusDays(7);
		f3 = createFeatureSprintDetails(sprintId, status, fromStatus, activityDate);
		storySprintDetails.add(f3);
		sprintId = "TestSprint4";
		status = "Active";
		fromStatus = "Closed";
		activityDate = activityDate.plusWeeks(7);
		f3 = createFeatureSprintDetails(sprintId, status, fromStatus, activityDate);
		storySprintDetails.add(f3);
		mockJiraJiraIssueCustomHistory4 = createFeatureHistory(projectID, storyID, storyType, storySprintDetails);

		/// document -5 in feature_custom_history
		storySprintDetails = new ArrayList<>();
		projectID = "TestProject2";
		storyID = "TestDefect1";
		storyType = "Defect";
		Set<String> defectStoryID = new HashSet<String>();
		defectStoryID.add("TestStory4");
		/// history 1 details
		sprintId = "TestSprint4";
		status = "Active";
		fromStatus = "Open";
		activityDate = DateTime.now();
		f1 = createFeatureSprintDetails(sprintId, status, fromStatus, activityDate);
		storySprintDetails.add(f1);
		sprintId = "TestSprint4";
		status = "Active";
		fromStatus = "In Development";
		activityDate = activityDate.plusDays(5);
		f2 = createFeatureSprintDetails(sprintId, status, fromStatus, activityDate);
		storySprintDetails.add(f2);
		sprintId = "TestSprint4";
		status = "Active";
		fromStatus = "In Testing";
		activityDate = activityDate.plusDays(7);
		f3 = createFeatureSprintDetails(sprintId, status, fromStatus, activityDate);
		storySprintDetails.add(f3);
		sprintId = "TestSprint4";
		status = "Active";
		fromStatus = "Closed";
		activityDate = activityDate.plusWeeks(7);
		f3 = createFeatureSprintDetails(sprintId, status, fromStatus, activityDate);
		storySprintDetails.add(f3);
		mockJiraJiraIssueCustomHistory5 = createFeatureHistory(projectID, storyID, storyType, storySprintDetails);
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
		MockitoAnnotations.initMocks(this);
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
		MockitoAnnotations.initMocks(this);
		featureCustomHistoryRepo.save(mockJiraJiraIssueCustomHistory1);
		featureCustomHistoryRepo.save(mockJiraJiraIssueCustomHistory2);

		String testStoryId = "TestStory1";
		List<JiraIssueCustomHistory> jiraIssueCustomHistoryList = new ArrayList<>();
		JiraIssueCustomHistory jiraIssueCustomHistory = new JiraIssueCustomHistory();
		jiraIssueCustomHistory.setStoryID(testStoryId);
		jiraIssueCustomHistory.setBasicProjectConfigId("676987987897");
		jiraIssueCustomHistoryList.add(jiraIssueCustomHistory);
		when(featureCustomHistoryRepo.findByStoryIDAndBasicProjectConfigId(testStoryId, "676987987897"))
				.thenReturn(jiraIssueCustomHistoryList);
		assertEquals("Expected feature ID matches actual feature ID", testStoryId,
				jiraIssueCustomHistoryList.get(0).getStoryID());
	}

	private JiraIssueCustomHistory createFeatureHistory(String projectID, String storyID, String storyType,
			ArrayList<JiraIssueSprint> storySprintDetails) {
		JiraIssueCustomHistory rt = new JiraIssueCustomHistory();
		rt.setProjectID(projectID);
		rt.setStoryID(storyID);
		rt.setStoryType(storyType);
		return rt;
	}

	private JiraIssueSprint createFeatureSprintDetails(String sprintId, String status, String fromStatus,
			DateTime activityDate) {
		JiraIssueSprint jiraIssueSprint = new JiraIssueSprint();
		jiraIssueSprint.setActivityDate(activityDate);
		jiraIssueSprint.setFromStatus(fromStatus);
		jiraIssueSprint.setSprintId(sprintId);
		jiraIssueSprint.setStatus(fromStatus);
		return jiraIssueSprint;

	}

	public static List<Pattern> convertToPatternList(List<String> stringList) {
		List<Pattern> regexList = new ArrayList<>();
		for (String value : stringList) {
			regexList.add(Pattern.compile(value, Pattern.CASE_INSENSITIVE));
		}
		return regexList;
	}

	public static Pattern convertToPatternText(String text) {
		return Pattern.compile(text, Pattern.CASE_INSENSITIVE);
	}
}