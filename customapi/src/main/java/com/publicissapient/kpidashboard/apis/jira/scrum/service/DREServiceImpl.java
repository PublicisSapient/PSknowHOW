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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

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
	private static final String REMOVED = "Closed Defects";
	private static final String TOTAL = "Total Defects";
	private static final String DEV = "DeveloperKpi";
	public static final String TOTAL_DEFECTS = "totalDefects";
	public static final String DEFECT_HISTORY = "defectHistory";
	public static final String SPRINT_DETAILS = "sprintDetails";
	public static final String PROJECT_WISE_DEFECT_REMOVEL_STATUS = "projectWiseDefectRemovelStatus";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	public static final String SUB_TASK_BUGS = "subTaskBugs";
	public static final String SPRINT_REPORTED_BUGS = "Sprint Reported Bugs";
	public static final String JIRA_ISSUE_CLOSED_DATE = "jiraIssueClosedDate";
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
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue, KPICode.DEFECT_REMOVAL_EFFICIENCY);
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
		List<JiraIssue> sprintReportedBugsList;
		List<String> defectType = new ArrayList<>();
		leafNodeList.forEach(leaf -> {

			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());

			projectWiseDefectRemovelStatus.put(basicProjectConfigId.toString(),
					fieldMapping.getJiraDefectRemovalStatusKPI34());

			defectType.add(NormalizedJira.DEFECT_TYPE.getValue());
			mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(), defectType);
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
		});

		List<SprintDetails> sprintDetails = sprintRepository.findBySprintIDIn(sprintList);
		Set<String> totalNonBugIssues = new HashSet<>();
		Set<String> totalIssue = new HashSet<>();
		Set<String> totalIssueInSprint = new HashSet<>();
		sprintDetails.forEach(sprintDetail -> {
			if (CollectionUtils.isNotEmpty(sprintDetail.getTotalIssues())) {
				FieldMapping fieldMapping = configHelperService.getFieldMapping(sprintDetail.getBasicProjectConfigId());
				totalNonBugIssues.addAll(sprintDetail.getTotalIssues().stream()
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

		if (CollectionUtils.isNotEmpty(totalIssue)) {
			List<JiraIssue> totalSprintReportDefects = jiraIssueRepository.findIssueByNumber(mapOfFilters, totalIssue,
					uniqueProjectMap);
			sprintReportedBugsList = totalSprintReportDefects;
			List<JiraIssue> subTaskBugs = jiraIssueRepository
					.findLinkedDefects(mapOfFilters, totalNonBugIssues, uniqueProjectMap).stream()
					.filter(jiraIssue -> !totalIssueInSprint.contains(jiraIssue.getNumber())).collect(Collectors.toList());

			ArrayList<JiraIssue> totalDefects = new ArrayList<>(subTaskBugs);
			List<JiraIssueCustomHistory> defectsCustomHistory = jiraIssueCustomHistoryRepository
					.findByStoryIDInAndBasicProjectConfigIdIn(
							subTaskBugs.stream().map(JiraIssue::getNumber).collect(Collectors.toList()),
							basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

			setDbQueryLogger(sprintDetails, totalDefects, totalSprintReportDefects);

			resultListMap.put(SPRINT_REPORTED_BUGS, sprintReportedBugsList);
			resultListMap.put(TOTAL_DEFECTS, totalSprintReportDefects);
			resultListMap.put(SUB_TASK_BUGS, subTaskBugs);
			resultListMap.put(DEFECT_HISTORY, defectsCustomHistory);
			resultListMap.put(SPRINT_DETAILS, sprintDetails);
			resultListMap.put(PROJECT_WISE_DEFECT_REMOVEL_STATUS, projectWiseDefectRemovelStatus);

		}

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
		List<JiraIssue> totalDefects = (List<JiraIssue>) storyDefectDataListMap.get(TOTAL_DEFECTS);
		List<JiraIssue> subTaskBugs = (List<JiraIssue>) storyDefectDataListMap.get(SUB_TASK_BUGS);
		List<JiraIssue> sprintReportedBugs = (List<JiraIssue>) storyDefectDataListMap.get(SPRINT_REPORTED_BUGS);
		List<JiraIssueCustomHistory> defectsCustomHistory = (List<JiraIssueCustomHistory>) storyDefectDataListMap.get(DEFECT_HISTORY);
		List<SprintDetails> sprintDetails = (List<SprintDetails>) storyDefectDataListMap.get(SPRINT_DETAILS);
		Map<String, List<String>> projectWiseDefectRemovalStatus = (Map<String, List<String>>) storyDefectDataListMap.get(PROJECT_WISE_DEFECT_REMOVEL_STATUS);

		Map<Pair<String, String>, Double> sprintWiseDREMap = new HashMap<>();
		Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseTotaldDefectListMap = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseCloseddDefectListMap = new HashMap<>();
		List<KPIExcelData> excelData = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(sprintDetails)) {

			sprintDetails.forEach(sd -> {
				List<JiraIssue> sprintWiseClosedDefectList = new ArrayList<>();
				List<JiraIssue> sprintWiseTotaldDefectList = new ArrayList<>();

				List<String> totalStoryIdList = new ArrayList<>();
				Map<String, Object> subCategoryWiseClosedAndTotalDefectList = new HashMap<>();

				List<String> completedSprintIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sd,
						CommonConstant.COMPLETED_ISSUES);
				Set<JiraIssue> totalSubTask = new HashSet<>();
				getSubtasks(subTaskBugs, defectsCustomHistory, projectWiseDefectRemovalStatus, totalSubTask, sd);
				List<String> totalIssues = new ArrayList<>(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sd,
						CommonConstant.TOTAL_ISSUES));
				List<JiraIssue> subCategoryWiseTotalDefectList = totalDefects.stream()
						.filter(f -> totalIssues.contains(f.getNumber()))
						.collect(Collectors.toList());
				subCategoryWiseTotalDefectList.addAll(totalSubTask);

				List<JiraIssue> subCategoryWiseClosedDefectList = sprintReportedBugs.stream()
						.filter(f -> completedSprintIssues.contains(f.getNumber()) && CollectionUtils
								.emptyIfNull(projectWiseDefectRemovalStatus.get(f.getBasicProjectConfigId())).stream()
								.anyMatch(s -> s.equalsIgnoreCase(f.getJiraStatus())))
						.collect(Collectors.toList());

				subCategoryWiseClosedDefectList.addAll(getCompletedSubTasksByHistory(totalSubTask, defectsCustomHistory,
						sd, projectWiseDefectRemovalStatus));

				double dreForCurrentLeaf = 0.0d;
				subCategoryWiseClosedAndTotalDefectList.put(CLOSED_DEFECT_DATA, subCategoryWiseClosedDefectList);
				subCategoryWiseClosedAndTotalDefectList.put(TOTAL_DEFECT_DATA, subCategoryWiseTotalDefectList);
				if (CollectionUtils.isNotEmpty(subCategoryWiseClosedDefectList)
						&& CollectionUtils.isNotEmpty(subCategoryWiseTotalDefectList)) {

					dreForCurrentLeaf = calculateKPIMetrics(subCategoryWiseClosedAndTotalDefectList);
				} else if (CollectionUtils.isEmpty(subCategoryWiseTotalDefectList)) {
					// Adding check when total defects injected is 0n, DRE will be
					// 100.0 in this case
					dreForCurrentLeaf = 100.0d;
				}

				sprintWiseClosedDefectList.addAll(subCategoryWiseClosedDefectList);
				sprintWiseTotaldDefectList.addAll(subCategoryWiseTotalDefectList);
				Pair<String, String> sprint = Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID());
				sprintWiseCloseddDefectListMap.put(sprint, sprintWiseClosedDefectList);
				sprintWiseTotaldDefectListMap.put(sprint, sprintWiseTotaldDefectList);

				setSprintWiseLogger(sprint, totalStoryIdList, sprintWiseTotaldDefectList, sprintWiseClosedDefectList);

				sprintWiseDREMap.put(sprint, dreForCurrentLeaf);
				setHowerMap(sprintWiseHowerMap, sprint, sprintWiseClosedDefectList, sprintWiseTotaldDefectList);
			});
		}

		sprintLeafNodeList.forEach(node -> {

			String trendLineName = node.getProjectFilter().getName();

			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), node.getSprintFilter().getId());

			double dreForCurrentLeaf;

			if (sprintWiseDREMap.containsKey(currentNodeIdentifier)) {
				dreForCurrentLeaf = sprintWiseDREMap.get(currentNodeIdentifier);
				List<JiraIssue> sprintWiseClosedDefectList = sprintWiseCloseddDefectListMap.get(currentNodeIdentifier);
				List<JiraIssue> sprintWiseTotaldDefectList = sprintWiseTotaldDefectListMap.get(currentNodeIdentifier);
				populateExcelDataObject(requestTrackerId, node.getSprintFilter().getName(), excelData,
						sprintWiseClosedDefectList, sprintWiseTotaldDefectList);

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

	private void populateExcelDataObject(String requestTrackerId, String sprintName, List<KPIExcelData> excelData,
			List<JiraIssue> sprintWiseClosedDefectList, List<JiraIssue> sprintWiseTotaldDefectList) {
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
	 * @param sprintDetails
	 * @param totalDefectList
	 * @param linkedStoryIds
	 */
	private void setDbQueryLogger(List<SprintDetails> sprintDetails, List<JiraIssue> totalDefectList,
								  List<JiraIssue> linkedStoryIds) {

		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.info(SEPARATOR_ASTERISK);
			log.info("************* DRE (dB) *******************");
			log.info("SprintDetails[{}]: {}", sprintDetails.size(), sprintDetails);
			log.info("TotalDefectList LinkedWith -> story[{}]: {}", totalDefectList.size(), linkedStoryIds);
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

	public List<JiraIssue> getCompletedSubTasksByHistory(Set<JiraIssue> totalSubTask,
			List<JiraIssueCustomHistory> subTaskHistory, SprintDetails sprintDetail,
			Map<String, List<String>> projectWiseDefectRemovelStatus) {
		List<JiraIssue> completedSubtaskOfSprint = new ArrayList<>();
		LocalDate sprintEndDate = sprintDetail.getCompleteDate() != null
				? LocalDate.parse(sprintDetail.getCompleteDate().split("\\.")[0], DATE_TIME_FORMATTER)
				: LocalDate.parse(sprintDetail.getEndDate().split("\\.")[0], DATE_TIME_FORMATTER);
		LocalDate sprintStartDate = sprintDetail.getActivatedDate() != null
				? LocalDate.parse(sprintDetail.getActivatedDate().split("\\.")[0], DATE_TIME_FORMATTER)
				: LocalDate.parse(sprintDetail.getStartDate().split("\\.")[0], DATE_TIME_FORMATTER);
		List<String> defectRemovalStatus = projectWiseDefectRemovelStatus
				.get(sprintDetail.getBasicProjectConfigId().toString());
		totalSubTask.forEach(jiraIssue -> {
			JiraIssueCustomHistory jiraIssueCustomHistory = subTaskHistory.stream().filter(
					issueCustomHistory -> issueCustomHistory.getStoryID().equalsIgnoreCase(jiraIssue.getNumber()))
					.findFirst().orElse(new JiraIssueCustomHistory());
			Optional<JiraHistoryChangeLog> issueSprint = jiraIssueCustomHistory.getStatusUpdationLog().stream()
					.filter(jiraIssueSprint -> DateUtil.isWithinDateRange(jiraIssueSprint.getUpdatedOn().toLocalDate(),
							sprintStartDate, sprintEndDate))
					.reduce((a, b) -> b);
			if (issueSprint.isPresent() && defectRemovalStatus.contains(issueSprint.get().getChangedTo()))
				completedSubtaskOfSprint.add(jiraIssue);
		});
		return completedSubtaskOfSprint;
	}

	private static void getSubtasks(List<JiraIssue> allSubTaskBugs, List<JiraIssueCustomHistory> defectsCustomHistory,
									Map<String, List<String>> projectWiseDefectRemovalStatus,
									Set<JiraIssue> totalSubTask, SprintDetails sprintDetail) {
		LocalDateTime sprintEndDate = sprintDetail.getCompleteDate() != null
				? LocalDateTime.parse(sprintDetail.getCompleteDate().split("\\.")[0], DATE_TIME_FORMATTER)
				: LocalDateTime.parse(sprintDetail.getEndDate().split("\\.")[0], DATE_TIME_FORMATTER);
		LocalDateTime sprintStartDate = sprintDetail.getActivatedDate() != null
				? LocalDateTime.parse(sprintDetail.getActivatedDate().split("\\.")[0], DATE_TIME_FORMATTER)
				: LocalDateTime.parse(sprintDetail.getStartDate().split("\\.")[0], DATE_TIME_FORMATTER);
		allSubTaskBugs.forEach(jiraIssue -> {
			LocalDateTime jiraCreatedDate = LocalDateTime.parse(jiraIssue.getCreatedDate().split("\\.")[0], DATE_TIME_FORMATTER);
			JiraIssueCustomHistory jiraIssueCustomHistoryOfClosedSubTask = defectsCustomHistory.stream()
					.filter(jiraIssueCustomHistory -> jiraIssueCustomHistory.getStoryID()
							.equalsIgnoreCase(jiraIssue.getNumber())).findFirst().orElse(new JiraIssueCustomHistory());
			Map<String, LocalDateTime> jiraTicketClosedDateMap = new HashMap<>();
			getJiraIssueClosedDate(projectWiseDefectRemovalStatus, jiraIssue, jiraIssueCustomHistoryOfClosedSubTask,
					jiraTicketClosedDateMap);

			if (CollectionUtils.isNotEmpty(jiraIssue.getSprintIdList()) && jiraIssue.getSprintIdList()
					.contains(sprintDetail.getSprintID().split("_")[0])
					&& projectWiseDefectRemovalStatus.get(jiraIssue.getBasicProjectConfigId()).stream()
					.anyMatch(s -> !s.equalsIgnoreCase(jiraIssue.getStatus()))
					&& null != jiraTicketClosedDateMap.get(JIRA_ISSUE_CLOSED_DATE)
					&& ((sprintEndDate.isAfter(jiraCreatedDate)
					&& jiraTicketClosedDateMap.get(JIRA_ISSUE_CLOSED_DATE).isAfter(sprintEndDate)) ||
					(DateUtil.isWithinDateTimeRange(jiraTicketClosedDateMap.get(JIRA_ISSUE_CLOSED_DATE),
							sprintStartDate, sprintEndDate)))) {
				totalSubTask.add(jiraIssue);
			}
			if (CollectionUtils.isNotEmpty(jiraIssue.getSprintIdList()) && jiraIssue.getSprintIdList()
					.contains(sprintDetail.getSprintID().split("_")[0]) && null != jiraTicketClosedDateMap.get(JIRA_ISSUE_CLOSED_DATE)
					&& DateUtil.isWithinDateTimeRange(jiraTicketClosedDateMap.get(JIRA_ISSUE_CLOSED_DATE),
					sprintStartDate, sprintEndDate)) {
				totalSubTask.add(jiraIssue);
			}
		});
	}

	private static void getJiraIssueClosedDate(Map<String, List<String>> projectWiseDefectRemovalStatus, JiraIssue jiraIssue,
											   JiraIssueCustomHistory jiraIssueCustomHistoryOfClosedSubTask,
											   Map<String, LocalDateTime> jiraTicketClosedDateMap) {
		jiraIssueCustomHistoryOfClosedSubTask.getStatusUpdationLog().forEach(historyChangeLog -> {
			List<String> closureStatusList = projectWiseDefectRemovalStatus.get(jiraIssue.getBasicProjectConfigId());
			LocalDateTime jiraTicketClosedDate = null;
			if (CollectionUtils.isNotEmpty(closureStatusList)
					&& closureStatusList.contains(historyChangeLog.getChangedTo())) {
				jiraTicketClosedDate = historyChangeLog.getUpdatedOn();
			}
			if (null != jiraTicketClosedDate) {
				jiraTicketClosedDateMap.put(JIRA_ISSUE_CLOSED_DATE, jiraTicketClosedDate);
			}
		});
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping){
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI34(),KPICode.DEFECT_REMOVAL_EFFICIENCY.getKpiId());
	}
}
