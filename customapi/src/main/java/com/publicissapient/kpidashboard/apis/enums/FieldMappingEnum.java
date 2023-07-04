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
			"jiraIterationCompletionTypeIC", "jiraIterationCompletionStatusIC")),

	KPI39("Sprint Velocity", KPISource.JIRA.name(), Arrays.asList("jiraSprintVelocityIssueTypeSV",
			"jiraIterationCompletionStatusSV", "jiraIssueDeliverdStatusSV", "jiraIterationCompletionTypeSV")),

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
			Arrays.asList("jiraStatusForDevelopmentIFTPR", "jiraIFTPRStoryIdentification", "jiradefecttypeIFTPR",
					"jiraIterationCompletionTypeIFTPR", "jiraIterationCompletionStatusIFTPR")),

	KPI53("Lead Time (Kanban)", KPISource.JIRA.name(),
			Arrays.asList("jiraIntakeToDorIssueTypeLT", "jiraDor", "jiraLiveStatusLTK")),

	KPI3("Lead Time (Scrum)", KPISource.JIRA.name(), Arrays.asList("jiradefecttypeLT", "jiraIntakeToDorIssueTypeLT",
			"jiraDor", "jiraDodLT", "jiraLiveStatusLT")),

	KPI34("Defect Removal Efficiency", KPISource.JIRA.name(), Arrays.asList("jiraDefectRemovalStatus",
			"resolutionTypeForRejectionDRE", "jiraDefectRejectionStatusDRE", "jiraDefectRemovalIssueType")),

	KPI37("Defect Rejection Rate", KPISource.JIRA.name(), Arrays.asList("resolutionTypeForRejectionDRR",
			"jiraDefectRejectionStatusDRR", "jiraDefectRejectionlIssueType")),

	KPI28("Defect Count By Priority(Scrum)", KPISource.JIRA.name(), Arrays.asList("jiraDefectCountlIssueTypeDC",
			"resolutionTypeForRejectionDC", "jiraDefectRejectionStatusDC")),

	KPI140("Defect Count by Priority (Iteration)", KPISource.JIRA.name(), Arrays.asList("jiradefecttypeIDCP",
			"jiraIterationCompletionStatusIDCP", "jiraIterationCompletionTypeIDCP")),

	KPI144("Defect Count by Priority (Release)", KPISource.JIRA.name(), Arrays.asList("jiradefecttypeRDCP")),

	KPI143("Defect Count by Assignee (Release)", KPISource.JIRA.name(), Arrays.asList("jiradefecttypeRDCA")),

	KPI142("Defect Count by RCA (Release)", KPISource.JIRA.name(), Arrays.asList("jiradefecttypeRDCR")),

	KPI141("Defect Count by Status (Release)", KPISource.JIRA.name(), Arrays.asList("jiradefecttypeRDCS")),

	KPI36("Defect Count by RCA (Scrum)", KPISource.JIRA.name(), Arrays.asList("jiraDefectCountlIssueTypeRCA",
			"resolutionTypeForRejectionRCA", "jiraDefectRejectionStatusRCA")),

	KPI132("Defect Count by RCA (Iteration)", KPISource.JIRA.name(), Arrays.asList("jiradefecttypeIDCR",
			"jiraIterationCompletionStatusIDCR", "jiraIterationCompletionTypeIDCR")),

	KPI136("Defect Count by Status (Iteration)", KPISource.JIRA.name(), Arrays.asList("jiradefecttypeIDCS",
			"jiraIterationCompletionStatusIDCS", "jiraIterationCompletionTypeIDCS")),

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
			Arrays.asList("resolutionTypeForRejectionQS", "jiraDefectRejectionStatusQS", "jiradefecttypeQS",
					"defectPriorityQS", "excludeRCAFromQS", "jiraIterationCompletionStatusQS",
					"jiraIterationCompletionStatusQS")),

	KPI126("Created vs Resolved defects", KPISource.JIRA.name(),
			Arrays.asList("jiradefecttypeCVR", "jiraIssueDeliverdStatusCVR")),

	KPI72("Commitment Reliability", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusCR", "jiraIterationCompletionTypeCR")),

	KPI122("Closure Possible Today", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusCPT", "jiraIterationCompletionTypeCPT","jiraStatusForInProgressCPT")),

	KPI145("Dev Completion Status", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusDCS", "jiraIterationCompletionTypeDCS","jiraStatusForInProgressDCS","jiraDevDoneStatusDCS")),

	KPI75("Estimate vs Actual", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusEVA", "jiraIterationCompletionTypeEVA")),

	KPI124("Estimation Hygiene", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusEH", "jiraIterationCompletionTypeEH","issueStatusExcluMissingWorkEH")),

	KPI123("Issue Likely To Spill", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusILS", "jiraIterationCompletionTypeILS","jiraStatusForInProgressILS")),

	KPI125("Iteration Burnup", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusIBU", "jiraIterationCompletionTypeIBU","jiraStatusForInProgressIBU")),

	KPI120("Iteration Commitment", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusICO", "jiraIterationCompletionTypeICO")),

	KPI128("Planned Work Status", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusPWS", "jiraIterationCompletionTypePWS","jiraDevDoneStatusPWS","jiraStatusForInProgressPWS")),

	KPI134("Unplanned Work Status", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusUPWS", "jiraIterationCompletionTypeUPWS")),

	KPI119("Work Remaining", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusWR", "jiraIterationCompletionTypeWR","jiraDevDoneStatusWR","jiraStatusForInProgressWR")),

	KPI131("Wastage", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusIW", "jiraIterationCompletionTypeIW")),

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