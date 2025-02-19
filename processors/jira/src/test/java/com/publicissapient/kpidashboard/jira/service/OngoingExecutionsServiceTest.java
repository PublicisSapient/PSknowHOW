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

package com.publicissapient.kpidashboard.jira.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;

@RunWith(MockitoJUnitRunner.class)
public class OngoingExecutionsServiceTest {

	@Mock
	ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@InjectMocks
	private OngoingExecutionsService ongoingExecutionsService;

	@Before
	public void setUp() {
		// Initialize the service before test
	}

	@Test
	public void testIsExecutionInProgress() {
		// Arrange
		String projectConfigId = "project123";

		// Act
		boolean isInProgress = ongoingExecutionsService.isExecutionInProgress(projectConfigId);

		// Assert
		assertFalse("No execution should be in progress initially", isInProgress);
	}

	@Test
	public void testMarkExecutionInProgress() {
		// Arrange
		String projectConfigId = "project123";

		// Act
		ongoingExecutionsService.markExecutionInProgress(projectConfigId);

		// Assert
		assertTrue("Execution should be marked as in progress",
				ongoingExecutionsService.isExecutionInProgress(projectConfigId));
	}

	@Test
	public void testMarkExecutionAsCompleted() {
		// Arrange
		String projectConfigId = "project123";
		ongoingExecutionsService.markExecutionInProgress(projectConfigId);

		// Act
		ongoingExecutionsService.markExecutionAsCompleted(projectConfigId);

		// Assert
		assertFalse("Execution should be marked as completed",
				ongoingExecutionsService.isExecutionInProgress(projectConfigId));
	}
}
