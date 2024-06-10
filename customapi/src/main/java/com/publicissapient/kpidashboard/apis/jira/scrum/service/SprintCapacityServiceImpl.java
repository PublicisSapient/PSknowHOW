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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
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
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;

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

	@Autowired
	private KpiHelperService kpiHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private FilterHelperService flterHelperService;
	@Autowired
	private CacheService cacheService;
	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

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

			double estimateTimeForCurrentLeaf = 0.0d;
			if (null != sprintWiseEstimateTimeMap.get(currentNodeEstimateTime)) {
				estimateTimeForCurrentLeaf = sprintWiseEstimateTimeMap.get(currentNodeEstimateTime);
			}
			double loggedTimeForCurrentLeaf = 0.0;
			if (ObjectUtils.isNotEmpty(sprintWiseLoggedTimeMap.get(currentNodeIdentifier))) {
				loggedTimeForCurrentLeaf = Double
						.parseDouble(df2.format(sprintWiseLoggedTimeMap.get(currentNodeIdentifier)));

				List<JiraIssue> sprintJiraIssues = issueUsedForLoggedTimeMap.get(currentNodeIdentifier);
				populateExcelDataObject(requestTrackerId, excelData, sprintJiraIssues, node, loggedTimePerIssueList);
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
		kpiElement.setExcelColumns(KPIExcelColumn.SPRINT_CAPACITY_UTILIZATION.getColumns(sprintLeafNodeList,
				cacheService, flterHelperService));
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
		Map<String, Object> resultListMap = kpiHelperService.fetchSprintCapacityDataFromDb(leafNodeList);
		List<CapacityKpiData> estimateTimeList = kpiHelperService.fetchCapacityDataFromDB(leafNodeList);
		resultListMap.put(ESTIMATE_TIME, estimateTimeList);
		return resultListMap;

	}

	/**
	 * Prepares Map for loggedWork with each Issue, loggedWork of a sprint,
	 * issueUsedForLoggedTime of a sprint And EstimateTime of a sprint
	 *
	 * @param sprintWiseEstimateTimeMap
	 *            sprintWiseEstimateTimeMap
	 * @param resultMap
	 *            resultMap
	 * @return Map<Pair < String, String>, Double>
	 */
	@SuppressWarnings(UNCHECKED)
	private Map<Pair<String, String>, Double> prepareMapForLoggedWorkAndEstimateTime(
			Map<Pair<String, String>, Double> sprintWiseEstimateTimeMap, Map<String, Object> resultMap,
			List<LoggedTimePerIssue> loggedTimePerIssueList,
			Map<Pair<String, String>, List<JiraIssue>> issueUsedForLoggedTimeMap) {

		List<JiraIssue> allJiraIssue = (List<JiraIssue>) resultMap.get(STORY_LIST);

		List<SprintDetails> sprintDetails = (List<SprintDetails>) resultMap.get(SPRINTSDETAILS);

		Map<Pair<String, String>, Double> loggedTimeMap = new HashMap<>();

		if (CollectionUtils.isNotEmpty(allJiraIssue)) {
			sprintDetails.forEach(sd -> {
				Set<JiraIssue> totalJiraIssues = KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sd,
						sd.getTotalIssues(), allJiraIssue);
				double timeLoggedInSeconds = 0.0d;
				Set<String> issueList = totalJiraIssues.stream().map(JiraIssue::getNumber).collect(Collectors.toSet());
				List<JiraIssueCustomHistory> jiraIssueCustomHistoryList = (List<JiraIssueCustomHistory>) resultMap
						.get(JIRA_ISSUE_HISTORY_DATA);
				if (CollectionUtils.isNotEmpty(jiraIssueCustomHistoryList)) {
					for (JiraIssueCustomHistory jiraIssueCustomHistory : jiraIssueCustomHistoryList) {
						if (issueList.contains(jiraIssueCustomHistory.getStoryID())
								&& jiraIssueCustomHistory.getWorkLog() != null) {
							// timeLoggedForAnIssueInSeconds will give work log of an issue between the time
							// period of sprint startDate to endDate
							double timeLoggedForAnIssueInSeconds = KpiDataHelper.getWorkLogs(
									jiraIssueCustomHistory.getWorkLog(), sd.getStartDate(), sd.getEndDate());
							// this will be used to create map for excel population
							loggedTimePerIssueList.add(new LoggedTimePerIssue(sd.getBasicProjectConfigId().toString(),
									sd.getSprintID(), jiraIssueCustomHistory.getStoryID(),
									timeLoggedForAnIssueInSeconds / (60 * 60)));
							// timeLoggedInSeconds will give work log of all issue between the time period
							// of sprint startDate to endDate
							timeLoggedInSeconds = timeLoggedInSeconds + timeLoggedForAnIssueInSeconds;

						}
					}
				}
				loggedTimeMap.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
						timeLoggedInSeconds / (60 * 60));
				issueUsedForLoggedTimeMap.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
						new ArrayList<>(totalJiraIssues));
			});
		}

		Map<Pair<String, String>, Double> estimateTimeMap;
		estimateTimeMap = ((List<CapacityKpiData>) resultMap.get(ESTIMATE_TIME)).stream()
				.collect(Collectors.toMap(
						key -> Pair.of(key.getBasicProjectConfigId().toString(), key.getSprintID().toLowerCase()),
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
	 * @param loggedTimePerIssueList
	 *            loggedTimePerIssueList
	 */
	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> sprintCapacityList, Node node, List<LoggedTimePerIssue> loggedTimePerIssueList) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			String sprintName = node.getSprintFilter().getName();
			String sprintId = node.getSprintFilter().getId();
			String projectConfigId = node.getProjectFilter().getBasicProjectConfigId().toString();

			// Filter loggedTimePerIssueList based on projectConfigId and sprintId
			List<LoggedTimePerIssue> filteredLoggedTimeList = loggedTimePerIssueList.stream()
					.filter(item -> item.getProjectConfigId().equalsIgnoreCase(projectConfigId)
							&& item.getSprintId().equalsIgnoreCase(sprintId))
					.toList();

			// Create a map of storyId and loggedTimeInHours from filtered list
			Map<String, Double> storyIdToLoggedTimeMap = filteredLoggedTimeList.stream().collect(
					Collectors.toMap(LoggedTimePerIssue::getStoryId, LoggedTimePerIssue::getLoggedTimeInHours,(e1,e2)->e1));

			KPIExcelUtility.populateSprintCapacity(sprintName, sprintCapacityList, excelData, storyIdToLoggedTimeMap);
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
