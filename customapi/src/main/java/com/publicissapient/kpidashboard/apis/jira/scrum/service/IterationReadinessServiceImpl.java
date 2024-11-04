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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.backlogdashboard.JiraBacklogKPIService;
import com.publicissapient.kpidashboard.apis.jira.service.backlogdashboard.JiraBacklogServiceR;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
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
public class IterationReadinessServiceImpl extends JiraBacklogKPIService<Integer, List<Object>> {

	private static final String PROJECT_WISE_JIRA_ISSUE = "Jira Issue";
	public static final String SPRINT_LIST = "Sprint List";
	public static final String ISSUE_COUNT = "Issue Count";
	public static final String STORY_POINT = "Story Points";
	public static final String IN_PROGRESS = "In Progress";
	public static final String READY_FOR_DEV = "Ready for Dev";
	public static final String NOT_REFINED = "Not Refined";
	public static final String AXIS_LABEL_COUNT = "Count";
	public static final String AXIS_LABEL_SP = "SP";
	@Autowired
	private JiraBacklogServiceR jiraService;
	@Autowired
	private ConfigHelperService configHelperService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node projectNode)
			throws ApplicationException {

		projectWiseLeafNodeValue(projectNode, kpiElement, kpiRequest);
		log.info("Iteration Readiness Service impl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();

		if (leafNode != null) {
			log.info("Iteration Readiness kpi -> Requested project : {}", leafNode.getProjectFilter().getName());
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(leafNode.getProjectFilter().getBasicProjectConfigId());
			List<JiraIssue> totalJiraIssue = jiraService.getJiraIssuesForCurrentSprint();
			final List<String> filterByIssueTypeKPI161 = Optional.ofNullable(fieldMapping.getJiraIssueTypeNamesKPI161())
					.orElse(Collections.emptyList()).stream().map(String::toLowerCase).collect(Collectors.toList());
			// filtering by type only when type is updated in fieldMapping else all types
			// will be shown
			if (CollectionUtils.isNotEmpty(filterByIssueTypeKPI161)) {
				totalJiraIssue = totalJiraIssue.stream()
						.filter(jiraIssue -> filterByIssueTypeKPI161.contains(jiraIssue.getTypeName().toLowerCase()))
						.collect(Collectors.toList());
			}
			List<String> totalSprint = new ArrayList<>(jiraService.getFutureSprintsList());
			totalSprint.add(CommonConstant.BLANK);
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
	 * @param leafNode
	 *            leafNodeList
	 * @param kpiElement
	 *            kpiElement
	 * @param kpiRequest
	 *            kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(Node leafNode, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		List<IterationKpiValue> overAllIterationKpiValue = new ArrayList<>();
		if (leafNode != null) {
			Object basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			Map<String, Object> resultMap = fetchKPIDataFromDb(leafNode, "", "", kpiRequest);
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

					DataCount issueCountDc = new DataCount();
					List<DataCount> dataCountList = new ArrayList<>();
					DataCount storyPointDc = new DataCount();

					// filter by to inProgress category
					List<JiraIssue> inProgressJiraIssue = filterByStatus(sprint, jiraIssues, inProgressStatus);
					// filter by refined category
					List<JiraIssue> refinedJiraIssues = filterByStatus(sprint, jiraIssues, backlogRefinedStatus);
					// filter by not refined category
					List<JiraIssue> notRefinedJiraIssues = filterByStatus(sprint, jiraIssues, backlogNotRefinedStatus);

					filteredJiraIssue.addAll(inProgressJiraIssue);
					filteredJiraIssue.addAll(refinedJiraIssues);
					filteredJiraIssue.addAll(notRefinedJiraIssues);

					// create drill down
					long inProgressCount = inProgressJiraIssue.size();
					double inProgressSize = KpiDataHelper.calculateStoryPoints(inProgressJiraIssue, fieldMapping);
					createIssueCountDrillDown(inProgressJiraIssue, IN_PROGRESS, inProgressCount, inProgressSize,
							dataCountList, fieldMapping);

					long refinedIssuesCount = refinedJiraIssues.size();
					double refinedIssuesSize = KpiDataHelper.calculateStoryPoints(refinedJiraIssues, fieldMapping);
					createIssueCountDrillDown(refinedJiraIssues, READY_FOR_DEV, refinedIssuesCount, refinedIssuesSize,
							dataCountList, fieldMapping);

					long notRefinedIssuesCount = notRefinedJiraIssues.size();
					double notRefinedIssuesSize = KpiDataHelper.calculateStoryPoints(notRefinedJiraIssues,
							fieldMapping);
					createIssueCountDrillDown(notRefinedJiraIssues, NOT_REFINED, notRefinedIssuesCount,
							notRefinedIssuesSize, dataCountList, fieldMapping);

					setDataCount(sprint, issueCountDc, storyPointDc);

					issueCountDc.setData(String.valueOf(inProgressCount + refinedIssuesCount + notRefinedIssuesCount));
					storyPointDc.setData(String.valueOf(inProgressSize + refinedIssuesSize + notRefinedIssuesSize));
					issueCountDc.setValue(dataCountList);
					storyPointDc.setValue(dataCountList);
					issueCountDcList.add(issueCountDc);
					storyPointDcList.add(storyPointDc);

				});

				IterationKpiValue issueCountIterationKpiValue = new IterationKpiValue();
				issueCountIterationKpiValue.setFilter1(ISSUE_COUNT);
				issueCountIterationKpiValue.setYAxisLabel(CommonConstant.COUNT);
				issueCountIterationKpiValue.setValue(issueCountDcList);

				IterationKpiValue storyPointIterationKpiValue = new IterationKpiValue();
				storyPointIterationKpiValue.setFilter1(STORY_POINT);
				storyPointIterationKpiValue.setYAxisLabel(CommonConstant.SP);
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

	private static void createIssueCountDrillDown(List<JiraIssue> jiraIssueList, String definedStatus,
			long definedStatusCount, double issueSize, List<DataCount> dataCountList, FieldMapping fieldMapping) {
		List<DataCount> drillDownList = new ArrayList<>();
		Map<String, List<JiraIssue>> issueCountStatusMap = jiraIssueList.stream()
				.collect(Collectors.groupingBy(JiraIssue::getStatus));
		issueCountStatusMap.forEach((status, issueList) -> drillDownList.add(new DataCount(status, issueList.size(),
				KpiDataHelper.calculateStoryPoints(issueList, fieldMapping), null)));
		DataCount definedStatusDc = new DataCount(definedStatus, definedStatusCount, issueSize, drillDownList);
		dataCountList.add(definedStatusDc);
	}

	/**
	 * Sets data count
	 *
	 * @param sprint
	 *            sprint
	 * @param issueCountDc
	 *            issueCountDc
	 * @param storyPointDc
	 *            storyPointDc
	 */
	private static void setDataCount(String sprint, DataCount issueCountDc, DataCount storyPointDc) {
		if (StringUtils.isNotEmpty(sprint)) {
			issueCountDc.setSSprintName(sprint);
			issueCountDc.setKpiGroup(CommonConstant.FUTURE_SPRINTS);
			storyPointDc.setSSprintName(sprint);
			storyPointDc.setKpiGroup(CommonConstant.FUTURE_SPRINTS);
		} else {
			issueCountDc.setSSprintName(CommonConstant.BACKLOG);
			issueCountDc.setKpiGroup(CommonConstant.BACKLOG);
			storyPointDc.setSSprintName(CommonConstant.BACKLOG);
			storyPointDc.setKpiGroup(CommonConstant.BACKLOG);
		}
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

}
