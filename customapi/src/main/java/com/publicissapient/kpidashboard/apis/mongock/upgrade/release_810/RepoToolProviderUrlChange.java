package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_810;

import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.List;

@ChangeUnit(id = "repo_tool_provider_url_change", order = "8110", author = "kunkambl", systemVersion = "8.1.0")
public class RepoToolProviderUrlChange {

	private final MongoTemplate mongoTemplate;

	public RepoToolProviderUrlChange(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		changeRepoToolProviderTestApiUrls();
	}

	public void changeRepoToolProviderTestApiUrls() {
		List<WriteModel<Document>> bulkUpdateOps = new ArrayList<>();

		// Update for bitbucket tool
		bulkUpdateOps.add(new UpdateOneModel<>(new Document("toolName", "bitbucket"),

				new Document("$set", new Document().append("testServerApiUrl", "/bitbucket/rest/api/1.0/projects/")
						.append("testApiUrl", "https://api.bitbucket.org/2.0/workspaces/"))));

		// Update for gitlab tool
		bulkUpdateOps.add(new UpdateOneModel<>(new Document("toolName", "gitlab"),
				new Document("$set", new Document("testApiUrl", "/api/v4/projects/"))));
		BulkWriteOptions options = new BulkWriteOptions().ordered(false);
		mongoTemplate.getCollection("repo_tools_provider").bulkWrite(bulkUpdateOps, options);
	}

	@RollbackExecution
	public void rollback() {
        changeRepoToolProviderTestApiUrlsRollback();
	}

	public void changeRepoToolProviderTestApiUrlsRollback() {
		List<WriteModel<Document>> bulkUpdateOps = new ArrayList<>();
		bulkUpdateOps.add(new UpdateOneModel<>(new Document("toolName", "bitbucket"),

				new Document("$set",
						new Document().append("testServerApiUrl", "https://api.bitbucket.org/2.0/repositories/")
								.append("testApiUrl", ""))));

		// Update for gitlab tool
		bulkUpdateOps.add(new UpdateOneModel<>(new Document("toolName", "gitlab"),
				new Document("$set", new Document("testApiUrl", "https://gitlab.com/api/v4/projects/"))));
		BulkWriteOptions options = new BulkWriteOptions().ordered(false);
		mongoTemplate.getCollection("repo_tools_provider").bulkWrite(bulkUpdateOps, options);
	}

}
