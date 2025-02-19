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

package com.publicissapient.kpidashboard.jira.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;

import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;

@RunWith(MockitoJUnitRunner.class)
public class JobStepProgressListenerTest {

	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;
	@InjectMocks
	private JobStepProgressListener jobStepProgressListener;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void beforeStep() {
		StepExecution stepExecution = new StepExecution("testStep", null);
		jobStepProgressListener.beforeStep(stepExecution);
		// Add assertions if needed
	}

	@Test
	public void afterStep() {
		StepExecution stepExecutionMock = mock(StepExecution.class);
		when(stepExecutionMock.getStatus()).thenReturn(BatchStatus.COMPLETED);
		ExitStatus exitStatus = jobStepProgressListener.afterStep(stepExecutionMock);
		assertNull(exitStatus);
	}

	@Test
	public void afterStep_withSchedulerFalse() {
		// Arrange
		StepExecution stepExecutionMock = mock(StepExecution.class);
		when(stepExecutionMock.getStatus()).thenReturn(BatchStatus.COMPLETED);
		when(stepExecutionMock.getStepName()).thenReturn("testStep");

		// Act
		ExitStatus exitStatus = jobStepProgressListener.afterStep(stepExecutionMock);

		// Assert
		assertNull(exitStatus);
	}
}
