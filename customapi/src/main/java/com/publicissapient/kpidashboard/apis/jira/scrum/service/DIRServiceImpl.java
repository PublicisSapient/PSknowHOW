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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
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
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;

import lombok.extern.slf4j.Slf4j;

/**
 * This class calculates the DIR and trend analysis of the DIR.
 * 
 * @author pkum34
 *
 */
@Component
@Slf4j
public class DIRServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {
	private static final String STORY_DATA = "storyData";
	private static final String DEFECT_DATA = "defectData";
	private static final String STORY = "Stories";
	private static final String DEFECT = "Defects";
	private static final String ISSUE_DATA = "issueData";

	@Autowired
	private KpiHelperService kpiHelperService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getQualifierType() {
		return KPICode.DEFECT_INJECTION_RATE.name();
	}

	/**
	 * {@inheritDoc}
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

		log.debug("[DIR-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.DEFECT_INJECTION_RATE);
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue, KPICode.DEFECT_INJECTION_RATE);
		kpiElement.setTrendValueList(trendValues);

		return kpiElement;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		long startTime = System.currentTimeMillis();

		Map<String, Object> resultListMap = kpiHelperService.fetchDIRDataFromDb(leafNodeList, kpiRequest);

		if (log.isDebugEnabled()) {
			List<SprintWiseStory> storyDataList = (List<SprintWiseStory>) resultListMap.get(STORY_DATA);
			List<JiraIssue> defectDataList = (List<JiraIssue>) resultListMap.get(DEFECT_DATA);
			long processTime = System.currentTimeMillis() - startTime;
			log.info("[DIR-DB-QUERY][]. storyData count: {} defectData count: {}  time: {}", storyDataList.size(),
					defectDataList.size(), processTime);
		}

		return resultListMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double calculateKPIMetrics(Map<String, Object> filterComponentIdWiseFCHMap) {
		return null;
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
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		String startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();
		long jiraTime = System.currentTimeMillis();

		Map<String, Object> storyDefectDataListMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate,
				kpiRequest);
		log.info("DIR taking fetchKPIDataFromDb:{}", System.currentTimeMillis() - jiraTime);
		List<SprintWiseStory> sprintWiseStoryList = (List<SprintWiseStory>) storyDefectDataListMap.get(STORY_DATA);
		List<JiraIssue> jiraIssueList = (List<JiraIssue>) storyDefectDataListMap.get(ISSUE_DATA);
		Map<String, Set<JiraIssue>> projectWiseStories = jiraIssueList.stream()
				.collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId, Collectors.toSet()));

		Map<Pair<String, String>, List<SprintWiseStory>> sprintWiseMap = sprintWiseStoryList.stream().collect(Collectors
				.groupingBy(sws -> Pair.of(sws.getBasicProjectConfigId(), sws.getSprint()), Collectors.toList()));

		Map<Pair<String, String>, Double> sprintWiseDIRMap = new HashMap<>();

		Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap = new HashMap<>();
		Map<Pair<String, String>, List<String>> sprintWiseTotalStoryIdList = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseDefectListMap = new HashMap<>();

		sprintWiseMap.forEach((sprint, sprintWiseStories) -> {
			List<JiraIssue> sprintWiseDefectList = new ArrayList<>();

			List<String> totalStoryIdList = new ArrayList<>();
			sprintWiseStories.stream().map(SprintWiseStory::getStoryList).collect(Collectors.toList())
					.forEach(totalStoryIdList::addAll);
			sprintWiseTotalStoryIdList.put(sprint, totalStoryIdList);

			List<JiraIssue> defectList = ((List<JiraIssue>) storyDefectDataListMap.get(DEFECT_DATA)).stream()
					.filter(f -> sprint.getKey().equals(f.getBasicProjectConfigId())
							&& CollectionUtils.containsAny(f.getDefectStoryID(), totalStoryIdList))
					.collect(Collectors.toList());
			sprintWiseDefectListMap.put(sprint, defectList);

			double dirForCurrentLeaf = 0.0d;
			if (CollectionUtils.isNotEmpty(defectList) && CollectionUtils.isNotEmpty(sprintWiseStories)) {
				dirForCurrentLeaf = ((double) defectList.size() / totalStoryIdList.size()) * 100;
			}
			sprintWiseDefectList.addAll(defectList);
			sprintWiseDIRMap.put(sprint, dirForCurrentLeaf);
			setHowerMap(sprintWiseHowerMap, sprint, totalStoryIdList, sprintWiseDefectList);
		});
		List<KPIExcelData> excelData = new ArrayList<>();
		sprintLeafNodeList.forEach(node -> {
			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), currentSprintComponentId);

			double defectInjectionRateForCurrentLeaf;

			if (sprintWiseDIRMap.containsKey(currentNodeIdentifier)) {
				defectInjectionRateForCurrentLeaf = sprintWiseDIRMap.get(currentNodeIdentifier);
				// if for populating excel data
				if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
					List<String> totalStoryIdList = sprintWiseTotalStoryIdList.get(currentNodeIdentifier);
					List<JiraIssue> defectList = sprintWiseDefectListMap.get(currentNodeIdentifier);
					Set<JiraIssue> jiraIssues = projectWiseStories
							.get(node.getProjectFilter().getBasicProjectConfigId().toString());
					Map<String, JiraIssue> issueMapping = new HashMap<>();
					jiraIssues.stream().forEach(issue -> issueMapping.putIfAbsent(issue.getNumber(), issue));
					KPIExcelUtility.populateDirExcelData(node.getSprintFilter().getName(), totalStoryIdList, defectList,
							excelData, issueMapping);
				}
			} else {
				defectInjectionRateForCurrentLeaf = 0.0d;
			}

			log.debug("[DIR-SPRINT-WISE][{}]. DIR for sprint {}  is {}", requestTrackerId,
					node.getSprintFilter().getName(), defectInjectionRateForCurrentLeaf);

			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(Math.round(defectInjectionRateForCurrentLeaf)));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
			dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
			dataCount.setValue(defectInjectionRateForCurrentLeaf);
			dataCount.setHoverValue(sprintWiseHowerMap.get(currentNodeIdentifier));
			mapTmp.get(node.getId()).setValue(new ArrayList<>(Arrays.asList(dataCount)));

			trendValueList.add(dataCount);
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_INJECTION_RATE.getColumns());
	}

	/**
	 * This method sets the defect and story count for each leaf node to show data
	 * on trend line on mouse hover.
	 * 
	 * @param sprintWiseHowerMap
	 *            map of sprint key and hover value
	 * @param sprint
	 *            key to identify sprint
	 * @param storyIdList
	 *            story id list
	 * @param sprintWiseDefectList
	 *            defects linked to story
	 */
	private void setHowerMap(Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap,
			Pair<String, String> sprint, List<String> storyIdList, List<JiraIssue> sprintWiseDefectList) {
		Map<String, Object> howerMap = new LinkedHashMap<>();
		if (CollectionUtils.isNotEmpty(sprintWiseDefectList)) {
			howerMap.put(DEFECT, sprintWiseDefectList.size());
		} else {
			howerMap.put(DEFECT, 0);
		}
		if (CollectionUtils.isNotEmpty(storyIdList)) {
			howerMap.put(STORY, storyIdList.size());
		} else {
			howerMap.put(STORY, 0);
		}
		sprintWiseHowerMap.put(sprint, howerMap);
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping){
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI14(),KPICode.DEFECT_INJECTION_RATE.getKpiId());
	}
}
