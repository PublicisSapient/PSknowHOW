/*
 *
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.publicissapient.kpidashboard.common.model.application;

import org.junit.Assert;
import org.junit.Test;

public class SprintTraceLogTest {
	// Field id of type ObjectId - was not mocked since Mockito doesn't mock a Final
	// class when 'mock-maker-inline' option is not set
	SprintTraceLog sprintTraceLog = new SprintTraceLog("sprintId", true, true, 0L);

	@Test
	public void testEquals() throws Exception {
		boolean result = sprintTraceLog.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = sprintTraceLog.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = sprintTraceLog.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetSprintId() throws Exception {
		sprintTraceLog.setSprintId("sprintId");
	}

	@Test
	public void testSetFetchSuccessful() throws Exception {
		sprintTraceLog.setFetchSuccessful(true);
	}

	@Test
	public void testSetErrorInFetch() throws Exception {
		sprintTraceLog.setErrorInFetch(true);
	}

	@Test
	public void testSetLastSyncDateTime() throws Exception {
		sprintTraceLog.setLastSyncDateTime(0L);
	}

	@Test
	public void testBuilder() throws Exception {
		SprintTraceLog.SprintTraceLogBuilder result = SprintTraceLog.builder();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetId() throws Exception {
		sprintTraceLog.setId(null);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme