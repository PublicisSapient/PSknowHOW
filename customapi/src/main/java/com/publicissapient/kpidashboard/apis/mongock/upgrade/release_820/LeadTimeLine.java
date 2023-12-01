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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_820;

import java.util.ArrayList;
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
@SuppressWarnings("java:S1192")
@ChangeUnit(id = "lead_time_line", order = "8205", author = "shi6", systemVersion = "8.2.0")
public class LeadTimeLine {
	private final MongoTemplate mongoTemplate;

	public LeadTimeLine(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public boolean execution() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection("kpi_master");
		Document filter = new Document("kpiId", "kpi3");

		// Define the update using the Updates class
		Document update = new Document("$set", new Document().append("thresholdValue", "20").append("kpiUnit", "Days")
				.append("chartType", "line")
				.append("kpiInfo.definition",
						"Lead Time is the time from the moment when the request was made by a client and placed on a board to when all work on this item is completed and the request was delivered to the client")
				.append("kpiInfo.formula", null)
				.append( "kpiFilter", "dropdown")
				.append("kpiInfo.details", Arrays.asList(new Document("type", "link").append("kpiLinkDetail",
						new Document().append("text", "Detailed Information at").append("link",
								"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70811702/Lead+time"))))
				.append("yAxisLabel", "Days").append("xAxisLabel", "Range").append("boxType", null)
				.append("kpiWidth", null).append("showTrend", true).append("aggregationCriteria", "sum")
				.append("lowerThresholdBG", "white").append("upperThresholdBG", "red")
				.append("maturityRange", Arrays.asList("-60", "60-45", "45-30", "30-10", "10-")));

		// Perform the update
		kpiMaster.updateOne(filter, update);

		mongoTemplate.getCollection("field_mapping_structure").insertOne(new Document("fieldName", "thresholdValueKPI3")
				.append("fieldLabel", "Target KPI Value").append("fieldType", "number")
				.append("section", "Custom Fields Mapping").append("tooltip", new Document("definition",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown")));
		return true;
	}

	@RollbackExecution
	public void rollbackOrdering() {
		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection("kpi_master");
		Document filter = new Document("kpiId", "kpi3");
		Document update = new Document("$set", new Document().append("thresholdValue", "20").append("kpiUnit", "Days")
				.append("chartType", "")
				.append( "kpiFilter", "multiSelectDropDown")
				.append("kpiInfo.definition",
						"Measures Total time between a request was made and  all work on this item is completed and the request was delivered .")
				.append("kpiInfo.formula",
						Arrays.asList(new Document("lhs",
								"It is calculated as the sum Ideation time, Development time & Release time")))
				.append("kpiInfo.details", Arrays.asList()).append("yAxisLabel", "").append("xAxisLabel", "")
				.append("kpiWidth", 100).append("showTrend", false).append("aggregationCriteria", null)
				.append("lowerThresholdBG", null).append("upperThresholdBG", null).append("boxType", "2_column")
				.append("maturityRange", null));

		// Perform the update
		kpiMaster.updateOne(filter, update);
		// Define the update operation using $push
		List<Document> newDetailsObjects = new ArrayList<>();
		newDetailsObjects.add(new Document("type", "paragraph").append("value",
				"Ideation time (Intake to DOR): Time taken from issue creation to it being ready for Sprint."));
		newDetailsObjects.add(new Document("type", "paragraph").append("value",
				"Development time (DOR to DOD): Time taken from start of work on an issue to it being completed in the Sprint as per DOD."));
		newDetailsObjects.add(new Document("type", "paragraph").append("value",
				"Release time (DOD to Live): Time taken between story completion to it going live."));
		newDetailsObjects.add(new Document("type", "link").append("kpiLinkDetail",
				new Document().append("text", "Detailed Information at").append("link",
						"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/2916400/BACKLOG+Governance#Lead-time")));

		Document pushObjects = new Document("$push",
				new Document("kpiInfo.details", new Document("$each", newDetailsObjects)));

		// Perform the update
		kpiMaster.updateOne(filter, pushObjects);
		mongoTemplate.getCollection("field_mapping_structure")
				.deleteOne(new Document("fieldName", "thresholdValueKPI3"));

	}
}
