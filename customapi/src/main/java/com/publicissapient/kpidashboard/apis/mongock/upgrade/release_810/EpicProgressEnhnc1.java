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

import java.util.Arrays;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.BeforeExecution;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackBeforeExecution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shi6
 */
@SuppressWarnings("java:S1192")
@ChangeUnit(id = "epic_progress_enhnc1", order = "8109", author = "shi6", systemVersion = "8.1.0")
public class EpicProgressEnhnc1 {
	private final MongoTemplate mongoTemplate;
	private MongoCollection<Document> kpiMaster;

	public EpicProgressEnhnc1(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@BeforeExecution
	public void beforeExecute() {
		kpiMaster = mongoTemplate.getCollection("kpi_master");
	}

	@Execution
	public void execution() {
		updateKpiMaster();

	}

	public void updateKpiMaster() {
		// Create the document to insert

		Document filter = new Document("kpiId", "kpi169");

		// Define the update operation to set the "kpiFilter" field to "radioButton"
		Document update = new Document("$set", new Document("kpiFilter", "radioButton"));

		// Perform the update operation
		kpiMaster.updateOne(filter, update);

		filter = new Document("kpiId", new Document("$in", Arrays.asList("kpi151", "kpi152", "kpi155")));

		// Define the update operation to unset the "kpiFilter" field
		update = new Document("$unset", new Document("kpiFilter", 1));

		// Perform the update operation
		kpiMaster.updateMany(filter, update);

	}

	@RollbackExecution
	public void rollbackMaster() {

		// Rollback the update of "kpi169" document
		Document filter = new Document("kpiId", "kpi169");
		Document update = new Document("$unset", new Document("kpiFilter", 1));
		kpiMaster.updateOne(filter, update);

		// Rollback the update of multiple documents
		filter = new Document("kpiId", new Document("$in", Arrays.asList("kpi151", "kpi152", "kpi155")));
		update = new Document("$set", new Document("kpiFilter", "dropdown"));
		kpiMaster.updateMany(filter, update);
	}

	@RollbackBeforeExecution
	public void rollbackBeforeExecution() {
		// do not rquire the implementation
	}
}
