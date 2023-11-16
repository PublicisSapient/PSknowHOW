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

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.BeforeExecution;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackBeforeExecution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shi6
 */
@ChangeUnit(id = "feature_threshold_config", order = "8101", author = "shi6", systemVersion = "8.1.0")
public class FeatureThresholdConfig {

	private final MongoTemplate mongoTemplate;
	private MongoCollection<Document> fieldMappingStructure;

	public FeatureThresholdConfig(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@BeforeExecution
	public void beforeExecution() {
		fieldMappingStructure = mongoTemplate.getCollection("field_mapping_structure");
	}

	@Execution
	public boolean execution() {
		insertFieldMapping();
		return true;
	}

	public void insertFieldMapping() {
		fieldMappingStructure.insertMany(Arrays.asList(createDocument("thresholdValueKPI14"),
				createDocument("thresholdValueKPI82"), createDocument("thresholdValueKPI111"),
				createDocument("thresholdValueKPI35"), createDocument("thresholdValueKPI34"),
				createDocument("thresholdValueKPI37"), createDocument("thresholdValueKPI28"),
				createDocument("thresholdValueKPI36"), createDocument("thresholdValueKPI16"),
				createDocument("thresholdValueKPI17"), createDocument("thresholdValueKPI38"),
				createDocument("thresholdValueKPI27"), createDocument("thresholdValueKPI72"),
				createDocument("thresholdValueKPI84"), createDocument("thresholdValueKPI11"),
				createDocument("thresholdValueKPI62"), createDocument("thresholdValueKPI64"),
				createDocument("thresholdValueKPI67"), createDocument("thresholdValueKPI157"),
				createDocument("thresholdValueKPI158"), createDocument("thresholdValueKPI159"),
				createDocument("thresholdValueKPI160"), createDocument("thresholdValueKPI164")));

	}

	private Document createDocument(String fieldName) {
		return new Document("fieldName", fieldName).append("fieldLabel", "Target KPI Value")
				.append("fieldType", "number").append("section", "Custom Fields Mapping")
				.append("tooltip", new Document("definition",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"));
	}

	@RollbackExecution
	public void rollback() {
		rollBackFieldMappingStructure();
	}

	public void rollBackFieldMappingStructure() {
		List<String> fieldNamesToDelete = Arrays.asList("thresholdValueKPI14", "thresholdValueKPI82",
				"thresholdValueKPI111", "thresholdValueKPI35", "thresholdValueKPI34", "thresholdValueKPI37",
				"thresholdValueKPI28", "thresholdValueKPI36", "thresholdValueKPI16", "thresholdValueKPI17",
				"thresholdValueKPI38", "thresholdValueKPI27", "thresholdValueKPI72", "thresholdValueKPI84",
				"thresholdValueKPI11", "thresholdValueKPI62", "thresholdValueKPI64", "thresholdValueKPI67",
				"thresholdValueKPI65", "thresholdValueKPI157", "thresholdValueKPI158", "thresholdValueKPI159",
				"thresholdValueKPI160", "thresholdValueKPI164");
		Document filter = new Document("fieldName", new Document("$in", fieldNamesToDelete));

		// Delete documents that match the filter
		fieldMappingStructure.deleteMany(filter);
	}

	@RollbackBeforeExecution
	public void rollbackBeforeExecution() {
		// do not rquire the implementation
	}
}
