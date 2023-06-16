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
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.publicissapient.kpidashboard.common.constant.AuthType;

public class AuthenticationUtilTest {

	@Before
	public void setup() {
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("user",
				"password");
		authentication.setDetails(AuthType.STANDARD.name());
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@After
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void shouldGetAuthType() {
		assertEquals(AuthType.STANDARD, AuthenticationUtil.getAuthTypeFromContext());
	}

	@Test
	public void nullAuth() {
		SecurityContextHolder.clearContext();
		assertNull(AuthenticationUtil.getUsernameFromContext());
		assertNull(AuthenticationUtil.getAuthTypeFromContext());
	}

	@Test
	public void getUsernameFromContext() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		AuthenticationUtil.getUsernameFromContext();

	}

	@Test
	public void getAuthTypeFromContext() {
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("user",
				"password");
		authentication.setDetails(AuthType.STANDARD);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		AuthenticationUtil.getAuthTypeFromContext();

	}

}
