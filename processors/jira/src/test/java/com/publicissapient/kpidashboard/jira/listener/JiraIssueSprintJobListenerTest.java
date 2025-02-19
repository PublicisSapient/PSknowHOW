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

package com.publicissapient.kpidashboard.jira.listener;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.application.SprintTraceLog;
import com.publicissapient.kpidashboard.common.repository.application.SprintTraceLogRepository;
import com.publicissapient.kpidashboard.jira.cache.JiraProcessorCacheEvictor;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.service.JiraClientService;

@RunWith(MockitoJUnitRunner.class)
public class JiraIssueSprintJobListenerTest {
	@Mock
	private SprintTraceLogRepository sprintTraceLogRepository;

	@Mock
	private JiraProcessorCacheEvictor processorCacheEvictor;

	@Mock
	private JiraClientService jiraClientService;

	@Mock
	private ProcessorJiraRestClient client;

	@Mock
	private KerberosClient kerberosClient;

	@InjectMocks
	private JiraIssueSprintJobListener listener;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		when(jiraClientService.isContainRestClient(null)).thenReturn(true);
		when(jiraClientService.getRestClientMap(null)).thenReturn(client);
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
		verify(processorCacheEvictor, times(3)).evictCache(anyString(), anyString());

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
