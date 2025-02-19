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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1010;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shunaray
 */
@ChangeUnit(id = "hygiene_estimate_logic", order = "10108", author = "shunaray", systemVersion = "10.1.0")
public class HygieneEstimateLogic {

	private final MongoTemplate mongoTemplate;

	public HygieneEstimateLogic(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		insertFieldMappingStructure();
	}

	public void insertFieldMappingStructure() {
		List<Document> documents = Arrays.asList(
				createDocument("jiraIssueTypeExcludeKPI75", "Issue types to be excluded", "chips", "Issue_Type",
						"Issue Types Mapping", "Specify issue types to exclude from KPI calculations."),
				createDocument("jiraIssueTypeExcludeKPI124", "Issue types to be excluded", "chips", "Issue_Type",
						"Issue Types Mapping", "Specify issue types to exclude from KPI calculations."));
		mongoTemplate.getCollection("field_mapping_structure").insertMany(documents);
	}

	private Document createDocument(String fieldName, String fieldLabel, String fieldType, String fieldCategory,
			String section, String definition) {
		return new Document("fieldName", fieldName).append("fieldLabel", fieldLabel).append("fieldType", fieldType)
				.append("processorCommon", false).append("fieldCategory", fieldCategory).append("section", section)
				.append("tooltip", new Document("definition", definition));
	}

	@RollbackExecution
	public void rollback() {
		rollbackFieldMappingStructure();
	}

	public void rollbackFieldMappingStructure() {
		List<String> fieldNames = Arrays.asList("jiraIssueTypeExcludeKPI75", "jiraIssueTypeExcludeKPI124");
		mongoTemplate.getCollection("field_mapping_structure")
				.deleteMany(new Document("fieldName", new Document("$in", fieldNames)));
	}
}
