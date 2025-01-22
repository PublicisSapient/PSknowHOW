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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1210;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@ChangeUnit(id = "update_code_violations_kpi_column_config", order = "12108", author = "kunkambl", systemVersion = "12.1.0")
public class CodeViolationKPIExcelColumnUpdate {
    private final MongoTemplate mongoTemplate;

    private static final String COLUMN_NAME = "columnName";
    private static final String IS_SHOWN = "isShown";
    private static final String ORDER = "order";
    private static final String IS_DEFAULT = "isDefault";
    private static final String KPI_COLUMN_DETAILS = "kpiColumnDetails";
    private static final String KPI_COLUMN_CONFIGS = "kpi_column_configs";

    public CodeViolationKPIExcelColumnUpdate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void execution() {
        List<String> kpiIds = List.of("kpi38", "kpi64");

        // Create a query to match the documents
        Query query = new Query(Criteria.where("kpiId").in(kpiIds));

        Update removeColumn = new Update().pull(KPI_COLUMN_DETAILS, new Document(COLUMN_NAME, "Sonar Violations"));
        mongoTemplate.updateMulti(query, removeColumn, KPI_COLUMN_CONFIGS);

        Document newColumn1 = new Document(COLUMN_NAME, "Violation Type")
                .append(ORDER, 3)
                .append(IS_SHOWN, true)
                .append(IS_DEFAULT, false);

        Document newColumn2 = new Document(COLUMN_NAME, "Violation Severity")
                .append(ORDER, 4)
                .append(IS_SHOWN, true)
                .append(IS_DEFAULT, false);

        Update addColumns = new Update().push(KPI_COLUMN_DETAILS).each(newColumn1, newColumn2);

        mongoTemplate.updateMulti(query, addColumns, KPI_COLUMN_CONFIGS);
    }

    @RollbackExecution
    public void rollback() {
        List<String> kpiIds = List.of("kpi38", "kpi64");

        Query query = new Query(Criteria.where("kpiId").in(kpiIds));
        Update removeColumns = new Update()
                .pull(KPI_COLUMN_DETAILS, new Document(COLUMN_NAME, "Violation Type"))
                .pull(KPI_COLUMN_DETAILS, new Document(COLUMN_NAME, "Violation Severity"));

        mongoTemplate.updateMulti(query, removeColumns, KPI_COLUMN_CONFIGS);

        Document sonarColumn = new Document(COLUMN_NAME, "Sonar Violations")
                .append(ORDER, 3)
                .append(IS_SHOWN, true)
                .append(IS_DEFAULT, true);

        Update addColumns = new Update().push(KPI_COLUMN_DETAILS, sonarColumn);
        mongoTemplate.updateMulti(query, addColumns, KPI_COLUMN_CONFIGS);
    }
}
