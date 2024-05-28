package com.publicissapient.kpidashboard.apis.mongock.rollback.release_920;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author girpatha
 */
@ChangeUnit(id = "r_update_meantime_to_recover", order = "09206", author = "girpatha", systemVersion = "9.2.0")
public class UpdateMeanTimeToRecover {

	private final MongoTemplate mongoTemplate;
	private static final String KPI_ID = "kpi166";
	private static final String X_AXIS_LABEL = "Weeks";
	private static final String Y_AXIS_LABEL = "Hours";

	public UpdateMeanTimeToRecover(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {

		rollbackLabels(KPI_ID);
	}

	@RollbackExecution
	public void rollback() {

		updateLabels(KPI_ID, X_AXIS_LABEL, Y_AXIS_LABEL);
	}

	private void rollbackLabels(String kpiId) {
		Update update = new Update();
		update.unset("xAxisLabel");
		update.unset("yAxisLabel");
		mongoTemplate.updateFirst(getQueryByKpiId(kpiId), update, "kpi_master");
	}

	private void updateLabels(String kpiId, String xAxisLabel, String yAxisLabel) {
		Update update = new Update();
		update.set("xAxisLabel", xAxisLabel);
		update.set("yAxisLabel", yAxisLabel);
		mongoTemplate.updateFirst(getQueryByKpiId(kpiId), update, "kpi_master");
	}

	private Query getQueryByKpiId(String kpiId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("kpiId").is(kpiId));
		return query;
	}
}
