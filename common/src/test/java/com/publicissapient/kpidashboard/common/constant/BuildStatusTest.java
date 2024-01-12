package com.publicissapient.kpidashboard.common.constant;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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

	@Test(expected = IllegalArgumentException.class)
	public void testFromStringWithInvalidValue() {
		BuildStatus.fromString("INVALID_STATUS");
	}
}
