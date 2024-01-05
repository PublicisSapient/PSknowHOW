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

package com.publicissapient.kpidashboard.apis.sonar.utiils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;

/*
	@author shi6
 */
@RunWith(MockitoJUnitRunner.class)
public class SonarAPIUtilsTest {
	@InjectMocks
	SonarAPIUtils apiUtils;

	@Test
	public void testGetHeadersWithValidCredentials() {
		// Arrange
		String username = "john";
		String password = "secret";

		// Act
		HttpHeaders headers = apiUtils.getHeaders(username, password);
		HttpHeaders headers2 = apiUtils.getHeaders(username, true);
		HttpHeaders headers7 = apiUtils.getHeaders("", true);
		HttpHeaders headers8 = apiUtils.getHeaders(null, true);
		HttpHeaders headers9 = apiUtils.getHeaders(null, false);
		HttpHeaders headers3 = apiUtils.getHeaders("", password);
		HttpHeaders headers4 = apiUtils.getHeaders(null, password);
		HttpHeaders headers5 = apiUtils.getHeaders(username, "");
		HttpHeaders headers6 = apiUtils.getHeaders(username, null);

		// Assert
		Assert.assertNotNull(headers);
		Assert.assertNotNull(headers2);
		assertNull(headers3.getFirst("Authorization"));
		assertNull(headers4.getFirst("Authorization"));
		assertNull(headers5.getFirst("Authorization"));
		assertNull(headers6.getFirst("Authorization"));
		assertNull(headers7.getFirst("Authorization"));
		assertNull(headers8.getFirst("Authorization"));
		assertNull(headers9.getFirst("Authorization"));
		assertEquals("Basic am9objpzZWNyZXQ=", headers.getFirst("Authorization"));
		assertEquals("Basic " + Base64.encodeBase64String("john:".getBytes(StandardCharsets.US_ASCII)),
				headers2.getFirst("Authorization"));
	}

	@Test
	public void testConvertToStringWithValue() {
		// Arrange
		String key = "someKey";
		JSONObject jsonData = mock(JSONObject.class);
		when(jsonData.get(key)).thenReturn("someValue");

		// Act
		String result = apiUtils.convertToString(jsonData, key);
		String result2 = apiUtils.convertToString(jsonData, "");

		// Assert
		assertEquals("someValue", result);
		assertNull(result2);
	}

}