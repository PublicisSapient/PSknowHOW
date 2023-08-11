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

	KPI0("Processor", KPISource.JIRA.name(),
			Arrays.asList("jiradefecttype", "jiraIssueTypeNames", "jiraIterationCompletionStatusCustomField",
					"rootCause", "sprintName", "estimationCriteria", "jiraStoryPointsCustomField",
					"jiraBugRaisedByQACustomField", "jiraBugRaisedByQAIdentification", "jiraBugRaisedByQAValue",
					"jiraBugRaisedByCustomField", "jiraBugRaisedByValue", "jiraBugRaisedByIdentification",
					"epicCostOfDelay", "epicRiskReduction", "epicUserBusinessValue", "epicWsjf", "epicTimeCriticality",
					"epicJobSize", "additionalFilterConfig", "jiraDueDateField", "jiraDueDateCustomField", "jiraDevDueDateCustomField",
					"jiraIssueEpicType", "storyFirstStatus", "jiraTestAutomationIssueType",
					 "productionDefectCustomField", "productionDefectIdentifier",
					"productionDefectValue", "productionDefectComponentValue")),

	KPI1("Processor (Kanban)", KPISource.JIRA.name(), Arrays.asList("jiraIssueTypeNames", "storyFirstStatus",
			"epicCostOfDelay", "epicRiskReduction", "epicUserBusinessValue", "epicWsjf", "epicTimeCriticality",
			"epicJobSize", "jiraIssueEpicType", "rootCause", "additionalFilterConfig", "estimationCriteria",
			"jiraStoryPointsCustomField", "jiraLiveStatusLTK", "jiraLiveStatusNOPK", "jiraLiveStatusNOSK",
			"jiraLiveStatusNORK", "jiraLiveStatusOTA", "ticketCountIssueType",

			"kanbanRCACountIssueType",

			"jiraTicketVelocityIssueType",

			"ticketDeliverdStatus", "jiraTicketClosedStatus", "kanbanCycleTimeIssueType", "jiraTicketTriagedStatus",

			"jiraTicketRejectedStatus")),

	KPI40("Issue Count", KPISource.JIRA.name(), Arrays.asList("jiraStoryIdentificationKpi40")),

	KPI39("Sprint Velocity", KPISource.JIRA.name(), Arrays.asList("jiraIterationCompletionStatusKpi39" , "jiraIterationIssuetypeKPI39")),

	KPI5("Sprint Predictability", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationIssuetypeKpi5", "jiraIterationCompletionStatusKpi5")),

	KPI46("Sprint Capacity Utilization", KPISource.JIRA.name(), Arrays.asList("jiraSprintCapacityIssueTypeKpi46")),

	KPI82("First Time Pass Rate", KPISource.JIRA.name(),
			Arrays.asList("jiraStatusForDevelopmentKPI82", "jiraKPI82StoryIdentification",
					"jiraIssueDeliverdStatusKPI82", "resolutionTypeForRejectionKPI82", "jiraDefectRejectionStatusKPI82",
					"defectPriorityKPI82", "excludeRCAFromKPI82", "jiraStatusForQaKPI82", "jiraFtprRejectStatusKPI82")),

	KPI135("First Time Pass Rate (Iteration)", KPISource.JIRA.name(),
			Arrays.asList("jiraStatusForDevelopmentKPI135", "jiraKPI135StoryIdentification",
					"jiraIterationCompletionStatusKPI135", "resolutionTypeForRejectionKPI135", "excludeRCAFromKPI135",
					"jiraDefectRejectionStatusKPI135", "defectPriorityKPI135", "jiraDefectRejectionStatusKPI135",
					"jiraStatusForQaKPI135", "jiraFtprRejectStatusKPI135")),

	KPI3("Lead Time (Scrum)", KPISource.JIRA.name(), Arrays.asList("jiraIssueTypeKPI3", "jiraDorKPI3", "jiraDodKPI3",
			"jiraLiveStatusKPI3", "storyFirstStatusKPI3")),

	KPI34("Defect Removal Efficiency", KPISource.JIRA.name(), Arrays.asList("jiraDefectRemovalStatusKPI34")),

	KPI37("Defect Rejection Rate", KPISource.JIRA.name(),
			Arrays.asList("resolutionTypeForRejectionKPI37", "jiraDefectRejectionStatusKPI37", "jiraDodKPI37")),

	KPI28("Defect Count By Priority (Scrum)", KPISource.JIRA.name(), Arrays.asList("jiraDefectCountlIssueTypeKPI28",
			"resolutionTypeForRejectionKPI28", "jiraDefectRejectionStatusKPI28")),

	KPI140("Defect Count by Priority (Iteration)", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKPI140")),

	KPI36("Defect Count by RCA (Scrum)", KPISource.JIRA.name(), Arrays.asList("jiraDefectCountlIssueTypeKPI36",
			"resolutionTypeForRejectionRCAKPI36", "jiraDefectRejectionStatusRCAKPI36")),

	KPI132("Defect Count by RCA (Iteration)", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKPI132")),

	KPI136("Defect Count by Status (Iteration)", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKPI136")),

	KPI14("Defect Injection Rate", KPISource.JIRA.name(),
			Arrays.asList("excludeRCAFromKPI14", "resolutionTypeForRejectionKPI14", "jiraDefectRejectionStatusKPI14",
					"defectPriorityKPI14", "jiraDefectInjectionIssueTypeKPI14", "jiraDefectCreatedStatusKPI14",
					"jiraDodKPI14")),

	KPI111("Defect Density", KPISource.JIRA.name(),
			Arrays.asList("jiraDodQAKPI111", "jiraQAKPI111IssueType", "defectPriorityQAKPI111",
					"excludeRCAFromQAKPI111", "resolutionTypeForRejectionQAKPI111",
					"jiraDefectRejectionStatusQAKPI111")),

	KPI127("Production Defects Ageing", KPISource.JIRA.name(),
			Arrays.asList("jiraDodKPI127", "jiraLiveStatusKPI127", "jiraDefectDroppedStatusKPI127")),

	KPI35("Defect Seepage Rate", KPISource.JIRA.name(),
			Arrays.asList("jiraIssueTypeKPI35", "resolutionTypeForRejectionKPI35", "jiraDefectRejectionStatusKPI35")),

	KPI133("Quality Status", KPISource.JIRA.name(),
			Arrays.asList("resolutionTypeForRejectionKPI133", "jiraDefectRejectionStatusKPI133", "defectPriorityKPI133",
					"excludeRCAFromKPI133", "jiraIterationCompletionStatusKPI133")),

	KPI126("Created vs Resolved defects", KPISource.JIRA.name(), Arrays.asList("jiraIssueDeliverdStatusKPI126")),

	KPI72("Commitment Reliability", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKpi72", "jiraIterationIssuetypeKpi72")),

	KPI122("Closure Possible Today", KPISource.JIRA.name(), Arrays.asList("jiraIterationCompletionStatusKPI122",
			"jiraIterationIssuetypeKPI122", "jiraStatusForInProgressKPI122")),

	KPI145("Dev Completion Status", KPISource.JIRA.name(), Arrays.asList("jiraIterationCompletionStatusKPI145",
			"jiraIterationIssuetypeKPI145", "jiraStatusForInProgressDCS", "jiraDevDoneStatusKPI145")),

	KPI75("Estimate vs Actual", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKPI75", "jiraIterationIssuetypeKPI75")),

	KPI124("Estimation Hygiene", KPISource.JIRA.name(), Arrays.asList("jiraIterationCompletionStatusKPI124",
			"jiraIterationIssuetypeKPI124", "issueStatusExcluMissingWorkKPI124")),

	KPI123("Issue Likely To Spill", KPISource.JIRA.name(), Arrays.asList("jiraIterationCompletionStatusKPI123",
			"jiraIterationIssuetypeKPI123", "jiraStatusForInProgressKPI123")),

	KPI125("Iteration Burnup", KPISource.JIRA.name(), Arrays.asList("jiraIterationCompletionStatusKPI125",
			"jiraIterationIssuetypeKPI125", "jiraStatusForInProgressKPI125")),

	KPI120("Iteration Commitment", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKPI120", "jiraIterationIssuetypeKPI120")),

	KPI128("Planned Work Status", KPISource.JIRA.name(), Arrays.asList("jiraIterationCompletionStatusKPI128",
			"jiraIterationIssuetypeKPI128", "jiraDevDoneStatusKPI128", "jiraStatusForInProgressKPI128")),

	KPI134("Unplanned Work Status", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKPI134", "jiraIterationIssuetypeKPI134")),

	KPI119("Work Remaining", KPISource.JIRA.name(), Arrays.asList("jiraIterationCompletionStatusKPI119",
			"jiraIterationIssuetypeKPI119", "jiraDevDoneStatusKPI119", "jiraStatusForInProgressKPI119")),

	KPI131("Wastage", KPISource.JIRA.name(), Arrays.asList("jiraIncludeBlockedStatusKPI131",
			"jiraIterationCompletionStatusKPI131", "jiraIterationIssuetypeKPI131", "jiraWaitStatusKPI131")),

	KPI138("Backlog Readiness Efficiency", KPISource.JIRA.name(), Arrays.asList("jiraIterationCompletionStatusKPI138",
			"jiraIterationIssuetypeKPI138", "readyForDevelopmentStatusKPI138", "jiraIssueDeliverdStatusKPI138")),

	KPI137("Defect Reopen Rate (Backlog)", KPISource.JIRA.name(), Arrays.asList("jiraDefectClosedStatusKPI137")),

	KPI129("Issues Without Story Link", KPISource.JIRA.name(),
			Arrays.asList("jiraStoryIdentificationKPI129", "excludeStatusKpi129")),

	KPI139("Refinement Rejection Rate", KPISource.JIRA.name(), Arrays.asList("jiraAcceptedInRefinementKPI139",
			"jiraReadyForRefinementKPI139", "jiraRejectedInRefinementKPI139")),

	KPI148("Flow Load", KPISource.JIRA.name(),
			Arrays.asList("storyFirstStatusKPI148", "jiraStatusForQaKPI148", "jiraStatusForInProgressKPI148")),

	KPI151("Backlog Count By Status", KPISource.JIRA.name(),
			Arrays.asList("jiraDodKPI151", "jiraDefectRejectionStatusKPI151", "jiraLiveStatusKPI151")),

	KPI152("Backlog Count By Issue Type", KPISource.JIRA.name(),
			Arrays.asList("jiraDodKPI152", "jiraDefectRejectionStatusKPI152", "jiraLiveStatusKPI152"));

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

	public List<String> getFields() {
		return fields;
	}

	public String getKpiName() {
		return kpiName;
	}

}