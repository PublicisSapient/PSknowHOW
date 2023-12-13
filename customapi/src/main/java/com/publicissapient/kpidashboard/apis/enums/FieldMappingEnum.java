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
import java.util.Collections;
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
					"epicJobSize", "additionalFilterConfig", "jiraDueDateField", "jiraDueDateCustomField",
					"jiraDevDueDateField", "jiraIssueEpicType", "storyFirstStatus", "jiraTestAutomationIssueType",
					"productionDefectCustomField", "productionDefectIdentifier", "productionDefectValue",
					"productionDefectComponentValue", "notificationEnabler", "epicPlannedValue", "epicAchievedValue",
					"epicLink", "jiraSubTaskDefectType", "testingPhaseDefectCustomField", "testingPhaseDefectsIdentifier",
					"testingPhaseDefectValue", "testingPhaseDefectComponentValue", "jiraSubTaskIdentification")),

	KPI1("Processor (Kanban)", KPISource.JIRA.name(), Arrays.asList("jiraIssueTypeNames", "storyFirstStatus",
			"epicCostOfDelay", "epicRiskReduction", "epicUserBusinessValue", "epicWsjf", "epicTimeCriticality",
			"epicJobSize", "jiraIssueEpicType", "rootCause", "additionalFilterConfig", "estimationCriteria",
			"jiraStoryPointsCustomField", "jiraLiveStatusLTK", "jiraLiveStatusNOPK", "jiraLiveStatusNOSK",
			"jiraLiveStatusNORK", "jiraLiveStatusOTA", "ticketCountIssueType", "kanbanRCACountIssueType",
			"jiraTicketVelocityIssueType", "ticketDeliverdStatus", "jiraTicketClosedStatus", "kanbanCycleTimeIssueType",
			"jiraTicketTriagedStatus", "jiraTicketRejectedStatus", "jiraSubTaskDefectType")),

	KPI40("Issue Count", KPISource.JIRA.name(), Arrays.asList("jiraStoryIdentificationKpi40","thresholdValueKPI40")),

	KPI39("Sprint Velocity", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKpi39", "jiraIterationIssuetypeKPI39","thresholdValueKPI39")),

	KPI5("Sprint Predictability", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationIssuetypeKpi5", "jiraIterationCompletionStatusKpi5","thresholdValueKPI5")),

	KPI46("Sprint Capacity Utilization", KPISource.JIRA.name(), Arrays.asList("jiraSprintCapacityIssueTypeKpi46","thresholdValueKPI46")),

	KPI82("First Time Pass Rate", KPISource.JIRA.name(),
			Arrays.asList("jiraStatusForDevelopmentKPI82", "jiraKPI82StoryIdentification",
					"jiraIssueDeliverdStatusKPI82", "resolutionTypeForRejectionKPI82", "jiraDefectRejectionStatusKPI82",
					"defectPriorityKPI82", "includeRCAForKPI82", "jiraStatusForQaKPI82", "jiraFtprRejectStatusKPI82", "thresholdValueKPI82", "jiraLabelsKPI82")),

	KPI135("First Time Pass Rate (Iteration)", KPISource.JIRA.name(),
			Arrays.asList("jiraStatusForDevelopmentKPI135", "jiraKPI135StoryIdentification",
					"jiraIterationCompletionStatusKPI135", "resolutionTypeForRejectionKPI135", "includeRCAForKPI135",
					"jiraDefectRejectionStatusKPI135", "defectPriorityKPI135", "jiraDefectRejectionStatusKPI135",
					"jiraStatusForQaKPI135", "jiraFtprRejectStatusKPI135", "jiraLabelsKPI135")),

	KPI3("Lead Time (Scrum)", KPISource.JIRA.name(), Arrays.asList("jiraIssueTypeKPI3", "jiraLiveStatusKPI3", "thresholdValueKPI3")),

	KPI34("Defect Removal Efficiency", KPISource.JIRA.name(),
			Arrays.asList("jiraDefectRemovalStatusKPI34", "thresholdValueKPI34")),

	KPI37("Defect Rejection Rate", KPISource.JIRA.name(), Arrays.asList("resolutionTypeForRejectionKPI37",
			"jiraDefectRejectionStatusKPI37", "jiraDodKPI37", "thresholdValueKPI37")),

	KPI28("Defect Count By Priority (Scrum)", KPISource.JIRA.name(), Arrays.asList("jiraDefectCountlIssueTypeKPI28",
			"resolutionTypeForRejectionKPI28", "jiraDefectRejectionStatusKPI28", "thresholdValueKPI28")),

	KPI140("Defect Count by Priority (Iteration)", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKPI140")),

	KPI36("Defect Count by RCA (Scrum)", KPISource.JIRA.name(), Arrays.asList("jiraDefectCountlIssueTypeKPI36",
			"resolutionTypeForRejectionRCAKPI36", "jiraDefectRejectionStatusRCAKPI36", "thresholdValueKPI36")),

	KPI132("Defect Count by RCA (Iteration)", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKPI132")),

	KPI136("Defect Count by Status (Iteration)", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKPI136")),

	KPI14("Defect Injection Rate", KPISource.JIRA.name(),
			Arrays.asList("includeRCAForKPI14", "resolutionTypeForRejectionKPI14", "jiraDefectRejectionStatusKPI14",
					"defectPriorityKPI14", "jiraDefectInjectionIssueTypeKPI14", "jiraDefectCreatedStatusKPI14",
					"jiraDodKPI14", "thresholdValueKPI14", "jiraLabelsKPI14")),

	KPI111("Defect Density", KPISource.JIRA.name(),
			Arrays.asList("jiraDodQAKPI111", "jiraQAKPI111IssueType", "defectPriorityQAKPI111",
					"includeRCAForQAKPI111", "resolutionTypeForRejectionQAKPI111", "jiraDefectRejectionStatusQAKPI111",
					"thresholdValueKPI111")),

	KPI127("Production Defects Ageing", KPISource.JIRA.name(),
			Arrays.asList("jiraDodKPI127", "jiraLiveStatusKPI127", "jiraDefectDroppedStatusKPI127","thresholdValueKPI127")),

	KPI35("Defect Seepage Rate", KPISource.JIRA.name(), Arrays.asList("jiraIssueTypeKPI35",
			"resolutionTypeForRejectionKPI35", "jiraDefectRejectionStatusKPI35", "thresholdValueKPI35")),

	KPI133("Quality Status", KPISource.JIRA.name(),
			Arrays.asList("resolutionTypeForRejectionKPI133", "jiraDefectRejectionStatusKPI133", "defectPriorityKPI133",
					"includeRCAForKPI133", "jiraIterationCompletionStatusKPI133", "jiraItrQSIssueTypeKPI133")),

	KPI126("Created vs Resolved defects", KPISource.JIRA.name(), Arrays.asList("jiraIssueDeliverdStatusKPI126","thresholdValueKPI126")),

	KPI72("Commitment Reliability", KPISource.JIRA.name(),
			Arrays.asList("jiraIterationCompletionStatusKpi72", "jiraIterationIssuetypeKpi72", "thresholdValueKPI72")),

	KPI122("Closure Possible Today", KPISource.JIRA.name(), Arrays.asList("jiraIterationCompletionStatusKPI122",
			"jiraIterationIssuetypeKPI122", "jiraStatusForInProgressKPI122")),

	KPI145("Dev Completion Status", KPISource.JIRA.name(), Arrays.asList("jiraIterationCompletionStatusKPI145",
			"jiraIterationIssuetypeKPI145", "jiraStatusForInProgressKPI145", "jiraDevDoneStatusKPI145")),

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

	KPI148("Flow Load", KPISource.JIRA.name(), Arrays.asList("storyFirstStatusKPI148", "jiraStatusForQaKPI148",
			"jiraStatusForInProgressKPI148", "jiraIssueTypeNamesKPI148")),

	KPI146("Flow Distribution", KPISource.JIRA.name(), Arrays.asList("jiraIssueTypeNamesKPI146")),

	KPI151("Backlog Count By Status", KPISource.JIRA.name(), Arrays.asList("jiraDodKPI151",
			"jiraDefectRejectionStatusKPI151", "jiraLiveStatusKPI151", "jiraIssueTypeNamesKPI151")),

	KPI152("Backlog Count By Issue Type", KPISource.JIRA.name(), Arrays.asList("jiraDodKPI152",
			"jiraDefectRejectionStatusKPI152", "jiraLiveStatusKPI152", "jiraIssueTypeNamesKPI152")),

	KPI153("PI Predictability", KPISource.JIRA.name(), Arrays.asList("jiraIssueEpicTypeKPI153","thresholdValueKPI153")),

	KPI154("Daily StandUp View", KPISource.JIRA.name(),
			Arrays.asList("jiraStatusStartDevelopmentKPI154", "jiraDevDoneStatusKPI154", "jiraQADoneStatusKPI154",
					"jiraIterationCompletionStatusKPI154", "jiraStatusForInProgressKPI154","storyFirstStatusKPI154", "jiraOnHoldStatusKPI154")),

	// DTS-26123 start
	KPI155("Defect Count By Type", KPISource.JIRA.name(),
			Arrays.asList("jiraDefectRejectionStatusKPI155", "jiraDodKPI155", "jiraLiveStatusKPI155")),
	// DTS-26123 end
	KPI42("Regression Automation Coverage", KPISource.ZEPHYR.name(), Arrays.asList("uploadDataKPI42","thresholdValueKPI42")),

	KPI16("Insprint Automation Coverage", KPISource.ZEPHYR.name(),
			Arrays.asList("uploadDataKPI16", "thresholdValueKPI16")),

	KPI161("Iteration Readiness", KPISource.JIRA.name(), Arrays.asList("jiraIssueTypeNamesKPI161",
			"jiraStatusForInProgressKPI161", "jiraStatusForRefinedKPI161", "jiraStatusForNotRefinedKPI161")),

	KPI164("Scope Churn", KPISource.JIRA.name(), Arrays.asList("jiraStoryIdentificationKPI164","thresholdValueKPI164")),

	KPI156("Lead Time For Change", KPISource.JIRA.name(),
			Arrays.asList("leadTimeConfigRepoTool", "toBranchForMRKPI156" , "jiraDodKPI156" , "jiraIssueTypeKPI156","thresholdValueKPI156")),

	KPI163("Defect by Testing Phase", KPISource.JIRA.name(), Collections.singletonList("jiraDodKPI163")),

	KPI150("Release BurnUp", KPISource.JIRA.name(), Arrays.asList("startDateCountKPI150","populateByDevDoneKPI150","jiraDevDoneStatusKPI150")),

	KPI17("Unit Test Coverage", KPISource.SONAR.name(), Arrays.asList("thresholdValueKPI17")),

	KPI62("Unit Test Coverage (Kanban) ", KPISource.SONARKANBAN.name(), Arrays.asList("thresholdValueKPI62")),

	KPI38("Sonar Violations", KPISource.SONAR.name(), Arrays.asList("thresholdValueKPI38")),

	KPI64("Sonar Violations (Kanban) ", KPISource.SONAR.name(), Arrays.asList("thresholdValueKPI64")),

	KPI84("Mean Time To Merge", KPISource.BITBUCKET.name(), Arrays.asList("thresholdValueKPI84")),

	KPI11("Check-Ins & Merge Requests", KPISource.BITBUCKET.name(), Arrays.asList("thresholdValueKPI11")),

	KPI157("Check-Ins & Merge Requests (Developer) ", KPISource.BITBUCKET.name(), Arrays.asList("thresholdValueKPI157")),

	KPI158("Mean Time To Merge (Developer) ", KPISource.BITBUCKET.name(), Arrays.asList("thresholdValueKPI158")),

	KPI159("Number of Check-ins (Developer) ", KPISource.BITBUCKETKANBAN.name(), Arrays.asList("thresholdValueKPI159")),

	KPI160("Pickup Time (Developer) ", KPISource.BITBUCKET.name(), Arrays.asList("thresholdValueKPI160")),

	KPI65("Check-Ins & Merge Requests (Kanban) ", KPISource.BITBUCKET.name(), Arrays.asList("thresholdValueKPI65")),

	KPI27("Sonar Tech Debt", KPISource.SONAR.name(), Arrays.asList("thresholdValueKPI27")),

	KPI67("Sonar Tech Debt (Kanban)", KPISource.SONAR.name(), Arrays.asList("thresholdValueKPI67")),

	KPI166("Mean Time to Recover", KPISource.JIRA.name(), Arrays.asList("jiraStoryIdentificationKPI166","jiraDodKPI166","jiraProductionIncidentIdentification",
			"jiraProdIncidentRaisedByCustomField" , "jiraProdIncidentRaisedByValue", "thresholdValueKPI166")),

	KPI170("Flow Efficiency", KPISource.JIRA.name(), Arrays.asList("jiraIssueWaitStateKPI170", "jiraIssueClosedStateKPI170","thresholdValueKPI170")),

	KPI171("Cycle Time", KPISource.JIRA.name(), Arrays.asList("jiraIssueTypeKPI171", "jiraDorKPI171", "jiraDodKPI171",
			"jiraLiveStatusKPI171", "storyFirstStatusKPI171")),
	KPI162("PR Size", KPISource.BITBUCKET.name(), Collections.singletonList("thresholdValueKPI162")),

	KPI73("Release Frequency", KPISource.JIRA.name(), Collections.singletonList("thresholdValueKPI73")),
	KPI149("Happiness Index", KPISource.JIRA.name(), Collections.singletonList("thresholdValueKPI149")),
	KPI113("Value delivered (Cost of Delay)", KPISource.JIRA.name(), Collections.singletonList("thresholdValueKPI113")),
	KPI70("Test Execution and pass percentage", KPISource.ZEPHYR.name(), Collections.singletonList("thresholdValueKPI70")),
	KPI8("Code Build Time", KPISource.JENKINS.name(), Collections.singletonList("thresholdValueKPI8")),
	KPI116("Change Failure Rate", KPISource.JENKINS.name(), Collections.singletonList("thresholdValueKPI116")),
	KPI118("Deployment Frequency", KPISource.JENKINS.name(), Collections.singletonList("thresholdValueKPI118"));

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