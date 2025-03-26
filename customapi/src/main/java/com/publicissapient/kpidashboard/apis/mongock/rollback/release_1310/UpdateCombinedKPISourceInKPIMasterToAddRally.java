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

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1310;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author girpatha
 */
@ChangeUnit(id = "r_update_combinedKpiSource_in_kpi_master_to_add_rally", order = "013100", author = "girpatha", systemVersion = "13.1.0")
public class UpdateCombinedKPISourceInKPIMasterToAddRally {

	private final MongoTemplate mongoTemplate;
	private static final String EXISTING_KPI_SOURCE = "Jira/Azure";
	private static final String UPDATED_KPI_SOURCE = "Jira/Azure/Rally";

	public UpdateCombinedKPISourceInKPIMasterToAddRally(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection("kpi_master");
		// Update documents
		updateDocument(kpiMaster, UPDATED_KPI_SOURCE, EXISTING_KPI_SOURCE);
	}

	private void updateDocument(MongoCollection<Document> kpiCategoryMapping, String existingKPISource,
			String updatedKPISource) {
		// Create the filter
		Document filter = new Document("combinedKpiSource", existingKPISource);
		// Create the update
		Document update = new Document("$set", new Document("combinedKpiSource", updatedKPISource));
		// Perform the update
		kpiCategoryMapping.updateMany(filter, update);
	}

	@RollbackExecution
	public void rollback() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection("kpi_master");
		// Update documents
		updateDocument(kpiMaster, EXISTING_KPI_SOURCE, UPDATED_KPI_SOURCE);
	}
}
