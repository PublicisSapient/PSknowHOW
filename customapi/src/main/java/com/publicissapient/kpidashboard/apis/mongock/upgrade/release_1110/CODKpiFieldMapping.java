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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1110;

import java.util.Arrays;
import java.util.Collections;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.model.Filters;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "cod_kpi_field_mapping", order = "11105", author = "kunkambl", systemVersion = "11.1.0")
public class CODKpiFieldMapping {

	public static final String FIELD_NAME = "fieldName";
	public static final String ISSUE_TYPES_TO_CONSIDER_KPI_113 = "issueTypesToConsiderKpi113";
	public static final String CLOSED_ISSUE_STATUS_TO_CONSIDER_KPI_113 = "closedIssueStatusToConsiderKpi113";
	private final MongoTemplate mongoTemplate;

	public CODKpiFieldMapping(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		Document issueTypeFieldMapping = new Document().append(FIELD_NAME, ISSUE_TYPES_TO_CONSIDER_KPI_113)
				.append("fieldLabel", "Issue types to consider").append("fieldType", "chips")
				.append("fieldCategory", "Issue_Type").append("section", "Issue Types Mapping").append("processorCommon", false)
				.append("tooltip", new Document("definition", "All issue types used for epics or features"))
				.append("fieldDisplayOrder", 1).append("sectionOrder", 1).append("mandatory", true)
				.append("nodeSpecific", false);

		Document issueStatusFieldMapping = new Document().append(FIELD_NAME, CLOSED_ISSUE_STATUS_TO_CONSIDER_KPI_113)
				.append("fieldLabel", "Status to identify completed issues").append("fieldType", "chips")
				.append("section", "WorkFlow Status Mapping").append("fieldCategory", "workflow")
				.append("processorCommon", false)
				.append("tooltip", new Document("definition",
						"All workflow statuses to identify completed issues. If multiple statuses are specified, the first status an issue transitions to will be considered"))
				.append("fieldDisplayOrder", 1).append("sectionOrder", 1).append("mandatory", true)
				.append("nodeSpecific", false);

		mongoTemplate.getCollection("field_mapping_structure")
				.insertMany(Arrays.asList(issueTypeFieldMapping, issueStatusFieldMapping));

		Document update = new Document().append("$set",
				new Document().append(ISSUE_TYPES_TO_CONSIDER_KPI_113, Collections.singleton("Epic")));
		mongoTemplate.getCollection("field_mapping").updateMany(new Document(), update);
	}

	@RollbackExecution
	public void rollback() {
		mongoTemplate.getCollection("field_mapping_structure")
				.deleteMany(Filters.or(Filters.eq(FIELD_NAME, ISSUE_TYPES_TO_CONSIDER_KPI_113),
						Filters.eq(FIELD_NAME, CLOSED_ISSUE_STATUS_TO_CONSIDER_KPI_113)));

		Document update = new Document();
		update.append("$unset", new Document(ISSUE_TYPES_TO_CONSIDER_KPI_113, ""));
		update.append("$unset", new Document(CLOSED_ISSUE_STATUS_TO_CONSIDER_KPI_113, ""));
		mongoTemplate.getCollection("field_mapping").updateMany(new Document(), update);
	}
}
