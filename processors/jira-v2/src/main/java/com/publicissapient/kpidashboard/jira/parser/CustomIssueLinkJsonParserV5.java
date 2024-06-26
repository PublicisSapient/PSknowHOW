/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.jira.parser;

import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.ISSUE_TYPE_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.PRIORITY_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.STATUS_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.SUMMARY_FIELD;

import javax.annotation.Nullable;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.internal.json.BasicPriorityJsonParser;
import com.atlassian.jira.rest.client.internal.json.IssueLinkJsonParserV5;
import com.atlassian.jira.rest.client.internal.json.IssueTypeJsonParser;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;
import com.atlassian.jira.rest.client.internal.json.StatusJsonParser;
import com.publicissapient.kpidashboard.jira.model.CustomIssueLink;

public class CustomIssueLinkJsonParserV5 implements JsonObjectParser<CustomIssueLink> {
	private final IssueLinkJsonParserV5 issueLinkJsonParserV5 = new IssueLinkJsonParserV5();
	private final BasicPriorityJsonParser basicPriorityJsonParser = new BasicPriorityJsonParser();
	private final StatusJsonParser statusJsonParser = new StatusJsonParser();
	private final IssueTypeJsonParser issueTypeJsonParser = new IssueTypeJsonParser();

	private static final String FIELDS = "fields";
	private static final String VALUE_ATTR = "value";

	@Override
	public CustomIssueLink parse(JSONObject json) throws JSONException {
		IssueLink issueLink = issueLinkJsonParserV5.parse(json);
		JSONObject link;
		if (json.has("inwardIssue")) {
			link = json.getJSONObject("inwardIssue");
		} else {
			link = json.getJSONObject("outwardIssue");
		}
		String summary = getFieldStringValue(link, SUMMARY_FIELD.id);
		Status status = statusJsonParser.parse(getFieldUnisex(link, STATUS_FIELD.id));
		BasicPriority priority = getOptionalNestedField(link, PRIORITY_FIELD.id, basicPriorityJsonParser);
		IssueType issueType = issueTypeJsonParser.parse(getFieldUnisex(link, ISSUE_TYPE_FIELD.id));
		return new CustomIssueLink(issueLink.getTargetIssueKey(), issueLink.getTargetIssueUri(),
				issueLink.getIssueLinkType(), summary, status, priority, issueType);
	}

	private String getFieldStringValue(final JSONObject json, final String attributeName) throws JSONException {
		final JSONObject fieldsJson = json.getJSONObject(FIELDS);
		final Object summaryObject = fieldsJson.get(attributeName);
		if (summaryObject instanceof JSONObject) { // pre JIRA 5.0 way
			return ((JSONObject) summaryObject).getString(VALUE_ATTR);
		}
		if (summaryObject instanceof String) { // JIRA 5.0 way
			return (String) summaryObject;
		}
		throw new JSONException("Cannot parse [" + attributeName + "] from available fields");
	}

	private JSONObject getFieldUnisex(final JSONObject json, final String attributeName) throws JSONException {
		final JSONObject fieldsJson = json.getJSONObject(FIELDS);
		final JSONObject fieldJson = fieldsJson.getJSONObject(attributeName);
		if (fieldJson.has(VALUE_ATTR)) {
			return fieldJson.getJSONObject(VALUE_ATTR); // pre 5.0 way
		} else {
			return fieldJson; // JIRA 5.0 way
		}
	}

	@Nullable
	private <T> T getOptionalNestedField(final JSONObject s, final String fieldId, final JsonObjectParser<T> jsonParser)
			throws JSONException {
		final JSONObject fieldJson = JsonParseUtil.getNestedOptionalObject(s, FIELDS, fieldId);
		// for fields like assignee (when unassigned) value attribute may be missing
		// completely
		if (fieldJson != null) {
			return jsonParser.parse(fieldJson);
		}
		return null;
	}
}
