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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.jira.rest.client.api.domain.BasicUser;

@RunWith(MockitoJUnitRunner.class)
public class JsonParseUtilTest {

	@Test
	public void testParseJsonArray() throws JSONException {
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(new JSONObject().put("name", "John"));
		jsonArray.put(new JSONObject().put("name", "Jane"));

		Collection<String> result = JsonParseUtil.parseJsonArray(jsonArray, json -> json.getString("name"));

		assertEquals(Arrays.asList("John", "Jane"), result);
	}

	// @Test
	// public void testParseOptionalExpandableProperty() throws JSONException {
	// JSONObject json = new JSONObject();
	// json.put("size", 2);
	// json.put("items", new JSONArray().put(new JSONObject().put("id", 1)).put(new
	// JSONObject().put("id", 2)));
	//
	// ExpandableProperty<Integer> result =
	// JsonParseUtil.parseOptionalExpandableProperty(json,
	// json -> json.getInt("id"));
	//
	// assertEquals(2, result.getSize());
	// assertEquals(Arrays.asList(1, 2), result.getItems());
	// }

	@Test
	public void testParseURI() {
		URI result = JsonParseUtil.parseURI("http://example.com");

		assertEquals(URI.create("http://example.com"), result);
	}

	@Test
	public void testParseBasicUser() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("name", "John");
		json.put("displayName", "John Doe");

		BasicUser result = JsonParseUtil.parseBasicUser(json);

		assertEquals("John Doe", result.getDisplayName());
	}

	@Test
	public void testGetOptionalString() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("attributeName", "value");

		String result = JsonParseUtil.getOptionalString(json, "attributeName");

		assertEquals("value", result);
	}

	@Test
	public void testGetOptionalStringWithNullValue() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("attributeName", JSONObject.EXPLICIT_NULL);

		String result = JsonParseUtil.getOptionalString(json, "attributeName");

		assertNull(result);
	}
}
