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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

import lombok.extern.slf4j.Slf4j;

/**
 * The class contains all required common methods for iteration kpis
 *
 * @author shi6
 */

@Slf4j
public final class IterationKpiHelper {

	private IterationKpiHelper() {
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

	/*
	 * to transform sprintdetails for iteration kpis
	 */
	public static SprintDetails transformIterSprintdetail(List<JiraIssueCustomHistory> jiraIssueCustomHistoryList,
			Set<String> issues, SprintDetails dbSprintDetail, List<String> completeIssueType,
			List<String> completionStatus, ObjectId projectConfigId) {
		Map<ObjectId, Map<String, List<LocalDateTime>>> projectIssueWiseClosedDates = new HashMap<>();
		Map<String, List<LocalDateTime>> issueWiseMinDateTime = new HashMap<>();
		if (CollectionUtils.isNotEmpty(completionStatus)) {
			for (String issue : issues) {
				List<JiraHistoryChangeLog> statusUpdationLog = jiraIssueCustomHistoryList.stream()
						.filter(jiraIssueCustomHistory -> jiraIssueCustomHistory.getStoryID().equalsIgnoreCase(issue))
						.flatMap(history -> history.getStatusUpdationLog().stream())
						.sorted(Comparator.comparing(JiraHistoryChangeLog::getUpdatedOn)).collect(Collectors.toList());
				/*
				 * iterate over status logs and if some not completed status appears then that
				 * has to be considered as reopen scenario, and at that time whatever statuses
				 * present in minimumCompletedStatusWiseMap, out of them the minimum date has to
				 * be considered of that closed cycle.
				 */
				if (CollectionUtils.isNotEmpty(statusUpdationLog)) {
					Map<String, LocalDateTime> minimumCompletedStatusWiseMap = new HashMap<>();
					List<LocalDateTime> minimumDate = new ArrayList<>();

					KpiDataHelper.getMiniDateOfCompleteCycle(completionStatus, statusUpdationLog,
							minimumCompletedStatusWiseMap, minimumDate);
					// if some status is left in the last cycle then that has to added in the
					// minimum set
					if (MapUtils.isNotEmpty(minimumCompletedStatusWiseMap)) {
						LocalDateTime minDate = minimumCompletedStatusWiseMap.values().stream()
								.min(LocalDateTime::compareTo).orElse(null);
						if (minDate != null) {
							minimumDate.add(minDate);
							minimumCompletedStatusWiseMap.clear();
						}
					}
					issueWiseMinDateTime.put(issue, minimumDate);
				}
			}
			projectIssueWiseClosedDates.put(projectConfigId, issueWiseMinDateTime);
		}
		return KpiDataHelper.processSprintBasedOnFieldMappings(dbSprintDetail, completeIssueType, completionStatus,
				projectIssueWiseClosedDates);
	}

}