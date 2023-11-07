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

import java.util.Collections;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author eswbogol
 */
@ChangeUnit(id = "scope_churn_filters", order = "8106", author = "eswbogol", systemVersion = "8.1.0")
public class ScopeChurnChangeUnit {

	private final MongoTemplate mongoTemplate;

	public ScopeChurnChangeUnit(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		scopeChurnFilter();
	}

	public void scopeChurnFilter() {
		mongoTemplate.getCollection("kpi_master").updateOne(
				new Document("kpiId", new Document("$in", Collections.singletonList("kpi164"))),
				new Document("$set", new Document("kpiFilter", "radioButton")));
	}

	@RollbackExecution
	public void rollback() {
		// the rollback methods should be placed here
	}
}
