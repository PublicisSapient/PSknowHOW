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

package com.publicissapient.kpidashboard.apis.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * to order the headings of excel columns
 */
@SuppressWarnings("java:S1192")
public enum KPIExcelColumn {

	CODE_BUILD_TIME("kpi8",
			Arrays.asList("Project Name", "Job Name", "Start Time", "End Time", "Duration", "Build Status", "Build Url",
					"Weeks")), ISSUE_COUNT("kpi40",
							Arrays.asList("Sprint Name", "Story ID", "Issue Description")), CODE_COMMIT("kpi11",
									Arrays.asList("Project Name", "Repository Url", "Branch", "Days/Weeks", "No. Of Commit",
											"No. of Merge")),

	MEAN_TIME_TO_MERGE("kpi84",
			Arrays.asList("Project", "Repository Url", "Branch", "Days/Weeks",
					"Mean Time To Merge (In Hours)")), AVERAGE_RESOLUTION_TIME(
							"kpi83",
							Arrays.asList("Sprint Name", "Story ID", "Issue Description", "Issue Type",
									"Resolution Time(In Days)")),

	LEAD_TIME("kpi3", Arrays.asList("Issue ID", "Issue Type", "Issue Description", "Created Date", "Closed Date",
			"Lead Time (In Days)")),

	LEAD_TIME_KANBAN("kpi53", Arrays.asList("Project Name", "Story ID", "Issue Description", "Open to Triage(In Days)",
			"Triage to Complete (In Days)", "Complete TO Live (In Days)", "Lead Time (In Days)")),

	SPRINT_VELOCITY("kpi39",
			Arrays.asList("Sprint Name", "Story ID", "Issue Description",
					"Size(story point/hours)")), SPRINT_PREDICTABILITY(
							"kpi5",
							Arrays.asList("Sprint Name", "Story ID", "Issue Description",
									"Size(story point/hours)")), SPRINT_CAPACITY_UTILIZATION(
											"kpi46",
											Arrays.asList("Sprint Name", "Story ID", "Issue Description",
													"Original Time Estimate (in hours)",
													"Total Time Spent (in hours)")), COMMITMENT_RELIABILITY("kpi72",
															Arrays.asList("Sprint Name", "Story ID", "Issue Status","Issue Type",
																	"Initial Commitment", "Size(story point/hours)")),

	DEFECT_INJECTION_RATE("kpi14", Arrays.asList("Sprint Name", "Story ID", "Issue Description", "Linked Defects")),

	FIRST_TIME_PASS_RATE("kpi82", Arrays.asList("Sprint Name", "Story ID", "Issue Description", "First Time Pass")),

	DEFECT_DENSITY("kpi111", Arrays.asList("Sprint Name", "Story ID", "Issue Description", "Linked Defects to Story",
			"Size(story point/hours)")),

	DEFECT_SEEPAGE_RATE("kpi35", Arrays.asList("Sprint Name", "Defect ID", "Issue Description", "Escaped Defect")),

	DEFECT_REMOVAL_EFFICIENCY("kpi34",
			Arrays.asList("Sprint Name", "Defect ID", "Issue Description", "Defect Removed")),

	DEFECT_REJECTION_RATE("kpi37", Arrays.asList("Sprint Name", "Defect ID", "Issue Description", "Defect Rejected")),

	DEFECT_COUNT_BY_PRIORITY("kpi28", Arrays.asList("Sprint Name", "Defect ID", "Issue Description", "Priority")),

	DEFECT_COUNT_BY_RCA("kpi36", Arrays.asList("Sprint Name", "Defect ID", "Issue Description", "Root Cause")),

	DEFECT_COUNT_BY_PRIORITY_PIE_CHART("kpi140", Arrays.asList("Defect ID", "Issue Description", "Issue Status",
			"Issue Type", "Size(story point/hours)", "Root Cause", "Priority", "Assignee", "Created during Iteration")),

	DEFECT_COUNT_BY_RCA_PIE_CHART("kpi132", Arrays.asList("Defect ID", "Issue Description", "Issue Status",
			"Issue Type", "Size(story point/hours)", "Root Cause", "Priority", "Assignee", "Created during Iteration")),

	CREATED_VS_RESOLVED_DEFECTS("kpi126", Arrays.asList("Sprint Name", "Created Defect ID", "Issue Description",
			"Defect added after Sprint Start", "Resolved")),

