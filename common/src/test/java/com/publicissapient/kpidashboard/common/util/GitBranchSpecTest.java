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

package com.publicissapient.kpidashboard.common.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class GitBranchSpecTest {

	@Test
	public void testConstructor() {
		GitBranchSpec branchSpec = new GitBranchSpec("feature/**");
		assertEquals("feature/**", branchSpec.toString());
	}

	/*
	 * @Test(expected = IllegalArgumentException.class) public void
	 * testConstructorWithNullName() { new GitBranchSpec(null); }
	 */

	@Test
	public void testConstructorWithEmptyName() {
		GitBranchSpec branchSpec = new GitBranchSpec("");
		assertEquals("**", branchSpec.toString());
	}

	@Test
	public void testToString() {
		GitBranchSpec branchSpec = new GitBranchSpec("bugfix/*");
		assertEquals("bugfix/*", branchSpec.toString());
	}

	@Test
	public void testMatches() {
		GitBranchSpec branchSpec = new GitBranchSpec("feature/*");
		assertTrue(branchSpec.matches("refs/heads/feature/new-feature"));
		assertFalse(branchSpec.matches("refs/heads/bugfix/fix-bug"));
	}

	@Test
	public void testMatchesWithDoubleStar() {
		GitBranchSpec branchSpec = new GitBranchSpec("feature/**");
		assertTrue(branchSpec.matches("refs/heads/feature/new-feature"));
		assertFalse(branchSpec.matches("refs/heads/bugfix/fix-bug"));
	}

	@Test
	public void testMatchesWithRegex() {
		GitBranchSpec branchSpec = new GitBranchSpec(":refs/heads/feature/\\d{4}-\\d{2}-\\d{2}");
		assertTrue(branchSpec.matches("refs/heads/feature/2022-01-01"));
		assertFalse(branchSpec.matches("refs/heads/feature/feature-branch"));
	}
}
