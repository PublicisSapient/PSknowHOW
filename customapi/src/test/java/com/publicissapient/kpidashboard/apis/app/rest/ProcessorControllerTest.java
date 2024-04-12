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

package com.publicissapient.kpidashboard.apis.app.rest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.publicissapient.kpidashboard.apis.appsetting.rest.ProcessorController;
import com.publicissapient.kpidashboard.apis.appsetting.service.ProcessorService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.util.TestUtil;
import com.publicissapient.kpidashboard.common.model.generic.Processor;

/**
 * This class contains test cases for ProcessorController.class
 *
 * @author pansharm5
 */

@RunWith(MockitoJUnitRunner.class)
public class ProcessorControllerTest {

	private MockMvc mockMvc;

	@InjectMocks
	private ProcessorController processorController;

	@Mock
	private ProcessorService processorService;

	/**
	 * method includes preprocesses for test cases
	 */
	@Before
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(processorController).build();

	}

	/**
	 * method includes post processes for test cases
	 */
	@After
	public void after() {
		mockMvc = null;
	}

	/**
	 * method to test /processor restPoint ; Get All Processors
	 *
	 * @throws Exception
	 */
	@Test
	public void test_getData() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/processor").contentType(TestUtil.APPLICATION_JSON_UTF8))
				.andExpect(status().is5xxServerError());
	}

	/**
	 * method to test /processor restPoint ; Get All Processors
	 *
	 * @throws Exception
	 */
	@Test
	public void test_getData200() throws Exception {
		List<Processor> listProcessor = new ArrayList<>();
		Mockito.when(processorService.getAllProcessorDetails())
				.thenReturn(new ServiceResponse(true, StringUtils.EMPTY, listProcessor));
		mockMvc.perform(MockMvcRequestBuilders.get("/processor").contentType(TestUtil.APPLICATION_JSON_UTF8))
				.andExpect(status().is2xxSuccessful());
	}

}
