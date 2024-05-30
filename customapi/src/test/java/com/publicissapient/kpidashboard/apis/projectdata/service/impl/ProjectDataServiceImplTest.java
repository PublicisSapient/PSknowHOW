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

package com.publicissapient.kpidashboard.apis.projectdata.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectReleaseV2;
import com.publicissapient.kpidashboard.common.model.jira.DataRequest;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseV2Repo;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueV2Repository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintV2Repository;

class ProjectDataServiceImplTest {

	@Mock
	JiraIssueV2Repository jiraIssueV2Repository;

	@Mock
	SprintV2Repository SprintV2Repository;

	@Mock
	ProjectReleaseV2Repo projectReleaseV2Repo;

	@Mock
	ProjectBasicConfigRepository projectBasicConfigRepository;

	@InjectMocks
	ProjectDataServiceImpl projectDataService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testGetProjectJiraIssues() {
		DataRequest dataRequest = new DataRequest();
		dataRequest.setBoardId("boardId");
		dataRequest.setProjectId("65118da7965fbb0d14bce23c");
		projectDataService.getProjectJiraIssues(dataRequest);
		verify(jiraIssueV2Repository, times(1)).findByBasicProjectConfigIdAndBoardId(anyString(), anyString());
	}

	@Test
	void testGetIssueTypes() {
		DataRequest dataRequest = new DataRequest();
		dataRequest.setBoardId("boardId");
		projectDataService.getIssueTypes(dataRequest);
		verify(jiraIssueV2Repository, times(1)).findIssueTypesByBoardId(anyString());
	}

	@Test
	void testGetIssueTypesWithProjectKey() {
		DataRequest dataRequest = new DataRequest();
		dataRequest.setProjectKey("key");
		projectDataService.getIssueTypes(dataRequest);
		verify(jiraIssueV2Repository, times(1)).findIssueTypesByProjectKey(anyString());
	}

	@Test
	void testGetProjectSprints() {
		DataRequest dataRequest = new DataRequest();
		dataRequest.setProjectId("65118da7965fbb0d14bce23c");
		projectDataService.getProjectSprints(dataRequest);
		verify(SprintV2Repository, times(1)).findByBasicProjectConfigId(any());
	}

	@Test
	void testGetProjectReleases() {
		DataRequest dataRequest = new DataRequest();
		dataRequest.setProjectId("65118da7965fbb0d14bce23c");
		projectDataService.getProjectReleases(dataRequest);
		verify(projectReleaseV2Repo, times(1)).findByConfigId(any());
	}

	@Test
	void testGetProjectJiraIssuesWithNullBoardIdAndProjectId() {
		DataRequest dataRequest = new DataRequest();
		projectDataService.getProjectJiraIssues(dataRequest);
		verify(jiraIssueV2Repository, times(0)).findByBasicProjectConfigIdAndBoardId(anyString(), anyString());
	}

	@Test
	void testGetIssueTypesWithNullBoardIdAndProjectKey() {
		DataRequest dataRequest = new DataRequest();
		projectDataService.getIssueTypes(dataRequest);
		verify(jiraIssueV2Repository, times(0)).findIssueTypesByBoardId(anyString());
		verify(jiraIssueV2Repository, times(0)).findIssueTypesByProjectKey(anyString());
	}

	@Test
	void testGetProjectSprintsWithNullProjectId() {
		DataRequest dataRequest = new DataRequest();
		projectDataService.getProjectSprints(dataRequest);
		verify(SprintV2Repository, times(0)).findByBasicProjectConfigId(any());
	}

	@Test
	void testGetProjectReleasesWithNullProjectId() {
		DataRequest dataRequest = new DataRequest();
		projectDataService.getProjectReleases(dataRequest);
		verify(projectReleaseV2Repo, times(0)).findByConfigId(any());
	}

	@Test
	void testGetProjectJiraIssuesWithEmptyRepository() {
		DataRequest dataRequest = new DataRequest();
		dataRequest.setBoardId("boardId");
		dataRequest.setProjectId("65118da7965fbb0d14bce23c");
		when(jiraIssueV2Repository.findByBasicProjectConfigIdAndBoardId(anyString(), anyString()))
				.thenReturn(Collections.emptyList());
		ServiceResponse result = projectDataService.getProjectJiraIssues(dataRequest);
		assertNull(result.getData());
	}

