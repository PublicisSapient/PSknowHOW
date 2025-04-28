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

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1100;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChangeUnit(id = "r_late_refinement", order = "011001", author = "shi6", systemVersion = "11.0.0")
public class LateRefinementRollback {
	private static final String KPI_MASTER = "kpi_master";
	private static final String KPI_EXCEL_COLUMN_CONFIG = "kpi_column_configs";
	private static final String LATE_REFINEMENT_LINK = "https://knowhow.suite.publicissapient.com/wiki/spaces/PS/pages/159154188/Late+Refinement";
	private static final String DETAILED_INFO = "Detailed Information at";
	private static final String ISSUE_TYPES_TOOLTIP = "All issue types used by your Jira project";
	private static final String ISSUE_TYPES_LABEL = "Issue types to be included";
	private static final String CUSTOM_FIELD = "CustomField";
	private static final String CHIPS_TYPE = "chips";
	private static final String ISSUE_TYPE_CATEGORY = "Issue_Type";
	private static final String WORKFLOW_CATEGORY = "workflow";
	private static final String FIELDS_CATEGORY = "fields";
	private static final String ISSUE_TYPES_SECTION = "Issue Types Mapping";
	private static final String WORKFLOW_STATUS_SECTION = "WorkFlow Status Mapping";
	private static final String CUSTOM_FIELDS_SECTION = "Custom Fields Mapping";
	private static final String DOR_STATUS_TOOLTIP = "Workflow statuses that does not belong to DOR, DOD and Live which indicate a work item has not yet been picked up in the sprint based on the Definition of Ready (DoR)";
	private static final String REFINED_WORKITEM_TOOLTIP = "Provide field name to identify a refined workitem. Example: customfield_13999<hr>";
	private static final String MATCH_LENGTH_TOOLTIP = "Provide number to match the character length";
	private static final String MATCH_VALUE_TOOLTIP = "Provide characters to match the value";
	private static final String KPI188 = "kpi188";

	// Document keys
	private static final String KEY_FIELD_NAME = "fieldName";
	private static final String KEY_FIELD_LABEL = "fieldLabel";
	private static final String KEY_PLACE_HOLDER = "placeHolderText";
	private static final String KEY_FIELD_DISPLAY_ORDER = "fieldDisplayOrder";
	private static final String KEY_FIELD_TYPE = "fieldType";
	private static final String KEY_FIELD_CATEGORY = "fieldCategory";
	private static final String KEY_SECTION = "section";
	private static final String KEY_SECTION_ORDER = "sectionOrder";
	private static final String KEY_TOOLTIP = "tooltip";
	private static final String KEY_DEFINITION = "definition";
	private static final String KEY_MANDATORY = "mandatory";
	private static final String KEY_OPTIONS = "options";
	private static final String KEY_LABEL = "label";
	private static final String KEY_VALUE = "value";
	private static final String KEY_NESTED_FIELDS = "nestedFields";
	private static final String KEY_FILTER_GROUP = "filterGroup";
	private static final String KEY_PROCESSOR_COMMON = "processorCommon";
	private static final String KEY_KPI_ID = "kpiId";
	private static final String KEY_COLUMN_NAME = "columnName";
	private static final String KEY_ORDER = "order";
	private static final String KEY_IS_SHOWN = "isShown";
	private static final String KEY_IS_DEFAULT = "isDefault";
	private static final String KEY_KPI_NAME = "kpiName";
	private static final String KEY_KPI_UNIT = "kpiUnit";
	private static final String KEY_IS_DELETED = "isDeleted";
	private static final String KEY_DEFAULT_ORDER = "defaultOrder";
	private static final String KEY_KPI_CATEGORY = "kpiCategory";
	private static final String KEY_KPI_SUB_CATEGORY = "kpiSubCategory";
	private static final String KEY_KPI_SOURCE = "kpiSource";
	private static final String KEY_COMBINED_SOURCE = "combinedKpiSource";
	private static final String KEY_GROUP_ID = "groupId";
	private static final String KEY_THRESHOLD_VALUE = "thresholdValue";
	private static final String KEY_KANBAN = "kanban";
	private static final String KEY_CHART_TYPE = "chartType";
	private static final String KEY_KPI_INFO = "kpiInfo";
	private static final String KEY_DETAILS = "details";
	private static final String KEY_TYPE = "type";
	private static final String KEY_KPI_LINK_DETAIL = "kpiLinkDetail";
	private static final String KEY_LINK = "link";
	private static final String KEY_YAXIS_LABEL = "yAxisLabel";
	private static final String KEY_IS_POSITIVE_TREND = "isPositiveTrend";
	private static final String KEY_SHOW_TREND = "showTrend";
	private static final String KEY_IS_ADDITIONAL_FILTER = "isAdditionalFilterSupport";
	private static final String KEY_KPI_WIDTH = "kpiWidth";
	private static final String KEY_BOX_TYPE = "boxType";
	private static final String KEY_CALCULATE_MATURITY = "calculateMaturity";
	private static final String KEY_MAX_VALUE = "maxValue";
	private static final String KEY_KPI_FILTER = "kpiFilter";
	private static final String KEY_BASIC_PROJECT_CONFIG_ID = "basicProjectConfigId";
	private static final String KEY_KPI_COLUMN_DETAILS = "kpiColumnDetails";

