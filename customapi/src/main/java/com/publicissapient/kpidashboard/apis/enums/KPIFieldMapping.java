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
public enum KPIFieldMapping {

	KPI0(Arrays.asList("jiradefecttype", "jiraIssueTypeNames", "jiraIterationCompletionTypeCustomField",
			"jiraIterationCompletionStatusCustomField", "estimationCriteria", "storyPointToHourMapping",
			"jiraStoryPointsCustomField", "jiraStatusForDevelopment", "jiraDod")),

	kpi40(Arrays.asList("jiraStoryIdentificationIC", "jiraIterationCompletionTypeCustomField",
			"jiraIterationCompletionStatusCustomField", "jiradefecttype")),

	kpi39(Arrays.asList("jiraSprintVelocityIssueTypeSV", "jiraIterationCompletionTypeCustomField",
			"jiraIssueDeliverdStatusSV", "jiraIterationCompletionStatusCustomField", "estimationCriteria",
			"storyPointToHourMapping", "jiraStoryPointsCustomField")),

	kpi72(Arrays.asList("jiraIterationCompletionTypeCustomField", "jiraIterationCompletionStatusCustomField",
			"estimationCriteria", "storyPointToHourMapping", "jiraStoryPointsCustomField")),

	kpi5(Arrays.asList("jiraIterationCompletionTypeCustomField", "jiraIterationCompletionStatusCustomField",
			"estimationCriteria", "storyPointToHourMapping", "jiraStoryPointsCustomField")),

	kpi46(Arrays.asList("jiraSprintCapacityIssueType")),

	kpi83(Arrays.asList("resolutionTypeForRejectionAVR", "jiraIssueDeliverdStatusAVR", "jiraDefectRejectionStatusAVR",
			"jiraIssueTypeNames", "jiradefecttype", "jiraStatusForDevelopmentAVR")),

	kpi82(Arrays.asList("jiraStatusForDevelopmentFTPR", "jiraFTPRStoryIdentification")),

	kpi135(Arrays.asList("jiraStatusForDevelopmentIFTPR", "jiraIFTPRStoryIdentification")),

	kpi53(Arrays.asList("jiraIntakeToDorIssueTypeLT", "jiraDorLT", "Issue Description", "First Time Pass")), kpi3(
			Arrays.asList("jiraIntakeToDorIssueTypeLT", "jiraDorLT", "jiraDod", "jiraLiveStatus"));

	// @formatter:on

	private List<String> fields;

	KPIFieldMapping(List<String> fields) {
		this.fields = fields;

	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}

}