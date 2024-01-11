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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
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
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UnplannedWorkStatusServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {
	public static final String UNCHECKED = "unchecked";
	public static final String OVERALL_UNPLANNED = "Overall Unplanned";
	public static final String COMPLETED = "Completed";
	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String SEARCH_BY_PRIORITY = "Filter by priority";
	private static final String ISSUES = "issues";
	private static final String OVERALL = "Overall";
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
		return KPICode.UNPLANNED_WORK_STATUS.name();
	}

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			log.info("Unplanned Work Status -> Requested sprint : {}", leafNode.getName());
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
						dbSprintDetail, fieldMapping.getJiraIterationIssuetypeKPI134(),
						fieldMapping.getJiraIterationCompletionStatusKPI134(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> totalIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.TOTAL_ISSUES);
				List<String> completedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.COMPLETED_ISSUES);
				if (CollectionUtils.isNotEmpty(totalIssues)) {
					List<JiraIssue> jiraIssueList = IterationKpiHelper.getFilteredJiraIssue(totalIssues,
							totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getTotalIssues(), jiraIssueList);
					resultListMap.put(ISSUES, new ArrayList<>(filtersIssuesList));
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

		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		List<String> allCompletedIssuesList = (List<String>) resultMap.get(COMPLETED);
		// Filtering out the issues without due date.
		List<JiraIssue> allIssuesWithoutDueDate = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(allIssues)) {
			allIssuesWithoutDueDate = allIssues.stream()
					.filter(jiraIssue -> StringUtils.isBlank(jiraIssue.getDueDate())).collect(Collectors.toList());
		}
		if (CollectionUtils.isNotEmpty(allIssuesWithoutDueDate)) {
			log.info("Unplanned Work Status -> request id : {} total jira Issues : {}", requestTrackerId,
					allIssuesWithoutDueDate.size());
			// Creating map of modal Objects
			Map<String, IterationKpiModalValue> modalObjectMap = KpiDataHelper.createMapOfModalObject(allIssues);
			Map<String, Map<String, List<JiraIssue>>> typeAndPriorityWiseIssues = allIssuesWithoutDueDate.stream()
					.collect(Collectors.groupingBy(JiraIssue::getTypeName,
							Collectors.groupingBy(JiraIssue::getPriority)));

			Set<String> issueTypes = new HashSet<>();
			Set<String> priorities = new HashSet<>();
			List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
			List<Integer> overAllIssueCountCompleted = Arrays.asList(0);
			List<Double> overAllStoryPointsCompleted = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimateCompleted = Arrays.asList(0.0);
			List<Integer> overAllIssueCountUnplanned = Arrays.asList(0);
			List<Double> overAllStoryPointsUnplanned = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimateUnplanned = Arrays.asList(0.0);
			List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
			typeAndPriorityWiseIssues
					.forEach((issueType, priorityWiseIssue) -> priorityWiseIssue.forEach((priority, issues) -> {
						issueTypes.add(issueType);
						priorities.add(priority);
						List<IterationKpiModalValue> modalValues = new ArrayList<>();
						int issueCountCompleted = 0;
						Double storyPointCompleted = 0.0;
						Double originalEstimateCompleted = 0.0;
						int issueCountUnplanned = 0;
						Double storyPointUnplanned = 0.0;
						Double originalEstimateUnplanned = 0.0;
						for (JiraIssue jiraIssue : issues) {
							issueCountUnplanned = issueCountUnplanned + 1;
							overAllIssueCountUnplanned.set(0, overAllIssueCountUnplanned.get(0) + 1);

							storyPointUnplanned = KpiDataHelper.getStoryPoint(overAllStoryPointsUnplanned,
									storyPointUnplanned, jiraIssue);
							originalEstimateUnplanned = KpiDataHelper.getOriginalEstimate(
									overAllOriginalEstimateUnplanned, originalEstimateUnplanned, jiraIssue);
							// For unplanned completed issues
							if (allCompletedIssuesList.contains(jiraIssue.getNumber())) {
								issueCountCompleted = issueCountCompleted + 1;
								overAllIssueCountCompleted.set(0, overAllIssueCountCompleted.get(0) + 1);

								storyPointCompleted = KpiDataHelper.getStoryPoint(overAllStoryPointsCompleted,
										storyPointCompleted, jiraIssue);
								originalEstimateCompleted = KpiDataHelper.getOriginalEstimate(
										overAllOriginalEstimateCompleted, originalEstimateCompleted, jiraIssue);
							}
							KPIExcelUtility.populateIterationKPI(overAllmodalValues, modalValues, jiraIssue,
									fieldMapping, modalObjectMap);
						}
						List<IterationKpiData> data = new ArrayList<>();
						IterationKpiData issueCountsPlanned;
						IterationKpiData issueCountsActual;
						issueCountsPlanned = createIterationKpiData(OVERALL_UNPLANNED, fieldMapping,
								issueCountUnplanned, storyPointUnplanned, originalEstimateUnplanned, modalValues);
						issueCountsActual = createIterationKpiData(COMPLETED, fieldMapping, issueCountCompleted,
								storyPointCompleted, originalEstimateCompleted, null);
						data.add(issueCountsPlanned);
						data.add(issueCountsActual);
						IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, priority, data);
						iterationKpiValues.add(iterationKpiValue);
					}));
			List<IterationKpiData> data = new ArrayList<>();
			IterationKpiData overAllIssueCountsPlanned;
			IterationKpiData overAllIssueCountsActual;
			overAllIssueCountsPlanned = createIterationKpiData(OVERALL_UNPLANNED, fieldMapping,
					overAllIssueCountUnplanned.get(0), overAllStoryPointsUnplanned.get(0),
					overAllOriginalEstimateUnplanned.get(0), overAllmodalValues);
			overAllIssueCountsActual = createIterationKpiData(COMPLETED, fieldMapping,
					overAllIssueCountCompleted.get(0), overAllStoryPointsCompleted.get(0),
					overAllOriginalEstimateCompleted.get(0), null);
			data.add(overAllIssueCountsPlanned);
			data.add(overAllIssueCountsActual);
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
			iterationKpiValues.add(overAllIterationKpiValue);

			// Create kpi level filters
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypes);
			IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, priorities);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
			trendValue.setValue(iterationKpiValues);
			kpiElement.setFilters(iterationKpiFilters);
			kpiElement.setSprint(latestSprint.getName());
			kpiElement.setModalHeads(KPIExcelColumn.UNPLANNED_WORK_STATUS.getColumns());
			kpiElement.setTrendValueList(trendValue);
		}
	}

}
