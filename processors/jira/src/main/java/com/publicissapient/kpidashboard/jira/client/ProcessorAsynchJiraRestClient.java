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

package com.publicissapient.kpidashboard.jira.client;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClient;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;

public class ProcessorAsynchJiraRestClient extends AsynchronousJiraRestClient implements ProcessorJiraRestClient {

	private final SearchRestClient searchRestClient;
	private final CustomAsynchronousIssueRestClient issueRestClient;

	/**
	 * @param serverUri
	 *            Jira server URI
	 * @param httpClient
	 *            Disposable Http client instance
	 */
	public ProcessorAsynchJiraRestClient(final URI serverUri, final DisposableHttpClient httpClient) {
		super(serverUri, httpClient);
		final URI baseUri = UriBuilder.fromUri(serverUri).path("/rest/agile/latest").build();
		final URI searchUri = UriBuilder.fromUri(serverUri).path("/rest/api/latest").build();
		this.issueRestClient = new CustomAsynchronousIssueRestClient(baseUri, httpClient, super.getSessionClient(),
				super.getMetadataClient());
		this.searchRestClient = new ProcessorAsynchSearchRestClient(searchUri, httpClient);
	}

	@Override
	public IssueRestClient getIssueClient() {
		return issueRestClient;
	}

	/**
	 * @return searchRestClient
	 */
	@Override
	public SearchRestClient getProcessorSearchClient() {
		return searchRestClient;
	}

	@Override
	public CustomAsynchronousIssueRestClient getCustomIssueClient() {
		return issueRestClient;
	}

}