	DEFECT_COUNT_BY_STATUS_PIE_CHART("kpi136", Arrays.asList("Defect ID", "Issue Description", "Issue Status",
			"Issue Type", "Size(story point/hours)", "Root Cause", "Priority", "Assignee", "Created during Iteration")),

	REGRESSION_AUTOMATION_COVERAGE("kpi42", Arrays.asList("Sprint Name", "Test Case ID", "Automated")),

	INSPRINT_AUTOMATION_COVERAGE("kpi16", Arrays.asList("Sprint Name", "Test Case ID", "Linked Story ID", "Automated")),

	UNIT_TEST_COVERAGE("kpi17", Arrays.asList("Project", "Job Name", "Unit Coverage", "Weeks")),

	SONAR_VIOLATIONS("kpi38", Arrays.asList("Project", "Job Name", "Sonar Violations", "Weeks")),

	SONAR_TECH_DEBT("kpi27", Arrays.asList("Project", "Job Name", "Tech Debt (in days)", "Weeks")),

	CHANGE_FAILURE_RATE("kpi116", Arrays.asList("Project", "Job Name", "Total Build Count", "Total Build Failure Count",
			"Build Failure Percentage", "Weeks")),

	TEST_EXECUTION_AND_PASS_PERCENTAGE("kpi70",
			Arrays.asList("Sprint Name", "Total Test", "Executed Test", "Execution %", "Passed Test", "Passed %")),

	COST_OF_DELAY("kpi113",
			Arrays.asList("Project Name", "Cost of Delay", "Epic ID", "Epic Name", "Epic End Date", "Month")),

	RELEASE_FREQUENCY("kpi73",
			Arrays.asList("Project Name", "Release Name", "Release Description", "Release End Date", "Month")),

	DEPLOYMENT_FREQUENCY("kpi118", Arrays.asList("Project Name", "Date", "Job Name", "Weeks", "Environment")),

	DEFECTS_WITHOUT_STORY_LINK("kpi80",
			Arrays.asList("Project Name", "Priority", "Defects Without Story Link", "Issue Description")),

	TEST_WITHOUT_STORY_LINK("kpi79", Arrays.asList("Project Name", "Test Case ID", "Linked to Story")),

	ISSUES_WITHOUT_STORY_LINK("kpi129", Arrays.asList("Issue Id", "Issue Description")),

	PRODUCTION_DEFECTS_AGEING("kpi127",
			Arrays.asList("Project Name", "Defect ID", "Issue Description", "Priority", "Created Date", "Status")),

	UNIT_TEST_COVERAGE_KANBAN("kpi62", Arrays.asList("Project", "Job Name", "Unit Coverage", "Day/Week/Month")),

	SONAR_VIOLATIONS_KANBAN("kpi64", Arrays.asList("Project", "Job Name", "Sonar Violations", "Day/Week/Month")),

	SONAR_TECH_DEBT_KANBAN("kpi67", Arrays.asList("Project", "Job Name", "Tech Debt (in days)", "Day/Week/Month")),

	TEST_EXECUTION_KANBAN("kpi71", Arrays.asList("Project", "Execution Date", "Total Test", "Executed Test",
			"Execution %", "Passed Test", "Passed %")),

	KANBAN_REGRESSION_PASS_PERCENTAGE("kpi63", Arrays.asList("Project", "Day/Week/Month", "Test Case ID", "Automated")),

	OPEN_TICKET_AGING_BY_PRIORITY("kpi997",
			Arrays.asList("Project", "Ticket Issue ID", "Priority", "Created Date", "Issue Status")),

	NET_OPEN_TICKET_COUNT_BY_STATUS("kpi48",
			Arrays.asList("Project", "Day/Week/Month", "Ticket Issue ID", "Issue Status", "Created Date")),

	NET_OPEN_TICKET_COUNT_BY_RCA("kpi51",
			Arrays.asList("Project", "Day/Week/Month", "Ticket Issue ID", "Root Cause", "Created Date")),

	TICKET_COUNT_BY_PRIORITY("kpi50",
			Arrays.asList("Project", "Day/Week/Month", "Ticket Issue ID", "Priority", "Created Date")),

	TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE("kpi55",
			Arrays.asList("Project", "Day/Week/Month", "Ticket Issue ID", "Issue Type", "Status")),

	TICKET_OPEN_VS_CLOSE_BY_PRIORITY("kpi54",
			Arrays.asList("Project", "Day/Week/Month", "Ticket Issue ID", "Issue Priority", "Status")),

	TICKET_VELOCITY("kpi49",
			Arrays.asList("Project Name", "Day/Week/Month", "Ticket Issue ID", "Issue Type", "Size (In Story Points)")),

	CODE_BUILD_TIME_KANBAN("kpi66", Arrays.asList("Project Name", "Job Name", "Start Time", "End Time", "Duration",
			"Build Status", "Build Url")),

	CODE_COMMIT_MERGE_KANBAN("kpi65",
			Arrays.asList("Project Name", "Repository Url", "Branch", "Days/Weeks", "No. Of Commit")),

	TEAM_CAPACITY_KANBAN("kpi58",
			Arrays.asList("Project Name", "Start Date", "End Date", "Estimated Capacity (in hours)")),

	ISSUES_LIKELY_TO_SPILL("kpi123",
			Arrays.asList("Issue Id", "Issue Type", "Issue Description", "Issue Priority", "Size(story point/hours)",
					"Issue Status", "Due Date", "Remaining Estimate", "Predicted Completion Date", "Assignee")),

	ITERATION_COMMITMENT("kpi120", Arrays.asList("Issue Id", "Issue Description", "Issue Status", "Issue Type",
			"Size(story point/hours)", "Priority", "Due Date", "Original Estimate", "Remaining Estimate", "Assignee")),

	ESTIMATE_HYGINE("kpi124", Arrays.asList("Issue Id", "Issue Description", "Issue Status", "Issue Type", "Assignee")),

	ITERATION_STATUS("kpi130", Arrays.asList("Issue Id", "Issue Type", "Priority", "Issue Description", "Issue Status",
			"Due Date", "Remaining Hours", "Delay")),

	ESTIMATE_VS_ACTUAL("kpi75", Arrays.asList("Issue Id", "Issue Description", "Issue Status", "Issue Type",
			"Original Estimate", "Logged Work", "Assignee")),

	PLANNED_WORK_STATUS("kpi128", Arrays.asList(new KPIExcelColumnInfo("Issue Id", ""),
			new KPIExcelColumnInfo("Issue Description", ""), new KPIExcelColumnInfo("Issue Status", ""),
			new KPIExcelColumnInfo("Issue Type", ""), new KPIExcelColumnInfo("Size(story point/hours)", ""),
			new KPIExcelColumnInfo("Original Estimate", ""), new KPIExcelColumnInfo("Remaining Estimate", ""),
			new KPIExcelColumnInfo("Due Date", ""), new KPIExcelColumnInfo("Actual Start Date", ""),
			new KPIExcelColumnInfo("Dev Completion Date", ""), new KPIExcelColumnInfo("Actual Completion Date", ""),
			new KPIExcelColumnInfo("Delay(in days)",
					"Delay is calculated based on difference between time taken to complete an issue that depends on the Due date and Actual completion date (In Days)"),
			new KPIExcelColumnInfo("Predicted Completion Date", ""),
			new KPIExcelColumnInfo("Potential Delay(in days)", ""), new KPIExcelColumnInfo("Assignee", ""))),

	DEV_COMPLETION_STATUS("kpi145", Arrays.asList(new KPIExcelColumnInfo("Issue Id", ""),
			new KPIExcelColumnInfo("Issue Description", ""), new KPIExcelColumnInfo("Issue Status", ""),
			new KPIExcelColumnInfo("Issue Type", ""), new KPIExcelColumnInfo("Size(story point/hours)", ""),
			new KPIExcelColumnInfo("Remaining Estimate", ""), new KPIExcelColumnInfo("Dev Due Date", ""),
			new KPIExcelColumnInfo("Dev Completion Date", ""),
			new KPIExcelColumnInfo("Delay(in days)",
					"Delay is calculated based on difference between time taken to complete Development of an issue that depends on the Dev Due date and Dev completion date (In Days)"),
			new KPIExcelColumnInfo("Assignee", ""))),

