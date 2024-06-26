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

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard.JiraIterationKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.util.IterationKpiHelper;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.publicissapient.kpidashboard.common.constant.CommonConstant.NOT_COMPLETED_ISSUES;
import static com.publicissapient.kpidashboard.common.constant.CommonConstant.OVERALL;

/**
 * This class process the KPI request for Risks And Dependencies
 *
 * @author purgupta2
 */
@Slf4j
@Component
public class RisksAndDependenciesServiceImpl extends JiraIterationKPIService {

	private static final String ISSUES = "issues";
	private static final String ISSUES_RISK_TYPE = "Risks";
	private static final String ISSUES_DEPENDENCY_TYPE = "Dependencies";

	@Autowired
	ConfigHelperService configHelperService;

	@Override
	public String getQualifierType() {
		return KPICode.RISKS_AND_DEPENDENCIES.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node sprintNode)
			throws ApplicationException {
		DataCount trendValue = new DataCount();
		sprintWiseLeafNodeValue(sprintNode, trendValue, kpiElement, kpiRequest);
		log.info("RisksAndDependenciesServiceImpl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint level.
	 *
	 * @param latestSprint
	 *            latestSprint
	 * @param trendValue
	 *            trendValue
	 * @param kpiElement
	 *            kpiElement
	 * @param kpiRequest
	 *            kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(Node latestSprint, DataCount trendValue, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprint, null, null, kpiRequest);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		List<String> notcompletedIssues = (List<String>) resultMap.get(NOT_COMPLETED_ISSUES);

		if (CollectionUtils.isNotEmpty(allIssues)) {
			log.info("Risks And Dependencies -> request id : {} total jira Issues : {}", requestTrackerId,
					allIssues.size());

			FieldMapping fieldMapping = getFieldMapping(latestSprint);
			Map<String, IterationKpiModalValue> modalObjectMap = KpiDataHelper.createMapOfModalObject(allIssues);
			List<IterationKpiModalValue> riskModalValues = new ArrayList<>();
			List<IterationKpiModalValue> dependencyModalValues = new ArrayList<>();

			int[] counts = processIssues(allIssues, notcompletedIssues, fieldMapping, modalObjectMap, riskModalValues,
					dependencyModalValues);

			List<IterationKpiValue> iterationKpiValues = createIterationKpiValues(counts, riskModalValues,
					dependencyModalValues);

			setKpiElementValues(trendValue, kpiElement, iterationKpiValues, latestSprint);
		}
	}

	/**
	 * Retrieves the field mapping for the given latest sprint.
	 *
	 * @param latestSprint
	 *            The latest sprint node.
	 * @return The field mapping corresponding to the latest sprint.
	 * @throws NullPointerException
	 *             If the latest sprint is null.
	 */
	private FieldMapping getFieldMapping(Node latestSprint) {
		return configHelperService.getFieldMappingMap()
				.get(Objects.requireNonNull(latestSprint).getProjectFilter().getBasicProjectConfigId());
	}

	/**
	 * Processes the list of Jira issues and calculates various counts related to
	 * risks and dependencies.
	 *
	 * @param allIssues
	 *            The list of all Jira issues.
	 * @param notcompletedIssues
	 *            The list of all notcompletedIssues number.
	 * @param fieldMapping
	 *            The field mapping for the current sprint.
	 * @param modalObjectMap
	 *            The map containing modal objects.
	 * @param riskModalValues
	 *            The list containing modal values for risks.
	 * @param dependencyModalValues
	 *            The list containing modal values for dependencies.
	 * @return An array containing counts of different types of issues.
	 */
	private int[] processIssues(List<JiraIssue> allIssues, List<String> notcompletedIssues, FieldMapping fieldMapping,
			Map<String, IterationKpiModalValue> modalObjectMap, List<IterationKpiModalValue> riskModalValues,
			List<IterationKpiModalValue> dependencyModalValues) {
		int riskIssue = 0;
		int openRiskIssue = 0;
		int dependencyIssue = 0;
		int openDependencyIssue = 0;

		for (JiraIssue jiraIssue : allIssues) {
			if (isRiskOrDependency(fieldMapping.getJiraIssueRiskTypeKPI176(), jiraIssue)) {
				riskIssue++;
				KPIExcelUtility.populateIterationKPI(riskModalValues, new ArrayList<>(), jiraIssue, fieldMapping,
						modalObjectMap);
			} else if (isRiskOrDependency(fieldMapping.getJiraIssueDependencyTypeKPI176(), jiraIssue)) {
				dependencyIssue++;
				KPIExcelUtility.populateIterationKPI(dependencyModalValues, new ArrayList<>(), jiraIssue, fieldMapping,
						modalObjectMap);
			}
			if (jiraIssue.getNumber() == null || notcompletedIssues.contains(jiraIssue.getNumber())) {
				if (isRiskOrDependency(fieldMapping.getJiraIssueRiskTypeKPI176(), jiraIssue)) {
					openRiskIssue++;
				} else if (isRiskOrDependency(fieldMapping.getJiraIssueDependencyTypeKPI176(), jiraIssue)) {
					openDependencyIssue++;
				}
			}
		}

		return new int[] { riskIssue, openRiskIssue, dependencyIssue, openDependencyIssue };
	}

	/**
	 * Checks whether the given Jira issue is a risk or a dependency.
	 *
	 * @param fieldMapping
	 *            The field mapping for risk or dependency.
	 * @param jiraIssue
	 *            The Jira issue to check.
	 * @return True if the issue is a risk or dependency, false otherwise.
	 */
	private boolean isRiskOrDependency(List<String> fieldMapping, JiraIssue jiraIssue) {
		return fieldMapping != null && fieldMapping.stream().map(String::toLowerCase).toList()
				.contains(jiraIssue.getTypeName().toLowerCase());
	}

	/**
	 * Creates a list of IterationKpiValue objects based on the provided counts and
	 * modal values.
	 *
	 * @param counts
	 *            The counts of different types of issues.
	 * @param riskModalValues
	 *            The list containing modal values for risks.
	 * @param dependencyModalValues
	 *            The list containing modal values for dependencies.
	 * @return A list of IterationKpiValue objects.
	 */
	private List<IterationKpiValue> createIterationKpiValues(int[] counts, List<IterationKpiModalValue> riskModalValues,
			List<IterationKpiModalValue> dependencyModalValues) {
		List<IterationKpiValue> iterationKpiValueList = new ArrayList<>();
		int riskIssue = counts[0];
		int openRiskIssue = counts[1];
		int dependencyIssue = counts[2];
		int openDependencyIssue = counts[3];

		List<IterationKpiData> data = new ArrayList<>();
		IterationKpiData riskIterationKpiData = new IterationKpiData(ISSUES_RISK_TYPE, (double) openRiskIssue,
				(double) riskIssue, null, "", riskModalValues);
		IterationKpiData dependencyIterationKpiData = new IterationKpiData(ISSUES_DEPENDENCY_TYPE,
				(double) openDependencyIssue, (double) dependencyIssue, null, "", dependencyModalValues);
		data.add(riskIterationKpiData);
		data.add(dependencyIterationKpiData);

		IterationKpiValue iterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
		iterationKpiValueList.add(iterationKpiValue);

		return iterationKpiValueList;
	}

	/**
	 * Sets KPI element values using the provided trend value, KPI element,
	 * iteration KPI values, and latest sprint.
	 *
	 * @param trendValue
	 *            The trend value containing iteration KPI values.
	 * @param kpiElement
	 *            The KPI element to set values for.
	 * @param iterationKpiValues
	 *            The list of iteration KPI values.
	 * @param latestSprint
	 *            The latest sprint node.
	 */
	private void setKpiElementValues(DataCount trendValue, KpiElement kpiElement,
			List<IterationKpiValue> iterationKpiValues, Node latestSprint) {
		trendValue.setValue(iterationKpiValues);
		kpiElement.setSprint(latestSprint.getName());
		kpiElement.setModalHeads(KPIExcelColumn.RISKS_AND_DEPENDENCIES.getColumns());
		kpiElement.setTrendValueList(trendValue);
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		if (null != leafNode) {
			log.info("Risks And Dependencies -> Requested sprint : {}", leafNode.getName());
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
						dbSprintDetail, null, fieldMapping.getJiraIterationCompletionStatusKPI176(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> totalIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.TOTAL_ISSUES);
				List<String> notcompletedIssues = KpiDataHelper
						.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails, NOT_COMPLETED_ISSUES);
				if (CollectionUtils.isNotEmpty(totalIssues)) {
					List<JiraIssue> jiraIssueList = IterationKpiHelper.getFilteredJiraIssue(totalIssues,
							totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getTotalIssues(), jiraIssueList);
					resultListMap.put(ISSUES, new ArrayList<>(filtersIssuesList));
					resultListMap.put(NOT_COMPLETED_ISSUES, notcompletedIssues);
				}
			}
		}
		return resultListMap;
	}
}
