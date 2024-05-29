package com.publicissapient.kpidashboard.apis.mongock.rollback.release_920;

import java.util.Collections;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "r_release_plan_kpi", order = "09204", author = "purgupta2", systemVersion = "9.2.0")
public class ReleasePlanKpi {

    public static final String FIELD_MAPPING_STRUCTURE = "field_mapping_structure";
    public static final String FIELD_LABEL = "fieldLabel";
    public static final String FIELD_NAME = "fieldName";
    public static final String DEFINITION = "definition";
    public static final String LABELS = "Labels";
    private static final String KPI_ID = "kpiId";
    private static final String KPI_MASTER = "kpi_master";

    private final MongoTemplate mongoTemplate;

    public ReleasePlanKpi(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void execution() {
        mongoTemplate.getCollection(KPI_MASTER).deleteOne(new Document(KPI_ID, "kpi179"));
    }

    public void addToKpiMaster() {

        Document kpiDocument = new Document().append(KPI_ID, "kpi179").append("kpiName", "Release Plan")
                .append("maxValue", "").append("kpiUnit", "Count").append("isDeleted", "False")
                .append("defaultOrder", 1).append("kpiCategory", "Iteration").append("kpiSource", "Jira")
                .append("combinedKpiSource","Jira/Azure").append("isPositiveTrend",true).append("showTrend",false)
                .append("groupId", 9).append("thresholdValue", "").append("kanban", false).append("chartType", "CumulativeMultilineChart")
                .append("yAxisLabel", "Count").append("xAxisLabel", "").append("isAdditionalfFilterSupport", false)
                .append("kpiFilter", "").append("calculateMaturity", false)
                .append("kpiInfo", new Document()
                        .append(DEFINITION, "Displays the cumulative daily planned dues of the release based on the due dates of work items within the release scope.\n\nAdditionally, it provides an overview of the entire release scope.")
                        .append("details", Collections.singletonList(new Document("type", "link").append(
                                "kpiLinkDetail",
                                new Document().append("text", "Detailed Information at").append("link",
                                        "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/147652609/Release+Release+Plan")))))
                .append("kpiSubCategory", "Speed").append("kpiWidth", "100").append("boxType","chart");
        // Insert the document into the collection
        mongoTemplate.getCollection(KPI_MASTER).insertOne(kpiDocument);
    }

    @RollbackExecution
    public void rollback() {
        addToKpiMaster();
    }
}
