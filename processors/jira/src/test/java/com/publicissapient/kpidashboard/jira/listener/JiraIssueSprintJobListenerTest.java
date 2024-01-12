package com.publicissapient.kpidashboard.jira.listener;

import com.publicissapient.kpidashboard.common.model.application.SprintTraceLog;
import com.publicissapient.kpidashboard.common.repository.application.SprintTraceLogRepository;
import com.publicissapient.kpidashboard.jira.cache.JiraProcessorCacheEvictor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.AssertJUnit.*;

@RunWith(MockitoJUnitRunner.class)
public class JiraIssueSprintJobListenerTest {
	@Mock
	private SprintTraceLogRepository sprintTraceLogRepository;

	@Mock
	private JiraProcessorCacheEvictor processorCacheEvictor;

	@InjectMocks
	private JiraIssueSprintJobListener listener;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testAfterJob_SuccessfulJobExecution() {
		// Arrange
		JobExecution jobExecution = new JobExecution(1L);
		jobExecution.setStatus(BatchStatus.COMPLETED);
		String sprintId = "testSprintId";

		// Mocking the repository's findFirstBySprintId method
		SprintTraceLog fetchDetails = new SprintTraceLog();
		fetchDetails.setSprintId(sprintId);
        fetchDetails.setLastSyncDateTime(endTime);
        when(sprintTraceLogRepository.findFirstBySprintId(any())).thenReturn(fetchDetails);

		// Act
		listener.afterJob(jobExecution);

		// Assert
		// Verify that the status and fetch details are set correctly
		assertNotNull(fetchDetails.getLastSyncDateTime());
		assertFalse(fetchDetails.isErrorInFetch());
		assertTrue(fetchDetails.isFetchSuccessful());

		// Verify that the cache is cleared
		verify(processorCacheEvictor, times(1)).evictCache(anyString(), anyString());

		// Verify that the sprint trace log is saved
		verify(sprintTraceLogRepository, times(1)).save(fetchDetails);
	}

	@Test
	public void testAfterJob_FailedJobExecution() {
		// Arrange
		JobExecution jobExecution = new JobExecution(1L);
		jobExecution.setStatus(BatchStatus.FAILED);
		String sprintId = "testSprintId";

		// Mocking the repository's findFirstBySprintId method
		SprintTraceLog fetchDetails = new SprintTraceLog();
		fetchDetails.setSprintId(sprintId);
		when(sprintTraceLogRepository.findFirstBySprintId(null)).thenReturn(fetchDetails);

		// Act
		listener.afterJob(jobExecution);

		// Assert
		// Verify that the status and fetch details are set correctly
		assertTrue(fetchDetails.isErrorInFetch());
		assertFalse(fetchDetails.isFetchSuccessful());
	}

	private long endTime = System.currentTimeMillis();
}
