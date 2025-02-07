package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1220;

import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * prijain3
 */
@ChangeUnit(id = "update_kpi_master_group_ids", order = "12204", author = "prijain3", systemVersion = "12.2.0")
public class UpdateKpiMasterGroupIds {
	private final MongoTemplate mongoTemplate;
	private static final String KPI_MASTER = "kpi_master";
	private static final String KPI_ID = "kpiId";
	private static final String GROUP_ID = "groupId";

	public UpdateKpiMasterGroupIds(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection(KPI_MASTER);
        //update KPI group ids
		changeFieldValue("kpi149", GROUP_ID, 4, kpiMaster);
		changeFieldValue("kpi114", GROUP_ID, 5, kpiMaster);
        changeFieldValue("kpi74", GROUP_ID, 5, kpiMaster);
        changeFieldValue("kpi8", GROUP_ID, 30, kpiMaster);
        changeFieldValue("kpi172", GROUP_ID, 30, kpiMaster);
		changeFieldValue("kpi66", GROUP_ID, 31, kpiMaster);
		changeFieldValue("kpi46", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi40", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi164", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi58", GROUP_ID, 3, kpiMaster);
		changeFieldValue("kpi49", GROUP_ID, 3, kpiMaster);
		changeFieldValue("kpi53", GROUP_ID, 3, kpiMaster);
		changeFieldValue("kpi82", GROUP_ID, 25, kpiMaster);
		changeFieldValue("kpi36", GROUP_ID, 25, kpiMaster);
		changeFieldValue("kpi37", GROUP_ID, 25, kpiMaster);
		changeFieldValue("kpi34", GROUP_ID, 25, kpiMaster);
		changeFieldValue("kpi14", GROUP_ID, 25, kpiMaster);
		changeFieldValue("kpi126", GROUP_ID, 24, kpiMaster);
		changeFieldValue("kpi28", GROUP_ID, 24, kpiMaster);
		changeFieldValue("kpi35", GROUP_ID, 24, kpiMaster);
		changeFieldValue("kpi111", GROUP_ID, 24, kpiMaster);
		changeFieldValue("kpi70", GROUP_ID, 23, kpiMaster);
		changeFieldValue("kpi16", GROUP_ID, 23, kpiMaster);
		changeFieldValue("kpi42", GROUP_ID, 23, kpiMaster);
		changeFieldValue("kpi168", GROUP_ID, 22, kpiMaster);
		changeFieldValue("kpi27", GROUP_ID, 22, kpiMaster);
		changeFieldValue("kpi38", GROUP_ID, 22, kpiMaster);
		changeFieldValue("kpi17", GROUP_ID, 22, kpiMaster);
		changeFieldValue("kpi71", GROUP_ID, 29, kpiMaster);
		changeFieldValue("kpi63", GROUP_ID, 29, kpiMaster);
		changeFieldValue("kpi67", GROUP_ID, 28, kpiMaster);
		changeFieldValue("kpi64", GROUP_ID, 28, kpiMaster);
		changeFieldValue("kpi62", GROUP_ID, 28, kpiMaster);
		changeFieldValue("kpi50", GROUP_ID, 27, kpiMaster);
		changeFieldValue("kpi51", GROUP_ID, 27, kpiMaster);
		changeFieldValue("kpi48", GROUP_ID, 27, kpiMaster);
		changeFieldValue("kpi54", GROUP_ID, 26, kpiMaster);
		changeFieldValue("kpi55", GROUP_ID, 26, kpiMaster);
		changeFieldValue("kpi997", GROUP_ID, 26, kpiMaster);
		changeFieldValue("kpi183", GROUP_ID, 16, kpiMaster);
		changeFieldValue("kpi184", GROUP_ID, 16, kpiMaster);
		changeFieldValue("kpi185", GROUP_ID, 7, kpiMaster);
		changeFieldValue("kpi182", GROUP_ID, 7, kpiMaster);
		changeFieldValue("kpi181", GROUP_ID, 7, kpiMaster);
		changeFieldValue("kpi180", GROUP_ID, 7, kpiMaster);
		changeFieldValue("kpi173", GROUP_ID, 7, kpiMaster);
		changeFieldValue("kpi11", GROUP_ID, 7, kpiMaster);
		changeFieldValue("kpi84", GROUP_ID, 7, kpiMaster);
		changeFieldValue("kpi162", GROUP_ID, 6, kpiMaster);
		changeFieldValue("kpi160", GROUP_ID, 6, kpiMaster);
		changeFieldValue("kpi158", GROUP_ID, 6, kpiMaster);
		changeFieldValue("kpi157", GROUP_ID, 6, kpiMaster);
		changeFieldValue("kpi186", GROUP_ID, 6, kpiMaster);
		changeFieldValue("kpi159", GROUP_ID, 21, kpiMaster);
		changeFieldValue("kpi65", GROUP_ID, 21, kpiMaster);
		changeFieldValue("kpi169", GROUP_ID, 12, kpiMaster);
	}

	@RollbackExecution
	public void rollback() {
        MongoCollection<Document> kpiMaster = mongoTemplate.getCollection(KPI_MASTER);
		// update KPI group ids
		changeFieldValue("kpi149", GROUP_ID, 16, kpiMaster);
		changeFieldValue("kpi114", GROUP_ID, 4, kpiMaster);
		changeFieldValue("kpi74", GROUP_ID, 4, kpiMaster);
		changeFieldValue("kpi8", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi172", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi66", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi46", GROUP_ID, 5, kpiMaster);
		changeFieldValue("kpi40", GROUP_ID, 5, kpiMaster);
		changeFieldValue("kpi164", GROUP_ID, 5, kpiMaster);
		changeFieldValue("kpi58", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi49", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi53", GROUP_ID, 3, kpiMaster);
		changeFieldValue("kpi82", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi36", GROUP_ID, 3, kpiMaster);
		changeFieldValue("kpi37", GROUP_ID, 3, kpiMaster);
		changeFieldValue("kpi34", GROUP_ID, 3, kpiMaster);
		changeFieldValue("kpi14", GROUP_ID, 3, kpiMaster);
		changeFieldValue("kpi126", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi28", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi35", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi111", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi70", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi16", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi42", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi168", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi27", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi38", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi17", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi71", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi63", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi67", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi64", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi62", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi50", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi51", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi48", GROUP_ID, 2, kpiMaster);
		changeFieldValue("kpi54", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi55", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi997", GROUP_ID, 2, kpiMaster);
		changeFieldValue("kpi183", GROUP_ID, 5, kpiMaster);
		changeFieldValue("kpi184", GROUP_ID, 5, kpiMaster);
		changeFieldValue("kpi185", GROUP_ID, 2, kpiMaster);
		changeFieldValue("kpi182", GROUP_ID, 2, kpiMaster);
		changeFieldValue("kpi181", GROUP_ID, 2, kpiMaster);
		changeFieldValue("kpi180", GROUP_ID, 2, kpiMaster);
		changeFieldValue("kpi173", GROUP_ID, 2, kpiMaster);
		changeFieldValue("kpi11", GROUP_ID, 2, kpiMaster);
		changeFieldValue("kpi84", GROUP_ID, 2, kpiMaster);
		changeFieldValue("kpi162", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi160", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi158", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi157", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi186", GROUP_ID, 2, kpiMaster);
		changeFieldValue("kpi159", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi65", GROUP_ID, 1, kpiMaster);
		changeFieldValue("kpi169", GROUP_ID, 9, kpiMaster);

	}

	private void changeFieldValue(String kpiId, String field, Integer value, MongoCollection<Document> kpiMaster) {
		kpiMaster.updateMany(new Document(KPI_ID, new Document("$in", List.of(kpiId))),
				new Document("$set", new Document(field, value)));
	}


}
