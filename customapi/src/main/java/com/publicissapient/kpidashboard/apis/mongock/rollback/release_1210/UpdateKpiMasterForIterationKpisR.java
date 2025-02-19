package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1210;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1210.UpdateKpiMasterForIterationKpis;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/** prijain3 */
@ChangeUnit(id = "r_update_kpi_master_for_iteration_kpis", order = "012100", author = "prijain3", systemVersion = "12.1.0")
public class UpdateKpiMasterForIterationKpisR {

	UpdateKpiMasterForIterationKpis upgradeKpiMaster;

	public UpdateKpiMasterForIterationKpisR(MongoTemplate mongoTemplate) {
		upgradeKpiMaster = new UpdateKpiMasterForIterationKpis(mongoTemplate);
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
