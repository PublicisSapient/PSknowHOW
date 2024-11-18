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
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.DateTime;

import com.publicissapient.kpidashboard.apis.constant.Constant;
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
	private static final String INTAKE_TO_DOR = "Intake to DOR";
	private static final String DOR_TO_DOD = "DOR to DOD";
	private static final String DOD_TO_LIVE = "DOD to Live";
	private static final String LEAD_TIME = "LEAD TIME";

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
	 * @return
	 */
	public static boolean setRangeWiseJiraIssuesMap(
			Map<String, Map<String, List<JiraIssueCustomHistory>>> rangeWiseJiraIssuesMap,
			JiraIssueCustomHistory issueCustomHistory, LocalDateTime closedDate, Map<Long, String> monthRangeMap) {
		AtomicBoolean addedToMap = new AtomicBoolean(false);
		if (ObjectUtils.isNotEmpty(closedDate)) {
			long daysBetween = DAYS.between(KpiDataHelper.convertStringToDate(closedDate.toString()), LocalDate.now());
			monthRangeMap.forEach((noOfDay, range) -> {
				if (noOfDay > daysBetween) {
					addedToMap.set(true);
					rangeWiseJiraIssuesMap.computeIfAbsent(range, k -> new HashMap<>())
							.computeIfAbsent(issueCustomHistory.getStoryType(), k -> new ArrayList<>())
							.add(issueCustomHistory);
				}
			});
		}
		return addedToMap.get();
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

	public static void setReadyTime(CycleTimeValidationData cycleTimeValidationData, CycleTime cycleTime,
			JiraHistoryChangeLog statusUpdateLog, DateTime updatedOn, List<String> dorStatus) {
		if (cycleTime.getReadyTime() == null && CollectionUtils.isNotEmpty(dorStatus)
				&& dorStatus.contains(statusUpdateLog.getChangedTo().toLowerCase())) {
			cycleTime.setReadyLocalDateTime(statusUpdateLog.getUpdatedOn());
			cycleTime.setReadyTime(updatedOn);
			cycleTimeValidationData.setDorDate(updatedOn);
		}
	}

	public static void setDODTime(JiraHistoryChangeLog statusUpdateLog, DateTime updatedOn, List<String> dodStatus,
			String storyFirstStatus, Map<String, DateTime> dodStatusDateMap) {
		// reopen sceneario
		if (CollectionUtils.isNotEmpty(dodStatus) && statusUpdateLog.getChangedFrom() != null
				&& dodStatus.contains(statusUpdateLog.getChangedFrom().toLowerCase())
				&& storyFirstStatus.equalsIgnoreCase(statusUpdateLog.getChangedTo())) {
			dodStatusDateMap.clear();
		} // taking the delivery date of first closed status date of last closed cycle
		if (CollectionUtils.isNotEmpty(dodStatus) && dodStatus.contains(statusUpdateLog.getChangedTo().toLowerCase())) {
			if (dodStatusDateMap.containsKey(statusUpdateLog.getChangedTo().toLowerCase())) {
				dodStatusDateMap.clear();
			}
			dodStatusDateMap.put(statusUpdateLog.getChangedTo(), updatedOn);
		}
	}

	public static String setValueInCycleTime(DateTime startTime, DateTime endTime, String level,
			CycleTimeValidationData cycleTimeValidationData, Set<String> issueTypes) {
		String weekHours = KpiDataHelper.calWeekHours(startTime, endTime);
		if (!weekHours.equalsIgnoreCase(Constant.NOT_AVAILABLE)) {
			if (issueTypes != null)
				issueTypes.add(cycleTimeValidationData.getIssueType());
			long timeInDays = KpiDataHelper.calculateTimeInDays(Long.parseLong(weekHours));
			switch (level) {
			case INTAKE_TO_DOR:
				cycleTimeValidationData.setIntakeTime(timeInDays);
				break;

			case DOR_TO_DOD:
				cycleTimeValidationData.setDorTime(timeInDays);
				break;

			case DOD_TO_LIVE:
				cycleTimeValidationData.setDodTime(timeInDays);
				break;

			case LEAD_TIME:
				cycleTimeValidationData.setLeadTime(timeInDays);
				break;

			default:

			}
		}
		return weekHours;
	}
}