/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_820;

import java.util.Arrays;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * add flow efficiency kpi and field mapping
 * 
 * @author shi6
 */
@SuppressWarnings("java:S1192")
@ChangeUnit(id = "r_cycle_time", order = "08206", author = "shi6", systemVersion = "8.2.0")
public class CycleTime {
	private final MongoTemplate mongoTemplate;
	private static final String FIELD_NAME = "fieldName";

	public CycleTime(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		deleteKpiMaster();
		deleteFieldMappingStructure();
	}

	public void deleteKpiMaster() {
		mongoTemplate.getCollection("kpi_master").deleteOne(new Document("kpiId", "kpi171"));
	}

	public void deleteFieldMappingStructure() {
		MongoCollection<Document> fieldMappingStructure = mongoTemplate.getCollection("field_mapping_structure");

		// Update document with "fieldName" equal to "jiraDorKPI3"
		fieldMappingStructure.updateOne(Filters.eq("fieldName", "jiraDorKPI171"),
				Updates.set("fieldName", "jiraDorKPI3"));

		// Update document with "fieldName" equal to "jiraDodKPI3"
		fieldMappingStructure.updateOne(Filters.eq("fieldName", "jiraDodKPI171"),
				Updates.set("fieldName", "jiraDodKPI3"));

		// Update document with "fieldName" equal to "storyFirstStatusKPI3"
		fieldMappingStructure.updateOne(Filters.eq("fieldName", "storyFirstStatusKPI171"),
				Updates.set("fieldName", "storyFirstStatusKPI3"));
		fieldMappingStructure.deleteMany(new Document(FIELD_NAME,
				new Document("$in", Arrays.asList("jiraLiveStatusKPI171", "jiraIssueTypeKPI171"))));
	}

	@RollbackExecution
	public void rollback() {
		// no implementation required
	}

}
