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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFilters;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFiltersOptions;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.jira.IterationStatus;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This class fetches the daily closure on Iteration dashboard. Trend analysis
 * for Daily Closure KPI has total closed defect count at y-axis and day at
 * x-axis. {@link JiraKPIService}
 *
 * @author Lakshmi Singh
 */
@Component
@Slf4j
public class IterationStatusServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";

	private static final String OPEN_ISSUES = "openIssuesCausingDelay";
	private static final String DELAY_DETAILS = "delayDetails";
	private static final String SEARCH_BY_PRIORITY = "Filter by priority";
	private static final String PARSE_EXCEPTION = "Exception while parse date...";
	private static final String TOTALISSUES = "totalIssues";
	private static final String SPRINT = "sprint";
	private static final String JIRAISSUEMAP = "jiraIssueMap";
	private static final String JIRAOPENISSUEMAP = "jiraOpenIssueMap";
	private static final String COMPLETED_ISSUES = "completedIssues";
	private static final String ISSUES_CAUSING_DELAY = "Delayed";
	private static final String DAYS = "Days";
	private static final String NET_DELAYED_ISSUES = "Net Delay";
	private static final String LABELINFO = "(Issues Count)";
	private static final String ISSUES_DONE_BEFORE_TIME = "Done Before Time";
	private static final String NOT_COMPLETED_ISSUES = "notCompletedIssues";
	private static final String JIRAISSUECUSTOMHISTORYMAP = "jiraIssueCustomHistoryMap";
	private static final String JIRAOPENISSUECUSTOMHISTORYMAP = "jiraOpenIssueCustomHistoryMap";
	private static final String OVERALL = "Overall";
	private static final String DATE_FORMAT = "yyyy-MM-dd";

	private static Integer getRemainingEstimateTime(JiraIssue issueObject) {
		Integer remainingEstimate = 0;
		if (issueObject.getRemainingEstimateMinutes() != null) {
			remainingEstimate = (issueObject.getRemainingEstimateMinutes() / 60) / 8;
		}
		return remainingEstimate;
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		DataCount trendValue = new DataCount();
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

			Filters filters = Filters.getFilter(k);
			if (Filters.SPRINT == filters) {
				try {
					projectWiseLeafNodeValue(v, trendValue, kpiElement, kpiRequest);
				} catch (ParseException e) {
					log.error(PARSE_EXCEPTION + e.getMessage());
				}
			}
		});
		return kpiElement;
	}

	@Override
	public String getQualifierType() {
		return KPICode.ITERATION_STATUS.name();
	}

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			log.info("Iteration Status -> Requested sprint : {}", leafNode.getName());
			SprintDetails sprintDetails = getSprintDetailsFromBaseClass();
			if (null != sprintDetails) {

				Set<SprintIssue> compIssues = sprintDetails.getCompletedIssues();

				Set<SprintIssue> nonCompIssues = sprintDetails.getNotCompletedIssues();

				List<String> completedIssues = compIssues.stream().map(SprintIssue::getNumber)
						.collect(Collectors.toList());

				List<String> issuesNotCompleted = nonCompIssues.stream().map(SprintIssue::getNumber)
						.collect(Collectors.toList());

				List<String> issuesList = new ArrayList<>();
				issuesList.addAll(completedIssues);
				issuesList.addAll(issuesNotCompleted);

				List<JiraIssue> totalJiraIssues = getJiraIssuesFromBaseClass(issuesList);

				List<JiraIssueCustomHistory> totalJiraIssuesHistory = getJiraIssuesCustomHistoryFromBaseClass(
						issuesList);

				Map<String, JiraIssue> jiraOpenIssueMap;
				Map<String, JiraIssue> jiraIssueMap = new HashMap<>();
				Map<String, JiraIssueCustomHistory> jiraOpenIssueCustomHistoryMap = new HashMap<>();
				Map<String, JiraIssueCustomHistory> jiraIssueCustomHistoryMap = new HashMap<>();

				jiraOpenIssueMap = totalJiraIssues.stream()
						.collect(Collectors.toMap(JiraIssue::getNumber, Function.identity()));

				if (CollectionUtils.isNotEmpty(nonCompIssues)) {
					jiraOpenIssueMap = jiraIssuesMapping(totalJiraIssues, nonCompIssues, issuesNotCompleted,
							sprintDetails);
					jiraOpenIssueCustomHistoryMap = jiraIssueCustomHistoryMapping(issuesNotCompleted,
							totalJiraIssuesHistory);
				}
				if (CollectionUtils.isNotEmpty(compIssues)) {
					jiraIssueMap = jiraIssuesMapping(totalJiraIssues, compIssues, completedIssues, sprintDetails);
					jiraIssueCustomHistoryMap = jiraIssueCustomHistoryMapping(completedIssues, totalJiraIssuesHistory);
				}

				resultListMap.put(TOTALISSUES, totalJiraIssues);
				resultListMap.put(COMPLETED_ISSUES, compIssues);
				resultListMap.put(NOT_COMPLETED_ISSUES, nonCompIssues);
				resultListMap.put(SPRINT, sprintDetails);
				resultListMap.put(JIRAISSUEMAP, jiraIssueMap);
				resultListMap.put(JIRAISSUECUSTOMHISTORYMAP, jiraIssueCustomHistoryMap);
				resultListMap.put(JIRAOPENISSUEMAP, jiraOpenIssueMap);
				resultListMap.put(JIRAOPENISSUECUSTOMHISTORYMAP, jiraOpenIssueCustomHistoryMap);
			}
		}
		return resultListMap;
	}

	private Map<String, JiraIssueCustomHistory> jiraIssueCustomHistoryMapping(List<String> issues,
			List<JiraIssueCustomHistory> totalJiraIssuesHistory) {
		Map<String, JiraIssueCustomHistory> jiraOpenIssueCustomHistoryMap;
		List<JiraIssueCustomHistory> historyList = totalJiraIssuesHistory.stream()
				.filter(f -> CollectionUtils.containsAny(Arrays.asList(f.getStoryID()), issues))
				.collect(Collectors.toList());
		jiraOpenIssueCustomHistoryMap = historyList.stream()
				.collect(Collectors.toMap(JiraIssueCustomHistory::getStoryID, Function.identity()));
		return jiraOpenIssueCustomHistoryMap;
	}

	private Map<String, JiraIssue> jiraIssuesMapping(List<JiraIssue> totalJiraIssues, Set<SprintIssue> sprintIssues,
			List<String> issues, SprintDetails sprintDetails) {
		Set<JiraIssue> filtersOpenIssuesList;
		Map<String, JiraIssue> jiraOpenIssueMap;
		List<JiraIssue> jiraIssueList = totalJiraIssues.stream()
				.filter(f -> CollectionUtils.containsAny(Arrays.asList(f.getNumber()), issues))
				.collect(Collectors.toList());
		filtersOpenIssuesList = KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
				sprintIssues, jiraIssueList);
		jiraOpenIssueMap = filtersOpenIssuesList.stream()
				.collect(Collectors.toMap(JiraIssue::getNumber, Function.identity()));
		return jiraOpenIssueMap;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint level.
	 *
	 * @param sprintLeafNodeList
	 * @param trendValue
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(List<Node> sprintLeafNodeList, DataCount trendValue, KpiElement kpiElement,
			KpiRequest kpiRequest) throws ParseException {
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		Set<SprintIssue> completedIssues = (Set<SprintIssue>) resultMap.get(COMPLETED_ISSUES);
		Set<SprintIssue> openIssues = (Set<SprintIssue>) resultMap.get(NOT_COMPLETED_ISSUES);
		Map<String, JiraIssue> jiraMap = (Map<String, JiraIssue>) resultMap.get(JIRAISSUEMAP);
		Map<String, JiraIssueCustomHistory> jiraHistoryMap = (Map<String, JiraIssueCustomHistory>) resultMap
				.get(JIRAISSUECUSTOMHISTORYMAP);
		Map<String, JiraIssue> jiraOpenMap = (Map<String, JiraIssue>) resultMap.get(JIRAOPENISSUEMAP);
		Map<String, JiraIssueCustomHistory> jiraOpenHistoryMap = (Map<String, JiraIssueCustomHistory>) resultMap
				.get(JIRAOPENISSUECUSTOMHISTORYMAP);

		SprintDetails value = (SprintDetails) resultMap.get(SPRINT);
		String startDate = value.getStartDate();
		String endDate = value.getEndDate();

		Map<String, List<IterationStatus>> closedIssuesDelay = new HashMap<>();
		Map<String, List<IterationStatus>> openIssuesDelay = new HashMap<>();

		if (CollectionUtils.isNotEmpty((Set<SprintIssue>) resultMap.get(COMPLETED_ISSUES))) {
			closedIssuesDelay = findDelayOfClosedIssues(completedIssues, jiraMap, jiraHistoryMap, startDate, endDate);
		}
		if (CollectionUtils.isNotEmpty((Set<SprintIssue>) resultMap.get(NOT_COMPLETED_ISSUES))) {
			openIssuesDelay = findDelayOfOpenIssues(openIssues, jiraOpenMap, jiraOpenHistoryMap, startDate, endDate,
					closedIssuesDelay);
		}

		// calculating net delay of closed issues before, after time
		List<IterationStatus> iterationKpiModalValuesNetDelay = calculateNetDelay(closedIssuesDelay.get(DELAY_DETAILS),
				openIssuesDelay.get(OPEN_ISSUES));

		// issues done before time
		List<IterationStatus> iterationKpiModalValuesIssuesDoneBeforeTime = closedIssuesDelay
				.get("issuesClosedBeforeDueDate");

		// issues causing delay of open issues not done yet and issues closed after
		// duedate
		List<IterationStatus> iterationKpiModalValuesIssuesCausingDelay = issuesCausingDelay(
				closedIssuesDelay.get("issuesClosedAfterDelayDate"), openIssuesDelay.get(OPEN_ISSUES));

		Set<String> issueTypes = new HashSet<>();
		Set<String> priorities = new HashSet<>();

		List<IterationKpiValue> iterationKpiValues = new ArrayList<>();

		List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();

		List<Integer> netdelayCount = Arrays.asList(0);
		int overAllDelayNumberCount = 0;
		int overAllBTDelayNumberCount = 0;
		int overAllCDDelayNumberCount = 0;
		List<Double> overAllCDCount = Arrays.asList(0.0);
		List<Double> overAllBTCount = Arrays.asList(0.0);
		/* grouping map based on type and priority */
		Map<String, Map<String, List<IterationStatus>>> typeAndPriorityWiseCdIssues = new HashMap<>();
		Map<String, Map<String, List<IterationStatus>>> typeAndPriorityWiseBtIssues = new HashMap<>();
		if (CollectionUtils.isNotEmpty(iterationKpiModalValuesIssuesCausingDelay)) {
			typeAndPriorityWiseCdIssues = iterationKpiModalValuesIssuesCausingDelay.stream().collect(Collectors
					.groupingBy(IterationStatus::getTypeName, Collectors.groupingBy(IterationStatus::getPriority)));
		}
		if (CollectionUtils.isNotEmpty(iterationKpiModalValuesIssuesDoneBeforeTime)) {
			typeAndPriorityWiseBtIssues = iterationKpiModalValuesIssuesDoneBeforeTime.stream().collect(Collectors
					.groupingBy(IterationStatus::getTypeName, Collectors.groupingBy(IterationStatus::getPriority)));
		}

		if (CollectionUtils.isNotEmpty(iterationKpiModalValuesNetDelay)) {
			Map<String, Map<String, List<IterationStatus>>> typeAndPriorityWiseIssues = iterationKpiModalValuesNetDelay
					.stream().collect(Collectors.groupingBy(IterationStatus::getTypeName,
							Collectors.groupingBy(IterationStatus::getPriority)));

			for (Map.Entry<String, Map<String, List<IterationStatus>>> entry : typeAndPriorityWiseIssues.entrySet()) {
				Map<String, List<IterationStatus>> typeWiseData = entry.getValue();
				for (Map.Entry<String, List<IterationStatus>> prData : typeWiseData.entrySet()) {
					List<IterationStatus> issues = prData.getValue();
					// finding the cd issues passing issuetype and priority
					int cdCount = 0;
					int cdDelayNumberCount = 0;
					Map<String, List<IterationStatus>> priorityWiseCdIssues = typeAndPriorityWiseCdIssues
							.get(entry.getKey());
					if (MapUtils.isNotEmpty(priorityWiseCdIssues)) {
						List<IterationStatus> cdIssues = priorityWiseCdIssues.get(prData.getKey());
						if ((cdIssues != null) && !cdIssues.isEmpty()) {
							for (IterationStatus iterationStatus : cdIssues) {
								cdDelayNumberCount = Integer.parseInt(String
										.valueOf((cdDelayNumberCount) + Integer.parseInt(iterationStatus.getDelay())));
								overAllCDDelayNumberCount = Integer.parseInt(String.valueOf(
										(overAllCDDelayNumberCount) + Integer.parseInt(iterationStatus.getDelay())));
								cdCount = cdCount + 1;
								overAllCDCount.set(0, overAllCDCount.get(0) + 1);
							}
						}
					} else {
						overAllCDCount.set(0, overAllCDCount.get(0) + 0);
					}
					//
					// finding the bt issues passing issuetype and priority
					int btCount = 0;
					int btDelayNumberCount = 0;
					Map<String, List<IterationStatus>> priorityWiseBtIssues = typeAndPriorityWiseBtIssues
							.get(entry.getKey());
					if (MapUtils.isNotEmpty(priorityWiseBtIssues)) {
						List<IterationStatus> btIssues = priorityWiseBtIssues.get(prData.getKey());
						if ((btIssues != null) && !btIssues.isEmpty()) {
							for (IterationStatus iterationStatus : btIssues) {
								btDelayNumberCount = Integer.parseInt(String
										.valueOf((btDelayNumberCount) + Integer.parseInt(iterationStatus.getDelay())));
								overAllBTDelayNumberCount = Integer.parseInt(String.valueOf(
										(overAllBTDelayNumberCount) + Integer.parseInt(iterationStatus.getDelay())));
								btCount = btCount + 1;
								overAllBTCount.set(0, overAllBTCount.get(0) + 1);
							}
						}
					} else {
						overAllBTCount.set(0, overAllBTCount.get(0) + 0);
					}

					issueTypes.add(entry.getKey());
					priorities.add(prData.getKey());
					List<IterationKpiModalValue> modalValues = new ArrayList<>();
					int delayCount = 0;
					int delayNumberCount = 0;
					for (IterationStatus iterationStatus : issues) {
						delayNumberCount = Integer.parseInt(
								String.valueOf((delayNumberCount) + Integer.parseInt(iterationStatus.getDelay())));
						overAllDelayNumberCount = Integer.parseInt(String
								.valueOf((overAllDelayNumberCount) + Integer.parseInt(iterationStatus.getDelay())));
						delayCount = delayCount + 1;
						netdelayCount.set(0, netdelayCount.get(0) + 1);
						populateIterationStatusData(overAllmodalValues, modalValues, iterationStatus);
					}
					List<IterationKpiData> data = new ArrayList<>();
					IterationKpiData issueAtRisk = new IterationKpiData(NET_DELAYED_ISSUES,
							Double.valueOf(delayNumberCount), null, null, DAYS, modalValues);
					IterationKpiData issuecd = new IterationKpiData(ISSUES_CAUSING_DELAY, Double.valueOf(cdCount), null,
							LABELINFO, "", null);
					IterationKpiData issuebt = new IterationKpiData(ISSUES_DONE_BEFORE_TIME, Double.valueOf(btCount),
							null, LABELINFO, "", null);
					data.add(issueAtRisk);
					data.add(issuecd);
					data.add(issuebt);
					IterationKpiValue iterationKpiValue = new IterationKpiValue(entry.getKey(), prData.getKey(), data);
					iterationKpiValues.add(iterationKpiValue);
				}
			}
			List<IterationKpiData> data = new ArrayList<>();

			IterationKpiData overAllIssuesAtRisk = new IterationKpiData(NET_DELAYED_ISSUES,
					Double.valueOf(overAllDelayNumberCount), null, null, DAYS, overAllmodalValues);
			IterationKpiData overAllIssuescd = new IterationKpiData(ISSUES_CAUSING_DELAY, overAllCDCount.get(0), null,
					LABELINFO, "", null);

			IterationKpiData overAllIssuesbt = new IterationKpiData(ISSUES_DONE_BEFORE_TIME, overAllBTCount.get(0),
					null, LABELINFO, "", null);
			data.add(overAllIssuesAtRisk);
			data.add(overAllIssuescd);
			data.add(overAllIssuesbt);
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
			iterationKpiValues.add(overAllIterationKpiValue);

			// Create kpi level filters
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypes);
			IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, priorities);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
			// Modal Heads Options
			trendValue.setValue(iterationKpiValues);
			kpiElement.setFilters(iterationKpiFilters);
			kpiElement.setSprint(latestSprint.getName());
			kpiElement.setModalHeads(KPIExcelColumn.ITERATION_STATUS.getColumns());
			kpiElement.setTrendValueList(trendValue);

		}
	}

	private List<IterationStatus> issuesCausingDelay(List<IterationStatus> issuesClosedAfterDelayDate,
			List<IterationStatus> openIssuesCausingDelay) {
		List<IterationStatus> issuesCausedDelay = new ArrayList<>();
		if ((issuesClosedAfterDelayDate != null) && !issuesClosedAfterDelayDate.isEmpty()) {
			issuesCausedDelay.addAll(issuesClosedAfterDelayDate.stream()
					.filter(iterationStatus -> Integer.parseInt(iterationStatus.getDelay()) != 0)
					.collect(Collectors.toList()));
		}
		if ((openIssuesCausingDelay != null) && !openIssuesCausingDelay.isEmpty()) {
			issuesCausedDelay.addAll(openIssuesCausingDelay);
		}
		return issuesCausedDelay;
	}

	/*
	 * this method calculates the net delay
	 */
	private List<IterationStatus> calculateNetDelay(List<IterationStatus> delayDetails,
			List<IterationStatus> openIssuesCausingDelay) {
		List<IterationStatus> iterationKpiModalValuesNetDelay = new ArrayList<>();
		if ((delayDetails != null) && !delayDetails.isEmpty()) {
			iterationKpiModalValuesNetDelay.addAll(delayDetails);
		}
		if ((openIssuesCausingDelay != null) && !openIssuesCausingDelay.isEmpty()) {
			iterationKpiModalValuesNetDelay.addAll(openIssuesCausingDelay);
		}
		return iterationKpiModalValuesNetDelay;
	}

	/*
	 * method to find delay in closed issues
	 */
	private Map<String, List<IterationStatus>> findDelayOfClosedIssues(Set<SprintIssue> completedIssues,
			Map<String, JiraIssue> jiraMap, Map<String, JiraIssueCustomHistory> jiraHistoryMap, String startDate,
			String endDate) {
		Map<String, List<IterationStatus>> resultList = new HashMap<>();
		List<IterationStatus> jiraBeforeTimeIssueList = new ArrayList<>();
		List<IterationStatus> jiraAfterTimeIssueList = new ArrayList<>();
		List<IterationStatus> jiraDelayIssueList = new ArrayList<>();
		for (SprintIssue story : completedIssues) {
			IterationStatus iterationKpiModalValue;
			JiraIssueCustomHistory issueHistoryObject = jiraHistoryMap.get(story.getNumber());
			JiraIssue issueObject = jiraMap.get(story.getNumber());
			if ((Objects.nonNull(issueHistoryObject)) && Objects.nonNull(issueObject)) {
				String closedDate = findClosedDate(issueHistoryObject, startDate, endDate, story.getStatus());
				String dueDate = issueObject.getDueDate();
				if (StringUtils.isNotEmpty(dueDate) && StringUtils.isNotEmpty(closedDate)) {
					// count the number of days excluding weekends
					int daysDiff = CommonUtils.closedStoryAndPotentialDelays(DateTime.parse(dueDate),
							DateTime.parse(closedDate));
					if (daysDiff > 0) {
						iterationKpiModalValue = prepareStoryDetails(issueObject, "-" + daysDiff);
						jiraBeforeTimeIssueList.add(iterationKpiModalValue);
					} else {
						iterationKpiModalValue = prepareStoryDetails(issueObject, String.valueOf(daysDiff * -1));
						jiraAfterTimeIssueList.add(iterationKpiModalValue);
					}
				}
			}
		}
		jiraDelayIssueList.addAll(jiraBeforeTimeIssueList);
		jiraDelayIssueList.addAll(jiraAfterTimeIssueList);
		resultList.computeIfPresent(DELAY_DETAILS, (k, v) -> {
			v.addAll(jiraDelayIssueList);
			return v;
		});
		resultList.putIfAbsent(DELAY_DETAILS, jiraDelayIssueList);
		resultList.put("issuesClosedAfterDelayDate", jiraAfterTimeIssueList);
		resultList.put("issuesClosedBeforeDueDate", jiraBeforeTimeIssueList);
		return resultList;
	}

	private IterationStatus prepareStoryDetails(JiraIssue issueObject, String delay) {
		IterationStatus iterationStatus = new IterationStatus();
		iterationStatus.setIssueId(issueObject.getNumber());
		iterationStatus.setUrl(issueObject.getUrl());
		iterationStatus.setTypeName(issueObject.getTypeName());
		iterationStatus.setPriority(issueObject.getPriority());
		iterationStatus.setIssueDescription(issueObject.getName());
		iterationStatus.setIssueStatus(issueObject.getStatus());
		iterationStatus.setDueDate(DateUtil.dateTimeConverter(issueObject.getDueDate(), DateUtil.TIME_FORMAT_WITH_SEC,
				DateUtil.DISPLAY_DATE_FORMAT));
		if (issueObject.getRemainingEstimateMinutes() != null) {
			iterationStatus.setRemainingEstimateMinutes(issueObject.getRemainingEstimateMinutes() / 60);
		}
		iterationStatus.setDelay(delay);
		return iterationStatus;
	}

	private Map<String, List<IterationStatus>> findDelayOfOpenIssues(Set<SprintIssue> openIssues,
			Map<String, JiraIssue> jiraOpenMap, Map<String, JiraIssueCustomHistory> jiraOpenHistoryMap,
			String startDate, String endDate, Map<String, List<IterationStatus>> resultList) throws ParseException {

		Map<String, List<IterationStatus>> resultListOpenIssues = new HashMap<>();
		List<IterationStatus> jiraDelayIssueList = new ArrayList<>();
		List<IterationStatus> jiraNegativeDelayIssueList = new ArrayList<>();
		for (SprintIssue story : openIssues) {
			IterationStatus iterationKpiModalValue;
			JiraIssueCustomHistory issueHistoryObject = jiraOpenHistoryMap.get(story.getNumber());
			JiraIssue issueObject = jiraOpenMap.get(story.getNumber());
			if ((Objects.nonNull(issueHistoryObject)) && Objects.nonNull(issueObject)
					&& StringUtils.isNotEmpty(issueObject.getDueDate())) {
				iterationKpiModalValue = createIterationKpiModal(startDate, endDate, issueObject);
				if ((iterationKpiModalValue.getIssueId() != null)) {
					if (Integer.parseInt(iterationKpiModalValue.getDelay()) > 0) {
						jiraNegativeDelayIssueList.add(iterationKpiModalValue);
					} else {
						jiraDelayIssueList.add(iterationKpiModalValue);
					}
				}
			}
		}
		resultList.computeIfPresent(DELAY_DETAILS, (k, v) -> {
			v.addAll(jiraDelayIssueList);
			return v;
		});
		resultList.putIfAbsent(DELAY_DETAILS, jiraDelayIssueList);
		resultListOpenIssues.put(OPEN_ISSUES, jiraNegativeDelayIssueList);
		return resultListOpenIssues;
	}

	private IterationStatus createIterationKpiModal(String startDate, String endDate, JiraIssue issueObject)
			throws ParseException {
		IterationStatus iterationKpiModalValue;
		SimpleDateFormat sdformat = new SimpleDateFormat(DATE_FORMAT);
		DateTime dueDate = DateTime.parse(issueObject.getDueDate());
		Date storyDueDate = sdformat.parse(String.valueOf(dueDate));
		Date sprintEndData = sdformat.parse(String.valueOf(endDate));
		Date todayDate = sdformat.parse(String.valueOf(DateTime.now()));
		Date sprintStartDate = sdformat.parse(String.valueOf(startDate));
		/*
		 * case of stories past the due date and are closed curr date, duedate,
		 * sprintenddate
		 */
		if (todayDate.compareTo(storyDueDate) > 0) { // if current date > than story due date .i.e story
			// past the due date
			if (todayDate.compareTo(sprintEndData) > 0) {// if curr date is > sprint end date .i.e closed
				// sprint case

				// delayList =
				// String.valueOf(potentialDelayOfStoriesPastDueDateClosedSprint(issueObject,
				// endDate,
				// startDate)); // due date passed and closed sprint
				iterationKpiModalValue = potentialDelayOfStoriesPastDueDateClosedSprint(issueObject, endDate);
			} else {// if curr date is < sprint end date .i.e active sprint case, Story spillage
				// case
				if (storyDueDate.compareTo(sprintStartDate) < 0) { // spilled story and due date not changed
					// < sprint start date
					iterationKpiModalValue = spilledIssues(startDate, issueObject);
				} else {
					// duedate passed but active sprint
					iterationKpiModalValue = issuesPastDueDateInsideSprint(dueDate, startDate, endDate, issueObject);
				}
			}
		} else { // if current date is less than story due date, stories inside due date but not
			// closed, Active story case
			iterationKpiModalValue = potentialDelayOfStoriesInsideDueDate(endDate, dueDate, issueObject);
		}
		return iterationKpiModalValue;
	}

	private IterationStatus potentialDelayOfStoriesInsideDueDate(String endDate, DateTime dueDate,
			JiraIssue issueObject) throws ParseException {
		SimpleDateFormat sdformat = new SimpleDateFormat(DATE_FORMAT);
		Date sprintEndData = sdformat.parse(String.valueOf(endDate));
		DateTime currDate = DateTime.now();
		Date todayDate = sdformat.parse(String.valueOf(currDate));
		String delayList = null;
		String diffREDelays = null;
		IterationStatus iterationStatus = new IterationStatus();
		if (todayDate.compareTo(sprintEndData) < 0) {
			Integer daysDiff = CommonUtils.closedStoryAndPotentialDelays(dueDate, currDate);
			Integer remainingEstimateTime = getRemainingEstimateTime(issueObject);
			if (remainingEstimateTime > daysDiff) {
				diffREDelays = String.valueOf(remainingEstimateTime - daysDiff);
				diffREDelays = String.valueOf(Integer.valueOf(diffREDelays));
			} else {
				daysDiff = daysDiff - remainingEstimateTime;
				diffREDelays = (daysDiff == 0) ? String.valueOf(daysDiff) : "-" + (daysDiff);
			}
			delayList = diffREDelays;
			iterationStatus = prepareStoryDetails(issueObject, delayList);
		}
		return iterationStatus;
	}

	private IterationStatus issuesPastDueDateInsideSprint(DateTime dueDate, String startDate, String endDate,
			JiraIssue issueObject) throws ParseException {
		SimpleDateFormat sdformat = new SimpleDateFormat(DATE_FORMAT);
		Date storyDueDate = sdformat.parse(String.valueOf(dueDate));
		Date sprintStartDate = sdformat.parse(String.valueOf(startDate));
		Date sprintEndDate = sdformat.parse(String.valueOf(endDate));
		Integer delayList = 0;
		Integer delayDaysAlready = 0;
		DateTime currDate = DateTime.now();
		IterationStatus iterationStatus = new IterationStatus();
		if (storyDueDate.compareTo(sprintStartDate) >= 0 && storyDueDate.compareTo(sprintEndDate) < 0) {
			delayDaysAlready = CommonUtils.openStoryDelay(currDate, dueDate, false);
			Integer remainingEstimateTime = getRemainingEstimateTime(issueObject);
			if (remainingEstimateTime > 0) {
				delayDaysAlready = remainingEstimateTime + delayDaysAlready;
			}
			delayList = (delayList + (delayDaysAlready));
			iterationStatus = prepareStoryDetails(issueObject, String.valueOf(delayList));
		}
		return iterationStatus;
	}

	private IterationStatus spilledIssues(String startDate, JiraIssue issueObject) {
		DateTime currDate = DateTime.now();
		DateTime sprintStart = DateTime.parse(startDate);
		Integer delayDaysAlready = 0;
		Integer delayList = 0;
		Integer estimateTime = getRemainingEstimateTime(issueObject);
		delayDaysAlready = CommonUtils.openStoryDelay(currDate, sprintStart, true);
		delayList = (delayList + delayDaysAlready + estimateTime);
		return prepareStoryDetails(issueObject, String.valueOf(delayList));
	}

	private IterationStatus potentialDelayOfStoriesPastDueDateClosedSprint(JiraIssue issueObject, String endDate)
			throws ParseException {
		Integer delayList = 0;
		IterationStatus iterationStatus = new IterationStatus();
		SimpleDateFormat sdformat = new SimpleDateFormat(DATE_FORMAT);
		DateTime dueDate = DateTime.parse(issueObject.getDueDate());
		Date storyDueDate = sdformat.parse(String.valueOf(dueDate));
		Date sprintEndData = sdformat.parse(String.valueOf(endDate));
		DateTime sprintEnd = DateTime.parse(endDate);
		if (storyDueDate.compareTo(sprintEndData) < 0) {
			Integer delayDaysAlready = CommonUtils.closedStoryAndPotentialDelays(sprintEnd, dueDate);
			delayList = (delayList + delayDaysAlready) * -1;
			iterationStatus = prepareStoryDetails(issueObject, String.valueOf(delayList));
		}
		return iterationStatus;
	}

	public String findClosedDate(JiraIssueCustomHistory issueHistoryObject, String startDate, String endDate,
			String status) {
		String date;
		for (int i = 0; i < issueHistoryObject.getStatusUpdationLog().size(); i++) {
			if (issueHistoryObject.getStatusUpdationLog().get(i).getChangedTo().equalsIgnoreCase(status)) {
				date = issueHistoryObject.getStatusUpdationLog().get(i).getUpdatedOn().toString();
				DateTime closedDate = DateTime.parse(date);
				DateTime startDateValue = DateTime.parse(startDate);
				DateTime endDateValue = DateTime.parse(endDate);
				if (closedDate.isAfter(startDateValue) && closedDate.isBefore(endDateValue)) {
					return date;
				}
			}
		}
		return null;
	}
}