	@Test
	void testGetProjectJiraIssuesWithProjectKey() {
		DataRequest dataRequest = new DataRequest();
		dataRequest.setProjectKey("key");
		when(jiraIssueV2Repository.findByProjectKey(anyString())).thenReturn(Collections.emptyList());
		ServiceResponse result = projectDataService.getProjectJiraIssues(dataRequest);
		assertNull(result.getData());
	}

	@Test
	void testGetProjectJiraIssuesWithIssueIds() {
		DataRequest dataRequest = new DataRequest();
		dataRequest.setProjectId("65118da7965fbb0d14bce23c");
		dataRequest.setIssueIds(Collections.singletonList("issueId"));
		projectDataService.getProjectJiraIssues(dataRequest);
		verify(jiraIssueV2Repository, times(1)).findByBasicProjectConfigIdAndIssueIdIn(anyString(), anyList());
	}

	@Test
	void testGetProjectJiraIssuesWithSprintIds() {
		DataRequest dataRequest = new DataRequest();
		dataRequest.setProjectId("65118da7965fbb0d14bce23c");
		dataRequest.setBoardId("boardId");
		dataRequest.setSprintIds(Collections.singletonList("sprintId"));
		projectDataService.getProjectJiraIssues(dataRequest);
		verify(jiraIssueV2Repository, times(1)).findByProjectIdAndBoardIdAndSprintIdIn(anyString(), anyString(),
				anyList());
	}

	@Test
	void testGetScrumProjects() {
		// Arrange
		List<ProjectBasicConfig> mockScrumProjects = new ArrayList<>();
		mockScrumProjects.add(new ProjectBasicConfig());
		when(projectBasicConfigRepository.findByKanban(false)).thenReturn(mockScrumProjects);

		// Act
		ServiceResponse response = projectDataService.getScrumProjects();

		// Assert
		assertNotNull(response);
		assertTrue(response.getSuccess());
		assertEquals("The list of Scrum projects has been successfully retrieved.", response.getMessage());
		assertNotNull(response.getData());
		assertEquals(mockScrumProjects, response.getData());
		verify(projectBasicConfigRepository, times(1)).findByKanban(false);
	}

	@Test
	void testGetScrumProjectsWhenListIsEmpty() {
		// Arrange
		when(projectBasicConfigRepository.findByKanban(false)).thenReturn(Collections.emptyList());

		// Act
		ServiceResponse response = projectDataService.getScrumProjects();

		// Assert
		assertNotNull(response);
		assertTrue(response.getSuccess());
		assertEquals("No Scrum projects were found.", response.getMessage());
		assertNull(response.getData());
		verify(projectBasicConfigRepository, times(1)).findByKanban(false);
	}

	@Test
	void testGetScrumProjectsWhenListIsNotEmpty() {
		// Arrange
		List<ProjectBasicConfig> mockScrumProjects = new ArrayList<>();
		mockScrumProjects.add(new ProjectBasicConfig());
		when(projectBasicConfigRepository.findByKanban(false)).thenReturn(mockScrumProjects);

		// Act
		ServiceResponse response = projectDataService.getScrumProjects();

		// Assert
		assertNotNull(response);
		assertTrue(response.getSuccess());
		assertEquals("The list of Scrum projects has been successfully retrieved.", response.getMessage());
		assertNotNull(response.getData());
		assertEquals(mockScrumProjects, response.getData());
		verify(projectBasicConfigRepository, times(1)).findByKanban(false);
	}

	@Test
	void testGetProjectReleasesWhenReleaseDetailsExist() {
		// Arrange
		DataRequest dataRequest = new DataRequest();
		dataRequest.setProjectId("65118da7965fbb0d14bce23c");
		ProjectReleaseV2 mockProjectReleaseDetails = new ProjectReleaseV2();
		when(projectReleaseV2Repo.findByConfigId(any())).thenReturn(mockProjectReleaseDetails);

		// Act
		ServiceResponse response = projectDataService.getProjectReleases(dataRequest);

		// Assert
		assertNotNull(response);
		assertTrue(response.getSuccess());
		assertEquals("The release details for the Scrum project have been successfully retrieved.",
				response.getMessage());
		assertNotNull(response.getData());
		assertEquals(mockProjectReleaseDetails, response.getData());
		verify(projectReleaseV2Repo, times(1)).findByConfigId(any());
	}

}