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

package com.publicissapient.kpidashboard.apis.jenkins.rest;

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
import com.publicissapient.kpidashboard.apis.jenkins.service.JenkinsServiceKanbanR;
import com.publicissapient.kpidashboard.apis.jenkins.service.JenkinsServiceR;
import com.publicissapient.kpidashboard.apis.model.KpiElement;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsControllerRTest {

	private MockMvc mockMvc;

	@Mock
	private JenkinsServiceR jenkinsService;

	@Mock
	private JenkinsServiceKanbanR jenkinsServiceKanban;

	@InjectMocks
	private JenkinsController jenkinsController;

	@Mock
	private CacheService cacheService;

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(jenkinsController).build();
	}

	@After
	public void after() {
		mockMvc = null;
	}

	@Test
	public void getJenkinsKPIMetricReturnsValue() throws Exception {
		String request = "{\n" + "  \"kpiList\": [\n" + "    {\n" + "      \"id\": \"5b753628d42937acd035b7ef\",\n"
				+ "      \"kpiId\": \"kpi8\",\n" + "      \"kpiName\": \"Code Build Time\",\n"
				+ "      \"isDeleted\": \"False\",\n" + "      \"kpiCategory\": \"Productivity\",\n"
				+ "      \"kpiUnit\": \"min\",\n" + "      \"kpiSource\": \"Jenkins\",\n"
				+ "      \"maxValue\": \"\",\n" + "      \"chartType\": \"gaugeChart\"\n" + "    }\n" + "  ],\n"
				+ "  \"ids\": [\n" + "    \"GMA_GMA\"\n" + "  ],\n" + "  \"level\": 1\n" + "}";

		List<KpiElement> kpiElementList = new ArrayList<KpiElement>();
		KpiElement kpiElement = new KpiElement();
		kpiElement.setValue(100);
		kpiElement.setId("5b6be4bf39e7aef89a0fc456");
		kpiElement.setKpiSource("Jenkins");
		kpiElementList.add(kpiElement);
		when(jenkinsService.process(Mockito.any())).thenReturn(kpiElementList);
		mockMvc.perform(post("/jenkins/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andExpect(status().is2xxSuccessful());

	}

	@Test
	public void getJenkinsKPIMetricThrowException() throws Exception {

		String request = "{\n" + "  \"level\": 3,\n" + "  \"ids\": [\n" + "    \"OPRO Sprint 71_12138_10304_PR\",\n"
				+ "    \"OPRO Sprint 72_12139_10304_PR\"\n" + "  ],\n" + "  \"kpiList\": []\n" + "}";

		mockMvc.perform(post("/jenkins/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andDo(print()).andExpect(status().isBadRequest());

	}

	@Test
	public void getJenkinsKanbanAggregatedMetricsReturnsValue() throws Exception {
		String request = "{\n" + "  \"kpiList\": [\n" + "    {\n" + "      \"id\": \"5b753628d42937acd035b7ef\",\n"
				+ "      \"kpiId\": \"kpi8\",\n" + "      \"kpiName\": \"Code Build Time\",\n"
				+ "      \"isDeleted\": \"False\",\n" + "      \"kpiCategory\": \"Productivity\",\n"
				+ "      \"kpiUnit\": \"min\",\n" + "      \"kpiSource\": \"Jenkins\",\n"
				+ "      \"maxValue\": \"\",\n" + "      \"chartType\": \"gaugeChart\"\n" + "    }\n" + "  ],\n"
				+ "  \"ids\": [\n" + "    \"GMA_GMA\"\n" + "  ],\n" + "  \"level\": 1\n" + "}";

		List<KpiElement> kpiElementList = new ArrayList<KpiElement>();
		KpiElement kpiElement = new KpiElement();
		kpiElement.setValue(100);
		kpiElement.setId("5b6be4bf39e7aef89a0fc456");
		kpiElement.setKpiSource("SonarKanban");
		kpiElement.setKpiId(KPICode.CODE_BUILD_TIME_KANBAN.getKpiId());
		kpiElementList.add(kpiElement);
		when(jenkinsServiceKanban.process(Mockito.any())).thenReturn(kpiElementList);
		mockMvc.perform(post("/jenkinskanban/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andExpect(status().is2xxSuccessful());

	}

	@Test
	public void getJenkinsKanbanKPIMetricReturns400() throws Exception {

		String request = "{\n" + "  \"level\": 3,\n" + "  \"ids\": [\n" + "    \"OPRO Sprint 71_12138_10304_PR\",\n"
				+ "    \"OPRO Sprint 72_12139_10304_PR\"\n" + "  ],\n" + "  \"kpiList\": []\n" + "}";

		mockMvc.perform(post("/jenkinskanban/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andDo(print()).andExpect(status().isBadRequest());

	}

}
