package com.publicissapient.kpidashboard.common.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

	/*@Test(expected = IllegalArgumentException.class)
	public void testFromStringInvalidStatus() {
		DeploymentStatus.fromString("INVALID_STATUS");
	}*/
}
