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

@ChangeUnit(id = "change_repo_tool_kpi_combined_source", order = "101015", author = "kunkambl", systemVersion = "10.1.0")
public class ChangeCombinedSourceDevKpis {

	private final MongoTemplate mongoTemplate;
	private static final String EXISTING_KPI_SOURCE = "RepoTool";
	private static final String UPDATED_KPI_SOURCE = "Bitbucket/AzureRepository/GitHub/GitLab";

	public ChangeCombinedSourceDevKpis(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection("kpi_master");
		// Update documents
		updateDocument(kpiMaster, "kpi160", UPDATED_KPI_SOURCE);
		updateDocument(kpiMaster, "kpi162", UPDATED_KPI_SOURCE);
		updateDocument(kpiMaster, "kpi157", UPDATED_KPI_SOURCE);
		updateDocument(kpiMaster, "kpi173", UPDATED_KPI_SOURCE);
		updateDocument(kpiMaster, "kpi158", UPDATED_KPI_SOURCE);
		updateDocument(kpiMaster, "kpi159", UPDATED_KPI_SOURCE);
	}

	private void updateDocument(MongoCollection<Document> kpiCategoryMapping, String kpiId, String kpiSource) {
		// Create the filter
		Document filter = new Document("kpiId", kpiId);
		// Create the update
		Document update = new Document("$set", new Document("combinedKpiSource", kpiSource));
		// Perform the update
		kpiCategoryMapping.updateOne(filter, update);
	}

	@RollbackExecution
	public void rollback() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection("kpi_master");
		// Update documents
		updateDocument(kpiMaster, "kpi160", EXISTING_KPI_SOURCE);
		updateDocument(kpiMaster, "kpi162", EXISTING_KPI_SOURCE);
		updateDocument(kpiMaster, "kpi157", EXISTING_KPI_SOURCE);
		updateDocument(kpiMaster, "kpi173", EXISTING_KPI_SOURCE);
		updateDocument(kpiMaster, "kpi158", EXISTING_KPI_SOURCE);
		updateDocument(kpiMaster, "kpi159", EXISTING_KPI_SOURCE);
	}
}
