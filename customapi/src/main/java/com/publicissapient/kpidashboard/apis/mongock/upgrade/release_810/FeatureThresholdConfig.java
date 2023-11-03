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
package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_810;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shi6
 */
@ChangeUnit(id = "feature_threshold_config", order = "8101", author = "shi6", systemVersion = "8.1.0")
public class FeatureThresholdConfig {

	private final MongoTemplate mongoTemplate;

	public FeatureThresholdConfig(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		bulkUpdateKpiMaster();
	}

	public void bulkUpdateKpiMaster() {
		Bson filter = Filters.in("kpiId", "kpi150");
		Bson update = Updates.set("defaultOrder", 1);
		mongoTemplate.getCollection("kpi_master").updateMany(filter, update);

	}

	@RollbackExecution
	public void rollback() {
		rollbackKpiMaster();
	}

	public void rollbackKpiMaster() {
		//provide rollback script
	}
}
