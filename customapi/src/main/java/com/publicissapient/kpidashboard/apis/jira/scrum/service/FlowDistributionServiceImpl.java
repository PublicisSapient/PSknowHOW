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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FlowDistributionServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {
	public static final String BACKLOG_CUSTOM_HISTORY = "backlogCustomHistory";
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	// storyType have more than two word the stackChart hover fn break
	private static String combineType(String storyType) {
		// logic to combine multiple words into a single key
		return storyType.replaceAll("\\s+", "-");
	}

	@Override
	public String getQualifierType() {
		return KPICode.FLOW_DISTRIBUTION.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		List<DataCount> trendValueList = new ArrayList<>();
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
			Filters filters = Filters.getFilter(k);
			if (Filters.PROJECT == filters) {
				projectWiseLeafNodeValue(v, trendValueList, kpiElement, kpiRequest);
			}
		});
		log.info("FlowDistributionServiceImpl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);

		if (leafNode != null) {
			log.info("Flow Distribution kpi -> Requested project : {}", leafNode.getProjectFilter().getName());
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(leafNode.getProjectFilter().getBasicProjectConfigId());

			List<JiraIssueCustomHistory> jiraIssueCustomHistoryList = new ArrayList<>();

			if (CollectionUtils.isNotEmpty(fieldMapping.getJiraIssueTypeNamesKPI146())) {
				jiraIssueCustomHistoryList = getJiraIssuesCustomHistoryFromBaseClass();
				jiraIssueCustomHistoryList = jiraIssueCustomHistoryList.stream()
						.filter(jiraIssueCustomHistory -> fieldMapping.getJiraIssueTypeNamesKPI146()
								.contains(jiraIssueCustomHistory.getStoryType()))
						.collect(Collectors.toList());
			}

			resultListMap.put(BACKLOG_CUSTOM_HISTORY, new ArrayList<>(jiraIssueCustomHistoryList));
		}
		return resultListMap;
	}

	/**
	 * Populates KPI value to leaf nodes and gives the trend analysis at project
	 * level.
	 *
	 * @param leafNode
	 * @param trendValueList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(List<Node> leafNode, List<DataCount> trendValueList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		// this method fetch dates for past history data
		CustomDateRange dateRange = KpiDataHelper.getMonthsForPastDataHistory(customApiConfig.getFlowKpiMonthCount());

		// get start and end date in yyyy-mm-dd format
		String startDate = dateRange.getStartDate().format(DATE_FORMATTER);
		String endDate = dateRange.getEndDate().format(DATE_FORMATTER);

		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();

		Map<String, Object> resultMap = fetchKPIDataFromDb(leafNode, startDate, endDate, kpiRequest);

		List<JiraIssueCustomHistory> jiraIssueCustomHistories = (List<JiraIssueCustomHistory>) resultMap
				.get(BACKLOG_CUSTOM_HISTORY);

		if (CollectionUtils.isNotEmpty(jiraIssueCustomHistories)) {

			Map<String, Map<String, Integer>> groupByDateAndTypeCount = jiraIssueCustomHistories.stream()
					.collect(Collectors.groupingBy(issue -> issue.getCreatedDate().toString().split("T")[0],
							Collectors.groupingBy(issue -> combineType(issue.getStoryType()),
									Collectors.summingInt(issue -> 1))));

			// Sort the groupByDateAndTypeCount map by date in ascending order
			TreeMap<String, Map<String, Integer>> sortedByDateTypeCountMap = new TreeMap<>(groupByDateAndTypeCount);

			// Get the map from the start date or the immediate next date for modal window
			Map<String, Map<String, Integer>> mapAfterStartDate = sortedByDateTypeCountMap.entrySet().stream()
					.filter(entry -> entry.getKey().compareTo(startDate) >= 0).findFirst().map(Map.Entry::getKey)
					.map(sortedByDateTypeCountMap::tailMap).orElse(new TreeMap<>());

			// fetching the start date backlog type count
			Map<String, Integer> startDateTypeCount = startDateTypeCount(startDate, sortedByDateTypeCountMap);

			// adding start date backlog count for cumulativeAddition
			addStartDateTypeCount(startDate, sortedByDateTypeCountMap, startDateTypeCount);

			Map<String, Map<String, Integer>> cumulativeAddedCountMap = createCumulativeTypeCount(startDate, endDate,
					sortedByDateTypeCountMap);

			populateTrendValueList(trendValueList, cumulativeAddedCountMap);
			populateExcelDataObject(requestTrackerId, excelData, mapAfterStartDate);
			log.info("FlowDistributionServiceImpl -> request id : {} dateWiseCountMap : {}", requestTrackerId,
					cumulativeAddedCountMap);
		}
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.FLOW_DISTRIBUTION.getColumns());
		kpiElement.setTrendValueList(trendValueList);
	}

	/**
	 * for populating the trendValueList
	 *
	 * @param dataList
	 * @param dateTypeCountMap
	 */
	private void populateTrendValueList(List<DataCount> dataList, Map<String, Map<String, Integer>> dateTypeCountMap) {
		for (Map.Entry<String, Map<String, Integer>> entry : dateTypeCountMap.entrySet()) {
			String date = entry.getKey();
			Map<String, Integer> typeCountMap = entry.getValue();
			DataCount dc = new DataCount();
			dc.setDate(date);
			dc.setValue(typeCountMap);
			dataList.add(dc);
		}
	}

	/**
	 * populate the Excel for modal window
	 *
	 * @param requestTrackerId
	 * @param excelData
	 * @param dateTypeCountMap
	 */
	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			Map<String, Map<String, Integer>> dateTypeCountMap) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& !Objects.isNull(dateTypeCountMap)) {
			KPIExcelUtility.populateFlowKPI(dateTypeCountMap, excelData);
		}
	}

	/**
	 * Method to create cumulative type count from start date to end date
	 * 
	 * @param startDate
	 * @param endDate
	 * @param sortedByDateTypeCountMap
	 * @return
	 */
	private Map<String, Map<String, Integer>> createCumulativeTypeCount(String startDate, String endDate,
			TreeMap<String, Map<String, Integer>> sortedByDateTypeCountMap) {

		Map<String, Map<String, Integer>> cumulativeAddedCountMap = new LinkedHashMap<>();

		LocalDate currentDate = LocalDate.parse(startDate);
		LocalDate lastDate = LocalDate.parse(endDate);
		Map<String, Integer> accumulatedMap = null;

		while (!currentDate.isAfter(lastDate)) {
			String currentDateString = currentDate.toString();
			Map<String, Integer> currentMap = sortedByDateTypeCountMap.getOrDefault(currentDateString, new HashMap<>());

			if (accumulatedMap != null) {
				for (Map.Entry<String, Integer> entry : currentMap.entrySet()) {
					String key = entry.getKey();
					int value = entry.getValue();
					accumulatedMap.put(key, accumulatedMap.getOrDefault(key, 0) + value);
				}
			} else {
				accumulatedMap = new HashMap<>(currentMap);
			}
			cumulativeAddedCountMap.put(currentDateString, new HashMap<>(accumulatedMap));
			currentDate = currentDate.plusDays(1);
		}
		return cumulativeAddedCountMap;
	}

	/**
	 * For fetching start date backlog type count
	 * 
	 * @param startDate
	 * @param sortedByDateTypeCountMap
	 * @return
	 */
	private Map<String, Integer> startDateTypeCount(String startDate,
			TreeMap<String, Map<String, Integer>> sortedByDateTypeCountMap) {
		Map<String, Integer> startDateTypeCount = new HashMap<>();
		for (Map.Entry<String, Map<String, Integer>> entry : sortedByDateTypeCountMap.entrySet()) {
			String date = entry.getKey();
			Map<String, Integer> typeCountMap = entry.getValue();

			// If we've reached the start date, break
			if (date.compareTo(startDate) >= 0) {
				break;
			}
			// Otherwise, add up the type count for this date
			typeCountMap.forEach(
					(type, count) -> startDateTypeCount.put(type, startDateTypeCount.getOrDefault(type, 0) + count));
		}
		return startDateTypeCount;
	}

	/**
	 * Adding start date type count
	 * 
	 * @param startDate
	 * @param sortedByDateTypeCountMap
	 * @param tillStartDateTypeCount
	 */
	private void addStartDateTypeCount(String startDate, TreeMap<String, Map<String, Integer>> sortedByDateTypeCountMap,
			Map<String, Integer> tillStartDateTypeCount) {
		if (sortedByDateTypeCountMap.containsKey(startDate)) {
			Map<String, Integer> startDateTypeCount = sortedByDateTypeCountMap.get(startDate);

			for (Map.Entry<String, Integer> entry : tillStartDateTypeCount.entrySet()) {
				String key = entry.getKey();
				Integer value = entry.getValue();
				startDateTypeCount.put(key, startDateTypeCount.getOrDefault(key, 0) + value);
			}
		} else {
			sortedByDateTypeCountMap.put(startDate, tillStartDateTypeCount);
		}
	}

}
