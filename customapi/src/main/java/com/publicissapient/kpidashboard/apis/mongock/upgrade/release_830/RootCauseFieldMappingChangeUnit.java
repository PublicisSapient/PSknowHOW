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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_830;

import java.util.Arrays;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author kunkambl
 */
@ChangeUnit(id = "root_cause_field_map", order = "8301", author = "kunkambl", systemVersion = "8.3.0")
public class RootCauseFieldMappingChangeUnit {

	public static final String FIELD_MAPPING_STRUCTURE = "field_mapping_structure";
	public static final String FIELD_LABEL = "fieldLabel";
	public static final String FIELD_TYPE = "fieldType";
	public static final String FIELD_NAME = "fieldName";
	public static final String TOOL_TIP = "tooltip";
	public static final String DEFINITION = "definition";
	public static final String LABELS = "Labels";
	public static final String CUSTOM_FIELD = "CustomField";
	private static final String FIELDS = "fields";
	private static final String ROOT_CAUSE = "rootCause";
	private static final String FIELD_CATEGORY = "fieldCategory";
	private static final String ROOT_CAUSE_IDENTIFIER = "rootCauseIdentifier";

	private final MongoTemplate mongoTemplate;

	public RootCauseFieldMappingChangeUnit(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		deleteRCAFieldMapping();
		insertFieldMapping();
		updateRootCause();
	}

	public void deleteRCAFieldMapping() {
		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE).deleteOne(new Document(FIELD_NAME, ROOT_CAUSE));
	}

	public void insertFieldMapping() {
		Document fieldMappingDocument = new Document().append(FIELD_NAME, ROOT_CAUSE_IDENTIFIER)
				.append(FIELD_LABEL, "Root Cause").append(FIELD_TYPE, "radiobutton").append(FIELD_CATEGORY, FIELDS)
				.append("section", "Custom Fields Mapping")
				.append(TOOL_TIP, new Document(DEFINITION,
						"JIRA/AZURE applications let you add custom fields in addition to the built-in fields. Root Cause is a custom field in JIRA. So User need to provide that custom field which is associated with Root Cause in Users JIRA Installation."))
				.append("nestedFields",
						Arrays.asList(
								new Document().append(FIELD_NAME, ROOT_CAUSE).append(FIELD_LABEL, "Root Cause CustomField")
										.append(FIELD_TYPE, "text").append(FIELD_CATEGORY, FIELDS)
										.append("filterGroup", Arrays.asList(CUSTOM_FIELD))
										.append(TOOL_TIP, new Document(DEFINITION, " Provide customfield name to identify Root Cause.")),
								new Document().append(FIELD_NAME, "rootCauseValues").append(FIELD_LABEL, "Root Cause Defect Values")
										.append(FIELD_TYPE, "chips").append("filterGroup", Arrays.asList(LABELS))
										.append(TOOL_TIP, new Document(DEFINITION, "Provide label name to identify Root Cause."))))
				.append("options", Arrays.asList(new Document().append("label", CUSTOM_FIELD).append("value", CUSTOM_FIELD),
						new Document().append("label", LABELS).append("value", LABELS)));

		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE).insertOne(fieldMappingDocument);
	}

	public void updateRootCause() {
		Document query = new Document(ROOT_CAUSE, new Document("$ne", ""));
		Document update = new Document("$set", new Document(ROOT_CAUSE_IDENTIFIER, CUSTOM_FIELD));
		mongoTemplate.getCollection("field_mapping").updateMany(query, update);
	}

	@RollbackExecution
	public void rollback() {
		deleteRCAFieldMappingRollback();
		insertFieldMappingRollback();
		updateRootCauseRollback();
	}

	public void deleteRCAFieldMappingRollback() {
		Document rootCauseDocument = new Document().append(FIELD_NAME, ROOT_CAUSE).append(FIELD_LABEL, "Root Cause")
				.append(FIELD_TYPE, "text").append(FIELD_CATEGORY, FIELDS).append("section", "Custom Fields Mapping")
				.append(TOOL_TIP, new Document(DEFINITION,
						"JIRA/AZURE applications let you add custom fields in addition to the built-in fields. Root Cause is a custom field in JIRA. So User need to provide that custom field which is associated with Root Cause in Users JIRA Installation."));
		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE).insertOne(rootCauseDocument);
	}

	public void insertFieldMappingRollback() {
		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE).deleteOne(new Document(FIELD_NAME, ROOT_CAUSE_IDENTIFIER));
	}

	public void updateRootCauseRollback() {
		Document query = new Document(ROOT_CAUSE_IDENTIFIER, new Document("$ne", ""));
		Document update = new Document("$set", new Document(ROOT_CAUSE, ""));
		mongoTemplate.getCollection("field_mapping").updateMany(query, update);
	}
}
