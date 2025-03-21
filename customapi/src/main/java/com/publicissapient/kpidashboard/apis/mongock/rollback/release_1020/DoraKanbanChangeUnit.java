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
package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1020;

import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shunaray
 */
@ChangeUnit(id = "r_dora_kanban_kpi", order = "010202", author = "shunaray", systemVersion = "10.2.0")
public class DoraKanbanChangeUnit {
	public static final String KPI_ID = "kpiId";
	public static final String LEVEL = "level";
	public static final String BG_COLOR = "bgColor";
	public static final String DISPLAY_RANGE = "displayRange";
	public static final String LABEL = "label";
	public static final String BOARD_ID = "boardId";
	private final MongoTemplate mongoTemplate;

	public DoraKanbanChangeUnit(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		rollbackKPIDocs();
		updateFilterBoardId(7, 14);
		updateFilterBoardId(15, 13);
		updateFilterBoardId(16, 15);
		updateFilterBoardId(17, 16);
		updateProjectTypeSwitchEnabled(14, false);
	}

	private void rollbackKPIDocs() {
		mongoTemplate.getCollection("kpi_master")
				.deleteMany(new Document(KPI_ID, new Document("$in", List.of("kpi184", "kpi183"))));
	}

	private void updateProjectTypeSwitchEnabled(int boardId, boolean enabled) {
		mongoTemplate.getCollection("filters").updateMany(new Document(BOARD_ID, boardId),
				new Document("$set", new Document("projectTypeSwitch.enabled", enabled)));
	}

	@RollbackExecution
	public void rollback() {
		insertDoraKanban();
		updateFilterBoardId(14, 7);
		updateFilterBoardId(16, 17);
		updateFilterBoardId(15, 16);
		updateFilterBoardId(13, 15);
		updateProjectTypeSwitchEnabled(7, true);
	}

	public void insertDoraKanban() {
		List<Document> kpiDocuments = List.of(
				new Document().append(KPI_ID, "kpi184").append("kpiName", "Change Failure Rate").append("isDeleted", "False")
						.append("defaultOrder", 19).append("kpiCategory", "Dora").append("kpiUnit", "%").append("chartType", "line")
						.append("upperThresholdBG", "red").append("lowerThresholdBG", "white").append("xAxisLabel", "Weeks")
						.append("yAxisLabel", "Percentage").append("showTrend", true).append("isPositiveTrend", false)
						.append("calculateMaturity", true).append("hideOverallFilter", true).append("kpiSource", "Jenkins")
						.append("maxValue", "100").append("thresholdValue", "30").append("kanban", true).append("groupId", 5)
						.append("kpiInfo", new Document()
								.append("definition", "Measures the proportion of builds that have failed over a given period of time")
								.append(
										"formula",
										List.of(new Document()
												.append("lhs", "Change Failure Rate").append("operator", "division")
												.append("operands", List.of("Total number of failed Builds", "Total number of Builds"))))
								.append("details", List.of(new Document().append("type", "link").append("kpiLinkDetail",
										new Document().append("text", "Detailed Information at").append("link",
												"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/71958608/DORA+Change+Failure+Rate"))))
								.append("_class", "com.publicissapient.kpidashboard.common.model.application.KpiInfo"))
						.append("kpiFilter", "dropDown").append("aggregationCriteria", "average")
						.append("aggregationCircleCriteria", "average").append("isTrendCalculative", false)
						.append("isAdditionalFilterSupport", false)
						.append("maturityRange", List.of("-60", "60-45", "45-30", "30-15", "15-"))
						.append("maturityLevel",
								List.of(new Document().append(LEVEL, "M5").append(BG_COLOR, "#167a26").append(DISPLAY_RANGE, "0-15 %"),
										new Document().append(LEVEL, "M4").append(BG_COLOR, "#4ebb1a").append(DISPLAY_RANGE, "15-30 %"),
										new Document().append(LEVEL, "M3").append(BG_COLOR, "#ef7643").append(DISPLAY_RANGE, "30-45 %"),
										new Document().append(LEVEL, "M2").append(BG_COLOR, "#f53535").append(DISPLAY_RANGE, "45-60 %"),
										new Document().append(LEVEL, "M1").append(BG_COLOR, "#c91212").append(DISPLAY_RANGE,
												"60 % and Above")))
						.append("combinedKpiSource", "Jenkins/Bamboo/GitHubAction/AzurePipeline/Teamcity"),
				new Document().append(KPI_ID, "kpi183").append("kpiName", "Deployment Frequency").append("isDeleted", "False")
						.append("defaultOrder", 20).append("kpiCategory", "Dora").append("kpiUnit", "Number")
						.append("chartType", "line").append("upperThresholdBG", "white").append("lowerThresholdBG", "red")
						.append("xAxisLabel", "Weeks").append("yAxisLabel", "Count").append("showTrend", true)
						.append("isPositiveTrend", true).append("calculateMaturity", true).append("hideOverallFilter", false)
						.append("kpiSource", "Jenkins").append("maxValue", "100").append("thresholdValue", "6")
						.append("kanban", true).append("groupId", 5)
						.append("kpiInfo", new Document()
								.append("definition", "Measures how often code is deployed to production in a period")
								.append("details", List.of(new Document().append("type", "link").append("kpiLinkDetail",
										new Document().append("text", "Detailed Information at").append("link",
												"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/71827544/DORA+Deployment+Frequency"))))
								.append("_class", "com.publicissapient.kpidashboard.common.model.application.KpiInfo"))
						.append("kpiFilter", "multiSelectDropDown").append("aggregationCriteria", "sum")
						.append("aggregationCircleCriteria", "sum").append("isTrendCalculative", false)
						.append("isAdditionalFilterSupport", false)
						.append("maturityRange", List.of("0-2", "2-4", "4-6", "6-8", "8-"))
						.append("maturityLevel",
								List.of(
										new Document().append(LEVEL, "M5").append(BG_COLOR, "#167a26").append(LABEL, ">= 2 per week")
												.append(DISPLAY_RANGE, "8 and Above"),
										new Document().append(LEVEL, "M4").append(BG_COLOR, "#4ebb1a").append(LABEL, "Once per week")
												.append(DISPLAY_RANGE, "6,7"),
										new Document().append(LEVEL, "M3").append(BG_COLOR, "#ef7643").append(LABEL, "Once in 2 weeks")
												.append(DISPLAY_RANGE, "4,5"),
										new Document().append(LEVEL, "M2").append(BG_COLOR, "#f53535").append(LABEL, "Once in 4 weeks")
												.append(DISPLAY_RANGE, "2,3"),
										new Document().append(LEVEL, "M1").append(BG_COLOR, "#c91212").append(LABEL, "< Once in 8 weeks")
												.append(DISPLAY_RANGE, "0,1")))
						.append("combinedKpiSource", "Jenkins/Bamboo/GitHubAction/AzurePipeline/Teamcity"));

		mongoTemplate.getCollection("kpi_master").insertMany(kpiDocuments);
	}

	/**
	 * Moving dora to scrum, kanban thus changing the boardId
	 *
	 * @param oldBoardId
	 *          older board id
	 * @param newBoardId
	 *          new board id
	 */
	private void updateFilterBoardId(int oldBoardId, int newBoardId) {
		mongoTemplate.getCollection("filters").updateMany(new Document(BOARD_ID, oldBoardId),
				new Document("$set", new Document(BOARD_ID, newBoardId)));
	}
}
