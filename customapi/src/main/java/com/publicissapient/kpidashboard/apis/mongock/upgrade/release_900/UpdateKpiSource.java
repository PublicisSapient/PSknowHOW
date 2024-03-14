/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_900;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/***
 * @author rendk
 */
@ChangeUnit(id = "combined_kpi_source_update", order = "9008", author = "rendk", systemVersion = "9.0.0")
public class UpdateKpiSource {
	private final MongoTemplate mongoTemplate;

	private static final String KPI_SOURCE = "kpiSource";

	private static final String UNSET = "$unset";

	private static final String SET = "$set";

	private static final String COMBINED_KPI_SOURCE = "combinedKpiSource";

	private static final String KPI_MASTER = "kpi_master";

	public UpdateKpiSource(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	private void updateCombinedKpiSourceForJira() {
		Document condition = new Document(KPI_SOURCE, "Jira");
		Document update = new Document(SET, new Document(COMBINED_KPI_SOURCE, "Jira/Azure"));
		mongoTemplate.getCollection(KPI_MASTER).updateMany(condition, update);
	}

	private void updateCombinedKpiSourceForJenkins() {
		Document condition = new Document(KPI_SOURCE, "Jenkins");
		Document update = new Document(SET,
				new Document(COMBINED_KPI_SOURCE, "Jenkins/Bamboo/GitHubAction/AzurePipeline/Teamcity"));
		mongoTemplate.getCollection(KPI_MASTER).updateMany(condition, update);
	}

	private void updateCombinedKpiSourceForBitBucket() {
		Document condition = new Document(KPI_SOURCE, "BitBucket");
		Document update = new Document(SET,
				new Document(COMBINED_KPI_SOURCE, "Bitbucket/AzureRepository/GitHub/GitLab/RepoTool"));
		mongoTemplate.getCollection(KPI_MASTER).updateMany(condition, update);
	}

	private void updateCombinedKpiSourceForZypher() {
		Document condition = new Document(KPI_SOURCE, "Zypher");
		Document update = new Document(SET, new Document(COMBINED_KPI_SOURCE, "Zephyr/Zypher/JiraTest"));
		mongoTemplate.getCollection(KPI_MASTER).updateMany(condition, update);
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
		Document update = new Document(UNSET, new Document(COMBINED_KPI_SOURCE, ""));
		mongoTemplate.getCollection(KPI_MASTER).updateMany(condition, update);
	}

	private void rollbackCombinedKpiSourceForJenkins() {
		Document condition = new Document(KPI_SOURCE, "Jenkins");
		Document update = new Document(UNSET, new Document(COMBINED_KPI_SOURCE, ""));
		mongoTemplate.getCollection(KPI_MASTER).updateMany(condition, update);
	}

	private void rollbackCombinedKpiSourceForBitBucket() {
		Document condition = new Document(KPI_SOURCE, "BitBucket");
		Document update = new Document(UNSET, new Document(COMBINED_KPI_SOURCE, ""));
		mongoTemplate.getCollection(KPI_MASTER).updateMany(condition, update);
	}

	private void rollbackCombinedKpiSourceForZypher() {
		Document condition = new Document(KPI_SOURCE, "Zypher");
		Document update = new Document(UNSET, new Document(COMBINED_KPI_SOURCE, ""));
		mongoTemplate.getCollection(KPI_MASTER).updateMany(condition, update);
	}

	@RollbackExecution
	public void rollBack() {
		rollbackCombinedKpiSourceForJira();
		rollbackCombinedKpiSourceForJenkins();
		rollbackCombinedKpiSourceForBitBucket();
		rollbackCombinedKpiSourceForZypher();
	}
}
