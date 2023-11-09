package com.publicissapient.kpidashboard.apis.sonar.utiils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpHeaders;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides utility methods for Sonar.
 *
 */
@Slf4j
public class SonarAPIUtils {

	private static final String AUTHORIZATION= "Authorization";

	private SonarAPIUtils() {

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
	 * Converts json Array to string response body.
	 *
	 * @param responseBody
	 *            the api response body string
	 * @param key
	 *            the project key
	 * @return the string data
	 */
	public static JSONArray parseData(String responseBody, String key) throws ParseException {
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(responseBody);
		return (JSONArray) jsonObject.get(key);
	}

	/**
	 * Converts String to json objects.
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
	 * Converts list of string to jsonArray.
	 *
	 * @param jsonArray
	 *            the json object
	 * @param key
	 *            the project key
	 * @return the string data
	 */
	public static List<String> convertListFromArray(JSONArray jsonArray, String key) {
		List<String> list = new ArrayList<>();
		for (Object obj : jsonArray) {
			JSONObject jsonObject = (JSONObject) obj;
			if (jsonObject != null) {
				String value = jsonObject.get(key).toString();
				list.add(value);
			}
		}
		return list;
	}

}
