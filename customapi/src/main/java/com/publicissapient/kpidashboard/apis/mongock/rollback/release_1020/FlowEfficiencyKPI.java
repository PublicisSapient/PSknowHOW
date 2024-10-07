package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1020;

import com.mongodb.client.MongoCollection;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeUnit(id = "r_flow_efficiency", order = "010205", author = "aksshriv1", systemVersion = "10.2.0")
public class FlowEfficiencyKPI {

    private final MongoTemplate mongoTemplate;

    public FlowEfficiencyKPI(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void execution() {
        MongoCollection<Document> kpiMaster = mongoTemplate.getCollection("kpi_master");
        updateDocument(kpiMaster, "kpi170", "Duration");
    }
    private void updateDocument(MongoCollection<Document> kpiMaster, String kpiId, String label) {
        // Create the filter
        Document filter = new Document("kpiId", kpiId);
        // Create the update
        Document update = new Document("$set", new Document("xAxisLabel", label));
        // Perform the update
        kpiMaster.updateOne(filter, update);
    }
    @RollbackExecution
    public void rollBack() {
        // no implementation required
    }
}
