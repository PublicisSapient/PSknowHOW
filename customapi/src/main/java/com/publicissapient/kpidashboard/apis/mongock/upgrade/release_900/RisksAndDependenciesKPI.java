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
package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_900;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author purgupta2
 */
@ChangeUnit(id = "risks_and_dependencies", order = "9103", author = "purgupta2", systemVersion = "9.0.1")
public class RisksAndDependenciesKPI {

	public static final String FIELD_MAPPING_STRUCTURE = "field_mapping_structure";
	public static final String FIELD_LABEL = "fieldLabel";
	public static final String FIELD_TYPE = "fieldType";
	public static final String FIELD_NAME = "fieldName";
	public static final String TOOL_TIP = "tooltip";
	public static final String DEFINITION = "definition";
	public static final String LABELS = "Labels";
	private static final String FIELD_CATEGORY = "fieldCategory";
	private static final String RISK_ISSUETYPE = "jiraIssueRiskTypeKPI176";
	private static final String DEPENPENCY_ISSUETYPE = "jiraIssueDependencyTypeKPI176";
	private static final String DELIVERED_STATUS = "jiraIterationCompletionStatusKPI176";
	private static final String KPI_ID = "kpiId";
	private static final String KPI_MASTER = "kpi_master";
	private static final String SECTION = "section";
	private static final Object CHIPS = "chips";
	private static final String MANDATORY = "mandatory";
	private static final String STR = "      \"isShown\": true,\n" + "      \"isDefault\": true\n" + "    }, {\n";
	private static final String ISSUES = "issues";

	private final MongoTemplate mongoTemplate;

	public RisksAndDependenciesKPI(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		addToKpiMaster();
		updateFieldMappingStructure();
		insertKpiColumnConfig();
		updateMetaDataIdentifier();
	}

	public void insertKpiColumnConfig() {
		Document document = Document.parse("{\n" + "    \"kpiId\": \"kpi176\",\n" + "    \"kpiColumnDetails\": [{\n"
				+ "      \"columnName\": \"Issue Id\",\n" + "      \"order\": 0,\n" + STR
				+ "      \"columnName\": \"Issue Type\",\n" + "      \"order\": 1,\n" + STR
				+ "      \"columnName\": \"Issue Description\",\n" + "      \"order\": 2,\n" + STR
				+ "      \"columnName\": \"Issue Status\",\n" + "      \"order\": 3,\n" + STR
				+ "      \"columnName\": \"Priority\",\n" + "      \"order\": 4,\n" + STR
				+ "      \"columnName\": \"Created Date\",\n" + "      \"order\": 5,\n" + STR
				+ "      \"columnName\": \"Assignee\",\n" + "      \"order\": 6,\n" + "      \"isShown\": true,\n"
				+ "      \"isDefault\": false\n" + "    }]\n" + "  }");

		mongoTemplate.insert(document, "kpi_column_configs");
	}

	public void updateMetaDataIdentifier() {
		Document filterQuery = new Document("templateCode", "7");

		Document metaDataIdentifierRisk = new Document("type", RISK_ISSUETYPE).append("value", List.of("Risk"));
		Document metaDataIdentifierDependency = new Document("type", DEPENPENCY_ISSUETYPE).append("value", List.of("Dependency"));

		Document updateOperation = new Document("$push", new Document(ISSUES, metaDataIdentifierRisk));
		Document updateOperation1 = new Document("$push", new Document(ISSUES, metaDataIdentifierDependency));

		MongoCollection<Document> metadataIdentifierCollection = mongoTemplate.getCollection("metadata_identifier");
		metadataIdentifierCollection.updateOne(filterQuery, updateOperation);
		metadataIdentifierCollection.updateOne(filterQuery, updateOperation1);
	}

	public void addToKpiMaster() {

		Document kpiDocument = new Document().append(KPI_ID, "kpi176").append("kpiName", "Risks And Dependencies")
				.append("maxValue", "").append("kpiUnit", "Count").append("isDeleted", "False")
				.append("defaultOrder", 6).append("kpiCategory", "Iteration").append("kpiSource", "Jira")
				.append("groupId", 8).append("thresholdValue", "").append("kanban", false).append("chartType", "")
				.append("yAxisLabel", "").append("xAxisLabel", "").append("isAdditionalfFilterSupport", false)
				.append("kpiFilter", "radioButton").append("calculateMaturity", false)
				.append("kpiInfo", new Document()
						.append(DEFINITION, "It displayed all the risks and dependencies tagged in a sprint")
						.append("details", Collections.singletonList(new Document("type", "link").append(
								"kpiLinkDetail",
								new Document().append("text", "Detailed Information at").append("link",
										"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/102891521/Risks+and+Dependencies")))))
				.append("kpiSubCategory", "Iteration Review");
		// Insert the document into the collection
		mongoTemplate.getCollection(KPI_MASTER).insertOne(kpiDocument);

		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection(KPI_MASTER);

		// Update documents
		updateDocument(kpiMaster, "kpi122", 7);
		updateDocument(kpiMaster, "kpi123", 8);
		updateDocument(kpiMaster, "kpi125", 9);
		updateDocument(kpiMaster, "kpi131", 10);
		updateDocument(kpiMaster, "kpi135", 12);
		updateDocument(kpiMaster, "kpi133", 13);
		updateDocument(kpiMaster, "kpi136", 14);
		updateDocument(kpiMaster, "kpi132", 15);
		updateDocument(kpiMaster, "kpi140", 16);
		updateDocument(kpiMaster, "kpi124", 22);
		updateDocument(kpiMaster, "kpi75", 23);

	}

