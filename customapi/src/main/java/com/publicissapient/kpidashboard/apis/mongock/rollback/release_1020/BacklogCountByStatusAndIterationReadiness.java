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
package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1020;

import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author aksshriv1
 */
@ChangeUnit(id = "r_backlog_itr_cosmetic", order = "010206", author = "aksshriv1", systemVersion = "10.2.0")
public class BacklogCountByStatusAndIterationReadiness {

	private final MongoTemplate mongoTemplate;
	public static final String FIELD_TYPE = "fieldType";
	public static final String FIELD_NAME = "fieldName";
	public static final String JIRA_DEFECT_REJECTION_STATUS_KPI_151 = "jiraDefectRejectionStatusKPI151";
	public static final String TEXT = "text";
	public static final String JIRA_STATUS_FOR_REFINED_KPI_161 = "jiraStatusForRefinedKPI161";
	public static final String FIELD_LABEL = "fieldLabel";
	public static final String STATUS_TO_IDENTIFY_IN_REFINED_ISSUES = "Status to identify In Refined issues";

	public BacklogCountByStatusAndIterationReadiness(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		MongoCollection<Document> fieldMapping = mongoTemplate.getCollection("field_mapping");
		updateFieldMappingBackToString(fieldMapping);
		MongoCollection<Document> fieldMappingStructure = mongoTemplate.getCollection("field_mapping_structure");
		updateFieldMappingstructureBack(fieldMappingStructure);

	}

	@RollbackExecution
	public void rollBack() {
		// no implementation required
	}

	private static void updateFieldMappingBackToString(MongoCollection<Document> fieldMapping) {
		fieldMapping.find(new Document(JIRA_DEFECT_REJECTION_STATUS_KPI_151, new Document("$type", "array")))
				.forEach(doc -> {
					List<String> values = (List<String>) doc.get(JIRA_DEFECT_REJECTION_STATUS_KPI_151);
					// Assuming that the list contains a single string, so we take the first
					// element.
					String value = values != null && !values.isEmpty() ? values.get(0) : null;
					Document updateQuery = new Document("_id", doc.get("_id"));
					Document updateDoc = new Document("$set", new Document(JIRA_DEFECT_REJECTION_STATUS_KPI_151, value));
					fieldMapping.updateOne(updateQuery, updateDoc);
				});
	}

	private static void updateFieldMappingstructureBack(MongoCollection<Document> fieldMappingStructure) {
		Document filter = new Document(FIELD_NAME, JIRA_DEFECT_REJECTION_STATUS_KPI_151);
		Document update = new Document("$set", new Document(FIELD_TYPE, TEXT));
		fieldMappingStructure.updateOne(filter, update);

		Document itr = new Document(FIELD_NAME, JIRA_STATUS_FOR_REFINED_KPI_161);
		Document itrUpdate = new Document("$set", new Document(FIELD_LABEL, STATUS_TO_IDENTIFY_IN_REFINED_ISSUES));
		fieldMappingStructure.updateOne(itr, itrUpdate);
	}

}
