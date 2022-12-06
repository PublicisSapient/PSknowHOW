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

import com.publicissapient.kpidashboard.apis.constant.KPIExcelConstant;

import java.util.Arrays;
import java.util.List;

/**
 * to order the headings of excel columns
 */
public enum KPIExcelColumn {

    CODE_BUILD_TIME("kpi8", Arrays.asList(KPIExcelConstant.PROJECT_NAME, KPIExcelConstant.JOB_NAME, KPIExcelConstant.START_TIME, KPIExcelConstant.END_TIME, KPIExcelConstant.DURATION, KPIExcelConstant.BUILD_STATUS, KPIExcelConstant.BUILD_URL, KPIExcelConstant.WEEKS)),
    STORY_COUNT("kpi40", Arrays.asList(KPIExcelConstant.SPRINT_NAME, KPIExcelConstant.STORY_ID, KPIExcelConstant.ISSUE_DESCRIPTION)),
    CODE_COMMIT("kpi11", Arrays.asList(KPIExcelConstant.PROJECT_NAME, KPIExcelConstant.REPOSITORY_URL, KPIExcelConstant.BRANCH, KPIExcelConstant.DAY, KPIExcelConstant.COMMITS, KPIExcelConstant.MERGE)),

    MEAN_TIME_TO_MERGE("kpi84", Arrays.asList(KPIExcelConstant.PROJECT, KPIExcelConstant.REPOSITORY_URL, KPIExcelConstant.BRANCH, KPIExcelConstant.WEEKS, KPIExcelConstant.MEAN_TIME_TO_MERGE)),
    AVERAGE_RESOLUTION_TIME("kpi83", Arrays.asList(KPIExcelConstant.SPRINT_NAME, KPIExcelConstant.STORY_ID, KPIExcelConstant.ISSUE_DESCRIPTION, KPIExcelConstant.ISSUE_TYPE, KPIExcelConstant.RESOLUTION_TIME)),
    LEAD_TIME("kpi3", Arrays.asList(KPIExcelConstant.PROJECT_NAME, KPIExcelConstant.STORY_ID,KPIExcelConstant.ISSUE_DESCRIPTION,KPIExcelConstant.INTAKE_TO_DOR, KPIExcelConstant.DOR_TO_DOD, KPIExcelConstant.DOD_TO_LIVE, KPIExcelConstant.LEAD_TIME)),

    LEAD_TIME_KANBAN("kpi53", Arrays.asList(KPIExcelConstant.PROJECT_NAME,KPIExcelConstant.STORY_ID,KPIExcelConstant.ISSUE_DESCRIPTION, KPIExcelConstant.OPEN_TO_TRIAGE, KPIExcelConstant.TRIAGE_TO_COMPLETE, KPIExcelConstant.COMPLETE_TO_LIVE, KPIExcelConstant.LEAD_TIME)),

    SPRINT_VELOCITY("kpi39", Arrays.asList(KPIExcelConstant.SPRINT_NAME, KPIExcelConstant.STORY_ID, KPIExcelConstant.ISSUE_DESCRIPTION, KPIExcelConstant.STORY_SIZE_SP)),
	SPRINT_PREDICTABILITY("kpi5", Arrays.asList(KPIExcelConstant.SPRINT_NAME, KPIExcelConstant.STORY_ID, KPIExcelConstant.ISSUE_DESCRIPTION, KPIExcelConstant.STORY_SIZE_SP)),
	SPRINT_CAPACITY_UTILIZATION("kpi46", Arrays.asList(KPIExcelConstant.SPRINT_NAME, KPIExcelConstant.STORY_ID, KPIExcelConstant.ISSUE_DESCRIPTION, KPIExcelConstant.ORIGINAL_TIME_ESTIMATE_IN_HOURS, "Total Time Spent (in hours)")),
    COMMITMENT_RELIABILITY("kpi72", Arrays.asList(KPIExcelConstant.SPRINT_NAME, KPIExcelConstant.STORY_ID, "Closed" ,KPIExcelConstant.STORY_SIZE_SP)),


    DEFECT_INJECTION_RATE("kpi14", Arrays.asList(KPIExcelConstant.SPRINT_NAME, KPIExcelConstant.STORY_ID, KPIExcelConstant.ISSUE_DESCRIPTION, "Linked Defects")),

    FIRST_TIME_PASS_RATE("kpi82", Arrays.asList(KPIExcelConstant.SPRINT_NAME, KPIExcelConstant.STORY_ID, KPIExcelConstant.ISSUE_DESCRIPTION, "First Time Pass")),

    DEFECT_DENSITY("kpi111", Arrays.asList(KPIExcelConstant.SPRINT_NAME, KPIExcelConstant.STORY_ID, KPIExcelConstant.ISSUE_DESCRIPTION, "Linked Defects to Story",KPIExcelConstant.STORY_SIZE_SP)),

