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

import com.mongodb.client.model.IndexOptions;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author bogolesw
 */
@ChangeUnit(id = "ddl2", order = "002", author = "PSKnowHOW")
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
	private static final String TYPE_NAME = "typeName";
	private static final String STATUS = "status";
	private static final String SPRINT_ID = "sprintID";
	private static final String DEFECT_STORY_ID = "defectStoryID";
	private static final String BASIC_PROJECT_CONFIG_ID_1 = "basicProjectConfigId_1";
	private static final String CREATED_DATE = "createdDate";
	private static final String STORY_TYPE = "storyType";
	private static final String PROJECT_ID = "projectID";
	private static final String TEST_EXECUTION = "test_execution";
	private static final String PROCESSOR_ITEMS = "processor_items";
	private static final String MERGE_REQUESTS = "merge_requests";
	private static final String BUILD_DETAILS = "build_details";
	private static final String DEPLOYMENTS = "deployments";
	private static final String PROCESSOR_ITEM_ID = "processorItemId";

	public CreateDatabaseIndexesChangeLog(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		clearAndExecuteJiraIssueIndexes();
		clearAndExecuteJiraIssueCustomHistoryIndexes();
		clearAndExecuteKanbanJiraIssueIndexes();
		clearAndExecuteKanbanIssueCustomHistoryIndexes();
		clearAndExecuteSprintDetailsIndexes();
		clearAndExecuteTestCaseDetailsIndexes();
		clearAndExecuteTestExecutionIndexes();
		clearAndExecuteBuildDetailsIndexes();
		clearAndExecuteDeploymentsIndexes();
		clearAndExecuteUserInfoIndexes();
		clearAndExecuteUserTokenDataIndexes();
		clearAndExecuteMergeRequestsIndexes();
		clearAndExecuteProcessorItemsIndexes();
	}

	public void clearAndExecuteJiraIssueIndexes() {
		mongoTemplate.getDb().getCollection(JIRA_ISSUE).dropIndexes();
		// Index 1
		mongoTemplate.getCollection(JIRA_ISSUE).createIndex(new Document("_id", 1), new IndexOptions().name("_id_"));

		// Index 2
		mongoTemplate.getCollection(JIRA_ISSUE).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append(TYPE_NAME, 1),
				new IndexOptions().name("basicProjectConfigId_1_typeName_1"));

		// Index 3
		mongoTemplate.getCollection(JIRA_ISSUE).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append(SPRINT_ID, 1).append(TYPE_NAME, 1),
				new IndexOptions().name("basicProjectConfigId_1_sprintID_1_typeName_1"));

		// Index 4
		mongoTemplate.getCollection(JIRA_ISSUE).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append(TYPE_NAME, 1).append(STATUS, 1),
				new IndexOptions().name("basicProjectConfigId_1_typeName_1_status_1"));

		// Index 5
		mongoTemplate.getCollection(JIRA_ISSUE).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append(DEFECT_STORY_ID, 1).append(TYPE_NAME, 1),
				new IndexOptions().name("basicProjectConfigId_1_defectStoryID_1_typeName_1"));

		// Index 6
		mongoTemplate.getCollection(JIRA_ISSUE).createIndex(new Document(TYPE_NAME, 1).append(DEFECT_STORY_ID, 1),
				new IndexOptions().name("typeName_1_defectStoryID_1"));

		// Index 7
		mongoTemplate.getCollection(JIRA_ISSUE).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append("number", 1),
				new IndexOptions().name("basicProjectConfigId_1_number_1"));

		// Index 8
		mongoTemplate.getCollection(JIRA_ISSUE).createIndex(new Document(BASIC_PROJECT_CONFIG_ID, 1),
				new IndexOptions().name(BASIC_PROJECT_CONFIG_ID_1));

		// Index 9
		mongoTemplate.getCollection(JIRA_ISSUE).createIndex(
				new Document(SPRINT_ID, 1).append(BASIC_PROJECT_CONFIG_ID, 1).append(TYPE_NAME, 1),
				new IndexOptions().name("sprintID_1_basicProjectConfigId_1_typeName_1"));

		// Index 10
		mongoTemplate.getCollection(JIRA_ISSUE).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append(DEFECT_STORY_ID, 1).append(TYPE_NAME, 1),
				new IndexOptions().name("basicProjectConfigId_1_defectStoryID_1_typeName_1"));

		// Index 11
		mongoTemplate.getCollection(JIRA_ISSUE).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append(TYPE_NAME, 1).append("number", 1),
				new IndexOptions().name("basicProjectConfigId_1_typeName_1_number_1"));

		// Index 12
		mongoTemplate.getCollection(JIRA_ISSUE).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append(SPRINT_ID, 1).append(TYPE_NAME, 1),
				new IndexOptions().name("basicProjectConfigId_1_sprintID_1_typeName_1"));

		// Index 13
		mongoTemplate.getCollection(JIRA_ISSUE).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append(TYPE_NAME, 1).append(STATUS, 1),
				new IndexOptions().name("basicProjectConfigId_1_typeName_1_status_1"));

		// Index 14
		mongoTemplate.getCollection(JIRA_ISSUE).createIndex(
				new Document(SPRINT_ID, -1).append(BASIC_PROJECT_CONFIG_ID, 1),
				new IndexOptions().name("sprintID_-1_basicProjectConfigId_1"));

	}

	public void clearAndExecuteJiraIssueCustomHistoryIndexes() {
		mongoTemplate.getDb().getCollection(JIRA_ISSUE_CUSTOM_HISTORY).dropIndexes();
		// Index 1
		mongoTemplate.getCollection(JIRA_ISSUE_CUSTOM_HISTORY).createIndex(
				new Document("storyID", 1).append(BASIC_PROJECT_CONFIG_ID, 1),
				new IndexOptions().name("storyID_1_basicProjectConfigId_1"));

		// Index 2
		mongoTemplate.getCollection(JIRA_ISSUE_CUSTOM_HISTORY).createIndex(new Document("storyID", 1),
				new IndexOptions().name("storyID_1"));

		// Index 3
		mongoTemplate.getCollection(JIRA_ISSUE_CUSTOM_HISTORY).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append(CREATED_DATE, -1),
				new IndexOptions().name("basicProjectConfigId_1_createdDate_-1"));

		// Index 4
		mongoTemplate.getCollection(JIRA_ISSUE_CUSTOM_HISTORY).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append(CREATED_DATE, -1).append(STORY_TYPE, 1),
				new IndexOptions().name("basicProjectConfigId_1_createdDate_-1_storyType_1"));

		// Index 5
		mongoTemplate.getCollection(JIRA_ISSUE_CUSTOM_HISTORY).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append(STORY_TYPE, 1),
				new IndexOptions().name("basicProjectConfigId_1_storyType_1"));

		// Index 6
		mongoTemplate.getCollection(JIRA_ISSUE_CUSTOM_HISTORY).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append("storySprintDetails.fromStatus", 1),
				new IndexOptions().name("basicProjectConfigId_1_storySprintDetails.fromStatus_1"));
	}

	public void clearAndExecuteKanbanJiraIssueIndexes() {
		mongoTemplate.getDb().getCollection(KANBAN_JIRA_ISSUE).dropIndexes();
		mongoTemplate.getCollection(KANBAN_JIRA_ISSUE).createIndex(new Document(PROJECT_ID, 1).append(STATUS, 1),
				new IndexOptions().name("projectID_1_status_1"));

		// Index 2
		mongoTemplate.getCollection(KANBAN_JIRA_ISSUE).createIndex(new Document(PROJECT_ID, 1).append(TYPE_NAME, 1),
				new IndexOptions().name("projectID_1_typeName_1"));

		// Index 3
		mongoTemplate.getCollection(KANBAN_JIRA_ISSUE).createIndex(new Document(PROJECT_ID, 1).append(CREATED_DATE, -1),
				new IndexOptions().name("projectID_1_createdDate_-1"));

		// Index 4
		mongoTemplate.getCollection(KANBAN_JIRA_ISSUE).createIndex(
				new Document("processorId", 1).append(BASIC_PROJECT_CONFIG_ID, 1).append("changeDate", -1),
				new IndexOptions().name("processorId_1_basicProjectConfigId_1_changeDate_-1"));
	}

	public void clearAndExecuteKanbanIssueCustomHistoryIndexes() {
		mongoTemplate.getDb().getCollection(KANBAN_JIRA_ISSUE_CUSTOM_HISTORY).dropIndexes();
		// Index 1
		mongoTemplate.getCollection(KANBAN_JIRA_ISSUE_CUSTOM_HISTORY).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1), new IndexOptions().name(BASIC_PROJECT_CONFIG_ID_1));

		// Index 2
		mongoTemplate.getCollection(KANBAN_JIRA_ISSUE_CUSTOM_HISTORY).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append(STORY_TYPE, 1),
				new IndexOptions().name("basicProjectConfigId_1_storyType_1"));

		// Index 3
		mongoTemplate.getCollection(KANBAN_JIRA_ISSUE_CUSTOM_HISTORY).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append(TYPE_NAME, 1),
				new IndexOptions().name("basicProjectConfigId_1_typeName_1"));

		// Index 4
		mongoTemplate.getCollection(KANBAN_JIRA_ISSUE_CUSTOM_HISTORY).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append(STATUS, 1).append(TYPE_NAME, 1),
				new IndexOptions().name("basicProjectConfigId_1_status_1_typeName_1"));

		// Index 5
		mongoTemplate.getCollection(KANBAN_JIRA_ISSUE_CUSTOM_HISTORY).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append(CREATED_DATE, -1),
				new IndexOptions().name("basicProjectConfigId_1_createdDate_-1"));

		// Index 6
		mongoTemplate.getCollection(KANBAN_JIRA_ISSUE_CUSTOM_HISTORY).createIndex(
				new Document("processorId", 1).append(BASIC_PROJECT_CONFIG_ID, 1).append("changeDate", -1),
				new IndexOptions().name("processorId_1_basicProjectConfigId_1_changeDate_-1"));
	}

	public void clearAndExecuteSprintDetailsIndexes() {
		mongoTemplate.getDb().getCollection(SPRINT_DETAILS).dropIndexes();
		mongoTemplate.getCollection(SPRINT_DETAILS).createIndex(new Document(SPRINT_ID, -1),
				new IndexOptions().name("sprintID_-1").unique(true));

		// Index 2
		mongoTemplate.getCollection(SPRINT_DETAILS).createIndex(new Document(SPRINT_ID, 1),
				new IndexOptions().name("sprintID_1"));

		// Index 3
		mongoTemplate.getCollection(SPRINT_DETAILS).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append("state", 1),
				new IndexOptions().name("basicProjectConfigId_1_state_1"));
	}

	public void clearAndExecuteTestCaseDetailsIndexes() {
		mongoTemplate.getDb().getCollection(TEST_CASE_DETAILS).dropIndexes();
		// Index 1
		mongoTemplate.getCollection(TEST_CASE_DETAILS).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append("isTestCanBeAutomated", -1).append(TYPE_NAME, 1),
				new IndexOptions().name("basicProjectConfigId_1_isTestCanBeAutomated_-1_typeName_1"));

		// Index 2
		mongoTemplate.getCollection(TEST_CASE_DETAILS).createIndex(new Document(BASIC_PROJECT_CONFIG_ID, 1),
				new IndexOptions().name(BASIC_PROJECT_CONFIG_ID_1));

		// Index 3
		mongoTemplate.getCollection(TEST_CASE_DETAILS).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append(DEFECT_STORY_ID, 1),
				new IndexOptions().name("basicProjectConfigId_1_defectStoryID_1"));

		// Index 4
		mongoTemplate.getCollection(TEST_CASE_DETAILS).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append("isTestCanBeAutomated", 1).append(TYPE_NAME, 1),
				new IndexOptions().name("basicProjectConfigId_1_isTestCanBeAutomated_1_typeName_1"));
	}

	public void clearAndExecuteTestExecutionIndexes() {
		mongoTemplate.getDb().getCollection(TEST_EXECUTION).dropIndexes();
		mongoTemplate.getCollection(TEST_EXECUTION).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append(SPRINT_ID, 1),
				new IndexOptions().name("basicProjectConfigId_1_sprintId_1"));

		// Index 2
		mongoTemplate.getCollection(TEST_EXECUTION).createIndex(
				new Document(SPRINT_ID, 1).append(BASIC_PROJECT_CONFIG_ID, 1),
				new IndexOptions().name("sprintId_1_basicProjectConfigId_1"));
	}

	public void clearAndExecuteBuildDetailsIndexes() {
		mongoTemplate.getDb().getCollection(BUILD_DETAILS).dropIndexes();
		mongoTemplate.getCollection(BUILD_DETAILS).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append("buildStatus", 1),
				new IndexOptions().name("basicProjectConfigId_1_buildStatus_1"));

		// Index 2
		mongoTemplate.getCollection(BUILD_DETAILS).createIndex(
				new Document("buildStatus", 1).append("startTime", 1).append("endTime", 1).append(PROCESSOR_ITEM_ID, 1),
				new IndexOptions().name("buildStatus_1_startTime_1_endTime_1_processorItemId_1"));
	}

	public void clearAndExecuteDeploymentsIndexes() {
		mongoTemplate.getDb().getCollection(DEPLOYMENTS).dropIndexes();
		mongoTemplate.getCollection(DEPLOYMENTS).createIndex(
				new Document(BASIC_PROJECT_CONFIG_ID, 1).append("deploymentStatus", 1),
				new IndexOptions().name("basicProjectConfigId_1_deploymentStatus_1"));

		// Index 2
		mongoTemplate.getCollection(DEPLOYMENTS).createIndex(
				new Document("deploymentStatus", 1).append("startTime", 1).append("endTime", 1)
						.append("projectToolConfigId", 1),
				new IndexOptions().name("deploymentStatus_1_startTime_1_endTime_1_projectToolConfigId_1"));
	}

	public void clearAndExecuteUserInfoIndexes() {
		mongoTemplate.getDb().getCollection(USER_INFO).dropIndexes();
		mongoTemplate.getCollection(USER_INFO).createIndex(new Document("username", 1).append("authType", 1),
				new IndexOptions().name("username_1_authType_1"));

		// Index 2
		mongoTemplate.getCollection(USER_INFO).createIndex(new Document("username", 1),
				new IndexOptions().name("username_1"));
	}

	public void clearAndExecuteUserTokenDataIndexes() {
		mongoTemplate.getDb().getCollection("usertokendata").dropIndexes();
		mongoTemplate.getCollection("usertokendata").createIndex(new Document("userToken", 1),
				new IndexOptions().name("userToken_1"));
	}

	public void clearAndExecuteMergeRequestsIndexes() {
		mongoTemplate.getDb().getCollection(MERGE_REQUESTS).dropIndexes();
		mongoTemplate.getCollection(MERGE_REQUESTS).createIndex(new Document("_id", 1),
				new IndexOptions().name("_id_"));

		// Create index for PROCESSOR_ITEM_ID field in the collection
		mongoTemplate.getCollection(MERGE_REQUESTS).createIndex(new Document(PROCESSOR_ITEM_ID, 1),
				new IndexOptions().name("processorItemId_1"));

		// Create compound index for PROCESSOR_ITEM_ID, CREATED_DATE, "fromBranch", and
		// "closedDate" fields in the collection
		mongoTemplate.getCollection(MERGE_REQUESTS)
				.createIndex(
						new Document(PROCESSOR_ITEM_ID, 1).append(CREATED_DATE, 1).append("fromBranch", 1)
								.append("closedDate", 1),
						new IndexOptions().name("processorItemId_1_createdDate_1_fromBranch_1_closedDate_1"));
	}

	public void clearAndExecuteProcessorItemsIndexes() {
		mongoTemplate.getDb().getCollection(PROCESSOR_ITEMS).dropIndexes();
		mongoTemplate.getCollection(PROCESSOR_ITEMS).createIndex(new Document("_id", 1),
				new IndexOptions().name("_id_"));

		// Create index for "toolConfigId" field in the collection
		mongoTemplate.getCollection(PROCESSOR_ITEMS).createIndex(new Document("toolConfigId", 1),
				new IndexOptions().name("toolConfigId_1"));
	}

	@RollbackExecution
	public void rollback() {
		// We are inserting the documents through DDL, no rollback to any collections.
	}
}
