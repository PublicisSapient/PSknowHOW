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

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_810;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author kunkambl
 */
@ChangeUnit(id = "r_flow_efficiency_kpi", order = "08107", author = "kunkambl", systemVersion = "8.1.0")
public class FlowEfficiencyKpiChangeUnit {

	private static final String DEFINITION = "definition";
	private static final String FIELD_NAME = "fieldName";
	private final MongoTemplate mongoTemplate;

	public FlowEfficiencyKpiChangeUnit(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		addFlowEfficiencyKpiToKpiMasterRollback();
		addFlowEfficiencyFieldMappingStructureRollback();
	}

	public void addFlowEfficiencyKpiToKpiMasterRollback() {
		String kpiIdToDelete = "kpi170";
		Document filter = new Document("kpiId", kpiIdToDelete);
		mongoTemplate.getCollection("kpi_master").deleteOne(filter);
	}

	public void addFlowEfficiencyFieldMappingStructureRollback() {
		List<String> fieldNamesToDelete = Arrays.asList("jiraIssueClosedStateKPI170", "jiraIssueWaitStateKPI170");
		Document filter = new Document(FIELD_NAME, new Document("$in", fieldNamesToDelete));
		mongoTemplate.getCollection("field_mapping_structure").deleteMany(filter);
	}

	@RollbackExecution
	public void rollback() {
		addFlowEfficiencyKpiToKpiMaster();
		addFlowEfficiencyFieldMappingStructure();
	}

	public void addFlowEfficiencyKpiToKpiMaster() {
		Document kpiDocument = new Document().append("kpiId", "kpi170").append("kpiName", "Flow Efficiency")
				.append("kpiUnit", "").append("isDeleted", "False").append("defaultOrder", 1)
				.append("kpiCategory", "Backlog").append("kpiSource", "Jira").append("groupId", 11)
				.append("thresholdValue", "").append("kanban", false).append("chartType", "line")
				.append("kpiInfo", new Document(DEFINITION,
						"Flow load indicates how many items are currently in the backlog. This KPI emphasizes on limiting work in progress to enabling a fast flow of issues"))
				.append("xAxisLabel", "Duration").append("yAxisLabel", "Percentage").append("isPositiveTrend", false)
				.append("kpiFilter", "dropDown").append("showTrend", false).append("aggregationCriteria", "average")
				.append("isAdditionalFilterSupport", false).append("calculateMaturity", false)
				.append("kpiSubCategory", "Flow KPIs");
		mongoTemplate.getCollection("kpi_master").insertOne(kpiDocument);
	}

	public void addFlowEfficiencyFieldMappingStructure() {

		Document closeStatusDocument = new Document(FIELD_NAME, "jiraIssueClosedStateKPI170")
				.append("fieldLabel", "Status to identify Close Statuses").append("fieldType", "chips")
				.append("section", "WorkFlow Status Mapping").append("tooltip", new Document(DEFINITION,
						"All statuses that signify an issue is 'DONE' based on 'Definition Of Done'"));

		Document waitStatusDocument = new Document(FIELD_NAME, "jiraIssueWaitStateKPI170")
				.append("fieldLabel", "Status to identify Wait Statuses").append("fieldType", "chips")
				.append("section", "WorkFlow Status Mapping").append("tooltip", new Document(DEFINITION,
						"The statuses wherein no activity takes place and signifies that issue is in queue"));

		mongoTemplate.getCollection("field_mapping_structure")
				.insertMany(Arrays.asList(waitStatusDocument, closeStatusDocument));

	}

}
