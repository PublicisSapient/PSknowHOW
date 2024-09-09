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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_920;

import java.util.Collections;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shi6
 */
@SuppressWarnings("java:S1192")
@ChangeUnit(id = "combination_defect_count", order = "9202", author = "shi6", systemVersion = "9.2.0")
public class CombinationDefectCount {
	public static final String FIELD_MAPPING_STRUCTURE = "field_mapping_structure";
	public static final String FIELD_LABEL = "fieldLabel";
	public static final String FIELD_NAME = "fieldName";
	public static final String DEFINITION = "definition";
	public static final String LABELS = "Labels";
	private static final String KPI_ID = "kpiId";
	private static final String KPI_MASTER = "kpi_master";
	private final MongoTemplate mongoTemplate;

	public CombinationDefectCount(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		Document kpiDocument = new Document().append(KPI_ID, "kpi178").append("kpiName", "Defect Count By")
				.append("isDeleted", "False").append("defaultOrder", 1).append("kpiCategory", "Release")
				.append("kpiSubCategory", "Quality").append("kpiSource", "Jira")
				.append("combinedKpiSource", "Jira/Azure").append("groupId", 9).append("kanban", false)
				.append("chartType", "chartWithFilter")
				.append("kpiInfo", new Document().append(DEFINITION,
						"It shows the breakup of all defects tagged to a release grouped by Status, Priority, or RCA.")
						.append("details", Collections.singletonList(new Document("type", "link").append(
								"kpiLinkDetail",
								new Document().append("text", "Detailed Information at").append("link",
										"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/144146433/Release+Defect+count+by")))))
				.append("isAdditionalFilterSupport", false).append("kpiFilter", "").append("boxType", "chart")
				.append("calculateMaturity", false);
		// Insert the document into the collection
		mongoTemplate.getCollection(KPI_MASTER).insertOne(kpiDocument);

	}

	@RollbackExecution
	public void rollback() {
		mongoTemplate.getCollection(KPI_MASTER).deleteOne(new Document(KPI_ID, "kpi178"));
	}

}
