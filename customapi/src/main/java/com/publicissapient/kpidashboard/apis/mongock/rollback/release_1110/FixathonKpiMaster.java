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
package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1110;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shi6
 */
@ChangeUnit(id = "r_fixathon_kpi_master", order = "011101", author = "shi6", systemVersion = "11.1.0")
public class FixathonKpiMaster {

	public static final String FIELD_TYPE = "fieldType";
	public static final String CHIPS = "chips";
	public static final String FIELD_NAME = "fieldName";
	public static final String JIRA_DEFECT_REJECTION_STATUS_KPI_151 = "jiraDefectRejectionStatusKPI151";
	public static final String TEXT = "text";
	public static final String JIRA_STATUS_FOR_REFINED_KPI_161 = "jiraStatusForRefinedKPI161";
	public static final String FIELD_LABEL = "fieldLabel";
	public static final String STATUS_TO_IDENTIFY_IN_READY_FOR_DEV = "Status to identify In Ready For Dev";
	private final MongoTemplate mongoTemplate;

	public FixathonKpiMaster(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		changeKpiName("kpi38","Sonar Violations");
		changeKpiName("kpi64","Sonar Violations");
	}

	private void updateLeadTimeForChange() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection("kpi_master");
		kpiMaster.updateMany(new Document(), new Document("$unset", new Document("kpiInfo.details", "")));

		// Step 2: Set new details
		kpiMaster.updateMany(new Document(), new Document("$set", new Document("kpiInfo.details", List.of(
				new Document("type", "paragraph").append("value",
						"LEAD TIME FOR CHANGE Captures the time between a code change to commit and deployed to production."),
				new Document("type", "link").append("kpiLinkDetail",
						new Document("text", "Detailed Information at").append("link",
								"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/71663772/DORA+Lead+time+for+changes"))))));

	}

	// Change Y-axis of Release Frequency to ‘No. of Releases'
	public void updateYAxisLabel() {
		mongoTemplate.getCollection("kpi_master").updateMany(
				new Document("kpiId", new Document("$in", Arrays.asList("kpi73", "kpi74"))),
				new Document("$set", new Document("yAxisLabel", "No. of Releases")));
	}

	public void changeKpiName(String kpiId, String kpiName) {
		mongoTemplate.getCollection("kpi_master").updateMany(
				new Document("kpiId", new Document("$in", Arrays.asList(kpiId))),
				new Document("$set", new Document("kpiName", kpiName)));
	}

	@RollbackExecution
	public void rollBack() {
		updateLeadTimeForChange();
		updateYAxisLabel();
		changeKpiName("kpi38","Code Violations");
		changeKpiName("kpi64","Code Violations");
	}

}