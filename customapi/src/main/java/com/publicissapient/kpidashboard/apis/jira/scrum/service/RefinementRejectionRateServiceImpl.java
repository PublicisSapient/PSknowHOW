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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeatureHistory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.IssueBacklog;
import com.publicissapient.kpidashboard.common.model.jira.IssueBacklogCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.repository.jira.IssueBacklogCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.IssueBacklogRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This class calculates the Sprint capacity.
 *
 * @author pkum34
 */
@Component
@Slf4j
public class RefinementRejectionRateServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	public static final String UNCHECKED = "unchecked";
	private static final String UNASSIGNED_JIRA_ISSUE_HISTORY = "Unassigned Jira Issue History";
	private static final String UNASSIGNED_JIRA_ISSUE = "Unassigned Jira Issue";
	private static final String READY_FOR_REFINEMENT_ISSUE = "Ready For Refinement";
	private static final String ACCEPTED_IN_REFINEMENT_ISSUE = "Accepted In Refinement";
	private static final String REJECTED_IN_REFINEMENT_ISSUE = "Rejected In Refinement";
	private static final String ACCEPTED_IN_REFINEMENT_HOVER_VALUE = "Accepted Stories";
	private static final String REJECTED_IN_REFINEMENT_HOVER_VALUE = "Rejected Stories";
	private static final String TOTAL_STORIES = "Total Stories";
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Autowired
	private KpiHelperService kpiHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private IssueBacklogCustomHistoryRepository issueBacklogCustomHistoryRepository;

	@Autowired
	private IssueBacklogRepository issueBacklogRepository;

	/**
	 * @return String
	 */
	@Override
	public String getQualifierType() {
		return KPICode.REFINEMENT_REJECTION_RATE.name();
	}

	/**
	 * gets the KPI related data ana populate the same on the KPI element to display
	 * it on dashboard
	 *
	 * @param kpiRequest
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @return
	 * @throws ApplicationException
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<DataCount> trendValueList = new ArrayList<>();
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
			Filters filters = Filters.getFilter(k);
			if (Filters.PROJECT == filters) {
				projectWiseLeafNodeValue(v, trendValueList, kpiElement, kpiRequest, mapTmp);
			}
		});
		return kpiElement;
	}

	/**
	 * Function to fetch Un Assigned Jira issues and its history from db and
	 * filtering 'Ready For Refinement','Accepted In Refinement', 'Rejected In
	 * Refinement' stories
	 *
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @param kpiRequest
	 * @return
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		return getUnAssignedIssueDataMap(leafNodeList, startDate, endDate);

	}

	/**
	 * Not in Use
	 *
	 * @param sprintCapacityMap
	 *            type of db object
	 * @return
	 */
	@SuppressWarnings(UNCHECKED)
	@Override
	public Double calculateKPIMetrics(Map<String, Object> sprintCapacityMap) {
		return 0.0;
	}

	/**
	 * Prepare Data for Refinement Rejected Rate KPI
	 *
	 * @param trendValueList
	 * @param kpiElement
	 */
	private void projectWiseLeafNodeValue(List<Node> leafNode, List<DataCount> trendValueList, KpiElement kpiElement,
			KpiRequest kpiRequest, Map<String, Node> mapTmp) {

		CustomDateRange dateRange = KpiDataHelper.getDayForPastDataHistory(customApiConfig.getBacklogWeekCount() * 5);

		// get start and end date in yyyy-mm-dd format
		String startDate = dateRange.getStartDate().format(DATE_FORMATTER);
		String endDate = dateRange.getEndDate().format(DATE_FORMATTER);

		Map<String, Object> resultMap = fetchKPIDataFromDb(leafNode, startDate, endDate, kpiRequest);

		Map<String, String> weekMap = genrateWeekMap(startDate);

		List<IssueBacklog> readyForRefinementIssueBacklogs = new ArrayList<>();
		List<IssueBacklog> rejectedInRefinementIssueBacklogs = new ArrayList<>();
		List<IssueBacklog> acceptedInRefinementIssueBacklogs = new ArrayList<>();
		List<IssueBacklog> unAssignedIssueBacklogs = (List<IssueBacklog>) resultMap.get(UNASSIGNED_JIRA_ISSUE);
		List<IssueBacklogCustomHistory> issueBacklogCustomHistories = (List<IssueBacklogCustomHistory>) resultMap
				.get(UNASSIGNED_JIRA_ISSUE_HISTORY);

		List<KPIExcelData> excelData = new ArrayList<>();
		leafNode.forEach(node -> {

			Map<String, LocalDateTime> jiraDateMap = validateUnAssignedIssueBacklogs(unAssignedIssueBacklogs,
					readyForRefinementIssueBacklogs, acceptedInRefinementIssueBacklogs,
					rejectedInRefinementIssueBacklogs, issueBacklogCustomHistories,
					configHelperService.getFieldMappingMap().get(node.getProjectFilter().getBasicProjectConfigId()));

			Map<String, Object> defaultMap = new HashMap<>();
			defaultMap.put(READY_FOR_REFINEMENT_ISSUE, readyForRefinementIssueBacklogs);
			defaultMap.put(REJECTED_IN_REFINEMENT_ISSUE, rejectedInRefinementIssueBacklogs);
			defaultMap.put(ACCEPTED_IN_REFINEMENT_ISSUE, acceptedInRefinementIssueBacklogs);

			Map<String, List<Map<String, Object>>> projectWiseMap = getProjectWiseDataMap(node, defaultMap);

			String trendLineName = node.getProjectFilter().getName();
			Map<String, Map<String, List<IssueBacklog>>> weekAndTypeMap = populateWeekAndTypeMap(weekMap);
			List<DataCount> dataList = new ArrayList<>();
			List<IssueBacklog> issuesExcel = new ArrayList<>();
			if (null != projectWiseMap.get(node.getId()) && !rejectedInRefinementIssueBacklogs.isEmpty()
					&& !readyForRefinementIssueBacklogs.isEmpty() && !acceptedInRefinementIssueBacklogs.isEmpty()) {
				getWeekWiseRecord(projectWiseMap.get(node.getId()), weekAndTypeMap, weekMap, jiraDateMap);
				for (Map.Entry<String, Map<String, List<IssueBacklog>>> entry : weekAndTypeMap.entrySet()) {
					String week = entry.getKey();
					double accepted = weekAndTypeMap.get(week).get(ACCEPTED_IN_REFINEMENT_ISSUE).size();
					double rejected = weekAndTypeMap.get(week).get(REJECTED_IN_REFINEMENT_ISSUE).size();
					double total = rejected + accepted;
					double refinementRate = 0;
					if (accepted > 0 || rejected > 0 && total > 0) {
						refinementRate = (rejected / total) * 100;
					}
					Map<String, Object> hoverValue = new HashMap<>();
					populateTrendValueList(dataList, week, hoverValue, accepted, rejected, refinementRate, total,
							weekMap, trendLineName);
				}
				trendValueList.add(new DataCount(node.getProjectFilter().getName(), dataList));
			}
			weekAndTypeMap.keySet().stream().forEach(f -> weekAndTypeMap.get(f).keySet().stream()
					.forEach(issue -> issuesExcel.addAll(weekAndTypeMap.get(f).get(issue))));
			KPIExcelUtility.populateRefinementRejectionExcelData(excelData, issuesExcel, weekAndTypeMap, jiraDateMap);
			mapTmp.get(node.getId()).setValue(trendValueList);
			kpiElement.setTrendValueList(trendValueList);
		});

		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.REFINEMENT_REJECTION_RATE.getColumns());
	}

	/**
	 * This Method is used to Iterate over JIRA Issue History record and store the
	 * based on last updated Data
	 * 
	 * @param unAssignedIssueBacklogs
	 * @param readyForRefinementIssueBacklogs
	 * @param acceptedInRefinementIssueBacklogs
	 * @param rejectedInRefinementIssueBacklogs
	 * @param issueBacklogCustomHistories
	 * @param fieldMapping
	 * @return
	 */
	public Map<String, LocalDateTime> validateUnAssignedIssueBacklogs(List<IssueBacklog> unAssignedIssueBacklogs,
			List<IssueBacklog> readyForRefinementIssueBacklogs, List<IssueBacklog> acceptedInRefinementIssueBacklogs,
			List<IssueBacklog> rejectedInRefinementIssueBacklogs,
			List<IssueBacklogCustomHistory> issueBacklogCustomHistories, FieldMapping fieldMapping) {
		Map<String, LocalDateTime> jiraDateMap = new HashMap<>();
		for (IssueBacklogCustomHistory hist : issueBacklogCustomHistories) {
			List<IssueBacklog> issueBacklog = unAssignedIssueBacklogs.stream()
					.filter(f -> f.getNumber().equalsIgnoreCase(hist.getStoryID())).map(Function.identity())
					.collect(Collectors.toList());
			String status = getStatusAndUpdateJiraDateMap(fieldMapping, jiraDateMap, hist);
			if (status.equalsIgnoreCase(ACCEPTED_IN_REFINEMENT_ISSUE)) {
				acceptedInRefinementIssueBacklogs.addAll(issueBacklog);
			} else if (status.equalsIgnoreCase(REJECTED_IN_REFINEMENT_ISSUE)) {
				rejectedInRefinementIssueBacklogs.addAll(issueBacklog);
			} else if (status.equalsIgnoreCase(READY_FOR_REFINEMENT_ISSUE)) {
				readyForRefinementIssueBacklogs.addAll(issueBacklog);
			}
		}
		return jiraDateMap;
	}

	/**
	 * This Method is used to fetch the latest Status from Ready, Accepted &
	 * Rejected and Activity Date from history and store it in a map
	 * 
	 * @param fieldMapping
	 * @param jiraDateMap
	 * @param hist
	 * @return
	 */
	private String getStatusAndUpdateJiraDateMap(FieldMapping fieldMapping, Map<String, LocalDateTime> jiraDateMap,
			IssueBacklogCustomHistory hist) {
		String status = "";
		String fromStatus = "";
		LocalDateTime changeDate = LocalDateTime.now();
		int count = 0;
		for (JiraHistoryChangeLog story : hist.getStatusUpdationLog()) {
			if (count == 0) {
				changeDate = story.getUpdatedOn();
			} else {
				fromStatus = story.getChangedTo();
				if (CollectionUtils.isNotEmpty(fieldMapping.getJiraReadyForRefinement())
						&& fieldMapping.getJiraReadyForRefinement().contains(fromStatus)) {
					status = READY_FOR_REFINEMENT_ISSUE;
					changeDate = story.getUpdatedOn();
				} else if (CollectionUtils.isNotEmpty(fieldMapping.getJiraAcceptedInRefinement())
						&& fieldMapping.getJiraAcceptedInRefinement().contains(fromStatus)) {
					status = ACCEPTED_IN_REFINEMENT_ISSUE;
					changeDate = story.getUpdatedOn();
				} else if (CollectionUtils.isNotEmpty(fieldMapping.getJiraRejectedInRefinement())
						&& fieldMapping.getJiraRejectedInRefinement().contains(fromStatus)) {
					status = REJECTED_IN_REFINEMENT_ISSUE;
					changeDate = story.getUpdatedOn();
				}
			}
			count++;
		}

		jiraDateMap.put(hist.getStoryID(), changeDate);
		return status;
	}

	/**
	 * Create Week Wise map based on the end date(current Date)
	 * 
	 * @param endDate
	 * @return
	 */
	private Map<String, String> genrateWeekMap(String endDate) {
		Map<String, String> weekMap = new LinkedHashMap<>();
		int weekCount = (customApiConfig.getBacklogWeekCount());
		LocalDate currentDate = LocalDate.parse(endDate);
		for (int i = weekCount; i > 0; i--) {
			LocalDate monday = currentDate.with(DayOfWeek.MONDAY);
			LocalDate sunday = currentDate.with(DayOfWeek.SUNDAY);
			String weekName = "Week" + (i);
			String dateRange = DateUtil.dateTimeConverter(monday.toString(), DateUtil.DATE_FORMAT,
					DateUtil.DISPLAY_DATE_FORMAT) + " to "
					+ DateUtil.dateTimeConverter(sunday.toString(), DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT);
			currentDate = sunday.plusDays(1);
			weekMap.put(weekName, dateRange);
		}

		return weekMap;
	}

	/**
	 * Populate Trend Data Value to trendValueList
	 *
	 * @param dataList
	 * @param week
	 * @param hoverValue
	 * @param accepted
	 * @param rejected
	 * @param refinementRate
	 * @param total
	 */
	private void populateTrendValueList(List<DataCount> dataList, String week, Map<String, Object> hoverValue,
			double accepted, double rejected, double refinementRate, double total, Map<String, String> weekMap,
			String trendLineName) {
		hoverValue.put(ACCEPTED_IN_REFINEMENT_HOVER_VALUE, accepted);
		hoverValue.put(TOTAL_STORIES, total);
		hoverValue.put(REJECTED_IN_REFINEMENT_HOVER_VALUE, rejected);
		DataCount dataCount = new DataCount();
		dataCount.setSSprintName(week + "(" + weekMap.get(week) + ")");
		dataCount.setSProjectName(trendLineName);
		dataCount.setValue(refinementRate);
		dataCount.setHoverValue(hoverValue);
		dataList.add(dataCount);
	}

	/**
	 * Create Default Week map for last 45 Days
	 *
	 * @return
	 */
	private Map<String, Map<String, List<IssueBacklog>>> populateWeekAndTypeMap(Map<String, String> weekMap) {

		Map<String, Map<String, List<IssueBacklog>>> dateMap = new LinkedHashMap<>();
		for (String week : weekMap.keySet()) {
			Map<String, List<IssueBacklog>> statusDataMap = new HashMap<>();
			statusDataMap.put(READY_FOR_REFINEMENT_ISSUE, new ArrayList<>());
			statusDataMap.put(ACCEPTED_IN_REFINEMENT_ISSUE, new ArrayList<>());
			statusDataMap.put(REJECTED_IN_REFINEMENT_ISSUE, new ArrayList<>());
			dateMap.put(week, statusDataMap);
		}
		return dateMap;
	}

	/**
	 * Creates a Week wise map to store jira issues datamap
	 *
	 * @param resultMapList
	 * @return
	 */
	private void getWeekWiseRecord(List<Map<String, Object>> resultMapList,
			Map<String, Map<String, List<IssueBacklog>>> dataMap, Map<String, String> weekMap,
			Map<String, LocalDateTime> jiraDateMap) {
		resultMapList.stream().forEach(
				f -> f.keySet().stream().forEach(sub -> ((List<IssueBacklog>) f.get(sub)).stream().forEach(issue -> {
					LocalDate jiraDate = null;
					if (null != jiraDateMap.get(issue.getNumber())) {
						jiraDate = jiraDateMap.get(issue.getNumber()).toLocalDate();
					}
					genrateWeekAndPopulateJiraDateMap(dataMap, weekMap, sub, issue, jiraDate);

				})));
	}

	private void genrateWeekAndPopulateJiraDateMap(Map<String, Map<String, List<IssueBacklog>>> dataMap,
			Map<String, String> weekMap, String sub, IssueBacklog issue, LocalDate jiraDate) {
		if (null != jiraDate) {
			LocalDate monday = jiraDate.with(DayOfWeek.MONDAY);
			LocalDate sunday = jiraDate.with(DayOfWeek.SUNDAY);
			String value = DateUtil.dateTimeConverter(monday.toString(), DateUtil.DATE_FORMAT,
					DateUtil.DISPLAY_DATE_FORMAT) + " to "
					+ DateUtil.dateTimeConverter(sunday.toString(), DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT);
			String weekVal = "";
			for (String week : weekMap.keySet()) {
				if (weekMap.get(week).equalsIgnoreCase(value)) {
					weekVal = week;
					break;
				}
			}
			if (null != weekVal && !weekVal.isEmpty()) {
				dataMap.get(weekVal).get(sub).add(issue);
			}
		}
	}

	/**
	 * Not In Use
	 *
	 * @param valueList
	 *            values
	 * @param kpiName
	 *            kpiName
	 * @return
	 */
	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	/**
	 * This method help create Jira issue on category basis for project ID
	 * 
	 * @param node
	 * @param resultMap
	 * @return
	 */
	private Map<String, List<Map<String, Object>>> getProjectWiseDataMap(Node node, Map<String, Object> resultMap) {
		Map<String, List<Map<String, Object>>> dataMap = new HashMap<>();
		for (String map : resultMap.keySet()) {
			List<IssueBacklog> dataList = ((List<IssueBacklog>) resultMap.get(map)).stream()
					.filter(f -> f.getProjectID().equalsIgnoreCase(node.getProjectFilter().getId()))
					.collect(Collectors.toList());
			Map<String, Object> subMap = new HashMap<>();
			subMap.put(map, dataList);
			if (null == dataMap.get(node.getId())) {
				dataMap.put(node.getId(), new ArrayList<>());
			}
			dataMap.get(node.getId()).add(subMap);
		}
		return dataMap;
	}

	/**
	 * This method is used to fetch Un-assigned Jira issues and its history details
	 *
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public Map<String, Object> getUnAssignedIssueDataMap(List<Node> leafNodeList, String startDate, String endDate) {
		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		List<String> projectList = new ArrayList<>();

		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			projectList.add(basicProjectConfigId.toString());
			mapOfFilters.put(JiraFeatureHistory.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
					projectList.stream().distinct().collect(Collectors.toList()));
		});

		List<IssueBacklog> unAssignedIssueBacklogs = new ArrayList<>();
		unAssignedIssueBacklogs.addAll(issueBacklogRepository.findUnassignedIssues(startDate, endDate, mapOfFilters));
		List<String> historyData = unAssignedIssueBacklogs.stream().map(IssueBacklog::getNumber)
				.collect(Collectors.toList());
		List<IssueBacklogCustomHistory> issueBacklogCustomHistories = new ArrayList<>();
		issueBacklogCustomHistories.addAll(
				issueBacklogCustomHistoryRepository.findByStoryIDInAndBasicProjectConfigIdIn(historyData, projectList));

		resultListMap.put(UNASSIGNED_JIRA_ISSUE, unAssignedIssueBacklogs);
		resultListMap.put(UNASSIGNED_JIRA_ISSUE_HISTORY, issueBacklogCustomHistories);
		return resultListMap;
	}
}
