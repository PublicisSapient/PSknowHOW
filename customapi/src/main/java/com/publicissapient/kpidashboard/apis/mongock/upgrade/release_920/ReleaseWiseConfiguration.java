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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_920;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shi6
 */
@SuppressWarnings("java:S1192")
@ChangeUnit(id = "release_wise_config", order = "9201", author = "shi6", systemVersion = "9.2.0")
public class ReleaseWiseConfiguration {

	private final MongoTemplate mongoTemplate;

	public ReleaseWiseConfiguration(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		MongoCollection<Document> collection = mongoTemplate.getCollection("field_mapping");
		collection.find(new Document("startDateCountKPI150", new Document("$exists", true))).forEach(document -> {
			// Get the integer value from the document
			Integer integerValue = document.getInteger("startDateCountKPI150");

			// Create a Map to hold the integer value
			Map<String, Integer> mapValue = new HashMap<>();
			mapValue.put("", integerValue);

			// Save the updated document back to the collection
			collection.updateOne(new Document("_id", document.getObjectId("_id")),
					new Document("$set", new Document("startDateCountKPI150", mapValue)));
		});

		MongoCollection<Document> mappingStructure = mongoTemplate.getCollection("field_mapping_structure");

		// Define the filter to match documents with fieldName "startDateCountKPI150"
		Document filter = new Document("fieldName", "startDateCountKPI150");

		// Define the update operation to set the "nodeSpecific" field to true
		Document update = new Document("$set", new Document("nodeSpecific", true));

		// Execute the update operation on the collection
		mappingStructure.updateMany(filter, update);

	}

	@RollbackExecution
	public void rollback() {
		MongoCollection<Document> field = mongoTemplate.getCollection("field_mapping_structure");

		// Define the filter to match documents with fieldName "startDateCountKPI150"
		Document filter = new Document("fieldName", "startDateCountKPI150");

		// Define the rollback update operation to unset the "nodeSpecific" field
		Document rollback = new Document("$unset", new Document("nodeSpecific", ""));

		// Execute the rollback update operation on the collection
		field.updateMany(filter, rollback);
	}

}
