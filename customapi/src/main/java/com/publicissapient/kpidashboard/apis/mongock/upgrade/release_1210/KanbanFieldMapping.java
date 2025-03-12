/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1210;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.MergeOptions;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shunaray
 */
@ChangeUnit(id = "kanban_field_mapping", order = "12106", author = "shunaray", systemVersion = "12.1.0")
public class KanbanFieldMapping {

	public static final String FIELD_MAPPING = "field_mapping";
	public static final String FIELD_MAPPING_STRUCTURE = "field_mapping_structure";
	public static final String FIELD_NAME = "fieldName";
	public static final String KPI_MASTER = "kpi_master";
	public static final String JIRA_LIVE_STATUS_LTK = "jiraLiveStatusLTK";
	public static final String JIRA_LIVE_STATUS_NOPK = "jiraLiveStatusNOPK";
	public static final String JIRA_LIVE_STATUS_NOSK = "jiraLiveStatusNOSK";
	public static final String JIRA_LIVE_STATUS_NORK = "jiraLiveStatusNORK";
	public static final String JIRA_LIVE_STATUS_OTA = "jiraLiveStatusOTA";
	public static final String TICKET_COUNT_ISSUE_TYPE = "ticketCountIssueType";
	public static final String KANBAN_RCA_COUNT_ISSUE_TYPE = "kanbanRCACountIssueType";
	public static final String JIRA_TICKET_VELOCITY_ISSUE_TYPE = "jiraTicketVelocityIssueType";
	public static final String TICKET_DELIVERD_STATUS = "ticketDeliverdStatus";
	public static final String JIRA_TICKET_CLOSED_STATUS = "jiraTicketClosedStatus";
	public static final String KANBAN_CYCLE_TIME_ISSUE_TYPE = "kanbanCycleTimeIssueType";
	public static final String JIRA_TICKET_TRIAGED_STATUS = "jiraTicketTriagedStatus";
	public static final String JIRA_TICKET_REJECTED_STATUS = "jiraTicketRejectedStatus";
	public static final String STATUS_TO_IDENTIFY_LIVE_TICKETS = "Status to identify Live tickets";
	public static final String WORKFLOW_STATUS_USED_TO_IDENTIFY_TICKETS_IN_LIVE_STATE = "Workflow status used to identify tickets in Live state";
	public static final String WORK_FLOW_STATUS_MAPPING = "WorkFlow Status Mapping";
	public static final String ISSUE_TYPES_TO_CONSIDER = "Issue types to consider";
	public static final String ISSUE_TYPES_MAPPING = "Issue Types Mapping";
	public static final String PROVIDE_ANY_STATUS_FROM_WORKFLOW_ON_WHICH_LIVE_IS_CONSIDERED = "Provide any status from workflow on which Live is considered.";
	public static final String ALL_ISSUE_TYPE_TO_TRACK_TICKETS = "All issue types used to track tickets";

	private final MongoTemplate mongoTemplate;

