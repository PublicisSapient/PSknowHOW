/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */
@Slf4j
@Component
public class FirstTimePassRateServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String FIRST_TIME_PASS_STORIES = "ftpStories";
	private static final String SPRINT_WISE_CLOSED_STORIES = "sprintWiseClosedStories";
	private static final String HOVER_KEY_CLOSED_STORIES = "Closed Stories";
	private static final String HOVER_KEY_FTP_STORIES = "FTP Stories";
	private static final String ISSUE_DATA = "Issue Data";
	public static final String DEFECT_FOR_EXCEL = "defect for Excel";

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private FilterHelperService flterHelperService;

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private CacheService cacheService;
	@Autowired
	private KpiDataCacheService kpiDataCacheService;
	@Autowired
	private KpiDataProvider kpiDataProvider;
	private List<String> sprintIdList = Collections.synchronizedList(new ArrayList<>());

	@Override
	public String getQualifierType() {
		return KPICode.FIRST_TIME_PASS_RATE.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, TreeAggregatorDetail treeAggregatorDetail)
			throws ApplicationException {
		List<DataCount> trendValueList = new ArrayList<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		sprintIdList = treeAggregatorDetail.getMapOfListOfLeafNodes().get(CommonConstant.SPRINT_MASTER).stream()
				.map(node -> node.getSprintFilter().getId()).collect(Collectors.toList());
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			Filters filters = Filters.getFilter(k);
			if (Filters.SPRINT == filters) {
				sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);
			}
		});

		log.debug("[FTPR-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.FIRST_TIME_PASS_RATE);
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.FIRST_TIME_PASS_RATE);
		kpiElement.setTrendValueList(trendValues);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);
		log.debug("[STORYCOUNT-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();
		sprintLeafNodeList.sort(
				(node1, node2) -> node1.getSprintFilter().getStartDate().compareTo(node2.getSprintFilter().getStartDate()));
		String startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();
		long time = System.currentTimeMillis();
		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate, kpiRequest);
		log.info("FirstTimePassRate taking fetchKPIDataFromDb {}", String.valueOf(System.currentTimeMillis() - time));
		List<SprintWiseStory> sprintWiseStoryList = (List<SprintWiseStory>) resultMap.get(SPRINT_WISE_CLOSED_STORIES);
		Map<Pair<String, String>, List<SprintWiseStory>> sprintWiseMap = sprintWiseStoryList.stream().collect(
				Collectors.groupingBy(sws -> Pair.of(sws.getBasicProjectConfigId(), sws.getSprint()), Collectors.toList()));
		List<JiraIssue> jiraIssueList = (List<JiraIssue>) resultMap.get(ISSUE_DATA);
		Set<JiraIssue> defectListsForExcel = (Set<JiraIssue>) resultMap.get(DEFECT_FOR_EXCEL);
		Map<String, Set<JiraIssue>> projectWiseStories = jiraIssueList.stream()
				.collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId, Collectors.toSet()));
		Map<String, Set<JiraIssue>> projectWiseDefect = defectListsForExcel.stream()
				.collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId, Collectors.toSet()));
		Map<Pair<String, String>, Double> sprintWiseFTPRMap = new HashMap<>();
		Map<Pair<String, String>, List<String>> sprintWiseTotalStoryIdList = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseFTPListMap = new HashMap<>();

		Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap = new HashMap<>();
		List<KPIExcelData> excelData = new ArrayList<>();
		sprintWiseMap.forEach((sprint, sprintWiseStories) -> {
			List<Double> addFilterFtprList = new ArrayList<>();
			List<String> totalStoryIdList = new ArrayList<>();
			sprintWiseStories.stream().map(SprintWiseStory::getStoryList).collect(Collectors.toList())
					.forEach(totalStoryIdList::addAll);
			sprintWiseTotalStoryIdList.put(sprint, totalStoryIdList);

			List<JiraIssue> ftpStoriesList = ((List<JiraIssue>) resultMap.get(FIRST_TIME_PASS_STORIES)).stream()
					.filter(jiraIssue -> jiraIssue.getSprintID().equals(sprint.getValue())).collect(Collectors.toList());
			sprintWiseFTPListMap.put(sprint, ftpStoriesList);

			double ftprForCurrentLeaf = 0.0d;
			if (CollectionUtils.isNotEmpty(ftpStoriesList) && CollectionUtils.isNotEmpty(totalStoryIdList)) {
				ftprForCurrentLeaf = ((double) ftpStoriesList.size() / totalStoryIdList.size()) * 100;
			}
			addFilterFtprList.add(ftprForCurrentLeaf);

			double sprintWiseFtpr = calculateKpiValue(addFilterFtprList, KPICode.FIRST_TIME_PASS_RATE.getKpiId());
			sprintWiseFTPRMap.put(sprint, sprintWiseFtpr);
			setHowerMap(sprintWiseHowerMap, sprint, totalStoryIdList, ftpStoriesList);
		});
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
				.get(sprintLeafNodeList.get(0).getProjectFilter().getBasicProjectConfigId());
		sprintLeafNodeList.forEach(node -> {
			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair.of(node.getProjectFilter().getBasicProjectConfigId().toString(),
					currentSprintComponentId);
			double ftprForCurrentLeaf;

			if (sprintWiseFTPRMap.containsKey(currentNodeIdentifier)) {
				ftprForCurrentLeaf = sprintWiseFTPRMap.get(currentNodeIdentifier);
				// if for populating excel data
				if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
					List<String> totalStoryIdList = sprintWiseTotalStoryIdList.get(currentNodeIdentifier);
					List<JiraIssue> ftpStoriesList = sprintWiseFTPListMap.get(currentNodeIdentifier);
					Set<JiraIssue> jiraIssues = projectWiseStories
							.get(node.getProjectFilter().getBasicProjectConfigId().toString());
					Set<JiraIssue> linkedDefects = projectWiseDefect
							.get(node.getProjectFilter().getBasicProjectConfigId().toString());
					Map<String, JiraIssue> issueMapping = new HashMap<>();
					Optional.ofNullable(jiraIssues)
							.ifPresent(jIssue -> jIssue.forEach(issue -> issueMapping.putIfAbsent(issue.getNumber(), issue)));
					List<KPIExcelData> excelDatas = new ArrayList<>();
					KPIExcelUtility.populateFTPRExcelData(totalStoryIdList, ftpStoriesList, excelDatas, issueMapping,
							new ArrayList<>(linkedDefects != null ? linkedDefects : new ArrayList<>()), customApiConfig, fieldMapping,
							node);
					excelData.addAll(excelDatas);
				}
			} else {
				ftprForCurrentLeaf = 0.0d;
			}

			log.debug("[FTPR-SPRINT-WISE][{}]. FTPR for sprint {}  is {}", requestTrackerId, node.getSprintFilter().getName(),
					ftprForCurrentLeaf);

			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(Math.round(ftprForCurrentLeaf)));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
			dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
			dataCount.setValue(ftprForCurrentLeaf);
			dataCount.setHoverValue(sprintWiseHowerMap.get(currentNodeIdentifier));
			mapTmp.get(node.getId()).setValue(new ArrayList<DataCount>(Arrays.asList(dataCount)));

			trendValueList.add(dataCount);
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(
				KPIExcelColumn.FIRST_TIME_PASS_RATE.getColumns(sprintLeafNodeList, cacheService, flterHelperService));
	}

	private void setHowerMap(Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap,
			Pair<String, String> sprint, List<String> storyIdList, List<JiraIssue> sprintWiseFtpStories) {
		Map<String, Object> howerMap = new LinkedHashMap<>();
		if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(sprintWiseFtpStories)) {
			howerMap.put(HOVER_KEY_FTP_STORIES, sprintWiseFtpStories.size());
		} else {
			howerMap.put(HOVER_KEY_FTP_STORIES, 0);
		}
		if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(storyIdList)) {
			howerMap.put(HOVER_KEY_CLOSED_STORIES, storyIdList.size());
		} else {
			howerMap.put(HOVER_KEY_CLOSED_STORIES, 0);
		}
		sprintWiseHowerMap.put(sprint, howerMap);
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return 0.0D;
	}

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

		List<SprintWiseStory> sprintWiseStories = new ArrayList<>();
		List<JiraIssue> defectListWoDrop = new ArrayList<>();
		List<JiraIssue> jiraIssueList = new ArrayList<>();
		Set<JiraIssue> defectListsForExcel = new HashSet<>();
		boolean fetchCachedData = flterHelperService.isFilterSelectedTillSprintLevel(kpiRequest.getLevel(), false);
		projectWiseSprints.forEach((basicProjectConfigId, sprintList) -> {
			Map<String, Object> result;
			if (fetchCachedData) { // fetch data from cache only if Filter is selected till Sprint
				// level.
				result = kpiDataCacheService.fetchFirstTimePassRateData(kpiRequest, basicProjectConfigId, sprintIdList,
						KPICode.FIRST_TIME_PASS_RATE.getKpiId());
			} else { // fetch data from DB if filters below Sprint level (i.e. additional filters)
				result = kpiDataProvider.fetchFirstTimePassRateDataFromDb(kpiRequest, basicProjectConfigId, sprintList);
			}

			sprintWiseStories.addAll((List<SprintWiseStory>) result.get(SPRINT_WISE_CLOSED_STORIES));
			defectListWoDrop.addAll((List<JiraIssue>) result.get(FIRST_TIME_PASS_STORIES));
			jiraIssueList.addAll((List<JiraIssue>) result.get(ISSUE_DATA));
			defectListsForExcel.addAll((Set<JiraIssue>) result.get(DEFECT_FOR_EXCEL));
		});

		resultListMap.put(SPRINT_WISE_CLOSED_STORIES, sprintWiseStories);
		resultListMap.put(FIRST_TIME_PASS_STORIES, defectListWoDrop);
		resultListMap.put(ISSUE_DATA, jiraIssueList);
		resultListMap.put(DEFECT_FOR_EXCEL, defectListsForExcel);
		return resultListMap;
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiId) {
		return calculateKpiValueForDouble(valueList, kpiId);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI82(), KPICode.FIRST_TIME_PASS_RATE.getKpiId());
	}
}
