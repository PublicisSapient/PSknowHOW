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
	public static final String FIRST_DEV_STATUS = "firstDevstatus";
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
	public static final String FUTURE_SPRINTS = "Future Sprints";

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

	public static final String CACHE_AGG_CIRCLE_CRITERIA = "cache_aggregation_circle_criteria";
	public static final String CACHE_MATURITY_RANGE = "cache_maturity_range";

	public static final String COMPLETED_ISSUES = "completedIssues";
	public static final String TOTAL_ISSUES = "totalIssues";
	public static final String ADDED_ISSUES = "addedIssues";
	public static final String ADDED = "Added";
	public static final String REMOVED = "Removed";
	public static final String PUNTED_ISSUES = "puntedIssues";
	public static final String COMPLETED_ISSUES_ANOTHER_SPRINT = "issuesCompletedInAnotherSprint";
	public static final String NOT_COMPLETED_ISSUES = "issuesNotCompletedInCurrentSprint";

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
	public static final String SUBTASK_DATA = "collectSubtaskData";
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
	public static final String DEV_DUE_DATE = "Dev Due Date";

	public static final String REJECTED = "Rejected";
	public static final String ITERATION = "Iteration";
	public static final String RELEASE = "Release";
	public static final String BACKLOG = "Backlog";

	public static final String RELEASED = "Released";
	public static final String UNRELEASED = "Unreleased";
	public static final String REPO_TOOLS = "RepoTool";
    public static final String CACHE_FIELD_MAPPING_STUCTURE = "cache_field_mapping_stucture";
	public static final String CACHE_PROJECT_TOOL_CONFIG= "cache_project_tool_config";
	public static final String CUSTOM_TEMPLATE_CODE_SCRUM="10";
	public static final String CUSTOM_TEMPLATE_CODE_KANBAN="9";
	public static final String EPICLINK = "epicLink";

	public static final String SOLID_LINE_TYPE = "solid";

	public static final String DOTTED_LINE_TYPE = "dotted";

	public static final String JIRADEFECTTYPE="jiradefecttype";
	public static final String JIRAKPI135STORYIDENTIFICATION="jiraKPI135StoryIdentification";//azure
	public static final String JIRAKPI82STORYIDENTIFICATION="jiraKPI82StoryIdentification";//azure
	public static final String JIRAISSUETYPEKPI3="jiraIssueTypeKPI3";
	public static final String JIRAISSUETYPENAMES="jiraIssueTypeNames";
	public static final String JIRAISSUEEPICTYPE="jiraIssueEpicType";
	public static final String JIRADEFECTINJECTIONISSUETYPEKPI14="jiraDefectInjectionIssueTypeKPI14";
	public static final String JIRADODKPI14="jiraDodKPI14";
	public static final String JIRADODQAKPI111="jiraDodQAKPI111";
	public static final String JIRADODKPI3="jiraDodKPI3";
	public static final String JIRADODKPI127="jiraDodKPI127";
	public static final String JIRADODKPI152="jiraDodKPI152";
	public static final String JIRADODKPI151="jiraDodKPI151";
	public static final String JIRADODKPI37="jiraDodKPI37";
	public static final String JIRASTARTDEVKPI54="jiraStatusStartDevelopmentKPI154";
	public static final String JIRADODKPI155="jiraDodKPI155";
	public static final String JIRADODKPI163="jiraDodKPI163";
	public static final String JIRAISSUETYPEKPI35="jiraIssueTypeKPI35";
	public static final String JIRADEFECTREMOVALISSUETYPEKPI34="jiraDefectRemovalIssueTypeKPI34";
	public static final String JIRATESTAUTOMATIONISSUETYPE="jiraTestAutomationIssueType";
	public static final String JIRASPRINTVELOCITYISSUETYPEKPI138="jiraSprintVelocityIssueTypeKPI138";
	public static final String JIRASPRINTCAPACITYISSUETYPEKPI46="jiraSprintCapacityIssueTypeKpi46";
	public static final String JIRAISSUETYPEKPI37="jiraIssueTypeKPI37";
	public static final String JIRADEFECTCOUNTLISSUETYPEKPI28="jiraDefectCountlIssueTypeKPI28";
	public static final String JIRADEFECTCOUNTLISSUETYPEKPI36="jiraDefectCountlIssueTypeKPI36";
	public static final String JIRAQAKPI111ISSUETYPE="jiraQAKPI111IssueType";
	public static final String JIRASTORYIDENTIFICATIONKPI129="jiraStoryIdentificationKPI129";
	public static final String JIRASTORYIDENTIFICATIONKPI166="jiraStoryIdentificationKPI166";
	public static final String JIRASTORYIDENTIFICATIONKPI40="jiraStoryIdentificationKpi40";
	public static final String JIRA_STORY_IDENTIFICATION_KPI164="jiraStoryIdentificationKPI164";

	public static final String STORYFIRSTSTATUSKPI148="storyFirstStatusKPI148";
	public static final String STORYFIRSTSTATUSKPI154="storyFirstStatusKPI154";
	public static final String STORYFIRSTSTATUSKPI3="storyFirstStatusKPI3";
	public static final String JIRASTATUSFORQAKPI148="jiraStatusForQaKPI148";
	public static final String JIRASTATUSFORQAKPI135="jiraStatusForQaKPI135";
	public static final String JIRASTATUSFORQAKPI82="jiraStatusForQaKPI82";
	public static final String JIRASTATUSFORDEVELOPMENTKPI82="jiraStatusForDevelopmentKPI82";
	public static final String JIRASTATUSFORDEVELOPMENTKPI135="jiraStatusForDevelopmentKPI135";
	public static final String JIRADEFECTCREATEDSTATUSKPI14="jiraDefectCreatedStatusKPI14";
	public static final String JIRADEFECTREJECTIONSTATUSKPI152="jiraDefectRejectionStatusKPI152";
	public static final String JIRADEFECTREJECTIONSTATUSKPI151="jiraDefectRejectionStatusKPI151";
	public static final String JIRADEFECTREJECTIONSTATUSKPI28="jiraDefectRejectionStatusKPI28";
	public static final String JIRADEFECTREJECTIONSTATUSKPI34="jiraDefectRejectionStatusKPI34";
	public static final String JIRADEFECTREJECTIONSTATUSKPI37="jiraDefectRejectionStatusKPI37";
	public static final String JIRADEFECTREJECTIONSTATUSKPI35="jiraDefectRejectionStatusKPI35";
	public static final String JIRADEFECTREJECTIONSTATUSKPI82="jiraDefectRejectionStatusKPI82";
	public static final String JIRADEFECTREJECTIONSTATUSKPI135="jiraDefectRejectionStatusKPI135";
	public static final String JIRADEFECTREJECTIONSTATUSKPI133="jiraDefectRejectionStatusKPI133";
	public static final String JIRADEFECTREJECTIONSTATUSRCAKPI36="jiraDefectRejectionStatusRCAKPI36";
	public static final String JIRADEFECTREJECTIONSTATUSKPI14="jiraDefectRejectionStatusKPI14";
	public static final String JIRADEFECTREJECTIONSTATUSQAKPI111="jiraDefectRejectionStatusQAKPI111";
	public static final String JIRADEFECTREMOVALSTATUSKPI34="jiraDefectRemovalStatusKPI34";
	public static final String JIRADEFECTCLOSEDSTATUSKPI137="jiraDefectClosedStatusKPI137";
	public static final String JIRAISSUEDELIVERDSTATUSKPI138="jiraIssueDeliverdStatusKPI138";
	public static final String JIRAISSUEDELIVERDSTATUSKPI126="jiraIssueDeliverdStatusKPI126";
	public static final String JIRAISSUEDELIVERDSTATUSKPI82="jiraIssueDeliverdStatusKPI82";
	public static final String JIRADORKPI3="jiraDorKPI3";
	public static final String JIRALIVESTATUSKPI3="jiraLiveStatusKPI3";
	public static final String JIRALIVESTATUSKPI127="jiraLiveStatusKPI127";
	public static final String JIRALIVESTATUSKPI152="jiraLiveStatusKPI152";
	public static final String JIRALIVESTATUSKPI151="jiraLiveStatusKPI151";

	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI135="jiraIterationCompletionStatusKPI135";//azure all below
	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI122="jiraIterationCompletionStatusKPI122";
	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI75="jiraIterationCompletionStatusKPI75";
	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI145="jiraIterationCompletionStatusKPI145";
	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI140="jiraIterationCompletionStatusKPI140";
	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI132="jiraIterationCompletionStatusKPI132";
	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI136="jiraIterationCompletionStatusKPI136";
	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI72="jiraIterationCompletionStatusKpi72";
	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI39="jiraIterationCompletionStatusKpi39";
	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI5="jiraIterationCompletionStatusKpi5";
	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI124="jiraIterationCompletionStatusKPI124";
	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI123="jiraIterationCompletionStatusKPI123";
	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI125="jiraIterationCompletionStatusKPI125";
	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI120="jiraIterationCompletionStatusKPI120";
	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI128="jiraIterationCompletionStatusKPI128";
	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI134="jiraIterationCompletionStatusKPI134";
	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI133="jiraIterationCompletionStatusKPI133";
	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI131="jiraIterationCompletionStatusKPI131";
	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI138="jiraIterationCompletionStatusKPI138";
	public static final String JIRAITERATIONCOMPLETIONSTATUSKPI119="jiraIterationCompletionStatusKPI119";
	public static final String JIRAITERATIONCOMPLETIONSTATUSCUSTOMFIELD="jiraIterationCompletionStatusCustomField";
	public static final String RESOLUTIONTYPEFORREJECTIONKPI28="resolutionTypeForRejectionKPI28";
	public static final String RESOLUTIONTYPEFORREJECTIONKPI34="resolutionTypeForRejectionKPI34";
	public static final String RESOLUTIONTYPEFORREJECTIONKPI37="resolutionTypeForRejectionKPI37";
	public static final String RESOLUTIONTYPEFORREJECTIONKPI35="resolutionTypeForRejectionKPI35";
	public static final String RESOLUTIONTYPEFORREJECTIONKPI135="resolutionTypeForRejectionKPI135";
	public static final String RESOLUTIONTYPEFORREJECTIONKPI82="resolutionTypeForRejectionKPI82";
	public static final String RESOLUTIONTYPEFORREJECTIONKPI133="resolutionTypeForRejectionKPI133";
	public static final String RESOLUTIONTYPEFORREJECTIONRCAKPI36="resolutionTypeForRejectionRCAKPI36";
	public static final String RESOLUTIONTYPEFORREJECTIONQAKPI111="resolutionTypeForRejectionQAKPI111";
	public static final String RESOLUTIONTYPEFORREJECTIONKPI14="resolutionTypeForRejectionKPI14";
	public static final String ISSUESTATUSEXCLUMISSINGWORKKPI124="issueStatusExcluMissingWorkKPI124";
	public static final String JIRADEFECTDROPPEDSTATUSKPI127="jiraDefectDroppedStatusKPI127";
	public static final String JIRABLOCKEDSTATUSKPI131="jiraBlockedStatusKPI131";
	public static final String JIRAWAITSTATUSKPI131="jiraWaitStatusKPI131";
	public static final String JIRASTATUSFORINPROGRESSKPI148="jiraStatusForInProgressKPI148";
	public static final String JIRASTATUSFORINPROGRESSKPI122="jiraStatusForInProgressKPI122";
	public static final String JIRASTATUSFORINPROGRESSKPI145="jiraStatusForInProgressKPI145";
	public static final String JIRASTATUSFORINPROGRESSKPI125="jiraStatusForInProgressKPI125";
	public static final String JIRASTATUSFORINPROGRESSKPI128="jiraStatusForInProgressKPI128";
	public static final String JIRASTATUSFORINPROGRESSKPI123="jiraStatusForInProgressKPI123";
	public static final String JIRASTATUSFORINPROGRESSKPI119="jiraStatusForInProgressKPI119";
	public static final String JIRASTATUSFORINPROGRESSKPI154="jiraStatusForInProgressKPI154";

	public static final String JIRASTORYPOINTSCUSTOMFIELD="jiraStoryPointsCustomField";
	public static final String EPICCOSTOFDELAY="epicCostOfDelay";
	public static final String EPICRISKREDUCTION="epicRiskReduction";
	public static final String EPICUSERBUSINESSVALUE="epicUserBusinessValue";
	public static final String EPICWSJF="epicWsjf";
	public static final String EPICTIMECRITICALITY="epicTimeCriticality";
	public static final String EPICJOBSIZE="epicJobSize";
	public static final String SPRINTNAME="sprintName";
	public static final String JIRATECHDEBTISSUETYPE="jiraTechDebtIssueType";
	public static final String ACTIVE = "ACTIVE";

	public static final String JIRA = "Jira";

	public static final String REPO = "Repo";
	public static final String JIRADODKPI166="jiraDodKPI166";

	private CommonConstant() {

	}
}
