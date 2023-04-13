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

import static com.publicissapient.kpidashboard.common.constant.CommonConstant.OVERALL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
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
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueSprint;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

@Component
public class WastageServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WastageServiceImpl.class);
	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String SEARCH_BY_PRIORITY = "Filter by priority";
	private static final String ISSUES = "issues";
	private static final String ISSUES_CUSTOM_HISTORY = "issues custom history";
	private static final String SPRINT_DETAILS = "sprint details";
	private static final String BLOCKED_TIME = "Blocked Time";
	private static final String WAITING_TIME = "Waiting Time";
	private static final String WASTAGE = "Wastage";
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public String getQualifierType() {
		return KPICode.WASTAGE.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		DataCount trendValue = new DataCount();
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

			Filters filters = Filters.getFilter(k);
			if (Filters.SPRINT == filters) {
				projectWiseLeafNodeValue(v, trendValue, kpiElement, kpiRequest);
			}
		});
		return kpiElement;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);

		if (null != leafNode) {
			LOGGER.info("Wastage -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId().toString();
			String sprintId = leafNode.getSprintFilter().getId();
			SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintId);
			if (null != sprintDetails) {
				List<String> totalIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.TOTAL_ISSUES);
				if (CollectionUtils.isNotEmpty(totalIssues)) {
					List<JiraIssue> issueList = jiraIssueRepository.findByNumberInAndBasicProjectConfigId(totalIssues,
							basicProjectConfigId);
					List<JiraIssueCustomHistory> issueHistoryList = jiraIssueCustomHistoryRepository
							.findByStoryIDInAndBasicProjectConfigIdIn(totalIssues,
									Collections.singletonList(basicProjectConfigId));
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getTotalIssues(), issueList);
					resultListMap.put(ISSUES, new ArrayList<>(filtersIssuesList));
					resultListMap.put(ISSUES_CUSTOM_HISTORY, new ArrayList<>(issueHistoryList));
					resultListMap.put(SPRINT_DETAILS, sprintDetails);
				}
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
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		List<JiraIssueCustomHistory> allIssueHistory = (List<JiraIssueCustomHistory>) resultMap
				.get(ISSUES_CUSTOM_HISTORY);
		SprintDetails sprintDetail = (SprintDetails) resultMap.get(SPRINT_DETAILS);

		if (CollectionUtils.isNotEmpty(allIssues)) {
			LOGGER.info("Wastage -> request id : {} total jira Issues : {}", requestTrackerId, allIssues.size());

			Map<String, Map<String, List<JiraIssue>>> typeAndPriorityWiseIssues = allIssues.stream().collect(
					Collectors.groupingBy(JiraIssue::getTypeName, Collectors.groupingBy(JiraIssue::getPriority)));

			Set<String> issueTypes = new HashSet<>();
			Set<String> priorities = new HashSet<>();
			List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
			List<Integer> overAllBlockedTime = Arrays.asList(0);
			List<Integer> overAllWaitedTime = Arrays.asList(0);
			List<Integer> overAllWastedTime = Arrays.asList(0);
			List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(latestSprint.getProjectFilter().getBasicProjectConfigId());

			List<List<String>> fetchBlockAndWaitStatus = filedMappingExist(fieldMapping);

			List<String> blockedStatusList = fetchBlockAndWaitStatus.get(0);
			List<String> waitStatusList = fetchBlockAndWaitStatus.get(1);
			typeAndPriorityWiseIssues
					.forEach((issueType, priorityWiseIssue) -> priorityWiseIssue.forEach((priority, issues) -> {
						issueTypes.add(issueType);
						priorities.add(priority);
						List<IterationKpiModalValue> modalValues = new ArrayList<>();
						int blockedTime = 0;
						int waitedTime = 0;
						for (JiraIssue jiraIssue : issues) {
							int jiraIssueWaitedTime = 0;
							int jiraIssueBlockedTime = 0;
							JiraIssueCustomHistory issueCustomHistory = allIssueHistory.stream()
									.filter(jiraIssueCustomHistory -> jiraIssueCustomHistory.getStoryID()
											.equals(jiraIssue.getNumber()))
									.findFirst().orElse(new JiraIssueCustomHistory());

							List<Integer> waitedTimeAndBlockedTime = calculateWaitAndBlockTime(issueCustomHistory,
									sprintDetail, blockedStatusList, waitStatusList);
							jiraIssueWaitedTime = waitedTimeAndBlockedTime.get(0);
							jiraIssueBlockedTime = waitedTimeAndBlockedTime.get(1);
							if (jiraIssueWaitedTime != 0) {
								waitedTime += jiraIssueWaitedTime;
								overAllWaitedTime.set(0, overAllWaitedTime.get(0) + jiraIssueWaitedTime);
							}
							if (jiraIssueBlockedTime != 0) {
								blockedTime += jiraIssueBlockedTime;
								overAllBlockedTime.set(0, overAllBlockedTime.get(0) + jiraIssueBlockedTime);
							}
							KPIExcelUtility.populateIterationDataForWastage(overAllmodalValues, modalValues, jiraIssue, jiraIssueBlockedTime,
									jiraIssueWaitedTime, fieldMapping);
						}
						List<IterationKpiData> data = new ArrayList<>();
						IterationKpiData wastage = new IterationKpiData(WASTAGE,
								Double.valueOf((waitedTime + blockedTime)), null, null, CommonConstant.DAY, modalValues);
						IterationKpiData blocked = new IterationKpiData(BLOCKED_TIME, Double.valueOf(blockedTime), null,
								null, CommonConstant.DAY, null);
						IterationKpiData waited = new IterationKpiData(WAITING_TIME, Double.valueOf(waitedTime), null,
								null, CommonConstant.DAY, null);
						data.add(wastage);
						data.add(blocked);
						data.add(waited);
						IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, priority, data);
						iterationKpiValues.add(iterationKpiValue);
					}));
			List<IterationKpiData> data = new ArrayList<>();
			overAllWastedTime.set(0, overAllWaitedTime.get(0) + overAllBlockedTime.get(0));
			IterationKpiData overAllWastage = new IterationKpiData(WASTAGE, Double.valueOf(overAllWastedTime.get(0)),
					null, null, CommonConstant.DAY, overAllmodalValues);
			IterationKpiData overAllBlocked = new IterationKpiData(BLOCKED_TIME,
					Double.valueOf(overAllBlockedTime.get(0)), null, null, CommonConstant.DAY, null);
			IterationKpiData overAllWaited = new IterationKpiData(WAITING_TIME,
					Double.valueOf(overAllWaitedTime.get(0)), null, null, CommonConstant.DAY, null);
			data.add(overAllWastage);
			data.add(overAllBlocked);
			data.add(overAllWaited);
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
			kpiElement.setModalHeads(KPIExcelColumn.WASTAGE.getColumns());
			kpiElement.setTrendValueList(trendValue);
		}
	}

	/**
	 * Check for the fieldMapping and return blockStatus and waitStatus as list
	 * 
	 * @param fieldMapping
	 * @return List<List<String>>
	 */
	private List<List<String>> filedMappingExist(FieldMapping fieldMapping) {
		List<String> blockedStatus = new ArrayList<>();
		List<String> waitStatus = new ArrayList<>();
		if (null != fieldMapping) {
			if (CollectionUtils.isNotEmpty(fieldMapping.getJiraBlockedStatus()))
				blockedStatus = fieldMapping.getJiraBlockedStatus();

			if (CollectionUtils.isNotEmpty(fieldMapping.getJiraWaitStatus()))
				waitStatus = fieldMapping.getJiraWaitStatus();
		}
		return Arrays.asList(blockedStatus, waitStatus);
	}

	/**
	 * Calculate the waitTime and BlockTime
	 *
	 * @param issueCustomHistory
	 * @param sprintDetail
	 * @return List<Integer>
	 */
	List<Integer> calculateWaitAndBlockTime(JiraIssueCustomHistory issueCustomHistory, SprintDetails sprintDetail,
			List<String> blockedStatusList, List<String> waitStatusList) {
		List<JiraIssueSprint> storySprintDetails = new ArrayList<>();
		List<Integer> resultList = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(issueCustomHistory.getStorySprintDetails())) {
			storySprintDetails = issueCustomHistory.getStorySprintDetails();
		}
		int blockedTime = 0;
		int waitedTime = 0;
		for (int i = 0; i < storySprintDetails.size(); i++) {
			JiraIssueSprint entry = storySprintDetails.get(i);

			blockedTime = calculateBlockAndWaitTimeBasedOnFieldMapping(entry, blockedStatusList,
					storySprintDetails, i, sprintDetail, blockedTime);
			waitedTime = calculateBlockAndWaitTimeBasedOnFieldMapping(entry, waitStatusList, storySprintDetails,
					i, sprintDetail, waitedTime);
		}

		resultList.add( calculateBlockandwaitTimeinDays(waitedTime));
		resultList.add(calculateBlockandwaitTimeinDays(blockedTime));
		return resultList;

	}

	private int calculateBlockandwaitTimeinDays(int blockedTime) {

		int blockTimeinMin = (blockedTime / 24) * 8 * 60;
		int remainingBlocktimeInMin = (blockedTime % 24) * 60;
		if (remainingBlocktimeInMin >= 480) {
			blockTimeinMin = blockTimeinMin + 480;
		} else {
			blockTimeinMin = blockTimeinMin + remainingBlocktimeInMin;
		}

		return blockTimeinMin;

	}


	/**
	 * Calculate the wait and block time w.r.t fieldMappingStatus
	 *
	 * @param entry
	 * @param fieldMappingStatus
	 * @param storySprintDetails
	 * @param index
	 * @param sprintDetails
	 * @param time
	 * @return int
	 */
	private int calculateBlockAndWaitTimeBasedOnFieldMapping(JiraIssueSprint entry, List<String> fieldMappingStatus,
			List<JiraIssueSprint> storySprintDetails, int index, SprintDetails sprintDetails, int time) {
		DateTime sprintStartDate = DateUtil.stringToDateTime(sprintDetails.getStartDate(), DATE_TIME_FORMAT);
		DateTime sprintEndDate = DateUtil.stringToDateTime(sprintDetails.getEndDate(), DATE_TIME_FORMAT);
		DateTime entryActivityDate = entry.getActivityDate();
		if (CollectionUtils.isNotEmpty(fieldMappingStatus) && fieldMappingStatus.contains(entry.getFromStatus())) {
			int hours = 0;
			// Checking for indexOutOfBound in storySprintDetails list
			if (storySprintDetails.size() == index + 1) {
				hours = hoursForLastEntryOfStorySprintDetails(sprintDetails, sprintStartDate, sprintEndDate,
						entryActivityDate);
			} else {
				// Find fetch the next element of storySprintDetails
				JiraIssueSprint nextEntry = storySprintDetails.get(index + 1);
				DateTime nextEntryActivityDate = nextEntry.getActivityDate();
				// Checking if both alternate element are inside the sprint start and end date
				if (!(entryActivityDate.isBefore(sprintStartDate) && nextEntryActivityDate.isBefore(sprintStartDate))
						&& !(entryActivityDate.isAfter(sprintEndDate)
								&& nextEntryActivityDate.isAfter(sprintEndDate))) {
					hours = hoursForEntriesInBetweenSprint(sprintStartDate, sprintEndDate, entryActivityDate,
							nextEntryActivityDate);
				}
			}
			if (hours != 0)
				time += hours;
		}
		return time;
	}

	// Calculate the time for entries which lies between sprint start and end date
	// or one of them is inside sprint start end date
	private int hoursForEntriesInBetweenSprint(DateTime sprintStartDate, DateTime sprintEndDate,
			DateTime entryActivityDate, DateTime nextEntryActivityDate) {
		int hours;
		if (nextEntryActivityDate.isBefore(sprintEndDate)) {
			if (entryActivityDate.isAfter(sprintStartDate)) {
				hours = Hours.hoursBetween(entryActivityDate,  nextEntryActivityDate).getHours()
						- minusHoursOfWeekEndDays(entryActivityDate, nextEntryActivityDate);
			} else {
				hours = Hours.hoursBetween(sprintStartDate,  nextEntryActivityDate).getHours()
						- minusHoursOfWeekEndDays(sprintStartDate, nextEntryActivityDate);
			}
		} else {
			if (entryActivityDate.isAfter(sprintStartDate)) {
				hours = Hours.hoursBetween(entryActivityDate,  sprintEndDate).getHours()
						- minusHoursOfWeekEndDays(entryActivityDate, sprintEndDate);
			} else {
				hours = Hours.hoursBetween(sprintStartDate,  sprintEndDate).getHours()
						- minusHoursOfWeekEndDays(sprintStartDate, sprintEndDate);
			}
		}
		return hours;
	}

	// Calculate the time for last entry of storySprintDetails
	private int hoursForLastEntryOfStorySprintDetails(SprintDetails sprintDetails, DateTime sprintStartDate,
			DateTime sprintEndDate, DateTime entryActivityDate) {
		int hours = 0;
		if (entryActivityDate.isAfter(sprintStartDate)) {
			if (entryActivityDate.isBefore(sprintEndDate)) {
				if (Objects.equals(sprintDetails.getState(), SprintDetails.SPRINT_STATE_ACTIVE)) {
					hours = Hours.hoursBetween(entryActivityDate,  DateTime.now()).getHours()
							- minusHoursOfWeekEndDays(entryActivityDate, DateTime.now());
				} else {

					hours = Hours.hoursBetween(entryActivityDate,  sprintEndDate).getHours()
							- minusHoursOfWeekEndDays(entryActivityDate, sprintEndDate);
				}
			}
		} else {
			if (Objects.equals(sprintDetails.getState(), SprintDetails.SPRINT_STATE_ACTIVE)) {
				DateTime currDate = DateTime.now();
				hours = Hours.hoursBetween(sprintStartDate,  currDate).getHours()
				- minusHoursOfWeekEndDays(sprintStartDate, currDate);
			} else {
				hours = Hours.hoursBetween(sprintStartDate,  sprintEndDate).getHours()
						- minusHoursOfWeekEndDays(sprintStartDate, sprintEndDate);
			}
		}
		return hours;
	}

	public boolean isWeekEnd(DateTime dateTime) {
		int dayOfWeek = dateTime.getDayOfWeek();
		return dayOfWeek == 6 || dayOfWeek == 7;
	}

	public int saturdaySundayCount(DateTime d1, DateTime d2) {
		int countWeekEnd = 0;
		while (!d1.isAfter(d2)) {
			if (isWeekEnd(d1)) {
				countWeekEnd++;
			}
			d1 = d1.plusDays(1);
		}
		return countWeekEnd;
	}

	public int minusHoursOfWeekEndDays(DateTime d1, DateTime d2) {
		int countOfWeekEndDays = saturdaySundayCount(d1, d2);
		if (countOfWeekEndDays != 0) {
			return countOfWeekEndDays * 24;
		} else {
			return 0;
		}
	}

	// used for populating the Excel file

}
