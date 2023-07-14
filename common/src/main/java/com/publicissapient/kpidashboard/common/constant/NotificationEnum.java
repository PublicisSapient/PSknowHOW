package com.publicissapient.kpidashboard.common.constant;

public enum NotificationEnum {

	PROJECT_ACCESS("Project Access Request"), USER_APPROVAL("User Access Request");

	private String value;

	NotificationEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
