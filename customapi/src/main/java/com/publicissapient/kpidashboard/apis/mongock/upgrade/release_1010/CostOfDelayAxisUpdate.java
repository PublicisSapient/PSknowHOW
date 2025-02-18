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

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * updated kpi name and y-axis label
 *
 * @author aksshriv1
 */
@ChangeUnit(id = "cod_y_axislabel", order = "101013", author = "aksshriv1", systemVersion = "10.1.0")
public class CostOfDelayAxisUpdate {

	private final MongoTemplate mongoTemplate;

	public CostOfDelayAxisUpdate(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		updatekpi113();
	}

	public void updatekpi113() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection("kpi_master");
		Document filter = new Document("kpiId", "kpi113");

		Document update = new Document("$set",
				new Document("kpiName", "Value Delivery (Cost of Delay)").append("yAxisLabel", "Cost of Delay"));

		// Perform the update
		kpiMaster.updateOne(filter, update);
	}

	@RollbackExecution
	public void rollback() {
		rollbackkpi113();
	}

	public void rollbackkpi113() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection("kpi_master");
		Document filter = new Document("kpiId", "kpi113");

		Document update = new Document("$set",
				new Document("kpiName", "Value Delivered (Cost of Delay)").append("yAxisLabel", "Count(Days)"));

		// Perform the update
		kpiMaster.updateOne(filter, update);
	}
}
