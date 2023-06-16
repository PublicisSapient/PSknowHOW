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

import javax.validation.constraints.NotNull;

import com.publicissapient.kpidashboard.apis.auth.model.Authentication;

/**
 * The Authentication request.
 */
public class AuthenticationRequest {

	@NotNull
	private String username;

	@NotNull
	private String password;

	@NotNull
	private String email;

	private String userRole;

	/**
	 * Gets username.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets username.
	 *
	 * @param username
	 *            the username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Gets password.
	 *
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets password.
	 *
	 * @param password
	 *            the password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets email.
	 *
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets email.
	 *
	 * @param email
	 *            the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * To authentication authentication.
	 *
	 * @return the authentication
	 */
	public Authentication toAuthentication() {
		return new Authentication(username, password, email);
	}

	/**
	 * Copy to authentication.
	 *
	 * @param authentication
	 *            the authentication
	 * @return the authentication
	 */
	public Authentication copyTo(Authentication authentication) {
		Authentication updated = toAuthentication();
		updated.setId(authentication.getId());
		return updated;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String uRole) {
		userRole = uRole;
	}

}
