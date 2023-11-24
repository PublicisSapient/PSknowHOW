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
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
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
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EstimateVsActualServiceImpl extends JiraIterationKPIService {

	public static final String UNCHECKED = "unchecked";
	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String ISSUES = "issues";
	private static final String ORIGINAL_ESTIMATES = "Original Estimates";
	private static final String LOGGED_WORK = "Logged Work";
	private static final String OVERALL = "Overall";
	private static final String HOURS = "Hours";
	@Autowired
	private ConfigHelperService configHelperService;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node sprintNode)
			throws ApplicationException {
		if (Filters.getFilter(sprintNode.getGroupName()) == Filters.SPRINT) {
			projectWiseLeafNodeValue(sprintNode, kpiElement, kpiRequest);
		}
		return kpiElement;
	}

	@Override
	public String getQualifierType() {
		return KPICode.ESTIMATE_VS_ACTUAL.name();
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		if (null != leafNode) {
			log.info("Estimate Vs Actual -> Requested sprint : {}", leafNode.getName());
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

				sprintDetails = transformIterSprintdetail(totalHistoryList, issueList, dbSprintDetail,
						fieldMapping.getJiraIterationIssuetypeKPI75(),
						fieldMapping.getJiraIterationCompletionStatusKPI75(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> totalIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.TOTAL_ISSUES);
				if (CollectionUtils.isNotEmpty(totalIssues)) {
					List<JiraIssue> jiraIssueList = getFilteredJiraIssue(totalIssues, totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getTotalIssues(), jiraIssueList);
					resultListMap.put(ISSUES, new ArrayList<>(filtersIssuesList));
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
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(Node sprintLeafNode, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNode, null, null, kpiRequest);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		if (CollectionUtils.isNotEmpty(allIssues)) {
			log.info("Estimate Vs Actual -> request id : {} total jira Issues : {}", requestTrackerId,
					allIssues.size());
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(Objects.requireNonNull(sprintLeafNode).getProjectFilter().getBasicProjectConfigId());
			// Creating map of modal Objects
			Map<String, IterationKpiModalValue> modalObjectMap = KpiDataHelper.createMapOfModalObject(allIssues);
			Map<String, List<JiraIssue>> typeWiseIssues = allIssues.stream()
					.collect(Collectors.groupingBy(JiraIssue::getTypeName));

			Set<String> issueTypes = new HashSet<>();
			List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
			List<Integer> overAllOrigEst = Arrays.asList(0);
			List<Integer> overAllLogWork = Arrays.asList(0);
			List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
			typeWiseIssues.forEach((issueType, issues) -> {
				issueTypes.add(issueType);
				List<IterationKpiModalValue> modalValues = new ArrayList<>();
				int origEstData = 0;
				int logWorkData = 0;

				for (JiraIssue jiraIssue : issues) {
					KPIExcelUtility.populateIterationKPI(overAllmodalValues, modalValues, jiraIssue, fieldMapping,
							modalObjectMap);
					if (null != jiraIssue.getOriginalEstimateMinutes()) {
						origEstData = origEstData + jiraIssue.getOriginalEstimateMinutes();
						overAllOrigEst.set(0, overAllOrigEst.get(0) + jiraIssue.getOriginalEstimateMinutes());
					}
					if (null != jiraIssue.getTimeSpentInMinutes()) {
						logWorkData = logWorkData + jiraIssue.getTimeSpentInMinutes();
						overAllLogWork.set(0, overAllLogWork.get(0) + jiraIssue.getTimeSpentInMinutes());
					}
				}
				List<IterationKpiData> data = new ArrayList<>();
				IterationKpiData originalEstimates = new IterationKpiData(ORIGINAL_ESTIMATES,
						Double.valueOf(origEstData), null, null, HOURS, modalValues);
				IterationKpiData loggedWork = new IterationKpiData(LOGGED_WORK, Double.valueOf(logWorkData), null, null,
						HOURS, null);
				data.add(originalEstimates);
				data.add(loggedWork);
				IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, null, data);
				iterationKpiValues.add(iterationKpiValue);

			});
			List<IterationKpiData> data = new ArrayList<>();

			IterationKpiData overAllorigEstimates = new IterationKpiData(ORIGINAL_ESTIMATES,
					Double.valueOf(overAllOrigEst.get(0)), null, null, HOURS, overAllmodalValues);
			IterationKpiData overAllloggedWork = new IterationKpiData(LOGGED_WORK,
					Double.valueOf(overAllLogWork.get(0)), null, null, HOURS, null);
			data.add(overAllorigEstimates);
			data.add(overAllloggedWork);
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
			iterationKpiValues.add(overAllIterationKpiValue);

			// Create kpi level filters
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypes);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, null);
			kpiElement.setFilters(iterationKpiFilters);
			kpiElement.setSprint(sprintLeafNode.getName());
			kpiElement.setModalHeads(KPIExcelColumn.ESTIMATE_VS_ACTUAL.getColumns());
			kpiElement.setTrendValueList(iterationKpiValues);
		}
	}

}
