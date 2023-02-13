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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
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
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueSprint;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

/**
 * This class fetches the daily closure on Iteration dashboard. Trend analysis
 * for Daily Closure KPI has total closed defect count at y-axis and day at
 * x-axis. {@link JiraKPIService}
 *
 * @author tauakram
 */
@Component
@Slf4j
public class DailyClosureServiceImpl extends JiraKPIService<Map<String, Long>, List<Object>, Map<String, Object>> {
	private static final String ISSUES = "issues";
	private static final String SPRINT = "sprint";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	private static final DateTimeFormatter YYYY_MM_DD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static final String UNCHECKED = "unchecked";

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueHistoryRepository;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getQualifierType() {
		return KPICode.DAILY_CLOSURES.name();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		List<DataCount> trendValueList = new ArrayList<>();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

			if (Filters.getFilter(k) == Filters.SPRINT) {
				sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);
			}

		});

		kpiElement.setTrendValueList(trendValueList);

		return kpiElement;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Long> calculateKPIMetrics(Map<String, Object> objectMap) {
		return new HashMap<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(final List<Node> leafNodeList, final String startDate,
			final String endDate, final KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			log.info("Daily Closure -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId().toString();
			String sprintId = leafNode.getSprintFilter().getId();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(leafNode.getProjectFilter().getBasicProjectConfigId());
			SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintId);
			if (null != sprintDetails && CollectionUtils.isNotEmpty(fieldMapping.getJiraDod())) {
				List<String> completedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.COMPLETED_ISSUES);
				if (CollectionUtils.isNotEmpty(completedIssues)) {
					List<JiraIssue> issueList = jiraIssueRepository
							.findByNumberInAndBasicProjectConfigId(completedIssues, basicProjectConfigId);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getTotalIssues(), issueList);

					List<JiraIssueCustomHistory> completedJiraIssuesHistory = jiraIssueHistoryRepository
							.findByStoryIDInAndBasicProjectConfigIdIn(completedIssues,
									Arrays.asList(basicProjectConfigId));
					Map<String, String> activityMap = getClosedDate(completedJiraIssuesHistory, sprintDetails);
					filtersIssuesList.forEach(issue -> issue
							.setUpdateDate(activityMap.getOrDefault(issue.getNumber(), issue.getUpdateDate())));

					resultListMap.put(ISSUES, new ArrayList<>(filtersIssuesList));
					resultListMap.put(SPRINT, sprintDetails);
				}
			}
		}
		return resultListMap;
	}

	private Map<String, String> getClosedDate(List<JiraIssueCustomHistory> completedJiraIssuesHistory,
			SprintDetails sprintDetails) {
		Map<String, String> closedDateMap = new HashMap<>();
		completedJiraIssuesHistory.stream().forEach(jiraIssueCustomHistory -> {
			List<JiraIssueSprint> storySprintDetail = jiraIssueCustomHistory.getStorySprintDetails();
			SprintIssue sprintIssue = sprintDetails.getCompletedIssues().stream()
					.filter(s -> s.getNumber().equals(jiraIssueCustomHistory.getStoryID())).findFirst().get();
			if (CollectionUtils.isNotEmpty(storySprintDetail)) {
				for (int i = storySprintDetail.size() - 1; i >= 0; i--) {
					if (storySprintDetail.get(i).getFromStatus().equalsIgnoreCase(sprintIssue.getStatus())) {
						DateTime dateValue = DateTime.parse(storySprintDetail.get(i).getActivityDate().toString());
						DateTime startDateValue = DateTime.parse(sprintDetails.getStartDate());
						DateTime endDateValue = DateTime.parse(sprintDetails.getEndDate());
						if (dateValue.isAfter(startDateValue) && dateValue.isBefore(endDateValue)) {
							closedDateMap.put(jiraIssueCustomHistory.getStoryID(),
									getFormattedDate(storySprintDetail.get(i).getActivityDate()));
							break;
						}
					}
				}
			}
		});
		return closedDateMap;
	}

	/**
	 *
	 * @param dateTime
	 *            dateTime
	 * @return
	 */
	private static String getFormattedDate(DateTime dateTime) {
		if (dateTime != null) {
			try {
				return ISODateTimeFormat.dateHourMinuteSecondMillis().print(dateTime) + "0000";
			} catch (IllegalArgumentException e) {
				log.error("error while parsing date: {} {}", dateTime, e);
			}
		}

		return "";
	}

	/**
	 * This method populates KPI value to sprint leaf nodes. It also gives the trend
	 * analysis at sprint wise.
	 *
	 * @param mapTmp
	 *            node id map
	 * @param sprintLeafNodeList
	 *            sprint nodes list
	 * @param trendValueList
	 *            list to hold trend nodes data
	 * @param kpiElement
	 *            the KpiElement
	 * @param kpiRequest
	 *            the KpiRequest
	 */
	@SuppressWarnings(UNCHECKED)
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelDataList = new ArrayList<>();
		List<JiraIssue> issuesExcel = new ArrayList<>();

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);
		Object basicProjectConfigId = latestSprint.getProjectFilter().getBasicProjectConfigId();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT);
		if (CollectionUtils.isNotEmpty(allIssues)) {
			log.info("Daily Closures -> request id : {} total jira Issues : {}", requestTrackerId, allIssues.size());

			Map<String, Map<String, List<JiraIssue>>> dateAndtypeWiseIssues = allIssues.stream()
					.filter(f -> StringUtils.isNotBlank(f.getUpdateDate()))
					.collect(Collectors.groupingBy(
							f -> LocalDate.parse(f.getUpdateDate().split("\\.")[0], DATE_TIME_FORMATTER).toString(),
							Collectors.groupingBy(JiraIssue::getTypeName)));

			LocalDate end = LocalDate.parse(sprintDetails.getEndDate().split("\\.")[0], DATE_TIME_FORMATTER);
			LocalDate start = LocalDate.parse(sprintDetails.getStartDate().split("\\.")[0], DATE_TIME_FORMATTER)
					.minusDays(1);
			Map<String, DataCount> dateWiseDataCount = new LinkedHashMap<>();
			for (LocalDate date = end; date.isAfter(start); date = date.minusDays(1)) {
				dateWiseDataCount.put(date.format(YYYY_MM_DD_FORMATTER), new DataCount());
			}
			List<DataCount> data = new ArrayList<>();
			dateWiseDataCount.forEach((date, dataCount) -> {
				Map<String, Integer> value = new HashMap<>();
				if (null != dateAndtypeWiseIssues.get(date)) {
					Map<String, List<JiraIssue>> typeWiseMap = dateAndtypeWiseIssues.get(date);
					typeWiseMap.forEach((type, issues) -> {
						value.put(type, issues.size());
						issuesExcel.addAll(issues);
					});
				}
				dataCount.setValue(value);
				dataCount.setSProjectName(latestSprint.getProjectFilter().getName());
				dataCount.setSSprintID(latestSprint.getSprintFilter().getId());
				dataCount.setSSprintName(date);
				dataCount.setHoverValue(new HashMap<>());
				data.add(dataCount);
			});
			trendValueList.add(new DataCount(latestSprint.getProjectFilter().getName(), Lists.reverse(data)));
			if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
				KPIExcelUtility.populateDailyClosureExcelData(excelDataList, issuesExcel, fieldMapping);
			}
		}
		kpiElement.setExcelData(excelDataList);
		kpiElement.setExcelColumns(KPIExcelColumn.DAILY_CLOSURES.getColumns());
	}

	@Override
	public Map<String, Long> calculateKpiValue(List<Map<String, Long>> valueList, String kpiName) {
		return calculateKpiValueForMap(valueList, kpiName);
	}
}
