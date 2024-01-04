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
package com.publicissapient.kpidashboard.apis.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

/**
 * @author shi6
 */
@RunWith(MockitoJUnitRunner.class)
public class RestAPIUtilsTest {

	@Mock
	private AesEncryptionService aesEncryptionService;

	@Mock
	private CustomApiConfig customApiConfig;

	@Mock
	private JSONParser parser;

	@InjectMocks
	private RestAPIUtils restAPIUtils;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testGetHeadersWithAccessToken() {
		// Mock data
		String accessToken = "mockAccessToken";
		boolean usingBasicAuth = false;


		// Mock the result
		HttpHeaders headers = RestAPIUtils.getHeaders(accessToken, usingBasicAuth);

		// Assertions
		assertEquals("Bearer mockAccessToken", headers.getFirst("Authorization"));
	}

	@Test
	public void testGetHeadersWithBasicAuth() {
		// Mock data
		String accessToken = "mockAccessToken";
		boolean usingBasicAuth = true;

		// Mock the result
		HttpHeaders headers = RestAPIUtils.getHeaders(accessToken, usingBasicAuth);

		// Assertions
		assertEquals("Basic " + java.util.Base64.getEncoder().encodeToString("mockAccessToken:".getBytes()),
				headers.getFirst("Authorization"));
	}

	@Test
	public void testGetHeaders() {
		// Mock data
		String username = "mockUsername";
		String password = "mockPassword";

		HttpHeaders headers = restAPIUtils.getHeaders(username, password);

		// Assertions
		assertEquals("Basic " + java.util.Base64.getEncoder().encodeToString("mockUsername:mockPassword".getBytes()),
				headers.getFirst("Authorization"));
	}

	@Test
	public void testGetHeadersForPAT() {
		// Mock data
		String password = "mockPassword";

		HttpHeaders headers = restAPIUtils.getHeadersForPAT(password);

		// Assertions
		assertEquals("Bearer mockPassword", headers.getFirst("Authorization"));
	}

	@Test
	public void testAddHeaders() {
		// Mock data
		HttpHeaders existingHeaders = new HttpHeaders();
		existingHeaders.add("ExistingKey", "ExistingValue");

		String keyToAdd = "NewKey";
		String valueToAdd = "NewValue";

		// Mock the result
		RestAPIUtils restAPIUtils = new RestAPIUtils();
		HttpHeaders updatedHeaders = restAPIUtils.addHeaders(existingHeaders, keyToAdd, valueToAdd);

		// Assertions
		assertEquals("ExistingValue", existingHeaders.getFirst("ExistingKey"));
		assertEquals("NewValue", updatedHeaders.getFirst("NewKey"));
	}

	@Test
	public void testAddHeadersWithNullExistingHeaders() {
		// Mock data
		HttpHeaders existingHeaders = null;

		String keyToAdd = "NewKey";
		String valueToAdd = "NewValue";

		// Mock the result
		RestAPIUtils restAPIUtils = new RestAPIUtils();
		HttpHeaders updatedHeaders = restAPIUtils.addHeaders(existingHeaders, keyToAdd, valueToAdd);

		// Assertions
		assertEquals("NewValue", updatedHeaders.getFirst("NewKey"));
	}

	@Test
	public void testConvertJSONArrayFromResponse() throws ParseException {
		// Mock data
		String responseBody = "{\"key\": [\"value1\", \"value2\"]}";
		String key = "key";

		// Call the method
		JSONArray jsonArray = restAPIUtils.convertJSONArrayFromResponse(responseBody, key);

		// Assertions
		assertEquals("value1", jsonArray.get(0));
		assertEquals("value2", jsonArray.get(1));
	}

	@Test
	public void testConvertToString() {
		// Mock data
		JSONObject jsonData = new JSONObject();
		jsonData.put("key", "value");

		// Call the method
		String result = restAPIUtils.convertToString(jsonData, "key");

		// Assertion
		assertEquals("value", result);
	}

	@Test
	public void testConvertListFromMultipleArray() {
		JSONObject innerJsonObject = new JSONObject();
		innerJsonObject.put("key", "value");
		innerJsonObject.put("_class", "com.some.other.Class");

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("jobs", createJsonArray(innerJsonObject));
		jsonObject.put("_class", "com.cloudbees.hudson.plugins.folder.Folder");

		// Call the method
		List<String> result = restAPIUtils.convertListFromMultipleArray(createJsonArray(jsonObject), "key");

		// Assertion
		assertEquals(1, result.size()); // No items should be added because _class is not
										// "com.cloudbees.hudson.plugins.folder.Folder"

        assertTrue(result.contains("value"));
		// Mock data with the correct _class
		innerJsonObject.put("_class", "com.cloudbees.hudson.plugins.folder.Folder");

		// Call the method again
		result = restAPIUtils.convertListFromMultipleArray(createJsonArray(jsonObject), "key");

		// Assertion
		assertEquals(0, result.size());
	}

	@Test
	public void testGetJsonArrayFromJSONObj() {
		// Mock data
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("key", createJsonArray("value1", "value2"));

		// Call the method
		JSONArray result = restAPIUtils.getJsonArrayFromJSONObj(jsonObject, "key");

		// Assertion
		assertEquals(2, result.size());
		assertEquals("value1", result.get(0));
		assertEquals("value2", result.get(1));
	}

	@Test
	public void testDecryptPassword() {
		// Mock data
		String encryptedPassword = "encryptedPassword";
		when(aesEncryptionService.decrypt(any(), any())).thenReturn("decryptedPassword");

		// Call the method
		String result = restAPIUtils.decryptPassword(encryptedPassword);

		// Assertion
		assertEquals("decryptedPassword", result);
	}

	private JSONArray createJsonArray(Object... values) {
		JSONArray jsonArray = new JSONArray();
		for (Object value : values) {
			jsonArray.add(value);
		}
		return jsonArray;
	}

	private JSONObject createJsonObject() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("key", createJsonArray("value1", "value2"));
		return jsonObject;
	}

	private JSONArray createJsonArray() {
		JSONArray jsonArray = new JSONArray();
		jsonArray.add("value1");
		jsonArray.add("value2");
		return jsonArray;
	}
}