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


package com.publicissapient.kpidashboard.jira.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.jira.model.JiraProcessor;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;

import com.publicissapient.kpidashboard.common.model.ProcessorExecutionBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfigurationImpl;
import com.publicissapient.kpidashboard.jira.service.OngoingExecutionsService;

@RunWith(MockitoJUnitRunner.class)
public class JobControllerTest {
	@Mock
	private Job fetchIssueKanbanJqlJob;

	@Mock
	private JobLauncher jobLauncher;

	@Mock
	private Job fetchIssueScrumBoardJob;

	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;

	@Mock
	private ProjectToolConfigRepository toolRepository;

	@Mock
	private OngoingExecutionsService ongoingExecutionsService;

	@Mock
	private Job fetchIssueSprintJob;

	@InjectMocks
	private JobController jobController;

	@Mock
	private Job fetchIssueKanbanBoardJob;

	@Mock
	private FetchProjectConfigurationImpl fetchProjectConfiguration;
	@Mock
	private Job fetchIssueScrumJqlJob;
	@Mock
	private JiraProcessor jiraProcessor;

	@Mock
	private JiraProcessorRepository jiraProcessorRepository;

	@Before
	public void init() {
		when(jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA)).thenReturn(jiraProcessor);
		when(jiraProcessor.getId()).thenReturn(new ObjectId("63bfa0d5b7617e260763ca21"));
	}

	@Test
	public void testStartScrumBoardJob_Success() throws JobExecutionException {
		// Mocking fetchBasicProjConfId to return a list of project IDs
		List<String> projectIds = new ArrayList<>();
		projectIds.add("projectId1");
		projectIds.add("projectId2");
		when(fetchProjectConfiguration.fetchBasicProjConfId(any(), anyBoolean(), anyBoolean())).thenReturn(projectIds);

		// Calling the method
		ResponseEntity<String> response = jobController.startScrumBoardJob();

		// Verifying the response
		assertEquals("job started for scrum board", response.getBody());
	}

	@Test
	public void testStartScrumBoardJob_ExceptionHandling() throws JobExecutionException {
		// Mocking fetchBasicProjConfId to return a list of project IDs
		List<String> projectIds = new ArrayList<>();
		projectIds.add("projectId1");
		when(fetchProjectConfiguration.fetchBasicProjConfId(any(), anyBoolean(), anyBoolean())).thenReturn(projectIds);

		// Mocking jobLauncher.run() to throw an exception
	    doThrow(new RuntimeException("Simulated job execution exception")).when(jobLauncher)
				.run(eq(fetchIssueScrumBoardJob), any(JobParameters.class));

		// Calling the method
		ResponseEntity<String> response = jobController.startScrumBoardJob();

		// Verifying the response
		assertEquals("job started for scrum board", response.getBody());
	}

	@Test
	public void testStartScrumJqlJob_Success() throws Exception {
		// Mocking fetchBasicProjConfId to return a list of project IDs
		List<String> projectIds = new ArrayList<>();
		projectIds.add("projectId1");
		when(fetchProjectConfiguration.fetchBasicProjConfId(any(), anyBoolean(), anyBoolean())).thenReturn(projectIds);

		// Mocking jobLauncher.run() to return a JobExecution instance
        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(new JobExecution(1L));

		// Calling the method
		ResponseEntity<String> response = jobController.startScrumJqlJob();

		// Verifying the response
		assertEquals("job started for scrum JQL", response.getBody());
	}

	@Test
	public void testStartScrumJqlJob_ExceptionHandling() throws Exception {
		// Mocking fetchBasicProjConfId to return a list of project IDs
		List<String> projectIds = new ArrayList<>();
		projectIds.add("projectId1");
		when(fetchProjectConfiguration.fetchBasicProjConfId(any(), anyBoolean(), anyBoolean())).thenReturn(projectIds);

		// Mocking jobLauncher.run() to throw an exception
		doThrow(new RuntimeException("Simulated job execution exception")).when(jobLauncher)
				.run(eq(fetchIssueScrumJqlJob), any(JobParameters.class));

		// Calling the method
		ResponseEntity<String> response = jobController.startScrumJqlJob();

		// Verifying the response
		assertEquals("job started for scrum JQL", response.getBody());
	}

	@Test
	public void testStartKanbanJob_Success() throws Exception {
		// Mocking fetchBasicProjConfId to return a list of project IDs
		List<String> projectIds = new ArrayList<>();
		projectIds.add("projectId1");
		when(fetchProjectConfiguration.fetchBasicProjConfId(any(), anyBoolean(), anyBoolean())).thenReturn(projectIds);

		// Mocking jobLauncher.run() to return a JobExecution instance
		when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(new JobExecution(1L));

		// Calling the method
		ResponseEntity<String> response = jobController.startKanbanJob();

		// Verifying the response
		assertEquals("job started for Kanban Board", response.getBody());
	}

	@Test
	public void testStartKanbanJob_ExceptionHandling() throws Exception {
		// Mocking fetchBasicProjConfId to return a list of project IDs
		List<String> projectIds = new ArrayList<>();
		projectIds.add("projectId1");
		when(fetchProjectConfiguration.fetchBasicProjConfId(any(), anyBoolean(), anyBoolean())).thenReturn(projectIds);

		// Mocking jobLauncher.run() to throw an exception
		//doThrow(new RuntimeException("Simulated job execution exception")).when(jobLauncher)
			//	.run(eq(fetchIssueKanbanBoardJob), any(JobParameters.class));

		// Calling the method
		ResponseEntity<String> response = jobController.startKanbanJob();

		// Verifying the response
		assertEquals("job started for Kanban Board", response.getBody());
	}

	@Test
	public void testStartKanbanJqlJob_Success() throws Exception {
		// Mocking fetchBasicProjConfId to return a list of project IDs
		List<String> projectIds = new ArrayList<>();
		projectIds.add("projectId1");
		when(fetchProjectConfiguration.fetchBasicProjConfId(any(), anyBoolean(), anyBoolean())).thenReturn(projectIds);

		// Mocking jobLauncher.run() to return a JobExecution instance
		when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(new JobExecution(1L));

		// Calling the method
		ResponseEntity<String> response = jobController.startKanbanJqlJob();

		// Verifying the response
		assertEquals("job started for Kanban JQL", response.getBody());
	}

	@Test
	public void testStartKanbanJqlJob_ExceptionHandling() throws Exception {
		// Mocking fetchBasicProjConfId to return a list of project IDs
		List<String> projectIds = new ArrayList<>();
		projectIds.add("projectId1");
		when(fetchProjectConfiguration.fetchBasicProjConfId(any(), anyBoolean(), anyBoolean())).thenReturn(projectIds);

		// Mocking jobLauncher.run() to throw an exception
		doThrow(new RuntimeException("Simulated job execution exception")).when(jobLauncher)
				.run(eq(fetchIssueKanbanJqlJob), any(JobParameters.class));

		// Calling the method
		ResponseEntity<String> response = jobController.startKanbanJqlJob();

		// Verifying the response
		assertEquals("job started for Kanban JQL", response.getBody());
	}

	@Test
	public void testStartFetchSprintJob_Success() throws Exception {
		// Mocking jobLauncher.run() to return a JobExecution instance
		when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(new JobExecution(1L));

		// Calling the method with a sprintId
		ResponseEntity<String> response = jobController.startFetchSprintJob("sprint123");

		// Verifying the response
		assertEquals("job started for Sprint : sprint123", response.getBody());
	}

	@Test
	public void testStartFetchSprintJob_ExceptionHandling() throws Exception {
		// Mocking jobLauncher.run() to throw an exception
		doThrow(new RuntimeException("Simulated job execution exception")).when(jobLauncher)
				.run(eq(fetchIssueSprintJob), any(JobParameters.class));

		// Calling the method with a sprintId
		ResponseEntity<String> response = jobController.startFetchSprintJob("sprint456");

		// Verifying the response
		assertEquals("job started for Sprint : sprint456", response.getBody());
	}

	@Test
	public void testStartProjectWiseIssueJob_ExceptionHandling() throws Exception {
		// Mocking ongoingExecutionsService.isExecutionInProgress() to return true
		when(ongoingExecutionsService.isExecutionInProgress(anyString())).thenReturn(true);

		// Calling the method with ProcessorExecutionBasicConfig
		ProcessorExecutionBasicConfig processorExecutionBasicConfig = new ProcessorExecutionBasicConfig();
		processorExecutionBasicConfig.setProjectBasicConfigIds(Collections.singletonList("projex8749874ctId"));
		ResponseEntity<String> response = jobController.startProjectWiseIssueJob(processorExecutionBasicConfig);

		// Verifying the response and mocked interactions
		assertEquals("Jira processor run is already in progress for this project. Please try after some time.",
				response.getBody());
	}

	@Test
	public void testStartProjectWiseIssueJob_Success() throws Exception {
		// Mocking ongoingExecutionsService.isExecutionInProgress() to return false
		when(ongoingExecutionsService.isExecutionInProgress(anyString())).thenReturn(false);
		// Mocking findById() to return an Optional<ProjectBasicConfig>
		Optional<ProjectBasicConfig> projectBasicConfig = Optional.of(new ProjectBasicConfig());

		// Mocking findByToolNameAndBasicProjectConfigId() to return a list of ProjectToolConfig
		List<ProjectToolConfig> projectToolConfigs = Collections.singletonList(new ProjectToolConfig());
		//when(toolRepository.findByToolNameAndBasicProjectConfigId(any(), any())).thenReturn(projectToolConfigs);

		// Calling the method with ProcessorExecutionBasicConfig
		ProcessorExecutionBasicConfig processorExecutionBasicConfig = new ProcessorExecutionBasicConfig();
		processorExecutionBasicConfig.setProjectBasicConfigIds(Collections.singletonList("507f1f77bcf86cd799439011"));
		ResponseEntity<String> response = jobController.startProjectWiseIssueJob(processorExecutionBasicConfig);

		// Verify that the response is as expected
		assertEquals("Job started for BasicProjectConfigId: 507f1f77bcf86cd799439011", response.getBody());
		assertEquals(200, response.getStatusCode().value());

	}

	@Test
	public void testStartProjectWiseIssueJob_SuccessfulExecution() throws Exception {
		// Mocking ongoingExecutionsService.isExecutionInProgress() to return false
		when(ongoingExecutionsService.isExecutionInProgress(anyString())).thenReturn(false);

		// Mocking findById() to return an Optional<ProjectBasicConfig>
		Optional<ProjectBasicConfig> projectBasicConfig = Optional.of(new ProjectBasicConfig());
		when(projectConfigRepository.findById(any())).thenReturn(projectBasicConfig);

		// Mocking findByToolNameAndBasicProjectConfigId() to return a list of ProjectToolConfig
		List<ProjectToolConfig> projectToolConfigs = Collections.singletonList(new ProjectToolConfig());
		when(toolRepository.findByToolNameAndBasicProjectConfigId(any(), any())).thenReturn(projectToolConfigs);

		// Calling the method with ProcessorExecutionBasicConfig
		ProcessorExecutionBasicConfig processorExecutionBasicConfig = new ProcessorExecutionBasicConfig();
		processorExecutionBasicConfig.setProjectBasicConfigIds(Collections.singletonList("507f1f77bcf86cd799439011"));
		ResponseEntity<String> response = jobController.startProjectWiseIssueJob(processorExecutionBasicConfig);
	}

	@Test
	public void testMetaData() throws Exception {
		// Mocking ongoingExecutionsService.isExecutionInProgress() to return false

	// Calling the method with ProcessorExecutionBasicConfig
		ProcessorExecutionBasicConfig processorExecutionBasicConfig = new ProcessorExecutionBasicConfig();
		processorExecutionBasicConfig.setProjectBasicConfigIds(Collections.singletonList("507f1f77bcf86cd799439011"));
		ResponseEntity<String> response = jobController.runMetadataStep("507f1f77bcf86cd799439011");
	}

	@Test
	public void testMetaDataException() throws Exception {
		// Mocking ongoingExecutionsService.isExecutionInProgress() to return false

		// Mocking findById() to return an Optional<ProjectBasicConfig>


		// Mocking findByToolNameAndBasicProjectConfigId() to return a list of ProjectToolConfig

		// Calling the method with ProcessorExecutionBasicConfig
		ProcessorExecutionBasicConfig processorExecutionBasicConfig = new ProcessorExecutionBasicConfig();
		processorExecutionBasicConfig.setProjectBasicConfigIds(Collections.singletonList("507f1f77bcf86cd799439011"));
		ResponseEntity<String> response = jobController.runMetadataStep("507f1f77bcf86cd799439011");
	}

	@Test
	public void testStartProjectWiseIssueJobKanban_SuccessfulExecution() throws Exception {
		// Mocking ongoingExecutionsService.isExecutionInProgress() to return false
		when(ongoingExecutionsService.isExecutionInProgress(anyString())).thenReturn(false);

		// Mocking findById() to return an Optional<ProjectBasicConfig>
		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setIsKanban(true);
		when(projectConfigRepository.findById(any())).thenReturn(Optional.of(projectBasicConfig));

		// Mocking findByToolNameAndBasicProjectConfigId() to return a list of ProjectToolConfig
		List<ProjectToolConfig> projectToolConfigs = Collections.singletonList(new ProjectToolConfig());
		when(toolRepository.findByToolNameAndBasicProjectConfigId(any(), any())).thenReturn(projectToolConfigs);

		// Calling the method with ProcessorExecutionBasicConfig
		ProcessorExecutionBasicConfig processorExecutionBasicConfig = new ProcessorExecutionBasicConfig();
		processorExecutionBasicConfig.setProjectBasicConfigIds(Collections.singletonList("507f1f77bcf86cd799439011"));
		ResponseEntity<String> response = jobController.startProjectWiseIssueJob(processorExecutionBasicConfig);
	}

}
