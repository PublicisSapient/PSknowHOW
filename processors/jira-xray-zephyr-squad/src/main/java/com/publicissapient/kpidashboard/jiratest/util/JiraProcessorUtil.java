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

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import com.publicissapient.kpidashboard.jiratest.model.ProjectConfFieldMapping;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public final class JiraProcessorUtil {

	private static final String NULL_STR = "null";
	public static final String ORDER_BY = "order by";
	public static final String UPDATED_DATE = "updateddate";

	private JiraProcessorUtil() {
		// Default
	}

	/**
	 * This method return UTF-8 decoded string response
	 *
	 * @param jiraResponse
	 *            Object of the Jira Response
	 * @return Decoded String
	 */
	public static String deodeUTF8String(Object jiraResponse) {
		if (jiraResponse == null) {
			return "";
		}
		String responseStr = jiraResponse.toString();
		byte[] responseBytes;
		try {
			CharsetDecoder charsetDecoder = StandardCharsets.UTF_8.newDecoder();
			if (responseStr.isEmpty() || NULL_STR.equalsIgnoreCase(responseStr)) {
				return StringUtils.EMPTY;
			}
			responseBytes = responseStr.getBytes(StandardCharsets.UTF_8);
			charsetDecoder.decode(ByteBuffer.wrap(responseBytes));
			return new String(responseBytes, StandardCharsets.UTF_8);
		} catch (CharacterCodingException e) {
			log.error("error while decoding String using UTF-8 {}  {}", responseStr, e);
			return StringUtils.EMPTY;
		}
	}

	/**
	 * Formats Input date using ISODateTimeFormatter
	 *
	 * @param date
	 *            date to be formatted
	 * @return formatted Date String
	 */
	public static String getFormattedDate(String date) {
		if (date != null && !date.isEmpty()) {
			try {
				DateTime dateTime = ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(date);
				return ISODateTimeFormat.dateHourMinuteSecondMillis().print(dateTime) + "0000";
			} catch (IllegalArgumentException e) {
				log.error("error while parsing date: {} {}", date, e);
			}
		}

		return "";
	}

	public static String createJql(String projectKey, Map<String, String> startDateTimeStrByIssueType) {

		if (StringUtils.isEmpty(projectKey) || startDateTimeStrByIssueType == null) {
			return "";
		}
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("project IN ('");
		stringBuilder.append(projectKey);
//		stringBuilder.append("') AND ");
//		if (Objects.nonNull(projectConfig.getProcessorToolConnection())
//				&& StringUtils.isNotEmpty(projectConfig.getProcessorToolConnection().getBoardQuery())) {
//			stringBuilder.append(projectConfig.getProcessorToolConnection().getBoardQuery());
//			stringBuilder.append(" AND");
//		}
//		stringBuilder.append(" (");
		int size = startDateTimeStrByIssueType.entrySet().size();
		int count = 0;
		for (Map.Entry<String, String> entry : startDateTimeStrByIssueType.entrySet()) {
			count++;
			String type = entry.getKey();
			String dateTime = entry.getValue();

			stringBuilder.append("(issuetype IN ('" + type + "') AND updatedDate>='" + dateTime + "')");
			if (count < size) {
				stringBuilder.append(" OR ");
			}
		}

		stringBuilder.append(") ORDER BY updated DESC");

		return stringBuilder.toString();
	}

	/**
	 * process jql
	 *
	 * @param query
	 *            jqlquery
	 * @param startDateTimeStrByIssueType
	 *            datewise issuetype map
	 * @param dataExist
	 *            data already exist in db or not
	 * @return processed JQL
	 */
	public static String processJql(String query, Map<String, String> startDateTimeStrByIssueType, boolean dataExist,
			ProjectConfFieldMapping projectConfig) {

		String finalQuery = StringUtils.EMPTY;
		if (StringUtils.isEmpty(query) || startDateTimeStrByIssueType == null) {
			return finalQuery;
		}
		query = query.toLowerCase().split(ORDER_BY)[0];
		String[] testCaseTypes = projectConfig.getProcessorToolConnection().getJiraTestCaseType();
		StringBuilder queryWithIssueTypes = new StringBuilder(query);
		for (String testCaseType : testCaseTypes) {
			queryWithIssueTypes.append(" AND issuetype = '").append(testCaseType).append("'");
		}
		StringBuilder issueTypeDateQuery = new StringBuilder();
		int size = startDateTimeStrByIssueType.entrySet().size();
		int count = 0;
		issueTypeDateQuery.append(" (");
		for (Map.Entry<String, String> entry : startDateTimeStrByIssueType.entrySet()) {
			count++;
			String type = entry.getKey();
			String dateTime = entry.getValue();

			issueTypeDateQuery.append("(issuetype IN ('" + type + "') AND updatedDate>='" + dateTime + "')");
			if (count < size) {
				issueTypeDateQuery.append(" OR ");
			}
		}

		issueTypeDateQuery.append(") ");

		if (dataExist) {
			if (StringUtils.containsIgnoreCase(queryWithIssueTypes, UPDATED_DATE)) {
				finalQuery = replaceDateQuery(queryWithIssueTypes.toString(), issueTypeDateQuery.toString());
			} else {
				finalQuery = appendDateQuery(issueTypeDateQuery.toString(), "AND " + queryWithIssueTypes);
			}
		} else {
			if (StringUtils.containsIgnoreCase(queryWithIssueTypes.toString(), UPDATED_DATE)) {
				finalQuery = appendDateQuery(queryWithIssueTypes.toString(), "");
			} else {
				finalQuery = appendDateQuery(issueTypeDateQuery.toString(), "AND " + queryWithIssueTypes);
			}
		}
		return finalQuery;
	}

	/**
	 * replace updated date
	 *
	 * @param preQuery
	 * @param postQuery
	 * @return replaced query
	 */
	private static String replaceDateQuery(String preQuery, String postQuery) {
		StringBuilder sb = new StringBuilder();
		sb.append(preQuery.replace(UPDATED_DATE, postQuery));
		sb.append(" ORDER BY updated DESC");
		return sb.toString();
	}

	private static String appendDateQuery(String preQuery, String postQuery) {
		StringBuilder sb = new StringBuilder();
		sb.append(preQuery);
		sb.append(" ");
		sb.append(postQuery);
		sb.append(" ORDER BY updated DESC");
		return sb.toString();
	}
}
