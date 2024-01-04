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

import static java.time.temporal.ChronoUnit.HOURS;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.BacklogKpiHelper;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author kunkambl Flow Efficiency KPI from Flow KPIS tab of Backlog
 */
@Slf4j
@Component
public class FlowEfficiencyServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final String ISSUE_COUNT = "Issue Count";
	private static final String HISTORY = "history";
	private static final String OVERALL = "Overall";

	@Autowired
	ConfigHelperService configHelperService;

	@Autowired
	CustomApiConfig customApiConfig;

	@Autowired
	JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	KpiHelperService kpiHelperService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getQualifierType() {
		return KPICode.FLOW_EFFICIENCY.name();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		log.info("FLOW-EFFICIENCY {}", kpiRequest.getRequestTrackerId());
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {

			Filters filters = Filters.getFilter(k);
			if (Filters.PROJECT == filters) {
				projectWiseLeafNodeValue(kpiElement, mapTmp, v, kpiRequest);
			}
		});

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();

		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.FLOW_EFFICIENCY);
		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.FLOW_EFFICIENCY);

		Map<String, Map<String, List<DataCount>>> priorityTypeProjectWiseDc = new LinkedHashMap<>();
		trendValuesMap.forEach((priority, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			priorityTypeProjectWiseDc.put(priority, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		priorityTypeProjectWiseDc.forEach((filter, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.entrySet().forEach(trend -> dataList.addAll(trend.getValue()));
			dataCountGroup.setFilter(filter);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});
		kpiElement.setTrendValueList(dataCountGroups);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);

		return kpiElement;
	}

	/**
	 *
	 * @param kpiElement
	 *			kpiElement
	 * @param mapTmp
	 *			mapTmp
	 * @param leafNodeList
	 * 			leaf
	 * @param kpiRequest
	 * 			kpiRequest
	 */
	private void projectWiseLeafNodeValue(KpiElement kpiElement, Map<String, Node> mapTmp, List<Node> leafNodeList,
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		List<String> rangeList = customApiConfig.getFlowEfficiencyXAxisRange();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		FieldMapping fieldMapping = leafNode != null
				? configHelperService.getFieldMappingMap().get(leafNode.getProjectFilter().getBasicProjectConfigId())
				: new FieldMapping();

		String startDate = LocalDate.now().minusMonths(6).toString();
		String endDate = LocalDate.now().toString();

		Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, startDate, endDate, kpiRequest);
		List<JiraIssueCustomHistory> allIssueHistory = (List<JiraIssueCustomHistory>) resultMap.get(HISTORY);

		Map<String, Map<String, List<JiraIssueCustomHistory>>> rangeAndStatusWiseJiraIssueMap = new LinkedHashMap<>();
		List<String> waitTimeList = new ArrayList<>();
		List<String> totalTimeList = new ArrayList<>();
		LinkedHashMap<JiraIssueCustomHistory, Double> flowEfficiencyMap = new LinkedHashMap<>();
		filterDataBasedOnXAxisRangeWise(rangeList, allIssueHistory, rangeAndStatusWiseJiraIssueMap, flowEfficiencyMap,
				waitTimeList, totalTimeList, fieldMapping);
		LinkedHashMap<String, List<DataCount>> dataCountMap = setDataCountMap(rangeAndStatusWiseJiraIssueMap,
				flowEfficiencyMap, leafNode);
		populateExcelDataObject(requestTrackerId, excelData, flowEfficiencyMap, waitTimeList, totalTimeList);
		if (leafNode != null)
			mapTmp.get(leafNode.getId()).setValue(dataCountMap);
		
		List<String> xAxisRange = new ArrayList<>(rangeList);
		Collections.reverse(xAxisRange);
		kpiElement.setxAxisValues(xAxisRange);
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.FLOW_EFFICIENCY.getColumns());
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();

		List<String> basicProjectConfigIds = new ArrayList<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

			basicProjectConfigIds.add(basicProjectConfigId.toString());

			List<String> status = new ArrayList<>();
			if (Optional.ofNullable(fieldMapping.getJiraIssueClosedStateKPI170()).isPresent()) {
				status.addAll(fieldMapping.getJiraIssueClosedStateKPI170());
			}

			mapOfProjectFilters.put("statusUpdationLog.story.changedTo", CommonUtils.convertToPatternList(status));
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		});
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		resultListMap.put(HISTORY, jiraIssueCustomHistoryRepository
				.findByFilterAndFromStatusMapWithDateFilter(mapOfFilters, uniqueProjectMap, startDate, endDate));

		return resultListMap;
	}

	/**
	 * Sort jira issues by x-axis range and issue type and populate flow efficiency
	 * map
	 * 
	 * @param xAxisRange
	 * 			x axis data points
	 * @param projectWiseJiraIssueList
	 * 			list of jiraIssueCustomHistory
	 * @param rangeWiseJiraIssuesMap
	 * 			map of jira issues by data points
	 * @param flowEfficiencyMap
	 * 			map of jira issue and flow efficiency
	 * @param waitTimeList
	 * 			list of wait time per issue
	 * @param totalTimeList
	 * 			list of total time per issue
	 * @param fieldMapping
	 * 			field mapping
	 */
	private void filterDataBasedOnXAxisRangeWise(List<String> xAxisRange,
			List<JiraIssueCustomHistory> projectWiseJiraIssueList,
			Map<String, Map<String, List<JiraIssueCustomHistory>>> rangeWiseJiraIssuesMap,
			LinkedHashMap<JiraIssueCustomHistory, Double> flowEfficiencyMap, List<String> waitTimeList,
			List<String> totalTimeList, FieldMapping fieldMapping) {
		Map<Long, String> monthRangeMap = new HashMap<>();
		BacklogKpiHelper.initializeRangeMapForProjects(rangeWiseJiraIssuesMap, xAxisRange, monthRangeMap);

		projectWiseJiraIssueList.forEach(issue -> {
			if (!flowEfficiencyMap.containsKey(issue))
				calculateFlowEfficiency(issue, fieldMapping, waitTimeList, totalTimeList, flowEfficiencyMap,
						monthRangeMap, rangeWiseJiraIssuesMap);
		});
	}



	/**
	 * calculate flow efficiency for each jira issue
	 * 
	 * @param issueCustomHistory
	 * 			jira issue custom history
	 * @param fieldMapping
	 * 			field mapping
	 * @param waitTimeList
	 * 			list of wait time per issue
	 * @param totalTimeList
	 * 			list of total time per issue
	 * @param flowEfficiencyMap
	 * 			map of jira issue and flow efficiency
	 */
	private void calculateFlowEfficiency(JiraIssueCustomHistory issueCustomHistory, FieldMapping fieldMapping,
			List<String> waitTimeList, List<String> totalTimeList,
			LinkedHashMap<JiraIssueCustomHistory, Double> flowEfficiencyMap, Map<Long, String> monthRangeMap,
			Map<String, Map<String, List<JiraIssueCustomHistory>>> rangeWiseJiraIssuesMap) {
		List<JiraHistoryChangeLog> statusUpdateLog = issueCustomHistory.getStatusUpdationLog();
		long waitedTime = 0;
		long totalTime = 0;
		LocalDateTime closedDate = null;
		if (issueCustomHistory.getCreatedDate() != null) {
			for (int i = 0; i < statusUpdateLog.size() - 1; i++) {
				JiraHistoryChangeLog currentChangelog = statusUpdateLog.get(i);
				JiraHistoryChangeLog nextChangeLog = statusUpdateLog.get(i + 1);

				if (fieldMapping.getJiraIssueWaitStateKPI170().contains(currentChangelog.getChangedTo())) {
					waitedTime += calculateWaitedTime(currentChangelog.getUpdatedOn(), nextChangeLog.getUpdatedOn());
				}
				if (fieldMapping.getJiraIssueClosedStateKPI170().contains(nextChangeLog.getChangedTo())) {
					closedDate = nextChangeLog.getUpdatedOn();
					totalTime = calculateWaitedTime(
							LocalDateTime.ofInstant(issueCustomHistory.getCreatedDate().toDate().toInstant(),
									ZoneId.systemDefault()),
							closedDate);
				}
			}

			if (closedDate != null && totalTime != 0) {
				BacklogKpiHelper.setRangeWiseJiraIssuesMap(rangeWiseJiraIssuesMap, issueCustomHistory, closedDate, monthRangeMap);
				double flowEfficiency = calculatePercentage(waitedTime, totalTime);
				waitTimeList.add(kpiHelperService.convertHoursToDaysString(waitedTime));
				totalTimeList.add(kpiHelperService.convertHoursToDaysString(totalTime));
				flowEfficiencyMap.put(issueCustomHistory, flowEfficiency);
			}
		}
	}

	/**
	 * calculate time between two dates excluding weekends
	 * 
	 * @param start
	 * 			start date
	 * @param end
	 * 			end date
	 * @return hours between start date and end date
	 */
	private long calculateWaitedTime(LocalDateTime start, LocalDateTime end) {
		return HOURS.between(start, end) - kpiHelperService.minusHoursOfWeekEndDays(start, end);
	}



	/**
	 *
	 * @param waitedTime
	 * 			wait time
	 * @param totalTime
	 * 		total time
	 * @return percentage of wait time byt total time
	 */
	private double calculatePercentage(long waitedTime, long totalTime) {
		double wait = kpiHelperService.getTimeInWorkHours(waitedTime);
		double total = kpiHelperService.getTimeInWorkHours(totalTime);
		return (1 - (wait / total)) * 100;
	}


	public double calculateAverage(List<JiraIssueCustomHistory> jiraIssueList,
			Map<JiraIssueCustomHistory, Double> flowEfficiencyMap) {
		return jiraIssueList.stream().filter(flowEfficiencyMap::containsKey).mapToDouble(flowEfficiencyMap::get)
				.average().orElse(0.0);
	}

	/**
	 * create trendValueMap of DataCounts
	 * 
	 * @param rangeAndStatusWiseJiraIssueMap
	 * 			map of jira issue by range
	 * @param flowEfficiencyMap
	 * 			map of flow efficiency by issue
	 * @param leafNode
	 * 			project node
	 * @return data count map by filter
	 */
	private LinkedHashMap<String, List<DataCount>> setDataCountMap(
			Map<String, Map<String, List<JiraIssueCustomHistory>>> rangeAndStatusWiseJiraIssueMap,
			LinkedHashMap<JiraIssueCustomHistory, Double> flowEfficiencyMap, Node leafNode) {
		LinkedHashMap<String, List<DataCount>> dataCountMap = new LinkedHashMap<>();
		List<String> totalIssueTypeString = rangeAndStatusWiseJiraIssueMap.values().stream()
				.flatMap(innerMap -> innerMap.values().stream().flatMap(List::stream))
				.map(JiraIssueCustomHistory::getStoryType).distinct().collect(Collectors.toList());
		rangeAndStatusWiseJiraIssueMap.forEach((dateRange, statusWiseJiraIssues) -> {
			totalIssueTypeString.forEach(issueType -> {
				List<JiraIssueCustomHistory> typeWiseIssues = statusWiseJiraIssues.getOrDefault(issueType,
						new ArrayList<>());
				double average = calculateAverage(typeWiseIssues, flowEfficiencyMap);
				setDataCount(leafNode.getProjectFilter().getName(), issueType, dateRange, average, dataCountMap,
						typeWiseIssues.size());
			});
			List<JiraIssueCustomHistory> totalRangeWiseIssues = statusWiseJiraIssues.values().stream()
					.flatMap(List::stream).collect(Collectors.toList());
			double average = calculateAverage(totalRangeWiseIssues, flowEfficiencyMap);
			setDataCount(leafNode.getProjectFilter().getName(), OVERALL, dateRange, average, dataCountMap,
					totalRangeWiseIssues.size());
		});

		return dataCountMap;
	}

	/**
	 * create DataCount by Filter
	 * 
	 * @param projectName
	 * 			project name
	 * @param filter
	 * 			filter
	 * @param dateRange
	 * 			date range
	 * @param value
	 * 			average flow efficiency
	 * @param dataCountMap
	 * 			data count by filter
	 * @param count
	 * 			no of issues for range
	 */
	public void setDataCount(String projectName, String filter, String dateRange, double value,
			Map<String, List<DataCount>> dataCountMap, int count) {
		Map<String, Object> hoverMap = new HashMap<>();
		hoverMap.put(ISSUE_COUNT, count);
		DataCount dataCount = new DataCount();
		dataCount.setValue(value);
		dataCount.setData(String.valueOf(value));
		dataCount.setSSprintName(dateRange);
		dataCount.setSSprintID(dateRange);
		dataCount.setDate(dateRange);
		dataCount.setSProjectName(projectName);
		dataCount.setKpiGroup(filter);
		dataCount.setHoverValue(hoverMap);
		dataCountMap.computeIfAbsent(filter, k -> new ArrayList<>()).add(dataCount);
	}

	/**
	 * populate excel data
	 * 
	 * @param requestTrackerId
	 * 			tracker id for kpi request
	 * @param excelData
	 * 			excel data
	 * @param flowEfficiencyMap
	 *			map of flow efficiency by jira issue
	 * @param waitTimeList
	 * 			wait time list
	 * @param totalTimeList
	 * 			total time list
	 */
	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			LinkedHashMap<JiraIssueCustomHistory, Double> flowEfficiencyMap, List<String> waitTimeList,
			List<String> totalTimeList) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateFlowEfficiency(flowEfficiencyMap, waitTimeList, totalTimeList, excelData);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI170(), KPICode.FLOW_EFFICIENCY.getKpiId());
	}

}
