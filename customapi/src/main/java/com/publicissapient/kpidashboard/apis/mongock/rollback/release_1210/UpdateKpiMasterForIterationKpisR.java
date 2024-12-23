package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1210;

import java.util.Arrays;
import java.util.List;

import com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1210.UpdateKpiMasterForIterationKpis;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * prijain3
 */
@ChangeUnit(id = "r_update_kpi_master_for_iteration_kpis", order = "012100", author = "prijain3", systemVersion = "12.1.0")
public class UpdateKpiMasterForIterationKpisR {
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
	private static final String TABLE_V2 = "table-v2";

	UpdateKpiMasterForIterationKpis upgradeKpiMaster;

	public UpdateKpiMasterForIterationKpisR(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
		upgradeKpiMaster = new UpdateKpiMasterForIterationKpis(this.mongoTemplate);
	}

	@Execution
	public void execution() {
		upgradeKpiMaster.rollback();
	}

	@RollbackExecution
	public void rollback() {
		upgradeKpiMaster.execution();
	}

}
