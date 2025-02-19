/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1110;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author purgupta2
 */
@ChangeUnit(id = "r_fixathon_field_mapping", order = "011102", author = "purgupta2", systemVersion = "11.1.0")
public class FixathonFieldMappingStructure {

	public static final String FIELD_MAPPING_STRUCTURE = "field_mapping_structure";
	public static final String FIELD_LABEL = "fieldLabel";
	public static final String FIELD_TYPE = "fieldType";
	public static final String FIELD_NAME = "fieldName";
	public static final String TOOL_TIP = "tooltip";
	public static final String DEFINITION = "definition";
	public static final String VALUE = "value";
	private static final String FIELD_CATEGORY = "fieldCategory";
	private static final String SECTION = "section";
	private static final String CHIPS = "chips";
	private static final String DELIVERED_STATUS = "jiraIterationCompletionStatusKPI138";
	public static final String ISSUE_TYPES_TO_CONSIDER = "Issue types to consider";
	public static final String JIRA_ISSUE_TYPE_NAMES_KPI_161 = "jiraIssueTypeNamesKPI161";
	public static final String JIRA_STATUS_FOR_NOT_REFINED_KPI_161 = "jiraStatusForNotRefinedKPI161";
	public static final String JIRA_STATUS_FOR_REFINED_KPI_161 = "jiraStatusForRefinedKPI161";
	public static final String JIRA_STATUS_FOR_IN_PROGRESS_KPI_161 = "jiraStatusForInProgressKPI161";
	public static final String JIRA_ISSUE_TYPE_KPI_171 = "jiraIssueTypeKPI171";
	public static final String STORY_FIRST_STATUS_KPI_171 = "storyFirstStatusKPI171";
	public static final String JIRA_DOR_KPI_171 = "jiraDorKPI171";
	public static final String JIRA_DOD_KPI_171 = "jiraDodKPI171";
	public static final String JIRA_LIVE_STATUS_KPI_171 = "jiraLiveStatusKPI171";
	public static final String FIELD_DISPLAY_ORDER = "fieldDisplayOrder";
	public static final String SECTION_ORDER = "sectionOrder";
	public static final String UNSET = "$unset";
	private static final String AGEING_CONSIDERED_STATUS = "jiraStatusToConsiderKPI127";

	private final MongoTemplate mongoTemplate;

	public FixathonFieldMappingStructure(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		final MongoCollection<Document> fieldMappingStructCollection = mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE);
		updateFieldMappingByFieldName("readyForDevelopmentStatusKPI138", "Status to identify issues Ready for Development",
				"Status to identify Ready for development from the backlog.", fieldMappingStructCollection);
		updateFieldMappingByFieldName(DELIVERED_STATUS, "Issue Delivered Status",
				"Status from workflow on which issue is delivered. <br> Example: Closed<hr>", fieldMappingStructCollection);

		// Rollback Unlinked Work Items field mapping update
		updateFieldLabel("jiraStoryIdentificationKPI129", ISSUE_TYPES_TO_CONSIDER, fieldMappingStructCollection);
		updateFieldLabel("jiraDefectClosedStatusKPI137", "Status to identify Closed Bugs", fieldMappingStructCollection);
		rollbackAddRedirectUrlField(fieldMappingStructCollection);
		updateFieldMappingByFieldName("jiraDefectRejectionStatusKPI151", "Status to identify rejected defects",
				fieldMappingStructCollection);
		updateFieldMappingByFieldName("jiraDefectRejectionStatusKPI152", "Status to identify rejected defects",
				fieldMappingStructCollection);
		updateFieldMappingByFieldName("jiraIssueTypeKPI3", "Issue types to consider ‘Completed status’",
				"All issue types that should be included in Lead time calculation", fieldMappingStructCollection);
		updateFieldMappingByFieldName("jiraLiveStatusKPI3", "Live Status - Lead Time",
				"Workflow status/es to identify that an issue is live in Production", fieldMappingStructCollection);

