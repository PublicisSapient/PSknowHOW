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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Component
public class ClosurePossibleTodayServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	public static final String UNCHECKED = "unchecked";
	private static final Logger LOGGER = LoggerFactory.getLogger(ClosurePossibleTodayServiceImpl.class);
	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String ISSUES = "issues";
	private static final String ISSUE_COUNT = "Issue Count";
	private static final String OVERALL = "Overall";
	private static final String SPRINT_DETAILS = "sprint details";

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
		return KPICode.CLOSURE_POSSIBLE_TODAY.name();
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
			LOGGER.info("Closure Possible Today -> Requested sprint : {}", leafNode.getName());
			SprintDetails dbSprintDetail = getSprintDetailsFromBaseClass();
			SprintDetails sprintDetail;
			if (null != dbSprintDetail) {
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
						.get(leafNode.getProjectFilter().getBasicProjectConfigId());
				// to modify sprintdetails on the basis of configuration for the project
				List<JiraIssueCustomHistory> totalHistoryList = getJiraIssuesCustomHistoryFromBaseClass();
				List<JiraIssue> totalJiraIssueList = getJiraIssuesFromBaseClass();
				Set<String> issueList = totalJiraIssueList.stream().map(JiraIssue::getNumber)
						.collect(Collectors.toSet());

				sprintDetail = IterationKpiHelper.transformIterSprintdetail(totalHistoryList, issueList,
						dbSprintDetail, fieldMapping.getJiraIterationCompletionStatusKPI122(),
						fieldMapping.getJiraIterationCompletionStatusKPI122(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> notCompletedIssues = KpiDataHelper
						.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail, CommonConstant.NOT_COMPLETED_ISSUES);
				if (CollectionUtils.isNotEmpty(notCompletedIssues)) {
					List<JiraIssue> notCompltedJiraIssueList = IterationKpiHelper
							.getFilteredJiraIssue(notCompletedIssues, totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetail,
									sprintDetail.getNotCompletedIssues(), notCompltedJiraIssueList);
					resultListMap.put(ISSUES, new ArrayList<>(filtersIssuesList));
					resultListMap.put(SPRINT_DETAILS, sprintDetail);
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
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
				.get(Objects.requireNonNull(latestSprint).getProjectFilter().getBasicProjectConfigId());

		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT_DETAILS);
		if (CollectionUtils.isNotEmpty(allIssues)) {
			LOGGER.info("Closure Possible Today -> request id : {} total jira Issues : {}", requestTrackerId,
					allIssues.size());
			// Creating map of modal Objects
			Map<String, IterationKpiModalValue> modalObjectMap = KpiDataHelper.createMapOfModalObject(allIssues);
			Map<String, List<JiraIssue>> typeWiseIssues = allIssues.stream()
					.collect(Collectors.groupingBy(JiraIssue::getTypeName));
			List<IterationPotentialDelay> iterationPotentialDelayList = calculatePotentialDelay(sprintDetails,
					allIssues, fieldMapping);
			Map<String, IterationPotentialDelay> issueWiseDelay = iterationPotentialDelayList.stream()
					.collect(Collectors.toMap(IterationPotentialDelay::getIssueId, Function.identity(), (e1, e2) -> e2,
							LinkedHashMap::new));
			Set<String> issueTypes = new HashSet<>();
			List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
			List<Integer> overAllIssueCount = Arrays.asList(0);
			List<Double> overAllStoryPoints = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimate = Arrays.asList(0.0);
			List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
			typeWiseIssues.forEach((issueType, issues) -> {
				issueTypes.add(issueType);
				List<IterationKpiModalValue> modalValues = new ArrayList<>();
				int issueCount = 0;
				Double storyPoint = 0.0;
				Double originalEstimate = 0.0;
				for (JiraIssue jiraIssue : issues) {
					if (issueWiseDelay.containsKey(jiraIssue.getNumber()) && issueWiseDelay.get(jiraIssue.getNumber())
							.getPredictedCompletedDate().equals(LocalDate.now().toString())) {
						KPIExcelUtility.populateIterationKPI(overAllmodalValues, modalValues, jiraIssue, fieldMapping,
								modalObjectMap);
						issueCount = issueCount + 1;
						overAllIssueCount.set(0, overAllIssueCount.get(0) + 1);
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
				IterationKpiData issueCounts = new IterationKpiData(ISSUE_COUNT, Double.valueOf(issueCount), null, null,
						"", modalValues);
				IterationKpiData storyPoints;
				if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
					storyPoints = new IterationKpiData(CommonConstant.STORY_POINT, roundingOff(storyPoint), null, null,
							CommonConstant.SP, null);
				} else {
					storyPoints = new IterationKpiData(CommonConstant.ORIGINAL_ESTIMATE, roundingOff(originalEstimate),
							null, null, CommonConstant.HOURS, null);
				}
				data.add(issueCounts);
				data.add(storyPoints);
				IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, null, data);
				iterationKpiValues.add(iterationKpiValue);
			});
			List<IterationKpiData> data = new ArrayList<>();
			IterationKpiData overAllCount = new IterationKpiData(ISSUE_COUNT, Double.valueOf(overAllIssueCount.get(0)),
					null, null, "", overAllmodalValues);
			IterationKpiData overAllStPoints;
			if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
					&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
				overAllStPoints = new IterationKpiData(CommonConstant.STORY_POINT,
						roundingOff(overAllStoryPoints.get(0)), null, null, CommonConstant.SP, null);
			} else {
				overAllStPoints = new IterationKpiData(CommonConstant.ORIGINAL_ESTIMATE,
						roundingOff(overAllOriginalEstimate.get(0)), null, null, CommonConstant.HOURS, null);
			}
			data.add(overAllCount);
			data.add(overAllStPoints);
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, null, data);
			iterationKpiValues.add(overAllIterationKpiValue);

			// Create kpi level filters
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypes);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, null);
			trendValue.setValue(iterationKpiValues);
			kpiElement.setFilters(iterationKpiFilters);
			kpiElement.setSprint(latestSprint.getName());
			kpiElement.setModalHeads(KPIExcelColumn.CLOSURES_POSSIBLE_TODAY.getColumns());
			kpiElement.setTrendValueList(trendValue);
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
				CalculatePCDHelper.arrangeJiraIssueList(fieldMapping.getJiraStatusForInProgressKPI122(), jiraIssues,
						inProgressIssues, openIssues);
				iterationPotentialDelayList
						.addAll(sprintWiseDelayCalculation(inProgressIssues, openIssues, sprintDetails));
			});
		}

		if (CollectionUtils.isNotEmpty(fieldMapping.getJiraStatusForInProgressKPI122())) {
			List<JiraIssue> inProgressIssues = allIssues.stream()
					.filter(jiraIssue -> (jiraIssue.getAssigneeId() == null)
							&& StringUtils.isNotEmpty(jiraIssue.getDueDate())
							&& (fieldMapping.getJiraStatusForInProgressKPI122().contains(jiraIssue.getStatus())))
					.collect(Collectors.toList());

			List<JiraIssue> openIssues = new ArrayList<>();
			iterationPotentialDelayList.addAll(sprintWiseDelayCalculation(inProgressIssues, openIssues, sprintDetails));
		}
		return iterationPotentialDelayList;
	}

}
