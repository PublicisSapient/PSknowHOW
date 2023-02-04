package com.publicissapient.kpidashboard.common.constant;

/**
 * Enumeration of valid deployment statuses.
 */
public enum DeploymentStatus {

	SUCCESS, FAILURE, UNSTABLE, ABORTED, IN_PROGRESS, UNKNOWN;

	public static DeploymentStatus fromString(String value) {
		for (DeploymentStatus deploymentStatus : values()) {
			if (deploymentStatus.toString().equalsIgnoreCase(value)) {
				return deploymentStatus;
			}
		}
		throw new IllegalArgumentException(value + " is not a valid DeploymentStatus.");
	}

	public static boolean contains(String value) {
		for (DeploymentStatus deploymentStatus : values()) {
			if (deploymentStatus.toString().equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}

	public static String getAllValues(){
		StringBuilder allStatus = new StringBuilder();
		for (DeploymentStatus deploymentStatus : values()) {
			allStatus.append(deploymentStatus+"/");
		}
		return allStatus.substring(0, allStatus.length()-1);
	}
}
