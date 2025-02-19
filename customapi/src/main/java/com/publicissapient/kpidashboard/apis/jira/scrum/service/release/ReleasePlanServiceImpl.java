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
package com.publicissapient.kpidashboard.apis.jira.scrum.service.release;

import static com.publicissapient.kpidashboard.apis.util.ReleaseKpiHelper.getStoryPoint;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.releasedashboard.JiraReleaseKPIService;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This service for managing Release plan Kpi on Release Board. Gives analysis
 * of release scope vs plan. {@link JiraReleaseKPIService}
 *
 * @author purgupta2
 */
@Slf4j
@Component
public class ReleasePlanServiceImpl extends JiraReleaseKPIService {

	public static final String OVERALL_ISSUE = "OVERALL ISSUE";
	public static final String OVERALL = "OVERALL";
	private static final String TOTAL_ISSUES = "totalIssues";
	private static final String ADDED_TO_RELEASE = "addedToRelease";
	private static final String FULL_RELEASE = "fullRelease";
	private static final String REMOVED_FROM_RELEASE = "removedFromRelease";
	private static final String RELEASE_SCOPE = "Release Scope";
	private static final String RELEASE_PLANNED = "Release planned";
	private static final String ISSUE_COUNT = "Issue Count";
	private static final String STORY_POINT = "Story Points";
	private static final int DAYS_RANGE = 120;
	private static final String LINE_GRAPH_TYPE = "line";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private final List<JiraIssue> allReleaseTaggedIssue = new ArrayList<>();
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private ConfigHelperService configHelperService;
	private LocalDate tempStartDate = null;

