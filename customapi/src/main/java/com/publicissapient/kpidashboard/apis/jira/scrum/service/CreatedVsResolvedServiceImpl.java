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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

import lombok.extern.slf4j.Slf4j;

/**
 * This class calculates the Created vs Resolved Defects.
 *
 * @author marjesur
 */
@Component
@Slf4j
public class CreatedVsResolvedServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String SEPARATOR_ASTERISK = "*************************************";
	private static final String CREATED_VS_RESOLVED_KEY = "createdVsResolvedKey";
	public static final String STORY_LIST = "storyList";
	private static final String SPRINT_WISE_SPRINTDETAILS = "sprintWiseSprintDetailMap";
	private static final String SPRINT_WISE_SUB_TASK_BUGS = "sprintWiseSubTaskBugs";
	private static final String SUB_TASK_BUGS_HISTORY = "SubTaskBugsHistory";
	private static final String CREATED_DEFECTS = "createdDefects";
	private static final String RESOLVED_DEFECTS = "resolvedDefects";
	private static final String TAGGED_DEFECTS_CREATED_AFTER_SPRINT = "Added Defects";
	private static final String TAGGED_DEFECTS = "Total Defects";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private CacheService cacheService;
	@Autowired
	private FilterHelperService filterHelperService;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private KpiDataCacheService kpiDataCacheService;
	@Autowired
	private KpiDataProvider kpiDataProvider;

	private List<String> sprintIdList = Collections.synchronizedList(new ArrayList<>());

	/**
	 * Gets Qualifier Type
	 *
	 * @return KPICode's <tt>CREATED_VS_RESOLVED</tt> enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.CREATED_VS_RESOLVED_DEFECTS.name();
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
			if (Filters.getFilter(k) == Filters.SPRINT) {
				sprintWiseLeafNodeValue(mapTmp, v, kpiElement, kpiRequest);
			}
		});

		log.debug("[CREATED-RESOLVED-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.CREATED_VS_RESOLVED_DEFECTS);
		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.CREATED_VS_RESOLVED_DEFECTS);
		Map<String, Map<String, List<DataCount>>> statusTypeProjectWiseDc = new LinkedHashMap<>();
		Map<String, List<DataCount>> unsortedMap = trendValuesMap.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
		unsortedMap.forEach((statusType, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			statusTypeProjectWiseDc.put(statusType, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		statusTypeProjectWiseDc.forEach((issueType, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.entrySet().forEach(trend -> dataList.addAll(trend.getValue()));
			dataCountGroup.setFilter(issueType);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});
		kpiElement.setTrendValueList(dataCountGroups);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);
		return kpiElement;
	}

	/**
	 * Fetches KPI Data from Created Vs Resolved.
	 *
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @param kpiRequest
	 * @return {@code Map<String, Object>}
	 */
	@SuppressWarnings("unchecked")
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

		List<JiraIssue> totalSprintReportDefects = new ArrayList<>();
		List<JiraIssue> subTaskBugs = new ArrayList<>();
		List<JiraIssueCustomHistory> subTaskBugsCustomHistory = new ArrayList<>();
		List<SprintDetails> sprintDetails = new ArrayList<>();
		List<JiraIssue> totalIssue = new ArrayList<>();
		boolean fetchCachedData = filterHelperService.isFilterSelectedTillSprintLevel(kpiRequest.getLevel(), false);
		projectWiseSprints.forEach((basicProjectConfigId, sprintList) -> {
			Map<String, Object> result;
			if (fetchCachedData) { // fetch data from cache only if Filter is selected till Sprint
				// level.
				result = kpiDataCacheService.fetchCreatedVsResolvedData(kpiRequest, basicProjectConfigId, sprintIdList,
						KPICode.CREATED_VS_RESOLVED_DEFECTS.getKpiId());
			} else { // fetch data from DB if filters below Sprint level (i.e. additional filters)
				result = kpiDataProvider.fetchCreatedVsResolvedData(kpiRequest, basicProjectConfigId, sprintList);
			}

			totalSprintReportDefects
					.addAll((List<JiraIssue>) result.getOrDefault(CREATED_VS_RESOLVED_KEY, new ArrayList<>()));
			subTaskBugs.addAll((List<JiraIssue>) result.getOrDefault(SPRINT_WISE_SUB_TASK_BUGS, new ArrayList<>()));
			subTaskBugsCustomHistory
					.addAll((List<JiraIssueCustomHistory>) result.getOrDefault(SUB_TASK_BUGS_HISTORY, new ArrayList<>()));
			List<SprintDetails> sprintDetailsList = (List<SprintDetails>) result.getOrDefault(SPRINT_WISE_SPRINTDETAILS,
					new ArrayList<>());
			sprintDetails.addAll(sprintDetailsList.stream().filter(sprint -> sprintList.contains(sprint.getSprintID()))
					.collect(Collectors.toSet()));
			totalIssue.addAll((List<JiraIssue>) result.getOrDefault(STORY_LIST, new ArrayList<>()));
		});
		resultListMap.put(CREATED_VS_RESOLVED_KEY, totalSprintReportDefects);
		resultListMap.put(SPRINT_WISE_SUB_TASK_BUGS, subTaskBugs);
		resultListMap.put(SUB_TASK_BUGS_HISTORY, subTaskBugsCustomHistory);
		resultListMap.put(SPRINT_WISE_SPRINTDETAILS, sprintDetails);
		resultListMap.put(STORY_LIST, totalIssue);

		setDbQueryLogger((List<JiraIssue>) resultListMap.get(CREATED_VS_RESOLVED_KEY));
		return resultListMap;
	}

	/**
	 * @param createdVsResolvedMap
	 * @return timeLogged in seconds
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Double calculateKPIMetrics(Map<String, Object> createdVsResolvedMap) {
		String requestTrackerId = getRequestTrackerId();
		Double createdVsResolved = 0.0d;
		List<JiraIssue> createdVsResolvedList = (List<JiraIssue>) createdVsResolvedMap.get(CREATED_VS_RESOLVED_KEY);
		log.debug("[CREATED-RESOLVED][{}]. Stories Count: {}", requestTrackerId, createdVsResolvedList.size());
		for (JiraIssue jiraIssue : createdVsResolvedList) {
			createdVsResolved = createdVsResolved + Double.valueOf(jiraIssue.getCount());
		}
		return createdVsResolved;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint wise.
	 *
	 * @param mapTmp
	 * @param kpiElement
	 * @param sprintLeafNodeList
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();

		sprintLeafNodeList.sort(
				(node1, node2) -> node1.getSprintFilter().getStartDate().compareTo(node2.getSprintFilter().getStartDate()));
		long time = System.currentTimeMillis();
		Map<String, Object> createdVsResolvedMap = fetchKPIDataFromDb(sprintLeafNodeList, null, null, kpiRequest);
		log.info("CreatedVsResolved taking fetchKPIDataFromDb {}", System.currentTimeMillis() - time);

		List<JiraIssue> allJiraIssue = (List<JiraIssue>) createdVsResolvedMap.get(CREATED_VS_RESOLVED_KEY);

		List<JiraIssue> allSubTaskBugs = (List<JiraIssue>) createdVsResolvedMap.get(SPRINT_WISE_SUB_TASK_BUGS);
		List<JiraIssue> storyList = (List<JiraIssue>) createdVsResolvedMap.get(STORY_LIST);

		List<JiraIssueCustomHistory> allSubTaskBugsHistory = (List<JiraIssueCustomHistory>) createdVsResolvedMap
				.get(SUB_TASK_BUGS_HISTORY);

		List<SprintDetails> sprintDetails = (List<SprintDetails>) createdVsResolvedMap.get(SPRINT_WISE_SPRINTDETAILS);

		Map<Pair<String, String>, List<JiraIssue>> sprintWiseCreatedIssues = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseClosedIssues = new HashMap<>();
		Map<Pair<String, String>, Map<String, String>> sprintWiseClosedIssueStatus = new HashMap<>();

		if ((CollectionUtils.isNotEmpty(allJiraIssue) || CollectionUtils.isNotEmpty(allSubTaskBugs)) &&
				CollectionUtils.isNotEmpty(sprintDetails)) {

			sprintDetails.forEach(sd -> {
				List<String> availableIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sd,
						CommonConstant.TOTAL_ISSUES);
				List<String> completedSprintIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sd,
						CommonConstant.COMPLETED_ISSUES);
				List<JiraIssue> totalIssues = allJiraIssue.stream()
						.filter(element -> availableIssues.contains(element.getNumber())).collect(Collectors.toList());
				List<JiraIssue> totalSubTask = getTotalSubTasks(
						allSubTaskBugs.stream()
								.filter(jiraIssue -> CollectionUtils.isNotEmpty(jiraIssue.getSprintIdList()) &&
										jiraIssue.getSprintIdList().contains(sd.getSprintID().split("_")[0]))
								.collect(Collectors.toList()),
						sd, allSubTaskBugsHistory);
				totalIssues.addAll(totalSubTask);
				List<JiraIssue> completedIssues = getCompletedIssues(allJiraIssue.stream()
						.filter(element -> completedSprintIssues.contains(element.getNumber())).collect(Collectors.toList()), sd);
				// to get the resolved status along with the completed jira issue
				Map<String, String> issueWiseDeliveredStatus = completedIssues.stream()
						.collect(Collectors.toMap(JiraIssue::getNumber, JiraIssue::getStatus, (e1, e2) -> e1));

				FieldMapping fieldMapping = configHelperService.getFieldMapping(sd.getBasicProjectConfigId());
				List<String> deliveredStatus = Optional.ofNullable(fieldMapping)
						.map(FieldMapping::getJiraIssueDeliverdStatusKPI126).orElse(Collections.emptyList()).stream()
						.map(String::toLowerCase).collect(Collectors.toList());
				completedIssues.addAll(KpiDataHelper.getCompletedSubTasksByHistory(totalSubTask, allSubTaskBugsHistory, sd,
						deliveredStatus, issueWiseDeliveredStatus));

				Pair<String, String> nodeIdentifier = Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID());
				sprintWiseCreatedIssues.put(nodeIdentifier, totalIssues);
				sprintWiseClosedIssues.put(nodeIdentifier, completedIssues);
				sprintWiseClosedIssueStatus.put(nodeIdentifier, issueWiseDeliveredStatus);
			});
		}

		List<KPIExcelData> excelData = new ArrayList<>();

		sprintLeafNodeList.forEach(node -> {
			// Leaf node wise data
			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair.of(node.getProjectFilter().getBasicProjectConfigId().toString(),
					currentSprintComponentId);

			Map<String, Object> createdVsResolvedHoverMap = new HashMap<>();

			List<JiraIssue> totalCreatedIssues = new ArrayList<>();
			List<JiraIssue> totalCreatedIssuesAfter = new ArrayList<>();
			List<JiraIssue> totalClosedIssues = new ArrayList<>();
			Map<String, String> closedIssuesWithStatus = new HashMap<>();

			Optional<SprintDetails> jiraSprint = sprintDetails.stream()
					.filter(sd -> sd.getSprintID().equalsIgnoreCase(currentSprintComponentId)).findFirst();
			String sprintStartDate = jiraSprint.isPresent() && jiraSprint.get().getActivatedDate() != null
					? jiraSprint.get().getActivatedDate()
					: node.getSprintFilter().getStartDate();
			if (CollectionUtils.isNotEmpty(sprintWiseCreatedIssues.get(currentNodeIdentifier))) {
				totalCreatedIssues = sprintWiseCreatedIssues.get(currentNodeIdentifier);
				totalCreatedIssuesAfter = totalCreatedIssues.stream()
						.filter(jiraIssue -> LocalDate.parse(jiraIssue.getCreatedDate().split("\\.")[0], DATE_TIME_FORMATTER)
								.isAfter(LocalDate.parse(sprintStartDate.split("\\.")[0], DATE_TIME_FORMATTER)))
						.collect(Collectors.toList());
				totalClosedIssues = sprintWiseClosedIssues.get(currentNodeIdentifier);
				closedIssuesWithStatus = sprintWiseClosedIssueStatus.get(currentNodeIdentifier);
			}
			Map<String, Map<String, Double>> createdVsResolvedDefectsMap = createCreatedVsResolvedMap(totalCreatedIssues,
					totalClosedIssues, totalCreatedIssuesAfter, createdVsResolvedHoverMap);
			populateExcelDataObject(requestTrackerId, excelData, node, totalCreatedIssues, closedIssuesWithStatus,
					totalCreatedIssuesAfter, storyList);

			log.debug("[CREATED-VS-RESOLVED-SPRINT-WISE][{}]. Created Vs Resolved for sprint {}  is {} - {}",
					requestTrackerId, node.getSprintFilter().getName(), createdVsResolvedDefectsMap.get(TAGGED_DEFECTS),
					createdVsResolvedDefectsMap.get(TAGGED_DEFECTS_CREATED_AFTER_SPRINT));

			Map<String, List<DataCount>> dataCountMap = new HashMap<>();
			createdVsResolvedDefectsMap.forEach((key, value) -> {
				DataCount dataCount = new DataCount();
				dataCount.setData(String.valueOf(value.get(CREATED_DEFECTS)));
				dataCount.setSProjectName(trendLineName);
				dataCount.setSSprintID(node.getSprintFilter().getId());
				dataCount.setSSprintName(node.getSprintFilter().getName());
				dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
				dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
				dataCount.setValue(value.get(CREATED_DEFECTS));
				dataCount.setKpiGroup(key);
				dataCount.setLineValue(value.get(RESOLVED_DEFECTS));
				dataCount.setHoverValue(getHoverMap(key, createdVsResolvedHoverMap));
				dataCountMap.put(key, new ArrayList<>(Arrays.asList(dataCount)));
			});

			mapTmp.get(node.getId()).setValue(dataCountMap);
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(
				KPIExcelColumn.CREATED_VS_RESOLVED_DEFECTS.getColumns(sprintLeafNodeList, cacheService, filterHelperService));
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData, Node node,
			List<JiraIssue> totalCreatedTickets, Map<String, String> closedIssuesWithStatus,
			List<JiraIssue> totalCreatedTicketsSprintStart, List<JiraIssue> storyList) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			Map<String, JiraIssue> createdTicketMap = new HashMap<>();
			totalCreatedTickets.forEach(jiraIssue -> createdTicketMap.putIfAbsent(jiraIssue.getNumber(), jiraIssue));
			KPIExcelUtility.populateCreatedVsResolvedExcelData(node.getSprintFilter().getName(), createdTicketMap,
					totalCreatedTicketsSprintStart, closedIssuesWithStatus, excelData, customApiConfig, storyList);
		}
	}

	/**
	 * Sets DB Query log
	 *
	 * @param storyFeatureList
	 */
	private void setDbQueryLogger(List<JiraIssue> storyFeatureList) {

		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.info(SEPARATOR_ASTERISK);
			log.info("************* Sprint Created Vs Resolved (dB) *******************");
			if (null != storyFeatureList && !storyFeatureList.isEmpty()) {
				List<String> storyIdList = storyFeatureList.stream().map(JiraIssue::getNumber).collect(Collectors.toList());
				log.info("Story[{}]: {}", storyIdList.size(), storyIdList);
			}
			log.info("******************X----X*******************");
		}
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	/**
	 * @param allSubTasks
	 * @param sprintDetails
	 * @param subTaskHistory
	 * @return sprint sub task bugs
	 */
	public List<JiraIssue> getTotalSubTasks(List<JiraIssue> allSubTasks, SprintDetails sprintDetails,
			List<JiraIssueCustomHistory> subTaskHistory) {
		LocalDateTime sprintEndDate = sprintDetails.getCompleteDate() != null
				? LocalDateTime.parse(sprintDetails.getCompleteDate().split("\\.")[0], DATE_TIME_FORMATTER)
				: LocalDateTime.parse(sprintDetails.getEndDate().split("\\.")[0], DATE_TIME_FORMATTER);
		LocalDateTime sprintStartDate = sprintDetails.getActivatedDate() != null
				? LocalDateTime.parse(sprintDetails.getActivatedDate().split("\\.")[0], DATE_TIME_FORMATTER)
				: LocalDateTime.parse(sprintDetails.getStartDate().split("\\.")[0], DATE_TIME_FORMATTER);
		FieldMapping fieldMapping = configHelperService.getFieldMapping(sprintDetails.getBasicProjectConfigId());
		List<JiraIssue> subTaskTaggedWithSprint = new ArrayList<>();

		allSubTasks.forEach(jiraIssue -> {
			JiraIssueCustomHistory jiraIssueCustomHistory = subTaskHistory.stream()
					.filter(issueCustomHistory -> issueCustomHistory.getStoryID().equalsIgnoreCase(jiraIssue.getNumber()))
					.findFirst().orElse(new JiraIssueCustomHistory());
			Optional<JiraHistoryChangeLog> jiraHistoryChangeLog = jiraIssueCustomHistory.getStatusUpdationLog().stream()
					.filter(changeLog -> fieldMapping.getJiraIssueDeliverdStatusKPI126().contains(changeLog.getChangedTo()) &&
							changeLog.getUpdatedOn().isAfter(sprintStartDate))
					.findFirst();
			if (jiraHistoryChangeLog.isPresent() &&
					sprintEndDate.isAfter(LocalDateTime.parse(jiraIssue.getCreatedDate().split("\\.")[0], DATE_TIME_FORMATTER)))
				subTaskTaggedWithSprint.add(jiraIssue);
		});
		return subTaskTaggedWithSprint;
	}

	private List<JiraIssue> getCompletedIssues(List<JiraIssue> sprintWiseDefects, SprintDetails sprintDetails) {
		FieldMapping fieldMapping = configHelperService.getFieldMapping(sprintDetails.getBasicProjectConfigId());
		if (CollectionUtils.isNotEmpty(fieldMapping.getJiraIssueDeliverdStatusKPI126()))
			return sprintWiseDefects.stream()
					.filter(jiraIssue -> fieldMapping.getJiraIssueDeliverdStatusKPI126().contains(jiraIssue.getStatus()))
					.collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	/**
	 * @param createdIssues
	 * @param resolvedIssues
	 * @param createdIssueSubSet
	 * @param createdVsResolvedHoverMap
	 * @return
	 */
	private Map<String, Map<String, Double>> createCreatedVsResolvedMap(List<JiraIssue> createdIssues,
			List<JiraIssue> resolvedIssues, List<JiraIssue> createdIssueSubSet,
			Map<String, Object> createdVsResolvedHoverMap) {

		Map<String, Map<String, Double>> createdVsResolvedViewMap = new HashMap<>();
		Map<String, Double> taggedDefectsMap = new HashMap<>();
		Map<String, Double> taggedCreatedDefectsMap = new HashMap<>();
		double createdIssueSize = createdIssues.size();
		double resolvedIssueSize = resolvedIssues.size();
		double createdIssueSubSetResolved = createdIssueSubSet.stream().filter(resolvedIssues::contains).count();
		taggedDefectsMap.put(CREATED_DEFECTS, createdIssueSize);
		taggedDefectsMap.put(RESOLVED_DEFECTS, resolvedIssueSize);
		createdVsResolvedViewMap.put(TAGGED_DEFECTS, taggedDefectsMap);
		createdIssueSize = createdIssueSubSet.size();
		taggedCreatedDefectsMap.put(CREATED_DEFECTS, createdIssueSize);
		taggedCreatedDefectsMap.put(RESOLVED_DEFECTS, createdIssueSubSetResolved);
		createdVsResolvedViewMap.put(TAGGED_DEFECTS_CREATED_AFTER_SPRINT, taggedCreatedDefectsMap);

		createdVsResolvedHoverMap.put(TAGGED_DEFECTS + CREATED_DEFECTS, createdIssues.size());
		createdVsResolvedHoverMap.put(TAGGED_DEFECTS + RESOLVED_DEFECTS, resolvedIssues.size());
		createdVsResolvedHoverMap.put(TAGGED_DEFECTS_CREATED_AFTER_SPRINT + CREATED_DEFECTS, createdIssueSubSet.size());
		createdVsResolvedHoverMap.put(TAGGED_DEFECTS_CREATED_AFTER_SPRINT + RESOLVED_DEFECTS, createdIssueSubSetResolved);
		return createdVsResolvedViewMap;
	}

	/**
	 * @param key
	 * @param createdVsResolvedHoverMap
	 * @return
	 */
	private Map<String, Object> getHoverMap(String key, Map<String, Object> createdVsResolvedHoverMap) {
		Map<String, Object> hoverMap = new LinkedHashMap<>();

		if (key.equalsIgnoreCase(TAGGED_DEFECTS)) {
			hoverMap.put(CREATED_DEFECTS, createdVsResolvedHoverMap.getOrDefault(TAGGED_DEFECTS + CREATED_DEFECTS, 0));
			hoverMap.put(RESOLVED_DEFECTS, createdVsResolvedHoverMap.getOrDefault(TAGGED_DEFECTS + RESOLVED_DEFECTS, 0));
		} else {
			hoverMap.put(CREATED_DEFECTS,
					createdVsResolvedHoverMap.getOrDefault(TAGGED_DEFECTS_CREATED_AFTER_SPRINT + CREATED_DEFECTS, 0));
			hoverMap.put(RESOLVED_DEFECTS,
					createdVsResolvedHoverMap.getOrDefault(TAGGED_DEFECTS_CREATED_AFTER_SPRINT + RESOLVED_DEFECTS, 0));
		}
		return hoverMap;
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI126(),
				KPICode.CREATED_VS_RESOLVED_DEFECTS.getKpiId());
	}
}
