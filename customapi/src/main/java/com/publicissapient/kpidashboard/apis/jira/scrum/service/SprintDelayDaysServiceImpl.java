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

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class SprintDelayDaysServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SprintDelayDaysServiceImpl.class);

	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String SEARCH_BY_PRIORITY = "Filter by status";
	public static final String UNCHECKED = "unchecked";
	private static final String ISSUES = "issues";
	private static final String SPRINT = "sprint";
	private static final String JIRAISSUEMAP = "jiraIssueMap";

	private static final String JIRASPILLEDISSUEMAP = "jiraSpilledIssueMap";
	private static final String JIRASPILLEDISSUECUSTOMHISTORYMAP = "jiraSpilledIssueCustomHistoryMap";
	private static final String JIRAOPENISSUEMAP = "jiraOpenIssueMap";
	private static final String COMPLETED_ISSUES = "completedIssues";

	private static final String COMPLETED_ISSUES_ANOTHER_SPRINT = "issuesCompletedInAnotherSprint";
	private static final String TOTAL_ISSUES = "totalIssues";
	private static final String TOTAL_ISSUES_KEYS = "totalIssuesKeys";
	private static final String NOT_COMPLETED_ISSUES = "issuesNotCompletedInCurrentSprint";
	private static final String JIRAISSUECUSTOMHISTORYMAP = "jiraIssueCustomHistoryMap";
	private static final String JIRAOPENISSUECUSTOMHISTORYMAP = "jiraOpenIssueCustomHistoryMap";
	private static final String ISSUE_COUNT = "Issue Count";
	private static final String STORY_POINT = "Story Point";
	private static final String REM_HOURS = "Hours";
	private static final String OVERALL = "Overall";
	private static final String SP = "SP";
	private static final String HOURS = "Hours";

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private ConfigHelperService configHelperService;

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
					throw new RuntimeException(e);
				}
			}
		});
		return kpiElement;
	}

	@Override
	public String getQualifierType() {
		return KPICode.DELAY_DAYS.name();
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
			String basicProjectConfigId = leafNode.getProjectFilter()
					.getBasicProjectConfigId().toString();
			String sprintId = leafNode.getSprintFilter().getId();
			SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintId);
			if (null != sprintDetails) {

				List<String> issuesCompletedInAnotherSprint = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.COMPLETED_ISSUES_ANOTHER_SPRINT);

				List<String> completedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						COMPLETED_ISSUES);

				List<String> issuesNotCompletedInCurrentSprint = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						NOT_COMPLETED_ISSUES);

				Set<JiraIssue> filtersOpenIssuesList = new HashSet<>();
				Set<JiraIssue> filtersIssuesList = new HashSet<>();
				Set<JiraIssue> filtersSpilledIssuesList = new HashSet<>();
				Map<String, JiraIssue> jiraOpenIssueMap = new HashMap<>();
				Map<String, JiraIssue> jiraIssueMap = new HashMap<>();
				Map<String, JiraIssue> jiraSpilledIssueMap = new HashMap<>();
				Map<String, JiraIssueCustomHistory> jiraOpenIssueCustomHistoryMap = new HashMap<>();
				Map<String, JiraIssueCustomHistory> jiraSpilledIssueCustomHistoryMap = new HashMap<>();
				Map<String, JiraIssueCustomHistory> jiraIssueCustomHistoryMap = new HashMap<>();
				List<JiraIssue> totalIssues = new ArrayList<>();

				if(CollectionUtils.isNotEmpty(issuesCompletedInAnotherSprint)){
					List<JiraIssue> spilledIssuesList = jiraIssueRepository
							.findByNumberInAndBasicProjectConfigId(issuesNotCompletedInCurrentSprint, basicProjectConfigId);

					List<JiraIssueCustomHistory> storiesHistory = jiraIssueCustomHistoryRepository
							.findByStoryIDInAndBasicProjectConfigIdIn(issuesCompletedInAnotherSprint, Arrays.asList(basicProjectConfigId));

					jiraSpilledIssueCustomHistoryMap = storiesHistory.stream()
							.collect(Collectors.toMap(JiraIssueCustomHistory::getStoryID, Function.identity()));

					filtersSpilledIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getNotCompletedIssues(), spilledIssuesList);

					jiraSpilledIssueMap = filtersSpilledIssuesList.stream()
							.collect(Collectors.toMap(JiraIssue::getNumber, Function.identity()));
				}

				if (CollectionUtils.isNotEmpty(issuesNotCompletedInCurrentSprint)){
					List<JiraIssue> openIssueList = jiraIssueRepository
							.findByNumberInAndBasicProjectConfigId(issuesNotCompletedInCurrentSprint, basicProjectConfigId);

					List<JiraIssueCustomHistory> storiesHistory = jiraIssueCustomHistoryRepository
							.findByStoryIDInAndBasicProjectConfigIdIn(issuesNotCompletedInCurrentSprint, Arrays.asList(basicProjectConfigId));

					jiraOpenIssueCustomHistoryMap = storiesHistory.stream()
							.collect(Collectors.toMap(JiraIssueCustomHistory::getStoryID, Function.identity()));

					filtersOpenIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getNotCompletedIssues(), openIssueList);

					jiraOpenIssueMap = filtersOpenIssuesList.stream()
							.collect(Collectors.toMap(JiraIssue::getNumber, Function.identity()));

				}

				if (CollectionUtils.isNotEmpty(completedIssues)) {
					List<JiraIssue> issueList = jiraIssueRepository
							.findByNumberInAndBasicProjectConfigId(completedIssues, basicProjectConfigId);


					List<JiraIssueCustomHistory> storiesHistory = jiraIssueCustomHistoryRepository
							.findByStoryIDInAndBasicProjectConfigIdIn(completedIssues, Arrays.asList(basicProjectConfigId));

					jiraIssueCustomHistoryMap = storiesHistory.stream()
							.collect(Collectors.toMap(JiraIssueCustomHistory::getStoryID, Function.identity()));

					filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getCompletedIssues(), issueList);

					jiraIssueMap = filtersIssuesList.stream()
							.collect(Collectors.toMap(JiraIssue::getNumber, Function.identity()));
				}

				resultListMap.put(ISSUES, new ArrayList<>(filtersIssuesList));
				resultListMap.put(COMPLETED_ISSUES_ANOTHER_SPRINT, issuesCompletedInAnotherSprint);
				resultListMap.put(COMPLETED_ISSUES, completedIssues);
				resultListMap.put(NOT_COMPLETED_ISSUES, issuesNotCompletedInCurrentSprint);
				resultListMap.put(SPRINT, sprintDetails);
				resultListMap.put(JIRAISSUEMAP, jiraIssueMap);
				resultListMap.put(JIRAISSUECUSTOMHISTORYMAP, jiraIssueCustomHistoryMap);
				resultListMap.put(JIRASPILLEDISSUEMAP, jiraSpilledIssueMap);
				resultListMap.put(JIRASPILLEDISSUECUSTOMHISTORYMAP, jiraSpilledIssueCustomHistoryMap);
				resultListMap.put(JIRAOPENISSUEMAP, jiraOpenIssueMap);
				resultListMap.put(JIRAOPENISSUECUSTOMHISTORYMAP, jiraOpenIssueCustomHistoryMap);
			}
		}
		return resultListMap;
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
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
				.get(latestSprint.getProjectFilter().getBasicProjectConfigId());

		//List<JiraIssue> allIssues = ((List<JiraIssue>) resultMap.get(ISSUES));
		List<String> completedIssues = (List<String>) resultMap.get(COMPLETED_ISSUES);
		List<String> openIssues = (List<String>) resultMap.get(NOT_COMPLETED_ISSUES);
		List<String> spilledIssues = (List<String>) resultMap.get(COMPLETED_ISSUES_ANOTHER_SPRINT);
		Map<String, JiraIssue> jiraMap = (Map<String, JiraIssue>) resultMap.get(JIRAISSUEMAP);
		Map<String, JiraIssueCustomHistory> jiraHistoryMap = (Map<String, JiraIssueCustomHistory>) resultMap.get(JIRAISSUECUSTOMHISTORYMAP);
		Map<String, JiraIssue> jiraOpenMap = (Map<String, JiraIssue>) resultMap.get(JIRAOPENISSUEMAP);
		Map<String, JiraIssueCustomHistory> jiraOpenHistoryMap = (Map<String, JiraIssueCustomHistory>) resultMap.get(JIRAOPENISSUECUSTOMHISTORYMAP);
		Map<String, JiraIssue> jiraSpilledMap = (Map<String, JiraIssue>) resultMap.get(JIRASPILLEDISSUEMAP);
		Map<String, JiraIssueCustomHistory> jiraSpilledHistoryMap = (Map<String, JiraIssueCustomHistory>) resultMap.get(JIRASPILLEDISSUECUSTOMHISTORYMAP);

		SprintDetails value = (SprintDetails) resultMap.get("sprint");
		String startDate = value.getStartDate();
		String endDate = value.getEndDate();


