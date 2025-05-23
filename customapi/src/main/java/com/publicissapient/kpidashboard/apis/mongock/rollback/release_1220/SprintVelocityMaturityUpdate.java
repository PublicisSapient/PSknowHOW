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

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1220;

import java.util.Arrays;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author kunkambl
 */
@ChangeUnit(id = "r_update_sprint_velocity_maturity", order = "012201", author = "kunkambl", systemVersion = "12.2.0")
public class SprintVelocityMaturityUpdate {
	private MongoTemplate mongoTemplate;

	public SprintVelocityMaturityUpdate(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execute() {
		Query query = new Query(Criteria.where("kpiId").is("kpi39"));
		Update update = new Update();
		update.set("maturityRange", null);
		update.set("calculateMaturity", false);
		mongoTemplate.updateFirst(query, update, "kpi_master");
	}

	@RollbackExecution
	public void rollback() {
		Query query = new Query(Criteria.where("kpiId").is("kpi39"));
		Update update = new Update();
		update.set("maturityRange", Arrays.asList("1-2", "2-3", "3-4", "4-5", "5-6"));
		update.set("calculateMaturity", true);
		mongoTemplate.updateFirst(query, update, "kpi_master");
	}
}
