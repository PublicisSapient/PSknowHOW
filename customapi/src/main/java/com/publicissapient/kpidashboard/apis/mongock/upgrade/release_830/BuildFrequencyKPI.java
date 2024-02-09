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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_830;

import java.util.Arrays;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * add build frequency kpi and field mapping
 * 
 * @author aksshriv1
 */
@ChangeUnit(id = "build_freq", order = "8331", author = "aksshriv1", systemVersion = "8.3.3")
public class BuildFrequencyKPI {
	public static final String KPI_ID = "kpiId";
	public static final String KPI_172 = "kpi172";
	public static final String COLUMN_NAME = "columnName";
	public static final String ORDER = "order";
	public static final String IS_SHOWN = "isShown";
	public static final String IS_DEFAULT = "isDefault";
	private final MongoTemplate mongoTemplate;
	private static final String DEFINITION = "definition";
	private static final String FIELD_NAME = "fieldName";

	public BuildFrequencyKPI(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		addToKpiMaster();
		addFieldToFieldMappingStructure();
		addToKpiColumnConfig();
		addToKpiCategoryMapping();
	}

	public void addToKpiMaster() {

		// Insert document into 'kpi_master' collection
		Document kpiDocument = new Document().append(KPI_ID, KPI_172).append("kpiName", "Build Frequency")
				.append("maxValue", "").append("kpiUnit", "").append("isDeleted", "False").append("defaultOrder", 24)
				.append("kpiSource", "Jenkins").append("groupId", 1).append("thresholdValue", "8")
				.append("kanban", false).append("chartType", "line")
				.append("kpiInfo", new Document(DEFINITION,
						"Build frequency refers the number of successful builds done in a specific time frame.")
						.append("details", Arrays.asList(new Document("type", "link").append("kpiLinkDetail",
								new Document("text", "Detailed Information at").append("link",
										"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/92930049/Build+Frequency")))))
				.append("upperThresholdBG", "white").append("lowerThresholdBG", "red").append("xAxisLabel", "Weeks")
				.append("yAxisLabel", "Builds Count").append("isPositiveTrend", false).append("showTrend", true)
				.append("kpiFilter", "dropDown").append("aggregationCriteria", "sum")
				.append("isAdditionalFilterSupport", false).append("calculateMaturity", true)
				.append("hideOverallFilter", true)
				.append("maturityRange", Arrays.asList("-1", "2-4", "5-8", "8-10", "10-"));
		// Insert the document into the collection
		mongoTemplate.getCollection("kpi_master").insertOne(kpiDocument);

	}

	public void addFieldToFieldMappingStructure() {

		Document thresholdValueMapping = new Document(FIELD_NAME, "thresholdValueKPI172")
				.append("fieldLabel", "Target KPI Value").append("fieldType", "number")
				.append("section", "Custom Fields Mapping").append("tooltip",
						new Document(DEFINITION, "Target KPI value denotes the bare "
								+ "minimum a project should maintain for a KPI. User should just input the number and"
								+ " the unit like percentage, hours will automatically be considered."
								+ " If the threshold is empty, then a common target KPI line will be shown"));

		mongoTemplate.getCollection("field_mapping_structure").insertOne(thresholdValueMapping);

	}

	public void addToKpiColumnConfig() {

		Document kpiColumnConfigsDocument = new Document().append("basicProjectConfigId", null).append(KPI_ID, KPI_172)
				.append("kpiColumnDetails",
						Arrays.asList(
								new Document(COLUMN_NAME, "Project Name").append(ORDER, 0).append(IS_SHOWN, true)
										.append(IS_DEFAULT, false),
								new Document(COLUMN_NAME, "Job Name").append(ORDER, 1).append(IS_SHOWN, true)
										.append(IS_DEFAULT, false),
								new Document(COLUMN_NAME, "Weeks").append(ORDER, 2).append(IS_SHOWN, true)
										.append(IS_DEFAULT, false),
								new Document(COLUMN_NAME, "Start Date").append(ORDER, 3).append(IS_SHOWN, true)
										.append(IS_DEFAULT, false),
								new Document(COLUMN_NAME, "Build Url").append(ORDER, 4).append(IS_SHOWN, true)
										.append(IS_DEFAULT, false)));

		mongoTemplate.getCollection("kpi_column_configs").insertOne(kpiColumnConfigsDocument);
	}

	public void addToKpiCategoryMapping() {
		Document kpiCategoryMappingDocument = new Document().append(KPI_ID, KPI_172).append("categoryId", "speed")
				.append("kpiOrder", 10).append("kanban", false);
		mongoTemplate.getCollection("kpi_category_mapping").insertOne(kpiCategoryMappingDocument);
	}

	@RollbackExecution
	public void rollback() {
		deleteKpiMaster();
		deleteFieldMappingStructure();
		deleteKPIColumnConfig();
		deleteKpiCategoryMapping();
	}

	public void deleteKpiCategoryMapping() {
		mongoTemplate.getCollection("kpi_category_mapping").deleteOne(new Document(KPI_ID, KPI_172));
	}

	public void deleteKPIColumnConfig() {
		mongoTemplate.getCollection("kpi_column_configs").deleteOne(new Document(KPI_ID, KPI_172));
	}

	public void deleteKpiMaster() {
		mongoTemplate.getCollection("kpi_master").deleteOne(new Document(KPI_ID, KPI_172));
	}

	public void deleteFieldMappingStructure() {
		MongoCollection<Document> fieldMappingStructure = mongoTemplate.getCollection("field_mapping_structure");
		fieldMappingStructure
				.deleteMany(new Document(FIELD_NAME, new Document("$in", Arrays.asList("thresholdValueKPI172"))));
	}
}
