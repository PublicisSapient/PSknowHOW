/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1220;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Arrays;

/**
 * Change Unit to update columnName from "Job Name" to "Job Name / Pipeline Name" for kpiIds "kpi66","kpi8","kpi116","kpi184","kpi172"
 * @author girpatha
 */
@ChangeUnit(id = "update_kpi_column_config_job_name", order = "12202", author = "girpatha", systemVersion = "12.2.0")
public class UpdateKpiColumnConfigJobName {

    private final MongoTemplate mongoTemplate;

    public UpdateKpiColumnConfigJobName(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void execution() {
        mongoTemplate.getCollection("kpi_column_configs").updateMany(
                new Document("kpiId", new Document("$in", Arrays.asList("kpi66", "kpi8","kpi116","kpi184","kpi172")))
                        .append("kpiColumnDetails.columnName", "Job Name"),
                new Document("$set", new Document("kpiColumnDetails.$.columnName", "Job Name / Pipeline Name"))
        );
    }

    @RollbackExecution
    public void rollback() {
        mongoTemplate.getCollection("kpi_column_configs").updateMany(
                new Document("kpiId", new Document("$in", Arrays.asList("kpi66", "kpi8","kpi116","kpi184","kpi172")))
                        .append("kpiColumnDetails.columnName", "Job Name / Pipeline Name"),
                new Document("$set", new Document("kpiColumnDetails.$.columnName", "Job Name"))
        );
    }
}