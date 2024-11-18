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
package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_810;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.WriteModel;

import io.mongock.api.annotations.BeforeExecution;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackBeforeExecution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shi6
 */
@SuppressWarnings("java:S1192")
@ChangeUnit(id = "dsv_screen2", order = "8108", author = "shi6", systemVersion = "8.1.0")
public class DSVScreen2 {

	private final MongoTemplate mongoTemplate;
	private MongoCollection<Document> fieldMappingStructure;

	public DSVScreen2(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@BeforeExecution
	public void beforeExecution() {
		fieldMappingStructure = mongoTemplate.getCollection("field_mapping_structure");
	}

	@Execution
	public boolean execution() {
		insertFieldMapping();
		updateMetadata();
		return true;
	}

	public void insertFieldMapping() {
		fieldMappingStructure.insertMany(Arrays.asList(
				createDocument("jiraStatusStartDevelopmentKPI154", "Status to identify start of development",
						"workflow", "WorkFlow Status Mapping",
						"Status from workflow on which issue is started development. <br> Example: In Analysis<hr>"),
				createDocument("jiraDevDoneStatusKPI154", "Status to identify Dev completed issues", "workflow",
						"WorkFlow Status Mapping",
						"Status that confirms that the development work is completed and an issue can be passed on for testing"),
				createDocument("jiraQADoneStatusKPI154", "Status to identify QA completed issues", "workflow",
						"WorkFlow Status Mapping",
						"Status that confirms that the QA work is completed and an issue can be ready for signoff/close"),
				createDocument("jiraIterationCompletionStatusKPI154", "Status to identify completed issues", "workflow",
						"WorkFlow Status Mapping",
						"All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"),
				createDocument("jiraStatusForInProgressKPI154", "Status to identify In Progress issues", "workflow",
						"WorkFlow Status Mapping",
						"All statuses that issues have moved from the Created status and also has not been completed. <br> This field is same as the configuration field of Work Remaining KPI)")
								.append("readOnly", true),
				createDocument("jiraSubTaskIdentification", "Sub-Task Issue Types", "Issue_Type", "Issue Types Mapping",
						"Any issue type mentioned will be considered as sub-task linked with story"),
				createDocument("storyFirstStatusKPI154", "Status when 'Story' issue type is created", "workflow",
						"WorkFlow Status Mapping", "All issue types that identify with a Story."),
				createDocument("jiraOnHoldStatusKPI154", "Status when issue type is put on Hold", "workflow",
						"WorkFlow Status Mapping", "All status that identify hold/blocked statuses.")));

	}

	private void updateMetadata() {
		// Initialize a list to store the bulk write operations
		List<WriteModel<Document>> metaDataOperations = new ArrayList<>();

		// Add the updateMany operations to the list
		metaDataOperations
				.add(new UpdateManyModel<>(Filters.or(Filters.eq("templateCode", "8"), Filters.eq("tool", "Azure")),
						Updates.addToSet("workflow", new Document("type", "firstDevstatus").append("value",
								Arrays.asList("In Analysis", "IN ANALYSIS", "In Development", "In Progress")))));

		metaDataOperations.add(new UpdateManyModel<>(Filters.eq("templateCode", "7"),
				Updates.addToSet("workflow", new Document("type", "jiraStatusForInProgressKPI154").append("value",
						Arrays.asList("In Analysis", "In Development", "In Progress")))));

		metaDataOperations.add(new UpdateManyModel<>(Filters.eq("templateCode", "7"),
				Updates.addToSet("workflow", new Document("type", "jiraStatusStartDevelopmentKPI154").append("value",
						Arrays.asList("In Analysis", "IN ANALYSIS", "In Development", "In Progress")))));

		metaDataOperations.add(new UpdateManyModel<>(Filters.eq("templateCode", "7"), Updates.addToSet("workflow",
				new Document("type", "storyFirstStatusKPI154").append("value", Collections.singletonList("Open")))));

		// Get the collection
		mongoTemplate.getCollection("metadata_identifier").bulkWrite(metaDataOperations,
				new BulkWriteOptions().ordered(false));

	}

	private Document createDocument(String fieldName, String fieldLabel, String fieldCategory, String section,
			String definition) {
		return new Document("fieldName", fieldName).append("fieldLabel", fieldLabel).append("fieldType", "chips")
				.append("section", section).append("fieldCategory", fieldCategory)
				.append("tooltip", new Document("definition", definition));

	}

	@RollbackExecution
	public void rollback() {
		rollBackMetaAndFieldMappingStructure();
	}

	public void rollBackMetaAndFieldMappingStructure() {

		List<String> fieldNamesToDelete = Arrays.asList("jiraStatusStartDevelopmentKPI154", "jiraDevDoneStatusKPI154",
				"jiraQADoneStatusKPI154", "jiraIterationCompletionStatusKPI154", "jiraStatusForInProgressKPI154",
				"jiraSubTaskIdentification", "storyFirstStatusKPI154", "jiraOnHoldStatusKPI154" );
		Document filter = new Document("fieldName", new Document("$in", fieldNamesToDelete));
		// Delete documents that match the filter
		fieldMappingStructure.deleteMany(filter);

		MongoCollection<Document> collection = mongoTemplate.getCollection("metadata_identifier");

		// Create the filter
		Document metaFilter = new Document("$or", Arrays.asList(new Document("templateCode", "8"),
				new Document("tool", "Azure"), new Document("templateCode", "7")));

		// Create the update operation
		Document update = new Document("$pull",
				new Document("workflow",
						new Document("$in",
								Arrays.asList(new Document("type", "firstDevstatus"),
										new Document("type", "jiraStatusForInProgressKPI154"),
										new Document("type", "jiraStatusStartDevelopmentKPI154"),
										new Document("type", "storyFirstStatusKPI154")))));

		// Update the documents
		collection.updateMany(metaFilter, update);
	}

	@RollbackBeforeExecution
	public void rollbackBeforeExecution() {
		// do not rquire the implementation
	}
}
