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

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_900;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeUnit(id = "r_rework_rate_kpi", order = "09002", author = "kunkambl", systemVersion = "9.0.0")
public class ReworkRateKpi {

	private final MongoTemplate mongoTemplate;

	public ReworkRateKpi(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		deleteKpiMaster();
	}

	public void insertKpi173() {
		Document kpiDocument = new Document();
		kpiDocument.append("kpiId", "kpi173").append("kpiName", "Rework Rate").append("maxValue", "")
				.append("kpiUnit", "%").append("isDeleted", false).append("defaultOrder", 5).append("groupId", 2)
				.append("kpiSource", "BitBucket").append("kanban", false).append("chartType", "line").append("kpiInfo",
						new Document("definition",
								"Percentage of code changes in which an engineer rewrites code that they recently updated (within the past three weeks)."))
				.append("xAxisLabel", "Weeks").append("yAxisLabel", "Percentage").append("isPositiveTrend", false)
				.append("showTrend", true).append("kpiFilter", "dropDown").append("aggregationCriteria", "average")
				.append("isAdditionalFilterSupport", false).append("calculateMaturity", false)
				.append("hideOverallFilter", true).append("isRepoToolKpi", true).append("kpiCategory", "Developer")
				.append("maturityRange", new String[] { "-80", "80-50", "50-20", "20-5", "5-" });

		mongoTemplate.getCollection("kpi_master").insertOne(kpiDocument);
	}

	@RollbackExecution
	public void rollBack() {
		insertKpi173();
	}

	public void deleteKpiMaster() {
		mongoTemplate.getCollection("kpi_master").deleteOne(new Document("kpiId", "kpi173"));
	}
}
