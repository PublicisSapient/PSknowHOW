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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_830;

import java.util.Collections;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * add rca exclusion field mapping
 * 
 * @author shi6
 */
@ChangeUnit(id = "dctp_enhnc", order = "8332", author = "shi6", systemVersion = "8.3.3")
public class DCTPEnhnc {
	public static final String ORDER = "order";
	private final MongoTemplate mongoTemplate;
	private static final String DEFINITION = "definition";
	private static final String FIELD_NAME = "fieldName";

	public DCTPEnhnc(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		Document excludeRCA = new Document(FIELD_NAME, "excludeRCAFromKPI163")
				.append("fieldLabel", "Root cause values to be excluded").append("fieldType", "chips")
				.append("section", "Defects Mapping").append("tooltip",
						new Document(DEFINITION, "Root cause reasons for defects which are to be excluded."));

		mongoTemplate.getCollection("field_mapping_structure").insertOne(excludeRCA);
	}

	@RollbackExecution
	public void rollback() {
		MongoCollection<Document> fieldMappingStructure = mongoTemplate.getCollection("field_mapping_structure");
		fieldMappingStructure
				.deleteMany(new Document(FIELD_NAME, new Document("$in", Collections.singletonList("excludeRCAFromKPI163"))));
	}
}
