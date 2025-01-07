package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1210;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeUnit(id = "add_kpi_category_mapping", order = "12107", author = "aksshriv1", systemVersion = "12.1.0")
public class IncludeKPIInDashBoardKPI {

	private final MongoTemplate mongoTemplate;

	public IncludeKPIInDashBoardKPI(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		mongoTemplate.getCollection("kpi_category_mapping").insertOne(new Document().append("kpiId", "kpi149")
				.append("categoryId", "value").append("kpiOrder", 4.0).append("kanban", false));
	}

	@RollbackExecution
	public void rollback() {
		mongoTemplate.getCollection("kpi_category_mapping").deleteOne(new Document("kpiId", "kpi149"));
	}
}