	@Override
	public String getQualifierType() {
		return KPICode.RELEASE_PLAN.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node releaseNode)
			throws ApplicationException {
		releaseWiseLeafNodeValue(releaseNode, kpiElement, kpiRequest);
		log.info("ReleasePlanServiceImpl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	/**
	 * Populate Release Wise Leaf Node Value
	 *
	 * @param latestRelease
	 *          List<Node>
	 * @param kpiElement
	 *          kpiElement
	 * @param kpiRequest
	 *          kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void releaseWiseLeafNodeValue(Node latestRelease, KpiElement kpiElement, // NOSONAR
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		String startDate = latestRelease.getReleaseFilter().getStartDate();
		String endDate = latestRelease.getReleaseFilter().getEndDate();

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestRelease, null, null, kpiRequest);
		List<JiraIssue> releaseIssues = (List<JiraIssue>) resultMap.get(TOTAL_ISSUES);
		Map<LocalDate, List<JiraIssue>> addedIssuesMap = (Map<LocalDate, List<JiraIssue>>) resultMap.get(ADDED_TO_RELEASE);
		Map<LocalDate, List<JiraIssue>> fullReleaseIssueMap = (Map<LocalDate, List<JiraIssue>>) resultMap.get(FULL_RELEASE);
		Map<LocalDate, List<JiraIssue>> removeIssueMap = (Map<LocalDate, List<JiraIssue>>) resultMap
				.get(REMOVED_FROM_RELEASE);

		List<IterationKpiValue> iterationKpiValueList = new ArrayList<>();
		long range;
		String duration;
		if (CollectionUtils.isNotEmpty(releaseIssues) && MapUtils.isNotEmpty(fullReleaseIssueMap)) {
			Object basicProjectConfigId = latestRelease.getProjectFilter().getBasicProjectConfigId();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			/*
			 * if start-time is absent, then the date at which issue was added and remained
			 * added in the entire release is considered to be the start date if end date is
			 * absent then it means that issue is unreleased, so till today we can consider
			 * as end date
			 */
			LocalDate startLocalDate = StringUtils.isEmpty(startDate)
					? fullReleaseIssueMap.keySet().stream().filter(Objects::nonNull).min(LocalDate::compareTo).orElse(null)
					: LocalDate.parse(startDate.split("T")[0], DATE_TIME_FORMATTER);
			LocalDate endLocalDate = StringUtils.isEmpty(endDate)
					? LocalDate.now()
					: LocalDate.parse(endDate.split("T")[0], DATE_TIME_FORMATTER);
			Map<String, Long> durationRangeMap = getDurationRangeMap(startLocalDate, endLocalDate);
			duration = durationRangeMap.keySet().stream().findFirst().orElse("");
			range = durationRangeMap.values().stream().findFirst().orElse(0L);
			fullReleaseIssueMap = prepareIssueBeforeStartDate(fullReleaseIssueMap, startLocalDate);

			assert startLocalDate != null;
			tempStartDate = LocalDate.parse(startLocalDate.toString());
			allReleaseTaggedIssue.clear();
			fullReleaseIssueMap.forEach((k, v) -> allReleaseTaggedIssue.addAll(v));
			List<JiraIssue> overallIssues = new ArrayList<>();
			List<DataCountGroup> issueCountDataGroup = new ArrayList<>();
			List<DataCountGroup> issueSizeCountDataGroup = new ArrayList<>();
			// populating release scope vs planned
			for (int i = 0; i < range && !startLocalDate.isAfter(endLocalDate); i++) {
				DataCountGroup issueCount = new DataCountGroup();
				DataCountGroup issueSize = new DataCountGroup();
				CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(startLocalDate, duration);
				Map<String, List<JiraIssue>> filterWiseGroupedMap = createFilterWiseGroupedMap(dateRange, addedIssuesMap,
						removeIssueMap, fullReleaseIssueMap, overallIssues);
				String date = getRange(dateRange, duration);
				populateFilterWiseDataMap(filterWiseGroupedMap, issueCount, issueSize, date, duration, dateRange, fieldMapping);
				startLocalDate = getNextRangeDate(duration, startLocalDate, endLocalDate);
				issueCountDataGroup.add(issueCount);
				issueSizeCountDataGroup.add(issueSize);
			}
			releaseIssues.retainAll(allReleaseTaggedIssue);
			LocalDate maxPlannedDueDate = releaseIssues.stream().map(JiraIssue::getDueDate).filter(Objects::nonNull)
					.filter(dueDate -> !dueDate.isBlank())
					.map(dueDate -> LocalDate.parse(dueDate.split("T")[0], DATE_TIME_FORMATTER)).max(Comparator.naturalOrder())
					.orElse(null);

			populateExcelDataObject(requestTrackerId, excelData, releaseIssues, fieldMapping);
			if (CollectionUtils.isNotEmpty(issueCountDataGroup)) {
				Map<String, Object> additionalInfoMap = new HashMap<>();
				additionalInfoMap.put("isXaxisGapRequired", true);
				additionalInfoMap.put("plannedDueDate", String.valueOf(maxPlannedDueDate));

				IterationKpiValue kpiValueIssueCount = new IterationKpiValue();
				kpiValueIssueCount.setDataGroup(issueCountDataGroup);
				kpiValueIssueCount.setFilter1(ISSUE_COUNT);
				kpiValueIssueCount.setYAxisLabel(CommonConstant.COUNT);
				kpiValueIssueCount.setAdditionalInfo(additionalInfoMap);

				IterationKpiValue kpiValueSizeCount = new IterationKpiValue();
				kpiValueSizeCount.setDataGroup(issueSizeCountDataGroup);
				kpiValueSizeCount.setFilter1(STORY_POINT);
				kpiValueSizeCount.setYAxisLabel(CommonConstant.SP);
				kpiValueSizeCount.setAdditionalInfo(additionalInfoMap);

				iterationKpiValueList.add(kpiValueSizeCount);
				iterationKpiValueList.add(kpiValueIssueCount);

				kpiElement.setModalHeads(KPIExcelColumn.RELEASE_PLAN.getColumns());
				kpiElement.setExcelColumns(KPIExcelColumn.RELEASE_PLAN.getColumns());
				kpiElement.setExcelData(excelData);
			}
		}
		kpiElement.setTrendValueList(iterationKpiValueList);
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		if (null != leafNode) {
			log.info("Release Plan -> Requested release : {}", leafNode.getName());

			List<String> releaseList = getReleaseList();
			if (CollectionUtils.isNotEmpty(releaseList)) {
				List<JiraIssueCustomHistory> allIssuesHistory = getJiraIssuesCustomHistoryFromBaseClass();
				final String basicProjConfigId = leafNode.getProjectFilter().getBasicProjectConfigId().toString();
				List<JiraIssue> releaseIssues = jiraIssueRepository.findByNumberInAndBasicProjectConfigId(
						allIssuesHistory.stream().map(JiraIssueCustomHistory::getStoryID).collect(Collectors.toList()),
						basicProjConfigId);

				Map<LocalDate, List<JiraIssue>> addedIssuesMap = new HashMap<>();
				Map<LocalDate, List<JiraIssue>> removeIssueMap = new HashMap<>();
				Map<LocalDate, List<JiraIssue>> fullReleaseMap = new HashMap<>();
				dateWiseLogs(allIssuesHistory, releaseList.stream().findFirst().orElse(null), releaseIssues, addedIssuesMap,
						removeIssueMap, fullReleaseMap);
				resultListMap.put(FULL_RELEASE, fullReleaseMap);
				resultListMap.put(ADDED_TO_RELEASE, addedIssuesMap);
				resultListMap.put(REMOVED_FROM_RELEASE, removeIssueMap);
				resultListMap.put(TOTAL_ISSUES, releaseIssues);
			}
		}
		return resultListMap;
	}

	/**
	 * Used to Create Date wise log
	 *
	 * @param allIssuesHistory
	 *          List<JiraIssueCustomHistory>
	 * @param releaseName
	 *          Name of release
	 * @param releaseIssue
	 *          List<JiraIssue>
	 * @param addedIssuesMap
	 *          Map<LocalDate, List<JiraIssue>>
	 * @param removeIssueMap
	 *          Map<LocalDate, List<JiraIssue>>
	 * @param fullReleaseMap
	 *          Map<LocalDate, List<JiraIssue>>
	 */
	private void dateWiseLogs(List<JiraIssueCustomHistory> allIssuesHistory, String releaseName, // NOSONAR
			List<JiraIssue> releaseIssue, Map<LocalDate, List<JiraIssue>> addedIssuesMap,
			Map<LocalDate, List<JiraIssue>> removeIssueMap, Map<LocalDate, List<JiraIssue>> fullReleaseMap) {

		releaseName = releaseName != null ? releaseName : "";
		String finalReleaseName = releaseName.toLowerCase();
		allIssuesHistory.forEach(issueHistory -> {
			List<JiraHistoryChangeLog> fixVersionUpdateLog = issueHistory.getFixVersionUpdationLog();
			fixVersionUpdateLog.sort(Comparator.comparing(JiraHistoryChangeLog::getUpdatedOn));
			List<JiraIssue> jiraIssueList = getRespectiveJiraIssue(releaseIssue, issueHistory);
			int lastIndex = fixVersionUpdateLog.size() - 1;
			fixVersionUpdateLog.stream()
					.filter(updateLogs -> updateLogs.getChangedTo().toLowerCase().contains(finalReleaseName) ||
							updateLogs.getChangedFrom().toLowerCase().contains(finalReleaseName))
					.forEach(updateLogs -> {
						LocalDate updatedLog;
						if (updateLogs.getChangedTo().toLowerCase().contains(finalReleaseName)) {
							if (fixVersionUpdateLog.get(lastIndex).getChangedTo().toLowerCase().contains(finalReleaseName)) {
								updatedLog = fixVersionUpdateLog.get(lastIndex).getUpdatedOn().toLocalDate();
								List<JiraIssue> cloneList = new ArrayList<>(jiraIssueList);
								fullReleaseMap.computeIfPresent(updatedLog, (k, v) -> {
									v.addAll(cloneList);
									return v;
								});
								fullReleaseMap.putIfAbsent(updatedLog, cloneList);
							}
							updatedLog = updateLogs.getUpdatedOn().toLocalDate();
							addedIssuesMap.computeIfPresent(updatedLog, (k, v) -> {
								v.addAll(jiraIssueList);
								return v;
							});
							addedIssuesMap.putIfAbsent(updatedLog, jiraIssueList);
						}
						if (updateLogs.getChangedFrom().toLowerCase().contains(finalReleaseName)) {
							List<JiraIssue> removeJiraIssueLIst = new ArrayList<>(jiraIssueList);
							updatedLog = updateLogs.getUpdatedOn().toLocalDate();
							removeIssueMap.computeIfPresent(updatedLog, (k, v) -> {
								v.addAll(removeJiraIssueLIst);
								return v;
							});
							removeIssueMap.putIfAbsent(updatedLog, removeJiraIssueLIst);
						}
					});
		});
	}

	/**
	 * Get JiraIssue for respective CustomHistory
	 *
	 * @param totalIssueList
	 *          List<JiraIssue>
	 * @param issueHistory
	 *          issueHistory
	 * @return List<JiraIssue>
	 */
	private List<JiraIssue> getRespectiveJiraIssue(List<JiraIssue> totalIssueList, JiraIssueCustomHistory issueHistory) {
		return totalIssueList.stream()
				.filter(jiraIssue -> jiraIssue.getNumber().equalsIgnoreCase(issueHistory.getStoryID()))
				.collect(Collectors.toList());
	}

	/**
	 * Method for calculation x-axis duration & range
	 *
	 * @param startLocalDate
	 *          startDate
	 * @param endLocalDate
	 *          endDate
	 * @return Map<String, Long>
	 */
	private Map<String, Long> getDurationRangeMap(LocalDate startLocalDate, LocalDate endLocalDate) {
		Map<String, Long> map = new HashMap<>();
		long range;
		String duration;
		// representing in months if week count > 120
		if (ChronoUnit.WEEKS.between(Objects.requireNonNull(startLocalDate), endLocalDate) > DAYS_RANGE) {
			range = ChronoUnit.MONTHS.between(Objects.requireNonNull(startLocalDate), endLocalDate) + 1;
			duration = CommonConstant.MONTH;
		}
		// added+1 to add the end date as well
		else if (ChronoUnit.DAYS.between(startLocalDate, endLocalDate) + 1 > DAYS_RANGE) {
			startLocalDate = startLocalDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
			range = ChronoUnit.WEEKS.between(Objects.requireNonNull(startLocalDate), endLocalDate) + 1;
			duration = CommonConstant.WEEK;
		} else {
			range = DAYS_RANGE;
			duration = CommonConstant.DAYS;
		}
		map.put(duration, range);
		return map;
	}

	/**
	 * issue completed/tagged before the release version but happened to be present
	 * in selected release then issues to be present on the first day of release
	 * start date
	 */
	private Map<LocalDate, List<JiraIssue>> prepareIssueBeforeStartDate(
			Map<LocalDate, List<JiraIssue>> completedReleaseMap, LocalDate startLocalDate) {
		Map<LocalDate, List<JiraIssue>> rangedCompletedMap = new HashMap<>();
		completedReleaseMap.forEach((date, issues) -> {
			if (!date.isAfter(startLocalDate)) {
				rangedCompletedMap.computeIfPresent(startLocalDate, (key, value) -> {
					value.addAll(issues);
					return value;
				});
				rangedCompletedMap.putIfAbsent(startLocalDate, issues);
			} else {
				rangedCompletedMap.put(date, issues);
			}
		});
		return rangedCompletedMap;
	}

	/**
	 * Method to get Release Scope,Progress in a DateRange
	 *
	 * @param dateRange
	 *          CustomDateRange
	 * @param addedIssuesMap
	 *          Map<LocalDate, List<JiraIssue>>
	 * @param removeIssueMap
	 *          Map<LocalDate, List<JiraIssue>>
	 * @param fullReleaseIssueMap
	 *          Map<LocalDate, List<JiraIssue>>
	 * @param overallIssues
	 *          List<JiraIssue>
	 * @return Map<String, List<JiraIssue>>
	 */
	private Map<String, List<JiraIssue>> createFilterWiseGroupedMap(CustomDateRange dateRange,
			Map<LocalDate, List<JiraIssue>> addedIssuesMap, Map<LocalDate, List<JiraIssue>> removeIssueMap,
			Map<LocalDate, List<JiraIssue>> fullReleaseIssueMap, List<JiraIssue> overallIssues) {
		Map<String, List<JiraIssue>> groupedMap = new HashMap<>();
		List<JiraIssue> defaultIssues = new ArrayList<>();
		List<JiraIssue> allAddedIssues = new ArrayList<>();
		List<JiraIssue> removedIssues = new ArrayList<>();

		for (LocalDate currentDate = dateRange.getStartDate(); DateUtil.isWithinDateRange(currentDate,
				dateRange.getStartDate(), dateRange.getEndDate()); currentDate = currentDate.plusDays(1)) {
			if (currentDate.isEqual(tempStartDate)) {
				defaultIssues.addAll(addedIssuesMap.getOrDefault(currentDate, new ArrayList<>()));
				defaultIssues.removeAll(removeIssueMap.getOrDefault(currentDate, new ArrayList<>()));
				defaultIssues.addAll(fullReleaseIssueMap.getOrDefault(currentDate, new ArrayList<>())); // defaultMap
			} else {
				defaultIssues.addAll(fullReleaseIssueMap.getOrDefault(currentDate, new ArrayList<>())); // defaultMap
				allAddedIssues.addAll(addedIssuesMap.getOrDefault(currentDate, new ArrayList<>()));
				removedIssues.addAll(removeIssueMap.getOrDefault(currentDate, new ArrayList<>()));
			}
		}

		// if on same day issue is added to release and removed from release, then on
		// same day delete that issue from
		// removed
		List<JiraIssue> commonIssues = (List<JiraIssue>) CollectionUtils.intersection(allAddedIssues, removedIssues);
		removedIssues.removeAll(commonIssues);
		allAddedIssues.removeAll(commonIssues);
		overallIssues.removeAll(commonIssues);
		// issues removed and added on same day
		List<JiraIssue> removedThenAdded = (List<JiraIssue>) CollectionUtils.intersection(commonIssues, defaultIssues);
		allAddedIssues.addAll(removedThenAdded);
		allAddedIssues.removeAll(overallIssues);

		overallIssues.addAll(defaultIssues);
		overallIssues.addAll(allAddedIssues);
		overallIssues.removeAll(removedIssues);
		overallIssues = overallIssues.stream().distinct().collect(Collectors.toList());

		groupedMap.put(OVERALL_ISSUE, overallIssues);
		return groupedMap;
	}

	/**
	 * Method for population of Release Scope,Progress & Prediction
	 *
	 * @param filterWiseGroupedMap
	 *          Map<String, List<JiraIssue>>
	 * @param issueCount
	 *          DataCountGroup of Issue Count
	 * @param issueSize
	 *          DataCountGroup of Issue Size
	 * @param date
	 *          Date of x-axis
	 * @param duration
	 *          Duration
	 * @param dateRange
	 *          date range
	 * @param fieldMapping
	 *          fieldMapping
	 */
	private void populateFilterWiseDataMap(Map<String, List<JiraIssue>> filterWiseGroupedMap, DataCountGroup issueCount,
			DataCountGroup issueSize, String date, String duration, CustomDateRange dateRange, FieldMapping fieldMapping) {
		List<DataCount> issueCountDataList = new ArrayList<>();
		List<DataCount> issueSizeDataList = new ArrayList<>();

		List<JiraIssue> overallIssues = filterWiseGroupedMap.getOrDefault(OVERALL_ISSUE, new ArrayList<>());

		LocalDate endDate = dateRange.getEndDate();

		// gives issue whose dueDates are till endDate
		List<JiraIssue> matchingIssues = overallIssues.stream().filter(issue -> issue.getDueDate() != null)
				.filter(issue -> !issue.getDueDate().isBlank()).filter(issue -> {
					LocalDate dueDate = LocalDate.parse(issue.getDueDate().split("T")[0], DATE_TIME_FORMATTER);
					return DateUtil.equalAndBeforeTime(dueDate, endDate);
				}).toList();

		createDataCount((long) overallIssues.size(), RELEASE_SCOPE, issueCountDataList);
		createDataCount((long) matchingIssues.size(), RELEASE_PLANNED, issueCountDataList);

		issueCount.setFilter(date);
		issueCount.setDuration(duration);
		issueCount.setValue(issueCountDataList);

		createDataCount(getStoryPoint(overallIssues, fieldMapping), RELEASE_SCOPE, issueSizeDataList);
		createDataCount(getStoryPoint(matchingIssues, fieldMapping), RELEASE_PLANNED, issueSizeDataList);

		issueSize.setFilter(date);
		issueSize.setDuration(duration);
		issueSize.setValue(issueSizeDataList);
	}

	/**
	 * Method to Populate the DataCount Object
	 *
	 * @param value
	 *          value
	 * @param label
	 *          Label
	 * @param issueCountDataList
	 *          List<DataCount>
	 */
	private void createDataCount(Object value, String label, List<DataCount> issueCountDataList) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setKpiGroup(label);
		dataCount.setValue(value);
		dataCount.setGraphType(LINE_GRAPH_TYPE);
		dataCount.setLineCategory(CommonConstant.SOLID_LINE_TYPE);
		issueCountDataList.add(dataCount);
	}

