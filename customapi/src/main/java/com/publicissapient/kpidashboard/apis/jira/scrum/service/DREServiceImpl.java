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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

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
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.DEFECT_REMOVAL_EFFICIENCY);
		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, nodeWiseKPIValue,
				KPICode.DEFECT_REMOVAL_EFFICIENCY);
		trendValuesMap = sortTrendValueMap(trendValuesMap, priorityTypes(true));
		Map<String, Map<String, List<DataCount>>> issueTypeProjectWiseDc = new LinkedHashMap<>();
		trendValuesMap.forEach((issueType, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			issueTypeProjectWiseDc.put(issueType, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		issueTypeProjectWiseDc.forEach((issueType, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.entrySet().stream().forEach(trend -> dataList.addAll(trend.getValue()));
			dataCountGroup.setFilter(issueType);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});
		kpiElement.setTrendValueList(dataCountGroups);
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
		return calculateDREValue(closedDefectCount, totalDefectCount);
	}

	public Map<String, Double> calculateKPIMetrics(Map<String, List<JiraIssue>> priorityWiseTotalDefectListMap,
			Map<String, List<JiraIssue>> priorityWiseClosedDefectListMap) {
		Map<String, Double> priorityValueMap = new HashMap<>();
		Set<String> priorities = priorityWiseTotalDefectListMap.keySet();
		if (CollectionUtils.isNotEmpty(priorities)) {
			for (String priority : priorities) {
				int priorityClosedDefectCount = CollectionUtils
						.isNotEmpty(priorityWiseClosedDefectListMap.get(priority))
								? priorityWiseClosedDefectListMap.get(priority).size()
								: 0;
				int priorityTotalDefectCount = CollectionUtils.isNotEmpty(priorityWiseTotalDefectListMap.get(priority))
						? priorityWiseTotalDefectListMap.get(priority).size()
						: 0;
				priorityValueMap.put(priority, calculateDREValue(priorityClosedDefectCount, priorityTotalDefectCount));
			}
		}
		return priorityValueMap;
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

		Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseTotaldDefectListMap = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseCloseddDefectListMap = new HashMap<>();

		Map<Pair<String, String>, Map<String, Double>> sprintWiseDREPriorityMap = new HashMap<>();
		Map<Pair<String, String>, Map<String, List<JiraIssue>>> sprintPriorityWiseTotalDefectListMap = new HashMap<>();
		Map<Pair<String, String>, Map<String, List<JiraIssue>>> sprintPriorityWiseClosedDefectListMap = new HashMap<>();

		List<KPIExcelData> excelData = new ArrayList<>();

		Set<String> projectWisePriorityList = new HashSet<>();
		sprintWiseMap.forEach((sprint, sprintWiseStories) -> {

			List<JiraIssue> sprintWiseClosedDefectList = new ArrayList<>();
			List<JiraIssue> sprintWiseTotaldDefectList = new ArrayList<>();

			List<String> totalStoryIdList = new ArrayList<>();

			sprintWiseStories.stream().map(SprintWiseStory::getStoryList).collect(Collectors.toList())
					.forEach(totalStoryIdList::addAll);

			List<JiraIssue> subCategoryWiseClosedDefectList = getFilteredDefects(
					(List<JiraIssue>) storyDefectDataListMap.get(CLOSED_DEFECT_DATA), sprint, totalStoryIdList);
			List<JiraIssue> subCategoryWiseTotaldDefectList = getFilteredDefects(
					(List<JiraIssue>) storyDefectDataListMap.get(TOTAL_DEFECT_DATA), sprint, totalStoryIdList);

			Map<String, List<JiraIssue>> priorityWiseTotalDefectListMap = getPriorityWiseDefectListMap(
					subCategoryWiseTotaldDefectList, sprint, totalStoryIdList);
			Map<String, List<JiraIssue>> priorityWiseClosedDefectListMap = getPriorityWiseDefectListMap(
					subCategoryWiseClosedDefectList, sprint, totalStoryIdList);

			sprintWiseClosedDefectList.addAll(subCategoryWiseClosedDefectList);
			sprintWiseTotaldDefectList.addAll(subCategoryWiseTotaldDefectList);
			sprintWiseTotaldDefectListMap.put(sprint, sprintWiseTotaldDefectList);
			sprintWiseCloseddDefectListMap.put(sprint, sprintWiseClosedDefectList);

			projectWisePriorityList.addAll(priorityWiseTotalDefectListMap.keySet());
			sprintPriorityWiseTotalDefectListMap.put(sprint, priorityWiseTotalDefectListMap);
			sprintPriorityWiseClosedDefectListMap.put(sprint, priorityWiseClosedDefectListMap);

			setSprintWiseLogger(sprint, totalStoryIdList, sprintWiseTotaldDefectList, sprintWiseClosedDefectList);
			setHowerMap(sprintWiseHowerMap, sprint, priorityWiseClosedDefectListMap, priorityWiseTotalDefectListMap);
			sprintWiseDREPriorityMap.put(sprint,
					calculateKPIMetrics(priorityWiseTotalDefectListMap, priorityWiseClosedDefectListMap));
		});

		sprintLeafNodeList.forEach(node -> {

			String trendLineName = node.getProjectFilter().getName();

			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), node.getSprintFilter().getId());

			Map<String, List<DataCount>> dataCountMap = new HashMap<>();
			Map<String, Double> priorityMap = sprintWiseDREPriorityMap.getOrDefault(currentNodeIdentifier,
					new HashMap<>());
			Map<String, Double> finalMap = new HashMap<>();
			Map<String, Object> overAllHoverValueMap = new HashMap<>();
			if (CollectionUtils.isNotEmpty(projectWisePriorityList)) {
				projectWisePriorityList.forEach(priority -> {
					Double priorityWiseCount = priorityMap.getOrDefault(priority, 100.0D);
					finalMap.put(StringUtils.capitalize(priority), priorityWiseCount);
					overAllHoverValueMap.put(StringUtils.capitalize(priority), priorityWiseCount);
				});
				projectWisePriorityList.forEach(priority -> finalMap.computeIfAbsent(priority, val -> 100.0D));
				int sprintClosedDefectCount = CollectionUtils
						.isNotEmpty(sprintWiseCloseddDefectListMap.get(currentNodeIdentifier))
								? sprintWiseCloseddDefectListMap.get(currentNodeIdentifier).size()
								: 0;
				int sprintTotalDefectCount = CollectionUtils
						.isNotEmpty(sprintWiseTotaldDefectListMap.get(currentNodeIdentifier))
								? sprintWiseTotaldDefectListMap.get(currentNodeIdentifier).size()
								: 0;
				finalMap.put(CommonConstant.OVERALL,
						calculateDREValue(sprintClosedDefectCount, sprintTotalDefectCount));
			}
			String finalTrendLineName = trendLineName;
			finalMap.forEach((priority, value) -> {
				DataCount dataCount = getDataCountObject(node, finalTrendLineName, sprintWiseHowerMap, priority, value,
						currentNodeIdentifier);
				populateDataValues(dataCount, priority, currentNodeIdentifier, sprintPriorityWiseTotalDefectListMap,
						sprintPriorityWiseClosedDefectListMap, sprintWiseTotaldDefectListMap,
						sprintWiseCloseddDefectListMap);
				trendValueList.add(dataCount);
				dataCountMap.computeIfAbsent(priority, k -> new ArrayList<>()).add(dataCount);
			});

			populateExcelDataObject(requestTrackerId, node.getSprintFilter().getName(), excelData,
					currentNodeIdentifier, sprintWiseCloseddDefectListMap, sprintWiseTotaldDefectListMap);
			log.debug("[DRE-SPRINT-WISE][{}]. DRE for sprint {} is {}", requestTrackerId,
					node.getSprintFilter().getName());
			mapTmp.get(node.getId()).setValue(dataCountMap);
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_REMOVAL_EFFICIENCY.getColumns());
	}

	private List<JiraIssue> getFilteredDefects(List<JiraIssue> subCategoryWiseClosedDefectList,
			Pair<String, String> sprint, List<String> totalStoryIdList) {
		return subCategoryWiseClosedDefectList.stream()
				.filter(f -> sprint.getKey().equals(f.getBasicProjectConfigId())
						&& CollectionUtils.containsAny(f.getDefectStoryID(), totalStoryIdList))
				.collect(Collectors.toList());
	}

	private Map<String, List<JiraIssue>> getPriorityWiseDefectListMap(List<JiraIssue> subCategoryWiseTotaldDefectList,
			Pair<String, String> sprint, List<String> totalStoryIdList) {
		Map<String, List<JiraIssue>> priorityWiseTotalDefectListMap = new HashMap<>();
		for (JiraIssue defect : subCategoryWiseTotaldDefectList) {
			if (sprint.getKey().equals(defect.getBasicProjectConfigId())
					&& CollectionUtils.containsAny(defect.getDefectStoryID(), totalStoryIdList)) {
				String priorityValue = KPIHelperUtil.getPriorityValue(defect.getPriority(), customApiConfig);
				if (CollectionUtils.isEmpty(priorityWiseTotalDefectListMap.get(priorityValue)))
					priorityWiseTotalDefectListMap.put(priorityValue, new ArrayList<>());
				priorityWiseTotalDefectListMap.get(priorityValue).add(defect);
			}
		}
		return priorityWiseTotalDefectListMap;
	}

	private DataCount getDataCountObject(Node node, String trendLineName,
			Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap, String priority, Double value,
			Pair<String, String> currentNodeIdentifier) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(Math.round(value)));
		dataCount.setSProjectName(trendLineName);
		dataCount.setSSprintID(node.getSprintFilter().getId());
		dataCount.setSSprintName(node.getSprintFilter().getName());
		dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
		dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
		dataCount.setValue(value);
		dataCount.setKpiGroup(priority);
		if (sprintWiseHowerMap != null && sprintWiseHowerMap.get(currentNodeIdentifier) != null) {
			Map<String, Object> howerMap = new LinkedHashMap<>();
			if (CommonConstant.OVERALL.equalsIgnoreCase(priority)) {
				howerMap = sprintWiseHowerMap.get(currentNodeIdentifier);
			} else if (sprintWiseHowerMap.get(currentNodeIdentifier).get(priority) != null) {
				howerMap.put(priority, sprintWiseHowerMap.get(currentNodeIdentifier).get(priority));
			} else {
				howerMap.put(priority, getHoverString(0, 0));
			}
			dataCount.setHoverValue(howerMap);
		}
		return dataCount;
	}

	private void populateDataValues(DataCount dataCount, String priority, Pair<String, String> currentNodeIdentifier,
			Map<Pair<String, String>, Map<String, List<JiraIssue>>> sprintPriorityWiseTotalDefectListMap,
			Map<Pair<String, String>, Map<String, List<JiraIssue>>> sprintPriorityWiseClosedDefectListMap,
			Map<Pair<String, String>, List<JiraIssue>> sprintWiseTotaldDefectListMap,
			Map<Pair<String, String>, List<JiraIssue>> sprintWiseCloseddDefectListMap) {
		Integer sprintPriorityTotalDefectCount = sprintPriorityWiseTotalDefectListMap.get(currentNodeIdentifier) != null
				&& CollectionUtils
						.isNotEmpty(sprintPriorityWiseTotalDefectListMap.get(currentNodeIdentifier).get(priority))
								? sprintPriorityWiseTotalDefectListMap.get(currentNodeIdentifier).get(priority).size()
								: 0;
		Integer sprintPriorityClosedDefectCount = sprintPriorityWiseClosedDefectListMap
				.get(currentNodeIdentifier) != null
				&& CollectionUtils
						.isNotEmpty(sprintPriorityWiseClosedDefectListMap.get(currentNodeIdentifier).get(priority))
								? sprintPriorityWiseClosedDefectListMap.get(currentNodeIdentifier).get(priority).size()
								: 0;
		if (CommonConstant.OVERALL.equalsIgnoreCase(priority)) {
			sprintPriorityTotalDefectCount = CollectionUtils
					.isNotEmpty(sprintWiseTotaldDefectListMap.get(currentNodeIdentifier))
							? sprintWiseTotaldDefectListMap.get(currentNodeIdentifier).size()
							: 0;
			sprintPriorityClosedDefectCount = CollectionUtils
					.isNotEmpty(sprintWiseCloseddDefectListMap.get(currentNodeIdentifier))
							? sprintWiseCloseddDefectListMap.get(currentNodeIdentifier).size()
							: 0;
		}
		Map<String, Object> dataValues = new HashMap<>();
		dataValues.put("totalValue", sprintPriorityTotalDefectCount);
		dataValues.put("actualValue", sprintPriorityClosedDefectCount);
		dataCount.setDataValues(dataValues);
	}

	private String getHoverString(int closedDefectCount, int totalDefectCount) {
		return REMOVED + ":" + closedDefectCount + ", " + TOTAL + ":" + totalDefectCount;
	}

	private Double calculateDREValue(int closedDefectCount, int totalDefectCount) {
		Double dreValue = 100D;
		if (totalDefectCount > 0) {
			dreValue = (double) Math.round((100.0 * closedDefectCount) / (totalDefectCount));
		}
		return dreValue;
	}

	private void populateExcelDataObject(String requestTrackerId, String sprintName, List<KPIExcelData> excelData,
			Pair<String, String> currentNodeIdentifier,
			Map<Pair<String, String>, List<JiraIssue>> sprintWiseCloseddDefectListMap,
			Map<Pair<String, String>, List<JiraIssue>> sprintWiseTotaldDefectListMap) {
		List<JiraIssue> sprintWiseClosedDefectList = sprintWiseCloseddDefectListMap.get(currentNodeIdentifier);
		List<JiraIssue> sprintWiseTotaldDefectList = sprintWiseTotaldDefectListMap.get(currentNodeIdentifier);
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
	 * @param closedDefects
	 * @param totalDefects
	 */
	private void setHowerMap(Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap,
			Pair<String, String> sprint, Map<String, List<JiraIssue>> closedDefects,
			Map<String, List<JiraIssue>> totalDefects) {
		Map<String, Object> howerMap = new LinkedHashMap<>();
		if (closedDefects != null && totalDefects != null && CollectionUtils.isNotEmpty(totalDefects.keySet())) {
			Set<String> priorities = totalDefects.keySet();
			for (String priority : priorities) {
				int closedDefectCount = CollectionUtils.isNotEmpty(closedDefects.get(priority))
						? closedDefects.get(priority).size()
						: 0;
				int totalDefectCount = CollectionUtils.isNotEmpty(totalDefects.get(priority))
						? totalDefects.get(priority).size()
						: 0;
				howerMap.put(priority, getHoverString(closedDefectCount, totalDefectCount));
			}
		}
		sprintWiseHowerMap.put(sprint, howerMap);
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	private Map<String, List<DataCount>> sortTrendValueMap(Map<String, List<DataCount>> trendMap,
			List<String> keyOrder) {
		Map<String, List<DataCount>> sortedMap = new LinkedHashMap<>();
		keyOrder.forEach(order -> {
			if (null != trendMap.get(order)) {
				sortedMap.put(order, trendMap.get(order));
			}
		});
		return sortedMap;
	}

	private List<String> priorityTypes(boolean addOverall) {
		if (addOverall) {
			return Arrays.asList(CommonConstant.OVERALL, Constant.P1, Constant.P2, Constant.P3, Constant.P4,
					Constant.MISC);
		} else {
			return Arrays.asList(Constant.P1, Constant.P2, Constant.P3, Constant.P4, Constant.MISC);
		}
	}

}
