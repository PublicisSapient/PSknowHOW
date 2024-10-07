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
package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1100;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * @author purgupta2
 */
@ChangeUnit(id = "change_iteration_kpis_groupId", order = "11001", author = "purgupta2", systemVersion = "11.0.0")
public class ChangeIterationKPIsGroupId {
	private final MongoTemplate mongoTemplate;

	public ChangeIterationKPIsGroupId(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		updateIterationKPIsGroupId();
	}

	private void updateIterationKPIsGroupId() {
		updateGroupId(Arrays.asList("kpi119", "kpi132", "kpi136", "kpi140"), 17);
		updateGroupId(Arrays.asList("kpi123", "kpi122", "kpi134", "kpi131"), 18);
		updateGroupId(Arrays.asList("kpi75", "kpi124", "kpi135", "kpi176"), 19);
		updateGroupId(List.of("kpi125"), 20);
	}

	private void updateGroupId(List<String> kpiIds, int groupId) {
		mongoTemplate.getCollection("kpi_master").updateMany(new Document("kpiId", new Document("$in", kpiIds)),
				new Document("$set", new Document("groupId", groupId)));
	}

	@RollbackExecution
	public void rollBack() {
		updateGroupId(Arrays.asList("kpi119", "kpi132", "kp136", "kpi140", "kpi123", "kpi122", "kpi134", "kpi131",
				"kpi75", "kpi124", "kpi135", "kpi176", "kpi125"), 8);
	}
}
