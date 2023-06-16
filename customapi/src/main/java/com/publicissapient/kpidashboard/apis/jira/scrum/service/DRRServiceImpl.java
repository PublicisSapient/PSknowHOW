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
import java.util.stream.Collectors;

import javax.ws.rs.core.Feature;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
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

import lombok.extern.slf4j.Slf4j;

/**
 * This class calculates the DRR and trend analysis of the DRR.
 * 
 * @author pkum34
 *
 */
@Component
@Slf4j
public class DRRServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String SEPARATOR_ASTERISK = "*************************************";
	private static final String REJECTED_DEFECT_DATA = "rejectedBugKey";
	private static final String TOTAL_DEFECT_DATA = "totalBugKey";
	private static final String SPRINT_WISE_STORY_DATA = "storyData";
	private static final String REJECTED = "Rejected Defects";
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
		return KPICode.DEFECT_REJECTION_RATE.name();
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

		log.debug("[DRR-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.DEFECT_REJECTION_RATE);
		List<DataCount> trendValues = getTrendValues(kpiRequest, nodeWiseKPIValue, KPICode.DEFECT_REJECTION_RATE);
		kpiElement.setTrendValueList(trendValues);

		log.debug("[DRR-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
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
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, Map<String, List<String>>> defectResolutionRejectionMap = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());

			KpiHelperService.getDroppedDefectsFilters(defectResolutionRejectionMap, basicProjectConfigId, fieldMapping);
			mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(fieldMapping.getJiraDefectRejectionlIssueType()));
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
		});
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, flterHelperService);

		mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
				sprintList.stream().distinct().collect(Collectors.toList()));
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		// Fetch Story ID List grouped by Sprint
		List<SprintWiseStory> sprintWiseStoryList = jiraIssueRepository.findIssuesGroupBySprint(mapOfFilters,
				uniqueProjectMap, kpiRequest.getFilterToShowOnTrend(), DEV);

		List<String> storyIdList = new ArrayList<>();
		sprintWiseStoryList.forEach(s -> storyIdList.addAll(s.getStoryList()));
		// remove keys when search defects based on stories
		Map<String, List<String>> mapOfFiltersWithStoryIds = new LinkedHashMap<>();
		mapOfFiltersWithStoryIds.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));
		mapOfFiltersWithStoryIds.put(JiraFeature.DEFECT_STORY_ID.getFieldValueInFeature(), storyIdList);
		mapOfFiltersWithStoryIds.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
				Arrays.asList(NormalizedJira.DEFECT_TYPE.getValue()));

		// Fetch Defects linked with story ID's
		List<JiraIssue> totalDefectList = jiraIssueRepository.findIssuesByType(mapOfFiltersWithStoryIds);

		// Find defect with rejected status. Avoided making dB query
		if (!defectResolutionRejectionMap.isEmpty()) {
			List<JiraIssue> canceledDefectList = new ArrayList<>();
			getDefectsWithDrop(defectResolutionRejectionMap, totalDefectList, canceledDefectList);

			setDbQueryLogger(storyIdList, totalDefectList, canceledDefectList);

			resultListMap.put(REJECTED_DEFECT_DATA, canceledDefectList);
		} else {
			resultListMap.put(REJECTED_DEFECT_DATA, Lists.newArrayList());
		}

		resultListMap.put(SPRINT_WISE_STORY_DATA, sprintWiseStoryList);
		resultListMap.put(TOTAL_DEFECT_DATA, totalDefectList);
		return resultListMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Double calculateKPIMetrics(Map<String, Object> rejectedAndTotalDefectDataMap) {
		int cancelledDefectCount = ((List<Feature>) rejectedAndTotalDefectDataMap.get(REJECTED_DEFECT_DATA)).size();
		int totalDefectCount = ((List<Feature>) rejectedAndTotalDefectDataMap.get(TOTAL_DEFECT_DATA)).size();
		return (double) Math.round((100.0 * cancelledDefectCount) / (totalDefectCount));
	}

	/**
	 * This method populates KPI value to sprint leaf nodes. It also gives the trend
	 * analysis at sprint wise.
	 *
	 * @param mapTmp
	 * @param kpiElement
	 * @param sprintLeafNodeList
	 * @param trendValueList
	 */
	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();

		sortSprintLeafNodeListAsc(sprintLeafNodeList);

		String startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();
		Map<String, Object> storyDefectDataListMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate,
				kpiRequest);

		List<SprintWiseStory> sprintWiseStoryList = (List<SprintWiseStory>) storyDefectDataListMap
				.get(SPRINT_WISE_STORY_DATA);

		Map<Pair<String, String>, List<SprintWiseStory>> sprintWiseMap = sprintWiseStoryList.stream().collect(Collectors
				.groupingBy(sws -> Pair.of(sws.getBasicProjectConfigId(), sws.getSprint()), Collectors.toList()));

		Map<Pair<String, String>, Double> sprintWiseDRRMap = new HashMap<>();
		List<KPIExcelData> excelData = new ArrayList<>();
		Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseTotaldDefectListMap = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseRejectedDefectListMap = new HashMap<>();


		sprintWiseMap.forEach((sprint, sprintWiseStories) -> {

			List<JiraIssue> sprintWiseRejectedDefectList = new ArrayList<>();
			List<JiraIssue> sprintWiseTotaldDefectList = new ArrayList<>();
			List<Double> subCategoryWiseDRRList = new ArrayList<>();
			List<String> totalStoryIdList = new ArrayList<>();

			sprintWiseStories.stream().map(SprintWiseStory::getStoryList).collect(Collectors.toList())
					.forEach(totalStoryIdList::addAll);

			Map<String, Object> sprintWiseRejectedAndTotalDefects = new HashMap<>();
			List<JiraIssue> sprintWiseRejectedDefects = ((List<JiraIssue>) storyDefectDataListMap
					.get(REJECTED_DEFECT_DATA))
							.stream()
							.filter(f -> sprint.getKey().equals(f.getBasicProjectConfigId())
									&& CollectionUtils.containsAny(f.getDefectStoryID(), totalStoryIdList))
							.collect(Collectors.toList());
			List<JiraIssue> sprintWiseTotaldDefects = ((List<JiraIssue>) storyDefectDataListMap.get(TOTAL_DEFECT_DATA))
					.stream()
					.filter(f -> sprint.getKey().equals(f.getBasicProjectConfigId())
							&& CollectionUtils.containsAny(f.getDefectStoryID(), totalStoryIdList))
					.collect(Collectors.toList());
			double drrForCurrentLeaf = 0.0d;

			sprintWiseRejectedAndTotalDefects.put(REJECTED_DEFECT_DATA, sprintWiseRejectedDefects);
			sprintWiseRejectedAndTotalDefects.put(TOTAL_DEFECT_DATA, sprintWiseTotaldDefects);
			if (CollectionUtils.isNotEmpty(sprintWiseRejectedDefects)
					&& CollectionUtils.isNotEmpty(sprintWiseTotaldDefects)) {

				drrForCurrentLeaf = calculateKPIMetrics(sprintWiseRejectedAndTotalDefects);
			}
			subCategoryWiseDRRList.add(drrForCurrentLeaf);
			sprintWiseRejectedDefectList.addAll(sprintWiseRejectedDefects);
			sprintWiseTotaldDefectList.addAll(sprintWiseTotaldDefects);
			sprintWiseRejectedDefectListMap.put(sprint,sprintWiseRejectedDefectList);
			sprintWiseTotaldDefectListMap.put(sprint,sprintWiseTotaldDefectList);

			setSprintWiseLogger(sprint, totalStoryIdList, sprintWiseTotaldDefectList, sprintWiseRejectedDefectList);

			sprintWiseDRRMap.put(sprint, drrForCurrentLeaf);
			setHowerMap(sprintWiseHowerMap, sprint, sprintWiseRejectedDefectList, sprintWiseTotaldDefectList);
		});

		sprintLeafNodeList.forEach(node -> {
			// Leaf node wise data
			String trendLineName = node.getProjectFilter().getName();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), node.getSprintFilter().getId());

			double drrForCurrentLeaf;

			if (sprintWiseDRRMap.containsKey(currentNodeIdentifier)) {
				drrForCurrentLeaf = sprintWiseDRRMap.get(currentNodeIdentifier);
				List<JiraIssue> sprintWiseRejectedDefectList = sprintWiseRejectedDefectListMap.get(currentNodeIdentifier);
				List<JiraIssue> sprintWiseTotaldDefectList = sprintWiseTotaldDefectListMap.get(currentNodeIdentifier);

				populateExcelDataObject(requestTrackerId, node.getSprintFilter().getName(), excelData,
						sprintWiseRejectedDefectList, sprintWiseTotaldDefectList);

			} else {
				drrForCurrentLeaf = 0.0d;
			}

			log.debug("[DRR-SPRINT-WISE][{}]. DRR for sprint {}  is {}", requestTrackerId,
					node.getSprintFilter().getName(), drrForCurrentLeaf);

			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(Math.round(drrForCurrentLeaf)));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
			dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
			dataCount.setValue(drrForCurrentLeaf);
			dataCount.setHoverValue(sprintWiseHowerMap.get(currentNodeIdentifier));
			mapTmp.get(node.getId()).setValue(new ArrayList<DataCount>(Arrays.asList(dataCount)));
			trendValueList.add(dataCount);

		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_REJECTION_RATE.getColumns());
	}

	/**
	 * Sorts the sprint node in ascending order of sprint begin date.
	 *
	 * @param sprintLeafNodeList
	 */
	private void sortSprintLeafNodeListAsc(List<Node> sprintLeafNodeList) {
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
	}

	private void populateExcelDataObject(String requestTrackerId,String sprintName,
			List<KPIExcelData> excelData, List<JiraIssue> sprintWiseRejectedDefectList,
			List<JiraIssue> sprintWiseTotaldDefectList) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			Map<String, JiraIssue> totalDefectList = new HashMap<>();
			sprintWiseTotaldDefectList.stream().forEach(bugs -> totalDefectList.putIfAbsent(bugs.getNumber(), bugs));
			KPIExcelUtility.populateDefectRelatedExcelData(sprintName, totalDefectList, sprintWiseRejectedDefectList,
					excelData, KPICode.DEFECT_REJECTION_RATE.getKpiId());
		}
	}

	/**
	 * Sets logger for data fetched from DB.
	 *
	 * @param storyIdList
	 * @param totalDefectList
	 * @param canceledDefectList
	 */
	private void setDbQueryLogger(List<String> storyIdList, List<JiraIssue> totalDefectList,
			List<JiraIssue> canceledDefectList) {

		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.info(SEPARATOR_ASTERISK);
			log.info("************* DRR (dB) *******************");
			log.info("Story[{}]: {}", storyIdList.size(), storyIdList);
			log.info("TotalDefectList LinkedWith -> story[{}]: {}", totalDefectList.size(),
					totalDefectList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			log.info("CanceledDefectList [{}]: {}", canceledDefectList.size(),
					canceledDefectList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			log.info(SEPARATOR_ASTERISK);
			log.info("******************X----X*******************");
		}
	}

	/**
	 * Sets logger for sprint level data.
	 *
	 * @param sprint
	 * @param storyIdList
	 * @param sprintWiseTotaldDefectList
	 * @param sprintWiseRejectedDefectList
	 */
	private void setSprintWiseLogger(Pair<String, String> sprint, List<String> storyIdList,
			List<JiraIssue> sprintWiseTotaldDefectList, List<JiraIssue> sprintWiseRejectedDefectList) {

		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.debug(SEPARATOR_ASTERISK);
			log.debug("************* SPRINT WISE DRR *******************");
			log.debug("Sprint: {}", sprint.getValue());
			log.debug("Story[{}]: {}", storyIdList.size(), storyIdList);
			log.debug("SprintWiseTotaldDefectList[{}]: {}", sprintWiseTotaldDefectList.size(),
					sprintWiseTotaldDefectList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			log.debug("SprintWiseRejectedDefectList[{}]: {}", sprintWiseRejectedDefectList.size(),
					sprintWiseRejectedDefectList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			log.debug(SEPARATOR_ASTERISK);
			log.debug(SEPARATOR_ASTERISK);
		}
	}

	/**
	 * Sets map to show on hover of sprint node.
	 *
	 * @param sprintWiseHowerMap
	 * @param sprint
	 * @param rejected
	 * @param total
	 */
	private void setHowerMap(Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap,
			Pair<String, String> sprint, List<JiraIssue> rejected, List<JiraIssue> total) {
		Map<String, Object> howerMap = new LinkedHashMap<>();
		if (CollectionUtils.isNotEmpty(rejected)) {
			howerMap.put(REJECTED, rejected.size());
		} else {
			howerMap.put(REJECTED, 0);
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

	public static void getDefectsWithDrop(Map<String, Map<String,List<String>>> droppedDefects, List<JiraIssue> defectDataList,
											 List<JiraIssue> defectListWthDrop) {
		if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(defectDataList)) {
			Set<JiraIssue> defectListWthDropSet = new HashSet<>();
			defectDataList.forEach(jiraIssue -> {
				getDefectsWthDrop(droppedDefects, defectListWthDropSet, jiraIssue);
			});
			defectListWthDrop.addAll(defectListWthDropSet);
		}
	}

	private static void getDefectsWthDrop(Map<String, Map<String,List<String>>> droppedDefects, Set<JiraIssue> defectListWthDropSet, JiraIssue jiraIssue) {
		if (!StringUtils.isBlank(jiraIssue.getStatus())) {
			Map<String,List<String>> defectStatus = droppedDefects.get(jiraIssue.getBasicProjectConfigId());
			if (!defectStatus.isEmpty()) {
				if (StringUtils.isNotEmpty(jiraIssue.getResolution()) &&
						CollectionUtils.isNotEmpty(defectStatus.get(Constant.RESOLUTION_TYPE_FOR_REJECTION)) &&
						defectStatus.get(Constant.RESOLUTION_TYPE_FOR_REJECTION).contains(jiraIssue.getResolution())) {
					defectListWthDropSet.add(jiraIssue);
				} else if (StringUtils.isNotEmpty(jiraIssue.getStatus()) &&
						CollectionUtils.isNotEmpty(defectStatus.get(Constant.DEFECT_REJECTION_STATUS)) &&
						defectStatus.get(Constant.DEFECT_REJECTION_STATUS).contains(jiraIssue.getStatus())) {
					defectListWthDropSet.add(jiraIssue);
				}
			} else {
				defectListWthDropSet.add(jiraIssue);
			}
		}
	}

}
