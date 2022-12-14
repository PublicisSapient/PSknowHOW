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

import java.util.Arrays;

/**
 * The enum Kpi code.
 *
 * @author tauakram Mapping of MasterData KPI Code with KPI Name.
 */
public enum KPICode {

	/**
	 * The Dor to dod.
	 */
	LEAD_TIME("kpi3", KPISource.JIRA.name()),
	/**
	 * Sprint predictability kpi code.
	 */
	SPRINT_PREDICTABILITY("kpi5", KPISource.JIRA.name()),
	/**
	 * Code build time kpi code.
	 */
	CODE_BUILD_TIME("kpi8", KPISource.JENKINS.name()),
	/**
	 * Code commit kpi code.
	 */
	CODE_COMMIT("kpi11", KPISource.BITBUCKET.name()),
	/**
	 * Defect injection rate kpi code.
	 */
	DEFECT_INJECTION_RATE("kpi14", KPISource.JIRA.name()),
	/**
	 * Code quality kpi code.
	 */
	CODE_QUALITY("kpi15", KPISource.SONAR.name()),
	/**
	 * Automation test percentage kpi code.
	 */
	INSPRINT_AUTOMATION_COVERAGE("kpi16", KPISource.ZEPHYR.name()),
	/**
	 * J unit kpi code.
	 */
	UNIT_TEST_COVERAGE("kpi17", KPISource.SONAR.name()),
	/**
	 * Tech debt kpi code.
	 */
	// JS_UNIT("kpi18", KPISource.SONAR.name()),
	TECH_DEBT("kpi26", KPISource.JIRA.name()),
	/**
	 * Sonar tech debt kpi code.
	 */
	SONAR_TECH_DEBT("kpi27", KPISource.SONAR.name()),
	/**
	 * Defect count kpi code.
	 */
	DEFECT_COUNT_BY_PRIORITY("kpi28", KPISource.JIRA.name()),
	/**
	 * Defect removal efficiency kpi code.
	 */
	DEFECT_REMOVAL_EFFICIENCY("kpi34", KPISource.JIRA.name()),
	/**
	 * Defect seepage rate kpi code.
	 */
	DEFECT_SEEPAGE_RATE("kpi35", KPISource.JIRA.name()),
	/**
	 * Defect rca kpi code.
	 */
	DEFECT_COUNT_BY_RCA("kpi36", KPISource.JIRA.name()),
	/**
	 * Defect rejection rate kpi code.
	 */
	DEFECT_REJECTION_RATE("kpi37", KPISource.JIRA.name()),
	/**
	 * Sonar violations kpi code.
	 */
	SONAR_VIOLATIONS("kpi38", KPISource.SONAR.name()),
	/**
	 * Sprint velocity kpi code.
	 */
	SPRINT_VELOCITY("kpi39", KPISource.JIRA.name()),
	/**
	 * Story count kpi code.
	 */
	STORY_COUNT("kpi40", KPISource.JIRA.name()),

