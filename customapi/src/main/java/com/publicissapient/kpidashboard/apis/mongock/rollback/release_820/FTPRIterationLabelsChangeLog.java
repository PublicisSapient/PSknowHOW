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

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_820;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.model.Filters;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author eswbogol
 */
@ChangeUnit(id = "r_ftpr_iteration_labels_changeLog", order = "08203", author = "eswbogol", systemVersion = "8.2.0")
public class FTPRIterationLabelsChangeLog {

	private final MongoTemplate mongoTemplate;
	private static final String FIELD_MAPPING_STRUCTURE = "field_mapping_structure";
	private static final String FIELD_NAME = "fieldName";

	public FTPRIterationLabelsChangeLog(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		deleteFieldMappingStructure();
	}

	public void deleteFieldMappingStructure() {
		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE).deleteOne(Filters.eq(FIELD_NAME, "jiraLabelsKPI135"));
	}

	@RollbackExecution
	public void rollback() {
		rollbackFieldMappingStructure();
	}

	public void rollbackFieldMappingStructure() {
		// provide rollback script
	}

}
