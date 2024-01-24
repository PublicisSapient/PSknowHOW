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

import static com.publicissapient.kpidashboard.apis.util.KpiDataHelper.sprintWiseDelayCalculation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.CalculatePCDHelper;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.IterationKpiHelper;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.IterationPotentialDelay;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
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
public class IterationBurnupServiceImpl extends JiraKPIService<Map<String, Long>, List<Object>, Map<String, Object>> {
	public static final String UNCHECKED = "unchecked";
	public static final String DUE_DATE = "dueDate";
	public static final String UPDATE_DATE = "updateDate";
	public static final String PLANNED_COMPLETION = "Planned Completion";
	public static final String OVERALL_SCOPE = "Overall Scope";
	public static final String ACTUAL_COMPLETION = "Completion Till Date";
	public static final String PREDICTED_COMPLETION = "Predicted Completion";
	public static final String MIN_PREDICTED = "Minimum Prediction Date";
	public static final String MAX_REMOVAL = "Maximum Removal Date";
	public static final String MAX_COMPLETION = "Maximum Completion Date";
	public static final String DATE = "date";
	public static final String FULL_SPRINT_ISSUES = "Full Sprint Issues";
	public static final String REMOVED_FROM_CLOSED = "Removed";
	public static final String DOTTED_LINE = "Gap Between Completed and Predicted";
	private static final String SPRINT = "sprint";
	private static final String ISSUES = "issues";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Autowired
	private ConfigHelperService configHelperService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getQualifierType() {
		return KPICode.ITERATION_BURNUP.name();
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
				List<JiraIssueCustomHistory> totalHistoryList = getJiraIssuesCustomHistoryFromBaseClass();
				List<JiraIssue> totalJiraIssueList = getJiraIssuesFromBaseClass();
				Set<String> issueList = totalJiraIssueList.stream().map(JiraIssue::getNumber)
						.collect(Collectors.toSet());

				sprintDetails = IterationKpiHelper.transformIterSprintdetail(totalHistoryList, issueList,
						dbSprintDetail, fieldMapping.getJiraIterationIssuetypeKPI125(),
						fieldMapping.getJiraIterationCompletionStatusKPI125(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

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
							sprintDetails, sprintIssues,
							IterationKpiHelper.getFilteredJiraIssue(allIssues, totalJiraIssueList));
					List<JiraIssueCustomHistory> allIssuesHistory = IterationKpiHelper
							.getFilteredJiraIssueHistory(allIssues, totalHistoryList);
					Map<LocalDate, List<JiraIssue>> fullSprintIssues = new HashMap<>();
					Map<LocalDate, List<JiraIssue>> addedIssues = new HashMap<>();
					Map<LocalDate, List<JiraIssue>> removedIssues = new HashMap<>();
					Map<LocalDate, List<JiraIssue>> completedIssues = new HashMap<>();
					Map<LocalDate, List<JiraIssue>> removedCompletedIssues = new HashMap<>();
					allIssuesHistory.forEach(issueHistory -> {
						if (CollectionUtils.isNotEmpty(issueHistory.getSprintUpdationLog())) {
							List<JiraHistoryChangeLog> sprintUpdationLog = issueHistory.getSprintUpdationLog();
							Collections.sort(sprintUpdationLog,
									Comparator.comparing(JiraHistoryChangeLog::getUpdatedOn));
							createAddedandRemovedIssueDateWiseMap(sprintDetails, totalIssueList, addedIssues,
									removedIssues, fullSprintIssues, issueHistory, sprintUpdationLog);
						}
						createCompletedIssuesDateWiseMap(sprintDetails, totalIssueList, completedIssues,
								removedCompletedIssues, issueHistory, sprintStartDate);
					});

					resultListMap.put(FULL_SPRINT_ISSUES, fullSprintIssues);
					resultListMap.put(CommonConstant.PUNTED_ISSUES, removedIssues);
					resultListMap.put(CommonConstant.ADDED_ISSUES, addedIssues);
					resultListMap.put(CommonConstant.COMPLETED_ISSUES, completedIssues);
					resultListMap.put(REMOVED_FROM_CLOSED, removedCompletedIssues);
					resultListMap.put(SPRINT, sprintDetails);
					resultListMap.put(ISSUES, totalIssueList);
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

		String requestTrackerId = getRequestTrackerId();
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);
		Object basicProjectConfigId = latestSprint.getProjectFilter().getBasicProjectConfigId();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT);
		if (ObjectUtils.isNotEmpty(sprintDetails)) {
			Map<LocalDate, List<JiraIssue>> fullSprintIssuesMap = (Map<LocalDate, List<JiraIssue>>) resultMap
					.get(FULL_SPRINT_ISSUES);
			Map<LocalDate, List<JiraIssue>> removedIssuesMap = (Map<LocalDate, List<JiraIssue>>) resultMap
					.get(CommonConstant.PUNTED_ISSUES);
			Map<LocalDate, List<JiraIssue>> addedIssuesMap = (Map<LocalDate, List<JiraIssue>>) resultMap
					.get(CommonConstant.ADDED_ISSUES);
			Map<LocalDate, List<JiraIssue>> completedIssueMap = (Map<LocalDate, List<JiraIssue>>) resultMap
					.get(CommonConstant.COMPLETED_ISSUES);
			Map<LocalDate, Set<JiraIssue>> removedFromClosed = (Map<LocalDate, Set<JiraIssue>>) resultMap
					.get(REMOVED_FROM_CLOSED);
			List<JiraIssue> totalIssueList = new ArrayList<>((Set<JiraIssue>) resultMap.get(ISSUES));

			log.info("Iteration Burnups -> request id : {} total jira Issues : {}", requestTrackerId,
					fullSprintIssuesMap.size());
			List<String> totalSprintDetailsIssues = sprintDetails.getTotalIssues().stream().map(SprintIssue::getNumber)
					.collect(Collectors.toList());

			// CALCULATION OF PCDS OF ALL ISSUES
			List<JiraIssue> totalJiraIssueList = totalIssueList.stream()
					.filter(issue -> totalSprintDetailsIssues.contains(issue.getNumber())).collect(Collectors.toList());
			List<IterationPotentialDelay> iterationPotentialDelayList = calculatePotentialDelay(sprintDetails,
					totalJiraIssueList, fieldMapping);
			Map<String, IterationPotentialDelay> issueWiseDelay = iterationPotentialDelayList.stream()
					.collect(Collectors.toMap(IterationPotentialDelay::getIssueId, Function.identity(), (e1, e2) -> e2,
							LinkedHashMap::new));
			Set<JiraIssue> allPotentialDelayIssue = totalJiraIssueList.stream()
					.filter(i -> issueWiseDelay.containsKey(i.getNumber())).collect(Collectors.toSet());
			Map<LocalDate, List<JiraIssue>> potentialDelay = allPotentialDelayIssue.stream()
					.filter(f -> StringUtils.isNotBlank(issueWiseDelay.get(f.getNumber()).getPredictedCompletedDate()))
					.collect(Collectors.groupingBy(
							f -> LocalDate.parse(issueWiseDelay.get(f.getNumber()).getPredictedCompletedDate())));

			LocalDate sprintStartDate = DateUtil.stringToLocalDate(sprintDetails.getStartDate(),
					DateUtil.TIME_FORMAT_WITH_SEC);
			LocalDate sprintEndDate = DateUtil
					.stringToLocalDate(sprintDetails.getEndDate(), DateUtil.TIME_FORMAT_WITH_SEC).plusDays(1);

			Map<String, LocalDate> maxCompleteAndMinPCDDate = getMaxCompleteAndMinPCDDate(completedIssueMap,
					potentialDelay, removedIssuesMap);
			LocalDate maxCompletionDate = maxCompleteAndMinPCDDate.getOrDefault(MAX_COMPLETION, null);
			LocalDate minimumpredicateddate = maxCompleteAndMinPCDDate.getOrDefault(MIN_PREDICTED, null);
			LocalDate maximumRemovalDate = maxCompleteAndMinPCDDate.get(MAX_REMOVAL);

			List<DataCountGroup> dataCountGroups = new ArrayList<>();
			List<JiraIssue> processedAllIssues = new ArrayList<>();
			List<JiraIssue> processedPlannedIssues = new ArrayList<>();
			List<JiraIssue> processCompletedIssues = new ArrayList<>();
			List<JiraIssue> pcdIssues = new ArrayList<>();

			for (LocalDate date = sprintStartDate; date.isBefore(sprintEndDate); date = date.plusDays(1)) {
				DataCountGroup dataCountGroup = new DataCountGroup();
				List<DataCount> dataCountList = new ArrayList<>();
				Long dueDateWiseTypeCountMap = calculateOverallScopeDayWise(fullSprintIssuesMap, removedIssuesMap,
						addedIssuesMap, processedAllIssues, date, sprintDetails, maximumRemovalDate);
				Long plannedDateWiseTypeCount = processFieldWiseeIssues(processedAllIssues, date,
						processedPlannedIssues, DUE_DATE);
				dataCountList.add(getDataCountObject(dueDateWiseTypeCountMap, latestSprint.getId(), OVERALL_SCOPE));
				dataCountList
						.add(getDataCountObject(plannedDateWiseTypeCount, latestSprint.getId(), PLANNED_COMPLETION));
				if (ObjectUtils.isNotEmpty(maxCompletionDate)
						&& (date.isBefore(maxCompletionDate) || date.isEqual(maxCompletionDate))) {
					List<JiraIssue> completedIssues = completedIssueMap.getOrDefault(date, new ArrayList<>());
					completedIssues.addAll(processCompletedIssues);
					completedIssues.retainAll(processedAllIssues);
					removeExtraTransitionOnSprintEndDate(sprintDetails, maxCompletionDate, completedIssues,
							sprintDetails.getNotCompletedIssues().stream().map(SprintIssue::getNumber)
									.collect(Collectors.toList()));
					completedIssues.removeAll(removedFromClosed.getOrDefault(date, new HashSet<>()));
					Long closedDateWiseCount = processFieldWiseeIssues(completedIssues, date, processCompletedIssues,
							UPDATE_DATE);
					dataCountList.add(getDataCountObject(closedDateWiseCount, latestSprint.getId(), ACTUAL_COMPLETION));
				}
				if (ObjectUtils.isNotEmpty(minimumpredicateddate)
						&& (date.isEqual(minimumpredicateddate) || date.isAfter(minimumpredicateddate))) {
					pcdIssues.addAll(potentialDelay.getOrDefault(date, new ArrayList<>()));
					dataCountList.add(
							getDataCountObject((long) pcdIssues.size(), latestSprint.getId(), PREDICTED_COMPLETION));
				}

				dataCountGroup.setFilter(date.toString());
				dataCountGroup.setValue(dataCountList);
				dataCountGroups.add(dataCountGroup);
			}

			IterationKpiValue iterationKpiValue = new IterationKpiValue();
			iterationKpiValue.setDataGroup(dataCountGroups);
			iterationKpiValue.setFilter1("OVERALL");
			iterationKpiValue.setAdditionalGroup(Arrays.asList(DOTTED_LINE));
			List<IterationKpiValue> iterationKpiValueList = new ArrayList<>();
			iterationKpiValueList.add(iterationKpiValue);
			kpiElement.setTrendValueList(iterationKpiValueList);
		}
	}

	private Long processFieldWiseeIssues(List<JiraIssue> processedAllIssues, LocalDate date,
			List<JiraIssue> processedPlannedIssues, String keyCheck) {
		if (keyCheck.equalsIgnoreCase(DUE_DATE)) {
			processedPlannedIssues.addAll(processedAllIssues.stream()
					.filter(f -> StringUtils.isNotBlank(f.getDueDate())
							&& DateUtil.stringToLocalDate(f.getDueDate(), DateUtil.TIME_FORMAT_WITH_SEC).isEqual(date))
					.distinct().collect(Collectors.toList()));
			return (long) processedPlannedIssues.size();

		} else if (keyCheck.equalsIgnoreCase(UPDATE_DATE)) {
			processedPlannedIssues.addAll(processedAllIssues.stream().filter(
					f -> StringUtils.isNotBlank(f.getUpdateDate()) && LocalDate.parse(f.getUpdateDate()).isEqual(date))
					.distinct().collect(Collectors.toList()));
			return (long) processedAllIssues.size();
		}
		return 0L;
	}

	private Long calculateOverallScopeDayWise(Map<LocalDate, List<JiraIssue>> allIssues,
			Map<LocalDate, List<JiraIssue>> removedIssues, Map<LocalDate, List<JiraIssue>> addedIssues,
			List<JiraIssue> processedIssues, LocalDate date, SprintDetails sprintDetails,
			LocalDate maximumRemovalDate) {
		List<JiraIssue> allIssuesOrDefault = allIssues.getOrDefault(date, new ArrayList<>());
		List<JiraIssue> removedJiraIssues = removedIssues.getOrDefault(date, new ArrayList<>());
		List<JiraIssue> addedJiraIssues = addedIssues.getOrDefault(date, new ArrayList<>());
		Set<String> puntedIssues = checkNullList(sprintDetails.getPuntedIssues()).stream().map(SprintIssue::getNumber)
				.collect(Collectors.toSet());
		removeExtraTransitionOnSprintEndDate(sprintDetails, maximumRemovalDate, removedJiraIssues,
				sprintDetails.getTotalIssues().stream().map(SprintIssue::getNumber).collect(Collectors.toList()));

		// if an issue is present in both on the same day, then whatever in the punted
		// issues those should be removed once and for all
		List<JiraIssue> commonIssues = (List<JiraIssue>) CollectionUtils.intersection(removedJiraIssues,
				addedJiraIssues);
		// if punted issues are present in commonIssues
		// remove from both, else just remove from added
		if (CollectionUtils.isNotEmpty(commonIssues)) {
			commonIssues.stream().forEach(issue -> {
				if (puntedIssues.contains(issue.getNumber())) {
					removedJiraIssues.removeIf(jira -> issue.getNumber().equalsIgnoreCase(jira.getNumber()));
					addedJiraIssues.removeIf(jira -> issue.getNumber().equalsIgnoreCase(jira.getNumber()));
					allIssuesOrDefault.removeIf(jira -> issue.getNumber().equalsIgnoreCase(jira.getNumber()));
				} else {
					// if not in punted then not to be removed from remove issues
					removedJiraIssues.removeIf(jira -> issue.getNumber().equalsIgnoreCase(jira.getNumber()));
				}
			});
		}
		processedIssues.addAll(allIssuesOrDefault);
		processedIssues.addAll(addedJiraIssues);
		processedIssues.removeAll(removedJiraIssues);
		processedIssues = processedIssues.stream().distinct().collect(Collectors.toList());
		return (long) processedIssues.size();

	}

	/*
	 * if on the last day of sprint closure, some issues get closed after the sprint
	 * end time but sprint report has fixed them in not completed segment, then
	 * those has to be removed from completed Issue Map
	 */
	private void removeExtraTransitionOnSprintEndDate(SprintDetails sprintDetails, LocalDate maximumRemovalDate,
			List<JiraIssue> baseIssues, List<String> issuesToBeRemoved) {
		if (sprintDetails.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_CLOSED)
				&& ObjectUtils.isNotEmpty(maximumRemovalDate)) {
			baseIssues.removeIf(issue -> issuesToBeRemoved.contains(issue.getNumber()));
		}
	}

	/**
	 * with assignees criteria calculating potential delay for inprogress and open
	 * issues and without assignees calculating potential delay for inprogress
	 * stories
	 *
	 * @param sprintDetails
	 * @param allIssues
	 * @param fieldMapping
	 * @return
	 */
	private List<IterationPotentialDelay> calculatePotentialDelay(SprintDetails sprintDetails,
			List<JiraIssue> allIssues, FieldMapping fieldMapping) {
		List<IterationPotentialDelay> iterationPotentialDelayList = new ArrayList<>();
		Map<String, List<JiraIssue>> assigneeWiseJiraIssue = allIssues.stream()
				.filter(jiraIssue -> jiraIssue.getAssigneeId() != null)
				.collect(Collectors.groupingBy(JiraIssue::getAssigneeId));

		if (MapUtils.isNotEmpty(assigneeWiseJiraIssue)) {
			assigneeWiseJiraIssue.forEach((assignee, jiraIssues) -> {
				List<JiraIssue> inProgressIssues = new ArrayList<>();
				List<JiraIssue> openIssues = new ArrayList<>();
				CalculatePCDHelper.arrangeJiraIssueList(fieldMapping.getJiraStatusForInProgressKPI125(), jiraIssues,
						inProgressIssues, openIssues);
				iterationPotentialDelayList
						.addAll(sprintWiseDelayCalculation(inProgressIssues, openIssues, sprintDetails));
			});
		}

		if (CollectionUtils.isNotEmpty(fieldMapping.getJiraStatusForInProgressKPI125())) {
			List<JiraIssue> inProgressIssues = allIssues.stream()
					.filter(jiraIssue -> (jiraIssue.getAssigneeId() == null)
							&& StringUtils.isNotEmpty(jiraIssue.getDueDate())
							&& (fieldMapping.getJiraStatusForInProgressKPI125().contains(jiraIssue.getStatus())))
					.collect(Collectors.toList());

			List<JiraIssue> openIssues = new ArrayList<>();
			iterationPotentialDelayList.addAll(sprintWiseDelayCalculation(inProgressIssues, openIssues, sprintDetails));
		}
		return iterationPotentialDelayList;
	}

	/*
	 * PCD will be shown from todays date till sprint end, if something got
	 * completed today then actual completion to be shown till today with
	 * cumulation, otherwise till today-1 date
	 */

	private Map<String, LocalDate> getMaxCompleteAndMinPCDDate(Map<LocalDate, List<JiraIssue>> completedIssueMap,
			Map<LocalDate, List<JiraIssue>> potentialDelay, Map<LocalDate, List<JiraIssue>> removedIssuesMap) {
		Map<String, LocalDate> dateMap = new HashMap<>();
		LocalDate maxCompletionDate = null;
		LocalDate minimumpredicateddate = null;
		if (MapUtils.isNotEmpty(completedIssueMap)) {
			maxCompletionDate = completedIssueMap.keySet().stream().filter(Objects::nonNull).max(LocalDate::compareTo)
					.orElse(null);
		}
		if (MapUtils.isNotEmpty(potentialDelay)) {
			minimumpredicateddate = potentialDelay.keySet().stream().filter(Objects::nonNull).min(LocalDate::compareTo)
					.orElse(null);
		}
		if ((Objects.nonNull(maxCompletionDate) && Objects.nonNull(minimumpredicateddate))
				&& (maxCompletionDate.isBefore(minimumpredicateddate))) {
			maxCompletionDate = minimumpredicateddate.minusDays(1);

		}
		dateMap.put(MIN_PREDICTED, minimumpredicateddate);
		dateMap.put(MAX_COMPLETION, maxCompletionDate);
		dateMap.put(MAX_REMOVAL,
				removedIssuesMap.keySet().stream().filter(Objects::nonNull).min(LocalDate::compareTo).orElse(null));
		return dateMap;
	}

	private DataCount getDataCountObject(Long value, String projectName, String label) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSProjectName(projectName);
		dataCount.setKpiGroup(label);
		dataCount.setValue(value);
		return dataCount;
	}

}
