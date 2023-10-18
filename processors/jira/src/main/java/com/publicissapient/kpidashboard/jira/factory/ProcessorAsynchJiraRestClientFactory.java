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

package com.publicissapient.kpidashboard.jira.factory;

import java.net.URI;

import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorAsynchJiraRestClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.service.BearerTokenAuthenticationHandler;
import com.publicissapient.kpidashboard.jira.service.SpnegoAuthenticationHandler;

public class ProcessorAsynchJiraRestClientFactory extends AsynchronousJiraRestClientFactory {

	/**
	 * Creates JIRA Client
	 *
	 * @param serverUri
	 *            Jira Server URI
	 * @param authenticationHandler
	 *            Authentication handler instance
	 * @param jiraProcessorConfig
	 *            Jira processor config
	 * @return ProcessorJiraRestClient
	 */
	public ProcessorJiraRestClient create(final URI serverUri, final AuthenticationHandler authenticationHandler,
			JiraProcessorConfig jiraProcessorConfig) {
		final DisposableHttpClient httpClient = new ProcessorAsynchHttpClientFactory().createProcessorClient(serverUri,
				authenticationHandler, jiraProcessorConfig);
		return new ProcessorAsynchJiraRestClient(serverUri, httpClient);
	}

	/**
	 * Creates JIRA client with Basic HTTP Authentication
	 *
	 * @param serverUri
	 *            Jira Server URI
	 * @param username
	 *            Jira login username
	 * @param password
	 *            Jira Login password
	 * @param jiraProcessorConfig
	 *            Jira processor config
	 * @return ProcessorJiraRestClient
	 */
	public ProcessorJiraRestClient createWithBasicHttpAuthentication(final URI serverUri, final String username,
			final String password, JiraProcessorConfig jiraProcessorConfig) {
		return create(serverUri, new BasicHttpAuthenticationHandler(username, password), jiraProcessorConfig);
	}

	/**
	 * Creates JIRA client with Bearer Token Authentication
	 *
	 * @param serverUri
	 *            Jira Server URI
	 * @param bearerToken
	 *            Jira Login password
	 * @param jiraProcessorConfig
	 *            Jira processor config
	 * @return ProcessorJiraRestClient
	 */
	public ProcessorJiraRestClient createWithBearerTokenAuthentication(final URI serverUri, final String bearerToken,
			JiraProcessorConfig jiraProcessorConfig) {
		return create(serverUri, new BearerTokenAuthenticationHandler(bearerToken), jiraProcessorConfig);

	}

	/**
	 * Creates JIRA client with Basic HTTP Authentication
	 *
	 * @param serverUri
	 *            Jira Server URI
	 * @param authCookies
	 *            Authorization cookies
	 * @param jiraProcessorConfig
	 *            Jira processor config
	 * @return ProcessorJiraRestClient
	 */
	public ProcessorJiraRestClient createWithAuthenticationCookies(final URI serverUri, final String authCookies,
			JiraProcessorConfig jiraProcessorConfig) {
		return create(serverUri, new SpnegoAuthenticationHandler(authCookies), jiraProcessorConfig);
	}
}
