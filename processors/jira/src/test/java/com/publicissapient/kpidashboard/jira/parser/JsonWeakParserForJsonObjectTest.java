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

package com.publicissapient.kpidashboard.jira.parser;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;

@RunWith(MockitoJUnitRunner.class)
public class JsonWeakParserForJsonObjectTest {

	@InjectMocks
	JsonWeakParserForJsonObject jsonWeakParserForJsonObject;

	@Mock
	private JsonObjectParser<String> mockJsonObjectParser;

	@Test
	public void testParseWithJsonObjectInput() throws JSONException {
		MockitoAnnotations.openMocks(this);

		JsonWeakParserForJsonObject<String> parser = new JsonWeakParserForJsonObject<>(mockJsonObjectParser);
		JSONObject jsonObject = new JSONObject();

		try {
			parser.parse(jsonObject);
			Mockito.verify(mockJsonObjectParser).parse(jsonObject);
		} catch (JSONException e) {
			Assert.fail("Unexpected JSONException: " + e.getMessage());
		}
	}

	@Test(expected = JSONException.class)
	public void testParseWithNonJsonObjectInput() throws JSONException {
		MockitoAnnotations.openMocks(this);

		JsonWeakParserForJsonObject<String> parser = new JsonWeakParserForJsonObject<>(mockJsonObjectParser);
		String nonJsonObjectInput = "TestString";

		parser.parse(nonJsonObjectInput);
		// If the code reaches this point, the test should fail because it should throw
		// a JSONException.
	}
}
