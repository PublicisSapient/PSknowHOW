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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "r_developer_kpi_integeration", order = "010201", author = "shi6", systemVersion = "10.2.0")
public class DeveloperKPIIntegeration {
	private static final String THRESHOLD = "Target KPI value denotes the bare " +
			"minimum a project should maintain for a KPI. User should just input the number and" +
			" the unit like percentage, hours will automatically be considered." +
			" If the threshold is empty, then a common target KPI line will be shown";
	public static final String WHITE = "white";

	private final MongoTemplate mongoTemplate;

	public DeveloperKPIIntegeration(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		deleteKpiMaster("kpi180");
		deleteKpiMaster("kpi181");
		deleteKpiMaster("kpi182");
		fieldMappingStructureDelete("thresholdValueKPI180");
		fieldMappingStructureDelete("thresholdValueKPI181");
		fieldMappingStructureDelete("thresholdValueKPI182");
	}

	@RollbackExecution
	public void rollBack() {
		List<String> levels = Arrays.asList("-80", "80-50", "50-20", "20-5", "5-");
		insertKpis("kpi180", "Revert Rate", "The percentage of total pull requests opened that are reverts.",
				"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/197263361/Developer+Revert+Rate", levels, false,
				"red", WHITE);
		insertKpis("kpi181", "PR Decline Rate",
				"The percentage of opened Pull Requests that are declined within a timeframe.",
				"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/205357058/Developer+PR+Decline+Rate", levels,
				false, "red", WHITE);
		insertKpis("kpi182", "PR Success Rate",
				"PR success rate measures the number of pull requests that went through the process without being abandoned or discarded as against the total PRs raised in a defined period  A low or declining Pull Request Success Rate represents high or increasing waste",
				"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/75726849/Developer+PR+Success+Rate",
				Arrays.asList("-5", "5-20", "20-50", "50-80", "80-"), true, WHITE, "red");
		fieldMappingStructureInsert("thresholdValueKPI182", THRESHOLD);
		fieldMappingStructureInsert("thresholdValueKPI181", THRESHOLD);
		fieldMappingStructureInsert("thresholdValueKPI180", THRESHOLD);
	}

	public void insertKpis(String kpiId, String kpiName, String kpiInfo, String link, List<String> maturityRange,
			boolean isPositiveTrend, String upperThresholdBG, String lowerThresholdBG) {
		Document kpiDocument = new Document().append("kpiId", kpiId).append("kpiName", kpiName).append("maxValue", "")
				.append("kpiUnit", "%").append("isDeleted", false).append("defaultOrder", 5).append("groupId", 2)
				.append("kpiSource", "BitBucket").append("combinedKpiSource", "Bitbucket/AzureRepository/GitHub/GitLab")
				.append("kanban", false).append("chartType", "line")
				.append("kpiInfo",
						new Document().append("definition", kpiInfo).append("details",
								Collections.singletonList(new Document().append("type", "link").append("kpiLinkDetail",
										new Document().append("text", "Detailed Information at").append("link", link)))))
				.append("xAxisLabel", "Weeks").append("yAxisLabel", "Percentage").append("isPositiveTrend", isPositiveTrend)
				.append("upperThresholdBG", upperThresholdBG).append("lowerThresholdBG", lowerThresholdBG)
				.append("thresholdValue", "50").append("showTrend", true).append("kpiFilter", "dropDown")
				.append("aggregationCriteria", "average").append("isAdditionalFilterSupport", false)
				.append("calculateMaturity", false).append("hideOverallFilter", true).append("isRepoToolKpi", true)
				.append("kpiCategory", "Developer").append("maturityRange", maturityRange);

		mongoTemplate.getCollection("kpi_master").insertOne(kpiDocument);
	}

	public void fieldMappingStructureInsert(String fieldName, String toolTip) {
		Document thresholdValueMapping = new Document("fieldName", fieldName).append("fieldLabel", "Target KPI Value")
				.append("fieldType", "number").append("section", "Project Level Threshold").append("fieldDisplayOrder", "1")
				.append("sectionOrder", "6").append("tooltip", new Document("definition", toolTip));

		mongoTemplate.getCollection("field_mapping_structure").insertOne(thresholdValueMapping);
	}

	public void deleteKpiMaster(String kpiId) {
		mongoTemplate.getCollection("kpi_master").deleteOne(new Document("kpiId", kpiId));
	}

	public void fieldMappingStructureDelete(String fieldName) {
		mongoTemplate.getCollection("field_mapping_structure").deleteOne(new Document("fieldName", fieldName));
	}
}
