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

public class AssigneeDetailsDTOTest {
	AssigneeDetailsDTO assigneeDetailsDTO = new AssigneeDetailsDTO("name", "displayName");

	@Test
	public void testEquals() throws Exception {
		boolean result = assigneeDetailsDTO.equals(new AssigneeDetailsDTO());
		Assert.assertEquals(false, result);
	}

	@Test
	public void testSetName() throws Exception {
		assigneeDetailsDTO.setName("name");
	}

	@Test
	public void testSetDisplayName() throws Exception {
		assigneeDetailsDTO.setDisplayName("displayName");
	}

	@Test
	public void testToString() throws Exception {
		String result = assigneeDetailsDTO.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testBuilder() throws Exception {
		AssigneeDetailsDTO.AssigneeDetailsDTOBuilder result = AssigneeDetailsDTO.builder();
		Assert.assertNotNull(result);
	}
}

