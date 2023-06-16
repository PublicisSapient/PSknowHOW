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
package com.publicissapient.kpidashboard.apis.kpis;

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

import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.model.MasterResponse;
import com.publicissapient.kpidashboard.common.model.application.KpiMaster;

/**
 * @author prigupta8
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class KPIMasterControllerTest {

	private MockMvc mockMvc;
	private List<KpiMaster> kpiMasterList;

	@Mock
	private KpiHelperService kpiHelperService;

	@InjectMocks
	private KPIMasterController kpiMasterController;

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(kpiMasterController).build();

		kpiMasterList = new ArrayList<>();
		KpiMaster kpiMaster = new KpiMaster();
		kpiMaster.setKpiId("kpi3");
		kpiMaster.setKpiName("DOR To DOD");
		kpiMaster.setKpiCategory("Productivity");
		kpiMaster.setKpiSource("Jira");
		kpiMaster.setKanban(false);
		kpiMasterList.add(kpiMaster);
	}

	@After
	public void after() {
		mockMvc = null;
	}

	@Test
	public void fetchMasterDataTest() throws Exception {

		when(kpiHelperService.fetchKpiMasterList()).thenReturn(new MasterResponse(kpiMasterList));
		mockMvc.perform(get("/masterData")).andExpect(status().is2xxSuccessful())
				.andExpect(content().contentType("application/json"));
	}
}
