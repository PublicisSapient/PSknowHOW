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
 * @author shunaray
 */
@ChangeUnit(id = "build_kpi_align", order = "10101", author = "shunaray", systemVersion = "10.1.0")
public class BuildKpiOrder {
	private final MongoTemplate mongoTemplate;

	public BuildKpiOrder(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public boolean execution() {
		MongoCollection<Document> kpiCategoryMapping = mongoTemplate.getCollection("kpi_category_mapping");

		// Update documents
		updateDocument(kpiCategoryMapping, "kpi164", 8); // scope churn kpi
		updateDocument(kpiCategoryMapping, "kpi8", 9); // code build time kpi
		return true;
	}

	private void updateDocument(MongoCollection<Document> kpiCategoryMapping, String kpiId, int kpiOrder) {
		// Create the filter
		Document filter = new Document("kpiId", kpiId);

		// Create the update
		Document update = new Document("$set", new Document("kpiOrder", kpiOrder));

		// Perform the update
		kpiCategoryMapping.updateOne(filter, update);
	}

	@RollbackExecution
	public void rollbackOrdering() {
		MongoCollection<Document> kpiCategoryMapping = mongoTemplate.getCollection("kpi_category_mapping");

		// Update documents
		updateDocument(kpiCategoryMapping, "kpi164", 9); // scope churn kpi
		updateDocument(kpiCategoryMapping, "kpi8", 8); // code build time kpi
	}
}
