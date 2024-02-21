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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.publicissapient.kpidashboard.apis.auth.model.CustomUserDetails;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.common.constant.AuthType;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAuthenticationResponseServiceTest {

	private static final String USERNAME = "user1";
	private static final Object PASSWORD = "password";

	@Mock
	private TokenAuthenticationService tokenAuthenticationService;

	@Mock
	private UserInfoService userInfoService;

	@InjectMocks
	private DefaultAuthenticationResponseService service;

	private MockHttpServletResponse httpServletResponse;

	private Authentication authentication;

	@Mock
	private Authentication auth;

	@Before
	public void setup() {
		SecurityContextHolder.clearContext();
		httpServletResponse = new MockHttpServletResponse();
		authentication = createAuthentication();
		List<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_VIEWER"),
				new SimpleGrantedAuthority("ROLE_SUPERADMIN"));
		Mockito.doReturn(authorities).when(userInfoService).getAuthorities(anyString());
	}

	@Test
	public void shouldHandleResponse() throws Exception {
		ArgumentCaptor<UsernamePasswordAuthenticationToken> captorAuthentication = ArgumentCaptor
				.forClass(UsernamePasswordAuthenticationToken.class);
		// when(busCompOwnerService.assignOwnerToDashboards("","","",null))
		service.handle(httpServletResponse, authentication);

		verify(tokenAuthenticationService).addAuthentication(Mockito.any(HttpServletResponse.class),
				captorAuthentication.capture());

		UsernamePasswordAuthenticationToken capture = captorAuthentication.getValue();
		assertEquals(USERNAME, capture.getName());
		assertEquals(PASSWORD, capture.getCredentials().toString());

		Collection<GrantedAuthority> authorities = capture.getAuthorities();
		assertEquals(2, authorities.size());
		assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_VIEWER")));
		assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_SUPERADMIN")));

		AuthType details = (AuthType) capture.getDetails();
		assertEquals(AuthType.STANDARD, details);
	}

	private Authentication createAuthentication() {
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
				USERNAME, PASSWORD);
		usernamePasswordAuthenticationToken.setDetails(AuthType.STANDARD);
		return usernamePasswordAuthenticationToken;
	}

	@Test
	public void shouldHandleResponse1() throws Exception {
		ArgumentCaptor<UsernamePasswordAuthenticationToken> captorAuthentication = ArgumentCaptor
				.forClass(UsernamePasswordAuthenticationToken.class);
		CustomUserDetails customUserDetails = new CustomUserDetails();
		customUserDetails.setFirstName("firstName");
		customUserDetails.setMiddleName("middleName");
		customUserDetails.setLastName("lastName");
		customUserDetails.setDisplayName("displayName");
		customUserDetails.setEmailAddress("emailAddress");

		service.handle(httpServletResponse, authentication);

		verify(tokenAuthenticationService).addAuthentication(Mockito.any(HttpServletResponse.class),
				captorAuthentication.capture());

		UsernamePasswordAuthenticationToken capture = captorAuthentication.getValue();
		Collection<GrantedAuthority> authorities = capture.getAuthorities();
		assertEquals(2, authorities.size());
		assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_VIEWER")));
		assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_SUPERADMIN")));

		AuthType details = (AuthType) capture.getDetails();
		assertEquals(AuthType.STANDARD, details);
	}

}
