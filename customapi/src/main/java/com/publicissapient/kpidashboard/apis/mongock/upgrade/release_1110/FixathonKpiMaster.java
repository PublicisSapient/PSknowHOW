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
@ChangeUnit(id = "fixathon_kpi_master", order = "11101", author = "shi6", systemVersion = "11.1.0")
public class FixathonKpiMaster {

	public static final String FIELD_TYPE = "fieldType";
	public static final String FIELD_NAME = "fieldName";
	public static final String FIELD_LABEL = "fieldLabel";
	private static final String KPIID = "kpiId";
	private static final String KPIINFO_DETAILS = "kpiInfo.details";
	private final MongoTemplate mongoTemplate;

	public FixathonKpiMaster(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection("kpi_master");
		// Change Y-axis of Release Frequency to ‘No. of Releases'
		updateYAxisLabel(kpiMaster, Arrays.asList("kpi73", "kpi74"), "No. of Releases");
		changeKpiName("kpi38", "Code Violations", kpiMaster);
		changeKpiName("kpi64", "Code Violations", kpiMaster);
		changeKpiName("kpi124", "Issue Hygiene", kpiMaster);
		// renaming Issue without Story link to Unlinked Work Items
		changeKpiName("kpi129", "Unlinked Work Items", kpiMaster);
		updateDuplicateInfo(kpiMaster);
		updateMaturityInfo("kpi5");
		// renaming Iteration BurnUp y-axis
		updateYAxisLabel(kpiMaster, List.of("kpi125"), "Issue Count");
		//Mean Time to Recover KPI, update aggregation criteria from sum to average
		updateAggregationCriteria(kpiMaster, Arrays.asList("kpi166"), "average");
	}

	private void updateDuplicateInfo(MongoCollection<Document> kpiMaster) {

		kpiMaster.updateMany(new Document(KPIID, "kpi156"), new Document("$unset", new Document(KPIINFO_DETAILS, "")));
		kpiMaster.updateMany(new Document(KPIID, "kpi150"), new Document("$unset", new Document(KPIINFO_DETAILS, "")));

		// Step 2: Set new details
		kpiMaster.updateMany(new Document(KPIID, "kpi156"), new Document("$set", new Document(KPIINFO_DETAILS, List.of(
				new Document("type", "paragraph").append("value",
						"LEAD TIME FOR CHANGE Captures the time between a code change to commit and deployed to production."),
				new Document("type", "link").append("kpiLinkDetail",
						new Document("text", "Detailed Information at").append("link",
								"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/71663772/DORA+Lead+time+for+changes"))))));

		// Step 2: Set new details
		kpiMaster.updateMany(new Document(KPIID, "kpi150"),
				new Document("$set", new Document(KPIINFO_DETAILS, List.of(new Document("type", "link")
						.append("kpiLinkDetail", new Document("text", "Detailed Information at").append("link",
								"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70484023/Release+Release+Burnup"))))));

	}


	public void updateYAxisLabel(MongoCollection<Document> kpiMaster, List<String> kpiIds, String yAxisLabel) {
		kpiMaster.updateMany(new Document("kpiId", new Document("$in", kpiIds)),
				new Document("$set", new Document("yAxisLabel", yAxisLabel)));
	}

	public void changeKpiName(String kpiId, String kpiName, MongoCollection<Document> kpiMaster) {
		kpiMaster.updateMany(new Document("kpiId", new Document("$in", Arrays.asList(kpiId))),
				new Document("$set", new Document("kpiName", kpiName)));
	}

	private void updateMaturityInfo(String kpiId) {
		Document query = new Document("kpiId", kpiId);
		Document update = new Document("$set",
				new Document().append("maturityRange", Arrays.asList("60-", "40-60", "25-40", "10-25", "0-10"))
						.append("aggregationCriteria", "deviation").append("calculateMaturity", true));
		mongoTemplate.getCollection("kpi_master").updateOne(query, update);
	}

	private void updateMaturityInfoRollBack(String kpiId) {
		Document query = new Document("kpiId", kpiId);
		Document update = new Document("$unset",
				new Document().append("maturityRange", Arrays.asList("60-", "40-60", "25-40", "10-25", "0-10"))
						.append("aggregationCriteria", "deviation").append("calculateMaturity", true));
		mongoTemplate.getCollection("kpi_master").updateOne(query, update);
	}

	private void updateAggregationCriteria(MongoCollection<Document> kpiMaster, List<String> kpiIds, String aggCriteria) {
		kpiMaster.updateMany(new Document("kpiId", new Document("$in", kpiIds)),
				new Document("$set", new Document("aggregationCriteria", aggCriteria)));
	}

	@RollbackExecution
	public void rollBack() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection("kpi_master");
		updateYAxisLabel(kpiMaster, Arrays.asList("kpi73", "kpi74"), "Count");
		changeKpiName("kpi38", "Sonar Violations", kpiMaster);
		changeKpiName("kpi64", "Sonar Violations", 	kpiMaster);
		changeKpiName("kpi124", "Estimation Hygiene", kpiMaster);
		// rollback to Issue without Story link from Unlinked Work Items
		changeKpiName("kpi129", "Issues Without Story Link", kpiMaster);
		//rollback Iteration BurnUp y-axis change
		updateYAxisLabel(kpiMaster, List.of("kpi125"), "Count");
		updateMaturityInfoRollBack("kpi5");
		//Mean Time to Recover KPI, rollback aggregation criteria from average to sum
		updateAggregationCriteria(kpiMaster, Arrays.asList("kpi166"), "sum");
	}

}