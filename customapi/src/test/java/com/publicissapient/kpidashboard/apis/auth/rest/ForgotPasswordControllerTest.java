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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;


import jakarta.servlet.http.HttpServletRequest;
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

import com.publicissapient.kpidashboard.apis.auth.service.ForgotPasswordRequest;
import com.publicissapient.kpidashboard.apis.auth.service.ForgotPasswordService;
import com.publicissapient.kpidashboard.apis.auth.service.ResetPasswordRequest;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.ResetPasswordTokenStatusEnum;
import com.publicissapient.kpidashboard.apis.util.TestUtil;
import com.publicissapient.kpidashboard.common.exceptions.ApplicationException;

/**
 * 
 * @author vijmishr1
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ForgotPasswordControllerTest extends Mockito {

	private MockMvc mockMvc;

	@InjectMocks
	private ForgotPasswordController forgotPasswordController;

	@Mock
	private ForgotPasswordService forgotPasswordService;

	@Mock
	private HttpServletRequest httpServletRequest;
	@Mock
	private CustomApiConfig customApiConfig;

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(forgotPasswordController).build();
	}

	@After
	public void after() {
		mockMvc = null;
	}

	@Test
	public void processForgotPasswordTest() throws Exception {
		ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest();
		forgotPasswordRequest.setEmail("abc@xyz.com");
		mockMvc.perform(MockMvcRequestBuilders.post("/forgotPassword").contentType(TestUtil.APPLICATION_JSON_UTF8)
				.content(TestUtil.convertObjectToJsonBytes(forgotPasswordRequest))).andExpect(status().isBadRequest());
	}

	@Test
	public void processForgotPasswordTestWithException() throws Exception {
		when(customApiConfig.getUiHost()).thenReturn("localhost");

		when(forgotPasswordService.processForgotPassword(any(), any())).thenReturn(null);
		ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest();
		forgotPasswordRequest.setEmail("abc@xyz.com");
		mockMvc.perform(MockMvcRequestBuilders.post("/forgotPassword").contentType(TestUtil.APPLICATION_JSON_UTF8)
				.content(TestUtil.convertObjectToJsonBytes(forgotPasswordRequest))).andExpect(status().isOk());
	}

	@Test()
	public void validateTokenTest() throws Exception {
		when(customApiConfig.getUiHost()).thenReturn("localhost");
		when(customApiConfig.getUiPort()).thenReturn("9999");

		when(forgotPasswordService.validateEmailToken(any())).thenReturn(ResetPasswordTokenStatusEnum.VALID);
		mockMvc.perform(MockMvcRequestBuilders.get("/validateToken").contentType(TestUtil.APPLICATION_JSON_UTF8)
				.param("token", UUID.randomUUID().toString()));
	}

	@Test
	public void updatePasswordTest() throws Exception {
		ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
		resetPasswordRequest.setResetToken("resetToken");
		resetPasswordRequest.setPassword("password");
		mockMvc.perform(MockMvcRequestBuilders.post("/resetPassword").contentType(TestUtil.APPLICATION_JSON_UTF8)
				.content(TestUtil.convertObjectToJsonBytes(resetPasswordRequest))).andExpect(status().isOk());
	}

	@Test
	public void updatePasswordWithExceptionTest() throws Exception {
		when(forgotPasswordService.resetPassword(any()))
				.thenThrow(new ApplicationException("Token is ", ApplicationException.BAD_DATA));
		ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
		mockMvc.perform(MockMvcRequestBuilders.post("/resetPassword").contentType(TestUtil.APPLICATION_JSON_UTF8)
				.content(TestUtil.convertObjectToJsonBytes(resetPasswordRequest))).andExpect(status().isBadRequest());
	}

}
