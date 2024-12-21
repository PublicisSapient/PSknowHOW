package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1210;

import com.mongodb.client.MongoCollection;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * prijain3
 */
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

	public UpdateKpiMasterForIterationKpis(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection(KPI_MASTER);
        //update KPI names
        changeFieldValue("kpi75", KPI_NAME, "Estimate vs Actual time", kpiMaster);
        changeFieldValue("kpi136", KPI_NAME, "Defect Count by", kpiMaster);
        changeFieldValue("kpi128", KPI_NAME, "Work Status", kpiMaster);
        changeFieldValue("kpi135", KPI_NAME, "First Time Pass Rate (%)", kpiMaster);

		//update KPI subcategory
		changeFieldValue("kpi125", "kpiSubCategory", "Iteration Review", kpiMaster);

		deleteDocument(Arrays.asList("kpi132", "kpi140", "kpi145", "kpi134"), kpiMaster);

		//update chart type
		changeFieldValue("kpi120", CHART_TYPE, "stacked-bar-chart", kpiMaster);
		changeFieldValue("kpi119", CHART_TYPE, "bar-chart", kpiMaster);
		changeFieldValue("kpi131", CHART_TYPE, "stacked-bar", kpiMaster);
		changeFieldValue("kpi75", CHART_TYPE, "bar-chart", kpiMaster);
		changeFieldValue("kpi136", CHART_TYPE, "chartWithFilter", kpiMaster);
		changeFieldValue("kpi128", CHART_TYPE, "grouped-bar-chart", kpiMaster);
		changeFieldValue("kpi124", CHART_TYPE, "table-v2", kpiMaster);
		changeFieldValue("kpi122", CHART_TYPE, "table-v2", kpiMaster);
		changeFieldValue("kpi135", CHART_TYPE, "tabular-with-donut-chart", kpiMaster);
		changeFieldValue("kpi133", CHART_TYPE, "tableNonRawData", kpiMaster);
		changeFieldValue("kpi123", CHART_TYPE, "table-v2", kpiMaster);

		//update KPI Width
		changeFieldValue("kpi120", KPI_WIDTH, 50, kpiMaster);
		changeFieldValue("kpi119", KPI_WIDTH, 50, kpiMaster);
		changeFieldValue("kpi125", KPI_WIDTH, 66, kpiMaster);
		changeFieldValue("kpi131", KPI_WIDTH, 33, kpiMaster);
		changeFieldValue("kpi75", KPI_WIDTH, 33, kpiMaster);
		changeFieldValue("kpi136", KPI_WIDTH, 66, kpiMaster);
		changeFieldValue("kpi128", KPI_WIDTH, 66, kpiMaster);
		changeFieldValue("kpi124", KPI_WIDTH, 33, kpiMaster);
		changeFieldValue("kpi122", KPI_WIDTH, 33, kpiMaster);
		changeFieldValue("kpi135", KPI_WIDTH, 33, kpiMaster);
		changeFieldValue("kpi133", KPI_WIDTH, 33, kpiMaster);
		changeFieldValue("kpi123", KPI_WIDTH, 33, kpiMaster);

		//update KPI Height
		changeFieldValue("kpi120", KPI_HEIGHT, 100, kpiMaster);
		changeFieldValue("kpi119", KPI_HEIGHT, 100, kpiMaster);
		changeFieldValue("kpi125", KPI_HEIGHT, 100, kpiMaster);
		changeFieldValue("kpi131", KPI_HEIGHT, 100, kpiMaster);
		changeFieldValue("kpi75", KPI_HEIGHT, 100, kpiMaster);
		changeFieldValue("kpi136", KPI_HEIGHT, 100, kpiMaster);
		changeFieldValue("kpi128", KPI_HEIGHT, 100, kpiMaster);
		changeFieldValue("kpi124", KPI_HEIGHT, 100, kpiMaster);
		changeFieldValue("kpi122", KPI_HEIGHT, 50, kpiMaster);
		changeFieldValue("kpi135", KPI_HEIGHT, 50, kpiMaster);
		changeFieldValue("kpi133", KPI_HEIGHT, 50, kpiMaster);
		changeFieldValue("kpi123", KPI_HEIGHT, 50, kpiMaster);

		//update Display Order
		changeFieldValue("kpi120", DISPLAY_ORDER, 1, kpiMaster);
		changeFieldValue("kpi119", DISPLAY_ORDER, 2, kpiMaster);
		changeFieldValue("kpi125", DISPLAY_ORDER, 3, kpiMaster);
		changeFieldValue("kpi131", DISPLAY_ORDER, 4, kpiMaster);
		changeFieldValue("kpi75", DISPLAY_ORDER, 5, kpiMaster);
		changeFieldValue("kpi136", DISPLAY_ORDER, 6, kpiMaster);
		changeFieldValue("kpi128", DISPLAY_ORDER, 7, kpiMaster);
		changeFieldValue("kpi124", DISPLAY_ORDER, 8, kpiMaster);
		changeFieldValue("kpi122", DISPLAY_ORDER, 9, kpiMaster);
		changeFieldValue("kpi135", DISPLAY_ORDER, 10, kpiMaster);
		changeFieldValue("kpi133", DISPLAY_ORDER, 11, kpiMaster);
		changeFieldValue("kpi123", DISPLAY_ORDER, 12, kpiMaster);

		//update Raw data kpi flag
		changeFieldValue("kpi120", IS_RAW_DATA, true, kpiMaster);
		changeFieldValue("kpi119", IS_RAW_DATA, true, kpiMaster);
		changeFieldValue("kpi125", IS_RAW_DATA, false, kpiMaster);
		changeFieldValue("kpi131", IS_RAW_DATA, true, kpiMaster);
		changeFieldValue("kpi75", IS_RAW_DATA, true, kpiMaster);
		changeFieldValue("kpi136", IS_RAW_DATA, false, kpiMaster);
		changeFieldValue("kpi128", IS_RAW_DATA, true, kpiMaster);
		changeFieldValue("kpi124", IS_RAW_DATA, true, kpiMaster);
		changeFieldValue("kpi122", IS_RAW_DATA, true, kpiMaster);
		changeFieldValue("kpi135", IS_RAW_DATA, false, kpiMaster);
		changeFieldValue("kpi133", IS_RAW_DATA, false, kpiMaster);
		changeFieldValue("kpi123", IS_RAW_DATA, true, kpiMaster);
	}

	@RollbackExecution
	public void rollback() {
        MongoCollection<Document> kpiMaster = mongoTemplate.getCollection(KPI_MASTER);
		// update KPI names
		changeFieldValue("kpi75", KPI_NAME, "Estimate vs Actual", kpiMaster);
		changeFieldValue("kpi136", KPI_NAME, "Defect Count by Status", kpiMaster);
		changeFieldValue("kpi128", KPI_NAME, "Planned Work Status", kpiMaster);
		changeFieldValue("kpi135", KPI_NAME, "First Time Pass Rate", kpiMaster);

		//update KPI subcategory
		changeFieldValue("kpi125", "kpiSubCategory", "Iteration Progress", kpiMaster);
		
		//update chart type
		removeField("kpi120", CHART_TYPE, kpiMaster);
		removeField("kpi119", CHART_TYPE, kpiMaster);
		removeField("kpi131", CHART_TYPE, kpiMaster);
		removeField("kpi75", CHART_TYPE, kpiMaster);
		changeFieldValue("kpi136", CHART_TYPE, "pieChart", kpiMaster);
		removeField("kpi128", CHART_TYPE, kpiMaster);
		removeField("kpi124", CHART_TYPE, kpiMaster);
		removeField("kpi122", CHART_TYPE, kpiMaster);
		removeField("kpi135", CHART_TYPE, kpiMaster);
		removeField("kpi133", CHART_TYPE, kpiMaster);
		removeField("kpi123", CHART_TYPE, kpiMaster);

		//update KPI Width
		changeFieldValue("kpi120", KPI_WIDTH, 100, kpiMaster);
		changeFieldValue("kpi125", KPI_WIDTH, 100, kpiMaster);

		//update Display Order
		changeFieldValue("kpi120", DISPLAY_ORDER, 1, kpiMaster);
		changeFieldValue("kpi119", DISPLAY_ORDER, 4, kpiMaster);
		changeFieldValue("kpi125", DISPLAY_ORDER, 9, kpiMaster);
		changeFieldValue("kpi131", DISPLAY_ORDER, 10, kpiMaster);
		changeFieldValue("kpi75", DISPLAY_ORDER, 23, kpiMaster);
		changeFieldValue("kpi136", DISPLAY_ORDER, 14, kpiMaster);
		changeFieldValue("kpi128", DISPLAY_ORDER, 2, kpiMaster);
		changeFieldValue("kpi124", DISPLAY_ORDER, 22, kpiMaster);
		changeFieldValue("kpi122", DISPLAY_ORDER, 7, kpiMaster);
		changeFieldValue("kpi135", DISPLAY_ORDER, 12, kpiMaster);
		changeFieldValue("kpi133", DISPLAY_ORDER, 13, kpiMaster);
		changeFieldValue("kpi123", DISPLAY_ORDER, 8, kpiMaster);
		
		List<String> iterationKpis = Arrays.asList("kpi120", "kpi119", "kpi125", "kpi131", "kpi75", "kpi136", "kpi128",
				"kpi124", "kpi122", "kpi135", "kpi133", "kpi123");
		iterationKpis.forEach(kpiId -> {
			removeField("kpi120", KPI_HEIGHT, kpiMaster);
			removeField("kpi120", IS_RAW_DATA, kpiMaster);
		});
		iterationKpis.remove("kpi120");
		iterationKpis.remove("kpi125");
		iterationKpis.forEach(kpiId -> removeField("kpi120", KPI_WIDTH, kpiMaster));
	}

	private void changeFieldValue(String kpiId, String field, String value, MongoCollection<Document> kpiMaster) {
		kpiMaster.updateMany(new Document(KPI_ID, new Document("$in", Arrays.asList(kpiId))),
				new Document("$set", new Document(field, value)));
	}

	private void changeFieldValue(String kpiId, String field, Integer value, MongoCollection<Document> kpiMaster) {
		kpiMaster.updateMany(new Document(KPI_ID, new Document("$in", Arrays.asList(kpiId))),
				new Document("$set", new Document(field, value)));
	}

	private void changeFieldValue(String kpiId, String field, Boolean value, MongoCollection<Document> kpiMaster) {
		kpiMaster.updateMany(new Document(KPI_ID, new Document("$in", Arrays.asList(kpiId))),
				new Document("$set", new Document(field, value)));
	}

	private void removeField(String kpiId, String field, MongoCollection<Document> kpiMaster) {
		kpiMaster.updateMany(new Document(KPI_ID, new Document("$in", Arrays.asList(kpiId))),
				new Document("$unset", new Document(field, "")));
	}

	private void deleteDocument(List<String> kpiId, MongoCollection<Document> kpiMaster) {
		kpiMaster.deleteMany(new Document(KPI_ID, new Document("$in", kpiId)));
	}
}
