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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

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
public class JiraProcessorUtil {

	// not static because not thread safe
	private static final String SPRINT_SPLIT = "(?=,\\w+=)";
	private static final String NULL_STR = "null";
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
			sprint.setState(jsonNode.get(STATE) == null ? null : jsonNode.get(STATE).asText());
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

	public static String processJqlForSprintFetch(List<String> issueKeys) {
		String finalQuery = org.apache.commons.lang3.StringUtils.EMPTY;
		if (issueKeys == null) {
			return finalQuery;
		}
		StringBuilder issueKeysDataQuery = new StringBuilder();

		int size = issueKeys.size();
		int count = 0;
		issueKeysDataQuery.append("issueKey in (");

		for (String issueKey : issueKeys) {
			count++;
			issueKeysDataQuery.append(issueKey);
			if (count < size) {
				issueKeysDataQuery.append(", ");
			}
		}
		issueKeysDataQuery.append(")");

		finalQuery = issueKeysDataQuery.toString();

		return finalQuery;
	}
}
