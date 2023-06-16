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

public enum JiraFeatureHistory {
	STORY_ID("storyID"), DEFECT_STORY_ID("defectStoryID"), CREATED_DATE("createdDate"), HISTORY_STATUS(
			"historyDetails.status"), STORY_TYPE("storyType"), PROJECT_ID(
					"projectId"), BASIC_PROJECT_CONFIG_ID("basicProjectConfigId"), INVALID("Invalid");

	private String fieldValueInFeature;

	JiraFeatureHistory(String fieldValueInFeature) {
		this.setFieldValueInFeature(fieldValueInFeature);
	}

	public static JiraFeatureHistory getJiraFeatureField(String valueType) {

		return Arrays.asList(JiraFeatureHistory.values()).stream()
				.filter(t -> t.getFieldValueInFeature().equalsIgnoreCase(valueType)).findAny().orElse(INVALID);
	}

	public String getFieldValueInFeature() {
		return fieldValueInFeature;
	}

	private void setFieldValueInFeature(String fieldValueInFeature) {
		this.fieldValueInFeature = fieldValueInFeature;
	}
}
