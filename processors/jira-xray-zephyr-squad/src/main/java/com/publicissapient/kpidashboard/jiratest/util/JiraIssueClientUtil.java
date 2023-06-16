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

package com.publicissapient.kpidashboard.jiratest.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;

import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JiraIssueClientUtil {

	private JiraIssueClientUtil() {
		super();
	}

	/**
	 * Gets list from json object or array
	 *
	 * @param issueField
	 *            Atlassian IssueField
	 * @return list return from JsonObject or Array
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Collection getListFromJson(IssueField issueField) {

		Object value = issueField.getValue();
		final List list = new ArrayList<>();
		if (value instanceof JSONArray) {

			((JSONArray) value).forEach(v -> {
				try {
					list.add(((JSONObject) v).get(JiraConstants.VALUE));
				} catch (JSONException e) {
					log.error("JIRA PROCESSOR | Error while parsing Atlassian Issue JSON Object", e);
				}
			});
		} else if (value instanceof JSONObject) {
			try {
				list.add(((JSONObject) value).get(JiraConstants.VALUE));
			} catch (JSONException e) {
				log.error("JIRA PROCESSOR | Error while parsing Atlassian Issue JSON Object", e);
			}
		}
		return list;
	}

	/**
	 * Builds Filed Map
	 *
	 * @param fields
	 *            IssueField Iterable
	 * @return Map of FieldIssue ID and FieldIssue Object
	 */
	public static Map<String, IssueField> buildFieldMap(Iterable<IssueField> fields) {
		Map<String, IssueField> rt = new HashMap<>();

		if (fields != null) {
			for (IssueField issueField : fields) {
				rt.put(issueField.getId(), issueField);
			}
		}

		return rt;
	}

	/**
	 * Sorts Change Log group
	 *
	 * @param issue
	 *            Atlassian Issue object
	 * @return List of ChangelogGroup
	 */
	public static List<ChangelogGroup> sortChangeLogGroup(Issue issue) {
		Iterable<ChangelogGroup> changelogItr = issue.getChangelog();
		List<ChangelogGroup> changeLogList = Lists.newArrayList(changelogItr.iterator());
		changeLogList.sort((ChangelogGroup obj1, ChangelogGroup obj2) -> {
			DateTime activityDate1 = obj1.getCreated();
			DateTime activityDate2 = obj2.getCreated();
			return activityDate1.compareTo(activityDate2);
		});
		return changeLogList;
	}

	public static List<String> getLabelsList(Issue issue) {
		List<String> labels = new ArrayList<>();
		if (issue.getLabels() != null) {
			for (String labelName : issue.getLabels()) {
				labels.add(JiraProcessorUtil.deodeUTF8String(labelName));
			}
		}
		return labels;
	}

	public static Set<String> getIssueTypeNames(FieldMapping fieldMapping) {
		Set<String> issueTypeNames = new HashSet<>();
		for (String issueTypeName : fieldMapping.getJiraIssueTypeNames()) {
			issueTypeNames.add(issueTypeName.toLowerCase(Locale.getDefault()));
		}
		return issueTypeNames;
	}

	public static List<String> getAffectedVersions(Issue issue) {
		List<String> affectedVersions = new ArrayList<>();
		if (issue.getAffectedVersions() != null) {
			for (Version affectedVersionName : issue.getAffectedVersions()) {
				affectedVersions.add(affectedVersionName.getName());
			}
		}
		return affectedVersions;
	}
}
