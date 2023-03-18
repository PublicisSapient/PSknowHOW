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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.*;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.*;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

@Component
public class OverallCompletionStatusServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(OverallCompletionStatusServiceImpl.class);

	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String SEARCH_BY_PRIORITY = "Filter by priority";
	public static final String UNCHECKED = "unchecked";
	private static final String ISSUES = "issues";
	private static final String DELAY = "Delay";
	private static final String OVERALL = "Overall";
	private static final String SPRINT_DETAILS = "sprintDetails";
	public static final String ISSUE_CUSTOM_HISTORY = "issues custom history";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	public static final String ACTUAL_COMPLETION_DAYS = "actualCompletionDays";
	public static final String ACTUAL_COMPLETE_DATE = "actualCompleteDate";
	private static final String LABEL_INFO = "(Issue Count/Story Points)";
	private static final String LABEL_INFO_FOR_ORIGINAL_ESTIMATE = "(Issue Count/Original Estimate)";
	public static final String COMPLETED = "Completed";
	public static final String PLANNED = "Planned";
	public static final String ACTUAL = "Actual";
	public static final String ACTUAL_START_DATE = "actualStartDate";
	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

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
		return KPICode.OVERALL_COMPLETION_STATUS.name();
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
			LOGGER.info("Overall Completion Status -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId().toString();
			String sprintId = leafNode.getSprintFilter().getId();
			SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintId);
			if (null != sprintDetails) {
				List<String> totalIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.TOTAL_ISSUES);
				List<String> completedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.COMPLETED_ISSUES);
				if (CollectionUtils.isNotEmpty(totalIssues)) {
					List<JiraIssue> issueList = jiraIssueRepository
							.findByNumberInAndBasicProjectConfigId(totalIssues, basicProjectConfigId);
					List<JiraIssueCustomHistory> issueHistoryList = jiraIssueCustomHistoryRepository
							.findByStoryIDInAndBasicProjectConfigIdIn(totalIssues,
									Collections.singletonList(basicProjectConfigId));
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getTotalIssues(), issueList);
					resultListMap.put(ISSUES, new ArrayList<>(filtersIssuesList));
					resultListMap.put(ISSUE_CUSTOM_HISTORY, new ArrayList<>(issueHistoryList));
					resultListMap.put(SPRINT_DETAILS, sprintDetails);
				}
				resultListMap.put(COMPLETED,new ArrayList<>(completedIssues));
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
		Object basicProjectConfigId = latestSprint.getProjectFilter().getBasicProjectConfigId();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);

		SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT_DETAILS);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		List<String> allCompletedIssuesList = (List<String>) resultMap.get(COMPLETED);
		List<JiraIssueCustomHistory> allIssueHistories = (List<JiraIssueCustomHistory>) resultMap
				.get(ISSUE_CUSTOM_HISTORY);
		if (CollectionUtils.isNotEmpty(allIssues)) {
			LOGGER.info("Overall Completion Status -> request id : {} total jira Issues : {}", requestTrackerId, allIssues.size());
			Map<String, Map<String, List<JiraIssue>>> typeAndPriorityWiseIssues = allIssues.stream().collect(
					Collectors.groupingBy(JiraIssue::getTypeName, Collectors.groupingBy(JiraIssue::getPriority)));

			Set<String> issueTypes = new HashSet<>();
			Set<String> priorities = new HashSet<>();
			List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
			List<Integer> overAllIssueCountActual = Arrays.asList(0);
			List<Double> overAllStoryPointsActual = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimateActual = Arrays.asList(0.0);
			List<Integer> overAllIssueCountPlanned = Arrays.asList(0);
			List<Double> overAllStoryPointsPlanned = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimatePlanned = Arrays.asList(0.0);
			List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
			typeAndPriorityWiseIssues
					.forEach((issueType, priorityWiseIssue) -> priorityWiseIssue.forEach((priority, issues) -> {
						issueTypes.add(issueType);
						priorities.add(priority);
						List<IterationKpiModalValue> modalValues = new ArrayList<>();
						int issueCount = 0;
						Double storyPoint = 0.0;
						Double originalEstimate = 0.0;
						int issueCountPlanned = 0;
						Double storyPointPlanned = 0.0;
						Double originalEstimatePlanned = 0.0;
						for (JiraIssue jiraIssue : issues) {
							if (SprintDetails.SPRINT_STATE_ACTIVE.equalsIgnoreCase(sprintDetails.getState())) {
								// Checking if dueDate is < today date for active sprint
								if (StringUtils.isNotEmpty(jiraIssue.getDueDate()) && DateUtil
										.stringToLocalDate(jiraIssue.getDueDate(), DateUtil.TIME_FORMAT_WITH_SEC)
										.isBefore(LocalDate.now())) {
									issueCountPlanned = issueCountPlanned + 1;
									overAllIssueCountPlanned.set(0, overAllIssueCountPlanned.get(0) + 1);
									if (null != jiraIssue.getStoryPoints()) {
										storyPointPlanned = storyPointPlanned + jiraIssue.getStoryPoints();
										overAllStoryPointsPlanned.set(0,
												overAllStoryPointsPlanned.get(0) + jiraIssue.getStoryPoints());
									}
									if (null != jiraIssue.getOriginalEstimateMinutes()) {
										originalEstimatePlanned = originalEstimatePlanned
												+ jiraIssue.getOriginalEstimateMinutes();
										overAllOriginalEstimatePlanned.set(0, overAllOriginalEstimatePlanned.get(0)
												+ jiraIssue.getOriginalEstimateMinutes());
									}
								}
							} else {
								// Checking if dueDate is <= sprint End Date for closed sprint
								if (StringUtils.isNotEmpty(jiraIssue.getDueDate()) && DateUtil
										.stringToLocalDate(jiraIssue.getDueDate(), DateUtil.TIME_FORMAT_WITH_SEC)
										.isBefore(DateUtil.stringToLocalDate(sprintDetails.getCompleteDate(),
												DateUtil.TIME_FORMAT_WITH_SEC).plusDays(1))) {
									issueCountPlanned = issueCountPlanned + 1;
									overAllIssueCountPlanned.set(0, overAllIssueCountPlanned.get(0) + 1);
									if (null != jiraIssue.getStoryPoints()) {
										storyPointPlanned = storyPointPlanned + jiraIssue.getStoryPoints();
										overAllStoryPointsPlanned.set(0,
												overAllStoryPointsPlanned.get(0) + jiraIssue.getStoryPoints());
									}
									if (null != jiraIssue.getOriginalEstimateMinutes()) {
										originalEstimatePlanned = originalEstimatePlanned
												+ jiraIssue.getOriginalEstimateMinutes();
										overAllOriginalEstimatePlanned.set(0, overAllOriginalEstimatePlanned.get(0)
												+ jiraIssue.getOriginalEstimateMinutes());
									}
								}
							}
							// Calculating delay for only completed issues
							if (allCompletedIssuesList.contains(jiraIssue.getNumber())) {
								int jiraIssueDelay = 0;
								int originalEstimateInDays = 0;
								issueCount = issueCount + 1;
								overAllIssueCountActual.set(0, overAllIssueCountActual.get(0) + 1);

								JiraIssueCustomHistory issueCustomHistory = allIssueHistories.stream()
										.filter(jiraIssueCustomHistory -> jiraIssueCustomHistory.getStoryID()
												.equals(jiraIssue.getNumber()))
										.findFirst().orElse(new JiraIssueCustomHistory());

								if (jiraIssue.getOriginalEstimateMinutes() != null)
									originalEstimateInDays = (jiraIssue.getOriginalEstimateMinutes() / 60) / 8;

								String devCompletionDate = getDevCompletionDate(issueCustomHistory, fieldMapping);
								// calling function for cal actual completion days
								Map<String, Object> actualCompletionData = calActualCompletionDays(issueCustomHistory,
										sprintDetails, fieldMapping);
								int actualCompletionInDays = 0;

								if (actualCompletionData.get(ACTUAL_COMPLETION_DAYS) != "-") {
									actualCompletionInDays = (int) actualCompletionData.get(ACTUAL_COMPLETION_DAYS);
									jiraIssueDelay = actualCompletionInDays - originalEstimateInDays;
								}

								if (null != jiraIssue.getStoryPoints()) {
									storyPoint = storyPoint + jiraIssue.getStoryPoints();
									overAllStoryPointsActual.set(0,
											overAllStoryPointsActual.get(0) + jiraIssue.getStoryPoints());
								}
								if (null != jiraIssue.getOriginalEstimateMinutes()) {
									originalEstimate = originalEstimate + jiraIssue.getOriginalEstimateMinutes();
									overAllOriginalEstimateActual.set(0, overAllOriginalEstimateActual.get(0)
											+ jiraIssue.getOriginalEstimateMinutes());
								}
								populateIterationDataForWorkCompleted(overAllmodalValues, modalValues, jiraIssue,
										fieldMapping, actualCompletionData, jiraIssueDelay, devCompletionDate);
							}
						}
						List<IterationKpiData> data = new ArrayList<>();
						IterationKpiData issueCountsPlanned;
						IterationKpiData issueCountsActual;
						IterationKpiData delay;
						issueCountsPlanned = createIterationKpiData(PLANNED,fieldMapping, issueCountPlanned, storyPointPlanned, originalEstimatePlanned,null);
						issueCountsActual = createIterationKpiData(ACTUAL,fieldMapping, issueCount, storyPoint, originalEstimate,modalValues);
						delay = createIterationKpiData(DELAY,fieldMapping, issueCountPlanned-issueCount, storyPointPlanned-storyPoint, originalEstimatePlanned-originalEstimate,null);
						data.add(issueCountsPlanned);
						data.add(issueCountsActual);
						data.add(delay);
						IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, priority, data);
						iterationKpiValues.add(iterationKpiValue);
					}));
			List<IterationKpiData> data = new ArrayList<>();
			IterationKpiData overAllIssueCountsPlanned;
			IterationKpiData overAllIssueCountsActual;
			IterationKpiData overAllDelay;
			overAllIssueCountsPlanned = createIterationKpiData(PLANNED,fieldMapping, overAllIssueCountPlanned.get(0), overAllStoryPointsPlanned.get(0), overAllOriginalEstimatePlanned.get(0),null);
			overAllIssueCountsActual = createIterationKpiData(ACTUAL,fieldMapping, overAllIssueCountActual.get(0), overAllStoryPointsActual.get(0), overAllOriginalEstimateActual.get(0),overAllmodalValues);
			overAllDelay = createIterationKpiData(DELAY,fieldMapping,overAllIssueCountPlanned.get(0)-overAllIssueCountActual.get(0), overAllStoryPointsPlanned.get(0)-overAllStoryPointsActual.get(0),overAllOriginalEstimatePlanned.get(0)-overAllOriginalEstimateActual.get(0),null);
			data.add(overAllIssueCountsPlanned);
			data.add(overAllIssueCountsActual);
			data.add(overAllDelay);
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
			iterationKpiValues.add(overAllIterationKpiValue);

			// Create kpi level filters
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypes);
			IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, priorities);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
			trendValue.setValue(iterationKpiValues);
			kpiElement.setFilters(iterationKpiFilters);
			kpiElement.setSprint(latestSprint.getName());
			kpiElement.setExcelColumnInfo(KPIExcelColumn.OVERALL_COMPLETION_STATUS.getKpiExcelColumnInfo());
			kpiElement.setTrendValueList(trendValue);
		}
	}

	/**
	 *  For Assigning IterationKPiData
	 * @param label
	 * @param fieldMapping
	 * @param issueCount
	 * @param storyPoint
	 * @param originalEstimate
	 * @param modalvalue
	 * @return
	 */
	private IterationKpiData createIterationKpiData(String label, FieldMapping fieldMapping, Integer issueCount,
			Double storyPoint, Double originalEstimate, List<IterationKpiModalValue> modalvalue) {
		IterationKpiData iterationKpiData;
		if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			iterationKpiData = new IterationKpiData(label, Double.valueOf(issueCount), storyPoint, LABEL_INFO, "",
					CommonConstant.SP, modalvalue);
		} else {
			iterationKpiData = new IterationKpiData(label, Double.valueOf(issueCount), originalEstimate,
					LABEL_INFO_FOR_ORIGINAL_ESTIMATE, "", CommonConstant.DAY, modalvalue);
		}
		return iterationKpiData;
	}

	/**
	 * Method to calculate actualCompletion days both start and end date inclusive
	 *
	 * @param issueCustomHistory
	 * @param sprintDetail
	 * @param fieldMapping
	 * @return
	 */
	private Map<String, Object> calActualCompletionDays(JiraIssueCustomHistory issueCustomHistory,
			SprintDetails sprintDetail, FieldMapping fieldMapping) {
		List<String> inProgressStatuses = new ArrayList<>();
		List<JiraIssueSprint> filterStorySprintDetails = new ArrayList<>();

		LocalDate sprintStartDate = LocalDate.parse(sprintDetail.getStartDate().split("\\.")[0], DATE_TIME_FORMATTER);
		LocalDate sprintEndDate = LocalDate.parse(sprintDetail.getEndDate().split("\\.")[0], DATE_TIME_FORMATTER);
		Map<String, Object> resultList = new HashMap<>();

		// filtering storySprintDetails lies in between sprintStart and sprintEnd
		if (CollectionUtils.isNotEmpty(issueCustomHistory.getStorySprintDetails())) {
			filterStorySprintDetails = issueCustomHistory.getStorySprintDetails().stream()
					.filter(jiraIssueSprint -> DateUtil.isWithinDateRange(LocalDate
							.parse(jiraIssueSprint.getActivityDate().toString().split("\\.")[0], DATE_TIME_FORMATTER),
							sprintStartDate, sprintEndDate))
					.collect(Collectors.toList());
		}

		Set<String> closedStatus = sprintDetail.getCompletedIssues().stream().map(SprintIssue::getStatus)
				.collect(Collectors.toSet());

		// sorting the story history on basis of activityDate
		filterStorySprintDetails.sort(Comparator.comparing(JiraIssueSprint::getActivityDate));

		if (null != fieldMapping && CollectionUtils.isNotEmpty(fieldMapping.getJiraStatusForInProgress())) {
			inProgressStatuses = fieldMapping.getJiraStatusForInProgress();
		}

		LocalDate startDate = null;
		LocalDate endDate = null;
		boolean isStartDateFound = false;
		for (JiraIssueSprint storySprintDetail : filterStorySprintDetails) {
			LocalDate activityLocalDate = LocalDate
					.parse(storySprintDetail.getActivityDate().toString().split("\\.")[0], DATE_TIME_FORMATTER);

			if (inProgressStatuses.contains(storySprintDetail.getFromStatus()) && !isStartDateFound) {
				startDate = activityLocalDate;
				isStartDateFound = true;
			}
			if (closedStatus.contains(storySprintDetail.getFromStatus())) {
				endDate = activityLocalDate;
			}
		}
		if (startDate != null && endDate != null) {
			// +1 to include end date
			resultList.put(ACTUAL_COMPLETION_DAYS, CommonUtils.getWorkingDays(startDate, endDate) + 1);
		} else {
			resultList.put(ACTUAL_COMPLETION_DAYS, "-");
		}
		if (startDate != null)
			resultList.put(ACTUAL_START_DATE, startDate);
		else
			resultList.put(ACTUAL_START_DATE, "-");
		resultList.put(ACTUAL_COMPLETE_DATE, endDate);
		return resultList;
	}
}
