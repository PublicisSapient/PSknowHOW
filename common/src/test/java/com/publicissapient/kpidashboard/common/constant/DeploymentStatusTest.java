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

package com.publicissapient.kpidashboard.common.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DeploymentStatusTest {

	@Test
	public void testFromStringValidStatus() {
		assertEquals(DeploymentStatus.SUCCESS, DeploymentStatus.fromString("SUCCESS"));
		assertEquals(DeploymentStatus.FAILURE, DeploymentStatus.fromString("FAILURE"));
		assertEquals(DeploymentStatus.UNSTABLE, DeploymentStatus.fromString("UNSTABLE"));
		assertEquals(DeploymentStatus.ABORTED, DeploymentStatus.fromString("ABORTED"));
		assertEquals(DeploymentStatus.IN_PROGRESS, DeploymentStatus.fromString("IN_PROGRESS"));
		assertEquals(DeploymentStatus.UNKNOWN, DeploymentStatus.fromString("UNKNOWN"));
		assertEquals(DeploymentStatus.INACTIVE, DeploymentStatus.fromString("INACTIVE"));
	}

	/*
	 * @Test(expected = IllegalArgumentException.class) public void
	 * testFromStringInvalidStatus() {
	 * DeploymentStatus.fromString("INVALID_STATUS"); }
	 */
}
