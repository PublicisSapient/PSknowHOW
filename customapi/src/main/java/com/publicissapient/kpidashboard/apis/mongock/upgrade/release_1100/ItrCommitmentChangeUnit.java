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
package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1100;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shunaray
 */
@ChangeUnit(id = "itr_commit_enhc", order = "11000", author = "shunaray", systemVersion = "11.0.0")
public class ItrCommitmentChangeUnit {

	private final MongoTemplate mongoTemplate;

	public ItrCommitmentChangeUnit(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		Document document = new Document().append("fieldName", "jiraLabelsKPI120")
				.append("fieldLabel", "Labels for Scope Change Identification").append("fieldType", "chips")
				.append("section", "WorkFlow Status Mapping").append("tooltip",
						new Document("definition", "Specify labels to detect and track scope changes effectively"));

		mongoTemplate.getCollection("field_mapping_structure").insertOne(document);

	}

	@RollbackExecution
	public void rollback() {
		mongoTemplate.getCollection("field_mapping_structure").deleteOne(new Document("fieldName", "jiraLabelsKPI120"));

	}

}
