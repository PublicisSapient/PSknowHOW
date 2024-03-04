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
