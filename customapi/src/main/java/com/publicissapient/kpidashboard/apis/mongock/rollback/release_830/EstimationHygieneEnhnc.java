package com.publicissapient.kpidashboard.apis.mongock.rollback.release_830;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * remove build frequency kpi and field mapping
 *
 * @author aksshriv1
 */
@ChangeUnit(id = "r_est_hyg_enh", order = "08341", author = "aksshriv1", systemVersion = "8.3.4")
public class EstimationHygieneEnhnc {

	public static final String FIELD_LABEL = "fieldLabel";
	public static final String ISSUE_TYPE_TO_BE_INCLUDED = "Issue type to be included";
	public static final String ISSUE_TYPES_TO_CONSIDER_COMPLETED_STATUS = "Issue types to consider ‘Completed status’";
	public static final String FIELD_MAPPING_STRUCTURE = "field_mapping_structure";
	private final MongoTemplate mongoTemplate;

	public EstimationHygieneEnhnc(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		rollbackFieldMappingStructure();
	}

	public void rollbackFieldMappingStructure() {
		MongoCollection<Document> fieldMappingCollection = mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE);

		fieldMappingCollection.updateMany(new Document(FIELD_LABEL, ISSUE_TYPES_TO_CONSIDER_COMPLETED_STATUS),
				new Document("$set", new Document(FIELD_LABEL, ISSUE_TYPE_TO_BE_INCLUDED)));
	}

	@RollbackExecution
	public void rollback() {
		// no implementation required
	}
}
