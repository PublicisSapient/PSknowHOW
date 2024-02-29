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
