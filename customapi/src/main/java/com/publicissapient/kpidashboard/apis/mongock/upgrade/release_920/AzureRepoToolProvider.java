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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_920;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * @author kunkambl
 */
@ChangeUnit(id = "azure_repo_tool_provider", order = "9205", author = "kunkambl", systemVersion = "9.2.0")
public class AzureRepoToolProvider {

	private final MongoTemplate mongoTemplate;

	public AzureRepoToolProvider(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		Document document = new Document();
		document.put("toolName", "azurerepository");
		document.put("repoToolProvider", "azure");

		mongoTemplate.getCollection("repo_tools_provider").insertOne(document);
	}

	@RollbackExecution
	public void rollback() {
		mongoTemplate.getCollection("repo_tools_provider").deleteOne(new Document("toolName", "azurerepository"));
	}

}
