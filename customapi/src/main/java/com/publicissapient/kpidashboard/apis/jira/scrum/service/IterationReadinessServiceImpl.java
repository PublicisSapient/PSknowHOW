/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

import lombok.extern.slf4j.Slf4j;

/**
 * Jira service class to fetch Iteration readiness kpi details
 *
 * @author aksshriv1
 *
 */
@Slf4j
@Component
public class IterationReadinessServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final String PROJECT_WISE_JIRA_ISSUE = "Jira Issue";
	public static final String SPRINT_LIST = "Sprint List";
	public static final String ISSUE_COUNT = "Issue Count";
	public static final String STORY_POINT = "Story Points";
	public static final String IN_PROGRESS = "In Progress";
	public static final String READY_FOR_DEV = "Ready for Dev";
	public static final String NOT_REFINED = "Not Refined";
	@Autowired
	private JiraServiceR jiraService;
	@Autowired
	private ConfigHelperService configHelperService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
			Filters filters = Filters.getFilter(k);
			if (Filters.PROJECT == filters) {
				projectWiseLeafNodeValue(v, kpiElement, kpiRequest);
			}
		});
		log.info("Iteration Readiness Service impl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);

		if (leafNode != null) {
			log.info("Iteration Readiness kpi -> Requested project : {}", leafNode.getProjectFilter().getName());
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(leafNode.getProjectFilter().getBasicProjectConfigId());
			List<JiraIssue> totalJiraIssue = new ArrayList<>();

			totalJiraIssue = jiraService.getJiraIssuesForCurrentSprint();
			final List<String> filterByIssueTypeKPI161 = Optional.ofNullable(fieldMapping.getJiraIssueTypeNamesKPI161())
					.orElse(Collections.emptyList()).stream().map(String::toLowerCase).collect(Collectors.toList());
			// filtering by type only when type is updated in fieldMapping else all types
			// will be shown
			if (CollectionUtils.isNotEmpty(filterByIssueTypeKPI161)) {
				totalJiraIssue = totalJiraIssue.stream()
						.filter(jiraIssue -> filterByIssueTypeKPI161.contains(jiraIssue.getTypeName().toLowerCase()))
						.collect(Collectors.toList());
			}
			List<String> totalSprint = jiraService.getFutureSprintsList();
			resultListMap.put(PROJECT_WISE_JIRA_ISSUE, totalJiraIssue);
			resultListMap.put(SPRINT_LIST, totalSprint);
		}

		return resultListMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getQualifierType() {
		return KPICode.ITERATION_READINESS_KPI.name();
	}

	/**
	 * Populates KPI value to project leaf nodes. It also gives the trend analysis
	 * project wise.
	 *
	 * @param leafNodeList
	 *            leafNodeList
	 * @param kpiElement
	 *            kpiElement
	 * @param kpiRequest
	 *            kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(List<Node> leafNodeList, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		List<IterationKpiValue> overAllIterationKpiValue = new ArrayList<>();
		if (leafNode != null) {
			Object basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, "", "", kpiRequest);
			List<JiraIssue> jiraIssues = (List<JiraIssue>) resultMap.get(PROJECT_WISE_JIRA_ISSUE);
			List<String> sprintList = (List<String>) resultMap.get(SPRINT_LIST);
			if (CollectionUtils.isNotEmpty(sprintList) && CollectionUtils.isNotEmpty(jiraIssues)) {

				List<DataCount> issueCountDcList = new ArrayList<>();
				List<DataCount> storyPointDcList = new ArrayList<>();
				List<JiraIssue> filteredJiraIssue = new ArrayList<>();
				List<String> inProgressStatus = Optional.ofNullable(fieldMapping.getJiraStatusForInProgressKPI161())
						.orElse(Collections.emptyList()).stream().map(String::toLowerCase).collect(Collectors.toList());
				List<String> backlogRefinedStatus = Optional.ofNullable(fieldMapping.getJiraStatusForRefinedKPI161())
						.orElse(Collections.emptyList()).stream().map(String::toLowerCase).collect(Collectors.toList());
				List<String> backlogNotRefinedStatus = Optional
						.ofNullable(fieldMapping.getJiraStatusForNotRefinedKPI161()).orElse(Collections.emptyList())
						.stream().map(String::toLowerCase).collect(Collectors.toList());
				sprintList.forEach(sprint -> {
					Map<String, List<JiraIssue>> statusWiseJiraIssue = new LinkedHashMap<>();
					statusWiseJiraIssue.put(IN_PROGRESS, filterByStatus(sprint, jiraIssues, inProgressStatus));
					statusWiseJiraIssue.put(READY_FOR_DEV, filterByStatus(sprint, jiraIssues, backlogRefinedStatus));
					statusWiseJiraIssue.put(NOT_REFINED, filterByStatus(sprint, jiraIssues, backlogNotRefinedStatus));
					DataCount issueCountDc = new DataCount();
					DataCount storyPointDc = new DataCount();
					issueCountDc.setSSprintName(sprint);
					issueCountDc.setKpiGroup(CommonConstant.FUTURE_SPRINTS);
					storyPointDc.setSSprintName(sprint);
					storyPointDc.setKpiGroup(CommonConstant.FUTURE_SPRINTS);
					HashMap<Object, Integer> mapOfIssueCount = new LinkedHashMap<>();
					HashMap<Object, Double> mapOfStoryPoint = new LinkedHashMap<>();
					if (MapUtils.isNotEmpty(statusWiseJiraIssue)) {
						statusWiseJiraIssue.forEach((status, jiraIssue) -> {
							filteredJiraIssue.addAll(jiraIssue);
							mapOfIssueCount.put(status, jiraIssue.size());
							mapOfStoryPoint.put(status, KpiDataHelper.calculateStoryPoints(jiraIssue, fieldMapping));

						});
						issueCountDc.setData(
								String.valueOf(mapOfIssueCount.values().stream().mapToInt(Integer::intValue).sum()));
						storyPointDc.setData(String
								.valueOf(mapOfStoryPoint.values().stream().mapToDouble(Double::doubleValue).sum()));
					} else {
						issueCountDc.setData(String.valueOf(0));
						storyPointDc.setData(String.valueOf(0));
					}
					issueCountDc.setValue(mapOfIssueCount);
					storyPointDc.setValue(mapOfStoryPoint);
					issueCountDcList.add(issueCountDc);
					storyPointDcList.add(storyPointDc);

				});
				IterationKpiValue issueCountIterationKpiValue = new IterationKpiValue();
				issueCountIterationKpiValue.setFilter1(ISSUE_COUNT);
				issueCountIterationKpiValue.setValue(issueCountDcList);

				IterationKpiValue storyPointIterationKpiValue = new IterationKpiValue();
				storyPointIterationKpiValue.setFilter1(STORY_POINT);
				storyPointIterationKpiValue.setValue(storyPointDcList);

				overAllIterationKpiValue.add(storyPointIterationKpiValue);
				overAllIterationKpiValue.add(issueCountIterationKpiValue);

				populateExcelDataObject(requestTrackerId, excelData, filteredJiraIssue, fieldMapping);
				kpiElement.setModalHeads(KPIExcelColumn.ITERATION_READINESS.getColumns());
				kpiElement.setExcelColumns(KPIExcelColumn.ITERATION_READINESS.getColumns());
				kpiElement.setExcelData(excelData);
				log.info("Iteration Readiness Service Impl -> request id : {} future sprints jira Issues : {}",
						requestTrackerId, filteredJiraIssue);
			}

		}
		kpiElement.setTrendValueList(overAllIterationKpiValue);
	}

	/**
	 * Method to filter issues w.r.t sprint & status
	 * 
	 * @param sprint
	 *            sprint
	 * @param jiraIssues
	 *            jiraIssues
	 * @param statusList
	 *            statusList
	 * @return List<JiraIssue>
	 */
	private List<JiraIssue> filterByStatus(String sprint, List<JiraIssue> jiraIssues, List<String> statusList) {
		return jiraIssues.stream().filter(jiraIssue -> jiraIssue.getSprintName().equalsIgnoreCase(sprint))
				.filter(jiraIssue -> statusList.contains(jiraIssue.getStatus().toLowerCase()))
				.collect(Collectors.toList());
	}

	/**
	 *
	 * @param requestTrackerId
	 *            requestTrackerId
	 * @param excelData
	 *            excelData
	 * @param jiraIssueList
	 *            jiraIssueList
	 * @param fieldMapping
	 *            fieldMapping
	 */
	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> jiraIssueList, FieldMapping fieldMapping) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& CollectionUtils.isNotEmpty(jiraIssueList)) {
			KPIExcelUtility.populateIterationReadinessExcelData(jiraIssueList, excelData, fieldMapping);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

}
