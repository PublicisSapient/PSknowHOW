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
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.publicissapient.kpidashboard.common.constant.CommonConstant.NOT_COMPLETED_ISSUES;

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
		sprintWiseLeafNodeValue(sprintNode, kpiElement, kpiRequest);
		log.info("RisksAndDependenciesServiceImpl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint level.
	 *
	 * @param latestSprint
	 *            latestSprint
	 * @param kpiElement
	 *            kpiElement
	 * @param kpiRequest
	 *            kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(Node latestSprint, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprint, null, null, kpiRequest);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		List<String> notCompletedIssues = (List<String>) resultMap.get(NOT_COMPLETED_ISSUES);

		if (CollectionUtils.isNotEmpty(allIssues)) {
			log.info("Risks And Dependencies -> request id : {} total jira Issues : {}", requestTrackerId,
					allIssues.size());

			FieldMapping fieldMapping = getFieldMapping(latestSprint);
			Map<String, IssueKpiModalValue> issueKpiModalObject = KpiDataHelper.createMapOfIssueModal(allIssues);

			int[] counts = processIssues(allIssues, notCompletedIssues, fieldMapping, issueKpiModalObject);

			KpiDataGroup dataGroup = new KpiDataGroup();
			List<KpiData> dataGroup1 = new ArrayList<>();
			dataGroup1.add(createKpiData(ISSUES_RISK_TYPE, 1, (double) counts[1], (double) counts[0]));
			dataGroup1.add(createKpiData(ISSUES_DEPENDENCY_TYPE, 2, (double) counts[3], (double) counts[2]));
			dataGroup.setDataGroup1(dataGroup1);

			kpiElement.setSprint(latestSprint.getName());
			kpiElement.setModalHeads(KPIExcelColumn.RISKS_AND_DEPENDENCIES.getColumns());
			kpiElement.setIssueData(new HashSet<>(issueKpiModalObject.values()));
			kpiElement.setDataGroup(dataGroup);
		}
	}

	/**
	 * Creates kpi data object.
	 *
	 * @param name
	 * @param order
	 * @param kpiValue
	 * @param kpiValue1
	 * @return
	 */
	private KpiData createKpiData(String name, Integer order, Double kpiValue, Double kpiValue1) {
		KpiData data = new KpiData();
		data.setName(name);
		data.setOrder(order);
		data.setKpiValue(kpiValue);
		data.setKpiValue1(kpiValue1);
		return data;
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
	 * @param notCompletedIssues
	 *            The list of all notCompletedIssues number.
	 * @param fieldMapping
	 *            The field mapping for the current sprint.
	 * @param issueKpiModalObject
	 *            The map containing modal objects.
	 * @return An array containing counts of different types of issues.
	 */
	private int[] processIssues(List<JiraIssue> allIssues, List<String> notCompletedIssues, FieldMapping fieldMapping,
			Map<String, IssueKpiModalValue> issueKpiModalObject) {
		int riskIssue = 0;
		int openRiskIssue = 0;
		int dependencyIssue = 0;
		int openDependencyIssue = 0;

		for (JiraIssue issue : allIssues) {
			KPIExcelUtility.populateIssueModal(issue, fieldMapping, issueKpiModalObject);
			if (isRiskOrDependency(fieldMapping.getJiraIssueRiskTypeKPI176(), issue)) {
				riskIssue++;
			} else if (isRiskOrDependency(fieldMapping.getJiraIssueDependencyTypeKPI176(), issue)) {
				dependencyIssue++;
			}
			if (issue.getNumber() == null || notCompletedIssues.contains(issue.getNumber())) {
				if (isRiskOrDependency(fieldMapping.getJiraIssueRiskTypeKPI176(), issue)) {
					openRiskIssue++;
				} else if (isRiskOrDependency(fieldMapping.getJiraIssueDependencyTypeKPI176(), issue)) {
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
				// to modify sprint details on the basis of configuration for the project
				List<JiraIssueCustomHistory> totalHistoryList = getJiraIssuesCustomHistoryFromBaseClass();
				List<JiraIssue> totalJiraIssueList = getJiraIssuesFromBaseClass();
				Set<String> issueList = totalJiraIssueList.stream().map(JiraIssue::getNumber)
						.collect(Collectors.toSet());

				sprintDetails = IterationKpiHelper.transformIterSprintdetail(totalHistoryList, issueList,
						dbSprintDetail, null, fieldMapping.getJiraIterationCompletionStatusKPI176(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> totalIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.TOTAL_ISSUES);
				List<String> notCompletedIssues = KpiDataHelper
						.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails, NOT_COMPLETED_ISSUES);
				if (CollectionUtils.isNotEmpty(totalIssues)) {
					List<JiraIssue> jiraIssueList = IterationKpiHelper.getFilteredJiraIssue(totalIssues,
							totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getTotalIssues(), jiraIssueList);
					resultListMap.put(ISSUES, new ArrayList<>(filtersIssuesList));
					resultListMap.put(NOT_COMPLETED_ISSUES, notCompletedIssues);
				}
			}
		}
		return resultListMap;
	}
}
