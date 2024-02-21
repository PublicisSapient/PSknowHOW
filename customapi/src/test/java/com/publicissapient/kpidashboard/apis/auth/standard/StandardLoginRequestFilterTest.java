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

package com.publicissapient.kpidashboard.apis.auth.standard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;

import com.publicissapient.kpidashboard.apis.auth.AuthenticationResultHandler;
import com.publicissapient.kpidashboard.apis.auth.CustomAuthenticationFailureHandler;
import com.publicissapient.kpidashboard.apis.auth.service.AuthTypesConfigService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.application.AuthTypeStatus;

@RunWith(MockitoJUnitRunner.class)
public class StandardLoginRequestFilterTest {

	@Mock
	private AuthenticationManager manager;

	@Mock
	private AuthenticationResultHandler resultHandler;

	@Mock
	private CustomAuthenticationFailureHandler authenticationFailureHandler;

	private String path;

	private StandardLoginRequestFilter filter;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private CustomApiConfig customApiConfig;

	@Mock
	private AuthTypesConfigService authTypesConfigService;

	@Before
	public void setup() {
		path = "/login";
		filter = new StandardLoginRequestFilter(path, manager, resultHandler, authenticationFailureHandler,
				customApiConfig, authTypesConfigService);
	}

	@Test
	public void shouldCreateFilter() {
		assertNotNull(filter);
	}

	@Test(expected = AuthenticationServiceException.class)
	public void shouldThrowExceptionIfNoPost() {
		when(request.getMethod()).thenReturn("GET");
		filter.attemptAuthentication(request, response);
	}

	@Test
	public void shouldAuthenticate() {
		when(request.getMethod()).thenReturn("POST");
		AuthTypeStatus authTypeStatus = new AuthTypeStatus();
		authTypeStatus.setAdLogin(false);
		authTypeStatus.setStandardLogin(true);
		when(authTypesConfigService.getAuthTypesStatus()).thenReturn(authTypeStatus);
		String principal = "user1";
		String credentials = "password1";
		when(request.getParameter("username")).thenReturn(principal + " ");
		when(request.getParameter("password")).thenReturn(credentials);
		Authentication auth = new StandardAuthenticationToken(principal, credentials);
		ArgumentCaptor<Authentication> argumentCaptor = ArgumentCaptor.forClass(Authentication.class);
		when(manager.authenticate(argumentCaptor.capture())).thenReturn(auth);
		Authentication result = filter.attemptAuthentication(request, response);

		assertNotNull(result);
		Authentication authentication = argumentCaptor.getValue();
		assertEquals(principal, authentication.getPrincipal());
		assertEquals(credentials, authentication.getCredentials());
		assertEquals(AuthType.STANDARD, authentication.getDetails());
	}

	@Test
	public void shouldAuthenticateWithNullUsernamePassword() {
		when(request.getMethod()).thenReturn("POST");
		AuthTypeStatus authTypeStatus = new AuthTypeStatus();
		authTypeStatus.setAdLogin(false);
		authTypeStatus.setStandardLogin(true);
		when(authTypesConfigService.getAuthTypesStatus()).thenReturn(authTypeStatus);
		String principal = null;
		String credentials = null;
		when(request.getParameter("username")).thenReturn(principal);
		when(request.getParameter("password")).thenReturn(credentials);
		Authentication auth = new StandardAuthenticationToken(principal, credentials);
		ArgumentCaptor<Authentication> argumentCaptor = ArgumentCaptor.forClass(Authentication.class);
		when(manager.authenticate(argumentCaptor.capture())).thenReturn(auth);

		Authentication result = filter.attemptAuthentication(request, response);

		assertNotNull(result);
		Authentication authentication = argumentCaptor.getValue();
		assertEquals("", authentication.getPrincipal());
		assertEquals("", authentication.getCredentials());
		assertEquals(AuthType.STANDARD, authentication.getDetails());
	}

	@Test(expected = AuthenticationServiceException.class)
	public void shouldThrowExceptionIfDisabled() {
		AuthTypeStatus authTypeStatus = new AuthTypeStatus();
		authTypeStatus.setAdLogin(true);
		authTypeStatus.setStandardLogin(false);
		when(authTypesConfigService.getAuthTypesStatus()).thenReturn(authTypeStatus);
		filter.attemptAuthentication(request, response);
	}

}
