package com.publicissapient.kpidashboard.apis.mongock;

import com.mongodb.client.MongoCollection;
import com.publicissapient.kpidashboard.apis.util.MongockUtils;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.*;

@Slf4j
@ChangeUnit(id = "DDL", order = "001", author = "hargupta15", runAlways = true, systemVersion = "8.0.0")
public class Installation {
	private final MongoTemplate mongoTemplate;

	public Installation(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void changeSet() {
		MongoCollection<Document> rolesCollection = mongoTemplate.getCollection(MongockUtils.ROLES_COLLECTION);
		//rolesCollection = getDocumentMongoCollection(rolesCollection);
		if (rolesCollection.countDocuments() == 0) {
			List<Document> roles = getRoles();
			mongoTemplate.insert(roles, MongockUtils.ROLES_COLLECTION);
		}

	}

	private MongoCollection<Document> getDocumentMongoCollection(MongoCollection<Document> rolesCollection) {
		if (!mongoTemplate.collectionExists(MongockUtils.ROLES_COLLECTION))
			rolesCollection = mongoTemplate.createCollection(MongockUtils.ROLES_COLLECTION);
		return rolesCollection;
	}

	private static List<Document> getRoles() {
		return Arrays.asList(new Document("roleName", "ROLE_PROJECT_VIEWER")
						.append("displayName", "Project Viewer").append("description", "read kpi data at project level")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date()).append("isDeleted", false)
						.append("permissionNames", Collections.singletonList("View")),
				new Document("roleName", "ROLE_PROJECT_ADMIN").append("displayName", "Project Admin")
						.append("description", "manage user-roles at project level")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false).append("permissionNames", Collections.singletonList("View")),
				new Document("roleName", "ROLE_SUPERADMIN").append("displayName", "Super Admin")
						.append("description", "access to every resource in the instance")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false).append("permissionNames", Collections.singletonList("ViewAll")),
				new Document("roleName", "ROLE_GUEST").append("displayName", "Guest")
						.append("description", "read access for the instance").append("createdDate", new Date())
						.append("lastModifiedDate", new Date()).append("isDeleted", false)
						.append("permissionNames", Collections.singletonList("View")));
	}

	@RollbackExecution
	public void rollback() {
		mongoTemplate.dropCollection(MongockUtils.ROLES_COLLECTION);
	}
}
