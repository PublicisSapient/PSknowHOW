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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1221;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * Fix for DTS-44135: Clean and re-fetch Jira issue epicLinked field data
 * 
 * @author shunaray
 */
@ChangeUnit(id = "jira_trace_clean", order = "12211", author = "shunaray", systemVersion = "12.2.1")
public class TraceLogChangeUnit {

	private final MongoTemplate mongoTemplate;

	public TraceLogChangeUnit(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		deleteProcessorExecutionTraceLog();
	}

	public void deleteProcessorExecutionTraceLog() {
		mongoTemplate.getCollection("processor_execution_trace_log").deleteMany(
				new Document("basicProjectConfigId", "6762bc10ed9a4560dc380a67").append("processorName", "Jira"));
	}

	@RollbackExecution
	public void rollback() {
		// doesn't require rollback
	}
}
