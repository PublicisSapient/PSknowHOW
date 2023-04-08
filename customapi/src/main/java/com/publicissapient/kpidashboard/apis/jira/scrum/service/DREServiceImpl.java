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

/**
 * 
 */
package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
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
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

/**
 * This class calculated KPI value for DRE and its trend analysis.
 * 
 * @author pkum34
 *
 */
@Component
@Slf4j
public class DREServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String SEPARATOR_ASTERISK = "*************************************";
	private static final String CLOSED_DEFECT_DATA = "closedBugKey";
	private static final String TOTAL_DEFECT_DATA = "totalBugKey";
	private static final String SPRINT_WISE_STORY_DATA = "storyData";
	private static final String REMOVED = "Closed Defects";
	private static final String TOTAL = "Total Defects";
	private static final String DEV = "DeveloperKpi";
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private FilterHelperService flterHelperService;

	@Override
	public String getQualifierType() {
		return KPICode.DEFECT_REMOVAL_EFFICIENCY.name();
	}

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

		log.debug("[DRE-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.DEFECT_REMOVAL_EFFICIENCY);
		List<DataCount> trendValues = getTrendValues(kpiRequest, nodeWiseKPIValue, KPICode.DEFECT_REMOVAL_EFFICIENCY);
		kpiElement.setTrendValueList(trendValues);

		log.debug("[DRE-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		List<String> sprintList = new ArrayList<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		Map<String, List<String>> projectWiseDefectRemovelStatus = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, Map<String, List<String>>> droppedDefects = new HashMap<>();

		leafNodeList.forEach(leaf -> {

			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());

			projectWiseDefectRemovelStatus.put(basicProjectConfigId.toString(),
					fieldMapping.getJiraDefectRemovalStatus());
			mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(fieldMapping.getJiraDefectRemovalIssueType()));
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
			KpiHelperService.getDroppedDefectsFilters(droppedDefects, basicProjectConfigId, fieldMapping);

		});

		/** additional filter **/
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, flterHelperService);

		mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
				sprintList.stream().distinct().collect(Collectors.toList()));
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		// Fetch Story ID grouped by Sprint
		List<SprintWiseStory> sprintWiseStoryList = jiraIssueRepository.findIssuesGroupBySprint(mapOfFilters,
				uniqueProjectMap, kpiRequest.getFilterToShowOnTrend(), DEV);
		List<JiraIssue> issuesBySprintAndType = jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters,
				uniqueProjectMap);
		List<JiraIssue> storyListWoDrop = new ArrayList<>();
		KpiHelperService.getDefectsWithoutDrop(droppedDefects, issuesBySprintAndType, storyListWoDrop);
		KpiHelperService.removeRejectedStoriesFromSprint(sprintWiseStoryList, storyListWoDrop);

		List<String> storyIdList = new ArrayList<>();
		sprintWiseStoryList.forEach(s -> storyIdList.addAll(s.getStoryList()));

		Map<String, List<String>> mapOfFiltersWithStoryIds = new LinkedHashMap<>();
		mapOfFiltersWithStoryIds.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));
		mapOfFiltersWithStoryIds.put(JiraFeature.DEFECT_STORY_ID.getFieldValueInFeature(), storyIdList);
		mapOfFiltersWithStoryIds.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
				Arrays.asList(NormalizedJira.DEFECT_TYPE.getValue()));
		// Fetch Defects linked with story ID's
		List<JiraIssue> totalDefectList = jiraIssueRepository.findIssuesByType(mapOfFiltersWithStoryIds);
		List<JiraIssue> defectListWoDrop = new ArrayList<>();
		KpiHelperService.getDefectsWithoutDrop(droppedDefects, totalDefectList, defectListWoDrop);

		// Find defect with closed status. Avoided making dB query
		List<JiraIssue> closeDefectList = defectListWoDrop.stream()
				.filter(f -> CollectionUtils
						.emptyIfNull(projectWiseDefectRemovelStatus.get(f.getBasicProjectConfigId())).stream()
						.anyMatch(s -> s.equalsIgnoreCase(f.getJiraStatus())))
				.collect(Collectors.toList());

		setDbQueryLogger(storyIdList, totalDefectList, closeDefectList);

		resultListMap.put(SPRINT_WISE_STORY_DATA, sprintWiseStoryList);
		resultListMap.put(CLOSED_DEFECT_DATA, closeDefectList);
		resultListMap.put(TOTAL_DEFECT_DATA, defectListWoDrop);

		return resultListMap;

	}

	@SuppressWarnings("unchecked")
	@Override
	public Double calculateKPIMetrics(Map<String, Object> closedAndTotalDefectDataMap) {
		int closedDefectCount = ((List<JiraIssue>) closedAndTotalDefectDataMap.get(CLOSED_DEFECT_DATA)).size();
		int totalDefectCount = ((List<JiraIssue>) closedAndTotalDefectDataMap.get(TOTAL_DEFECT_DATA)).size();
		return (double) Math.round((100.0 * closedDefectCount) / (totalDefectCount));
	}

	/**
	 * This method populates KPI value to sprint leaf nodes. It also gives the trend
	 * analysis at sprint wise.
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

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));

		String startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();

		Map<String, Object> storyDefectDataListMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate,
				kpiRequest);
		List<SprintWiseStory> sprintWiseStoryList = (List<SprintWiseStory>) storyDefectDataListMap
				.get(SPRINT_WISE_STORY_DATA);

		Map<Pair<String, String>, List<SprintWiseStory>> sprintWiseMap = sprintWiseStoryList.stream().collect(Collectors
				.groupingBy(sws -> Pair.of(sws.getBasicProjectConfigId(), sws.getSprint()), Collectors.toList()));

		Map<Pair<String, String>, Double> sprintWiseDREMap = new HashMap<>();
		Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseTotaldDefectListMap = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseCloseddDefectListMap = new HashMap<>();
		List<KPIExcelData> excelData = new ArrayList<>();

		sprintWiseMap.forEach((sprint, sprintWiseStories) -> {

			List<JiraIssue> sprintWiseClosedDefectList = new ArrayList<>();
			List<JiraIssue> sprintWiseTotaldDefectList = new ArrayList<>();
			List<Double> subCategoryWiseDREList = new ArrayList<>();
			List<String> totalStoryIdList = new ArrayList<>();

			sprintWiseStories.stream().map(SprintWiseStory::getStoryList).collect(Collectors.toList())
					.forEach(totalStoryIdList::addAll);

			Map<String, Object> subCategoryWiseClosedAndTotalDefectList = new HashMap<>();
			List<JiraIssue> subCategoryWiseClosedDefectList = ((List<JiraIssue>) storyDefectDataListMap
					.get(CLOSED_DEFECT_DATA))
							.stream()
							.filter(f -> sprint.getKey().equals(f.getBasicProjectConfigId())
									&& CollectionUtils.containsAny(f.getDefectStoryID(), totalStoryIdList))
							.collect(Collectors.toList());
			List<JiraIssue> subCategoryWiseTotaldDefectList = ((List<JiraIssue>) storyDefectDataListMap
					.get(TOTAL_DEFECT_DATA))
							.stream()
							.filter(f -> sprint.getKey().equals(f.getBasicProjectConfigId())
									&& CollectionUtils.containsAny(f.getDefectStoryID(), totalStoryIdList))
							.collect(Collectors.toList());

			double dreForCurrentLeaf = 0.0d;
			subCategoryWiseClosedAndTotalDefectList.put(CLOSED_DEFECT_DATA, subCategoryWiseClosedDefectList);
			subCategoryWiseClosedAndTotalDefectList.put(TOTAL_DEFECT_DATA, subCategoryWiseTotaldDefectList);
			if (CollectionUtils.isNotEmpty(subCategoryWiseClosedDefectList)
					&& CollectionUtils.isNotEmpty(subCategoryWiseTotaldDefectList)) {

				dreForCurrentLeaf = calculateKPIMetrics(subCategoryWiseClosedAndTotalDefectList);
			} else if (CollectionUtils.isEmpty(subCategoryWiseTotaldDefectList)) {
				// Adding check when total defects injected is 0n, DRE will be
				// 100.0 in this case
				dreForCurrentLeaf = 100.0d;
			}
			subCategoryWiseDREList.add(dreForCurrentLeaf);
			sprintWiseClosedDefectList.addAll(subCategoryWiseClosedDefectList);
			sprintWiseTotaldDefectList.addAll(subCategoryWiseTotaldDefectList);
			sprintWiseCloseddDefectListMap.put(sprint,sprintWiseClosedDefectList);
			sprintWiseTotaldDefectListMap.put(sprint,sprintWiseTotaldDefectList);

			setSprintWiseLogger(sprint, totalStoryIdList, sprintWiseTotaldDefectList, sprintWiseClosedDefectList);

			sprintWiseDREMap.put(sprint, dreForCurrentLeaf);
			setHowerMap(sprintWiseHowerMap, sprint, sprintWiseClosedDefectList, sprintWiseTotaldDefectList);
		});

		sprintLeafNodeList.forEach(node -> {

			String trendLineName = node.getProjectFilter().getName();

			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), node.getSprintFilter().getId());

			double dreForCurrentLeaf;

			if (sprintWiseDREMap.containsKey(currentNodeIdentifier)) {
				dreForCurrentLeaf = sprintWiseDREMap.get(currentNodeIdentifier);
				List<JiraIssue> sprintWiseClosedDefectList = sprintWiseCloseddDefectListMap.get(currentNodeIdentifier);
				List<JiraIssue> sprintWiseTotaldDefectList = sprintWiseTotaldDefectListMap.get(currentNodeIdentifier);
				populateExcelDataObject(requestTrackerId, node.getSprintFilter().getName(), excelData,sprintWiseClosedDefectList, sprintWiseTotaldDefectList);

			} else {
				dreForCurrentLeaf = 0.0d;
			}
			log.debug("[DRE-SPRINT-WISE][{}]. DRE for sprint {}  is {}", requestTrackerId,
					node.getSprintFilter().getName(), dreForCurrentLeaf);

			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(Math.round(dreForCurrentLeaf)));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
			dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
			dataCount.setValue(dreForCurrentLeaf);
			dataCount.setHoverValue(sprintWiseHowerMap.get(currentNodeIdentifier));
			mapTmp.get(node.getId()).setValue(new ArrayList<DataCount>(Arrays.asList(dataCount)));
			trendValueList.add(dataCount);

		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_REMOVAL_EFFICIENCY.getColumns());
	}

	private void populateExcelDataObject(String requestTrackerId,String sprintName,
			List<KPIExcelData> excelData, List<JiraIssue> sprintWiseClosedDefectList,
			List<JiraIssue> sprintWiseTotaldDefectList) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			Map<String, JiraIssue> totalDefectList = new HashMap<>();
			sprintWiseTotaldDefectList.stream().forEach(bugs -> totalDefectList.putIfAbsent(bugs.getNumber(), bugs));

			KPIExcelUtility.populateDefectRelatedExcelData(sprintName, totalDefectList, sprintWiseClosedDefectList,
					excelData, KPICode.DEFECT_REMOVAL_EFFICIENCY.getKpiId());

		}
	}

	/**
	 * Sets logger for DB data.
	 * 
	 * @param storyIdList
	 * @param totalDefectList
	 * @param closedDefectList
	 */
	private void setDbQueryLogger(List<String> storyIdList, List<JiraIssue> totalDefectList,
			List<JiraIssue> closedDefectList) {

		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.info(SEPARATOR_ASTERISK);
			log.info("************* DRE (dB) *******************");
			log.info("Story[{}]: {}", storyIdList.size(), storyIdList);
			log.info("TotalDefectList LinkedWith -> story[{}]: {}", totalDefectList.size(),
					totalDefectList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			log.info("ClosedDefectList [{}]: {}", closedDefectList.size(),
					closedDefectList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			log.info(SEPARATOR_ASTERISK);
			log.info("******************X----X*******************");
		}
	}

	/**
	 * Sets logger for sprint level KPI data.
	 * 
	 * @param sprint
	 * @param storyIdList
	 * @param sprintWiseTotaldDefectList
	 * @param sprintWiseClosedDefectList
	 */
	private void setSprintWiseLogger(Pair<String, String> sprint, List<String> storyIdList,
			List<JiraIssue> sprintWiseTotaldDefectList, List<JiraIssue> sprintWiseClosedDefectList) {

		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.debug(SEPARATOR_ASTERISK);
			log.debug("************* SPRINT WISE DRE *******************");
			log.debug("Sprint: {}", sprint.getValue());
			log.debug("Story[{}]: {}", storyIdList.size(), storyIdList);
			log.debug("SprintWiseTotaldDefectList[{}]: {}", sprintWiseTotaldDefectList.size(),
					sprintWiseTotaldDefectList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			log.debug("SprintWiseClosedDefectList[{}]: {}", sprintWiseClosedDefectList.size(),
					sprintWiseClosedDefectList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			log.debug(SEPARATOR_ASTERISK);
			log.debug(SEPARATOR_ASTERISK);
		}
	}

	/**
	 * Sets map to show on hover of sprint node.
	 * 
	 * @param sprintWiseHowerMap
	 * @param sprint
	 * @param closed
	 * @param total
	 */
	private void setHowerMap(Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap,
			Pair<String, String> sprint, List<JiraIssue> closed, List<JiraIssue> total) {
		Map<String, Object> howerMap = new LinkedHashMap<>();
		if (CollectionUtils.isNotEmpty(closed)) {
			howerMap.put(REMOVED, closed.size());
		} else {
			howerMap.put(REMOVED, 0);
		}
		if (CollectionUtils.isNotEmpty(total)) {
			howerMap.put(TOTAL, total.size());
		} else {
			howerMap.put(TOTAL, 0);
		}
		sprintWiseHowerMap.put(sprint, howerMap);
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

}
