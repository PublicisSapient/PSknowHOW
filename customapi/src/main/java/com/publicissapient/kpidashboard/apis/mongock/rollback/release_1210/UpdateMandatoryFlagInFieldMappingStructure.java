/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1210;

import com.mongodb.client.MongoCollection;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * @author girpatha
 */
@ChangeUnit(id = "r_metadata_identifier_updater_for_templates", order = "012103", author = "girpatha", systemVersion = "12.1.0")
public class UpdateMandatoryFlagInFieldMappingStructure {

	private final MongoTemplate mongoTemplate;
	private static final String FIELD_MAPPING_STRUCTURE = "field_mapping_structure";

	public UpdateMandatoryFlagInFieldMappingStructure(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		MongoCollection<Document> collection = mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE);
		collection.updateMany(new Document("mandatory", false),
				new Document("$set", new Document("mandatory", true)));
	}

	@RollbackExecution
	public void rollback() {
		MongoCollection<Document> collection = mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE);
		collection.updateMany(new Document("mandatory", true),
				new Document("$set", new Document("mandatory", false)));
	}

}
