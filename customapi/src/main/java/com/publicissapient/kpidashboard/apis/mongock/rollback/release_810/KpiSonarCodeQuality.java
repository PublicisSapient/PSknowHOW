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

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_810;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shi6
 */
@ChangeUnit(id = "r_kpi_sonar_code_quality", order = "08104", author = "shi6", systemVersion = "8.1.0")
public class KpiSonarCodeQuality {

	private final MongoTemplate mongoTemplate;

	public KpiSonarCodeQuality(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public boolean execution() {
		// Delete "Sonar Code Quality Kpi" from kpi_master collection
		mongoTemplate.getCollection("kpi_master").findOneAndDelete(new Document("kpiId", "kpi168"));

		// Delete "Sonar Code Quality Kpi" from kpi_category_mapping collection
		mongoTemplate.getCollection("kpi_category_mapping").findOneAndDelete(new Document("kpiId", "kpi168"));
		return true;

	}

	@RollbackExecution
	public void rollback() {
		// do not require the implementation
	}
}