	/**
	 * Method to Get Date on basis of range for x-axis representation
	 *
	 * @param dateRange
	 *          CustomDateRange
	 * @param range
	 *          range
	 * @return x-axis representation value
	 */
	private String getRange(CustomDateRange dateRange, String range) {
		String date;
		if (range.equalsIgnoreCase(CommonConstant.MONTH)) {
			LocalDate dateValue = dateRange.getStartDate();
			date = dateValue.getYear() + Constant.DASH + dateValue.getMonthValue();
		} else if (range.equalsIgnoreCase(CommonConstant.WEEK)) {
			LocalDate endDate = dateRange.getEndDate();
			while (!endDate.getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
				endDate = endDate.minusDays(1);
			}
			date = DateUtil.dateTimeConverter(dateRange.getStartDate().toString(), DateUtil.DATE_FORMAT,
					DateUtil.DISPLAY_DATE_FORMAT) + " to " +
					DateUtil.dateTimeConverter(endDate.toString(), DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT);
		} else {
			date = dateRange.getStartDate().toString();
		}
		return date;
	}

	/**
	 * Get Next Date of population based on duration
	 *
	 * @param duration
	 *          duration
	 * @param currentDate
	 *          currDate
	 * @return LocalDate
	 */
	private LocalDate getNextRangeDate(String duration, LocalDate currentDate, LocalDate endLocalDate) {
		if (duration.equalsIgnoreCase(CommonConstant.MONTH)) {
			currentDate = currentDate.plusMonths(1);
		} else if (duration.equalsIgnoreCase(CommonConstant.WEEK)) {
			LocalDate monday = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
			currentDate = monday.plusWeeks(1);
			if (currentDate.isAfter(endLocalDate)) {
				currentDate = endLocalDate;
			}
		} else {
			currentDate = currentDate.plusDays(1);
		}
		return currentDate;
	}

	/**
	 * Method to Populate Excel Data Object
	 *
	 * @param requestTrackerId
	 *          requestTrackerId
	 * @param excelData
	 *          excelData
	 * @param jiraIssueList
	 *          jiraIssueList
	 * @param fieldMapping
	 *          fieldMapping
	 */
	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> jiraIssueList, FieldMapping fieldMapping) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase()) &&
				CollectionUtils.isNotEmpty(jiraIssueList)) {

			KPIExcelUtility.populateReleasePlanExcelData(jiraIssueList, excelData, fieldMapping);
		}
	}
}
