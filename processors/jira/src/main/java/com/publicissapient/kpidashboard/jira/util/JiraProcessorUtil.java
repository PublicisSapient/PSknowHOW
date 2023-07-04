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

package com.publicissapient.kpidashboard.jira.util;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.simple.JSONArray;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.util.JsonUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public final class JiraProcessorUtil {

	private static final JiraProcessorUtil INSTANCE = new JiraProcessorUtil();

	// not static because not thread safe
	private static final String SPRINT_SPLIT = "(?=,\\w+=)";
	private static final String NULL_STR = "null";
	private static final String PROJECTKEY = "project";
	private static final String ID = "id";
	private static final String STATE = "state";
	private static final String RAPIDVIEWID = "rapidViewId";
	private static final String NAME = "name";
	private static final String STARTDATE = "startDate";
	private static final String ENDDATE = "endDate";
	private static final String COMPLETEDATE = "completeDate";
	private static final String ACTIVATEDDATE = "activatedDate";
	private static final String GOAL = "goal";
	private static final String BOARDID = "boardId";

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

	/**
	 * Processes Sprint Data
	 *
	 * @param data
	 *            Sprint Data object
	 * @return List of sprints
	 * @throws ParseException
	 *             ParseException
	 * @throws JSONException
	 *             JSONException
	 */
	public static List<SprintDetails> processSprintDetail(Object data) throws ParseException, JSONException {
		List<SprintDetails> sprints = new ArrayList<>();

		if (data instanceof JSONArray) {
			for (Object obj : (JSONArray) data) {
				String dataStr = obj == null ? null : obj.toString();

				SprintDetails sprint = processSingleSprint(dataStr);
				addSprintToList(sprints, sprint);
			}
		} else if (data instanceof org.codehaus.jettison.json.JSONArray) {
			org.codehaus.jettison.json.JSONArray jsonArray = (org.codehaus.jettison.json.JSONArray) data;
			for (int i = 0; i < jsonArray.length(); ++i) {
				Object obj = jsonArray.get(i);
				String dataStr = obj == null ? null : obj.toString();
				SprintDetails sprint = processSingleSprint(dataStr);
				addSprintToList(sprints, sprint);
			}
		}

		return sprints;
	}

	private static void addSprintToList(List<SprintDetails> sprints, SprintDetails sprint) {
		if (sprint != null) {
			sprints.add(sprint);
		}
	}

	/**
	 * Process Single Sprint Data
	 *
	 * @param sprintData
	 *            single sprint data
	 * @return Sprint object
	 */

	public static SprintDetails processSingleSprint(String sprintData) {

		SprintDetails sprint = null;
		if (StringUtils.isNotBlank(sprintData)) {
			sprint = new SprintDetails();
			if (JsonUtils.isValidJSON(sprintData)) {
				setSprintDetailsFromJson(sprintData, sprint);
			} else {
				setSprintDetailsFromString(sprintData, sprint);
			}

		}
		return sprint;
	}

	private static void setSprintDetailsFromString(String sprintData, SprintDetails sprint) {
		sprintData = sprintData.trim().replaceAll("\\s", " ");
		String sprintDataStr = sprintData.substring(sprintData.indexOf('[') + 1, sprintData.length() - 1);
		String[] splitStringList = sprintDataStr.split(SPRINT_SPLIT);

		for (String splitString : splitStringList) {
			int equalIndex = splitString.indexOf('=');

			// just in case logic changes above
			if (equalIndex > 0) {
				String key = splitString.charAt(0) == ',' ? splitString.substring(1, equalIndex)
						: splitString.substring(0, equalIndex);
				String valueAsStr = equalIndex == splitString.length() - 1 ? ""
						: splitString.substring(equalIndex + 1, splitString.length());

				if ("<null>".equalsIgnoreCase(valueAsStr)) {
					valueAsStr = null;
				}
				switch (key) {
				case ID:
					sprint.setOriginalSprintId(valueAsStr);
					sprint.setSprintID(valueAsStr);
					break;
				case STATE:
					sprint.setState(valueAsStr);
					break;
				case RAPIDVIEWID:
					List<String> rapidViewIdList = new ArrayList<>();
					rapidViewIdList.add(valueAsStr);
					sprint.setOriginBoardId(rapidViewIdList);
					break;
				case NAME:
					sprint.setSprintName(valueAsStr);
					break;
				case STARTDATE:
					sprint.setStartDate(getFormattedDateForSprintDetails(valueAsStr));
					break;
				case ENDDATE:
					sprint.setEndDate(getFormattedDateForSprintDetails(valueAsStr));
					break;
				case COMPLETEDATE:
					sprint.setCompleteDate(getFormattedDateForSprintDetails(valueAsStr));
					break;
				case ACTIVATEDDATE:
					sprint.setActivatedDate(getFormattedDateForSprintDetails(valueAsStr));
					break;
				case GOAL:
					sprint.setGoal(valueAsStr);
					break;
				case BOARDID:
					List<String> boardList = new ArrayList<>();
					boardList.add(valueAsStr);
					sprint.setOriginBoardId(boardList);
					break;
				default:
					break;
				}
			}
		}
	}

	private static void setSprintDetailsFromJson(String sprintData, SprintDetails sprint) {
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			JsonNode jsonNode = objectMapper.readTree(sprintData);
			sprint.setSprintID(jsonNode.get(ID) == null ? null : jsonNode.get(ID).asText());
			sprint.setOriginalSprintId(jsonNode.get(ID) == null ? null : jsonNode.get(ID).asText());
			sprint.setState(jsonNode.get(STATE) == null ? null : jsonNode.get(STATE).asText().toUpperCase());
			String boardId = null;

			if (jsonNode.get(RAPIDVIEWID) == null) {
				if (jsonNode.get(BOARDID) != null) {
					boardId = jsonNode.get(BOARDID).asText();
				}
			} else {
				boardId = jsonNode.get(RAPIDVIEWID).asText();
			}
			List<String> boardIdList = new ArrayList<>();
			boardIdList.add(boardId);
			sprint.setOriginBoardId(boardIdList);
			sprint.setSprintName(jsonNode.get(NAME) == null ? null : jsonNode.get(NAME).asText());
			sprint.setStartDate(jsonNode.get(STARTDATE) == null ? null
					: getFormattedDateForSprintDetails(jsonNode.get(STARTDATE).asText()));
			sprint.setEndDate(jsonNode.get(ENDDATE) == null ? null
					: getFormattedDateForSprintDetails(jsonNode.get(ENDDATE).asText()));
			sprint.setCompleteDate(jsonNode.get(COMPLETEDATE) == null ? null
					: getFormattedDateForSprintDetails(jsonNode.get(COMPLETEDATE).asText()));
			sprint.setActivatedDate(jsonNode.get(ACTIVATEDDATE) == null ? null
					: getFormattedDateForSprintDetails(jsonNode.get(ACTIVATEDDATE).asText()));
			sprint.setGoal(jsonNode.get(GOAL) == null ? null : jsonNode.get(GOAL).asText());

		} catch (JsonProcessingException e) {
			log.error("Error in parsing sprint data : " + sprintData, e);
		}
	}

	/**
	 * Gets Formatted date time Object
	 *
	 * @param dateString
	 *            DateString
	 * @return Formatted Date object
	 */
	public static Date getFormattedDateTime(String dateString) {
		String inputDate = dateString;
		Date formattedDate = null;
		if (dateString == null) {
			return null;
		}
		int charIndex = inputDate.indexOf('.');
		if (charIndex != -1) {
			inputDate = inputDate.substring(0, charIndex);
		}
		try {
			formattedDate = new SimpleDateFormat(JiraConstants.DATE_TIME_FORMAT, Locale.US).parse(inputDate);
		} catch (ParseException e) {
			log.error("Error while converting String Date to date object {}  {}", dateString, e);
		}
		return formattedDate;
	}

	/**
	 * Get Date in String formatted
	 *
	 * @param date
	 *            Date Object
	 * @return formatted Date as String
	 */
	public static String getFormattedDateString(Date date) {
		DateFormat df = new SimpleDateFormat(JiraConstants.DATE_TIME_FORMAT, Locale.US);
		return df.format(date);
	}

	/**
	 * Subtracts minutes from given date
	 *
	 * @param date
	 *            Date Object
	 * @param minutes
	 *            Minutes to subtract
	 * @return Adjusted Date
	 */
	public static Date getTimeAdjustedDate(Date date, int minutes) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, -1 * minutes);
		return calendar.getTime();
	}

	public static String createJql(String projectKey, Map<String, String> startDateTimeStrByIssueType) {

		if (StringUtils.isEmpty(projectKey) || startDateTimeStrByIssueType == null) {
			return "";
		}
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("project IN ('");
		stringBuilder.append(projectKey);
		stringBuilder.append("') AND (");

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
	public static String processJql(String query, Map<String, String> startDateTimeStrByIssueType, boolean dataExist) {

		String finalQuery = StringUtils.EMPTY;
		if (StringUtils.isEmpty(query) || startDateTimeStrByIssueType == null) {
			return finalQuery;
		}
		query = query.toLowerCase().split(JiraConstants.ORDERBY)[0];
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
			if (StringUtils.containsIgnoreCase(query, JiraConstants.UPDATEDDATE)) {
				finalQuery = replaceDateQuery(query, issueTypeDateQuery.toString());
			} else {
				finalQuery = appendDateQuery(issueTypeDateQuery.toString(), "AND " + query);
			}
		} else {
			if (StringUtils.containsIgnoreCase(query, JiraConstants.UPDATEDDATE)) {
				finalQuery = appendDateQuery(query, "");
			} else {
				finalQuery = appendDateQuery(issueTypeDateQuery.toString(), "AND " + query);
			}
		}

		return finalQuery;
	}

	/**
	 * append pre and post query
	 *
	 * @param preQuery
	 * @param postQuery
	 * @return appended query
	 */
	private static String appendDateQuery(String preQuery, String postQuery) {
		StringBuilder sb = new StringBuilder();
		sb.append(preQuery);
		sb.append(" ");
		sb.append(postQuery);
		sb.append(" ORDER BY updated DESC");
		return sb.toString();
	}

	/**
	 * find project key from query
	 *
	 * @param query
	 *            jql query
	 * @return string result
	 */
	public static String getProjectKey(String query) {
		String projectKey = null;
		if (StringUtils.containsIgnoreCase(query, PROJECTKEY)) {
			query = query.toLowerCase().split(PROJECTKEY)[1].replaceAll("[^a-zA-Z0-9 ]", "").trim();
			if (query.indexOf(' ') > -1) {
				projectKey = query.substring(0, query.indexOf(' '));
			} else {
				projectKey = query;
			}
		}
		return projectKey;
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
		sb.append(preQuery.replace(JiraConstants.UPDATEDDATE, postQuery));
		sb.append(" ORDER BY updated DESC");
		return sb.toString();
	}

	public static String convertToNewFormat(String dateStr) {
		TimeZone utc = TimeZone.getTimeZone("UTC");
		SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		SimpleDateFormat destFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		sourceFormat.setTimeZone(utc);
		Date convertedDate = null;
		try {
			convertedDate = sourceFormat.parse(dateStr);
		} catch (ParseException e) {
			log.error("Parsing error while converting date format", e);
		}
		return destFormat.format(convertedDate);
	}

	public static String getFormattedDateForSprintDetails(String date) {
		if (date != null && !date.isEmpty()) {
			try {
				DateTime dateTime = ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(date);
				return ISODateTimeFormat.dateHourMinuteSecondMillis().print(dateTime) + "Z";
			} catch (IllegalArgumentException e) {
				log.error("error while parsing date: {} {}", date, e);
			}
		}

		return "";
	}

}
