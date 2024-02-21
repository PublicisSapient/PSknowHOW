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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.AtomicDouble;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.jira.service.SprintVelocityServiceHelper;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepositoryCustom;

import lombok.extern.slf4j.Slf4j;

/**
 * This class calculates the DRR and trend analysis of the DRR.
 * 
 * @author pkum34
 *
 */
@Component
@Slf4j
public class SprintVelocityServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String VELOCITY = "Velocity";
	private static final String AVERAGE_VELOCITY = "AverageVelocity";
	private static final String SEPARATOR_ASTERISK = "*************************************";
	private static final String SPRINTVELOCITYKEY = "sprintVelocityKey";
	private static final String SPRINT_WISE_SPRINTDETAILS = "sprintWiseSprintDetailMap";

	private static final String STORY_LOG = "Story[{}]: {}";
	@Autowired
	private KpiHelperService kpiHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private SprintRepository sprintRepository;
	@Autowired
	private SprintVelocityServiceHelper velocityHelper;
	@Autowired
	private SprintRepositoryCustom sprintRepositoryCustom;

	/**
	 * Gets Qualifier Type
	 *
	 * @return KPICode's <tt>SPRINT_VELOCITY</tt> enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.SPRINT_VELOCITY.name();
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

		log.debug("[SPRINT-VELOCITY-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.SPRINT_VELOCITY);
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue, KPICode.SPRINT_VELOCITY);
		kpiElement.setTrendValueList(trendValues);
		log.debug("[SPRINT-VELOCITY-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
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
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Set<ObjectId> basicProjectConfigObjectIds = new HashSet<>();
		List<String> sprintStatusList = new ArrayList<>();
		leafNodeList
				.forEach(leaf -> basicProjectConfigObjectIds.add(leaf.getProjectFilter().getBasicProjectConfigId()));
		sprintStatusList.add(SprintDetails.SPRINT_STATE_CLOSED);
		sprintStatusList.add(SprintDetails.SPRINT_STATE_CLOSED.toLowerCase());
		long time2 = System.currentTimeMillis();
		List<SprintDetails> totalSprintDetails = sprintRepositoryCustom
				.findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(basicProjectConfigObjectIds,
						sprintStatusList,
						(long) customApiConfig.getSprintVelocityLimit() + customApiConfig.getSprintCountForFilters());
		log.info("Sprint Velocity findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc method time taking {}",
				System.currentTimeMillis() - time2);
		// Group the SprintDetails by basicProjectConfigId
		Map<ObjectId, List<SprintDetails>> sprintDetailsByProjectId = totalSprintDetails.stream()
				.collect(Collectors.groupingBy(SprintDetails::getBasicProjectConfigId));

		// Limit the list of SprintDetails for each basicProjectConfigId to
		// customApiConfig.getSprintVelocityLimit()+customApiConfig.getSprintCountForFilters()
		List<SprintDetails> result = sprintDetailsByProjectId.values().stream()
				.flatMap(list -> list.stream().limit(
						(long) customApiConfig.getSprintVelocityLimit() + customApiConfig.getSprintCountForFilters()))
				.collect(Collectors.toList());

		if (CollectionUtils.isNotEmpty(totalSprintDetails)) {
			Map<ObjectId, List<String>> projectWiseSprintDetails = result.stream()
					.collect(Collectors.groupingBy(SprintDetails::getBasicProjectConfigId,
							Collectors.collectingAndThen(Collectors.toList(),
									s -> s.stream().map(SprintDetails::getSprintID).collect(Collectors.toList()))));

			resultListMap = kpiHelperService.fetchSprintVelocityDataFromDb(kpiRequest, projectWiseSprintDetails,
					result);

		}
		return resultListMap;

	}

	/**
	 * Calculates KPI Metrics
	 * 
	 * @param techDebtStoryMap
	 * @return Double
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Double calculateKPIMetrics(Map<String, Object> techDebtStoryMap) {

		String requestTrackerId = getRequestTrackerId();
		Double sprintVelocity = 0.0d;
		List<JiraIssue> sprintVelocityList = (List<JiraIssue>) techDebtStoryMap.get(SPRINTVELOCITYKEY);
		log.debug("[SPRINT-VELOCITY][{}]. Stories Count: {}", requestTrackerId, sprintVelocityList.size());
		for (JiraIssue jiraIssue : sprintVelocityList) {
			sprintVelocity = sprintVelocity + Double.valueOf(jiraIssue.getEstimate());
		}
		return sprintVelocity;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint wise.
	 * 
	 * @param mapTmp
	 * @param trendValueList
	 * @param sprintLeafNodeList
	 * @param kpiElement
	 */
	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		long time = System.currentTimeMillis();
		Map<String, Object> sprintVelocityStoryMap = fetchKPIDataFromDb(sprintLeafNodeList, null, null, kpiRequest);
		log.info("Sprint Velocity taking fetchKPIDataFromDb {}", String.valueOf(System.currentTimeMillis() - time));

		List<JiraIssue> allJiraIssue = (List<JiraIssue>) sprintVelocityStoryMap.get(SPRINTVELOCITYKEY);

		FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
				.get(sprintLeafNodeList.get(0).getProjectFilter().getBasicProjectConfigId());

		List<SprintDetails> sprintDetails = (List<SprintDetails>) sprintVelocityStoryMap.get(SPRINT_WISE_SPRINTDETAILS);
		Map<Pair<String, String>, Set<JiraIssue>> currentSprintLeafVelocityMap = new HashMap<>();
		velocityHelper.getSprintIssuesForProject(allJiraIssue, sprintDetails, currentSprintLeafVelocityMap);

		Map<Pair<String, String>, Double> sprintVelocity = getSprintVelocityMap(currentSprintLeafVelocityMap,
				sprintDetails);

		List<KPIExcelData> excelData = new ArrayList<>();
		Map<String, Integer> avgVelocityCount = new HashMap<>();
		sprintLeafNodeList.forEach(node -> {
			// Leaf node wise data
			String projId = node.getProjectFilter().getBasicProjectConfigId().toString();
			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair.of(projId, currentSprintComponentId);

			double sprintVelocityForCurrentLeaf = 0.0;
			if (CollectionUtils.isNotEmpty(sprintDetails)) {
				sprintVelocityForCurrentLeaf = sprintVelocity.getOrDefault(currentNodeIdentifier,
						sprintVelocityForCurrentLeaf);
			}

			populateExcelDataObject(requestTrackerId, excelData, currentSprintLeafVelocityMap, node, fieldMapping);
			setSprintWiseLogger(node.getSprintFilter().getName(),
					currentSprintLeafVelocityMap.get(currentNodeIdentifier), sprintVelocityForCurrentLeaf);

			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(roundingOff(sprintVelocityForCurrentLeaf)));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
			dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
			dataCount.setLineValue(roundingOff(sprintVelocityForCurrentLeaf));
			if (!avgVelocityCount.containsKey(projId)) {
				avgVelocityCount.put(projId, 0);
			}

			double averageVelocity = getAverageVelocity(sprintVelocity, avgVelocityCount.get(projId), projId,
					currentSprintComponentId);
			if (averageVelocity >= 0) {
				dataCount.setValue(averageVelocity);
				Map<String, Object> hoverValue = new HashMap<>();
				hoverValue.put(AVERAGE_VELOCITY, roundingOff(averageVelocity));
				hoverValue.put(VELOCITY, roundingOff((Double) dataCount.getLineValue()));
				dataCount.setHoverValue(hoverValue);
				avgVelocityCount.put(projId, avgVelocityCount.get(projId) + 1);
			} else {
				dataCount.setValue(0.0);
			}
			mapTmp.get(node.getId()).setValue(new ArrayList<DataCount>(Arrays.asList(dataCount)));
			trendValueList.add(dataCount);
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.SPRINT_VELOCITY.getColumns());
	}

	/**
	 * Create map consisting of sprint and its velocity
	 *
	 * @param currentSprintLeafVelocityMap
	 * @param oldSprintDetails
	 * @return
	 */
	private Map<Pair<String, String>, Double> getSprintVelocityMap(
			Map<Pair<String, String>, Set<JiraIssue>> currentSprintLeafVelocityMap,
			List<SprintDetails> oldSprintDetails) {
		log.debug("In the velocity map creation");
		Map<Pair<String, String>, Double> sprintVelocity = new LinkedHashMap<>();
		if (CollectionUtils.isNotEmpty(oldSprintDetails)) {
			oldSprintDetails.forEach(sprint -> {
				FieldMapping fieldMap = configHelperService.getFieldMappingMap().get(sprint.getBasicProjectConfigId());
				Pair<String, String> currentNodeIdentifier = Pair.of(sprint.getBasicProjectConfigId().toString(),
						sprint.getSprintID());
				double sprintVelocityForCurrentLeaf = velocityHelper
						.calculateSprintVelocityValue(currentSprintLeafVelocityMap, currentNodeIdentifier, fieldMap);
				sprintVelocity.put(currentNodeIdentifier, sprintVelocityForCurrentLeaf);
			});
		}
		log.debug("End of the velocity map creation with the size {}", sprintVelocity.size());
		return sprintVelocity;
	}

	/**
	 * Get average velocity of 5 sprints
	 *
	 * @param sprintVelocityMap
	 * @param
	 * @param basicProjId
	 * @return
	 */
	private double getAverageVelocity(Map<Pair<String, String>, Double> sprintVelocityMap, int sprintCount,
			String basicProjId, String currentSprintComponentId) {
		AtomicDouble sumVelocity = new AtomicDouble();
		AtomicInteger count = new AtomicInteger();
		int sprintCountForAvgVel = customApiConfig.getSprintVelocityLimit() + 1;
		AtomicInteger validCount = new AtomicInteger();
		AtomicBoolean flag = new AtomicBoolean(false);
		sprintVelocityMap.entrySet().forEach(velocityMap -> {
			if (velocityMap.getKey().getKey().equals(basicProjId)) {
				count.set(count.get() + 1);
				if ((velocityMap.getKey().getValue().equalsIgnoreCase(currentSprintComponentId) || flag.get())
						&& validCount.get() < sprintCountForAvgVel) {
					flag.set(true);
					validCount.set(validCount.get() + 1);
					sumVelocity.set(sumVelocity.get() + velocityMap.getValue());
				}
			}
		});
		log.debug("The average velocity of sprint {} is {}", sprintCount, sumVelocity.get());
		if (validCount.get() < sprintCountForAvgVel) {
			return sumVelocity.get() / validCount.get();
		}
		return sumVelocity.get() / sprintCountForAvgVel;
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			Map<Pair<String, String>, Set<JiraIssue>> currentSprintLeafVelocityMap, Node node,
			FieldMapping fieldMapping) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), node.getSprintFilter().getId());

			if (MapUtils.isNotEmpty(currentSprintLeafVelocityMap)
					&& CollectionUtils.isNotEmpty(currentSprintLeafVelocityMap.get(currentNodeIdentifier))) {
				Set<JiraIssue> jiraIssues = currentSprintLeafVelocityMap.get(currentNodeIdentifier);
				Map<String, JiraIssue> totalSprintStoryMap = new HashMap<>();
				jiraIssues.stream().forEach(issue -> totalSprintStoryMap.putIfAbsent(issue.getNumber(), issue));
				KPIExcelUtility.populateSprintVelocity(node.getSprintFilter().getName(), totalSprintStoryMap, excelData,
						fieldMapping);
			}
		}
	}

	/**
	 * Sets DB query Logger
	 *
	 * @param jiraIssues
	 */
	private void setDbQueryLogger(List<JiraIssue> jiraIssues) {

		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.info(SEPARATOR_ASTERISK);
			log.info("************* Sprint Velocity (dB) *******************");
			if (null != jiraIssues && !jiraIssues.isEmpty()) {
				List<String> storyIdList = jiraIssues.stream().map(JiraIssue::getNumber).collect(Collectors.toList());
				log.info(STORY_LOG, storyIdList.size(), storyIdList);
			}
			log.info(SEPARATOR_ASTERISK);
			log.info("******************X----X*******************");
		}
	}

	/**
	 * Sets Sprint wise Logger
	 * 
	 * @param sprint
	 * @param issueDetailsSet
	 * @param sprintVelocity
	 */
	private void setSprintWiseLogger(String sprint, Set<JiraIssue> issueDetailsSet, Double sprintVelocity) {

		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.info(SEPARATOR_ASTERISK);
			log.info("************* SPRINT WISE Sprint Velocity *******************");
			log.info("Sprint: {}", sprint);
			if (CollectionUtils.isNotEmpty(issueDetailsSet)) {
				List<String> storyIdList = issueDetailsSet.stream().map(issueDetails -> issueDetails.getNumber())
						.collect(Collectors.toList());
				log.info(STORY_LOG, storyIdList.size(), storyIdList);
				List<Double> storyPointIdList = issueDetailsSet.stream()
						.map(issueDetails -> issueDetails.getStoryPoints()).collect(Collectors.toList());
				log.info(STORY_LOG, storyIdList.size(), storyPointIdList);
			}
			log.info("Sprint Velocity: {}", sprintVelocity);
			log.info(SEPARATOR_ASTERISK);
			log.info(SEPARATOR_ASTERISK);
		}
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI39(), KPICode.SPRINT_VELOCITY.getKpiId());
	}

}
