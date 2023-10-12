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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
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
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
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
	private static final String LOGGED_HOURS = "Logged Hours";
	private static final String ORIGINAL = "Original";
	private static final String PLANNED = "Planned View";
	private static final String EXECUTED = "Execution View";
	private static final String AVAILABLE_CAPACITY = "Available Capacity";
	private final DecimalFormat df2 = new DecimalFormat(".##");
	@Autowired
	private KpiHelperService kpiHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private ConfigHelperService configHelperService;

	/**
	 * Gets KPI Data
	 *
	 * @param kpiRequest
	 *            kpiRequest
	 * @param kpiElement
	 *            kpiElement
	 * @param treeAggregatorDetail
	 *            treeAggregatorDetail
	 * @return KpiElement KpiElement
	 * @throws ApplicationException
	 *             kpiElement
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				sprintWiseLeafNodeValue(mapTmp, v, kpiElement, kpiRequest);
			}
		});

		log.debug("[SPRINT-CAPACITY-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.SPRINT_CAPACITY_UTILIZATION);
		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, nodeWiseKPIValue,
				KPICode.SPRINT_CAPACITY_UTILIZATION);

		Map<String, Map<String, List<DataCount>>> filterWiseProjectWiseDC = new LinkedHashMap<>();
		Map<String, List<DataCount>> unsortedMap = trendValuesMap.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
		unsortedMap.forEach((filterType, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			filterWiseProjectWiseDC.put(filterType, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		filterWiseProjectWiseDC.forEach((filter, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.forEach((key, value) -> dataList.addAll(value));
			dataCountGroup.setFilter(filter);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});

		kpiElement.setTrendValueList(dataCountGroups);
		return kpiElement;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint wise.
	 *
	 * @param mapTmp
	 *            mapTmp
	 * @param kpiElement
	 *            kpiElement
	 * @param sprintLeafNodeList
	 *            sprintLeafNodeList
	 * @param kpiRequest
	 *            kpiRequest
	 */
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();

		sprintLeafNodeList.sort(Comparator.comparing(node -> node.getSprintFilter().getStartDate()));

		Map<String, Object> sprintCapacityStoryMap = fetchKPIDataFromDb(sprintLeafNodeList, null, null, kpiRequest);

		Map<Pair<String, String>, Double> sprintWiseEstimateTimeMap = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseLoggedTimeMap = prepareMapForSprintAndFilters(
				sprintWiseEstimateTimeMap, sprintCapacityStoryMap);

		List<KPIExcelData> excelData = new ArrayList<>();

		sprintLeafNodeList.forEach(node -> {
			// Leaf node wise data
			String currentSprintComponentId = node.getSprintFilter().getId();
			String trendLineName = node.getProjectFilter().getName();
			Map<String, Object> capacityHowerMap = new HashMap<>();
			ObjectId basicProjectConfigId = node.getProjectFilter().getBasicProjectConfigId();
			Pair<String, String> currentNodeIdentifier = Pair.of(basicProjectConfigId.toString(),
					currentSprintComponentId);
			Pair<String, String> currentNodeEstimateTime = Pair.of(basicProjectConfigId.toString(),
					currentSprintComponentId.toLowerCase());
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

			double estimateTimeForCurrentLeaf = 0.0d;
			if (null != sprintWiseEstimateTimeMap.get(currentNodeEstimateTime)) {
				estimateTimeForCurrentLeaf = sprintWiseEstimateTimeMap.get(currentNodeEstimateTime);
			}

			List<JiraIssue> jiraIssueList = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(sprintWiseLoggedTimeMap.get(currentNodeIdentifier))) {
				jiraIssueList = sprintWiseLoggedTimeMap.get(currentNodeIdentifier);
				populateExcelDataObject(requestTrackerId, excelData, jiraIssueList, node, fieldMapping);
			}
			Map<String, Map<String, Double>> capacityMapFilter = sprintCapacityMapFilter(jiraIssueList,
					estimateTimeForCurrentLeaf, capacityHowerMap, fieldMapping);

			Map<String, List<DataCount>> dataCountMap = new HashMap<>();
			capacityMapFilter.forEach((key, value) -> {
				DataCount dataCount = new DataCount();
				dataCount.setData(String.valueOf(value.get(ESTIMATED_HOURS)));
				dataCount.setSProjectName(trendLineName);
				dataCount.setSSprintID(node.getSprintFilter().getId());
				dataCount.setSSprintName(node.getSprintFilter().getName());
				dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
				dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
				dataCount.setValue(value.get(ESTIMATED_HOURS));
				dataCount.setKpiGroup(key);
				dataCount.setLineValue(value.get(LOGGED_HOURS));
				dataCount.setHoverValue(getHoverMap(key, capacityHowerMap));
				dataCountMap.put(key, new ArrayList<>(Arrays.asList(dataCount)));
			});
			mapTmp.get(node.getId()).setValue(dataCountMap);
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.SPRINT_CAPACITY_UTILIZATION.getColumns());
	}

	/**
	 * Fetches KPI Data from DB
	 *
	 * @param leafNodeList
	 *            leafNodeList
	 * @param startDate
	 *            startDate
	 * @param endDate
	 *            endDate
	 * @param kpiRequest
	 *            kpiRequest
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
	 * Sets DB Query log
	 *
	 * @param storyFeatureList
	 *            storyFeatureList
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

	/**
	 * Prepares Map for Sprint and Filters
	 *
	 * @param sprintWiseEstimateTimeMap
	 *            sprintWiseEstimateTimeMap
	 * @param sprintCapacityStoryMap
	 *            sprintCapacityStoryMap
	 * @return Map<Pair < String, String>, List<Feature>>
	 */
	@SuppressWarnings(UNCHECKED)
	private Map<Pair<String, String>, List<JiraIssue>> prepareMapForSprintAndFilters(
			Map<Pair<String, String>, Double> sprintWiseEstimateTimeMap, Map<String, Object> sprintCapacityStoryMap) {
		Map<Pair<String, String>, List<JiraIssue>> loggedTimeMap = ((List<JiraIssue>) sprintCapacityStoryMap
				.get(SPRINTCAPACITYKEY))
						.stream()
						.collect(Collectors.groupingBy(
								feature -> Pair.of(feature.getBasicProjectConfigId(), feature.getSprintID()),
								Collectors.toList()));

		Map<Pair<String, String>, Double> estimateTimeMap = ((List<CapacityKpiData>) sprintCapacityStoryMap
				.get(ESTIMATE_TIME))
						.stream()
						.collect(Collectors.toMap(
								key -> Pair.of(key.getBasicProjectConfigId().toString(),
										key.getSprintID().toLowerCase()),
								CapacityKpiData::getCapacityPerSprint, Double::sum));

		sprintWiseEstimateTimeMap.putAll(estimateTimeMap);
		return loggedTimeMap;
	}

	/**
	 * Populates validation data node of the KPI element.
	 *
	 * @param requestTrackerId
	 *            requestTrackerId
	 * @param excelData
	 *            excelData
	 * @param sprintCapacityList
	 *            sprintCapacityList
	 * @param node
	 *            node
	 * @param fieldMapping
	 *            fieldMapping
	 */
	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> sprintCapacityList, Node node, FieldMapping fieldMapping) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			String sprintName = node.getSprintFilter().getName();
			KPIExcelUtility.populateSprintCapacity(sprintName, sprintCapacityList, excelData, fieldMapping);
		}
	}

	/**
	 * 
	 * @param createdIssues
	 *            createdIssues
	 * @param estimate
	 *            estimate
	 * @param sprintCapacityHowerMap
	 *            sprintCapacityHowerMap
	 * @param fieldMapping
	 *            fieldMapping
	 * @return map
	 */
	private Map<String, Map<String, Double>> sprintCapacityMapFilter(List<JiraIssue> createdIssues, Double estimate,

			Map<String, Object> sprintCapacityHowerMap, FieldMapping fieldMapping) {

		Map<String, Map<String, Double>> sprintCapcityMapFilter = new HashMap<>();
		Map<String, Double> taggedDefectsMap = new HashMap<>();
		Map<String, Double> taggedCreatedDefectsMap = new HashMap<>();
		Double timeLoggedInMinutes = 0.0d;
		Double originalEstimateInMinutes = 0.0d;

		if (CollectionUtils.isNotEmpty(createdIssues)) {
			Predicate<JiraIssue> excludeSpilledIssue = jiraIssue -> !fieldMapping.getExcludeSpilledKpi46()
					.equalsIgnoreCase("On")
					|| Optional.ofNullable(jiraIssue.getSprintIdList()).orElse(Arrays.asList("")).size() <= 1;

			if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
					&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)
					&& Objects.nonNull(fieldMapping.getStoryPointToHourMapping())) {

				originalEstimateInMinutes = createdIssues.stream()
						.filter(jiraIssue -> Objects.nonNull(jiraIssue.getStoryPoints())).filter(excludeSpilledIssue)
						.mapToDouble(jiraIssue -> jiraIssue.getStoryPoints() * fieldMapping.getStoryPointToHourMapping()
								* 60)
						.sum();
			} else {
				originalEstimateInMinutes = createdIssues.stream()
						.filter(jiraIssue -> Objects.nonNull(jiraIssue.getAggregateTimeOriginalEstimateMinutes()))
						.filter(excludeSpilledIssue).mapToDouble(JiraIssue::getAggregateTimeOriginalEstimateMinutes)
						.sum();
			}
			timeLoggedInMinutes = createdIssues.stream()
					.filter(jiraIssue -> Objects.nonNull(jiraIssue.getTimeSpentInMinutes()))
					.mapToDouble(JiraIssue::getTimeSpentInMinutes).sum();
		}
		Double loggedWorkHours = Double.valueOf(df2.format(timeLoggedInMinutes / 60));
		Double estimatedHours = Double.valueOf(df2.format(originalEstimateInMinutes / 60));

		taggedDefectsMap.put(ESTIMATED_HOURS, estimate);
		taggedDefectsMap.put(LOGGED_HOURS, loggedWorkHours);
		sprintCapcityMapFilter.put(EXECUTED, taggedDefectsMap);

		taggedCreatedDefectsMap.put(ESTIMATED_HOURS, estimate);
		taggedCreatedDefectsMap.put(LOGGED_HOURS, estimatedHours);
		sprintCapcityMapFilter.put(PLANNED, taggedCreatedDefectsMap);

		sprintCapacityHowerMap.put(AVAILABLE_CAPACITY, estimate);
		sprintCapacityHowerMap.put(PLANNED + ORIGINAL, estimatedHours);

		sprintCapacityHowerMap.put(EXECUTED + LOGGED_HOURS, loggedWorkHours);
		return sprintCapcityMapFilter;
	}

	/**
	 * 
	 * @param key
	 *            key
	 * @param sprintCapacityHowerMap
	 *            sprintCapacityHowerMap
	 * @return map
	 */
	private Map<String, Object> getHoverMap(String key, Map<String, Object> sprintCapacityHowerMap) {
		Map<String, Object> hoverMap = new LinkedHashMap<>();
		hoverMap.put(AVAILABLE_CAPACITY, sprintCapacityHowerMap.getOrDefault(AVAILABLE_CAPACITY, 0));
		if (key.equalsIgnoreCase(PLANNED)) {
			hoverMap.put(ESTIMATED_HOURS, sprintCapacityHowerMap.getOrDefault(PLANNED + ORIGINAL, 0));
		} else {
			hoverMap.put(LOGGED_HOURS, sprintCapacityHowerMap.getOrDefault(EXECUTED + LOGGED_HOURS, 0));
		}
		return hoverMap;
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> sprintCapacityMap) {
		return null;
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	/**
	 * Gets Qualifier Type
	 *
	 * @return KPICode's <tt>SPRINT_CAPACITY</tt> enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.SPRINT_CAPACITY_UTILIZATION.name();
	}
}
