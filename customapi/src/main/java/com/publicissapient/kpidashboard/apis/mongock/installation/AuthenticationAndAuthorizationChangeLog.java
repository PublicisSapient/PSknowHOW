/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.publicissapient.kpidashboard.apis.mongock.installation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;

/**
 * @author hargupta15
 */
@Slf4j
@SuppressWarnings("java:S1192")
@ChangeUnit(id = "ddl1", order = "001", author = "PSKnowHOW")
public class AuthenticationAndAuthorizationChangeLog {
	public static final String ROLES_COLLECTION = "roles";
	public static final String ACTION_POLICY_RULE_COLLECTION = "action_policy_rule";

	private final MongoTemplate mongoTemplate;

	public AuthenticationAndAuthorizationChangeLog(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void changeSet() {
		MongoCollection<Document> rolesCollection = getOrCreateCollection(ROLES_COLLECTION);
		if (rolesCollection.countDocuments() == 0) {
			List<Document> roles = getRoles();
			mongoTemplate.insert(roles, ROLES_COLLECTION);
		}

		MongoCollection<Document> actionPolicyRuleCollection = getOrCreateCollection(ACTION_POLICY_RULE_COLLECTION);
		if (actionPolicyRuleCollection.countDocuments() == 0) {
			List<Document> actionPolicyRules = getActionPolicyRule();
			mongoTemplate.insert(actionPolicyRules, ACTION_POLICY_RULE_COLLECTION);
		}

	}

	public MongoCollection<Document> getOrCreateCollection(String collectionName) {
		if (!mongoTemplate.collectionExists(collectionName))
			return mongoTemplate.createCollection(collectionName);
		return mongoTemplate.getCollection(collectionName);
	}

	private List<Document> getRoles() {
		List<Document> documents = new ArrayList<>();
		Document document1 = new Document();
		document1.append("roleName", "ROLE_GUEST").append("displayName", "Guest")
				.append("description", "read access for the instance").append("createdDate", new Date())
				.append("lastModifiedDate", new Date()).append("isDeleted", "False").append("permissions",
						Arrays.asList(new Document("permissionName", "View").append("operationName", "Read")
								.append("resourceName", "resource4").append("createdDate", new Date())
								.append("lastModifiedDate", new Date()).append("isDeleted", "False")));
		documents.add(document1);
		// Document 2
		Document document2 = new Document();
		document2.append("roleName", "ROLE_SUPERADMIN").append("displayName", "Super Admin")
				.append("description", "access to every resource in the instance").append("createdDate", new Date())
				.append("lastModifiedDate", new Date()).append("isDeleted", "False").append("permissions",
						Arrays.asList(new Document("permissionName", "ViewAll").append("operationName", "Read")
								.append("resourceName", "resource5").append("createdDate", new Date())
								.append("lastModifiedDate", new Date()).append("isDeleted", "False")));
		documents.add(document2);
		// Document 3 - ROLE_PROJECT_ADMIN
		Document document3 = new Document();
		document3.append("roleName", "ROLE_PROJECT_ADMIN").append("displayName", "Project Admin")
				.append("description", "manage user-roles at project level").append("createdDate", new Date())
				.append("lastModifiedDate", new Date()).append("isDeleted", "False").append("permissions",
						Arrays.asList(new Document("permissionName", "View").append("operationName", "Read")
								.append("resourceName", "resource4").append("createdDate", new Date())
								.append("lastModifiedDate", new Date()).append("isDeleted", "False")));
		documents.add(document3);

		// Document 4 - ROLE_PROJECT_VIEWER
		Document document4 = new Document();
		document4.append("roleName", "ROLE_PROJECT_VIEWER").append("displayName", "Project Viewer")
				.append("description", "read kpi data at project level").append("createdDate", new Date())
				.append("lastModifiedDate", new Date()).append("isDeleted", "False").append("permissions",
						Arrays.asList(new Document("permissionName", "View").append("operationName", "Read")
								.append("resourceName", "resource4").append("createdDate", new Date())
								.append("lastModifiedDate", new Date()).append("isDeleted", "False")));
		documents.add(document4);
		return documents;
	}

	private List<Document> getActionPolicyRule() {
		return Arrays.asList(new Document("name", "Super Admin").append("roleAllowed", "")
				.append("description", "Super Admin can do all.")
				.append("roleActionCheck", "subject.authorities.contains('ROLE_SUPERADMIN')").append("condition", true)
				.append("createdDate", new Date()).append("lastModifiedDate", new Date()).append("isDeleted", false),

				new Document("name", "Add projects").append("roleAllowed", "")
						.append("description", "Any user can add a project except guest user")
						.append("roleActionCheck",
								"!subject.authorities.contains('ROLE_GUEST') && action == 'ADD_PROJECT'")
						.append("condition", true).append("createdDate", new Date())
						.append("lastModifiedDate", new Date()).append("isDeleted", false),

				new Document("name", "Update projects").append("roleAllowed", "")
						.append("description",
								"User with ROLE_PROJECT_ADMIN can update the project if has access of it")
						.append("roleActionCheck",
								"subject.authorities.contains('ROLE_PROJECT_ADMIN') && action == 'UPDATE_PROJECT'")
						.append("condition",
								"projectAccessManager.hasProjectEditPermission(resource.id, subject.getUsername())")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),

				new Document("name", "Delete tool").append("roleAllowed", "").append("description",
						"User with ROLE_PROJECT_ADMIN can delete the tool associated with a project if has access of that project")
						.append("roleActionCheck",
								"subject.authorities.contains('ROLE_PROJECT_ADMIN') && action == 'DELETE_TOOL'")
						.append("condition",
								"projectAccessManager.hasProjectEditPermission(resource, subject.getUsername())")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),

				new Document("name", "Cache project config").append("roleAllowed", "")
						.append("description", "User can get cached/saved projects by him/her self")
						.append("roleActionCheck",
								"{'GET_SAVED_PROJECTS', 'SAVE_CACHE_PROJECT', 'UPDATE_CACHE_PROJECT'}.contains(action)")
						.append("condition", "subject.username == resource").append("createdDate", new Date())
						.append("lastModifiedDate", new Date()).append("isDeleted", false),

				new Document("name", "Global config field").append("roleAllowed", "")
						.append("description", "Only ROLE_SUPERADMIN can access this resource")
						.append("roleActionCheck", "action == 'GET_GLOBAL_CONFIG_FIELD' && resource != 'centralConfig'")
						.append("condition", true).append("createdDate", new Date())
						.append("lastModifiedDate", new Date()).append("isDeleted", false),

				new Document("name", "Central Config").append("roleAllowed", "")
						.append("description", "Only ROLE_SUPERADMIN can access this resource")
						.append("roleActionCheck", "action == 'GET_GLOBAL_CONFIG_FIELD' && resource == 'centralConfig'")
						.append("condition", true).append("createdDate", new Date())
						.append("lastModifiedDate", new Date()).append("isDeleted", false),

				new Document("name", "Upload/Delete logo").append("roleAllowed", "")
						.append("description", "Only superadmin can upload or delete logo")
						.append("roleActionCheck",
								"resource == 'LOGO' && {'FILE_UPLOAD', 'DELETE_LOGO'}.contains(action)")
						.append("condition", "subject.authorities.contains('ROLE_SUPERADMIN')")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),

