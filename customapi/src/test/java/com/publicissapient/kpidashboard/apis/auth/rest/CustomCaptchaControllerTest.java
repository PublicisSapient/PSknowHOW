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

package com.publicissapient.kpidashboard.apis.auth.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.publicissapient.kpidashboard.apis.auth.service.CaptchaService;

@RunWith(MockitoJUnitRunner.class)
public class CustomCaptchaControllerTest {

	private MockMvc mockMvc;

	@Mock
	private CaptchaService captchaService;

	@InjectMocks
	private CustomCaptchaController customCaptchaController;

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(customCaptchaController).build();
	}

	@After
	public void after() {
		mockMvc = null;
	}

	@Test
	public void testCaptcha() throws Exception {
		mockMvc.perform(get("/login/captcha")).andExpect(status().isOk());
	}
}
