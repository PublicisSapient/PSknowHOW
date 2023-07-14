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

package com.publicissapient.kpidashboard.apis.common.rest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.publicissapient.kpidashboard.apis.common.service.VersionMetadataService;
import com.publicissapient.kpidashboard.apis.util.TestUtil;

@RunWith(MockitoJUnitRunner.class)
public class VersionMetadataControllerTest {

	@InjectMocks
	VersionMetadataController versionMetadataController;
	@Mock
	private VersionMetadataService versionMetadataService;
	private MockMvc mockMvc;

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(versionMetadataController).build();
	}

	@After
	public void after() {
		mockMvc = null;
	}

	@Test
	public void getTestGetAllAccount() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/getversionmetadata").contentType(TestUtil.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk());
	}

}
