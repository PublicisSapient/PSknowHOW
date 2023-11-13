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

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_810;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.model.Filters;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author eswbogol
 */
@ChangeUnit(id = "r_dir_ftpr_labels_changeLog", order = "08103", author = "eswbogol", systemVersion = "8.1.0")
public class DIRFTPRLabelsChangeLog {

	private final MongoTemplate mongoTemplate;
	private static final String FIELD_MAPPING_STRUCTURE = "field_mapping_structure";
	private static final String FIELD_NAME = "fieldName";

	public DIRFTPRLabelsChangeLog(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		deleteFieldMappingStructure();
	}

	public void deleteFieldMappingStructure() {
		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE).deleteOne(Filters.eq(FIELD_NAME, "jiraLabelsKPI14"));
		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE).deleteOne(Filters.eq(FIELD_NAME, "jiraLabelsKPI82"));
	}

	@RollbackExecution
	public void rollback() {
		//rollback
	}

}
