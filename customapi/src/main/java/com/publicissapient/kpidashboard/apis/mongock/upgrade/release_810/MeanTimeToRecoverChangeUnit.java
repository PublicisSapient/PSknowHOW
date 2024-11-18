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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_810;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.publicissapient.kpidashboard.common.model.application.KpiMaster;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shunaray
 */
@SuppressWarnings("java:S1192")
@ChangeUnit(id = "mean_time_to_recover", order = "8105", author = "shunaray", systemVersion = "8.1.0")
public class MeanTimeToRecoverChangeUnit {
	private final MongoTemplate mongoTemplate;

	public MeanTimeToRecoverChangeUnit(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		insertMeanTimeToRecover();
		storyToIdentifyFieldMapping();
		productionIncidentFieldMapping();
		dodStatusFieldMapping();
		insertKpiColumnConfig();
		updateMetadataIdentifier();
		addLinkDetailToLeadTimeForChange();
	}

	public void insertMeanTimeToRecover() {
		Document kpiInfo = new Document();
		List<Document> details = new ArrayList<>();

		Document detail1 = new Document();
		detail1.append("type", "paragraph");
		detail1.append("value",
				"For all the production incident tickets raised during a time period, the time between created date and closed date of the incident ticket will be calculated.");
		details.add(detail1);

		Document detail2 = new Document();
		detail2.append("type", "paragraph");
		detail2.append("value", "The average of all such tickets will be shown.");
		details.add(detail2);

		Document detail3 = new Document();
		detail3.append("type", "link");
		detail3.append("kpiLinkDetail", new Document("text", "Detailed Information at").append("link",
				"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/59080705/DORA+KPIs#Mean-time-to-Recover-(MTTR)"));
		details.add(detail3);


		kpiInfo.append("definition",
				"Mean time to recover will be based on the Production incident tickets raised during a certain period of time.");
		kpiInfo.append("details", details);
		kpiInfo.append("maturityLevels", new ArrayList<>());

		Document kpiDocument = new Document();
		kpiDocument.append("kpiId", "kpi166");
		kpiDocument.append("kpiName", "Mean Time to Recover");
		kpiDocument.append("maxValue", "100");
		kpiDocument.append("kpiUnit", "Hours");
		kpiDocument.append("isDeleted", "False");
		kpiDocument.append("defaultOrder", 4);
		kpiDocument.append("kpiSource", "Jira");
		kpiDocument.append("kpiCategory", "Dora");
		kpiDocument.append("groupId", 15);
		kpiDocument.append("thresholdValue", 0);
		kpiDocument.append("kanban", false);
		kpiDocument.append("chartType", "line");
		kpiDocument.append("kpiInfo", kpiInfo);
		kpiDocument.append("xAxisLabel", "Weeks");
		kpiDocument.append("yAxisLabel", "Hours");
		kpiDocument.append("isPositiveTrend", false);
		kpiDocument.append("showTrend", true);
		kpiDocument.append("kpiFilter", "");
		kpiDocument.append("aggregationCriteria", "sum");
		kpiDocument.append("aggregationCircleCriteria", "average");
		kpiDocument.append("isAdditionalFilterSupport", false);
		kpiDocument.append("calculateMaturity", false);

		mongoTemplate.insert(kpiDocument, "kpi_master");
	}

	public void storyToIdentifyFieldMapping() {
		Document document = new Document("fieldName", "jiraStoryIdentificationKPI166")
				.append("fieldLabel", "Issue type to identify Production incidents").append("fieldType", "chips")
				.append("fieldCategory", "Issue_Type").append("section", "Issue Types Mapping")
				.append("tooltip", new Document("definition",
						"All issue types that are used as/equivalent to Production incidents."));
		mongoTemplate.insert(document, "field_mapping_structure");
	}

	public void productionIncidentFieldMapping() {
		Document field1 = new Document("fieldName", "jiraProductionIncidentIdentification")
				.append("fieldLabel", "Production incidents identification").append("fieldType", "radiobutton")
				.append("section", "Defects Mapping")
				.append("tooltip", new Document("definition",
						"This field is used to identify if a production incident is raised by third party or client:<br>1. CustomField : If a separate custom field is used<br>2. Labels : If a label is used to identify. Example: PROD_DEFECT (This has to be one value).<hr>"))
				.append("options",
						Arrays.asList(new Document("label", "CustomField").append("value", "CustomField"),
								new Document("label", "Labels").append("value", "Labels")))
				.append("nestedFields", Arrays.asList(new Document("fieldName", "jiraProdIncidentRaisedByCustomField")
						.append("fieldLabel", "Production Incident Custom Field").append("fieldType", "text")
						.append("fieldCategory", "fields").append("filterGroup", Arrays.asList("CustomField"))
						.append("tooltip", new Document("definition",
								"Provide customfield name to identify Production Incident. <br> Example: customfield_13907<hr>")),
						new Document("fieldName", "jiraProdIncidentRaisedByValue")
								.append("fieldLabel", "Production Incident Values").append("fieldType", "chips")
								.append("filterGroup", Arrays.asList("CustomField", "Labels"))
								.append("tooltip", new Document("definition",
										"Provide label name to identify Production Incident Example: PROD_INCIDENT <hr>"))));

		mongoTemplate.insert(field1, "field_mapping_structure");
	}

