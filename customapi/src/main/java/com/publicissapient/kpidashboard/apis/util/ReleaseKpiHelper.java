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

package com.publicissapient.kpidashboard.apis.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

import lombok.extern.slf4j.Slf4j;

/**
 * The class contains all required common methods for release kpis
 *
 * @author shi6
 */

@Slf4j
public final class ReleaseKpiHelper {

	private ReleaseKpiHelper() {
	}


	public static List<JiraIssue> getFilteredReleaseJiraIssuesFromBaseClass(List<JiraIssue> jiraIssuesForCurrentRelease,
			Set<JiraIssue> defectsList) {
		List<JiraIssue> filteredJiraIssue = new ArrayList<>();
		List<JiraIssue> subtaskDefects = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(defectsList)) {
			subtaskDefects.addAll(defectsList);
			subtaskDefects.removeIf(jiraIssuesForCurrentRelease::contains);
			filteredJiraIssue = subtaskDefects;
		}
		filteredJiraIssue.addAll(jiraIssuesForCurrentRelease);
		return filteredJiraIssue;
	}

	/**
	 * Filtering the jiraIssue based on releaseStatus
	 *
	 * @param jiraIssueList
	 *            jiraIssueList
	 * @param statusMap
	 *            statusMap
	 * @return list of jiraIssue
	 */
	public static List<JiraIssue> filterIssuesByStatus(List<JiraIssue> jiraIssueList, Map<Long, String> statusMap) {
		List<JiraIssue> filteredJiraIssue = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(jiraIssueList) && MapUtils.isNotEmpty(statusMap)) {
			filteredJiraIssue = jiraIssueList.stream()
					.filter(jiraIssue -> statusMap.containsValue(jiraIssue.getStatus())).collect(Collectors.toList());
		}
		return filteredJiraIssue;

	}
}