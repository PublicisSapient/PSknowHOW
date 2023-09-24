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

package com.publicissapient.kpidashboard.jira.parser;

import java.util.Collections;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.json.GenericJsonArrayParser;
import com.atlassian.jira.rest.client.internal.json.SearchResultJsonParser;

public class CustomSearchResultJsonParser extends SearchResultJsonParser {

	@Override
	public SearchResult parse(JSONObject json) throws JSONException {
		final int startAt = json.getInt("startAt");
		final int maxResults = json.getInt("maxResults");
		final int total = json.getInt("total");
		final JSONArray issuesJsonArray = json.getJSONArray("issues");

		final Iterable<Issue> issues;
		if (issuesJsonArray.length() > 0) {
			final CustomIssueJsonParser issueParser = new CustomIssueJsonParser(json.getJSONObject("names"),
					json.getJSONObject("schema"));
			final GenericJsonArrayParser<Issue> issuesParser = GenericJsonArrayParser.create(issueParser);
			issues = issuesParser.parse(issuesJsonArray);
		} else {
			issues = Collections.emptyList();
		}
		return new SearchResult(startAt, maxResults, total, issues);
	}

}
