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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
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
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

import lombok.extern.slf4j.Slf4j;

/**
 * This class calculates the Sprint capacity.
 *
 * @author pkum34
 */
@Component
@Slf4j
public class SprintCapacityServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	public static final String UNCHECKED = "unchecked";
	private static final String SEPARATOR_ASTERISK = "*************************************";
	private static final String SPRINTCAPACITYKEY = "sprintCapacityKey";
	private static final String ESTIMATE_TIME = "Estimate_Time";
	private static final String ESTIMATED_HOURS = "Estimated Hours";
	private static final String LOGGED_HOURS = "Logged Work";
	private final DecimalFormat df2 = new DecimalFormat(".##");
	@Autowired
	private KpiHelperService kpiHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;

	/**
	 * Gets Qualifier Type
	 *
	 * @return KPICode's <tt>SPRINT_CAPACITY</tt> enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.SPRINT_CAPACITY_UTILIZATION.name();
	}

	/**
	 * Gets KPI Data
	 *
	 * @param kpiRequest
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @return KpiElement
	 * @throws ApplicationException
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		List<DataCount> trendValueList = new ArrayList<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);
			}
		});

		log.debug("[SPRINT-CAPACITY-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.SPRINT_CAPACITY_UTILIZATION);
		// 3rd change : remove code to set trendValuelist and call
		// getTrendValues method
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue, KPICode.SPRINT_CAPACITY_UTILIZATION);
		kpiElement.setTrendValueList(trendValues);
		return kpiElement;
	}

	/**
	 * Fetches KPI Data from DB
	 *
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @param kpiRequest
	 * @return {@code Map<String, Object>}
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, Object> resultListMap = new HashMap<>();
		List<JiraIssue> sprintCapacityList = kpiHelperService.fetchSprintCapacityDataFromDb(leafNodeList);
		List<CapacityKpiData> estimateTimeList = kpiHelperService.fetchCapacityDataFromDB(leafNodeList);
		setDbQueryLogger(sprintCapacityList);
		resultListMap.put(SPRINTCAPACITYKEY, sprintCapacityList);
		resultListMap.put(ESTIMATE_TIME, estimateTimeList);
		return resultListMap;

	}

	/**
	 * @param sprintCapacityMap
	 * @return timeLogged in seconds
	 */
	@SuppressWarnings(UNCHECKED)
	@Override
	public Double calculateKPIMetrics(Map<String, Object> sprintCapacityMap) {
		String requestTrackerId = getRequestTrackerId();
		Double timeLoggedInMinutes = 0.0d;
		List<JiraIssue> sprintCapacityList = (List<JiraIssue>) sprintCapacityMap.get(SPRINTCAPACITYKEY);
		log.debug("[SPRINT-CAPACITY][{}]. Stories Count: {}", requestTrackerId, sprintCapacityList.size());
		if (CollectionUtils.isNotEmpty(sprintCapacityList)) {
			for (JiraIssue jiraIssue : sprintCapacityList) {
				if (jiraIssue.getTimeSpentInMinutes() != null) {
					timeLoggedInMinutes = timeLoggedInMinutes + jiraIssue.getTimeSpentInMinutes();
				}
			}
		}
		log.debug("[SPRINT-CAPACITY][{}]. Logged time: {}", requestTrackerId, timeLoggedInMinutes / 60);
		return timeLoggedInMinutes / 60;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint wise.
	 *
	 * @param mapTmp
	 * @param kpiElement
	 * @param sprintLeafNodeList
	 * @param trendValueList
	 * @param kpiRequest
	 */
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		long time = System.currentTimeMillis();
		Map<String, Object> sprintCapacityStoryMap = fetchKPIDataFromDb(sprintLeafNodeList, null, null, kpiRequest);
		log.info("Sprint Capacity taking fetchKPIDataFromDb {}", String.valueOf(System.currentTimeMillis() - time));

		Map<Pair<String, String>, Double> sprintWiseEstimateTimeMap = new HashMap<>();

		Map<Pair<String, String>, List<JiraIssue>> sprintWiseLoggedTimeMap = prepareMapForSprintAndFilters(
				sprintWiseEstimateTimeMap, kpiRequest, sprintCapacityStoryMap);

		List<KPIExcelData> excelData = new ArrayList<>();

		sprintLeafNodeList.forEach(node -> {
			// Leaf node wise data
			String currentSprintComponentId = node.getSprintFilter().getId();
			String trendLineName = node.getProjectFilter().getName();

			Map<String, Object> hoverValue = new HashMap<>();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), currentSprintComponentId);
			Pair<String, String> currentNodeEstimateTime = Pair.of(
					node.getProjectFilter().getBasicProjectConfigId().toString(),
					currentSprintComponentId.toLowerCase());

			double estimateTimeForCurrentLeaf = 0.0d;
			if (null != sprintWiseEstimateTimeMap.get(currentNodeEstimateTime)) {
				estimateTimeForCurrentLeaf = sprintWiseEstimateTimeMap.get(currentNodeEstimateTime);
			}
			double loggedTimeForCurrentLeaf = 0.0;
			if (CollectionUtils.isNotEmpty(sprintWiseLoggedTimeMap.get(currentNodeIdentifier))) {
				Map<String, Object> currentSprintLeafCapacityMap = new HashMap<>();
				currentSprintLeafCapacityMap.put(SPRINTCAPACITYKEY, sprintWiseLoggedTimeMap.get(currentNodeIdentifier));
				loggedTimeForCurrentLeaf = Double
						.valueOf(df2.format(calculateKPIMetrics(currentSprintLeafCapacityMap)));

				List<JiraIssue> sprintJiraIssues = sprintWiseLoggedTimeMap.get(currentNodeIdentifier);
				populateExcelDataObject(requestTrackerId, excelData, sprintJiraIssues, node);
			}
			hoverValue.put(ESTIMATED_HOURS, (int) estimateTimeForCurrentLeaf);
			hoverValue.put(LOGGED_HOURS, (int) loggedTimeForCurrentLeaf);
			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(estimateTimeForCurrentLeaf));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setValue(estimateTimeForCurrentLeaf);
			dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
			dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
			dataCount.setLineValue(loggedTimeForCurrentLeaf);
			dataCount.setHoverValue(hoverValue);
			trendValueList.add(dataCount);
			mapTmp.get(node.getId()).setValue(new ArrayList<>(Arrays.asList(dataCount)));
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.SPRINT_CAPACITY_UTILIZATION.getColumns());
	}

	/**
	 * Prepares Map for Sprint and Filters
	 *
	 * @param sprintWiseEstimateTimeMap
	 * @param kpiRequest
	 * @param sprintCapacityStoryMap
	 * @return Map<Pair < String, String>, List<Feature>>
	 */
	@SuppressWarnings(UNCHECKED)
	private Map<Pair<String, String>, List<JiraIssue>> prepareMapForSprintAndFilters(
			Map<Pair<String, String>, Double> sprintWiseEstimateTimeMap, KpiRequest kpiRequest,
			Map<String, Object> sprintCapacityStoryMap) {

		Map<Pair<String, String>, List<JiraIssue>> loggedTimeMap;
		Map<Pair<String, String>, Double> estimateTimeMap;

		loggedTimeMap = ((List<JiraIssue>) sprintCapacityStoryMap.get(SPRINTCAPACITYKEY)).stream()
				.collect(Collectors.groupingBy(
						feature -> Pair.of(feature.getBasicProjectConfigId(), feature.getSprintID()),
						Collectors.toList()));
		estimateTimeMap = ((List<CapacityKpiData>) sprintCapacityStoryMap.get(ESTIMATE_TIME)).stream()
				.collect(Collectors.toMap(
						key -> Pair.of(key.getBasicProjectConfigId().toString(), key.getSprintID().toLowerCase()),
						CapacityKpiData::getCapacityPerSprint, (val1, val2) -> val1 + val2));

		sprintWiseEstimateTimeMap.putAll(estimateTimeMap);
		return loggedTimeMap;
	}

	/**
	 * Populates validation data node of the KPI element.
	 *
	 * @param requestTrackerId
	 * @param excelData
	 * @param sprintCapacityList
	 * @param node
	 */
	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> sprintCapacityList, Node node) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			String sprintName = node.getSprintFilter().getName();

			KPIExcelUtility.populateSprintCapacity(sprintName, sprintCapacityList, excelData);
		}
	}

	/**
	 * Sets DB Query log
	 *
	 * @param storyFeatureList
	 */
	private void setDbQueryLogger(List<JiraIssue> storyFeatureList) {

		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.info(SEPARATOR_ASTERISK);
			log.info("************* Sprint Capacity (dB) *******************");
			if (null != storyFeatureList && !storyFeatureList.isEmpty()) {
				List<String> storyIdList = storyFeatureList.stream().map(JiraIssue::getNumber)
						.collect(Collectors.toList());
				log.info("Story[{}]: {}", storyIdList.size(), storyIdList);
			}
			log.info(SEPARATOR_ASTERISK);
			log.info("******************X----X*******************");
		}
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI46(),
				KPICode.SPRINT_CAPACITY_UTILIZATION.getKpiId());
	}

}
