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
package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1110;

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
@ChangeUnit(id = "r_remove_madatory", order = "011104", author = "shi6", systemVersion = "11.1.0")
public class RemoveMandatory {

	public static final String FIELD_MAPPING_STRUCTURE = "field_mapping_structure";
	public static final String FIELD_NAME = "fieldName";
	private static final List<String> FIELD_NAME_LIST = Arrays.asList("jiraSubTaskIdentification",
			"jiraSubTaskDefectType");

	private final MongoTemplate mongoTemplate;

	public RemoveMandatory(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		updateMandatoryFields(true);
	}

	@RollbackExecution
	public void rollBack() {
		updateMandatoryFields(false);
	}

	private void updateMandatoryFields(boolean isMandatory) {
		final MongoCollection<Document> fieldMappingStructCollection = mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE);
		updateMandatory(FIELD_NAME_LIST, isMandatory, fieldMappingStructCollection, "$set");
	}

	private void updateMandatory(List<String> fieldNameList, boolean isMandatory,
			MongoCollection<Document> fieldMappingStructCollection, String update) {
		Document updateDocument = new Document(update, new Document("mandatory", isMandatory));
		for (String fieldName : fieldNameList) {
			fieldMappingStructCollection.updateOne(new Document(FIELD_NAME, fieldName), updateDocument);
		}
	}
}
