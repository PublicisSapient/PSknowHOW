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

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Removing repotool trace logs as a part of scm-processor refactoring
 *
 * @author kunkambl
 */
@ChangeUnit(id = "remove_repo_tool_tracelog", order = "101010", author = "kunkambl", systemVersion = "10.1.0")
public class RemoveRepoToolTraceLogs {

	private final MongoTemplate mongoTemplate;

	public RemoveRepoToolTraceLogs(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		mongoTemplate.getCollection("processor_execution_trace_log")
				.deleteMany(new Document("processorName", "RepoTool"));
	}

}
