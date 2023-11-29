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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.publicissapient.kpidashboard.common.model.jira.StatusWiseIssue;

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
	public static final String SP = " SP";
	public static final String ISSUE_COUNT = "Issue Count";
	public static final String STORY_POINT = "Story point";
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

			if (CollectionUtils.isNotEmpty(fieldMapping.getJiraIssueTypeNamesKPI161())) {
				totalJiraIssue = jiraService.getJiraIssuesForCurrentSprint();
				totalJiraIssue = totalJiraIssue.stream().filter(
						jiraIssue -> fieldMapping.getJiraIssueTypeNamesKPI161().contains(jiraIssue.getTypeName()))
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
//		IterationKpiValue overAllIterationKpiValue = new IterationKpiValue();
		List<IterationKpiValue> overAllIterationKpiValue = new ArrayList<>();
		if (leafNode != null) {
			Object basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, "", "", kpiRequest);
			List<JiraIssue> jiraIssues = (List<JiraIssue>) resultMap.get(PROJECT_WISE_JIRA_ISSUE);
			List<String> sprintList = (List<String>) resultMap.get(SPRINT_LIST);
			if (CollectionUtils.isNotEmpty(sprintList) && CollectionUtils.isNotEmpty(jiraIssues)) {

//				List<DataCount> dataCountList = new ArrayList<>();
				List<DataCount> issueCountDcList = new ArrayList<>();
				List<DataCount> storyPointDcList = new ArrayList<>();
				List<JiraIssue> filteredJiraIssue = new ArrayList<>();
				List<String> inProgressStatus = Optional.ofNullable(fieldMapping.getJiraStatusForInProgressKPI161()).orElse(Collections.emptyList()).stream().map(String::toLowerCase).collect(Collectors.toList());
				List<String> backlogRefinedStatus = Optional.ofNullable(fieldMapping.getJiraStatusForRefinedKPI161()).orElse(Collections.emptyList()).stream().map(String::toLowerCase).collect(Collectors.toList());
				List<String> backlogNotRefinedStatus = Optional.ofNullable(fieldMapping.getJiraStatusForNotRefinedKPI161()).orElse(Collections.emptyList()).stream().map(String::toLowerCase).collect(Collectors.toList());
				sprintList.forEach(sprint -> {
					/*Map<String, List<JiraIssue>> statusWiseJiraIssue = jiraIssues.stream()
							.filter(jiraIssue -> jiraIssue.getSprintName().equalsIgnoreCase(sprint))
							.collect(Collectors.groupingBy(JiraIssue::getStatus));*/
					Map<String, List<JiraIssue>> statusWiseJiraIssue = new HashMap<>();
					statusWiseJiraIssue.put("InProgress", filterByStatus(sprint, jiraIssues, inProgressStatus));
					statusWiseJiraIssue.put("Refined", filterByStatus(sprint, jiraIssues, backlogRefinedStatus));
					statusWiseJiraIssue.put("NotRefined", filterByStatus(sprint, jiraIssues, backlogNotRefinedStatus));
					DataCount dataCount = new DataCount();
					DataCount issueCountDc = new DataCount();
					DataCount storyPointDc = new DataCount();
					/*dataCount.setSSprintName(sprint);
					dataCount.setKpiGroup(CommonConstant.FUTURE_SPRINTS);*/
					issueCountDc.setSSprintName(sprint);
					issueCountDc.setKpiGroup(CommonConstant.FUTURE_SPRINTS);
					storyPointDc.setSSprintName(sprint);
					storyPointDc.setKpiGroup(CommonConstant.FUTURE_SPRINTS);
					HashMap<Object, Integer> mapOfIssueCount = new HashMap<>();
					HashMap<Object, Double> mapOfStoryPoint = new HashMap<>();
//					Map<String, StatusWiseIssue> statusWiseStoryCountAndPointMap = new LinkedHashMap<>();
					if (MapUtils.isNotEmpty(statusWiseJiraIssue)) {
//						TreeMap<String, List<JiraIssue>> sortedStatusJiraMap = new TreeMap<>(statusWiseJiraIssue);
						statusWiseJiraIssue.forEach((status, jiraIssue) -> {
							filteredJiraIssue.addAll(jiraIssue);
							StatusWiseIssue statusWiseData = getStatusWiseStoryCountAndPointList(jiraIssue,
									fieldMapping);


							mapOfIssueCount.put(status,jiraIssue.size());


							mapOfStoryPoint.put(status,KpiDataHelper.calculateStoryPoints(jiraIssue,fieldMapping));

//							issueCountDc.setValue(statusWiseData.getIssueCount());
//							storyPointDc.setValue(statusWiseData.getIssueStoryPoint());
//							statusWiseStoryCountAndPointMap.put(status, statusWiseData);
						});
						issueCountDc.setData(String.valueOf(mapOfIssueCount.values().stream()
								.mapToInt(Integer::intValue)
								.sum()));
						storyPointDc.setData(String.valueOf(mapOfStoryPoint.values().stream()
								.mapToDouble(Double::doubleValue)
								.sum()));
						/*dataCount.setData(
								String.valueOf(sortedStatusJiraMap.values().stream().mapToInt(List::size).sum()));*/
					} else {
//						dataCount.setData(String.valueOf(0));
						issueCountDc.setData(String.valueOf(0));
						storyPointDc.setData(String.valueOf(0));
					}
					issueCountDc.setValue(mapOfIssueCount);
					storyPointDc.setValue(mapOfStoryPoint);
//					dataCount.setValue(statusWiseStoryCountAndPointMap);
					issueCountDcList.add(issueCountDc);
					storyPointDcList.add(storyPointDc);
//					dataCountList.add(dataCount);

				});
				IterationKpiValue issueCountIterationKpiValue = new IterationKpiValue();
				issueCountIterationKpiValue.setFilter1(ISSUE_COUNT);
				issueCountIterationKpiValue.setValue(issueCountDcList);

				IterationKpiValue storyPointIterationKpiValue = new IterationKpiValue();
				storyPointIterationKpiValue.setFilter1(STORY_POINT);
				storyPointIterationKpiValue.setValue(storyPointDcList);

				overAllIterationKpiValue.add(issueCountIterationKpiValue);
				overAllIterationKpiValue.add(storyPointIterationKpiValue);

//				overAllIterationKpiValue.setValue(dataCountList);
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

	private List<JiraIssue> filterByStatus(String sprint, List<JiraIssue> jiraIssues, List<String> statusList) {
		return jiraIssues.stream()
				.filter(jiraIssue -> jiraIssue.getSprintName().equalsIgnoreCase(sprint))
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
	 * calculate status wise total story points and issue counts in a sprint
	 *
	 * @param jiraIssueList
	 *            jiraIssueList
	 * @param fieldMapping
	 *            fieldMapping
	 * @return return StatusWiseIssue
	 */
	private StatusWiseIssue getStatusWiseStoryCountAndPointList(List<JiraIssue> jiraIssueList,
			FieldMapping fieldMapping) {
		StatusWiseIssue statusWiseCountAndPoints = new StatusWiseIssue();
		statusWiseCountAndPoints.setIssueCount((double) jiraIssueList.size());
		double totalStoryPoints = jiraIssueList.stream().mapToDouble(jiraIssue -> {
			if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
					&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
				return Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0d);
			} else {
				Integer integer = Optional.ofNullable(jiraIssue.getOriginalEstimateMinutes()).orElse(0);
				int inHours = integer / 60;
				return inHours / fieldMapping.getStoryPointToHourMapping();
			}
		}).sum();

		statusWiseCountAndPoints.setIssueStoryPoint(totalStoryPoints + SP);

		return statusWiseCountAndPoints;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

}
