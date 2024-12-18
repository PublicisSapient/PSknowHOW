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

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard.JiraIterationKPIService;
import com.publicissapient.kpidashboard.apis.model.*;
import com.publicissapient.kpidashboard.apis.util.IterationKpiHelper;
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
public class IssueHygieneV2ServiceImpl extends JiraIterationKPIService {

	public static final String UNCHECKED = "unchecked";
	private static final String FILTER_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String ISSUES = "issues";
	private static final String ISSUES_WITHOUT_ESTIMATES = "Issue without estimates";
	private static final String ISSUES_WITH_MISSING_WORKLOGS = "Issue with missing worklogs";
	private static final String FILTER_TYPE = "Multi";
	@Autowired
	ConfigHelperService configHelperService;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node sprintNode)
			throws ApplicationException {
		projectWiseLeafNodeValue(sprintNode, kpiElement, kpiRequest);
		return kpiElement;
	}

	@Override
	public String getQualifierType() {
		return KPICode.ISSUE_HYGIENE.name();
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		if (null != leafNode) {
			log.info("Estimation Hygiene -> Requested sprint : {}", leafNode.getName());
			SprintDetails dbSprintDetail = getSprintDetailsFromBaseClass();
			SprintDetails sprintDetails;
			if (null != dbSprintDetail) {
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
						.get(leafNode.getProjectFilter().getBasicProjectConfigId());
				// to modify sprint details on the basis of configuration for the project
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
					if (CollectionUtils.isNotEmpty(fieldMapping.getJiraIssueTypeExcludeKPI124())) {
						Set<String> defectTypeSet = fieldMapping.getJiraIssueTypeExcludeKPI124().stream()
								.map(String::toLowerCase).collect(Collectors.toSet());
						filtersIssuesList = filtersIssuesList.stream()
								.filter(jiraIssue -> !defectTypeSet.contains(jiraIssue.getTypeName().toLowerCase()))
								.collect(Collectors.toCollection(HashSet::new));
					}
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
	 * @param latestSprint
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(Node latestSprint, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprint, null, null, kpiRequest);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		if (CollectionUtils.isNotEmpty(allIssues)) {
			log.info("Issue Hygiene -> request id : {} total jira Issues : {}", requestTrackerId, allIssues.size());
			// Creating map of modal Objects
			Map<String, IssueKpiModalValue> issueKpiModalObject = KpiDataHelper.createMapOfIssueModal(allIssues);

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(Objects.requireNonNull(latestSprint).getProjectFilter().getBasicProjectConfigId());
			allIssues.forEach(issue -> {
				KPIExcelUtility.populateIssueModal(issue, fieldMapping, issueKpiModalObject);
				IssueKpiModalValue data = issueKpiModalObject.get(issue.getNumber());
				data.setCategory(new ArrayList<>());
				if (issue.getEstimate() == null || Double.valueOf(issue.getEstimate()).equals(0.0)) {
					data.getCategory().add(ISSUES_WITHOUT_ESTIMATES);
				}
				if ((issue.getTimeSpentInMinutes() == null || issue.getTimeSpentInMinutes() == 0)
						&& !checkStatus(issue, fieldMapping)) {
					data.getCategory().add(ISSUES_WITH_MISSING_WORKLOGS);
				}
			});

			kpiElement.setSprint(latestSprint.getName());
			kpiElement.setModalHeads(KPIExcelColumn.ISSUE_HYGINE.getColumns());
			kpiElement.setIssueData(new HashSet<>(issueKpiModalObject.values()));
			kpiElement.setFilterGroup(createFilterGroup());
			kpiElement.setDataGroup(createDataGroup());
		}
	}

	/**
	 * Creates filter group.
	 * 
	 * @return
	 */
	private FilterGroup createFilterGroup() {
		FilterGroup filterGroup = new FilterGroup();
		// for the group by selection
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
	 * @return
	 */
	private KpiDataGroup createDataGroup() {
		KpiDataGroup dataGroup = new KpiDataGroup();

		List<KpiData> dataGroup1 = new ArrayList<>();
		dataGroup1
				.add(createKpiData("", ISSUES_WITHOUT_ESTIMATES, 1, "count", "", "Category", ISSUES_WITHOUT_ESTIMATES));
		dataGroup1.add(createKpiData("", ISSUES_WITH_MISSING_WORKLOGS, 2, "count", "", "Category",
				ISSUES_WITH_MISSING_WORKLOGS));

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
	private KpiData createKpiData(String key, String name, Integer order, String aggregation, String unit, String key1,
			String value1) {
		KpiData data = new KpiData();
		data.setKey(key);
		data.setName(name);
		data.setOrder(order);
		data.setAggregation(aggregation);
		data.setUnit(unit);
		data.setShowAsLegend(false);
		data.setKey1(key1);
		data.setValue1(value1);
		return data;
	}

	private boolean checkStatus(JiraIssue jiraIssue, FieldMapping fieldMapping) {

		boolean toDrop = false;
		if (null != fieldMapping && CollectionUtils.isNotEmpty(fieldMapping.getIssueStatusExcluMissingWorkKPI124())) {
			toDrop = fieldMapping.getIssueStatusExcluMissingWorkKPI124().stream().map(String::toUpperCase).toList()
					.contains(jiraIssue.getJiraStatus().toUpperCase());
		}
		return toDrop;
	}

}
