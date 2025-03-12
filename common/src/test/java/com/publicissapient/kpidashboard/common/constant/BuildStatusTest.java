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

public class BuildStatusTest {

	@Test
	public void testFromStringWithSuccess() {
		BuildStatus buildStatus = BuildStatus.fromString("SUCCESS");
		assertEquals(BuildStatus.SUCCESS, buildStatus);
	}

	@Test
	public void testFromStringWithFailure() {
		BuildStatus buildStatus = BuildStatus.fromString("FAILURE");
		assertEquals(BuildStatus.FAILURE, buildStatus);
	}

	@Test
	public void testFromStringWithUnstable() {
		BuildStatus buildStatus = BuildStatus.fromString("UNSTABLE");
		assertEquals(BuildStatus.UNSTABLE, buildStatus);
	}

	@Test
	public void testFromStringWithAborted() {
		BuildStatus buildStatus = BuildStatus.fromString("ABORTED");
		assertEquals(BuildStatus.ABORTED, buildStatus);
	}

	@Test
	public void testFromStringWithInProgress() {
		BuildStatus buildStatus = BuildStatus.fromString("IN_PROGRESS");
		assertEquals(BuildStatus.IN_PROGRESS, buildStatus);
	}

	@Test
	public void testFromStringWithUnknown() {
		BuildStatus buildStatus = BuildStatus.fromString("UNKNOWN");
		assertEquals(BuildStatus.UNKNOWN, buildStatus);
	}

	/*
	 * @Test(expected = IllegalArgumentException.class) public void
	 * testFromStringWithInvalidValue() { BuildStatus.fromString("INVALID_STATUS");
	 * }
	 */
}
