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

@ChangeUnit(id = "r_kpi_source_update", order = "9101", author = "rendk", systemVersion = "9.1.0")
public class UpdateKpiSource {
    private final MongoTemplate mongoTemplate;
    private final String KPI_SOURCE = "kpiSource";

    public UpdateKpiSource(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    void execution()
    {
        MongoCollection<Document> kpiMasterCollection = mongoTemplate.getCollection("kpi_master");

        List<WriteModel<Document>> bulkOps = new ArrayList<>();
        bulkOps.add(new UpdateManyModel<>(
                new Document(KPI_SOURCE, "Jira/Azure/Zephyr"),
                new Document("$set", new Document(KPI_SOURCE, "Jira"))
        ));
        bulkOps.add(new UpdateManyModel<>(
                new Document(KPI_SOURCE, "Jenkins/Bamboo/GitHubAction/AzurePipeline/Teamcity"),
                new Document("$set", new Document(KPI_SOURCE, "Jenkins"))
        ));
        bulkOps.add(new UpdateManyModel<>(
                new Document(KPI_SOURCE, "Bitbucket/AzureRepository/GitHub/GitLab/RepoTool"),
                new Document("$set", new Document(KPI_SOURCE, "Bitbucket"))
        ));
        bulkOps.add(new UpdateManyModel<>(
                new Document(KPI_SOURCE, "Zephyr/JiraTest"),
                new Document("$set", new Document(KPI_SOURCE, "Zephyr"))
        ));
        kpiMasterCollection.bulkWrite(bulkOps);
    }
    @RollbackExecution
    public void rollBack() {

        MongoCollection<Document> kpiMasterCollection = mongoTemplate.getCollection("kpi_master");
        List<WriteModel<Document>> bulkOps = new ArrayList<>();

        bulkOps.add(new UpdateManyModel<>(
                new Document(KPI_SOURCE, "Jira"),
                new Document("$set", new Document(KPI_SOURCE, "Jira/Azure/Zephyr"))
        ));

        bulkOps.add(new UpdateManyModel<>(
                new Document(KPI_SOURCE, "Jenkins"),
                new Document("$set", new Document(KPI_SOURCE, "Jenkins/Bamboo/GitHubAction/AzurePipeline/Teamcity"))
        ));

        bulkOps.add(new UpdateManyModel<>(
                new Document(KPI_SOURCE, "Bitbucket"),
                new Document("$set", new Document(KPI_SOURCE, "Bitbucket/AzureRepository/GitHub/GitLab/RepoTool"))
        ));
        bulkOps.add(new UpdateManyModel<>(
                new Document(KPI_SOURCE, "Zephyr"),
                new Document("$set", new Document(KPI_SOURCE, "Zephyr/JiraTest"))
        ));
        kpiMasterCollection.bulkWrite(bulkOps);
    }

}
