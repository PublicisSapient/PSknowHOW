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

package com.publicissapient.kpidashboard.apis.appsetting.rest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.publicissapient.kpidashboard.apis.appsetting.service.EditKpiConfigService;
import com.publicissapient.kpidashboard.apis.util.TestUtil;

/**
 * This class provides various methods to TEST operations on EditKPIConfig
 * controller
 *
 * @author jagmongr
 */

@RunWith(MockitoJUnitRunner.class)
public class EditKpiConfigControllerTest {

	@InjectMocks
	EditKpiConfigController editKpiConfigController;
	@Mock
	private EditKpiConfigService editKpiConfigService;
	private MockMvc mockMvc;

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(editKpiConfigController).build();
	}

	@After
	public void after() {
		mockMvc = null;
	}

	@Before
	public void setUp() {

		List<String> editKpiConfigType = Arrays.asList("fields");
		List<String> typeList = new ArrayList<>();
		typeList.addAll(editKpiConfigType);

		List<String> projectconfigidList = new ArrayList<>();
		projectconfigidList.add("5f7ee917485b2c09bc8bac7a");

	}

	@Test
	public void getTestDataForType() throws NullPointerException {

		try {
			mockMvc.perform(MockMvcRequestBuilders.get("/editConfig/jira/editKpi/5f7ee917485b2c09bc8bac7a")
					.contentType(TestUtil.APPLICATION_JSON_UTF8)).andExpect(status().isOk());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}