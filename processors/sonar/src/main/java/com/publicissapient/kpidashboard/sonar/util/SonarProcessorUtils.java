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

package com.publicissapient.kpidashboard.sonar.util;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import com.publicissapient.kpidashboard.common.constant.SonarMetricStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides utility methods for Sonar Processor.
 *
 */
@Slf4j
public final class SonarProcessorUtils {

	private static final String FORMATTED_DATE = "yyyy-MM-dd'T'HH:mm:ssZ";
	private static final String CODE_QUALITY_STATUS_WARN = "WARN";
	private static final String CODE_QUALITY_STATUS_ALERT = "ALERT";
	private static final int HOURS_IN_DAY = 8;
	private static final String AUTHORIZATION = "Authorization";

	private SonarProcessorUtils() {
	}

	/**
	 * Provides JSONArray after parsing data.
	 * 
	 * @param url
	 *            the url
	 * @param restOp
	 *            the rest operation
	 * @param httpHeaders
	 *            the http header
	 * @return the json array
	 * @throws ParseException
	 *             if the given string doesn’t meet format
	 */
	public static JSONArray parseData(String url, RestOperations restOp, HttpEntity<String> httpHeaders)
			throws ParseException {
		ResponseEntity<String> response = restOp.exchange(url, HttpMethod.GET, httpHeaders, String.class);
		return (JSONArray) new JSONParser().parse(response.getBody());
	}

	/**
	 * Parses response body as JSON array.
	 * 
	 * @param url
	 *            the url
	 * @param restOp
	 *            the rest operation
	 * @param key
	 *            the project key
	 * @param httpHeaders
	 *            the http header
	 * @return JSONArray the JSON array
	 * @throws ParseException
	 *             if the given string doesn’t meet format
	 */
	public static JSONArray parseData(String url, RestOperations restOp, String key, HttpEntity<String> httpHeaders)
			throws ParseException {
		ResponseEntity<String> response = restOp.exchange(url, HttpMethod.GET, httpHeaders, String.class);
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(response.getBody());
		log.debug(url);
		return (JSONArray) jsonObject.get(key);
	}

	/**
	 * Provides timestamp.
	 * 
	 * @param jsonData
	 *            the json object data
	 * @param key
	 *            the project key
	 * @return the timestamp
	 */
	public static long getTimestamp(JSONObject jsonData, String key) {
		Object jsonObj = jsonData.get(key);
		if (jsonObj != null) {
			try {
				return new SimpleDateFormat(FORMATTED_DATE, Locale.US).parse(jsonObj.toString()).getTime();
			} catch (java.text.ParseException ex) {
				log.error("{} is not in expected format: {} ", jsonObj, FORMATTED_DATE, ex);
			}
		}
		return 0;
	}

	/**
	 * Converts json object to string data.
	 * 
	 * @param jsonData
	 *            the json object
	 * @param key
	 *            the project key
	 * @return the string data
	 */
	public static String convertToString(JSONObject jsonData, String key) {
		Object jsonObj = jsonData.get(key);
		return jsonObj == null ? null : jsonObj.toString();
	}

	/**
	 * Converts json object to string data safely.
	 * 
	 * @param jsonData
	 *            the json object
	 * @param key
	 *            the project key
	 * @return the string data
	 */
	public static String convertToStringSafe(JSONObject jsonData, String key) {
		Object jsonObj = jsonData.get(key);
		return jsonObj == null ? "" : jsonObj.toString();
	}

	/**
	 * Creates HTTP Headers.
	 * 
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 * @return HttpHeaders the http header
	 */
	public static HttpHeaders getHeaders(String username, String password) {
		HttpHeaders header = new HttpHeaders();
		if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
			String authentication = username + ":" + password;
			byte[] encodedAuth = Base64.encodeBase64(authentication.getBytes(StandardCharsets.US_ASCII));
			String authenticationHeader = "Basic " + new String(encodedAuth);
			header.set(AUTHORIZATION, authenticationHeader);
		}
		return header;
	}

	public static HttpHeaders getHeaders(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		if (accessToken != null && !accessToken.isEmpty()) {
			headers.add(AUTHORIZATION, "Bearer " + accessToken);
		}
		return headers;
	}

	public static HttpHeaders getHeaders(String accessToken, boolean usingBasicAuth) {
		HttpHeaders headers = new HttpHeaders();
		if (accessToken != null && !accessToken.isEmpty()) {
			if (usingBasicAuth) {
				String authentication = accessToken + ":";
				byte[] encodedAuth = Base64.encodeBase64(authentication.getBytes(StandardCharsets.US_ASCII));
				String authenticationHeader = "Basic " + new String(encodedAuth);
				headers.set(AUTHORIZATION, authenticationHeader);
			} else {
				headers.add(AUTHORIZATION, "Bearer " + accessToken);
			}
		}
		return headers;
	}

	/**
	 * Provides code quality metrics status.
	 * 
	 * @param metricStatus
	 *            the code quality status
	 * @return the code quality metric status
	 */
	public static SonarMetricStatus getMetricStatus(String metricStatus) {
		if (StringUtils.isBlank(metricStatus)) {
			return SonarMetricStatus.OK;
		}
		switch (metricStatus) {
		case CODE_QUALITY_STATUS_WARN:
			return SonarMetricStatus.WARNING;
		case CODE_QUALITY_STATUS_ALERT:
			return SonarMetricStatus.ALERT;
		default:
			return SonarMetricStatus.OK;
		}
	}

	/**
	 * Parses Timestamp.
	 * 
	 * @param prjData
	 *            the project data
	 * @param latestVersion
	 *            the latest version
	 * @return the JSONObject
	 */
	public static JSONObject parseTimestamp(JSONObject prjData, String latestVersion) {
		JSONObject lvJson = null;
		JSONObject timeStamp = (JSONObject) prjData.get("v");
		if (null != timeStamp) {
			lvJson = (JSONObject) timeStamp.get(latestVersion);
		}
		return lvJson;
	}

	/**
	 * Date formatter.
	 * 
	 * @param duration
	 *            the duration
	 * @return the data format as string
	 */
	public static String dateFormatter(String duration) {
		Long durationInMinutes = Long.valueOf(duration);
		if (durationInMinutes == 0) {
			return "0";
		}
		boolean isNegative = durationInMinutes < 0;
		Long absDuration = Math.abs(durationInMinutes);

		int days = ((Double) ((double) absDuration / HOURS_IN_DAY / 60)).intValue();
		Long remainingDuration = absDuration - (days * HOURS_IN_DAY * 60);
		int hours = ((Double) (remainingDuration.doubleValue() / 60)).intValue();
		remainingDuration = remainingDuration - (hours * 60);
		int minutes = remainingDuration.intValue();

		return SonarUtils.formatDuration(days, hours, minutes, isNegative);
	}

}
