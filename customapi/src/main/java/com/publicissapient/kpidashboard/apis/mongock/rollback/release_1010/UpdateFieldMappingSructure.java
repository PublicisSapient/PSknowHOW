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
package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1010;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author girpatha
 */
@ChangeUnit(id = "r_update_fieldmapping_structure", order = "010102", author = "girpatha", systemVersion = "10.1.0")
public class UpdateFieldMappingSructure {

	private final MongoTemplate mongoTemplate;
	private static final String FIELD_NAME = "jiraTestAutomationIssueType";
	private static final String FIELD_LABEL = "In Sprint Automation - Issue Types with Linked Test Case ";

	public UpdateFieldMappingSructure(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {

		rollbackFieldLabel(FIELD_NAME);
	}

	@RollbackExecution
	public void rollback() {

		updateFieldLabel(FIELD_NAME, FIELD_LABEL);
	}

	private void rollbackFieldLabel(String fieldName) {
		Update update = new Update();
		update.unset("fieldLabel");
		mongoTemplate.updateFirst(getQueryByFieldName(fieldName), update, "field_mapping_structure");
	}

	private void updateFieldLabel(String fieldName, String fieldLabel) {
		Update update = new Update();
		update.set("fieldLabel", fieldLabel);
		mongoTemplate.updateFirst(getQueryByFieldName(fieldName), update, "field_mapping_structure");
	}

	private Query getQueryByFieldName(String fieldName) {
		Query query = new Query();
		query.addCriteria(Criteria.where("fieldName").is(fieldName));
		return query;
	}
}
