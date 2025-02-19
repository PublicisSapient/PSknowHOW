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
package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1010;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shi6
 */
@Slf4j
@ChangeUnit(id = "update_estimation_nested_field", order = "10103", author = "shi6", systemVersion = "10.1.0")
public class UpdateEstimationNestedField {

	private static final String FIELD_NAME = "fieldName";
	private static final String ESTIMATION_CRITERIA = "estimationCriteria";
	private static final String NESTED_FIELD = "nestedFields";
	private final MongoTemplate mongoTemplate;

	public UpdateEstimationNestedField(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public boolean changeSet() {

		MongoCollection<Document> collection = mongoTemplate.getCollection("field_mapping_structure");

		// Find the document with the specified fieldName
		Document document = collection.find(Filters.eq(FIELD_NAME, ESTIMATION_CRITERIA)).first();

		if (document != null) {
			List<Document> nestedFields = (List<Document>) document.get(NESTED_FIELD);

			for (Document nestedField : nestedFields) {
				if ("storyPointToHourMapping".equals(nestedField.getString(FIELD_NAME))) {
					nestedField.put("fieldLabel", "Hour to Story Point");
					nestedField.put("filterGroup", Arrays.asList("Story Point", "Actual Estimation"));
					nestedField.put("tooltip", new Document("definition",
							"Estimation technique used by teams. Eg., enter 8 if 1 Story Point is equivalent to 8hrs of effort spent per day."));
				}
			}

			// Update the document with the modified nested fields
			collection.updateOne(Filters.eq(FIELD_NAME, ESTIMATION_CRITERIA), Updates.set(NESTED_FIELD, nestedFields));
		}

		return true;
	}

	@RollbackExecution
	public void rollback() {
		MongoCollection<Document> collection = mongoTemplate.getCollection("field_mapping_structure");

		// Find the document with the specified fieldName
		Document document = collection.find(Filters.eq(FIELD_NAME, ESTIMATION_CRITERIA)).first();

		if (document != null) {
			List<Document> nestedFields = (List<Document>) document.get(NESTED_FIELD);

			for (Document nestedField : nestedFields) {
				if ("storyPointToHourMapping".equals(nestedField.getString(FIELD_NAME))) {
					nestedField.put("fieldLabel", "Story Point to Hour Conversion");
					nestedField.put("filterGroup", List.of("Story Point"));
					nestedField.put("tooltip",
							new Document("definition", "Estimation technique used by teams for e.g. story points, Hours etc."));
				}
			}

			// Update the document with the modified nested fields
			collection.updateOne(Filters.eq(FIELD_NAME, ESTIMATION_CRITERIA), Updates.set(NESTED_FIELD, nestedFields));
		}
	}
}
