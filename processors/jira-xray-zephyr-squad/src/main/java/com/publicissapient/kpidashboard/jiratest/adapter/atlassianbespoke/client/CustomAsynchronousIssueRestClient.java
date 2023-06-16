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

package com.publicissapient.kpidashboard.jiratest.adapter.atlassianbespoke.client;

import java.net.URI;
import java.util.EnumSet;

import javax.ws.rs.core.UriBuilder;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.SessionRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.internal.async.AsynchronousIssueRestClient;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.publicissapient.kpidashboard.jiratest.adapter.atlassianbespoke.parser.CustomIssueJsonParser;

import io.atlassian.util.concurrent.Promise;

public class CustomAsynchronousIssueRestClient extends AsynchronousIssueRestClient {

	private static final EnumSet<Expandos> DEFAULT_EXPANDS = EnumSet.of(Expandos.NAMES, Expandos.SCHEMA,
			Expandos.TRANSITIONS);
	private static final Function<Expandos, String> EXPANDO_TO_PARAM = from -> from.name().toLowerCase(); // NOSONAR
	private final URI baseUri;
	CustomIssueJsonParser issueJsonParser = new CustomIssueJsonParser();

	public CustomAsynchronousIssueRestClient(URI baseUri, HttpClient client, SessionRestClient sessionRestClient,
			MetadataRestClient metadataRestClient) {
		super(baseUri, client, sessionRestClient, metadataRestClient);
		this.baseUri = baseUri;
	}

	@Override
	public Promise<Issue> getIssue(final String issueKey, final Iterable<Expandos> expand) {
		final UriBuilder uriBuilder = UriBuilder.fromUri(baseUri);
		final Iterable<Expandos> expands = Iterables.concat(DEFAULT_EXPANDS, expand);
		uriBuilder.path("issue").path(issueKey).queryParam("expand",
				String.join(",", Iterables.transform(expands, EXPANDO_TO_PARAM)));
		return getAndParse(uriBuilder.build(), issueJsonParser);
	}

}
