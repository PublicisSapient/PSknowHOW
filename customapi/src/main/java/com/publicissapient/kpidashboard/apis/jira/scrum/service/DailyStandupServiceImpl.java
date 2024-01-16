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

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.CalculatePCDHelper;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.Filter;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.IterationKpiHelper;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.AssigneeCapacity;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.jira.IterationPotentialDelay;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueReleaseStatusRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * kpi of iteration dashboard, Daily Standup View which runs for the active
 * sprint, and return data for both the screens
 * 
 * @author shi6
 */

@Component
@Slf4j
public class DailyStandupServiceImpl extends JiraKPIService<Map<String, Long>, List<Object>, Map<String, Object>> {
	public static final String UNCHECKED = "unchecked";
	public static final String UNASSIGNED = "Unassigned";
	private static final String SPRINT = "sprint";
	private static final String ISSUES = "issues";
	private static final String CLOSE_STATUS = "close status";
	private static final String STATUS_CATEGORY = "status category";
	public static final String NOT_COMPLETED_JIRAISSUE = "notCompletedJiraIssue";
	public static final String ASSIGNEE_DETAILS = "AssigneeDetails";
	public static final String REMAINING_CAPACITY = "Remaining Capacity";
	public static final String REMAINING_ESTIMATE = "Remaining Estimate";
	public static final String REMAINING_WORK = "Remaining Work";
	public static final String DELAY = "Delay";
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	private static final String HISTORY_ISSUES = "historyIssue";
	private static final String EPICS = "epics";
	private static final String FILTER_BUTTON = "button";
	private static final String FILTER_INPROGRESS_SCR2 = "In Progress";
	private static final String FILTER_CLOSED_SCR2 = "Done";
	private static final String FILTER_OPEN_SCR2 = "Open";
	private static final String FILTER_ONHOLD_SCR2 = "On Hold";

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CapacityKpiDataRepository capacityKpiDataRepository;

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private JiraIssueReleaseStatusRepository jiraIssueReleaseStatusRepository;

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
	 * This method populates KPI value to sprint leaf nodes. It also gives the trend
	 * analysis at sprint wise.
	 */
	@SuppressWarnings(UNCHECKED)
	private void sprintWiseLeafNodeValue(List<Node> sprintLeafNodeList, KpiElement kpiElement, KpiRequest kpiRequest) {
		sprintLeafNodeList.sort(Comparator.comparing(node -> node.getSprintFilter().getStartDate()));
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);
		Object basicProjectConfigId = Objects.requireNonNull(latestSprint).getProjectFilter().getBasicProjectConfigId();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
		List<UserWiseCardDetail> userWiseCardDetails = new ArrayList<>();

