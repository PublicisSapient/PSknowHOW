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
package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_920;

import java.util.Collections;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author purgupta2
 */
@ChangeUnit(id = "release_plan_kpi", order = "9204", author = "purgupta2", systemVersion = "9.2.0")
public class ReleasePlanKpi {

	public static final String FIELD_MAPPING_STRUCTURE = "field_mapping_structure";
	public static final String FIELD_LABEL = "fieldLabel";
	public static final String FIELD_NAME = "fieldName";
	public static final String DEFINITION = "definition";
	public static final String LABELS = "Labels";
	private static final String KPI_ID = "kpiId";
	private static final String KPI_MASTER = "kpi_master";

	private final MongoTemplate mongoTemplate;

	public ReleasePlanKpi(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		addToKpiMaster();
	}

	public void addToKpiMaster() {

		Document kpiDocument = new Document().append(KPI_ID, "kpi179").append("kpiName", "Release Plan")
				.append("maxValue", "").append("kpiUnit", "Count").append("isDeleted", "False").append("defaultOrder", 1)
				.append("kpiCategory", "Release").append("kpiSource", "Jira").append("combinedKpiSource", "Jira/Azure")
				.append("isPositiveTrend", true).append("showTrend", false).append("groupId", 9).append("thresholdValue", "")
				.append("kanban", false).append("chartType", "CumulativeMultilineChart").append("yAxisLabel", "Count")
				.append("xAxisLabel", "").append("isAdditionalfFilterSupport", false).append("kpiFilter", "")
				.append("calculateMaturity", false)
				.append("kpiInfo", new Document().append(DEFINITION,
						"Displays the cumulative daily planned dues of the release based on the due dates of work items within the release scope.\n\nAdditionally, it provides an overview of the entire release scope.")
						.append("details", Collections.singletonList(new Document("type", "link").append("kpiLinkDetail",
								new Document().append("text", "Detailed Information at").append("link",
										"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/147652609/Release+Release+Plan")))))
				.append("kpiSubCategory", "Speed").append("kpiWidth", 100).append("boxType", "chart");
		// Insert the document into the collection
		mongoTemplate.getCollection(KPI_MASTER).insertOne(kpiDocument);
	}

	@RollbackExecution
	public void rollback() {
		mongoTemplate.getCollection(KPI_MASTER).deleteOne(new Document(KPI_ID, "kpi179"));
	}
}
