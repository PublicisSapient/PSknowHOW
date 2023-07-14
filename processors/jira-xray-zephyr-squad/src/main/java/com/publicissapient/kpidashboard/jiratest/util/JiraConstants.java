/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2020 Sapient Limited.
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

package com.publicissapient.kpidashboard.jiratest.util;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public final class JiraConstants {

	public static final Set<String> ISSUE_FIELD_SET = new HashSet<>(); // NO SONAR
	public static final String LABELS = "Labels";
	public static final String CUSTOM_FIELD = "CustomField";
	public static final String TEST_AUTOMATED = "Automated";
	public static final String YES = "Yes";
	public static final String VALUE = "value";
	public static final String SETTING_TEST_CASE_START_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS";
	public static final String TEST_CASE_CHANGE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS";
	public static final String START_AT_ATTRIBUTE = "startAt";
	public static final String MAX_RESULTS_ATTRIBUTE = "maxResults";
	public static final int MAX_JQL_LENGTH_FOR_HTTP_GET = 3000;
	public static final String JQL_ATTRIBUTE = "jql";
	public static final String FILTER_FAVOURITE_PATH = "filter/favourite";
	public static final String FILTER_PATH_FORMAT = "filter/%s";
	public static final String SEARCH_URI_PREFIX = "search";
	public static final String EXPAND_ATTRIBUTE = "expand";
	public static final String FIELDS_ATTRIBUTE = "fields";

	public static final String AUTOMATION = "Automation";
	public static final String CAN_BE_AUTOMATED = "Manual";

	static {
		ISSUE_FIELD_SET.add("*all,-attachment,-worklog,-comment,-votes,-watches");
	}

}
