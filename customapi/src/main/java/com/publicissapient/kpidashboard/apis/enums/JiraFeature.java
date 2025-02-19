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

/** */
package com.publicissapient.kpidashboard.apis.enums;

import java.util.Arrays;

/**
 * Enum for Jira feature
 *
 * @author tauakram
 */
public enum JiraFeature {

	/** Issue type jira feature. */
	ISSUE_TYPE("typeName"),
	/** Original Issue type jira feature. */
	ORIGINAL_ISSUE_TYPE("originalType"),
	/** DEFECT type jira feature. */
	DEFECT_PRIORITY("priority"),
	/** Project id jira feature. */
	PROJECT_ID("projectID"),

	/** Status jira feature. */
	STATUS("status"),
	/** Test Case Status jira feature. */
	TEST_CASE_STATUS("testCaseStatus"),

	/** Sprint id jira feature. */
	SPRINT_ID("sprintID"),
	/** Issue number jira feature. */
	ISSUE_NUMBER("number"),
	/** Defect story id jira feature. */
	DEFECT_STORY_ID("defectStoryID"),
	/** Jira issue status jira feature. */
	JIRA_ISSUE_STATUS("jiraStatus"),
	/** Labels jira feature. */
	LABELS("labels"),
	/** Atm test folder jira feature. */
	ATM_TEST_FOLDER("testCaseFolderName"),
	/** Build name jira feature. */
	BUILD_NAME("buildNumber"),
	/** Release id jira feature. */
	RELEASE_ID("releaseId"),
	/** Dev name jira feature. */
	DEV_NAME("developerName"),
	/** Qa name jira feature. */
	QA_NAME("qaName"),
	/** Created date jira feature. */
	CREATED_DATE("createdDate"),
	/** Speedy issue type jira feature. */
	SPEEDY_ISSUE_TYPE("speedyIssueType"),
	/** Sprint begin date jira feature. */
	SPRINT_BEGIN_DATE("sprintBeginDate"),

	/** Invalid jira feature. */
	INVALID("Invalid"),
	/** Project name jira feature. */
	PROJECT_NAME("projectName"),
	/** Can test automated jira feature. */
	CAN_TEST_AUTOMATED("isTestCanBeAutomated"),
	/** Sprint status jira feature. */
	SPRINT_STATUS("sprintAssetState"),

	CHANGE_DATE("changeDate"),
	/** Jira production defect jira feature. */
	JIRA_PRODUCTION_DEFECT("productionDefect"),

	BASIC_PROJECT_CONFIG_ID("basicProjectConfigId"), ADDITIONAL_FILTERS_FILTERID(
			"additionalFilters.filterId"), ADDITIONAL_FILTERS_FILTERVALUES_VALUEID("additionalFilters.filterValues.valueId");

	private String fieldValueInFeature;

	JiraFeature(String fieldValueInFeature) {
		this.setFieldValueInFeature(fieldValueInFeature);
	}

	/**
	 * Gets jira feature field.
	 *
	 * @param valueType
	 *          the value type
	 * @return the jira feature field
	 */
	public static JiraFeature getJiraFeatureField(String valueType) {

		return Arrays.asList(JiraFeature.values()).stream()
				.filter(t -> t.getFieldValueInFeature().equalsIgnoreCase(valueType)).findAny().orElse(INVALID);
	}

	/**
	 * Gets field value in feature.
	 *
	 * @return the field value in feature
	 */
	public String getFieldValueInFeature() {
		return fieldValueInFeature;
	}

	/** Sets field value in feature. */
	private void setFieldValueInFeature(String fieldValueInFeature) {
		this.fieldValueInFeature = fieldValueInFeature;
	}
}
