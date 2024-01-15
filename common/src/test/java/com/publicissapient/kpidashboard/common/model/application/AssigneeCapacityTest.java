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

import com.publicissapient.kpidashboard.common.constant.Role;
import org.junit.Assert;
import org.junit.Test;

public class AssigneeCapacityTest {

	AssigneeCapacity assigneeCapacity = new AssigneeCapacity("userId", "userName", Role.BACKEND_DEVELOPER,
			Double.valueOf(0), Double.valueOf(0), Double.valueOf(0), Integer.valueOf(0));

	@Test
	public void testEquals() throws Exception {
		boolean result = assigneeCapacity.equals(new AssigneeCapacity());
		Assert.assertEquals(false, result);
	}

	@Test
	public void testSetUserId() throws Exception {
		assigneeCapacity.setUserId("userId");
	}

	@Test
	public void testSetUserName() throws Exception {
		assigneeCapacity.setUserName("userName");
	}

	@Test
	public void testSetRole() throws Exception {
		assigneeCapacity.setRole(Role.BACKEND_DEVELOPER);
	}

	@Test
	public void testSetPlannedCapacity() throws Exception {
		assigneeCapacity.setPlannedCapacity(Double.valueOf(0));
	}

	@Test
	public void testSetLeaves() throws Exception {
		assigneeCapacity.setLeaves(Double.valueOf(0));
	}

	@Test
	public void testSetAvailableCapacity() throws Exception {
		assigneeCapacity.setAvailableCapacity(Double.valueOf(0));
	}

	@Test
	public void testSetHappinessRating() throws Exception {
		assigneeCapacity.setHappinessRating(Integer.valueOf(0));
	}


	@Test
	public void testBuilder() throws Exception {
		AssigneeCapacity.AssigneeCapacityBuilder result = AssigneeCapacity.builder();
		Assert.assertNotNull( result);
	}
}
