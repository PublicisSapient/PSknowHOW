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
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
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
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("javadoc")
@Service
@Slf4j
public class CostOfDelayServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String COD_DATA = "costOfDelayData";
	private static final String COD_DATA_HISTORY = "costOfDelayDataHistory";
	private static final String FIELD_MAPPING = "fieldMapping";
	
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private FilterHelperService flterHelperService;
	@Autowired
	private CacheService cacheService;
	@Autowired
	private KpiDataCacheService kpiDataCacheService;

	@Override
	public Double calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return null;
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		List<DataCount> trendValueList = new ArrayList<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {

			Filters filters = Filters.getFilter(k);
			if (Filters.PROJECT == filters) {
				projectWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, getRequestTrackerId(), kpiRequest);
			}

		});

		log.debug("[PROJECT-WISE][{}]. Values of leaf node after KPI calculation {}", kpiRequest.getRequestTrackerId(),
				root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.COST_OF_DELAY);
		// 3rd change : remove code to set trendValuelist and call
		// getTrendValues method
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue, KPICode.COST_OF_DELAY);
		kpiElement.setTrendValueList(trendValues);

		return kpiElement;
	}

	/**
	 * Fetches KPI data from the database for the given list of project nodes.
	 *
	 * @param leafNodeList the list of project nodes
	 * @param startDate the start date for the KPI data (not used in this implementation)
	 * @param endDate the end date for the KPI data (not used in this implementation)
	 * @param kpiRequest the KPI request object containing additional request details
	 * @return a map containing the fetched KPI data, with keys "costOfDelayData" and "costOfDelayDataHistory"
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, Object> resultListMap = new HashMap<>();
		List<ObjectId> basicProjectConfigIds = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(leafNodeList)) {
			leafNodeList.forEach(leaf -> {
				ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
				basicProjectConfigIds.add(basicProjectConfigId);
			});

		}
		List<JiraIssue> codList = new ArrayList<>();
		List<JiraIssueCustomHistory> codHistory = new ArrayList<>();
		Map<String, List<String>> closedStatusMap = new HashMap<>();
		basicProjectConfigIds.forEach(basicProjectConfigId -> {
			Map<String, Object> result = kpiDataCacheService.fetchCostOfDelayData(basicProjectConfigId,
					KPICode.COST_OF_DELAY.getKpiId());
			codList.addAll((List<JiraIssue>) result.get(COD_DATA));
			codHistory.addAll((List<JiraIssueCustomHistory>) result.get(COD_DATA_HISTORY));
			closedStatusMap.putAll((Map<String, List<String>>) result.get(FIELD_MAPPING));
		});
		resultListMap.put(COD_DATA, codList);
		resultListMap.put(COD_DATA_HISTORY, codHistory);
		resultListMap.put(FIELD_MAPPING, closedStatusMap);
		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.COST_OF_DELAY.name();
	}

	/**
	 * Calculate KPI value for selected project nodes.
	 *
	 * @param projectLeafNodeList
	 *            list of sprint leaf nodes
	 * @param trendValueList
	 *            list containing data to show on KPI
	 * @param kpiElement
	 *            kpiElement
	 * @param kpiRequest
	 *            KpiRequest
	 */
	private void projectWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> projectLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, String requestTrackerId, KpiRequest kpiRequest) {

		Map<String, Object> resultMap = fetchKPIDataFromDb(projectLeafNodeList, null, null, kpiRequest);
		List<KPIExcelData> excelData = new ArrayList<>();

		projectLeafNodeList.forEach(
				node -> setProjectNodeValue(mapTmp, node, resultMap, trendValueList, requestTrackerId, excelData));
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(
				KPIExcelColumn.COST_OF_DELAY.getColumns(projectLeafNodeList, cacheService, flterHelperService));
	}

	/**
	 * Sets the KPI value for a project node.
	 *
	 * @param mapTmp
	 *            a map containing the KPI values for each node
	 * @param node
	 *            the project node
	 * @param resultMap
	 *            a map containing the fetched KPI data
	 * @param trendValueList
	 *            a list containing the data to show on the KPI
	 * @param requestTrackerId
	 *            the request tracker ID
	 * @param excelData
	 *            a list containing the data to be shown in the Excel sheet
	 */
	@SuppressWarnings("unchecked")
	private void setProjectNodeValue(Map<String, Node> mapTmp, Node node, Map<String, Object> resultMap,
			List<DataCount> trendValueList, String requestTrackerId, List<KPIExcelData> excelData) {
		Map<String, Double> lastNMonthMap = getLastNMonth(customApiConfig.getJiraXaxisMonthCount());
		String projectName = node.getProjectFilter().getName();
		List<JiraIssue> epicList = new ArrayList<>();
		Map<String, Map<String, Integer>> howerMap = new HashMap<>();
		Map<String, List<JiraIssue>> filterWiseDataMap = createProjectWiseGrouping(
				(List<JiraIssue>) resultMap.get(COD_DATA), JiraIssue::getBasicProjectConfigId);
		Map<String, List<JiraIssueCustomHistory>> filterWiseHistoryDataMap = createProjectWiseGrouping(
				(List<JiraIssueCustomHistory>) resultMap.get(COD_DATA_HISTORY),
				JiraIssueCustomHistory::getBasicProjectConfigId);
		Map<String, List<String>> fieldMappingMap = (Map<String, List<String>>) resultMap.get(FIELD_MAPPING);
		List<JiraIssue> jiraIssues = filterWiseDataMap
				.get(node.getProjectFilter().getBasicProjectConfigId().toString());
		List<JiraIssueCustomHistory> jiraIssueCustomHistories = filterWiseHistoryDataMap
				.get(node.getProjectFilter().getBasicProjectConfigId().toString());
		List<String> closedStatues = fieldMappingMap.get(node.getProjectFilter().getBasicProjectConfigId().toString());
		if (CollectionUtils.isEmpty(jiraIssues) || CollectionUtils.isEmpty(jiraIssueCustomHistories)) {
			return;
		}
		for (JiraIssue js : jiraIssues) {
			String number = js.getNumber();
			Optional<String> epicEndDateOpt = jiraIssueCustomHistories.stream()
					.filter(jiraIssueCustomHistory -> jiraIssueCustomHistory.getStoryID().equals(number)).findFirst()
					.flatMap(jiraIssueCustomHistory -> jiraIssueCustomHistory.getStatusUpdationLog().stream()
							.filter(jiraHistoryChangeLog -> closedStatues
									.contains(jiraHistoryChangeLog.getChangedTo().toLowerCase()))
							.findFirst().map(jiraHistoryChangeLog -> jiraHistoryChangeLog.getUpdatedOn().toString()));
			String epicEndDate = epicEndDateOpt.orElse(null);
			js.setEpicEndDate(epicEndDate);
			if (epicEndDate != null) {
				DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(DateUtil.DATE_FORMAT)
						.toFormatter();
				LocalDate dateValue = LocalDate.parse(epicEndDate.split("T")[0], formatter);
				String date = dateValue.getYear() + Constant.DASH + dateValue.getMonthValue();
				lastNMonthMap.computeIfPresent(date, (key, value) -> {
					epicList.add(js);
					Integer costOfDelay = (int) js.getCostOfDelay();
					Map<String, Integer> epicWiseCost = new HashMap<>();
					epicWiseCost.put(number, costOfDelay);
					if (howerMap.containsKey(date)) {
						epicWiseCost.putAll(howerMap.get(date));
						howerMap.put(date, epicWiseCost);
					} else {
						howerMap.put(date, epicWiseCost);
					}
					return value + costOfDelay;
				});

			}

		}

		List<DataCount> dcList = new ArrayList<>();
		lastNMonthMap.forEach((k, v) -> {
			DataCount dataCount = new DataCount();
			dataCount.setDate(k);
			dataCount.setValue(v);
			dataCount.setData(v.toString());
			dataCount.setSProjectName(projectName);
			dataCount.setHoverValue(new HashMap<>());
			dcList.add(dataCount);
			trendValueList.add(dataCount);

		});
		mapTmp.get(node.getId()).setValue(dcList);

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateCODExcelData(projectName, epicList, excelData);
		}

	}

	/**
	 * Groups a list of items by a project ID extracted using the provided function.
	 *
	 * @param <T> the type of items in the list
	 * @param resultList the list of items to be grouped
	 * @param projectIdExtractor a function to extract the project ID from an item
	 * @return a map where the keys are project IDs and the values are lists of items associated with those project IDs
	 */
	private <T> Map<String, List<T>> createProjectWiseGrouping(List<T> resultList,
			Function<T, String> projectIdExtractor) {
		return resultList.stream().filter(p -> projectIdExtractor.apply(p) != null)
				.collect(Collectors.groupingBy(projectIdExtractor));
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI113(), KPICode.COST_OF_DELAY.getKpiId());
	}

}
