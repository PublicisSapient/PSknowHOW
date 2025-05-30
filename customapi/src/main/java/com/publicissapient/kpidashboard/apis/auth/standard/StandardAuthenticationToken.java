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

import java.util.Collection;
import java.util.Objects;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class StandardAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 7187799207155545385L;

	private final transient Object principal;
	private transient Object credentials;

	/**
	 * @param principal
	 * @param credentials
	 */
	@SuppressWarnings("PMD")
	public StandardAuthenticationToken(Object principal, Object credentials) {
		super(null);
		this.principal = principal;
		this.credentials = credentials;
		setAuthenticated(false);
	}

	/**
	 * @param principal
	 * @param credentials
	 * @param authorities
	 */
	@SuppressWarnings("PMD")
	public StandardAuthenticationToken(Object principal, Object credentials,
			Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.principal = principal;
		this.credentials = credentials;
		super.setAuthenticated(true);
	}

	/**
	 * @return credentials
	 */
	@Override
	public Object getCredentials() {
		return this.credentials;
	}

	/**
	 * @return principal
	 */
	@Override
	public Object getPrincipal() {
		return this.principal;
	}

	/**
	 * Sets authenticated false if isAuthneticated is false, Throws
	 * IllegalArgumentException if isAuthneticated is true.
	 */
	@Override
	public void setAuthenticated(boolean isAuthenticated) {
		if (isAuthenticated) {
			throw new IllegalArgumentException(
					"Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
		}

		super.setAuthenticated(false);
	}

	/** Erases credentials and sets credentials object to null */
	@Override
	public void eraseCredentials() {
		super.eraseCredentials();
		credentials = null;
	}

	/**
	 * Overridden method of Object's equal method compares principal and credentials
	 * object to check StandardAuthenticationToken is equal
	 *
	 * @return true if invoked object's principal and credentials are matching,
	 *         false if they are not matching
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		if (!super.equals(obj)) {
			return false;
		}
		StandardAuthenticationToken that = (StandardAuthenticationToken) obj;
		return Objects.equals(principal, that.principal) && Objects.equals(credentials, that.credentials);
	}

	/**
	 * Overridden method of Object's hashcode method
	 *
	 * @return hashcode
	 */
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), principal, credentials);
	}
}
