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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author harjinda1
 */
@RunWith(MockitoJUnitRunner.class)
public class AzurePipelineUtilsTest {

	@Test
	public void isSameServerInfoPositive() {
		String url1 = "https://123456:234567@azurepipeline.com/job/job1";
		String url2 = "https://123456:234567@azurepipeline.com/job/job1";
		assertTrue(AzurePipelineUtils.isSameServerInfo(url1, url2));
	}

	@Test
	public void isSameServerInfoNegative() {
		String url1 = "";
		String url2 = "https://123456:234567@azurepipeline.com/job/job1";
		assertFalse(AzurePipelineUtils.isSameServerInfo(url1, url2));
	}

	@Test
	public void isSameServerInfoMalformedUrl() {
		String invalidUrlWithSpace = "https:// 234567@azurepipeline.com/job/job1";
		String url2 = "https://234567@azurepipeline.com/job/job1";
		assertFalse(AzurePipelineUtils.isSameServerInfo(invalidUrlWithSpace, url2));
	}

	@Test
	public void joinUrlWithoutBackslash() {
		String url1 = "https://test.com/testUser/testProject";
		String url2 = "_apis/build/builds";
		String result = "https://test.com/testUser/testProject/_apis/build/builds";
		assertEquals(result, AzurePipelineUtils.joinURL(url1, url2));
	}

	@Test
	public void joinUrlWithBackslash() {
		String url1 = "https://test.com/testUser/testProject/";
		String url2 = "_apis/build/builds";
		String result = "https://test.com/testUser/testProject/_apis/build/builds";
		assertEquals(result, AzurePipelineUtils.joinURL(url1, url2));
	}

	@Test
	public void getDateFromTimeInMili() {
		String resDate = "1970-01-01T00:08:20.000Z";
		long time = 500000;
		assertEquals(resDate, AzurePipelineUtils.getDateFromTimeInMili(time));
	}

	@Test
	public void addParamToBaseUrl() {
		StringBuilder url = new StringBuilder("https://test.com/testUser/testProject/_apis/build/builds");
		String resUrl = "https://test.com/testUser/testProject/_apis/build/builds?api-version=5.1";
		String finalUrl = AzurePipelineUtils.addParam(url, "api-version", "5.1").toString();
		assertEquals(resUrl, finalUrl);
	}

	@Test
	public void addParamToQueryUrl() {
		StringBuilder url = new StringBuilder(
				"https://test.com/testUser/testProject/_apis/build/builds?api-version=5.1");
		String resUrl = "https://test.com/testUser/testProject/_apis/build/builds?api-version=5.1&definitions=1";
		String finalUrl = AzurePipelineUtils.addParam(url, "definitions", "1").toString();
		assertEquals(resUrl, finalUrl);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getString() {
		JSONObject obj = new JSONObject();
		obj.put("id", 1);
		obj.put("url", "https://test.com/testUser/testProject/_apis/build/builds");
		assertEquals("https://test.com/testUser/testProject/_apis/build/builds",
				AzurePipelineUtils.getString(obj, "url"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getJsonArray() {
		JSONObject obj = new JSONObject();
		JSONArray array = new JSONArray();
		array.add(1);
		array.add(2);
		obj.put("id", 1);
		obj.put("url", "https://test.com/testUser/testProject/_apis/build/builds");
		obj.put("buildIds", array);
		assertEquals(array, AzurePipelineUtils.getJsonArray(obj, "buildIds"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getJsonObject() {
		JSONObject obj = new JSONObject();
		JSONObject buildObj = new JSONObject();
		buildObj.put("buildId", 3);
		buildObj.put("buildUrl", "https://test.com/testUser/_apis/build/Builds/7");
		obj.put("id", 1);
		obj.put("url", "https://test.com/testUser/testProject/_apis/build/builds");
		obj.put("buildObj", buildObj);
		assertEquals(buildObj, AzurePipelineUtils.getJsonObject(obj, "buildObj"));
	}
}