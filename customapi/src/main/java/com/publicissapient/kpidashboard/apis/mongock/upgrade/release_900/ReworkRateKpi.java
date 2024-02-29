package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_900;

import io.mongock.api.annotations.ChangeUnit;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeUnit(id = "rework_rate_kpi", order = "9002", author = "kunkambl", systemVersion = "9.0.0")
public class ReworkRateKpi {

	private final MongoTemplate mongoTemplate;

	public ReworkRateKpi(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	public void insertKpi173() {
		Document kpi173 = new Document();
		kpi173.append("kpiId", "kpi173")
				.append("kpiName", "Rework Rate")
				.append("maxValue", 10)
				.append("kpiUnit", "%")
				.append("isDeleted", false)
				.append("defaultOrder", 4)
				.append("groupId", 1)
				.append("kpiSource", "BitBucket")
				.append("kanban", false)
				.append("chartType", "line")
				.append("kpiInfo", new Document("definition", "Percentage of code changes in which an engineer rewrites code that they recently updated (within the past three weeks)."))
				.append("xAxisLabel", "Weeks")
				.append("yAxisLabel", "Percentage")
				.append("isPositiveTrend", false)
				.append("showTrend", true)
				.append("kpiFilter", "dropDown")
				.append("aggregationCriteria", "average")
				.append("isAdditionalFilterSupport", false)
				.append("calculateMaturity", false)
				.append("hideOverallFilter", true)
				.append("isRepoToolKpi", true)
				.append("kpiCategory", "Developer")
				.append("maturityRange", new String[]{"-80", "80-50", "50-20", "20-5", "5-"});

		kpiMasterCollection.insertOne(kpi173);
	}

}
