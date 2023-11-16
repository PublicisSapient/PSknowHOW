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
import java.util.List;

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

    @RollbackExecution
    public void rollback() {
        changeRepoToolProviderTestApiUrls();
        changePRSizeMaturity();
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
}