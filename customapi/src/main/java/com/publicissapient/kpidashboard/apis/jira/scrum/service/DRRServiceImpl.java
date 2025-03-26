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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.Feature;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

import lombok.extern.slf4j.Slf4j;

/**
 * This class calculates the DRR and trend analysis of the DRR.
 *
 * @author pkum34
 */
@Component
@Slf4j
public class DRRServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String SEPARATOR_ASTERISK = "*************************************";
	private static final String REJECTED_DEFECT_DATA = "rejectedBugKey";
	private static final String CLOSED_DEFECT_DATA = "closedDefects";
	private static final String REJECTED = "Rejected Defects";
	private static final String COMPLETED = "Completed Defects";
	public static final String SPRINT_WISE_SPRINT_DETAILS = "sprintWiseSprintDetails";
	public static final String TOTAL_DEFECT_LIST = "totalDefectList";
	public static final String STORY_LIST = "storyList";

	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private FilterHelperService filterHelperService;
	@Autowired
	private CacheService cacheService;
	@Autowired
	private KpiDataCacheService kpiDataCacheService;
	@Autowired
	private KpiDataProvider kpiDataProvider;

	private static final String TOTAL_SPRINT_SUBTASK_DEFECTS = "totalSprintSubtaskDefects";
	private static final String SUB_TASK_BUGS_HISTORY = "SubTaskBugsHistory";
	private List<String> sprintIdList = Collections.synchronizedList(new ArrayList<>());

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
		sprintIdList = treeAggregatorDetail.getMapOfListOfLeafNodes().get(CommonConstant.SPRINT_MASTER).stream()
				.map(node -> node.getSprintFilter().getId()).collect(Collectors.toList());
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);
			}
		});

		log.debug("[DRR-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.DEFECT_REJECTION_RATE);
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.DEFECT_REJECTION_RATE);
		kpiElement.setTrendValueList(trendValues);

		log.debug("[DRR-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Map<ObjectId, List<String>> projectWiseSprints = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			projectWiseSprints.putIfAbsent(basicProjectConfigId, new ArrayList<>());
			projectWiseSprints.get(basicProjectConfigId).add(leaf.getSprintFilter().getId());
		});

		List<JiraIssue> totalIssue = new ArrayList<>();
		List<JiraIssue> totalSubTaskDefects = new ArrayList<>();
		List<JiraIssueCustomHistory> subTaskBugsCustomHistory = new ArrayList<>();
		List<SprintDetails> sprintDetails = new ArrayList<>();
		List<JiraIssue> canceledDefectList = new ArrayList<>();
		List<JiraIssue> totalDefectList = new ArrayList<>();
		boolean fetchCachedData = filterHelperService.isFilterSelectedTillSprintLevel(kpiRequest.getLevel(), false);
		projectWiseSprints.forEach((basicProjectConfigId, sprintList) -> {
			Map<String, Object> result;
			if (fetchCachedData) { // fetch data from cache only if Filter is selected till Sprint
				// level.
				result = kpiDataCacheService.fetchDRRData(kpiRequest, basicProjectConfigId, sprintIdList,
						KPICode.DEFECT_REJECTION_RATE.getKpiId());
			} else { // fetch data from DB if filters below Sprint level (i.e. additional filters)
				result = kpiDataProvider.fetchDRRData(kpiRequest, basicProjectConfigId, sprintList);
			}

			totalSubTaskDefects
					.addAll((List<JiraIssue>) result.getOrDefault(TOTAL_SPRINT_SUBTASK_DEFECTS, new ArrayList<>()));
			subTaskBugsCustomHistory.addAll(
					(List<JiraIssueCustomHistory>) result.getOrDefault(SUB_TASK_BUGS_HISTORY, new ArrayList<>()));
			List<SprintDetails> sprintDetailsList = (List<SprintDetails>) result
					.getOrDefault(SPRINT_WISE_SPRINT_DETAILS, new ArrayList<>());
			sprintDetails.addAll(sprintDetailsList.stream().filter(sprint -> sprintList.contains(sprint.getSprintID()))
					.collect(Collectors.toSet()));
			totalIssue.addAll((List<JiraIssue>) result.getOrDefault(STORY_LIST, new ArrayList<>()));
			canceledDefectList.addAll((List<JiraIssue>) result.getOrDefault(REJECTED_DEFECT_DATA, new ArrayList<>()));
			totalDefectList.addAll((List<JiraIssue>) result.getOrDefault(TOTAL_DEFECT_LIST, new ArrayList<>()));
		});
		resultListMap.put(TOTAL_SPRINT_SUBTASK_DEFECTS, totalSubTaskDefects);
		resultListMap.put(SUB_TASK_BUGS_HISTORY, subTaskBugsCustomHistory);
		resultListMap.put(SPRINT_WISE_SPRINT_DETAILS, sprintDetails);
		resultListMap.put(STORY_LIST, totalIssue);
		resultListMap.put(REJECTED_DEFECT_DATA, canceledDefectList);
		resultListMap.put(TOTAL_DEFECT_LIST, totalDefectList);

		setDbQueryLogger(totalDefectList, canceledDefectList);

		return resultListMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Double calculateKPIMetrics(Map<String, Object> rejectedAndTotalDefectDataMap) {
		int cancelledDefectCount = ((List<Feature>) rejectedAndTotalDefectDataMap.get(REJECTED_DEFECT_DATA)).size();
		int totalDefectCount = ((List<Feature>) rejectedAndTotalDefectDataMap.get(CLOSED_DEFECT_DATA)).size();
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

		Map<String, Object> resultListMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate, kpiRequest);
		List<JiraIssue> totalDefectList = (List<JiraIssue>) resultListMap.get(TOTAL_DEFECT_LIST);
		List<JiraIssue> totalSubtaskList = (List<JiraIssue>) resultListMap.get(TOTAL_SPRINT_SUBTASK_DEFECTS);
		List<JiraIssue> storyList = (List<JiraIssue>) resultListMap.get(STORY_LIST);
		List<JiraIssueCustomHistory> totalSubtaskHistory = (List<JiraIssueCustomHistory>) resultListMap
				.get(SUB_TASK_BUGS_HISTORY);
		List<JiraIssue> canceledDefectList = (List<JiraIssue>) resultListMap.get(REJECTED_DEFECT_DATA);
		List<SprintDetails> sprintDetails = (List<SprintDetails>) resultListMap.get(SPRINT_WISE_SPRINT_DETAILS);

		Map<Pair<String, String>, Double> sprintWiseDRRMap = new HashMap<>();
		List<KPIExcelData> excelData = new ArrayList<>();
		Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseCompletedDefectListMap = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseRejectedDefectListMap = new HashMap<>();

		Set<String> canceledDefectNumbers = canceledDefectList.stream().map(JiraIssue::getNumber)
				.collect(Collectors.toSet());

		sprintDetails.forEach(sd -> {
			List<String> totalSprintIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sd,
					CommonConstant.TOTAL_ISSUES);

			List<String> completedSprintIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sd,
					CommonConstant.COMPLETED_ISSUES);

			FieldMapping fieldMapping = configHelperService.getFieldMapping(sd.getBasicProjectConfigId());
			// For finding the completed Defect we are taking combination of DodStatus &
			// DefectRejectionStatus
			List<String> dodStatus = Optional.ofNullable(fieldMapping).map(FieldMapping::getJiraDodKPI37)
					.orElse(Collections.emptyList()).stream().map(String::toLowerCase).toList();
			String defectRejectionStatus = Optional.ofNullable(fieldMapping)
					.map(FieldMapping::getJiraDefectRejectionStatusKPI37).orElse("");
			List<String> dodAndDefectRejStatus = new ArrayList<>(dodStatus);
			if (StringUtils.isNotEmpty(defectRejectionStatus))
				dodAndDefectRejStatus.add(defectRejectionStatus.toLowerCase());

			List<JiraIssue> sprintSubtask = KpiDataHelper.getTotalSprintSubTasks(totalSubtaskList.stream()
					.filter(jiraIssue -> CollectionUtils.isNotEmpty(jiraIssue.getSprintIdList())
							&& jiraIssue.getSprintIdList().contains(sd.getSprintID().split("_")[0]))
					.collect(Collectors.toList()), sd, totalSubtaskHistory, dodAndDefectRejStatus);

			List<JiraIssue> sprintRejectedDefects = canceledDefectList.stream()
					.filter(element -> totalSprintIssues.contains(element.getNumber())).collect(Collectors.toList());

			List<JiraIssue> sprintRejectedSubtaskDefect = sprintSubtask.stream()
					.filter(jiraIssue -> canceledDefectNumbers.contains(jiraIssue.getNumber())).toList();

			sprintRejectedDefects.addAll(sprintRejectedSubtaskDefect);

			List<JiraIssue> sprintCompletedDefects = totalDefectList.stream()
					.filter(element -> completedSprintIssues.contains(element.getNumber()))
					.filter(element -> dodAndDefectRejStatus.contains(element.getStatus().toLowerCase()))
					.collect(Collectors.toList());

			sprintCompletedDefects.addAll(KpiDataHelper.getCompletedSubTasksByHistory(sprintSubtask,
					totalSubtaskHistory, sd, dodAndDefectRejStatus, new HashMap<>()));

			List<JiraIssue> sprintWiseRejectedDefectList = new ArrayList<>();
			List<JiraIssue> sprintWiseCompletedDefectList = new ArrayList<>();
			List<Double> subCategoryWiseDRRList = new ArrayList<>();

			Map<String, Object> sprintWiseRejectedAndTotalDefects = new HashMap<>();
			Pair<String, String> sprint = Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID());
			double drrForCurrentLeaf = 0.0d;

			sprintWiseRejectedAndTotalDefects.put(REJECTED_DEFECT_DATA, sprintRejectedDefects);
			sprintWiseRejectedAndTotalDefects.put(CLOSED_DEFECT_DATA, sprintCompletedDefects);
			if (CollectionUtils.isNotEmpty(sprintRejectedDefects)
					&& CollectionUtils.isNotEmpty(sprintCompletedDefects)) {

				drrForCurrentLeaf = calculateKPIMetrics(sprintWiseRejectedAndTotalDefects);
				sprintWiseDRRMap.put(sprint, drrForCurrentLeaf);
			}
			subCategoryWiseDRRList.add(drrForCurrentLeaf);
			sprintWiseRejectedDefectList.addAll(sprintRejectedDefects);
			sprintWiseCompletedDefectList.addAll(sprintCompletedDefects);
			sprintWiseRejectedDefectListMap.put(sprint, sprintWiseRejectedDefectList);
			sprintWiseCompletedDefectListMap.put(sprint, sprintWiseCompletedDefectList);

			setSprintWiseLogger(sprint, sprintWiseCompletedDefectList, sprintWiseRejectedDefectList);


			setHoverMap(sprintWiseHowerMap, sprint, sprintWiseRejectedDefectList, sprintWiseCompletedDefectList);
		});

		sprintLeafNodeList.forEach(node -> {
			// Leaf node wise data
			String trendLineName = node.getProjectFilter().getName();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), node.getSprintFilter().getId());

			double drrForCurrentLeaf;

			if (sprintWiseDRRMap.containsKey(currentNodeIdentifier)) {
				drrForCurrentLeaf = sprintWiseDRRMap.get(currentNodeIdentifier);
				List<JiraIssue> sprintWiseRejectedDefectList = sprintWiseRejectedDefectListMap
						.get(currentNodeIdentifier);
				List<JiraIssue> sprintWiseCompletedDefectList = sprintWiseCompletedDefectListMap
						.get(currentNodeIdentifier);
				List<JiraIssue> sprintWiseCompAndRejectedList = new ArrayList<>(sprintWiseCompletedDefectList);
				sprintWiseCompAndRejectedList.addAll(sprintWiseRejectedDefectList);
				populateExcelDataObject(requestTrackerId, node.getSprintFilter().getName(), excelData,
						sprintWiseRejectedDefectList, sprintWiseCompAndRejectedList, storyList);

			} else {
				drrForCurrentLeaf = Double.NaN;
			}

			log.debug("[DRR-SPRINT-WISE][{}]. DRR for sprint {}  is {}", requestTrackerId,
					node.getSprintFilter().getName(), drrForCurrentLeaf);

			DataCount dataCount = new DataCount();
			if (!Double.isNaN(drrForCurrentLeaf)) {
				dataCount.setData(String.valueOf(Math.round(drrForCurrentLeaf)));
				dataCount.setValue(drrForCurrentLeaf);
				dataCount.setHoverValue(sprintWiseHowerMap.get(currentNodeIdentifier));
			}

			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
			dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));

			mapTmp.get(node.getId()).setValue(new ArrayList<DataCount>(Arrays.asList(dataCount)));
			trendValueList.add(dataCount);
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(
				KPIExcelColumn.DEFECT_REJECTION_RATE.getColumns(sprintLeafNodeList, cacheService, filterHelperService));
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

	private void populateExcelDataObject(String requestTrackerId, String sprintName, List<KPIExcelData> excelData,
			List<JiraIssue> sprintWiseRejectedDefectList, List<JiraIssue> sprintWiseCompAndRejectedList,
			List<JiraIssue> storyList) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			Map<String, JiraIssue> totalDefectList = new HashMap<>();
			sprintWiseCompAndRejectedList.forEach(bugs -> totalDefectList.putIfAbsent(bugs.getNumber(), bugs));
			KPIExcelUtility.populateDefectRelatedExcelData(sprintName, totalDefectList, sprintWiseRejectedDefectList,
					excelData, KPICode.DEFECT_REJECTION_RATE.getKpiId(), customApiConfig, storyList);
		}
	}

	/**
	 * Sets logger for data fetched from DB.
	 *
	 * @param totalDefectList
	 * @param canceledDefectList
	 */
	private void setDbQueryLogger(List<JiraIssue> totalDefectList, List<JiraIssue> canceledDefectList) {

		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.info(SEPARATOR_ASTERISK);
			log.info("************* DRR (dB) *******************");
			log.info("TotalDefectList -> story[{}]: {}", totalDefectList.size(),
					totalDefectList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			log.info("CanceledDefectList [{}]: {}", canceledDefectList.size(),
					canceledDefectList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			log.info("******************X----X*******************");
		}
	}

	/**
	 * Sets logger for sprint level data.
	 *
	 * @param sprint
	 * @param sprintWiseCompletedDefectList
	 * @param sprintWiseRejectedDefectList
	 */
	private void setSprintWiseLogger(Pair<String, String> sprint, List<JiraIssue> sprintWiseCompletedDefectList,
			List<JiraIssue> sprintWiseRejectedDefectList) {

		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.debug("************* SPRINT WISE DRR *******************");
			log.debug("Sprint: {}", sprint.getValue());
			log.debug("SprintWiseCompletedDefectList[{}]: {}", sprintWiseCompletedDefectList.size(),
					sprintWiseCompletedDefectList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			log.debug("SprintWiseRejectedDefectList[{}]: {}", sprintWiseRejectedDefectList.size(),
					sprintWiseRejectedDefectList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			log.debug(SEPARATOR_ASTERISK);
		}
	}

	/**
	 * Sets map to show on hover of sprint node.
	 *
	 * @param sprintWiseHoverMap
	 * @param sprint
	 * @param rejected
	 * @param completed
	 */
	private void setHoverMap(Map<Pair<String, String>, Map<String, Object>> sprintWiseHoverMap,
			Pair<String, String> sprint, List<JiraIssue> rejected, List<JiraIssue> completed) {
		Map<String, Object> howerMap = new LinkedHashMap<>();
		if (CollectionUtils.isNotEmpty(rejected)) {
			howerMap.put(REJECTED, rejected.size());
		} else {
			howerMap.put(REJECTED, 0);
		}
		if (CollectionUtils.isNotEmpty(completed)) {
			howerMap.put(COMPLETED, completed.size());
		} else {
			howerMap.put(COMPLETED, 0);
		}
		sprintWiseHoverMap.put(sprint, howerMap);
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI37(), KPICode.DEFECT_REJECTION_RATE.getKpiId());
	}
}
