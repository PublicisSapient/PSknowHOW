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

import java.util.Arrays;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import io.mongock.api.annotations.BeforeExecution;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackBeforeExecution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shi6
 */
@ChangeUnit(id = "kpi_sonar_code_quality", order = "8104", author = "shi6", systemVersion = "8.1.0")
public class KpiSonarCodeQuality {
	private static final String KPI_ID= "kpiId";
	private static final String KPI_168= "kpi168";
	private static final String PARAGRAPH= "paragraph";
	private static final String VALUE= "value";

	private final MongoTemplate mongoTemplate;
	private MongoCollection<Document> kpiMaster;
	private MongoCollection<Document> kpiMasterMapping;

	public KpiSonarCodeQuality(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@BeforeExecution
	public void beforeExecute() {
		kpiMaster = mongoTemplate.getCollection("kpi_master");
		kpiMasterMapping = mongoTemplate.getCollection("kpi_category_mapping");
	}

	@Execution
	public void execution() {
		addSonarCodeQualityInKpiMaster();
		updateInKpiCategoryMapping();
	}

	public void addSonarCodeQualityInKpiMaster() {
		// Create the document to insert
		Document document = new Document().append(KPI_ID, KPI_168).append("kpiName", "Sonar Code Quality")
				.append("kpiUnit", "unit").append("maxValue", "90").append("isDeleted", "False")
				.append("defaultOrder", 14).append("kpiSource", "Sonar").append("groupId", 1).append("kanban", false)
				.append("chartType", "bar-with-y-axis-group")
				.append("kpiInfo", new Document().append("definition",
						"Sonar Code Quality is graded based on the static and dynamic code analysis procedure built in Sonarqube that analyses code from multiple perspectives.")
						.append("details", Arrays.asList(
								new Document().append("type", PARAGRAPH).append(VALUE,
										"Code Quality in Sonarqube is shown as Grades (A to E)."),
								new Document().append("type", PARAGRAPH).append(VALUE,
										"A is the highest (best) and,"),
								new Document().append("type", PARAGRAPH).append(VALUE, "E is the least"),
								new Document().append("type", "link").append("kpiLinkDetail",
										new Document().append("text", "Detailed Information at").append("link",
												"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Sonar-Code-Quality")))))
				.append("xAxisLabel", "Months").append("yAxisLabel", "Code Quality").append("isPositiveTrend", true)
				.append("showTrend", true).append("kpiFilter", "dropDown").append("aggregationCriteria", "average")
				.append("isAdditionalFilterSupport", false).append("calculateMaturity", true)
				.append("hideOverallFilter", true).append("maturityRange", Arrays.asList("5", "4", "3", "2", "1"))
				.append("yaxisOrder", new Document().append(String.valueOf(5), "E").append(String.valueOf(4), "D")
						.append(String.valueOf(3), "C").append(String.valueOf(2), "B").append(String.valueOf(1), "A"));

		// Insert the document into the kpiMaster
		mongoTemplate.getCollection("kpi_master").insertOne(document);
	}

	private void updateInKpiCategoryMapping() {
		MongoCollection<Document> kpiCategoryMapping = mongoTemplate.getCollection("kpi_category_mapping");
		// Query to find the kpi_category_mapping for kpiId = 38
		// Execute the query
		MongoCursor<Document> cursor = kpiCategoryMapping.find(new Document(KPI_ID, "kpi38")).iterator();

		// Initialize the categoryId
		String categoryId = null;
		// Iterate over the results (assuming kpiId is unique)
		while (cursor.hasNext()) {
			Document document = cursor.next();
			categoryId = document.getString("categoryId");
		}

		// Check if categoryId is not null and create an insert script
		if (categoryId != null) {
			// Create the insert script document
			kpiCategoryMapping.insertOne(new Document().append(KPI_ID, KPI_168).append("categoryId", categoryId)
					.append("kpiOrder", 15).append("kanban", false));
		}
	}

	@RollbackExecution
	public void rollbackMasterAndCategory() {
		// Delete "Sonar Code Quality Kpi" from kpi_master collection
		kpiMaster.findOneAndDelete(new Document(KPI_ID, KPI_168));

		// Delete "Sonar Code Quality Kpi" from kpi_category_mapping collection
		kpiMasterMapping.findOneAndDelete(new Document(KPI_ID, KPI_168));
	}

	@RollbackBeforeExecution
	public void rollbackBeforeExecution() {
		// do not rquire the implementation
	}
}
