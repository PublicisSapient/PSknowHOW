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
import java.util.Set;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import org.apache.commons.collections4.CollectionUtils;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;

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

	/*
	filter all jiraIssues
	 */
	public static List<JiraIssue> getFilteredJiraIssue(List<String> issueNumberList, List<JiraIssue> allJiraIssues) {
		List<JiraIssue> filterJiraIssueList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(issueNumberList) && CollectionUtils.isNotEmpty(allJiraIssues)) {
			filterJiraIssueList = allJiraIssues.stream()
					.filter(jiraIssue -> issueNumberList.contains(jiraIssue.getNumber())).collect(Collectors.toList());
		}
		return filterJiraIssueList;
	}

	/*
	filter all issueHistory
	 */
	public static List<JiraIssueCustomHistory> getFilteredJiraIssueHistory(List<String> issueNumberList,
			List<JiraIssueCustomHistory> jiraIssueCustomHistoryList) {
		List<JiraIssueCustomHistory> jiraIssueCustomHistories = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(issueNumberList) && CollectionUtils.isNotEmpty(jiraIssueCustomHistoryList)) {
			jiraIssueCustomHistories = jiraIssueCustomHistoryList.stream()
					.filter(jiraIssue -> issueNumberList.contains(jiraIssue.getStoryID())).collect(Collectors.toList());
		}
		return jiraIssueCustomHistories;
	}

	public static List<JiraIssue> getFilteredReleaseJiraIssuesFromBaseClass(List<JiraIssue> jiraIssuesForCurrentRelease, Set<JiraIssue> defectsList) {
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
}