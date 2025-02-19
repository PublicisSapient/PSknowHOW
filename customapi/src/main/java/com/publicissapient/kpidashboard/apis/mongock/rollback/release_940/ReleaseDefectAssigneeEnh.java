package com.publicissapient.kpidashboard.apis.mongock.rollback.release_940;

import java.util.Collections;

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
@ChangeUnit(id = "r_release_defectAssignee", order = "09401", author = "aksshriv1", systemVersion = "9.4.0")
public class ReleaseDefectAssigneeEnh {

	private final MongoTemplate mongoTemplate;
	private static final String FIELD_NAME = "fieldName";

	public ReleaseDefectAssigneeEnh(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		MongoCollection<Document> fieldMappingStructure = mongoTemplate.getCollection("field_mapping_structure");
		fieldMappingStructure
				.deleteOne(new Document(FIELD_NAME, new Document("$in", Collections.singletonList("jiraDodKPI143"))));
	}

	@RollbackExecution
	public void rollback() {
		// no implementation required
	}
}
