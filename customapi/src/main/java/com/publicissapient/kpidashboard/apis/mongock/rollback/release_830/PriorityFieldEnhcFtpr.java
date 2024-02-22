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

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_830;


import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author shunaray
 */
@SuppressWarnings("java:S1192")
@ChangeUnit(id = "r_priority_field_ftpr_enhc", order = "08352", author = "shunaray", systemVersion = "8.3.5")
public class PriorityFieldEnhcFtpr {

    private final MongoTemplate mongoTemplate;

    public PriorityFieldEnhcFtpr(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void execution() {
        rollbackOptionsUpdate();
    }

    public void rollbackOptionsUpdate() {
        mongoTemplate.getCollection("field_mapping_structure")
                .updateMany(
                        new Document("fieldName", new Document("$in",
                                Arrays.asList("defectPriorityKPI82", "defectPriorityKPI135"))),
                        new Document("$set", new Document("options", Arrays.asList(
                                new Document("label", "p1").append("value", "p1"),
                                new Document("label", "p2").append("value", "p2"),
                                new Document("label", "p3").append("value", "p3"),
                                new Document("label", "p4").append("value", "p4"),
                                new Document("label", "p5").append("value", "p5")
                        )))
                );
    }

    @RollbackExecution
    public void rollback() {
        updateOptions();
        clearFtpDefectPriorities();
    }

    public void updateOptions() {
        List<Document> options = Arrays.asList(
                new Document("label", "p1").append("value", "p1").append("operator", "<").append("maxValue", "10").append("minValue", "1"),
                new Document("label", "p2").append("value", "p2").append("operator", "<").append("maxValue", "10").append("minValue", "1"),
                new Document("label", "p3").append("value", "p3").append("operator", "<").append("maxValue", "10").append("minValue", "1"),
                new Document("label", "p4").append("value", "p4").append("operator", "<").append("maxValue", "10").append("minValue", "1"),
                new Document("label", "p5").append("value", "p5").append("operator", "<").append("maxValue", "10").append("minValue", "1")
        );

        mongoTemplate.getCollection("field_mapping_structure")
                .updateMany(
                        new Document("fieldName", new Document("$in", Arrays.asList("defectPriorityKPI82", "defectPriorityKPI135"))),
                        new Document("$set", new Document("options", options))
                );
    }

    public void clearFtpDefectPriorities() {
        mongoTemplate.getCollection("field_mapping").updateMany(
                new Document(),
                new Document("$set", new Document("defectPriorityKPI135", new ArrayList<>())
                        .append("defectPriorityKPI82", new ArrayList<>()))
        );
    }



}
