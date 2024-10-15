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
package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1110;

import com.mongodb.client.model.Filters;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Arrays;

/**
 * @author purgupta2
 */
@ChangeUnit(id = "fixathon_field_mapping", order = "11102", author = "purgupta2", systemVersion = "11.1.0")
public class FixathonFieldMappingStructure {

	public static final String FIELD_MAPPING_STRUCTURE = "field_mapping_structure";
	public static final String FIELD_LABEL = "fieldLabel";
	public static final String FIELD_TYPE = "fieldType";
	public static final String FIELD_NAME = "fieldName";
	public static final String TOOL_TIP = "tooltip";
	public static final String DEFINITION = "definition";
	public static final String LABEL = "label";
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
		updateFieldMappingStr();
		deleteFieldMappingStr();
	}

	public void updateFieldMappingStr() {
		// Update for jiraIssueDeliverdStatusKPI138
		updateFieldMappingByFieldName("jiraIssueDeliverdStatusKPI138", "Status to identify DOR",
				"Workflow statuses to identify when an issue is considered 'Delivered' based on the Definition of Done (DoD), used to measure average velocity. Please list all statuses that mark an issue as 'Delivered'.");

		// Update for readyForDevelopmentStatusKPI138
		updateFieldMappingByFieldName("readyForDevelopmentStatusKPI138", "Status to identify DOD",
				"Workflow status/es that identify that a backlog item is ready to be taken in a sprint based on Definition of Ready (DOR)");
	}

	private void updateFieldMappingByFieldName(String fieldName, String fieldLabel, String tooltipDefinition) {
		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE).updateMany(
				new Document(FIELD_NAME, new Document("$in", Arrays.asList(fieldName))), new Document("$set",
						new Document(FIELD_LABEL, fieldLabel).append("tooltip.definition", tooltipDefinition)));
	}

	private void deleteFieldMappingStr() {
		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE)
				.deleteMany(Filters.or(Filters.eq(FIELD_NAME, DELIVERED_STATUS)));
	}

	@RollbackExecution
	public void rollBack() {
		updateFieldMappingByFieldName("readyForDevelopmentStatusKPI138",
				"Status to identify issues Ready for Development",
				"Status to identify Ready for development from the backlog.");
		updateFieldMappingByFieldName(DELIVERED_STATUS, "Issue Delivered Status",
				"Status from workflow on which issue is delivered. <br> Example: Closed<hr>");
		insertFieldMappingStructure();
	}

	public void insertFieldMappingStructure() {
		Document jiraIterationCompletionStatusKPI138 = new Document().append(FIELD_NAME, DELIVERED_STATUS)
				.append(FIELD_LABEL, "Custom Completion status/es").append(FIELD_TYPE, CHIPS)
				.append(FIELD_CATEGORY, "workflow").append(SECTION, "WorkFlow Status Mapping")
				.append(TOOL_TIP, new Document(DEFINITION,
						"All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"));

		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE)
				.insertMany(Arrays.asList(jiraIterationCompletionStatusKPI138));
	}

}
