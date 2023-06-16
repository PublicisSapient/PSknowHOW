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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.Sets;

public class StandardAuthenticationTokenTest {

	private static final String CREDS = "creds";
	private static final String PRINCIPAL = "principal";

	@Test
	public void shouldCreateTokenNotAuthenticated() {
		Authentication auth = new StandardAuthenticationToken(PRINCIPAL, CREDS);
		assertEquals(PRINCIPAL, auth.getPrincipal());
		assertEquals(CREDS, auth.getCredentials());
		assertFalse(auth.isAuthenticated());
	}

	@Test
	public void shouldCreateTokenAuthenitcated() {
		Collection<? extends GrantedAuthority> authorities = Sets.newHashSet();
		Authentication auth = new StandardAuthenticationToken(PRINCIPAL, CREDS, authorities);
		assertEquals(PRINCIPAL, auth.getPrincipal());
		assertEquals(CREDS, auth.getCredentials());
		assertTrue(auth.isAuthenticated());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldSetAuthenticatedException() {
		Authentication auth = new StandardAuthenticationToken(PRINCIPAL, CREDS);
		auth.setAuthenticated(true);
	}

	@Test
	public void shouldSetAuthenticatedToFalse() {
		Collection<? extends GrantedAuthority> authorities = Sets.newHashSet();
		Authentication auth = new StandardAuthenticationToken(PRINCIPAL, CREDS, authorities);
		auth.setAuthenticated(false);
		assertFalse(auth.isAuthenticated());
	}

	@Test
	public void shouldEraseCreds() {
		Collection<? extends GrantedAuthority> authorities = Sets.newHashSet();
		StandardAuthenticationToken auth = new StandardAuthenticationToken(PRINCIPAL, CREDS, authorities);
		auth.eraseCredentials();
		assertNull(auth.getCredentials());
	}

	@Test
	public void equals() {
		StandardAuthenticationToken obj = new StandardAuthenticationToken(PRINCIPAL, CREDS);
		assertTrue(obj.equals(obj));
		;

	}

	@Test
	public void NotEquals() {
		Collection<? extends GrantedAuthority> authorities = Sets.newHashSet();
		StandardAuthenticationToken obj = new StandardAuthenticationToken(PRINCIPAL, CREDS);
		StandardAuthenticationToken obj1 = new StandardAuthenticationToken(PRINCIPAL, authorities);
		assertFalse(obj.equals(obj1));
		;

	}

	@Test
	public void nullObj() {
		StandardAuthenticationToken obj = new StandardAuthenticationToken(PRINCIPAL, CREDS);
		assertFalse(obj.equals(null));
		;

	}

	@Test
	public void notNullObj() {
		StandardAuthenticationToken obj = new StandardAuthenticationToken(PRINCIPAL, CREDS);
		obj.equals(obj.getPrincipal());

	}

	@Test
	public void ObjGetClass() {
		StandardAuthenticationToken obj = new StandardAuthenticationToken(PRINCIPAL, CREDS);
		assertFalse(getClass().equals(obj.getClass()));

	}

	@Test
	public void ObjGetClasstrue() {
		StandardAuthenticationTokenTest obj = new StandardAuthenticationTokenTest();
		assertTrue(getClass().equals(obj.getClass()));

	}

	@Test
	public void equalother() {
		Object obj = new StandardAuthenticationToken(PRINCIPAL, CREDS);
		StandardAuthenticationToken that = (StandardAuthenticationToken) obj;
		Object principal = "principal";
		Object credentials = "creds";
		assertTrue(principal.equals(that.getPrincipal()));
		assertTrue(credentials.equals(that.getCredentials()));

	}

	@Test
	public void hashCodes() {
		hashCode();
	}

}