		// fetch from db
		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT);
		if (ObjectUtils.isNotEmpty(sprintDetails)) {
			List<JiraIssue> notCompletedJiraIssue = (List<JiraIssue>) resultMap.get(NOT_COMPLETED_JIRAISSUE);
			CapacityKpiData capacityKpiData = (CapacityKpiData) resultMap.get(ASSIGNEE_DETAILS);
			List<JiraIssue> totalIssueList = new ArrayList<>((Set<JiraIssue>) resultMap.get(ISSUES));
			JiraIssueReleaseStatus statusCategory = (JiraIssueReleaseStatus) resultMap.get(STATUS_CATEGORY);
			resultMap.put(CLOSE_STATUS, getClosedStatus(fieldMapping, sprintDetails));
			Map<String, Set<String>> parentChildRelation = findLinkedSubTasks(totalIssueList, fieldMapping);

			Map<String, String> userWiseRole = new HashMap<>();
			Map<String, StandUpViewKpiData> userWiseRemainingCapacity = new HashMap<>();
			Map<String, StandUpViewKpiData> remianingWork = new HashMap<>();
			Map<String, StandUpViewKpiData> assigneeWiseRemaingEstimate = new HashMap<>();
			Map<String, StandUpViewKpiData> assigneeWiseMaxDelay = new HashMap<>();
			Function<JiraIssue, String> function = issue -> {
				if (issue.getAssigneeId() == null) {
					issue.setAssigneeName(UNASSIGNED);
					return UNASSIGNED;
				}
				return issue.getAssigneeId();
			};
			Map<String, IterationKpiModalValue> mapOfModalObject = KpiDataHelper.createMapOfModalObject(totalIssueList);
			Map<String, List<JiraIssue>> assigneeWiseList = totalIssueList.stream()
					.collect(Collectors.groupingBy(function));

			Map<String, List<JiraIssue>> assigneeWiseNotCompleted = notCompletedJiraIssue.stream()
					.collect(Collectors.groupingBy(function));
			String estimationCriteria = fieldMapping.getEstimationCriteria();

			// calculate remaining estimate
			calculateAssigneeWiseRemainingEstimate(assigneeWiseNotCompleted, remianingWork, assigneeWiseRemaingEstimate,
					estimationCriteria);

			// Calculate Delay
			List<IterationPotentialDelay> iterationPotentialDelayList = CalculatePCDHelper.calculatePotentialDelay(
					sprintDetails, notCompletedJiraIssue, fieldMapping.getJiraStatusForInProgressKPI154());

			Map<String, IterationPotentialDelay> issueWiseDelay = CalculatePCDHelper.checkMaxDelayAssigneeWise(
					iterationPotentialDelayList, fieldMapping.getJiraStatusForInProgressKPI154());
			calculateAssigneeWiseMaxDelay(issueWiseDelay, assigneeWiseMaxDelay);

			// Calculate Remaining Capacity
			calculateRemainingCapacity(sprintDetails, capacityKpiData, userWiseRole, userWiseRemainingCapacity);

			StandUpViewKpiData defaultRemainingWork = estimationCriteria.equalsIgnoreCase(CommonConstant.STORY_POINT)
					? StandUpViewKpiData.builder().value(Constant.DASH).unit1(CommonConstant.SP).build()
					: StandUpViewKpiData.builder().value(Constant.DASH).unit1(CommonConstant.HOURS).build();

			Set<String> allRoles = new HashSet<>();
			for (Map.Entry<String, List<JiraIssue>> listEntry : assigneeWiseList.entrySet()) {
				UserWiseCardDetail userWiseCardDetail = new UserWiseCardDetail();
				LinkedHashMap<String, StandUpViewKpiData> cardDetails = new LinkedHashMap<>();
				String role = UNASSIGNED;
				if (MapUtils.isNotEmpty(userWiseRole)) {
					role = userWiseRole.getOrDefault(listEntry.getKey(), UNASSIGNED);
				}
				List<JiraIssue> jiraIssueList = listEntry.getValue();
				populateModal(jiraIssueList, resultMap, sprintDetails, parentChildRelation, issueWiseDelay,
						mapOfModalObject, fieldMapping);
				String assigneeId = listEntry.getKey();
				String assigneeName = jiraIssueList.stream().findFirst().orElse(new JiraIssue()).getAssigneeName();

				cardDetails.put(REMAINING_CAPACITY, userWiseRemainingCapacity.getOrDefault(assigneeId,
						StandUpViewKpiData.builder().value(Constant.DASH).unit(CommonConstant.DAY).build()));
				cardDetails.put(REMAINING_ESTIMATE, assigneeWiseRemaingEstimate.getOrDefault(assigneeId,
						StandUpViewKpiData.builder().value(Constant.DASH).unit(CommonConstant.DAY).build()));
				cardDetails.put(REMAINING_WORK, remianingWork.getOrDefault(assigneeId, defaultRemainingWork));
				cardDetails.put(DELAY, assigneeWiseMaxDelay.getOrDefault(assigneeId,
						StandUpViewKpiData.builder().value(Constant.DASH).unit(CommonConstant.DAY).build()));

				userWiseCardDetail.setCardDetails(cardDetails);
				userWiseCardDetail.setAssigneeId(assigneeId);
				userWiseCardDetail.setRole(role);
				allRoles.add(role);
				userWiseCardDetail.setAssigneeName(assigneeName);
				userWiseCardDetails.add(userWiseCardDetail);
			}
			// set filter on Second Screen
			setFilters(kpiElement, fieldMapping, allRoles, statusCategory);
			kpiElement.setIssueData(new HashSet<>(mapOfModalObject.values()));
			userWiseCardDetails.sort(Comparator.comparing(UserWiseCardDetail::getAssigneeName));
			kpiElement.setModalHeads(KPIExcelColumn.DAILY_STANDUP_VIEW.getColumns());

		}
		kpiElement.setTrendValueList(userWiseCardDetails);

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
			log.info("Daily Standup View -> Requested sprint : {}", leafNode.getName());
			SprintDetails sprintDetails;
			SprintDetails dbSprintDetail = getSprintDetailsFromBaseClass();
			if (null != dbSprintDetail) {
				ObjectId basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId();
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

				// to modify sprintdetails on the basis of configuration for the project
				List<JiraIssueCustomHistory> totalHistoryList = getJiraIssuesCustomHistoryFromBaseClass();
				List<JiraIssue> totalJiraIssueList = getJiraIssuesFromBaseClass();
				Set<String> issueList = totalJiraIssueList.stream().map(JiraIssue::getNumber)
						.collect(Collectors.toSet());
				sprintDetails = IterationKpiHelper.transformIterSprintdetail(totalHistoryList, issueList,
						dbSprintDetail, fieldMapping.getJiraIterationCompletionStatusKPI119(),
						fieldMapping.getJiraIterationCompletionStatusKPI119(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> notCompletedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(
						sprintDetails, CommonConstant.NOT_COMPLETED_ISSUES);
				List<String> allIssues = new ArrayList<>();
				allIssues.addAll(notCompletedIssues);
				allIssues.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.COMPLETED_ISSUES));

				if (CollectionUtils.isNotEmpty(allIssues)) {
					List<JiraIssue> filteredAllJiraIssue = IterationKpiHelper.getFilteredJiraIssue(allIssues,
							totalJiraIssueList);
					Set<JiraIssue> totalIssueList = KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(
							sprintDetails, null, filteredAllJiraIssue);

					List<JiraIssue> filteredNotCompletedJiraIssue = IterationKpiHelper
							.getFilteredJiraIssue(notCompletedIssues, new ArrayList<>(totalIssueList));

					List<JiraIssueCustomHistory> issueHistoryList = IterationKpiHelper.getFilteredJiraIssueHistory(
							totalIssueList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()),
							totalHistoryList);

					if (CollectionUtils.isNotEmpty(fieldMapping.getJiraSubTaskIdentification())) {
						List<String> taskType = fieldMapping.getJiraSubTaskIdentification();
						processSubtaskFromDb(basicProjectConfigId, allIssues, totalIssueList, issueHistoryList,
								taskType);
					}

					Set<JiraIssue> epics = jiraIssueRepository.findNumberInAndBasicProjectConfigIdAndTypeName(
							totalIssueList.stream().map(JiraIssue::getEpicLinked).collect(Collectors.toList()),
							leafNode.getProjectFilter().getBasicProjectConfigId().toString(),
							NormalizedJira.ISSUE_TYPE.getValue());

					// get sprint start and sprint end time
					resultListMap.put(SPRINT, sprintDetails);
					// all jira issue within sprint
					resultListMap.put(ISSUES, totalIssueList);
					// get all the transitions occurred on the issue (Screen-2)
					resultListMap.put(HISTORY_ISSUES, issueHistoryList);
					// get epic name linked with the story (Screen-2)
					resultListMap.put(EPICS, epics);
					// calculate remaining estimate and delays
					resultListMap.put(NOT_COMPLETED_JIRAISSUE, filteredNotCompletedJiraIssue);
					// to get the capacity of assignees(Screen-1)
					resultListMap.put(ASSIGNEE_DETAILS, capacityKpiDataRepository.findBySprintIDAndBasicProjectConfigId(
							sprintDetails.getSprintID(), sprintDetails.getBasicProjectConfigId()));
					// release status category for status filters
					resultListMap.put(STATUS_CATEGORY, jiraIssueReleaseStatusRepository
							.findByBasicProjectConfigId(basicProjectConfigId.toString()));

				}
			}
		}
		return resultListMap;
	}

	private void processSubtaskFromDb(ObjectId basicProjectConfigId, List<String> allIssues,
			Set<JiraIssue> totalIssueList, List<JiraIssueCustomHistory> issueHistoryList, List<String> taskType) {
		// combined both sub-tasks and totalIssuelist
		Set<JiraIssue> subTasksJiraIssue = jiraIssueRepository
				.findByBasicProjectConfigIdAndParentStoryIdInAndOriginalTypeIn(basicProjectConfigId.toString(),
						new HashSet<>(allIssues), taskType);
		if (CollectionUtils.isNotEmpty(subTasksJiraIssue)) {
			totalIssueList.addAll(subTasksJiraIssue);
			issueHistoryList.addAll(jiraIssueCustomHistoryRepository.findByStoryIDInAndBasicProjectConfigId(
					subTasksJiraIssue.stream().map(JiraIssue::getNumber).collect(Collectors.toSet()),
					basicProjectConfigId.toString()));
		}
	}

	private Set<String> getClosedStatus(FieldMapping fieldMapping, SprintDetails sprintDetails) {
		return fieldMapping != null && CollectionUtils.isNotEmpty(fieldMapping.getJiraIterationCompletionStatusKPI154())
				? new HashSet<>(fieldMapping.getJiraIterationCompletionStatusKPI154())
				: sprintDetails.getCompletedIssues().stream().map(SprintIssue::getStatus).collect(Collectors.toSet());
	}

	/*
	 * create map of parent linked with children issueType
	 */
	private Map<String, Set<String>> findLinkedSubTasks(List<JiraIssue> jiraIssueList, FieldMapping fieldMapping) {
		Map<String, Set<String>> parentChild = new HashMap<>();
		if (CollectionUtils.isNotEmpty(fieldMapping.getJiraSubTaskIdentification())) {
			Set<String> tasks = new HashSet<>(fieldMapping.getJiraSubTaskIdentification());
			jiraIssueList.stream()
					.filter(issue -> tasks.contains(issue.getOriginalType())
							&& CollectionUtils.isNotEmpty(issue.getParentStoryId()))
					.forEach(childIssue -> childIssue.getParentStoryId().forEach(parent -> {
						parentChild.computeIfPresent(parent, (k, v) -> {
							v.add(childIssue.getNumber());
							return v;
						});
						parentChild.computeIfAbsent(parent, k -> new HashSet<>()).add(childIssue.getNumber());
					}));
		}
		return parentChild;
	}

	/**
	 * calculate remaining estimate on the basis of fieldmapping
	 */
	private void calculateAssigneeWiseRemainingEstimate(Map<String, List<JiraIssue>> assigneeWiseNotCompleted,
			Map<String, StandUpViewKpiData> remainingWork,
			Map<String, StandUpViewKpiData> assigneeWiseRemainingEstimate, String estimationCriteria) {
		String estimationUnit = estimationCriteria.equalsIgnoreCase(CommonConstant.STORY_POINT) ? CommonConstant.SP
				: CommonConstant.DAY;

		assigneeWiseNotCompleted.forEach((assigneeId, jiraIssueList) -> {
			double totalEstimate = jiraIssueList.stream()
					.mapToDouble(issue -> estimationCriteria.equalsIgnoreCase(CommonConstant.STORY_POINT)
							? Optional.ofNullable(issue.getStoryPoints()).orElse(0d) // NOSONAR
							: Optional.ofNullable(issue.getOriginalEstimateMinutes()).orElse(0))
					.sum();
			remainingWork.put(assigneeId, new StandUpViewKpiData(String.valueOf(jiraIssueList.size()),
					String.valueOf(totalEstimate), null, estimationUnit));

			int totalRemainingEstimate = jiraIssueList.stream()
					.mapToInt(issue -> Optional.ofNullable(issue.getRemainingEstimateMinutes()).orElse(0)).sum();
			assigneeWiseRemainingEstimate.put(assigneeId,
					new StandUpViewKpiData(String.valueOf(totalRemainingEstimate), null, CommonConstant.DAY, null));
		});
	}

	private void calculateAssigneeWiseMaxDelay(Map<String, IterationPotentialDelay> issueWiseDelay,
			Map<String, StandUpViewKpiData> assigneeWiseMaxDelay) {
		List<IterationPotentialDelay> maxPotentialDelayList = new ArrayList<>(issueWiseDelay.values());
		Map<String, List<IterationPotentialDelay>> collect = maxPotentialDelayList.stream()
				.filter(IterationPotentialDelay::isMaxMarker)
				.collect(Collectors.groupingBy(IterationPotentialDelay::getAssigneeId));

		collect.forEach((assigneeId, delayList) -> {
			int totalDelayInMinutes = (int) delayList.stream()
					.mapToDouble(delay -> Optional.of(delay.getPotentialDelay()).orElse(0) * 60 * 8).sum();

			assigneeWiseMaxDelay.put(assigneeId,
					new StandUpViewKpiData(String.valueOf(totalDelayInMinutes), null, CommonConstant.DAY, null));
		});
	}

	private void calculateRemainingCapacity(SprintDetails sprintDetails, CapacityKpiData capacityKpiData,
			Map<String, String> userWiseRole, Map<String, StandUpViewKpiData> userWiseRemainingCapacity) {
		LocalDate sprintStartDate = LocalDate.parse(sprintDetails.getStartDate().split("T")[0], DATE_FORMATTER);
		LocalDate sprintEndDate = LocalDate.parse(sprintDetails.getEndDate().split("T")[0], DATE_FORMATTER);
		int daysBetween = checkWorkingDays(sprintStartDate, sprintEndDate);
		int daysLeft = checkWorkingDays(LocalDate.now(), sprintEndDate);

		if (capacityKpiData != null && CollectionUtils.isNotEmpty(capacityKpiData.getAssigneeCapacity())) {
			capacityKpiData.getAssigneeCapacity().forEach(assignee -> {
				getAssigneeWiseAvailableCapacity(daysBetween, daysLeft, assignee, userWiseRemainingCapacity);
				if (assignee.getRole() != null)
					userWiseRole.put(assignee.getUserId(), assignee.getRole().getRoleValue());
			});
		}
	}

	/*
	 * excluding weekends in calculating of capacity
	 */
	private int checkWorkingDays(LocalDate startDate, LocalDate endDate) {
		int incrementCounter = 1;
		if (startDate.getDayOfWeek() == DayOfWeek.SATURDAY || startDate.getDayOfWeek() == DayOfWeek.SUNDAY)
			incrementCounter--;
		return CommonUtils.getWorkingDays(startDate, endDate) + incrementCounter;
	}

	/**
	 * calculate Remaining Capacity (available capacity/days in sprint)* remaining
	 * days
	 */
	private void getAssigneeWiseAvailableCapacity(int daysBetween, int daysLeft, AssigneeCapacity assignee,
			Map<String, StandUpViewKpiData> userWiseRemainingCapacity) {
		if (assignee.getAvailableCapacity() != null) {
			double remainingCapacity = roundingOff((assignee.getAvailableCapacity() / daysBetween) * daysLeft);
			// capacity/8hrs, to calculate in days
			double remainingCapacityInMinutes = remainingCapacity * 60;
			userWiseRemainingCapacity.putIfAbsent(assignee.getUserId(),
					new StandUpViewKpiData(String.valueOf(remainingCapacityInMinutes), null, CommonConstant.DAY, null));
		}
	}

	private void populateModal(List<JiraIssue> jiraIssueList, Map<String, Object> resultMap,
			SprintDetails sprintDetails, Map<String, Set<String>> linkedSubTasks,
			Map<String, IterationPotentialDelay> issueWiseDelay, Map<String, IterationKpiModalValue> mapOfModalObject,
			FieldMapping fieldMapping) {
		List<JiraIssue> epicList = new ArrayList<>((Set<JiraIssue>) resultMap.get(EPICS));

		Map<String, String> epicMap = epicList.stream()
				.collect(Collectors.toMap(JiraIssue::getNumber, JiraIssue::getName));
		List<JiraIssueCustomHistory> totalHistoryList = (List<JiraIssueCustomHistory>) resultMap.get(HISTORY_ISSUES);
		Set<String> closedStatus = (Set<String>) resultMap.get(CLOSE_STATUS);

		if (CollectionUtils.isNotEmpty(totalHistoryList)) {
			LocalDate sprintStartDate = LocalDate.parse(sprintDetails.getStartDate().split("T")[0], DATE_FORMATTER);
			LocalDateTime sprintStartDateTime = LocalDateTime.parse(sprintDetails.getStartDate().split("\\.")[0],
					DATE_TIME_FORMATTER);
			LocalDate sprintEndDate = LocalDate.parse(sprintDetails.getEndDate().split("T")[0], DATE_FORMATTER);

			for (JiraIssue jiraIssue : jiraIssueList) {
				KPIExcelUtility.populateIterationKPI(null, null, jiraIssue, fieldMapping, mapOfModalObject);
				JiraIssueCustomHistory issueHistory = totalHistoryList.stream()
						.filter(jiraIssueCustomHistory -> jiraIssueCustomHistory.getStoryID()
								.equalsIgnoreCase(jiraIssue.getNumber()))
						.findFirst().orElse(new JiraIssueCustomHistory());

				List<JiraHistoryChangeLog> inSprintStatusLogs = getInSprintStatusLogs(
						issueHistory.getStatusUpdationLog(), sprintStartDate, sprintEndDate);
				List<JiraHistoryChangeLog> inSprintAssigneeLogs = getInSprintStatusLogs(
						issueHistory.getAssigneeUpdationLog(), sprintStartDate, sprintEndDate);
				List<JiraHistoryChangeLog> inSprintWorkLogs = getInSprintStatusLogs(issueHistory.getWorkLog(),
						sprintStartDate, sprintEndDate);

				IterationKpiModalValue iterationKpiModalValue = mapOfModalObject.get(jiraIssue.getNumber());
				// getDevCompletion Date
				iterationKpiModalValue.setDevCompletionDateInTime(
						getDevCompletionDateInTime(issueHistory, fieldMapping.getJiraDevDoneStatusKPI154()));

				if (CollectionUtils.isNotEmpty(jiraIssue.getSprintIdList()) && jiraIssue.getSprintIdList().size() > 1)
					iterationKpiModalValue.setSpill(true);

				getMaxCompleteMaxTestDevStartTime(inSprintStatusLogs, fieldMapping, iterationKpiModalValue,
						closedStatus, jiraIssue, sprintStartDateTime.toString());

				setPCDandDelay(iterationKpiModalValue, issueWiseDelay, jiraIssue);
				iterationKpiModalValue.setStatusLogGroup(
						createDateWiseLogs(inSprintStatusLogs, iterationKpiModalValue.getActualStartDateInTime(),
								iterationKpiModalValue.getActualCompletionDateInTime()));
				iterationKpiModalValue.setWorkLogGroup(createDateWiseLogs(inSprintWorkLogs));
				iterationKpiModalValue.setAssigneeLogGroup(
						createDateWiseLogs(inSprintAssigneeLogs, iterationKpiModalValue.getActualStartDateInTime(),
								iterationKpiModalValue.getActualCompletionDateInTime()));

				iterationKpiModalValue.setTimeWithUser(
						calculateWithLastTime(inSprintAssigneeLogs, issueHistory.getAssigneeUpdationLog(),
								sprintStartDateTime, iterationKpiModalValue.getActualCompletionDateInTime()));
				iterationKpiModalValue.setTimeWithStatus(
						calculateWithLastTime(inSprintStatusLogs, issueHistory.getStatusUpdationLog(),
								sprintStartDateTime, iterationKpiModalValue.getActualCompletionDateInTime()));

				setEstimatesInSeconds(jiraIssue, iterationKpiModalValue);

				epicMap.computeIfPresent(jiraIssue.getEpicLinked(), (k, v) -> {
					iterationKpiModalValue.setEpicName(v);
					return v;
				});

				if (MapUtils.isNotEmpty(linkedSubTasks))
					linkedSubTasks.computeIfPresent(jiraIssue.getNumber(), (k, v) -> {
						iterationKpiModalValue.setSubTask(v);
						return v;
					});
				iterationKpiModalValue.setParentStory(jiraIssue.getParentStoryId());
				iterationKpiModalValue
						.setIssueType(StringUtils.isNotEmpty(jiraIssue.getOriginalType()) ? jiraIssue.getOriginalType()
								: jiraIssue.getTypeName());
				setPreclosedSubTask(fieldMapping, closedStatus, jiraIssue, iterationKpiModalValue, issueHistory);
			}
		}
	}

	/*
	 * getting minimum dev done status, similar to the log of DevCompletion kpi
	 */
	public String getDevCompletionDateInTime(JiraIssueCustomHistory issueCustomHistory, List<String> devDoneStatuses) {
		String devCompleteDate = "-";
		List<JiraHistoryChangeLog> filterStatusUpdationLog = issueCustomHistory.getStatusUpdationLog();
		if (CollectionUtils.isNotEmpty(devDoneStatuses)) {
			devCompleteDate = filterStatusUpdationLog.stream()
					.filter(jiraHistoryChangeLog -> devDoneStatuses.contains(jiraHistoryChangeLog.getChangedTo())
							&& jiraHistoryChangeLog.getUpdatedOn() != null)
					.findFirst().map(jiraHistoryChangeLog -> jiraHistoryChangeLog.getUpdatedOn().toString())
					.orElse(devCompleteDate);
		}
		return devCompleteDate;
	}

	private void getMaxCompleteMaxTestDevStartTime(List<JiraHistoryChangeLog> filterStatusUpdationLogs,
			FieldMapping fieldMapping, IterationKpiModalValue iterationKpiModalValue, Set<String> closedStatus,
			JiraIssue jiraIssue, String sprintStartDateInTime) {

		Set<String> testStatus = fieldMapping != null
				&& CollectionUtils.isNotEmpty(fieldMapping.getJiraQADoneStatusKPI154())
						? new HashSet<>(fieldMapping.getJiraQADoneStatusKPI154())
						: new HashSet<>();

		Set<String> startOfDevelopment = fieldMapping != null
				&& CollectionUtils.isNotEmpty(fieldMapping.getJiraStatusStartDevelopmentKPI154())
						? new HashSet<>(fieldMapping.getJiraStatusStartDevelopmentKPI154())
						: new HashSet<>();

		boolean isStartDateFound = false;

		Map<String, LocalDateTime> closedStatusDateMap = new HashMap<>();
		Map<String, LocalDateTime> testClosedStatusMap = new HashMap<>();

		for (JiraHistoryChangeLog statusUpdationLog : filterStatusUpdationLogs) {
			LocalDateTime activityLocalDate = statusUpdationLog.getUpdatedOn();
			if (!isStartDateFound && startOfDevelopment.contains(statusUpdationLog.getChangedTo())) {
				iterationKpiModalValue.setActualStartDateInTime(activityLocalDate.toString());
				isStartDateFound = true;
			}
			if (closedStatus.contains(jiraIssue.getStatus())) {
				getLatestCycleStatusMap(closedStatus, statusUpdationLog, closedStatusDateMap, activityLocalDate);
			}
			getLatestCycleStatusMap(testStatus, statusUpdationLog, testClosedStatusMap, activityLocalDate);
		}

		if (StringUtils.isNotEmpty(iterationKpiModalValue.getActualStartDateInTime())
				&& iterationKpiModalValue.isSpill())
			iterationKpiModalValue.setActualStartDateInTime(sprintStartDateInTime);

		// Getting the max date of closed and test status.
		iterationKpiModalValue.setActualCompletionDateInTime(closedStatusDateMap.values().stream()
				.filter(Objects::nonNull).max(LocalDateTime::compareTo).map(LocalDateTime::toString).orElse(null));
		iterationKpiModalValue.setTestCompletedInTime(testClosedStatusMap.values().stream().filter(Objects::nonNull)
				.max(LocalDateTime::compareTo).map(LocalDateTime::toString).orElse(null));
	}

	/*
	 * get latest cycle
	 */
	private void getLatestCycleStatusMap(Set<String> fieldMappingStatus, JiraHistoryChangeLog statusUpdationLog,
			Map<String, LocalDateTime> statusMap, LocalDateTime activityLocalDate) {
		if (fieldMappingStatus.contains(statusUpdationLog.getChangedTo())) {
			if (statusMap.containsKey(statusUpdationLog.getChangedTo())) {
				statusMap.clear();
			}
			statusMap.put(statusUpdationLog.getChangedTo(), activityLocalDate);
		}
	}

	private void setPCDandDelay(IterationKpiModalValue jiraIssueModalObject,
			Map<String, IterationPotentialDelay> issueWiseDelay, JiraIssue jiraIssue) {
		if (issueWiseDelay.containsKey(jiraIssue.getNumber()) && StringUtils.isNotEmpty(jiraIssue.getDueDate())) {
			IterationPotentialDelay iterationPotentialDelay = issueWiseDelay.get(jiraIssue.getNumber());
			jiraIssueModalObject.setPotentialDelay(iterationPotentialDelay.getPotentialDelay() + "d");
			jiraIssueModalObject.setPredictedCompletionDate(
					DateUtil.dateTimeConverter(iterationPotentialDelay.getPredictedCompletedDate(),
							DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));

		} else {
			jiraIssueModalObject.setPotentialOverallDelay("-");
			jiraIssueModalObject.setPredictedCompletionDate("-");
		}
	}

	/*
	 * creating date wise logs of history
	 */
	private Map<String, List<String>> createDateWiseLogs(List<JiraHistoryChangeLog> historyLog) {
		Map<String, List<String>> dateWiseLogMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(historyLog)) {
			for (JiraHistoryChangeLog log : historyLog) {
				String changedOn = log.getUpdatedOn().toLocalDate().toString();
				dateWiseLogMap.computeIfPresent(changedOn, (date, logs) -> {
					logs.add(log.getChangedTo());
					return logs;
				});

				dateWiseLogMap.computeIfAbsent(changedOn, k -> {
					List<String> statusLogs = new ArrayList<>();
					statusLogs.add(log.getChangedTo());
					return statusLogs;
				});

			}
			dateWiseLogMap = dateWiseLogMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(
					Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		}
		return dateWiseLogMap;
	}

	private Map<String, List<String>> createDateWiseLogs(List<JiraHistoryChangeLog> historyLog,
			String actualStartDateInTime, String actualCompletionTime) {
		if (StringUtils.isNotEmpty(actualCompletionTime)) {
			LocalDateTime endLocalTime = LocalDateTime.parse(actualCompletionTime);
			historyLog = historyLog.stream().filter(log -> DateUtil.equalAndBeforTime(log.getUpdatedOn(), endLocalTime))
					.collect(Collectors.toList());
		}
		if (StringUtils.isNotEmpty(actualStartDateInTime)) {
			LocalDateTime startLocalTime = LocalDateTime.parse(actualStartDateInTime);
			historyLog = historyLog.stream()
					.filter(log -> DateUtil.equalAndAfterTime(log.getUpdatedOn(), startLocalTime))
					.collect(Collectors.toList());
		}
		return createDateWiseLogs(historyLog);
	}

	/*
	 * if within active sprint no logs were generated, but the last change occurred
	 * in the closed sprint continues in the active sprint, then the time should be
	 * calculated from the sprintStartTime till today
	 */
	private String calculateWithLastTime(List<JiraHistoryChangeLog> inSprintHistoryLogs,
			List<JiraHistoryChangeLog> allLogs, LocalDateTime sprintStartDateTime, String actualCompletionDateInTime) {
		String lastTimeInString = null;
		if (StringUtils.isEmpty(actualCompletionDateInTime)) {
			if (CollectionUtils.isNotEmpty(inSprintHistoryLogs)) {
				inSprintHistoryLogs.sort(Comparator.comparing(JiraHistoryChangeLog::getUpdatedOn).reversed());
				lastTimeInString = CommonUtils.convertSecondsToDays((int) Duration
						.between(inSprintHistoryLogs.get(0).getUpdatedOn(), LocalDateTime.now()).getSeconds());
			} else if (CollectionUtils.isNotEmpty(allLogs)) {
				lastTimeInString = CommonUtils.convertSecondsToDays(
						(int) Duration.between(sprintStartDateTime, LocalDateTime.now()).getSeconds());
			}

		} else
			lastTimeInString = Constant.DASH;
		return lastTimeInString;
	}

	private void setEstimatesInSeconds(JiraIssue jiraIssue, IterationKpiModalValue iterationKpiModalValue) {
		if (jiraIssue.getTimeSpentInMinutes() != null)
			iterationKpiModalValue.setLoggedWorkInSeconds((long) (jiraIssue.getTimeSpentInMinutes() * 60));
		if (jiraIssue.getRemainingEstimateMinutes() != null)
			iterationKpiModalValue.setRemainingEstimateInSeconds((long) (jiraIssue.getRemainingEstimateMinutes() * 60));
		if (jiraIssue.getOriginalEstimateMinutes() != null)
			iterationKpiModalValue.setOriginalEstimateInSeconds((long) (jiraIssue.getOriginalEstimateMinutes() * 60));
	}

	/*
	 * if subtasks issues were closed in the previous sprint, and also spilled, then
	 * set preClosed to true to provide a different identification for this
	 */
	private void setPreclosedSubTask(FieldMapping fieldMapping, Set<String> closedStatus, JiraIssue jiraIssue,
			IterationKpiModalValue iterationKpiModalValue, JiraIssueCustomHistory issueHistory) {
		if (fieldMapping != null && CollectionUtils.isNotEmpty(fieldMapping.getJiraSubTaskIdentification())
				&& fieldMapping.getJiraSubTaskIdentification().contains(jiraIssue.getOriginalType())
				&& StringUtils.isEmpty(iterationKpiModalValue.getActualCompletionDateInTime())
				&& closedStatus.contains(iterationKpiModalValue.getIssueStatus())) {
			iterationKpiModalValue.setPreClosed(true);
			iterationKpiModalValue.setTimeWithUser(Constant.DASH);
			iterationKpiModalValue.setTimeWithStatus(Constant.DASH);
			Map<String, LocalDateTime> closedStatusDateMap = new HashMap<>();
			if (CollectionUtils.isNotEmpty(issueHistory.getStatusUpdationLog())) {
				for (JiraHistoryChangeLog statusUpdationLog : issueHistory.getStatusUpdationLog()) {
					LocalDateTime activityLocalDate = statusUpdationLog.getUpdatedOn();
					if (closedStatus.contains(jiraIssue.getStatus())) {
						getLatestCycleStatusMap(closedStatus, statusUpdationLog, closedStatusDateMap,
								activityLocalDate);
					}
				}
				iterationKpiModalValue.setChangeDate(closedStatusDateMap.values().stream().filter(Objects::nonNull)
						.max(LocalDateTime::compareTo).map(a -> a.toLocalDate().toString()).orElse(null));
			}
		}

	}

	/*
	 * filter Status on Second Screen
	 */
	private void setFilters(KpiElement kpiElement, FieldMapping fieldMapping, Set<String> allRoles,
			JiraIssueReleaseStatus statusCategory) {

		List<Filter> firstScreenFilter = new ArrayList<>();
		// Role Filter on First Screen
		// Filters on Second Screen in the order on hold->inprogress->open->done
		List<Filter> secondScreenFilters = new ArrayList<>();
		List<String> values = allRoles.stream().sorted().collect(Collectors.toList());
		Filter filter = new Filter("role", "singleSelect", values);
		firstScreenFilter.add(filter);

		if (ObjectUtils.isNotEmpty(statusCategory)) {
			List<String> inProgressStatus = new ArrayList<>();

			// On Hold Status configured
			if (CollectionUtils.isNotEmpty(fieldMapping.getJiraOnHoldStatusKPI154())) {
				secondScreenFilters.add(new Filter(FILTER_ONHOLD_SCR2, fieldMapping.getJiraOnHoldStatusKPI154(),
						FILTER_BUTTON, false, 3));
			}
			// In progress Filters
			if (CollectionUtils.isNotEmpty(statusCategory.getInProgressList().values())) {
				inProgressStatus = new ArrayList<>(statusCategory.getInProgressList().values());
				secondScreenFilters.add(new Filter(FILTER_INPROGRESS_SCR2, inProgressStatus, FILTER_BUTTON, true, 1));
			}

			// Open Filters
			Filter openFilter;
			List<String> openStatus = new ArrayList<>(statusCategory.getToDoList().values());
			if (CollectionUtils.isNotEmpty(fieldMapping.getStoryFirstStatusKPI154())) {
				openStatus.addAll(fieldMapping.getStoryFirstStatusKPI154());
			} else {
				openStatus.addAll(Arrays.asList(fieldMapping.getStoryFirstStatus()));
			}
			openFilter = new Filter(FILTER_OPEN_SCR2, openStatus, FILTER_BUTTON, false, null);
			secondScreenFilters.add(openFilter);

			// closed filters
			if (CollectionUtils.isNotEmpty(fieldMapping.getJiraIterationCompletionStatusKPI154())) {
				List<String> jiraIterationCompletionStatusKPI154 = fieldMapping
						.getJiraIterationCompletionStatusKPI154();
				inProgressStatus.removeAll(jiraIterationCompletionStatusKPI154);
				secondScreenFilters.add(
						new Filter(FILTER_CLOSED_SCR2, jiraIterationCompletionStatusKPI154, FILTER_BUTTON, true, 2));

			} else if (CollectionUtils.isNotEmpty(statusCategory.getClosedList().values())) {
				List<String> closedList = new ArrayList<>(statusCategory.getClosedList().values());
				inProgressStatus.removeAll(closedList);
				secondScreenFilters.add(new Filter(FILTER_CLOSED_SCR2, closedList, FILTER_BUTTON, true, 2));
			}
		}
		kpiElement.setFilterData(firstScreenFilter);
		kpiElement.setStandUpStatusFilter(secondScreenFilters);
	}

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
	public Map<String, Long> calculateKPIMetrics(Map<String, Object> objectMap) {
		return new HashMap<>();
	}

	@Data
	protected static class UserWiseCardDetail {
		String assigneeId;
		String assigneeName;
		String role;
		LinkedHashMap<String, StandUpViewKpiData> cardDetails;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	protected static class StandUpViewKpiData {
		private String value;
		private String value1;
		private String unit;
		private String unit1;
	}

}
