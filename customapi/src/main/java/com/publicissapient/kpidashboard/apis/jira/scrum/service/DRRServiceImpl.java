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
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

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
	private static final String CLOSED_DEFECT_DATA = "closedDefects";
	private static final String REJECTED = "Rejected Defects";
	private static final String COMPLETED = "Completed Defects";
	private static final String DEV = "DeveloperKpi";
	public static final String SPRINT_WISE_SPRINT_DETAILS = "sprintWiseSprintDetails";
	public static final String TOTAL_DEFECT_LIST = "totalDefectList";

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private FilterHelperService flterHelperService;
	@Autowired
	private SprintRepository sprintRepository;
	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	private static final String TOTAL_SPRINT_SUBTASK_DEFECTS = "totalSprintSubtaskDefects";
	private static final String SUB_TASK_BUGS_HISTORY = "SubTaskBugsHistory";

	public static void getDefectsWithDrop(Map<String, Map<String, List<String>>> droppedDefects,
			List<JiraIssue> defectDataList, List<JiraIssue> defectListWthDrop) {
		if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(defectDataList)) {
			Set<JiraIssue> defectListWthDropSet = new HashSet<>();
			defectDataList.forEach(jiraIssue -> getDefectsWthDrop(droppedDefects, defectListWthDropSet, jiraIssue));
			defectListWthDrop.addAll(defectListWthDropSet);
		}
	}

	private static void getDefectsWthDrop(Map<String, Map<String, List<String>>> droppedDefects,
			Set<JiraIssue> defectListWthDropSet, JiraIssue jiraIssue) {
		if (!StringUtils.isBlank(jiraIssue.getStatus())) {
			Map<String, List<String>> defectStatus = droppedDefects.get(jiraIssue.getBasicProjectConfigId());
			if (!defectStatus.isEmpty() && (StringUtils.isNotEmpty(jiraIssue.getResolution())
					&& CollectionUtils.isNotEmpty(defectStatus.get(Constant.RESOLUTION_TYPE_FOR_REJECTION))
					&& defectStatus.get(Constant.RESOLUTION_TYPE_FOR_REJECTION).stream().map(String::toLowerCase)
							.collect(Collectors.toList()).contains(jiraIssue.getResolution().toLowerCase())
					|| (StringUtils.isNotEmpty(jiraIssue.getStatus())
							&& CollectionUtils.isNotEmpty(defectStatus.get(Constant.DEFECT_REJECTION_STATUS))
							&& defectStatus.get(Constant.DEFECT_REJECTION_STATUS).stream().map(String::toLowerCase)
									.collect(Collectors.toList()).contains(jiraIssue.getStatus().toLowerCase())))) {
				defectListWthDropSet.add(jiraIssue);

			}
		}
	}

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
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue, KPICode.DEFECT_REJECTION_RATE);
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
		List<String> defectType = new ArrayList<>();

		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());
			defectType.add(NormalizedJira.DEFECT_TYPE.getValue());
			KpiHelperService.getDroppedDefectsFilters(defectResolutionRejectionMap, basicProjectConfigId,
					fieldMapping.getResolutionTypeForRejectionKPI37(),
					fieldMapping.getJiraDefectRejectionStatusKPI37());
			mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(defectType));
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
		});

		List<SprintDetails> sprintDetails = sprintRepository.findBySprintIDIn(sprintList);

		Set<String> totalSprintReportStories = new HashSet<>();
		Set<String> totalIssue = new HashSet<>();
		Set<String> totalIssueInSprint = new HashSet<>();
		sprintDetails.forEach(sprintDetail -> {
			if (CollectionUtils.isNotEmpty(sprintDetail.getTotalIssues())) {
				FieldMapping fieldMapping = configHelperService.getFieldMapping(sprintDetail.getBasicProjectConfigId());
				totalSprintReportStories.addAll(sprintDetail.getTotalIssues().stream()
						.filter(sprintIssue -> !fieldMapping.getJiradefecttype().contains(sprintIssue.getTypeName()))
						.map(SprintIssue::getNumber).collect(Collectors.toSet()));
				totalIssue.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail,
						CommonConstant.TOTAL_ISSUES));
			}
			totalIssueInSprint.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail,
					CommonConstant.TOTAL_ISSUES));
			totalIssueInSprint.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail,
					CommonConstant.COMPLETED_ISSUES_ANOTHER_SPRINT));
			totalIssueInSprint.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail,
					CommonConstant.PUNTED_ISSUES));
			totalIssueInSprint.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail,
					CommonConstant.ADDED_ISSUES));

		});

		/** additional filter **/
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, flterHelperService);

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));
		List<JiraIssue> totalDefectList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(totalIssue)) {
			List<JiraIssue> totalSprintReportDefects = jiraIssueRepository.findIssueByNumber(mapOfFilters, totalIssue,
					uniqueProjectMap);

			List<JiraIssue> totalSubTaskDefects = jiraIssueRepository
					.findLinkedDefects(mapOfFilters, totalSprintReportStories, uniqueProjectMap).stream()
					.filter(jiraIssue -> !totalIssueInSprint.contains(jiraIssue.getNumber())).collect(Collectors.toList());

			List<JiraIssueCustomHistory> subTaskBugsCustomHistory = jiraIssueCustomHistoryRepository
					.findByStoryIDInAndBasicProjectConfigIdIn(
							totalSubTaskDefects.stream().map(JiraIssue::getNumber).collect(Collectors.toList()),
							basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));
			totalDefectList.addAll(totalSprintReportDefects);
			totalDefectList.addAll(totalSubTaskDefects);
			resultListMap.put(TOTAL_SPRINT_SUBTASK_DEFECTS, totalSubTaskDefects);
			resultListMap.put(SUB_TASK_BUGS_HISTORY, subTaskBugsCustomHistory);
			resultListMap.put(SPRINT_WISE_SPRINT_DETAILS, sprintDetails);

		}
		// Find defect with rejected status. Avoided making dB query
		if (!defectResolutionRejectionMap.isEmpty()) {
			List<JiraIssue> canceledDefectList = new ArrayList<>();
			getDefectsWithDrop(defectResolutionRejectionMap, totalDefectList, canceledDefectList);

			setDbQueryLogger(totalDefectList, canceledDefectList);

			resultListMap.put(REJECTED_DEFECT_DATA, canceledDefectList);
		} else {
			resultListMap.put(REJECTED_DEFECT_DATA, Lists.newArrayList());
		}

		resultListMap.put(TOTAL_DEFECT_LIST, totalDefectList);
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
					.orElse(Collections.emptyList()).stream().map(String::toLowerCase).collect(Collectors.toList());
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
					.filter(jiraIssue -> canceledDefectNumbers.contains(jiraIssue.getNumber()))
					.collect(Collectors.toList());

			sprintRejectedDefects.addAll(sprintRejectedSubtaskDefect);

			List<JiraIssue> sprintCompletedDefects = totalDefectList.stream()
					.filter(element -> completedSprintIssues.contains(element.getNumber()))
					.filter(element -> dodAndDefectRejStatus.contains(element.getStatus().toLowerCase())).collect(Collectors.toList());

			sprintCompletedDefects.addAll(
					KpiDataHelper.getCompletedSubTasksByHistory(sprintSubtask, totalSubtaskHistory, sd, dodAndDefectRejStatus));

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
			}
			subCategoryWiseDRRList.add(drrForCurrentLeaf);
			sprintWiseRejectedDefectList.addAll(sprintRejectedDefects);
			sprintWiseCompletedDefectList.addAll(sprintCompletedDefects);
			sprintWiseRejectedDefectListMap.put(sprint, sprintWiseRejectedDefectList);
			sprintWiseCompletedDefectListMap.put(sprint, sprintWiseCompletedDefectList);

			setSprintWiseLogger(sprint, sprintWiseCompletedDefectList, sprintWiseRejectedDefectList);

			sprintWiseDRRMap.put(sprint, drrForCurrentLeaf);
			setHowerMap(sprintWiseHowerMap, sprint, sprintWiseRejectedDefectList, sprintWiseCompletedDefectList);
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
						sprintWiseRejectedDefectList, sprintWiseCompAndRejectedList);

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

	private void populateExcelDataObject(String requestTrackerId, String sprintName, List<KPIExcelData> excelData,
			List<JiraIssue> sprintWiseRejectedDefectList, List<JiraIssue> sprintWiseCompAndRejectedList) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			Map<String, JiraIssue> totalDefectList = new HashMap<>();
			sprintWiseCompAndRejectedList.stream().forEach(bugs -> totalDefectList.putIfAbsent(bugs.getNumber(), bugs));
			KPIExcelUtility.populateDefectRelatedExcelData(sprintName, totalDefectList, sprintWiseRejectedDefectList,
					excelData, KPICode.DEFECT_REJECTION_RATE.getKpiId());
		}
	}

	/**
	 * Sets logger for data fetched from DB.
	 *
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
			log.info(SEPARATOR_ASTERISK);
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
			log.debug(SEPARATOR_ASTERISK);
			log.debug("************* SPRINT WISE DRR *******************");
			log.debug("Sprint: {}", sprint.getValue());
			log.debug("SprintWiseCompletedDefectList[{}]: {}", sprintWiseCompletedDefectList.size(),
					sprintWiseCompletedDefectList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
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
	 * @param completed
	 */
	private void setHowerMap(Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap,
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
		sprintWiseHowerMap.put(sprint, howerMap);
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping){
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI37(),KPICode.DEFECT_REJECTION_RATE.getKpiId());
	}

}
