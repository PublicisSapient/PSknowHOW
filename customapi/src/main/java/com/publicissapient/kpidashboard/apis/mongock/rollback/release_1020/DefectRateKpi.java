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

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1020;

import java.util.Collections;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "r_defect_rate_kpi", order = "010204", author = "kunkambl", systemVersion = "10.2.0")
public class DefectRateKpi {

	private final MongoTemplate mongoTemplate;

	public DefectRateKpi(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		deleteKpiMaster();
		fieldMappingStructureDelete();
	}

	public void insertKpi186() {
		Document kpiDocument = new Document().append("kpiId", "kpi186").append("kpiName", "Defect Rate")
				.append("maxValue", "").append("kpiUnit", "%").append("isDeleted", false).append("defaultOrder", 10)
				.append("groupId", 2).append("kpiSource", "BitBucket")
				.append("combinedKpiSource", "Bitbucket/AzureRepository/GitHub/GitLab").append("kanban", false)
				.append("chartType", "line")
				.append("kpiInfo", new Document()
						.append("definition", "The percentage of merged pull requests that are addressing defects.")
						.append("details", Collections.singletonList(new Document().append("type", "link").append("kpiLinkDetail",
								new Document().append("text", "Detailed Information at").append("link",
										"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/226394113/Developer+Defect+Rate")))))
				.append("xAxisLabel", "Days/Weeks/Months").append("yAxisLabel", "Percentage").append("isPositiveTrend", false)
				.append("upperThresholdBG", "red").append("lowerThresholdBG", "white").append("showTrend", true)
				.append("kpiFilter", "dropDown").append("aggregationCriteria", "average")
				.append("isAdditionalFilterSupport", false).append("calculateMaturity", false).append("hideOverallFilter", true)
				.append("isRepoToolKpi", true).append("kpiCategory", "Developer").append("maturityRange", null);

		mongoTemplate.getCollection("kpi_master").insertOne(kpiDocument);
	}

	public void fieldMappingStructureInsert() {
		Document thresholdValueMapping = new Document("fieldName", "thresholdValueKPI186")
				.append("fieldLabel", "Target KPI Value").append("fieldType", "number")
				.append("section", "Project Level Threshold").append("tooltip",
						new Document("definition",
								"Target KPI value denotes the bare " +
										"minimum a project should maintain for a KPI. User should just input the number and" +
										" the unit like percentage, hours will automatically be considered." +
										" If the threshold is empty, then a common target KPI line will be shown"));

		mongoTemplate.getCollection("field_mapping_structure").insertOne(thresholdValueMapping);
	}

	@RollbackExecution
	public void rollBack() {
		insertKpi186();
		fieldMappingStructureInsert();
	}

	public void deleteKpiMaster() {
		mongoTemplate.getCollection("kpi_master").deleteOne(new Document("kpiId", "kpi186"));
	}

	public void fieldMappingStructureDelete() {
		mongoTemplate.getCollection("field_mapping_structure").deleteOne(new Document("fieldName", "thresholdValueKPI186"));
	}
}
