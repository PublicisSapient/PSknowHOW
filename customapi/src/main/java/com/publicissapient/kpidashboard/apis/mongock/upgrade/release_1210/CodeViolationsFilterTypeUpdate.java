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

/**
 *
 */

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1210;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@ChangeUnit(id = "update_code_violations_filter_type", order = "12105", author = "kunkambl", systemVersion = "12.1.0")
public class CodeViolationsFilterTypeUpdate {
    private final MongoTemplate mongoTemplate;

    public CodeViolationsFilterTypeUpdate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void execution() {
        Query query = new Query(Criteria.where("kpiId").in("kpi38", "kpi64"));
        Update update = new Update();
        update.set("kpiFilter", "multiTypeFilters");
        mongoTemplate.updateMulti(query, update, "kpi_master");
    }

    @RollbackExecution
    public void rollback() {
        Query query = new Query(Criteria.where("kpiId").in("kpi38", "kpi64"));
        Update update = new Update();
        update.set("kpiFilter", "multiSelectDropDown");
        mongoTemplate.updateMulti(query, update, "kpi_master");
    }
}