    DEFECT_SEEPAGE_RATE("kpi35", Arrays.asList(KPIExcelConstant.SPRINT_NAME, KPIExcelConstant.DEFECT_ID, KPIExcelConstant.ISSUE_DESCRIPTION, "Escaped Defect")),

    DEFECT_REMOVAL_EFFICIENCY("kpi34", Arrays.asList(KPIExcelConstant.SPRINT_NAME, KPIExcelConstant.DEFECT_ID, KPIExcelConstant.ISSUE_DESCRIPTION, "Defect Removed")),

    DEFECT_REJECTION_RATE("kpi37", Arrays.asList(KPIExcelConstant.SPRINT_NAME, KPIExcelConstant.DEFECT_ID, KPIExcelConstant.ISSUE_DESCRIPTION, "Defect Rejected")),

    DEFECT_COUNT_BY_PRIORITY("kpi28", Arrays.asList(KPIExcelConstant.SPRINT_NAME, KPIExcelConstant.DEFECT_ID, KPIExcelConstant.ISSUE_DESCRIPTION, "Priority")),

    DEFECT_COUNT_BY_RCA("kpi36", Arrays.asList(KPIExcelConstant.SPRINT_NAME, KPIExcelConstant.DEFECT_ID, KPIExcelConstant.ISSUE_DESCRIPTION, "Root Cause")),

    CREATED_VS_RESOLVED_DEFECTS("kpi126", Arrays.asList(KPIExcelConstant.SPRINT_NAME, "Created Defect ID", KPIExcelConstant.ISSUE_DESCRIPTION, "Resolved")),

    REGRESSION_AUTOMATION_COVERAGE("kpi42", Arrays.asList(KPIExcelConstant.SPRINT_NAME, "Test Case ID", "Automated")),

    INSPRINT_AUTOMATION_COVERAGE("kpi16", Arrays.asList(KPIExcelConstant.SPRINT_NAME, "Test Case ID", "Linked Story ID", "Automated")),

    UNIT_TEST_COVERAGE("kpi17", Arrays.asList(KPIExcelConstant.PROJECT, KPIExcelConstant.JOB_NAME, "Unit Coverage", KPIExcelConstant.WEEKS)),

    SONAR_VIOLATIONS("kpi38", Arrays.asList(KPIExcelConstant.PROJECT, KPIExcelConstant.JOB_NAME, "Sonar Violations", KPIExcelConstant.WEEKS)),

    SONAR_TECH_DEBT("kpi27", Arrays.asList(KPIExcelConstant.PROJECT, KPIExcelConstant.JOB_NAME, "Tech Debt (in days)", KPIExcelConstant.WEEKS)),

    CHANGE_FAILURE_RATE("kpi116", Arrays.asList(KPIExcelConstant.PROJECT, KPIExcelConstant.JOB_NAME, "Total Build Count", "Total Build Failure Count", "Build Failure Percentage", KPIExcelConstant.WEEKS)),

    TEST_EXECUTION_AND_PASS_PERCENTAGE("kpi70", Arrays.asList(KPIExcelConstant.SPRINT_NAME, "Total Test", "Executed Test", "Execution %", "Passed Test", "Passed %")),

    COST_OF_DELAY("kpi113", Arrays.asList(KPIExcelConstant.PROJECT_NAME, "Cost of Delay", "Epic ID", "Epic Name", "Epic End Date", "Month")),

    RELEASE_FREQUENCY("kpi73", Arrays.asList(KPIExcelConstant.PROJECT_NAME, "Release Name", "Release Description", "Release End Date", "Month")),

    DEPLOYMENT_FREQUENCY("kpi118", Arrays.asList(KPIExcelConstant.PROJECT_NAME, "Date", KPIExcelConstant.JOB_NAME, "Month", "Environment")),

    DEFECTS_WITHOUT_STORY_LINK("kpi80", Arrays.asList(KPIExcelConstant.PROJECT_NAME,"Priority","Defects Without Story Link", KPIExcelConstant.ISSUE_DESCRIPTION)),

    TEST_WITHOUT_STORY_LINK("kpi79", Arrays.asList(KPIExcelConstant.PROJECT_NAME,"Test Case ID","Linked to Story")),

    PRODUCTION_DEFECTS_AGEING("kpi127", Arrays.asList(KPIExcelConstant.PROJECT_NAME, KPIExcelConstant.DEFECT_ID, KPIExcelConstant.ISSUE_DESCRIPTION, "Priority", "Created Date", "Status")),

	UNIT_TEST_COVERAGE_KANBAN("kpi62", Arrays.asList(KPIExcelConstant.PROJECT, KPIExcelConstant.JOB_NAME, "Unit Coverage", KPIExcelConstant.DAY_WEEK_MONTH)),

	SONAR_VIOLATIONS_KANBAN("kpi64", Arrays.asList(KPIExcelConstant.PROJECT, KPIExcelConstant.JOB_NAME, "Sonar Violations", KPIExcelConstant.DAY_WEEK_MONTH)),