	private void updateDocument(MongoCollection<Document> kpiMaster, String kpiId, int defaultOrder) {
		// Create the filter
		Document filter = new Document(KPI_ID, kpiId);

		// Create the update
		Document update = new Document("$set", new Document("defaultOrder", defaultOrder));

		// Perform the update
		kpiMaster.updateOne(filter, update);
	}

	public void updateFieldMappingStructure() {
		Document jiraIterationCompletionStatusKPI176 = new Document().append(FIELD_NAME, DELIVERED_STATUS)
				.append(FIELD_LABEL, "Custom Completion status/es").append(FIELD_TYPE, CHIPS)
				.append(FIELD_CATEGORY, "workflow").append(SECTION, "WorkFlow Status Mapping")
				.append(TOOL_TIP, new Document(DEFINITION,
						"All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"))
				.append(MANDATORY, true);

		Document jiraIssueRiskTypeKPI176 = new Document().append(FIELD_NAME, RISK_ISSUETYPE)
				.append(FIELD_LABEL, "Issue type to identify risks").append(FIELD_TYPE, CHIPS)
				.append(FIELD_CATEGORY, "Issue_Type").append(SECTION, "Issue Type Mapping")
				.append(TOOL_TIP, new Document(DEFINITION, "This field is used to identify Risk Issue type."))
				.append(MANDATORY, true);

		Document jiraIssueDependencyTypeKPI176 = new Document().append(FIELD_NAME, DEPENPENCY_ISSUETYPE)
				.append(FIELD_LABEL, "Issue type to identify dependencies").append(FIELD_TYPE, CHIPS)
				.append(FIELD_CATEGORY, "Issue_Type").append(SECTION, "Issue Type Mapping")
				.append(TOOL_TIP, new Document(DEFINITION, "This field is used to identify Dependency Issue type."))
				.append(MANDATORY, true);

		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE).insertMany(Arrays
				.asList(jiraIterationCompletionStatusKPI176, jiraIssueRiskTypeKPI176, jiraIssueDependencyTypeKPI176));
	}

	@RollbackExecution
	public void rollback() {
		deleteKpiMaster();
		deleteFieldMappingRollback();
		deleteMetadataEntries();
	}

	public void deleteKpiMaster() {
		mongoTemplate.getCollection(KPI_MASTER).deleteOne(new Document(KPI_ID, "kpi176"));

		MongoCollection<Document> kpiMaster = mongoTemplate.getCollection(KPI_MASTER);

		updateDocument(kpiMaster, "kpi122", 6);
		updateDocument(kpiMaster, "kpi123", 7);
		updateDocument(kpiMaster, "kpi125", 8);
		updateDocument(kpiMaster, "kpi131", 9);
		updateDocument(kpiMaster, "kpi135", 11);
		updateDocument(kpiMaster, "kpi133", 12);
		updateDocument(kpiMaster, "kpi136", 13);
		updateDocument(kpiMaster, "kpi132", 14);
		updateDocument(kpiMaster, "kpi140", 15);
		updateDocument(kpiMaster, "kpi124", 21);
		updateDocument(kpiMaster, "kpi75", 22);
	}

	private void deleteFieldMappingRollback() {
		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE)
				.deleteMany(Filters.or(Filters.eq(FIELD_NAME, RISK_ISSUETYPE),
						Filters.eq(FIELD_NAME, DEPENPENCY_ISSUETYPE), Filters.eq(FIELD_NAME, DELIVERED_STATUS)));
	}

	public void deleteMetadataEntries() {
		Document filterQuery = new Document("templateCode", "7");

		Document metaDataIdentifierRisk = new Document("type", RISK_ISSUETYPE);
		Document metaDataIdentifierDependency = new Document("type", DEPENPENCY_ISSUETYPE);

		Document updateOperation = new Document("$pull", new Document(ISSUES, metaDataIdentifierRisk));
		Document updateOperation1 = new Document("$pull", new Document(ISSUES, metaDataIdentifierDependency));

		MongoCollection<Document> metadataIdentifierCollection = mongoTemplate.getCollection("metadata_identifier");
		metadataIdentifierCollection.updateOne(filterQuery, updateOperation);
		metadataIdentifierCollection.updateOne(filterQuery, updateOperation1);
	}

}
