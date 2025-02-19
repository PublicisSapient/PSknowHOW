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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1210;

import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author prijain3
 */
@ChangeUnit(id = "iteration_kpi_column_config_update", order = "12105", author = "prijain3", systemVersion = "12.1.0")
public class IterationKpiColumnConfigUpdate {

	private final MongoTemplate mongoTemplate;

	private static final String COLUMN_NAME = "columnName";
	private static final String IS_SHOWN = "isShown";
	private static final String ORDER = "order";
	private static final String IS_DEFAULT = "isDefault";
	private static final String KPI_ID = "kpiId";
	private static final String KPI_COLUMN_DETAILS = "kpiColumnDetails";
	private static final String ISSUE_TYPE = "Issue Type";
	private static final String ISSUE_DESCRIPTION = "Issue Description";
	private static final String SIZE = "Size(story point/hours)";
	private static final String PRIORITY = "Priority";
	private static final String ASSIGNEE = "Assignee";
	private static final String ISSUE_STATUS = "Issue Status";

	public IterationKpiColumnConfigUpdate(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		MongoCollection<Document> kpiColumnConfig = mongoTemplate.getCollection("kpi_column_configs");
		// Iteration burnup KPI
		Document filter = new Document(KPI_ID, "kpi125");
		Document update = new Document("$set", new Document(KPI_COLUMN_DETAILS, List.of(
				new Document().append(COLUMN_NAME, "Issue Id").append(ORDER, 1).append(IS_SHOWN, true).append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, ISSUE_TYPE).append(ORDER, 2).append(IS_SHOWN, true).append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, ISSUE_DESCRIPTION).append(ORDER, 3).append(IS_SHOWN, true).append(IS_DEFAULT,
						true),
				new Document().append(COLUMN_NAME, SIZE).append(ORDER, 4).append(IS_SHOWN, true).append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, PRIORITY).append(ORDER, 5).append(IS_SHOWN, true).append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, ASSIGNEE).append(ORDER, 6).append(IS_SHOWN, true).append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, ISSUE_STATUS).append(ORDER, 7).append(IS_SHOWN, true).append(IS_DEFAULT,
						true),
				new Document().append(COLUMN_NAME, "Due Date").append(ORDER, 8).append(IS_SHOWN, true).append(IS_DEFAULT,
						true))));

		// Perform the update
		kpiColumnConfig.updateMany(filter, update);

		// Defect Count by KPI
		Document filter1 = new Document(KPI_ID, "kpi136");
		Document update1 = new Document("$set", new Document(KPI_COLUMN_DETAILS, List.of(
				new Document().append(COLUMN_NAME, "Issue Id").append(ORDER, 0).append(IS_SHOWN, true).append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, ISSUE_DESCRIPTION).append(ORDER, 1).append(IS_SHOWN, true).append(IS_DEFAULT,
						true),
				new Document().append(COLUMN_NAME, ISSUE_STATUS).append(ORDER, 2).append(IS_SHOWN, true).append(IS_DEFAULT,
						true),
				new Document().append(COLUMN_NAME, ISSUE_TYPE).append(ORDER, 3).append(IS_SHOWN, true).append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, SIZE).append(ORDER, 4).append(IS_SHOWN, true).append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, "Root Cause List").append(ORDER, 5).append(IS_SHOWN, true).append(IS_DEFAULT,
						true),
				new Document().append(COLUMN_NAME, PRIORITY).append(ORDER, 6).append(IS_SHOWN, true).append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, ASSIGNEE).append(ORDER, 7).append(IS_SHOWN, true).append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, "Created during Iteration").append(ORDER, 8).append(IS_SHOWN, true)
						.append(IS_DEFAULT, true))));

		// Perform the update
		kpiColumnConfig.updateMany(filter1, update1);

		List.of("kpi176", "kpi124", "kpi128").forEach(kpiId -> {
			Document filter2 = new Document(KPI_ID, kpiId);
			Document categoryColumn = new Document("$push", new Document(KPI_COLUMN_DETAILS, new Document()
					.append(COLUMN_NAME, "Category").append(ORDER, 15).append(IS_SHOWN, true).append(IS_DEFAULT, true)));
			kpiColumnConfig.updateMany(filter2, categoryColumn);
		});
	}

	@RollbackExecution
	public void rollback() {
		MongoCollection<Document> kpiColumnConfig = mongoTemplate.getCollection("kpi_column_configs");
		// Iteration burnup KPI
		Document filter = new Document(KPI_ID, "kpi125");
		Document update = new Document("$set", new Document(KPI_COLUMN_DETAILS, List.of(
				new Document().append(COLUMN_NAME, "Issue ID").append(ORDER, 0).append(IS_SHOWN, true).append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, ISSUE_TYPE).append(ORDER, 1).append(IS_SHOWN, true).append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, ISSUE_DESCRIPTION).append(ORDER, 2).append(IS_SHOWN, true).append(IS_DEFAULT,
						true),
				new Document().append(COLUMN_NAME, ISSUE_STATUS).append(ORDER, 3).append(IS_SHOWN, true).append(IS_DEFAULT,
						true),
				new Document().append(COLUMN_NAME, SIZE).append(ORDER, 4).append(IS_SHOWN, true).append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, "Planned Completion Date (Due Date)").append(ORDER, 5).append(IS_SHOWN, true)
						.append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, "Actual Completion Date").append(ORDER, 6).append(IS_SHOWN, true)
						.append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, "Remaining Estimate").append(ORDER, 7).append(IS_SHOWN, true)
						.append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, "Potential Delay(in days)").append(ORDER, 8).append(IS_SHOWN, true)
						.append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, "Predicted Completion Date").append(ORDER, 9).append(IS_SHOWN, true)
						.append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, ASSIGNEE).append(ORDER, 10).append(IS_SHOWN, true).append(IS_DEFAULT,
						true))));

		// Perform the update
		kpiColumnConfig.updateMany(filter, update);

		// Defect Count by KPI
		Document filter1 = new Document(KPI_ID, "kpi136");
		Document update1 = new Document("$set", new Document(KPI_COLUMN_DETAILS, List.of(
				new Document().append(COLUMN_NAME, "Defect ID").append(ORDER, 0).append(IS_SHOWN, true).append(IS_DEFAULT,
						true),
				new Document().append(COLUMN_NAME, ISSUE_DESCRIPTION).append(ORDER, 1).append(IS_SHOWN, true).append(IS_DEFAULT,
						true),
				new Document().append(COLUMN_NAME, ISSUE_STATUS).append(ORDER, 2).append(IS_SHOWN, true).append(IS_DEFAULT,
						true),
				new Document().append(COLUMN_NAME, ISSUE_TYPE).append(ORDER, 3).append(IS_SHOWN, true).append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, SIZE).append(ORDER, 4).append(IS_SHOWN, true).append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, "Root Cause").append(ORDER, 5).append(IS_SHOWN, true).append(IS_DEFAULT,
						true),
				new Document().append(COLUMN_NAME, PRIORITY).append(ORDER, 6).append(IS_SHOWN, true).append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, ASSIGNEE).append(ORDER, 7).append(IS_SHOWN, true).append(IS_DEFAULT, true),
				new Document().append(COLUMN_NAME, "Created during Iteration").append(ORDER, 8).append(IS_SHOWN, true)
						.append(IS_DEFAULT, true))));

		// Perform the update
		kpiColumnConfig.updateMany(filter1, update1);

		List.of("kpi176", "kpi124", "kpi128").forEach(kpiId -> {
			Document filter2 = new Document(KPI_ID, kpiId);
			Document categoryColumn = new Document("$pull", new Document(KPI_COLUMN_DETAILS, new Document()
					.append(COLUMN_NAME, "Category").append(ORDER, 15).append(IS_SHOWN, true).append(IS_DEFAULT, true)));
			kpiColumnConfig.updateMany(filter2, categoryColumn);
		});
	}
}
