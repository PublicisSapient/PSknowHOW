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

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shi6
 */
@ChangeUnit(id = "backlog_health_order", order = "8111", author = "shi6", systemVersion = "8.1.0")
public class BacklogHealthOrder {
	private final MongoTemplate mongoTemplate;

	public BacklogHealthOrder(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public boolean execution() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection("kpi_master");

		// Update documents
		updateDocument(kpiMaster, "kpi138", 1);
		updateDocument(kpiMaster, "kpi127", 2);
		updateDocument(kpiMaster, "kpi129", 3);
		updateDocument(kpiMaster, "kpi161", 4);
		updateDocument(kpiMaster, "kpi137", 5);
		updateDocument(kpiMaster, "kpi139", 6);
		return true;
	}

	private void updateDocument(MongoCollection<Document> kpiMaster, String kpiId, int defaultOrder) {
		// Create the filter
		Document filter = new Document("kpiId", kpiId);

		// Create the update
		Document update = new Document("$set", new Document("defaultOrder", defaultOrder));

		// Perform the update
		kpiMaster.updateOne(filter, update);
	}

	@RollbackExecution
	public void rollbackOrdering() {
		MongoCollection<Document> collection = mongoTemplate.getCollection("kpi_master");

		updateDocument(collection, "kpi138", 8);
		updateDocument(collection, "kpi129", 2);
		updateDocument(collection, "kpi137", 3);
		updateDocument(collection, "kpi161", 5);
		updateDocument(collection, "kpi127", 4);
		updateDocument(collection, "kpi139", 5);
	}
}
