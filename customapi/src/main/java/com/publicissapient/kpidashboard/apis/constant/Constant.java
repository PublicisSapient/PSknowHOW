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

package com.publicissapient.kpidashboard.apis.constant;

/**
 * specific constants
 *
 * @author tauakram
 */
public final class Constant {

	public static final Short TREND_LIMIT = (short) 15;

	public static final String MIN = "min ";
	public static final String SEC = "sec";

	public static final String TOOL_JIRA = "Jira";
	public static final String TOOL_AZURE = "Azure";
	public static final String TOOL_JENKINS = "Jenkins";
	public static final String TOOL_AZUREPIPELINE = "AzurePipeline";
	public static final String TOOL_SONAR = "Sonar";
	public static final String TOOL_BITBUCKET = "Bitbucket";
	public static final String TOOL_GITLAB = "GitLab";
	public static final String TOOL_AZUREREPO = "AzureRepository";
	public static final String TOOL_TEAMCITY = "Teamcity";
	public static final String TOOL_ZEPHYR = "Zephyr";
	public static final String TOOL_BAMBOO = "Bamboo";
	public static final String TOOL_GITHUB = "GitHub";
	public static final String EXCEL_YES = "Y";

	public static final String MEDIAN = "median";
	public static final String PERCENTILE = "percentile";
	public static final String SUM = "sum";
	public static final String AVERAGE = "average";

	public static final String BLOCKER_VIOLATIONS = "blocker_violations";
	public static final String CRITICAL_VIOLATIONS = "critical_violations";
	public static final String MAJOR_VIOLATIONS = "major_violations";
	public static final String MINOR_VIOLATIONS = "minor_violations";
	public static final String INFO_VIOLATIONS = "info_violations";

	public static final String SPEEDY_ROOT = "Root";
	public static final String PROJECT = "Project";
	public static final String SPRINT = "Sprint";
	public static final String DATE = "date";
	public static final String KANBAN = "Kanban";
	public static final String SCRUM = "Scrum";
	public static final String AGGREGATED_VALUE = "Overall";

	public static final String COVERAGE = "coverage";

	public static final String NOT_AVAILABLE = "NA";

	public static final String AUTOMATED_PERCENTAGE = "automatedPercentage";

	public static final String SPLITTER = "~";

	public static final String EMPTY_STRING = "";
	public static final String ZERO = "0";
	public static final String TILDA_SYMBOL = "^";
	public static final String DOLLAR_SYMBOL = "$";
	public static final String FORWARD_SLASH = "/";
	public static final String BACKWARD_FORWARD_SLASH = "\\\\/";
	public static final String UNDERSCORE = "_";
	public static final String BACKWARD_SLASH_OPEN = "\\\\(";
	public static final String BACKWARD_SLASH_CLOSE = "\\\\)";
	public static final String ROUND_OPEN_BRACKET = "\\(";
	public static final String ROUND_CLOSE_BRACKET = "\\)";

	public static final String DAYS = "Days";
	public static final String PERCENTAGE = "%";
	public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
	public static final Integer DEFAULT_DEFECT_COUNT = Integer.valueOf(0);
	public static final int INCREMENTER_DAY = 1;

	public static final int LAST_X_DAYS_INTERVAL = 7;
	public static final String WASTAGE = "wastage";

	public static final String LOGO_FIL_NAME = "PsKnowHowLogo.png";
	public static final String FALSE = "False";

	public static final String KPI_REQUEST_TRACKER_ID_KEY = "kpiRequestTrackerId";

	public static final Integer DAYS_IN_MONTHS = 30;

	public static final String DEFAULT = "Default";
	public static final String DASH = "-";
	public static final String BLANK = "";

	public static final String ACCESS_REQUEST_STATUS_PENDING = "Pending";
	public static final String ACCESS_REQUEST_STATUS_APPROVED = "Approved";
	public static final String ACCESS_REQUEST_STATUS_REJECTED = "Rejected";
	public static final String LABELS = "Labels";

	public static final String P1 = "P1";
	public static final String P2 = "P2";
	public static final String P3 = "P3";
	public static final String P4 = "P4";
	public static final String P5 = "P5";
	public static final String MISC = "MISC";
	public static final String WHITESPACE = "\\s";

	public static final String CACHE_HIERARCHY_LEVEL = "hierarchyLevelCache";
	public static final String CACHE_KANBAN_HIERARCHY_LEVEL = "kanbanhierarchyLevelCache";

	public static final String CACHE_HIERARCHY_LEVEL_MAP = "hierarchyLevelMap";
	public static final String CACHE_KANBAN_HIERARCHY_LEVEL_MAP = "kanbanhierarchyLevelMap";

	public static final String ROLE_VIEWER = "ROLE_VIEWER";
	public static final String ROLE_GUEST = "ROLE_GUEST";
	public static final String ROLE_PROJECT_VIEWER = "ROLE_PROJECT_VIEWER";
	public static final String ROLE_PROJECT_ADMIN = "ROLE_PROJECT_ADMIN";
	public static final String ROLE_SUPERADMIN = "ROLE_SUPERADMIN";
	public static final String CODE_ISSUE = "code issue";
	public static final String CODE = "code";
	public static final String CODING = "coding";
	public static final String CACHE_ADDITIONAL_FILTER_HIERARCHY_LEVEL = "additionalFilterHierLevel";
	public static final String RESOLUTION_TYPE_FOR_REJECTION = "Resolution Type for Rejection";
	public static final String DEFECT_REJECTION_STATUS = "Defect Rejection Status";
	public static final String RED = "#FF0000";
	public static final String AMBER = "#FFBF00";
	public static final String GREEN = "#00ff00";
	public static final String DOT =".";
	public static final String STAR="*";
	public static final String COUNT = "count";
	public static final String DURATION = "duration";

	public static final String REPO_TOOLS = "RepoTool";

	private Constant() {
	}

}
