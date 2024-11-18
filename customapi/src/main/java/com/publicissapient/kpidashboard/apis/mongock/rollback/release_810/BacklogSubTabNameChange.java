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

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shunaray
 */
@ChangeUnit(id = "r_backlog_sub_tab_name_change", order = "08113", author = "shunaray", systemVersion = "8.1.0")
public class BacklogSubTabNameChange {
	private final MongoTemplate mongoTemplate;

	public BacklogSubTabNameChange(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		rollbackUpdateKpiSubCategory();

	}
	public void rollbackUpdateKpiSubCategory() {
		List<String> kpiIdsToRollback = Arrays.asList("kpi152", "kpi155", "kpi151");

		Document filter = new Document("kpiId", new Document("$in", kpiIdsToRollback));
		Document update = new Document("$set", new Document("kpiSubCategory", "Summary"));

		mongoTemplate.getCollection("kpi_master").updateMany(filter, update);
	}


	@RollbackExecution
	public void rollback() {
		updateKpiSubCategory();
	}

	public void updateKpiSubCategory() {
		List<String> kpiIdsToUpdate = Arrays.asList("kpi152", "kpi155", "kpi151");

		Document filter = new Document("kpiId", new Document("$in", kpiIdsToUpdate));
		Document update = new Document("$set", new Document("kpiSubCategory", "Backlog Overview"));

		mongoTemplate.getCollection("kpi_master").updateMany(filter, update);
	}

}
