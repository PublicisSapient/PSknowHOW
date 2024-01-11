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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;

import lombok.extern.slf4j.Slf4j;

/**
 * This class calculates the QA Defect Density KPI and trend analysis of the
 * same.
 *
 * @author prasaxen3
 */
@Component
@Slf4j
public class QADDServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String STORY_DATA = "storyData";

	private static final String DEFECT_DATA = "defectData";

	private static final String STORY_POINTS_DATA = "Size of Closed Stories";

	private static final String STORY_POINTS = "storyPoints";

	private static final String DEFECT = "Defects";

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public String getQualifierType() {
		return KPICode.DEFECT_DENSITY.name();
	}

	/**
	 * Gets the kpi data.
	 *
	 * @param kpiRequest
	 *            the kpi request
	 * @param kpiElement
	 *            the kpi element
	 * @param treeAggregatorDetail
	 *            the tree aggregator detail
	 * @return the kpi data
	 * @throws ApplicationException
	 *             the application exception
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

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.DEFECT_DENSITY);
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue, KPICode.DEFECT_DENSITY);
		kpiElement.setTrendValueList(trendValues);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);

		return kpiElement;
	}

	/**
	 * This method populates KPI value to sprint leaf nodes. It also gives the trend
	 * analysis at sprint wise.
	 *
	 * @param mapTmp
	 *            node is map
	 * @param sprintLeafNodeList
	 *            sprint nodes list
	 * @param trendValueList
	 *            list to hold trend data
	 * @param kpiElement
	 *            KpiElement
	 * @param kpiRequest
	 *            KpiRequest
	 */
	@SuppressWarnings("unchecked")
	public void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		String startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();

		FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
				.get(sprintLeafNodeList.get(0).getProjectFilter().getBasicProjectConfigId());
		long time = System.currentTimeMillis();
		Map<String, Object> storyDefectDataListMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate,
				kpiRequest);
		log.info("QADD taking fetchKPIDataFromDb {}", String.valueOf(System.currentTimeMillis() - time));

		List<SprintWiseStory> sprintWiseStoryList = (List<SprintWiseStory>) storyDefectDataListMap.get(STORY_DATA);
		List<JiraIssue> storyFilteredList = (List<JiraIssue>) storyDefectDataListMap.get(STORY_POINTS);

		Map<Pair<String, String>, List<SprintWiseStory>> sprintWiseMap = sprintWiseStoryList.stream().collect(Collectors
				.groupingBy(sws -> Pair.of(sws.getBasicProjectConfigId(), sws.getSprint()), Collectors.toList()));

		Map<Pair<String, String>, Double> sprintWiseQADDMap = new HashMap<>();
		List<KPIExcelData> excelData = new ArrayList<>();
		Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap = new HashMap<>();

		Map<Pair<String, String>, List<String>> sprintWiseStoryMAP = new HashMap<>();
		Map<Pair<String, String>, Set<JiraIssue>> sprintWiseDefectListMap = new HashMap<>();
		processHowerMap(sprintWiseMap, storyDefectDataListMap, sprintWiseQADDMap, sprintWiseHowerMap, storyFilteredList,
				sprintWiseStoryMAP, sprintWiseDefectListMap, fieldMapping);

		Map<String, JiraIssue> allStoryMap = new HashMap<>();
		storyFilteredList.stream().forEach(story -> allStoryMap.putIfAbsent(story.getNumber(), story));

		sprintLeafNodeList.forEach(node -> {

			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), currentSprintComponentId);

			double qaddForCurrentLeaf;

			if (sprintWiseQADDMap.containsKey(currentNodeIdentifier)) {
				qaddForCurrentLeaf = sprintWiseQADDMap.get(currentNodeIdentifier);
				if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
					List<String> totalStoryIdList = sprintWiseStoryMAP.get(currentNodeIdentifier);
					Set<JiraIssue> sprintWiseDefectList = sprintWiseDefectListMap.get(currentNodeIdentifier);
					KPIExcelUtility.populateDefectDensityExcelData(node.getSprintFilter().getName(), totalStoryIdList,
							new ArrayList<>(sprintWiseDefectList), excelData, allStoryMap, fieldMapping);
				}
			} else {
				qaddForCurrentLeaf = 0.0d;
			}
			// aggregated value to exclude the sprint with sum of story points
			// is zero
			if (qaddForCurrentLeaf == -1000.0) {
				qaddForCurrentLeaf = 0.0d;
			}
			log.debug("[QADD-SPRINT-WISE][{}]. QADD for sprint {}  is {}", requestTrackerId,
					node.getSprintFilter().getName(), qaddForCurrentLeaf);

			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(Math.round(qaddForCurrentLeaf)));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
			dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
			dataCount.setValue(qaddForCurrentLeaf);
			dataCount.setHoverValue(sprintWiseHowerMap.get(currentNodeIdentifier));
			mapTmp.get(node.getId()).setValue(new ArrayList<DataCount>(Arrays.asList(dataCount)));

			trendValueList.add(dataCount);
		});

		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_DENSITY.getColumns());

	}

	/**
	 * Sets the hower map.
	 *
	 * @param sprintWiseHowerMap
	 *            the sprint wise hower map
	 * @param sprint
	 *            the sprint
	 * @param storyList
	 *            the story list
	 * @param sprintWiseDefectList
	 *            the sprint wise defect list
	 */
	private void setHowerMap(Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap,
			Pair<String, String> sprint, List<JiraIssue> storyList, Set<JiraIssue> sprintWiseDefectList,
			FieldMapping fieldMapping) {
		Map<String, Object> howerMap = new LinkedHashMap<>();
		if (CollectionUtils.isNotEmpty(sprintWiseDefectList)) {
			howerMap.put(DEFECT, sprintWiseDefectList.size());
		} else {
			howerMap.put(DEFECT, 0);
		}
		if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			howerMap.put(STORY_POINTS_DATA, storyList.stream().mapToDouble(JiraIssue::getStoryPoints).sum());
		} else {
			double totalOriginalEstimate = storyList.stream()
					.filter(jiraIssue -> Objects.nonNull(jiraIssue.getAggregateTimeOriginalEstimateMinutes()))
					.mapToDouble(JiraIssue::getAggregateTimeOriginalEstimateMinutes).sum();
			double totalOriginalEstimateInHours = totalOriginalEstimate / 60;
			double storyPointsData = Double.parseDouble(
					String.format("%.2f", totalOriginalEstimateInHours / fieldMapping.getStoryPointToHourMapping()));
			howerMap.put(STORY_POINTS_DATA, storyPointsData);
		}
		sprintWiseHowerMap.put(sprint, howerMap);
	}

	/**
	 * Process hower map and sets sprintwise KPI value map.
	 * 
	 * @param sprintWiseMap
	 * @param storyDefectDataListMap
	 * @param sprintWiseQADDMap
	 * @param sprintWiseHowerMap
	 * @param storyFilteredList
	 * @param sprintWiseStoryMAP
	 * @param sprintWiseDefectListMap
	 */
	private void processHowerMap(Map<Pair<String, String>, List<SprintWiseStory>> sprintWiseMap, // NOPMD//NOSONAR
			Map<String, Object> storyDefectDataListMap, Map<Pair<String, String>, Double> sprintWiseQADDMap, // NOSONAR
			Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap, List<JiraIssue> storyFilteredList,
			Map<Pair<String, String>, List<String>> sprintWiseStoryMAP,
			Map<Pair<String, String>, Set<JiraIssue>> sprintWiseDefectListMap, FieldMapping fieldMapping) {// NOSONAR
		sprintWiseMap.forEach((sprint, sprintWiseStories) -> {
			Set<JiraIssue> sprintWiseDefectList = new HashSet<>();
			List<Double> qaddList = new ArrayList<>();
			List<String> totalStoryIdList = new ArrayList<>();
			List<JiraIssue> storyList = new ArrayList<>();
			List<String> storyPointList = new ArrayList<>();
			List<String> storyIds = new ArrayList<>();
			sprintWiseStories.stream().map(SprintWiseStory::getStoryList).collect(Collectors.toList())
					.forEach(storyIds::addAll);
			processSubCategoryMap(storyIds, storyDefectDataListMap, qaddList, sprintWiseDefectList, totalStoryIdList,
					storyList, storyFilteredList, storyPointList, fieldMapping);

			double sprintWiseQADD = calculateKpiValue(qaddList, KPICode.DEFECT_DENSITY.getKpiId());
			sprintWiseQADDMap.put(sprint, sprintWiseQADD);
			sprintWiseStoryMAP.put(sprint, totalStoryIdList);
			sprintWiseDefectListMap.put(sprint, sprintWiseDefectList);

			setHowerMap(sprintWiseHowerMap, sprint, storyList, sprintWiseDefectList, fieldMapping);
		});
	}

	/**
	 * Process sub category map and evaluates the KPI value.
	 *
	 * @param storyIdList
	 *            the story id list
	 * @param storyDefectDataListMap
	 *            the story defect data list map
	 * @param qaddList
	 *            the qadd list
	 * @param sprintWiseDefectList
	 *            the sprint wise defect list
	 * @param totalStoryIdList
	 *            the total story id list
	 * @param storyList
	 *            the story list
	 * @param storyFilteredList
	 *            the story filtered list
	 * @param storyPointList2
	 *            the story point list 2
	 */
	private void processSubCategoryMap(List<String> storyIdList, Map<String, Object> storyDefectDataListMap, // NOSONAR
																											 // //NOSONAR
			List<Double> qaddList, Set<JiraIssue> sprintWiseDefectList, List<String> totalStoryIdList,
			List<JiraIssue> storyList, List<JiraIssue> storyFilteredList, List<String> storyPointList2,
			FieldMapping fieldMapping) {// NOSONAR
		HashMap<String, JiraIssue> mapOfStories = new HashMap<>();
		for (JiraIssue f : storyFilteredList) {
			mapOfStories.put(f.getNumber(), f);
		}

		@SuppressWarnings("unchecked")
		Set<JiraIssue> additionalFilterDefectList = ((List<JiraIssue>) storyDefectDataListMap.get(DEFECT_DATA)).stream()
				.filter(f -> CollectionUtils.containsAny(f.getDefectStoryID(),
						storyIdList == null ? Collections.emptyList() : storyIdList))
				.collect(Collectors.toSet());
		populateList(additionalFilterDefectList, mapOfStories);
		@SuppressWarnings("unchecked")
		List<JiraIssue> storyPointList = ((List<JiraIssue>) storyDefectDataListMap.get(STORY_POINTS)).stream()
				.filter(f -> CollectionUtils.isNotEmpty(storyIdList) && storyIdList.contains(f.getNumber()))
				.collect(Collectors.toList());
		storyList.addAll(storyPointList);
		for (JiraIssue f : storyPointList) {
			storyPointList2.add(String.valueOf(f.getStoryPoints()));
		}
		double qaddForCurrentLeaf = 0.0d;
		double storyPointsTotal;
		if (CollectionUtils.isNotEmpty(storyList)) {
			if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
					&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
				storyPointsTotal = storyList.stream().mapToDouble(JiraIssue::getStoryPoints).sum();// NOPMD
			} else {
				storyPointsTotal = storyList.stream()
						.filter(jiraIssue -> Objects.nonNull(jiraIssue.getAggregateTimeOriginalEstimateMinutes()))
						.mapToDouble(JiraIssue::getAggregateTimeOriginalEstimateMinutes).sum();
				storyPointsTotal = storyPointsTotal / 60;
				storyPointsTotal = storyPointsTotal / fieldMapping.getStoryPointToHourMapping();
			}
			if (storyPointsTotal == 0.0d) {// NOPMD
				qaddForCurrentLeaf = -1000.0;
			} else if (CollectionUtils.isNotEmpty(additionalFilterDefectList)) {
				qaddForCurrentLeaf = (additionalFilterDefectList.size() / storyPointsTotal) * 100;
			}
		} else {
			qaddForCurrentLeaf = -1000.0;
		}
		qaddList.add(qaddForCurrentLeaf);
		sprintWiseDefectList.addAll(additionalFilterDefectList);
		totalStoryIdList.addAll(storyIdList == null ? Collections.emptyList() : storyIdList);
	}

	private void populateList(Set<JiraIssue> additionalFilterDefectList, HashMap<String, JiraIssue> mapOfStories) {
		if (!additionalFilterDefectList.isEmpty()) {
			JiraIssue jiraIssue = additionalFilterDefectList.stream().findFirst().orElse(new JiraIssue());
			// Filter for QA tagged defects
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(new ObjectId(jiraIssue.getBasicProjectConfigId()));

			if (null != fieldMapping && CollectionUtils.isNotEmpty(fieldMapping.getJiraBugRaisedByQAValue())) {
				additionalFilterDefectList = additionalFilterDefectList.stream().filter(f -> f.isDefectRaisedByQA()) // NOSONAR
						.collect(Collectors.toSet());
			} else if (null != fieldMapping && CollectionUtils.isNotEmpty(fieldMapping.getJiraBugRaisedByValue())) {
				additionalFilterDefectList = additionalFilterDefectList.stream()
						.filter(f -> f.getDefectRaisedBy() != null && !(f.getDefectRaisedBy().equalsIgnoreCase("UAT")))
						.collect(Collectors.toSet());
			}

			// Filter for defects NOT linked to stories in a given sprint
			additionalFilterDefectList.addAll(additionalFilterDefectList.stream()
					.filter(f -> (!f.getDefectStoryID().isEmpty()
							&& mapOfStories.containsKey(f.getDefectStoryID().iterator().next())))
					.collect(Collectors.toList()));
		}

	}

	/**
	 * Fetch filtered KPI data from database.
	 *
	 * @param leafNodeList
	 *            the leaf node list
	 * @param startDate
	 *            the start date
	 * @param endDate
	 *            the end date
	 * @param kpiRequest
	 *            the kpi request
	 * @return the map
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		long startTime = System.currentTimeMillis();

		Map<String, Object> resultListMap = kpiHelperService.fetchQADDFromDb(leafNodeList, kpiRequest);

		if (log.isDebugEnabled()) {
			List<SprintWiseStory> storyDataList = (List<SprintWiseStory>) resultListMap.get(STORY_DATA);
			List<JiraIssue> defectDataList = (List<JiraIssue>) resultListMap.get(DEFECT_DATA);// NOPMD
			log.info("[QADD-DB-QUERY][]. storyData count: {} defectData count: {}  time: {}", storyDataList.size(), // NOPMD
					defectDataList.size(), System.currentTimeMillis() - startTime);
		}

		return resultListMap;
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> objectMap) {
		return null;
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiId) {
		return calculateKpiValueForDouble(valueList, kpiId);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping){
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI111(),KPICode.DEFECT_DENSITY.getKpiId());
	}

}
