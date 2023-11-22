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

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_810;

import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author kunkambl
 */
@SuppressWarnings("java:S1192")
@ChangeUnit(id = "r_repo_tool_provider_url_change", order = "08110", author = "kunkambl", systemVersion = "8.1.0")
public class RepoToolProviderUrlChange {

    private final MongoTemplate mongoTemplate;

    public RepoToolProviderUrlChange(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void execution() {
        changeRepoToolProviderTestApiUrlsRollback();
        changePRSizeMaturityRollback();
        updateThresholdsRollback();
        updateKpi162Rollback();
    }

    public void changeRepoToolProviderTestApiUrlsRollback() {
        List<WriteModel<Document>> bulkUpdateOps = new ArrayList<>();
        bulkUpdateOps.add(new UpdateOneModel<>(new Document("toolName", "bitbucket"),

                new Document("$set",
                        new Document().append("testServerApiUrl", "https://api.bitbucket.org/2.0/repositories/")
                                .append("testApiUrl", ""))));

        // Update for gitlab tool
        bulkUpdateOps.add(new UpdateOneModel<>(new Document("toolName", "gitlab"),
                new Document("$set", new Document("testApiUrl", "https://gitlab.com/api/v4/projects/"))));
        BulkWriteOptions options = new BulkWriteOptions().ordered(false);
        mongoTemplate.getCollection("repo_tools_provider").bulkWrite(bulkUpdateOps, options);
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

    @RollbackExecution
    public void rollback() {
        changeRepoToolProviderTestApiUrls();
        changePRSizeMaturity();
        updateThresholds();
        updateKpi162();
    }

    public void changeRepoToolProviderTestApiUrls() {
        List<WriteModel<Document>> bulkUpdateOps = new ArrayList<>();

        // Update for bitbucket tool
        bulkUpdateOps.add(new UpdateOneModel<>(new Document("toolName", "bitbucket"),

                new Document("$set", new Document().append("testServerApiUrl", "/bitbucket/rest/api/1.0/projects/")
                        .append("testApiUrl", "https://api.bitbucket.org/2.0/workspaces/"))));

        // Update for gitlab tool
        bulkUpdateOps.add(new UpdateOneModel<>(new Document("toolName", "gitlab"),
                new Document("$set", new Document("testApiUrl", "/api/v4/projects/"))));
        BulkWriteOptions options = new BulkWriteOptions().ordered(false);
        mongoTemplate.getCollection("repo_tools_provider").bulkWrite(bulkUpdateOps, options);
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
}