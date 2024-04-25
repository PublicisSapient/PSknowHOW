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

package com.publicissapient.kpidashboard.jira.service;

import com.atlassian.httpclient.api.Request;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;

/**
 * Authentication handler for bearer token
 */
public class BearerTokenAuthenticationHandler implements AuthenticationHandler {

	private static final String AUTHORIZATION_HEADER = "Authorization";

	private static final String BEARER = "Bearer ";

	private final String bearerToken;

	public BearerTokenAuthenticationHandler(final String bearerToken) {
		this.bearerToken = bearerToken;
	}

	@Override
	public void configure(Request.Builder builder) {
		builder.setHeader(AUTHORIZATION_HEADER, BEARER + getBearerToken());
	}

	/**
	 * This method return bearer token
	 * 
	 * @return token
	 */
	private String getBearerToken() {
		return bearerToken;
	}
}