				new Document("name", "File Upload eng maturity master").append("roleAllowed", "").append("description",
						"ENG_MATURITY_MASTER can be uploaded by ROLE_SUPERADMIN only. Rest uploads can be done by any user")
						.append("roleActionCheck", "resource == 'ENG_MATURITY_MASTER' && action == 'FILE_UPLOAD'")
						.append("condition", "subject.authorities.contains('ROLE_SUPERADMIN')")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),

				new Document("name", "File Upload engg maturity").append("roleAllowed", "")
						.append("description", "project admin and superadmin can upload")
						.append("roleActionCheck", "resource == 'ENG_MATURITY' && action == 'FILE_UPLOAD'")
						.append("condition",
								"subject.authorities.contains('ROLE_SUPERADMIN') || subject.authorities.contains('ROLE_PROJECT_ADMIN')")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),
				new Document("name", "Processor List").append("roleAllowed", "")
						.append("description", "super admin and project admin can access list of processor")
						.append("roleActionCheck", "action == 'GET_PROCESSORS'")
						.append("condition",
								"subject.authorities.contains('ROLE_SUPERADMIN') || subject.authorities.contains('ROLE_PROJECT_ADMIN')")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),

				new Document("name", "Run Processor").append("roleAllowed", "")
						.append("description", "super admin and project admin can run processor")
						.append("roleActionCheck", "action == 'TRIGGER_PROCESSOR'")
						.append("condition",
								"projectAccessManager.canTriggerProcessorFor(resource, subject.getUsername())")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),

				new Document("name", "Access request of the user").append("roleAllowed", "")
						.append("description", "gets all access requests of the provided user")
						.append("roleActionCheck", "action == 'GET_ACCESS_REQUESTS_OF_USER'")
						.append("condition",
								"subject.authorities.contains('ROLE_SUPERADMIN') || subject.username == resource")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),

				new Document("name", "SAVE_PROJECT_TOOL").append("roleAllowed", "")
						.append("description", "User with ROLE_PROJECT_ADMIN save the project tool if has access of it")
						.append("roleActionCheck",
								"subject.authorities.contains('ROLE_PROJECT_ADMIN') && action == 'SAVE_PROJECT_TOOL'")
						.append("condition",
								"projectAccessManager.hasProjectEditPermission(resource, subject.getUsername())")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),

				new Document("name", "UPDATE_PROJECT_TOOL").append("roleAllowed", "")
						.append("description",
								"User with ROLE_PROJECT_ADMIN update the project tool if has access of it")
						.append("roleActionCheck",
								"subject.authorities.contains('ROLE_PROJECT_ADMIN') && {'UPDATE_PROJECT_TOOL','DELETE_PROJECT_TOOL'}.contains(action)")
						.append("condition",
								"projectAccessManager.hasProjectEditPermission(resource, subject.getUsername())")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),

				new Document("name", "CLEAN_PROJECT_TOOL_DATA").append("roleAllowed", "")
						.append("description",
								"User with ROLE_PROJECT_ADMIN update the project tool if has access of it")
						.append("roleActionCheck",
								"subject.authorities.contains('ROLE_PROJECT_ADMIN') && {'CLEAN_PROJECT_TOOL_DATA'}.contains(action)")
						.append("condition",
								"projectAccessManager.hasProjectEditPermission(resource, subject.getUsername())")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),

				new Document("name", "capacity tab").append("roleAllowed", "")
						.append("description",
								"User with ROLE_PROJECT_ADMIN and ROLE_SUPERADMIN can update or save the capacity data")
						.append("roleActionCheck",
								"subject.authorities.contains('ROLE_PROJECT_ADMIN') && action == 'SAVE_UPDATE_CAPACITY'")
						.append("condition",
								"projectAccessManager.hasProjectEditPermission(resource.basicProjectConfigId, subject.getUsername())")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),

				new Document("name", "DELETE_PROJECT").append("roleAllowed", "")
						.append("description", "User with ROLE_PROJECT_ADMIN can delete the project if granted access")
						.append("roleActionCheck",
								"subject.authorities.contains('ROLE_PROJECT_ADMIN') && {'DELETE_PROJECT'}.contains(action)")
						.append("condition",
								"projectAccessManager.hasProjectEditPermission(resource, subject.getUsername())")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),
				new Document("name", "test Execution").append("roleAllowed", "").append("description",
						"User with ROLE_PROJECT_ADMIN and ROLE_SUPERADMIN can update or save the test Execution data")
						.append("roleActionCheck",
								"subject.authorities.contains('ROLE_PROJECT_ADMIN') && action == 'SAVE_UPDATE_TEST_EXECUTION'")
						.append("condition",
								"projectAccessManager.hasProjectEditPermission(resource.basicProjectConfigId, subject.getUsername())")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),

				new Document("name", "Raise access request by the user").append("roleAllowed", "")
						.append("description", "Restrict guest user to raise access request")
						.append("roleActionCheck",
								"!subject.authorities.contains('ROLE_GUEST') && action == 'RAISE_ACCESS_REQUEST'")
						.append("condition", "true").append("createdDate", new Date())
						.append("lastModifiedDate", new Date()).append("isDeleted", false),

				new Document("name", "Connection access").append("roleAllowed", "")
						.append("description", "Restrict guest user to create, update, get and delete connection")
						.append("roleActionCheck",
								"!subject.authorities.contains('ROLE_GUEST') && action == 'CONNECTION_ACCESS'")
						.append("condition", "true").append("createdDate", new Date())
						.append("lastModifiedDate", new Date()).append("isDeleted", false),

				new Document("name", "AD Settings").append("roleAllowed", "")
						.append("description", "Only superadmin can see or update the AD settings")
						.append("roleActionCheck", "action == 'SAVE_AD_SETTING' || action == 'GET_AD_SETTING'")
						.append("condition", "subject.authorities.contains('ROLE_SUPERADMIN')")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),

				new Document("name", "Show Emm history and Statistics").append("roleAllowed", "").append("description",
						"Project viewer, Project admin, and superadmin can view emm upload history and statistics")
						.append("roleActionCheck", "{'GET_EMM_HISTORY', 'GET_EMM_STATISTICS'}.contains(action)")
						.append("condition",
								"subject.authorities.contains('ROLE_SUPERADMIN') || subject.authorities.contains('ROLE_PROJECT_VIEWER') || subject.authorities.contains('ROLE_PROJECT_ADMIN')")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),

				new Document("name", "Approve User").append("roleAllowed", "")
						.append("description", "get, update and reject new user")
						.append("roleActionCheck", "action == 'APPROVE_USER'")
						.append("condition", "subject.authorities.contains('ROLE_SUPERADMIN')")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),

				new Document("name", "Access Request status").append("roleAllowed", "")
						.append("description",
								"User with ROLE_PROJECT_ADMIN and ROLE_SUPERADMIN can see access request")
						.append("roleActionCheck", "action == 'ACCESS_REQUEST_STATUS'")
						.append("condition", "subject.authorities.contains('ROLE_PROJECT_ADMIN')")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),

				new Document("name", "Grant Access").append("roleAllowed", "")
						.append("description", "User with ROLE_PROJECT_ADMIN and ROLE_SUPERADMIN can grant access")
						.append("roleActionCheck", "action == 'GRANT_ACCESS'")
						.append("condition", "subject.authorities.contains('ROLE_PROJECT_ADMIN')")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),

				new Document("name", "DELETE_USER").append("roleAllowed", "")
						.append("description", "User with role ROLE_SUPERADMIN can delete the users if granted access")
						.append("roleActionCheck", "action == 'DELETE_USER'")
						.append("condition", "subject.authorities.contains('ROLE_SUPERADMIN')")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),
				new Document("name", "Login type Configuration").append("roleAllowed", "")
						.append("description", "SUPERADMIN can enable/disable a login type")
						.append("roleActionCheck",
								"action == 'CONFIGURE_LOGIN_TYPE' || action == 'GET_LOGIN_TYPES_CONFIG'")
						.append("condition", "subject.authorities.contains('ROLE_SUPERADMIN')")
						.append("createdDate", new Date()).append("lastModifiedDate", new Date())
						.append("isDeleted", false),

				new Document("name", "Fetch Sprint").append("roleAllowed", "")
						.append("description", "Any user can run active sprint fetch except guest user")
						.append("roleActionCheck",
								"!subject.authorities.contains('ROLE_GUEST') && action == 'TRIGGER_SPRINT_FETCH'")
						.append("condition", "true").append("createdDate", new Date())
						.append("lastModifiedDate", new Date()).append("isDeleted", false)

		);
	}

	@RollbackExecution
	public void rollback() {
		// We are inserting the documents through DDL, no rollback to any collections.
	}

}
