/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1010;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shunaray
 */
@ChangeUnit(id = "r_issue_count_fm", order = "010106", author = "shunaray", systemVersion = "10.1.0")
public class IssueCountEnhc {

	public static final String JIRA_STORY_CATEGORY_KPI_40 = "jiraStoryCategoryKpi40";
	private final MongoTemplate mongoTemplate;

	public IssueCountEnhc(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		rollbackInsertFieldMappingStructure();
		rollbackInsertFieldMappingVal();
		rollbackMetadataIdentifier();
	}

	public void rollbackInsertFieldMappingStructure() {
		// Define the query to find documents with the specific fieldName
		Document query = new Document("fieldName", JIRA_STORY_CATEGORY_KPI_40);

		// Execute the delete operation
		mongoTemplate.getCollection("field_mapping_structure").deleteMany(query);
	}

	public void rollbackInsertFieldMappingVal() {
		// Define the update operation to remove the field
		var update = new Document();
		update.append("$unset", new Document(JIRA_STORY_CATEGORY_KPI_40, ""));

		// Execute the update to remove the field from all documents
		mongoTemplate.getCollection("field_mapping").updateMany(new Document(), update);
	}

	public void rollbackMetadataIdentifier() {
		mongoTemplate.getCollection("metadata_identifier").updateMany(
				new Document("templateCode", new Document("$in", Arrays.asList("7"))),
				new Document("$pull", new Document("issues", new Document("type", JIRA_STORY_CATEGORY_KPI_40))));
	}

	@RollbackExecution
	public void rollback() {
		insertFieldMappingVal();
		insertFieldMappingStructure();
		updateMetadataIdentifier();
	}

	public void insertFieldMappingStructure() {
		Document newFieldMapping = new Document().append("fieldName", JIRA_STORY_CATEGORY_KPI_40)
				.append("fieldLabel", "Issue type to identify Story category").append("fieldType", "chips")
				.append("fieldCategory", "Issue_Type").append("section", "Issue Types Mapping")
				.append("tooltip", new Document("definition", "All issue types that are used as/equivalent to Story."))
				.append("mandatory", true);

		mongoTemplate.getCollection("field_mapping_structure").insertOne(newFieldMapping);
	}

	public void insertFieldMappingVal() {
		var fieldMapping = mongoTemplate.getCollection("field_mapping");

		// Define the new values
		var newValues = new Document();
		newValues.append(JIRA_STORY_CATEGORY_KPI_40, List.of("Story", "User Story", "Enabler Story", "Feature"));

		// Define the update operation
		var update = new Document();
		update.append("$set", newValues);

		// Execute the update
		fieldMapping.updateMany(new Document(), update);
	}

	public void updateMetadataIdentifier() {
		mongoTemplate.getCollection("metadata_identifier").updateMany(
				new Document("templateCode", new Document("$in", Arrays.asList("7"))),
				new Document("$push", new Document("issues", new Document("type", JIRA_STORY_CATEGORY_KPI_40).append("value",
						Arrays.asList("Story", "User Story", "Enabler Story", "Feature")))));
	}
}
