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
package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1020;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shunaray
 */
@ChangeUnit(id = "remove_value_additional_filters", order = "10207", author = "shunaray", systemVersion = "10.2.0")
public class RemoveAdditionalFiltersChangeUnit {
	private final MongoTemplate mongoTemplate;

	public RemoveAdditionalFiltersChangeUnit(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		mongoTemplate.getCollection("filters").updateMany(new Document("boardId", 4),
				new Document("$unset", new Document("additionalFilters", "")));
	}

	@RollbackExecution
	public void rollback() {
		// not required as value dashboard don't need additionalFilter of sqd,sprint
	}
}
