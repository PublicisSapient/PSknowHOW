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

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.publicissapient.kpidashboard.common.constant.Role;

public class CapacityMasterTest {
	@Mock
	List<AssigneeCapacity> assigneeCapacity;
	@InjectMocks
	CapacityMaster capacityMaster;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testSetId() throws Exception {
		capacityMaster.setId(null);
	}

	@Test
	public void testSetProjectNodeId() throws Exception {
		capacityMaster.setProjectNodeId("projectNodeId");
	}

	@Test
	public void testSetProjectName() throws Exception {
		capacityMaster.setProjectName("projectName");
	}

	@Test
	public void testSetSprintNodeId() throws Exception {
		capacityMaster.setSprintNodeId("sprintNodeId");
	}

	@Test
	public void testSetSprintName() throws Exception {
		capacityMaster.setSprintName("sprintName");
	}

	@Test
	public void testSetSprintState() throws Exception {
		capacityMaster.setSprintState("sprintState");
	}

	@Test
	public void testSetCapacity() throws Exception {
		capacityMaster.setCapacity(Double.valueOf(0));
	}

	@Test
	public void testSetStartDate() throws Exception {
		capacityMaster.setStartDate("startDate");
	}

	@Test
	public void testSetEndDate() throws Exception {
		capacityMaster.setEndDate("endDate");
	}

	@Test
	public void testSetBasicProjectConfigId() throws Exception {
		capacityMaster.setBasicProjectConfigId(null);
	}

	@Test
	public void testSetAssigneeCapacity() throws Exception {
		capacityMaster.setAssigneeCapacity(Arrays.<AssigneeCapacity>asList(new AssigneeCapacity("userId", "userName",
				Role.BACKEND_DEVELOPER, Double.valueOf(0), Double.valueOf(0), Double.valueOf(0), Integer.valueOf(0))));
	}

	@Test
	public void testSetKanban() throws Exception {
		capacityMaster.setKanban(true);
	}

	@Test
	public void testSetAssigneeDetails() throws Exception {
		capacityMaster.setAssigneeDetails(true);
	}
}

