package com.publicissapient.kpidashboard.jira.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfigurationImpl;
import com.publicissapient.kpidashboard.jira.service.OngoingExecutionsService;

@RunWith(MockitoJUnitRunner.class)
public class JobSchedulerTest {
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
	private JobScheduler jobScheduler;

	@Mock
	private Job fetchIssueKanbanBoardJob;

	@Mock
	private FetchProjectConfigurationImpl fetchProjectConfiguration;
	@Mock
	private Job fetchIssueScrumJqlJob;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testStartScrumBoardJob_Success() throws JobExecutionException {
		// Mocking fetchBasicProjConfId to return a list of project IDs
		List<String> projectIds = new ArrayList<>();
		projectIds.add("projectId1");
		projectIds.add("projectId2");
		when(fetchProjectConfiguration.fetchBasicProjConfId(any(), anyBoolean(), anyBoolean())).thenReturn(projectIds);

		// Mocking jobLauncher.run() to return a JobExecution instance
		when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(new JobExecution(1L));

		// Calling the method
		jobScheduler.startScrumBoardJob();
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

		jobScheduler.startScrumBoardJob();

	}

	@Test
	public void testStartScrumJqlJob_Success() throws Exception {
		// Mocking fetchBasicProjConfId to return a list of project IDs
		List<String> projectIds = new ArrayList<>();
		projectIds.add("projectId1");
		when(fetchProjectConfiguration.fetchBasicProjConfId(any(), anyBoolean(), anyBoolean())).thenReturn(projectIds);

		// Mocking jobLauncher.run() to return a JobExecution instance
		when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(new JobExecution(1L));

		jobScheduler.startScrumJqlJob();
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

		jobScheduler.startScrumJqlJob();
	}

	@Test
	public void testStartKanbanJob_Success() throws Exception {
		// Mocking fetchBasicProjConfId to return a list of project IDs
		List<String> projectIds = new ArrayList<>();
		projectIds.add("projectId1");
		when(fetchProjectConfiguration.fetchBasicProjConfId(any(), anyBoolean(), anyBoolean())).thenReturn(projectIds);

		// Mocking jobLauncher.run() to return a JobExecution instance
		when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(new JobExecution(1L));

		jobScheduler.startKanbanJob();

	}

	@Test
	public void testStartKanbanJob_ExceptionHandling() throws Exception {
		// Mocking fetchBasicProjConfId to return a list of project IDs
		List<String> projectIds = new ArrayList<>();
		projectIds.add("projectId1");
		when(fetchProjectConfiguration.fetchBasicProjConfId(any(), anyBoolean(), anyBoolean())).thenReturn(projectIds);

		// Mocking jobLauncher.run() to throw an exception
		// doThrow(new RuntimeException("Simulated job execution
		// exception")).when(jobLauncher)
		// .run(eq(fetchIssueKanbanBoardJob), any(JobParameters.class));

		jobScheduler.startKanbanJob();
	}

	@Test
	public void testStartKanbanJqlJob_Success() throws Exception {
		// Mocking fetchBasicProjConfId to return a list of project IDs
		List<String> projectIds = new ArrayList<>();
		projectIds.add("projectId1");
		when(fetchProjectConfiguration.fetchBasicProjConfId(any(), anyBoolean(), anyBoolean())).thenReturn(projectIds);

		// Mocking jobLauncher.run() to return a JobExecution instance
		//when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(new JobExecution(1L));

		jobScheduler.startKanbanJqlJob();
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

		jobScheduler.startKanbanJqlJob();
	}
}