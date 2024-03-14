/*
 *
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.publicissapient.kpidashboard.apis.mongock.rollback.release_830;

import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shi6
 */
@ChangeUnit(id = "r_mandatory_field", order = "08351", author = "shi6", systemVersion = "8.3.5")
public class MandatoryField {
	private static final String CUSTOM_ISSUE_TYPE_LABEL = "Issue Types to consider Custom Completion status/es";
	private static final String CUSTOM_STATUS_LABEL = "Custom Completion status/es";
	private static final String FIELD_NAME = "fieldName";
	private static final String WAIT_STATUS_KPI131 = "jiraWaitStatusKPI131";
	private static final String EPIC_TYPE = "jiraIssueEpicTypeKPI153";
	private static final String DOD_KPI111 = "jiraDodQAKPI111";
	private static final String ITERATION_COMPLETIONKPI122 = "jiraIterationCompletionStatusKPI122";
	private static final String ITERATION_COMPLETIONKPI119 = "jiraIterationCompletionStatusKPI119";
	private static final String ITERATION_COMPLETIONKPI123 = "jiraIterationCompletionStatusKPI123";
	public static final String TOGGLE_LABEL_RIGHT = "toggleLabelRight";
	public static final String JIRA_BUG_RAISED_BY_IDENTIFICATION = "jiraBugRaisedByIdentification";
	public static final String UAT_IDENTIFICATION = "jiraBugRaisedByIdentification";
	private final MongoTemplate mongoTemplate;

