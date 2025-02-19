/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1010;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author kunkambl
 */
@ChangeUnit(id = "remove_repo_tool_project_configs", order = "101010", author = "kunkambl", systemVersion = "10.1.0")
public class RemoveRepoToolProjectConfigs {
	private final MongoTemplate mongoTemplate;
	private List<Document> deletedDocuments;

	public RemoveRepoToolProjectConfigs(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
		this.deletedDocuments = new ArrayList<>();
	}

	@Execution
	public void execution() {

		MongoCollection<Document> projectToolConfigs = mongoTemplate.getCollection("project_tool_configs");
		// Find and store the documents to be deleted
		deletedDocuments = projectToolConfigs.find(new Document("toolName", "RepoTool")).into(new ArrayList<>());

		// Delete the documents
		projectToolConfigs.deleteMany(new Document("toolName", "RepoTool"));
	}

	@RollbackExecution
	public void rollback() {
		// Restore the deleted documents
		if (deletedDocuments != null && !deletedDocuments.isEmpty()) {
			mongoTemplate.getCollection("project_tool_configs").insertMany(deletedDocuments);
		}
	}
}
