package com.publicissapient.kpidashboard.apis.mongock.upgrade;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Arrays;
import java.util.List;

@Slf4j
@ChangeUnit(id = "Release_8.0.0", order = "007", author = "PSKnowHOW", systemVersion = "8.0.0")
public class Release_8_0_0 {
	private final MongoTemplate mongoTemplate;
	private static final String KPI_MASTER_COLLECTION = "kpi_master";
	private static final String FIELD_MAPPING_STRUCTURE_COLLECTION = "field_mapping_structure";

	public Release_8_0_0(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void changeSet() {
		insertStartDateCountFieldStructure();
		insertKpi165();
		insertKpi169();
		updateKPI(Arrays.asList("kpi40", "kpi46", "kpi164"), Update.update("groupId", 5));
		updateKPI(Arrays.asList("kpi14"), Update.update("groupId", 3));
		updateKPI(Arrays.asList("kpi149"), Update.update("groupId", 16));
		updateKPI(Arrays.asList("kpi152", "kpi155", "kpi151"), Update.update("kpiSubCategory", "Summary"));
		updateKPI(Arrays.asList("kpi139", "kpi138", "kpi127", "kpi137", "kpi129", "kpi161"),
				Update.update("kpiSubCategory", "Backlog Health"));
		updateKPI(Arrays.asList("kpi3", "kpi148", "kpi146"), Update.update("kpiSubCategory", "Flow KPIs"));
		updateKPI(Arrays.asList("kpi147", "kpi150"), Update.update("kpiSubCategory", "Speed"));
		updateKPI(Arrays.asList("kpi141", "kpi142", "kpi143", "kpi144", "kpi163"),
				Update.update("kpiSubCategory", "Quality"));
		updateKPI(Arrays.asList("kpi150", "kpi147", "kpi3"), Update.update("kpiWidth", 100));
		updateKPI(Arrays.asList("kpi150"), Update.update("defaultOrder", 1));
	}

	private void insertStartDateCountFieldStructure() {
		Query query = new Query(Criteria.where("fieldName").is("startDateCountKPI150"));
		if (!mongoTemplate.exists(query, FIELD_MAPPING_STRUCTURE_COLLECTION)) {
			Document document = new Document();
			document.append("fieldName", "startDateCountKPI150")
					.append("fieldLabel",
							"Count of days from the release start date to calculate closure rate for prediction")
					.append("fieldType", "number").append("section", "Issue Types Mapping");

			Document tooltip = new Document();
			tooltip.append("definition",
					"If this field is kept blank, then daily closure rate of issues is calculated based on the number of working days between today and the release start date or date when the first issue was added. This configuration allows you to decide from which date the closure rate should be calculated.");
			document.append("tooltip", tooltip);
			mongoTemplate.insert(document, FIELD_MAPPING_STRUCTURE_COLLECTION);
		}
	}

	private void updateKPI(List<String> kpiIds, Update update) {
		Query updateQuery = Query.query(Criteria.where("kpiId").in(kpiIds));
		mongoTemplate.updateMulti(updateQuery, update, KPI_MASTER_COLLECTION);
	}

	private void insertKpi165() {
		Query query1 = new Query(Criteria.where("kpiId").is("kpi165"));
		if (!mongoTemplate.exists(query1, KPI_MASTER_COLLECTION)) {
			Document document = new Document("kpiId", "kpi165").append("kpiName", "Epic Progress")
					.append("maxValue", "").append("kpiUnit", "Count").append("isDeleted", "False")
					.append("defaultOrder", 5).append("kpiCategory", "Release").append("kpiSubCategory", "Value")
					.append("kpiSource", "Jira").append("groupId", 9).append("thresholdValue", "")
					.append("kanban", false).append("chartType", "horizontalPercentBarChart")
					.append("kpiInfo", new Document("definition",
							"It depicts the progress of each epic in a release in terms of total count and %age completion."))
					.append("xAxisLabel", "").append("yAxisLabel", "").append("kpiWidth", 100)
					.append("isPositiveTrend", true).append("showTrend", false)
					.append("isAdditionalFilterSupport", false).append("kpiFilter", "multiSelectDropDown")
					.append("boxType", "chart").append("calculateMaturity", false);
			mongoTemplate.insert(document, KPI_MASTER_COLLECTION);
		}
	}

	private void insertKpi169() {
		Query query = new Query(Criteria.where("kpiId").is("kpi169"));
		if (!mongoTemplate.exists(query, KPI_MASTER_COLLECTION)) {
			Document document = new Document("kpiId", "kpi169").append("kpiName", "Epic Progress")
					.append("maxValue", "").append("kpiUnit", "Count").append("isDeleted", "False")
					.append("defaultOrder", 5).append("kpiCategory", "Backlog").append("kpiSource", "Jira")
					.append("groupId", 9).append("thresholdValue", "").append("kanban", false)
					.append("chartType", "horizontalPercentBarChart")
					.append("kpiInfo", new Document("definition",
							"It depicts the progress of each epic in terms of total count and %age completion."))
					.append("xAxisLabel", "").append("yAxisLabel", "").append("kpiWidth", 100)
					.append("isPositiveTrend", true).append("showTrend", false)
					.append("isAdditionalFilterSupport", false).append("kpiFilter", "multiSelectDropDown")
					.append("boxType", "chart").append("calculateMaturity", false)
					.append("kpiSubCategory", "Epic View");
			mongoTemplate.insert(document, KPI_MASTER_COLLECTION);
		}
	}

	@RollbackExecution
	public void rollback() {
		// As this represents the base version, it's important to note that you cannot
		// roll back any further so use rollback script.
	}

}
