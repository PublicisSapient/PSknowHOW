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

package com.publicissapient.kpidashboard.apis.auth.apitoken;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.simple.parser.ParseException;
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
import com.publicissapient.kpidashboard.common.constant.AuthType;

@RunWith(MockitoJUnitRunner.class)
public class ApiTokenRequestFilterTest {

	@Mock
	private AuthenticationManager manager;

	@Mock
	private AuthenticationResultHandler resultHandler;

	private String path;

	private ApiTokenRequestFilter filter;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Before
	public void setup() {
		path = "/**";
		filter = new ApiTokenRequestFilter(path, manager, resultHandler);
	}

	@Test
	public void shouldCreateFilter() {
		assertNotNull(filter);
	}

	@Test
	public void shouldAuthenticate() {
		String principal = "somesys";
		String credentials = "itWuQ7y5zVKX1n+k8trjCNnx99o7AXbO";
		String authHdr = "Basic UGFzc3dvcmRJc0F1dGhUb2tlbjp7ImFwaUtleSI6Iml0V3VRN3k1elZLWDFuK2s4dHJqQ05ueDk5bzdBWGJPIn0K";
		when(request.getHeader("apiUser")).thenReturn(principal);
		when(request.getHeader("Authorization")).thenReturn(authHdr);
		Authentication auth = new ApiTokenAuthenticationToken(principal, authHdr);
		ArgumentCaptor<Authentication> argumentCaptor = ArgumentCaptor.forClass(Authentication.class);
		when(manager.authenticate(argumentCaptor.capture())).thenReturn(auth);

		Authentication result = filter.attemptAuthentication(request, response);

		assertNotNull(result);
		Authentication authentication = argumentCaptor.getValue();
		assertEquals(principal, authentication.getPrincipal());
		assertEquals(credentials, authentication.getCredentials());
		assertEquals(AuthType.APIKEY, authentication.getDetails());
	}

	@Test
	public void attemptAuthentication_Fail() throws ParseException {
		String principal = "somesys";
		String credentials = "itWuQ7y5zVKX1n+k8trjCNnx99o7AXdf--d---d";
		String authHdr = "Basic UGFzc3dvcmRJc0F1dGhUb2tlbjp7ImFwaUtleSI6Iml0V3VRN3k1elZLWDFuK2s4dHJqQ05ueDk5bzdBWGJPIn0K";
		when(request.getHeader("apiUser")).thenReturn(principal);
		when(request.getHeader("Authorization")).thenReturn(authHdr);
		Authentication auth = new ApiTokenAuthenticationToken(principal, authHdr);
		ArgumentCaptor<Authentication> argumentCaptor = ArgumentCaptor.forClass(Authentication.class);
		when(manager.authenticate(argumentCaptor.capture())).thenReturn(auth);
		Authentication result = filter.attemptAuthentication(request, response);

		assertFalse(result.isAuthenticated());
	}

	@Test(expected = AuthenticationServiceException.class)
	public void attemptAuthentication_Exception() {
		String principal = "somesys";
		String credentials = "itWuQ7y5zVKX1n+k8trjCNnx99o7AXdf";
		String authHdr = "Basic UGFzc3dvcmRJc0F1dGhUb2tlbjp7ImFwaUtleSI6Iml0V3VRN3k1elZLWDFuK2s4dHJqQ05ueDk5bzdBWGJPIn[]{";
		when(request.getHeader("apiUser")).thenReturn(principal);
		when(request.getHeader("Authorization")).thenReturn(authHdr);
		Authentication auth = new ApiTokenAuthenticationToken(principal, authHdr);
		ArgumentCaptor<Authentication> argumentCaptor = ArgumentCaptor.forClass(Authentication.class);

		Authentication result = filter.attemptAuthentication(request, response);

	}

	@Test
	public void testFilter() throws IOException, ServletException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);

		String principal = "somesys";
		String credentials = "itWuQ7y5zVKX1n+k8trjCNnx99o7AXbO";
		String authHdr = "Basic UGFzc3dvcmRJc0F1dGhUb2tlbjp7ImFwaUtleSI6Iml0V3VRN3k1elZLWDFuK2s4dHJqQ05ueDk5bzdBWGJPIn0K";
		when(request.getHeader("apiUser")).thenReturn(principal);
		when(request.getHeader("Authorization")).thenReturn(authHdr);
		Authentication auth = new ApiTokenAuthenticationToken(principal, authHdr);
		ArgumentCaptor<Authentication> argumentCaptor = ArgumentCaptor.forClass(Authentication.class);
		when(manager.authenticate(argumentCaptor.capture())).thenReturn(auth);

		filter.doFilter(request, response, chain);
		assertEquals(0, response.getStatus());

	}

	@Test
	public void testFilter2() throws IOException, ServletException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);

		String principal = "somesys";
		String credentials = "itWuQ7y5zVKX1n+k8trjCNnx99o7AXbO";
		String authHdr = "Basic UGFzc3dvcmRJc0F1dGhUb2tlbjp7ImFwaUtleSI6Iml0V3VRN3k1elZLWDFuK2s4dHJqQ05ueDk5bzdBWGJPIn0K";
		when(request.getHeader("apiUser")).thenReturn(null);
		when(request.getHeader("Authorization")).thenReturn(null);
		Authentication auth = new ApiTokenAuthenticationToken(principal, authHdr);
		ArgumentCaptor<Authentication> argumentCaptor = ArgumentCaptor.forClass(Authentication.class);

		filter.doFilter(request, response, chain);
		assertEquals(0, response.getStatus());

	}
}