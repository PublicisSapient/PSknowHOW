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

package com.publicissapient.kpidashboard.apis.enums;

import java.util.Arrays;

/**
 * This enum is used to compare the key in thymeleaf email template. Make sure
 * the same key should be in HTML email format
 *
 * @author pkum34
 */
public enum NotificationCustomDataEnum {

	USER_NAME("User_Name"),

	USER_EMAIL("User_Email"),

	ACCESS_LEVEL("Access_Level"),

	ACCESS_ITEMS("Access_Items"),

	USER_PROJECTS("User_Projects"),

	USER_ROLES("User_Roles"),

	ACCOUNT_NAME("Account_Name"),

	TEAM_NAME("Team_Name"),

	YEAR("Year"),

	MONTH("Month"),

	UPLOADED_BY("Uploaded_By"),

	SERVER_HOST("Server_Host"),

	FEEDBACK_CONTENT("Feedback_Content"),

	FEEDBACK_CATEGORY("Feedback_Category"),

	FEEDBACK_TYPE("Feedback_Type"),

	INVALID("INVALID"),

	ADMIN_EMAIL("Admin_Email");

	private String value;

	NotificationCustomDataEnum(String value) {
		this.value = value;
	}

	public static NotificationCustomDataEnum getKPISource(String value) {
		return Arrays.asList(NotificationCustomDataEnum.values()).stream()
				.filter(kpi -> kpi.getValue().equalsIgnoreCase(value)).findAny().orElse(INVALID);
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
}
