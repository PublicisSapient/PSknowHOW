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
package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1110;

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
@ChangeUnit(id = "remove_mandatory", order = "11104", author = "shi6", systemVersion = "11.1.0")
public class RemoveMandatoryUpgrade {

	public static final String FIELD_MAPPING_STRUCTURE = "field_mapping_structure";
	public static final String FIELD_NAME = "fieldName";
	private static final List<String> FIELD_NAME_LIST = Arrays.asList("jiraSubTaskIdentification",
			"jiraSubTaskDefectType");

	private final MongoTemplate mongoTemplate;

	public RemoveMandatoryUpgrade(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		updateMandatoryFields(false);
	}

	@RollbackExecution
	public void rollBack() {
		updateMandatoryFields(true);
	}

	private void updateMandatoryFields(boolean isMandatory) {
		final MongoCollection<Document> fieldMappingStructCollection = mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE);
		Document updateDocument = new Document("$set", new Document("mandatory", isMandatory));
		for (String fieldName : FIELD_NAME_LIST) {
			fieldMappingStructCollection.updateOne(new Document(FIELD_NAME, fieldName), updateDocument);
		}
	}
}
