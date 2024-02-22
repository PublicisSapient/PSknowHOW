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
package com.publicissapient.kpidashboard.apis.mongock.rollback.release_910;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.List;

/***
 * @author rendk
 */
@ChangeUnit(id = "r_kpi_source_update", order = "9101", author = "rendk", systemVersion = "9.1.0")
public class UpdateKpiSource {
	private final MongoTemplate mongoTemplate;
	private static final String KPI_SOURCE = "kpiSource";

	public UpdateKpiSource(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		MongoCollection<Document> kpiMasterCollection = mongoTemplate.getCollection("kpi_master");

		List<WriteModel<Document>> bulkOps = new ArrayList<>();
		bulkOps.add(new UpdateManyModel<>(new Document(KPI_SOURCE, "Jira/Azure1"),
				new Document("$set", new Document(KPI_SOURCE, "Jira"))));
		bulkOps.add(
				new UpdateManyModel<>(new Document(KPI_SOURCE, "Jenkins/Bamboo/GitHubAction/AzurePipeline/Teamcity"),
						new Document("$set", new Document(KPI_SOURCE, "Jenkins"))));
		bulkOps.add(new UpdateManyModel<>(new Document(KPI_SOURCE, "Bitbucket/AzureRepository/GitHub/GitLab/RepoTool"),
				new Document("$set", new Document(KPI_SOURCE, "Bitbucket"))));
		bulkOps.add(new UpdateManyModel<>(new Document(KPI_SOURCE, "Zypher/JiraTest"),
				new Document("$set", new Document(KPI_SOURCE, "Zypher"))));
		kpiMasterCollection.bulkWrite(bulkOps);
	}

	@RollbackExecution
	public void rollBack() {

		MongoCollection<Document> kpiMasterCollection = mongoTemplate.getCollection("kpi_master");
		List<WriteModel<Document>> bulkOps = new ArrayList<>();

		bulkOps.add(new UpdateManyModel<>(new Document(KPI_SOURCE, "Jira"),
				new Document("$set", new Document(KPI_SOURCE, "Jira/Azure"))));
		bulkOps.add(new UpdateManyModel<>(new Document(KPI_SOURCE, "Jenkins"),
				new Document("$set", new Document(KPI_SOURCE, "Jenkins/Bamboo/GitHubAction/AzurePipeline/Teamcity"))));
		bulkOps.add(new UpdateManyModel<>(new Document(KPI_SOURCE, "Bitbucket"),
				new Document("$set", new Document(KPI_SOURCE, "Bitbucket/AzureRepository/GitHub/GitLab/RepoTool"))));
		bulkOps.add(new UpdateManyModel<>(new Document(KPI_SOURCE, "Zypher"),
				new Document("$set", new Document(KPI_SOURCE, "Zypher/JiraTest"))));
		kpiMasterCollection.bulkWrite(bulkOps);
	}

}
