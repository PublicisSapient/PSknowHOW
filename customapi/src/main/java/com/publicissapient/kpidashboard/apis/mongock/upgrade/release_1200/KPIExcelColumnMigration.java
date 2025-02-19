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
import com.mongodb.client.model.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChangeUnit(id = "excel_Column_Migration", order = "12002", author = "aksshriv1", systemVersion = "12.0.0")
public class KPIExcelColumnMigration {
	public static final String COLUMN_NAME = "columnName";
	public static final String KPI_ID = "kpiId";
	public static final String IS_SHOWN = "isShown";
	public static final String ORDER = "order";
	public static final String BASIC_PROJECT_CONFIG_ID = "basicProjectConfigId";
	public static final String IS_DEFAULT = "isDefault";
	public static final String KPI_COLUMN_DETAILS = "kpiColumnDetails";
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

			// Step 2: Delete all data except for the Iteration kpiIds and
			// LEAD_TIME_FOR_CHANGE
			List<String> kpiIdsToKeep = Arrays.asList("kpi128", "kpi121", "kpi119", "kpi75", "kpi123", "kpi122", "kpi120",
					"kpi124", "kpi132", "kpi133", "kpi134", "kpi125", "kpi131", "kpi135", "kpi136", "kpi140", "kpi145", "kpi154",
					"kpi176", "Kpi146", "Kpi156");

			sourceCollection.deleteMany(new Document(KPI_ID, new Document("$nin", kpiIdsToKeep)));
			log.info("Deleted unwanted kpi_column_configs entries.");

			// Step 3: Insert new data from the KPIExcelColumn enum
			for (KPIExcelColumn kpiExcelColumn : KPIExcelColumn.values()) {
				// Check if the kpiId already exists in the collection
				Document existingEntry = sourceCollection.find(new Document(KPI_ID, kpiExcelColumn.getKpiId())).first();
				if (existingEntry != null) {
					log.info("Skipping insertion for existing kpiId: {}", kpiExcelColumn.getKpiId());
					continue; // Skip this kpiId if it already exists
				}

				// Prepare the new document
				Document document = new Document();
				document.put(BASIC_PROJECT_CONFIG_ID, null);
				document.put(KPI_ID, kpiExcelColumn.getKpiId());
				document.put(KPI_COLUMN_DETAILS, createColumnDetails(kpiExcelColumn));
				// Insert into the collection
				sourceCollection.insertOne(document);
				log.info("Inserted new entry for kpiId: {}", kpiExcelColumn.getKpiId());
			}

			// Document for kpi156
			Document kpi156 = new Document(BASIC_PROJECT_CONFIG_ID, null).append(KPI_ID, "kpi156").append(KPI_COLUMN_DETAILS,
					List.of(
							new Document(COLUMN_NAME, "Project Name").append(ORDER, 1).append(IS_SHOWN, true).append(IS_DEFAULT,
									true),
							new Document(COLUMN_NAME, "Weeks").append(ORDER, 2).append(IS_SHOWN, true).append(IS_DEFAULT, true),
							new Document(COLUMN_NAME, "Story ID").append(ORDER, 3).append(IS_SHOWN, true).append(IS_DEFAULT, true),
							new Document(COLUMN_NAME, "Lead Time (In Days) [B-A]").append(ORDER, 4).append(IS_SHOWN, true)
									.append(IS_DEFAULT, true),
							new Document(COLUMN_NAME, "Change Completion Date [A]").append(ORDER, 5).append(IS_SHOWN, true)
									.append(IS_DEFAULT, true),
							new Document(COLUMN_NAME, "Change Release Date [B]").append(ORDER, 6).append(IS_SHOWN, true)
									.append(IS_DEFAULT, true),
							new Document(COLUMN_NAME, "Merge Request Id").append(ORDER, 7).append(IS_SHOWN, true).append(IS_DEFAULT,
									false),
							new Document(COLUMN_NAME, "Branch").append(ORDER, 8).append(IS_SHOWN, true).append(IS_DEFAULT, false)));
			// Document for kpi146
			Document kpi146 = new Document(BASIC_PROJECT_CONFIG_ID, null).append(KPI_ID, "kpi146").append(KPI_COLUMN_DETAILS,
					List.of(new Document(COLUMN_NAME, "Date").append(ORDER, 1).append(IS_SHOWN, true).append(IS_DEFAULT, true)));
			// Insert documents
			sourceCollection.insertMany(List.of(kpi156, kpi146));

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
			// Remove documents with the specified kpiIds
			sourceCollection.deleteMany(Filters.in(KPI_ID, "kpi156", "kpi146"));
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
			columnDetail.put(COLUMN_NAME, columnNames.get(i));
			columnDetail.put(ORDER, i + 1);
			columnDetail.put(IS_SHOWN, true);
			columnDetail.put(IS_DEFAULT, true);
			columnDetails.add(columnDetail);
		}
		return columnDetails;
	}
}
