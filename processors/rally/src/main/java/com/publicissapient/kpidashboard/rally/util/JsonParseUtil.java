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

package com.publicissapient.kpidashboard.rally.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Nullable;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.atlassian.jira.rest.client.api.ExpandableProperty;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;

public class JsonParseUtil {
	public static final String JIRA_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public static final DateTimeFormatter JIRA_DATE_TIME_FORMATTER = DateTimeFormat.forPattern(JIRA_DATE_TIME_PATTERN);
	public static final DateTimeFormatter JIRA_DATE_FORMATTER = ISODateTimeFormat.date();
	public static final String SELF_ATTR = "self";

	private JsonParseUtil() {
	}

	public static <T> Collection<T> parseJsonArray(final JSONArray jsonArray, final JsonObjectParser<T> jsonParser)
			throws JSONException {
		final Collection<T> res = new ArrayList<>(jsonArray.length());
		for (int i = 0; i < jsonArray.length(); i++) {
			res.add(jsonParser.parse(jsonArray.getJSONObject(i)));
		}
		return res;
	}

	@Nullable
	public static <T> ExpandableProperty<T> parseOptionalExpandableProperty(@Nullable final JSONObject json,
			final JsonObjectParser<T> expandablePropertyBuilder) throws JSONException {
		return parseExpandableProperty(json, true, expandablePropertyBuilder);
	}

	@Nullable
	private static <T> ExpandableProperty<T> parseExpandableProperty(@Nullable final JSONObject json,
			final Boolean optional, final JsonObjectParser<T> expandablePropertyBuilder) throws JSONException {
		if (json == null) {
			if (!optional) {
				throw new IllegalArgumentException("json object cannot be null while optional is false");
			}
			return null;
		}

		final int numItems = json.getInt("size");
		final Collection<T> items;
		JSONArray itemsJa = json.getJSONArray("items");

		if (itemsJa.length() > 0) {
			items = new ArrayList<>(numItems);
			for (int i = 0; i < itemsJa.length(); i++) {
				final T item = expandablePropertyBuilder.parse(itemsJa.getJSONObject(i));
				items.add(item);
			}
		} else {
			items = null;
		}

		return new ExpandableProperty<>(numItems, items);
	}

	public static URI optSelfUri(final JSONObject jsonObject, final URI defaultUri) {
		final String selfUri = jsonObject.optString(SELF_ATTR, null);
		return selfUri != null ? parseURI(selfUri) : defaultUri;
	}

	public static URI parseURI(final String str) {
		try {
			return new URI(str);
		} catch (URISyntaxException e) {
			throw new RestClientException(e);
		}
	}

	@Nullable
	public static BasicUser parseBasicUser(@Nullable final JSONObject json) throws JSONException {
		if (json == null) {
			return null;
		}
		String username = "";

		if (json.has("name")) {
			username = json.getString("name");
		}

		if (!json.has(JsonParseUtil.SELF_ATTR) && "Anonymous".equals(username)) {
			return null; // insane representation for unassigned user - JRADEV-4262
		}

		// deleted user? BUG in REST API: JRA-30263
		final URI selfUri = optSelfUri(json, BasicUser.INCOMPLETE_URI);
		return new BasicUser(selfUri, username, json.optString("displayName", null));
	}

	public static DateTime parseDateTime(final JSONObject jsonObject, final String attributeName) throws JSONException {
		return parseDateTime(jsonObject.getString(attributeName));
	}

	public static DateTime parseDateTime(final String str) {
		try {
			return JIRA_DATE_TIME_FORMATTER.parseDateTime(str);
		} catch (Exception e) {
			throw new RestClientException(e);
		}
	}

	@Nullable
	public static String getOptionalString(final JSONObject jsonObject, final String attributeName) {
		final Object res = jsonObject.opt(attributeName);
		if (res == JSONObject.EXPLICIT_NULL || res == null) {
			return null;
		}
		return res.toString();
	}
}