	public MandatoryField(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "field_mapping_structure");
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraStoryIdentificationKpi40", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraStoryIdentificationKPI164", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraDefectInjectionIssueTypeKPI14", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraDodKPI14", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraIssueDeliverdStatusKPI82", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraKPI82StoryIdentification", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraStatusForDevelopmentKPI82", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraStatusForQaKPI82", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraQAKPI111IssueType", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, DOD_KPI111, false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraIssueTypeKPI35", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, JIRA_BUG_RAISED_BY_IDENTIFICATION, true, false);
		processorCommonAndMandatoryFieldUnset(bulkOps, "resolutionTypeForRejectionKPI37", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraDodKPI37", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraDefectRejectionStatusKPI37", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraDefectCountlIssueTypeKPI28", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraDefectCountlIssueTypeKPI36", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraIssueDeliverdStatusKPI126", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, EPIC_TYPE, false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraStatusForInProgressKPI119", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, WAIT_STATUS_KPI131, false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraIncludeBlockedStatusKPI131", true, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraDevDoneStatusKPI145", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraStatusForInProgressKPI145", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraKPI135StoryIdentification", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraDodKPI163", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "readyForDevelopmentStatusKPI138", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraIssueDeliverdStatusKPI138", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraStatusForInProgressKPI161", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraStatusForRefinedKPI161", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraStatusForNotRefinedKPI161", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraStoryIdentificationKPI129", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraDefectClosedStatusKPI137", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraAcceptedInRefinementKPI139", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraReadyForRefinementKPI139", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraRejectedInRefinementKPI139", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraIssueTypeKPI3", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraLiveStatusKPI3", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraDorKPI171", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraDodKPI171", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraLiveStatusKPI171", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "storyFirstStatusKPI171", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraIssueWaitStateKPI170", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraIssueClosedStateKPI170", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraIssueTypeNamesKPI148", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraStatusForInProgressKPI148", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraStatusForQaKPI148", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "storyFirstStatusKPI148", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraIssueTypeNamesKPI146", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "epicPlannedValue", true, false);
		processorCommonAndMandatoryFieldUnset(bulkOps, "epicAchievedValue", true, false);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraTestAutomationIssueType", true, false);
		processorCommonAndMandatoryFieldUnset(bulkOps, "productionDefectIdentifier", true, false);
		processorCommonAndMandatoryFieldUnset(bulkOps, "testingPhaseDefectsIdentifier", true, false);

		processorCommonAndMandatoryFieldUnset(bulkOps, "jiradefecttype", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraIssueTypeNames", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "rootCauseIdentifier", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraStoryPointsCustomField", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "epicCostOfDelay", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "epicRiskReduction", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "epicUserBusinessValue", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "epicWsjf", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "epicTimeCriticality", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "epicJobSize", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "estimationCriteria", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraDueDateField", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "sprintName", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraDevDueDateField", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "epicLink", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "notificationEnabler", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraIssueEpicType", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraSubTaskDefectType", false, true);
		processorCommonAndMandatoryFieldUnset(bulkOps, "jiraSubTaskIdentification", false, true);

		String[] issueTypes = { "jiraIterationIssuetypeKPI39", "jiraIterationIssuetypeKPI138",
				"jiraIterationIssuetypeKPI119", "jiraIterationIssuetypeKPI131", "jiraIterationIssuetypeKPI134",
				"jiraIterationIssuetypeKPI128", "jiraIterationIssuetypeKPI120", "jiraIterationIssuetypeKPI125",
				"jiraIterationIssuetypeKPI123", "jiraIterationIssuetypeKPI124", "jiraIterationIssuetypeKPI75",
				"jiraIterationIssuetypeKPI145", "jiraIterationIssuetypeKPI122", "jiraIterationIssuetypeKpi72", "jiraIterationIssuetypeKpi5" };

		for (String issueType : issueTypes) {
			updateFieldLabel(bulkOps, issueType, "Issue types to consider ‘Completed status’");
		}

		String[] completionStatusTypes = { "jiraIterationCompletionStatusKpi72", "jiraIterationCompletionStatusKpi5",
				"jiraIterationCompletionStatusKpi39", "jiraIterationCompletionStatusKPI138", ITERATION_COMPLETIONKPI122,
				ITERATION_COMPLETIONKPI119, "jiraIterationCompletionStatusKPI131",
				"jiraIterationCompletionStatusKPI134", "jiraIterationCompletionStatusKPI133",
				"jiraIterationCompletionStatusKPI128", "jiraIterationCompletionStatusKPI120",
				"jiraIterationCompletionStatusKPI125", ITERATION_COMPLETIONKPI123,
				"jiraIterationCompletionStatusKPI135", "jiraIterationCompletionStatusKPI124",
				"jiraIterationCompletionStatusKPI75", "jiraIterationCompletionStatusKPI145", ITERATION_COMPLETIONKPI119,
				"jiraIterationCompletionStatusKPI154", "jiraIterationCompletionStatusCustomField" };

		for (String completionStatusType : completionStatusTypes) {
			updateFieldLabel(bulkOps, completionStatusType, "Status to identify completed issues");
		}

		updateFieldLabel(bulkOps, DOD_KPI111, "Status considered for defect closure");
		updateFieldLabel(bulkOps, EPIC_TYPE, "Epic Issue Type");
		updateFieldLabel(bulkOps, WAIT_STATUS_KPI131, "Status that signify queue");
		updateFieldLabel(bulkOps, UAT_IDENTIFICATION, "Escaped defects identification (Processor Run)");
		changeToggleLabelRight(bulkOps, "populateByDevDoneKPI150", "Dev Completion*" );
		addInfoToTooltip(bulkOps, ITERATION_COMPLETIONKPI122,
				"All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion).");
		addInfoToTooltip(bulkOps, ITERATION_COMPLETIONKPI123,
				"All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion).");

		bulkOps.execute();
	}

	public void processorCommonAndMandatoryFieldUnset(BulkOperations bulkOps, String field, boolean processorCommon,
			boolean mandatoryField) {
		Query query = new Query(Criteria.where(FIELD_NAME).is(field));
		if (processorCommon)
			bulkOps.updateOne(query, new Update().unset("processorCommon"));
		if (mandatoryField)
			bulkOps.updateOne(query, new Update().unset("mandatory"));
	}

	@RollbackExecution
	public void rollback() {
		BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "field_mapping_structure");
		processorCommonAndMandatoryField(bulkOps, "jiraStoryIdentificationKpi40", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraStoryIdentificationKPI164", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraDefectInjectionIssueTypeKPI14", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraDodKPI14", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraIssueDeliverdStatusKPI82", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraKPI82StoryIdentification", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraStatusForDevelopmentKPI82", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraStatusForQaKPI82", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraQAKPI111IssueType", false, true);
		processorCommonAndMandatoryField(bulkOps, DOD_KPI111, false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraIssueTypeKPI35", false, true);
		processorCommonAndMandatoryField(bulkOps, JIRA_BUG_RAISED_BY_IDENTIFICATION, true, false);
		processorCommonAndMandatoryField(bulkOps, "resolutionTypeForRejectionKPI37", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraDodKPI37", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraDefectRejectionStatusKPI37", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraDefectCountlIssueTypeKPI28", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraDefectCountlIssueTypeKPI36", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraIssueDeliverdStatusKPI126", false, true);
		processorCommonAndMandatoryField(bulkOps, EPIC_TYPE, false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraStatusForInProgressKPI119", false, true);
		processorCommonAndMandatoryField(bulkOps, WAIT_STATUS_KPI131, false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraIncludeBlockedStatusKPI131", true, true);
		processorCommonAndMandatoryField(bulkOps, "jiraDevDoneStatusKPI145", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraStatusForInProgressKPI145", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraKPI135StoryIdentification", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraDodKPI163", false, true);
		processorCommonAndMandatoryField(bulkOps, "readyForDevelopmentStatusKPI138", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraIssueDeliverdStatusKPI138", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraStatusForInProgressKPI161", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraStatusForRefinedKPI161", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraStatusForNotRefinedKPI161", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraStoryIdentificationKPI129", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraDefectClosedStatusKPI137", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraAcceptedInRefinementKPI139", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraReadyForRefinementKPI139", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraRejectedInRefinementKPI139", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraIssueTypeKPI3", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraLiveStatusKPI3", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraDorKPI171", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraDodKPI171", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraLiveStatusKPI171", false, true);
		processorCommonAndMandatoryField(bulkOps, "storyFirstStatusKPI171", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraIssueWaitStateKPI170", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraIssueClosedStateKPI170", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraIssueTypeNamesKPI148", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraStatusForInProgressKPI148", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraStatusForQaKPI148", false, true);
		processorCommonAndMandatoryField(bulkOps, "storyFirstStatusKPI148", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraIssueTypeNamesKPI146", false, true);
		processorCommonAndMandatoryField(bulkOps, "epicPlannedValue", true, false);
		processorCommonAndMandatoryField(bulkOps, "epicAchievedValue", true, false);
		processorCommonAndMandatoryField(bulkOps, "jiraTestAutomationIssueType", true, false);
		processorCommonAndMandatoryField(bulkOps, "productionDefectIdentifier", true, false);
		processorCommonAndMandatoryField(bulkOps, "testingPhaseDefectsIdentifier", true, false);

		processorCommonAndMandatoryField(bulkOps, "jiradefecttype", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraIssueTypeNames", false, true);
		processorCommonAndMandatoryField(bulkOps, "rootCauseIdentifier", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraStoryPointsCustomField", false, true);
		processorCommonAndMandatoryField(bulkOps, "epicCostOfDelay", false, true);
		processorCommonAndMandatoryField(bulkOps, "epicRiskReduction", false, true);
		processorCommonAndMandatoryField(bulkOps, "epicUserBusinessValue", false, true);
		processorCommonAndMandatoryField(bulkOps, "epicWsjf", false, true);
		processorCommonAndMandatoryField(bulkOps, "epicTimeCriticality", false, true);
		processorCommonAndMandatoryField(bulkOps, "epicJobSize", false, true);
		processorCommonAndMandatoryField(bulkOps, "estimationCriteria", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraDueDateField", false, true);
		processorCommonAndMandatoryField(bulkOps, "sprintName", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraDevDueDateField", false, true);
		processorCommonAndMandatoryField(bulkOps, "epicLink", false, true);
		processorCommonAndMandatoryField(bulkOps, "notificationEnabler", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraIssueEpicType", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraSubTaskDefectType", false, true);
		processorCommonAndMandatoryField(bulkOps, "jiraSubTaskIdentification", false, true);

		String[] issueTypes = { "jiraIterationIssuetypeKPI39", "jiraIterationIssuetypeKPI138",
				"jiraIterationIssuetypeKPI119", "jiraIterationIssuetypeKPI131", "jiraIterationIssuetypeKPI134",
				"jiraIterationIssuetypeKPI128", "jiraIterationIssuetypeKPI120", "jiraIterationIssuetypeKPI125",
				"jiraIterationIssuetypeKPI123", "jiraIterationIssuetypeKPI124", "jiraIterationIssuetypeKPI75",
				"jiraIterationIssuetypeKPI145", "jiraIterationIssuetypeKPI122", "jiraIterationIssuetypeKpi72", "jiraIterationIssuetypeKpi5"};

		for (String issueType : issueTypes) {
			updateFieldLabel(bulkOps, issueType, CUSTOM_ISSUE_TYPE_LABEL);
		}

		String[] completionStatusTypes = { "jiraIterationCompletionStatusKpi72", "jiraIterationCompletionStatusKpi5",
				"jiraIterationCompletionStatusKpi39", "jiraIterationCompletionStatusKPI138", ITERATION_COMPLETIONKPI122,
				ITERATION_COMPLETIONKPI119, "jiraIterationCompletionStatusKPI131",
				"jiraIterationCompletionStatusKPI134", "jiraIterationCompletionStatusKPI133",
				"jiraIterationCompletionStatusKPI128", "jiraIterationCompletionStatusKPI120",
				"jiraIterationCompletionStatusKPI125", ITERATION_COMPLETIONKPI123,
				"jiraIterationCompletionStatusKPI135", "jiraIterationCompletionStatusKPI124",
				"jiraIterationCompletionStatusKPI75", "jiraIterationCompletionStatusKPI145", ITERATION_COMPLETIONKPI119,
				"jiraIterationCompletionStatusKPI154", "jiraIterationCompletionStatusCustomField" };

		for (String completionStatusType : completionStatusTypes) {
			updateFieldLabel(bulkOps, completionStatusType, CUSTOM_STATUS_LABEL);
		}

		updateFieldLabel(bulkOps, DOD_KPI111, "Status Consider for Issue Closure");
		updateFieldLabel(bulkOps, EPIC_TYPE, "Issue type to identify epic/feature");
		updateFieldLabel(bulkOps, WAIT_STATUS_KPI131, "Status to identify Wait Statuses");
		updateFieldLabel(bulkOps, UAT_IDENTIFICATION, "Escaped defects identification" );
		changeToggleLabelRight(bulkOps, "populateByDevDoneKPI150", "Dev Completion" );
		addInfoToTooltip(bulkOps, ITERATION_COMPLETIONKPI122,
				"All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion). The configured value need to be same as defined in \"Custom Completion status/es \" under work remaining ");
		addInfoToTooltip(bulkOps, ITERATION_COMPLETIONKPI123,
				"All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion). The configured value need to be same as defined in\"Status to identify In progress issues\" under work remaining ");

		bulkOps.execute();
	}

	public void processorCommonAndMandatoryField(BulkOperations bulkOps, String field, boolean processorCommon,
			boolean mandatoryField) {
		Query query = new Query(Criteria.where(FIELD_NAME).is(field));
		if (processorCommon)
			bulkOps.updateOne(query, new Update().set("processorCommon", true));
		if (mandatoryField)
			bulkOps.updateOne(query, new Update().set("mandatory", true));
	}

	public void updateFieldLabel(BulkOperations bulkOps, String fieldNameToUpdate, String newFieldLabel) {
		bulkOps.updateOne(new Query(Criteria.where(FIELD_NAME).is(fieldNameToUpdate)),
				new Update().set("fieldLabel", newFieldLabel));
	}

	public void changeToggleLabelRight(BulkOperations bulkOps, String fieldNameToUpdate, String newVal) {
		bulkOps.updateOne(new Query(Criteria.where(FIELD_NAME).is(fieldNameToUpdate)),
				new Update().set(TOGGLE_LABEL_RIGHT, newVal));
	}

	public void addInfoToTooltip(BulkOperations bulkOps, String fieldName, String info) {
		bulkOps.updateOne(new Query(Criteria.where(FIELD_NAME).is(fieldName)),
				new Update().set("tooltip.definition", info));
	}

}
