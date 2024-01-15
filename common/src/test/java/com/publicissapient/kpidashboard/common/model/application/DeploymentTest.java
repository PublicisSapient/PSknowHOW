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

import com.publicissapient.kpidashboard.common.constant.DeploymentStatus;
import org.junit.Assert;
import org.junit.Test;

public class DeploymentTest {

	Deployment deployment = new Deployment(null, null, null, "envId", "envName", "envUrl", "startTime", "endTime", 0L,
			DeploymentStatus.SUCCESS, "jobId", "jobName", "jobFolderName", "deployedBy", "number", "createdAt",
			"updatedTime");

	@Test
	public void testEquals() throws Exception {
		boolean result = deployment.equals("obj");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = deployment.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetProcessorId() throws Exception {
		deployment.setProcessorId(null);
	}

	@Test
	public void testSetBasicProjectConfigId() throws Exception {
		deployment.setBasicProjectConfigId(null);
	}

	@Test
	public void testSetProjectToolConfigId() throws Exception {
		deployment.setProjectToolConfigId(null);
	}

	@Test
	public void testSetEnvId() throws Exception {
		deployment.setEnvId("envId");
	}

	@Test
	public void testSetEnvName() throws Exception {
		deployment.setEnvName("envName");
	}

	@Test
	public void testSetEnvUrl() throws Exception {
		deployment.setEnvUrl("envUrl");
	}

	@Test
	public void testSetStartTime() throws Exception {
		deployment.setStartTime("startTime");
	}

	@Test
	public void testSetEndTime() throws Exception {
		deployment.setEndTime("endTime");
	}

	@Test
	public void testSetDuration() throws Exception {
		deployment.setDuration(0L);
	}

	@Test
	public void testSetDeploymentStatus() throws Exception {
		deployment.setDeploymentStatus(DeploymentStatus.SUCCESS);
	}

	@Test
	public void testSetJobId() throws Exception {
		deployment.setJobId("jobId");
	}

	@Test
	public void testSetJobName() throws Exception {
		deployment.setJobName("jobName");
	}

	@Test
	public void testSetJobFolderName() throws Exception {
		deployment.setJobFolderName("jobFolderName");
	}

	@Test
	public void testSetDeployedBy() throws Exception {
		deployment.setDeployedBy("deployedBy");
	}

	@Test
	public void testSetNumber() throws Exception {
		deployment.setNumber("number");
	}

	@Test
	public void testSetCreatedAt() throws Exception {
		deployment.setCreatedAt("createdAt");
	}

	@Test
	public void testSetUpdatedTime() throws Exception {
		deployment.setUpdatedTime("updatedTime");
	}

	@Test
	public void testSetId() throws Exception {
		deployment.setId(null);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme