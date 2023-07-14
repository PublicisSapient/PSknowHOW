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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.publicissapient.kpidashboard.apis.auth.service.ApiTokenService;

@RunWith(MockitoJUnitRunner.class)
public class ApiTokenAuthenticationProviderTest {

	@Mock
	private ApiTokenService service;

	@InjectMocks
	private ApiTokenAuthenticationProvider provider;

	@Test
	public void shouldAuthenticate() {
		String username = "username";
		String password = "password";
		Authentication auth = new ApiTokenAuthenticationToken(username, password);
		when(service.authenticate(username, password)).thenReturn(auth);

		Authentication result = provider.authenticate(auth);

		assertSame(auth, result);
	}

	@Test
	public void shouldSupportStandardAuthToken() {
		assertTrue(provider.supports(ApiTokenAuthenticationToken.class));
		assertFalse(provider.supports(UsernamePasswordAuthenticationToken.class));
	}
}
