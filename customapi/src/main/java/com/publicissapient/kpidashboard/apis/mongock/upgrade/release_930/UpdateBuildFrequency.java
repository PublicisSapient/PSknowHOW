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
@ChangeUnit(id = "update_build_frequency", order = "9302", author = "kunkambl", systemVersion = "9.3.0")
public class UpdateBuildFrequency {

	private final MongoTemplate mongoTemplate;
	private static final String KPI_ID = "kpi172";

	public UpdateBuildFrequency(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		Update update = new Update();
		update.set("maturityRange", Arrays.asList("1-2", "2-4", "5-8", "8-10", "10-"));
		mongoTemplate.updateFirst(getQueryByKpiId(), update, "kpi_master");
	}

	@RollbackExecution
	public void rollback() {
		Update update = new Update();
		update.set("maturityRange", Arrays.asList("-1", "2-4", "5-8", "8-10", "10-"));
		mongoTemplate.updateFirst(getQueryByKpiId(), update, "kpi_master");
	}

	private Query getQueryByKpiId() {
		Query query = new Query();
		query.addCriteria(Criteria.where("kpiId").is(KPI_ID));
		return query;
	}
}
