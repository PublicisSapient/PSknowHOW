package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1110;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author kunkambl
 */
@ChangeUnit(id = "fixathon_delete_jira_last_sync", order = "11103", author = "kunkambl", systemVersion = "11.1.0")
public class FixathonCleanJiraLastSync {

	private final MongoTemplate mongoTemplate;

	public FixathonCleanJiraLastSync(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		mongoTemplate.getCollection("processor_execution_trace_log").deleteMany(new Document("processorName", "Jira"));
	}

	@RollbackExecution
	public void rollback() {
		// No rollback required
	}
}
