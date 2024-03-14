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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_900;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;
import com.publicissapient.kpidashboard.apis.util.MongockUtil;

import io.mongock.api.annotations.BeforeExecution;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackBeforeExecution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shunaray
 */
@ChangeUnit(id = "tEngine_code_security", order = "9009", author = "shunaray", systemVersion = "9.0.0")
public class TEngineCodeSecurity {

	public static final String TARGET_KPI_VALUE = "Target KPI Value";
	public static final String NUMBER = "number";
	public static final String CUSTOM_FIELDS_MAPPING = "Custom Fields Mapping";
	public static final String TOOL_TIP_DEF = "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown";
	public static final String COLUMN_NAME = "columnName";
	public static final String ORDER = "order";
	public static final String IS_SHOWN = "isShown";
	public static final String IS_DEFAULT = "isDefault";
	public static final String KPI_ID = "kpiId";
	public static final String KPI_174 = "kpi174";
	private final MongoTemplate mongoTemplate;
	private MongoCollection<Document> fieldMappingStructure;
	private MongoCollection<Document> kpiMaster;
	private MongoCollection<Document> kpiCategoryMapping;
	private MongoCollection<Document> kpiColumnConfig;

	@BeforeExecution
	public void beforeExecution() {
		fieldMappingStructure = mongoTemplate.getCollection("field_mapping_structure");
		kpiMaster = mongoTemplate.getCollection("kpi_master");
		kpiCategoryMapping = mongoTemplate.getCollection("kpi_category_mapping");
		kpiColumnConfig = mongoTemplate.getCollection("kpi_column_configs");
	}

	public TEngineCodeSecurity(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		insertFieldMapping();
		addTechDebtSonarSecurityKpi();
		addKpiToQualityBoard();
		addToKpiColumnConfig();
		changeKpiName("kpi27", "Tech Debt - Sonar Maintainability");
	}

	public void insertFieldMapping() {
		fieldMappingStructure.insertMany(Arrays.asList(
				MongockUtil.createFieldMapping("thresholdValueKPI174", TARGET_KPI_VALUE, CUSTOM_FIELDS_MAPPING, null,
						NUMBER, TOOL_TIP_DEF),
				MongockUtil.createFieldMapping("costPerLineKPI174", "Time (min) required to write 1 Line of code (LOC)",
						"Issue Types Mapping", null, NUMBER,
						"Effort invested in writing number of LOC (Default 30 min taken for each LOC)")));

	}

	public void addTechDebtSonarSecurityKpi() {
		Document kpiDocument = new Document().append(KPI_ID, KPI_174).append("kpiName", "Tech Debt - Sonar Security")
				.append("maxValue", "100").append("kpiUnit", "%").append("isDeleted", "False")
				.append("defaultOrder", 15.0).append("kpiSource", "Sonar").append("groupId", 1.0)
				.append("thresholdValue", "50").append("kanban", false).append("chartType", "line")
				.append("kpiInfo", new Document().append("definition",
						"Measures the evolution of effort required to fix all Vulnerabilities detected with Sonar in the code.")
						.append("formula",
								Arrays.asList(new Document().append("lhs", "Remediation Effort Change")
										.append("operator", "division").append("operands",
												Arrays.asList("Sec%|TimeIntervalEnd", "Sec%|TimeIntervalStart")),
										new Document().append("lhs", "Sec%").append("operator", "division")
												.append("operands", Arrays.asList("Σsecurity_remediation_effort",
														"Σncloc × (Average minutes per line of code, default 30)"))))
						.append("details", Collections.singletonList(new Document().append("type", "link").append(
								"kpiLinkDetail",
								new Document().append("text", "Detailed Information at").append("link",
										"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/111411201/Tech+Debt+-+Sonar+Security")))))
				.append("xAxisLabel", "Weeks").append("yAxisLabel", "Percentage")
				.append("isPositiveTrend", false).append("showTrend", true).append("kpiFilter", "dropDown")
				.append("aggregationCriteria", "average").append("isAdditionalFilterSupport", false)
				.append("calculateMaturity", true).append("hideOverallFilter", false)
				.append("maturityRange", Arrays.asList("-120", "120-100", "100-50", "50-0", "0-"))
				.append("lowerThresholdBG", "white").append("upperThresholdBG", "red");

		kpiMaster.insertOne(kpiDocument);
	}

	public void addKpiToQualityBoard() {
		Document kpiCategoryMappingDocument = new Document().append(KPI_ID, KPI_174).append("categoryId", "quality")
				.append("kpiOrder", 17).append("kanban", false);
		kpiCategoryMapping.insertOne(kpiCategoryMappingDocument);
	}

	public void changeKpiName(String kpiIdForNameChange, String newKpiName) {
		kpiMaster.updateOne(new Document(KPI_ID, kpiIdForNameChange),
				new Document("$set", new Document("kpiName", newKpiName)));

	}

	public void addToKpiColumnConfig() {

		Document kpiColumnConfigsDocument = new Document().append("basicProjectConfigId", null).append(KPI_ID, KPI_174)
				.append("kpiColumnDetails",
						Arrays.asList(
								new Document(COLUMN_NAME, "Project").append(ORDER, 0).append(IS_SHOWN, true)
										.append(IS_DEFAULT, false),
								new Document(COLUMN_NAME, "Job Name").append(ORDER, 1).append(IS_SHOWN, true)
										.append(IS_DEFAULT, false),
								new Document(COLUMN_NAME, "Remediation Effort").append(ORDER, 2)
										.append(IS_SHOWN, true).append(IS_DEFAULT, false),
								new Document(COLUMN_NAME, "Weeks").append(ORDER, 3).append(IS_SHOWN, true)
										.append(IS_DEFAULT, false)));

		mongoTemplate.getCollection("kpi_column_configs").insertOne(kpiColumnConfigsDocument);
	}

	@RollbackExecution
	public void rollback() {
		rollBackFieldMapping();
		rollBackKpiMaster();
		deleteKpiCategoryMapping();
		deleteKPIColumnConfig();
		changeKpiName("kpi27", "Sonar Tech Debt");
	}

	public void rollBackFieldMapping() {
		List<String> fieldNamesToDelete = Arrays.asList("costPerLineKPI174", "thresholdValueKPI174");
		Document filter = new Document("fieldName", new Document("$in", fieldNamesToDelete));

		// Delete documents that match the filter
		fieldMappingStructure.deleteMany(filter);
	}

	public void rollBackKpiMaster() {
		List<String> kpiIdsToDelete = List.of(KPI_174);
		Document filter = new Document(KPI_ID, new Document("$in", kpiIdsToDelete));

		// Delete documents that match the filter
		kpiMaster.deleteMany(filter);
	}

	public void deleteKpiCategoryMapping() {
		kpiCategoryMapping.deleteOne(new Document(KPI_ID, KPI_174));
	}

	public void deleteKPIColumnConfig() {
		kpiColumnConfig.deleteOne(new Document(KPI_ID, KPI_174));
	}

	@RollbackBeforeExecution
	public void rollbackBeforeExecution() {
		// do not require the implementation
	}
}
