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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProjectSprintIssuesServiceImplTest {

	@InjectMocks
	private ProjectSprintIssuesServiceImpl projectSprintIssuesService;

	@Before
	public void setUp() {
		// Initialize the service before test
	}

	@Test
	public void addIssueAddsIssueToCorrectSprint() {
		ObjectId projectId = new ObjectId();
		String sprintName = "Sprint 1";
		String issueKey = "ISSUE-1";

		projectSprintIssuesService.addIssue(projectId, sprintName, issueKey);

		Map<String, List<String>> sprintIssueMap = projectSprintIssuesService.getSprintIssueMapForProject(projectId);
		assertNotNull(sprintIssueMap);
		assertTrue(sprintIssueMap.containsKey(sprintName));
		assertTrue(sprintIssueMap.get(sprintName).contains(issueKey));
	}

	@Test
	public void removeProjectRemovesAllIssuesForProject() {
		ObjectId projectId = new ObjectId();
		String sprintName = "Sprint 1";
		String issueKey = "ISSUE-1";

		projectSprintIssuesService.addIssue(projectId, sprintName, issueKey);
		projectSprintIssuesService.removeProject(projectId);

		Map<String, List<String>> sprintIssueMap = projectSprintIssuesService.getSprintIssueMapForProject(projectId);
		assertTrue(sprintIssueMap.isEmpty());
	}

	@Test
	public void getSprintIssueMapForProjectReturnsEmptyMapForNonExistentProject() {
		ObjectId projectId = new ObjectId();

		Map<String, List<String>> sprintIssueMap = projectSprintIssuesService.getSprintIssueMapForProject(projectId);
		assertNotNull(sprintIssueMap);
		assertTrue(sprintIssueMap.isEmpty());
	}

	@Test
	public void findOutliersBelowLowerBoundReturnsEmptyMapForNullSprintIssueMap() {
		ObjectId projectId = new ObjectId();
		Map<String, List<String>> outliers = projectSprintIssuesService.findOutliersBelowLowerBound(projectId);
		assertNotNull(outliers);
		assertTrue(outliers.isEmpty());
	}

	@Test
	public void findOutliersBelowLowerBoundReturnsEmptyMapForEmptySprintIssueMap() {
		ObjectId projectId = new ObjectId();
		projectSprintIssuesService.addIssue(projectId, "Sprint 1", "ISSUE-1");
		projectSprintIssuesService.removeProject(projectId);
		Map<String, List<String>> outliers = projectSprintIssuesService.findOutliersBelowLowerBound(projectId);
		assertNotNull(outliers);
		assertTrue(outliers.isEmpty());
	}

	@Test
	public void findOutliersBelowLowerBoundReturnsEmptyMapForSingleSprint() {
		ObjectId projectId = new ObjectId();
		projectSprintIssuesService.addIssue(projectId, "Sprint 1", "ISSUE-1");
		Map<String, List<String>> outliers = projectSprintIssuesService.findOutliersBelowLowerBound(projectId);
		assertNotNull(outliers);
		assertTrue(outliers.isEmpty());
	}

	@Test
	public void findOutliersBelowLowerBoundReturnsEmptyMapForNoOutliers() {
		ObjectId projectId = new ObjectId();
		projectSprintIssuesService.addIssue(projectId, "Sprint 1", "ISSUE-1");
		projectSprintIssuesService.addIssue(projectId, "Sprint 1", "ISSUE-2");
		projectSprintIssuesService.addIssue(projectId, "Sprint 2", "ISSUE-3");
		projectSprintIssuesService.addIssue(projectId, "Sprint 2", "ISSUE-4");
		Map<String, List<String>> outliers = projectSprintIssuesService.findOutliersBelowLowerBound(projectId);
		assertNotNull(outliers);
		assertTrue(outliers.isEmpty());
	}

	@Test
	public void findOutliersBelowLowerBoundHandlesSprintsWithSameNumberOfIssues() {
		ObjectId projectId = new ObjectId();
		projectSprintIssuesService.addIssue(projectId, "Sprint 1", "ISSUE-1");
		projectSprintIssuesService.addIssue(projectId, "Sprint 2", "ISSUE-2");
		Map<String, List<String>> outliers = projectSprintIssuesService.findOutliersBelowLowerBound(projectId);
		assertNotNull(outliers);
		assertTrue(outliers.isEmpty());
	}

	@Test
	public void findOutliersBelowLowerBoundReturnsEmptyMapForNoIssuesInSprints() {
		ObjectId projectId = new ObjectId();
		projectSprintIssuesService.addIssue(projectId, "Sprint 1", "ISSUE-1");
		projectSprintIssuesService.removeProject(projectId);
		projectSprintIssuesService.addIssue(projectId, "Sprint 1", "ISSUE-1");
		projectSprintIssuesService.getSprintIssueMapForProject(projectId).get("Sprint 1").clear();

		Map<String, List<String>> outliers = projectSprintIssuesService.findOutliersBelowLowerBound(projectId);
		assertNotNull(outliers);
		assertTrue(outliers.isEmpty());
	}

	@Test
	public void testPrintSprintIssuesTableSimple() {
		Map<String, List<String>> sprintIssueMap = new HashMap<>();
		sprintIssueMap.put("Sprint 1", List.of("ISSUE-1", "ISSUE-2"));
		sprintIssueMap.put("Sprint 2", List.of("ISSUE-3"));

		String actualOutput = projectSprintIssuesService.printSprintIssuesTable(sprintIssueMap);

		assertNotNull(actualOutput);
		assertTrue(actualOutput.contains("Sprint 1"));
		assertTrue(actualOutput.contains("ISSUE-1"));
	}

}