	WORK_REMAINING("kpi119",
			Arrays.asList("Issue Id", "Issue Description", "Issue Status", "Issue Type", "Size(story point/hours)",
					"Original Estimate", "Remaining Estimate", "Dev Due Date", "Dev Completion Date", "Due Date",
					"Predicted Completion Date", "Potential Delay(in days)", "Assignee")),

	WASTAGE("kpi131", Arrays.asList("Issue Id", "Issue Type", "Issue Description", "Priority",
			"Size(story point/hours)", "Blocked Time", "Wait Time", "Total Wastage", "Assignee")),

	QUALITY_STATUS("kpi133", Arrays.asList("Issue Id", "Issue Type", "Issue Description", "Issue Status", "Priority",
			"Linked Stories", "Linked Stories Size", "Assignee")),

	UNPLANNED_WORK_STATUS("kpi134", Arrays.asList("Issue Id", "Issue Type", "Issue Description", "Priority",
			"Issue Status", "Size(story point/hours)", "Remaining Estimate", "Assignee")),

	CLOSURES_POSSIBLE_TODAY("kpi122", Arrays.asList("Issue Id", "Issue Type", "Issue Description",
			"Size(story point/hours)", "Issue Status", "Due Date", "Remaining Estimate", "Assignee")),

	INVALID("INVALID_KPI", Arrays.asList("Invalid")), BACKLOG_READINESS_EFFICIENCY("kpi138",
			Arrays.asList("Issue Id", "Issue Type", "Issue Description", "Priority", "Size(story point/hours)")),

	FIRST_TIME_PASS_RATE_ITERATION("kpi135",
			Arrays.asList("Issue Id", "Issue Description", "First Time Pass", "Linked Defect", "Defect Priority")),

	DEFECT_REOPEN_RATE("kpi137", Arrays.asList("Issue Id", "Issue Description", "Issue Status", "Priority",
			"Closed Date", "Reopen Date", "Time taken to reopen")),

	REFINEMENT_REJECTION_RATE("kpi139", Arrays.asList("Issue ID", "Issue Description", "Priority", "Status",
			"Change Date", "Weeks", "Issue Status")),

	DEFECT_COUNT_BY_STATUS_RELEASE("kpi141", Arrays.asList("Issue ID", "Issue Description", "Sprint Name", "Issue Type",
			"Issue Status", "Root Cause", "Priority", "Assignee")),
	BACKLOG_COUNT_BY_STATUS("kpi151", Arrays.asList("Issue ID", "Issue Description", "Issue Type",
			"Issue Status", "Priority","Created Date","Updated Date", "Assignee")),
	ITERATION_READINESS("kpi161", Arrays.asList("Issue ID", "Issue Description", "Sprint Name", "Sprint Start Date", "Issue Type",
			"Issue Status", "Size(story point/hours)")),
	BACKLOG_COUNT_BY_ISSUE_TYPE("kpi152", Arrays.asList("Issue ID", "Issue Description", "Issue Type",
			"Issue Status", "Priority","Created Date","Updated Date", "Assignee")),

	DEFECT_COUNT_BY_RCA_RELEASE("kpi142", Arrays.asList("Issue ID", "Issue Description", "Sprint Name", "Issue Type",
			"Issue Status", "Root Cause", "Priority", "Assignee")),

	DEFECT_COUNT_BY_ASSIGNEE_RELEASE("kpi143", Arrays.asList("Issue ID", "Issue Description", "Sprint Name",
			"Issue Type", "Issue Status", "Root Cause", "Priority", "Assignee")),

	DEFECT_COUNT_BY_PRIORITY_RELEASE("kpi144", Arrays.asList("Issue ID", "Issue Description", "Sprint Name",
			"Issue Type", "Issue Status", "Root Cause", "Priority", "Assignee")),

	RELEASE_PROGRESS("kpi147",
			Arrays.asList("Issue ID", "Issue Type", "Issue Description","Size(story point/hours)", "Priority", "Assignee",
					"Issue Status")),

	HAPPINESS_INDEX_RATE("kpi149",
							Arrays.asList("Sprint Name", "User Name", "Sprint Rating")),

	FLOW_DISTRIBUTION("Kpi146",
									Arrays.asList("Date")), FLOW_LOAD("kpi148", Arrays.asList("Date")),

