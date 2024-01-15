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

import com.publicissapient.kpidashboard.common.constant.BuildStatus;

public class BuildTest {
	// Field processorId of type ObjectId - was not mocked since Mockito doesn't
	// mock a Final class when 'mock-maker-inline' option is not set
	// Field basicProjectConfigId of type ObjectId - was not mocked since Mockito
	// doesn't mock a Final class when 'mock-maker-inline' option is not set
	// Field projectToolConfigId of type ObjectId - was not mocked since Mockito
	// doesn't mock a Final class when 'mock-maker-inline' option is not set
	// Field buildStatus of type BuildStatus - was not mocked since Mockito doesn't
	// mock enums
	// Field id of type ObjectId - was not mocked since Mockito doesn't mock a Final
	// class when 'mock-maker-inline' option is not set
	Build build = new Build(null, null, null, 0L, "buildJob", "jobFolder", "number", "buildUrl", 0L, 0L, 0L,
			BuildStatus.SUCCESS, "startedBy", "log", "updatedTime");

	@Test
	public void testEquals() throws Exception {
		boolean result = build.equals(new Build());
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = build.canEqual(new Build());
		Assert.assertEquals(true, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = build.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetProcessorId() throws Exception {
		build.setProcessorId(null);
	}

	@Test
	public void testSetBasicProjectConfigId() throws Exception {
		build.setBasicProjectConfigId(null);
	}

	@Test
	public void testSetProjectToolConfigId() throws Exception {
		build.setProjectToolConfigId(null);
	}

	@Test
	public void testSetTimestamp() throws Exception {
		build.setTimestamp(0L);
	}

	@Test
	public void testSetBuildJob() throws Exception {
		build.setBuildJob("buildJob");
	}

	@Test
	public void testSetJobFolder() throws Exception {
		build.setJobFolder("jobFolder");
	}

	@Test
	public void testSetNumber() throws Exception {
		build.setNumber("number");
	}

	@Test
	public void testSetBuildUrl() throws Exception {
		build.setBuildUrl("buildUrl");
	}

	@Test
	public void testSetStartTime() throws Exception {
		build.setStartTime(0L);
	}

	@Test
	public void testSetEndTime() throws Exception {
		build.setEndTime(0L);
	}

	@Test
	public void testSetDuration() throws Exception {
		build.setDuration(0L);
	}

	@Test
	public void testSetBuildStatus() throws Exception {
		build.setBuildStatus(BuildStatus.SUCCESS);
	}

	@Test
	public void testSetStartedBy() throws Exception {
		build.setStartedBy("startedBy");
	}

	@Test
	public void testSetLog() throws Exception {
		build.setLog("log");
	}

	@Test
	public void testSetUpdatedTime() throws Exception {
		build.setUpdatedTime("updatedTime");
	}

	@Test
	public void testSetId() throws Exception {
		build.setId(null);
	}
}

