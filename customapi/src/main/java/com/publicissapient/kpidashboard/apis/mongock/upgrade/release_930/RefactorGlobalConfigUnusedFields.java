package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_930;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.model.Updates;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author pawkandp
 */
@ChangeUnit(id = "refactor_global_config_unused_fields", order = "9303", author = "pawkandp", systemVersion = "9.3.0")
public class RefactorGlobalConfigUnusedFields {
	private final MongoTemplate mongoTemplate;

	private static final String GLOBAL_CONFIG = "global_config";

	public RefactorGlobalConfigUnusedFields(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	private void removeZephyrCloudBaseUrl() {
		Bson unset = Updates.unset("zephyrCloudBaseUrl");
		mongoTemplate.getCollection(GLOBAL_CONFIG).updateMany(new Document(), unset);
	}

	private void removeAuthTypeStatus() {
		Bson unset = Updates.unset("authTypeStatus");
		mongoTemplate.getCollection(GLOBAL_CONFIG).updateMany(new Document(), unset);
	}

	private void removeAdServerDetail() {
		Bson unset = Updates.unset("adServerDetail");
		mongoTemplate.getCollection(GLOBAL_CONFIG).updateMany(new Document(), unset);
	}

	private void rollbackZephyrCloudBaseUrl() {
		Bson set = Updates.set("zephyrCloudBaseUrl", "https://api.zephyrscale.smartbear.com/v2/");
		mongoTemplate.getCollection(GLOBAL_CONFIG).updateMany(new Document(), set);
	}

	private void rollbackAuthTypeStatus() {
		Bson set = Updates.set("authTypeStatus", new Document("standardLogin", true).append("adLogin", true));
		mongoTemplate.getCollection(GLOBAL_CONFIG).updateMany(new Document(), set);
	}

	@Execution
	public void execution() {
		removeZephyrCloudBaseUrl();
		removeAuthTypeStatus();
		removeAdServerDetail();
	}

	@RollbackExecution
	public void rollBack() {
		rollbackZephyrCloudBaseUrl();
		rollbackAuthTypeStatus();
	}
}
