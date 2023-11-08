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
package com.publicissapient.kpidashboard.apis.mongock.rollback.release_810;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shi6
 */
@ChangeUnit(id = "r_dsv_screen2", order = "08108", author = "shi6", systemVersion = "8.1.0")
public class DSVScreen2 {

	private final MongoTemplate mongoTemplate;

	public DSVScreen2(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public boolean execution() {
		insertFieldMapping();
		return true;
	}

	public void insertFieldMapping() {
		MongoCollection<Document> fieldMappingStructure = mongoTemplate.getCollection("field_mapping_structure");
		List<String> fieldNamesToDelete = Arrays.asList("jiraStatusStartDevelopmentKPI154", "jiraDevDoneStatusKPI154",
				"jiraQADoneStatusKPI154", "jiraIterationCompletionStatusKPI154", "jiraStatusForInProgressKPI154",
				"jiraSubTaskIdentification", "storyFirstStatusKPI154", "jiraOnHoldStatusKPI154");
		Document filter = new Document("fieldName", new Document("$in", fieldNamesToDelete));
		// Delete documents that match the filter
		fieldMappingStructure.deleteMany(filter);

		MongoCollection<Document> identifier = mongoTemplate.getCollection("metadata_identifier");

		Document indentifierFilter = new Document("$or", Arrays.asList(new Document("templateCode", "8"),
				new Document("tool", "Azure"), new Document("templateCode", "7")));

		// Define the update operation to remove elements from the "workflow" array
		Document update = new Document("$pull",
				new Document("workflow",
						new Document("type",
								new Document("$in", Arrays.asList("firstDevstatus", "jiraStatusForInProgressKPI154",
										"jiraStatusStartDevelopmentKPI154", "storyFirstStatusKPI154")))));

		// Perform the update operation
		identifier.updateMany(indentifierFilter, update);

	}

	@RollbackExecution
	public void rollback() {
		// not required
	}

}
