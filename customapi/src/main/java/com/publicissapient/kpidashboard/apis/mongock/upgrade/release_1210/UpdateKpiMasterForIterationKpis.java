package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1210;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/** prijain3 */
@ChangeUnit(id = "update_kpi_master_for_iteration_kpis", order = "12000", author = "prijain3", systemVersion = "12.1.0")
public class UpdateKpiMasterForIterationKpis {
	private final MongoTemplate mongoTemplate;
	private static final String KPI_MASTER = "kpi_master";
	private static final String KPI_ID = "kpiId";
	private static final String KPI_NAME = "kpiName";
	private static final String CHART_TYPE = "chartType";
	private static final String KPI_WIDTH = "kpiWidth";
	private static final String KPI_HEIGHT = "kpiHeight";
	private static final String DISPLAY_ORDER = "defaultOrder";
	private static final String IS_RAW_DATA = "isRawData";
	private static final String KPI_75 = "kpi75";
	private static final String KPI_136 = "kpi136";
	private static final String KPI_128 = "kpi128";
	private static final String KPI_135 = "kpi135";
	private static final String KPI_120 = "kpi120";
	private static final String KPI_125 = "kpi125";
	private static final String KPI_124 = "kpi124";
	private static final String KPI_122 = "kpi122";
	private static final String KPI_123 = "kpi123";
	private static final String KPI_133 = "kpi133";
	private static final String KPI_131 = "kpi131";
	private static final String KPI_119 = "kpi119";
	private static final String KPI_176 = "kpi176";
	private static final String TABLE_V2 = "table-v2";

