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
import java.util.Optional;
import java.util.Set;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
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
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

@Component
public class ScopeChangeServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScopeChangeServiceImpl.class);

	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String SEARCH_BY_PRIORITY = "Filter by status";
	public static final String UNCHECKED = "unchecked";
	private static final String PUNTED_ISSUES = "puntedIssues";
	private static final String ADDED_ISSUES = "addedIssues";
	private static final String EXCLUDE_ADDED_ISSUES = "excludeAddedIssues";
	private static final String SCOPE_ADDED = "Scope added";
	private static final String LABEL_INFO = "(Issue Count/Story Points)";
	private static final String LABEL_INFO_FOR_ORIGINAL_ESTIMATE = "(Issue Count/Original Estimate)";
	private static final String SCOPE_REMOVED = "Scope removed";
	private static final String ITERATION_COMMITMENT = "Iteration Commitment";
	private static final String OVERALL = "Overall";

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private SprintRepository sprintRepository;

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
			LOGGER.info("Scope Change -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter()
					.getBasicProjectConfigId().toString();
			String sprintId = leafNode.getSprintFilter().getId();
			SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintId);
			if (null != sprintDetails) {
				List<String> puntedIssues =  KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.PUNTED_ISSUES);
				Set<String> addedIssues = sprintDetails.getAddedIssues();
				List<String> completeAndIncompleteIssues = Stream
						.of(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
								CommonConstant.COMPLETED_ISSUES),
								KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
										CommonConstant.NOT_COMPLETED_ISSUES))
						.flatMap(Collection::stream).collect(Collectors.toList());

				if (CollectionUtils.isNotEmpty(puntedIssues)) {
					List<JiraIssue> issueList = jiraIssueRepository.findByNumberInAndBasicProjectConfigId(puntedIssues,
							basicProjectConfigId);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getPuntedIssues(), issueList);
					resultListMap.put(PUNTED_ISSUES, new ArrayList<>(filtersIssuesList));
				}
				if (CollectionUtils.isNotEmpty(addedIssues)) {
					List<JiraIssue> issueList = jiraIssueRepository.findByNumberInAndBasicProjectConfigId(new ArrayList<>(addedIssues),
							basicProjectConfigId);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									new HashSet<>(), issueList);
					resultListMap.put(ADDED_ISSUES, new ArrayList<>(filtersIssuesList));
					completeAndIncompleteIssues.removeAll(new ArrayList<>(addedIssues));
				}
				if (CollectionUtils.isNotEmpty(completeAndIncompleteIssues)) {
					List<JiraIssue> issueList = jiraIssueRepository.findByNumberInAndBasicProjectConfigId(
							new ArrayList<>(completeAndIncompleteIssues), basicProjectConfigId);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails, new HashSet<>(),
									issueList);
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
		Set<String> issueTypes = new HashSet<>();
		Set<String> statuses = new HashSet<>();
		List<IterationKpiModalValue> overAllAddmodalValues = new ArrayList<>();
		List<IterationKpiModalValue> overAllRemovedmodalValues = new ArrayList<>();
		List<IterationKpiModalValue> overAllInitialmodalValues = new ArrayList<>();

		List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
		List<IterationKpiData> data = new ArrayList<>();

		if(CollectionUtils.isNotEmpty(initialIssues)) {
			LOGGER.info("Scope Change -> request id : {} initial jira Issues : {}", requestTrackerId, initialIssues.size());
			Map<String, Map<String, List<JiraIssue>>> typeAndStatusWiseInitialIssues = initialIssues.stream().collect(
					Collectors.groupingBy(JiraIssue::getTypeName, Collectors.groupingBy(JiraIssue::getStatus)));
			List<Integer> overAllInitialIssueCount = Arrays.asList(0);
			List<Double> overAllInitialIssueSp = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimate = Arrays.asList(0.0);
			setScopeChange(issueTypes, statuses, typeAndStatusWiseInitialIssues, iterationKpiValues,
					overAllInitialIssueCount, overAllInitialIssueSp, overAllInitialmodalValues, ITERATION_COMMITMENT,
					fieldMapping, overAllOriginalEstimate, addedIssues);
			IterationKpiData overAllInitialCount = StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria()) &&
					fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)
					? new IterationKpiData(ITERATION_COMMITMENT, Double.valueOf(overAllInitialIssueCount.get(0)),
					overAllInitialIssueSp.get(0), LABEL_INFO, "", overAllInitialmodalValues)
					: new IterationKpiData(ITERATION_COMMITMENT, Double.valueOf(overAllInitialIssueCount.get(0)),
					overAllOriginalEstimate.get(0) / 60, LABEL_INFO_FOR_ORIGINAL_ESTIMATE, "", overAllInitialmodalValues);
			data.add(overAllInitialCount);

		}

		if (CollectionUtils.isNotEmpty(addedIssues)) {
			LOGGER.info("Scope Change -> request id : {} added jira Issues : {}", requestTrackerId, addedIssues.size());
			Map<String, Map<String, List<JiraIssue>>> typeAndStatusWiseAddedIssues = addedIssues.stream().collect(
					Collectors.groupingBy(JiraIssue::getTypeName, Collectors.groupingBy(JiraIssue::getStatus)));
			List<Integer> overAllAddedIssueCount = Arrays.asList(0);
			List<Double> overAllAddedIssueSp = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimate = Arrays.asList(0.0);
			setScopeChange(issueTypes, statuses, typeAndStatusWiseAddedIssues, iterationKpiValues,
					overAllAddedIssueCount, overAllAddedIssueSp, overAllAddmodalValues, SCOPE_ADDED,
					fieldMapping, overAllOriginalEstimate, addedIssues);
			IterationKpiData overAllAddedCount;
			if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria()) &&
					fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
				overAllAddedCount = new IterationKpiData(SCOPE_ADDED, Double.valueOf(overAllAddedIssueCount.get(0))
						, overAllAddedIssueSp.get(0), LABEL_INFO, "", overAllAddmodalValues);
			} else {
				overAllAddedCount = new IterationKpiData(SCOPE_ADDED, Double.valueOf(overAllAddedIssueCount.get(0)),
						overAllOriginalEstimate.get(0)/60, LABEL_INFO_FOR_ORIGINAL_ESTIMATE, "", overAllAddmodalValues);
			}
			data.add(overAllAddedCount);
		}
		if (CollectionUtils.isNotEmpty(puntedIssues)) {
			LOGGER.info("Scope Change -> request id : {} punted jira Issues : {}", requestTrackerId,
					puntedIssues.size());
			Map<String, Map<String, List<JiraIssue>>> typeAndStatusWisePuntedIssues = puntedIssues.stream().collect(
					Collectors.groupingBy(JiraIssue::getTypeName, Collectors.groupingBy(JiraIssue::getStatus)));
			List<Integer> overAllPunIssueCount = Arrays.asList(0);
			List<Double> overAllPunIssueSp = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimate = Arrays.asList(0.0);
			setScopeChange(issueTypes, statuses, typeAndStatusWisePuntedIssues, iterationKpiValues,
					overAllPunIssueCount, overAllPunIssueSp, overAllRemovedmodalValues, SCOPE_REMOVED,
					fieldMapping, overAllOriginalEstimate, addedIssues);
			IterationKpiData overAllPuntedCount;
			if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria()) &&
					fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
				overAllPuntedCount = new IterationKpiData(SCOPE_REMOVED, Double.valueOf(overAllPunIssueCount.get(0))
						, overAllPunIssueSp.get(0), LABEL_INFO, "", overAllRemovedmodalValues);
			} else {
				overAllPuntedCount = new IterationKpiData(SCOPE_REMOVED, Double.valueOf(overAllPunIssueCount.get(0)),
						overAllOriginalEstimate.get(0)/60, LABEL_INFO_FOR_ORIGINAL_ESTIMATE, "",
						overAllRemovedmodalValues);
			}
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

	private void setScopeChange(Set<String> issueTypes, Set<String> statuses,
			Map<String, Map<String, List<JiraIssue>>> typeAndStatusWiseIssues,
			List<IterationKpiValue> iterationKpiValues, List<Integer> overAllIssueCount, List<Double> overAllIssueSp,
			List<IterationKpiModalValue> overAllmodalValues, String label,
								FieldMapping fieldMapping, List<Double> overAllOriginalEstimate, List<JiraIssue> addedIssues) {
		typeAndStatusWiseIssues.forEach((issueType, statusWiseIssue) ->
			statusWiseIssue.forEach((status, issues) -> {
				issueTypes.add(issueType);
				statuses.add(status);
				List<IterationKpiModalValue> modalValues = new ArrayList<>();
				int issueCount = 0;
				double storyPoints = 0;
				Double originalEstimate = 0.0;
				for (JiraIssue jiraIssue : issues) {
					String marker = CollectionUtils.isNotEmpty(addedIssues) && addedIssues.contains(jiraIssue)
							? CommonConstant.RED
							: null;
					populateIterationData(overAllmodalValues, modalValues, jiraIssue, false, null, marker);
					issueCount = issueCount + 1;
					if (null != jiraIssue.getStoryPoints()) {
						storyPoints = storyPoints + jiraIssue.getStoryPoints();
						overAllIssueSp.set(0, overAllIssueSp.get(0) + jiraIssue.getStoryPoints());
					}
					if (null != jiraIssue.getOriginalEstimateMinutes()) {
						originalEstimate = originalEstimate + jiraIssue.getOriginalEstimateMinutes();
						overAllOriginalEstimate.set(0, overAllOriginalEstimate.get(0) + jiraIssue.getOriginalEstimateMinutes());
					}
					overAllIssueCount.set(0, overAllIssueCount.get(0) + 1);
				}
				List<IterationKpiData> data = new ArrayList<>();
				IterationKpiData issueCounts;
				if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria()) &&
						fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
					issueCounts = new IterationKpiData(label, Double.valueOf(issueCount), storyPoints,
							LABEL_INFO, "", modalValues);
				} else {
					issueCounts = new IterationKpiData(label, Double.valueOf(issueCount),
							overAllOriginalEstimate.get(0)/60,
							LABEL_INFO_FOR_ORIGINAL_ESTIMATE, "", modalValues);
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


}