	public KanbanFieldMapping(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	private static Map<String, List<String>> getFieldToDuplicateMap() {
		// Map of original field to list of duplicate fields
		Map<String, List<String>> fieldMappings = new HashMap<>();

		fieldMappings.put(JIRA_LIVE_STATUS_LTK, List.of("jiraLiveStatusKPI53"));
		fieldMappings.put(JIRA_LIVE_STATUS_NOPK, List.of("jiraLiveStatusKPI50"));
		fieldMappings.put(JIRA_LIVE_STATUS_NOSK, List.of("jiraLiveStatusKPI48"));
		fieldMappings.put(JIRA_LIVE_STATUS_NORK, List.of("jiraLiveStatusKPI51"));
		fieldMappings.put(JIRA_LIVE_STATUS_OTA, List.of("jiraLiveStatusKPI997"));

		fieldMappings.put(TICKET_COUNT_ISSUE_TYPE, List.of("ticketCountIssueTypeKPI48", "ticketCountIssueTypeKPI50",
				"ticketCountIssueTypeKPI54", "ticketCountIssueTypeKPI55", "ticketCountIssueTypeKPI997"));

		fieldMappings.put(KANBAN_RCA_COUNT_ISSUE_TYPE, List.of("kanbanRCACountIssueTypeKPI51"));

		fieldMappings.put(JIRA_TICKET_VELOCITY_ISSUE_TYPE, List.of("jiraTicketVelocityIssueTypeKPI49"));

		fieldMappings.put(TICKET_DELIVERD_STATUS, List.of("ticketDeliveredStatusKPI49"));

		fieldMappings.put(JIRA_TICKET_CLOSED_STATUS,
				List.of("jiraTicketClosedStatusKPI48", "jiraTicketClosedStatusKPI50", "jiraTicketClosedStatusKPI51",
						"jiraTicketClosedStatusKPI53", "jiraTicketClosedStatusKPI54", "jiraTicketClosedStatusKPI55",
						"jiraTicketClosedStatusKPI997"));

		fieldMappings.put(KANBAN_CYCLE_TIME_ISSUE_TYPE, List.of("kanbanCycleTimeIssueTypeKPI53"));

		fieldMappings.put(JIRA_TICKET_TRIAGED_STATUS, List.of("jiraTicketTriagedStatusKPI53"));

		fieldMappings.put(JIRA_TICKET_REJECTED_STATUS, List.of("jiraTicketRejectedStatusKPI50",
				"jiraTicketRejectedStatusKPI151", "jiraTicketRejectedStatusKPI48", "jiraTicketRejectedStatusKPI997"));

		fieldMappings.put("thresholdValueKPI159",
				List.of("thresholdValueKPI51", "thresholdValueKPI55", "thresholdValueKPI54", "thresholdValueKPI50",
						"thresholdValueKPI48", "thresholdValueKPI997", "thresholdValueKPI63", "thresholdValueKPI71",
						"thresholdValueKPI49", "thresholdValueKPI58", "thresholdValueKPI66", "thresholdValueKPI53",
						"thresholdValueKPI74", "thresholdValueKPI114", "thresholdValueKPI183", "thresholdValueKPI184",
						"thresholdValueKPI65"));

		return fieldMappings;
	}

	@Execution
	public void execution() {
		duplicateFieldMappingValue();
		duplicateFieldMappingStruct();
		addThresholdAndBackGroundColor();
		updateFieldMappingStruct();
	}

	/** Duplicate the field mapping values */
	public void duplicateFieldMappingValue() {
		final Map<String, List<String>> fieldToDuplicateMap = getFieldToDuplicateMap();

		// Use aggregation pipeline to update the documents
		List<Bson> pipeline = new ArrayList<>();
		fieldToDuplicateMap.forEach((originalValue, duplicateFields) -> duplicateFields.forEach(
				duplicateField -> pipeline.add(Aggregates.addFields(new Field<>(duplicateField, "$" + originalValue)))));
		pipeline.add(Aggregates.merge(FIELD_MAPPING));

		mongoTemplate.getCollection(FIELD_MAPPING).aggregate(pipeline).toCollection();
	}

	/** Duplicate the field mapping structure */
	public void duplicateFieldMappingStruct() {
		Map<String, List<String>> fieldMappings = getFieldToDuplicateMap();

		for (Map.Entry<String, List<String>> entry : fieldMappings.entrySet()) {
			String originalFieldName = entry.getKey();
			List<String> duplicateFieldNames = entry.getValue();

			// Find the original document
			Document originalDoc = mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE)
					.find(new Document(FIELD_NAME, originalFieldName)).first();

			if (originalDoc != null) {
				// Remove the _id field to avoid duplication error
				originalDoc.remove("_id");

				for (String duplicateFieldName : duplicateFieldNames) {
					// Create and insert the duplicate
					Document duplicateDoc = new Document(originalDoc);
					duplicateDoc.put("_id", new ObjectId());
					duplicateDoc.put(FIELD_NAME, duplicateFieldName);
					mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE).insertOne(duplicateDoc);
				}
			}
		}
	}

	private void addThresholdAndBackGroundColor() {
		addThresholdAndBgDetails("kpi51", "0", false);
		addThresholdAndBgDetails("kpi55", "0", false);
		addThresholdAndBgDetails("kpi54", "0", false);
		addThresholdAndBgDetails("kpi50", "0", false);
		addThresholdAndBgDetails("kpi48", "0", false);
		addThresholdAndBgDetails("kpi997", "0", false);
		addThresholdAndBgDetails("kpi63", "55", true);
		addThresholdAndBgDetails("kpi62", "55", true);
		addThresholdAndBgDetails("kpi64", "55", false);
		addThresholdAndBgDetails("kpi67", "55", false);
		addThresholdAndBgDetails("kpi71", "55", true);
		addThresholdAndBgDetails("kpi49", "0", true);
		addThresholdAndBgDetails("kpi58", "0", true);
		addThresholdAndBgDetails("kpi66", "0", false);
		addThresholdAndBgDetails("kpi65", "55", true);
		addThresholdAndBgDetails("kpi53", "0", false);
		addThresholdAndBgDetails("kpi74", "0", true);
		addThresholdAndBgDetails("kpi114", "0", true);
		addThresholdAndBgDetails("kpi159", "55", true);
		addThresholdAndBgDetails("kpi184", "30", false);
		addThresholdAndBgDetails("kpi183", "6", true);
	}

	/** Threshold value for kanban KPIs */
	private void addThresholdAndBgDetails(String kpiId, String thresholdValue, boolean isPositiveTrend) {
		String lowerThresholdBG = isPositiveTrend ? "red" : "white";
		String upperThresholdBG = isPositiveTrend ? "white" : "red";

		Document updateFilter = new Document("kpiId", kpiId);

		Document update = new Document("$set", new Document("thresholdValue", thresholdValue)
				.append("lowerThresholdBG", lowerThresholdBG).append("upperThresholdBG", upperThresholdBG));

		mongoTemplate.getCollection(KPI_MASTER).updateOne(updateFilter, update);
	}

	public void updateFieldMappingStruct() {
		updateField(JIRA_LIVE_STATUS_LTK, STATUS_TO_IDENTIFY_LIVE_TICKETS,
				WORKFLOW_STATUS_USED_TO_IDENTIFY_TICKETS_IN_LIVE_STATE, WORK_FLOW_STATUS_MAPPING, true);
		updateField(JIRA_LIVE_STATUS_NOPK, STATUS_TO_IDENTIFY_LIVE_TICKETS,
				WORKFLOW_STATUS_USED_TO_IDENTIFY_TICKETS_IN_LIVE_STATE, WORK_FLOW_STATUS_MAPPING, true);
		updateField(JIRA_LIVE_STATUS_NOSK, STATUS_TO_IDENTIFY_LIVE_TICKETS,
				WORKFLOW_STATUS_USED_TO_IDENTIFY_TICKETS_IN_LIVE_STATE, WORK_FLOW_STATUS_MAPPING, true);
		updateField(JIRA_LIVE_STATUS_NORK, STATUS_TO_IDENTIFY_LIVE_TICKETS,
				WORKFLOW_STATUS_USED_TO_IDENTIFY_TICKETS_IN_LIVE_STATE, WORK_FLOW_STATUS_MAPPING, true);
		updateField(JIRA_LIVE_STATUS_OTA, STATUS_TO_IDENTIFY_LIVE_TICKETS,
				WORKFLOW_STATUS_USED_TO_IDENTIFY_TICKETS_IN_LIVE_STATE, WORK_FLOW_STATUS_MAPPING, true);
		updateField(TICKET_COUNT_ISSUE_TYPE, ISSUE_TYPES_TO_CONSIDER, ALL_ISSUE_TYPE_TO_TRACK_TICKETS, ISSUE_TYPES_MAPPING,
				false);
		updateField(KANBAN_RCA_COUNT_ISSUE_TYPE, "Issue types to be included for RCA",
				"All issue types to be considered for root cause analysis", ISSUE_TYPES_MAPPING, false);
		updateField(JIRA_TICKET_VELOCITY_ISSUE_TYPE, ISSUE_TYPES_TO_CONSIDER, ALL_ISSUE_TYPE_TO_TRACK_TICKETS,
				ISSUE_TYPES_MAPPING, false);
		updateField(KANBAN_CYCLE_TIME_ISSUE_TYPE, ISSUE_TYPES_TO_CONSIDER, ALL_ISSUE_TYPE_TO_TRACK_TICKETS,
				ISSUE_TYPES_MAPPING, false);
		updateField(TICKET_DELIVERD_STATUS, "Status to identify Delivered tickets",
				"Workflow status used to identify delivered tickets", WORK_FLOW_STATUS_MAPPING, true);
		updateField(JIRA_TICKET_CLOSED_STATUS, "Status to identify Closed tickets",
				"Workflow status used to identify resolved tickets", WORK_FLOW_STATUS_MAPPING, true);
		updateField(JIRA_TICKET_TRIAGED_STATUS, "Status to identify Triaged tickets",
				"Workflow status used to identify triaged tickets", WORK_FLOW_STATUS_MAPPING, true);
		updateField(JIRA_TICKET_REJECTED_STATUS, "Status to identify Rejected/Dropped tickets",
				"Workflow status used to identify rejected/dropped tickets", WORK_FLOW_STATUS_MAPPING, true);
	}

	private void updateField(String fieldName, String fieldLabel, String tooltip, String section, boolean mandatory) {
		Document query = new Document(FIELD_NAME, fieldName);
		Document update = new Document("$set", new Document("fieldLabel", fieldLabel).append("tooltip.definition", tooltip)
				.append("section", section).append("mandatory", mandatory));
		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE).updateOne(query, update);

		// Check for duplicates and update them as well
		Map<String, List<String>> fieldMappings = getFieldToDuplicateMap();
		if (fieldMappings.containsKey(fieldName)) {
			for (String duplicateFieldName : fieldMappings.get(fieldName)) {
				Document duplicateQuery = new Document(FIELD_NAME, duplicateFieldName);
				mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE).updateOne(duplicateQuery, update);
			}
		}
	}

	@RollbackExecution
	public void rollback() {
		rollbackDuplicateFieldMappingValue();
		rollbackDuplicateFieldMappingStruct();
		rollbackThresholdAndBg();
		rollbackFieldMappingStruct();
	}

	/** Rollback the duplicate field mapping values */
	public void rollbackDuplicateFieldMappingValue() {
		final Map<String, List<String>> fieldToDuplicateMap = getFieldToDuplicateMap();

		// Use aggregation pipeline to remove the duplicated fields
		List<Bson> pipeline = new ArrayList<>();
		fieldToDuplicateMap.forEach((originalValue, duplicateFields) -> duplicateFields.forEach(duplicateField -> {
			Bson unsetOperation = Aggregates.unset(duplicateField);
			pipeline.add(unsetOperation);
		}));

		// Define merge options
		MergeOptions mergeOptions = new MergeOptions().whenMatched(MergeOptions.WhenMatched.REPLACE)
				.whenNotMatched(MergeOptions.WhenNotMatched.INSERT);

		pipeline.add(Aggregates.merge(FIELD_MAPPING, mergeOptions));

		mongoTemplate.getCollection(FIELD_MAPPING).aggregate(pipeline).toCollection();
	}

	/** Rollback the duplicated field mapping structure */
	public void rollbackDuplicateFieldMappingStruct() {
		final Map<String, List<String>> fieldToDuplicateMap = getFieldToDuplicateMap();

		// Collect all duplicate field names
		List<String> duplicateFieldNames = new ArrayList<>();
		fieldToDuplicateMap.values().forEach(duplicateFieldNames::addAll);

		// Delete all documents with field names in the collected list
		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE)
				.deleteMany(new Document(FIELD_NAME, new Document("$in", duplicateFieldNames)));
	}

	/** Rollback the threshold value for kanban KPIs */
	private void rollbackThresholdAndBg() {
		List<String> kpiIds = Arrays.asList("kpi51", "kpi55", "kpi54", "kpi50", "kpi48", "kpi997", "kpi63", "kpi62",
				"kpi64", "kpi67", "kpi71", "kpi49", "kpi58", "kpi66", "kpi65", "kpi53", "kpi74", "kpi114", "kpi159", "kpi184",
				"kpi183");

		Document filter = new Document("kpiId", new Document("$in", kpiIds));

		Document update = new Document("$unset",
				new Document("thresholdValue", "").append("lowerThresholdBG", "").append("upperThresholdBG", ""));

		mongoTemplate.getCollection(KPI_MASTER).updateMany(filter, update);
	}

	public void rollbackFieldMappingStruct() {
		updateField(JIRA_LIVE_STATUS_LTK, "Live Status - Lead Time",
				PROVIDE_ANY_STATUS_FROM_WORKFLOW_ON_WHICH_LIVE_IS_CONSIDERED, WORK_FLOW_STATUS_MAPPING, true);
		updateField(JIRA_LIVE_STATUS_NOPK, "Live Status - Net Open Ticket Count by Priority",
				PROVIDE_ANY_STATUS_FROM_WORKFLOW_ON_WHICH_LIVE_IS_CONSIDERED, WORK_FLOW_STATUS_MAPPING, true);
		updateField(JIRA_LIVE_STATUS_NOSK, "Live Status - Net Open Ticket by Status",
				PROVIDE_ANY_STATUS_FROM_WORKFLOW_ON_WHICH_LIVE_IS_CONSIDERED, WORK_FLOW_STATUS_MAPPING, true);
		updateField(JIRA_LIVE_STATUS_NORK, "Live Status - Net Open Ticket Count By RCA",
				PROVIDE_ANY_STATUS_FROM_WORKFLOW_ON_WHICH_LIVE_IS_CONSIDERED, WORK_FLOW_STATUS_MAPPING, true);
		updateField(JIRA_LIVE_STATUS_OTA, "Live Status - Open Ticket Ageing",
				PROVIDE_ANY_STATUS_FROM_WORKFLOW_ON_WHICH_LIVE_IS_CONSIDERED, WORK_FLOW_STATUS_MAPPING, true);
		updateField(TICKET_COUNT_ISSUE_TYPE, "Ticket Count Issue Type", "", ISSUE_TYPES_MAPPING, false);
		updateField(KANBAN_RCA_COUNT_ISSUE_TYPE, "Ticket RCA Count Issue Type", "", ISSUE_TYPES_MAPPING, false);
		updateField(JIRA_TICKET_VELOCITY_ISSUE_TYPE, "Ticket Velocity Issue Type", "", ISSUE_TYPES_MAPPING, false);
		updateField(KANBAN_CYCLE_TIME_ISSUE_TYPE, "Kanban Lead Time Issue Type", "", ISSUE_TYPES_MAPPING, false);
		updateField(TICKET_DELIVERD_STATUS, "Ticket Delivered Status",
				"Status from workflow on which ticket is considered as delivered.", WORK_FLOW_STATUS_MAPPING, true);
		updateField(JIRA_TICKET_CLOSED_STATUS, "Ticket Closed Status",
				"Status from workflow on which ticket is considered as Resolved.", WORK_FLOW_STATUS_MAPPING, true);
		updateField(JIRA_TICKET_TRIAGED_STATUS, "Ticket Triaged Status",
				"Status from workflow on which ticket is considered as Triaged.", WORK_FLOW_STATUS_MAPPING, true);
		updateField(JIRA_TICKET_REJECTED_STATUS, "Ticket Rejected/Dropped Status",
				"Status from workflow on which ticket is considered as Rejected/Dropped.", WORK_FLOW_STATUS_MAPPING, true);
	}
}