	public UpdateKpiMasterForIterationKpis(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection(KPI_MASTER);
		// update KPI names
		changeFieldValue(KPI_75, KPI_NAME, "Estimate vs Actual time", kpiMaster);
		changeFieldValue(KPI_136, KPI_NAME, "Defect Count by", kpiMaster);
		changeFieldValue(KPI_128, KPI_NAME, "Work Status", kpiMaster);
		changeFieldValue(KPI_135, KPI_NAME, "First Time Pass Rate (%)", kpiMaster);

		// update KPI subcategory
		changeFieldValue(KPI_125, "kpiSubCategory", "Iteration Review", kpiMaster);

		deleteDocument(Arrays.asList("kpi132", "kpi140", "kpi145", "kpi134"), kpiMaster);

		// update chart type
		changeFieldValue(KPI_120, CHART_TYPE, "stacked-bar-chart", kpiMaster);
		changeFieldValue(KPI_119, CHART_TYPE, "bar-chart", kpiMaster);
		changeFieldValue(KPI_131, CHART_TYPE, "stacked-bar", kpiMaster);
		changeFieldValue(KPI_75, CHART_TYPE, "bar-chart", kpiMaster);
		changeFieldValue(KPI_136, CHART_TYPE, "chartWithFilter", kpiMaster);
		changeFieldValue(KPI_128, CHART_TYPE, "grouped-bar-chart", kpiMaster);
		changeFieldValue(KPI_124, CHART_TYPE, TABLE_V2, kpiMaster);
		changeFieldValue(KPI_122, CHART_TYPE, TABLE_V2, kpiMaster);
		changeFieldValue(KPI_135, CHART_TYPE, "tabular-with-donut-chart", kpiMaster);
		changeFieldValue(KPI_133, CHART_TYPE, "tableNonRawData", kpiMaster);
		changeFieldValue(KPI_123, CHART_TYPE, TABLE_V2, kpiMaster);
		changeFieldValue(KPI_176, CHART_TYPE, "tableNonRawData", kpiMaster);

		// update KPI Width
		changeFieldValue(KPI_120, KPI_WIDTH, 50, kpiMaster);
		changeFieldValue(KPI_119, KPI_WIDTH, 50, kpiMaster);
		changeFieldValue(KPI_125, KPI_WIDTH, 66, kpiMaster);
		changeFieldValue(KPI_131, KPI_WIDTH, 33, kpiMaster);
		changeFieldValue(KPI_75, KPI_WIDTH, 33, kpiMaster);
		changeFieldValue(KPI_136, KPI_WIDTH, 66, kpiMaster);
		changeFieldValue(KPI_128, KPI_WIDTH, 66, kpiMaster);
		changeFieldValue(KPI_124, KPI_WIDTH, 33, kpiMaster);
		changeFieldValue(KPI_122, KPI_WIDTH, 33, kpiMaster);
		changeFieldValue(KPI_135, KPI_WIDTH, 33, kpiMaster);
		changeFieldValue(KPI_133, KPI_WIDTH, 33, kpiMaster);
		changeFieldValue(KPI_123, KPI_WIDTH, 33, kpiMaster);
		changeFieldValue(KPI_176, KPI_WIDTH, 33, kpiMaster);

		// update KPI Height
		changeFieldValue(KPI_120, KPI_HEIGHT, 100, kpiMaster);
		changeFieldValue(KPI_119, KPI_HEIGHT, 100, kpiMaster);
		changeFieldValue(KPI_125, KPI_HEIGHT, 100, kpiMaster);
		changeFieldValue(KPI_131, KPI_HEIGHT, 100, kpiMaster);
		changeFieldValue(KPI_75, KPI_HEIGHT, 100, kpiMaster);
		changeFieldValue(KPI_136, KPI_HEIGHT, 100, kpiMaster);
		changeFieldValue(KPI_128, KPI_HEIGHT, 100, kpiMaster);
		changeFieldValue(KPI_124, KPI_HEIGHT, 100, kpiMaster);
		changeFieldValue(KPI_122, KPI_HEIGHT, 50, kpiMaster);
		changeFieldValue(KPI_135, KPI_HEIGHT, 50, kpiMaster);
		changeFieldValue(KPI_133, KPI_HEIGHT, 50, kpiMaster);
		changeFieldValue(KPI_123, KPI_HEIGHT, 50, kpiMaster);
		changeFieldValue(KPI_176, KPI_HEIGHT, 50, kpiMaster);

		// update Display Order
		changeFieldValue(KPI_120, DISPLAY_ORDER, 1, kpiMaster);
		changeFieldValue(KPI_119, DISPLAY_ORDER, 2, kpiMaster);
		changeFieldValue(KPI_125, DISPLAY_ORDER, 3, kpiMaster);
		changeFieldValue(KPI_131, DISPLAY_ORDER, 4, kpiMaster);
		changeFieldValue(KPI_75, DISPLAY_ORDER, 5, kpiMaster);
		changeFieldValue(KPI_136, DISPLAY_ORDER, 6, kpiMaster);
		changeFieldValue(KPI_128, DISPLAY_ORDER, 7, kpiMaster);
		changeFieldValue(KPI_124, DISPLAY_ORDER, 8, kpiMaster);
		changeFieldValue(KPI_122, DISPLAY_ORDER, 9, kpiMaster);
		changeFieldValue(KPI_135, DISPLAY_ORDER, 10, kpiMaster);
		changeFieldValue(KPI_133, DISPLAY_ORDER, 11, kpiMaster);
		changeFieldValue(KPI_123, DISPLAY_ORDER, 12, kpiMaster);
		changeFieldValue(KPI_176, DISPLAY_ORDER, 13, kpiMaster);

		// update Raw data kpi flag
		changeFieldValue(KPI_120, IS_RAW_DATA, true, kpiMaster);
		changeFieldValue(KPI_119, IS_RAW_DATA, true, kpiMaster);
		changeFieldValue(KPI_125, IS_RAW_DATA, false, kpiMaster);
		changeFieldValue(KPI_131, IS_RAW_DATA, true, kpiMaster);
		changeFieldValue(KPI_75, IS_RAW_DATA, true, kpiMaster);
		changeFieldValue(KPI_136, IS_RAW_DATA, false, kpiMaster);
		changeFieldValue(KPI_128, IS_RAW_DATA, true, kpiMaster);
		changeFieldValue(KPI_124, IS_RAW_DATA, true, kpiMaster);
		changeFieldValue(KPI_122, IS_RAW_DATA, true, kpiMaster);
		changeFieldValue(KPI_135, IS_RAW_DATA, false, kpiMaster);
		changeFieldValue(KPI_133, IS_RAW_DATA, false, kpiMaster);
		changeFieldValue(KPI_123, IS_RAW_DATA, true, kpiMaster);
		changeFieldValue(KPI_176, IS_RAW_DATA, false, kpiMaster);
	}

	@RollbackExecution
	public void rollback() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection(KPI_MASTER);
		// update KPI names
		changeFieldValue(KPI_75, KPI_NAME, "Estimate vs Actual", kpiMaster);
		changeFieldValue(KPI_136, KPI_NAME, "Defect Count by Status", kpiMaster);
		changeFieldValue(KPI_128, KPI_NAME, "Planned Work Status", kpiMaster);
		changeFieldValue(KPI_135, KPI_NAME, "First Time Pass Rate", kpiMaster);

