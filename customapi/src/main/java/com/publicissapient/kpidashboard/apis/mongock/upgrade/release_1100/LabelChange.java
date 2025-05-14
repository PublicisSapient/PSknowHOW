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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1100;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "label_change", order = "11004", author = "shi6", systemVersion = "11.0.0")
public class LabelChange {
	private final MongoTemplate mongoTemplate;

	public LabelChange(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		MongoCollection<Document> collection = mongoTemplate.getCollection("kpi_master");
		updateLabel(collection, Arrays.asList("kpi73", "kpi74"), "Months", "Count");
		updateLabel(collection, Arrays.asList("kpi153"), "PIs", "Business Value");
		updateLabel(collection, Arrays.asList("kpi113"), "Months", "Cost of Delay");
	}

	@RollbackExecution
	public void rollback() {
		MongoCollection<Document> collection = mongoTemplate.getCollection("kpi_master");
		updateLabel(collection, Arrays.asList("kpi73", "kpi74"), null, null);
		updateLabel(collection, Arrays.asList("kpi153"), null, null);
		updateLabel(collection, Arrays.asList("kpi113"), null, null);
	}

	public void updateLabel(MongoCollection<Document> kpiMaster, List<String> kpiIds, String xAxisLabel,
			String yAxisLabel) {
		kpiMaster.updateMany(new Document("kpiId", new Document("$in", kpiIds)),
				new Document("$set", new Document("xAxisLabel", xAxisLabel).append("yAxisLabel", yAxisLabel)));
	}

}