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
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiDataProvider;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.LoggedTimePerIssue;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

import lombok.extern.slf4j.Slf4j;

/**
 * This class calculates the Sprint capacity.
 *
 * @author purgupta2
 */
@Component
@Slf4j
public class SprintCapacityServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	public static final String UNCHECKED = "unchecked";
	private static final String ESTIMATE_TIME = "Estimate_Time";
	private static final String ESTIMATED_HOURS = "Estimated Hours";
	private static final String LOGGED_HOURS = "Logged Work";
	private static final String STORY_LIST = "stories";
	private static final String SPRINTSDETAILS = "sprints";
	private final DecimalFormat df2 = new DecimalFormat(".##");
	private static final String JIRA_ISSUE_HISTORY_DATA = "JiraIssueHistoryData";

	private List<String> sprintIdList = Collections.synchronizedList(new ArrayList<>());

	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private FilterHelperService flterHelperService;
	@Autowired
	private CacheService cacheService;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private KpiDataCacheService kpiDataCacheService;
	@Autowired
	private KpiDataProvider kpiDataProvider;

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
	 *            kpiRequest
	 * @param kpiElement
	 *            kpiElement
	 * @param treeAggregatorDetail
	 *            treeAggregatorDetail
	 * @return KpiElement
	 * @throws ApplicationException
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		List<DataCount> trendValueList = new ArrayList<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		sprintIdList = treeAggregatorDetail.getMapOfListOfLeafNodes().get(CommonConstant.SPRINT_MASTER).stream()
				.map(node -> node.getSprintFilter().getId()).collect(Collectors.toList());
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
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.SPRINT_CAPACITY_UTILIZATION);
		kpiElement.setTrendValueList(trendValues);
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
	 * @param trendValueList
	 *            trendValueList
	 * @param kpiRequest
	 *            kpiRequest
	 */
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();

		sprintLeafNodeList.sort(Comparator.comparing(node -> node.getSprintFilter().getStartDate()));
		long time = System.currentTimeMillis();
		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNodeList, null, null, kpiRequest);
		log.info("Sprint Capacity taking fetchKPIDataFromDb {}", System.currentTimeMillis() - time);

		Map<Pair<String, String>, Double> sprintWiseEstimateTimeMap = new HashMap<>();
		List<LoggedTimePerIssue> loggedTimePerIssueList = new ArrayList<>();
		Map<Pair<String, String>, List<JiraIssue>> issueUsedForLoggedTimeMap = new HashMap<>();

		Map<Pair<String, String>, Double> sprintWiseLoggedTimeMap = prepareMapForLoggedWorkAndEstimateTime(
				sprintWiseEstimateTimeMap, resultMap, loggedTimePerIssueList, issueUsedForLoggedTimeMap);

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

			double estimateTimeForCurrentLeaf = Double.NaN;
			if (null != sprintWiseEstimateTimeMap.get(currentNodeEstimateTime)) {
				estimateTimeForCurrentLeaf = sprintWiseEstimateTimeMap.get(currentNodeEstimateTime);
			}
			double loggedTimeForCurrentLeaf = Double.NaN;
			if (ObjectUtils.isNotEmpty(sprintWiseLoggedTimeMap.get(currentNodeIdentifier))) {
				loggedTimeForCurrentLeaf = Double
						.parseDouble(df2.format(sprintWiseLoggedTimeMap.get(currentNodeIdentifier)));

				List<JiraIssue> sprintJiraIssues = issueUsedForLoggedTimeMap.get(currentNodeIdentifier);
				populateExcelDataObject(requestTrackerId, excelData, sprintJiraIssues, node, loggedTimePerIssueList);
			}
			hoverValue.put(ESTIMATED_HOURS, (int) estimateTimeForCurrentLeaf);
			hoverValue.put(LOGGED_HOURS, (int) loggedTimeForCurrentLeaf);
			DataCount dataCount = new DataCount();
			createDataCount(estimateTimeForCurrentLeaf, dataCount);
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
			dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
			dataCount.setLineValue(loggedTimeForCurrentLeaf);
			dataCount.setHoverValue(hoverValue);
			trendValueList.add(dataCount);
			mapTmp.get(node.getId()).setValue(new ArrayList<>(Arrays.asList(dataCount)));
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.SPRINT_CAPACITY_UTILIZATION.getColumns(sprintLeafNodeList,
				cacheService, flterHelperService));
	}

	private static void createDataCount(double estimateTimeForCurrentLeaf, DataCount dataCount) {
		if (!Double.isNaN(estimateTimeForCurrentLeaf)) {
			dataCount.setData(String.valueOf(estimateTimeForCurrentLeaf));
			dataCount.setValue(estimateTimeForCurrentLeaf);
		}
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
	@SuppressWarnings(UNCHECKED)
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<ObjectId, List<String>> projectWiseSprints = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			String sprint = leaf.getSprintFilter().getId();
			projectWiseSprints.computeIfAbsent(basicProjectConfigId, k -> new ArrayList<>()).add(sprint);
		});
		List<SprintDetails> sprintDetails = new ArrayList<>();
		List<JiraIssue> issueList = new ArrayList<>();
		List<JiraIssueCustomHistory> issueCustomHistoryList = new ArrayList<>();
		List<CapacityKpiData> estimateTimeList = new ArrayList<>();
		boolean fetchCachedData = flterHelperService.isFilterSelectedTillSprintLevel(kpiRequest.getLevel(), false);
		projectWiseSprints.forEach((basicProjectConfigId, sprints) -> {
			Map<String, Object> result;
			if (fetchCachedData) {
				result = kpiDataCacheService.fetchSprintCapacityData(kpiRequest, basicProjectConfigId, sprintIdList,
						KPICode.SPRINT_CAPACITY_UTILIZATION.getKpiId());
			} else {
				result = kpiDataProvider.fetchSprintCapacityDataFromDb(kpiRequest, basicProjectConfigId, sprints);
			}
			sprintDetails.addAll((List<SprintDetails>) result.get(SPRINTSDETAILS));
			issueList.addAll((List<JiraIssue>) result.get(STORY_LIST));
			issueCustomHistoryList.addAll((List<JiraIssueCustomHistory>) result.get(JIRA_ISSUE_HISTORY_DATA));
			estimateTimeList.addAll((List<CapacityKpiData>) result.get(ESTIMATE_TIME));
		});
		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(ESTIMATE_TIME, estimateTimeList);
		resultListMap.put(STORY_LIST, issueList);
		resultListMap.put(SPRINTSDETAILS, sprintDetails);
		resultListMap.put(JIRA_ISSUE_HISTORY_DATA, issueCustomHistoryList);
		return resultListMap;
	}

	/**
	 * Prepares a map for logged work and estimate time.
	 *
	 * @param sprintWiseEstimateTimeMap
	 *            The map containing sprint-wise estimate time.
	 * @param resultMap
	 *            The result map containing various data required for processing.
	 * @param loggedTimePerIssueList
	 *            The list to store logged time per issue.
	 * @param issueUsedForLoggedTimeMap
	 *            The map to store issues used for logged time.
	 * @return A map containing logged time data.
	 */
	@SuppressWarnings(UNCHECKED)
	private Map<Pair<String, String>, Double> prepareMapForLoggedWorkAndEstimateTime(
			Map<Pair<String, String>, Double> sprintWiseEstimateTimeMap, Map<String, Object> resultMap,
			List<LoggedTimePerIssue> loggedTimePerIssueList,
			Map<Pair<String, String>, List<JiraIssue>> issueUsedForLoggedTimeMap) {

		// Extract data from resultMap
		List<JiraIssue> allJiraIssue = (List<JiraIssue>) resultMap.get(STORY_LIST);
		List<SprintDetails> sprintDetails = (List<SprintDetails>) resultMap.get(SPRINTSDETAILS);
		Map<Pair<String, String>, Set<String>> parentChildMap = KpiDataHelper
				.getBasicConfigIdAndParentIdWiseChildrenMap(allJiraIssue);
		List<JiraIssueCustomHistory> jiraIssueCustomHistoryList = (List<JiraIssueCustomHistory>) resultMap
				.get(JIRA_ISSUE_HISTORY_DATA);
		List<CapacityKpiData> capacityKpiDataList = (List<CapacityKpiData>) resultMap.get(ESTIMATE_TIME);

		Map<Pair<String, String>, JiraIssueCustomHistory> jiraIssueCustomHistoryMap = jiraIssueCustomHistoryList
				.stream()
				.collect(Collectors.toMap(history -> Pair.of(history.getBasicProjectConfigId(), history.getStoryID()),
						history -> history, (existing, replacement) -> existing));

		Map<Pair<String, String>, Double> loggedTimeMap = new HashMap<>();

		if (CollectionUtils.isNotEmpty(allJiraIssue)) {
			for (SprintDetails sprintDetail : sprintDetails) {
				Set<JiraIssue> totalJiraIssues = KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(
						sprintDetail, sprintDetail.getTotalIssues(), allJiraIssue);
				double timeLoggedInSeconds = 0.0d;
				if (CollectionUtils.isNotEmpty(totalJiraIssues)) {
					for (JiraIssue issue : totalJiraIssues) {
						// timeLoggedForAnIssueInSeconds will give work log of an issue between the time
						// period of sprint startDate to endDate
						double timeLoggedForAnIssueInSeconds = calculateLoggedTimeForIssue(issue.getNumber(),
								sprintDetail, parentChildMap, jiraIssueCustomHistoryMap);
						// timeLoggedInSeconds will give work log of all issue between the time period
						// of sprint startDate to endDate
						timeLoggedInSeconds += timeLoggedForAnIssueInSeconds;
						// this will be used to create map for excel population
						loggedTimePerIssueList.add(new LoggedTimePerIssue(
								sprintDetail.getBasicProjectConfigId().toString(), sprintDetail.getSprintID(),
								issue.getNumber(), timeLoggedForAnIssueInSeconds / (60 * 60)));
					}

				} else {
					timeLoggedInSeconds = Double.NaN;
				}

				loggedTimeMap.put(
						Pair.of(sprintDetail.getBasicProjectConfigId().toString(), sprintDetail.getSprintID()),
						(Double.isNaN(timeLoggedInSeconds)) ? null : timeLoggedInSeconds / (60 * 60));
				issueUsedForLoggedTimeMap.put(
						Pair.of(sprintDetail.getBasicProjectConfigId().toString(), sprintDetail.getSprintID()),
						new ArrayList<>(totalJiraIssues));
			}
		}

		Map<Pair<String, String>, Double> estimateTimeMap = buildEstimateTimeMap(capacityKpiDataList);
		sprintWiseEstimateTimeMap.putAll(estimateTimeMap);

		return loggedTimeMap;
	}

	/**
	 * Calculates the logged time for parent and its child issues within a sprint.
	 *
	 * @param issueNumber
	 *            The issue number.
	 * @param sprintDetail
	 *            The sprint detail.
	 * @param parentChildMap
	 *            The map of parent and child issues.
	 * @param jiraIssueCustomHistoryMap
	 *            The map of Jira issue custom history.
	 * @return The total logged time for the issue in seconds.
	 */
	private double calculateLoggedTimeForIssue(String issueNumber, SprintDetails sprintDetail,
			Map<Pair<String, String>, Set<String>> parentChildMap,
			Map<Pair<String, String>, JiraIssueCustomHistory> jiraIssueCustomHistoryMap) {
		List<String> parentAndChildList = new ArrayList<>();
		parentAndChildList.add(issueNumber); // adding parent

		Set<String> childIssues = parentChildMap
				.get(Pair.of(sprintDetail.getBasicProjectConfigId().toString(), issueNumber));
		if (childIssues != null) {
			parentAndChildList.addAll(childIssues); // adding respective child
		}

		double timeLoggedForAnIssueInSeconds = 0;
		for (String parentAndChildNo : parentAndChildList) {
			JiraIssueCustomHistory jiraIssueCustomHistory = jiraIssueCustomHistoryMap
					.get(Pair.of(sprintDetail.getBasicProjectConfigId().toString(), parentAndChildNo));
			if (jiraIssueCustomHistory != null && jiraIssueCustomHistory.getWorkLog() != null) {
				timeLoggedForAnIssueInSeconds += KpiDataHelper.getWorkLogs(jiraIssueCustomHistory.getWorkLog(),
						sprintDetail.getStartDate(), sprintDetail.getEndDate());
			}
		}

		return timeLoggedForAnIssueInSeconds;
	}

	/**
	 * Builds a map containing the estimate time per sprint.
	 *
	 * @param capacityKpiDataList
	 *            The list of capacity KPI data.
	 * @return A map containing the estimate time per sprint.
	 */
	private Map<Pair<String, String>, Double> buildEstimateTimeMap(List<CapacityKpiData> capacityKpiDataList) {
		return capacityKpiDataList.stream()
				.collect(Collectors.toMap(
						key -> Pair.of(key.getBasicProjectConfigId().toString(), key.getSprintID().toLowerCase()),
						CapacityKpiData::getCapacityPerSprint, Double::sum));
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
	 * @param loggedTimePerIssueList
	 *            loggedTimePerIssueList
	 */
	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> sprintCapacityList, Node node, List<LoggedTimePerIssue> loggedTimePerIssueList) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			String sprintName = node.getSprintFilter().getName();
			String sprintId = node.getSprintFilter().getId();
			String projectConfigId = node.getProjectFilter().getBasicProjectConfigId().toString();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(node.getProjectFilter().getBasicProjectConfigId());

			// Filter loggedTimePerIssueList based on projectConfigId and sprintId
			List<LoggedTimePerIssue> filteredLoggedTimeList = loggedTimePerIssueList.stream()
					.filter(item -> item.getProjectConfigId().equalsIgnoreCase(projectConfigId)
							&& item.getSprintId().equalsIgnoreCase(sprintId))
					.toList();

			// Create a map of storyId and loggedTimeInHours from filtered list
			Map<String, Double> storyIdToLoggedTimeMap = filteredLoggedTimeList.stream().collect(Collectors
					.toMap(LoggedTimePerIssue::getStoryId, LoggedTimePerIssue::getLoggedTimeInHours, (e1, e2) -> e1));

			KPIExcelUtility.populateSprintCapacity(sprintName, sprintCapacityList, excelData, storyIdToLoggedTimeMap,
					fieldMapping, customApiConfig);
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

	/**
	 * @param sprintCapacityMap
	 *            sprintCapacityMap
	 * @return timeLogged in seconds
	 */
	@Override
	public Double calculateKPIMetrics(Map<String, Object> sprintCapacityMap) {
		return null;
	}
}
