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
public class RefactorGlobalConfigUnusedFields {
	private final MongoTemplate mongoTemplate;

	private static final String GLOBAL_CONFIG = "global_config";

	public RefactorGlobalConfigUnusedFields(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	private void setZephyrCloudBaseUrl() {
		Bson set = Updates.set("zephyrCloudBaseUrl", "https://api.zephyrscale.smartbear.com/v2/");
		mongoTemplate.getCollection(GLOBAL_CONFIG).updateMany(new Document(), set);
	}

	private void setAdServerDetail() {
		Document adServerDetail = new Document().append("username", "svc-apac-enggkpidash")
				.append("host", "lladldap-ext.fr.publicisgroupe.net").append("port", 639)
				.append("rootDn", "DC=global,DC=publicisgroupe,DC=net").append("domain", "publicisgroupe.net");

		Bson set = Updates.set("adServerDetail", adServerDetail);
		mongoTemplate.getCollection(GLOBAL_CONFIG).updateMany(new Document(), set);
	}

	private void setAuthTypeStatus() {
		Bson set = Updates.set("authTypeStatus", new Document("standardLogin", true).append("adLogin", true));
		mongoTemplate.getCollection(GLOBAL_CONFIG).updateMany(new Document(), set);
	}

	private void rollbackZephyrCloudBaseUrl() {
		Bson unset = Updates.unset("zephyrCloudBaseUrl");
		mongoTemplate.getCollection(GLOBAL_CONFIG).updateMany(new Document(), unset);
	}

	private void rollbackAuthTypeStatus() {
		Bson unset = Updates.unset("authTypeStatus");
		mongoTemplate.getCollection(GLOBAL_CONFIG).updateMany(new Document(), unset);
	}

	private void rollbackAdServerDetail() {
		Bson unset = Updates.unset("adServerDetail");
		mongoTemplate.getCollection(GLOBAL_CONFIG).updateMany(new Document(), unset);
	}

	@Execution
	public void execution() {
		setZephyrCloudBaseUrl();
		setAuthTypeStatus();
		setAdServerDetail();
	}

	@RollbackExecution
	public void rollBack() {
		rollbackZephyrCloudBaseUrl();
		rollbackAuthTypeStatus();
		rollbackAdServerDetail();
	}
}
