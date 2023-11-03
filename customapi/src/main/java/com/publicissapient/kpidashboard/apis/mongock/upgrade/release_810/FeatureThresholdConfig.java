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

		fieldMappingStructure.insertMany(Arrays.asList(createDocument("thresholdValueKPI14", "Target KPI Value",
				"number", "Custom Fields Mapping",
				"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI82", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI111", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI35", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI34", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI37", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI28", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI36", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI16", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI17", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI38", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI27", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI72", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI84", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI11", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI62", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI64", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI67", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI157", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI158", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI159", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI160", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"),
				createDocument("thresholdValueKPI164", "Target KPI Value", "number", "Custom Fields Mapping",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown")));

	}

	private Document createDocument(String fieldName, String fieldLabel, String fieldType, String section,
			String definition) {
		return new Document("fieldName", fieldName).append("fieldLabel", fieldLabel).append("fieldType", fieldType)
				.append("section", section).append("tooltip", new Document("definition", definition));
	}

	@RollbackExecution
	public void rollback() {
		rollbackKpiMaster();
	}

	public void rollbackKpiMaster() {
		String[] fieldNamesToDelete = { "thresholdValueKPI14", "thresholdValueKPI82", "thresholdValueKPI111",
				"thresholdValueKPI35", "thresholdValueKPI34", "thresholdValueKPI37", "thresholdValueKPI28",
				"thresholdValueKPI36", "thresholdValueKPI16", "thresholdValueKPI17", "thresholdValueKPI38",
				"thresholdValueKPI27", "thresholdValueKPI72", "thresholdValueKPI84", "thresholdValueKPI11",
				"thresholdValueKPI62", "thresholdValueKPI64", "thresholdValueKPI67", "thresholdValueKPI65",
				"thresholdValueKPI157", "thresholdValueKPI158", "thresholdValueKPI159", "thresholdValueKPI160",
				"thresholdValueKPI164" };
		Document filter = new Document("fieldName", new Document("$in", fieldNamesToDelete));

		// Delete documents that match the filter
		fieldMappingStructure.deleteMany(filter);
	}

	@RollbackBeforeExecution
	public void rollbackBeforeExecution() {
	}
}
