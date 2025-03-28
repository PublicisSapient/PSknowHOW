package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1220;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1220.UpdateKpiMasterGroupIds;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/** prijain3 */
@ChangeUnit(id = "r_update_kpi_master_group_ids", order = "012204", author = "prijain3", systemVersion = "12.2.0")
public class UpdateKpiMasterGroupIdsR {

	UpdateKpiMasterGroupIds upgradeKpiMaster;

	public UpdateKpiMasterGroupIdsR(MongoTemplate mongoTemplate) {
		upgradeKpiMaster = new UpdateKpiMasterGroupIds(mongoTemplate);
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
