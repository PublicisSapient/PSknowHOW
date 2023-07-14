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

package com.publicissapient.kpidashboard.apis.zephyr.rest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.zephyr.service.ZephyrService;
import com.publicissapient.kpidashboard.apis.zephyr.service.ZephyrServiceKanban;

/**
 * This class test the Zephyr Controller. In no way this representation of an
 * integration test. All the underlying services and repositories has been
 * mocked. Please don't format this class to keep the readability. Follow [
 * https://stackoverflow.com/questions/5115088/turn-off-eclipse-formatter-for-selected-code-area
 * ] to switch off formatter for section of code.
 * 
 * @author tauakram
 * 
 */

@RunWith(MockitoJUnitRunner.class)
public class ZephyrControllerTest {

	private MockMvc mockMvc;

	@Mock
	private ZephyrService zephyrService;

	@Mock
	private ZephyrServiceKanban zephyrServiceKanban;

	@Mock
	private CacheService cacheService;

	@InjectMocks
	private ZephyrController zephyrController;

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(zephyrController).build();
	}

	@After
	public void after() {
		mockMvc = null;
	}

	@Test
	public void getZephyrKPIMetricReturnsValue() throws Exception {

		// @formatter:off
		String request = "{\n" + 
				"  \"level\": 3,\n" + 
				"  \"ids\": [\n" + 
				"    \"OPRO Sprint 71_12138_10304_PR\",\n" + 
				"    \"OPRO Sprint 72_12139_10304_PR\"\n" + 
				"  ],\n" + 
				"  \"kpiList\": [\n" + 
				"    {\n" + 
				"      \"id\": \"5b6be4bf39e7aef89a0fc456\",\n" + 
				"      \"kpiId\": \"kpi16\",\n" + 
				"      \"kpiName\": \"Test Automation Percentage\",\n" + 
				"      \"isDeleted\": \"False\",\n" + 
				"      \"kpiCategory\": \"Quality\",\n" + 
				"      \"kpiUnit\": \"Percentage\",\n" + 
				"      \"kpiSource\": \"Zypher\",\n" + 
				"      \"maxValue\": \"100\",\n" + 
				"      \"chartType\": \"gaugeChart\"\n" + 
				"    },\n" + 
				"    {\n" + 
				"      \"id\": \"5b6be4bf39e7aef89a0fc456\",\n" + 
				"      \"kpiId\": \"kpi42\",\n" + 
				"      \"kpiName\": \"Regression Percentage\",\n" + 
				"      \"isDeleted\": \"False\",\n" + 
				"      \"kpiCategory\": \"Quality\",\n" + 
				"      \"kpiUnit\": \"Percentage\",\n" + 
				"      \"kpiSource\": \"Zypher\",\n" + 
				"      \"maxValue\": \"100\",\n" + 
				"      \"chartType\": \"gaugeChart\"\n" + 
				"    }\n" +
				"  ]\n" + 
				"}";
		// @formatter:on

		List<KpiElement> kpiElementList = new ArrayList<>();
		KpiElement kpi16 = new KpiElement();
		kpi16.setValue(100);
		kpi16.setId("5b6be4bf39e7aef89a0fc456");
		kpiElementList.add(kpi16);

		KpiElement kpi42 = new KpiElement();
		kpi42.setValue(80);
		kpi42.setId("6b6be4bf39e7aef89a0fc457");

		kpiElementList.add(kpi42);
		when(zephyrService.process(Mockito.any())).thenReturn(kpiElementList);

		mockMvc.perform(post("/zypher/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andExpect(status().is2xxSuccessful())
				// .andDo(print())
				.andExpect(jsonPath("$[0].value").value(100)).andExpect(jsonPath("$[1].value").value(80));

	}

	@Test
	public void getZephyrKPIMetricReturns400() throws Exception {

		// @formatter:off
		String request = "{\n" + 
				"  \"level\": 3,\n" + 
				"  \"ids\": [\n" + 
				"    \"OPRO Sprint 71_12138_10304_PR\",\n" + 
				"    \"OPRO Sprint 72_12139_10304_PR\"\n" + 
				"  ],\n" + 
				"  \"kpiList\": []\n" + 
				"}";
		// @formatter:on

		mockMvc.perform(post("/zypher/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andExpect(status().isBadRequest());

	}

	@Test
	public void getZephyrKanbanKPIMetricReturnsValue() throws Exception {

		// @formatter:off
		String request = "{\n" + 
				"  \"level\": 2,\n" + 
				"  \"ids\": [\n" + 
				"    \"PR\",\n" + 
				"    \"10304_PR\"\n" + 
				"  ],\n" + 
				"  \"kpiList\": [\n" + 
				"    {\n" + 
				"      \"id\": \"5b6be4bf39e7aef89a0fc456\",\n" + 
				"      \"kpiId\": \"kpi63\",\n" + 
				"      \"kpiName\": \"Kanban Regression Percentage\",\n" + 
				"      \"isDeleted\": \"False\",\n" + 
				"      \"kpiCategory\": \"Quality\",\n" + 
				"      \"kpiUnit\": \"Percentage\",\n" + 
				"      \"kpiSource\": \"ZEPHYRKANBAN\",\n" + 
				"      \"maxValue\": \"100\",\n" + 
				"      \"chartType\": \"gaugeChart\"\n" + 
				"    }\n" +
				"  ]\n" + 
				"}";
		// @formatter:on

		List<KpiElement> kpiElementList = new ArrayList<>();
		KpiElement kpi63 = new KpiElement();
		kpi63.setValue(100);
		kpi63.setId("5b6be4bf39e7aef89a0fc456");
		kpiElementList.add(kpi63);

		when(zephyrServiceKanban.process(Mockito.any())).thenReturn(kpiElementList);

		mockMvc.perform(post("/zypherkanban/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andExpect(status().is2xxSuccessful());

	}

	@Test
	public void getZephyrKanbanKPIMetricReturns400() throws Exception {
		// @formatter:off
		String request = "{\n" + 
				"  \"level\": 3,\n" + 
				"  \"ids\": [\n" + 
				"    \"OPRO Sprint 71_12138_10304_PR\",\n" + 
				"    \"OPRO Sprint 72_12139_10304_PR\"\n" + 
				"  ],\n" + 
				"  \"kpiList\": []\n" + 
				"}";
		// @formatter:on

		mockMvc.perform(post("/zypherkanban/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andExpect(status().isBadRequest());

	}

}
