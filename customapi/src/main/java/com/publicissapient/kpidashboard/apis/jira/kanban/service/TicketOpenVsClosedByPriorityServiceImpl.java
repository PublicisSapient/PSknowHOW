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

package com.publicissapient.kpidashboard.apis.jira.kanban.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.JiraFeatureHistory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
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
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TicketOpenVsClosedByPriorityServiceImpl extends JiraKPIService<Long, List<Object>, Map<String, Object>> {

	private static final String SUBGROUPCATEGORY = "subGroupCategory";
	private static final String RANGE = "range";
	private static final String DEV = "DeveloperKpi";
	private static final String OPENED_TICKET = "Open tickets";
	private static final String CLOSED_TICKET = "Closed tickets";
	private static final String PROJECT_WISE_ISSUETYPES = "projectWiseIssueTypes";
	private static final String PROJECT_WISE_CLOSED_STORY_STATUS = "projectWiseClosedStoryStatus";
	private static final String IN = "in";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	@Autowired
	private KanbanJiraIssueRepository kanbanJiraIssueRepository;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private KanbanJiraIssueHistoryRepository kanbanJiraIssueHistoryRepository;
	@Autowired
	private FilterHelperService flterHelperService;

	@Override
	public String getQualifierType() {
		return KPICode.TICKET_OPEN_VS_CLOSE_BY_PRIORITY.name();
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
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		log.info("[TICKET OPEN VS CLOSED RATE BY PRIORITY-KANBAN-LEAF-NODE-VALUE][{}]",
				kpiRequest.getRequestTrackerId());
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes()
				.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);

		dateWiseLeafNodeValue(mapTmp, projectList, kpiElement, kpiRequest);

		log.debug(
				"[TICKET OPEN VS CLOSED RATE BY PRIORITY-KANBAN-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.TICKET_OPEN_VS_CLOSE_BY_PRIORITY);
		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.TICKET_OPEN_VS_CLOSE_BY_PRIORITY);

		trendValuesMap = sortTrendValueMap(trendValuesMap, priorityTypes(true));
		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		trendValuesMap.forEach((key, datewiseDataCount) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			dataCountGroup.setFilter(key);
			dataCountGroup.setValue(datewiseDataCount);
			dataCountGroups.add(dataCountGroup);
		});

		kpiElement.setTrendValueList(dataCountGroups);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);
		return kpiElement;
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiId) {
		return calculateKpiValueForLong(valueList, kpiId);
	}

	/**
	 * Calculates KPI Metrics
	 * 
	 * @param subCategoryMap
	 * @return Integer
	 */
	@Override
	public Long calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return 0L;
	}

	/**
	 * Fetches KPI Data From Database
	 *
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @return resultListMap
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();
		List<String> projectList = new ArrayList<>();
		Map<String, Map<String, Object>> closedStatusProjectMap = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, List<String>> projectWiseIssueTypeMap = new HashMap<>();
		Map<String, List<String>> projectWiseClosedStatusMap = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> closedStatusFilters = new LinkedHashMap<>();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			projectList.add(basicProjectConfigId.toString());

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			if (Optional.ofNullable(fieldMapping.getTicketCountIssueType()).isPresent()) {
				mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(fieldMapping.getTicketCountIssueType()));

				projectWiseIssueTypeMap.put(basicProjectConfigId.toString(),
						fieldMapping.getTicketCountIssueType().stream().distinct().collect(Collectors.toList()));
			}
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
			if (Optional.ofNullable(fieldMapping.getJiraTicketClosedStatus()).isPresent()) {
				closedStatusFilters.put(JiraFeatureHistory.HISTORY_STATUS.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(fieldMapping.getJiraTicketClosedStatus()));

				closedStatusFilters.put(JiraFeatureHistory.STORY_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(fieldMapping.getTicketCountIssueType()));

				closedStatusProjectMap.put(basicProjectConfigId.toString(), closedStatusFilters);

				projectWiseClosedStatusMap.put(basicProjectConfigId.toString(),
						fieldMapping.getJiraTicketClosedStatus().stream().distinct().collect(Collectors.toList()));
			}
		});

		/** additional filter **/
		String subGroupCategory = KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.KANBAN,
				DEV, flterHelperService);
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				projectList.stream().distinct().collect(Collectors.toList()));

		resultListMap.put(OPENED_TICKET, kanbanJiraIssueRepository.findIssuesByDateAndType(mapOfFilters,
				uniqueProjectMap, startDate, endDate, RANGE));

		mapOfFilters.put(JiraFeatureHistory.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				mapOfFilters.get(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature()));

		resultListMap.put(CLOSED_TICKET, kanbanJiraIssueHistoryRepository.findIssuesByStatusAndDate(mapOfFilters,
				closedStatusProjectMap, startDate, endDate, IN));

		resultListMap.put(SUBGROUPCATEGORY, subGroupCategory);
		resultListMap.put(PROJECT_WISE_ISSUETYPES, projectWiseIssueTypeMap);
		resultListMap.put(PROJECT_WISE_CLOSED_STORY_STATUS, projectWiseClosedStatusMap);

		return resultListMap;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint wise.
	 * 
	 * @param mapTmp
	 * @param leafNodeList
	 * @param kpiElement
	 * @param kpiRequest
	 *
	 */
	private void dateWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		CustomDateRange dateRange = KpiDataHelper.getStartAndEndDate(kpiRequest);

		String startDate = dateRange.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String endDate = dateRange.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, startDate, endDate, kpiRequest);

		kpiWithFilter(resultMap, mapTmp, leafNodeList, kpiElement, kpiRequest);
	}

	private void kpiWithFilter(Map<String, Object> resultMap, Map<String, Node> mapTmp, List<Node> leafNodeList,
			KpiElement kpiElement, KpiRequest kpiRequest) {
		List<KPIExcelData> excelData = new ArrayList<>();
		String requestTrackerId = getKanbanRequestTrackerId();
		String subGroupCategory = (String) resultMap.get(SUBGROUPCATEGORY);
		Map<String, List<KanbanJiraIssue>> projectWiseOpenedJiraIssue = KpiDataHelper.createProjectWiseMapKanban(
				(List<KanbanJiraIssue>) resultMap.get(OPENED_TICKET), subGroupCategory, flterHelperService);

		Map<String, List<KanbanIssueCustomHistory>> projectWiseClosedJiraIssue = KpiDataHelper
				.createProjectWiseMapKanbanHistory((List<KanbanIssueCustomHistory>) resultMap.get(CLOSED_TICKET),
						subGroupCategory, flterHelperService);

		Map<String, List<String>> projectWiseClosedStatusMap = (Map<String, List<String>>) resultMap
				.get(PROJECT_WISE_CLOSED_STORY_STATUS);
		leafNodeList.forEach(node -> {
			String projectNodeId = node.getProjectFilter().getBasicProjectConfigId().toString();
			List<KanbanJiraIssue> kanbanOpenedIssueList = projectWiseOpenedJiraIssue.getOrDefault(projectNodeId,
					new ArrayList<>());
			List<KanbanIssueCustomHistory> kanbanClosedIssueList = projectWiseClosedJiraIssue
					.getOrDefault(projectNodeId, new ArrayList<>());
			if (CollectionUtils.isNotEmpty(kanbanOpenedIssueList)
					|| CollectionUtils.isNotEmpty(kanbanClosedIssueList)) {

				Map<String, List<DataCount>> projectFilterWiseDataMap = new HashMap<>();
				List<String> issueClosedStatusList = projectWiseClosedStatusMap.get(projectNodeId);
				LocalDate currentDate = LocalDate.now();
				for (int i = 0; i < kpiRequest.getKanbanXaxisDataPoints(); i++) {
					List<KanbanJiraIssue> dateWiseIssueTypeList = new ArrayList<>();
					List<KanbanIssueCustomHistory> dateWiseIssueClosedStatusList = new ArrayList<>();

					CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate,
							kpiRequest.getDuration());

					List<String> priorityList = priorityTypes(false);
					Map<String, Long> openedIssueCountMap = filterKanbanDataBasedOnStartAndEndDateAndIssueType(
							kanbanOpenedIssueList, priorityList, dateRange.getStartDate(), dateRange.getEndDate(),
							dateWiseIssueTypeList);

					Map<String, Long> closedIssueCountMap = filterKanbanHistoryDataBasedOnStartAndEndDateAndIssueType(
							kanbanClosedIssueList, priorityList, issueClosedStatusList, dateRange.getStartDate(),
							dateRange.getEndDate(), dateWiseIssueClosedStatusList);

					String date = getRange(dateRange, kpiRequest);

					populateProjectFilterWiseDataMap(openedIssueCountMap, closedIssueCountMap, projectFilterWiseDataMap,
							node.getProjectFilter().getId(), date);

					if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
						currentDate = currentDate.minusWeeks(1);
					} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
						currentDate = currentDate.minusMonths(1);
					} else {
						currentDate = currentDate.minusDays(1);
					}
					populateExcelDataObject(requestTrackerId, dateWiseIssueTypeList, dateWiseIssueClosedStatusList,
							date, node.getProjectFilter().getName(), excelData);

				}
				mapTmp.get(node.getId()).setValue(projectFilterWiseDataMap);
			}
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.TICKET_OPEN_VS_CLOSE_BY_PRIORITY.getColumns());

	}

	private String getRange(CustomDateRange dateRange, KpiRequest kpiRequest) {
		String range = null;
		if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
			range = DateUtil.dateTimeConverter(dateRange.getStartDate().toString(), DateUtil.DATE_FORMAT,
					DateUtil.DISPLAY_DATE_FORMAT) + " to "
					+ DateUtil.dateTimeConverter(dateRange.getEndDate().toString(), DateUtil.DATE_FORMAT,
							DateUtil.DISPLAY_DATE_FORMAT);
		} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
			range = dateRange.getStartDate().getMonth().toString();
		} else {
			range = dateRange.getStartDate().toString();
		}
		return range;
	}

	private void populateProjectFilterWiseDataMap(Map<String, Long> openedIssueCountMap,
			Map<String, Long> closedIssueCountMap, Map<String, List<DataCount>> projectFilterWiseDataMap,
			String projectNodeId, String date) {
		String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));

		openedIssueCountMap.forEach((key, value) -> {
			DataCount dcObj = getDataCountObject(value, closedIssueCountMap.getOrDefault(key, 0L), projectName, date,
					projectNodeId);
			projectFilterWiseDataMap.computeIfAbsent(key, k -> new ArrayList<>()).add(dcObj);
		});

		Long aggColumnValue = openedIssueCountMap.values().stream().mapToLong(p -> p).sum();
		Long aggLineValue = closedIssueCountMap.values().stream().mapToLong(p -> p).sum();

		projectFilterWiseDataMap.computeIfAbsent(CommonConstant.OVERALL, k -> new ArrayList<>())
				.add(getDataCountObject(aggColumnValue, aggLineValue, projectName, date, projectNodeId));
	}

	private DataCount getDataCountObject(Long value, Long lineValue, String projectName, String date,
			String projectNodeId) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSProjectName(projectName);
		dataCount.setDate(date);
		Map<String, Object> howerMap = new HashMap<>();
		howerMap.put(OPENED_TICKET, value.intValue());
		howerMap.put(CLOSED_TICKET, lineValue.intValue());
		dataCount.setHoverValue(howerMap);
		dataCount.setSprintIds(new ArrayList<>(Arrays.asList(projectNodeId)));
		dataCount.setSprintNames(new ArrayList<>(Arrays.asList(projectName)));
		dataCount.setValue(value);
		dataCount.setLineValue(lineValue);
		return dataCount;
	}

	private void populateExcelDataObject(String requestTrackerId, List<KanbanJiraIssue> dateWiseIssueTypeList,
			List<KanbanIssueCustomHistory> dateWiseIssueClosedStatusList, String dateProjectKey, String projectName,
			List<KPIExcelData> excelData) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& CollectionUtils.isNotEmpty(dateWiseIssueTypeList)) {
			KPIExcelUtility.populateOpenVsClosedExcelData(dateProjectKey, projectName, dateWiseIssueTypeList,
					dateWiseIssueClosedStatusList, excelData, KPICode.TICKET_OPEN_VS_CLOSE_BY_PRIORITY.getKpiId());

		}

	}

	public Map<String, Long> filterKanbanDataBasedOnStartAndEndDateAndIssueType(List<KanbanJiraIssue> issueList,
			List<String> priorityList, LocalDate startDate, LocalDate endDate,
			List<KanbanJiraIssue> dateWiseIssueTypeList) {
		Predicate<KanbanJiraIssue> predicate = issue -> LocalDateTime
				.parse(issue.getCreatedDate().split("\\.")[0], DATE_TIME_FORMATTER).isAfter(startDate.atTime(0, 0, 0))
				&& LocalDateTime.parse(issue.getCreatedDate().split("\\.")[0], DATE_TIME_FORMATTER)
						.isBefore(endDate.atTime(23, 59, 59));
		List<KanbanJiraIssue> filteredIssue = issueList.stream().filter(predicate).collect(Collectors.toList());
		Map<String, Long> projectIssueTypeMap = KPIHelperUtil.setpriorityKanban(filteredIssue, customApiConfig);
		// adding missing priority

		priorityList.forEach(priority -> projectIssueTypeMap.computeIfAbsent(priority, val -> 0L));
		dateWiseIssueTypeList.addAll(filteredIssue);
		return projectIssueTypeMap;
	}

	public Map<String, Long> filterKanbanHistoryDataBasedOnStartAndEndDateAndIssueType(
			List<KanbanIssueCustomHistory> issueList, List<String> priorityList, List<String> issueClosedStatusList,
			LocalDate startDate, LocalDate endDate, List<KanbanIssueCustomHistory> dateWiseIssueClosedStatusList) {
		Predicate<KanbanIssueHistory> predicate = issue -> issueClosedStatusList.contains(issue.getStatus())
				&& LocalDateTime.parse(issue.getActivityDate().split("\\.")[0], DATE_TIME_FORMATTER)
						.isAfter(startDate.atTime(0, 0, 0))
				&& LocalDateTime.parse(issue.getActivityDate().split("\\.")[0], DATE_TIME_FORMATTER)
						.isBefore(endDate.atTime(23, 59, 59));

		List<KanbanIssueCustomHistory> filteredIssue = new ArrayList<>();
		issueList.stream().forEach(issue -> {
			if (issue.getHistoryDetails().stream().anyMatch(predicate)) {
				filteredIssue.add(issue);
			}
		});
		Map<String, Long> projectIssueTypeMap = KPIHelperUtil.setpriorityKanbanHistory(filteredIssue, customApiConfig);

		// adding missing priority
		priorityList.forEach(priority -> projectIssueTypeMap.computeIfAbsent(priority, val -> 0L));

		dateWiseIssueClosedStatusList.addAll(filteredIssue);
		return projectIssueTypeMap;
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
