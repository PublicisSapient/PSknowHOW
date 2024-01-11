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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.CommonServiceImpl;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
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
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DefectCountByStatusServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	public static final String UNCHECKED = "unchecked";
	private static final String TOTAL_ISSUES = "Total Issues";
	private static final String SPRINT_DETAILS = "SprintDetails";
	private static final String CREATED_DURING_ITERATION = "Created during Iteration";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CommonServiceImpl commonService;

	private static void overallStatusCountMap(List<DataCount> dataCountListForAllPriorities,
			Map<String, Integer> overallStatusCountMapAggregate) {
		for (DataCount dataCount : dataCountListForAllPriorities) {
			Map<String, Integer> statusCountMap = (Map<String, Integer>) dataCount.getValue();
			statusCountMap.forEach((status, statusCountValue) -> overallStatusCountMapAggregate.merge(status,
					statusCountValue, Integer::sum));
		}
	}

	private static int getPriorityStatusCount(Map<String, Integer> overallStatusCountMap,
			Map<String, List<JiraIssue>> statusData, int priorityRCACount, Map<String, Integer> statusCountMap) {
		for (Map.Entry<String, List<JiraIssue>> rcaEntry : statusData.entrySet()) {
			String status = rcaEntry.getKey();
			List<JiraIssue> issues = rcaEntry.getValue();

			priorityRCACount += issues.size();
			statusCountMap.put(status, issues.size());
			overallStatusCountMap.merge(status, issues.size(), Integer::sum);
		}
		return priorityRCACount;
	}

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			log.info("Defect count by Status -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId().toString();
			String sprintId = leafNode.getSprintFilter().getId();
			List<String> defectType = new ArrayList<>();
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
						dbSprintDetail, new ArrayList<>(), fieldMapping.getJiraIterationCompletionStatusKPI136(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> totalIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.TOTAL_ISSUES);
				List<String> defectTypes = Optional.ofNullable(fieldMapping).map(FieldMapping::getJiradefecttype)
						.orElse(Collections.emptyList());
				Set<String> totalSprintReportDefects = new HashSet<>();
				Set<String> totalSprintReportStories = new HashSet<>();
				sprintDetails.getTotalIssues().stream().forEach(sprintIssue -> {
					if (defectTypes.contains(sprintIssue.getTypeName())) {
						totalSprintReportDefects.add(sprintIssue.getNumber());
					} else {
						totalSprintReportStories.add(sprintIssue.getNumber());
					}
				});

				Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
				Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
				Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
				defectType.add(NormalizedJira.DEFECT_TYPE.getValue());
				mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(defectType));
				uniqueProjectMap.put(basicProjectConfigId, mapOfProjectFilters);
				mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
						Collections.singletonList(basicProjectConfigId));

				if (CollectionUtils.isNotEmpty(totalIssues)) {
					List<JiraIssue> totalIssueList = IterationKpiHelper.getFilteredJiraIssue(totalIssues,
							totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getTotalIssues(), totalIssueList);

					// fetched all defects which is linked to current sprint report stories
					List<JiraIssue> linkedDefects = jiraIssueRepository.findLinkedDefects(mapOfFilters,
							totalSprintReportStories, uniqueProjectMap);

					// filter defects which is issue type not coming in sprint report
					List<JiraIssue> subTaskDefects = linkedDefects.stream()
							.filter(jiraIssue -> !totalSprintReportDefects.contains(jiraIssue.getNumber()))
							.collect(Collectors.toList());

					List<JiraIssue> totalSubTaskTaggedToSprint = subTaskDefects.stream()
							.filter(jiraIssue -> CollectionUtils.isNotEmpty(jiraIssue.getSprintIdList())
									&& jiraIssue.getSprintIdList().contains(sprintId.split("_")[0]))
							.collect(Collectors.toList());

					List<JiraIssue> allIssues = new ArrayList<>();
					allIssues.addAll(filtersIssuesList);
					allIssues.addAll(totalSubTaskTaggedToSprint);

					resultListMap.put(CommonConstant.TOTAL_ISSUES, new ArrayList<>(allIssues));
				}
				resultListMap.put(SPRINT_DETAILS, sprintDetails);
			}
		}
		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.DEFECT_COUNT_BY_STATUS_PIE_CHART.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				sprintWiseLeafNodeValue(v, kpiElement, kpiRequest);
			}
		});
		log.info("DefectCountByStatusServiceImpl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	private void sprintWiseLeafNodeValue(List<Node> sprintLeafNodeList, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<KPIExcelData> excelData = new ArrayList<>();
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);
		if (latestSprint != null) {
			Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(latestSprint.getProjectFilter().getBasicProjectConfigId());
			if (fieldMapping != null) {
				SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT_DETAILS);
				List<JiraIssue> allCompletedDefects = filterDefects(resultMap, fieldMapping);
				List<JiraIssue> createDuringIteration = allCompletedDefects.stream()
						.filter(jiraIssue -> DateUtil.isWithinDateRange(
								LocalDate.parse(jiraIssue.getCreatedDate().split("\\.")[0], DATE_TIME_FORMATTER),
								LocalDate.parse(sprintDetails.getStartDate().split("\\.")[0], DATE_TIME_FORMATTER),
								LocalDate.parse(sprintDetails.getEndDate().split("\\.")[0], DATE_TIME_FORMATTER)))
						.collect(Collectors.toList());
				Map<String, Map<String, List<JiraIssue>>> priorityWiseStatusList = getPriorityWiseStatusList(
						allCompletedDefects, createDuringIteration);
				List<Integer> overAllRCAIssueCount = Arrays.asList(0);
				log.info("DefectCountByStatusServiceImpl -> priorityWiseStatusList ->  : {}", priorityWiseStatusList);
				// filterDataList will consist of IterationKpiValue which will be set for all
				// priorities
				List<IterationKpiValue> filterDataList = new ArrayList<>();
				List<IterationKpiValue> sortedFilterDataList = new ArrayList<>();
				List<DataCount> dataCountListForAllPriorities = new ArrayList<>();
				Map<String, Integer> overallStatusCountMap = new HashMap<>();
				for (Map.Entry<String, Map<String, List<JiraIssue>>> entry : priorityWiseStatusList.entrySet()) {
					Map<String, List<JiraIssue>> statusData = entry.getValue();
					int priorityStatusCount = 0;
					Map<String, Integer> statusCountMap = new HashMap<>();
					// update and set the overall data
					priorityStatusCount = getPriorityStatusCount(overallStatusCountMap, statusData, priorityStatusCount,
							statusCountMap);
					DataCount priorityStatusData = new DataCount();
					priorityStatusData.setData(String.valueOf(priorityStatusCount));
					priorityStatusData.setValue(statusCountMap);
					List<DataCount> dataCountList = new ArrayList<>();
					dataCountList.add(priorityStatusData);
					dataCountListForAllPriorities.add(priorityStatusData);

					// to make structure to create pie chart
					List<DataCount> middleTrendValueListForPriorities = new ArrayList<>();
					DataCount middleOverallData = new DataCount();
					middleOverallData.setData(latestSprint.getProjectFilter().getName());
					middleOverallData.setValue(dataCountList);
					middleTrendValueListForPriorities.add(middleOverallData);

					IterationKpiValue filterData = new IterationKpiValue(entry.getKey(),
							middleTrendValueListForPriorities);
					filterDataList.add(filterData);

				}
				Map<String, Integer> overallStatusCountMapAggregate = new HashMap<>();
				overallStatusCountMap(dataCountListForAllPriorities, overallStatusCountMapAggregate);
				if (MapUtils.isNotEmpty(overallStatusCountMapAggregate)) {

					populateExcelDataObject(requestTrackerId, excelData, allCompletedDefects, createDuringIteration,
							latestSprint.getSprintFilter().getName(), fieldMapping);

					kpiElement.setSprint(latestSprint.getName());
					kpiElement.setModalHeads(KPIExcelColumn.DEFECT_COUNT_BY_STATUS_PIE_CHART.getColumns());
					kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_COUNT_BY_STATUS_PIE_CHART.getColumns());
					kpiElement.setExcelData(excelData);
					sortedFilterDataList.add(filterDataList.stream()
							.filter(iterationKpiValue -> iterationKpiValue.getFilter1()
									.equalsIgnoreCase(CREATED_DURING_ITERATION))
							.findFirst().orElse(new IterationKpiValue()));
					filterDataList.removeIf(iterationKpiValue -> iterationKpiValue.getFilter1()
							.equalsIgnoreCase(CREATED_DURING_ITERATION));
					sortListByKey(filterDataList);
					sortedFilterDataList.addAll(filterDataList);
					kpiElement.setTrendValueList(sortedFilterDataList);
					log.info("DefectCountByStatusServiceImpl -> request id : {} total jira Issues : {}",
							requestTrackerId, overAllRCAIssueCount.get(0));
				}
			}
		}
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> sprintWiseDefectDataList, List<JiraIssue> createDuringIteration, String name,
			FieldMapping fieldMapping) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& !Objects.isNull(sprintWiseDefectDataList) && !sprintWiseDefectDataList.isEmpty()) {
			KPIExcelUtility.populateDefectRCAandStatusRelatedExcelData(name, sprintWiseDefectDataList,
					createDuringIteration, excelData, fieldMapping);
		}

	}

	private List<JiraIssue> filterDefects(Map<String, Object> resultMap, FieldMapping fieldMapping) {
		List<String> defectStatuses = fieldMapping.getJiradefecttype();
		// subtask defects consider as BUG type in jira_issue
		defectStatuses.add(NormalizedJira.DEFECT_TYPE.getValue());
		if (CollectionUtils.isNotEmpty((List<JiraIssue>) resultMap.get(CommonConstant.TOTAL_ISSUES))) {
			return ((List<JiraIssue>) resultMap.get(CommonConstant.TOTAL_ISSUES)).stream()
					.filter(issue -> defectStatuses.contains(issue.getTypeName())).collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	private Map<String, Map<String, List<JiraIssue>>> getPriorityWiseStatusList(
			List<JiraIssue> allCompletedIssuesExcludeStory, List<JiraIssue> createDuringIteration) {
		Map<String, Map<String, List<JiraIssue>>> scopeWiseDefectsMap = new HashMap<>();
		scopeWiseDefectsMap.put(TOTAL_ISSUES,
				allCompletedIssuesExcludeStory.stream().collect(Collectors.groupingBy(JiraIssue::getStatus)));
		scopeWiseDefectsMap.put(CREATED_DURING_ITERATION,
				createDuringIteration.stream().collect(Collectors.groupingBy(JiraIssue::getStatus)));
		return scopeWiseDefectsMap;
	}

	private void sortListByKey(List<IterationKpiValue> list) {
		list.sort(Comparator.comparing(IterationKpiValue::getFilter1));
	}
}