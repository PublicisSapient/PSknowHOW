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
 * KpiFieldMapping
 */
@SuppressWarnings("java:S1192")
public enum FieldMappingEnum {

	KPI0("Processor",
			Arrays.asList("jiradefecttype", "jiraIssueTypeNames", "jiraIterationCompletionTypeCustomField",
					"jiraIterationCompletionStatusCustomField", "estimationCriteria", "storyPointToHourMapping",
					"jiraStoryPointsCustomField", "jiraStatusForDevelopment", "jiraDod", "jiraBugRaisedByQACustomField",
					"jiraBugRaisedByQAIdentification", "jiraBugRaisedByQAValue", "jiraBugRaisedByCustomField",
					"jiraBugRaisedByValue", "jiraBugRaisedByIdentification")),

	KPI40("Issue Count", Arrays.asList("jiraStoryIdentificationIC", "jiraIterationCompletionTypeCustomField",
			"jiraIterationCompletionStatusCustomField", "jiradefecttype")),

	KPI39("Sprint Velocity",
			Arrays.asList("jiraSprintVelocityIssueTypeSV", "jiraIterationCompletionTypeCustomField",
					"jiraIssueDeliverdStatusSV", "jiraIterationCompletionStatusCustomField", "estimationCriteria",
					"storyPointToHourMapping", "jiraStoryPointsCustomField")),

	KPI72("Commitment Reliability",
			Arrays.asList("jiraIterationCompletionTypeCustomField", "jiraIterationCompletionStatusCustomField",
					"estimationCriteria", "storyPointToHourMapping", "jiraStoryPointsCustomField")),

	KPI5("Sprint Predictability",
			Arrays.asList("jiraIterationCompletionTypeCustomField", "jiraIterationCompletionStatusCustomField",
					"estimationCriteria", "storyPointToHourMapping", "jiraStoryPointsCustomField")),

	KPI46("Sprint Capacity Utilization", Arrays.asList("jiraSprintCapacityIssueType")),

	KPI83("Average Resolution Time", Arrays.asList("resolutionTypeForRejectionAVR", "jiraIssueDeliverdStatusAVR",
			"jiraDefectRejectionStatusAVR", "jiraIssueTypeNames", "jiradefecttype", "jiraStatusForDevelopmentAVR")),

	KPI82("First Time Pass Rate",
			Arrays.asList("jiraStatusForDevelopmentFTPR", "jiraFTPRStoryIdentification", "jiraIssueDeliverdStatusFTPR",
					"resolutionTypeForRejectionFTPR", "jiraDefectRejectionStatusFTPR", "defectPriorityFTPR",
					"excludeRCAFromFTPR")),

	KPI135("First Time Pass Rate (Iteration)",
			Arrays.asList("jiraStatusForDevelopmentIFTPR", "jiraIFTPRStoryIdentification")),

	KPI53("Lead Time (Kanban)",
			Arrays.asList("jiraIntakeToDorIssueTypeLT", "jiraDorLT", "Issue Description", "First Time Pass")),

	KPI3("Lead Time (Scrum)", Arrays.asList("jiraIntakeToDorIssueTypeLT", "jiraDorLT", "jiraDodLT", "jiraLiveStatus")),

	KPI34("Defect Removal Efficiency", Arrays.asList("jiraDefectRemovalStatus", "resolutionTypeForRejectionDRE",
			"jiraDefectRejectionStatusDRE", "jiraDefectRemovalIssueType")),

	KPI37("Defect Rejection Rate", Arrays.asList("resolutionTypeForRejectionDRR", "jiraDefectRejectionStatusDRR",
			"jiraDefectRejectionlIssueType")),

	KPI28("Defect Count By Priority(Scrum)", Arrays.asList("jiradefecttype", "estimationCriteria")),

	KPI140("Defect Count by Priority (Iteration)", Arrays.asList("jiradefecttype")),

	KPI144("Defect Count by Priority (Release)", Arrays.asList("jiradefecttype")),

	KPI14("Defect Injection Rate",
			Arrays.asList("jiradefecttype", "estimationCriteria", "excludeRCAFromDIR", "resolutionTypeForRejectionDIR",
					"jiraDefectRejectionStatusDIR", "defectPriorityDIR", "jiraDefectInjectionIssueType",
					"jiraDefectCreatedStatus", "jiraDodDIR")),

	KPI111("Defect Density", Arrays.asList("jiraDodQADD", "jiraQADefectDensityIssueType", "defectPriorityQADD",
			"excludeRCAFromQADD", "resolutionTypeForRejectionQADD", "jiraDefectRejectionStatusQADD")),

	KPI127("Production Defects Ageing", Arrays.asList("jiraDodPDA")),

	KPI35("Defect Seepage", Arrays.asList("jiraDefectSeepageIssueType","resolutionTypeForRejectionDSR","jiraDefectRejectionStatusDSR")),

	KPI133("Quality Status", Arrays.asList("resolutionTypeForRejectionQS", "jiraDefectRejectionStatusQS",
			"defectPriorityQS", "excludeRCAFromQS"));

	// @formatter:on

	private List<String> fields;
	private String kpiName;

	FieldMappingEnum(String kpiName, List<String> fields) {
		this.kpiName = kpiName;
		this.fields = fields;

	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	public String getKpiName() {
		return kpiName;
	}

	public void setKpiName(String kpiName) {
		this.kpiName = kpiName;
	}

}