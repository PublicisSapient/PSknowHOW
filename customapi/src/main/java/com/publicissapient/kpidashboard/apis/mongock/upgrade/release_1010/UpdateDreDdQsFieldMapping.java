/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *    http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1010;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.model.Filters;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author purgupta2
 */
@ChangeUnit(id = "dre_dd_qs_fieldmapping", order = "10105", author = "purgupta2", systemVersion = "10.1.0")
public class UpdateDreDdQsFieldMapping {

	public static final String FIELD_MAPPING_STRUCTURE = "field_mapping_structure";
	public static final String FIELD_LABEL = "fieldLabel";
	public static final String FIELD_LABEL_DEF = "Labels to filter issues in consideration";
	public static final String FIELD_TYPE = "fieldType";
	public static final String FIELD_NAME = "fieldName";
	public static final String TOOL_TIP = "tooltip";
	public static final String TOOL_TIP_DEF = "Only issues with specified labels will be considered";
	public static final String DEFINITION = "definition";
	public static final String LABEL = "label";
	public static final String VALUE = "value";
	private static final String FIELD_CATEGORY = "fieldCategory";
	private static final String SECTION = "section";
	private static final String SECTION_DEF = "Issue Types Mapping";
	private static final Object CHIPS = "chips";
	private static final String RESOLUTION_TYPE = "resolutionTypeForRejectionKPI34";
	private static final String REJECTION_STATUS = "jiraDefectRejectionStatusKPI34";

	private final MongoTemplate mongoTemplate;

	public UpdateDreDdQsFieldMapping(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		updateFieldMappingStructure();
		insertFieldMappingVal();
	}

	public void updateFieldMappingStructure() {

		// added fieldmapping in DD
		Document jiraLabelsQAKPI111 = new Document().append(FIELD_NAME, "jiraLabelsQAKPI111")
				.append(FIELD_LABEL, FIELD_LABEL_DEF).append(FIELD_TYPE, CHIPS).append(SECTION, SECTION_DEF)
				.append(TOOL_TIP, new Document(DEFINITION, TOOL_TIP_DEF));

		// added fieldmapping in QS
		Document jiraLabelsKPI133 = new Document().append(FIELD_NAME, "jiraLabelsKPI133")
				.append(FIELD_LABEL, FIELD_LABEL_DEF).append(FIELD_TYPE, CHIPS).append(SECTION, SECTION_DEF)
				.append(TOOL_TIP, new Document(DEFINITION, TOOL_TIP_DEF));

		// added fieldmapping in DRE
		Document resolutionTypeForRejectionKPI34 = new Document().append(FIELD_NAME, RESOLUTION_TYPE)
				.append(FIELD_LABEL, "Resolution type to be excluded").append(FIELD_TYPE, CHIPS).append(SECTION, SECTION_DEF)
				.append(TOOL_TIP, new Document(DEFINITION,
						"Resolution types for defects that can be excluded from 'Defect Removal Efficiency' calculation"));

		Document includeRCAForKPI34 = new Document().append(FIELD_NAME, "includeRCAForKPI34")
				.append(FIELD_LABEL, "Root cause values to be included").append(FIELD_TYPE, CHIPS)
				.append(SECTION, "Defects Mapping").append(TOOL_TIP, new Document(DEFINITION,
						"Root cause reasons for defects to be included In 'Defect Removal Efficiency' calculation"));

		Document defectPriorityKPI34 = new Document().append(FIELD_NAME, "defectPriorityKPI34")
				.append(FIELD_LABEL, "Priority to be excluded").append(FIELD_TYPE, "multiselect")
				.append(SECTION, "Defects Mapping")
				.append(TOOL_TIP,
						new Document(DEFINITION,
								"Priority values of defects that can be excluded from 'Defect Removal Efficiency' calculation"))
				.append("options",
						Arrays.asList(new Document(LABEL, "p1").append(VALUE, "p1"), new Document(LABEL, "p2").append(VALUE, "p2"),
								new Document(LABEL, "p3").append(VALUE, "p3"), new Document(LABEL, "p4").append(VALUE, "p4"),
								new Document(LABEL, "p5").append(VALUE, "p5")));

		Document jiraDefectRejectionStatusKPI34 = new Document().append(FIELD_NAME, REJECTION_STATUS)
				.append(FIELD_LABEL, "Status to identify rejected defects").append(FIELD_TYPE, "text")
				.append(FIELD_CATEGORY, "workflow").append(SECTION, SECTION_DEF)
				.append(TOOL_TIP, new Document(DEFINITION, "All workflow statuses used to reject defects"));

		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE).insertMany(Arrays.asList(jiraLabelsQAKPI111, jiraLabelsKPI133,
				includeRCAForKPI34, defectPriorityKPI34, jiraDefectRejectionStatusKPI34, resolutionTypeForRejectionKPI34));
	}

	public void insertFieldMappingVal() {
		var fieldMapping = mongoTemplate.getCollection("field_mapping");

		// Define the new values
		var newValues = new Document();
		newValues.append(REJECTION_STATUS, "Rejected");
		newValues.append(RESOLUTION_TYPE, List.of("Invalid", "Duplicate", "Unrequired", "Cannot Reproduce", "Won't Fix"));

		// Define the update operation
		var update = new Document();
		update.append("$set", newValues);

		// Execute the update
		fieldMapping.updateMany(new Document(), update);
	}

	@RollbackExecution
	public void rollback() {
		deleteFieldMappingRollback();
		rollbackInsertFieldMappingVal();
	}

	private void deleteFieldMappingRollback() {
		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE)
				.deleteMany(Filters.or(Filters.eq(FIELD_NAME, "jiraLabelsQAKPI111"), Filters.eq(FIELD_NAME, "jiraLabelsKPI133"),
						Filters.eq(FIELD_NAME, "includeRCAForKPI34"), Filters.eq(FIELD_NAME, "defectPriorityKPI34"),
						Filters.eq(FIELD_NAME, REJECTION_STATUS), Filters.eq(FIELD_NAME, RESOLUTION_TYPE)));
	}

	public void rollbackInsertFieldMappingVal() {
		// Define the update operation to remove the field
		var update = new Document();
		update.append("$unset", new Document(REJECTION_STATUS, ""));
		update.append("$unset", new Document(RESOLUTION_TYPE, ""));

		// Execute the update to remove the field from all documents
		mongoTemplate.getCollection("field_mapping").updateMany(new Document(), update);
	}
}
