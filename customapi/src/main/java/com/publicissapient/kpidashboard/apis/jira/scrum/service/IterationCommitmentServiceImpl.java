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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class IterationCommitmentServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	public static final String UNCHECKED = "unchecked";
	public static final String OVERALL_COMMITMENT = "Overall Commitment";
	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String SEARCH_BY_PRIORITY = "Filter by status";
	private static final String PUNTED_ISSUES = "puntedIssues";
	private static final String ADDED_ISSUES = "addedIssues";
	private static final String EXCLUDE_ADDED_ISSUES = "excludeAddedIssues";
	private static final String SCOPE_ADDED = "Scope added";
	private static final String SCOPE_REMOVED = "Scope removed";
	private static final String INITIAL_COMMITMENT = "Initial Commitment";
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
		return KPICode.ITERATION_COMMITMENT.name();
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
			log.info("Scope Change -> Requested sprint : {}", leafNode.getName());
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
						dbSprintDetail, fieldMapping.getJiraIterationIssuetypeKPI120(),
						fieldMapping.getJiraIterationCompletionStatusKPI120(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> puntedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.PUNTED_ISSUES);
				Set<String> addedIssues = sprintDetails.getAddedIssues();
				List<String> completeAndIncompleteIssues = Stream
						.of(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
								CommonConstant.COMPLETED_ISSUES),
								KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
										CommonConstant.NOT_COMPLETED_ISSUES))
						.flatMap(Collection::stream).collect(Collectors.toList());
				// Adding issues which were added before sprint start and later removed form
				// sprint or dropped.
				completeAndIncompleteIssues.addAll(puntedIssues);
				if (CollectionUtils.isNotEmpty(puntedIssues)) {
					List<JiraIssue> filteredPuntedIssueList = IterationKpiHelper.getFilteredJiraIssue(puntedIssues,
							totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getPuntedIssues(), filteredPuntedIssueList);
					resultListMap.put(PUNTED_ISSUES, new ArrayList<>(filtersIssuesList));
				}
				if (CollectionUtils.isNotEmpty(addedIssues)) {
					List<JiraIssue> filterAddedIssueList = IterationKpiHelper
							.getFilteredJiraIssue(new ArrayList<>(addedIssues), totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails, new HashSet<>(),
									filterAddedIssueList);
					resultListMap.put(ADDED_ISSUES, new ArrayList<>(filtersIssuesList));
					completeAndIncompleteIssues.removeAll(new ArrayList<>(addedIssues));
				}
				if (CollectionUtils.isNotEmpty(completeAndIncompleteIssues)) {
					List<JiraIssue> filteredJiraIssue = IterationKpiHelper
							.getFilteredJiraIssue(new ArrayList<>(completeAndIncompleteIssues), totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails, new HashSet<>(),
									filteredJiraIssue);
					resultListMap.put(EXCLUDE_ADDED_ISSUES, new ArrayList<>(filtersIssuesList));
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
		Object basicProjectConfigId = latestSprint.getProjectFilter().getBasicProjectConfigId();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		List<JiraIssue> puntedIssues = (List<JiraIssue>) resultMap.get(PUNTED_ISSUES);
		List<JiraIssue> addedIssues = (List<JiraIssue>) resultMap.get(ADDED_ISSUES);
		List<JiraIssue> initialIssues = (List<JiraIssue>) resultMap.get(EXCLUDE_ADDED_ISSUES);
		List<JiraIssue> totalIssues = new ArrayList<>();
		Set<String> issueTypes = new HashSet<>();
		Set<String> statuses = new HashSet<>();
		List<IterationKpiModalValue> overAllAddmodalValues = new ArrayList<>();
		List<IterationKpiModalValue> overAllRemovedmodalValues = new ArrayList<>();
		List<IterationKpiModalValue> overAllInitialmodalValues = new ArrayList<>();
		List<IterationKpiModalValue> overAllTotalmodalValues = new ArrayList<>();

		List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
		List<IterationKpiData> data = new ArrayList<>();
		// for totalIssue adding initialIssues + addedIssues - puntedIssues
		if (CollectionUtils.isNotEmpty(initialIssues)) {
			totalIssues.addAll(initialIssues);
		}
		if (CollectionUtils.isNotEmpty(addedIssues)) {
			totalIssues.addAll(addedIssues);
		}
		if (CollectionUtils.isNotEmpty(puntedIssues)) {
			totalIssues.removeAll(puntedIssues);
		}

		if (CollectionUtils.isNotEmpty(totalIssues)) {
			log.info("Scope Change -> request id : {} total jira Issues : {}", requestTrackerId, totalIssues.size());
			List<Integer> overAllTotalIssueCount = Arrays.asList(0);
			List<Double> overAllTotalIssueSp = Arrays.asList(0.0);
			List<Double> overAllTotalOriginalEstimate = Arrays.asList(0.0);
			setScopeChange(issueTypes, statuses, totalIssues, iterationKpiValues, overAllTotalIssueCount,
					overAllTotalIssueSp, overAllTotalmodalValues, OVERALL_COMMITMENT, fieldMapping,
					overAllTotalOriginalEstimate);
			IterationKpiData overAllTotalCount = setIterationKpiData(fieldMapping, overAllTotalIssueCount,
					overAllTotalIssueSp, overAllTotalOriginalEstimate, overAllTotalmodalValues, OVERALL_COMMITMENT);
			data.add(overAllTotalCount);
		}

		if (CollectionUtils.isNotEmpty(initialIssues)) {
			log.info("Scope Change -> request id : {} initial jira Issues : {}", requestTrackerId,
					initialIssues.size());
			List<Integer> overAllInitialIssueCount = Arrays.asList(0);
			List<Double> overAllInitialIssueSp = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimate = Arrays.asList(0.0);
			setScopeChange(issueTypes, statuses, initialIssues, iterationKpiValues, overAllInitialIssueCount,
					overAllInitialIssueSp, overAllInitialmodalValues, INITIAL_COMMITMENT, fieldMapping,
					overAllOriginalEstimate);
			IterationKpiData overAllInitialCount = setIterationKpiData(fieldMapping, overAllInitialIssueCount,
					overAllInitialIssueSp, overAllOriginalEstimate, overAllInitialmodalValues, INITIAL_COMMITMENT);
			data.add(overAllInitialCount);
		}

		if (CollectionUtils.isNotEmpty(addedIssues)) {
			log.info("Scope Change -> request id : {} added jira Issues : {}", requestTrackerId, addedIssues.size());
			List<Integer> overAllAddedIssueCount = Arrays.asList(0);
			List<Double> overAllAddedIssueSp = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimate = Arrays.asList(0.0);
			setScopeChange(issueTypes, statuses, addedIssues, iterationKpiValues, overAllAddedIssueCount,
					overAllAddedIssueSp, overAllAddmodalValues, SCOPE_ADDED, fieldMapping, overAllOriginalEstimate);
			IterationKpiData overAllAddedCount = setIterationKpiData(fieldMapping, overAllAddedIssueCount,
					overAllAddedIssueSp, overAllOriginalEstimate, overAllAddmodalValues, SCOPE_ADDED);
			data.add(overAllAddedCount);
		}

		if (CollectionUtils.isNotEmpty(puntedIssues)) {
			log.info("Scope Change -> request id : {} punted jira Issues : {}", requestTrackerId, puntedIssues.size());
			List<Integer> overAllPunIssueCount = Arrays.asList(0);
			List<Double> overAllPunIssueSp = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimate = Arrays.asList(0.0);
			setScopeChange(issueTypes, statuses, puntedIssues, iterationKpiValues, overAllPunIssueCount,
					overAllPunIssueSp, overAllRemovedmodalValues, SCOPE_REMOVED, fieldMapping, overAllOriginalEstimate);
			IterationKpiData overAllPuntedCount = setIterationKpiData(fieldMapping, overAllPunIssueCount,
					overAllPunIssueSp, overAllOriginalEstimate, overAllRemovedmodalValues, SCOPE_REMOVED);
			data.add(overAllPuntedCount);
		}

		if (CollectionUtils.isNotEmpty(data)) {
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
			iterationKpiValues.add(overAllIterationKpiValue);

			// Create kpi level filters
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypes);
			IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, statuses);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
			trendValue.setValue(iterationKpiValues);
			kpiElement.setFilters(iterationKpiFilters);
			kpiElement.setSprint(latestSprint.getName());
			kpiElement.setModalHeads(KPIExcelColumn.ITERATION_COMMITMENT.getColumns());
		}
		kpiElement.setTrendValueList(trendValue);
	}

	private void setScopeChange(Set<String> issueTypes, Set<String> statuses, List<JiraIssue> allIssues,
			List<IterationKpiValue> iterationKpiValues, List<Integer> overAllIssueCount, List<Double> overAllIssueSp,
			List<IterationKpiModalValue> overAllmodalValues, String label, FieldMapping fieldMapping,
			List<Double> overAllOriginalEstimate) {
		Map<String, Map<String, List<JiraIssue>>> typeAndStatusWiseIssues = allIssues.stream()
				.collect(Collectors.groupingBy(JiraIssue::getTypeName, Collectors.groupingBy(JiraIssue::getStatus)));
		// Creating map of modal Objects
		Map<String, IterationKpiModalValue> modalObjectMap = KpiDataHelper.createMapOfModalObject(allIssues);
		typeAndStatusWiseIssues.forEach((issueType, statusWiseIssue) -> statusWiseIssue.forEach((status, issues) -> {
			issueTypes.add(issueType);
			statuses.add(status);
			List<IterationKpiModalValue> modalValues = new ArrayList<>();
			int issueCount = 0;
			double storyPoints = 0;
			Double originalEstimate = 0.0;
			for (JiraIssue jiraIssue : issues) {
				KPIExcelUtility.populateIterationKPI(overAllmodalValues, modalValues, jiraIssue, fieldMapping,
						modalObjectMap);
				issueCount = issueCount + 1;
				if (null != jiraIssue.getStoryPoints()) {
					storyPoints = storyPoints + jiraIssue.getStoryPoints();
					overAllIssueSp.set(0, overAllIssueSp.get(0) + jiraIssue.getStoryPoints());
				}
				if (null != jiraIssue.getOriginalEstimateMinutes()) {
					originalEstimate = originalEstimate + jiraIssue.getOriginalEstimateMinutes();
					overAllOriginalEstimate.set(0,
							overAllOriginalEstimate.get(0) + jiraIssue.getOriginalEstimateMinutes());
				}
				overAllIssueCount.set(0, overAllIssueCount.get(0) + 1);
			}
			List<IterationKpiData> data = new ArrayList<>();
			IterationKpiData issueCounts;
			if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
					&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
				issueCounts = new IterationKpiData(label, Double.valueOf(issueCount), roundingOff(storyPoints), null,
						"", CommonConstant.SP, modalValues);
			} else {
				issueCounts = new IterationKpiData(label, Double.valueOf(issueCount), roundingOff(originalEstimate),
						null, "", CommonConstant.DAY, modalValues);
			}
			data.add(issueCounts);
			IterationKpiValue matchingObject = iterationKpiValues.stream()
					.filter(p -> p.getFilter1().equals(issueType) && p.getFilter2().equals(status)).findAny()
					.orElse(null);
			if (null == matchingObject) {
				IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, status, data);
				iterationKpiValues.add(iterationKpiValue);
			} else {
				matchingObject.getData().addAll(data);
			}
		}));
	}

	private IterationKpiData setIterationKpiData(FieldMapping fieldMapping, List<Integer> overAllIssueCount,
			List<Double> overAllIssueSp, List<Double> overAllOriginalEstimate,
			List<IterationKpiModalValue> overAllModalValues, String kpiLabel) {
		if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			return new IterationKpiData(kpiLabel, Double.valueOf(overAllIssueCount.get(0)),
					roundingOff(overAllIssueSp.get(0)), null, "", CommonConstant.SP, overAllModalValues);
		} else {
			return new IterationKpiData(kpiLabel, Double.valueOf(overAllIssueCount.get(0)),
					roundingOff(overAllOriginalEstimate.get(0)), null, "", CommonConstant.DAY, overAllModalValues);
		}
	}

}
