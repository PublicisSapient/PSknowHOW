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

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1010;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "r_change_repo_tool_kpi_group_id", order = "0101012", author = "kunkambl", systemVersion = "10.1.0")
public class ChangeRepoToolKpiGroupId {

	private final MongoTemplate mongoTemplate;

	public ChangeRepoToolKpiGroupId(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection("kpi_master");
		// Update documents
		updateDocument(kpiMaster, "kpi84", 1); // mean time to merge kpi
		updateDocument(kpiMaster, "kpi11", 1); // code commit kpi
	}

	private void updateDocument(MongoCollection<Document> kpiCategoryMapping, String kpiId, int groupId) {
		// Create the filter
		Document filter = new Document("kpiId", kpiId);
		// Create the update
		Document update = new Document("$set", new Document("groupId", groupId));
		// Perform the update
		kpiCategoryMapping.updateOne(filter, update);
	}

	@RollbackExecution
	public void rollback() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection("kpi_master");
		// Update documents
		updateDocument(kpiMaster, "kpi84", 2); // mean time to merge kpi
		updateDocument(kpiMaster, "kpi11", 2); // code commit kpi
	}
}
