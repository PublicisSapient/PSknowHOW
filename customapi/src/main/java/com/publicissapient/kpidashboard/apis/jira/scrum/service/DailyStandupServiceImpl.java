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

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static com.publicissapient.kpidashboard.apis.jira.scrum.service.IterationBurnupServiceImpl.REMOVED_FROM_CLOSED;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Iteration Burnups KPI gives a graphical representation of no. of issues
 * planned to be closed each day of iteration, actual count of issues closed day
 * wise and the predicted Iteration Burnups for the remaining days of the
 * iteration. {@link JiraKPIService}
 *
 * @author tauakram
 */
@Component
@Slf4j
public class DailyStandupServiceImpl extends JiraKPIService<Map<String, Long>, List<Object>, Map<String, Object>> {
	public static final String UNCHECKED = "unchecked";
	public static final String DUE_DATE = "dueDate";
	private static final String SPRINT = "sprint";
	private static final String ISSUES = "issues";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CapacityKpiDataRepository capacityKpiDataRepository;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getQualifierType() {
		return KPICode.DAILY_STANDUP_VIEW.name();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				sprintWiseLeafNodeValue(v, kpiElement, kpiRequest);
			}
		});
		return kpiElement;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Long> calculateKPIMetrics(Map<String, Object> objectMap) {
		return new HashMap<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(final List<Node> leafNodeList, final String startDate,
			final String endDate, final KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			log.info("Iteration Burnup -> Requested sprint : {}", leafNode.getName());
			SprintDetails dbSprintDetail = getSprintDetailsFromBaseClass();
			SprintDetails sprintDetails;
			if (null != dbSprintDetail) {
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
						.get(leafNode.getProjectFilter().getBasicProjectConfigId());
				// to modify sprintdetails on the basis of configuration for the project
				sprintDetails = KpiDataHelper.processSprintBasedOnFieldMappings(
						Collections.singletonList(dbSprintDetail), fieldMapping.getJiraIterationIssuetypeKPI125(),
						fieldMapping.getJiraIterationCompletionStatusKPI125(), null).get(0);

				LocalDate sprintStartDate = LocalDate.parse(sprintDetails.getStartDate().split("T")[0],
						DATE_TIME_FORMATTER);

				List<String> allIssues = new ArrayList<>();
				List<String> notCompleted = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.NOT_COMPLETED_ISSUES);
				List<String> completed = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.COMPLETED_ISSUES);
				allIssues.addAll(notCompleted);
				allIssues.addAll(completed);
				allIssues.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.ADDED_ISSUES));
				allIssues.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.PUNTED_ISSUES));

				if (CollectionUtils.isNotEmpty(allIssues)) {
					Set<SprintIssue> sprintIssues = new HashSet<>();
					sprintIssues.addAll(checkNullList(sprintDetails.getTotalIssues()));
					sprintIssues.addAll(checkNullList(sprintDetails.getPuntedIssues()));
					Set<JiraIssue> totalIssueList = KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(
							sprintDetails, sprintIssues, getJiraIssuesFromBaseClass(allIssues));
					List<JiraIssueCustomHistory> allIssuesHistory = getJiraIssuesCustomHistoryFromBaseClass(allIssues);
					Map<LocalDate, List<JiraIssue>> fullSprintIssues = new HashMap<>();
					Map<LocalDate, List<JiraIssue>> addedIssues = new HashMap<>();
					Map<LocalDate, List<JiraIssue>> removedIssues = new HashMap<>();
					Map<LocalDate, List<JiraIssue>> completedIssues = new HashMap<>();
					Map<LocalDate, List<JiraIssue>> removedCompletedIssues = new HashMap<>();
					/*
					 * allIssuesHistory.forEach(issueHistory -> { if
					 * (CollectionUtils.isNotEmpty(issueHistory.getSprintUpdationLog())) {
					 * List<JiraHistoryChangeLog> sprintUpdationLog =
					 * issueHistory.getSprintUpdationLog(); Collections.sort(sprintUpdationLog,
					 * Comparator.comparing(JiraHistoryChangeLog::getUpdatedOn));
					 * createAddedandRemovedIssueDateWiseMap(sprintDetails, totalIssueList,
					 * addedIssues, removedIssues, fullSprintIssues, issueHistory,
					 * sprintUpdationLog); } createCompletedIssuesDateWiseMap(sprintDetails,
					 * totalIssueList, completedIssues, removedCompletedIssues, issueHistory,
					 * sprintStartDate); });
					 * 
					 * resultListMap.put(FULL_SPRINT_ISSUES, fullSprintIssues);
					 * resultListMap.put(CommonConstant.PUNTED_ISSUES, removedIssues);
					 * resultListMap.put(CommonConstant.ADDED_ISSUES, addedIssues);
					 * resultListMap.put(CommonConstant.COMPLETED_ISSUES, completedIssues);
					 * resultListMap.put(REMOVED_FROM_CLOSED, removedCompletedIssues);
					 */
					resultListMap.put(SPRINT, sprintDetails);
					resultListMap.put(ISSUES, totalIssueList);
					resultListMap.put("ISSUEHISTORY", allIssuesHistory);
					resultListMap.put("AssigneeDetails",
							capacityKpiDataRepository.findBySprintIDAndBasicProjectConfigId(sprintDetails.getSprintID(),
									sprintDetails.getBasicProjectConfigId()));
				}
			}
		}
		return resultListMap;
	}

	private Set<SprintIssue> checkNullList(Set<SprintIssue> totalIssues) {
		return CollectionUtils.isNotEmpty(totalIssues) ? totalIssues : new HashSet<>();
	}

	/*
	 * if last index of sprintUpdateLog is selected sprint then it means, the issue
	 * was present throughout put that issue in full-Sprint map and that list will
	 * be the base list. while all the other dynamics within sprint will be stored
	 * in AddedIssuesMap within sprint duration if an issue is removed then to be
	 * maintained in removedIssues map
	 */
	private void createAddedandRemovedIssueDateWiseMap(SprintDetails sprintDetails, Set<JiraIssue> totalIssueList,
			Map<LocalDate, List<JiraIssue>> addedIssuesMap, Map<LocalDate, List<JiraIssue>> removedIssues,
			Map<LocalDate, List<JiraIssue>> fullSprintMap, JiraIssueCustomHistory issueHistory,
			List<JiraHistoryChangeLog> sprintUpdationLog) {
		LocalDate startDate = LocalDate.parse(sprintDetails.getStartDate().split("T")[0], DATE_TIME_FORMATTER);
		LocalDate sprintEndDate = LocalDate.parse(sprintDetails.getEndDate().split("T")[0], DATE_TIME_FORMATTER);

		int lastIndex = sprintUpdationLog.size() - 1;
		sprintUpdationLog.stream()
				.filter(updateLogs -> updateLogs.getChangedTo().equalsIgnoreCase(sprintDetails.getSprintName())
						|| (updateLogs.getChangedFrom().equalsIgnoreCase(sprintDetails.getSprintName()) && DateUtil
								.isWithinDateRange(updateLogs.getUpdatedOn().toLocalDate(), startDate, sprintEndDate)))
				.forEach(updateLogs -> {
					List<JiraIssue> jiraIssueList = new ArrayList<>(
							getRespectiveJiraIssue(totalIssueList, issueHistory));
					if (updateLogs.getChangedTo().equalsIgnoreCase(sprintDetails.getSprintName())) {
						if (sprintUpdationLog.get(lastIndex).getUpdatedOn().toLocalDate()
								.isBefore(startDate.plusDays(1))) {
							fullSprintMap.computeIfPresent(startDate, (k, v) -> {
								v.addAll(jiraIssueList);
								return v;
							});
							fullSprintMap.putIfAbsent(startDate, jiraIssueList);
						}
						LocalDate updatedLog = updateLogs.getUpdatedOn().toLocalDate().isBefore(startDate.plusDays(1))
								? startDate
								: limitDateInSprint(updateLogs.getUpdatedOn().toLocalDate(), sprintEndDate);
						addedIssuesMap.computeIfPresent(updatedLog, (k, v) -> {
							v.addAll(jiraIssueList);
							return v;
						});
						addedIssuesMap.putIfAbsent(updatedLog, jiraIssueList);
					}

					if (updateLogs.getChangedFrom().equalsIgnoreCase(sprintDetails.getSprintName()) && DateUtil
							.isWithinDateRange(updateLogs.getUpdatedOn().toLocalDate(), startDate, sprintEndDate)) {
						List<JiraIssue> removeJiraIssueLIst = new ArrayList<>(jiraIssueList);
						LocalDate updatedLog = updateLogs.getUpdatedOn().toLocalDate();
						removedIssues.computeIfPresent(updatedLog, (k, v) -> {
							v.addAll(removeJiraIssueLIst);
							return v;
						});
						removedIssues.putIfAbsent(updatedLog, removeJiraIssueLIst);
					}
				});
	}

	private Set<JiraIssue> getRespectiveJiraIssue(Set<JiraIssue> totalIssueList, JiraIssueCustomHistory issueHistory) {
		return totalIssueList.stream()
				.filter(jiraIssue -> jiraIssue.getNumber().equalsIgnoreCase(issueHistory.getStoryID()))
				.collect(Collectors.toSet());
	}

	private void createCompletedIssuesDateWiseMap(SprintDetails sprintDetails, Set<JiraIssue> totalIssueList,
			Map<LocalDate, List<JiraIssue>> completedIssues, Map<LocalDate, List<JiraIssue>> removedCompletedIssues,
			JiraIssueCustomHistory issueHistory, LocalDate sprintStartDate) {
		LocalDate sprintEndDate = LocalDate.parse(sprintDetails.getEndDate().split("T")[0], DATE_TIME_FORMATTER);
		LocalDate endDate = sprintDetails.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_CLOSED)
				? LocalDate.parse(sprintDetails.getCompleteDate().split("T")[0], DATE_TIME_FORMATTER)
				: sprintEndDate;
		List<JiraHistoryChangeLog> statusUpdationLog = issueHistory.getStatusUpdationLog();
		statusUpdationLog = statusUpdationLog.stream()
				.filter(log -> DateUtil.isWithinDateRange(
						LocalDate.parse(log.getUpdatedOn().toString().split("T")[0], DATE_TIME_FORMATTER),
						sprintStartDate, endDate))
				.collect(Collectors.toList());
		Collections.sort(statusUpdationLog, Comparator.comparing(JiraHistoryChangeLog::getUpdatedOn));
		if (CollectionUtils.isNotEmpty(statusUpdationLog)) {
			Map<String, LocalDate> closedStatusDateMap = new HashMap<>();
			LocalDate removedDate = null;
			Set<String> closedStatus = sprintDetails.getCompletedIssues().stream().map(SprintIssue::getStatus)
					.collect(Collectors.toSet());
			int lastIndex = statusUpdationLog.size() - 1;
			boolean lastOpenState = !closedStatus.contains(statusUpdationLog.get(lastIndex).getChangedTo());
			for (JiraHistoryChangeLog jiraHistoryChangeLog : statusUpdationLog) {
				LocalDate activityDate = LocalDate.parse(jiraHistoryChangeLog.getUpdatedOn().toString().split("T")[0],
						DATE_TIME_FORMATTER);
				if (closedStatus.contains(jiraHistoryChangeLog.getChangedTo())) {
					if (closedStatusDateMap.containsKey(jiraHistoryChangeLog.getChangedTo())) {
						closedStatusDateMap.clear();
					}
					closedStatusDateMap.put(jiraHistoryChangeLog.getChangedTo(), activityDate);
				}
				removedDate = statusChangeFromClosedToOpen(removedDate, closedStatus, lastOpenState,
						jiraHistoryChangeLog, sprintEndDate);
			}
			// Getting the min date of closed status.
			LocalDate updatedLog = closedStatusDateMap.values().stream().filter(Objects::nonNull)
					.min(LocalDate::compareTo).orElse(null);
			updatedLog = limitDateInSprint(updatedLog, sprintEndDate);
			List<JiraIssue> jiraIssueList = new ArrayList<>(getRespectiveJiraIssue(totalIssueList, issueHistory));
			LocalDate finalUpdatedLog = updatedLog;
			jiraIssueList.forEach(issue -> issue.setUpdateDate(ObjectUtils.isEmpty(finalUpdatedLog)
					? LocalDate.parse(issue.getUpdateDate().split("T")[0], DATE_TIME_FORMATTER).toString()
					: finalUpdatedLog.toString()));
			completedIssues.computeIfPresent(updatedLog, (k, v) -> {
				v.addAll(jiraIssueList);
				return v;
			});
			completedIssues.putIfAbsent(updatedLog, jiraIssueList);
			createRemovedFromClosedIssuesMap(removedCompletedIssues, limitDateInSprint(removedDate, sprintEndDate),
					totalIssueList, issueHistory);
		}
	}

	/*
	 * if for closed sprint updation is happening after sprint end date time then it
	 * would be counted under the last day of sprint
	 */

	private LocalDate limitDateInSprint(LocalDate updatedLog, LocalDate sprintEndDate) {
		if (Objects.nonNull(updatedLog) && updatedLog.isAfter(sprintEndDate.minusDays(1))) {
			return sprintEndDate;
		} else {
			return updatedLog;
		}
	}

	/*
	 * issues opened after getting closed within sprint maintained
	 */
	private void createRemovedFromClosedIssuesMap(Map<LocalDate, List<JiraIssue>> removedCompletedIssues,
			LocalDate removedDate, Set<JiraIssue> totalIssueList, JiraIssueCustomHistory issueHistory) {
		List<JiraIssue> jiraIssueList = new ArrayList<>(getRespectiveJiraIssue(totalIssueList, issueHistory));
		if (ObjectUtils.isNotEmpty(removedDate)) {
			removedCompletedIssues.computeIfPresent(removedDate, (k, v) -> {
				v.addAll(jiraIssueList);
				return v;
			});
			removedCompletedIssues.putIfAbsent(removedDate, jiraIssueList);
		}
	}

	/**
	 * if an issue within sprint changed from closed to open state and remain open
	 * till the sprint end then it should be removed from closed map
	 */
	private LocalDate statusChangeFromClosedToOpen(LocalDate removedDate, Set<String> closedStatus, boolean openStatus,
			JiraHistoryChangeLog jiraHistoryChangeLog, LocalDate sprintEndDate) {
		if (openStatus && closedStatus.contains(jiraHistoryChangeLog.getChangedFrom())) {
			removedDate = sprintEndDate;
		}
		return removedDate;
	}

	/**
	 * This method populates KPI value to sprint leaf nodes. It also gives the trend
	 * analysis at sprint wise.
	 * 
	 * @param sprintLeafNodeList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings(UNCHECKED)
	private void sprintWiseLeafNodeValue(List<Node> sprintLeafNodeList, KpiElement kpiElement, KpiRequest kpiRequest) {
		DataCount dataCount = new DataCount();
		String requestTrackerId = getRequestTrackerId();
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);
		Object basicProjectConfigId = latestSprint.getProjectFilter().getBasicProjectConfigId();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		Map<LocalDate, List<JiraIssue>> fullSprintIssuesMap = (Map<LocalDate, List<JiraIssue>>) resultMap
				.get("FULL_SPRINT_ISSUES");
		SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT);
		List<JiraIssueCustomHistory> issuehistory = (List<JiraIssueCustomHistory>) resultMap.get("ISSUEHISTORY");
		CapacityKpiData capacityKpiData = (CapacityKpiData) resultMap.get("AssigneeDetails");
		Map<LocalDate, Set<JiraIssue>> removedFromClosed = (Map<LocalDate, Set<JiraIssue>>) resultMap
				.get(REMOVED_FROM_CLOSED);
		List<JiraIssue> totalIssueList = new ArrayList<>((Set<JiraIssue>) resultMap.get(ISSUES));
		if (ObjectUtils.isNotEmpty(sprintDetails)) {
			// log.info("Daily Standup View -> request id : {} total jira Issues : {}",
			// requestTrackerId,
			// fullSprintIssuesMap.size());

			List<IterationKpiValue> iterationKpiValueList = new ArrayList<>();
			Set<String> roles = new HashSet<>();
			Set<String> assignees = new HashSet<>();
			Map<String, IterationKpiModalValue> modalObjectMap = KpiDataHelper.createMapOfModalObject(totalIssueList);

			Map<String, String> userWiseRole = new HashMap<>();
			Map<String, List<JiraIssue>> assigneeWiseList = totalIssueList.stream()
					.collect(Collectors.groupingBy(JiraIssue::getAssigneeId));
			if (CollectionUtils.isNotEmpty(capacityKpiData.getAssigneeCapacity())) {
				capacityKpiData.getAssigneeCapacity().forEach(assignee -> {
					if (assignee.getRole() != null)
						userWiseRole.put(assignee.getUserId(), assignee.getRole().getRoleValue());
				});
			}
			List<KPIExcelData> issueDetailList = new ArrayList<>();
			for (Map.Entry<String, List<JiraIssue>> listEntry : assigneeWiseList.entrySet()) {
				IterationKpiValue kpiValue = new IterationKpiValue();
				String role = "UNASSIGNED";
				if (MapUtils.isNotEmpty(userWiseRole)) {
					role = userWiseRole.getOrDefault(listEntry.getKey(), "UNASSIGNED");
				}
				List<JiraIssue> jiraIssueList = listEntry.getValue();
				kpiValue.setIssueIdList(jiraIssueList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
				kpiValue.setFilter1(role);
				kpiValue.setFilter2(jiraIssueList.stream().findFirst().get().getAssigneeName());
				roles.add(role);
				assignees.add(jiraIssueList.stream().findFirst().get().getAssigneeName());
				iterationKpiValueList.add(kpiValue);
				KPIExcelUtility.prepareStoryData(listEntry.getValue(), issuehistory, issueDetailList, modalObjectMap,
						fieldMapping);
			}

			Set<IterationKpiModalValue> values = new HashSet<>(modalObjectMap.values());
			kpiElement.setIssueData(values);
			dataCount.setValue(iterationKpiValueList);
			kpiElement.setTrendValueList(dataCount);
		}
	}

}
