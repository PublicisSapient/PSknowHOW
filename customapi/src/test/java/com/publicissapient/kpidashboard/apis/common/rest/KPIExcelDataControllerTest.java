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

/**
 * 
 */
package com.publicissapient.kpidashboard.apis.common.rest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.publicissapient.kpidashboard.apis.appsetting.service.KPIExcelDataService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelValidationDataResponse;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;

/**
 * @author tauakram
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class KPIExcelDataControllerTest {

	private MockMvc mockMvc;
	private Map<String, ValidationData> kpiValidationDataMap = new HashMap<>();
	private KPIExcelValidationDataResponse kpiExcelValidationDataResponse = new KPIExcelValidationDataResponse();

	@Mock
	private KPIExcelDataService kpiExcelDataService;

	@InjectMocks
	private KPIExcelDataController kpiExcelDataController;

	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(kpiExcelDataController).build();

		ValidationData validationData = new ValidationData();
		validationData.setStoryKeyList(
				Arrays.asList("Project1_Sprint1_Story1", "Project1_Sprint1_Story2", "Project1_Sprint1_Story3"));
		validationData.setDefectKeyList(
				Arrays.asList("Project1_Sprint1_Defect1", "Project1_Sprint1_Defect2", "Project1_Sprint1_Defect3"));

		kpiValidationDataMap.put("Project1_Sprint1", validationData);

		kpiExcelValidationDataResponse.setMapOfSprintAndData(kpiValidationDataMap);

	}

	@After
	public void after() {
		mockMvc = null;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetKpiValidationData() throws Exception {
		// TODO GIRISH here discuss with team
		String request = "{\n" + "  \"kpiList\": [\n" + "    {\n" + "      \"id\": \"5b753628d42937acd035b7ef\",\n"
				+ "      \"kpiId\": \"kpi14\",\n" + "      \"kpiName\": \"Defect Injection Rate\",\n"
				+ "      \"isDeleted\": \"False\",\n" + "      \"kpiCategory\": \"Quality\",\n"
				+ "      \"kpiUnit\": \"Percentage\",\n" + "      \"kpiSource\": \"Jira\",\n"
				+ "      \"maxValue\": \"\",\n" + "      \"chartType\": \"gaugeChart\"\n" + "    }\n" + "  ],\n"
				+ "  \"ids\": [\n" + "    \"GMA_GMA\"\n" + "  ],\n" + "  \"level\": 1\n" + "}";
		when(kpiExcelDataService.process(Mockito.anyString(), Mockito.anyInt(), Mockito.any(),
				(List<String>) Mockito.isNull(), Mockito.any(), (Boolean) Mockito.isNull()))
						.thenReturn((KPIExcelValidationDataResponse) kpiExcelValidationDataResponse);

		mockMvc.perform(post("/v1/kpi/kpi14").header("x-filter-level", 1)
				.header("x-filter-id", Arrays.asList("GMA_GMA", "CIM_CIM")).contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(request)).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void testGetKpiData_invalidHeader() throws Exception {

		mockMvc.perform(get("/v1/kpi").header("x-filter-level", "test_file_for_upload").header("x-filter-id",
				Arrays.asList("GMA_GMA", "CIM_CIM"))).andExpect(status().is4xxClientError());
	}

	@Test
	public void testGetKpiData_missingHeader() throws Exception {

		mockMvc.perform(get("/v1/kpi").header("x-filter-level", 1)).andExpect(status().is4xxClientError());
	}

}