	SONAR_TECH_DEBT_KANBAN("kpi67", Arrays.asList(KPIExcelConstant.PROJECT, KPIExcelConstant.JOB_NAME, "Tech Debt (in days)", KPIExcelConstant.DAY_WEEK_MONTH)),

	TEST_EXECUTION_KANBAN("kpi71", Arrays.asList(KPIExcelConstant.PROJECT, "Execution Date", "Total Test", "Executed Test",
			"Execution %", "Passed Test", "Passed %")),

	KANBAN_REGRESSION_PASS_PERCENTAGE("kpi63", Arrays.asList(KPIExcelConstant.PROJECT,KPIExcelConstant.DAY_WEEK_MONTH, "Test Case ID", "Automated")),

	OPEN_TICKET_AGING_BY_PRIORITY("kpi997",
			Arrays.asList(KPIExcelConstant.PROJECT, KPIExcelConstant.TICKET_ISSUE_ID, "Priority", "Created Date", "Issue Status")),

	NET_OPEN_TICKET_COUNT_BY_STATUS("kpi48",
			Arrays.asList(KPIExcelConstant.PROJECT, KPIExcelConstant.DAY_WEEK_MONTH, KPIExcelConstant.TICKET_ISSUE_ID, "Issue Status", "Created Date")),

	NET_OPEN_TICKET_COUNT_BY_RCA("kpi51",
			Arrays.asList(KPIExcelConstant.PROJECT, KPIExcelConstant.DAY_WEEK_MONTH, KPIExcelConstant.TICKET_ISSUE_ID, "Root Cause", "Created Date")),

	TICKET_COUNT_BY_PRIORITY("kpi50",
			Arrays.asList(KPIExcelConstant.PROJECT, KPIExcelConstant.DAY_WEEK_MONTH, KPIExcelConstant.TICKET_ISSUE_ID, "Priority", "Created Date")),

	TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE("kpi55",
			Arrays.asList(KPIExcelConstant.PROJECT, KPIExcelConstant.DAY_WEEK_MONTH, KPIExcelConstant.TICKET_ISSUE_ID, KPIExcelConstant.ISSUE_TYPE, "Status")),

	TICKET_OPEN_VS_CLOSE_BY_PRIORITY("kpi54",
			Arrays.asList(KPIExcelConstant.PROJECT, KPIExcelConstant.DAY_WEEK_MONTH, KPIExcelConstant.TICKET_ISSUE_ID, "Issue Priority", "Status")),

	TICKET_VELOCITY("kpi49",
			Arrays.asList(KPIExcelConstant.PROJECT_NAME, KPIExcelConstant.DAY_WEEK_MONTH, KPIExcelConstant.TICKET_ISSUE_ID, KPIExcelConstant.ISSUE_TYPE, "Size (In Story Points)")),

	CODE_BUILD_TIME_KANBAN("kpi66", Arrays.asList(KPIExcelConstant.PROJECT_NAME, KPIExcelConstant.JOB_NAME, KPIExcelConstant.START_TIME, KPIExcelConstant.END_TIME, KPIExcelConstant.DURATION,
			KPIExcelConstant.BUILD_STATUS, KPIExcelConstant.BUILD_URL)),

	CODE_COMMIT_MERGE_KANBAN("kpi65",
			Arrays.asList(KPIExcelConstant.PROJECT_NAME, KPIExcelConstant.REPOSITORY_URL, KPIExcelConstant.BRANCH, KPIExcelConstant.DAY, KPIExcelConstant.COMMITS)),

	TEAM_CAPACITY_KANBAN("kpi58",
			Arrays.asList(KPIExcelConstant.PROJECT_NAME, "Start Date", "End Date", "Estimated Capacity (in hours)")),

    MISSING_WORK_LOGS("kpi115", Arrays.asList(KPIExcelConstant.SPRINT_NAME, KPIExcelConstant.STORY_ID, "Total Time Spent (in hours)")),

    DAILY_CLOSURES("kpi125", Arrays.asList(KPIExcelConstant.SPRINT_NAME, KPIExcelConstant.TICKET_ISSUE_ID, KPIExcelConstant.ISSUE_DESCRIPTION,KPIExcelConstant.ISSUE_TYPE)),

    STORIES_WITHOUT_ESTIMATE("kpi81", Arrays.asList(KPIExcelConstant.SPRINT_NAME, KPIExcelConstant.STORY_ID, KPIExcelConstant.ISSUE_DESCRIPTION,KPIExcelConstant.ORIGINAL_TIME_ESTIMATE_IN_HOURS)),

	INVALID("INVALID_KPI", Arrays.asList("Invalid"));

    // @formatter:on

    private String kpiId;

    private List<String> columns;
    

    KPIExcelColumn(String kpiID, List<String> columns) {
        this.kpiId = kpiID;
        this.setColumns(columns);
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

    /**
     * Sets source.
     *
     * @return the source
     */
    private void setColumns(List<String> columns) {
        this.columns = columns;
    }

}