	/**
	 * Total defect count kpi code.
	 */
	TOTAL_DEFECT_COUNT("kpi41", KPISource.JIRA.name()),
	/**
	 * Regression pass percentage kpi code.
	 */
	REGRESSION_AUTOMATION_COVERAGE("kpi42", KPISource.ZEPHYR.name()),
	/**
	 * Sprint capacity kpi code.
	 */
	SPRINT_CAPACITY_UTILIZATION("kpi46", KPISource.JIRA.name()),
	/**
	 * Throughput kpi code.
	 */
	THROUGHPUT("kpi47", KPISource.JIRA.name()),
	/**
	 * Total ticket count kpi code.
	 */
	NET_OPEN_TICKET_COUNT_BY_STATUS("kpi48", KPISource.JIRAKANBAN.name()),
	/**
	 * Ticket velocity kpi code.
	 */
	TICKET_VELOCITY("kpi49", KPISource.JIRAKANBAN.name()),
	/**
	 * Ticket count by priority kpi code.
	 */
	TICKET_COUNT_BY_PRIORITY("kpi50", KPISource.JIRAKANBAN.name()),
	/**
	 * Ticket rca kpi code.
	 */
	NET_OPEN_TICKET_COUNT_BY_RCA("kpi51", KPISource.JIRAKANBAN.name()),
	/**
	 * Cycle time kpi code.
	 */
	LEAD_TIME_KANBAN("kpi53", KPISource.JIRAKANBAN.name()),
	/**
	 * Ticket open rate kpi code.
	 */
	TICKET_OPEN_VS_CLOSE_BY_PRIORITY("kpi54", KPISource.JIRAKANBAN.name()),
	/**
	 * Story open rate by issue type kpi code.
	 */
	TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE("kpi55", KPISource.JIRAKANBAN.name()),
	/**
	 * Wip vs closed kpi code.
	 */
	WIP_VS_CLOSED("kpi56", KPISource.JIRAKANBAN.name()),
	/**
	 * Ticket throughput kpi code.
	 */
	TICKET_THROUGHPUT("kpi57", KPISource.JIRAKANBAN.name()),
	/**
	 * Team capacity kpi code.
	 */
	TEAM_CAPACITY("kpi58", KPISource.JIRAKANBAN.name()),
	/**
	 * Kanban jira tech debt kpi code.
	 */
	KANBAN_JIRA_TECH_DEBT("kpi60", KPISource.JIRAKANBAN.name()),
	/**
	 * Code quality kanban kpi code.
	 */
	CODE_QUALITY_KANBAN("kpi61", KPISource.SONARKANBAN.name()),
	/**
	 * J unit kanban kpi code.
	 */
	UNIT_TEST_COVERAGE_KANBAN("kpi62", KPISource.SONARKANBAN.name()),
	/**
	 * Kanban regression pass percentage kpi code.
	 */
	KANBAN_REGRESSION_PASS_PERCENTAGE("kpi63", KPISource.ZEPHYRKANBAN.name()),
	/**
	 * Sonar violations kanban kpi code.
	 */
	SONAR_VIOLATIONS_KANBAN("kpi64", KPISource.SONARKANBAN.name()),
	/**
	 * Code commit kanban kpi code.
	 */
	NUMBER_OF_CHECK_INS("kpi65", KPISource.BITBUCKETKANBAN.name()),
	/**
	 * Code build time kanban kpi code.
	 */
	CODE_BUILD_TIME_KANBAN("kpi66", KPISource.JENKINSKANBAN.name()),
	/**
	 * Sonar tech debt kanban kpi code.
	 */
	SONAR_TECH_DEBT_KANBAN("kpi67", KPISource.SONARKANBAN.name()),
	/**
	 * Sprint Productivity JIRA kpi code.
	 */
	SPRINT_PRODUCTIVITY("kpi68", KPISource.JIRA.name()),
	/**
	 * Sprint Wastage JIRA kpi code.
	 */
	SPRINT_WASTAGE("kpi69", KPISource.JIRA.name()),
	/**
	 * Test execution kpi code.
	 */
	TEST_EXECUTION_AND_PASS_PERCENTAGE("kpi70", KPISource.ZEPHYR.name()),
	/**
	 * Test execution kanban kpi code.
	 */
	TEST_EXECUTION_KANBAN("kpi71", KPISource.ZEPHYRKANBAN.name()),
	/**
	 * Sprint commitment reliability
	 */
	COMMITMENT_RELIABILITY("kpi72", KPISource.JIRA.name()),

	/**
	 * Project releases
	 */
	PROJECT_RELEASES("kpi73", KPISource.JIRA.name()),
	/**
	 * Project releases
	 */
	PROJECT_RELEASES_KANBAN("kpi74", KPISource.JIRAKANBAN.name()),
	/**
	 * Estimate vs Actual
	 */
	ESTIMATE_VS_ACTUAL("kpi75", KPISource.JIRA.name()),

	/**
	 * Story completion kpi code.
	 */
	STORIES_WITHOUT_ESTIMATE("kpi81", KPISource.JIRA.name()),

	/**
	 * QA defect density kpi code
	 */
	DEFECT_DENSITY("kpi111", KPISource.JIRA.name()),

	/**
	 * UAT defect density kpi code
	 */
	UAT_DEFECT_DENSITY_RATE("kpi112", KPISource.JIRA.name()),

	/**
	 * Cost Of Delay kpi code
	 */
	COST_OF_DELAY("kpi113", KPISource.JIRA.name()),
	/**
	 * Cost Of Delay kpi code for kanban
	 */
	COST_OF_DELAY_KANBAN("kpi114", KPISource.JIRAKANBAN.name()),

