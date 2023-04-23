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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.util.concurrent.AtomicDouble;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.IssueDetails;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

/**
 * This class calculates the DRR and trend analysis of the DRR.
 * 
 * @author pkum34
 *
 */
@Component
@Slf4j
public class SprintVelocityServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

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
		List<DataCount> trendValues = getTrendValues(kpiRequest, nodeWiseKPIValue, KPICode.SPRINT_VELOCITY);
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
		Map<ObjectId, List<String>> projectWiseSprintsForFilter = leafNodeList.stream().collect(Collectors.groupingBy(
				node -> node.getProjectFilter().getBasicProjectConfigId(),
				Collectors.collectingAndThen(Collectors.toList(),
						s -> s.stream().map(node -> node.getSprintFilter().getId()).collect(Collectors.toList()))));
		Map<String, Object> resultListMap = kpiHelperService.fetchSprintVelocityDataFromDb(projectWiseSprintsForFilter,
				kpiRequest);
		setDbQueryLogger((List<JiraIssue>) resultListMap.get(SPRINTVELOCITYKEY));
		Set<ObjectId> basicProjectConfigObjectIds = new HashSet<>();
		leafNodeList.forEach(leaf -> {
			basicProjectConfigObjectIds.add(leaf.getProjectFilter().getBasicProjectConfigId());
		});
		List<SprintDetails> totalSprintDetails = sprintRepository
				.findByBasicProjectConfigIdInAndStateOrderByStartDateDesc(basicProjectConfigObjectIds,
						SprintDetails.SPRINT_STATE_CLOSED);
		if (CollectionUtils.isNotEmpty(totalSprintDetails)) {
			Map<ObjectId, List<String>> projectWisePreviousSprintDetails = totalSprintDetails.stream()
					.collect(Collectors.groupingBy(SprintDetails::getBasicProjectConfigId,
							Collectors.collectingAndThen(Collectors.toList(),
									s -> s.stream().map(sprint -> sprint.getSprintID())
											.skip(customApiConfig.getSprintCountForFilters()).limit(4)
											.collect(Collectors.toList()))));
			Map<String, Object> otherSprintsListMap = kpiHelperService
					.fetchSprintVelocityDataFromDb(projectWisePreviousSprintDetails, kpiRequest);
			resultListMap.put("previous_Sprint_velocity", otherSprintsListMap.get(SPRINTVELOCITYKEY));
			resultListMap.put("previous_Sprint_wise_details", otherSprintsListMap.get(SPRINT_WISE_SPRINTDETAILS));
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

		Map<String, Object> sprintVelocityStoryMap = fetchKPIDataFromDb(sprintLeafNodeList, null, null, kpiRequest);

		List<JiraIssue> allJiraIssue = (List<JiraIssue>) sprintVelocityStoryMap.get(SPRINTVELOCITYKEY);

		Map<Pair<String, String>, List<JiraIssue>> sprintWiseIssues = new HashMap<>();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
				.get(sprintLeafNodeList.get(0).getProjectFilter().getBasicProjectConfigId());

		List<SprintDetails> sprintDetails = (List<SprintDetails>) sprintVelocityStoryMap.get(SPRINT_WISE_SPRINTDETAILS);
		Map<Pair<String, String>, Set<IssueDetails>> currentSprintLeafVelocityMap = new HashMap<>();
		getSprintIssuesForProject(allJiraIssue, sprintWiseIssues, sprintDetails, currentSprintLeafVelocityMap);

		List<SprintDetails> oldSprintDetails = (List<SprintDetails>) sprintVelocityStoryMap
				.get("previous_Sprint_wise_details");
		List<JiraIssue> oldAllJiraIssue = (List<JiraIssue>) sprintVelocityStoryMap.get("previous_Sprint_velocity");
		Map<Pair<String, String>, Set<IssueDetails>> oldcurrentSprintLeafVelocityMap = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> oldSprintWiseIssues = new HashMap<>();
		getSprintIssuesForProject(oldAllJiraIssue, oldSprintWiseIssues, oldSprintDetails,
				oldcurrentSprintLeafVelocityMap);
		Map<Pair<String, String>, Double> sprintVelocity = new HashMap<>();
		oldSprintDetails.forEach(sprint -> {
			FieldMapping fieldMap = configHelperService.getFieldMappingMap().get(sprint.getBasicProjectConfigId());
			Pair<String, String> currentNodeIdentifier = Pair.of(sprint.getBasicProjectConfigId().toString(),
					sprint.getSprintID());
			double sprintVelocityForCurrentLeaf = calculateSprintVelocityValue(currentSprintLeafVelocityMap,
					currentNodeIdentifier, sprintWiseIssues, fieldMap);
			sprintVelocity.put(currentNodeIdentifier, sprintVelocityForCurrentLeaf);
		});

		List<KPIExcelData> excelData = new ArrayList<>();
		AtomicInteger avgVelocityCount = new AtomicInteger();
		sprintLeafNodeList.forEach(node -> {
			// Leaf node wise data
			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), currentSprintComponentId);

			double sprintVelocityForCurrentLeaf = calculateSprintVelocityValue(currentSprintLeafVelocityMap,
					currentNodeIdentifier, sprintWiseIssues, fieldMapping);
			sprintVelocity.put(currentNodeIdentifier, sprintVelocityForCurrentLeaf);

			populateExcelDataObject(requestTrackerId, excelData, sprintWiseIssues, currentSprintLeafVelocityMap, node,
					fieldMapping);
			setSprintWiseLogger(node.getSprintFilter().getName(),
					currentSprintLeafVelocityMap.get(currentNodeIdentifier), sprintVelocityForCurrentLeaf);

			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(Math.round(sprintVelocityForCurrentLeaf)));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
			dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
			if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
					&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
				dataCount.setValue(sprintVelocityForCurrentLeaf);
			} else {
				dataCount.setValue(sprintVelocityForCurrentLeaf / fieldMapping.getStoryPointToHourMapping());
			}
			dataCount.setHoverValue(new HashMap<>());
			double averageVelocity = getAverageVelocity(sprintVelocity, avgVelocityCount.get(),
					node.getProjectFilter().getBasicProjectConfigId().toString());
			if (averageVelocity > 0) {
				dataCount.setLineValue(String.valueOf(Math.round(averageVelocity)));
				dataCount.setValue(String.valueOf(Math.round(sprintVelocityForCurrentLeaf)));
				Map<String, Object> hoverValue = new HashMap<>();
				hoverValue.put("averageVelocity", String.valueOf(Math.round(averageVelocity)));
				hoverValue.put("velocity", String.valueOf(Math.round(sprintVelocityForCurrentLeaf)));
				dataCount.setHoverValue(hoverValue);
				avgVelocityCount.set(avgVelocityCount.get() + 1);
			}
			mapTmp.get(node.getId()).setValue(new ArrayList<DataCount>(Arrays.asList(dataCount)));
			trendValueList.add(dataCount);
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.SPRINT_VELOCITY.getColumns());
	}

	private double getAverageVelocity(Map<Pair<String, String>, Double> sprintVelocityMap, int avgVelocityCount,
			String basicProjId) {
		AtomicDouble sumVelocity = new AtomicDouble();
		AtomicInteger count = new AtomicInteger();
		AtomicInteger validCount = new AtomicInteger();
		sprintVelocityMap.entrySet().forEach(velocityMap -> {
			if (velocityMap.getKey().getKey().equals(basicProjId)) {
				count.set(count.get() + 1);
				if (count.get() > avgVelocityCount ) {
					validCount.set(validCount.get() + 1);
					sumVelocity.set(sumVelocity.get() + velocityMap.getValue());
				}
			}
		});
		if (validCount.get() == 5) {
			return sumVelocity.get() / 5;
		}
		return 0.0;
	}

	private void getSprintIssuesForProject(List<JiraIssue> allJiraIssue,
			Map<Pair<String, String>, List<JiraIssue>> sprintWiseIssues, List<SprintDetails> sprintDetails,
			Map<Pair<String, String>, Set<IssueDetails>> currentSprintLeafVelocityMap) {
		if (CollectionUtils.isNotEmpty(sprintDetails)) {
			sprintDetails.forEach(sd -> {
				Set<IssueDetails> filterIssueDetailsSet = new HashSet<>();
				if (CollectionUtils.isNotEmpty(sd.getCompletedIssues())) {
					sd.getCompletedIssues().stream().forEach(sprintIssue -> {
						allJiraIssue.stream().forEach(jiraIssue -> {
							if (sprintIssue.getNumber().equals(jiraIssue.getNumber())) {
								IssueDetails issueDetails = new IssueDetails();
								issueDetails.setSprintIssue(sprintIssue);
								issueDetails.setUrl(jiraIssue.getUrl());
								issueDetails.setDesc(jiraIssue.getName());
								filterIssueDetailsSet.add(issueDetails);
							}
						});
						Pair<String, String> currentNodeIdentifier = Pair.of(sd.getBasicProjectConfigId().toString(),
								sd.getSprintID());
						currentSprintLeafVelocityMap.put(currentNodeIdentifier, filterIssueDetailsSet);
					});
				}
			});
		} else {
			if (CollectionUtils.isNotEmpty(allJiraIssue)) {
				// start : for azure board sprint details collections empty so
				// that we have to
				// prepare data from jira issue
				Map<String, List<JiraIssue>> projectWiseJiraIssues = allJiraIssue.stream()
						.collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId));
				projectWiseJiraIssues.forEach((basicProjectConfigId, projectWiseIssuesList) -> {
					Map<String, List<JiraIssue>> sprintWiseJiraIssues = projectWiseIssuesList.stream()
							.filter(jiraIssue -> Objects.nonNull(jiraIssue.getSprintID()))
							.collect(Collectors.groupingBy(JiraIssue::getSprintID));
					sprintWiseJiraIssues.forEach((sprintId, sprintWiseIssuesList) -> sprintWiseIssues
							.put(Pair.of(basicProjectConfigId, sprintId), sprintWiseIssuesList));
				});
			}
			// end : for azure board sprint details collections empty so that we
			// have to
			// prepare data from jira issue.
		}
	}

	private double calculateSprintVelocityValue(
			Map<Pair<String, String>, Set<IssueDetails>> currentSprintLeafVelocityMap,
			Pair<String, String> currentNodeIdentifier, Map<Pair<String, String>, List<JiraIssue>> sprintJiraIssues,
			FieldMapping fieldMapping) {
		double sprintVelocityForCurrentLeaf = 0.0d;
		if (CollectionUtils.isNotEmpty(sprintJiraIssues.get(currentNodeIdentifier))) {
			List<JiraIssue> jiraIssueList = sprintJiraIssues.get(currentNodeIdentifier);
			if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
					&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
				sprintVelocityForCurrentLeaf = jiraIssueList.stream()
						.mapToDouble(ji -> Double.valueOf(ji.getEstimate())).sum();
			} else {
				double totalOriginalEstimate = jiraIssueList.stream()
						.filter(jiraIssue -> Objects.nonNull(jiraIssue.getOriginalEstimateMinutes()))
						.mapToDouble(JiraIssue::getOriginalEstimateMinutes).sum();
				double totalOriginalEstimateInHours = totalOriginalEstimate / 60;
				sprintVelocityForCurrentLeaf = totalOriginalEstimateInHours / 60;
			}
		} else {
			if (Objects.nonNull(currentSprintLeafVelocityMap.get(currentNodeIdentifier))) {
				Set<IssueDetails> issueDetailsSet = currentSprintLeafVelocityMap.get(currentNodeIdentifier);
				if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
					sprintVelocityForCurrentLeaf = issueDetailsSet.stream()
							.filter(issueDetails -> Objects.nonNull(issueDetails.getSprintIssue().getStoryPoints()))
							.mapToDouble(issueDetails -> issueDetails.getSprintIssue().getStoryPoints()).sum();
				} else {
					double totalOriginalEstimate = issueDetailsSet.stream().filter(
							issueDetails -> Objects.nonNull(issueDetails.getSprintIssue().getOriginalEstimate()))
							.mapToDouble(issueDetails -> issueDetails.getSprintIssue().getOriginalEstimate()).sum();
					double totalOriginalEstimateInHours = totalOriginalEstimate / 60;
					sprintVelocityForCurrentLeaf = totalOriginalEstimateInHours / 60;
				}
			}
		}
		return sprintVelocityForCurrentLeaf;
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			Map<Pair<String, String>, List<JiraIssue>> sprintWiseIssues,
			Map<Pair<String, String>, Set<IssueDetails>> currentSprintLeafVelocityMap, Node node,
			FieldMapping fieldMapping) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), node.getSprintFilter().getId());

			if (CollectionUtils.isNotEmpty(sprintWiseIssues.get(currentNodeIdentifier))) {
				List<JiraIssue> jiraIssues = sprintWiseIssues.get(currentNodeIdentifier);
				Map<String, JiraIssue> totalSprintStoryMap = new HashMap<>();
				jiraIssues.stream().forEach(issue -> totalSprintStoryMap.putIfAbsent(issue.getNumber(), issue));
				KPIExcelUtility.populateSprintVelocity(node.getSprintFilter().getName(), totalSprintStoryMap, null,
						excelData, fieldMapping);
			} else {
				if (MapUtils.isNotEmpty(currentSprintLeafVelocityMap)
						&& CollectionUtils.isNotEmpty(currentSprintLeafVelocityMap.get(currentNodeIdentifier))) {
					Set<IssueDetails> issueDetailsSet = currentSprintLeafVelocityMap.get(currentNodeIdentifier);
					KPIExcelUtility.populateSprintVelocity(node.getSprintFilter().getName(), null, issueDetailsSet,
							excelData, fieldMapping);
				}
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
	private void setSprintWiseLogger(String sprint, Set<IssueDetails> issueDetailsSet, Double sprintVelocity) {

		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.info(SEPARATOR_ASTERISK);
			log.info("************* SPRINT WISE Sprint Velocity *******************");
			log.info("Sprint: {}", sprint);
			if (CollectionUtils.isNotEmpty(issueDetailsSet)) {
				List<String> storyIdList = issueDetailsSet.stream()
						.map(issueDetails -> issueDetails.getSprintIssue().getNumber()).collect(Collectors.toList());
				log.info(STORY_LOG, storyIdList.size(), storyIdList);
				List<Double> storyPointIdList = issueDetailsSet.stream()
						.map(issueDetails -> issueDetails.getSprintIssue().getStoryPoints())
						.collect(Collectors.toList());
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
}