	private final MongoTemplate mongoTemplate;

	public LateRefinementRollback(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void changeSet() {

		// Remove KPI 187 and 188 field mappings
		mongoTemplate.getCollection("field_mapping_structure")
				.deleteMany(new Document("$or",
						Arrays.asList(new Document(KEY_FIELD_NAME, "jiraIssueTypeNamesKPI187"),
								new Document(KEY_FIELD_NAME, "jiraStatusKPI187"),
								new Document(KEY_FIELD_NAME, "jiraRefinementCriteriaKPI188"),
								new Document(KEY_FIELD_NAME, "jiraIssueTypeNamesKPI188"))));

		// Remove KPI 187 and 188 from master
		mongoTemplate.getCollection(KPI_MASTER).deleteMany(new Document("$or",
				Arrays.asList(new Document(KEY_KPI_ID, "kpi187"), new Document(KEY_KPI_ID, KPI188))));

		// Remove KPI 188 excel config
		mongoTemplate.getCollection(KPI_EXCEL_COLUMN_CONFIG).deleteOne(new Document(KEY_KPI_ID, KPI188));

	}

	public void addKpi187And188FieldMappings(MongoTemplate mongoTemplate) {
		// KPI 187 field
		Document jiraIssueTypesKPI187 = new Document().append(KEY_FIELD_NAME, "jiraIssueTypeNamesKPI187")
				.append(KEY_FIELD_LABEL, ISSUE_TYPES_LABEL).append(KEY_PLACE_HOLDER, ISSUE_TYPES_LABEL)
				.append(KEY_FIELD_DISPLAY_ORDER, 1).append(KEY_FIELD_TYPE, CHIPS_TYPE)
				.append(KEY_FIELD_CATEGORY, ISSUE_TYPE_CATEGORY).append(KEY_SECTION, ISSUE_TYPES_SECTION)
				.append(KEY_SECTION_ORDER, 1).append(KEY_TOOLTIP, new Document(KEY_DEFINITION, ISSUE_TYPES_TOOLTIP))
				.append(KEY_MANDATORY, true);

		Document jiraStatusKPI187 = new Document().append(KEY_FIELD_NAME, "jiraStatusKPI187")
				.append(KEY_FIELD_LABEL, "Status/es before DoR").append(KEY_FIELD_DISPLAY_ORDER, 1)
				.append(KEY_FIELD_TYPE, CHIPS_TYPE).append(KEY_FIELD_CATEGORY, WORKFLOW_CATEGORY)
				.append(KEY_SECTION, WORKFLOW_STATUS_SECTION).append(KEY_SECTION_ORDER, 2)
				.append(KEY_TOOLTIP, new Document(KEY_DEFINITION, DOR_STATUS_TOOLTIP)).append(KEY_MANDATORY, true);

		// KPI 188 fields
		Document jiraRefinementCriteriaKPI188 = new Document().append(KEY_FIELD_NAME, "jiraRefinementCriteriaKPI188")
				.append(KEY_FIELD_LABEL, "Refinement criteria").append(KEY_FIELD_TYPE, "radiobutton")
				.append(KEY_SECTION, CUSTOM_FIELDS_SECTION).append(KEY_FIELD_DISPLAY_ORDER, 1)
				.append(KEY_SECTION_ORDER, 3)
				.append(KEY_MANDATORY, true)
				.append(KEY_TOOLTIP,
						new Document(KEY_DEFINITION, "Custom field to consider for a refined workitem."))
				.append(KEY_OPTIONS, Arrays.asList(new Document(KEY_LABEL,
						CUSTOM_FIELD).append(KEY_VALUE,
								CUSTOM_FIELD)))
				.append(KEY_NESTED_FIELDS,
						Arrays.asList(new Document().append(KEY_FIELD_NAME, "jiraRefinementByCustomFieldKPI188")
								.append(KEY_FIELD_LABEL, "Custom field to identify refined workitem")
								.append(KEY_PLACE_HOLDER, "Custom field to identify refined workitem")
								.append(KEY_FIELD_TYPE, "text").append(KEY_FIELD_CATEGORY, FIELDS_CATEGORY)
								.append(KEY_FILTER_GROUP, Arrays.asList(CUSTOM_FIELD))
								.append(KEY_TOOLTIP, new Document(KEY_DEFINITION, REFINED_WORKITEM_TOOLTIP)),
								new Document().append(KEY_FIELD_NAME, "jiraRefinementMinLengthKPI188")
										.append(KEY_FIELD_LABEL, "Custom field value min length")
										.append(KEY_PLACE_HOLDER, "Custom field value min length")
										.append(KEY_FIELD_TYPE, "number")
										.append(KEY_MANDATORY, true)
										.append(KEY_FILTER_GROUP, Arrays.asList(CUSTOM_FIELD))
										.append(KEY_TOOLTIP, new Document(KEY_DEFINITION, MATCH_LENGTH_TOOLTIP)),
								new Document().append(KEY_FIELD_NAME, "jiraRefinementKeywordsKPI188")
										.append(KEY_FIELD_LABEL, "Keywords to match")
										.append(KEY_PLACE_HOLDER, "Keywords to match")
										.append(KEY_FIELD_TYPE, CHIPS_TYPE)
										.append(KEY_FILTER_GROUP, Arrays.asList(CUSTOM_FIELD))
										.append(KEY_MANDATORY, true)
										.append(KEY_TOOLTIP, new Document(KEY_DEFINITION, MATCH_VALUE_TOOLTIP))))
				.append(KEY_PROCESSOR_COMMON, true);

		Document jiraIssueTypesKPI188 = new Document().append(KEY_FIELD_NAME, "jiraIssueTypeNamesKPI188")
				.append(KEY_FIELD_LABEL, ISSUE_TYPES_LABEL).append(KEY_PLACE_HOLDER, ISSUE_TYPES_LABEL)
				.append(KEY_FIELD_DISPLAY_ORDER, 1).append(KEY_FIELD_TYPE, CHIPS_TYPE)
				.append(KEY_FIELD_CATEGORY, ISSUE_TYPE_CATEGORY).append(KEY_SECTION, ISSUE_TYPES_SECTION)
				.append(KEY_SECTION_ORDER, 1).append(KEY_TOOLTIP, new Document(KEY_DEFINITION, ISSUE_TYPES_TOOLTIP))
				.append(KEY_MANDATORY, true);

		List<Document> fieldMap = Arrays.asList(jiraIssueTypesKPI187, jiraStatusKPI187, jiraRefinementCriteriaKPI188,
				jiraIssueTypesKPI188);

		mongoTemplate.getCollection("field_mapping_structure").insertMany(fieldMap);

	}

	public void addKpi187And188ToMaster(MongoTemplate mongoTemplate) {
		Document kpi187 = new Document().append(KEY_KPI_ID, "kpi187")
				.append(KEY_KPI_NAME, "Late Refinement (Current Sprint)").append(KEY_KPI_UNIT, "Count")
				.append(KEY_IS_DELETED, "False").append(KEY_DEFAULT_ORDER, 10).append(KEY_KPI_CATEGORY, "Iteration")
				.append(KEY_KPI_SUB_CATEGORY, "Iteration Progress").append(KEY_KPI_SOURCE, "Jira")
				.append(KEY_COMBINED_SOURCE, "Jira/Azure").append(KEY_GROUP_ID, 20).append(KEY_THRESHOLD_VALUE, "")
				.append(KEY_KANBAN, false).append(KEY_CHART_TYPE, "CumulativeMultilineChart")
				.append(KEY_KPI_INFO, new Document().append(KEY_DEFINITION,
						"Measures the number of work items that are not adequately refined in time for development planning. This includes: Work items in the current sprint that do not meet DoR (Definition of Ready).")
						.append(KEY_DETAILS,
								Arrays.asList(new Document().append(KEY_TYPE, "link").append(KEY_KPI_LINK_DETAIL,
										new Document().append(KEY_LABEL, DETAILED_INFO).append(KEY_LINK,
												LATE_REFINEMENT_LINK)))))
				.append(KEY_YAXIS_LABEL, "Percentage").append(KEY_IS_POSITIVE_TREND, true)
				.append(KEY_SHOW_TREND, false).append(KEY_IS_ADDITIONAL_FILTER, false).append(KEY_KPI_WIDTH, 100)
				.append(KEY_BOX_TYPE, "chart").append(KEY_CALCULATE_MATURITY, false);

		Document kpi188 = new Document().append(KEY_KPI_ID, KPI188)
				.append(KEY_KPI_NAME, "Late Refinement (Next Sprint)").append(KEY_MAX_VALUE, "")
				.append(KEY_IS_DELETED, "False").append(KEY_DEFAULT_ORDER, 12).append(KEY_KPI_CATEGORY, "Iteration")
				.append(KEY_KPI_SUB_CATEGORY, "Iteration Review").append(KEY_KPI_SOURCE, "Jira")
				.append(KEY_COMBINED_SOURCE, "Jira/Azure").append(KEY_GROUP_ID, 19).append(KEY_THRESHOLD_VALUE, "")
				.append(KEY_KANBAN, false).append(KEY_CHART_TYPE, null)
				.append(KEY_KPI_INFO, new Document().append(KEY_DEFINITION,
						"Measures the number of work items that are slated for the next sprint but still lack necessary refinement details. Work items tagged to the next sprint that lack sufficient refinement (e.g., missing description, acceptance criteria, etc).")
						.append(KEY_DETAILS,
								Arrays.asList(new Document().append(KEY_TYPE, "link").append(KEY_KPI_LINK_DETAIL,
										new Document().append(KEY_LABEL, DETAILED_INFO).append(KEY_LINK,
												LATE_REFINEMENT_LINK)))))
				.append(KEY_SHOW_TREND, false).append(KEY_IS_ADDITIONAL_FILTER, false)
				.append(KEY_KPI_FILTER, "multiSelectDropDown").append(KEY_BOX_TYPE, "3_column")
				.append(KEY_CALCULATE_MATURITY, false);

		mongoTemplate.insert(kpi187, KPI_MASTER);
		mongoTemplate.insert(kpi188, KPI_MASTER);
		log.info(
				"Added KPI 187 (Late Refinement - Current Sprint) and KPI 188 (Late Refinement - Future Sprint) to kpi_master");
	}

	public void addKpi188ExcelConfig(MongoTemplate mongoTemplate) {
		Document kpi188ExcelConfig = new Document().append(KEY_BASIC_PROJECT_CONFIG_ID, null).append(KEY_KPI_ID, KPI188)
				.append(KEY_KPI_COLUMN_DETAILS,
						Arrays.asList(
								new Document().append(KEY_COLUMN_NAME, "Issue Id").append(KEY_ORDER, 0)
										.append(KEY_IS_SHOWN, true).append(KEY_IS_DEFAULT, true),
								new Document().append(KEY_COLUMN_NAME, "Issue Description").append(KEY_ORDER, 1)
										.append(KEY_IS_SHOWN, true).append(KEY_IS_DEFAULT, true),
								new Document().append(KEY_COLUMN_NAME, "Sprint Name").append(KEY_ORDER, 2)
										.append(KEY_IS_SHOWN, true).append(KEY_IS_DEFAULT, false),
								new Document().append(KEY_COLUMN_NAME, "Issue Status").append(KEY_ORDER, 3)
										.append(KEY_IS_SHOWN, true).append(KEY_IS_DEFAULT, false),
								new Document().append(KEY_COLUMN_NAME, "Issue Type").append(KEY_ORDER, 4)
										.append(KEY_IS_SHOWN, true).append(KEY_IS_DEFAULT, false),
								new Document().append(KEY_COLUMN_NAME, "Un-Refined").append(KEY_ORDER, 5)
										.append(KEY_IS_SHOWN, true).append(KEY_IS_DEFAULT, false)));

		mongoTemplate.insert(kpi188ExcelConfig, KPI_EXCEL_COLUMN_CONFIG);
		log.info("Added excel column configuration for KPI 188 (Late Refinement - Future Sprint)");
	}

	@RollbackExecution
	public void rollback() {
		// Remove KPI 187 and 188 field mappings
		addKpi187And188FieldMappings(mongoTemplate);
		addKpi187And188ToMaster(mongoTemplate);
		addKpi188ExcelConfig(mongoTemplate);

		log.info("Rolled back KPI 187 and 188 configurations");
	}

}