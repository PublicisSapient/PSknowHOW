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

package com.publicissapient.kpidashboard.apis.jira.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.common.model.jira.IterationPotentialDelay;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Helper class for the PCD and potential delay calculation
 *
 *
 */
@Slf4j
@Service
public class CalculatePCDHelper {

	private CalculatePCDHelper() {
	}

	/**
	 * with assignees criteria calculating potential delay for inprogress and open
	 * issues and without assignees calculating potential delay for inprogress
	 * stories
	 *
	 * @param sprintDetails
	 *            sprintDetails
	 * @param allIssues
	 *            allIssues
	 * @param inProgressStatus
	 *            inProgressStatus
	 * @return List of potential delay
	 */
	public static List<IterationPotentialDelay> calculatePotentialDelay(SprintDetails sprintDetails,
			List<JiraIssue> allIssues, List<String> inProgressStatus) {
		List<IterationPotentialDelay> iterationPotentialDelayList = new ArrayList<>();
		Map<String, List<JiraIssue>> assigneeWiseJiraIssue = assigneeWiseJiraIssue(allIssues);

		if (MapUtils.isNotEmpty(assigneeWiseJiraIssue)) {
			assigneeWiseJiraIssue.forEach((assignee, jiraIssues) -> {
				List<JiraIssue> inProgressIssues = new ArrayList<>();
				List<JiraIssue> openIssues = new ArrayList<>();
				arrangeJiraIssueList(inProgressStatus, jiraIssues, inProgressIssues, openIssues);
				iterationPotentialDelayList
						.addAll(sprintWiseDelayCalculation(inProgressIssues, openIssues, sprintDetails));
			});
		}

		if (CollectionUtils.isNotEmpty(inProgressStatus)) {
			List<JiraIssue> inProgressIssues = allIssues.stream()
					.filter(jiraIssue -> (jiraIssue.getAssigneeId() == null)
							&& StringUtils.isNotEmpty(jiraIssue.getDueDate())
							&& (inProgressStatus.contains(jiraIssue.getStatus())))
					.collect(Collectors.toList());

			List<JiraIssue> openIssues = new ArrayList<>();
			iterationPotentialDelayList.addAll(sprintWiseDelayCalculation(inProgressIssues, openIssues, sprintDetails));
		}
		return iterationPotentialDelayList;
	}

	public static Map<String, List<JiraIssue>> assigneeWiseJiraIssue(List<JiraIssue> allIssues) {
		return allIssues.stream().filter(jiraIssue -> jiraIssue.getAssigneeId() != null)
				.collect(Collectors.groupingBy(JiraIssue::getAssigneeId));
	}

	public static List<IterationPotentialDelay> sprintWiseDelayCalculation(
			List<JiraIssue> inProgressIssuesJiraIssueList, List<JiraIssue> openIssuesJiraIssueList,
			SprintDetails sprintDetails) {
		List<IterationPotentialDelay> iterationPotentialDelayList = new ArrayList<>();
		LocalDate pivotPCD = null;
		Map<LocalDate, List<JiraIssue>> dueDateWiseInProgressJiraIssue = createDueDateWiseMap(
				inProgressIssuesJiraIssueList);
		Map<LocalDate, List<JiraIssue>> dueDateWiseOpenJiraIssue = createDueDateWiseMap(openIssuesJiraIssueList);
		if (MapUtils.isNotEmpty(dueDateWiseInProgressJiraIssue)) {
			for (Map.Entry<LocalDate, List<JiraIssue>> entry : dueDateWiseInProgressJiraIssue.entrySet()) {
				pivotPCD = getNextPotentialClosedDate(sprintDetails, iterationPotentialDelayList, pivotPCD, entry);
			}
		}
		if (MapUtils.isNotEmpty(dueDateWiseOpenJiraIssue)) {
			for (Map.Entry<LocalDate, List<JiraIssue>> entry : dueDateWiseOpenJiraIssue.entrySet()) {
				pivotPCD = getNextPotentialClosedDate(sprintDetails, iterationPotentialDelayList, pivotPCD, entry);
			}
		}
		return iterationPotentialDelayList;
	}

