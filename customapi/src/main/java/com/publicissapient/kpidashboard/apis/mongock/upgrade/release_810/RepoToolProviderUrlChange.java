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

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author kunkambl
 */
@SuppressWarnings("java:S1192")
@ChangeUnit(id = "repo_tool_provider_url_change", order = "8110", author = "kunkambl", systemVersion = "8.1.0")
public class RepoToolProviderUrlChange {

	private final MongoTemplate mongoTemplate;

	public RepoToolProviderUrlChange(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		changeRepoToolProviderTestApiUrls();
		changePRSizeMaturity();
		updateThresholds();
		updateKpi162();
	}

	public void changeRepoToolProviderTestApiUrls() {

		// Update for bitbucket tool
		Document filterBitbucket = new Document("toolName", "bitbucket");
		Document updateGitlab = new Document("$set",
				new Document().append("testServerApiUrl", "/bitbucket/rest/api/1.0/projects/").append("testApiUrl",
						"https://api.bitbucket.org/2.0/workspaces/"));

		mongoTemplate.getCollection("repo_tools_provider").updateOne(filterBitbucket, updateGitlab);
		// Update for gitlab tool
		Document filterGl = new Document("toolName", "gitlab");
		Document updateGl = new Document("$set",
				new Document().append("testApiUrl", "/api/v4/projects/").append("repoToolProvider", "gitlab"));

		mongoTemplate.getCollection("repo_tools_provider").updateOne(filterGl, updateGl);
	}

	public void changePRSizeMaturity() {

		mongoTemplate.getCollection("kpi_master").updateOne(new Document("kpiId", "kpi162"),
				new Document("$set", new Document("calculateMaturity", false)));
	}

	public void updateThresholds() {
		mongoTemplate.getCollection("kpi_master").updateMany(
				new Document("kpiId", new Document("$in", Arrays.asList("kpi160", "kpi158"))),
				new Document("$set", new Document("upperThresholdBG", "red").append("lowerThresholdBG", "white")));
	}

	public void updateKpi162() {
		Document filter = new Document("kpiId", "kpi162");
		Document update = new Document("$set", new Document("calculateMaturity", false).append("showTrend", false));

		mongoTemplate.getCollection("kpi_master").updateOne(filter, update);
	}

	@RollbackExecution
	public void rollback() {
		changeRepoToolProviderTestApiUrlsRollback();
		changePRSizeMaturityRollback();
		updateThresholdsRollback();
		updateKpi162Rollback();
	}

	public void changeRepoToolProviderTestApiUrlsRollback() {
		Document filterBb = new Document("toolName", "bitbucket");
		Document updateBb = new Document("$set", new Document()
				.append("testServerApiUrl", "https://api.bitbucket.org/2.0/repositories/").append("testApiUrl", ""));

		mongoTemplate.getCollection("repo_tools_provider").updateOne(filterBb, updateBb);

		Document filter = new Document("toolName", "gitlab");
		Document update = new Document("$set",
				new Document().append("testApiUrl", "https://gitlab.com/api/v4/projects/").append("repoToolProvider", ""));

		mongoTemplate.getCollection("repo_tools_provider").updateOne(filter, update);
	}

	public void changePRSizeMaturityRollback() {

		mongoTemplate.getCollection("kpi_master").updateOne(new Document("kpiId", "kpi162"),
				new Document("$set", new Document("calculateMaturity", true)));
	}

	public void updateThresholdsRollback() {
		mongoTemplate.getCollection("kpi_master").updateMany(
				new Document("kpiId", new Document("$in", Arrays.asList("kpi160", "kpi158"))),
				new Document("$set", new Document("upperThresholdBG", "").append("lowerThresholdBG", "")));
	}

	public void updateKpi162Rollback() {
		Document filter = new Document("kpiId", "kpi162");
		Document update = new Document("$set", new Document("calculateMaturity", true).append("showTrend", true));
		mongoTemplate.getCollection("kpi_master").updateOne(filter, update);
	}
}
