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

package com.publicissapient.kpidashboard.apis.sprinttracelog.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.publicissapient.kpidashboard.common.model.application.SprintTraceLog;
import com.publicissapient.kpidashboard.common.repository.application.SprintTraceLogRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;

@RunWith(MockitoJUnitRunner.class)
public class SprintTraceLogServiceImplTest {
	@InjectMocks
	private SprintTraceLogServiceImpl activeItrFetchService;

	@Mock
	private SprintTraceLogRepository sprintTraceLogRepository;

	private SprintTraceLog fetchRecord;
	private String validSprintId = "Sprint123";

	@Before
	public void setUp() {

		fetchRecord = new SprintTraceLog();
		fetchRecord.setSprintId(validSprintId);
	}

	@Test
    public void testGetActiveSprintFetchStatus_WhenValidSprintId_ReturnsSuccessResponse() {


        // Mock repository method call
        when(sprintTraceLogRepository.findFirstBySprintId(validSprintId))
                .thenReturn(fetchRecord);

        // Call the method under test
        ServiceResponse response = activeItrFetchService.getActiveSprintFetchStatus(validSprintId);

        // Assert the response
        assertTrue(response.getSuccess());
        assertEquals(response.getMessage(),"Sprint trace log");

    }

	@Test
	public void testGetActiveSprintFetchStatus_WhenInvalidSprintId_ReturnsErrorResponse() {
		// Test data
		String invalidSprintId = "";

		// Call the method under test
		ServiceResponse response = activeItrFetchService.getActiveSprintFetchStatus(invalidSprintId);

		// Assert the response
		assertFalse(response.getSuccess());
		assertEquals(response.getMessage(), "sprintId cannot be empty");
		assertNull(response.getData());

	}

	@Test
	public void testGetActiveSprintFetchStatus_WhenFetchRecordIsNull_ReturnsNoRecordFoundResponse() {
		// Test data
		String validSprintId = "Sprint456";

		// Mock repository method call (returning null)
		when(sprintTraceLogRepository.findFirstBySprintId(validSprintId)).thenReturn(null);

		// Call the method under test
		ServiceResponse response = activeItrFetchService.getActiveSprintFetchStatus(validSprintId);

		// Assert the response
		assertTrue(response.getSuccess());
		assertEquals(response.getMessage(), "No sync record found.");
		assertNull(response.getData());
	}
}