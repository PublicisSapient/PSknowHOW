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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_820;

import java.util.Arrays;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * add flow efficiency kpi and field mapping
 * 
 * @author shi6
 */
@SuppressWarnings("java:S1192")
@ChangeUnit(id = "cycle_time", order = "8206", author = "shi6", systemVersion = "8.2.0")
public class CycleTime {
	private final MongoTemplate mongoTemplate;

	private static final String DEFINITION = "definition";
	private static final String FIELD_NAME = "fieldName";

	public CycleTime(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		addToKpiMaster();
		updateFieldMappingStructure();
	}

	public void addToKpiMaster() {

		Document kpiDocument = new Document().append("kpiId", "kpi171").append("kpiName", "Cycle Time")
				.append("maxValue", "").append("kpiUnit", "Days").append("isDeleted", "False")
				.append("defaultOrder", 12).append("kpiCategory", "Backlog").append("kpiSource", "Jira")
				.append("groupId", 11).append("thresholdValue", "").append("kanban", false)
				.append("chartType", "")
				.append("yAxisLabel", "").append("xAxisLabel", "").append("isAdditionalfFilterSupport", false)
				.append("kpiFilter", "multiSelectDropDown").append("boxType", "2_column").append("calculateMaturity", false)
				.append("kpiInfo",new Document()
						.append("definition", "Cycle time helps ascertain time spent on each step of the complete issue lifecycle. It is being depicted in the visualization as 3 core cycles - Intake to DOR, DOR to DOD, DOD to Live")
						.append("details", Arrays.asList(new Document("type", "link").append("kpiLinkDetail",
								new Document().append("text", "Detailed Information at").append("link",
										"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70s418714/Cycle+time")))))
				.append("kpiSubCategory", "Flow KPIs");
		// Insert the document into the collection
		mongoTemplate.getCollection("kpi_master").insertOne(kpiDocument);

		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection("kpi_master");

		// Update documents
		updateDocument(kpiMaster, "kpi146", 11);
		updateDocument(kpiMaster, "kpi148", 13);

	}

	private void updateDocument(MongoCollection<Document> kpiMaster, String kpiId, int defaultOrder) {
		// Create the filter
		Document filter = new Document("kpiId", kpiId);

		// Create the update
		Document update = new Document("$set", new Document("defaultOrder", defaultOrder));

		// Perform the update
		kpiMaster.updateOne(filter, update);
	}

	public void updateFieldMappingStructure() {

		MongoCollection<Document> fieldMappingStructure = mongoTemplate.getCollection("field_mapping_structure");

		// Update document with "fieldName" equal to "jiraDorKPI3"
		fieldMappingStructure.updateOne(Filters.eq("fieldName", "jiraDorKPI3"),
				Updates.set("fieldName", "jiraDorKPI171"));

		// Update document with "fieldName" equal to "jiraDodKPI3"
		fieldMappingStructure.updateOne(Filters.eq("fieldName", "jiraDodKPI3"),
				Updates.set("fieldName", "jiraDodKPI171"));

		// Update document with "fieldName" equal to "storyFirstStatusKPI3"
		fieldMappingStructure.updateOne(Filters.eq("fieldName", "storyFirstStatusKPI3"),
				Updates.set("fieldName", "storyFirstStatusKPI171"));

		Document liveStatusMapping = new Document(FIELD_NAME, "jiraLiveStatusKPI171")
				.append("fieldLabel", "Live Status - Cycle Time").append("fieldCategory", "workflow")
				.append("fieldType", "chips").append("section", "WorkFlow Status Mapping").append("tooltip",
						new Document(DEFINITION, "Status/es that identify that an issue is LIVE in Production'"));

		Document issueTypeMapping = new Document(FIELD_NAME, "jiraIssueTypeKPI171")
				.append("fieldLabel", "Issue type to be included").append("fieldCategory", "Issue_Type")
				.append("fieldType", "chips").append("section", "Issue Types Mapping").append("tooltip",
						new Document(DEFINITION, "All issue types that should be included in Lead time calculation."));

		fieldMappingStructure.insertMany(Arrays.asList(issueTypeMapping, liveStatusMapping));

	}

	@RollbackExecution
	public void rollback() {
		deleteKpiMaster();
		deleteFieldMappingStructure();
	}

	public void deleteKpiMaster() {
		mongoTemplate.getCollection("kpi_master").deleteOne(new Document("kpiId", "kpi171"));
	}

	public void deleteFieldMappingStructure() {
		MongoCollection<Document> fieldMappingStructure = mongoTemplate.getCollection("field_mapping_structure");

		// Update document with "fieldName" equal to "jiraDorKPI3"
		fieldMappingStructure.updateOne(Filters.eq("fieldName", "jiraDorKPI171"),
				Updates.set("fieldName", "jiraDorKPI3"));

		// Update document with "fieldName" equal to "jiraDodKPI3"
		fieldMappingStructure.updateOne(Filters.eq("fieldName", "jiraDodKPI171"),
				Updates.set("fieldName", "jiraDodKPI3"));

		// Update document with "fieldName" equal to "storyFirstStatusKPI3"
		fieldMappingStructure.updateOne(Filters.eq("fieldName", "storyFirstStatusKPI171"),
				Updates.set("fieldName", "storyFirstStatusKPI3"));
		fieldMappingStructure.deleteMany(new Document(FIELD_NAME,
				new Document("$in", Arrays.asList("jiraLiveStatusKPI171", "jiraIssueTypeKPI171"))));
	}
}
