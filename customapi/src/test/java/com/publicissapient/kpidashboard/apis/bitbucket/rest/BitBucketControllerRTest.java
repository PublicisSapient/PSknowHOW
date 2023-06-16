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

package com.publicissapient.kpidashboard.apis.bitbucket.rest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

import com.publicissapient.kpidashboard.apis.bitbucket.service.BitBucketServiceKanbanR;
import com.publicissapient.kpidashboard.apis.bitbucket.service.BitBucketServiceR;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.model.KpiElement;

@RunWith(MockitoJUnitRunner.class)
public class BitBucketControllerRTest {

	private MockMvc mockMvc;

	@Mock
	private BitBucketServiceR bitbucketService;
	@Mock
	private BitBucketServiceKanbanR bitbucketServiceKanban;

	@Mock
	private CacheService cacheService;

	@InjectMocks
	private BitBucketController bitBucketController;

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(bitBucketController).build();
	}

	@After
	public void after() {
		mockMvc = null;
	}

	@Test
	public void getBitBucketKPIMetricReturnsValue() throws Exception {
		String request = "{\n" + "  \"kpiList\": [\n" + "    {\n" + "      \"id\": \"5b753628d42937acd035b7ef\",\n"
				+ "      \"kpiId\": \"kpi11\",\n" + "      \"kpiName\": \"BitBucketCheck-ins\",\n"
				+ "      \"isDeleted\": \"False\",\n" + "      \"kpiCategory\": \"Productivity\",\n"
				+ "      \"kpiUnit\": \"Number\",\n" + "      \"kpiSource\": \"BitBucket\",\n"
				+ "      \"maxValue\": \"\",\n" + "      \"chartType\": \"gaugeChart\"\n" + "    }\n" + "  ],\n"
				+ "  \"ids\": [\n" + "    \"GMA_GMA\"\n" + "  ],\n" + "  \"level\": 1\n" + "}";

		List<KpiElement> kpiElementList = new ArrayList<>();
		KpiElement kpiElement = new KpiElement();
		kpiElement.setValue(100);
		kpiElement.setId("5b6be4bf39e7aef89a0fc456");
		kpiElement.setKpiSource("BitBucket");
		kpiElementList.add(kpiElement);
		when(bitbucketService.process(Mockito.any())).thenReturn(kpiElementList);
		mockMvc.perform(post("/bitbucket/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andExpect(status().is2xxSuccessful());

	}

	@Test
	public void getBitBucketKPIMetricReturns400() throws Exception {

		String request = "{\n" + "  \"level\": 3,\n" + "  \"ids\": [\n" + "    \"OPRO Sprint 71_12138_10304_PR\",\n"
				+ "    \"OPRO Sprint 72_12139_10304_PR\"\n" + "  ],\n" + "  \"kpiList\": []\n" + "}";

		mockMvc.perform(post("/bitbucket/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andDo(print()).andExpect(status().isBadRequest());

	}

	@Test
	public void getBitBucketKPIMetricKanbanReturnsValue() throws Exception {
		String request = "{\n" + "  \"kpiList\": [\n" + "    {\n" + "      \"id\": \"5b753628d42937acd035b7ef\",\n"
				+ "      \"kpiId\": \"kpi11\",\n" + "      \"kpiName\": \"BitBucketCheck-ins\",\n"
				+ "      \"isDeleted\": \"False\",\n" + "      \"kpiCategory\": \"Productivity\",\n"
				+ "      \"kpiUnit\": \"Number\",\n" + "      \"kpiSource\": \"BitBucket\",\n"
				+ "      \"maxValue\": \"\",\n" + "      \"chartType\": \"gaugeChart\"\n" + "    }\n" + "  ],\n"
				+ "  \"ids\": [\n" + "    \"GMA_GMA\"\n" + "  ],\n" + "  \"level\": 1\n" + "}";

		List<KpiElement> kpiElementList = new ArrayList<>();
		KpiElement kpiElement = new KpiElement();
		kpiElement.setValue(100);
		kpiElement.setId("5b6be4bf39e7aef89a0fc456");
		kpiElement.setKpiSource("BitBucket");
		kpiElementList.add(kpiElement);

		when(bitbucketServiceKanban.process(Mockito.any())).thenReturn(kpiElementList);
		mockMvc.perform(post("/bitbucketkanban/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andExpect(status().is2xxSuccessful());

	}

	@Test
	public void getBitBucketKPIMetricKanbanReturns400() throws Exception {

		String request = "{\n" + "  \"level\": 3,\n" + "  \"ids\": [\n" + "    \"OPRO Sprint 71_12138_10304_PR\",\n"
				+ "    \"OPRO Sprint 72_12139_10304_PR\"\n" + "  ],\n" + "  \"kpiList\": []\n" + "}";

		mockMvc.perform(post("/bitbucketkanban/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andDo(print()).andExpect(status().isBadRequest());

	}

}
