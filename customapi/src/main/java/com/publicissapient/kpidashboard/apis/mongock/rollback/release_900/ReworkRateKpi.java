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

import java.util.Arrays;

@ChangeUnit(id = "r_rework_rate_kpi", order = "09002", author = "kunkambl", systemVersion = "9.0.0")
public class ReworkRateKpi {

	private final MongoTemplate mongoTemplate;

	public ReworkRateKpi(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		deleteKpiMaster();
		fieldMappingStructureDelete();
	}

	public void insertKpi173() {
		Document kpiDocument = new Document().append("kpiId", "kpi173").append("kpiName", "Rework Rate")
				.append("maxValue", "").append("kpiUnit", "%").append("isDeleted", false).append("defaultOrder", 5)
				.append("groupId", 2).append("kpiSource", "BitBucket").append("kanban", false)
				.append("chartType", "line").append("kpiInfo", new Document().append("definition",
								"Percentage of code changes in which an engineer rewrites code that they recently updated (within the past three weeks).")
						.append("details", Arrays.asList(new Document().append("type", "link").append("kpiLinkDetail",
								new Document().append("text", "Detailed Information at").append("link",
										"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/106528769/Developer+Rework+Rate")))))
				.append("xAxisLabel", "Weeks").append("yAxisLabel", "Percentage").append("isPositiveTrend", false)
				.append("upperThresholdBG", "red").append("lowerThresholdBG", "white").append("thresholdValue","50").append("showTrend", true)
				.append("kpiFilter", "dropDown").append("aggregationCriteria", "average")
				.append("isAdditionalFilterSupport", false).append("calculateMaturity", false)
				.append("hideOverallFilter", true).append("isRepoToolKpi", true).append("kpiCategory", "Developer")
				.append("maturityRange", Arrays.asList("-80", "80-50", "50-20", "20-5", "5-"));

		mongoTemplate.getCollection("kpi_master").insertOne(kpiDocument);
	}

	public void fieldMappingStructureInsert() {
		Document thresholdValueMapping = new Document("fieldName", "thresholdValueKPI173")
				.append("fieldLabel", "Target KPI Value").append("fieldType", "number")
				.append("section", "Custom Fields Mapping").append("tooltip",
						new Document("definition", "Target KPI value denotes the bare "
								+ "minimum a project should maintain for a KPI. User should just input the number and"
								+ " the unit like percentage, hours will automatically be considered."
								+ " If the threshold is empty, then a common target KPI line will be shown"));

		mongoTemplate.getCollection("field_mapping_structure").insertOne(thresholdValueMapping);
	}

	@RollbackExecution
	public void rollBack() {
		insertKpi173();
		fieldMappingStructureInsert();
	}

	public void deleteKpiMaster() {
		mongoTemplate.getCollection("kpi_master").deleteOne(new Document("kpiId", "kpi173"));
	}

	public void fieldMappingStructureDelete() {
		mongoTemplate.getCollection("field_mapping_structure")
				.deleteOne(new Document("fieldName", "thresholdValueKPI173"));
	}
}
