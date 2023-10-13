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
 * Custom SPNEGO Authentication handler for jira HTTP request
 */
public class SpnegoAuthenticationHandler implements AuthenticationHandler {

	private static final String COOKIE_HEADER = "Cookie";

	private final String authCookies;

	/**
	 * Constructor for authentication handler
	 * 
	 * @param authCookies
	 *            authCookies
	 */
	public SpnegoAuthenticationHandler(final String authCookies) {
		this.authCookies = authCookies;
	}

	/**
	 * overridden configure method
	 * 
	 * @param builder
	 *            builder
	 */
	@Override
	public void configure(Request.Builder builder) {
		builder.setHeader(COOKIE_HEADER, authCookies);
	}
}
