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
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

public class AuthPropertiesTest {

	private AuthProperties tokenAuthProperties;

	@Before
	public void setup() {
		tokenAuthProperties = new AuthProperties();
	}

	@Test
	public void testApplyDefaultsIfNeeded() {
		tokenAuthProperties.setExpirationTime(null);
		tokenAuthProperties.setSecret(null);

		tokenAuthProperties.applyDefaultsIfNeeded();

		assertNotNull(tokenAuthProperties.getExpirationTime());
		assertNotNull(tokenAuthProperties.getSecret());
	}

	@Test
	public void testApplyDefaultsNotNeeded() {
		tokenAuthProperties.setExpirationTime(8L);
		tokenAuthProperties.setSecret("secret");
		tokenAuthProperties.setLdapServerUrl("url");
		tokenAuthProperties.setLdapUserDnPattern("pattern");

		tokenAuthProperties.applyDefaultsIfNeeded();

		assertEquals(Long.valueOf(8), tokenAuthProperties.getExpirationTime());
		assertEquals("secret", tokenAuthProperties.getSecret());
		assertEquals("url", tokenAuthProperties.getLdapServerUrl());
		assertEquals("pattern", tokenAuthProperties.getLdapUserDnPattern());
	}

}
