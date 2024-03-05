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
package com.publicissapient.kpidashboard.apis.mongock.rollback.release_900;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/***
 * @author rendk
 */
@ChangeUnit(id = "r_combined_kpi_source_update", order = "09102", author = "rendk", systemVersion = "9.1.0")
public class UpdateKpiSource {
	private final MongoTemplate mongoTemplate;
	private static final String KPI_SOURCE = "kpiSource";

	public UpdateKpiSource(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	private void updateCombinedKpiSourceForJira() {
		Document condition = new Document(KPI_SOURCE, "Jira");
		Document update = new Document("$unset", new Document("combinedKpiSource", ""));
		mongoTemplate.getCollection("kpi_master").updateMany(condition, update);
	}

	private void updateCombinedKpiSourceForJenkins() {
		Document condition = new Document(KPI_SOURCE, "Jenkins");
		Document update = new Document("$unset", new Document("combinedKpiSource", ""));
		mongoTemplate.getCollection("kpi_master").updateMany(condition, update);
	}

	private void updateCombinedKpiSourceForBitBucket() {
		Document condition = new Document(KPI_SOURCE, "Bitbucket");
		Document update = new Document("$unset", new Document("combinedKpiSource", ""));
		mongoTemplate.getCollection("kpi_master").updateMany(condition, update);
	}

	private void updateCombinedKpiSourceForZypher() {
		Document condition = new Document(KPI_SOURCE, "Zypher");
		Document update = new Document("$unset", new Document("combinedKpiSource", ""));
		mongoTemplate.getCollection("kpi_master").updateMany(condition, update);
	}

	@Execution
	public void execution() {
		updateCombinedKpiSourceForJira();
		updateCombinedKpiSourceForJenkins();
		updateCombinedKpiSourceForBitBucket();
		updateCombinedKpiSourceForZypher();
	}

	private void rollbackCombinedKpiSourceForJira() {
		Document condition = new Document(KPI_SOURCE, "Jira");
		Document update = new Document("$set", new Document("combinedKpiSource", "Jira/Azure"));
		mongoTemplate.getCollection("kpi_master").updateMany(condition, update);
	}

	private void rollbackCombinedKpiSourceForJenkins() {
		Document condition = new Document(KPI_SOURCE, "Jenkins");
		Document update = new Document("$set",
				new Document("combinedKpiSource", "Jenkins/Bamboo/GitHubAction/AzurePipeline/Teamcity"));
		mongoTemplate.getCollection("kpi_master").updateMany(condition, update);
	}

	private void rollbackCombinedKpiSourceForBitBucket() {
		Document condition = new Document(KPI_SOURCE, "Bitbucket");
		Document update = new Document("$set",
				new Document("combinedKpiSource", "Bitbucket/AzureRepository/GitHub/GitLab/RepoTool"));
		mongoTemplate.getCollection("kpi_master").updateMany(condition, update);
	}

	private void rollbackCombinedKpiSourceForZypher() {
		Document condition = new Document(KPI_SOURCE, "Zypher");
		Document update = new Document("$set", new Document("combinedKpiSource", "Zypher/JiraTest"));
		mongoTemplate.getCollection("kpi_master").updateMany(condition, update);
	}

	@RollbackExecution
	public void rollBack() {
		rollbackCombinedKpiSourceForJira();
		rollbackCombinedKpiSourceForJenkins();
		rollbackCombinedKpiSourceForBitBucket();
		rollbackCombinedKpiSourceForZypher();
	}
}
