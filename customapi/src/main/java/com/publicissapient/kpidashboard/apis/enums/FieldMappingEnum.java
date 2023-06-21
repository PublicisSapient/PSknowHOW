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
import java.util.List;

/**
 * to order the headings of excel columns
 */
@SuppressWarnings("java:S1192")
public enum FieldMappingEnum {

	KPI0(Arrays.asList("jiradefecttype", "jiraIssueTypeNames", "jiraIterationCompletionTypeCustomField",
			"jiraIterationCompletionStatusCustomField", "estimationCriteria", "storyPointToHourMapping",
			"jiraStoryPointsCustomField", "jiraStatusForDevelopment", "jiraDod")),

	KPI40(Arrays.asList("jiraStoryIdentificationIC", "jiraIterationCompletionTypeCustomField",
			"jiraIterationCompletionStatusCustomField", "jiradefecttype")),

	KPI39(Arrays.asList("jiraSprintVelocityIssueTypeSV", "jiraIterationCompletionTypeCustomField",
			"jiraIssueDeliverdStatusSV", "jiraIterationCompletionStatusCustomField", "estimationCriteria",
			"storyPointToHourMapping", "jiraStoryPointsCustomField")),

	KPI72(Arrays.asList("jiraIterationCompletionTypeCustomField", "jiraIterationCompletionStatusCustomField",
			"estimationCriteria", "storyPointToHourMapping", "jiraStoryPointsCustomField")),

	KPI5(Arrays.asList("jiraIterationCompletionTypeCustomField", "jiraIterationCompletionStatusCustomField",
			"estimationCriteria", "storyPointToHourMapping", "jiraStoryPointsCustomField")),

	KPI46(Arrays.asList("jiraSprintCapacityIssueType")),

	KPI83(Arrays.asList("resolutionTypeForRejectionAVR", "jiraIssueDeliverdStatusAVR", "jiraDefectRejectionStatusAVR",
			"jiraIssueTypeNames", "jiradefecttype", "jiraStatusForDevelopmentAVR")),

	KPI82(Arrays.asList("jiraStatusForDevelopmentFTPR", "jiraFTPRStoryIdentification")),

	KPI135(Arrays.asList("jiraStatusForDevelopmentIFTPR", "jiraIFTPRStoryIdentification")),

	KPI53(Arrays.asList("jiraIntakeToDorIssueTypeLT", "jiraDorLT", "Issue Description", "First Time Pass")),

	KPI3(Arrays.asList("jiraIntakeToDorIssueTypeLT", "jiraDorLT", "jiraDod", "jiraLiveStatus")),

	KPI34(Arrays.asList("jiraDefectRemovalStatusDRE", "resolutionTypeForRejectionDRE", "jiraDefectRejectionStatusDRE", "jiraDefectRemovalIssueTypeDRE")),

	KPI37(Arrays.asList("resolutionTypeForRejectionDRR", "jiraDefectRejectionStatusDRR", "jiraDefectRejectionlIssueTypeDRR")),

	KPI28(Arrays.asList("jiradefecttype", "estimationCriteria")),

	KPI140(Arrays.asList("jiradefecttype")),

	KPI144(Arrays.asList("jiradefecttype"));



	// @formatter:on

	private List<String> fields;

	FieldMappingEnum(List<String> fields) {
		this.fields = fields;

	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}

}