/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1220;

import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * Change Unit to update the kpi_column_configs collection for kpiId "kpi118"
 * and "kpi183"
 *
 * @author girpatha
 */
@ChangeUnit(id = "r_update_kpi_column_config_kpi118_kpi183", order = "012203", author = "girpatha", systemVersion = "12.2.0")
public class UpdateKpiColumnConfigKpi118Kpi183 {

	private static final String KPI_COLUMN_DETAILS = "kpiColumnDetails";
	private static final String COLUMN_NAME = "columnName";
	private static final String ORDER = "order";
	private static final String KPI_118 = "kpi118";
	private static final String KPI_183 = "kpi183";
	private static final String ENV = "Environment";
	private static final String BASIC_PROJECT_CONFIG_ID = "basicProjectConfigId";
	private static final String IS_DEFAULT = "isDefault";
	private static final String PROJECT_NAME = "Project Name";
	private static final String IS_SHOWN = "isShown";
	private static final String KPI_COLUMN_CONFIGS = "kpi_column_configs";
	private static final String KPI_ID = "kpiId";
	private static final String WEEKS = "Weeks";
	private final MongoTemplate mongoTemplate;

	public UpdateKpiColumnConfigKpi118Kpi183(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		// Revert the changes by deleting the new documents
		mongoTemplate.getCollection(KPI_COLUMN_CONFIGS).deleteMany(new Document(KPI_ID, KPI_118));
		mongoTemplate.getCollection(KPI_COLUMN_CONFIGS).deleteMany(new Document(KPI_ID, KPI_183));

		// Insert original document for kpiId "kpi118"
		Document originalDocumentKpi118 = new Document().append(BASIC_PROJECT_CONFIG_ID, null).append(KPI_ID, KPI_118)
				.append(KPI_COLUMN_DETAILS, List.of(
						new Document(COLUMN_NAME, PROJECT_NAME).append(ORDER, 1).append(IS_SHOWN, true).append(IS_DEFAULT, true),
						new Document(COLUMN_NAME, "Job Name").append(ORDER, 2).append(IS_SHOWN, true).append(IS_DEFAULT, true),
						new Document(COLUMN_NAME, "Date").append(ORDER, 3).append(IS_SHOWN, true).append(IS_DEFAULT, true),
						new Document(COLUMN_NAME, "Pipeline Name").append(ORDER, 4).append(IS_SHOWN, true).append(IS_DEFAULT, true),
						new Document(COLUMN_NAME, WEEKS).append(ORDER, 5).append(IS_SHOWN, true).append(IS_DEFAULT, true),
						new Document(COLUMN_NAME, ENV).append(ORDER, 6).append(IS_SHOWN, true).append(IS_DEFAULT, true)));
		mongoTemplate.getCollection(KPI_COLUMN_CONFIGS).insertOne(originalDocumentKpi118);

		// Insert original document for kpiId "kpi183"
		Document originalDocumentKpi183 = new Document().append(BASIC_PROJECT_CONFIG_ID, null).append(KPI_ID, KPI_183)
				.append(KPI_COLUMN_DETAILS, List.of(
						new Document(COLUMN_NAME, PROJECT_NAME).append(ORDER, 1).append(IS_SHOWN, true).append(IS_DEFAULT, true),
						new Document(COLUMN_NAME, "Job Name").append(ORDER, 2).append(IS_SHOWN, true).append(IS_DEFAULT, true),
						new Document(COLUMN_NAME, "Date").append(ORDER, 3).append(IS_SHOWN, true).append(IS_DEFAULT, true),
						new Document(COLUMN_NAME, "Pipeline Name").append(ORDER, 4).append(IS_SHOWN, true).append(IS_DEFAULT, true),
						new Document(COLUMN_NAME, WEEKS).append(ORDER, 5).append(IS_SHOWN, true).append(IS_DEFAULT, true),
						new Document(COLUMN_NAME, ENV).append(ORDER, 6).append(IS_SHOWN, true).append(IS_DEFAULT, true)));
		mongoTemplate.getCollection(KPI_COLUMN_CONFIGS).insertOne(originalDocumentKpi183);
	}

	@RollbackExecution
	public void rollback() {
		// Delete existing documents based on kpiId
		mongoTemplate.getCollection(KPI_COLUMN_CONFIGS).deleteMany(new Document(KPI_ID, KPI_118));
		mongoTemplate.getCollection(KPI_COLUMN_CONFIGS).deleteMany(new Document(KPI_ID, KPI_183));

		// Insert new document for kpiId "kpi118"
		Document newDocumentKpi118 = new Document().append(BASIC_PROJECT_CONFIG_ID, null).append(KPI_ID, KPI_118)
				.append(KPI_COLUMN_DETAILS, List.of(
						new Document(COLUMN_NAME, PROJECT_NAME).append(ORDER, 1).append(IS_SHOWN, true).append(IS_DEFAULT, true),
						new Document(COLUMN_NAME, "Job Name / Pipeline Name").append(ORDER, 2).append(IS_SHOWN, true)
								.append(IS_DEFAULT, true),
						new Document(COLUMN_NAME, "Date").append(ORDER, 3).append(IS_SHOWN, true).append(IS_DEFAULT, true),
						new Document(COLUMN_NAME, WEEKS).append(ORDER, 4).append(IS_SHOWN, true).append(IS_DEFAULT, true),
						new Document(COLUMN_NAME, ENV).append(ORDER, 5).append(IS_SHOWN, true).append(IS_DEFAULT, true)));
		mongoTemplate.getCollection(KPI_COLUMN_CONFIGS).insertOne(newDocumentKpi118);

		// Insert new document for kpiId "kpi183"
		Document newDocumentKpi183 = new Document().append(BASIC_PROJECT_CONFIG_ID, null).append(KPI_ID, KPI_183)
				.append(KPI_COLUMN_DETAILS, List.of(
						new Document(COLUMN_NAME, PROJECT_NAME).append(ORDER, 1).append(IS_SHOWN, true).append(IS_DEFAULT, true),
						new Document(COLUMN_NAME, "Job Name / Pipeline Name").append(ORDER, 2).append(IS_SHOWN, true)
								.append(IS_DEFAULT, true),
						new Document(COLUMN_NAME, "Date").append(ORDER, 3).append(IS_SHOWN, true).append(IS_DEFAULT, true),
						new Document(COLUMN_NAME, WEEKS).append(ORDER, 4).append(IS_SHOWN, true).append(IS_DEFAULT, true),
						new Document(COLUMN_NAME, ENV).append(ORDER, 5).append(IS_SHOWN, true).append(IS_DEFAULT, true)));
		mongoTemplate.getCollection(KPI_COLUMN_CONFIGS).insertOne(newDocumentKpi183);
	}
}
