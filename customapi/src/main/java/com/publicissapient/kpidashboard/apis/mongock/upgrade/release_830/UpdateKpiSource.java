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
package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_830;

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
@ChangeUnit(id = "kpi_source_update", order = "8351", author = "rendk", systemVersion = "8.3.5")
public class UpdateKpiSource {
	private final MongoTemplate mongoTemplate;

	private static final String KPISOURCE = "kpiSource";

	public UpdateKpiSource(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {

		MongoCollection<Document> kpiMasterCollection = mongoTemplate.getCollection("kpi_master");
		List<WriteModel<Document>> bulkOps = new ArrayList<>();

		bulkOps.add(new UpdateManyModel<>(new Document(KPISOURCE, "Jira"),
				new Document("$set", new Document(KPISOURCE, "Jira/Azure"))));
		bulkOps.add(new UpdateManyModel<>(new Document(KPISOURCE, "Jenkins"),
				new Document("$set", new Document(KPISOURCE, "Jenkins/Bamboo/GitHubAction/AzurePipeline/Teamcity"))));
		bulkOps.add(new UpdateManyModel<>(new Document(KPISOURCE, "Bitbucket"),
				new Document("$set", new Document(KPISOURCE, "Bitbucket/AzureRepository/GitHub/GitLab/RepoTool"))));
		bulkOps.add(new UpdateManyModel<>(new Document(KPISOURCE, "Zypher"),
				new Document("$set", new Document(KPISOURCE, "Zypher/JiraTest"))));
		kpiMasterCollection.bulkWrite(bulkOps);
	}

	@RollbackExecution
	public void rollBack() {
		MongoCollection<Document> kpiMasterCollection = mongoTemplate.getCollection("kpi_master");

		List<WriteModel<Document>> bulkOps = new ArrayList<>();
		bulkOps.add(new UpdateManyModel<>(new Document(KPISOURCE, "Jira/Azure"),
				new Document("$set", new Document(KPISOURCE, "Jira"))));
		bulkOps.add(new UpdateManyModel<>(new Document(KPISOURCE, "Jenkins/Bamboo/GitHubAction/AzurePipeline/Teamcity"),
				new Document("$set", new Document(KPISOURCE, "Jenkins"))));
		bulkOps.add(new UpdateManyModel<>(new Document(KPISOURCE, "Bitbucket/AzureRepository/GitHub/GitLab/RepoTool"),
				new Document("$set", new Document(KPISOURCE, "Bitbucket"))));
		bulkOps.add(new UpdateManyModel<>(new Document(KPISOURCE, "Zypher/JiraTest"),
				new Document("$set", new Document(KPISOURCE, "Zypher"))));
		kpiMasterCollection.bulkWrite(bulkOps);
	}
}
