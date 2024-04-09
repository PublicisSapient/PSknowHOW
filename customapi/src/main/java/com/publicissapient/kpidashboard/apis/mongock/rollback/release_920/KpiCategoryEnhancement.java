/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_920;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.List;

@ChangeUnit(id = "r_kpi_category_enhancement", order = "09101", author = "kunkambl", systemVersion = "9.1.0")
public class KpiCategoryEnhancement {
	private final MongoTemplate mongoTemplate;
	private static final String KPI_ID = "kpiId";

	public KpiCategoryEnhancement(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execute() {
		rollbackUpdateKpiCategory();
	}

	public void updateKpiCategory() {
		MongoCollection<Document> kpiCategoryMappingCollection = mongoTemplate.getCollection("kpi_category_mapping");
		MongoCollection<Document> kpiMasterCollection = mongoTemplate.getCollection("kpi_master");

		List<WriteModel<Document>> bulkUpdates = new ArrayList<>();

		kpiCategoryMappingCollection.find().forEach(mapping -> {
			String kpiId = mapping.getString(KPI_ID);
			String categoryId = mapping.getString("categoryId");
			Document filter = new Document(KPI_ID, kpiId);
			Document update = new Document("$set", new Document("kpiCategory", categoryId));
			bulkUpdates.add(new UpdateOneModel<>(filter, update));
		});

		kpiMasterCollection.bulkWrite(bulkUpdates);
	}

	@RollbackExecution
	public void rollback() {
		updateKpiCategory();
	}

	public void rollbackUpdateKpiCategory() {
		MongoCollection<Document> kpiCategoryMappingCollection = mongoTemplate.getCollection("kpi_category_mapping");
		MongoCollection<Document> kpiMasterCollection = mongoTemplate.getCollection("kpi_master");

		List<String> kpiIdsInMapping = new ArrayList<>();

		kpiCategoryMappingCollection.find().forEach(mapping -> {
			String kpiId = mapping.getString(KPI_ID);
			kpiIdsInMapping.add(kpiId);
		});

		List<WriteModel<Document>> bulkUpdates = new ArrayList<>();

		for (String kpiId : kpiIdsInMapping) {
			Document filter = new Document(KPI_ID, kpiId);
			Document update = new Document("$unset", new Document("kpiCategory", ""));
			bulkUpdates.add(new UpdateOneModel<>(filter, update));
		}

		if (!bulkUpdates.isEmpty()) {
			kpiMasterCollection.bulkWrite(bulkUpdates);
		}
	}

}
