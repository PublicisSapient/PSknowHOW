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

/** */

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1310;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "r_remove_mandatory_field_kpi37_field_mapping", order = "013105", author = "kunkambl", systemVersion = "13.1.0")
public class DefectRejectionRateFieldMappingUpdate {

    private final MongoTemplate mongoTemplate;

    public DefectRejectionRateFieldMappingUpdate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void execution() {
        Query query = new Query(Criteria.where("fieldName").is("jiraDefectRejectionStatusKPI37"));
        Update update = new Update();
        update.set("mandatory", true);
        mongoTemplate.updateMulti(query, update, "field_mapping_structure");
    }

    @RollbackExecution
    public void rollback() {
        Query query = new Query(Criteria.where("fieldName").is("jiraDefectRejectionStatusKPI37"));
        Update update = new Update();
        update.set("mandatory", false);
        mongoTemplate.updateMulti(query, update, "field_mapping_structure");
    }
}
