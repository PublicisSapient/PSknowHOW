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

package com.publicissapient.kpidashboard.apis.auth.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.publicissapient.kpidashboard.apis.auth.model.SystemUser;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAuthenticationServiceImplTest {
	@InjectMocks
	DefaultAuthenticationServiceImpl yourService;
	@Mock
	private SecurityContext securityContext;

	@Mock
	private Authentication authentication;

	@Test
	public void testGetLoggedInUser_AuthenticatedUser() {
		// Arrange
		String expectedUsername = "testUser ";
		when(authentication.getName()).thenReturn(expectedUsername);
		when(authentication.isAuthenticated()).thenReturn(true);
		when(authentication.getPrincipal()).thenReturn(expectedUsername);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);

		// Act
		String loggedInUser = yourService.getLoggedInUser();

		// Assert
		assertEquals(expectedUsername, loggedInUser);
	}

	@Test
	public void testGetLoggedInUser_AnonymousUser() {
		// Arrange
		when(authentication.getName()).thenReturn("anonymousUser");
		when(authentication.isAuthenticated()).thenReturn(true);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);

		// Act
		String loggedInUser = yourService.getLoggedInUser();

		// Assert
		assertEquals(SystemUser.SYSTEM.getName(), loggedInUser);
	}

	@Test
	public void testGetLoggedInUser_NoAuthentication() {
		// Arrange
		when(securityContext.getAuthentication()).thenReturn(null);
		SecurityContextHolder.setContext(securityContext);

		// Act
		String loggedInUser = yourService.getLoggedInUser();

		// Assert
		assertEquals(SystemUser.SYSTEM.getName(), loggedInUser);
	}
}
