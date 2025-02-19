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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.sonar.data.ProjectToolConnectionFactory;

/**
 * @author shi6
 */
@ExtendWith(SpringExtension.class)
public class SonarProcessorUtilsTest {

	private static final String FORMATTED_DATE = "yyyy-MM-dd'T'HH:mm:ssZ";
	private static final String AUTHORIZATION = "Authorization";
	@InjectMocks
	SonarProcessorUtils sonarProcessorUtils;
	@Mock
	private ToolCredentialProvider toolCredentialProvider;

	@Test
	public void getHeaders_BasicAuthTrue() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Authorization", "Basic YWJjOg==");
		assertEquals(httpHeaders, SonarProcessorUtils.getHeaders("abc", true));
	}

	@Test
	public void getHeaders_BasicAuthFalse() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Authorization", "Bearer abc");
		assertEquals(httpHeaders, SonarProcessorUtils.getHeaders("abc", false));
	}

	@Test
	public void testVault() {
		ProjectToolConnectionFactory toolConnectionFactory = ProjectToolConnectionFactory.newInstance();
		ProcessorToolConnection processorToolConnection = toolConnectionFactory.getProcessorToolConnectionList().get(3);
		ToolCredential credential = new ToolCredential();
		credential.setUsername("dummy");
		credential.setPassword("dummy");
		doReturn(credential).when(toolCredentialProvider).findCredential(any());
		SonarUtils.getToolCredentials(toolCredentialProvider, processorToolConnection);
	}

	@Test
	public void testCloud() {
		ProjectToolConnectionFactory toolConnectionFactory = ProjectToolConnectionFactory.newInstance();
		ProcessorToolConnection processorToolConnection = toolConnectionFactory.getProcessorToolConnectionList().get(2);
		SonarUtils.getToolCredentials(toolCredentialProvider, processorToolConnection);
	}

	@Test
	void getTimestamp() {
		// Creating a mock JSON object
		JSONObject jsonData = new JSONObject();
		Date currentDate = new Date();
		jsonData.put("timestamp", new SimpleDateFormat(FORMATTED_DATE, Locale.ROOT).format(currentDate));

		// Testing the method
		long timestamp = sonarProcessorUtils.getTimestamp(jsonData, "timestamp");
	}

	@Test
	void convertToString() {
		// Creating a mock JSON object
		JSONObject jsonData = new JSONObject();
		jsonData.put("key", "value");

		// Testing the method
		String result = sonarProcessorUtils.convertToString(jsonData, "key");

		// Asserting the result
		assertEquals("value", result);
	}

	@Test
	void convertToStringSafe() {
		// Creating a mock JSON object
		JSONObject jsonData = new JSONObject();
		jsonData.put("key", "value");

		// Testing the method
		String result = sonarProcessorUtils.convertToStringSafe(jsonData, "key");

		// Asserting the result
		assertEquals("value", result);
	}

	@Test
	void getHeaders() {
		// Testing the method
		HttpHeaders headers = sonarProcessorUtils.getHeaders("username", "password");

		// Asserting the result
		assertEquals("Basic dXNlcm5hbWU6cGFzc3dvcmQ=", headers.getFirst(AUTHORIZATION));
	}

	@Test
	void getHeadersWithAccessToken() {
		// Testing the method
		HttpHeaders headers = sonarProcessorUtils.getHeaders("accessToken");

		// Asserting the result
		assertEquals("Bearer accessToken", headers.getFirst(AUTHORIZATION));
	}

	@Test
	void dateFormatter() {
		// Testing the method
		String result = sonarProcessorUtils.dateFormatter("1440");

		// Asserting the result
		assertEquals("3d", result);
	}
}
