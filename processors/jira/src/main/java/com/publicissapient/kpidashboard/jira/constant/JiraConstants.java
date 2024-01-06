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

package com.publicissapient.kpidashboard.jira.constant;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public final class JiraConstants {

	public static final Set<String> ISSUE_FIELD_SET = new HashSet<>();// NOSONAR
	public static final String STATUS = "status";
	public static final String ASSIGNEE = "assignee";
	public static final String PRIORITY = "priority";
	public static final String FIXVERSION = "fix version";
	public static final String DUEDATE = "duedate";
	public static final String LABELS = "Labels";
	public static final String CUSTOM_FIELD = "CustomField";
	public static final String ISSUE_TYPE = "IssueType";
	public static final String RCA_CAUSE_NONE = "None";
	public static final String RCA_NOT_AVAILABLE = "RCA Not Available";
	public static final String ISSUE_TYPE_DEFECT = "Defect";
	public static final String TEST_AUTOMATED = "Automated";
	public static final String YES = "Yes";
	public static final String VALUE = "value";
	public static final String CODE_ISSUE = "code issue";
	public static final String ACTUAL_ESTIMATION = "Actual Estimation";
	public static final String BUFFERED_ESTIMATION = "Buffered Estimation";
	public static final String STORY_POINT = "Story Point";
	public static final String JIRA_ISSUE_CHANGE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS";
	public static final String EMPTY_STR = "";
	public static final String FALSE = "False";
	public static final String COMBINE_IDS_SYMBOL = "_";
	public static final String START_AT_ATTRIBUTE = "startAt";
	public static final String MAX_RESULTS_ATTRIBUTE = "maxResults";
	public static final int MAX_JQL_LENGTH_FOR_HTTP_GET = 3000;
	public static final String JQL_ATTRIBUTE = "jql";
	public static final String FILTER_FAVOURITE_PATH = "filter/favourite";
	public static final String FILTER_PATH_FORMAT = "filter/%s";
	public static final String SEARCH_URI_PREFIX = "search";
	public static final String EXPAND_ATTRIBUTE = "expand";
	public static final String FIELDS_ATTRIBUTE = "fields";
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String AGGREGATED_TIME_SPENT = "aggregatetimespent";
	public static final String AGGREGATED_TIME_ORIGINAL = "aggregatetimeoriginalestimate";
	public static final String AGGREGATED_TIME_REMAIN = "aggregatetimeestimate";
	public static final String ID = "id";
	public static final String COMPONENT = "Component";
	public static final String JIRA = "Jira";
	public static final String ORDERBY = "order by";

	public static final String PARENT = "parent";
	public static final String KEY = "key";
	public static final String USER = "User";
	public static final String SPACE = " ";
	public static final String EPIC = "Epic";
	public static final String WORKLOG = "timespent";
	public static final String FLAG_STATUS_FOR_SERVER = "Flag as Impediment";
	public static final String FLAG_STATUS_FOR_CLOUD = "Flagged";
	public static final String QUERYDATEFORMAT = "yyyy-MM-dd HH:mm";
	public static final String TO_DO = "To Do";
	public static final String DONE = "Done";
	public static final String ERROR_MSG_401 = "Error 401 connecting to JIRA server, your credentials are probably wrong. Note: Ensure you are using JIRA user name not your email address.";
	public static final String ERROR_MSG_NO_RESULT_WAS_AVAILABLE = "No result was available from Jira unexpectedly - defaulting to blank response. The reason for this fault is the following : {}";

	static {
		ISSUE_FIELD_SET.add("*all,-attachment,-worklog,-comment,-votes,-watches");
	}

}
