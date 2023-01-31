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

import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectsAccess;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.kafka.common.protocol.types.Field;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

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

	private static final Logger LOGGER = LoggerFactory.getLogger(IterationStatusServiceImpl.class);

	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String SEARCH_BY_PRIORITY = "Filter by priority";
	private static final String TOTALISSUES = "totalIssues";
	private static final String SPRINT = "sprint";
	private static final String JIRAISSUEMAP = "jiraIssueMap";
	private static final String JIRAOPENISSUEMAP = "jiraOpenIssueMap";
	private static final String COMPLETED_ISSUES = "completedIssues";
	private static final String ISSUES_CAUSING_DELAY = "Delayed Issues";
	private static final String NET_DELAYED_ISSUES = "Net Delay";
	private static final String ISSUES_DONE_BEFORE_TIME = "Done Before Time";
	private static final String NOT_COMPLETED_ISSUES = "notCompletedIssues";
	private static final String JIRAISSUECUSTOMHISTORYMAP = "jiraIssueCustomHistoryMap";
	private static final String JIRAOPENISSUECUSTOMHISTORYMAP = "jiraOpenIssueCustomHistoryMap";
	private static final String OVERALL = "Overall";

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

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
					log.error("Exception while parse date..." + e.getMessage());
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
		Map<String, Object> issueKeyWiseSprintIssue = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			LOGGER.info("Work Remaining -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId().toString();
			String sprintId = leafNode.getSprintFilter().getId();
			SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintId);
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


				List<JiraIssue> totalJiraIssues = jiraIssueRepository.findByNumberInAndBasicProjectConfigId(issuesList,
						basicProjectConfigId);

				List<JiraIssueCustomHistory> totalJiraIssuesHistory = jiraIssueCustomHistoryRepository
						.findByStoryIDInAndBasicProjectConfigIdIn(issuesList, Arrays.asList(basicProjectConfigId));

				Map<String, JiraIssue> jiraOpenIssueMap = new HashMap<>();
				Map<String, JiraIssue> jiraIssueMap = new HashMap<>();
				Map<String, JiraIssueCustomHistory> jiraOpenIssueCustomHistoryMap = new HashMap<>();
				Map<String, JiraIssueCustomHistory> jiraIssueCustomHistoryMap = new HashMap<>();

				jiraOpenIssueMap = totalJiraIssues.stream()
						.collect(Collectors.toMap(JiraIssue::getNumber, Function.identity()));

				if (CollectionUtils.isNotEmpty(nonCompIssues)) {
					jiraOpenIssueMap = jiraIssuesMapping(totalJiraIssues, nonCompIssues, issuesNotCompleted, sprintDetails);
					jiraOpenIssueCustomHistoryMap = jiraIssueCustomHistoryMapping(issuesNotCompleted,
							totalJiraIssuesHistory, sprintDetails);
				}
				if (CollectionUtils.isNotEmpty(compIssues)) {
					jiraIssueMap = jiraIssuesMapping(totalJiraIssues, compIssues, completedIssues, sprintDetails);
					jiraIssueCustomHistoryMap = jiraIssueCustomHistoryMapping(completedIssues, totalJiraIssuesHistory,
							sprintDetails);
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
			List<JiraIssueCustomHistory> totalJiraIssuesHistory, SprintDetails sprintDetails) {
		Map<String, JiraIssueCustomHistory> jiraOpenIssueCustomHistoryMap = new HashMap<>();
		List<JiraIssueCustomHistory> historyList = totalJiraIssuesHistory.stream()
				.filter(f -> CollectionUtils.containsAny(Arrays.asList(f.getStoryID()), issues))
				.collect(Collectors.toList());
		jiraOpenIssueCustomHistoryMap = historyList.stream()
				.collect(Collectors.toMap(JiraIssueCustomHistory::getStoryID, Function.identity()));
		return jiraOpenIssueCustomHistoryMap;
	}

	private Map<String, JiraIssue> jiraIssuesMapping(List<JiraIssue> totalJiraIssues, Set<SprintIssue> sprintIssues, List<String> issues, SprintDetails sprintDetails) {
		Set<JiraIssue> filtersOpenIssuesList = new HashSet<>();
		Map<String, JiraIssue> jiraOpenIssueMap = new HashMap<>();
		List<JiraIssue> IssuesList = totalJiraIssues.stream()
				.filter(f -> CollectionUtils.containsAny(Arrays.asList(f.getNumber()), issues))
				.collect(Collectors.toList());
			filtersOpenIssuesList = KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
					sprintIssues, IssuesList);
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
		String requestTrackerId = getRequestTrackerId();

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

		SprintDetails value = (SprintDetails) resultMap.get("sprint");
		String startDate = value.getStartDate();
		String endDate = value.getEndDate();

		Map<String, List<IterationStatus>> closedIssuesDelay = new HashMap<>();
		Map<String, List<IterationStatus>> openIssuesDelay = new HashMap<>();

		if (CollectionUtils.isNotEmpty((Set<SprintIssue>) resultMap.get(COMPLETED_ISSUES))) {
			closedIssuesDelay = findDelayOfClosedIssues(completedIssues, jiraMap,
					jiraHistoryMap, startDate, endDate);
		}
		if(CollectionUtils.isNotEmpty((Set<SprintIssue>) resultMap.get(NOT_COMPLETED_ISSUES))){
			openIssuesDelay = findDelayOfOpenIssues(openIssues, jiraOpenMap,
					jiraOpenHistoryMap, startDate, endDate);
		}

			// calculating net delay of closed issues before, after time
			List<IterationStatus> iterationKpiModalValuesNetDelay = calculateNetDelay(
					closedIssuesDelay.get("delayDetails"), openIssuesDelay.get("openIssuesCausingDelay"));

			// issues done before time
			List<IterationStatus> iterationKpiModalValuesIssuesDoneBeforeTime = closedIssuesDelay
					.get("issuesClosedBeforeDueDate");

			// issues causing delay of open issues not done yet and issues closed after due
			// date
			List<IterationStatus> iterationKpiModalValuesIssuesCausingDelay = new ArrayList<>();
			iterationKpiModalValuesIssuesCausingDelay.addAll(closedIssuesDelay.get("issuesClosedAfterDelayDate"));
			iterationKpiModalValuesIssuesCausingDelay.addAll(openIssuesDelay.get("openIssuesCausingDelay"));

			Map<String, List<IterationStatus>> finalMap = new HashMap<>();
			finalMap.put("issuesCausingDelay", iterationKpiModalValuesIssuesCausingDelay);
			finalMap.put("issuesClosedBeforeDueDate", iterationKpiModalValuesIssuesDoneBeforeTime);

			List<IterationStatus> netDelay = iterationKpiModalValuesNetDelay;
			Set<String> issueTypes = new HashSet<>();
			Set<String> priorities = new HashSet<>();

			List<IterationStatus> allIssues = iterationKpiModalValuesIssuesCausingDelay;
			Set<String> issueTypes2 = new HashSet<>();
			Set<String> priorities2 = new HashSet<>();

			List<IterationStatus> completedBeforeTimeIssues = iterationKpiModalValuesIssuesDoneBeforeTime;
			Set<String> issueTypes3 = new HashSet<>();
			Set<String> priorities3 = new HashSet<>();

			Set<String> overAllIssueTypes = new HashSet<>();
			Set<String> overAllPriorities = new HashSet<>();

			List<IterationKpiValue> overAllData = new ArrayList<>();

			List<IterationKpiData> overAllDataOfNetDelay = new ArrayList<>();

			List<IterationKpiValue> finalIterationKpiValue = new ArrayList<>();


			if (CollectionUtils.isNotEmpty(netDelay)) {
				Map<String, Map<String, List<IterationStatus>>> typeAndPriorityWiseIssues = netDelay.stream()
						.collect(Collectors.groupingBy(IterationStatus::getTypeName,
								Collectors.groupingBy(IterationStatus::getPriority)));
				List<Integer> overAllIssuesNetDelay = Arrays.asList(0);

				List<IterationKpiValue> iterationKpiValuesNetDelay = new ArrayList<>();

				typeAndPriorityWiseIssues.forEach((issueType, priorityWiseIssue) -> {
					priorityWiseIssue.forEach((priority, issues) -> {
						List<IterationKpiModalValue> issuesCausingDelay = new ArrayList<>();
						issueTypes.add(issueType);
						priorities.add(priority);
						int issueCausingNetDelayCount = 0;
						for (IterationStatus iterationStatus : issues) {
							if (ObjectUtils.isNotEmpty(iterationStatus)) {
								issueCausingNetDelayCount++;
								overAllIssuesNetDelay.set(0, overAllIssuesNetDelay.get(0) + 1);
							}
						}
						List<IterationKpiData> data = new ArrayList<>();
						IterationKpiData issuesAtCausingDelay = new IterationKpiData(NET_DELAYED_ISSUES,
								Double.valueOf(issueCausingNetDelayCount), null, null, "", issuesCausingDelay);
						data.add(issuesAtCausingDelay);
						IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, priority, data);
						iterationKpiValuesNetDelay.add(iterationKpiValue);
					});
				});

				List<IterationKpiData> data = new ArrayList<>();

				IterationKpiData overAllDelay = new IterationKpiData(NET_DELAYED_ISSUES,
						Double.valueOf(overAllIssuesNetDelay.get(0)), null, null, "", null);
				data.add(overAllDelay);
				IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
				iterationKpiValuesNetDelay.add(overAllIterationKpiValue);
				finalIterationKpiValue.addAll(iterationKpiValuesNetDelay);
				//overAllDataOfNetDelay.add(overAllDelay);
			}


				List<IterationKpiData> overAllDataOfIssuesCausingDelay = new ArrayList<>();

			if (CollectionUtils.isNotEmpty(allIssues)) {
				Map<String, Map<String, List<IterationStatus>>> typeAndPriorityWiseIssues = allIssues.stream()
						.collect(Collectors.groupingBy(IterationStatus::getTypeName,
								Collectors.groupingBy(IterationStatus::getPriority)));

				List<Integer> overAllIssueCount = Arrays.asList(0);
				List<Integer> overAllIssuesCausingDelay = Arrays.asList(0);

				List<IterationKpiValue> iterationKpiValuesIssuesCausingDelay = new ArrayList<>();

				List<IterationKpiModalValue> overAllIssuesCausingDelayModalValues = new ArrayList<>();

				typeAndPriorityWiseIssues.forEach((issueType, priorityWiseIssue) -> {
					priorityWiseIssue.forEach((priority, issues) -> {
						List<IterationKpiModalValue> issuesCausingDelay = new ArrayList<>();
						issueTypes2.add(issueType);
						priorities2.add(priority);
						int issueCausingDelayCount = 0;
						for (IterationStatus iterationStatus : issues) {
							overAllIssueCount.set(0, overAllIssueCount.get(0) + 1);
							if (ObjectUtils.isNotEmpty(iterationStatus)) {
								issueCausingDelayCount++;
								overAllIssuesCausingDelay.set(0, overAllIssuesCausingDelay.get(0) + 1);
								// set modal values
								populateIterationStatusData(issuesCausingDelay, overAllIssuesCausingDelayModalValues,
										iterationStatus);
							}
						}
						List<IterationKpiData> data = new ArrayList<>();
						IterationKpiData issuesAtCausingDelay = new IterationKpiData(ISSUES_CAUSING_DELAY,
								Double.valueOf(issueCausingDelayCount), null, null, "", null);
						data.add(issuesAtCausingDelay);
						IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, priority, data);
						iterationKpiValuesIssuesCausingDelay.add(iterationKpiValue);
					});
				});
				List<IterationKpiData> data = new ArrayList<>();

				IterationKpiData overAllDelay = new IterationKpiData(ISSUES_CAUSING_DELAY,
						Double.valueOf(overAllIssuesCausingDelay.get(0)), null, null, "",
						overAllIssuesCausingDelayModalValues);
				data.add(overAllDelay);
				overAllDataOfIssuesCausingDelay.add(overAllDelay);
				IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
				iterationKpiValuesIssuesCausingDelay.add(overAllIterationKpiValue);
				finalIterationKpiValue.addAll(iterationKpiValuesIssuesCausingDelay);
			}

				List<IterationKpiData> overAllDataOfIssuesCompletedBeforeTime = new ArrayList<>();
				List<IterationKpiValue> iterationKpiValuesCompletedBeforeTime = new ArrayList<>();

			if (CollectionUtils.isNotEmpty(completedBeforeTimeIssues)) {
				Map<String, Map<String, List<IterationStatus>>> typeAndPriorityWiseIssues = completedBeforeTimeIssues
						.stream().collect(Collectors.groupingBy(IterationStatus::getTypeName,
								Collectors.groupingBy(IterationStatus::getPriority)));

				List<Integer> overAllIssueCount = Arrays.asList(0);
				List<Integer> overAllIssuesClosedBeforeTime = Arrays.asList(0);

				List<IterationKpiModalValue> overAllIssuesClosedBeforeTimeModalValues = new ArrayList<>();

				typeAndPriorityWiseIssues.forEach((issueType, priorityWiseIssue) -> {
					priorityWiseIssue.forEach((priority, issues) -> {
						List<IterationKpiModalValue> issuesClosedBeforeTime = new ArrayList<>();
						issueTypes3.add(issueType);
						priorities3.add(priority);
						int issueClosedBeforeTimeCount = 0;
						for (IterationStatus iterationStatus : issues) {
							overAllIssueCount.set(0, overAllIssueCount.get(0) + 1);
							if (ObjectUtils.isNotEmpty(iterationStatus)) {
								issueClosedBeforeTimeCount++;
								overAllIssuesClosedBeforeTime.set(0, overAllIssuesClosedBeforeTime.get(0) + 1);
								// set modal values
								populateIterationStatusData(issuesClosedBeforeTime,
										overAllIssuesClosedBeforeTimeModalValues, iterationStatus);
							}
						}
						List<IterationKpiData> data = new ArrayList<>();
						IterationKpiData issuesAtDoneBeforeTime = new IterationKpiData(ISSUES_DONE_BEFORE_TIME,
								Double.valueOf(issueClosedBeforeTimeCount), null, null, "", issuesClosedBeforeTime);
						data.add(issuesAtDoneBeforeTime);
						IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, priority, data);
						iterationKpiValuesCompletedBeforeTime.add(iterationKpiValue);
					});
				});
				List<IterationKpiData> data = new ArrayList<>();

				IterationKpiData overAllDelay = new IterationKpiData(ISSUES_DONE_BEFORE_TIME,
						Double.valueOf(overAllIssuesClosedBeforeTime.get(0)), null, null, "",
						overAllIssuesClosedBeforeTimeModalValues);

				data.add(overAllDelay);
				//overAllDataOfIssuesCausingDelay.add(overAllDelay);
				IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
				iterationKpiValuesCompletedBeforeTime.add(overAllIterationKpiValue);
				finalIterationKpiValue.addAll(iterationKpiValuesCompletedBeforeTime);

			}

				List<IterationKpiData> finalOverAll = new ArrayList<>();
				finalOverAll.addAll(overAllDataOfNetDelay);
				finalOverAll.addAll(overAllDataOfIssuesCausingDelay);
				finalOverAll.addAll(overAllDataOfIssuesCompletedBeforeTime);

				IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, finalOverAll);
				//iterationKpiValuesCompletedBeforeTime.add(overAllIterationKpiValue);
			//	overAllData.addAll(iterationKpiValuesCompletedBeforeTime);


				trendValue.setValue(finalIterationKpiValue);
				overAllIssueTypes.addAll(issueTypes);
				overAllIssueTypes.addAll(issueTypes2);
				overAllIssueTypes.addAll(issueTypes3);
				overAllPriorities.addAll(priorities);
				overAllPriorities.addAll(priorities2);
				overAllPriorities.addAll(priorities3);

				IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE,
						issueTypes);
				IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, priorities);
				IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
				kpiElement.setFilters(iterationKpiFilters);
				kpiElement.setSprint(latestSprint.getName());
				kpiElement.setModalHeads(KPIExcelColumn.ITERATION_STATUS.getColumns());
				kpiElement.setTrendValueList(trendValue);


	}

	/*
	 * this method calculates the net delay
	 */
	private List<IterationStatus> calculateNetDelay(List<IterationStatus> delayDetails,
			List<IterationStatus> openIssuesCausingDelay) {
		List<IterationStatus> iterationKpiModalValuesNetDelay = new ArrayList<>();
		iterationKpiModalValuesNetDelay.addAll(delayDetails);
		iterationKpiModalValuesNetDelay.addAll(openIssuesCausingDelay);
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
			IterationStatus iterationKpiModalValue = new IterationStatus();
			JiraIssueCustomHistory issueHistoryObject = jiraHistoryMap.get(story.getNumber());
			JiraIssue issueObject = jiraMap.get(story.getNumber());
			Integer daysDiff = 0;
			Integer delay = 0;
			if ((Objects.nonNull(issueHistoryObject)) && Objects.nonNull(issueObject)) {
				String closedDate = findClosedDate(issueHistoryObject, startDate, endDate, story.getStatus());
				String dueDate = issueObject.getDueDate();
				if (StringUtils.isNotEmpty(dueDate)) {
					try {
						// count the number of days excluding weekends
						daysDiff = CommonUtils.getDaysBetwDate(DateTime.parse(dueDate), DateTime.parse(closedDate));
					} catch (ParseException e) {
						log.error(e.getMessage());
					}

					iterationKpiModalValue = prepareStoryDetails(issueObject, daysDiff);
					jiraDelayIssueList.add(iterationKpiModalValue);
					if (daysDiff > 0) {
						iterationKpiModalValue = prepareStoryDetails(issueObject, daysDiff);
						jiraBeforeTimeIssueList.add(iterationKpiModalValue);
					} else {
						iterationKpiModalValue = prepareStoryDetails(issueObject, daysDiff);
						jiraAfterTimeIssueList.add(iterationKpiModalValue);
					}
				}
			}
		}
		resultList.put("delayDetails", jiraDelayIssueList);
		resultList.put("issuesClosedAfterDelayDate", jiraAfterTimeIssueList);
		resultList.put("issuesClosedBeforeDueDate", jiraBeforeTimeIssueList);
		return resultList;
	}

	private IterationStatus prepareStoryDetails(JiraIssue issueObject, Integer delay) {
		IterationStatus iterationStatus = new IterationStatus();
		iterationStatus.setIssueId(issueObject.getNumber());
		iterationStatus.setUrl(issueObject.getUrl());
		iterationStatus.setTypeName(issueObject.getTypeName());
		iterationStatus.setPriority(issueObject.getPriority());
		iterationStatus.setIssueDescription(issueObject.getName());
		iterationStatus.setIssueStatus(issueObject.getStatus());
		iterationStatus.setDueDate(issueObject.getDueDate());
		if (issueObject.getRemainingEstimateMinutes() != null) {
			iterationStatus.setRemainingEstimateMinutes(issueObject.getRemainingEstimateMinutes() / 60);
		}
		iterationStatus.setDelay(delay);
		return iterationStatus;
	}

	private Map<String, List<IterationStatus>> findDelayOfOpenIssues(Set<SprintIssue> openIssues,
			Map<String, JiraIssue> jiraOpenMap, Map<String, JiraIssueCustomHistory> jiraOpenHistoryMap,
			String startDate, String endDate) throws ParseException {

		Map<String, List<IterationStatus>> resultList = new HashMap<>();
		List<IterationStatus> jiraDelayIssueList = new ArrayList<>();
		for (SprintIssue story : openIssues) {
			IterationStatus iterationKpiModalValue = new IterationStatus();
			Integer delayList = 0;
			JiraIssueCustomHistory issueHistoryObject = jiraOpenHistoryMap.get(story.getNumber());
			JiraIssue issueObject = jiraOpenMap.get(story.getNumber());
			if ((Objects.nonNull(issueHistoryObject)) && Objects.nonNull(issueObject)) {
				DateTime currDate = DateTime.now();
				Date todayDate = new Date();
				Date storyDueDate = new Date();
				Date sprintEndData = new Date();
				Date sprintStartDate = new Date();
				SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
				DateTime dueDate = null;
				if (StringUtils.isNotEmpty(issueObject.getDueDate())) {
					dueDate = DateTime.parse(issueObject.getDueDate());
					try {
						storyDueDate = sdformat.parse(String.valueOf(dueDate));
					} catch (ParseException e) {
						log.error("Exception while parse date..." + e.getMessage());
					}
					try {
						sprintEndData = sdformat.parse(String.valueOf(endDate));
					} catch (ParseException e) {
						log.error("Exception while parse date..." + e.getMessage());
					}
					try {
						todayDate = sdformat.parse(String.valueOf(currDate));
					} catch (ParseException e) {
						log.error("Exception while parse date..." + e.getMessage());
					}
					try {
						sprintStartDate = sdformat.parse(String.valueOf(startDate));
					} catch (ParseException e) {
						log.error("Exception while parse date..." + e.getMessage());
					}
					/*
					 * case of stories past the due date and are closed curr date, duedate,
					 * sprintenddate
					 */
					if (todayDate.compareTo(storyDueDate) > 0) { // if current date > than story due date .i.e story
																 // past the due date
						if (todayDate.compareTo(sprintEndData) > 0) {// if curr date is > sprint end date .i.e closed
																	 // sprint case
							try {
								delayList = potentialDelayOfStoriesPastDueDateClosedSprint(issueObject, endDate,
										startDate);
							} catch (ParseException e) {
								log.error("Exception while parse date..." + e.getMessage());
							}
						} else {// if curr date is < sprint end date .i.e active sprint case, Story spillage
								// case
							if (storyDueDate.compareTo(sprintStartDate) < 0) { // spilled story and due date not changed
																			   // < sprint start date
								delayList = spilledIssues(startDate);
							} else {
								delayList = issuesPastDueDateInsideSprint(dueDate, startDate, endDate);
							}
						}
					} else { // if current date is less than story due date, stories inside due date but not
							 // closed, Active story case
						delayList = potentialDelayOfStoriesInsideDueDate(endDate, dueDate, issueObject);
					}
					iterationKpiModalValue = prepareStoryDetails(issueObject, delayList);
					jiraDelayIssueList.add(iterationKpiModalValue);
				}
			}
		}
		resultList.put("openIssuesCausingDelay", jiraDelayIssueList);
		return resultList;
	}

	private Integer potentialDelayOfStoriesInsideDueDate(String endDate, DateTime dueDate, JiraIssue issueObject)
			throws ParseException {
		SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
		Date sprintEndData = sdformat.parse(String.valueOf(endDate));
		DateTime currDate = DateTime.now();
		Date todayDate = sdformat.parse(String.valueOf(currDate));
		Integer delayList = 0;
		Integer diffREDelays = 0;
		if (todayDate.compareTo(sprintEndData) < 0) {
			try {
				Integer daysDiff = CommonUtils.getDaysBetwDate(dueDate, currDate);
				if (issueObject.getRemainingEstimateMinutes() != null) {
					Integer num = (issueObject.getRemainingEstimateMinutes() / 60) / 8;
					if (num > daysDiff) {
						diffREDelays = num - daysDiff;
						diffREDelays *= -1;
					} else {
						diffREDelays = daysDiff - num;
					}
					delayList += diffREDelays;
				} else
					delayList += daysDiff;
			} catch (ParseException e) {
				log.error("Exception while parse date..." + e.getMessage());
			}
		}
		return delayList;
	}

	private Integer issuesPastDueDateInsideSprint(DateTime dueDate, String startDate, String endDate)
			throws ParseException {
		SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
		Date storyDueDate = sdformat.parse(String.valueOf(dueDate));
		Date sprintStartDate = sdformat.parse(String.valueOf(startDate));
		Date sprintEndDate = sdformat.parse(String.valueOf(endDate));
		Integer delayList = 0;
		Integer delayDaysAlready = 0;
		DateTime currDate = DateTime.now();
		try {
			if (storyDueDate.compareTo(sprintStartDate) > 0 && storyDueDate.compareTo(sprintEndDate) < 0) {
				delayDaysAlready = CommonUtils.getDaysBetwDate(currDate, dueDate);
				delayList += delayDaysAlready;
			}

		} catch (ParseException e) {
			log.error("Exception while parse date..." + e.getMessage());
		}
		return delayList;
	}

	private Integer spilledIssues(String startDate) {
		DateTime currDate = DateTime.now();
		DateTime sprintStart = DateTime.parse(startDate);
		Integer delayDaysAlready = 0;
		Integer delayList = 0;
		try {
			delayDaysAlready = CommonUtils.getDaysBetwDate2(currDate, sprintStart);
			delayList += delayDaysAlready;
		} catch (ParseException e) {
			log.error("Exception while parse date..." + e.getMessage());
		}
		return delayList;
	}

	private Integer potentialDelayOfStoriesPastDueDateClosedSprint(JiraIssue issueObject, String endDate,
			String startDate) throws ParseException {
		Integer delayList = 0;
		SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
		DateTime dueDate = DateTime.parse(issueObject.getDueDate());
		Date storyDueDate = sdformat.parse(String.valueOf(dueDate));
		Date sprintEndData = sdformat.parse(String.valueOf(endDate));
		DateTime sprintEnd = DateTime.parse(endDate);
		if (storyDueDate.compareTo(sprintEndData) < 0) {
			try {
				Integer delayDaysAlready = CommonUtils.getDaysBetwDate(sprintEnd, dueDate);
				if (issueObject.getRemainingEstimateMinutes() != null) {
					Integer num = (issueObject.getRemainingEstimateMinutes() / 60) / 8;
					if (num > 0) {
						delayDaysAlready = (num + (delayDaysAlready)) * (-1);
					}
				}
				delayList = delayList + delayDaysAlready;
			} catch (ParseException e) {
				log.error("Exception while parse date..." + e.getMessage());
			}
		}
		return delayList;
	}

	public String findClosedDate(JiraIssueCustomHistory issueHistoryObject, String startDate, String endDate,
			String status) {
		String date;
		for(int i =0; i<issueHistoryObject.getStorySprintDetails().size(); i++){
			if(issueHistoryObject.getStorySprintDetails().get(i).getFromStatus().equalsIgnoreCase(status)){
				date = issueHistoryObject.getStorySprintDetails().get(i).getActivityDate().toString();
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
