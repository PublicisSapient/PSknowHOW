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

import java.util.Arrays;

/**
 * The Enum NormalizedJira.
 *
 * @author tauakram
 */

public enum NormalizedJira {

	// @formatter:off

	DEFECT_TYPE("Bug"), TEST_TYPE("Test"), YES_VALUE("Yes"), NO_VALUE("No"), THIRD_PARTY_DEFECT_VALUE("UAT"), TECHSTORY(
			"TechStory"), INVALID("Invalid"), TO_BE_AUTOMATED("To be automated"), QA_DEFECT_VALUE("QA"),STATUS("Closed"),ISSUE_TYPE("Epic") ;

	private String normalizedValue;

	/**
	 * Instantiates a new normalized jira.
	 *
	 * @param normalizedValue
	 *            the normalized value
	 */
	NormalizedJira(String normalizedValue) {
		this.setNormalizedValue(normalizedValue);
	}

	/**
	 * Gets the normalized jira value.
	 *
	 * @param valueType
	 *            the value type
	 * @return the normalized jira value
	 */
	public static NormalizedJira getNormalizedJiraValue(String valueType) {

		return Arrays.asList(NormalizedJira.values()).stream().filter(t -> t.getValue().equalsIgnoreCase(valueType))
				.findAny().orElse(INVALID);
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return normalizedValue;
	}

	/**
	 * Sets the normalized value.
	 *
	 * @param normalizedValue
	 *            the new normalized value
	 */
	private void setNormalizedValue(String normalizedValue) {
		this.normalizedValue = normalizedValue;
	}

}
