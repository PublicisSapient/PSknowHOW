package com.publicissapient.kpidashboard.apis.util;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.Document;

import java.util.Arrays;

public final class MongockUtils {

	public static final String ROLES_COLLECTION = "roles";
	public static final String KPI_MASTER_COLLECTION = "kpi_master";
	public static final String KPI_COLUMN_CONFIG_COLLECTION = "kpi_column_configs";
	public static final String FIELD_MAPPING_STRUCTURE_COLLECTION = "field_mapping_structure";
	public static final String ACTION_POLICY_RULE_COLLECTION = "action_policy_rule";
	public static final String FIELD_MAPPING = "field_mapping";
	public static final String kpiId = "kpiId";

	public static void checkCollectionExists(MongoTemplate mongoTemplate, String collectionName) {
		if (!mongoTemplate.collectionExists(collectionName))
			mongoTemplate.createCollection(collectionName);
	}

	public static Document createKpiColumnConfig(String kpiId) {
		return new Document("basicProjectConfigId", null).append("kpiId", kpiId).append("kpiColumnDetails",
				Arrays.asList(createColumnDetail("Issue ID", 0), createColumnDetail("Issue Description", 1),
						createColumnDetail("Issue Type", 2), createColumnDetail("Issue Status", 3),
						createColumnDetail("Priority", 4), createColumnDetail("Created Date", 5),
						createColumnDetail("Updated Date", 6), createColumnDetail("Assignee", 7)));
	}

	private static Document createColumnDetail(String columnName, int order) {
		return new Document("columnName", columnName).append("order", order).append("isShown", true).append("isDefault",
				true);
	}

	public static Document createKpiDocument(String kpiId, String kpiName, String kpiUnit, int defaultOrder,
			String kpiCategory, String kpiSource, int groupId, String definition, boolean isPositiveTrend,
			boolean showTrend, String kpiFilter, String boxType, boolean calculateMaturity) {
		Document kpiDocument = new Document();
		kpiDocument.append("kpiId", kpiId).append("kpiName", kpiName).append("kpiUnit", kpiUnit)
				.append("isDeleted", "False").append("defaultOrder", defaultOrder).append("kpiCategory", kpiCategory)
				.append("kpiSource", kpiSource).append("groupId", groupId).append("thresholdValue", "")
				.append("kanban", false).append("chartType", "pieChart")
				.append("kpiInfo", new Document("definition", definition)).append("xAxisLabel", "")
				.append("yAxisLabel", "").append("isPositiveTrend", isPositiveTrend).append("showTrend", showTrend)
				.append("isAdditionalFilterSupport", false).append("kpiFilter", kpiFilter).append("boxType", boxType)
				.append("calculateMaturity", calculateMaturity);

		return kpiDocument;
	}

	boolean isRelae7.3Done()
	{

	}
}
