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

	KPI0("Processor",KPISource.JIRA.name(),
			Arrays.asList("jiradefecttype", "jiraIssueTypeNames", "jiraIterationCompletionTypeCustomField",
					"jiraIterationCompletionStatusCustomField", "estimationCriteria", "jiraStoryPointsCustomField",
					"jiraDod", "jiraBugRaisedByQACustomField", "jiraBugRaisedByQAIdentification",
					"jiraBugRaisedByQAValue", "jiraBugRaisedByCustomField", "jiraBugRaisedByValue",
					"jiraBugRaisedByIdentification", "jiraLiveStatus", "epicCostOfDelay", "epicRiskReduction",
					"epicUserBusinessValue", "epicWsjf", "epicTimeCriticality", "epicJobSize","additionalFilterConfig")),

	KPI40("Issue Count", KPISource.JIRA.name(), Arrays.asList("jiraStoryIdentificationIC", "jiradefecttypeIC",
			"jiraIterationCompletionStatusIC")),

	KPI39("Sprint Velocity", KPISource.JIRA.name(), Arrays.asList("jiraSprintVelocityIssueTypeSV",
			"jiraIterationCompletionStatusSV", "jiraIssueDeliverdStatusSV")),

	KPI5("Sprint Predictability", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionTypeSP", "jiraIterationCompletionStatusSP")),

	KPI46("Sprint Capacity Utilization", KPISource.JIRA.name(), Arrays.asList("jiraSprintCapacityIssueType")),

	KPI83("Average Resolution Time", KPISource.JIRA.name(),
			Arrays.asList("resolutionTypeForRejectionAVR", "jiraIssueDeliverdStatusAVR", "jiraDefectRejectionStatusAVR",
					"jiradefecttypeAVR", "jiraStatusForDevelopmentAVR", "jiraIssueTypeNamesAVR")),

	KPI82("First Time Pass Rate", KPISource.JIRA.name(),
			Arrays.asList("jiraStatusForDevelopmentFTPR", "jiraFTPRStoryIdentification", "jiraIssueDeliverdStatusFTPR",
					"resolutionTypeForRejectionFTPR", "jiraDefectRejectionStatusFTPR", "defectPriorityFTPR",
					"excludeRCAFromFTPR", "jiradefecttypeFTPR")),

	KPI135("First Time Pass Rate (Iteration)", KPISource.JIRA.name(),
			Arrays.asList("jiraStatusForDevelopmentKPI135", "jiraKPI135StoryIdentification", "jiradefecttypeKPI135",
					"jiraIterationCompletionStatusKPI135")),

	KPI53("Lead Time (Kanban)", KPISource.JIRA.name(),
			Arrays.asList("jiraIntakeToDorIssueTypeLT", "jiraDorLT", "jiraLiveStatusLTK")),

	KPI3("Lead Time (Scrum)", KPISource.JIRA.name(), Arrays.asList("jiradefecttypeLT", "jiraIntakeToDorIssueTypeLT",
			"jiraDorLT", "jiraDodLT", "jiraLiveStatusLT")),

	KPI34("Defect Removal Efficiency", KPISource.JIRA.name(), Arrays.asList("jiraDefectRemovalStatus",
			"resolutionTypeForRejectionDRE", "jiraDefectRejectionStatusDRE", "jiraDefectRemovalIssueType")),

	KPI37("Defect Rejection Rate", KPISource.JIRA.name(), Arrays.asList("resolutionTypeForRejectionDRR",
			"jiraDefectRejectionStatusDRR", "jiraDefectRejectionlIssueType")),

	KPI28("Defect Count By Priority(Scrum)", KPISource.JIRA.name(), Arrays.asList("jiraDefectCountlIssueTypeDC",
			"resolutionTypeForRejectionDC", "jiraDefectRejectionStatusDC")),

	KPI140("Defect Count by Priority (Iteration)", KPISource.JIRA.name(), Arrays.asList("jiradefecttypeKPI140",
			"jiraIterationCompletionStatusKPI140")),

	KPI144("Defect Count by Priority (Release)", KPISource.JIRA.name(), Arrays.asList("jiradefecttypeRDCP")),

	KPI143("Defect Count by Assignee (Release)", KPISource.JIRA.name(), Arrays.asList("jiradefecttypeRDCA")),

	KPI142("Defect Count by RCA (Release)", KPISource.JIRA.name(), Arrays.asList("jiradefecttypeRDCR")),

	KPI141("Defect Count by Status (Release)", KPISource.JIRA.name(), Arrays.asList("jiradefecttypeRDCS")),

	KPI36("Defect Count by RCA (Scrum)", KPISource.JIRA.name(), Arrays.asList("jiraDefectCountlIssueTypeRCA",
			"resolutionTypeForRejectionRCA", "jiraDefectRejectionStatusRCA")),

	KPI132("Defect Count by RCA (Iteration)", KPISource.JIRA.name(), Arrays.asList("jiradefecttypeKPI132",
			"jiraIterationCompletionStatusKPI132")),

	KPI136("Defect Count by Status (Iteration)", KPISource.JIRA.name(), Arrays.asList("jiradefecttypeKPI136",
			"jiraIterationCompletionStatusKPI136")),

	KPI14("Defect Injection Rate", KPISource.JIRA.name(),
			Arrays.asList("jiradefecttype", "excludeRCAFromDIR", "resolutionTypeForRejectionDIR",
					"jiraDefectRejectionStatusDIR", "defectPriorityDIR", "jiraDefectInjectionIssueType",
					"jiraDefectCreatedStatus", "jiraDodDIR")),

	KPI111("Defect Density", KPISource.JIRA.name(),
			Arrays.asList("jiraDodQADD", "jiraQADefectDensityIssueType", "defectPriorityQADD", "excludeRCAFromQADD",
					"resolutionTypeForRejectionQADD", "jiraDefectRejectionStatusQADD")),

	KPI127("Production Defects Ageing", KPISource.JIRA.name(), Arrays.asList("jiraDodPDA", "jiraLiveStatusPDA")),

	KPI35("Defect Seepage Rate", KPISource.JIRA.name(), Arrays.asList("jiraDefectSeepageIssueType",
			"resolutionTypeForRejectionDSR", "jiraDefectRejectionStatusDSR")),

	KPI133("Quality Status", KPISource.JIRA.name(),
			Arrays.asList("resolutionTypeForRejectionKPI133", "jiraDefectRejectionStatusKPI133", "jiradefecttypeKPI133",
					"defectPriorityKPI133", "excludeRCAFromKPI133", "jiraIterationCompletionStatusKPI133")),

	KPI126("Created vs Resolved defects", KPISource.JIRA.name(),
			Arrays.asList("jiradefecttypeCVR", "jiraIssueDeliverdStatusCVR")),

	KPI72("Commitment Reliability", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusCR", "jiraIterationCompletionTypeCR")),

	KPI122("Closure Possible Today", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKPI122", "jiraIterationCompletionTypeKPI122","jiraStatusForInProgressKPI122")),

	KPI145("Dev Completion Status", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKPI145", "jiraIterationCompletionTypeKPI145",
					"jiraStatusForInProgressDCS","jiraDevDoneStatusKPI145")),

	KPI75("Estimate vs Actual", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKPI75", "jiraIterationCompletionTypeKPI75")),

	KPI124("Estimation Hygiene", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKPI124", "jiraIterationCompletionTypeKPI124","issueStatusExcluMissingWorkKPI124")),

	KPI123("Issue Likely To Spill", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKPI123", "jiraIterationCompletionTypeKPI123","jiraStatusForInProgressKPI123")),

	KPI125("Iteration Burnup", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKPI125", "jiraIterationCompletionTypeKPI125","jiraStatusForInProgressKPI125")),

	KPI120("Iteration Commitment", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKPI120", "jiraIterationCompletionTypeKPI120")),

	KPI128("Planned Work Status", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKPI128", "jiraIterationCompletionTypeKPI128","jiraDevDoneStatusKPI128","jiraStatusForInProgressKPI128")),

	KPI134("Unplanned Work Status", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKPI134", "jiraIterationCompletionTypeKPI134")),

	KPI119("Work Remaining", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKPI119", "jiraIterationCompletionTypeKPI119",
					"jiraDevDoneStatusKPI119","jiraStatusForInProgressKPI119")),

	KPI131("Wastage", KPISource.JIRA.name(),
			Arrays.asList("jiraIncludeBlockedStatusKPI131","jiraIterationCompletionStatusKPI131", "jiraIterationCompletionTypeKPI131",
					"jiraWaitStatusKPI131")),

	KPI138("Backlog Readiness Efficiency", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusBRE", "jiraIterationCompletionTypeBRE")),

	KPI137("Defect Reopen Rate (Backlog)", KPISource.JIRA.name(), Arrays.asList("jiradefecttypeBDRR")),

	KPI997("Open Ticket Ageing By Priority (Kanban)", KPISource.JIRA.name(), Arrays.asList("jiraLiveStatusOTA")),

	KPI51("Net Open Ticket Count By RCA (Kanban)", KPISource.JIRA.name(), Arrays.asList("jiraLiveStatusNORK")),

	KPI48("Net Open Ticket Count By Status (Kanban)", KPISource.JIRA.name(), Arrays.asList("jiraLiveStatusNOSK")),

	KPI50("Net Open Ticket Count by Priority (Kanban)", KPISource.JIRA.name(), Arrays.asList("jiraLiveStatusNOPK")),

	KPI129("Issues Without Story Link", KPISource.JIRA.name(), Arrays.asList("jiradefecttypeIWS"));

	// @formatter:on

	private List<String> fields;
	private String kpiName;
	private String kpiSource;

	FieldMappingEnum(String kpiName, String kpiSource, List<String> fields) {
		this.kpiName = kpiName;
		this.fields = fields;
		this.kpiSource = kpiSource;

	}

	public String getKpiSource() {
		return kpiSource;
	}

	public void setKpiSource(String kpiSource) {
		this.kpiSource = kpiSource;
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