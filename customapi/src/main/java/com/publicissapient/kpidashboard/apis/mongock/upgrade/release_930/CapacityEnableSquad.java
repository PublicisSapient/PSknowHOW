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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_930;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shi6
 */
@SuppressWarnings("java:S1192")
@ChangeUnit(id = "capacity_enable_Squad", order = "9301", author = "shi6", systemVersion = "9.3.0")
public class CapacityEnableSquad {

	private final MongoTemplate mongoTemplate;

	public CapacityEnableSquad(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {

		updateKpi("kpi58", true);
		updateKpi("kpi46", true);
		updateKpi("kpi121", true);
	}

	@RollbackExecution
	public void rollback() {
		updateKpi("kpi58", false);
		updateKpi("kpi46", false);
		updateKpi("kpi121", false);
	}

	private void updateKpi(String kpiId, boolean isAdditionalFilterSupport) {
		mongoTemplate.getCollection("kpi_master").updateOne(new Document("kpiId", kpiId),
				new Document("$set", new Document("isAdditionalFilterSupport", isAdditionalFilterSupport)));
	}
}
