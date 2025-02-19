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

public class CommitTypeTest {

	@Test
	public void testFromStringWithNotBuilt() {
		CommitType commitType = CommitType.fromString("NOT_BUILT");
		assertEquals(CommitType.NOT_BUILT, commitType);
	}

	@Test
	public void testFromStringWithMerge() {
		CommitType commitType = CommitType.fromString("MERGE");
		assertEquals(CommitType.MERGE, commitType);
	}

	@Test
	public void testFromStringWithNew() {
		CommitType commitType = CommitType.fromString("NEW");
		assertEquals(CommitType.NEW, commitType);
	}

	/*
	 * @Test(expected = IllegalArgumentException.class) public void
	 * testFromStringWithInvalidValue() {
	 * CommitType.fromString("INVALID_COMMIT_TYPE"); }
	 */
}
