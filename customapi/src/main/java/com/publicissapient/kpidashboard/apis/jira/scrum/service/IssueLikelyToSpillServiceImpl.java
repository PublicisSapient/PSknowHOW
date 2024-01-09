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
import java.util.ArrayList;
import java.util.Arrays;
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
import com.publicissapient.kpidashboard.apis.util.IterationKpiHelper;
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
public class IssueLikelyToSpillServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	public static final String UNCHECKED = "unchecked";
	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String SEARCH_BY_PRIORITY = "Filter by priority";
	private static final String ISSUES = "issues";
	private static final String ISSUES_AT_RISK = "Issues at Risk";
	private static final String OVERALL = "Overall";
	private static final String SPRINT_STATE_ACTIVE = "ACTIVE";
	private static final String SPRINT_DETAILS = "sprint details";

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private SprintRepository sprintRepository;

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
		return KPICode.ISSUE_LIKELY_TO_SPILL.name();
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
			log.info("Issue Likely to Spill -> Requested sprint : {}", leafNode.getName());

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

				sprintDetails = IterationKpiHelper.transformIterSprintdetail(totalHistoryList, issueList,
						dbSprintDetail, fieldMapping.getJiraIterationIssuetypeKPI123(),
						fieldMapping.getJiraIterationCompletionStatusKPI123(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> notCompletedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(
						sprintDetails, CommonConstant.NOT_COMPLETED_ISSUES);
				if (CollectionUtils.isNotEmpty(notCompletedIssues)) {
					List<JiraIssue> filteredJiraIssue = IterationKpiHelper.getFilteredJiraIssue(notCompletedIssues,
							totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getNotCompletedIssues(), filteredJiraIssue);
					resultListMap.put(ISSUES, new ArrayList<>(filtersIssuesList));
					resultListMap.put(SPRINT_DETAILS, sprintDetails);
				}
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

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT_DETAILS);
		if (CollectionUtils.isNotEmpty(allIssues)) {
			log.info("Issue Likely To Spill -> request id : {} total jira Issues : {}", requestTrackerId,
					allIssues.size());
			// Creating map of modal Objects
			Map<String, IterationKpiModalValue> modalObjectMap = KpiDataHelper.createMapOfModalObject(allIssues);
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(Objects.requireNonNull(latestSprint).getProjectFilter().getBasicProjectConfigId());

			Map<String, Map<String, List<JiraIssue>>> typeAndPriorityWiseIssues = allIssues.stream().collect(
					Collectors.groupingBy(JiraIssue::getTypeName, Collectors.groupingBy(JiraIssue::getPriority)));
			List<IterationPotentialDelay> iterationPotentialDelayList = calculatePotentialDelay(sprintDetails,
					allIssues, fieldMapping);
			Map<String, IterationPotentialDelay> issueWiseDelay = iterationPotentialDelayList.stream()
					.collect(Collectors.toMap(IterationPotentialDelay::getIssueId, Function.identity(), (e1, e2) -> e2,
							LinkedHashMap::new));

			Set<String> issueTypes = new HashSet<>();
			Set<String> priorities = new HashSet<>();
			List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
			List<Integer> overAllIssueCount = Arrays.asList(0);
			List<Double> overAllStoryPoints = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimate = Arrays.asList(0.0);
			List<Integer> overAllriskIssueCount = Arrays.asList(0);
			String sprintState = sprintDetails.getState();
			LocalDate sprintEndDate = DateUtil.stringToLocalDate(sprintDetails.getEndDate(),
					DateUtil.TIME_FORMAT_WITH_SEC);
			List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
			typeAndPriorityWiseIssues
					.forEach((issueType, priorityWiseIssue) -> priorityWiseIssue.forEach((priority, issues) -> {
						issueTypes.add(issueType);
						priorities.add(priority);
						List<IterationKpiModalValue> modalValues = new ArrayList<>();
						int issueCount = 0;
						int riskIssueCount = 0;
						Double storyPoint = 0.0;
						Double originalEstimate = 0.0;
						for (JiraIssue jiraIssue : issues) {
							issueCount = issueCount + 1;
							overAllIssueCount.set(0, overAllIssueCount.get(0) + 1);
							if (SPRINT_STATE_ACTIVE.equals(sprintState)) {
								if (isIssueAtRisk(jiraIssue, issueWiseDelay, sprintEndDate)
										|| (jiraIssue.getDueDate() != null)
												&& DateUtil.stringToLocalDate(jiraIssue.getDueDate(),
														DateUtil.TIME_FORMAT_WITH_SEC).isAfter(sprintEndDate)) {
									riskIssueCount = riskIssueCount + 1;
									overAllriskIssueCount.set(0, overAllriskIssueCount.get(0) + 1);
									KPIExcelUtility.populateIterationKPI(overAllmodalValues, modalValues, jiraIssue,
											fieldMapping, modalObjectMap);
									setKpiSpecificData(modalObjectMap, issueWiseDelay, jiraIssue);
									if (null != jiraIssue.getStoryPoints()) {
										storyPoint = storyPoint + jiraIssue.getStoryPoints();
										overAllStoryPoints.set(0,
												overAllStoryPoints.get(0) + jiraIssue.getStoryPoints());
									}
									if (null != jiraIssue.getOriginalEstimateMinutes()) {
										originalEstimate = originalEstimate + jiraIssue.getOriginalEstimateMinutes();
										overAllOriginalEstimate.set(0, overAllOriginalEstimate.get(0)
												+ jiraIssue.getOriginalEstimateMinutes());
									}
								}
							} else {
								riskIssueCount = riskIssueCount + 1;
								overAllriskIssueCount.set(0, overAllriskIssueCount.get(0) + 1);
								KPIExcelUtility.populateIterationKPI(overAllmodalValues, modalValues, jiraIssue,
										fieldMapping, modalObjectMap);
								setKpiSpecificData(modalObjectMap, issueWiseDelay, jiraIssue);
								if (null != jiraIssue.getStoryPoints()) {
									storyPoint = storyPoint + jiraIssue.getStoryPoints();
									overAllStoryPoints.set(0, overAllStoryPoints.get(0) + jiraIssue.getStoryPoints());
								}
								if (null != jiraIssue.getOriginalEstimateMinutes()) {
									originalEstimate = originalEstimate + jiraIssue.getOriginalEstimateMinutes();
									overAllOriginalEstimate.set(0,
											overAllOriginalEstimate.get(0) + jiraIssue.getOriginalEstimateMinutes());
								}
							}
						}
						List<IterationKpiData> data = new ArrayList<>();
						IterationKpiData issueAtRiskSp;
						IterationKpiData issueAtRisk = new IterationKpiData(ISSUES_AT_RISK,
								Double.valueOf(riskIssueCount), Double.valueOf(issueCount), null, "", modalValues);
						if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
								&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
							issueAtRiskSp = new IterationKpiData(CommonConstant.STORY_POINT, roundingOff(storyPoint),
									null, null, CommonConstant.SP, null);
						} else {
							issueAtRiskSp = new IterationKpiData(CommonConstant.ORIGINAL_ESTIMATE,
									roundingOff(originalEstimate), null, null, CommonConstant.HOURS, null);
						}
						data.add(issueAtRisk);
						data.add(issueAtRiskSp);
						IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, priority, data);
						iterationKpiValues.add(iterationKpiValue);
					}));
			List<IterationKpiData> data = new ArrayList<>();

			IterationKpiData overAllIssuesAtRisk = new IterationKpiData(ISSUES_AT_RISK,
					Double.valueOf(overAllriskIssueCount.get(0)), Double.valueOf(overAllIssueCount.get(0)), null, "",
					overAllmodalValues);
			IterationKpiData overAlllRiskSp;
			if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
					&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
				overAlllRiskSp = new IterationKpiData(CommonConstant.STORY_POINT,
						roundingOff(overAllStoryPoints.get(0)), null, null, CommonConstant.SP, null);
			} else {
				overAlllRiskSp = new IterationKpiData(CommonConstant.ORIGINAL_ESTIMATE,
						roundingOff(overAllOriginalEstimate.get(0)), null, null, CommonConstant.HOURS, null);
			}
			data.add(overAllIssuesAtRisk);
			data.add(overAlllRiskSp);
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
			iterationKpiValues.add(overAllIterationKpiValue);

			// Create kpi level filters
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypes);
			IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, priorities);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
			// Modal Heads Options
			trendValue.setValue(iterationKpiValues);
			kpiElement.setFilters(iterationKpiFilters);
			kpiElement.setSprint(latestSprint.getName());
			kpiElement.setModalHeads(KPIExcelColumn.ISSUES_LIKELY_TO_SPILL.getColumns());
			kpiElement.setTrendValueList(trendValue);
		}
	}

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
				CalculatePCDHelper.arrangeJiraIssueList(fieldMapping.getJiraStatusForInProgressKPI123(), jiraIssues,
						inProgressIssues, openIssues);
				iterationPotentialDelayList
						.addAll(sprintWiseDelayCalculation(inProgressIssues, openIssues, sprintDetails));
			});
		}

		if (CollectionUtils.isNotEmpty(fieldMapping.getJiraStatusForInProgressKPI123())) {
			List<JiraIssue> inProgressIssues = allIssues.stream()
					.filter(jiraIssue -> (jiraIssue.getAssigneeId() == null)
							&& StringUtils.isNotEmpty(jiraIssue.getDueDate())
							&& (fieldMapping.getJiraStatusForInProgressKPI123().contains(jiraIssue.getStatus())))
					.collect(Collectors.toList());

			List<JiraIssue> openIssues = new ArrayList<>();
			iterationPotentialDelayList.addAll(sprintWiseDelayCalculation(inProgressIssues, openIssues, sprintDetails));
		}
		return iterationPotentialDelayList;

	}

	private boolean isIssueAtRisk(JiraIssue jiraIssue, Map<String, IterationPotentialDelay> issueWiseDelay,
			LocalDate sprintEndDate) {
		return issueWiseDelay.containsKey(jiraIssue.getNumber()) && LocalDate
				.parse(issueWiseDelay.get(jiraIssue.getNumber()).getPredictedCompletedDate()).isAfter(sprintEndDate);
	}

	private void setKpiSpecificData(Map<String, IterationKpiModalValue> modalObjectMap,
			Map<String, IterationPotentialDelay> issueWiseDelay, JiraIssue jiraIssue) {
		IterationKpiModalValue jiraIssueModalObject = modalObjectMap.get(jiraIssue.getNumber());
		if (issueWiseDelay.containsKey(jiraIssue.getNumber())) {
			IterationPotentialDelay iterationPotentialDelay = issueWiseDelay.get(jiraIssue.getNumber());
			jiraIssueModalObject.setPotentialDelay(String.valueOf(iterationPotentialDelay.getPotentialDelay()) + "d");
			jiraIssueModalObject.setPredictedCompletionDate(
					DateUtil.dateTimeConverter(iterationPotentialDelay.getPredictedCompletedDate(),
							DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));

		} else {
			jiraIssueModalObject.setPotentialDelay("-");
			jiraIssueModalObject.setPredictedCompletionDate("-");
		}
	}

}
