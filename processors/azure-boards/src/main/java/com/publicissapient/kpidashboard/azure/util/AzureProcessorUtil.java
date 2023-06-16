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

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public final class AzureProcessorUtil {

	private static final String NULL_STR = "null";
	private static final String ENCODED_SPACE = "%20";

	private AzureProcessorUtil() {
		// Default
	}

	/**
	 * This method return UTF-8 decoded string response
	 *
	 * @param azureResponse
	 *            Object of the Azure Response
	 * @return Decoded String
	 */
	public static String deodeUTF8String(Object azureResponse) {
		if (azureResponse == null) {
			return "";
		}
		String responseStr = azureResponse.toString();
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
			formattedDate = new SimpleDateFormat(AzureConstants.DATE_TIME_FORMAT, Locale.US).parse(inputDate);
		} catch (ParseException e) {
			log.error("Error while converting String Date to date object {}  {}", dateString, e);
		}
		return formattedDate;
	}

	/**
	 * Adds parameter to the given url
	 *
	 * @param url
	 *            the url
	 * @param key
	 *            the parameter name
	 * @param value
	 *            the parameter value
	 * @return the updated url as StringBuilder
	 */
	public static StringBuilder addParam(StringBuilder url, String key, String value) {
		if (url.indexOf("?") == -1) {
			url.append("?");
		} else {
			url.append("&");
		}
		url.append(key + "=" + value);
		return url;
	}

	/**
	 * Join URL.
	 *
	 * @param base
	 *            the base
	 * @param paths
	 *            the path
	 * @return the join URL
	 */
	public static String joinURL(String base, String... paths) {
		StringBuilder result = new StringBuilder(base);
		for (String path : paths) {
			if (path != null) {
				String tmpPath = path.replaceFirst("^(\\/)+", "");
				if (result.lastIndexOf("/") != result.length() - 1) {
					result.append('/');
				}
				result.append(tmpPath);
			}
		}
		return result.toString();
	}

	public static String encodeSpaceInUrl(String url) {
		String resultUrl = null;
		if (StringUtils.isNotEmpty(url)) {
			resultUrl = url.trim().replace(" ", ENCODED_SPACE);
		}

		return resultUrl;
	}
}