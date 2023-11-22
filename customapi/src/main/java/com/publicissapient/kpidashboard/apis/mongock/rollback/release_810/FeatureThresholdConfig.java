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
 * provide rollback scripts
 * 
 * @author shi6
 */
@ChangeUnit(id = "r_feature_threshold_config", order = "08101", author = "shi6", systemVersion = "8.1.0")
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
		List<String> fieldNamesToDelete = Arrays.asList("thresholdValueKPI14", "thresholdValueKPI82",
				"thresholdValueKPI111", "thresholdValueKPI35", "thresholdValueKPI34", "thresholdValueKPI37",
				"thresholdValueKPI28", "thresholdValueKPI36", "thresholdValueKPI16", "thresholdValueKPI17",
				"thresholdValueKPI38", "thresholdValueKPI27", "thresholdValueKPI72", "thresholdValueKPI84",
				"thresholdValueKPI11", "thresholdValueKPI62", "thresholdValueKPI64", "thresholdValueKPI67",
				"thresholdValueKPI65", "thresholdValueKPI157", "thresholdValueKPI158", "thresholdValueKPI159",
				"thresholdValueKPI160", "thresholdValueKPI164");
		Document filter = new Document("fieldName", new Document("$in", fieldNamesToDelete));
		fieldMappingStructure.deleteMany(filter);
		return true;

	}

	@RollbackExecution
	public void rollback() {
		//do not require the implementation
	}

	@RollbackBeforeExecution
	public void rollbackBeforeExecution() {
		//do not require the implementation
	}

}
