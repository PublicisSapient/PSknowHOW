package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_940;

import java.util.Arrays;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * add release field mapping for burnup kpi
 *
 * @author aksshriv1
 */
@ChangeUnit(id = "release_defectAssignee", order = "9401", author = "aksshriv1", systemVersion = "9.4.0")
public class ReleaseDefectAssigneeEnh {

	private final MongoTemplate mongoTemplate;
	private static final String FIELD_NAME = "fieldName";
	private static final String DEFINITION = "definition";

	public ReleaseDefectAssigneeEnh(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		addFieldToFieldMappingStructure();
	}

	public void addFieldToFieldMappingStructure() {

		Document thresholdValueMapping = new Document(FIELD_NAME, "jiraDodKPI143").append("fieldLabel", "DOD Status")
				.append("fieldType", "chips").append("fieldCategory", "workflow").append("section", "WorkFlow Status Mapping")
				.append("tooltip", new Document(DEFINITION,
						"Status/es that identify that an issue is completed based on Definition of Done (DoD)."));

		mongoTemplate.getCollection("field_mapping_structure").insertOne(thresholdValueMapping);
	}

	@RollbackExecution
	public void rollback() {
		deleteFieldMappingStructure();
	}

	public void deleteFieldMappingStructure() {
		MongoCollection<Document> fieldMappingStructure = mongoTemplate.getCollection("field_mapping_structure");
		fieldMappingStructure.deleteMany(new Document(FIELD_NAME, new Document("$in", Arrays.asList("jiraDodKPI143"))));
	}
}
