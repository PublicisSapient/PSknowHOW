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
import java.util.Set;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Filter;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AbstractAsynchronousRestClient;
import com.atlassian.jira.rest.client.internal.json.FilterJsonParser;
import com.atlassian.jira.rest.client.internal.json.GenericJsonArrayParser;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.parser.CustomSearchResultJsonParser;

import io.atlassian.util.concurrent.Promise;

public class ProcessorAsynchSearchRestClient extends AbstractAsynchronousRestClient implements SearchRestClient {

	private static final Function<IssueRestClient.Expandos, String> EXPANDO_TO_PARAM = from -> from.name()
			.toLowerCase(); // NOSONAR
	private final CustomSearchResultJsonParser searchResultJsonParser = new CustomSearchResultJsonParser();
	private final FilterJsonParser filterJsonParser = new FilterJsonParser();
	private final GenericJsonArrayParser<Filter> filtersParser = GenericJsonArrayParser.create(new FilterJsonParser());
	private final URI searchUri;
	private final URI favouriteUri;
	private final URI baseUri;

	/**
	 * initialize URIs
	 *
	 * @param baseUri
	 *            Jira Base URI
	 * @param asyncHttpClient
	 *            Async http Client
	 */
	public ProcessorAsynchSearchRestClient(final URI baseUri, final HttpClient asyncHttpClient) {
		super(asyncHttpClient);
		this.baseUri = baseUri;
		this.searchUri = UriBuilder.fromUri(baseUri).path(JiraConstants.SEARCH_URI_PREFIX).build();
		this.favouriteUri = UriBuilder.fromUri(baseUri).path(JiraConstants.FILTER_FAVOURITE_PATH).build();
	}

	/**
	 * Searches JQL
	 *
	 * @param jql
	 *            Serach Jql query
	 */
	@Override
	public Promise<SearchResult> searchJql(@Nullable String jql) {
		return searchJql(jql, null, null, null);
	}

	/**
	 * Searches JQL
	 *
	 * @param jql
	 *            JQL query string
	 * @param maxResults
	 *            maximum result count
	 * @param startAt
	 *            search page start at
	 * @param fields
	 *            Fields to search
	 */
	@Override
	public Promise<SearchResult> searchJql(@Nullable String jql, @Nullable Integer maxResults,
			@Nullable Integer startAt, @Nullable Set<String> fields) {
		final Iterable<String> expandosValues = Iterables.transform(ImmutableList.of(SCHEMA, NAMES, CHANGELOG),
				EXPANDO_TO_PARAM);
		final String notNullJql = StringUtils.defaultString(jql);
		if (notNullJql.length() > JiraConstants.MAX_JQL_LENGTH_FOR_HTTP_GET) {
			return searchJqlImplPost(maxResults, startAt, expandosValues, notNullJql, fields);
		} else {
			return searchJqlImplGet(maxResults, startAt, expandosValues, notNullJql, fields);
		}
	}

	/**
	 * Search using JQL query
	 *
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
	private Promise<SearchResult> searchJqlImplGet(@Nullable Integer maxResults, @Nullable Integer startAt,
			Iterable<String> expandosValues, String jql, @Nullable Set<String> fields) {
		final UriBuilder uriBuilder = UriBuilder.fromUri(searchUri).queryParam(JiraConstants.JQL_ATTRIBUTE, jql)
				.queryParam(JiraConstants.EXPAND_ATTRIBUTE, String.join(",", expandosValues));

		if (fields != null) {
			uriBuilder.queryParam(JiraConstants.FIELDS_ATTRIBUTE, String.join(",", fields));
		}
		addOptionalQueryParam(uriBuilder, JiraConstants.MAX_RESULTS_ATTRIBUTE, maxResults);
		addOptionalQueryParam(uriBuilder, JiraConstants.START_AT_ATTRIBUTE, startAt);
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
				postEntity.put(JiraConstants.FIELDS_ATTRIBUTE, fields); // putOpt doesn't work with collections
			}
		} catch (JSONException e) {
			throw new RestClientException(e);
		}
		return postAndParse(searchUri, postEntity, searchResultJsonParser);
	}

	/**
	 * Gets Favourite Filters
	 *
	 * @return Promise of Iterable Filter
	 */
	@Override
	public Promise<Iterable<Filter>> getFavouriteFilters() {
		return getAndParse(favouriteUri, filtersParser);
	}

	/**
	 * Gets Filter
	 *
	 * @param filterUri
	 *            Filter URI
	 * @return Promise of Filter
	 */
	@Override
	public Promise<Filter> getFilter(URI filterUri) {
		return getAndParse(filterUri, filterJsonParser);
	}

	/**
	 * Gets Filter
	 *
	 * @param id
	 *            filter ID
	 * @return Promise of Filter Type
	 */
	@Override
	public Promise<Filter> getFilter(long id) {
		return getFilter(UriBuilder.fromUri(baseUri).path(String.format(JiraConstants.FILTER_PATH_FORMAT, id)).build());
	}

}
