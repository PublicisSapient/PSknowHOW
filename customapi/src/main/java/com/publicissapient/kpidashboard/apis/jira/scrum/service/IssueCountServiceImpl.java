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

import java.util.*;
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
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IssueCountServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	public static final String PROJECT_WISE_STORY_CATEGORIES = "projectWiseStoryCategories";
	public static final String PROJECT_WISE_TOTAL_CATEGORIES = "projectWiseTotalCategories";
	private static final String STORY_COUNT = "Story Count";
	private static final String TOTAL_COUNT = "Total Count";
	private static final String STORY_LIST = "stories";
	private static final String SPRINTSDETAILS = "sprints";

	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private FilterHelperService flterHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private CacheService cacheService;
	@Autowired
	private KpiDataCacheService kpiDataCacheService;
	@Autowired
	private KpiDataProvider kpiDataProvider;

	private List<String> sprintIdList = Collections.synchronizedList(new ArrayList<>());

	/**
	 * Gets Qualifier Type
	 *
	 * @return KPICode's <tt>ISSUE_COUNT</tt> enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.ISSUE_COUNT.name();
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
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, TreeAggregatorDetail treeAggregatorDetail)
			throws ApplicationException {

		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		sprintIdList = treeAggregatorDetail.getMapOfListOfLeafNodes().get(CommonConstant.SPRINT_MASTER).stream()
				.map(node -> node.getSprintFilter().getId()).collect(Collectors.toList());
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			Filters filters = Filters.getFilter(k);
			if (Filters.SPRINT == filters) {
				sprintWiseLeafNodeValue(mapTmp, v, kpiElement, kpiRequest);
			}
		});

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.ISSUE_COUNT);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.ISSUE_COUNT);

		Map<String, List<DataCount>> sortedMap = trendValuesMap.entrySet().stream().sorted(Map.Entry.comparingByKey())
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
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.entrySet().stream().forEach(trend -> dataList.addAll(trend.getValue()));
			dataCountGroup.setFilter(issueType);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});

		log.debug("[ISSUE-COUNT-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		kpiElement.setTrendValueList(dataCountGroups);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);
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
		Map<ObjectId, List<String>> projectWiseSprints = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			String sprint = leaf.getSprintFilter().getId();
			projectWiseSprints.putIfAbsent(basicProjectConfigId, new ArrayList<>());
			projectWiseSprints.get(basicProjectConfigId).add(sprint);
		});

		// for storing projectWise Total Count type Categories
		Map<String, List<String>> projectWiseJiraIdentification = new HashMap<>();
		// for storing projectWise Story Count type Categories
		Map<String, List<String>> projectWiseStoryCategories = new HashMap<>();
		List<SprintDetails> sprintDetails = new ArrayList<>();
		List<JiraIssue> issueList = new ArrayList<>();
		boolean fetchCachedData = flterHelperService.isFilterSelectedTillSprintLevel(kpiRequest.getLevel(), false);
		projectWiseSprints.forEach((basicProjectConfigId, sprintList) -> {
			Map<String, Object> result;
			if (fetchCachedData) { // fetch data from cache only if Filter is selected till Sprint
				// level.
				result = kpiDataCacheService.fetchIssueCountData(kpiRequest, basicProjectConfigId, sprintIdList,
						KPICode.ISSUE_COUNT.getKpiId());
			} else { // fetch data from DB if filters below Sprint level (i.e. additional filters)
				result = kpiDataProvider.fetchIssueCountDataFromDB(kpiRequest, basicProjectConfigId, sprintList);
			}
			List<JiraIssue> allJiraIssue = (List<JiraIssue>) result.get(STORY_LIST);
			List<SprintDetails> sprintDetailsList = (List<SprintDetails>) result.get(SPRINTSDETAILS);
			Map<String, List<String>> storyCategories = (Map<String, List<String>>) result.get(PROJECT_WISE_STORY_CATEGORIES);
			Map<String, List<String>> totalCategories = (Map<String, List<String>>) result.get(PROJECT_WISE_TOTAL_CATEGORIES);

			issueList.addAll(allJiraIssue);
			sprintDetails.addAll(sprintDetailsList.stream().filter(sprint -> sprintList.contains(sprint.getSprintID()))
					.collect(Collectors.toSet()));
			projectWiseStoryCategories.put(basicProjectConfigId.toString(),
					storyCategories.get(basicProjectConfigId.toString()));
			projectWiseJiraIdentification.put(basicProjectConfigId.toString(),
					totalCategories.get(basicProjectConfigId.toString()));
		});

		resultListMap.put(STORY_LIST, issueList);
		resultListMap.put(SPRINTSDETAILS, sprintDetails);
		resultListMap.put(PROJECT_WISE_STORY_CATEGORIES, projectWiseStoryCategories);
		resultListMap.put(PROJECT_WISE_TOTAL_CATEGORIES, projectWiseJiraIdentification);
		return resultListMap;
	}

	/**
	 * Calculates KPI Metrics
	 *
	 * @param subCategoryMap
	 * @return Integer
	 */
	@Override
	public Double calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		String requestTrackerId = getRequestTrackerId();

		log.debug("[JIRA-ISSUECOUNT][{}]. Total Story Count: {}", requestTrackerId, subCategoryMap);
		return 0.0d;
	}

	/**
	 * Populates KPI value to sprint leaf nodes andgives the trend analysis at
	 * sprint wise.
	 *
	 * @param mapTmp
	 * @param sprintLeafNodeList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();

		sprintLeafNodeList.sort(Comparator.comparing(node -> node.getSprintFilter().getStartDate()));

		String startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();
		long time = System.currentTimeMillis();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
				.get(sprintLeafNodeList.get(0).getProjectFilter().getBasicProjectConfigId());

		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate, kpiRequest);
		log.info("IssueCount taking fetchKPIDataFromDb {}", System.currentTimeMillis() - time);

		List<JiraIssue> allJiraIssue = (List<JiraIssue>) resultMap.get(STORY_LIST);

		List<SprintDetails> sprintDetails = (List<SprintDetails>) resultMap.get(SPRINTSDETAILS);

		Map<String, List<String>> projectWiseStoryCategories = (Map<String, List<String>>) resultMap
				.get(PROJECT_WISE_STORY_CATEGORIES);
		Map<String, List<String>> projectWiseTotalCategories = (Map<String, List<String>>) resultMap
				.get(PROJECT_WISE_TOTAL_CATEGORIES);

		Map<Pair<String, String>, List<JiraIssue>> sprintWiseStoryCatIssues = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseTotalCatIssues = new HashMap<>();

		Map<Pair<String, String>, List<String>> sprintWiseIssueNumbers = new HashMap<>();
		if (CollectionUtils.isNotEmpty(allJiraIssue)) {
			sprintDetails.forEach(sd -> {
				List<String> totalIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sd,
						CommonConstant.TOTAL_ISSUES);
				totalIssues.retainAll(allJiraIssue.stream().distinct().map(JiraIssue::getNumber).collect(Collectors.toList()));
				sprintWiseIssueNumbers.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()), totalIssues);
				Set<JiraIssue> totalJiraIssues = KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sd,
						sd.getTotalIssues(), allJiraIssue);
				List<String> totalIssueCatOfProj = projectWiseTotalCategories.get(sd.getBasicProjectConfigId().toString());
				List<String> totalStoryCatOfProj = projectWiseStoryCategories.get(sd.getBasicProjectConfigId().toString());
				// filtering out issues belong to projectWiseStoryCategories
				List<JiraIssue> storyCatIssues = totalJiraIssues.stream()
						.filter(issue -> totalStoryCatOfProj.contains(issue.getTypeName().toLowerCase()))
						.collect(Collectors.toList());
				// filtering out issues belong to totalCategories
				List<JiraIssue> totalCatIssues = totalJiraIssues.stream()
						.filter(issue -> totalIssueCatOfProj.contains(issue.getTypeName().toLowerCase()))
						.collect(Collectors.toList());
				sprintWiseStoryCatIssues.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
						new ArrayList<>(storyCatIssues));
				sprintWiseTotalCatIssues.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
						new ArrayList<>(totalCatIssues));
			});
		}

		List<KPIExcelData> excelData = new ArrayList<>();

		for (Node node : sprintLeafNodeList) {
			// Leaf node wise data
			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair.of(node.getProjectFilter().getBasicProjectConfigId().toString(),
					currentSprintComponentId);
			Double storyCount = 0.0;
			List<JiraIssue> totalPresentStoryIssue = new ArrayList<>();
			List<JiraIssue> totalPresentTotalIssue = new ArrayList<>();

			if (CollectionUtils.isNotEmpty(sprintWiseIssueNumbers.get(currentNodeIdentifier))) {
				List<String> totalPresentJiraIssue = sprintWiseIssueNumbers.get(currentNodeIdentifier);
				storyCount = ((Integer) totalPresentJiraIssue.size()).doubleValue();
				totalPresentStoryIssue = sprintWiseStoryCatIssues.get(currentNodeIdentifier);
				totalPresentTotalIssue = sprintWiseTotalCatIssues.get(currentNodeIdentifier);
				populateExcelData(requestTrackerId, allJiraIssue, excelData, node, totalPresentJiraIssue, fieldMapping);
			}

			Map<String, Double> issueCountMap = new LinkedHashMap<>();
			issueCountMap.put(STORY_COUNT, (double) totalPresentStoryIssue.size());
			issueCountMap.put(TOTAL_COUNT, (double) totalPresentTotalIssue.size());
			log.debug("[ISSUECOUNT-SPRINT-WISE][{}]. Total Stories Count for sprint {}  is {}", requestTrackerId,
					node.getSprintFilter().getName(), storyCount);

			Map<String, List<DataCount>> dataCountMap = new HashMap<>();

			for (Map.Entry<String, Double> map : issueCountMap.entrySet()) {
				DataCount dataCount = new DataCount();
				dataCount.setData(String.valueOf(map.getValue()));
				dataCount.setSProjectName(trendLineName);
				dataCount.setSSprintID(node.getSprintFilter().getId());
				dataCount.setSSprintName(node.getSprintFilter().getName());
				dataCount.setValue(map.getValue());
				dataCount.setKpiGroup(map.getKey());
				dataCount.setHoverValue(generateHoverMap(totalPresentStoryIssue, totalPresentTotalIssue, map.getKey()));
				dataCountMap.put(map.getKey(), new ArrayList<>(Arrays.asList(dataCount)));
			}
			mapTmp.get(node.getId()).setValue(dataCountMap);
		}
		kpiElement.setExcelData(excelData);
		kpiElement
				.setExcelColumns(KPIExcelColumn.ISSUE_COUNT.getColumns(sprintLeafNodeList, cacheService, flterHelperService));
	}

	private Map<String, Object> generateHoverMap(List<JiraIssue> totalPresentStoryIssue,
			List<JiraIssue> totalPresentTotalIssue, String key) {
		Map<String, Object> hoverMap = new LinkedHashMap<>();

		Map<String, Long> typeWiseCountMapStoryCat = totalPresentStoryIssue.stream()
				.collect(Collectors.groupingBy(JiraIssue::getTypeName, Collectors.counting()));

		Map<String, Long> typeWiseCountMapTotalCat = totalPresentTotalIssue.stream()
				.collect(Collectors.groupingBy(JiraIssue::getTypeName, Collectors.counting()));

		if (STORY_COUNT.equalsIgnoreCase(key)) {
			hoverMap.putAll(typeWiseCountMapStoryCat);
		}
		if (TOTAL_COUNT.equalsIgnoreCase(key)) {
			hoverMap.putAll(typeWiseCountMapTotalCat);
		}
		return hoverMap;
	}

	/**
	 * Populates Validation Data Object
	 *
	 * @param requestTrackerId
	 * @param allJiraIssuesList
	 * @param node
	 * @param totalPresentJiraIssue
	 * @param fieldMapping
	 */
	private void populateExcelData(String requestTrackerId, List<JiraIssue> allJiraIssuesList,
			List<KPIExcelData> excelData, Node node, List<String> totalPresentJiraIssue, FieldMapping fieldMapping) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			String sprintName = node.getSprintFilter().getName();
			KPIExcelUtility.populateSpeedKPIExcelData(sprintName, excelData, allJiraIssuesList, totalPresentJiraIssue,
					fieldMapping, customApiConfig);
		}
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI40(), KPICode.ISSUE_COUNT.getKpiId());
	}
}
