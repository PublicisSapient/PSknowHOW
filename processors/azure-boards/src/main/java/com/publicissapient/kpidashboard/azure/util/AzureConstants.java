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

package com.publicissapient.kpidashboard.azure.util;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public final class AzureConstants {

	public static final Set<String> ISSUE_FIELD_SET = new HashSet<>();// NOSONAR
	public static final String LABELS = "Labels";
	public static final String CUSTOM_FIELD = "CustomField";
	public static final String ISSUE_TYPE = "IssueType";
	public static final String RCA_CAUSE_NONE = "None";
	public static final String VALUE = "value";
	public static final String CODE_ISSUE = "code issue";
	public static final String OPEN_BRACKET = " ( ";
	public static final String CLOSED_BRACKET = " )";
	public static final String STORY_POINTS = "StoryPoints";
	public static final String SETTING_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String FALSE = "False";
	public static final String COMBINE_IDS_SYMBOL = "_";

	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String JIRA_ISSUE_CHANGE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS";

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String SCRUM_DATA = "scrumDataCount";
	public static final String KANBAN_DATA = "kanbanDataCount";
	public static final String URL = "url";
	public static final String DESCRIPTION = "description";
	public static final String REFERENCENAME = "referenceName";
	public static final String AZURE = "Azure";
	public static final String CATEGORY = "category";
	public static final String STATES = "states";

	public static final String ORDERBY = "order by";
	public static final String CHANGEDDATE = "[system.changeddate]";
	public static final String PRIORITY = "Microsoft.VSTS.Common.Priority";
	public static final String ASSIGNEE = "System.AssignedTo";
	public static final String LABEL = "System.Tags";
	public static final String WHERE = "where";
	public static final String DUE_DATE = "Microsoft.VSTS.Scheduling.DueDate";
	public static final String WORKLOG = "Microsoft.VSTS.Scheduling.CompletedWork";
    public static final String USER = "user";
	public static final String SPACE = " ";

	static {
		ISSUE_FIELD_SET.add("*all,-attachment,-worklog,-comment,-votes,-watches");
	}

	private AzureConstants() {

	}

}
