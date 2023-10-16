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

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public final class JiraProcessorUtil {

	private static final String NULL_STR = "null";

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

}