		// insert to fieldmapping structure
		List<Document> documents = new ArrayList<>();
		documents.add(createFieldMappingStructure(DELIVERED_STATUS, "Custom Completion status/es", CHIPS,
				"All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"));
		documents.add(createFieldMappingStructure("jiraDefectDroppedStatusKPI127", "Defect Dropped Status", CHIPS,
				"All statuses with which defect is linked."));
		Document jiraDodKPI127 = createFieldMappingStructure("jiraDodKPI127", "Status to identify completed issues", CHIPS,
				"All workflow statuses used to close issues of issue types in consideration.");
		jiraDodKPI127.append(FIELD_DISPLAY_ORDER, 8).append(SECTION_ORDER, 4);
		documents.add(jiraDodKPI127);
		documents.add(createFieldMappingStructure("jiraLiveStatusKPI127", "Status to identify live issues", "text",
				"Status/es that identify that an issue is LIVE in Production."));
		insertToFieldMappingStructure(fieldMappingStructCollection, documents);
		deleteFieldMappingStr(fieldMappingStructCollection, Arrays.asList(AGEING_CONSIDERED_STATUS));

		// rollback for cycle time kpi171
		rollbackCycleTimeFieldMappingChanges(fieldMappingStructCollection);
		rollBackFieldDisplayOrder(JIRA_ISSUE_TYPE_NAMES_KPI_161, 1, fieldMappingStructCollection);
		rollBackFieldDisplayOrder(JIRA_STATUS_FOR_NOT_REFINED_KPI_161, 1, fieldMappingStructCollection);
		rollBackFieldDisplayOrder(JIRA_STATUS_FOR_REFINED_KPI_161, 2, fieldMappingStructCollection);
		rollBackFieldDisplayOrder(JIRA_STATUS_FOR_IN_PROGRESS_KPI_161, 3, fieldMappingStructCollection);
		rollBackFieldDisplayOrderForKPI161(fieldMappingStructCollection);
		updateMandatory("productionDefectIdentifier", true, fieldMappingStructCollection, UNSET);
	}

	public void updateFieldMappingStr(MongoCollection<Document> fieldMappingStructCollection) {
		// Update for jiraIssueDeliverdStatusKPI138
		updateFieldMappingByFieldName("jiraIssueDeliverdStatusKPI138", "Status to identify DOD",
				"Workflow statuses to identify when an issue is considered 'Delivered' based on the Definition of Done (DoD), used to measure average velocity. Please list all statuses that mark an issue as 'Delivered'.",
				fieldMappingStructCollection);

		// Update for readyForDevelopmentStatusKPI138
		updateFieldMappingByFieldName("readyForDevelopmentStatusKPI138", "Status to identify DOR",
				"Workflow status/es that identify that a backlog item is ready to be taken in a sprint based on Definition of Ready (DOR)",
				fieldMappingStructCollection);
	}

	private void updateFieldMappingByFieldName(String fieldName, String fieldLabel, String tooltipDefinition,
			MongoCollection<Document> fieldMappingStructCollection) {

		fieldMappingStructCollection.updateMany(new Document(FIELD_NAME, new Document("$in", Arrays.asList(fieldName))),
				new Document("$set", new Document(FIELD_LABEL, fieldLabel).append("tooltip.definition", tooltipDefinition)));
	}

	private void updateFieldMappingByFieldName(String fieldName, String fieldLabel,
			MongoCollection<Document> fieldMappingStructCollection) {
		fieldMappingStructCollection.updateMany(new Document(FIELD_NAME, new Document("$in", Arrays.asList(fieldName))),
				new Document("$set", new Document(FIELD_LABEL, fieldLabel)));
	}

	private void deleteFieldMappingStr(MongoCollection<Document> fieldMappingStructCollection,
			List<String> fieldsToDelete) {
		fieldMappingStructCollection.deleteMany(Filters.or(Filters.in(FIELD_NAME, fieldsToDelete)));
	}

	public void rollbackAddRedirectUrlField(MongoCollection<Document> fieldMappingStructCollection) {
		fieldMappingStructCollection.updateMany(
				new Document(FIELD_NAME, new Document("$in", Arrays.asList("uploadDataKPI16", "uploadDataKPI42"))),
				new Document(UNSET, new Document("redirectUrl", "")));
	}

	private void rollbackCycleTimeFieldMappingChanges(MongoCollection<Document> fieldMappingStructCollection) {
		updateFieldLabel(JIRA_ISSUE_TYPE_KPI_171, "Issue types to consider ‘Completed status’",
				fieldMappingStructCollection);
		updateTooltipDefinition(JIRA_ISSUE_TYPE_KPI_171, "All issue types that should be included in Lead time calculation",
				fieldMappingStructCollection);

		updateFieldLabel(STORY_FIRST_STATUS_KPI_171, "Status when 'Story' issue type is created",
				fieldMappingStructCollection);
		updateTooltipDefinition(STORY_FIRST_STATUS_KPI_171, "All issue types that identify with a Story.",
				fieldMappingStructCollection);
		rollbackUpdateFieldDisplayOrder(STORY_FIRST_STATUS_KPI_171, fieldMappingStructCollection);

		updateFieldLabel(JIRA_DOR_KPI_171, "DOR status", fieldMappingStructCollection);
		updateTooltipDefinition(JIRA_DOR_KPI_171,
				"Status/es that identify that an issue is ready to be taken in the sprint", fieldMappingStructCollection);
		rollbackUpdateFieldDisplayOrder(JIRA_DOR_KPI_171, fieldMappingStructCollection);

		updateFieldLabel(JIRA_DOD_KPI_171, "Status to identify completed issues", fieldMappingStructCollection);
		updateTooltipDefinition(JIRA_DOD_KPI_171,
				"All workflow statuses used to identify completed issues based on Definition of Done (DoD).",
				fieldMappingStructCollection);
		updateFieldDisplayOrder(JIRA_DOD_KPI_171, 8, fieldMappingStructCollection);

		updateFieldLabel(JIRA_LIVE_STATUS_KPI_171, "Live Status - Cycle Time", fieldMappingStructCollection);
		updateTooltipDefinition(JIRA_LIVE_STATUS_KPI_171, "Status/es that identify that an issue is LIVE in Production'",
				fieldMappingStructCollection);
		rollbackUpdateFieldDisplayOrder(JIRA_LIVE_STATUS_KPI_171, fieldMappingStructCollection);
	}

	public void updateTooltipDefinition(String fieldName, String newTooltipDefinition,
			MongoCollection<Document> fieldMappingStructCollection) {
		fieldMappingStructCollection.updateOne(new Document(FIELD_NAME, fieldName),
				new Document("$set", new Document(TOOL_TIP, new Document(DEFINITION, newTooltipDefinition))));
	}

	public void updateFieldDisplayOrder(String fieldName, int newOrder,
			MongoCollection<Document> fieldMappingStructCollection) {
		fieldMappingStructCollection.updateOne(new Document(FIELD_NAME, fieldName),
				new Document("$set", new Document(FIELD_DISPLAY_ORDER, newOrder)));
	}

	public void rollbackUpdateFieldDisplayOrder(String fieldName,
			MongoCollection<Document> fieldMappingStructCollection) {
		fieldMappingStructCollection.updateOne(new Document(FIELD_NAME, fieldName),
				new Document(UNSET, new Document(FIELD_DISPLAY_ORDER, "")));
	}

	@RollbackExecution
	public void rollBack() {
		final MongoCollection<Document> fieldMappingStructCollection = mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE);
		updateFieldMappingStr(fieldMappingStructCollection);
		deleteFieldMappingStr(fieldMappingStructCollection,
				List.of(DELIVERED_STATUS, "jiraDefectDroppedStatusKPI127", "jiraDodKPI127", "jiraLiveStatusKPI127"));
		// production defect ageing
		Document jiraStatusToConsiderKPI127 = createFieldMappingStructure(AGEING_CONSIDERED_STATUS,
				"Statuses to be included", CHIPS, "workflow statuses to identify ageing production defects in the backlog")
				.append(FIELD_DISPLAY_ORDER, 8).append(SECTION_ORDER, 4).append("mandatory", true);
		insertToFieldMappingStructure(fieldMappingStructCollection, Arrays.asList(jiraStatusToConsiderKPI127));
		// Unlinked Work Items field mapping update
		updateFieldLabel("jiraStoryIdentificationKPI129", "Issue types to consider as Stories",
				fieldMappingStructCollection);
		updateFieldLabel("jiraDefectClosedStatusKPI137", "Status to identify Closed Issues", fieldMappingStructCollection);
		addRedirectUrlField(fieldMappingStructCollection);
		updateFieldMappingByFieldName("jiraDefectRejectionStatusKPI151", "Status to identify rejected issues",
				fieldMappingStructCollection);
		updateFieldMappingByFieldName("jiraDefectRejectionStatusKPI155", "Status to identify rejected issues",
				fieldMappingStructCollection);
		updateFieldMappingByFieldName("jiraIssueTypeKPI3", ISSUE_TYPES_TO_CONSIDER,
				"All issue types considered for Lead Time calculation.", fieldMappingStructCollection);
		updateFieldMappingByFieldName("jiraLiveStatusKPI3", "Status to identify Live issues",
				"Workflow status/es to identify that an issue is live in Production.", fieldMappingStructCollection);
		// cycle time kpi171 changes
		cycleTimeFieldMappingChanges(fieldMappingStructCollection);

		updateFieldDisplayOrder(JIRA_ISSUE_TYPE_NAMES_KPI_161, 1, fieldMappingStructCollection);
		updateFieldDisplayOrder(JIRA_STATUS_FOR_NOT_REFINED_KPI_161, 1, fieldMappingStructCollection);
		updateFieldDisplayOrder(JIRA_STATUS_FOR_REFINED_KPI_161, 2, fieldMappingStructCollection);
		updateFieldDisplayOrder(JIRA_STATUS_FOR_IN_PROGRESS_KPI_161, 3, fieldMappingStructCollection);
		updateFieldDisplayOrderForKPI161(fieldMappingStructCollection);
		updateMandatory("productionDefectIdentifier", true, fieldMappingStructCollection, "$set");
	}

	public void insertFieldMappingStructure(MongoCollection<Document> fieldMappingStructCollection) {
		Document jiraIterationCompletionStatusKPI138 = new Document().append(FIELD_NAME, DELIVERED_STATUS)
				.append(FIELD_LABEL, "Custom Completion status/es").append(FIELD_TYPE, CHIPS).append(FIELD_CATEGORY, "workflow")
				.append(SECTION, "WorkFlow Status Mapping").append(TOOL_TIP, new Document(DEFINITION,
						"All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"));

		fieldMappingStructCollection.insertMany(Arrays.asList(jiraIterationCompletionStatusKPI138));
	}

	public void updateFieldLabel(String fieldName, String newLabelName,
			MongoCollection<Document> fieldMappingStructCollection) {
		fieldMappingStructCollection.updateOne(new Document(FIELD_NAME, fieldName),
				new Document("$set", new Document(FIELD_LABEL, newLabelName)));
	}

	public void addRedirectUrlField(MongoCollection<Document> fieldMappingStructCollection) {
		fieldMappingStructCollection.updateMany(
				new Document(FIELD_NAME, new Document("$in", Arrays.asList("uploadDataKPI16", "uploadDataKPI42"))),
				new Document("$set", new Document("redirectUrl", "/dashboard/Config/Upload")));
	}

	private void cycleTimeFieldMappingChanges(MongoCollection<Document> fieldMappingStructCollection) {
		updateFieldLabel(JIRA_ISSUE_TYPE_KPI_171, ISSUE_TYPES_TO_CONSIDER, fieldMappingStructCollection);
		updateTooltipDefinition(JIRA_ISSUE_TYPE_KPI_171, "All issue types considered for Cycle calculation",
				fieldMappingStructCollection);

		updateFieldLabel(STORY_FIRST_STATUS_KPI_171, "First Status when issue is created", fieldMappingStructCollection);
		updateTooltipDefinition(STORY_FIRST_STATUS_KPI_171, "Default first status of all issue types in consideration",
				fieldMappingStructCollection);
		updateFieldDisplayOrder(STORY_FIRST_STATUS_KPI_171, 1, fieldMappingStructCollection);

		updateFieldLabel(JIRA_DOR_KPI_171, "Status to identify DOR", fieldMappingStructCollection);
		updateTooltipDefinition(JIRA_DOR_KPI_171,
				"Workflow status/es that identify that a backlog item is ready to be taken in a sprint based on Definition of Ready (DOR)",
				fieldMappingStructCollection);
		updateFieldDisplayOrder(JIRA_DOR_KPI_171, 2, fieldMappingStructCollection);

		updateFieldLabel(JIRA_DOD_KPI_171, "Status to identify DOD", fieldMappingStructCollection);
		updateTooltipDefinition(JIRA_DOD_KPI_171,
				"Workflow status/es to identify when an issue is delivered based on Definition of Done (DOD)",
				fieldMappingStructCollection);
		updateFieldDisplayOrder(JIRA_DOD_KPI_171, 3, fieldMappingStructCollection);

		updateFieldLabel(JIRA_LIVE_STATUS_KPI_171, "Status to identify Live issues", fieldMappingStructCollection);
		updateTooltipDefinition(JIRA_LIVE_STATUS_KPI_171,
				"Workflow status/es to identify that an issue is live in Production", fieldMappingStructCollection);
		updateFieldDisplayOrder(JIRA_LIVE_STATUS_KPI_171, 4, fieldMappingStructCollection);
	}

	public void updateFieldDisplayOrder(String fieldName, Integer fieldDisplayOrder,
			MongoCollection<Document> fieldMappingStructCollection) {
		fieldMappingStructCollection.updateOne(new Document(FIELD_NAME, fieldName),
				new Document("$set", new Document(FIELD_DISPLAY_ORDER, fieldDisplayOrder)));
	}

	public void updateFieldDisplayOrderForKPI161(MongoCollection<Document> fieldMappingStructCollection) {
		fieldMappingStructCollection.updateOne(
				new Document(FIELD_NAME, new Document("$in", Arrays.asList(JIRA_ISSUE_TYPE_NAMES_KPI_161))),
				new Document("$set", new Document(SECTION_ORDER, 1)));
		fieldMappingStructCollection.updateOne(
				new Document(FIELD_NAME, new Document("$in", Arrays.asList(JIRA_STATUS_FOR_NOT_REFINED_KPI_161,
						JIRA_STATUS_FOR_REFINED_KPI_161, JIRA_STATUS_FOR_IN_PROGRESS_KPI_161))),
				new Document("$set", new Document(SECTION_ORDER, 2)));
	}

	public void rollBackFieldDisplayOrder(String fieldName, Integer fieldDisplayOrder,
			MongoCollection<Document> fieldMappingStructCollection) {
		fieldMappingStructCollection.updateOne(new Document(FIELD_NAME, fieldName),
				new Document(UNSET, new Document(FIELD_DISPLAY_ORDER, fieldDisplayOrder)));
	}

	public void rollBackFieldDisplayOrderForKPI161(MongoCollection<Document> fieldMappingStructCollection) {
		fieldMappingStructCollection.updateOne(
				new Document(FIELD_NAME, new Document("$in", Arrays.asList(JIRA_ISSUE_TYPE_NAMES_KPI_161))),
				new Document(UNSET, new Document(SECTION_ORDER, 1)));
		fieldMappingStructCollection.updateOne(
				new Document(FIELD_NAME, new Document("$in", Arrays.asList(JIRA_STATUS_FOR_NOT_REFINED_KPI_161,
						JIRA_STATUS_FOR_REFINED_KPI_161, JIRA_STATUS_FOR_IN_PROGRESS_KPI_161))),
				new Document(UNSET, new Document(SECTION_ORDER, 2)));
	}

	private Document createFieldMappingStructure(String fieldName, String fieldLabel, String fieldType, String tooltip) {

		return new Document().append(FIELD_NAME, fieldName).append(FIELD_LABEL, fieldLabel).append(FIELD_TYPE, fieldType)
				.append(FIELD_CATEGORY, "workflow").append(SECTION, "WorkFlow Status Mapping")
				.append(TOOL_TIP, new Document(DEFINITION, tooltip));
	}

	private void insertToFieldMappingStructure(MongoCollection<Document> fieldMappingStructCollection,
			List<Document> list) {
		fieldMappingStructCollection.insertMany(list);
	}

	public void updateMandatory(String fieldName, boolean isMandatory,
			MongoCollection<Document> fieldMappingStructCollection, String update) {
		fieldMappingStructCollection.updateOne(new Document(FIELD_NAME, fieldName),
				new Document(update, new Document("mandatory", isMandatory)));
	}
}
