package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1210;

import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author kunkambl
 */
@ChangeUnit(id = "r_cod_kpi_column_config_update", order = "012104", author = "kunkambl", systemVersion = "12.1.0")
public class KpiColumnConfigUpdate {

	private final MongoTemplate mongoTemplate;

    private static final String COLUMN_NAME = "columnName";
    private static final String IS_SHOWN = "isShown";
    private static final String ORDER = "order";
    private static final String IS_DEFAULT = "isDefault";

	public KpiColumnConfigUpdate(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		mongoTemplate.getCollection("kpi_column_configs").deleteOne(new Document("kpiId", "kpi114"));
	}

    @RollbackExecution
    public void rollback() {
		mongoTemplate.getCollection("kpi_column_configs")
				.insertOne(new Document().append("basicProjectConfigId", null).append("kpiId", "kpi114").append(
						"kpiColumnDetails",
						List.of(new Document().append(COLUMN_NAME, "Project Name").append(ORDER, 1)
										.append(IS_SHOWN, true).append(IS_DEFAULT, true),
								new Document().append(COLUMN_NAME, "Cost of Delay").append(ORDER, 2)
										.append(IS_SHOWN, true).append(IS_DEFAULT, true),
								new Document().append(COLUMN_NAME, "Epic ID").append(ORDER, 3)
										.append(IS_SHOWN, true).append(IS_DEFAULT, true),
								new Document().append(COLUMN_NAME, "Epic Name").append(ORDER, 4)
										.append(IS_SHOWN, true).append(IS_DEFAULT, true),
								new Document().append(COLUMN_NAME, "Squad").append(ORDER, 5).append(IS_SHOWN, true)
										.append(IS_DEFAULT, true),
								new Document().append(COLUMN_NAME, "Epic End Date").append(ORDER, 6)
										.append(IS_SHOWN, true).append(IS_DEFAULT, true),
								new Document().append(COLUMN_NAME, "Month").append(ORDER, 7).append(IS_SHOWN, true)
										.append(IS_DEFAULT, true))));
    }

}