	/**
	 * create dueDateWise sorted Map only for the stories having dueDate
	 *
	 * @param arrangeJiraIssueList
	 *            arrangeJiraIssueList
	 * @return map
	 */
	private static Map<LocalDate, List<JiraIssue>> createDueDateWiseMap(List<JiraIssue> arrangeJiraIssueList) {
		TreeMap<LocalDate, List<JiraIssue>> localDateListMap = new TreeMap<>();
		if (CollectionUtils.isNotEmpty(arrangeJiraIssueList)) {
			arrangeJiraIssueList.forEach(jiraIssue -> {
				LocalDate dueDate = DateUtil.stringToLocalDate(jiraIssue.getDueDate(), DateUtil.TIME_FORMAT_WITH_SEC);
				localDateListMap.computeIfPresent(dueDate, (date, issue) -> {
					issue.add(jiraIssue);
					return issue;
				});
				localDateListMap.computeIfAbsent(dueDate, value -> {
					List<JiraIssue> issues = new ArrayList<>();
					issues.add(jiraIssue);
					return issues;
				});
			});
		}
		return localDateListMap;
	}

	private static LocalDate getNextPotentialClosedDate(SprintDetails sprintDetails,
			List<IterationPotentialDelay> iterationPotentialDelayList, LocalDate pivotPCD,
			Map.Entry<LocalDate, List<JiraIssue>> entry) {
		LocalDate pivotPCDLocal = null;
		String sprintState = sprintDetails.getState();
		LocalDate sprintEndDate = DateUtil.stringToLocalDate(sprintDetails.getEndDate(), DateUtil.TIME_FORMAT_WITH_SEC);
		for (JiraIssue issue : entry.getValue()) {
			LocalDate dueDate = entry.getKey();
			int remainingEstimateTime = getRemainingEstimateTime(issue);
			// if remaining time is 0 and sprint is closed, then PCD is sprint end time
			// otherwise will create PCD
			LocalDate potentialClosedDate = (remainingEstimateTime == 0
					&& sprintState.equalsIgnoreCase(SprintDetails.SPRINT_STATE_CLOSED)) ? sprintEndDate
							: createPotentialClosedDate(sprintDetails, remainingEstimateTime, pivotPCD);
			int potentialDelay = getPotentialDelay(dueDate, potentialClosedDate);
			iterationPotentialDelayList
					.add(createIterationPotentialDelay(potentialClosedDate, potentialDelay, remainingEstimateTime,
							issue, sprintState.equalsIgnoreCase(SprintDetails.SPRINT_STATE_CLOSED), dueDate));
			pivotPCDLocal = checkPivotPCD(sprintDetails, potentialClosedDate, remainingEstimateTime, pivotPCDLocal);
		}
		pivotPCD = pivotPCDLocal == null ? pivotPCD : pivotPCDLocal;
		return pivotPCD;
	}

	private static IterationPotentialDelay createIterationPotentialDelay(LocalDate potentialClosedDate,
			int potentialDelay, int remainingEstimateTime, JiraIssue issue, boolean sprintClosed, LocalDate dueDate) {
		IterationPotentialDelay iterationPotentialDelay = new IterationPotentialDelay();
		iterationPotentialDelay.setIssueId(issue.getNumber());
		iterationPotentialDelay.setPotentialDelay((sprintClosed && remainingEstimateTime == 0) ? 0 : potentialDelay);
		iterationPotentialDelay.setDueDate(dueDate.toString());
		iterationPotentialDelay.setPredictedCompletedDate(potentialClosedDate.toString());
		iterationPotentialDelay.setAssigneeId(issue.getAssigneeId());
		iterationPotentialDelay.setStatus(issue.getStatus());
		return iterationPotentialDelay;

	}

	/**
	 * if due date is less than potential closed date, then potential delay will be
	 * negative
	 *
	 * @param dueDate
	 *            dueDate
	 * @param potentialClosedDate
	 *            potentialClosedDate
	 * @return integer
	 */
	private static int getPotentialDelay(LocalDate dueDate, LocalDate potentialClosedDate) {
		int potentialDelays = CommonUtils.getWorkingDays(dueDate, potentialClosedDate);
		return (dueDate.isAfter(potentialClosedDate)) ? potentialDelays * (-1) : potentialDelays;
	}

