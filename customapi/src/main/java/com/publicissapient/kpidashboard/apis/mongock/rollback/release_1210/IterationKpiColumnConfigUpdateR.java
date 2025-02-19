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

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1210;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1210.IterationKpiColumnConfigUpdate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author prijain3
 */
@ChangeUnit(id = "r_iteration_kpi_column_config_update", order = "012105", author = "prijain3", systemVersion = "12.1.0")
public class IterationKpiColumnConfigUpdateR {

	IterationKpiColumnConfigUpdate kpiColumnConfigUpdate;

	public IterationKpiColumnConfigUpdateR(MongoTemplate mongoTemplate) {
		kpiColumnConfigUpdate = new IterationKpiColumnConfigUpdate(mongoTemplate);
	}

	@Execution
	public void execution() {
		kpiColumnConfigUpdate.rollback();
	}

	@RollbackExecution
	public void rollback() {
		kpiColumnConfigUpdate.execution();
	}
}