//		List<String> testingStatuses = fieldMapping.getJiraStatusForQa();
//		Double minutesInDay = fieldMapping.getWorkingHoursDayCPT() * 60;
		if (CollectionUtils.isNotEmpty((List<JiraIssue>) resultMap.get(ISSUES))) {
			List<Integer> finalCalculation = new ArrayList<>();
			List<JiraIssue> overAllJiraIssueList = new ArrayList<>();
 			Map<String, List<IterationKpiModalValue>> closedIssuesdelay = findDelayofClosedIssues(completedIssues, jiraMap, jiraHistoryMap,startDate,endDate);
//			//-25, ,5 2
			Map<String, List<IterationKpiModalValue>> openIssuesDelay = findDelayOfOpenIssues(openIssues, jiraOpenMap, jiraOpenHistoryMap, startDate, endDate);
			//LOGGER.info(closedIssuesdelay+"   closedIssuesDelay");
			LOGGER.info(openIssuesDelay+"   openIssuesDelay");


			Iterator<Map.Entry<String, List<IterationKpiModalValue>>> iterator = closedIssuesdelay.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, List<IterationKpiModalValue>> entry = iterator.next();
				System.out.println(entry.getKey() + ":" + entry.getValue());
			}
			List<IterationKpiModalValue> iterationKpiModalValuesClosedSprint = closedIssuesdelay.get("delayDetails");
			List<IterationKpiModalValue> iterationKpiModalValuesOpenSprint = openIssuesDelay.get("openIssuesCausingDelay");
			List<IterationKpiModalValue> iterationKpiModalValuesNetDelay = new ArrayList<>();
			iterationKpiModalValuesNetDelay.addAll(iterationKpiModalValuesClosedSprint);
			iterationKpiModalValuesNetDelay.addAll(iterationKpiModalValuesOpenSprint);

			LOGGER.info(iterationKpiModalValuesNetDelay + "netdelaystories");

			List<IterationKpiModalValue> iterationKpiModalValuesIssuesDoneBeforeTime = closedIssuesdelay.get("issuesClosedBeforeDueDate");

			List<IterationKpiModalValue> iterationKpiModalValuesIssuesCausingDelay = new ArrayList<>();
			iterationKpiModalValuesIssuesCausingDelay.addAll(closedIssuesdelay.get("issuesClosedAfterDelayDate"));
			iterationKpiModalValuesIssuesCausingDelay.addAll(openIssuesDelay.get("openIssuesCausingDelay"));

			List<IterationKpiModalValue> netDelay = new ArrayList<>();
			List<IterationKpiModalValue> netDelayChild = new ArrayList<>();
			List<IterationKpiModalValue> issuesCausingDelay = new ArrayList<>();
			List<IterationKpiModalValue> issuesDoneBeforeTime = new ArrayList<>();

			List<IterationKpiModalValue> allIssues = iterationKpiModalValuesNetDelay;


			if (CollectionUtils.isNotEmpty(allIssues)) {
				Map<String, List<IterationKpiModalValue>> typeWiseIssues = allIssues.stream()
						.collect(Collectors.groupingBy(IterationKpiModalValue::getIssueType));



			}

			if(!iterationKpiModalValuesNetDelay.isEmpty()){
				for (IterationKpiModalValue iterationKpiModalValue:iterationKpiModalValuesNetDelay){
					populateIssueWiseData(netDelay, netDelayChild, iterationKpiModalValue);
				}
			}

			if(!iterationKpiModalValuesIssuesDoneBeforeTime.isEmpty()){
				for (IterationKpiModalValue iterationKpiModalValue:iterationKpiModalValuesIssuesDoneBeforeTime){
					populateIssueWiseData(netDelay, netDelayChild, iterationKpiModalValue);
				}
			}