	/**
	 * Missing workLogs kpi code
	 */
	MISSING_WORK_LOGS("kpi115", KPISource.JIRA.name()),
	
	/**
	 * ACTUAL vs Remaining - add hygiene kpiId in HYGIENE_KPI_LIST variable defined in Constant.java class
	 */
	ACTUAL_VS_REMAINING("kpi78", KPISource.JIRA.name()),

	/**
	 * Test Case without Story link
	 */
	TEST_WITHOUT_STORY("kpi79", KPISource.ZEPHYR.name()),
	
	/**
	 * Defects Without Story link
	 */
	DEFECTS_WITHOUT_STORY_LINK("kpi80", KPISource.JIRA.name()),
	/**
	 * First time pass rate
	 */
	FIRST_TIME_PASS_RATE("kpi82", KPISource.JIRA.name()),
	/**
	 * Average Resolution Time
	 */
	AVERAGE_RESOLUTION_TIME("kpi83", KPISource.JIRA.name()),
	/**
	 * Average Resolution Time
	 */
	MEAN_TIME_TO_MERGE("kpi84", KPISource.BITBUCKET.name()),
	/**
	 * Change Failure Rate kpi code
	 */
	CHANGE_FAILURE_RATE("kpi116", KPISource.JENKINS.name()),
	/**
	 * Change Failure Rate kpi code for kanban
	 */
	CHANGE_FAILURE_RATE_KANBAN("kpi117", KPISource.JENKINSKANBAN.name()),
	/**
	 * Deployment Frequency kpi code
	 */
	DEPLOYMENT_FREQUENCY("kpi118", KPISource.JENKINS.name()),
	/**
	 * Open Ticket Aging By Priority kpi code
	 */
	OPEN_TICKET_AGING_BY_PRIORITY("kpi997", KPISource.JIRAKANBAN.name()),
	
	/**
	 * Work Remaining
	 */
	WORK_REMAINING("kpi119", KPISource.JIRA.name()),
	
	/**
	 * Scope Change
	 */
	SCOPE_CHANGE("kpi120", KPISource.JIRA.name()),
	
	/**
	 * Capacity 
	 */
	CAPACITY("kpi121", KPISource.JIRA.name()),
	
	/**
	 * Closure Possible Today 
	 */
	CLOSURE_POSSIBLE_TODAY("kpi122", KPISource.JIRA.name()),
	
	/**
	 * Issue Likely to Spill
	 */
	ISSUE_LIKELY_TO_SPILL("kpi123", KPISource.JIRA.name()),
	/**
	 * Production Defect kpi code
	 */
	PRODUCTION_ISSUES_BY_PRIORITY_AND_AGING("kpi127", KPISource.JIRA.name()),
	/**
	
	/**
	 * Estimation Hygiene
	 */
	ESTIMATION_HYGIENE("kpi124", KPISource.JIRA.name()),
	
	/**
	 * Daily Closures
	 */
	DAILY_CLOSURES("kpi125", KPISource.JIRA.name()),
	
	/**
	 * Created Vs Resolved
	 */
	CREATED_VS_RESOLVED_DEFECTS("kpi126", KPISource.JIRA.name()),
	
	/**
	 * Invalid kpi code.
	 */
	INVALID("INVALID_KPI", "Invalid"),

	/**
	 * Work Completed
	 */
	WORK_COMPLETED("kpi128", KPISource.JIRA.name());

	// @formatter:on

	private String kpiId;

	private String source;

	KPICode(String kpiID, String source) {
		this.kpiId = kpiID;
		this.setSource(source);
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
	 * Gets kpi.
	 *
	 * @param kpiID
	 *            the kpi id
	 * @return the kpi
	 */
	public static KPICode getKPI(String kpiID) {

		return Arrays.asList(KPICode.values()).stream().filter(kpi -> kpi.getKpiId().equalsIgnoreCase(kpiID)).findAny()
				.orElse(INVALID);
	}

	/**
	 * Gets source.
	 *
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Sets source.
	 *
	 * @return the source
	 */
	private void setSource(String source) {
		this.source = source;
	}

}