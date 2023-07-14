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

package com.publicissapient.kpidashboard.azurepipeline.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides utility methods for AzurePipeline processor.
 */
@Component
@Slf4j
public final class AzurePipelineUtils {

	private static final String DUMMY_USER = "dummyUser";

	private AzurePipelineUtils() {
	}

	/**
	 * Creates HTTP Headers.
	 * 
	 * @param userInfo
	 *            the user info
	 * @return the HttpHeaders
	 */
	public static HttpHeaders createHeaders(final String userInfo) {
		String credentials = DUMMY_USER + ":" + userInfo;
		byte[] encodedAuth = Base64.getEncoder().encode(credentials.getBytes(StandardCharsets.US_ASCII));
		String authHeader = "Basic " + new String(encodedAuth);

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, authHeader);
		return headers;
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

	/**
	 * Convert time in milliseconds to date time format in UTC timezone
	 * 
	 * @param time
	 *            the time in milliseconds we want to convert to date time format
	 * @return date time in string
	 */
	public static String getDateFromTimeInMili(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(time);
	}

	public static String getDateFromTimeInMili(String time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(time);
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
	 * Converts JSONObject to String.
	 * 
	 * @param json
	 *            the json object
	 * @param key
	 *            the key
	 * @return the string data
	 */
	public static String getString(JSONObject json, String key) {
		return (String) json.get(key);
	}

	/**
	 * Provides JsonArray.
	 * 
	 * @param json
	 *            the json
	 * @param key
	 *            the key
	 * @return the JSONArray
	 */
	public static JSONArray getJsonArray(JSONObject json, String key) {
		Object array = json.get(key);
		return array == null ? new JSONArray() : (JSONArray) array;
	}

	/**
	 * Provides JsonObject
	 * 
	 * @param json
	 *            the json object
	 * @param key
	 *            the key
	 * @return the JSONObject
	 */
	public static JSONObject getJsonObject(JSONObject json, String key) {
		Object object = json.get(key);
		return object == null ? new JSONObject() : (JSONObject) object;
	}

	/**
	 * Provides Domain name.
	 * 
	 * @param url
	 *            the URL
	 * @return the domain name
	 * @throws URISyntaxException
	 *             if there is any illegal character in URI
	 */
	public static String extractDomain(String url) throws URISyntaxException {
		URI uri = new URI(url);
		return uri.getHost();
	}

	/**
	 * Provides Port number.
	 * 
	 * @param url
	 *            the URL
	 * @return port the port number
	 * @throws URISyntaxException
	 *             if there is any illegal character in URI
	 */
	public static int extractPort(String url) throws URISyntaxException {
		URI uri = new URI(url);
		return uri.getPort();
	}

	/**
	 * Check whether two urls have the same server info
	 * 
	 * @param url1
	 *            url1
	 * @param url2
	 *            url2
	 * @return true if they have same server info else false
	 */
	public static boolean isSameServerInfo(String url1, String url2) {
		try {
			String domain1 = extractDomain(url1);
			int port1 = extractPort(url1);
			String domain2 = extractDomain(url2);
			int port2 = extractPort(url2);

			if (StringUtils.isEmpty(domain1) || StringUtils.isEmpty(domain2)) {
				return false;
			}
			if (domain1.equals(domain2) && port1 == port2) {
				return true;
			}

		} catch (URISyntaxException exception) {
			log.error(String.format("uri syntax error url1= %s, url2 = %s", url1, url2), exception);
		}

		return false;
	}
}
