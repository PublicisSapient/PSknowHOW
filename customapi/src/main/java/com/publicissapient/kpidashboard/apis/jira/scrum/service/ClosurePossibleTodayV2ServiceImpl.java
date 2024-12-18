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
import java.util.*;
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
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.CalculatePCDHelper;
import com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard.JiraIterationKPIService;
import com.publicissapient.kpidashboard.apis.model.*;
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
public class ClosurePossibleTodayV2ServiceImpl extends JiraIterationKPIService {

	public static final String UNCHECKED = "unchecked";
	private static final Logger LOGGER = LoggerFactory.getLogger(ClosurePossibleTodayV2ServiceImpl.class);
	private static final String FILTER_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String ISSUES = "issues";
	private static final String ISSUE_COUNT = "Issue Count";
	private static final String FILTER_TYPE = "Multi";
	private static final String SPRINT_DETAILS = "sprint details";

	@Autowired
	private ConfigHelperService configHelperService;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node sprintNode)
			throws ApplicationException {
		projectWiseLeafNodeValue(sprintNode, kpiElement, kpiRequest);
		return kpiElement;
	}

	@Override
	public String getQualifierType() {
		return KPICode.CLOSURE_POSSIBLE_TODAY.name();
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		if (null != leafNode) {
			LOGGER.info("Closure Possible Today -> Requested sprint : {}", leafNode.getName());
			SprintDetails dbSprintDetail = getSprintDetailsFromBaseClass();
			SprintDetails sprintDetail;
			if (null != dbSprintDetail) {
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
						.get(leafNode.getProjectFilter().getBasicProjectConfigId());
				// to modify sprint details on the basis of configuration for the project
				List<JiraIssueCustomHistory> totalHistoryList = getJiraIssuesCustomHistoryFromBaseClass();
				List<JiraIssue> totalJiraIssueList = getJiraIssuesFromBaseClass();
				Set<String> issueList = totalJiraIssueList.stream().map(JiraIssue::getNumber)
						.collect(Collectors.toSet());

				sprintDetail = IterationKpiHelper.transformIterSprintdetail(totalHistoryList, issueList, dbSprintDetail,
						fieldMapping.getJiraIterationIssuetypeKPI122(),
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
	 * @param latestSprintNode
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(Node latestSprintNode, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
				.get(Objects.requireNonNull(latestSprintNode).getProjectFilter().getBasicProjectConfigId());

		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT_DETAILS);
		Set<IssueKpiModalValue> issueData = new HashSet<>();
		if (CollectionUtils.isNotEmpty(allIssues)) {
			LOGGER.info("Closure Possible Today -> request id : {} total jira Issues : {}", requestTrackerId,
					allIssues.size());
			List<IterationPotentialDelay> iterationPotentialDelayList = calculatePotentialDelay(sprintDetails,
					allIssues, fieldMapping);
			Map<String, IterationPotentialDelay> issueWiseDelay = iterationPotentialDelayList.stream()
					.collect(Collectors.toMap(IterationPotentialDelay::getIssueId, Function.identity(), (e1, e2) -> e2,
							LinkedHashMap::new));
			// Creating map of modal Objects
			Map<String, IssueKpiModalValue> issueKpiModalObject = KpiDataHelper.createMapOfIssueModal(allIssues);
			allIssues.forEach(issue -> {
				if (issueWiseDelay.containsKey(issue.getNumber()) && issueWiseDelay.get(issue.getNumber())
						.getPredictedCompletedDate().equals(LocalDate.now().toString())) {
					KPIExcelUtility.populateIssueModal(issue, fieldMapping, issueKpiModalObject);
					IssueKpiModalValue data = issueKpiModalObject.get(issue.getNumber());
					data.setValue(0.0);
					if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
							&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
						data.setValue(issue.getStoryPoints());
					} else if (null != issue.getOriginalEstimateMinutes()) {
						data.setValue(Double.valueOf(issue.getOriginalEstimateMinutes()));
					}
					issueData.add(data);
				}
			});

			kpiElement.setSprint(latestSprintNode.getName());
			kpiElement.setModalHeads(KPIExcelColumn.CLOSURES_POSSIBLE_TODAY.getColumns());
			kpiElement.setIssueData(issueData);
			kpiElement.setFilterGroup(createFilterGroup());
			kpiElement.setDataGroup(createDataGroup(fieldMapping));
		}
	}

	/**
	 * Creates filter group.
	 * 
	 * @return
	 */
	private FilterGroup createFilterGroup() {
		FilterGroup filterGroup = new FilterGroup();

		List<Filter> filterList = new ArrayList<>();
		filterList.add(createFilter(FILTER_TYPE, FILTER_BY_ISSUE_TYPE, "Issue Type", 1));
		filterGroup.setFilterGroup1(filterList);

		return filterGroup;
	}

	/**
	 * Creates individual filter object.
	 * 
	 * @param type
	 * @param name
	 * @param key
	 * @param order
	 * @return
	 */
	private Filter createFilter(String type, String name, String key, Integer order) {
		Filter filter = new Filter();
		filter.setFilterType(type);
		filter.setFilterName(name);
		filter.setFilterKey(key);
		filter.setOrder(order);
		return filter;
	}

	/**
	 * Creates data group that tells what kind of data will be shown on chart.
	 * 
	 * @param fieldMapping
	 * @return
	 */
	private KpiDataGroup createDataGroup(FieldMapping fieldMapping) {
		KpiDataGroup dataGroup = new KpiDataGroup();

		String unit;
		String displayName;
		if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			unit = CommonConstant.SP;
			displayName = CommonConstant.STORY_POINT;
		} else {
			unit = CommonConstant.DAY;
			displayName = CommonConstant.ORIGINAL_ESTIMATE;
		}

		List<KpiData> dataGroup1 = new ArrayList<>();
		dataGroup1.add(createKpiData("", ISSUE_COUNT, 1, "count", ""));
		dataGroup1.add(createKpiData("Value", displayName, 2, "sum", unit));

		dataGroup.setDataGroup1(dataGroup1);
		return dataGroup;
	}

	/**
	 * Creates kpi data object.
	 * 
	 * @param key
	 * @param name
	 * @param order
	 * @param aggregation
	 * @param unit
	 * @return
	 */
	private KpiData createKpiData(String key, String name, Integer order, String aggregation, String unit) {
		KpiData data = new KpiData();
		data.setKey(key);
		data.setName(name);
		data.setOrder(order);
		data.setAggregation(aggregation);
		data.setUnit(unit);
		data.setShowAsLegend(false);
		return data;
	}

	/**
	 * with assignees criteria calculating potential delay for in progress and open
	 * issues and without assignees calculating potential delay for in progress
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
