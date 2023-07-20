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

package com.publicissapient.kpidashboard.common.constant;

/**
 * Defines the constants that can be used across modules.
 *
 */
public final class CommonConstant {

	public static final String JIRA_KPI_CACHE = "jiraKpiCache";
	public static final String AZURE_KPI_CACHE = "azureKpiCache";
	public static final String SONAR_KPI_CACHE = "sonarKpiCache";
	public static final String BITBUCKET_KPI_CACHE = "bitbucketKpiCache";
	public static final String GITLAB_KPI_CACHE = "gitLabKpiCache";
	public static final String JENKINS_KPI_CACHE = "jenkinsKpiCache";
	public static final String TESTING_KPI_CACHE = "testingKpiCache";
	public static final String JIRAKANBAN_KPI_CACHE = "jiraKanbanKpiCache";
	public static final String CACHE_ACCOUNT_HIERARCHY = "accountHierarchy";
	public static final String CACHE_ACCOUNT_HIERARCHY_KANBAN = "accountHierarchyKanban";
	public static final String CACHE_TOOL_CONFIG_MAP = "toolItemMap";
	public static final String CACHE_FIELD_MAPPING_MAP = "fieldMappingMap";
	public static final String CACHE_PROJECT_CONFIG_MAP = "projectConfigMap";
	public static final String CACHE_PROJECT_TOOL_CONFIG_MAP = "projectToolConfigMap";

	public static final String CACHE_CLEAR_ENDPOINT = "api/cache/clearCache";
	public static final String CLEAR_ALL_CACHE_ENDPOINT = "api/cache/clearAllCache";
	public static final String REPROCESS = "Reprocess";
	public static final String FILE_STATUS_UPLOADED = "Uploaded";

	public static final String SPRINT = "sprint";
	public static final String BUG = "bug";
	public static final String ISSUE_TYPE = "issuetype";
	public static final String TICKET_COUNT_ISSUE_TYPE = "ticketCountIssueType";

	public static final String FIRST_STATUS = "firststatus";
	public static final String OPEN = "Open";
	public static final String CLOSED = "Closed";
	public static final String FUTURE = "FUTURE";
	public static final String ROOT_CAUSE = "rootcause";
	public static final String DEVELOPMENT = "development";
	public static final String QA = "qa";
	public static final String STORY = "story";
	public static final String DOR = "dor";
	public static final String DOD = "dod";
	public static final String REJECTION = "rejection";
	public static final String DELIVERED = "delivered";
	public static final String STORYPOINT = "storypoint";
	public static final String ROOT_CAUSE_VALUE = "rootCauseValue";
	public static final String REJECTION_RESOLUTION = "rejectionResolution";
	public static final String QA_ROOT_CAUSE = "qaRootCause";
	public static final String UAT_DEFECT = "uatdefect";

	public static final String TICKET_CLOSED_STATUS = "ticketClosedStatus";
	public static final String TICKET_LIVE_STATUS = "ticketLiveStatus";
	public static final String JIRA_LIVE_STATUS = "jiraLiveStatus";
	public static final String JIRA_STATUS_FOR_CLOSED = "jiraStatusForClosed";
	public static final String TICKET_RESOLVED_STATUS = "ticketResolvedStatus";
	public static final String TICKET_REOPEN_STATUS = "ticketReopenStatus";
	public static final String TICKET_TRIAGED_STATUS = "ticketTriagedStatus";
	public static final String TICKET_WIP_STATUS = "ticketWIPStatus";
	public static final String TICKET_REJECTED_STATUS = "ticketRejectedStatus";
	public static final String TICKET_VELOCITY_ISSUE_TYPE = "ticketVelocityStatusIssue";
	public static final String TICKET_THROUGHPUT_ISSUE_TYPE = "ticketThroughputIssue";
	public static final String TICKET_WIP_CLOSED_ISSUE_TYPE = "ticketWipClosedIssue";
	public static final String TICKET_REOPEN_ISSUE_TYPE = "ticketReopenIssue";
	public static final String KANBAN_CYCLE_TIME_ISSUE_TYPE = "kanbanCycleTimeIssue";
	public static final String KANBAN_TECH_DEBT_ISSUE_TYPE = "kanbanTechDebtIssueType";

	public static final String META_ISSUE_TYPE = "Issue_Type";
	public static final String META_WORKFLOW = "workflow";
	public static final String META_FIELD = "fields";

	public static final String ACCOUNT = "Account";
	public static final String PROJECT = "Project";
	public static final String UNDERSCORE = "_";
	public static final String COST_OF_DELAY = "costOfDelay";
	public static final String USER_BUSINESS_VALUE = "businessValue";
	public static final String RISK_REDUCTION = "riskReduction";
	public static final String JOB_SIZE = "jobSize";
	public static final String WSJF = "wsjf";
	public static final String TIME_CRITICALITY = "timeCriticality";
	public static final String EPIC = "epic";

