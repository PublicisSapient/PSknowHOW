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

import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.PRIORITY_FIELD;

import javax.annotation.Nullable;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.atlassian.jira.rest.client.internal.json.BasicPriorityJsonParser;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;
import com.atlassian.jira.rest.client.internal.json.SubtaskJsonParser;
import com.publicissapient.kpidashboard.jira.model.CustomSubtask;

public class CustomSubtaskJsonParser implements JsonObjectParser<CustomSubtask> {
	private final BasicPriorityJsonParser priorityJsonParser = new BasicPriorityJsonParser();
	private final SubtaskJsonParser subtaskJsonParser = new SubtaskJsonParser();

	private static final String FIELDS = "fields";

	@Override
	public CustomSubtask parse(JSONObject json) throws JSONException {
		Subtask subtask = subtaskJsonParser.parse(json);
		JSONObject fieldsJson = json.getJSONObject(FIELDS);
		BasicPriority priority = null;
		if (fieldsJson.has("priority")) {
			priority = getOptionalNestedField(json, PRIORITY_FIELD.id, priorityJsonParser);
		}
		return new CustomSubtask(subtask.getIssueKey(), subtask.getIssueUri(), subtask.getSummary(),
				subtask.getIssueType(), subtask.getStatus(), priority);
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