//			for(int i=1; i<closedIssuesdelay.size();i++){
//				finalCalculation.add(closedIssuesdelay.get(i));
//			}

//			List<JiraIssue> allIssues = ((List<JiraIssue>) resultMap.get(ISSUES)).stream()
//					.filter(issue -> testingStatuses.contains(issue.getStatus())
//							|| (null != issue.getRemainingEstimateMinutes() && issue.getRemainingEstimateMinutes() > 0
//							&& issue.getRemainingEstimateMinutes() <= minutesInDay))
//					.collect(Collectors.toList());
//
//
//			if (CollectionUtils.isNotEmpty(allIssues)) {
//				LOGGER.info("Closure Possible Today -> request id : {} total jira Issues : {}", requestTrackerId,
//						allIssues.size());
//
//				Map<String, List<JiraIssue>> typeWiseIssues = allIssues.stream()
//						.collect(Collectors.groupingBy(JiraIssue::getTypeName));
//
//				Set<String> issueTypes = new HashSet<>();
//				List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
//				List<Integer> overAllIssueCount = Arrays.asList(0);
//				List<Double> overAllStoryPoints = Arrays.asList(0.0);
//				List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
//				typeWiseIssues.forEach((issueType, issues) -> {
//					issueTypes.add(issueType);
//					List<IterationKpiModalValue> modalValues = new ArrayList<>();
//					int issueCount = 0;
//					Double storyPoint = 0.0;
//					for (JiraIssue jiraIssue : issues) {
//						//populateIterationData(overAllmodalValues, modalValues, jiraIssue);
//						issueCount = issueCount + 1;
//						overAllIssueCount.set(0, overAllIssueCount.get(0) + 1);
//						if (null != jiraIssue.getStoryPoints()) {
//							storyPoint = storyPoint + jiraIssue.getStoryPoints();
//							overAllStoryPoints.set(0, overAllStoryPoints.get(0) + jiraIssue.getStoryPoints());
//						}
//					}
//					List<IterationKpiData> data = new ArrayList<>();
//					IterationKpiData issueCounts = new IterationKpiData(ISSUE_COUNT, Double.valueOf(issueCount), null,
//							null, "", modalValues);
//					IterationKpiData storyPoints = new IterationKpiData(STORY_POINT, storyPoint, null, null, SP, null);
//					data.add(issueCounts);
//					data.add(storyPoints);
//					IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, null, data);
//					iterationKpiValues.add(iterationKpiValue);
//				});
//				List<IterationKpiData> data = new ArrayList<>();
//				IterationKpiData overAllCount = new IterationKpiData(ISSUE_COUNT,
//						Double.valueOf(overAllIssueCount.get(0)), null, null, "", overAllmodalValues);
//				IterationKpiData overAllStPoints = new IterationKpiData(STORY_POINT, overAllStoryPoints.get(0), null,
//						null, SP, null);
//				data.add(overAllCount);
//				data.add(overAllStPoints);
//				IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, null, data);
//				iterationKpiValues.add(overAllIterationKpiValue);
//
//				// Create kpi level filters
//				IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypes);
//				IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, null);
//				trendValue.setValue(iterationKpiValues);
//				kpiElement.setFilters(iterationKpiFilters);
//				kpiElement.setSprint(latestSprint.getName());
//				kpiElement.setModalHeads(KPIExcelColumn.CLOSURES_POSSIBLE_TODAY.getColumns());
//				kpiElement.setTrendValueList(trendValue);
//			}
		}
	}

	private Map<String, List<IterationKpiModalValue>> findDelayofClosedIssues(List<String> completedIssues, Map<String, JiraIssue> jiraMap, Map<String, JiraIssueCustomHistory> jiraHistoryMap, String startDate, String endDate) {
		List<Integer> delayList = new ArrayList<>();
		Map<String, List<IterationKpiModalValue>> resultList = new HashMap<>();
		List<IterationKpiModalValue> jiraBeforeTimeIssueList = new ArrayList<>();
		List<IterationKpiModalValue> jiraAfterTimeIssueList = new ArrayList<>();
		List<IterationKpiModalValue> jiraDelayIssueList = new ArrayList<>();
		for (String story:completedIssues){
			IterationKpiModalValue iterationKpiModalValue = new IterationKpiModalValue();
			String issueNumber = story;
			JiraIssueCustomHistory issueHistoryObject = jiraHistoryMap.get(issueNumber);
			JiraIssue issueObject = jiraMap.get(issueNumber);
			Integer daysDiff = 0;
			Integer delay = 0;
			if((Objects.nonNull(issueHistoryObject))&&Objects.nonNull(issueObject)){
				String closedDate=findClosedDate(issueObject,issueHistoryObject,startDate,endDate,issueObject.getStatus());
				String dueDate = issueObject.getDueDate();
				if(StringUtils.isNotEmpty(dueDate)) {
					try {
						daysDiff = CommonUtils.getDaysBetwDate(DateTime.parse(dueDate), DateTime.parse(closedDate));

					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
					delay = daysDiff+delay;
					delayList.add(delay);
					iterationKpiModalValue = prepareStoryDetails(issueObject, delay, closedDate);
					jiraDelayIssueList.add(iterationKpiModalValue);
					if (dueDate.compareTo(closedDate) > 0) {
						iterationKpiModalValue = prepareStoryDetails(issueObject, delay, closedDate);
						jiraBeforeTimeIssueList.add(iterationKpiModalValue);
					} else {
						iterationKpiModalValue = prepareStoryDetails(issueObject, delay, closedDate);
						jiraAfterTimeIssueList.add(iterationKpiModalValue);
					}
				}
			}
		}
//		Integer count=0;
//		for (int i : delayList) {
//			count=count+i;
//		}
		resultList.put("delayDetails", jiraDelayIssueList);
		resultList.put("issuesClosedAfterDelayDate", jiraAfterTimeIssueList);
		resultList.put("issuesClosedBeforeDueDate", jiraBeforeTimeIssueList);
		return resultList;
	}

	private IterationKpiModalValue prepareStoryDetails(JiraIssue issueObject, Integer delay, String closedDate) {
		IterationKpiModalValue iterationKpiModalValue = new IterationKpiModalValue();
		iterationKpiModalValue.setIssueId(issueObject.getIssueId());
		iterationKpiModalValue.setIssueURL(issueObject.getUrl());
		iterationKpiModalValue.setIssueType(issueObject.getTypeName());
		iterationKpiModalValue.setPriority(issueObject.getPriority());
		iterationKpiModalValue.setDescription(issueObject.getName());
		iterationKpiModalValue.setIssueStatus(issueObject.getStatus());
		iterationKpiModalValue.setDueDate(issueObject.getDueDate());
		iterationKpiModalValue.setRemainingEstimateMinutes(issueObject.getRemainingEstimateMinutes());
		iterationKpiModalValue.setDelay(delay);
		return iterationKpiModalValue;
	}

	private Map<String, List<IterationKpiModalValue>> findDelayOfOpenIssues(List<String> openIssues, Map<String, JiraIssue> jiraOpenMap, Map<String, JiraIssueCustomHistory> jiraOpenHistoryMap, String startDate, String endDate) throws ParseException {

		List<Integer> totalDelayList = new ArrayList<>();
		Integer countOfIssuesOpenAndDelayed = 0;
		Map<String, List<IterationKpiModalValue>> resultList = new HashMap<>();
		List<IterationKpiModalValue> jiraDelayIssueList = new ArrayList<>();
		for (String story:openIssues){
			IterationKpiModalValue iterationKpiModalValue = new IterationKpiModalValue();
			Integer delayList = 0;
			String issueNumber = story;
			JiraIssueCustomHistory issueHistoryObject = jiraOpenHistoryMap.get(issueNumber);
			JiraIssue issueObject = jiraOpenMap.get(issueNumber);
			if((Objects.nonNull(issueHistoryObject))&&Objects.nonNull(issueObject)) {
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
						throw new RuntimeException(e);
					}
					try {
						sprintEndData = sdformat.parse(String.valueOf(endDate));
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
					try {
						todayDate = sdformat.parse(String.valueOf(currDate));
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
					try {
						sprintStartDate = sdformat.parse(String.valueOf(startDate));
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				/*
				case of stories past the due date and are closed
				curr date, duedate, sprintenddate
				*/

					if (todayDate.compareTo(storyDueDate) > 0) { //if current date > than story due date .i.e story past the due date
						if (todayDate.compareTo(sprintEndData) > 0) {//if curr date is > sprint end date .i.e closed sprint case
							try {
								delayList = potentialDelayOfStoriesPastDueDateClosedSprint(issueObject, endDate, startDate);
								iterationKpiModalValue = prepareStoryDetails(issueObject, delayList, "0");
								jiraDelayIssueList.add(iterationKpiModalValue);
							} catch (ParseException e) {
								throw new RuntimeException(e);
							}
						} else {//if curr date is < sprint end date .i.e active sprint case, Story spillage case
							if (storyDueDate.compareTo(sprintStartDate) < 0) { //spilled story and due date not changed < sprint start date
								delayList = spilledIssues(startDate);
								iterationKpiModalValue = prepareStoryDetails(issueObject, delayList, "0");
								jiraDelayIssueList.add(iterationKpiModalValue);
							} else {
								delayList = issuesPastDueDateInsideSprint(dueDate, startDate, endDate);
								iterationKpiModalValue = prepareStoryDetails(issueObject, delayList, "0");
								jiraDelayIssueList.add(iterationKpiModalValue);
							}
						}
					} else { //if current date is less than story due date, stories inside due date but not closed, Active story case
						delayList = potentialDelayOfStoriesInsideDueDate(endDate, dueDate, issueObject);
						iterationKpiModalValue = prepareStoryDetails(issueObject, delayList, "0");
					}
				}
//				if(delayList>0){
//					totalDelayList.add(delayList);
//				}
			}
		}
//		Integer count=0;
//		for (int i : totalDelayList) {
//			count=count+i;
//		}
		resultList.put("openIssuesCausingDelay", jiraDelayIssueList);
		return resultList;
	}

	private Integer potentialDelayOfStoriesInsideDueDate(String endDate, DateTime dueDate, JiraIssue issueObject) throws ParseException {
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
				throw new RuntimeException(e);
			}
		}
		return delayList;
	}

	private Integer issuesPastDueDateInsideSprint(DateTime dueDate, String startDate, String endDate) throws ParseException {
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
			throw new RuntimeException(e);
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
			throw new RuntimeException(e);
		}
		return delayList;
	}

	private Integer potentialDelayOfStoriesPastDueDateClosedSprint(JiraIssue issueObject, String endDate, String startDate) throws ParseException {
		Integer delayList = 0;
		SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
		DateTime dueDate = DateTime.parse(issueObject.getDueDate());
		Date storyDueDate = sdformat.parse(String.valueOf(dueDate));
		Date sprintEndData = sdformat.parse(String.valueOf(endDate));
		DateTime sprintEnd = DateTime.parse(endDate);
		if(storyDueDate.compareTo(sprintEndData)<0) {
			try {
				Integer delayDaysAlready = CommonUtils.getDaysBetwDate(sprintEnd, dueDate);
				if (issueObject.getRemainingEstimateMinutes() != null) {
					Integer num = (issueObject.getRemainingEstimateMinutes() / 60) / 8;
					if (num > 0) {
						delayDaysAlready = (num + (delayDaysAlready)) * (-1);
					}
				}
				delayList = delayList+delayDaysAlready;
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		return delayList;
	}

	public String findClosedDate(JiraIssue issueObject, JiraIssueCustomHistory issueHistoryObject, String startDate, String endDate, String status) {

		String date = issueHistoryObject.getStorySprintDetails().stream().filter(f -> f.getFromStatus().equalsIgnoreCase(status)).findFirst().orElse(null).getActivityDate().toString();
		DateTime datValu = DateTime.parse(date);
		DateTime startDateValue = DateTime.parse(startDate);
		DateTime endDateValue = DateTime.parse(endDate);
		if(datValu.isAfter(startDateValue) && datValu.isBefore(endDateValue)){
			return date;
		}
		return null;
	}
}
