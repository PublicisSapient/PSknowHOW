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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.CalculatePCDHelper;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFilters;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFiltersOptions;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.IterationKpiHelper;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.IterationPotentialDelay;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PlannedWorkStatusServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	public static final String UNCHECKED = "unchecked";
	public static final String ISSUE_CUSTOM_HISTORY = "issues custom history";
	public static final String ACTUAL_COMPLETE_DATE = "actualCompleteDate";
	public static final String COMPLETED = "Completed";
	public static final String PLANNED_COMPLETION = "Planned Completion";
	public static final String ACTUAL_COMPLETION = "Actual Completion";
	public static final String ACTUAL_START_DATE = "actualStartDate";
	public static final String ACTUAL_COMPLETION_DATA = "actualCompletionData";
	public static final String ISSUE_DELAY = "issueDelay";
	public static final String DEV_COMPLETION_DATE = "devCompletionDate";
	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String SEARCH_BY_PRIORITY = "Filter by priority";
	private static final String ISSUES = "issues";
	private static final String DELAY = "Delay";
	private static final String OVERALL = "Overall";
	private static final String SPRINT_DETAILS = "sprintDetails";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	@Autowired
	private ConfigHelperService configHelperService;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		DataCount trendValue = new DataCount();
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

			Filters filters = Filters.getFilter(k);
			if (Filters.SPRINT == filters) {
				projectWiseLeafNodeValue(v, trendValue, kpiElement, kpiRequest);
			}
		});
		return kpiElement;
	}

	@Override
	public String getQualifierType() {
		return KPICode.PLANNED_WORK_STATUS.name();
	}

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			log.info("Planned Work Status -> Requested sprint : {}", leafNode.getName());
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
						dbSprintDetail, fieldMapping.getJiraIterationIssuetypeKPI128(),
						fieldMapping.getJiraIterationCompletionStatusKPI128(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> totalIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.TOTAL_ISSUES);
				List<String> completedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.COMPLETED_ISSUES);
				if (CollectionUtils.isNotEmpty(totalIssues)) {
					List<JiraIssue> jiraIssueList = IterationKpiHelper.getFilteredJiraIssue(totalIssues,
							totalJiraIssueList);
					List<JiraIssueCustomHistory> issueHistoryList = IterationKpiHelper
							.getFilteredJiraIssueHistory(totalIssues, totalHistoryList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getTotalIssues(), jiraIssueList);
					resultListMap.put(ISSUES, new ArrayList<>(filtersIssuesList));
					resultListMap.put(ISSUE_CUSTOM_HISTORY, new ArrayList<>(issueHistoryList));
					resultListMap.put(SPRINT_DETAILS, sprintDetails);
				}
				resultListMap.put(COMPLETED, new ArrayList<>(completedIssues));
			}
		}
		return resultListMap;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint level.
	 *
	 * @param sprintLeafNodeList
	 * @param trendValue
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(List<Node> sprintLeafNodeList, DataCount trendValue, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);
		Object basicProjectConfigId = Objects.requireNonNull(latestSprint).getProjectFilter().getBasicProjectConfigId();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);

		SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT_DETAILS);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		// Filtering out the issues with due date.
		List<JiraIssue> allIssuesWithDueDate = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(allIssues)) {
			allIssuesWithDueDate = allIssues.stream()
					.filter(jiraIssue -> StringUtils.isNotBlank(jiraIssue.getDueDate())).collect(Collectors.toList());
		}

		List<String> allCompletedIssuesList = (List<String>) resultMap.get(COMPLETED);
		List<JiraIssueCustomHistory> allIssueHistories = (List<JiraIssueCustomHistory>) resultMap
				.get(ISSUE_CUSTOM_HISTORY);
		if (CollectionUtils.isNotEmpty(allIssuesWithDueDate)) {
			log.info("Planned Work Status -> request id : {} total jira Issues : {}", requestTrackerId,
					allIssuesWithDueDate.size());
			// Creating map of modal Objects
			Map<String, IterationKpiModalValue> modalObjectMap = KpiDataHelper
					.createMapOfModalObject(allIssuesWithDueDate);
			Map<String, Map<String, List<JiraIssue>>> typeAndPriorityWiseIssues = allIssuesWithDueDate.stream().collect(
					Collectors.groupingBy(JiraIssue::getTypeName, Collectors.groupingBy(JiraIssue::getPriority)));
			List<JiraIssue> notCompletedIssuesWithDueDate = allIssuesWithDueDate.stream()
					.filter(jiraIssue -> !allCompletedIssuesList.contains(jiraIssue.getNumber()))
					.collect(Collectors.toList());
			List<IterationPotentialDelay> iterationPotentialDelayList = calculatePotentialDelay(sprintDetails,
					notCompletedIssuesWithDueDate, fieldMapping);
			Map<String, IterationPotentialDelay> issueWiseDelay = checkMaxDelayAssigneeWise(
					notCompletedIssuesWithDueDate, iterationPotentialDelayList, sprintDetails);
			Set<String> issueTypes = new HashSet<>();
			Set<String> priorities = new HashSet<>();
			List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
			List<Integer> overAllIssueCountActual = Arrays.asList(0);
			List<Double> overAllStoryPointsActual = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimateActual = Arrays.asList(0.0);
			List<Integer> overAllIssueCountPlanned = Arrays.asList(0);
			List<Double> overAllStoryPointsPlanned = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimatePlanned = Arrays.asList(0.0);
			List<Integer> overallDelay = Arrays.asList(0);
			List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
			// For markerInfo
			Map<String, String> markerInfo = new HashMap<>();
			markerInfo.put(Constant.GREEN, "Issue finished earlier than planned are marked in Green");
			typeAndPriorityWiseIssues
					.forEach((issueType, priorityWiseIssue) -> priorityWiseIssue.forEach((priority, issues) -> {
						issueTypes.add(issueType);
						priorities.add(priority);
						List<IterationKpiModalValue> modalValues = new ArrayList<>();
						int issueCountActual = 0;
						Double storyPointActual = 0.0;
						Double originalEstimateActual = 0.0;
						int issueCountPlanned = 0;
						Double storyPointPlanned = 0.0;
						Double originalEstimatePlanned = 0.0;
						int delay = 0;
						for (JiraIssue jiraIssue : issues) {
							if (SprintDetails.SPRINT_STATE_ACTIVE.equalsIgnoreCase(sprintDetails.getState())) {
								// Checking if dueDate is < today date for active sprint
								if (DateUtil.stringToLocalDate(jiraIssue.getDueDate(), DateUtil.TIME_FORMAT_WITH_SEC)
										.isBefore(LocalDate.now())) {
									issueCountPlanned = issueCountPlanned + 1;
									overAllIssueCountPlanned.set(0, overAllIssueCountPlanned.get(0) + 1);

									storyPointPlanned = KpiDataHelper.getStoryPoint(overAllStoryPointsPlanned,
											storyPointPlanned, jiraIssue);
									originalEstimatePlanned = KpiDataHelper.getOriginalEstimate(
											overAllOriginalEstimatePlanned, originalEstimatePlanned, jiraIssue);
									Map<String, Object> jiraIssueData = jiraIssueCalculation(fieldMapping,
											sprintDetails, allIssueHistories, allCompletedIssuesList, jiraIssue);
									Map<String, Object> actualCompletionData = (Map<String, Object>) jiraIssueData
											.get(ACTUAL_COMPLETION_DATA);
									if (!jiraIssueData.get(ISSUE_DELAY).equals(Constant.DASH)) {
										int jiraIssueDelay = (int) jiraIssueData.get(ISSUE_DELAY);
										delay += KpiDataHelper.getDelayInMinutes(jiraIssueDelay);
										overallDelay.set(0,
												overallDelay.get(0) + KpiDataHelper.getDelayInMinutes(jiraIssueDelay));
									} else {
										delay = KpiDataHelper.checkDelay(jiraIssue, issueWiseDelay, delay,
												overallDelay);
									}
									KPIExcelUtility.populateIterationKPI(overAllmodalValues, modalValues, jiraIssue,
											fieldMapping, modalObjectMap);
									setKpiSpecificData(modalObjectMap, issueWiseDelay, jiraIssue, jiraIssueData,
											actualCompletionData);
								}
							} else {
								// Checking if dueDate is <= sprint End Date for closed sprint
								if (DateUtil.stringToLocalDate(jiraIssue.getDueDate(), DateUtil.TIME_FORMAT_WITH_SEC)
										.isBefore(DateUtil.stringToLocalDate(sprintDetails.getEndDate(),
												DateUtil.TIME_FORMAT_WITH_SEC).plusDays(1))) {
									issueCountPlanned = issueCountPlanned + 1;
									overAllIssueCountPlanned.set(0, overAllIssueCountPlanned.get(0) + 1);

									storyPointPlanned = KpiDataHelper.getStoryPoint(overAllStoryPointsPlanned,
											storyPointPlanned, jiraIssue);
									originalEstimatePlanned = KpiDataHelper.getOriginalEstimate(
											overAllOriginalEstimatePlanned, originalEstimatePlanned, jiraIssue);
									Map<String, Object> jiraIssueData = jiraIssueCalculation(fieldMapping,
											sprintDetails, allIssueHistories, allCompletedIssuesList, jiraIssue);
									Map<String, Object> actualCompletionData = (Map<String, Object>) jiraIssueData
											.get(ACTUAL_COMPLETION_DATA);
									if (!jiraIssueData.get(ISSUE_DELAY).equals(Constant.DASH)) {
										int jiraIssueDelay = (int) jiraIssueData.get(ISSUE_DELAY);
										delay += KpiDataHelper.getDelayInMinutes(jiraIssueDelay);
										overallDelay.set(0,
												overallDelay.get(0) + KpiDataHelper.getDelayInMinutes(jiraIssueDelay));
									} else {
										delay = KpiDataHelper.checkDelay(jiraIssue, issueWiseDelay, delay,
												overallDelay);
									}
									KPIExcelUtility.populateIterationKPI(overAllmodalValues, modalValues, jiraIssue,
											fieldMapping, modalObjectMap);
									setKpiSpecificData(modalObjectMap, issueWiseDelay, jiraIssue, jiraIssueData,
											actualCompletionData);
								}
							}
							// Calculating actual work status for only completed issues
							if (allCompletedIssuesList.contains(jiraIssue.getNumber())) {
								issueCountActual = issueCountActual + 1;
								overAllIssueCountActual.set(0, overAllIssueCountActual.get(0) + 1);

								storyPointActual = KpiDataHelper.getStoryPoint(overAllStoryPointsActual,
										storyPointActual, jiraIssue);
								originalEstimateActual = KpiDataHelper.getOriginalEstimate(
										overAllOriginalEstimateActual, originalEstimateActual, jiraIssue);

								if (DateUtil.stringToLocalDate(jiraIssue.getDueDate(), DateUtil.TIME_FORMAT_WITH_SEC)
										.isAfter(LocalDate.now().minusDays(1))) {
									Map<String, Object> jiraIssueData = jiraIssueCalculation(fieldMapping,
											sprintDetails, allIssueHistories, allCompletedIssuesList, jiraIssue);
									Map<String, Object> actualCompletionData = (Map<String, Object>) jiraIssueData
											.get(ACTUAL_COMPLETION_DATA);
									if (!jiraIssueData.get(ISSUE_DELAY).equals(Constant.DASH)) {
										int jiraIssueDelay = (int) jiraIssueData.get(ISSUE_DELAY);
										delay += KpiDataHelper.getDelayInMinutes(jiraIssueDelay);
										overallDelay.set(0,
												overallDelay.get(0) + KpiDataHelper.getDelayInMinutes(jiraIssueDelay));
									}
									KPIExcelUtility.populateIterationKPI(overAllmodalValues, modalValues, jiraIssue,
											fieldMapping, modalObjectMap);
									setKpiSpecificData(modalObjectMap, issueWiseDelay, jiraIssue, jiraIssueData,
											actualCompletionData);
								}
							}
						}
						List<IterationKpiData> data = new ArrayList<>();
						IterationKpiData issueCountsPlanned;
						IterationKpiData issueCountsActual;
						IterationKpiData delayed;
						issueCountsPlanned = createIterationKpiData(PLANNED_COMPLETION, fieldMapping, issueCountPlanned,
								storyPointPlanned, originalEstimatePlanned, modalValues);
						issueCountsActual = createIterationKpiData(ACTUAL_COMPLETION, fieldMapping, issueCountActual,
								storyPointActual, originalEstimateActual, null);
						delayed = new IterationKpiData(DELAY, (double) (delay), null, null, CommonConstant.DAY, null);
						data.add(issueCountsPlanned);
						data.add(issueCountsActual);
						data.add(delayed);
						IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, priority, data,
								Arrays.asList("marker"), markerInfo);
						iterationKpiValues.add(iterationKpiValue);
					}));
			List<IterationKpiData> data = new ArrayList<>();
			IterationKpiData overAllIssueCountsPlanned;
			IterationKpiData overAllIssueCountsActual;
			IterationKpiData overAllDelay;
			overAllIssueCountsPlanned = createIterationKpiData(PLANNED_COMPLETION, fieldMapping,
					overAllIssueCountPlanned.get(0), overAllStoryPointsPlanned.get(0),
					overAllOriginalEstimatePlanned.get(0), overAllmodalValues);
			overAllIssueCountsActual = createIterationKpiData(ACTUAL_COMPLETION, fieldMapping,
					overAllIssueCountActual.get(0), overAllStoryPointsActual.get(0),
					overAllOriginalEstimateActual.get(0), null);
			overAllDelay = new IterationKpiData(DELAY, (double) (overallDelay.get(0)), null, null, CommonConstant.DAY,
					null);
			data.add(overAllIssueCountsPlanned);
			data.add(overAllIssueCountsActual);
			data.add(overAllDelay);
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data,
					Arrays.asList("marker"), markerInfo);
			iterationKpiValues.add(overAllIterationKpiValue);

			// Create kpi level filters
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypes);
			IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, priorities);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
			trendValue.setValue(iterationKpiValues);
			kpiElement.setFilters(iterationKpiFilters);
			kpiElement.setSprint(latestSprint.getName());
			kpiElement.setExcelColumnInfo(KPIExcelColumn.PLANNED_WORK_STATUS.getKpiExcelColumnInfo());
			kpiElement.setTrendValueList(trendValue);
		}
	}

	/**
	 * Method to calculate story start and completed date
	 *
	 * @param issueCustomHistory
	 * @param allCompletedIssuesList
	 * @param sprintDetail
	 * @param fieldMapping
	 * @return
	 */
	private Map<String, Object> calStartAndEndDate(JiraIssueCustomHistory issueCustomHistory,
			List<String> allCompletedIssuesList, SprintDetails sprintDetail, FieldMapping fieldMapping) {
		List<String> inProgressStatuses = new ArrayList<>();
		List<JiraHistoryChangeLog> filterStatusUpdationLogs = new ArrayList<>();

		LocalDate sprintStartDate = LocalDate.parse(sprintDetail.getStartDate().split("\\.")[0], DATE_TIME_FORMATTER);
		LocalDate sprintEndDate = LocalDate.parse(sprintDetail.getEndDate().split("\\.")[0], DATE_TIME_FORMATTER);
		Map<String, Object> resultList = new HashMap<>();

		// filtering statusUpdationLogs lies in between sprintStart and sprintEnd
		filterStatusUpdationLogs = getFilterStatusUpdationLogs(issueCustomHistory, filterStatusUpdationLogs,
				sprintStartDate, sprintEndDate);

		// Creating the set of completed status
		Set<String> closedStatus = sprintDetail.getCompletedIssues().stream().map(SprintIssue::getStatus)
				.collect(Collectors.toSet());

		// sorting the story history on basis of UpdatedOn
		filterStatusUpdationLogs.sort(Comparator.comparing(JiraHistoryChangeLog::getUpdatedOn));

		// Getting inProgress Status
		if (null != fieldMapping && CollectionUtils.isNotEmpty(fieldMapping.getJiraStatusForInProgressKPI128())) {
			inProgressStatuses = fieldMapping.getJiraStatusForInProgressKPI128();
		}
		LocalDate startDate = null;
		LocalDate endDate;
		boolean isStartDateFound = false;

		Map<String, LocalDate> closedStatusDateMap = new HashMap<>();
		for (JiraHistoryChangeLog statusUpdationLog : filterStatusUpdationLogs) {
			LocalDate activityLocalDate = LocalDate.parse(
					statusUpdationLog.getUpdatedOn().toString().split("T")[0].concat("T00:00:00"), DATE_TIME_FORMATTER);

			if (inProgressStatuses.contains(statusUpdationLog.getChangedTo()) && !isStartDateFound) {
				startDate = activityLocalDate;
				isStartDateFound = true;
			}

			if (CollectionUtils.isNotEmpty(allCompletedIssuesList)
					&& allCompletedIssuesList.contains(issueCustomHistory.getStoryID())
					&& closedStatus.contains(statusUpdationLog.getChangedTo())) {
				if (closedStatusDateMap.containsKey(statusUpdationLog.getChangedTo())) {
					closedStatusDateMap.clear();
				}
				closedStatusDateMap.put(statusUpdationLog.getChangedTo(), activityLocalDate);
			}
		}
		// Getting the min date of closed status.
		endDate = closedStatusDateMap.values().stream().filter(Objects::nonNull).min(LocalDate::compareTo).orElse(null);
		resultList.put(ACTUAL_START_DATE, startDate);
		resultList.put(ACTUAL_COMPLETE_DATE, endDate);
		return resultList;
	}

	// Filtering the history which happened inside the sprint on basis of activity
	// date
	private List<JiraHistoryChangeLog> getFilterStatusUpdationLogs(JiraIssueCustomHistory issueCustomHistory,
			List<JiraHistoryChangeLog> filterStatusUpdationLogs, LocalDate sprintStartDate, LocalDate sprintEndDate) {
		if (CollectionUtils.isNotEmpty(issueCustomHistory.getStatusUpdationLog())) {
			filterStatusUpdationLogs = issueCustomHistory.getStatusUpdationLog().stream()
					.filter(jiraIssueSprint -> DateUtil.isWithinDateRange(
							LocalDate.parse(jiraIssueSprint.getUpdatedOn().toString().split("T")[0].concat("T00:00:00"),
									DATE_TIME_FORMATTER),
							sprintStartDate, sprintEndDate))
					.collect(Collectors.toList());
		}
		return filterStatusUpdationLogs;
	}

	/**
	 * To calculate delay, devCompletion date
	 * 
	 * @param fieldMapping
	 * @param sprintDetails
	 * @param allIssueHistories
	 * @param allCompletedIssuesList
	 * @param jiraIssue
	 * @return
	 */
	private Map<String, Object> jiraIssueCalculation(FieldMapping fieldMapping, SprintDetails sprintDetails,
			List<JiraIssueCustomHistory> allIssueHistories, List<String> allCompletedIssuesList, JiraIssue jiraIssue) {
		int jiraIssueDelay = 0;
		Map<String, Object> resultList = new HashMap<>();

		JiraIssueCustomHistory issueCustomHistory = allIssueHistories.stream()
				.filter(jiraIssueCustomHistory -> jiraIssueCustomHistory.getStoryID().equals(jiraIssue.getNumber()))
				.findFirst().orElse(new JiraIssueCustomHistory());

		String devCompletionDate = getDevCompletionDate(issueCustomHistory, fieldMapping.getJiraDevDoneStatusKPI128());
		// calling function for cal actual completion days
		Map<String, Object> actualCompletionData = calStartAndEndDate(issueCustomHistory, allCompletedIssuesList,
				sprintDetails, fieldMapping);

		if (actualCompletionData.get(ACTUAL_COMPLETE_DATE) != null && jiraIssue.getDueDate() != null) {
			LocalDate actualCompletedDate = (LocalDate) actualCompletionData.get(ACTUAL_COMPLETE_DATE);
			jiraIssueDelay = getDelay(DateUtil.stringToLocalDate(jiraIssue.getDueDate(), DateUtil.TIME_FORMAT_WITH_SEC),
					actualCompletedDate);
			resultList.put(ISSUE_DELAY, jiraIssueDelay);
		} else {
			resultList.put(ISSUE_DELAY, Constant.DASH);
		}
		resultList.put(DEV_COMPLETION_DATE, devCompletionDate);
		resultList.put(ACTUAL_COMPLETION_DATA, actualCompletionData);
		return resultList;
	}

	private int getDelay(LocalDate dueDate, LocalDate completedDate) {
		int potentialDelays = CommonUtils.getWorkingDays(dueDate, completedDate);
		return (dueDate.isAfter(completedDate)) ? potentialDelays * (-1) : potentialDelays;
	}

	private LinkedHashMap<String, IterationPotentialDelay> checkMaxDelayAssigneeWise(List<JiraIssue> jiraIssueList,
			List<IterationPotentialDelay> issueWiseDelay, SprintDetails sprintDetails) {
		if (SprintDetails.SPRINT_STATE_ACTIVE.equalsIgnoreCase(sprintDetails.getState())) {
			jiraIssueList = jiraIssueList.stream().filter(jiraIssue -> jiraIssue.getDueDate() != null && DateUtil
					.stringToLocalDate(jiraIssue.getDueDate(), DateUtil.TIME_FORMAT_WITH_SEC).isBefore(LocalDate.now()))
					.collect(Collectors.toList());
		} else {
			jiraIssueList = jiraIssueList
					.stream().filter(
							jiraIssue -> jiraIssue.getDueDate() != null
									&& DateUtil.stringToLocalDate(jiraIssue.getDueDate(), DateUtil.TIME_FORMAT_WITH_SEC)
											.isBefore(DateUtil.stringToLocalDate(sprintDetails.getEndDate(),
													DateUtil.TIME_FORMAT_WITH_SEC).plusDays(1)))
					.collect(Collectors.toList());
		}
		Map<String, List<JiraIssue>> assigneeWiseJiraIssue = assigneeWiseJiraIssue(jiraIssueList);
		List<IterationPotentialDelay> maxDelayList = new ArrayList<>();
		if (MapUtils.isNotEmpty(assigneeWiseJiraIssue)) {
			for (List<JiraIssue> jiraIssues : assigneeWiseJiraIssue.values()) {
				List<IterationPotentialDelay> delayList = new ArrayList<>();
				for (JiraIssue jiraIssue : jiraIssues) {
					issueWiseDelay.stream().filter(iterationPotentialDelay -> iterationPotentialDelay.getIssueId()
							.equalsIgnoreCase(jiraIssue.getNumber())).forEach(delayList::add);
				}

				if (CollectionUtils.isNotEmpty(delayList)) {
					// fetch the maximum delayed story of each assignee
					// and set the marker in the original IterationPotentialDelay list
					maxDelayList.add(
							delayList.stream().max(Comparator.comparing(IterationPotentialDelay::getPotentialDelay))
									.orElse(new IterationPotentialDelay()));
				}
			}
			if (CollectionUtils.isNotEmpty(maxDelayList)) {
				maxDelayList.stream()
						.forEach(iterationPotentialDelay -> issueWiseDelay.stream()
								.filter(issue -> issue.equals(iterationPotentialDelay))
								.forEach(issue -> issue.setMaxMarker(true)));
			}
		}
		return issueWiseDelay.stream().collect(Collectors.toMap(IterationPotentialDelay::getIssueId,
				Function.identity(), (e1, e2) -> e2, LinkedHashMap::new));
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
		Map<String, List<JiraIssue>> assigneeWiseJiraIssue = assigneeWiseJiraIssue(allIssues);

		if (MapUtils.isNotEmpty(assigneeWiseJiraIssue)) {
			assigneeWiseJiraIssue.forEach((assignee, jiraIssues) -> {
				List<JiraIssue> inProgressIssues = new ArrayList<>();
				List<JiraIssue> openIssues = new ArrayList<>();
				CalculatePCDHelper.arrangeJiraIssueList(fieldMapping.getJiraStatusForInProgressKPI128(), jiraIssues,
						inProgressIssues, openIssues);
				iterationPotentialDelayList
						.addAll(sprintWiseDelayCalculation(inProgressIssues, openIssues, sprintDetails));
			});
		}

		if (CollectionUtils.isNotEmpty(fieldMapping.getJiraStatusForInProgressKPI128())) {
			List<JiraIssue> inProgressIssues = allIssues.stream()
					.filter(jiraIssue -> (jiraIssue.getAssigneeId() == null)
							&& StringUtils.isNotEmpty(jiraIssue.getDueDate())
							&& (fieldMapping.getJiraStatusForInProgressKPI128().contains(jiraIssue.getStatus())))
					.collect(Collectors.toList());

			List<JiraIssue> openIssues = new ArrayList<>();
			iterationPotentialDelayList.addAll(sprintWiseDelayCalculation(inProgressIssues, openIssues, sprintDetails));
		}
		return iterationPotentialDelayList;
	}

	private Map<String, List<JiraIssue>> assigneeWiseJiraIssue(List<JiraIssue> allIssues) {
		return allIssues.stream().filter(jiraIssue -> jiraIssue.getAssigneeId() != null)
				.collect(Collectors.groupingBy(JiraIssue::getAssigneeName));
	}

	private void setKpiSpecificData(Map<String, IterationKpiModalValue> modalObjectMap,
			Map<String, IterationPotentialDelay> issueWiseDelay, JiraIssue jiraIssue, Map<String, Object> jiraIssueData,
			Map<String, Object> actualCompletionData) {
		IterationKpiModalValue jiraIssueModalObject = modalObjectMap.get(jiraIssue.getNumber());
		String markerValue = Constant.BLANK;
		jiraIssueModalObject.setDevCompletionDate(DateUtil.dateTimeConverter(
				(String) jiraIssueData.get(DEV_COMPLETION_DATE), DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));
		if (actualCompletionData.get(ACTUAL_COMPLETE_DATE) != null)
			jiraIssueModalObject.setActualCompletionDate(
					DateUtil.dateTimeConverter(actualCompletionData.get(ACTUAL_COMPLETE_DATE).toString(),
							DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));
		else
			jiraIssueModalObject.setActualCompletionDate(" - ");
		if (actualCompletionData.get(ACTUAL_START_DATE) != null) {
			jiraIssueModalObject.setActualStartDate(
					DateUtil.dateTimeConverter(actualCompletionData.get(ACTUAL_START_DATE).toString(),
							DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));
		} else
			jiraIssueModalObject.setActualStartDate(" - ");
		if (!jiraIssueData.get(ISSUE_DELAY).equals(Constant.DASH)) {
			jiraIssueModalObject.setDelayInDays(String.valueOf(jiraIssueData.get(ISSUE_DELAY)) + "d");
		} else {
			jiraIssueModalObject.setDelayInDays(" - ");
		}
		if (DateUtil.stringToLocalDate(jiraIssue.getDueDate(), DateUtil.TIME_FORMAT_WITH_SEC)
				.isAfter(LocalDate.now().minusDays(1))) {
			markerValue = Constant.GREEN;
		}
		if (issueWiseDelay.containsKey(jiraIssue.getNumber()) && StringUtils.isNotEmpty(jiraIssue.getDueDate())) {
			IterationPotentialDelay iterationPotentialDelay = issueWiseDelay.get(jiraIssue.getNumber());
			jiraIssueModalObject.setPotentialDelay(String.valueOf(iterationPotentialDelay.getPotentialDelay()) + "d");
			jiraIssueModalObject.setPredictedCompletionDate(
					DateUtil.dateTimeConverter(iterationPotentialDelay.getPredictedCompletedDate(),
							DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));
		} else {
			jiraIssueModalObject.setPotentialDelay("-");
			jiraIssueModalObject.setPredictedCompletionDate("-");
		}
		jiraIssueModalObject.setMarker(markerValue);
	}

}
