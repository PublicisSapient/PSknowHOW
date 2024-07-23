package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1010;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * updated hirarchy to add helper text
 *
 * @author tejgorip
 */
@ChangeUnit(id = "hierarchy_info_update", order = "10107", author = "tejgorip", systemVersion = "10.1.0")

public class hirarchyhelpertextEnh {

	private final MongoTemplate mongoTemplate;

	public hirarchyhelpertextEnh(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		updateHyrarchyInfo();
	}

	public void updateHyrarchyInfo() {
		MongoCollection<Document> hierarchyLevels = mongoTemplate.getCollection("hierarchy_levels");

		Document newField = new Document("hierarchyInfo", "Business Unit");
		Document query1 = new Document("level", 1);
		Document update1 = new Document("$set", newField);
		mongoTemplate.getCollection("hierarchy_levels").updateOne(query1, update1);

		Document newField2 = new Document("hierarchyInfo", "Industry");
		Document query2 = new Document("level", 2);
		Document update2 = new Document("$set", newField2);
		mongoTemplate.getCollection("hierarchy_levels").updateOne(query2, update2);

		Document newField3 = new Document("hierarchyInfo", "Account");
		Document query3 = new Document("level", 3);
		Document update3 = new Document("$set", newField3);
		mongoTemplate.getCollection("hierarchy_levels").updateOne(query3, update3);

		Document newField4 = new Document("hierarchyInfo", "Engagement");
		Document query4 = new Document("level", 4);
		Document update4 = new Document("$set", newField4);
		mongoTemplate.getCollection("hierarchy_levels").updateOne(query4, update4);

	}

	@RollbackExecution
	public void rollback() {
		rollbackkpi113();
	}

	public void rollbackkpi113() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection("hierarchy_levels");
		Document newField = new Document("hierarchyInfo", null);
		Document query1 = new Document("level", 1);
		Document update1 = new Document("$unset", newField);
		mongoTemplate.getCollection("hierarchy_levels").updateOne(query1, update1);

		Document newField2 = new Document("hierarchyInfo", "Industry");
		Document query2 = new Document("level", 2);
		Document update2 = new Document("$unset", newField2);
		mongoTemplate.getCollection("hierarchy_levels").updateOne(query2, update2);

		Document newField3 = new Document("hierarchyInfo", null);
		Document query3 = new Document("level", 3);
		Document update3 = new Document("$unset", newField3);
		mongoTemplate.getCollection("hierarchy_levels").updateOne(query3, update3);

		Document newField4 = new Document("hierarchyInfo", null);
		Document query4 = new Document("level", 4);
		Document update4 = new Document("$unset", newField4);
		mongoTemplate.getCollection("hierarchy_levels").updateOne(query4, update4);

	}
}
