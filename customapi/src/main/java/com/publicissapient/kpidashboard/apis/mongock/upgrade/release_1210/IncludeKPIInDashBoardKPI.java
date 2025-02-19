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
package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1210;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * Include left out KPI in dashboardConfig
 *
 * @author aksshriv1
 */
@ChangeUnit(id = "add_kpi_category_mapping", order = "12107", author = "aksshriv1", systemVersion = "12.1.0")
public class IncludeKPIInDashBoardKPI {

	private final MongoTemplate mongoTemplate;

	public IncludeKPIInDashBoardKPI(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		mongoTemplate.getCollection("kpi_category_mapping").insertOne(new Document().append("kpiId", "kpi149")
				.append("categoryId", "value").append("kpiOrder", 4.0).append("kanban", false));
	}

	@RollbackExecution
	public void rollback() {
		mongoTemplate.getCollection("kpi_category_mapping").deleteOne(new Document("kpiId", "kpi149"));
	}
}
