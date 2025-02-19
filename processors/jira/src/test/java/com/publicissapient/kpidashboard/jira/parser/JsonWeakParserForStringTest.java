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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JsonWeakParserForStringTest {

	@InjectMocks
	JsonWeakParserForString jsonWeakParserForString;

	@Test
	public void testParseWithStringInput() throws JSONException {
		String inputString = "TestString";

		try {
			String result = jsonWeakParserForString.parse(inputString);
			Assert.assertEquals("Parsed string should be equal to input string", inputString, result);
		} catch (JSONException e) {
			Assert.fail("Unexpected JSONException: " + e.getMessage());
		}
	}

	@Test(expected = JSONException.class)
	public void testParseWithNonStringInput() throws JSONException {
		Integer nonStringInput = 123;

		jsonWeakParserForString.parse(nonStringInput);
		// If the code reaches this point, the test should fail because it should throw
		// a JSONException.
	}
}
