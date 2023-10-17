package com.publicissapient.kpidashboard.apis.script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.UpdateOptions;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "7.4.0 upgrade", order = "001", author = "bogolesw")
public class ReleaseChangeLog74 {
	private final MongoTemplate mongoTemplate;

	private static final String KPI_MASTER_DB = "kpi_master";

	public ReleaseChangeLog74(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void changeSetForKpiMaster() {
		List<UpdateManyModel<Document>> bulkUpdateKpiMaster = new ArrayList<>();

		// Update document with kpiId "kpi34"
		Document filter1 = new Document("kpiId", "kpi34");
		Document update1 = new Document("$set", new Document("kpiInfo.formula.$[].operands", Arrays
				.asList("No. of defects in the iteration that are fixed", "Total no. of defects in an iteration")));
		bulkUpdateKpiMaster.add(new UpdateManyModel<>(filter1, update1, new UpdateOptions()));

		// Update documents with kpiIds in the list
		List<String> kpiIdsToUpdate = Arrays.asList("kpi129", "kpi138", "kpi3", "kpi148", "kpi152");
		Document filter2 = new Document("kpiId", new Document("$in", kpiIdsToUpdate));
		Document update2 = new Document("$set", new Document("groupId", 11));
		bulkUpdateKpiMaster.add(new UpdateManyModel<>(filter2, update2, new UpdateOptions()));

		BulkWriteOptions options = new BulkWriteOptions();
		mongoTemplate.getCollection(KPI_MASTER_DB).bulkWrite(bulkUpdateKpiMaster, options);
	}

	@RollbackExecution
	public void rollback() {
		mongoTemplate.dropCollection(KPI_MASTER_DB);
	}
}
