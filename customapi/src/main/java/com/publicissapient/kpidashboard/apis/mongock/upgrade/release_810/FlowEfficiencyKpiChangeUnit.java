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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_810;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * add flow efficiency kpi and field mapping
 * 
 * @author kunkambl
 */
@ChangeUnit(id = "flow_efficiency_kpi", order = "8107", author = "kunkambl", systemVersion = "8.1.0")
public class FlowEfficiencyKpiChangeUnit {
	private final MongoTemplate mongoTemplate;

	private static final String DEFINITION = "definition";
	private static final String FIELD_NAME = "fieldName";

	public FlowEfficiencyKpiChangeUnit(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		addFlowEfficiencyKpiToKpiMaster();
		addFlowEfficiencyFieldMappingStructure();
	}

	public void addFlowEfficiencyKpiToKpiMaster() {
		Document kpiDocument = new Document().append("kpiId", "kpi170").append("kpiName", "Flow Efficiency")
				.append("kpiUnit", "%").append("isDeleted", "False").append("defaultOrder", 1)
				.append("kpiCategory", "Backlog").append("kpiSource", "Jira").append("groupId", 11)
				.append("thresholdValue", "").append("kanban", false).append("chartType", "line")
				.append("kpiInfo", new Document(DEFINITION,
						"The percentage of time spent in work states vs wait states across the lifecycle of an issue"))
				.append("xAxisLabel", "Duration").append("yAxisLabel", "Percentage").append("isPositiveTrend", false)
				.append("kpiFilter", "dropDown").append("showTrend", false).append("aggregationCriteria", "average")
				.append("isAdditionalFilterSupport", false).append("calculateMaturity", false)
				.append("kpiSubCategory", "Flow KPIs");
		mongoTemplate.getCollection("kpi_master").insertOne(kpiDocument);
	}

	public void addFlowEfficiencyFieldMappingStructure() {

		Document closeStatusDocument = new Document(FIELD_NAME, "jiraIssueClosedStateKPI170")
				.append("fieldLabel", "Status to identify Close Statuses").append("fieldCategory", "workflow")
				.append("fieldType", "chips").append("section", "WorkFlow Status Mapping")
				.append("tooltip", new Document(DEFINITION,
						"All statuses that signify an issue is 'DONE' based on 'Definition Of Done'"));

		Document waitStatusDocument = new Document(FIELD_NAME, "jiraIssueWaitStateKPI170")
				.append("fieldLabel", "Status to identify Wait Statuses").append("fieldCategory", "workflow")
				.append("fieldType", "chips").append("section", "WorkFlow Status Mapping")
				.append("tooltip", new Document(DEFINITION,
						"The statuses wherein no activity takes place and signifies that issue is in queue"));

		mongoTemplate.getCollection("field_mapping_structure")
				.insertMany(Arrays.asList(waitStatusDocument, closeStatusDocument));

	}

	@RollbackExecution
	public void rollback() {
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
}
