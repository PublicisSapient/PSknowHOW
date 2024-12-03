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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1200;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChangeUnit(id = "excel_Column_Migration", order = "12002", author = "aksshriv1", systemVersion = "12.0.0")
public class KPIExcelColumnMigration {
	public static final String KPI_ID = "kpiId";
	private final MongoTemplate mongoTemplate;

	public KPIExcelColumnMigration(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void changeSet() {
		try {
			// Step 1: Create backup collection if not exists and backup existing data
			String backupCollectionName = "kpi_column_configs_backup";
			if (!mongoTemplate.collectionExists(backupCollectionName)) {
				mongoTemplate.createCollection(backupCollectionName);
			}

			MongoCollection<Document> backupCollection = mongoTemplate.getCollection(backupCollectionName);
			MongoCollection<Document> sourceCollection = mongoTemplate.getCollection("kpi_column_configs");

			// Backup existing data
			try (MongoCursor<Document> cursor = sourceCollection.find().iterator()) {
				while (cursor.hasNext()) {
					Document document = cursor.next();
					document.remove("_id"); // Remove the _id field to avoid duplicate key errors
					backupCollection.insertOne(document);
				}
			}

			// Step 2: Delete all data except for the Iteration kpiIds
			List<String> kpiIdsToKeep = Arrays.asList("kpi128", "kpi121", "kpi119", "kpi75", "kpi123", "kpi122",
					"kpi120", "kpi124", "kpi132", "kpi133", "kpi134", "kpi125", "kpi131", "kpi135", "kpi136", "kpi140",
					"kpi145", "kpi154", "kpi176");

			sourceCollection.deleteMany(new Document(KPI_ID, new Document("$nin", kpiIdsToKeep)));
			log.info("Deleted unwanted kpi_column_configs entries.");

			// Step 3: Insert new data from the KPIExcelColumn enum
			for (KPIExcelColumn kpiExcelColumn : KPIExcelColumn.values()) {
				// Check if the kpiId already exists in the collection
				Document existingEntry = sourceCollection.find(new Document(KPI_ID, kpiExcelColumn.getKpiId()))
						.first();
				if (existingEntry != null) {
					log.info("Skipping insertion for existing kpiId: {}", kpiExcelColumn.getKpiId());
					continue; // Skip this kpiId if it already exists
				}

				// Prepare the new document
				Document document = new Document();
				document.put("basicProjectConfigId", null);
				document.put(KPI_ID, kpiExcelColumn.getKpiId());
				document.put("kpiColumnDetails", createColumnDetails(kpiExcelColumn));

				// Insert into the collection
				sourceCollection.insertOne(document);
				log.info("Inserted new entry for kpiId: {}", kpiExcelColumn.getKpiId());
			}
			log.info("Inserted new KPI column configs from KPIExcelColumn enum.");
		} catch (Exception e) {
			log.error("Error during KPI column migration: ", e);
		}
	}

	@RollbackExecution
	public void rollback() {
		try {
			MongoCollection<Document> backupCollection = mongoTemplate.getCollection("kpi_column_configs_backup");
			if (backupCollection.countDocuments() == 0) {
				log.warn("No backup data found. Rollback skipped.");
				return;
			}

			// Clear the target collection
			MongoCollection<Document> sourceCollection = mongoTemplate.getCollection("kpi_column_configs");
			sourceCollection.deleteMany(new Document());

			// Restore backup
			try (MongoCursor<Document> cursor = backupCollection.find().iterator()) {
				while (cursor.hasNext()) {
					Document document = cursor.next();
					document.remove("_id"); // Remove the _id field to avoid duplicate key errors
					sourceCollection.insertOne(document);
				}
			}
			log.info("Rollback completed. Data restored from backup.");
		} catch (Exception e) {
			log.error("Error during rollback: ", e);
		}
	}

	private List<Document> createColumnDetails(KPIExcelColumn kpiExcelColumn) {
		List<Document> columnDetails = new ArrayList<>();
		List<String> columnNames = kpiExcelColumn.getColumns();

		// Defensive check for null or empty columnNames
		if (columnNames == null || columnNames.isEmpty()) {
			log.warn("No columns defined for KPI ID: {}", kpiExcelColumn.getKpiId());
			return columnDetails; // Return an empty list to avoid NullPointerException
		}

		for (int i = 0; i < columnNames.size(); i++) {
			Document columnDetail = new Document();
			columnDetail.put("columnName", columnNames.get(i));
			columnDetail.put("order", i + 1);
			columnDetail.put("isShown", true);
			columnDetail.put("isDefault", true);
			columnDetails.add(columnDetail);
		}
		return columnDetails;
	}

}
