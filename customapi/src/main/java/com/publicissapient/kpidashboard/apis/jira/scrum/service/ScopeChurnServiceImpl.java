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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
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
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

import lombok.extern.slf4j.Slf4j;

/**
 * This class fetches the Scope Churn KPI along with trend analysis. Trend
 * analysis for Scope Churn KPI has percentage at y-axis and sprint id at
 * x-axis. {@link JiraKPIService}
 *
 * @author Shubh
 */
@Component
@Slf4j
public class ScopeChurnServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	public static final String TOTAL_ISSUE = "totalIssue";
	public static final String SCOPE_CHANGE = "Scope Change";
	public static final String SPRINT_DETAILS = "sprintDetails";
	public static final String INITIAL_SCOPE = "Initial Commitment";
	public static final String SCOPE_CHANGE_ISSUE_HISTORY = "scopeChangeIssuesHistories";
	public static final String SEPARATOR_ASTERISK = "*************************************";
	private static final String STORY_POINTS = "Story Points";
	private static final String ISSUE_COUNT = "Issue Count";

	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private FilterHelperService filterHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private CacheService cacheService;
	@Autowired
	private KpiDataCacheService kpiDataCacheService;
	@Autowired
	private KpiDataProvider kpiDataProvider;

	private List<String> sprintIdList = Collections.synchronizedList(new ArrayList<>());

	/** {@inheritDoc} */
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

		log.debug("[SCOPE-CHURN-LEAF-NODE-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.SCOPE_CHURN);
		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.SCOPE_CHURN);
		Map<String, List<DataCount>> sortedMap = trendValuesMap.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
		Map<String, Map<String, List<DataCount>>> countProjectWiseDc = new LinkedHashMap<>();
		sortedMap.forEach((countType, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			countProjectWiseDc.put(countType, projectWiseDc);
		});
		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		countProjectWiseDc.forEach((issueType, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = projectWiseDc.values().stream().flatMap(Collection::stream)
					.collect(Collectors.toList());
			dataCountGroup.setFilter(issueType);
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
	 * @param sprintLeafNodeList
	 * @param trendValueList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		Map<String, Object> storyChurnFetchDetails = fetchKPIDataFromDb(sprintLeafNodeList, null, null, kpiRequest);

		List<SprintDetails> sprintDetails = (List<SprintDetails>) storyChurnFetchDetails.get(SPRINT_DETAILS);
		List<JiraIssue> fetchedIssue = (List<JiraIssue>) storyChurnFetchDetails.get(TOTAL_ISSUE);
		List<JiraIssueCustomHistory> fetchedIssueHistory = (List<JiraIssueCustomHistory>) storyChurnFetchDetails
				.getOrDefault(SCOPE_CHANGE_ISSUE_HISTORY, new ArrayList<>());
		Map<String, List<JiraHistoryChangeLog>> issueKeyWiseHistoryMap = fetchedIssueHistory.stream()
				.collect(Collectors.toMap(JiraIssueCustomHistory::getStoryID,
						JiraIssueCustomHistory::getSprintUpdationLog, (existingValue, newValue) -> newValue,
						LinkedHashMap::new));

		Map<Pair<String, String>, Double> sprintWiseStoryChurnDataMap = new HashMap<>();
		Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseAddedListMap = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseRemovedListMap = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseInitialCommitListMap = new HashMap<>();
		Map<Pair<String, String>, String> sprintNameMap = new HashMap<>();
		List<KPIExcelData> excelData = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(sprintDetails)) {

			sprintDetails.forEach(sd -> {
				List<JiraIssue> sprintWiseAddedList;
				List<JiraIssue> sprintWiseRemovedList;
				List<JiraIssue> sprintWiseInitialComitList;

				Map<String, Object> currentSprintLeafNodeDataMap = new HashMap<>();

				List<String> completedIssues = new ArrayList<>(
						KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sd, CommonConstant.COMPLETED_ISSUES));
				List<String> notCompletedIssues = new ArrayList<>(KpiDataHelper
						.getIssuesIdListBasedOnTypeFromSprintDetails(sd, CommonConstant.NOT_COMPLETED_ISSUES));
				List<String> removedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sd,
						CommonConstant.PUNTED_ISSUES);
				List<String> addedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sd,
						CommonConstant.ADDED_ISSUES);
				// For Initial Commitment Issue : completed + notCompleted + removed - added
				List<String> initialCommitIssue = new ArrayList<>();
				initialCommitIssue.addAll(completedIssues);
				initialCommitIssue.addAll(notCompletedIssues);
				initialCommitIssue.addAll(removedIssues);
				initialCommitIssue.removeAll(addedIssues);
				Set<JiraIssue> totalJiraIssueFromSprintReport = KpiDataHelper
						.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sd, new HashSet<>(), fetchedIssue);
				sprintWiseAddedList = totalJiraIssueFromSprintReport.stream()
						.filter(f -> addedIssues.contains(f.getNumber())).collect(Collectors.toList());
				sprintWiseRemovedList = totalJiraIssueFromSprintReport.stream()
						.filter(f -> removedIssues.contains(f.getNumber())).collect(Collectors.toList());
				sprintWiseInitialComitList = totalJiraIssueFromSprintReport.stream()
						.filter(f -> initialCommitIssue.contains(f.getNumber())).collect(Collectors.toList());
				// For Scope Change : Added + Removed Issue (duplicate incl)
				List<JiraIssue> sprintWiseScopeChangeList = new ArrayList<>(sprintWiseAddedList);
				sprintWiseScopeChangeList.addAll(sprintWiseRemovedList);
				double storyChurnForCurrLeaf = Double.NaN;
				currentSprintLeafNodeDataMap.put(SCOPE_CHANGE, sprintWiseScopeChangeList);
				currentSprintLeafNodeDataMap.put(INITIAL_SCOPE, sprintWiseInitialComitList);
				if (CollectionUtils.isNotEmpty(sprintWiseScopeChangeList)
						&& CollectionUtils.isNotEmpty(sprintWiseInitialComitList)) {

					storyChurnForCurrLeaf = calculateKPIMetrics(currentSprintLeafNodeDataMap);
					Pair<String, String> sprint = Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID());
					sprintWiseAddedListMap.put(sprint, sprintWiseAddedList);
					sprintWiseRemovedListMap.put(sprint, sprintWiseRemovedList);
					sprintWiseStoryChurnDataMap.put(sprint, storyChurnForCurrLeaf);
					sprintNameMap.put(sprint, sd.getSprintName());
					sprintWiseInitialCommitListMap.put(sprint, sprintWiseInitialComitList);
					setHoverMap(sprintWiseHowerMap, sprint, sprintWiseScopeChangeList, sprintWiseInitialComitList);
					setSprintWiseLogger(sprint, sprintWiseAddedList, sprintWiseRemovedList, sprintWiseInitialComitList);
				}

			});
		}

		sprintLeafNodeList.forEach(node -> {
			String trendLineName = node.getProjectFilter().getName();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(node.getProjectFilter().getBasicProjectConfigId());

			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), node.getSprintFilter().getId());

			double dreForCurrentLeaf;
			Map<String, List<DataCount>> dataCountMap = new HashMap<>();
			Map<String, Map<String, List<JiraIssue>>> issueCountMap = new HashMap<>();
			if (sprintWiseStoryChurnDataMap.containsKey(currentNodeIdentifier)) {
				dreForCurrentLeaf = sprintWiseStoryChurnDataMap.get(currentNodeIdentifier);
				List<JiraIssue> sprintWiseAddedList = sprintWiseAddedListMap.get(currentNodeIdentifier);
				List<JiraIssue> spirntWiseRemovedList = sprintWiseRemovedListMap.get(currentNodeIdentifier);
				String sprintName = sprintNameMap.get(currentNodeIdentifier);
				populateExcelDataObject(node.getSprintFilter().getName(), excelData, sprintWiseAddedList,
						spirntWiseRemovedList, issueKeyWiseHistoryMap, fieldMapping, sprintName);
				issueCountMap = getFiltersMap(sprintWiseAddedListMap, sprintWiseRemovedListMap,
						sprintWiseInitialCommitListMap, currentNodeIdentifier);

			} else {
				dreForCurrentLeaf = Double.NaN;
				setDefaultMap(issueCountMap);
			}
			log.debug("[SCOPE-CHURN-SPRINT-WISE][{}]. STORY-CHURN for sprint {}  is {}", requestTrackerId,
					node.getSprintFilter().getName(), dreForCurrentLeaf);

			for (Map.Entry<String, Map<String, List<JiraIssue>>> map : issueCountMap.entrySet()) {
				DataCount dataCount = getDataCount(node, trendLineName, dataCountMap, map, fieldMapping);
				trendValueList.add(dataCount);
			}
			mapTmp.get(node.getId()).setValue(dataCountMap);
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(
				KPIExcelColumn.SCOPE_CHURN.getColumns(sprintLeafNodeList, cacheService, filterHelperService));
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		String requestTrackerId = getRequestTrackerId();
		Map<ObjectId, List<String>> projectWiseSprints = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			String sprint = leaf.getSprintFilter().getId();
			projectWiseSprints.putIfAbsent(basicProjectConfigId, new ArrayList<>());
			projectWiseSprints.get(basicProjectConfigId).add(sprint);
		});

		List<SprintDetails> sprintDetails = new ArrayList<>();
		List<JiraIssue> totalJiraIssue = new ArrayList<>();
		List<JiraIssueCustomHistory> scopeChangeIssueHistories = new ArrayList<>();

		boolean fetchCachedData = filterHelperService.isFilterSelectedTillSprintLevel(kpiRequest.getLevel(), false);
		projectWiseSprints.forEach((basicProjectConfigId, sprintList) -> {
			Map<String, Object> result;
			if (fetchCachedData) { // fetch data from cache only if Filter is selected till Sprint
				// level.
				result = kpiDataCacheService.fetchScopeChurnData(kpiRequest, basicProjectConfigId, sprintIdList,
						KPICode.SCOPE_CHURN.getKpiId());
			} else { // fetch data from DB if filters below Sprint level (i.e. additional filters)
				result = kpiDataProvider.fetchScopeChurnData(kpiRequest, basicProjectConfigId, sprintList);
			}
			List<JiraIssue> allJiraIssue = (List<JiraIssue>) result.get(TOTAL_ISSUE);
			List<SprintDetails> sprintDetailsList = (List<SprintDetails>) result.get(SPRINT_DETAILS);
			List<JiraIssueCustomHistory> issueHistories = (List<JiraIssueCustomHistory>) result
					.get(SCOPE_CHANGE_ISSUE_HISTORY);

			sprintDetails.addAll(sprintDetailsList.stream().filter(sprint -> sprintList.contains(sprint.getSprintID()))
					.collect(Collectors.toSet()));
			totalJiraIssue.addAll(allJiraIssue);
			if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
				scopeChangeIssueHistories.addAll(issueHistories);
			}
		});

		resultListMap.put(TOTAL_ISSUE, totalJiraIssue);
		resultListMap.put(SPRINT_DETAILS, sprintDetails);
		resultListMap.put(SCOPE_CHANGE_ISSUE_HISTORY, scopeChangeIssueHistories);

		setDbQueryLogger(sprintDetails, totalJiraIssue, scopeChangeIssueHistories);
		return resultListMap;
	}

	/**
	 * Sets DB Query log
	 *
	 * @param sprintDetails
	 * @param totalJiraIssue
	 * @param scopeChangeIssueHistories
	 */
	private void setDbQueryLogger(List<SprintDetails> sprintDetails, List<JiraIssue> totalJiraIssue,
			List<JiraIssueCustomHistory> scopeChangeIssueHistories) {
		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.info(SEPARATOR_ASTERISK);
			log.info("************* SCOPE CHURN (dB) *******************");
			log.info("SprintDetails[{}]: {}", sprintDetails.size(), sprintDetails);
			log.info("TotalJiraIssue[{}]: {}", totalJiraIssue.size(), totalJiraIssue);
			log.info("ScopeChangeJiraIssueHistory[{}]: {}", scopeChangeIssueHistories.size(),
					scopeChangeIssueHistories);
			log.info(SEPARATOR_ASTERISK);
			log.info("******************X----X*******************");
		}
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public Double calculateKPIMetrics(Map<String, Object> scopeChangeAndInitialScopeMap) {
		int scopeChange = ((List<JiraIssue>) scopeChangeAndInitialScopeMap.get(SCOPE_CHANGE)).size();
		int initialScope = ((List<JiraIssue>) scopeChangeAndInitialScopeMap.get(INITIAL_SCOPE)).size();
		return (double) Math.round((100.0 * scopeChange) / (initialScope));
	}

	/**
	 * Sets map to show on hover of sprint node.
	 *
	 * @param sprintWiseHowerMap
	 * @param sprint
	 * @param scopeChange
	 * @param initialIssue
	 */
	private void setHoverMap(Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap,
			Pair<String, String> sprint, List<JiraIssue> scopeChange, List<JiraIssue> initialIssue) {
		Map<String, Object> howerMap = new LinkedHashMap<>();
		if (CollectionUtils.isNotEmpty(scopeChange)) {
			howerMap.put(SCOPE_CHANGE, scopeChange.size());
		} else {
			howerMap.put(SCOPE_CHANGE, 0);
		}
		if (CollectionUtils.isNotEmpty(initialIssue)) {
			howerMap.put(INITIAL_SCOPE, initialIssue.size());
		} else {
			howerMap.put(INITIAL_SCOPE, 0);
		}
		sprintWiseHowerMap.put(sprint, howerMap);
	}

	/**
	 * Sets logger for sprint level KPI data.
	 *
	 * @param sprint
	 * @param sprintWiseAddedList
	 * @param sprintWiseRemovedList
	 * @param sprintWiseInitialComitList
	 */
	private void setSprintWiseLogger(Pair<String, String> sprint, List<JiraIssue> sprintWiseAddedList,
			List<JiraIssue> sprintWiseRemovedList, List<JiraIssue> sprintWiseInitialComitList) {
		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.debug(SEPARATOR_ASTERISK);
			log.debug("************* SPRINT WISE SCOPE CHURN *******************");
			log.debug("Sprint: {}", sprint.getValue());
			log.debug("SprintWiseAddedList[{}]: {}", sprintWiseAddedList.size(),
					sprintWiseAddedList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			log.debug("SprintWiseRemovedList[{}]: {}", sprintWiseRemovedList.size(),
					sprintWiseAddedList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			log.debug("SprintWiseInitialCommitList[{}]: {}", sprintWiseInitialComitList.size(),
					sprintWiseAddedList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			log.debug(SEPARATOR_ASTERISK);
		}
	}

	/**
	 * Method to populate the Excel
	 *
	 * @param sprintName
	 * @param excelData
	 * @param sprintWiseAddedList
	 * @param sprintWiseRemovedList
	 * @param issueWiseHistoryMap
	 * @param fieldMapping
	 * @param curSprintName
	 */
	private void populateExcelDataObject(String sprintName, List<KPIExcelData> excelData,
			List<JiraIssue> sprintWiseAddedList, List<JiraIssue> sprintWiseRemovedList,
			Map<String, List<JiraHistoryChangeLog>> issueWiseHistoryMap, FieldMapping fieldMapping,
			String curSprintName) {
		String requestTrackerId = getRequestTrackerId();

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			Map<String, String> addedIssueDateMap = KpiDataHelper.processSprintIssues(sprintWiseAddedList,
					curSprintName, issueWiseHistoryMap, CommonConstant.ADDED);
			Map<String, String> removedIssueDateMap = KpiDataHelper.processSprintIssues(sprintWiseRemovedList,
					curSprintName, issueWiseHistoryMap, CommonConstant.REMOVED);

			if (CollectionUtils.isNotEmpty(sprintWiseRemovedList) || CollectionUtils.isNotEmpty(sprintWiseAddedList)) {
				Map<String, List<JiraIssue>> totalSprintStoryMap = new HashMap<>();
				totalSprintStoryMap.put(CommonConstant.ADDED, sprintWiseAddedList);
				totalSprintStoryMap.put(CommonConstant.REMOVED, sprintWiseRemovedList);
				KPIExcelUtility.populateScopeChurn(sprintName, totalSprintStoryMap, addedIssueDateMap,
						removedIssueDateMap, excelData, fieldMapping, customApiConfig);
			}
		}
	}

	private DataCount getDataCount(Node node, String trendLineName, Map<String, List<DataCount>> dataCountMap,
			Map.Entry<String, Map<String, List<JiraIssue>>> map, FieldMapping fieldMapping) {
		DataCount dataCount = new DataCount();
		dataCount.setSProjectName(trendLineName);
		dataCount.setSSprintID(node.getSprintFilter().getId());
		dataCount.setSSprintName(node.getSprintFilter().getName());
		getDataCountValues(map, dataCount, fieldMapping);
		dataCount.setHoverValue(generateHoverMap(map.getValue(), map.getKey(), fieldMapping));
		dataCountMap.put(map.getKey(), new ArrayList<>(Collections.singletonList(dataCount)));
		return dataCount;
	}

	/*
	 * This method will construct filterMap for story points and issue count with
	 * initial scope and scope change jiraIssues
	 */
	private static Map<String, Map<String, List<JiraIssue>>> getFiltersMap(
			Map<Pair<String, String>, List<JiraIssue>> sprintWiseAddedListMap,
			Map<Pair<String, String>, List<JiraIssue>> sprintWiseRemovedListMap,
			Map<Pair<String, String>, List<JiraIssue>> sprintWiseInitialCommitListMap,
			Pair<String, String> currentNodeIdentifier) {
		Map<String, Map<String, List<JiraIssue>>> issueCountMap = new LinkedHashMap<>();
		Map<String, List<JiraIssue>> storyPointMap = new HashMap<>();
		storyPointMap.put(INITIAL_SCOPE, sprintWiseInitialCommitListMap.get(currentNodeIdentifier));
		List<JiraIssue> scopeChangeList = new ArrayList<>(sprintWiseAddedListMap.get(currentNodeIdentifier));
		scopeChangeList.addAll(sprintWiseRemovedListMap.get(currentNodeIdentifier));
		storyPointMap.put(SCOPE_CHANGE, scopeChangeList);
		issueCountMap.put(STORY_POINTS, storyPointMap);

		Map<String, List<JiraIssue>> issueCountMapMap = new HashMap<>();
		issueCountMapMap.put(INITIAL_SCOPE, sprintWiseInitialCommitListMap.get(currentNodeIdentifier));
		List<JiraIssue> scopeChangeListForIssueCount = new ArrayList<>(
				sprintWiseAddedListMap.get(currentNodeIdentifier));
		scopeChangeListForIssueCount.addAll(sprintWiseRemovedListMap.get(currentNodeIdentifier));
		issueCountMapMap.put(SCOPE_CHANGE, scopeChangeListForIssueCount);
		issueCountMap.put(ISSUE_COUNT, issueCountMapMap);
		return issueCountMap;
	}

	private void setDefaultMap(Map<String, Map<String, List<JiraIssue>>> issueCountMap) {
		Map<String, List<JiraIssue>> storyPointMap = new HashMap<>();
		storyPointMap.put(INITIAL_SCOPE, null);
		storyPointMap.put(SCOPE_CHANGE, null);
		issueCountMap.put(STORY_POINTS, storyPointMap);
		issueCountMap.put(ISSUE_COUNT, storyPointMap);
	}

	private static void getDataCountValues(Map.Entry<String, Map<String, List<JiraIssue>>> map, DataCount dataCount,
			FieldMapping fieldMapping) {
		setStoryPoints(map, dataCount, fieldMapping);
		setIssueCount(map, dataCount);
	}

	/*
	 * This method sets issue count of both the scope change and initial scope
	 */
	private static void setIssueCount(Map.Entry<String, Map<String, List<JiraIssue>>> map, DataCount dataCount) {
		if (ISSUE_COUNT.equalsIgnoreCase(map.getKey())) {
			double scopeChangeIssuesCount = Double.NaN;
			double initialScopeIssuesCount = Double.NaN;
			for (Map.Entry<String, List<JiraIssue>> entry : map.getValue().entrySet()) {
				String s = entry.getKey();
				if (entry.getValue() != null) {
					double storySize = entry.getValue().size();
					if (s.equalsIgnoreCase(SCOPE_CHANGE)) {
						scopeChangeIssuesCount = storySize;
					} else if (s.equalsIgnoreCase(INITIAL_SCOPE)) {
						initialScopeIssuesCount = storySize;
					}
				}
			}

			double data = Double.NaN;
			if (!Double.isNaN(initialScopeIssuesCount) && !Double.isNaN(scopeChangeIssuesCount)) {
				data = 100.0 * scopeChangeIssuesCount / initialScopeIssuesCount;
			}

			createDataCount(dataCount, data);
		}
	}

	/*
	 * This method sets story Points of both the scope change and initial scope
	 */
	private static void setStoryPoints(Map.Entry<String, Map<String, List<JiraIssue>>> map, DataCount dataCount,
			FieldMapping fieldMapping) {
		if (STORY_POINTS.equalsIgnoreCase(map.getKey())) {
			double scopeChangeStoryPoints = Double.NaN;
			double initialScopeStoryPoints = Double.NaN;
			for (Map.Entry<String, List<JiraIssue>> entry : map.getValue().entrySet()) {
				String s = entry.getKey();
				if (entry.getValue() != null) {
					double storyPoints = KpiDataHelper.calculateStoryPoints(entry.getValue(), fieldMapping);
					if (s.equalsIgnoreCase(SCOPE_CHANGE)) {
						scopeChangeStoryPoints = storyPoints;
					} else if (s.equalsIgnoreCase(INITIAL_SCOPE)) {
						initialScopeStoryPoints = storyPoints;
					}
				}
			}

			double data = Double.NaN;
			if (!Double.isNaN(scopeChangeStoryPoints) && !Double.isNaN(initialScopeStoryPoints)) {
				data = 100.0 * scopeChangeStoryPoints / initialScopeStoryPoints;
			}

			createDataCount(dataCount, data);
		}
	}

	private static void createDataCount(DataCount dataCount, double data) {
		if (!Double.isNaN(data)) {
			dataCount.setData(String.valueOf(Math.round(data)));
			dataCount.setValue(Math.round(data));
		}
	}

	/*
	 * This method generate hoverMap for the scope change and initial scope based on
	 * the issue count and story points
	 */
	private Map<String, Object> generateHoverMap(Map<String, List<JiraIssue>> valueMap, String key,
			FieldMapping fieldMapping) {
		Map<String, Object> hoverMap = new LinkedHashMap<>();
		if (STORY_POINTS.equalsIgnoreCase(key)) {
			valueMap.forEach((s, jiraIssues) -> {

				if (jiraIssues != null) {
					double storyPoints = roundingOff(KpiDataHelper.calculateStoryPoints(jiraIssues, fieldMapping));
					hoverMap.put(s, storyPoints);
				} else {
					hoverMap.put(s, null);
				}

			});
		}
		if (ISSUE_COUNT.equalsIgnoreCase(key)) {
			valueMap.forEach((s, jiraIssues) -> {
				if (jiraIssues != null) {
					double issueCount = jiraIssues.size();
					hoverMap.put(s, issueCount);
				} else {
					hoverMap.put(s, null);
				}

			});
		}
		return hoverMap;
	}

	/** {@inheritDoc} */
	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	/** {@inheritDoc} */
	@Override
	public String getQualifierType() {
		return KPICode.SCOPE_CHURN.name();
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI164(), KPICode.SCOPE_CHURN.getKpiId());
	}
}