	/*
	 * add remaining estimates to the PCD calculated from the previous stories
	 */
	private static LocalDate createPotentialClosedDate(SprintDetails sprintDetails, int remainingEstimateTime,
			LocalDate pivotPCD) {
		LocalDate pcd;
		if (pivotPCD == null) {
			// for the first calculation
			LocalDate startDate = sprintDetails.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_CLOSED)
					? DateUtil.stringToLocalDate(sprintDetails.getEndDate(), DateUtil.TIME_FORMAT_WITH_SEC)
					: LocalDate.now();

			pcd = CommonUtils.getWorkingDayAfterAdditionofDays(startDate, remainingEstimateTime);
		} else {
			pcd = CommonUtils.getWorkingDayAfterAdditionofDays(pivotPCD, remainingEstimateTime);
		}
		return pcd;
	}

	private static int getRemainingEstimateTime(JiraIssue issueObject) {
		int remainingEstimate = 0;
		if (issueObject.getRemainingEstimateMinutes() != null) {
			remainingEstimate = (issueObject.getRemainingEstimateMinutes() / 60) / 8;
		}
		return remainingEstimate;
	}

	/**
	 * In closed sprint if a Remaining Estimate is 0, then the potential closing
	 * date will be same as sprint' end date, whose potential closing date will not
	 * be taken into account for further storie's delay calculation
	 *
	 * @param sprintDetails
	 *            sprintDetails
	 * @param potentialClosedDate
	 *            potentialClosedDate
	 * @param remainingEstimateTime
	 *            remainingEstimateTime
	 * @param pivotPCDLocal
	 *            pivotPCDLocal
	 * @return localdate
	 */
	private static LocalDate checkPivotPCD(SprintDetails sprintDetails, LocalDate potentialClosedDate,
			int remainingEstimateTime, LocalDate pivotPCDLocal) {
		if ((pivotPCDLocal == null || pivotPCDLocal.isBefore(potentialClosedDate))
				&& (!sprintDetails.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_CLOSED)
						|| (sprintDetails.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_CLOSED)
								&& remainingEstimateTime != 0))) {
			pivotPCDLocal = potentialClosedDate;
		}
		return pivotPCDLocal;
	}

	/**
	 * setting in progress and open issues
	 *
	 * @param inProgressStatus
	 *            inProgressStatus
	 * @param allIssues
	 *            allIssues
	 * @param inProgressIssues
	 *            inProgressIssues
	 * @param openIssues
	 *            openIssues
	 */
	public static void arrangeJiraIssueList(List<String> inProgressStatus, List<JiraIssue> allIssues,
			List<JiraIssue> inProgressIssues, List<JiraIssue> openIssues) {
		List<JiraIssue> jiraIssuesWithDueDate = allIssues.stream()
				.filter(issue -> StringUtils.isNotEmpty(issue.getDueDate())).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(inProgressStatus)) {
			inProgressIssues.addAll(
					jiraIssuesWithDueDate.stream().filter(jiraIssue -> inProgressStatus.contains(jiraIssue.getStatus()))
							.collect(Collectors.toList()));
			openIssues.addAll(jiraIssuesWithDueDate.stream()
					.filter(jiraIssue -> !inProgressStatus.contains(jiraIssue.getStatus()))
					.collect(Collectors.toList()));
		} else {
			openIssues.addAll(jiraIssuesWithDueDate);
		}

	}

	public static Map<String, IterationPotentialDelay> checkMaxDelayAssigneeWise(
			List<IterationPotentialDelay> issueWiseDelay, List<String> inProgressStatus) {
		Map<String, List<IterationPotentialDelay>> assigneeWiseDelay = issueWiseDelay.stream()
				.collect(Collectors.groupingBy(IterationPotentialDelay::getAssigneeId));
		List<IterationPotentialDelay> maxDelayList = new ArrayList<>();
		List<String> jiraStatusInProgress = CollectionUtils.isNotEmpty(inProgressStatus) ? inProgressStatus
				: new ArrayList<>();
		assigneeWiseDelay.entrySet().forEach(assignee -> maxDelayList.add(assignee.getValue().stream()
				.filter(iterationPotentialDelay -> jiraStatusInProgress.contains(iterationPotentialDelay.getStatus()))
				.max(Comparator.comparing(IterationPotentialDelay::getPotentialDelay))
				.orElse(new IterationPotentialDelay())));
		if (CollectionUtils.isNotEmpty(maxDelayList)) {
			maxDelayList.forEach(iterationPotentialDelay -> issueWiseDelay.stream()
					.filter(issue -> issue.equals(iterationPotentialDelay)).forEach(issue -> issue.setMaxMarker(true)));
		}
		return issueWiseDelay.stream().collect(Collectors.toMap(IterationPotentialDelay::getIssueId,
				Function.identity(), (e1, e2) -> e2, LinkedHashMap::new));
	}

}