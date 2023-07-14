/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2020 Sapient Limited.
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

package com.publicissapient.kpidashboard.jiratest.adapter.impl.async.factory;

import java.net.URI;

import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.publicissapient.kpidashboard.jiratest.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jiratest.adapter.impl.async.impl.ProcessorAsynchJiraRestClient;
import com.publicissapient.kpidashboard.jiratest.config.JiraTestProcessorConfig;

public class ProcessorAsynchJiraRestClientFactory extends AsynchronousJiraRestClientFactory {

	/**
	 * Creates JIRA Client
	 *
	 * @param serverUri
	 *            Jira Server URI
	 * @param authenticationHandler
	 *            Authentication handler instance
	 * @param jiraTestProcessorConfig
	 *            Jira Test processor config
	 * @return SpeedyJiraRestClient
	 */
	public ProcessorJiraRestClient create(final URI serverUri, final AuthenticationHandler authenticationHandler,
			JiraTestProcessorConfig jiraTestProcessorConfig) {
		final DisposableHttpClient httpClient = new ProcessorAsynchHttpClientFactory().createProcessorClient(serverUri,
				authenticationHandler, jiraTestProcessorConfig);
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
	 * @param jiraTestProcessorConfig
	 *            Jira Test processor config
	 * @return SpeedyJiraRestClient
	 */
	public ProcessorJiraRestClient createWithBasicHttpAuthentication(final URI serverUri, final String username,
			final String password, JiraTestProcessorConfig jiraTestProcessorConfig) {
		return create(serverUri, new BasicHttpAuthenticationHandler(username, password), jiraTestProcessorConfig);
	}
}
