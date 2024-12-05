/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1200;

import com.mongodb.client.MongoCollection;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Arrays;

/**
 * @author purgupta2
 */
@ChangeUnit(id = "r_cycle_time_filter", order = "012003", author = "purgupta2", systemVersion = "12.0.0")
public class RCycleTimeKPIFilterCorrection {

    public static final String FIELD_TYPE = "fieldType";
    public static final String FIELD_NAME = "fieldName";
    public static final String FIELD_LABEL = "fieldLabel";
    private static final String KPIID = "kpiId";
    public static final String KPI_MASTER = "kpi_master";
    private final MongoTemplate mongoTemplate;

    public RCycleTimeKPIFilterCorrection(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void execution() {
        MongoCollection<Document> kpiMaster = mongoTemplate.getCollection(KPI_MASTER);
        changeKpiFilter("kpi171", "multiSelectDropDown", kpiMaster);
    }

    public void changeKpiFilter(String kpiId, String kpiFilter, MongoCollection<Document> kpiMaster) {
        kpiMaster.updateMany(new Document(KPIID, new Document("$in", Arrays.asList(kpiId))),
                new Document("$set", new Document("kpiFilter", kpiFilter)));
    }

    @RollbackExecution
    public void rollBack() {
        MongoCollection<Document> kpiMaster = mongoTemplate.getCollection(KPI_MASTER);
        changeKpiFilter("kpi171", "dropdown", kpiMaster);
    }

}