	RELEASE_BURNUP("kpi150", Arrays.asList("Issue ID", "Issue Type", "Issue Description", "Size(story point/hours)",
			"Priority", "Assignee", "Issue Status", "Release Tag Date (Latest)", "Dev Completion Date", "Completion Date")),

	PI_PREDICTABILITY("kpi153", Arrays.asList("Project Name", "Epic ID", "Epic Name", "Status", "PI Name",
			"Planned Value", "Achieved Value")),

	DAILY_STANDUP_VIEW("kpi154", Arrays.asList("Remaining Capacity", "Remaining Estimate", "Remaining Work", "Delay")),

	//DTS-26123 start
	DEFECT_COUNT_BY_TYPE("kpi155", Arrays.asList("Issue ID", "Issue Description", "Issue Type",
			"Issue Status", "Sprint Name", "Priority","Created Date","Updated Date", "Assignee")),
	//DTS-26123 end
	SCOPE_CHURN("kpi164", Arrays.asList("Sprint Name","Issue ID", "Issue Type", "Issue Description", "Size(story point/hours)","Scope Change Date","Scope Change (Added/Removed)","Issue Status")),

	LEAD_TIME_FOR_CHANGE("Kpi156", Arrays.asList("Project Name", "Date", "Story ID", "Lead Time (In Days)",
			"Completion Date", "Merge Date", "Release Date", "Merge Request Id", "Branch")),

	RELEASE_DEFECT_BY_TEST_PHASE("kpi163", Arrays.asList("Issue ID", "Issue Description", "Issue Type", "Priority",
			"Sprint Name", "Assignee", "Issue Status")),

	PICKUP_TIME("kpi160",
			Arrays.asList("Project", "Repository Url", "Branch", "Days/Weeks",
					"Pickup Time (In Hours)")),

	PR_SIZE("kpi162",
			Arrays.asList("Project", "Repository Url", "Branch", "Days/Weeks",
					"PR Size (No. of lines)")),

	EPIC_PROGRESS("kpi165", Arrays.asList("Epic ID", "Epic Name", "Size(story point/hours)", "Epic Status")),

	BACKLOG_EPIC_PROGRESS("kpi169", Arrays.asList("Epic ID", "Epic Name", "Size(story point/hours)", "Epic Status")),

    MEAN_TIME_TO_RECOVER("kpi166", Arrays.asList("Project Name", "Date", "Story ID", "Issue Type", "Issue Description",
            "Created Date", "Completion Date", "Time to Recover (In Hours)")),

	CODE_QUALITY("kpi168", Arrays.asList("Project", "Job Name", "Code Quality", "Month")),
	FLOW_EFFICIENCY("kpi170", Arrays.asList("Issue ID", "Issue Type", "Issue Description", "Size (In Story Points)",
			"Wait Time", "Total Time", "Flow Efficiency")),

	CYCLE_TIME("kpi171", Arrays.asList("Issue Id", "Issue Type", "Issue Description","DOR Date", "Intake to DOR",
			"DOD Date", "DOR to DOD", "Live Date", "DOD to Live"));

	// @formatter:on

	private String kpiId;

	private List<String> columns;
	private List<KPIExcelColumnInfo> kpiExcelColumnInfo;

	KPIExcelColumn(String kpiID, List<Object> columns) {
		this.kpiId = kpiID;
		if (columns.get(0) instanceof String) {
			this.columns = columns.stream().map(Object::toString).collect(Collectors.toList());
		} else {
			ObjectMapper objectMapper = new ObjectMapper();
			List<KPIExcelColumnInfo> kpiExcelColumnInfoList = new ArrayList<>();
			columns.forEach(o -> {
				KPIExcelColumnInfo kpiExcelColumnInfo1 = objectMapper.convertValue(o, KPIExcelColumnInfo.class);
				kpiExcelColumnInfoList.add(kpiExcelColumnInfo1);
			});
			this.kpiExcelColumnInfo = kpiExcelColumnInfoList;
		}
	}

	public List<KPIExcelColumnInfo> getKpiExcelColumnInfo() {
		return kpiExcelColumnInfo;
	}

	/**
	 * Gets kpi id.
	 *
	 * @return the kpi id
	 */
	public String getKpiId() {
		return kpiId;
	}

	/**
	 * Gets source.
	 *
	 * @return the source
	 */
	public List<String> getColumns() {
		return columns;
	}

}