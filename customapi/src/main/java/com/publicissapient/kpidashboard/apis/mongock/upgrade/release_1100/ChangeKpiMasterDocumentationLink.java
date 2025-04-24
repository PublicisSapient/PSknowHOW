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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1100;

import com.mongodb.client.MongoCollection;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.HashMap;
import java.util.Map;

@ChangeUnit(id = "change_kpimaster_documentation_link", order = "11003", author = "kunkambl", systemVersion = "11.0.0")
public class ChangeKpiMasterDocumentationLink {
    private final MongoTemplate mongoTemplate;
    private final Map<ObjectId, String> oldLinkMap = new HashMap<>();
    private static final String KPI_DETAILS = "details";
    private static final String KPI_LINK_DETAIL = "kpiLinkDetail";

    public ChangeKpiMasterDocumentationLink(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void execution() {
        updateLink();
    }

    @RollbackExecution
    public void rollback() {
        revertLink();
    }

    private void updateLink() {
        MongoCollection<Document> collection = mongoTemplate.getCollection("kpi_master");

        collection.find().forEach(document -> {
            ObjectId documentId = document.getObjectId("_id");
            String kpiId = document.getString("kpiId");
            String kpiName = document.getString("kpiName");

            if (kpiId != null && kpiName != null) {
                Document kpiInfo = document.get("kpiInfo", Document.class);

                if (kpiInfo != null) {
                    // Store the old link in the map
                    String oldLink = getOldLink(kpiInfo);
                    if (oldLink != null) {
                        oldLinkMap.put(documentId, oldLink);
                    }

                    // Construct and update to the new link
					String newLink = String.format("https://knowhow.tools.publicis.sapient.com/wiki/%s-%s", kpiId,
							kpiName.trim().replace(" ", "+").replaceAll("[()%]", ""));
					updateDetails(kpiInfo, newLink);
                }

                collection.replaceOne(new Document("_id", documentId), document);
            }
        });
    }

    private String getOldLink(Document kpiInfo) {
        if (kpiInfo.containsKey(KPI_DETAILS)) {
            var detailsList = kpiInfo.getList(KPI_DETAILS, Document.class);
            for (Document detail : detailsList) {
                if ("link".equals(detail.getString("type"))) {
                    Document kpiLinkDetail = detail.get(KPI_LINK_DETAIL, Document.class);
                    if (kpiLinkDetail != null) {
                        return kpiLinkDetail.getString("link");
                    }
                }
            }
        }
        return null;
    }

    private void updateDetails(Document kpiInfo, String newLink) {
        if (kpiInfo.containsKey(KPI_DETAILS)) {
            var detailsList = kpiInfo.getList(KPI_DETAILS, Document.class);
            for (Document detail : detailsList) {
                updateLinkDetail(detail, newLink);
            }
        }
    }

    private void updateLinkDetail(Document detail, String newLink) {
        if ("link".equals(detail.getString("type"))) {
            Document kpiLinkDetail = detail.get(KPI_LINK_DETAIL, Document.class);
            if (kpiLinkDetail != null) {
                kpiLinkDetail.put("link", newLink);
            }
        }
    }

    private void revertLink() {
        MongoCollection<Document> collection = mongoTemplate.getCollection("kpi_master");

        collection.find().forEach(document -> {
            ObjectId documentId = document.getObjectId("_id");
            Document kpiInfo = document.get("kpiInfo", Document.class);

            if (kpiInfo != null) {
                // Retrieve the old link from the map
                String oldLink = oldLinkMap.get(documentId);
                if (oldLink != null) {
                    revertDetails(kpiInfo, oldLink);
                }
            }

            collection.replaceOne(new Document("_id", documentId), document);
        });
    }

    private void revertDetails(Document kpiInfo, String oldLink) {
        if (kpiInfo.containsKey(KPI_DETAILS)) {
            var detailsList = kpiInfo.getList(KPI_DETAILS, Document.class);
            for (Document detail : detailsList) {
                revertLinkDetail(detail, oldLink);
            }
        }
    }

    private void revertLinkDetail(Document detail, String oldLink) {
        if ("link".equals(detail.getString("type"))) {
            Document kpiLinkDetail = detail.get(KPI_LINK_DETAIL, Document.class);
            if (kpiLinkDetail != null) {
                kpiLinkDetail.put("link", oldLink);
            }
        }
    }
}