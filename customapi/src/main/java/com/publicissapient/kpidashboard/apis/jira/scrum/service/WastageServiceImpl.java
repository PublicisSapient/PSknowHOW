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
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.*;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.publicissapient.kpidashboard.common.constant.CommonConstant.OVERALL;

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
	private static final String HOURS = "Hours";

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
							populateIterationData(overAllmodalValues, modalValues, jiraIssue, jiraIssueBlockedTime,
									jiraIssueWaitedTime, fieldMapping);
						}
						List<IterationKpiData> data = new ArrayList<>();
						IterationKpiData wastage = new IterationKpiData(WASTAGE,
								Double.valueOf((waitedTime + blockedTime)), null, null, HOURS, modalValues);
						IterationKpiData blocked = new IterationKpiData(BLOCKED_TIME, Double.valueOf(blockedTime), null,
								null, HOURS, null);
						IterationKpiData waited = new IterationKpiData(WAITING_TIME, Double.valueOf(waitedTime), null,
								null, HOURS, null);
						data.add(wastage);
						data.add(blocked);
						data.add(waited);
						IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, priority, data);
						iterationKpiValues.add(iterationKpiValue);
					}));
			List<IterationKpiData> data = new ArrayList<>();
			overAllWastedTime.set(0, overAllWaitedTime.get(0) + overAllBlockedTime.get(0));
			IterationKpiData overAllWastage = new IterationKpiData(WASTAGE, Double.valueOf(overAllWastedTime.get(0)),
					null, null, HOURS, overAllmodalValues);
			IterationKpiData overAllBlocked = new IterationKpiData(BLOCKED_TIME,
					Double.valueOf(overAllBlockedTime.get(0)), null, null, HOURS, null);
			IterationKpiData overAllWaited = new IterationKpiData(WAITING_TIME,
					Double.valueOf(overAllWaitedTime.get(0)), null, null, HOURS, null);
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
		List<JiraHistoryChangeLog> statusUpdationLogs = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(issueCustomHistory.getStatusUpdationLog())) {
			statusUpdationLogs = issueCustomHistory.getStatusUpdationLog();
		}
		int blockedTime = 0;
		int waitedTime = 0;
		for (int i = 0; i < statusUpdationLogs.size(); i++) {
			JiraHistoryChangeLog entry = statusUpdationLogs.get(i);

			blockedTime = calculateBlockAndWaitTimeBasedOnFieldMapping(entry, blockedStatusList, statusUpdationLogs, i,
					sprintDetail, blockedTime);
			waitedTime = calculateBlockAndWaitTimeBasedOnFieldMapping(entry, waitStatusList, statusUpdationLogs, i,
					sprintDetail, waitedTime);
		}
		return Arrays.asList(waitedTime, blockedTime);
	}

	/**
	 * Calculate the wait and block time w.r.t fieldMappingStatus
	 * 
	 * @param entry
	 * @param fieldMappingStatus
	 * @param statusUpdationLogs
	 * @param index
	 * @param sprintDetails
	 * @param time
	 * @return int
	 */
	private int calculateBlockAndWaitTimeBasedOnFieldMapping(JiraHistoryChangeLog entry,
			List<String> fieldMappingStatus, List<JiraHistoryChangeLog> statusUpdationLogs, int index,
			SprintDetails sprintDetails, int time) {
		LocalDateTime sprintStartDate = DateUtil.convertingStringToLocalDateTime(sprintDetails.getStartDate(),DateUtil.TIME_FORMAT);
		LocalDateTime sprintEndDate = DateUtil.convertingStringToLocalDateTime(sprintDetails.getEndDate(),DateUtil.TIME_FORMAT);
		LocalDateTime entryActivityDate = entry.getUpdatedOn();
		if (CollectionUtils.isNotEmpty(fieldMappingStatus) && fieldMappingStatus.contains(entry.getChangedTo())) {
			int minutes = 0;
			// Checking for indexOutOfBound in storySprintDetails list
			if (statusUpdationLogs.size() == index + 1) {
				minutes = minutesForLastEntryOfStorySprintDetails(sprintDetails, sprintStartDate, sprintEndDate,
						entryActivityDate);
			} else {
				// Find fetch the next element of storySprintDetails
				JiraHistoryChangeLog nextEntry = statusUpdationLogs.get(index + 1);
				LocalDateTime nextEntryActivityDate = nextEntry.getUpdatedOn();
				// Checking if both alternate element are inside the sprint start and end date
				if (!(entryActivityDate.isBefore(sprintStartDate) && nextEntryActivityDate.isBefore(sprintStartDate))
						&& !(entryActivityDate.isAfter(sprintEndDate)
								&& nextEntryActivityDate.isAfter(sprintEndDate))) {
					minutes = minutesForEntriesInBetweenSprint(sprintStartDate, sprintEndDate, entryActivityDate,
							nextEntryActivityDate);
				}
			}
			if (minutes != 0)
				time += minutes;
		}
		return time;
	}

	// Calculate the time for entries which lies between sprint start and end date
	// or one of them is inside sprint start end date
	private int minutesForEntriesInBetweenSprint(LocalDateTime sprintStartDate, LocalDateTime sprintEndDate,
												 LocalDateTime entryActivityDate, LocalDateTime nextEntryActivityDate) {
		int minutes;
		if (nextEntryActivityDate.isBefore(sprintEndDate)) {
			if (entryActivityDate.isAfter(sprintStartDate)) {
				minutes = (int) (ChronoUnit.MINUTES.between(entryActivityDate, nextEntryActivityDate)
										- minusMinutesInWeekEndDays(entryActivityDate, nextEntryActivityDate));
			} else {
				minutes = (int) ChronoUnit.MINUTES.between(sprintStartDate, nextEntryActivityDate)
						- minusMinutesInWeekEndDays(sprintStartDate, nextEntryActivityDate);
			}
		} else {
			if (entryActivityDate.isAfter(sprintStartDate)) {
				minutes = (int) ChronoUnit.MINUTES.between(entryActivityDate, sprintEndDate)
						- minusMinutesInWeekEndDays(entryActivityDate, sprintEndDate);
			} else {
				minutes = (int) ChronoUnit.MINUTES.between(sprintStartDate, sprintEndDate)
						- minusMinutesInWeekEndDays(sprintStartDate, sprintEndDate);
			}
		}
		return minutes;
	}

	// Calculate the time for last entry of storySprintDetails
	private int minutesForLastEntryOfStorySprintDetails(SprintDetails sprintDetails, LocalDateTime sprintStartDate,
			LocalDateTime sprintEndDate, LocalDateTime entryActivityDate) {
		int minutes = 0;
		if (entryActivityDate.isAfter(sprintStartDate)) {
			if (entryActivityDate.isBefore(sprintEndDate)) {
				if (Objects.equals(sprintDetails.getState(), SprintDetails.SPRINT_STATE_ACTIVE)) {
					minutes = (int) (ChronoUnit.MINUTES.between(entryActivityDate, LocalDateTime.now())
												- minusMinutesInWeekEndDays(entryActivityDate, LocalDateTime.now()));
				} else {
					minutes = (int) (ChronoUnit.MINUTES.between(entryActivityDate, sprintEndDate)
												- minusMinutesInWeekEndDays(entryActivityDate, sprintEndDate));
				}
			}
		} else {
			if (Objects.equals(sprintDetails.getState(), SprintDetails.SPRINT_STATE_ACTIVE)) {
				LocalDateTime currDate = LocalDateTime.now();
				minutes = (int) (ChronoUnit.MINUTES.between(sprintStartDate, currDate)
										- minusMinutesInWeekEndDays(sprintStartDate, currDate));
			} else {
				minutes = (int) ChronoUnit.MINUTES.between(sprintStartDate, sprintEndDate)
						- minusMinutesInWeekEndDays(sprintStartDate, sprintEndDate);
			}
		}
		return minutes;
	}

	public boolean isWeekEnd(LocalDateTime localDateTime) {
		int dayOfWeek = localDateTime.getDayOfWeek().getValue();
		return dayOfWeek == 6 || dayOfWeek == 7;
	}

	public int saturdaySundayCount(LocalDateTime d1, LocalDateTime d2) {
		int countWeekEnd = 0;
		while (!d1.isAfter(d2)) {
			if (isWeekEnd(d1)) {
				countWeekEnd++;
			}
			d1 = d1.plusDays(1);
		}
		return countWeekEnd;
	}

	public int minusMinutesInWeekEndDays(LocalDateTime d1, LocalDateTime d2) {
		int countOfWeekEndDays = saturdaySundayCount(d1, d2);
		if (countOfWeekEndDays != 0) {
			return countOfWeekEndDays * 24 * 60;
		} else {
			return 0;
		}
	}

	// used for populating the Excel file
	public void populateIterationData(List<IterationKpiModalValue> overAllmodalValues,
			List<IterationKpiModalValue> modalValues, JiraIssue jiraIssue, int blockedTime, int waitTime,
			FieldMapping fieldMapping) {
		int wastageTime = blockedTime + waitTime;
		int originalEstimate = 0;
		IterationKpiModalValue iterationKpiModalValue = new IterationKpiModalValue();
		iterationKpiModalValue.setIssueId(jiraIssue.getNumber());
		iterationKpiModalValue.setIssueURL(jiraIssue.getUrl());
		iterationKpiModalValue.setDescription(jiraIssue.getName());
		iterationKpiModalValue.setIssueStatus(jiraIssue.getStatus());
		iterationKpiModalValue.setIssueType(jiraIssue.getTypeName());
		iterationKpiModalValue.setPriority(jiraIssue.getPriority());

		if (null != jiraIssue.getStoryPoints() && StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			iterationKpiModalValue.setIssueSize(jiraIssue.getStoryPoints().toString());
		}
		if (null != jiraIssue.getOriginalEstimateMinutes()
				&& StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.ACTUAL_ESTIMATION)) {
			originalEstimate = jiraIssue.getOriginalEstimateMinutes() / 60;
			iterationKpiModalValue.setIssueSize(originalEstimate + " hrs");
		}
		if ((blockedTime != 0)) {
			iterationKpiModalValue.setBlockedTime(String.valueOf(blockedTime / 60 + "h " + blockedTime % 60 + " m"));
		} else {
			iterationKpiModalValue.setBlockedTime(blockedTime + " h");
		}
		if ((waitTime != 0)) {
			iterationKpiModalValue.setWaitTime(String.valueOf(waitTime / 60 + "h " + waitTime % 60 + " m"));
		} else {
			iterationKpiModalValue.setWaitTime(waitTime + " h");
		}
		if ((wastageTime != 0)) {
			iterationKpiModalValue.setWastage(String.valueOf(wastageTime / 60 + "h " + wastageTime % 60 + " m"));
		} else {
			iterationKpiModalValue.setWastage(String.valueOf(wastageTime + " h"));
		}
		modalValues.add(iterationKpiModalValue);
		overAllmodalValues.add(iterationKpiModalValue);
	}

}
