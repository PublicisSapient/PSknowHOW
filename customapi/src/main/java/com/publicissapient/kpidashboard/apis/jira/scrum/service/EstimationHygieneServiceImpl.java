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
public class EstimationHygieneServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	public static final String UNCHECKED = "unchecked";
	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String ISSUES = "issues";
	private static final String ISSUES_WITHOUT_ESTIMATES = "Issue without estimates";
	private static final String ISSUES_MISSING_WORKLOGS = "Issue with missing worklogs";
	private static final String OVERALL = "Overall";
	@Autowired
	ConfigHelperService configHelperService;

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
		return KPICode.ESTIMATION_HYGIENE.name();
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
			log.info("Estimation Hygiene -> Requested sprint : {}", leafNode.getName());
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
						dbSprintDetail, fieldMapping.getJiraIterationIssuetypeKPI124(),
						fieldMapping.getJiraIterationCompletionStatusKPI124(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> totalIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.TOTAL_ISSUES);
				if (CollectionUtils.isNotEmpty(totalIssues)) {
					List<JiraIssue> jiraIssueList = IterationKpiHelper.getFilteredJiraIssue(totalIssues,
							totalJiraIssueList);
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
			log.info("Estimation Hygiene -> request id : {} total jira Issues : {}", requestTrackerId,
					allIssues.size());
			// Creating map of modal Objects
			Map<String, IterationKpiModalValue> modalObjectMap = KpiDataHelper.createMapOfModalObject(allIssues);
			Map<String, List<JiraIssue>> typeWiseIssues = allIssues.stream()
					.collect(Collectors.groupingBy(JiraIssue::getTypeName));

			Set<String> issueTypes = new HashSet<>();
			List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
			List<Integer> overAllIssueCount = Arrays.asList(0);
			List<Integer> overAllWithoutEstimate = Arrays.asList(0);
			List<Integer> overAllMissingLog = Arrays.asList(0);

			List<IterationKpiModalValue> overAllWithoutEstmodalValues = new ArrayList<>();
			List<IterationKpiModalValue> overAllMissingModalValues = new ArrayList<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(Objects.requireNonNull(latestSprint).getProjectFilter().getBasicProjectConfigId());
			typeWiseIssues.forEach((issueType, issues) -> {
				issueTypes.add(issueType);
				List<IterationKpiModalValue> withoutEstmodalValues = new ArrayList<>();
				List<IterationKpiModalValue> missingmodalValues = new ArrayList<>();
				int issueCount = 0;
				int issueWithoutEstimate = 0;
				int issueMissingLog = 0;

				for (JiraIssue jiraIssue : issues) {
					issueCount++;

					overAllIssueCount.set(0, overAllIssueCount.get(0) + 1);
					if (jiraIssue.getEstimate() == null || Double.valueOf(jiraIssue.getEstimate()).equals(0.0)) {
						issueWithoutEstimate++;
						overAllWithoutEstimate.set(0, overAllWithoutEstimate.get(0) + 1);
						// set modal values
						KPIExcelUtility.populateIterationKPI(withoutEstmodalValues, overAllWithoutEstmodalValues,
								jiraIssue, fieldMapping, modalObjectMap);
					}

					if ((jiraIssue.getTimeSpentInMinutes() == null || jiraIssue.getTimeSpentInMinutes() == 0)
							&& !checkStatus(jiraIssue, fieldMapping)) {
						issueMissingLog++;
						overAllMissingLog.set(0, overAllMissingLog.get(0) + 1);
						// set modal values
						KPIExcelUtility.populateIterationKPI(missingmodalValues, overAllMissingModalValues, jiraIssue,
								fieldMapping, modalObjectMap);
					}

				}
				List<IterationKpiData> data = new ArrayList<>();
				IterationKpiData issueAtRisk = new IterationKpiData(ISSUES_WITHOUT_ESTIMATES,
						Double.valueOf(issueWithoutEstimate), Double.valueOf(issueCount), null, "",
						withoutEstmodalValues);
				IterationKpiData missingWorkLog = new IterationKpiData(ISSUES_MISSING_WORKLOGS,
						Double.valueOf(issueMissingLog), Double.valueOf(issueCount), null, "", missingmodalValues);
				data.add(issueAtRisk);
				data.add(missingWorkLog);
				IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, null, data);
				iterationKpiValues.add(iterationKpiValue);

			});
			List<IterationKpiData> data = new ArrayList<>();

			IterationKpiData overAllWithouEst = new IterationKpiData(ISSUES_WITHOUT_ESTIMATES,
					Double.valueOf(overAllWithoutEstimate.get(0)), Double.valueOf(overAllIssueCount.get(0)), null, "",
					overAllWithoutEstmodalValues);
			IterationKpiData overAllMissWorkLog = new IterationKpiData(ISSUES_MISSING_WORKLOGS,
					Double.valueOf(overAllMissingLog.get(0)), Double.valueOf(overAllIssueCount.get(0)), null, "",
					overAllMissingModalValues);
			data.add(overAllWithouEst);
			data.add(overAllMissWorkLog);
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
			iterationKpiValues.add(overAllIterationKpiValue);

			// Create kpi level filters
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypes);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, null);
			trendValue.setValue(iterationKpiValues);
			kpiElement.setFilters(iterationKpiFilters);
			kpiElement.setSprint(latestSprint.getName());
			kpiElement.setModalHeads(KPIExcelColumn.ESTIMATE_HYGINE.getColumns());
			kpiElement.setTrendValueList(trendValue);
		}
	}

	private boolean checkStatus(JiraIssue jiraIssue, FieldMapping fieldMapping) {

		boolean toDrop = false;
		if (null != fieldMapping && CollectionUtils.isNotEmpty(fieldMapping.getIssueStatusExcluMissingWorkKPI124())) {
			toDrop = fieldMapping.getIssueStatusExcluMissingWorkKPI124().stream().map(String::toUpperCase)
					.collect(Collectors.toList()).contains(jiraIssue.getJiraStatus().toUpperCase());
		}
		return toDrop;
	}

}
