package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1310;

import com.mongodb.client.MongoCollection;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeUnit(id = "change_kpi_document_link", order = "13101", author = "kunkambl", systemVersion = "13.1.0")
public class ChangeKpiMasterDocumentationLinkAsset {

    private final MongoTemplate mongoTemplate;

    public ChangeKpiMasterDocumentationLinkAsset(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void updateLink(String fromLink, String toLink) {
        MongoCollection<Document> collection = mongoTemplate.getCollection("kpi_master");

        collection.find().forEach(document -> {
            Document kpiInfo = document.get("kpiInfo", Document.class);

            if (kpiInfo != null) {
                updateDetails(kpiInfo, fromLink, toLink);
            }

            collection.replaceOne(new Document("_id", document.get("_id")), document);
        });
    }

    private void updateDetails(Document kpiInfo, String fromLink, String toLink) {
        if (kpiInfo.containsKey("details")) {
            var detailsList = kpiInfo.getList("details", Document.class);
            for (Document detail : detailsList) {
                updateLinkDetail(detail, fromLink, toLink);
            }
        }
    }

    private void updateLinkDetail(Document detail, String fromLink, String toLink) {
        if ("link".equals(detail.getString("type"))) {
            Document kpiLinkDetail = detail.get("kpiLinkDetail", Document.class);
            if (kpiLinkDetail != null) {
                String link = kpiLinkDetail.getString("link");
                if (link != null && link.contains(fromLink)) {
                    String updatedLink = link.replace(fromLink, toLink);
                    kpiLinkDetail.put("link", updatedLink);
                }
            }
        }
    }

    @Execution
    public void execution() {
        updateLink("psknowhow", "knowhow");
    }

    @RollbackExecution
    public void rollback() {
        updateLink("knowhow", "psknowhow");
    }

}
