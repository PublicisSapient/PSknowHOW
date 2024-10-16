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
package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1110;

import java.util.Arrays;

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
	private static final Object CHIPS = "chips";
	private static final String DELIVERED_STATUS = "jiraIterationCompletionStatusKPI138";

	private final MongoTemplate mongoTemplate;

	public FixathonFieldMappingStructure(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		final MongoCollection<Document> fieldMappingStructCollection = mongoTemplate
				.getCollection(FIELD_MAPPING_STRUCTURE);
		updateFieldMappingByFieldName("readyForDevelopmentStatusKPI138",
				"Status to identify issues Ready for Development",
				"Status to identify Ready for development from the backlog.", fieldMappingStructCollection);
		updateFieldMappingByFieldName(DELIVERED_STATUS, "Issue Delivered Status",
				"Status from workflow on which issue is delivered. <br> Example: Closed<hr>",
				fieldMappingStructCollection);
		insertFieldMappingStructure(fieldMappingStructCollection);
		// Rollback Unlinked Work Items field mapping update
		updateFieldLabel("jiraStoryIdentificationKPI129", "Issue types to consider", fieldMappingStructCollection);
		updateFieldLabel("jiraDefectClosedStatusKPI137", "Status to identify Closed Bugs", fieldMappingStructCollection);
		rollbackAddRedirectUrlField(fieldMappingStructCollection);
        updateFieldMappingByFieldName("jiraDefectRejectionStatusKPI151","Status to identify rejected defects", fieldMappingStructCollection);
        updateFieldMappingByFieldName("jiraDefectRejectionStatusKPI155","Ticket Rejected/Dropped Status", fieldMappingStructCollection);
        updateFieldMappingByFieldName("jiraIssueTypeKPI3","Issue types to consider ‘Completed status’","All issue types that should be included in Lead time calculation",fieldMappingStructCollection);
        updateFieldMappingByFieldName("jiraLiveStatusKPI3","Live Status - Lead Time","Workflow status/es to identify that an issue is live in Production",fieldMappingStructCollection);
	}

	public void updateFieldMappingStr(MongoCollection<Document> fieldMappingStructCollection) {
		// Update for jiraIssueDeliverdStatusKPI138
		updateFieldMappingByFieldName("jiraIssueDeliverdStatusKPI138", "Status to identify DOR",
				"Workflow statuses to identify when an issue is considered 'Delivered' based on the Definition of Done (DoD), used to measure average velocity. Please list all statuses that mark an issue as 'Delivered'.",
				fieldMappingStructCollection);

		// Update for readyForDevelopmentStatusKPI138
		updateFieldMappingByFieldName("readyForDevelopmentStatusKPI138", "Status to identify DOD",
				"Workflow status/es that identify that a backlog item is ready to be taken in a sprint based on Definition of Ready (DOR)",
				fieldMappingStructCollection);
	}

	private void updateFieldMappingByFieldName(String fieldName, String fieldLabel, String tooltipDefinition,
			MongoCollection<Document> fieldMappingStructCollection) {

		fieldMappingStructCollection.updateMany(new Document(FIELD_NAME, new Document("$in", Arrays.asList(fieldName))),
				new Document("$set",
						new Document(FIELD_LABEL, fieldLabel).append("tooltip.definition", tooltipDefinition)));
	}

    private void updateFieldMappingByFieldName(String fieldName, String fieldLabel ,
											   MongoCollection<Document> fieldMappingStructCollection) {
		fieldMappingStructCollection.updateMany(
                new Document(FIELD_NAME, new Document("$in", Arrays.asList(fieldName))), new Document("$set",
                        new Document(FIELD_LABEL, fieldLabel)));
    }

	private void deleteFieldMappingStr(MongoCollection<Document> fieldMappingStructCollection) {
		fieldMappingStructCollection.deleteMany(Filters.or(Filters.eq(FIELD_NAME, DELIVERED_STATUS)));
	}

	public void rollbackAddRedirectUrlField(MongoCollection<Document> fieldMappingStructCollection) {
		fieldMappingStructCollection.updateMany(
				new Document(FIELD_NAME, new Document("$in", Arrays.asList("uploadDataKPI16", "uploadDataKPI42"))),
				new Document("$unset", new Document("redirectUrl", "")));
	}

	@RollbackExecution
	public void rollBack() {
		final MongoCollection<Document> fieldMappingStructCollection = mongoTemplate
				.getCollection(FIELD_MAPPING_STRUCTURE);
		updateFieldMappingStr(fieldMappingStructCollection);
		deleteFieldMappingStr(fieldMappingStructCollection);
		// Unlinked Work Items field mapping update
		updateFieldLabel("jiraStoryIdentificationKPI129", "Issue types to consider as Stories",
				fieldMappingStructCollection);
		updateFieldLabel("jiraDefectClosedStatusKPI137", "Status to identify Closed Issues", fieldMappingStructCollection);
		addRedirectUrlField(fieldMappingStructCollection);
        updateFieldMappingByFieldName("jiraDefectRejectionStatusKPI151","Status to identify rejected issues", fieldMappingStructCollection);
        updateFieldMappingByFieldName("jiraDefectRejectionStatusKPI155","Status to identify rejected issues", fieldMappingStructCollection);
        updateFieldMappingByFieldName("jiraIssueTypeKPI3","Issue types to consider","All issue types considered for Lead Time calculation.",fieldMappingStructCollection);
        updateFieldMappingByFieldName("jiraLiveStatusKPI3","Status to identify Live issues","Workflow status/es to identify that an issue is live in Production.",fieldMappingStructCollection);
    }

	public void insertFieldMappingStructure(MongoCollection<Document> fieldMappingStructCollection) {
		Document jiraIterationCompletionStatusKPI138 = new Document().append(FIELD_NAME, DELIVERED_STATUS)
				.append(FIELD_LABEL, "Custom Completion status/es").append(FIELD_TYPE, CHIPS)
				.append(FIELD_CATEGORY, "workflow").append(SECTION, "WorkFlow Status Mapping")
				.append(TOOL_TIP, new Document(DEFINITION,
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

}
