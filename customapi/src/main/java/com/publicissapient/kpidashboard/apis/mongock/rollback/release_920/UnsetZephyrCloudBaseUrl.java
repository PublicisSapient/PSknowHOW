package com.publicissapient.kpidashboard.apis.mongock.rollback.release_920;

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
@ChangeUnit(id = "r_unset_zephyr_cloud_base_url", order = "09203", author = "pawkandp", systemVersion = "9.2.0")
public class UnsetZephyrCloudBaseUrl {
	private final MongoTemplate mongoTemplate;

	private static final String GLOBAL_CONFIG = "global_config";

	public UnsetZephyrCloudBaseUrl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	private void rollbackZephyrCloudBaseUrl() {
		Bson unset = Updates.unset("zephyrCloudBaseUrl");
		mongoTemplate.getCollection(GLOBAL_CONFIG).updateMany(new Document(), unset);
	}

	private void setZephyrCloudBaseUrl() {
		Bson set = Updates.set("zephyrCloudBaseUrl", "https://api.zephyrscale.smartbear.com/v2/");
		mongoTemplate.getCollection(GLOBAL_CONFIG).updateMany(new Document(), set);
	}

	@Execution
	public void execution() {
		setZephyrCloudBaseUrl();
	}

	@RollbackExecution
	public void rollBack() {
		rollbackZephyrCloudBaseUrl();
	}
}
