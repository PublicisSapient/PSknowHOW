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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1020;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "flow_efficiency", order = "10205", author = "aksshriv1", systemVersion = "10.2.0")
public class FlowEfficiencyKPI {

	private final MongoTemplate mongoTemplate;

	public FlowEfficiencyKPI(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection("kpi_master");
		updateDocument(kpiMaster, "kpi170", "Range");
	}

	private void updateDocument(MongoCollection<Document> kpiMaster, String kpiId, String label) {
		// Create the filter
		Document filter = new Document("kpiId", kpiId);
		// Create the update
		Document update = new Document("$set", new Document("xAxisLabel", label));
		// Perform the update
		kpiMaster.updateOne(filter, update);
	}

	@RollbackExecution
	public void rollBack() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection("kpi_master");
		updateDocument(kpiMaster, "kpi170", "Duration");
	}
}
