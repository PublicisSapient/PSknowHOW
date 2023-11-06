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
@ChangeUnit(id = "dir_ftpr_labels_changeLog", order = "8103", author = "eswbogol", systemVersion = "8.1.0")
public class DIRFTPRLabelsChangeLog {

	private final MongoTemplate mongoTemplate;
	private static final String FIELD_MAPPING_STRUCTURE = "field_mapping_structure";
	private static final String FIELD_NAME = "fieldName";

	public DIRFTPRLabelsChangeLog(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		insertJiraLabelsKPI14InFieldMappingStructure();
		insertJiraLabelsKPI82InFieldMappingStructure();
	}

	public void insertJiraLabelsKPI14InFieldMappingStructure() {
		Document document = new Document();
		document.append(FIELD_NAME, "jiraLabelsKPI14");
		document.append("fieldLabel", "Labels to identify issues to be included");
		document.append("fieldType", "chips");
		document.append("section", "WorkFlow Status Mapping");
		Document tooltip = new Document();
		tooltip.append("definition", "Calculation should only those issues which have defined labels tagged.");
		document.append("tooltip", tooltip);
		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE).insertOne(document);
	}

	public void insertJiraLabelsKPI82InFieldMappingStructure() {
		Document document = new Document();
		document.append(FIELD_NAME, "jiraLabelsKPI82");
		document.append("fieldLabel", "Labels to identify issues to be included");
		document.append("fieldType", "chips");
		document.append("section", "WorkFlow Status Mapping");
		Document tooltip = new Document();
		tooltip.append("definition", "Calculation should only those issues which have defined labels tagged.");
		document.append("tooltip", tooltip);
		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE).insertOne(document);
	}

	@RollbackExecution
	public void rollback() {
		rollbackFieldMappingStructure();
	}

	public void rollbackFieldMappingStructure() {
		// provide rollback script
	}

}
