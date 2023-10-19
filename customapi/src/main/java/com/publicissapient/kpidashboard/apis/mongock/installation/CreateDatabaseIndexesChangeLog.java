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

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "ddl2", order = "002", author = "PSKnowHOW", runAlways = true)
public class CreateDatabaseIndexesChangeLog {

	private final MongoTemplate mongoTemplate;
	private static final String JIRA_ISSUE = "jira_issue";
	private static final String JIRA_ISSUE_CUSTOM_HISTORY = "jira_issue_custom_history";
	private static final String KANBAN_JIRA_ISSUE = "kanban_jira_issue";
	private static final String KANBAN_JIRA_ISSUE_CUSTOM_HISTORY = "kanban_issue_custom_history";
	private static final String TEST_CASE_DETAILS = "test_case_details";
	private static final String BASIC_PROJECT_CONFIG_ID = "basicProjectConfigId";
	private static final String SPRINT_DETAILS = "sprint_details";
	private static final String USER_INFO = "user_info";

	public CreateDatabaseIndexesChangeLog(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void createIndexes() {
		createIndexIfNotExists(JIRA_ISSUE, BASIC_PROJECT_CONFIG_ID, 1);
		createIndexIfNotExists(JIRA_ISSUE, "sprintID_basicProjectConfigId_typeName", 1);
		createIndexIfNotExists(JIRA_ISSUE, "basicProjectConfigId_typeName", 1);
		createIndexIfNotExists(JIRA_ISSUE, "sprintID_basicProjectConfigId_typeName_jiraStatus", 1);
		createIndexIfNotExists(JIRA_ISSUE, "basicProjectConfigId_typeName_status", 1);
		createIndexIfNotExists(JIRA_ISSUE, "basicProjectConfigId_defectStoryID_typeName", 1);
		createIndexIfNotExists(JIRA_ISSUE, "basicProjectConfigId_typeName_defectStoryID", 1);
		createIndexIfNotExists(JIRA_ISSUE, "basicProjectConfigId_typeName_rootCauseList", 1);
		createIndexIfNotExists(JIRA_ISSUE, "basicProjectConfigId_typeName_number", 1);
		createIndexIfNotExists(JIRA_ISSUE, "typeName_defectStoryID", 1);
		createIndexIfNotExists(JIRA_ISSUE, "sprintID_basicProjectConfigId", -1);

		createIndexIfNotExists(JIRA_ISSUE_CUSTOM_HISTORY, "storyID", 1);
		createIndexIfNotExists(JIRA_ISSUE_CUSTOM_HISTORY, "storyID_basicProjectConfigId", 1);
		createIndexIfNotExists(JIRA_ISSUE_CUSTOM_HISTORY, "basicProjectConfigId_createdDate_storyType", 1);
		createIndexIfNotExists(JIRA_ISSUE_CUSTOM_HISTORY, "basicProjectConfigId_storyType", 1);
		createIndexIfNotExists(JIRA_ISSUE_CUSTOM_HISTORY, "basicProjectConfigId_storySprintDetails.fromStatus", 1);

		createIndexIfNotExists(KANBAN_JIRA_ISSUE, "projectID", 1);
		createIndexIfNotExists(KANBAN_JIRA_ISSUE, "projectID_status", 1);
		createIndexIfNotExists(KANBAN_JIRA_ISSUE, "projectID_typeName", 1);
		createIndexIfNotExists(KANBAN_JIRA_ISSUE, "projectID_createdDate", -1);
		createIndexIfNotExists(KANBAN_JIRA_ISSUE, "processorId_basicProjectConfigId_changeDate", -1);

		createIndexIfNotExists(KANBAN_JIRA_ISSUE_CUSTOM_HISTORY, BASIC_PROJECT_CONFIG_ID, 1);
		createIndexIfNotExists(KANBAN_JIRA_ISSUE_CUSTOM_HISTORY, "basicProjectConfigId_storyType", 1);
		createIndexIfNotExists(KANBAN_JIRA_ISSUE_CUSTOM_HISTORY, "basicProjectConfigId_typeName", 1);
		createIndexIfNotExists(KANBAN_JIRA_ISSUE_CUSTOM_HISTORY, "basicProjectConfigId_status_typeName", 1);
		createIndexIfNotExists(KANBAN_JIRA_ISSUE_CUSTOM_HISTORY, "basicProjectConfigId_createdDate", -1);
		createIndexIfNotExists(KANBAN_JIRA_ISSUE_CUSTOM_HISTORY, "processorId_basicProjectConfigId_changeDate", -1);

		createIndexIfNotExists(SPRINT_DETAILS, "sprintID", -1);

		createIndexIfNotExists(TEST_CASE_DETAILS, BASIC_PROJECT_CONFIG_ID, 1);
		createIndexIfNotExists(TEST_CASE_DETAILS, "basicProjectConfigId_defectStoryID", 1);
		createIndexIfNotExists(TEST_CASE_DETAILS, "basicProjectConfigId_isTestCanBeAutomated_typeName", 1);

		createIndexIfNotExists("test_execution", "sprintId_basicProjectConfigId", 1);

		createIndexIfNotExists("build_details", "buildStatus_startTime_endTime", 1);

		createIndexIfNotExists("deployments", "deploymentStatus_startTime_endTime_projectToolConfigId", 1);

		createIndexIfNotExists(USER_INFO, "username", 1);
		createIndexIfNotExists(USER_INFO, "username_authType", 1);

		createIndexIfNotExists("usertokendata", "userToken", 1);

		createIndexIfNotExists("merge_requests", "processorItemId_createdDate_fromBranch_closedDate", 1);

		createIndexIfNotExists("processor_items", "toolConfigId", 1);

		createUniqueIndex();
	}

	public void createUniqueIndex() {
		String indexName = "IDX_UNQ_SPRINTID";
		String fieldName = "sprintID";

		if (indexExists(SPRINT_DETAILS, indexName, -1)) {
			mongoTemplate.getDb().getCollection(SPRINT_DETAILS).createIndex(new Document(fieldName, 1),
					new IndexOptions().unique(true).name(indexName));
		}
	}

	private void createIndexIfNotExists(String collectionName, String indexName, int order) {
		if (indexExists(collectionName, indexName, order)) {
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document index = new Document(indexName, order);
			collection.createIndex(index);
		}
	}

	private boolean indexExists(String collectionName, String indexName, int order) {
		MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
		for (Document index : collection.listIndexes()) {
			if (index.get("name").equals(indexName)
					&& index.get("key", Document.class).equals(new Document(indexName, order))) {
				return false;
			}
		}
		return true;
	}

	@RollbackExecution
	public void rollback() {
		// We are inserting the documents through DDL, no rollback to any collections.
	}
}
