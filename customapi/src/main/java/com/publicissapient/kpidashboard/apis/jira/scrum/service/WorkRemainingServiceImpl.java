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

import static com.publicissapient.kpidashboard.apis.util.IterationKpiHelper.getDevCompletionDate;
import static com.publicissapient.kpidashboard.apis.util.IterationKpiHelper.getFilteredJiraIssue;
import static com.publicissapient.kpidashboard.apis.util.IterationKpiHelper.getFilteredJiraIssueHistory;
import static com.publicissapient.kpidashboard.apis.util.IterationKpiHelper.transformIterSprintdetail;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.CalculatePCDHelper;
import com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard.JiraIterationKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFilters;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFiltersOptions;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.IterationPotentialDelay;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WorkRemainingServiceImpl extends JiraIterationKPIService {

	public static final String UNCHECKED = "unchecked";
	public static final String ISSUE_CUSTOM_HISTORY = "issues custom history";
	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String SEARCH_BY_PRIORITY = "Filter by status";
	private static final String ISSUES = "issues";
	private static final String ISSUE_COUNT = "Issue Count";
	private static final String REMAINING_WORK = "Remaining Work";
	private static final String POTENTIAL_DELAY = "Potential Delay";
	private static final String OVERALL = "Overall";
	private static final String SPRINT_DETAILS = "sprint details";
	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private SprintRepository sprintRepository;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node sprintNode)
			throws ApplicationException {
		DataCount trendValue = new DataCount();
		projectWiseLeafNodeValue(sprintNode, trendValue, kpiElement, kpiRequest);
		return kpiElement;
	}

	@Override
	public String getQualifierType() {
		return KPICode.WORK_REMAINING.name();
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		if (null != leafNode) {
			log.info("Work Remaining -> Requested sprint : {}", leafNode.getName());
			SprintDetails sprintDetails;
			SprintDetails dbSprintDetail = getSprintDetailsFromBaseClass();
			if (null != dbSprintDetail) {
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
						.get(leafNode.getProjectFilter().getBasicProjectConfigId());
				// to modify sprintdetails on the basis of configuration for the project
				List<JiraIssueCustomHistory> totalHistoryList = getJiraIssuesCustomHistoryFromBaseClass();
				List<JiraIssue> totalJiraIssueList = getJiraIssuesFromBaseClass();
				Set<String> issueList = totalJiraIssueList.stream().map(JiraIssue::getNumber)
						.collect(Collectors.toSet());

				sprintDetails = transformIterSprintdetail(totalHistoryList, issueList, dbSprintDetail,
						fieldMapping.getJiraIterationIssuetypeKPI119(),
						fieldMapping.getJiraIterationCompletionStatusKPI119(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> notCompletedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(
						sprintDetails, CommonConstant.NOT_COMPLETED_ISSUES);
				if (CollectionUtils.isNotEmpty(notCompletedIssues)) {
					List<JiraIssue> notCompletedJiraIssueList = getFilteredJiraIssue(notCompletedIssues,
							totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getNotCompletedIssues(), notCompletedJiraIssueList);
					List<JiraIssueCustomHistory> issueHistoryList = getFilteredJiraIssueHistory(
							notCompletedJiraIssueList.stream().map(JiraIssue::getNumber).toList(), totalHistoryList);
					resultListMap.put(ISSUES, new ArrayList<>(filtersIssuesList));
					resultListMap.put(SPRINT_DETAILS, sprintDetails);
					resultListMap.put(ISSUE_CUSTOM_HISTORY, issueHistoryList);
				}
			}
		}
		return resultListMap;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint level.
	 *
	 * @param sprintLeafNode
	 *            sprintLeafNode
	 * @param kpiElement
	 *            kpiElement
	 * @param kpiRequest
	 *            kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(Node sprintLeafNode, DataCount trendValue, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		Object basicProjectConfigId = Objects.requireNonNull(sprintLeafNode).getProjectFilter()
				.getBasicProjectConfigId();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNode, null, null, kpiRequest);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT_DETAILS);
		List<JiraIssueCustomHistory> allIssueHistories = (List<JiraIssueCustomHistory>) resultMap
				.get(ISSUE_CUSTOM_HISTORY);
		if (CollectionUtils.isNotEmpty(allIssues)) {
			log.info("Work Remaining -> request id : {} total jira Issues : {}", requestTrackerId, allIssues.size());
			// Creating map of modal Objects
			Map<String, IterationKpiModalValue> modalObjectMap = KpiDataHelper.createMapOfModalObject(allIssues);
			Map<String, Map<String, List<JiraIssue>>> typeAndStatusWiseIssues = allIssues.stream().collect(
					Collectors.groupingBy(JiraIssue::getTypeName, Collectors.groupingBy(JiraIssue::getStatus)));
			List<IterationPotentialDelay> iterationPotentialDelayList = CalculatePCDHelper
					.calculatePotentialDelay(sprintDetails, allIssues, fieldMapping.getJiraStatusForInProgressKPI119());
			Map<String, IterationPotentialDelay> issueWiseDelay = CalculatePCDHelper.checkMaxDelayAssigneeWise(
					iterationPotentialDelayList, fieldMapping.getJiraStatusForInProgressKPI119());
			Set<String> issueTypes = new HashSet<>();
			Set<String> statuses = new HashSet<>();
			List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
			List<Integer> overAllIssueCount = Arrays.asList(0);
			List<Double> overAllStoryPoints = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimate = Arrays.asList(0.0);
			List<Integer> overAllRemHours = Arrays.asList(0);
			List<Integer> overallPotentialDelay = Arrays.asList(0);
			List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
			List<IterationKpiModalValue> finalOverAllmodalValues = overAllmodalValues;
			// For markerInfo
			Map<String, String> markerInfo = new HashMap<>();
			markerInfo.put(Constant.AMBER, "Issue finishing in the last two days of the iteration are marked in AMBER");
			markerInfo.put(Constant.RED, "Issues finishing post issue due date are marked in RED");
			typeAndStatusWiseIssues
					.forEach((issueType, statusWiseIssue) -> statusWiseIssue.forEach((status, issues) -> {
						issueTypes.add(issueType);
						statuses.add(status);
						List<IterationKpiModalValue> modalValues = new ArrayList<>();
						List<IterationKpiModalValue> finalmodalValues = modalValues;
						int issueCount = 0;
						Double storyPoint = 0.0;
						Double originalEstimate = 0.0;
						int remHours = 0;
						int delay = 0;
						for (JiraIssue jiraIssue : issues) {
							JiraIssueCustomHistory issueCustomHistory = allIssueHistories.stream()
									.filter(jiraIssueCustomHistory -> jiraIssueCustomHistory.getStoryID()
											.equals(jiraIssue.getNumber()))
									.findFirst().orElse(new JiraIssueCustomHistory());
							String devCompletionDate = getDevCompletionDate(issueCustomHistory,
									fieldMapping.getJiraDevDoneStatusKPI119());
							KPIExcelUtility.populateIterationKPI(finalOverAllmodalValues, finalmodalValues, jiraIssue,
									fieldMapping, modalObjectMap);
							issueCount = issueCount + 1;
							overAllIssueCount.set(0, overAllIssueCount.get(0) + 1);
							if (null != jiraIssue.getRemainingEstimateMinutes()) {
								remHours = remHours + jiraIssue.getRemainingEstimateMinutes();
								overAllRemHours.set(0,
										overAllRemHours.get(0) + jiraIssue.getRemainingEstimateMinutes());
							}
							if (null != jiraIssue.getStoryPoints()) {
								storyPoint = storyPoint + jiraIssue.getStoryPoints();
								overAllStoryPoints.set(0, overAllStoryPoints.get(0) + jiraIssue.getStoryPoints());
							}
							if (null != jiraIssue.getOriginalEstimateMinutes()) {
								originalEstimate = originalEstimate + jiraIssue.getOriginalEstimateMinutes();
								overAllOriginalEstimate.set(0,
										overAllOriginalEstimate.get(0) + jiraIssue.getOriginalEstimateMinutes());
							}
							delay = checkDelay(jiraIssue, issueWiseDelay, delay, overallPotentialDelay);
							setKpiSpecificData(sprintDetails, modalObjectMap, issueWiseDelay, jiraIssue,
									devCompletionDate);
						}
						List<IterationKpiData> data = new ArrayList<>();
						modalValues = reverseSortModalValue(modalValues);
						IterationKpiData issueCounts = createIssueCountIterationData(fieldMapping, issueCount,
								storyPoint, originalEstimate, modalValues);

						IterationKpiData hours = new IterationKpiData(REMAINING_WORK, (double) remHours, null, null,
								CommonConstant.DAY, null);

						IterationKpiData potentialDelay = new IterationKpiData(POTENTIAL_DELAY, (double) delay, null,
								null, CommonConstant.DAY, null);

						data.add(issueCounts);
						data.add(hours);
						data.add(potentialDelay);
						IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, status, data,
								Arrays.asList("marker"), markerInfo);
						iterationKpiValues.add(iterationKpiValue);
					}));
			List<IterationKpiData> data = new ArrayList<>();
			overAllmodalValues = reverseSortModalValue(overAllmodalValues);
			IterationKpiData overAllCount = createIssueCountIterationData(fieldMapping, overAllIssueCount.get(0),
					overAllStoryPoints.get(0), overAllOriginalEstimate.get(0), overAllmodalValues);
			IterationKpiData overAllHours = new IterationKpiData(REMAINING_WORK, Double.valueOf(overAllRemHours.get(0)),
					null, null, CommonConstant.DAY, null);

			IterationKpiData overAllPotentialDelay = new IterationKpiData(POTENTIAL_DELAY,
					Double.valueOf(overallPotentialDelay.get(0)), null, null, CommonConstant.DAY, null);

			data.add(overAllCount);
			data.add(overAllHours);
			data.add(overAllPotentialDelay);
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data,
					Arrays.asList("marker"), markerInfo);
			iterationKpiValues.add(overAllIterationKpiValue);

			// Create kpi level filters
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypes);
			IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, statuses);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
			trendValue.setValue(iterationKpiValues);
			kpiElement.setFilters(iterationKpiFilters);
			kpiElement.setSprint(sprintLeafNode.getName());
			kpiElement.setModalHeads(KPIExcelColumn.WORK_REMAINING.getColumns());
			kpiElement.setTrendValueList(trendValue);
		}
	}

	private IterationKpiData createIssueCountIterationData(FieldMapping fieldMapping, int issueCount, Double storyPoint,
			Double originalEstimate, List<IterationKpiModalValue> modalValues) {
		IterationKpiData issueCounts;
		if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			issueCounts = new IterationKpiData(ISSUE_COUNT + "/" + CommonConstant.STORY_POINT,
					Double.valueOf(issueCount), roundingOff(storyPoint), null, "", CommonConstant.SP, modalValues);
		} else {
			issueCounts = new IterationKpiData(ISSUE_COUNT + "/" + CommonConstant.ORIGINAL_ESTIMATE,
					Double.valueOf(issueCount), roundingOff(originalEstimate), null, "", CommonConstant.DAY,
					modalValues);
		}
		return issueCounts;
	}

	private List<IterationKpiModalValue> reverseSortModalValue(List<IterationKpiModalValue> modalValues) {
		List<IterationKpiModalValue> sortedModalValue = new ArrayList<>();
		sortedModalValue.addAll(CollectionUtils.emptyIfNull(modalValues).stream()
				.filter(kpiModalValue -> StringUtils.isNotEmpty(kpiModalValue.getPredictedCompletionDate())
						&& !kpiModalValue.getPredictedCompletionDate().equalsIgnoreCase("-"))
				.sorted(Comparator.comparing(IterationKpiModalValue::getPredictedCompletionDate)).toList());
		sortedModalValue.addAll(CollectionUtils.emptyIfNull(modalValues).stream()
				.filter(kpiModalValue -> StringUtils.isEmpty(kpiModalValue.getPredictedCompletionDate())
						|| kpiModalValue.getPredictedCompletionDate().equalsIgnoreCase("-"))
				.toList());
		return sortedModalValue;

	}

	private int getDelayInMinutes(int delay) {
		return delay * 60 * 8;
	}

	private int checkDelay(JiraIssue jiraIssue, Map<String, IterationPotentialDelay> issueWiseDelay, int potentialDelay,
			List<Integer> overallPotentialDelay) {
		int finalDelay = 0;
		if (issueWiseDelay.containsKey(jiraIssue.getNumber())
				&& issueWiseDelay.get(jiraIssue.getNumber()).isMaxMarker()) {
			IterationPotentialDelay iterationPotentialDelay = issueWiseDelay.get(jiraIssue.getNumber());
			finalDelay = potentialDelay + getDelayInMinutes(iterationPotentialDelay.getPotentialDelay());
			overallPotentialDelay.set(0,
					overallPotentialDelay.get(0) + getDelayInMinutes(iterationPotentialDelay.getPotentialDelay()));
		} else {
			finalDelay = potentialDelay + finalDelay;
		}
		return finalDelay;
	}

	private void setKpiSpecificData(SprintDetails sprintDetails, Map<String, IterationKpiModalValue> modalObjectMap,
			Map<String, IterationPotentialDelay> issueWiseDelay, JiraIssue jiraIssue, String devCompletionDate) {
		IterationKpiModalValue jiraIssueModalObject = modalObjectMap.get(jiraIssue.getNumber());
		String devDate = DateUtil.dateTimeConverter(
				devCompletionDate, DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT);
		jiraIssueModalObject.setDevCompletionDate(StringUtils.isNotEmpty(devDate) ? devDate : Constant.DASH);
		String markerValue = Constant.BLANK;
		if (issueWiseDelay.containsKey(jiraIssue.getNumber()) && StringUtils.isNotEmpty(jiraIssue.getDueDate())) {
			IterationPotentialDelay iterationPotentialDelay = issueWiseDelay.get(jiraIssue.getNumber());
			jiraIssueModalObject.setPotentialDelay(iterationPotentialDelay.getPotentialDelay() + "d");
			final LocalDate sprintEndDate = DateUtil.stringToLocalDate(sprintDetails.getEndDate(),
					DateUtil.TIME_FORMAT_WITH_SEC);
			final LocalDate predictCompletionDate = LocalDate
					.parse(iterationPotentialDelay.getPredictedCompletedDate());
			if (!sprintEndDate.isBefore(predictCompletionDate)) {
				if (ChronoUnit.DAYS.between(predictCompletionDate, sprintEndDate) < 2) {
					markerValue = Constant.AMBER;
				}
			} else {
				markerValue = Constant.RED;
			}
			jiraIssueModalObject.setPredictedCompletionDate(
					DateUtil.dateTimeConverter(iterationPotentialDelay.getPredictedCompletedDate(),
							DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));

		} else {
			jiraIssueModalObject.setPotentialOverallDelay("-");
			jiraIssueModalObject.setPredictedCompletionDate("-");
			jiraIssueModalObject.setPotentialDelay(Constant.DASH);
		}
		jiraIssueModalObject.setMarker(markerValue);
	}
}