		// update KPI subcategory
		changeFieldValue(KPI_125, "kpiSubCategory", "Iteration Progress", kpiMaster);

		// update chart type
		removeField(KPI_120, CHART_TYPE, kpiMaster);
		removeField(KPI_119, CHART_TYPE, kpiMaster);
		removeField(KPI_131, CHART_TYPE, kpiMaster);
		removeField(KPI_75, CHART_TYPE, kpiMaster);
		changeFieldValue(KPI_136, CHART_TYPE, "pieChart", kpiMaster);
		removeField(KPI_128, CHART_TYPE, kpiMaster);
		removeField(KPI_124, CHART_TYPE, kpiMaster);
		removeField(KPI_122, CHART_TYPE, kpiMaster);
		removeField(KPI_135, CHART_TYPE, kpiMaster);
		removeField(KPI_133, CHART_TYPE, kpiMaster);
		removeField(KPI_123, CHART_TYPE, kpiMaster);
		removeField(KPI_176, CHART_TYPE, kpiMaster);

		// update KPI Width
		changeFieldValue(KPI_120, KPI_WIDTH, 100, kpiMaster);
		changeFieldValue(KPI_125, KPI_WIDTH, 100, kpiMaster);

		// update Display Order
		changeFieldValue(KPI_120, DISPLAY_ORDER, 1, kpiMaster);
		changeFieldValue(KPI_119, DISPLAY_ORDER, 4, kpiMaster);
		changeFieldValue(KPI_125, DISPLAY_ORDER, 9, kpiMaster);
		changeFieldValue(KPI_131, DISPLAY_ORDER, 10, kpiMaster);
		changeFieldValue(KPI_75, DISPLAY_ORDER, 23, kpiMaster);
		changeFieldValue(KPI_136, DISPLAY_ORDER, 14, kpiMaster);
		changeFieldValue(KPI_128, DISPLAY_ORDER, 2, kpiMaster);
		changeFieldValue(KPI_124, DISPLAY_ORDER, 22, kpiMaster);
		changeFieldValue(KPI_122, DISPLAY_ORDER, 7, kpiMaster);
		changeFieldValue(KPI_135, DISPLAY_ORDER, 12, kpiMaster);
		changeFieldValue(KPI_133, DISPLAY_ORDER, 13, kpiMaster);
		changeFieldValue(KPI_123, DISPLAY_ORDER, 8, kpiMaster);
		changeFieldValue(KPI_176, DISPLAY_ORDER, 6, kpiMaster);

		List<String> iterationKpis = Arrays.asList(KPI_120, KPI_119, KPI_125, KPI_131, KPI_75, KPI_136, KPI_128, KPI_124,
				KPI_122, KPI_135, KPI_133, KPI_123, KPI_176);
		iterationKpis.forEach(kpiId -> {
			removeField(kpiId, KPI_HEIGHT, kpiMaster);
			removeField(kpiId, IS_RAW_DATA, kpiMaster);
		});
		iterationKpis.remove(KPI_120);
		iterationKpis.remove(KPI_125);
		iterationKpis.forEach(kpiId -> removeField(kpiId, KPI_WIDTH, kpiMaster));
	}

	private void changeFieldValue(String kpiId, String field, String value, MongoCollection<Document> kpiMaster) {
		kpiMaster.updateMany(new Document(KPI_ID, new Document("$in", List.of(kpiId))),
				new Document("$set", new Document(field, value)));
	}

	private void changeFieldValue(String kpiId, String field, Integer value, MongoCollection<Document> kpiMaster) {
		kpiMaster.updateMany(new Document(KPI_ID, new Document("$in", List.of(kpiId))),
				new Document("$set", new Document(field, value)));
	}

	private void changeFieldValue(String kpiId, String field, Boolean value, MongoCollection<Document> kpiMaster) {
		kpiMaster.updateMany(new Document(KPI_ID, new Document("$in", List.of(kpiId))),
				new Document("$set", new Document(field, value)));
	}

	private void removeField(String kpiId, String field, MongoCollection<Document> kpiMaster) {
		kpiMaster.updateMany(new Document(KPI_ID, new Document("$in", List.of(kpiId))),
				new Document("$unset", new Document(field, "")));
	}

	private void deleteDocument(List<String> kpiId, MongoCollection<Document> kpiMaster) {
		kpiMaster.deleteMany(new Document(KPI_ID, new Document("$in", kpiId)));
	}
}
