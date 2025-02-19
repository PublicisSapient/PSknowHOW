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
package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1010;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shunaray
 */
@ChangeUnit(id = "plan_release_filter", order = "101011", author = "shunaray", systemVersion = "10.1.0")
public class ReleasePlanEnhc {

	private final MongoTemplate mongoTemplate;

	public ReleasePlanEnhc(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		updateFieldMappingStructure("Custom Fields Mapping");
		updateKpiFilter("radioButton");
	}

	public void updateKpiFilter(String radioButton) {
		mongoTemplate.getCollection("kpi_master").updateOne(new Document("kpiId", "kpi179"),
				new Document("$set", new Document("kpiFilter", radioButton)));
	}

	public void updateFieldMappingStructure(String section) {
		mongoTemplate.getCollection("field_mapping_structure").updateOne(new Document("fieldName", "startDateCountKPI150"),
				new Document("$set", new Document("section", section)));
	}

	@RollbackExecution
	public void rollback() {
		updateFieldMappingStructure("Issue Types Mapping");
		updateKpiFilter("");
	}
}
