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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_810;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author eswbogol
 */
@ChangeUnit(id = "dre_definition_changeLog", order = "8112", author = "eswbogol", systemVersion = "8.1.0")
public class DREDefinitionChangeLog {

	private final MongoTemplate mongoTemplate;

	public DREDefinitionChangeLog(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		updateKpiDefinition();
		updateDREFieldMappingStructure();
	}

	public void updateKpiDefinition() {
		mongoTemplate.getCollection("kpi_master").updateOne(new Document("kpiId", "kpi34"),
				new Document("$set", new Document("kpiInfo.definition",
						"Measure of percentage of defects closed against the total count tagged to the iteration")));
	}

	public void updateDREFieldMappingStructure() {
		Document filter = new Document("fieldName", "jiraDodKPI14");
		Document update = new Document("$set", new Document()
				.append("fieldLabel", "Status considered for Issue closure")
				.append("tooltip", new Document("definition",
						"Status considered for issue closure (Mention completed status of all types of issues)")));

		mongoTemplate.getCollection("field_mapping_structure").updateOne(filter, update);
	}

	@RollbackExecution
	public void rollback() {
		rollbackFieldMappingStructure();
	}

	public void rollbackFieldMappingStructure() {
		// provide rollback script
	}

}
