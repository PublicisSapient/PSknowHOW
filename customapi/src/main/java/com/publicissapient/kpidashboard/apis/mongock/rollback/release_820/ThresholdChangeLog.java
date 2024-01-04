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
 * @author shunaray
 */
@ChangeUnit(id = "r_threshold_change_log", order = "08208", author = "shunaray", systemVersion = "8.2.0")
public class ThresholdChangeLog {
	private final MongoTemplate mongoTemplate;
	private MongoCollection<Document> fieldMappingStructure;
	private MongoCollection<Document> kpiMaster;

	public ThresholdChangeLog(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@BeforeExecution
	public void beforeExecution() {
		fieldMappingStructure = mongoTemplate.getCollection("field_mapping_structure");
		kpiMaster = mongoTemplate.getCollection("kpi_master");
	}

	@Execution
	public boolean execution() {
		rollBackFieldMappingStructure();
		rollbackThresholdAndBg();
		return true;

	}

	public void rollBackFieldMappingStructure() {
		List<String> fieldNamesToDelete = Arrays.asList("thresholdValueKPI126", "thresholdValueKPI42",
				"thresholdValueKPI168", "thresholdValueKPI70", "thresholdValueKPI40", "thresholdValueKPI5",
				"thresholdValueKPI39", "thresholdValueKPI46", "thresholdValueKPI8", "thresholdValueKPI73",
				"thresholdValueKPI113", "thresholdValueKPI149", "thresholdValueKPI153", "thresholdValueKPI162",
				"thresholdValueKPI116", "thresholdValueKPI156", "thresholdValueKPI118", "thresholdValueKPI127",
				"thresholdValueKPI170", "thresholdValueKPI139", "thresholdValueKPI166");
		Document filter = new Document("fieldName", new Document("$in", fieldNamesToDelete));

		// Delete documents that match the filter
		fieldMappingStructure.deleteMany(filter);
	}
	private void rollbackThresholdAndBg() {
		List<String> kpiIds = Arrays.asList("kpi126", "kpi42", "kpi168", "kpi70", "kpi40", "kpi5", "kpi39", "kpi46",
				"kpi8", "kpi73", "kpi113", "kpi149", "kpi153", "kpi162", "kpi116", "kpi156", "kpi118", "kpi127",
				"kpi170", "kpi139","kpi166");

		Document filter = new Document("kpiId", new Document("$in", kpiIds));

		Document update = new Document("$unset", new Document("thresholdValue", "")
				.append("lowerThresholdBG", "")
				.append("upperThresholdBG", ""));

		kpiMaster.updateMany(filter, update);
	}

	@RollbackExecution
	public void rollback() {
		// do not require the implementation
	}

	@RollbackBeforeExecution
	public void rollbackBeforeExecution() {
		// do not require the implementation
	}

}
