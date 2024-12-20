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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1210;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author girpatha
 */
@ChangeUnit(id = "metadata_identifier_updater_for_templates", order = "12102", author = "girpatha", systemVersion = "12.1.0")
public class MetadataIdentifierUpdaterForTemplates {

    private final MongoTemplate mongoTemplate;

    @Value("${json.file.path}")
    private String JSON_FILE_PATH;

    private static final String METADATA_IDENTIFIER_COLLECTION = "metadata_identifier";
    private static final String TEMPLATE_CODE = "templateCode";
    private static final String TEMPLATE_NAME = "templateName";

    public MetadataIdentifierUpdaterForTemplates(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void execution() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Document> metadataIdentifiers = mapper.readValue(
                TypeReference.class.getResourceAsStream(JSON_FILE_PATH),
                new TypeReference<List<Document>>() {
                }
        );

        List<Document> filteredMetadataIdentifiers = metadataIdentifiers.stream()
                .filter(doc -> "11".equals(doc.getString(TEMPLATE_CODE)) || "12".equals(doc.getString(TEMPLATE_CODE)))
                .collect(Collectors.toList());

        MongoCollection<Document> collection = mongoTemplate.getCollection(METADATA_IDENTIFIER_COLLECTION);
        collection.insertMany(filteredMetadataIdentifiers);

        collection.updateMany(
                new Document(TEMPLATE_NAME, "Standard Template"),
                new Document("$set", new Document(TEMPLATE_NAME, "Standard non-DOJO Template"))
        );
    }

    @RollbackExecution
    public void rollback() {
        MongoCollection<Document> collection = mongoTemplate.getCollection(METADATA_IDENTIFIER_COLLECTION);
        collection.deleteMany(new Document(TEMPLATE_CODE, new Document("$in", List.of("11", "12"))));

        collection.updateMany(
                new Document(TEMPLATE_NAME, "Standard non-DOJO Template"),
                new Document("$set", new Document(TEMPLATE_NAME, "Standard Template"))
        );
    }

}
