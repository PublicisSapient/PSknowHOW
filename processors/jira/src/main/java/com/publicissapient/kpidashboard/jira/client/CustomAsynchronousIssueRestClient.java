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

import static com.atlassian.jira.rest.client.api.IssueRestClient.Expandos.CHANGELOG;
import static com.atlassian.jira.rest.client.api.IssueRestClient.Expandos.NAMES;
import static com.atlassian.jira.rest.client.api.IssueRestClient.Expandos.SCHEMA;

import java.net.URI;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.SessionRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousIssueRestClient;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.parser.CustomIssueJsonParser;
import com.publicissapient.kpidashboard.jira.parser.CustomSearchResultJsonParser;

import io.atlassian.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAsynchronousIssueRestClient extends AsynchronousIssueRestClient {

	private static final EnumSet<Expandos> DEFAULT_EXPANDS = EnumSet.of(Expandos.NAMES, Expandos.SCHEMA,
			Expandos.TRANSITIONS);
	private static final Function<Expandos, String> EXPANDO_TO_PARAM = from -> from.name().toLowerCase(); // NOSONAR
	private final CustomSearchResultJsonParser searchResultJsonParser = new CustomSearchResultJsonParser();
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

	public Promise<SearchResult> searchBoardIssue(String boardId, @Nullable String jql, @Nullable Integer maxResults,
			@Nullable Integer startAt, @Nullable Set<String> fields) {
		final Iterable<String> expandosValues = Iterables.transform(ImmutableList.of(SCHEMA, NAMES, CHANGELOG),
				EXPANDO_TO_PARAM);
		final String notNullJql = StringUtils.defaultString(jql);
		if (notNullJql.length() > JiraConstants.MAX_JQL_LENGTH_FOR_HTTP_GET) {
			return searchJqlImplPost(maxResults, startAt, expandosValues, notNullJql, fields);
		} else {
			return searchJqlImplGet(boardId, maxResults, startAt, expandosValues, notNullJql, fields);
		}
	}

	/**
	 * Search using JQL query
	 * 
	 * @param boardId
	 *            boardId
	 * @param maxResults
	 *            maximum results
	 * @param startAt
	 *            page search at start at
	 * @param expandosValues
	 *            List of String expandos
	 * @param jql
	 *            Jql query String
	 * @param fields
	 *            Fields
	 * @return Promise of SearchResult
	 */
	private Promise<SearchResult> searchJqlImplGet(String boardId, @Nullable Integer maxResults,
			@Nullable Integer startAt, Iterable<String> expandosValues, String jql, @Nullable Set<String> fields) {
		final UriBuilder uriBuilder = UriBuilder.fromUri(baseUri).path("/board/" + boardId + "/issue")
				.queryParam(JiraConstants.JQL_ATTRIBUTE, jql)
				.queryParam(JiraConstants.EXPAND_ATTRIBUTE, String.join(",", expandosValues));
		if (fields != null) {
			uriBuilder.queryParam(JiraConstants.FIELDS_ATTRIBUTE, String.join(",", fields));
		}
		addOptionalQueryParam(uriBuilder, JiraConstants.MAX_RESULTS_ATTRIBUTE, maxResults);
		addOptionalQueryParam(uriBuilder, JiraConstants.START_AT_ATTRIBUTE, startAt);
		log.info("Api Url : {} JQL: {} MaxResults: {} startAt : {}", baseUri + "/board/" + boardId + "/issue", jql,
				maxResults, startAt);
		return getAndParse(uriBuilder.build(), searchResultJsonParser);
	}

	/**
	 * Adds optional query params
	 * 
	 * @param uriBuilder
	 *            URI Builder
	 * @param key
	 *            Key
	 * @param values
	 *            optional param values
	 */
	private void addOptionalQueryParam(final UriBuilder uriBuilder, final String key, final Object... values) {
		if (values != null && values.length > 0 && values[0] != null) {
			uriBuilder.queryParam(key, values);
		}
	}

	/**
	 * @param maxResults
	 *            Maximum results
	 * @param startAt
	 *            start At page number
	 * @param expandosValues
	 *            Iterable String
	 * @param jql
	 *            Jql Query String
	 * @param fields
	 *            query fields
	 * @return Promise of SearchResult
	 */
	private Promise<SearchResult> searchJqlImplPost(@Nullable Integer maxResults, @Nullable Integer startAt,
			Iterable<String> expandosValues, String jql, @Nullable Set<String> fields) {
		final JSONObject postEntity = new JSONObject();
		try {
			postEntity.put(JiraConstants.JQL_ATTRIBUTE, jql)
					.put(JiraConstants.EXPAND_ATTRIBUTE, ImmutableList.copyOf(expandosValues))
					.putOpt(JiraConstants.START_AT_ATTRIBUTE, startAt)
					.putOpt(JiraConstants.MAX_RESULTS_ATTRIBUTE, maxResults);

			if (fields != null) {
				postEntity.put(JiraConstants.FIELDS_ATTRIBUTE, fields); // putOpt
																		// doesn't
																		// work
																		// with
																		// collections
			}
		} catch (JSONException e) {
			throw new RestClientException(e);
		}
		log.info("Making Post call as max length for Get Call has exceeded");
		return postAndParse(baseUri, postEntity, searchResultJsonParser);
	}

}
