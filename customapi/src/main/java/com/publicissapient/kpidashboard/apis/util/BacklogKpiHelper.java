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

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;

import com.publicissapient.kpidashboard.common.model.application.CycleTime;
import com.publicissapient.kpidashboard.common.model.application.CycleTimeValidationData;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;

import lombok.extern.slf4j.Slf4j;

/**
 * The class contains all required common methods for backlog kpis
 *
 * @author shi6
 */

@Slf4j
public final class BacklogKpiHelper {
	private static final String MONTH = "Month";

	private BacklogKpiHelper() {
	}

	/**
	 * create x-axis range map with duration
	 *
	 * @param rangeWiseJiraIssuesMap
	 *            map of jira issues by data points
	 * @param xAxisRange
	 *            x axis data points
	 * @param monthRangeMap
	 *            days and range map
	 *
	 */
	public static void initializeRangeMapForProjects(
			Map<String, Map<String, List<JiraIssueCustomHistory>>> rangeWiseJiraIssuesMap, List<String> xAxisRange,
			Map<Long, String> monthRangeMap) {
		LocalDateTime currentDate = LocalDateTime.now();
		xAxisRange.forEach(range -> {
			String[] rangeSplit = range.trim().split(" ");
			if (rangeSplit[2].contains(MONTH)) {
				monthRangeMap.put(DAYS.between(currentDate.minusMonths(Integer.parseInt(rangeSplit[1])), currentDate),
						range);
			} else {
				monthRangeMap.put(DAYS.between(currentDate.minusWeeks(Integer.parseInt(rangeSplit[1])), currentDate),
						range);
			}
			rangeWiseJiraIssuesMap.put(range, new HashMap<>());
		});
	}

	/**
	 * sets jira issue by closed date and issue type
	 *
	 * @param rangeWiseJiraIssuesMap
	 *            map of jira issues by data points
	 * @param issueCustomHistory
	 *            jira issue custom history
	 * @param closedDate
	 *            closed date of jira issue
	 * @param monthRangeMap
	 *            month range map
	 */
	public static void setRangeWiseJiraIssuesMap(
			Map<String, Map<String, List<JiraIssueCustomHistory>>> rangeWiseJiraIssuesMap,
			JiraIssueCustomHistory issueCustomHistory, LocalDateTime closedDate, Map<Long, String> monthRangeMap) {
		long daysBetween = DAYS.between(KpiDataHelper.convertStringToDate(closedDate.toString()), LocalDate.now());
		monthRangeMap.forEach((noOfDay, range) -> {
			if (noOfDay > daysBetween) {
				rangeWiseJiraIssuesMap.computeIfAbsent(range, k -> new HashMap<>())
						.computeIfAbsent(issueCustomHistory.getStoryType(), k -> new ArrayList<>())
						.add(issueCustomHistory);
			}
		});
	}

	public static void setLiveTime(CycleTimeValidationData cycleTimeValidationData, CycleTime cycleTime,
			JiraHistoryChangeLog statusUpdateLog, DateTime updatedOn, List<String> liveStatus) {
		if (cycleTime.getLiveTime() == null && CollectionUtils.isNotEmpty(liveStatus)
				&& liveStatus.contains(statusUpdateLog.getChangedTo().toLowerCase())) {
			cycleTime.setLiveLocalDateTime(statusUpdateLog.getUpdatedOn());
			cycleTime.setLiveTime(updatedOn);
			cycleTimeValidationData.setLiveDate(updatedOn);
		}
	}
}