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
import static org.mockito.ArgumentMatchers.eq;
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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectReleaseV2;
import com.publicissapient.kpidashboard.common.model.jira.DataRequest;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueV2;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetailsV2;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseV2Repo;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueV2Repository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintV2Repository;

class ProjectDataServiceImplTest {

	@Mock
	JiraIssueV2Repository jiraIssueV2Repository;

	@Mock
	SprintV2Repository sprintV2Repository;

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
	void testGetProjectJiraIssuesWithIssueIds() {
		// Arrange
		DataRequest dataRequest = new DataRequest();
		dataRequest.setProjectId("65118da7965fbb0d14bce23c");
		dataRequest.setIssueIds(Collections.singletonList("issueId"));

		Page<JiraIssueV2> mockPage = Mockito.mock(Page.class);
		when(jiraIssueV2Repository.findByBasicProjectConfigIdAndIssueIdIn(anyString(), anyList(), any(Pageable.class)))
				.thenReturn(mockPage);

		// Act
		projectDataService.getProjectJiraIssues(dataRequest, 0, 10);

		// Assert
		verify(jiraIssueV2Repository, times(1)).findByBasicProjectConfigIdAndIssueIdIn(eq(dataRequest.getProjectId()),
				eq(dataRequest.getIssueIds()), any(PageRequest.class));
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
		projectDataService.getProjectSprints(dataRequest,true);
		verify(sprintV2Repository, times(1)).findByBasicProjectConfigIdAndStateIgnoreCase(any(),any());
	}

	@Test
	void testGetProjectReleases() {
		DataRequest dataRequest = new DataRequest();
		dataRequest.setProjectId("65118da7965fbb0d14bce23c");
		projectDataService.getProjectReleases(dataRequest);
		verify(projectReleaseV2Repo, times(1)).findByConfigId(any());
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
		projectDataService.getProjectSprints(dataRequest,false);
		verify(sprintV2Repository, times(0)).findByBasicProjectConfigId(any());
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
		Page<JiraIssueV2> mockPage = Mockito.mock(Page.class);
		when(jiraIssueV2Repository.findByBasicProjectConfigIdAndBoardId(anyString(), anyString(), any()))
				.thenReturn(mockPage);
		ServiceResponse result = projectDataService.getProjectJiraIssues(dataRequest, 0, 10);
		assertNotNull(result.getData());
	}

	@Test
	void testGetProjectJiraIssuesWithSprintIds() {
		// Arrange
		DataRequest dataRequest = new DataRequest();
		dataRequest.setProjectId("65118da7965fbb0d14bce23c");
		dataRequest.setBoardId("boardId");
		dataRequest.setSprintIds(Collections.singletonList("sprintId"));

		Page<JiraIssueV2> mockPage = Mockito.mock(Page.class);
		when(jiraIssueV2Repository.findByProjectIdAndBoardIdAndSprintIdIn(anyString(), anyString(), anyList(),
				any(Pageable.class))).thenReturn(mockPage);

		// Act
		projectDataService.getProjectJiraIssues(dataRequest, 0, 10);

		// Assert
		verify(jiraIssueV2Repository, times(1)).findByProjectIdAndBoardIdAndSprintIdIn(eq(dataRequest.getProjectId()),
				eq(dataRequest.getBoardId()), eq(dataRequest.getSprintIds()), any(PageRequest.class));
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

	@Test
	void testGetProjectJiraIssuesWhenIssuesExist() {
		// Arrange
		DataRequest dataRequest = new DataRequest();
		dataRequest.setProjectKey("Test");
		Page<JiraIssueV2> mockPage = Mockito.mock(Page.class);
		when(jiraIssueV2Repository.findByProjectKey(any(), any())).thenReturn(mockPage);

		// Act
		ServiceResponse response = projectDataService.getProjectJiraIssues(dataRequest, 0, 10);

		// Assert
		assertNotNull(response);
		assertTrue(response.getSuccess());
	}

	@Test
	void testGetIssueTypesWhenIssueTypesExist() {
		// Arrange
		DataRequest dataRequest = new DataRequest();
		dataRequest.setBoardId("boardId");
		List<JiraIssueV2> mockIssueTypes = new ArrayList<>();
		mockIssueTypes.add(new JiraIssueV2());
		when(jiraIssueV2Repository.findIssueTypesByBoardId(any())).thenReturn(mockIssueTypes);

		// Act
		ServiceResponse response = projectDataService.getIssueTypes(dataRequest);

		// Assert
		assertNotNull(response);
		assertTrue(response.getSuccess());
		assertEquals("Issue types have been successfully retrieved using the provided 'boardId'.",
				response.getMessage());
		assertNotNull(response.getData());
		verify(jiraIssueV2Repository, times(1)).findIssueTypesByBoardId(any());
	}

	@Test
	void testGetIssueTypesWhenIssueTypesExist_byProjectKey() {
		// Arrange
		DataRequest dataRequest = new DataRequest();
		dataRequest.setProjectKey("dummyKey");
		List<JiraIssueV2> mockIssueTypes = new ArrayList<>();
		mockIssueTypes.add(new JiraIssueV2());
		when(jiraIssueV2Repository.findIssueTypesByProjectKey(any())).thenReturn(mockIssueTypes);

		// Act
		ServiceResponse response = projectDataService.getIssueTypes(dataRequest);

		// Assert
		assertNotNull(response);
		assertTrue(response.getSuccess());
		assertNotNull(response.getData());
		verify(jiraIssueV2Repository, times(1)).findIssueTypesByProjectKey(any());
	}

	@Test
	void testGetProjectSprintsWhenSprintDetailsExist() {
		// Arrange
		DataRequest dataRequest = new DataRequest();
		dataRequest.setProjectId("65118da7965fbb0d14bce23c");
		List<SprintDetailsV2> mockSprintDetails = new ArrayList<>();
		mockSprintDetails.add(new SprintDetailsV2());
		when(sprintV2Repository.findByBasicProjectConfigId(any())).thenReturn(mockSprintDetails);

		// Act
		ServiceResponse response = projectDataService.getProjectSprints(dataRequest,false);

		// Assert
		assertNotNull(response);
		assertTrue(response.getSuccess());
		assertEquals("The sprint details for the Scrum project have been successfully retrieved.",
				response.getMessage());
		assertNotNull(response.getData());
		assertEquals(mockSprintDetails, response.getData());
		verify(sprintV2Repository, times(1)).findByBasicProjectConfigId(any());
	}
}