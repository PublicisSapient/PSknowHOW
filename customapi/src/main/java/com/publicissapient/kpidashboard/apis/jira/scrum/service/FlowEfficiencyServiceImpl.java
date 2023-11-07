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

import static java.time.temporal.ChronoUnit.DAYS;
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
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.JiraFeatureHistory;
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
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
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
	private static final String MONTH = "Month";

	@Autowired
	ConfigHelperService configHelperService;

	@Autowired
	CustomApiConfig customApiConfig;

	@Autowired
	JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Override
	public String getQualifierType() {
		return KPICode.FLOW_EFFICIENCY.name();
	}

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
			projectWiseDc.entrySet().stream().forEach(trend -> dataList.addAll(trend.getValue()));
			dataCountGroup.setFilter(filter);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});
		kpiElement.setTrendValueList(dataCountGroups);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);

		return kpiElement;
	}

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

		LinkedHashMap<String, List<DataCount>> trendValueMap = calculateTrendValues(rangeAndStatusWiseJiraIssueMap,
				flowEfficiencyMap, leafNode);
		populateExcelDataObject(requestTrackerId, excelData, flowEfficiencyMap, waitTimeList, totalTimeList);
		if (leafNode != null)
			mapTmp.get(leafNode.getId()).setValue(trendValueMap);
		Collections.reverse(rangeList);
		kpiElement.setxAxisValues(rangeList);
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

			if (Optional.ofNullable(fieldMapping.getJiraIssueTypeKPI3()).isPresent()) {

				KpiDataHelper.prepareFieldMappingDefectTypeTransformation(mapOfProjectFilters,
						fieldMapping.getJiradefecttype(), fieldMapping.getJiraIssueTypeKPI3(),
						JiraFeatureHistory.STORY_TYPE.getFieldValueInFeature());
				uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

			}
			List<String> status = new ArrayList<>();
			if (Optional.ofNullable(fieldMapping.getJiraIssueClosedState170()).isPresent()) {
				status.addAll(fieldMapping.getJiraIssueClosedState170());
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
	 * @param projectWiseJiraIssueList
	 * @param rangeWiseJiraIssuesMap
	 * @param flowEfficiencyMap
	 * @param waitTimeList
	 * @param totalTimeList
	 * @param fieldMapping
	 */
	private void filterDataBasedOnXAxisRangeWise(List<String> xAxisRange,
			List<JiraIssueCustomHistory> projectWiseJiraIssueList,
			Map<String, Map<String, List<JiraIssueCustomHistory>>> rangeWiseJiraIssuesMap,
			LinkedHashMap<JiraIssueCustomHistory, Double> flowEfficiencyMap, List<String> waitTimeList,
			List<String> totalTimeList, FieldMapping fieldMapping) {
		Map<Long, String> monthRangeMap = new HashMap<>();
		initializeRangeMapForProjects(rangeWiseJiraIssuesMap, xAxisRange, monthRangeMap);

		projectWiseJiraIssueList.forEach(issue -> {
			long daysBetween = DAYS.between(KpiDataHelper.convertStringToDate(issue.getCreatedDate().toString()),
					LocalDate.now());
			monthRangeMap.forEach((noOfDay, range) -> {
				if (noOfDay > daysBetween) {
					rangeWiseJiraIssuesMap.computeIfAbsent(range, k -> new HashMap<>())
							.computeIfAbsent(issue.getStoryType(), k -> new ArrayList<>()).add(issue);
					if (!flowEfficiencyMap.containsKey(issue))
						calculateFlowEfficiency(issue, fieldMapping, waitTimeList, totalTimeList, flowEfficiencyMap);
				}
			});
		});
	}

	/**
	 * create x-axis range map with duration
	 * 
	 * @param rangeWiseJiraIssuesMap
	 * @param xAxisRange
	 * @param monthRangeMap
	 */
	private void initializeRangeMapForProjects(
			Map<String, Map<String, List<JiraIssueCustomHistory>>> rangeWiseJiraIssuesMap, List<String> xAxisRange,
			Map<Long, String> monthRangeMap) {
		LocalDateTime currentDate = LocalDateTime.now();
		xAxisRange.forEach(range -> {
			String[] rangeSplitted = range.trim().split(" ");
			if (rangeSplitted[2].contains(MONTH)) {
				monthRangeMap.put(
						DAYS.between(currentDate.minusMonths(Integer.parseInt(rangeSplitted[1])), currentDate), range);
			} else {
				monthRangeMap.put(DAYS.between(currentDate.minusWeeks(Integer.parseInt(rangeSplitted[1])), currentDate),
						range);
			}
			rangeWiseJiraIssuesMap.put(range, new HashMap<>());
		});
	}

	/**
	 * calculate flow efficiency for each jira issue
	 * 
	 * @param issueCustomHistory
	 * @param fieldMapping
	 * @param waitTimeList
	 * @param totalTimeList
	 * @param flowEfficiencyMap
	 */
	private void calculateFlowEfficiency(JiraIssueCustomHistory issueCustomHistory, FieldMapping fieldMapping,
			List<String> waitTimeList, List<String> totalTimeList,
			LinkedHashMap<JiraIssueCustomHistory, Double> flowEfficiencyMap) {
		List<JiraHistoryChangeLog> statusUpdationLog = issueCustomHistory.getStatusUpdationLog();
		long waitedTime = 0;
		long totalTime = 0;
		double flowEfficiency = 0;
		LocalDateTime closedDate = null;
		if (issueCustomHistory.getCreatedDate() != null) {
			for (int i = 0; i < statusUpdationLog.size() - 1; i++) {
				JiraHistoryChangeLog currentChangelog = statusUpdationLog.get(i);
				JiraHistoryChangeLog nextChangeLog = statusUpdationLog.get(i + 1);

				if (fieldMapping.getJiraIssueWaitState170().contains(currentChangelog.getChangedTo())) {
					waitedTime = calculateWaitedTime(currentChangelog.getUpdatedOn(), nextChangeLog.getUpdatedOn());
				}

				if (fieldMapping.getJiraIssueClosedState170().contains(nextChangeLog.getChangedTo())) {
					closedDate = nextChangeLog.getUpdatedOn();
					totalTime = calculateWaitedTime(
							LocalDateTime.ofInstant(issueCustomHistory.getCreatedDate().toDate().toInstant(),
									ZoneId.systemDefault()),
							closedDate);
				}
			}

			if (closedDate != null && totalTime != 0) {
				flowEfficiency = calculatePercentage(waitedTime, totalTime);
				waitTimeList.add(convertHoursToDaysString(waitedTime));
				totalTimeList.add(convertHoursToDaysString(totalTime));
				flowEfficiencyMap.put(issueCustomHistory, flowEfficiency);
			}
		}
	}

	/**
	 * calculate time between two dates excluding weekends
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	private long calculateWaitedTime(LocalDateTime start, LocalDateTime end) {
		return HOURS.between(start, end) - minusHoursOfWeekEndDays(start, end);
	}

	/**
	 * get weekend between two dates
	 * 
	 * @param d1
	 * @param d2
	 * @return
	 */
	public int minusHoursOfWeekEndDays(LocalDateTime d1, LocalDateTime d2) {
		int countOfWeekEndDays = saturdaySundayCount(d1, d2);
		if (countOfWeekEndDays != 0) {
			return countOfWeekEndDays * 24;
		} else {
			return 0;
		}
	}

	public int saturdaySundayCount(LocalDateTime d1, LocalDateTime d2) {
		int countWeekEnd = 0;
		while (!d1.isAfter(d2)) {
			if (isWeekEnd(d1)) {
				countWeekEnd++;
			}
			d1 = d1.plusDays(1);
		}
		return countWeekEnd;
	}

	public boolean isWeekEnd(LocalDateTime localDateTime) {
		int dayOfWeek = localDateTime.getDayOfWeek().getValue();
		return dayOfWeek == 6 || dayOfWeek == 7;
	}

	/**
	 *
	 * @param waitedTime
	 * @param totalTime
	 * @return
	 */
	private double calculatePercentage(long waitedTime, long totalTime) {
		double wait = getTimeInWorkHours(waitedTime);
		double total = getTimeInWorkHours(totalTime);
		return (1 - (wait / total)) * 100;
	}

	/**
	 * convert hours into work hours by 8 factor
	 * 
	 * @param timeInHours
	 * @return
	 */
	private long getTimeInWorkHours(long timeInHours) {
		long timeInHrs = (timeInHours / 24) * 8;
		long remainingTimeInMin = (timeInHours % 24);
		if (remainingTimeInMin >= 8) {
			timeInHrs = timeInHrs + 8;
		} else {
			timeInHrs = timeInHrs + remainingTimeInMin;
		}
		return timeInHrs;
	}

	/**
	 * convert total hours to days
	 * 
	 * @param hours
	 * @return
	 */
	private String convertHoursToDaysString(long hours) {
		hours = getTimeInWorkHours(hours);
		long days = hours / 8;
		long remainingHours = hours % 8;
		return String.format("%dd %dhrs", days, remainingHours);
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
	 * @param flowEfficiencyMap
	 * @param leafNode
	 * @return
	 */
	private LinkedHashMap<String, List<DataCount>> calculateTrendValues(
			Map<String, Map<String, List<JiraIssueCustomHistory>>> rangeAndStatusWiseJiraIssueMap,
			LinkedHashMap<JiraIssueCustomHistory, Double> flowEfficiencyMap, Node leafNode) {
		LinkedHashMap<String, List<DataCount>> trendValueMap = new LinkedHashMap<>();

		rangeAndStatusWiseJiraIssueMap.forEach((dateRange, statusWiseJiraIssues) -> {
			statusWiseJiraIssues.forEach((issueType, typeWiseIssues) -> {
				double average = calculateAverage(typeWiseIssues, flowEfficiencyMap);
				setTrendValueList(leafNode.getProjectFilter().getName(), issueType, dateRange, average, trendValueMap,
						typeWiseIssues.size());
			});
			List<JiraIssueCustomHistory> totalRangeWiseIssues = statusWiseJiraIssues.values().stream()
					.flatMap(List::stream).collect(Collectors.toList());
			double average = calculateAverage(totalRangeWiseIssues, flowEfficiencyMap);
			setTrendValueList(leafNode.getProjectFilter().getName(), OVERALL, dateRange, average, trendValueMap,
					totalRangeWiseIssues.size());
		});

		return trendValueMap;
	}

	/**
	 * create DataCount by Filter
	 * 
	 * @param projectName
	 * @param filter
	 * @param dateRange
	 * @param value
	 * @param trendValueMap
	 * @param count
	 */
	public void setTrendValueList(String projectName, String filter, String dateRange, double value,
			Map<String, List<DataCount>> trendValueMap, int count) {
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
		trendValueMap.computeIfAbsent(filter, k -> new ArrayList<>()).add(dataCount);
	}

	/**
	 * populate excel data
	 * 
	 * @param requestTrackerId
	 * @param excelData
	 * @param flowEfficiencyMap
	 * @param waitTimeList
	 * @param totalTimeList
	 */
	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			LinkedHashMap<JiraIssueCustomHistory, Double> flowEfficiencyMap, List<String> waitTimeList,
			List<String> totalTimeList) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateFlowEfficiency(flowEfficiencyMap, waitTimeList, totalTimeList, excelData);
		}

	}

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

}
