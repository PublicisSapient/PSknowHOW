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

package com.publicissapient.kpidashboard.apis.auth.ldap;

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
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.Authentication;

import com.publicissapient.kpidashboard.apis.activedirectory.service.ADServerDetailsService;
import com.publicissapient.kpidashboard.apis.auth.AuthenticationResultHandler;
import com.publicissapient.kpidashboard.apis.auth.CustomAuthenticationFailureHandler;
import com.publicissapient.kpidashboard.apis.auth.service.AuthTypesConfigService;
import com.publicissapient.kpidashboard.apis.auth.standard.StandardAuthenticationToken;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.common.activedirectory.modal.ADServerDetail;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.application.AuthTypeStatus;

@RunWith(MockitoJUnitRunner.class)
public class LdapLoginRequestFilterTest {

	ADServerDetail adUserDetail = null;
	@Mock
	private AuthenticationManager manager;
	@Mock
	private AuthenticationResultHandler authenticationResultHandler;
	@Mock
	private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
	@Mock
	private AuthenticationResultHandler resultHandler;
	private String path;
	private LdapLoginRequestFilter filter;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private ADServerDetailsService adServerDetailsService;
	@Mock
	private AuthTypesConfigService authTypesConfigService;

	@Before
	public void setup() {
		path = "/ldap";
		filter = new LdapLoginRequestFilter(path, manager, resultHandler, customAuthenticationFailureHandler,
				customApiConfig, adServerDetailsService, authTypesConfigService);
		adUserDetail = new ADServerDetail();
		adUserDetail.setDomain("domain");
		adUserDetail.setHost("host");
		adUserDetail.setPassword("password");
		adUserDetail.setRootDn("rootDn");
		adUserDetail.setUserDn("userDn");
		adUserDetail.setUsername("username");
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
		String principal = "user1";
		String credentials = "password1";
		when(request.getParameter("username")).thenReturn(principal + " ");
		when(request.getParameter("password")).thenReturn(credentials);
		Authentication auth = new StandardAuthenticationToken(principal, credentials);
		ArgumentCaptor<Authentication> argumentCaptor = ArgumentCaptor.forClass(Authentication.class);
		when(manager.authenticate(argumentCaptor.capture())).thenReturn(auth);
		when(adServerDetailsService.getADServerConfig()).thenReturn(adUserDetail);
		Authentication result = filter.attemptAuthentication(request, response);

		assertNotNull(result);
		Authentication authentication = argumentCaptor.getValue();
		assertEquals(principal, authentication.getPrincipal());
		assertEquals(AuthType.LDAP, authentication.getDetails());
	}

	@Test
	public void shouldAuthenticateWithNullUsernamePassword() {
		when(request.getMethod()).thenReturn("POST");
		String principal = null;
		String credentials = null;
		when(request.getParameter("username")).thenReturn(principal);
		when(request.getParameter("password")).thenReturn(credentials);
		Authentication auth = new StandardAuthenticationToken(principal, credentials);
		ArgumentCaptor<Authentication> argumentCaptor = ArgumentCaptor.forClass(Authentication.class);
		when(manager.authenticate(argumentCaptor.capture())).thenReturn(auth);
		when(adServerDetailsService.getADServerConfig()).thenReturn(adUserDetail);

		Authentication result = filter.attemptAuthentication(request, response);

		assertNotNull(result);
		Authentication authentication = argumentCaptor.getValue();
		assertEquals("", authentication.getPrincipal());
		assertEquals("", authentication.getCredentials());
		assertEquals(AuthType.LDAP, authentication.getDetails());
	}

	@Test(expected = ProviderNotFoundException.class)
	public void authenticateWithoutProvider() {
		when(request.getMethod()).thenReturn("POST");
		String principal = null;
		String credentials = null;
		when(request.getParameter("username")).thenReturn(principal);
		when(request.getParameter("password")).thenReturn(credentials);
		adUserDetail = null;
		when(adServerDetailsService.getADServerConfig()).thenReturn(adUserDetail);

		filter.attemptAuthentication(request, response);
	}

	@Test(expected = AuthenticationServiceException.class)
	public void shouldThrowExceptionIfDisabled() {
		AuthTypeStatus authTypeStatus = new AuthTypeStatus();
		authTypeStatus.setAdLogin(false);
		authTypeStatus.setStandardLogin(true);
		when(authTypesConfigService.getAuthTypesStatus()).thenReturn(authTypeStatus);
		filter.attemptAuthentication(request, response);
	}

}