	public static final String ENG_MATURITY = "ENG_MATURITY";
	public static final String ENG_MATURITY_MASTER = "ENG_MATURITY_MASTER";

	public static final String ARROW = "->";
	public static final String NEWLINE = "\n";
	public static final String ACC_HIERARCHY_PATH_SPLITTER = "###";
	public static final String BLANK = "";
	public static final String COMMA = ",";
	public static final String CACHE_KPI_MASTER = "cache_kpi_master";
	public static final String CACHE_HIERARCHY_LEVEL_VALUE = "cache_hierarchy_level_value";
	public static final String CACHE_PROJECT_BASIC_TREE = "cache_project_basic_tree";
	public static final String CACHE_USER_BOARD_CONFIG = "cache_user_board_config";

	public static final String OVERALL = "Overall";

	public static final String WEEK = "WEEKS";
	public static final String MONTH = "MONTHS";
	public static final String DAYS = "DAYS";
	public static final String DAY = "day";
	public static final String date = "date";

	public static final String HIERARCHY_LEVEL_ID_PROJECT = "project";
	public static final String HIERARCHY_LEVEL_NAME_PROJECT = "Project";
	public static final String HIERARCHY_LEVEL_ID_SPRINT = "sprint";
	public static final String HIERARCHY_LEVEL_NAME_SPRINT = "Sprint";
	public static final String HIERARCHY_LEVEL_ID_RELEASE = "release";
	public static final String HIERARCHY_LEVEL_NAME_RELEASE = "Release";

	public static final String ADDITIONAL_FILTER_VALUE_ID_SEPARATOR = "_";

	public static final String LABELS = "Labels";
	public static final String COMPONENT = "Component";
	public static final String CUSTOM_FIELD = "CustomField";
	public static final String CACHE_AGG_CRITERIA = "cache_aggregation_criteria";
	public static final String CACHE_MATURITY_RANGE = "cache_maturity_range";

	public static final String COMPLETED_ISSUES = "completedIssues";
	public static final String TOTAL_ISSUES = "totalIssues";
	public static final String ADDED_ISSUES = "addedIssues";
	public static final String ADDEDINBTWN_ISSUES = "addedIssues";
	public static final String PUNTED_ISSUES = "puntedIssues";
	public static final String COMPLETED_ISSUES_ANOTHER_SPRINT = "issuesCompletedInAnotherSprint";
	public static final String NOT_COMPLETED_ISSUES = "issuesNotCompletedInCurrentSprint";
	public static final String CACHE_KPI_FIELD_MAPPING = "cache_kpi_field_mapping";

	public static final String PSLOGDATA = "PSLogData";
	public static final String REQUESTID = "requestId";
	public static final String USER_NAME = "userName";
	public static final String ENVIRONMENT = "environment";
	public static final String PROJECTNAME = "projectName";
	public static final String PROJECT_CONFIG_ID = "projectBasicConfigId";
	public static final String FETCHING_ISSUE = "fetchingIssue";
	public static final String SPRINT_REPORTDATA = "collectSprintReport";
	public static final String SAVED_ISSUES = "savedIssues";
	public static final String SAVED_EPIC_ISSUES = "saveEpicIssues";
	public static final String PROJECT_EXECUTION_STATUS = "projectExecutionStatus";
	public static final String SPRINT_DATA = "collectSprintData";
	public static final String RELEASE_DATA = "collectReleaseData";
	public static final String JIRAISSUE_DATA = "collectJiraIssueData";
	public static final String EPIC_DATA = "collectEpicData";
	public static final String METADATA = "collectMetaData";
	public static final String PROJECT_RUN = "projectRun";
	public static final String CRON = "cron";
	public static final String JIRA_WAIT_STATUS = "jiraWaitStatus";
	public static final String JIRA_BLOCKED_STATUS = "jiraBlockedStatus";

	public static final String IS_FLAG_STATUS_INCLUDED_FOR_WASTAGE = "Include Flagged Issue";
	public static final String FLAG_STATUS_FOR_SERVER = "Requires attention";
	public static final String FLAG_STATUS_FOR_CLOUD = "Impediment";
	public static final String BLOCKED_STATUS_WASTAGE = "Blocked Status";
	public static final String JIRA_IN_PROGRESS_STATUS = "jiraStatusForInProgress";

	public static final String STORY_POINT = "Story Point";
	public static final String ACTUAL_ESTIMATION = "Actual Estimation";
	public static final String SP = "SP";
	public static final String HOURS = "Hours";
	public static final String INITIALHOURS = "InitialHours";
	public static final String ORIGINAL_ESTIMATE = "Original Estimate";
	public static final String DUE_DATE = "Due Date";

	public static final String REJECTED = "Rejected";
	public static final String ITERATION = "Iteration";
	public static final String RELEASE = "Release";

	public static final String RELEASED = "Released";
	public static final String UNRELEASED = "Unreleased";

	private CommonConstant() {

	}
}
