package com.publicissapient.kpidashboard.apis.mongock.rollback.release_830;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

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
		deleteRCAFieldMappingRollback();
		insertFieldMappingRollback();
		updateRootCauseRollback();
	}

	public void deleteRCAFieldMapping() {
		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE).deleteOne(new Document(FIELD_NAME, ROOT_CAUSE));
	}

	public void insertFieldMapping() {
		Document fieldMappingDocument = new Document().append(FIELD_NAME, ROOT_CAUSE_IDENTIFIER)
				.append(FIELD_LABEL, "Root Cause").append(FIELD_TYPE, "radiobutton").append(FIELD_CATEGORY, FIELDS)
				.append("section", "Custom Fields Mapping").append(TOOL_TIP, new Document(DEFINITION,
						"JIRA/AZURE applications let you add custom fields in addition to the built-in fields. Root Cause is a custom field in JIRA. So User need to provide that custom field which is associated with Root Cause in Users JIRA Installation."))
				.append("nestedFields", new Document[] {
						new Document().append(FIELD_NAME, ROOT_CAUSE).append(FIELD_LABEL, "Root Cause CustomField")
								.append(FIELD_TYPE, "text").append(FIELD_CATEGORY, FIELDS)
								.append("filterGroup", new String[] { CUSTOM_FIELD }).append(TOOL_TIP,
								new Document(DEFINITION,
										" Provide customfield name to Root Cause.")),
						new Document().append(FIELD_NAME, "rootCauseValues")
								.append(FIELD_LABEL, "Root Cause Defect Values").append(FIELD_TYPE, "chips")
								.append("filterGroup", new String[] { LABELS }).append(TOOL_TIP,
								new Document(DEFINITION, "Provide label name to identify Root Cause")) })
				.append("options",
						new Document[] { new Document().append("label", CUSTOM_FIELD).append("value", CUSTOM_FIELD),
								new Document().append("label", LABELS).append("value", LABELS) });

		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE).insertOne(fieldMappingDocument);
	}

	public void updateRootCause() {
		Document query = new Document(ROOT_CAUSE, new Document("$ne", ""));
		Document update = new Document("$set", new Document(ROOT_CAUSE_IDENTIFIER, CUSTOM_FIELD));
		mongoTemplate.getCollection("field_mapping").updateMany(query, update);
	}

	@RollbackExecution
	public void rollback() {
		deleteRCAFieldMapping();
		insertFieldMapping();
		updateRootCause();
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
