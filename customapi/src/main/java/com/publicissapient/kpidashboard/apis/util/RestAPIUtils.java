package com.publicissapient.kpidashboard.apis.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

/**
 * Utility class for rest api call request/response
 *
 * @author hiren babariya
 *
 */
@Component
public class RestAPIUtils {
	private static final String AUTHORIZATION = "Authorization";

	@Autowired
	private AesEncryptionService aesEncryptionService;

	@Autowired
	private CustomApiConfig customApiConfig;

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
	 * Creates HTTP Headers.
	 *
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 * @return HttpHeaders the http header
	 */
	public HttpHeaders getHeaders(String username, String password) {
		HttpHeaders header = new HttpHeaders();
		if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
			String authentication = username + ":" + password;
			byte[] encodedAuth = Base64.encodeBase64(authentication.getBytes(StandardCharsets.US_ASCII));
			String authenticationHeader = "Basic " + new String(encodedAuth);
			header.set(AUTHORIZATION, authenticationHeader);
		}
		return header;
	}

	public HttpHeaders getHeadersForPAT(String pat) {
		HttpHeaders header = new HttpHeaders();
		String authenticationHeader = "Bearer " + pat;
		header.set(AUTHORIZATION, authenticationHeader);
		return header;
	}

	/**
	 * Creates HTTP Headers.
	 *
	 * @param header
	 *            http header
	 *
	 * @param key
	 *            key
	 * @param value
	 *            value
	 * @return HttpHeaders the http header
	 */
	public HttpHeaders addHeaders(HttpHeaders header, String key, String value) {
		if (null != header) {
			header = new HttpHeaders();
		}
		header.add(key, value);
		return header;
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
	public JSONArray convertJSONArrayFromResponse(String responseBody, String key) throws ParseException {
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
	public String convertToString(JSONObject jsonData, String key) {
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
	public List<String> convertListFromArray(JSONArray jsonArray, String key) {
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

	public List<String> convertListFromMultipleArray(JSONArray jsonArray, String key) {
		List<String> list = new ArrayList<>();
		for (Object obj : jsonArray) {
			JSONObject jsonObject = (JSONObject) obj;
			if (jsonObject != null) {
				if (jsonObject.containsKey("jobs")) {
					JSONArray jobsArray = (JSONArray) (jsonObject.get("jobs"));
					List<String> newList = convertListFromMultipleArray(jobsArray, key);
					list.addAll(newList);
				}

				String className = jsonObject.get("_class").toString();
				if (!className.equalsIgnoreCase("com.cloudbees.hudson.plugins.folder.Folder")) {
					String value = jsonObject.get(key).toString();
					list.add(value);
				}
			}
		}
		return list;
	}

	public JSONArray getJsonArrayFromJSONObj(JSONObject obj, String key) {
		Object array = obj.get(key);
		return array == null ? new JSONArray() : (JSONArray) array;
	}

	public String decryptPassword(String encryptedPassword) {
		return aesEncryptionService.decrypt(encryptedPassword, customApiConfig.getAesEncryptionKey());
	}
}
