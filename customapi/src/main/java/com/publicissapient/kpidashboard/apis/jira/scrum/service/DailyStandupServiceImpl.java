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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AssigneeCapacity;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.jira.IterationPotentialDelay;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * First Screen of Daily StandUpView
 */

@Component
@Slf4j
public class DailyStandupServiceImpl extends JiraKPIService<Map<String, Long>, List<Object>, Map<String, Object>> {
	public static final String UNCHECKED = "unchecked";
	public static final String UNASSIGNED = "Unassigned";
	private static final String SPRINT = "sprint";
	private static final String ISSUES = "issues";
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
	private static final String FILTER_INPROGRESS_SCR2 = "In Progress";

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CapacityKpiDataRepository capacityKpiDataRepository;

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

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
			log.info("Daily Standup View -> Requested sprint : {}", leafNode.getName());
			SprintDetails sprintDetails;
			SprintDetails dbSprintDetail = getSprintDetailsFromBaseClass();
			if (null != dbSprintDetail && dbSprintDetail.getState().equals(SprintDetails.SPRINT_STATE_ACTIVE)) {
				ObjectId basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId();
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

				// to modify sprintdetails on the basis of configuration for the project
				sprintDetails = KpiDataHelper.processSprintBasedOnFieldMappings(
						Collections.singletonList(dbSprintDetail), fieldMapping.getJiraIterationIssuetypeKPI119(),
						fieldMapping.getJiraIterationCompletionStatusKPI154(), null).get(0);

				List<String> notCompletedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(
						sprintDetails, CommonConstant.NOT_COMPLETED_ISSUES);
				List<String> allIssues = new ArrayList<>();
				allIssues.addAll(notCompletedIssues);
				allIssues.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.COMPLETED_ISSUES));
				allIssues.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.ADDED_ISSUES));
				allIssues.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.PUNTED_ISSUES));

				if (CollectionUtils.isNotEmpty(allIssues)) {
					Set<JiraIssue> totalIssueList = KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(
							sprintDetails, null, getJiraIssuesFromBaseClass(allIssues));
					Set<JiraIssue> notCompletedJiraIssues = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getNotCompletedIssues(),
									getJiraIssuesFromBaseClass(notCompletedIssues));

					if (CollectionUtils.isNotEmpty(fieldMapping.getJiraSubTaskDefectType())) {
						List<String> taskType = fieldMapping.getJiraSubTaskDefectType();
						// combined both sub-tasks and totalIssuelist
						totalIssueList.addAll(
								jiraIssueRepository.findByBasicProjectConfigIdAndParentStoryIdInAndOriginalTypeIn(
										basicProjectConfigId.toString(), new HashSet<>(allIssues), taskType));
					}

					Set<JiraIssue> epics = new HashSet<>(jiraIssueRepository.findByNumberInAndBasicProjectConfigId(
							totalIssueList.stream().map(JiraIssue::getEpicLinked).collect(Collectors.toList()),
							basicProjectConfigId.toString()));

					List<JiraIssueCustomHistory> issueHistoryList = getJiraIssuesCustomHistoryFromBaseClass(
							totalIssueList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));

					resultListMap.put(SPRINT, sprintDetails);
					resultListMap.put(ISSUES, totalIssueList);
					resultListMap.put(HISTORY_ISSUES, issueHistoryList);
					resultListMap.put(EPICS, epics);
					resultListMap.put(NOT_COMPLETED_JIRAISSUE, new ArrayList<>(notCompletedJiraIssues));
					resultListMap.put(ASSIGNEE_DETAILS, capacityKpiDataRepository.findBySprintIDAndBasicProjectConfigId(
							sprintDetails.getSprintID(), sprintDetails.getBasicProjectConfigId()));
				}
			}
		}
		return resultListMap;
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
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);
		Object basicProjectConfigId = latestSprint.getProjectFilter().getBasicProjectConfigId();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
		List<UserWiseCardDetail> userWiseCardDetails = new ArrayList<>();

		// fetch from db
		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT);
		if (ObjectUtils.isNotEmpty(sprintDetails)
				&& sprintDetails.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_ACTIVE)) {
			List<JiraIssue> notCompletedJiraIssue = (List<JiraIssue>) resultMap.get(NOT_COMPLETED_JIRAISSUE);
			CapacityKpiData capacityKpiData = (CapacityKpiData) resultMap.get(ASSIGNEE_DETAILS);
			List<JiraIssue> totalIssueList = new ArrayList<>((Set<JiraIssue>) resultMap.get(ISSUES));

			Map<String, Set<String>> parentChildRelation = findLinkedSubTasks(totalIssueList, fieldMapping);

			List<Filter> filtersList = new ArrayList<>();
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

			// calculate remianing estimate
			calculateAssigneeWiseRemainingEstimate(assigneeWiseNotCompleted, remianingWork, assigneeWiseRemaingEstimate,
					estimationCriteria);

			if (CollectionUtils.isNotEmpty(fieldMapping.getJiraStatusForInProgressKPI154())) {
				Map<String, List<String>> inProgressFilters = new HashMap<>();
				inProgressFilters.put(FILTER_INPROGRESS_SCR2, fieldMapping.getJiraStatusForInProgressKPI154());
				kpiElement.setStandUpStatusFilter(inProgressFilters);
			}
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
						StandUpViewKpiData.builder().value(Constant.DASH).unit(CommonConstant.HOURS).build()));
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
			createRoleFilter(filtersList, allRoles);
			kpiElement.setIssueData(new HashSet<>(mapOfModalObject.values()));
			userWiseCardDetails.sort(Comparator.comparing(UserWiseCardDetail::getAssigneeName));
			kpiElement.setModalHeads(KPIExcelColumn.DAILY_STANDUP_VIEW.getColumns());
			kpiElement.setFilterData(filtersList);
		}
		kpiElement.setTrendValueList(userWiseCardDetails);

	}

	private void populateModal(List<JiraIssue> jiraIssueList, Map<String, Object> resultMap,
			SprintDetails sprintDetails, Map<String, Set<String>> linkedSubTasks,
			Map<String, IterationPotentialDelay> issueWiseDelay, Map<String, IterationKpiModalValue> mapOfModalObject,
			FieldMapping fieldMapping) {
		List<JiraIssue> epicList = new ArrayList<>((Set<JiraIssue>) resultMap.get(EPICS));

		Map<String, String> epicMap = epicList.stream()
				.collect(Collectors.toMap(JiraIssue::getNumber, JiraIssue::getName));
		List<JiraIssueCustomHistory> totalHistoryList = (List<JiraIssueCustomHistory>) resultMap.get(HISTORY_ISSUES);

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

				getMaxCompleteMaxTestDevStartTime(inSprintStatusLogs, sprintDetails, fieldMapping,
						iterationKpiModalValue);

				setPCDandDelay(iterationKpiModalValue, issueWiseDelay, jiraIssue);
				iterationKpiModalValue.setStatusLogGroup(createDateWiseLogs(inSprintStatusLogs));
				iterationKpiModalValue.setWorkLogGroup(createDateWiseLogs(inSprintWorkLogs));
				iterationKpiModalValue.setAssigneeLogGroup(createDateWiseLogs(inSprintAssigneeLogs));

				iterationKpiModalValue.setTimeWithUser(calculateWithLastTime(inSprintAssigneeLogs,
						issueHistory.getAssigneeUpdationLog(), sprintStartDateTime));
				iterationKpiModalValue.setTimeWithStatus(calculateWithLastTime(inSprintStatusLogs,
						issueHistory.getStatusUpdationLog(), sprintStartDateTime));
				setEstimatesInSeconds(jiraIssue, iterationKpiModalValue);

				epicMap.computeIfPresent(jiraIssue.getEpicLinked(), (k, v) -> {
					iterationKpiModalValue.setEpicName(v);
					return v;
				});
				if (CollectionUtils.isNotEmpty(jiraIssue.getSprintIdList()) && jiraIssue.getSprintIdList().size() > 1)
					iterationKpiModalValue.setSpill(true);
				if (MapUtils.isNotEmpty(linkedSubTasks))
					linkedSubTasks.computeIfPresent(jiraIssue.getNumber(), (k, v) -> {
						iterationKpiModalValue.setSubTask(v);
						return v;
					});
				iterationKpiModalValue.setParentStory(jiraIssue.getParentStoryId());
			}
		}
	}

	/*
	 * create map of parent linked with children issueType
	 */
	private Map<String, Set<String>> findLinkedSubTasks(List<JiraIssue> jiraIssueList, FieldMapping fieldMapping) {
		Map<String, Set<String>> parentChild = new HashMap<>();
		if (CollectionUtils.isNotEmpty(fieldMapping.getJiraSubTaskDefectType())) {
			Set<String> tasks = new HashSet<>(fieldMapping.getJiraSubTaskDefectType());
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

	private void setEstimatesInSeconds(JiraIssue jiraIssue, IterationKpiModalValue iterationKpiModalValue) {
		if (jiraIssue.getTimeSpentInMinutes() != null)
			iterationKpiModalValue.setLoggedWorkInSeconds((long) (jiraIssue.getTimeSpentInMinutes() * 60));
		if (jiraIssue.getRemainingEstimateMinutes() != null)
			iterationKpiModalValue.setRemainingEstimateInSeconds((long) (jiraIssue.getRemainingEstimateMinutes() * 60));
		if (jiraIssue.getOriginalEstimateMinutes() != null)
			iterationKpiModalValue.setOriginalEstimateInSeconds((long) (jiraIssue.getOriginalEstimateMinutes() * 60));
	}

	/*
	 * if within active sprint no logs were generated, but the last change occurred
	 * in the closed sprint continues in the active sprint, then the time should be
	 * calculated from the sprintStartTime till today
	 */
	private long calculateWithLastTime(List<JiraHistoryChangeLog> inSprintHistoryLogs,
			List<JiraHistoryChangeLog> allLogs, LocalDateTime sprintStartDateTime) {
		long lastTime = 0L;
		if (CollectionUtils.isNotEmpty(inSprintHistoryLogs)) {
			Collections.sort(inSprintHistoryLogs, Comparator.comparing(JiraHistoryChangeLog::getUpdatedOn).reversed());
			lastTime = ChronoUnit.SECONDS.between(inSprintHistoryLogs.get(0).getUpdatedOn(), LocalDateTime.now());
		} else if (CollectionUtils.isNotEmpty(allLogs)) {
			lastTime = ChronoUnit.SECONDS.between(sprintStartDateTime, LocalDateTime.now());
		}
		return lastTime;
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
				dateWiseLogMap.computeIfAbsent(changedOn, k -> new ArrayList<>()).add(log.getChangedTo());
			}
			dateWiseLogMap = dateWiseLogMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(
					Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		}
		return dateWiseLogMap;
	}

	private void calculateRemainingCapacity(SprintDetails sprintDetails, CapacityKpiData capacityKpiData,
			Map<String, String> userWiseRole, Map<String, StandUpViewKpiData> userWiseRemainingCapacity) {
		LocalDate sprintStartDate = LocalDate.parse(sprintDetails.getStartDate().split("T")[0], DATE_FORMATTER);
		LocalDate sprintEndDate = LocalDate.parse(sprintDetails.getEndDate().split("T")[0], DATE_FORMATTER);
		int daysBetween = checkWorkingDays(sprintStartDate, sprintEndDate);
		int daysLeft = checkWorkingDays(LocalDate.now(), sprintEndDate);

		if (capacityKpiData != null && CollectionUtils.isNotEmpty(capacityKpiData.getAssigneeCapacity())) {
			capacityKpiData.getAssigneeCapacity().forEach(assignee -> {
				calculateRemainigCapacity(daysBetween, daysLeft, assignee, userWiseRemainingCapacity);
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

	private void createRoleFilter(List<Filter> filtersList, Set<String> allRoles) {
		List<String> values = allRoles.stream().sorted().collect(Collectors.toList());
		Filter filter = new Filter();
		filter.setFilterKey("role");
		filter.setFilterType("singleSelect");
		filter.setOptions(values);
		filtersList.add(filter);
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

	/**
	 * calculate remainung estimate on the basis of fieldmapping
	 */
	private void calculateAssigneeWiseRemainingEstimate(Map<String, List<JiraIssue>> assigneeWiseNotCompleted,
			Map<String, StandUpViewKpiData> remainingWork,
			Map<String, StandUpViewKpiData> assigneeWiseRemainingEstimate, String estimationCriteria) {
		String estimationUnit = estimationCriteria.equalsIgnoreCase(CommonConstant.STORY_POINT) ? CommonConstant.SP
				: CommonConstant.HOURS;

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

	/**
	 * calculate Remaining Capacity (available capacity/days in sprint)* remaining
	 * days
	 */
	private void calculateRemainigCapacity(int daysBetween, int daysLeft, AssigneeCapacity assignee,
			Map<String, StandUpViewKpiData> userWiseRemainingCapacity) {
		if (assignee.getAvailableCapacity() != null) {
			double remainingCapacity = roundingOff((assignee.getAvailableCapacity() / daysBetween) * daysLeft);
			userWiseRemainingCapacity.putIfAbsent(assignee.getUserId(),
					new StandUpViewKpiData(String.valueOf(remainingCapacity), null, CommonConstant.HOURS, null));
		}
	}

	private void setPCDandDelay(IterationKpiModalValue jiraIssueModalObject,
			Map<String, IterationPotentialDelay> issueWiseDelay, JiraIssue jiraIssue) {
		if (issueWiseDelay.containsKey(jiraIssue.getNumber()) && StringUtils.isNotEmpty(jiraIssue.getDueDate())) {
			IterationPotentialDelay iterationPotentialDelay = issueWiseDelay.get(jiraIssue.getNumber());
			jiraIssueModalObject.setPotentialDelay(String.valueOf(iterationPotentialDelay.getPotentialDelay()) + "d");
			jiraIssueModalObject.setPredictedCompletionDate(
					DateUtil.dateTimeConverter(iterationPotentialDelay.getPredictedCompletedDate(),
							DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));

		} else {
			jiraIssueModalObject.setPotentialOverallDelay("-");
			jiraIssueModalObject.setPredictedCompletionDate("-");
		}
	}

	private void getMaxCompleteMaxTestDevStartTime(List<JiraHistoryChangeLog> filterStatusUpdationLogs,
			SprintDetails sprintDetail, FieldMapping fieldMapping, IterationKpiModalValue iterationKpiModalValue) {

		Set<String> closedStatus = fieldMapping != null
				&& CollectionUtils.isNotEmpty(fieldMapping.getJiraIterationCompletionStatusKPI154())
						? new HashSet<>(fieldMapping.getJiraIterationCompletionStatusKPI154())
						: sprintDetail.getCompletedIssues().stream().map(SprintIssue::getStatus)
								.collect(Collectors.toSet());

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
			getLatestCycleStatusMap(closedStatus, statusUpdationLog, closedStatusDateMap, activityLocalDate);
			getLatestCycleStatusMap(testStatus, statusUpdationLog, testClosedStatusMap, activityLocalDate);
		}

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

	@Data
	private class UserWiseCardDetail {
		String assigneeId;
		String assigneeName;
		String role;
		LinkedHashMap<String, StandUpViewKpiData> cardDetails;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	private static class StandUpViewKpiData {
		private String value;
		private String value1;
		private String unit;
		private String unit1;
	}

}
