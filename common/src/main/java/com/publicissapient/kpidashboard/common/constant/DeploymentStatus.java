package com.publicissapient.kpidashboard.common.constant;

/**
 * Enumeration of valid deployment statuses.
 */
public enum DeploymentStatus {

	SUCCESS, FAILURE, UNSTABLE, ABORTED, IN_PROGRESS, UNKNOWN, INACTIVE;

	public static DeploymentStatus fromString(String value) {
		for (DeploymentStatus deploymentStatus : values()) {
			if (deploymentStatus.toString().equalsIgnoreCase(value)) {
				return deploymentStatus;
			}
		}
		throw new IllegalArgumentException(value + " is not a valid DeploymentStatus.");
	}
}
