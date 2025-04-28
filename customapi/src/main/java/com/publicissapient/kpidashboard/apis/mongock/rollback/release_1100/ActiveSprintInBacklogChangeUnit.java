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

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1100;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * Change unit to insert a new field mapping for including active sprint data in
 * backlog KPIs.
 * 
 * @author shunaray
 */
@ChangeUnit(id = "r_active_sprint_filtration_backlog", order = "011002", author = "shunaray", systemVersion = "11.0.0")
public class ActiveSprintInBacklogChangeUnit {

	private final MongoTemplate mongoTemplate;

	public ActiveSprintInBacklogChangeUnit(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		mongoTemplate.getCollection("field_mapping_structure")
				.deleteOne(new Document("fieldName", "includeActiveSprintInBacklogKPI"));
	}

	@RollbackExecution
	public void rollback() {
		Document fieldMapping = new Document().append("fieldName", "includeActiveSprintInBacklogKPI")
				.append("fieldLabel", "Backlog KPIs include active sprint data").append("fieldType", "toggle")
				.append("toggleLabelLeft", "Exclude active sprint data")
				.append("toggleLabelRight", "Include active sprint data").append("section", "WorkFlow Status Mapping")
				.append("processorCommon", false).append("tooltip", new Document("definition",
						"Enabled State Backlog KPIs will populate including the active sprint data."));

		mongoTemplate.getCollection("field_mapping_structure").insertOne(fieldMapping);
	}

}