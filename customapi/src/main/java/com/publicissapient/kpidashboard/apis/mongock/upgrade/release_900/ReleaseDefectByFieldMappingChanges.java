package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_900;

import com.mongodb.client.model.Filters;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeUnit(id = "defectBy_field_mapping_changes", order ="9005" , author = "purgupta2", systemVersion = "9.1.0")
public class ReleaseDefectByFieldMappingChanges {

	public static final String FIELD_MAPPING_STRUCTURE = "field_mapping_structure";
	public static final String FIELD_LABEL = "fieldLabel";
	public static final String FIELD_TYPE = "fieldType";
	public static final String FIELD_NAME = "fieldName";
	public static final String TOOL_TIP = "tooltip";
	public static final String DEFINITION = "definition";
	public static final String LABELS = "Labels";
	private static final String FIELD_CATEGORY = "fieldCategory";
	private static final String DOD_STATUS_KPI142 = "jiraDodKPI142";
	private static final String DOD_STATUS_KPI144 = "jiraDodKPI144";

	private final MongoTemplate mongoTemplate;

	public ReleaseDefectByFieldMappingChanges(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		addDODStatusDefectByPriority();
		addDODStatusDefectByStatus();
	}

	private void addDODStatusDefectByStatus() {
		insertFieldMapping("jiraDodKPI142");
	}

	private void addDODStatusDefectByPriority() {
		insertFieldMapping("jiraDodKPI144");
	}

	public void insertFieldMapping(String currentFieldName) {
		Document fieldMappingDocument = new Document().append(FIELD_NAME, currentFieldName)
				.append(FIELD_LABEL, "DOD Status").append(FIELD_TYPE, "chips").append(FIELD_CATEGORY, "workflow")
				.append("section", "WorkFlow Status Mapping").append(TOOL_TIP, new Document(DEFINITION,
						"Status/es that identify that an issue is completed based on Definition of Done (DoD)."));

		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE).insertOne(fieldMappingDocument);
	}

	@RollbackExecution
	public void rollback() {
		deleteRCAAndPriorityFieldMappingRollback();
	}

	private void deleteRCAAndPriorityFieldMappingRollback() {
		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE).deleteMany(
				Filters.or(
						Filters.eq(FIELD_NAME, DOD_STATUS_KPI142),
						Filters.eq(FIELD_NAME, DOD_STATUS_KPI144)
				)
		);
	}
}
