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

import static com.publicissapient.kpidashboard.apis.util.IterationKpiHelper.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard.JiraIterationKPIService;
import com.publicissapient.kpidashboard.apis.model.*;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WastageV2ServiceImpl extends JiraIterationKPIService {

	private static final String FILTER_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String FILTER_BY_PRIORITY = "Filter by Priority";
	private static final String ISSUES = "issues";
	private static final String ISSUES_CUSTOM_HISTORY = "issues custom history";
	private static final String SPRINT_DETAILS = "sprint details";
	private static final String FILTER_TYPE = "Multi";
	private static final String SUM = "sum";

	@Autowired
	private ConfigHelperService configHelperService;

	/**
	 * Check for the flag status
	 *
	 * @param fieldMapping
	 * @return boolean flagStatus
	 */
	private static boolean checkFlagIncludedStatus(FieldMapping fieldMapping) {
		return null != fieldMapping && StringUtils.isNotEmpty(fieldMapping.getJiraIncludeBlockedStatusKPI131())
				&& fieldMapping.getJiraIncludeBlockedStatusKPI131()
						.contains(CommonConstant.IS_FLAG_STATUS_INCLUDED_FOR_WASTAGE);
	}

	@Override
	public String getQualifierType() {
		return KPICode.WASTAGE.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node sprintNode)
			throws ApplicationException {
		projectWiseLeafNodeValue(sprintNode, kpiElement, kpiRequest);
		return kpiElement;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();

		if (null != leafNode) {
			log.info("Wastage -> Requested sprint : {}", leafNode.getName());
			SprintDetails dbSprintDetail = getSprintDetailsFromBaseClass();
			SprintDetails sprintDetails;
			if (null != dbSprintDetail) {
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
						.get(leafNode.getProjectFilter().getBasicProjectConfigId());
				// to modify sprint details on the basis of configuration for the project
				List<JiraIssueCustomHistory> totalHistoryList = getJiraIssuesCustomHistoryFromBaseClass();
				List<JiraIssue> totalJiraIssueList = getJiraIssuesFromBaseClass();
				Set<String> issueList = totalJiraIssueList.stream().map(JiraIssue::getNumber)
						.collect(Collectors.toSet());

				sprintDetails = transformIterSprintdetail(totalHistoryList, issueList, dbSprintDetail,
						fieldMapping.getJiraIterationIssuetypeKPI131(),
						fieldMapping.getJiraIterationCompletionStatusKPI131(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> totalIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.TOTAL_ISSUES);
				if (CollectionUtils.isNotEmpty(totalIssues)) {
					List<JiraIssue> filteredJiraIssue = getFilteredJiraIssue(totalIssues, totalJiraIssueList);
					List<JiraIssueCustomHistory> issueHistoryList = getFilteredJiraIssueHistory(totalIssues,
							totalHistoryList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getTotalIssues(), filteredJiraIssue);
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
	 * @param sprintLeafNode
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(Node sprintLeafNode, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNode, null, null, kpiRequest);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		List<JiraIssueCustomHistory> allIssueHistory = (List<JiraIssueCustomHistory>) resultMap
				.get(ISSUES_CUSTOM_HISTORY);
		SprintDetails sprintDetail = (SprintDetails) resultMap.get(SPRINT_DETAILS);

		if (CollectionUtils.isNotEmpty(allIssues)) {
			log.info("Wastage -> request id : {} total jira Issues : {}", requestTrackerId, allIssues.size());

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(Objects.requireNonNull(sprintLeafNode).getProjectFilter().getBasicProjectConfigId());

			List<List<String>> fetchBlockAndWaitStatus = filedMappingExist(fieldMapping);
			boolean flagIncluded = checkFlagIncludedStatus(fieldMapping);
			log.info("Is flag included for wastage kpi calculation  {}", flagIncluded);

			List<String> blockedStatusList = fetchBlockAndWaitStatus.get(0);
			List<String> waitStatusList = fetchBlockAndWaitStatus.get(1);

			Map<String, IssueKpiModalValue> issueKpiModalObject = KpiDataHelper.createMapOfIssueModal(allIssues);
			allIssues.forEach(issue -> {
				KPIExcelUtility.populateIssueModal(issue, fieldMapping, issueKpiModalObject);
				IssueKpiModalValue data = issueKpiModalObject.get(issue.getNumber());
				JiraIssueCustomHistory issueCustomHistory = allIssueHistory.stream()
						.filter(jiraIssueCustomHistory -> jiraIssueCustomHistory.getStoryID().equals(issue.getNumber()))
						.findFirst().orElse(new JiraIssueCustomHistory());
				List<Integer> waitedTimeAndBlockedTime = calculateWaitAndBlockTime(issueCustomHistory, sprintDetail,
						blockedStatusList, waitStatusList, flagIncluded);
				data.setIssueBlockedTime(waitedTimeAndBlockedTime.get(1));
				data.setIssueWaitTime(waitedTimeAndBlockedTime.get(0));

			});

			kpiElement.setSprint(sprintLeafNode.getName());
			kpiElement.setModalHeads(KPIExcelColumn.WASTAGE.getColumns());
			kpiElement.setIssueData(new HashSet<>(issueKpiModalObject.values()));
			kpiElement.setFilterGroup(createFilterGroup());
			kpiElement.setDataGroup(createDataGroup());
		}
	}

	/**
	 * Creates filter group.
	 * 
	 * @return
	 */
	private FilterGroup createFilterGroup() {
		FilterGroup filterGroup = new FilterGroup();
		// for the group by selection
		List<Filter> filterList = new ArrayList<>();
		filterList.add(createFilter(FILTER_TYPE, FILTER_BY_ISSUE_TYPE, "Issue Type", 1));
		filterList.add(createFilter(FILTER_TYPE, FILTER_BY_PRIORITY, "Priority", 2));
		filterGroup.setFilterGroup1(filterList);

		return filterGroup;
	}

	/**
	 * Creates individual filter object.
	 * 
	 * @param type
	 * @param name
	 * @param key
	 * @param order
	 * @return
	 */
	private Filter createFilter(String type, String name, String key, Integer order) {
		Filter filter = new Filter();
		filter.setFilterType(type);
		filter.setFilterName(name);
		filter.setFilterKey(key);
		filter.setOrder(order);
		return filter;
	}

	/**
	 * Creates data group that tells what kind of data will be shown on chart.
	 *
	 * @return
	 */
	private KpiDataGroup createDataGroup() {
		KpiDataGroup dataGroup = new KpiDataGroup();

		KpiDataSummary summary = new KpiDataSummary();
		summary.setName("Total wastage");
		summary.setAggregation(SUM);

		List<KpiData> dataGroup1 = new ArrayList<>();
		dataGroup1.add(createKpiData("issueBlockedTime", "Blocked Time", 1, SUM, CommonConstant.DAY));
		dataGroup1.add(createKpiData("issueWaitTime", "Waiting Time", 2, SUM, CommonConstant.DAY));

		dataGroup.setSummary(summary);
		dataGroup.setDataGroup1(dataGroup1);
		return dataGroup;
	}

	/**
	 * Creates kpi data object.
	 * 
	 * @param key
	 * @param name
	 * @param order
	 * @param aggregation
	 * @param unit
	 * @return
	 */
	private KpiData createKpiData(String key, String name, Integer order, String aggregation, String unit) {
		KpiData data = new KpiData();
		data.setKey(key);
		data.setName(name);
		data.setOrder(order);
		data.setAggregation(aggregation);
		data.setUnit(unit);
		data.setShowAsLegend(true);
		return data;
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
			if (StringUtils.isNotEmpty(fieldMapping.getJiraIncludeBlockedStatusKPI131())
					&& fieldMapping.getJiraIncludeBlockedStatusKPI131().contains(CommonConstant.BLOCKED_STATUS_WASTAGE)
					&& CollectionUtils.isNotEmpty(fieldMapping.getJiraBlockedStatusKPI131()))
				blockedStatus = fieldMapping.getJiraBlockedStatusKPI131();

			if (CollectionUtils.isNotEmpty(fieldMapping.getJiraWaitStatusKPI131()))
				waitStatus = fieldMapping.getJiraWaitStatusKPI131();
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
			List<String> blockedStatusList, List<String> waitStatusList, boolean flagIncluded) {
		List<JiraHistoryChangeLog> statusUpdationLog = new ArrayList<>();
		List<JiraHistoryChangeLog> flagStatusUpdationLog;
		List<Integer> resultList = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(issueCustomHistory.getStatusUpdationLog())) {
			statusUpdationLog = issueCustomHistory.getStatusUpdationLog();
		}
		int blockedTime = 0;
		int waitedTime = 0;
		for (int i = 0; i < statusUpdationLog.size(); i++) {
			JiraHistoryChangeLog entry = statusUpdationLog.get(i);
			if (!flagIncluded) {
				blockedTime = calculateBlockAndWaitTimeBasedOnFieldMapping(entry, blockedStatusList, statusUpdationLog,
						i, sprintDetail, blockedTime);
			}
			waitedTime = calculateBlockAndWaitTimeBasedOnFieldMapping(entry, waitStatusList, statusUpdationLog, i,
					sprintDetail, waitedTime);
		}

		if (flagIncluded && CollectionUtils.isNotEmpty(issueCustomHistory.getFlagStatusChangeLog())) {
			flagStatusUpdationLog = issueCustomHistory.getFlagStatusChangeLog();
			for (int i = 0; i < flagStatusUpdationLog.size(); i++) {
				JiraHistoryChangeLog entry = flagStatusUpdationLog.get(i);
				blockedTime = calculateBlockTimeBasedOnFlagStatus(entry, flagStatusUpdationLog, i, sprintDetail,
						blockedTime);
			}
		}
		resultList.add(calculateBlockAndWaitTimeInMinute(waitedTime));
		resultList.add(calculateBlockAndWaitTimeInMinute(blockedTime));
		return resultList;
	}

	/**
	 * Calculate the block time w.r.t flag status
	 *
	 * @param entry
	 * @param flagStatusUpdationLog
	 * @param index
	 * @param sprintDetails
	 * @param time
	 * @return int
	 */
	private int calculateBlockTimeBasedOnFlagStatus(JiraHistoryChangeLog entry,
			List<JiraHistoryChangeLog> flagStatusUpdationLog, int index, SprintDetails sprintDetails, int time) {
		LocalDateTime sprintStartDate = DateUtil.convertingStringToLocalDateTime(sprintDetails.getStartDate(),
				DateUtil.TIME_FORMAT);
		LocalDateTime sprintEndDate = DateUtil.convertingStringToLocalDateTime(sprintDetails.getEndDate(),
				DateUtil.TIME_FORMAT);
		LocalDateTime entryActivityDate = entry.getUpdatedOn();
		if (entry.getChangedTo().equalsIgnoreCase(CommonConstant.FLAG_STATUS_FOR_SERVER)
				|| entry.getChangedTo().equalsIgnoreCase(CommonConstant.FLAG_STATUS_FOR_CLOUD)) {
			long hours = 0;
			// Checking for indexOutOfBound in flagStatusUpdationLog list
			if (flagStatusUpdationLog.size() == index + 1) {
				hours = hoursForLastEntryOfStatusUpdationLog(sprintDetails, sprintStartDate, sprintEndDate,
						entryActivityDate);
			} else {
				// Find fetch the next element of flagStatusUpdationLog
				JiraHistoryChangeLog nextEntry = flagStatusUpdationLog.get(index + 1);
				if (!nextEntry.getChangedTo().equalsIgnoreCase(CommonConstant.FLAG_STATUS_FOR_SERVER)
						|| !entry.getChangedTo().equalsIgnoreCase(CommonConstant.FLAG_STATUS_FOR_CLOUD)) {
					LocalDateTime nextEntryActivityDate = nextEntry.getUpdatedOn();
					// Checking if both alternate element are inside the sprint start and end date
					if (!(entryActivityDate.isBefore(sprintStartDate)
							&& nextEntryActivityDate.isBefore(sprintStartDate))
							&& !(entryActivityDate.isAfter(sprintEndDate)
									&& nextEntryActivityDate.isAfter(sprintEndDate))) {
						hours = hoursForEntriesInBetweenSprint(sprintStartDate, sprintEndDate, entryActivityDate,
								nextEntryActivityDate);
					}
				}
			}
			if (hours != 0)
				time += hours;
		}
		return time;
	}

	private int calculateBlockAndWaitTimeInMinute(int timeInHours) {
		int timeInMin = (timeInHours / 24) * 8 * 60;
		int remainingTimeInMin = (timeInHours % 24) * 60;
		if (remainingTimeInMin >= 480) {
			timeInMin = timeInMin + 480;
		} else {
			timeInMin = timeInMin + remainingTimeInMin;
		}
		return timeInMin;
	}

	/**
	 * Calculate the wait and block time w.r.t fieldMappingStatus
	 *
	 * @param entry
	 * @param fieldMappingStatus
	 * @param statusUpdationLog
	 * @param index
	 * @param sprintDetails
	 * @param time
	 * @return int
	 */
	private int calculateBlockAndWaitTimeBasedOnFieldMapping(JiraHistoryChangeLog entry,
			List<String> fieldMappingStatus, List<JiraHistoryChangeLog> statusUpdationLog, int index,
			SprintDetails sprintDetails, int time) {
		LocalDateTime sprintStartDate = DateUtil.convertingStringToLocalDateTime(sprintDetails.getStartDate(),
				DateUtil.TIME_FORMAT);
		LocalDateTime sprintEndDate = DateUtil.convertingStringToLocalDateTime(sprintDetails.getEndDate(),
				DateUtil.TIME_FORMAT);
		LocalDateTime entryActivityDate = entry.getUpdatedOn();
		if (CollectionUtils.isNotEmpty(fieldMappingStatus) && fieldMappingStatus.contains(entry.getChangedTo())) {
			int hours = 0;
			// Checking for indexOutOfBound in statusUpdationLog list
			if (statusUpdationLog.size() == index + 1) {
				hours = Math.toIntExact(hoursForLastEntryOfStatusUpdationLog(sprintDetails, sprintStartDate,
						sprintEndDate, entryActivityDate));
			} else {
				// Find fetch the next element of statusUpdationLog
				JiraHistoryChangeLog nextEntry = statusUpdationLog.get(index + 1);
				LocalDateTime nextEntryActivityDate = nextEntry.getUpdatedOn();
				// Checking if both alternate element are inside the sprint start and end date
				if (!(entryActivityDate.isBefore(sprintStartDate) && nextEntryActivityDate.isBefore(sprintStartDate))
						&& !(entryActivityDate.isAfter(sprintEndDate)
								&& nextEntryActivityDate.isAfter(sprintEndDate))) {
					hours = Math.toIntExact(hoursForEntriesInBetweenSprint(sprintStartDate, sprintEndDate,
							entryActivityDate, nextEntryActivityDate));
				}
			}
			if (hours != 0)
				time += hours;
		}
		return time;
	}

	// Calculate the time for entries which lies between sprint start and end date
	// or one of them is inside sprint start end date
	private long hoursForEntriesInBetweenSprint(LocalDateTime sprintStartDate, LocalDateTime sprintEndDate,
			LocalDateTime entryActivityDate, LocalDateTime nextEntryActivityDate) {
		long hours;
		if (nextEntryActivityDate.isBefore(sprintEndDate)) {
			if (entryActivityDate.isAfter(sprintStartDate)) {
				hours = (ChronoUnit.HOURS.between(entryActivityDate, nextEntryActivityDate)
						- minusHoursOfWeekEndDays(entryActivityDate, nextEntryActivityDate));
			} else {
				hours = (ChronoUnit.HOURS.between(sprintStartDate, nextEntryActivityDate)
						- minusHoursOfWeekEndDays(sprintStartDate, nextEntryActivityDate));
			}
		} else {
			if (entryActivityDate.isAfter(sprintStartDate)) {
				hours = (ChronoUnit.HOURS.between(entryActivityDate, sprintEndDate)
						- minusHoursOfWeekEndDays(entryActivityDate, sprintEndDate));
			} else {
				hours = (ChronoUnit.HOURS.between(sprintStartDate, sprintEndDate)
						- minusHoursOfWeekEndDays(sprintStartDate, sprintEndDate));
			}
		}
		return hours;
	}

	// Calculate the time for last entry of statusUpdationLog
	private long hoursForLastEntryOfStatusUpdationLog(SprintDetails sprintDetails, LocalDateTime sprintStartDate,
			LocalDateTime sprintEndDate, LocalDateTime entryActivityDate) {
		long hours = 0;
		if (entryActivityDate.isAfter(sprintStartDate)) {
			if (entryActivityDate.isBefore(sprintEndDate)) {
				if (Objects.equals(sprintDetails.getState(), SprintDetails.SPRINT_STATE_ACTIVE)) {
					hours = (ChronoUnit.HOURS.between(entryActivityDate, LocalDateTime.now())
							- minusHoursOfWeekEndDays(entryActivityDate, LocalDateTime.now()));
				} else {

					hours = (ChronoUnit.HOURS.between(entryActivityDate, sprintEndDate)
							- minusHoursOfWeekEndDays(entryActivityDate, sprintEndDate));
				}
			}
		} else {
			if (Objects.equals(sprintDetails.getState(), SprintDetails.SPRINT_STATE_ACTIVE)) {
				LocalDateTime currDate = LocalDateTime.now();
				hours = (ChronoUnit.HOURS.between(sprintStartDate, currDate)
						- minusHoursOfWeekEndDays(sprintStartDate, currDate));
			} else {
				hours = ChronoUnit.HOURS.between(sprintStartDate, sprintEndDate)
						- minusHoursOfWeekEndDays(sprintStartDate, sprintEndDate);
			}
		}
		return hours;
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

	public int minusHoursOfWeekEndDays(LocalDateTime d1, LocalDateTime d2) {
		int countOfWeekEndDays = saturdaySundayCount(d1, d2);
		if (countOfWeekEndDays != 0) {
			return countOfWeekEndDays * 24;
		} else {
			return 0;
		}
	}

}
