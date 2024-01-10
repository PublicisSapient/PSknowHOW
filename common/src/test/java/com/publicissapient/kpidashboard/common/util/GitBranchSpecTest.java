package com.publicissapient.kpidashboard.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GitBranchSpecTest {

	@Test
	public void testConstructor() {
		GitBranchSpec branchSpec = new GitBranchSpec("feature/**");
		assertEquals("feature/**", branchSpec.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNullName() {
		new GitBranchSpec(null);
	}

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
