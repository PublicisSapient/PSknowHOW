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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFilters;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFiltersOptions;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalColoumn;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

@Component
public class IssueLikelyToSpillServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(IssueLikelyToSpillServiceImpl.class);

	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String SEARCH_BY_PRIORITY = "Filter by priority";
	public static final String UNCHECKED = "unchecked";
	private static final String ISSUES = "issues";
	private static final String SPRINT_STATE = "sprintState";
	private static final String MODAL_HEAD_ISSUE_ID = "Issue Id";
	private static final String MODAL_HEAD_ISSUE_DESC = "Issue Description";
	private static final String ISSUES_AT_RISK = "Issues at Risk";
	private static final String STORYPOINTS = "Story Point";
	private static final String OVERALL = "Overall";
	private static final String SPRINT_STATE_ACTIVE = "ACTIVE";
	private static final String SPRINT_END_DATE = "endDate";
	private static final String SP = "SP";

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
			LOGGER.info("Issue Likely to Spill -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter()
					.getBasicProjectConfigId().toString();
			String sprintId = leafNode.getSprintFilter().getId();
			SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintId);
			if (null != sprintDetails) {
				List<String> notCompletedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.NOT_COMPLETED_ISSUES);
				if (CollectionUtils.isNotEmpty(notCompletedIssues)) {
					List<JiraIssue> issueList = jiraIssueRepository.findByNumberInAndBasicProjectConfigId(notCompletedIssues,
							basicProjectConfigId);
					resultListMap.put(ISSUES, issueList);
					resultListMap.put(SPRINT_STATE, sprintDetails.getState());
					resultListMap.put(SPRINT_END_DATE, sprintDetails.getEndDate());
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
		if (CollectionUtils.isNotEmpty(allIssues)) {
			LOGGER.info("Issue Likely To Spill -> request id : {} total jira Issues : {}", requestTrackerId,
					allIssues.size());

			Map<String, Map<String, List<JiraIssue>>> typeAndPriorityWiseIssues = allIssues.stream().collect(
					Collectors.groupingBy(JiraIssue::getTypeName, Collectors.groupingBy(JiraIssue::getPriority)));

			Set<String> issueTypes = new HashSet<>();
			Set<String> priorities = new HashSet<>();
			List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
			List<Integer> overAllIssueCount = Arrays.asList(0);
			List<Double> overAllStoryPoints = Arrays.asList(0.0);
			List<Integer> overAllriskIssueCount = Arrays.asList(0);
			String sprintState = (String) resultMap.get(SPRINT_STATE);
			String sprintEndDate = (String) resultMap.get(SPRINT_END_DATE);
			List<Double> timeRemainingInSprintMin = Arrays.asList(0.0);
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(latestSprint.getProjectFilter().getBasicProjectConfigId());
			if (StringUtils.isNotBlank(sprintEndDate)) {
				// +1 to include end date
				long weekDaysSprint = calcWeekDays(LocalDate.now(),
						LocalDate.parse(sprintEndDate.substring(0, sprintEndDate.indexOf('T')))) + 1;
				timeRemainingInSprintMin.set(0, weekDaysSprint * fieldMapping.getWorkingHoursDayCPT() * 60);
			}
			List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
			typeAndPriorityWiseIssues.forEach((issueType, priorityWiseIssue) -> {
				priorityWiseIssue.forEach((priority, issues) -> {
					issueTypes.add(issueType);
					priorities.add(priority);
					List<IterationKpiModalValue> modalValues = new ArrayList<>();
					int issueCount = 0;
					int riskIssueCount = 0;
					Double storyPoint = 0.0;
					for (JiraIssue jiraIssue : issues) {
						issueCount = issueCount + 1;
						overAllIssueCount.set(0, overAllIssueCount.get(0) + 1);
						if (SPRINT_STATE_ACTIVE.equals(sprintState)) {
							Integer timeRemaining = jiraIssue.getRemainingEstimateMinutes();
							if (null != timeRemaining && timeRemaining > timeRemainingInSprintMin.get(0)) {

								riskIssueCount = riskIssueCount + 1;
								overAllriskIssueCount.set(0, overAllriskIssueCount.get(0) + 1);
								IterationKpiModalColoumn iterationKpiModalColoumn = new IterationKpiModalColoumn(
										jiraIssue.getNumber(), jiraIssue.getUrl());
								IterationKpiModalValue iterationKpiModalValue = new IterationKpiModalValue(
										iterationKpiModalColoumn, jiraIssue.getName(), jiraIssue.getStatus(), jiraIssue.getTypeName());
								modalValues.add(iterationKpiModalValue);
								overAllmodalValues.add(iterationKpiModalValue);
								if (null != jiraIssue.getStoryPoints()) {
									storyPoint = storyPoint + jiraIssue.getStoryPoints();
									overAllStoryPoints.set(0, overAllStoryPoints.get(0) + jiraIssue.getStoryPoints());
								}
							}
						} else {
							riskIssueCount = riskIssueCount + 1;
							overAllriskIssueCount.set(0, overAllriskIssueCount.get(0) + 1);
							IterationKpiModalColoumn iterationKpiModalColoumn = new IterationKpiModalColoumn(
									jiraIssue.getNumber(), jiraIssue.getUrl());
							IterationKpiModalValue iterationKpiModalValue = new IterationKpiModalValue(
									iterationKpiModalColoumn, jiraIssue.getName(), jiraIssue.getStatus(), jiraIssue.getTypeName());
							modalValues.add(iterationKpiModalValue);
							overAllmodalValues.add(iterationKpiModalValue);
							if (null != jiraIssue.getStoryPoints()) {
								storyPoint = storyPoint + jiraIssue.getStoryPoints();
								overAllStoryPoints.set(0, overAllStoryPoints.get(0) + jiraIssue.getStoryPoints());
							}
						}
					}
					List<IterationKpiData> data = new ArrayList<>();
					IterationKpiData issueAtRisk = new IterationKpiData(ISSUES_AT_RISK, Double.valueOf(riskIssueCount),
							Double.valueOf(issueCount), null, "", modalValues);
					IterationKpiData issueAtRiskSp = new IterationKpiData(STORYPOINTS, storyPoint, null, null, SP,
							null);
					data.add(issueAtRisk);
					data.add(issueAtRiskSp);
					IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, priority, data);
					iterationKpiValues.add(iterationKpiValue);
				});

			});
			List<IterationKpiData> data = new ArrayList<>();

			IterationKpiData overAllIssuesAtRisk = new IterationKpiData(ISSUES_AT_RISK,
					Double.valueOf(overAllriskIssueCount.get(0)), Double.valueOf(overAllIssueCount.get(0)), null, "",
					overAllmodalValues);
			IterationKpiData overAlllRiskSp = new IterationKpiData(STORYPOINTS, overAllStoryPoints.get(0), null, null,
					SP, null);
			data.add(overAllIssuesAtRisk);
			data.add(overAlllRiskSp);
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
			iterationKpiValues.add(overAllIterationKpiValue);

			// Create kpi level filters
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypes);
			IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, priorities);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
			// Modal Heads Options
			List<String> modalHeads = Arrays.asList(MODAL_HEAD_ISSUE_ID, MODAL_HEAD_ISSUE_DESC, CommonConstant.MODAL_HEAD_ISSUE_STATUS,
					CommonConstant.MODAL_HEAD_ISSUE_TYPE);
			trendValue.setValue(iterationKpiValues);
			kpiElement.setFilters(iterationKpiFilters);
			kpiElement.setSprint(latestSprint.getName());
			kpiElement.setModalHeads(modalHeads);
			kpiElement.setTrendValueList(trendValue);
		}
	}
}
