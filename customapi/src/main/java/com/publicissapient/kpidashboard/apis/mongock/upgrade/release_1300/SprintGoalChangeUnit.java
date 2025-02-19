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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1300;

import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shunaray
 */
@ChangeUnit(id = "sprint_goals", order = "", author = "shunaray", systemVersion = "13.0.0")//todo::write in confluence
public class SprintGoalChangeUnit {

	private final MongoTemplate mongoTemplate;

	public SprintGoalChangeUnit(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		Document kpiDocument = new Document().append("kpiId", "kpi187").append("kpiName", "Sprint Goals")
				.append("isDeleted", "False").append("defaultOrder", 31).append("kpiUnit", "")
				.append("showTrend", false).append("calculateMaturity", false).append("hideOverallFilter", false)
				.append("kpiSource", "Jira").append("kanban", false).append("groupId", 32)
				.append("kpiInfo", new Document("details",
						List.of(new Document("type", "paragraph").append("value", "KPI for tracking Goals of project."),
								new Document("type", "link").append("kpiLinkDetail",
										new Document("text", "Detailed Information at").append("link", "")))))
				.append("isTrendCalculative", false).append("isAdditionalFilterSupport", false)
				.append("combinedKpiSource", "Jira/Azure");

		mongoTemplate.getCollection("kpi_master").insertOne(kpiDocument);

	}

	@RollbackExecution
	public void rollback() {
		mongoTemplate.getCollection("kpi_master").deleteOne(new Document("kpiId", "kpi187"));
	}

}
