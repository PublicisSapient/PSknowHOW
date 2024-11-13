/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.jira.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
@RunWith(MockitoJUnitRunner.class)
public class OutlierSprintCheckerImplTest {

	@Mock
	private SprintRepository sprintDetailsRepository;

	@Mock
	private JiraIssueRepository jiraIssueRepository;

	@InjectMocks
	private OutlierSprintCheckerImpl outlierSprintChecker;

	private ObjectId basicProjectConfigId;

	@Before
	public void setUp() {
		basicProjectConfigId = new ObjectId();
	}

	@Test
	public void findOutlierSprint_noOverlappingSprints_returnsEmptyMap() {
		when(sprintDetailsRepository.findByBasicProjectConfigIdWithFieldsSorted(basicProjectConfigId))
				.thenReturn(Collections.emptyList());

		Map<String, List<String>> result = outlierSprintChecker.findOutlierSprint(basicProjectConfigId);

		assertTrue(result.isEmpty());
	}

	@Test
	public void findOutlierSprint_withOverlappingSprints_returnsEmptyOutlierSprints() {
		SprintDetails sprint1 = new SprintDetails();
		sprint1.setSprintID("sprint1");
		sprint1.setEndDate("2023-01-01T18:25:00.000Z");

		SprintDetails sprint2 = new SprintDetails();
		sprint2.setSprintID("sprint2");
		sprint2.setStartDate("2023-01-01T18:25:00.000Z");

		when(sprintDetailsRepository.findByBasicProjectConfigIdWithFieldsSorted(basicProjectConfigId))
				.thenReturn(Arrays.asList(sprint1, sprint2));

		JiraIssue issue = new JiraIssue();
		issue.setSprintID("sprint2");
		issue.setNumber("ISSUE-1");

		Map<String, List<String>> result = outlierSprintChecker.findOutlierSprint(basicProjectConfigId);

		assertTrue(result.isEmpty());
	}

	@Test
	public void findOutlierSprint_withOverlappingSprints_logsOutlierSprint() {
		SprintDetails sprint1 = new SprintDetails();
		sprint1.setSprintID("sprint1");
		sprint1.setEndDate("2023-01-01T10:00:00.000Z");

		SprintDetails sprint2 = new SprintDetails();
		sprint2.setSprintID("sprint2");
		sprint2.setStartDate("2023-01-01T09:00:00.000Z");

		when(sprintDetailsRepository.findByBasicProjectConfigIdWithFieldsSorted(basicProjectConfigId))
				.thenReturn(Arrays.asList(sprint1, sprint2));

		outlierSprintChecker.findOutlierSprint(basicProjectConfigId);

		verify(sprintDetailsRepository).findByBasicProjectConfigIdWithFieldsSorted(basicProjectConfigId);

	}
}