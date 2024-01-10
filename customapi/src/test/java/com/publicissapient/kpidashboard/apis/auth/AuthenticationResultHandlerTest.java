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

package com.publicissapient.kpidashboard.apis.auth;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;

import com.publicissapient.kpidashboard.apis.auth.model.CustomUserDetails;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.CustomAnalyticsService;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationResultHandlerTest {

	@Mock
	private AuthenticationResponseService authenticationResponseService;

	@Mock
	private HttpServletResponse response;

	@Mock
	private Authentication authentication;

	@InjectMocks
	private AuthenticationResultHandler handler;

	@Mock
	private CustomAnalyticsService customAnalyticsService;

	@Mock
	private PrintWriter servletOutputStream;

	@Mock
	private AuthenticationService authenticationService;

	@Test
	public void testOnSucess() throws IOException, ServletException {
		JSONObject jsonObject = new JSONObject();
		Mockito.when(customAnalyticsService.addAnalyticsData(response, "userName")).thenReturn(jsonObject);
		Mockito.when(response.getWriter()).thenReturn(servletOutputStream);
		Mockito.doNothing().when(servletOutputStream).print(Mockito.anyString());
		when(authenticationService.getUsername(authentication)).thenReturn("userName");
		handler.onAuthenticationSuccess(null, response, authentication);
		verify(authenticationResponseService).handle(response, authentication);
	}

	@Test
	public void testOnSucess1() throws IOException, ServletException {
		CustomUserDetails cud = new CustomUserDetails();
		cud.setUsername("userName");
		JSONObject jsonObject = new JSONObject();
		Mockito.when(customAnalyticsService.addAnalyticsData(response, "userName")).thenReturn(jsonObject);
		Mockito.when(response.getWriter()).thenReturn(servletOutputStream);
		Mockito.doNothing().when(servletOutputStream).print(Mockito.anyString());
		when(authenticationService.getUsername(authentication)).thenReturn("userName");
		handler.onAuthenticationSuccess(null, response, authentication);
		verify(authenticationResponseService).handle(response, authentication);
	}

}