	public void dodStatusFieldMapping() {
		Document document = new Document("fieldName", "jiraDodKPI166").append("fieldLabel", "DOD Status")
				.append("fieldType", "chips").append("fieldCategory", "workflow")
				.append("section", "WorkFlow Status Mapping").append("tooltip", new Document("definition",
						"Status/es that identify that an issue is completed based on Definition of Done (DoD)."));
		mongoTemplate.insert(document, "field_mapping_structure");
	}

	public void insertKpiColumnConfig() {
		Document document = new Document("basicProjectConfigId", null).append("kpiId", "kpi166").append(
				"kpiColumnDetails",
				Arrays.asList(
						new Document("columnName", "Project Name").append("order", 0).append("isShown", true)
								.append("isDefault", true),
						new Document("columnName", "Date").append("order", 1).append("isShown", true)
								.append("isDefault", true),
						new Document("columnName", "Story ID").append("order", 2).append("isShown", true)
								.append("isDefault", true),
						new Document("columnName", "Issue Type").append("order", 3).append("isShown", true)
								.append("isDefault", true),
						new Document("columnName", "Issue Description").append("order", 4).append("isShown", true)
								.append("isDefault", true),
						new Document("columnName", "Created Date").append("order", 5).append("isShown", true)
								.append("isDefault", true),
						new Document("columnName", "Completion Date").append("order", 6).append("isShown", true)
								.append("isDefault", true),
						new Document("columnName", "Time to Recover (In Hours)").append("order", 7)
								.append("isShown", true).append("isDefault", true)));
		mongoTemplate.insert(document, "kpi_column_configs");
	}

	public void updateMetadataIdentifier() {
		Query query = new Query(Criteria.where("templateCode").in("7"));

		Update update = new Update().push("workflow")
				.each(new Document("type", "jiraDodKPI166").append("value", Arrays.asList("Closed"))).push("issues")
				.each(new Document("type", "jiraStoryIdentificationKPI166").append("value",
						Arrays.asList("Story", "Enabler Story", "Tech Story", "Change request")));

		mongoTemplate.updateMulti(query, update, "metadata_identifier");
	}

	public void addLinkDetailToLeadTimeForChange() {
		Query kpiQuery = new Query(Criteria.where("kpiId").is("kpi156"));

		Update kpiUpdate = new Update()
				.push("kpiInfo.details", new Document("type", "link")
						.append("kpiLinkDetail", new Document("text", "Detailed Information at")
								.append("link", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/59080705/DORA+KPIs#Lead-time-for-changes")));

		mongoTemplate.updateFirst(kpiQuery, kpiUpdate, KpiMaster.class);
	}

	@RollbackExecution
	public void rollback() {
		rollbackMeanTimeToRecover();
		rollbackStoryToIdentifyFieldMapping();
		rollbackProductionIncidentFieldMapping();
		rollbackDodStatusFieldMapping();
		rollbackInsertKpiColumnConfig();
		rollbackUpdateMetadataIdentifier();
		removeLinkDetailFromLeadTimeForChange();
	}

	public void rollbackMeanTimeToRecover() {
		Query query = new Query(Criteria.where("kpiId").is("kpi166"));
		mongoTemplate.remove(query, "kpi_master");
	}

	public void rollbackStoryToIdentifyFieldMapping() {
		Query query = new Query(Criteria.where("fieldName").is("jiraStoryIdentificationKPI166"));
		mongoTemplate.remove(query, "field_mapping_structure");
	}

	public void rollbackProductionIncidentFieldMapping() {
		Query query = new Query(Criteria.where("fieldName").is("jiraProductionIncidentIdentification"));
		mongoTemplate.remove(query, "field_mapping_structure");
	}

	public void rollbackDodStatusFieldMapping() {
		Query query = new Query(Criteria.where("fieldName").is("jiraDodKPI166"));
		mongoTemplate.remove(query, "field_mapping_structure");
	}

	public void rollbackInsertKpiColumnConfig() {
		Query query = new Query(Criteria.where("kpiId").is("kpi166"));
		mongoTemplate.remove(query, "kpi_column_configs");
	}

	public void rollbackUpdateMetadataIdentifier() {
		Query query = new Query(Criteria.where("templateCode").is("7"));
		Update update = new Update().pull("workflow", new Document("type", "jiraDodKPI166").append("value", "Closed"))
				.pull("issues", new Document("type", "jiraStoryIdentificationKPI166").append("value",
						Arrays.asList("Story", "Enabler Story", "Tech Story", "Change request")));

		mongoTemplate.updateMulti(query, update, "metadata_identifier");
	}

	public void removeLinkDetailFromLeadTimeForChange() {
		Query kpiQuery = new Query(Criteria.where("kpiId").is("kpi156"));

		Update kpiUpdate = new Update()
				.pull("kpiInfo.details", new Document("type", "link")
						.append("kpiLinkDetail", new Document("text", "Detailed Information at")
								.append("link", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/59080705/DORA+KPIs#Lead-time-for-changes")));

		mongoTemplate.updateFirst(kpiQuery, kpiUpdate, KpiMaster.class);
	}

}
