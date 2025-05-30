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

package com.publicissapient.kpidashboard.apis.kpis;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.model.FieldMappingStructureResponse;
import com.publicissapient.kpidashboard.apis.util.ProjectAccessUtil;
import com.publicissapient.kpidashboard.common.model.application.FieldMappingStructure;

@RunWith(MockitoJUnitRunner.class)
public class FieldMappingStructureControllerTest {

	private MockMvc mockMvc;

	@Mock
	private KpiHelperService kpiHelperService;

	@InjectMocks
	private FieldMappingStructureController fieldMappingStructureController;

	@Mock
	private FieldMappingStructure fieldMappingStructure = new FieldMappingStructure();

	private List<FieldMappingStructure> fieldMappingStructureList = new ArrayList<>();
	private FieldMappingStructureResponse fieldMappingStructureResponse = new FieldMappingStructureResponse();

	@Mock
	private ProjectAccessUtil projectAccessUtil;

	@Mock
	private UserAuthorizedProjectsService userAuthorizedProjectsService;

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(fieldMappingStructureController).build();
		fieldMappingStructureList.add(fieldMappingStructure);
		fieldMappingStructureResponse.setFieldConfiguration(fieldMappingStructureList);
		fieldMappingStructureResponse.setKpiSource("kpidId");
	}

	@After
	public void after() {
		mockMvc = null;
	}

	@Test
	public void fetchFieldMappingStructureByKpiId() throws Exception {
		when(kpiHelperService.fetchFieldMappingStructureByKpiId("665da03ba3a61d61665d21e1", "kpi0"))
				.thenReturn(fieldMappingStructureResponse);
		when(projectAccessUtil.configIdHasUserAccess(any())).thenReturn(true);
		mockMvc
				.perform(get("/kpiFieldMapping/665da03ba3a61d61665d21e1/kpi0"))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().contentType("application/json"));
	}
}
