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

package com.publicissapient.kpidashboard.apis.sonar.rest;

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

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.sonar.service.SonarServiceKanbanR;
import com.publicissapient.kpidashboard.apis.sonar.service.SonarServiceR;

@RunWith(MockitoJUnitRunner.class)
public class SonarControllerRTest {

	private MockMvc mockMvc;

	@Mock
	private SonarServiceR sonarService;

	@Mock
	private SonarServiceKanbanR sonarServiceKanban;

	@Mock
	private CacheService cacheService;

	@InjectMocks
	private SonarController sonarController;

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(sonarController).build();
	}

	@After
	public void after() {
		mockMvc = null;
	}

	@Test
	public void getSonarKPIMetricReturnsValue() throws Exception {
		// @formatter:off
		String request = "{\n" + "  \"level\": 3,\n" + "  \"ids\": [\n" + "    \"OPRO Sprint 71_12138_10304_PR\",\n"
				+ "    \"OPRO Sprint 72_12139_10304_PR\"\n" + "  ], " + "  \"kpiList\": [\n" + "    {\n"
				+ "      \"id\": \"5b753628d42937acd035b7ef\",\n" + "      \"kpiId\": \"kpi38\",\n"
				+ "      \"kpiName\": \"Sonar Violations\",\n" + "      \"isDeleted\": \"False\",\n"
				+ "      \"kpiCategory\": \"Quality\",\n" + "      \"kpiUnit\": \"SQALE Rating\",\n"
				+ "      \"kpiSource\": \"Sonar\",\n" + "      \"maxValue\": \"\",\n"
				+ "      \"chartType\": \"gaugeChart\"\n" + "    }\n" + "  ]" + "}";
		// @formatter:on

		List<KpiElement> kpiElementList = new ArrayList<>();
		KpiElement kpiElement = new KpiElement();
		kpiElement.setValue(100);
		kpiElement.setId("5b6be4bf39e7aef89a0fc456");
		kpiElement.setKpiSource("Sonar");
		kpiElement.setKpiId(KPICode.SONAR_VIOLATIONS.getKpiId());
		kpiElementList.add(kpiElement);

		when(sonarService.process(Mockito.any())).thenReturn(kpiElementList);
		mockMvc.perform(post("/sonar/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andExpect(status().is2xxSuccessful());

	}

	@Test
	public void getSonarKPIMetricReturns400() throws Exception {
		// @formatter:off
		String request = "{\n" + "  \"level\": 3,\n" + "  \"ids\": [\n" + "    \"OPRO Sprint 71_12138_10304_PR\",\n"
				+ "    \"OPRO Sprint 72_12139_10304_PR\"\n" + "  ],\n" + "  \"kpiList\": []\n" + "}";
		// @formatter:on

		mockMvc.perform(post("/sonar/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request)).andDo(print())
				.andExpect(status().isBadRequest());

	}

	@Test
	public void getSonarKPIMetricKanbanReturnsValue() throws Exception {

		String request = "{\n" + "  \"level\": 2,\n" + "  \"ids\": [\n" + "    \"PR\",\n" + "    \"10304_PR\"\n"
				+ "  ], " + "  \"kpiList\": [\n" + "    {\n" + "      \"id\": \"5b753628d42937acd035b7ef\",\n"
				+ "      \"kpiId\": \"kpi64\",\n" + "      \"kpiName\": \"Sonar Violations Kanban\",\n"
				+ "      \"isDeleted\": \"False\",\n" + "      \"kpiCategory\": \"Quality\",\n"
				+ "      \"kpiUnit\": \"SQALE Rating\",\n" + "      \"kpiSource\": \"SonarKanban\",\n"
				+ "      \"maxValue\": \"\",\n" + "      \"chartType\": \"gaugeChart\"\n" + "    }\n" + "  ]" + "}";

		KpiRequest req = new KpiRequest();
		req.setLevel(0);
		List<KpiElement> kpiElementList = new ArrayList<>();
		KpiElement kpiElement = new KpiElement();
		kpiElement.setValue(100);
		kpiElement.setId("5b6be4bf39e7aef89a0fc456");
		kpiElement.setKpiSource("SonarKanban");
		kpiElement.setKpiId(KPICode.SONAR_VIOLATIONS_KANBAN.getKpiId());
		kpiElementList.add(kpiElement);

		when(sonarServiceKanban.process(Mockito.any())).thenReturn(kpiElementList);
		mockMvc.perform(post("/sonarkanban/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void getSonarKPIMetricKanbanReturns400() throws Exception {
		// @formatter:off
		String request = "{\n" + "  \"level\": 3,\n" + "  \"ids\": [\n" + "    \"OPRO Sprint 71_12138_10304_PR\",\n"
				+ "    \"OPRO Sprint 72_12139_10304_PR\"\n" + "  ],\n" + "  \"kpiList\": []\n" + "}";
		// @formatter:on

		mockMvc.perform(post("/sonarkanban/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andDo(print()).andExpect(status().isBadRequest());

	}

}
