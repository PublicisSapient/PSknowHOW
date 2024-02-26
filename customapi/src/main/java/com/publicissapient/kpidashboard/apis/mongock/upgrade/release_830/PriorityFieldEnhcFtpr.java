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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_830;


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
@ChangeUnit(id = "priority_field_ftpr_enhc", order = "8352", author = "shunaray", systemVersion = "8.3.5")
public class PriorityFieldEnhcFtpr {
    public static final String LABEL = "label";
    public static final String VALUE = "value";
    public static final String LABEL_VALUE = "labelValue";
    public static final String OPERATOR = "operator";
    public static final String MAX_VALUE = "maxValue";
    public static final String MIN_VALUE = "minValue";
    public static final String DEFECT_PRIORITY_KPI_82 = "defectPriorityKPI82";
    public static final String DEFECT_PRIORITY_KPI_135 = "defectPriorityKPI135";

    private final MongoTemplate mongoTemplate;

    public PriorityFieldEnhcFtpr(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void execution() {
        updateOptions();
        clearFtpDefectPriorities();
    }

    public void updateOptions() {
        List<Document> options = Arrays.asList(
                new Document(LABEL_VALUE, "p1").append(VALUE, "p1").append(OPERATOR, "<=").append(MAX_VALUE, "10").append(MIN_VALUE, "1"),
                new Document(LABEL_VALUE, "p2").append(VALUE, "p2").append(OPERATOR, "<=").append(MAX_VALUE, "10").append(MIN_VALUE, "1"),
                new Document(LABEL_VALUE, "p3").append(VALUE, "p3").append(OPERATOR, "<=").append(MAX_VALUE, "10").append(MIN_VALUE, "1"),
                new Document(LABEL_VALUE, "p4").append(VALUE, "p4").append(OPERATOR, "<=").append(MAX_VALUE, "10").append(MIN_VALUE, "1"),
                new Document(LABEL_VALUE, "p5").append(VALUE, "p5").append(OPERATOR, "<=").append(MAX_VALUE, "10").append(MIN_VALUE, "1")
        );

        mongoTemplate.getCollection("field_mapping_structure")
                .updateMany(
                        new Document("fieldName", new Document("$in", Arrays.asList(DEFECT_PRIORITY_KPI_82, DEFECT_PRIORITY_KPI_135))),
                        new Document("$set", new Document("fieldType" , "conditionalinput").append("options", options))
                );
    }

    public void clearFtpDefectPriorities() {
        mongoTemplate.getCollection("field_mapping").updateMany(
                new Document(),
                new Document("$set", new Document(DEFECT_PRIORITY_KPI_135, new ArrayList<>())
                        .append(DEFECT_PRIORITY_KPI_82, new ArrayList<>()))
        );
    }

    @RollbackExecution
    public void rollback() {
        rollbackOptionsUpdate();

    }

    public void rollbackOptionsUpdate() {
        mongoTemplate.getCollection("field_mapping_structure")
                .updateMany(
                        new Document("fieldName", new Document("$in",
                                Arrays.asList(DEFECT_PRIORITY_KPI_82, DEFECT_PRIORITY_KPI_135))),
                        new Document("$set", new Document("options", Arrays.asList(
                                new Document(LABEL, "p1").append(VALUE, "p1"),
                                new Document(LABEL, "p2").append(VALUE, "p2"),
                                new Document(LABEL, "p3").append(VALUE, "p3"),
                                new Document(LABEL, "p4").append(VALUE, "p4"),
                                new Document(LABEL, "p5").append(VALUE, "p5")
                        )))
                );
    }


}
