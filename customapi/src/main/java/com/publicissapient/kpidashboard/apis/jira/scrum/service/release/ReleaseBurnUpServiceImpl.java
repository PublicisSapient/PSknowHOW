/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.jira.model.JiraIssueReferTime;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.CommonServiceImpl;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.model.ReleaseSpecification;
import com.publicissapient.kpidashboard.apis.jira.service.releasedashboard.JiraReleaseKPIService;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * This service for managing Release BurnUp Kpi on Release Board. Gives analysis
 * of release scope vs progress for released version & release prediction based
 * on closure rate (dev or qa) for unreleased version.
 * {@link JiraReleaseKPIService}
 */
@Slf4j
@Component
public class ReleaseBurnUpServiceImpl extends JiraReleaseKPIService {

	private static final String TOTAL_ISSUES = "totalIssues";
	private static final String ADDED_TO_RELEASE = "addedToRelease";
	private static final String FULL_RELEASE = "fullRelease";
	private static final String REMOVED_FROM_RELEASE = "removedFromRelease";
	private static final String ISSUE_COUNT = "Issue Count";
	private static final String STORY_POINT = "Story Points";
	private static final String SCOPE_REMOVED = "Scope Removed";
	private static final String SCOPE_ADDED = "Scope Added";
	private static final String RELEASE_SCOPE = "Release Scope";
	private static final int DAYS_RANGE = 120;
	private static final String RELEASE_PROGRESS = "Release Progress";
	private static final String LINE_GRAPH_TYPE = "line";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static final String RELEASE_PREDICTION = "Release Prediction";
	public static final String ISSUE_COUNT_PREDICTION = "issueCountPrediction";
	public static final String ISSUE_SIZE_PREDICTION = "issueSizePrediction";
	public static final String SHOW_PREDICTION = "showPrediction";
	public static final String OVERALL_COMPLETED = "OVERALL COMPLETED";
	private static final String AVG_ISSUE_COUNT = "avgIssueCount";
	private static final String AVG_STORY_POINT = "avgStoryPoint";
	public static final String IS_PREDICTION_BOUNDARY = "isPredictionBoundary";
	public static final String IS_ISSUE_COUNT_ACHIEVED = "isIssueCountAchieved";
	public static final String IS_STORY_POINT_ACHIEVED = "isStoryPointAchieved";
	public static final String OVERALL_ISSUE = "OVERALL ISSUE";
	public static final String DEV_COMPLETE_DATE_MAP = "devCompleteDateMap";
	public static final String AVERAGE_VELOCITY_IS = "Average velocity is ";
	public static final String ISSUE_COUNT_IN = " Issues in ";
	public static final String DAYS = " days)";
	public static final String SPS_IN = " SPs in ";
	public static final String IS_XAXIS_GAP_REQUIRED = "isXaxisGapRequired";
	public static final String CUSTOMISED_GROUP = "customisedGroup";
	public static final String TOTAL_AVG_VELOCITY = "totalAvgVelocity";

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CommonServiceImpl commonService;

	private LocalDateTime tempStartDate = null;
	private final List<JiraIssue> allReleaseTaggedIssue = new ArrayList<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node releaseNode)
			throws ApplicationException {
		releaseWiseLeafNodeValue(releaseNode, kpiElement, kpiRequest);
		log.info("ReleaseBurnUpServiceImpl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		if (null != leafNode) {
			log.info("Release BurnUp -> Requested release : {}", leafNode.getName());

			List<String> releaseList = getReleaseList();
			if (CollectionUtils.isNotEmpty(releaseList)) {
				List<JiraIssueCustomHistory> allIssuesHistory = getJiraIssuesCustomHistoryFromBaseClass();
				final String basicProjConfigId = leafNode.getProjectFilter().getBasicProjectConfigId().toString();
				List<JiraIssue> releaseIssues = jiraIssueRepository.findByNumberInAndBasicProjectConfigId(
						allIssuesHistory.stream().map(JiraIssueCustomHistory::getStoryID).collect(Collectors.toList()),
						basicProjConfigId);

				Map<LocalDate, List<JiraIssueReferTime>> addedIssuesMap = new HashMap<>();
				Map<LocalDate, List<JiraIssueReferTime>> removeIssueMap = new HashMap<>();
				Map<LocalDate, List<JiraIssueReferTime>> fullReleaseMap = new HashMap<>();
				Map<LocalDate, List<JiraIssueReferTime>> completedReleaseMap = new HashMap<>();
				Map<LocalDate, List<JiraIssueReferTime>> devCompletedReleaseMap = new HashMap<>();
				dateWiseLogs(allIssuesHistory, releaseList.stream().findFirst().orElse(null), releaseIssues,
						addedIssuesMap, removeIssueMap, fullReleaseMap, completedReleaseMap, devCompletedReleaseMap,
						basicProjConfigId);
				resultListMap.put(FULL_RELEASE, fullReleaseMap);
				resultListMap.put(ADDED_TO_RELEASE, addedIssuesMap);
				resultListMap.put(REMOVED_FROM_RELEASE, removeIssueMap);
				resultListMap.put(TOTAL_ISSUES, releaseIssues);
				resultListMap.put(RELEASE_PROGRESS, completedReleaseMap);
				resultListMap.put(DEV_COMPLETE_DATE_MAP, devCompletedReleaseMap);
			}

		}
		return resultListMap;
	}

	/**
	 * Used to Create Date wise log
	 *
	 * @param allIssuesHistory
	 *            List<JiraIssueCustomHistory>
	 * @param releaseName
	 *            Name of release
	 * @param releaseIssue
	 *            List<JiraIssue>
	 * @param addedIssuesMap
	 *            Map<LocalDate, List<JiraIssue>>
	 * @param removeIssueMap
	 *            Map<LocalDate, List<JiraIssue>>
	 * @param fullReleaseMap
	 *            Map<LocalDate, List<JiraIssue>>
	 * @param completedReleaseMap
	 *            Map<LocalDate, List<JiraIssue>>
	 */
	private void dateWiseLogs(List<JiraIssueCustomHistory> allIssuesHistory, String releaseName, // NOSONAR
			List<JiraIssue> releaseIssue, Map<LocalDate, List<JiraIssueReferTime>> addedIssuesMap,
			Map<LocalDate, List<JiraIssueReferTime>> removeIssueMap, Map<LocalDate, List<JiraIssueReferTime>> fullReleaseMap,
			Map<LocalDate, List<JiraIssueReferTime>> completedReleaseMap, Map<LocalDate, List<JiraIssueReferTime>> devCompletedReleaseMap,
			String basicProjConfigId) {

		releaseName = releaseName != null ? releaseName : "";
		String finalReleaseName = releaseName.toLowerCase();
		allIssuesHistory.forEach(issueHistory -> {
			List<JiraHistoryChangeLog> fixVersionUpdateLog = issueHistory.getFixVersionUpdationLog();
			fixVersionUpdateLog.sort(Comparator.comparing(JiraHistoryChangeLog::getUpdatedOn));
			int lastIndex = fixVersionUpdateLog.size() - 1;
			List<JiraIssue> jiraIssueList = getRespectiveJiraIssue(releaseIssue, issueHistory);
			fixVersionUpdateLog.stream()
					.filter(updateLogs -> updateLogs.getChangedTo().toLowerCase().contains(finalReleaseName)
							|| updateLogs.getChangedFrom().toLowerCase().contains(finalReleaseName))
					.forEach(updateLogs -> {
						LocalDate updatedLog;
						if (updateLogs.getChangedTo().toLowerCase().contains(finalReleaseName)) {
							if (fixVersionUpdateLog.get(lastIndex).getChangedTo().toLowerCase()
									.contains(finalReleaseName)) {
								updatedLog = fixVersionUpdateLog.get(lastIndex).getUpdatedOn().toLocalDate();
								List<JiraIssueReferTime> jiraIssueReferTime = createJiraIssueReferTime(fixVersionUpdateLog.get(lastIndex).getUpdatedOn(), jiraIssueList);
								fullReleaseMap.computeIfPresent(updatedLog, (k, v) -> {
									List<JiraIssueReferTime> jiraIssueReferTimes = new ArrayList<>(v);
									jiraIssueReferTimes.addAll(jiraIssueReferTime);
									return jiraIssueReferTimes;
								});
								fullReleaseMap.putIfAbsent(updatedLog, jiraIssueReferTime);
							}
							updatedLog = updateLogs.getUpdatedOn().toLocalDate();
							List<JiraIssueReferTime> jiraIssueReferTime = createJiraIssueReferTime(updateLogs.getUpdatedOn(), jiraIssueList);
							addedIssuesMap.computeIfPresent(updatedLog, (k, v) -> {
								List<JiraIssueReferTime> jiraIssueReferTimes = new ArrayList<>(v);
								jiraIssueReferTimes.addAll(jiraIssueReferTime);
								return jiraIssueReferTimes;
							});
							addedIssuesMap.putIfAbsent(updatedLog, jiraIssueReferTime);
						}
						if (updateLogs.getChangedFrom().toLowerCase().contains(finalReleaseName)) {
							List<JiraIssue> removeJiraIssueLIst = new ArrayList<>(jiraIssueList);
							updatedLog = updateLogs.getUpdatedOn().toLocalDate();
							List<JiraIssueReferTime> jiraIssueReferTime = createJiraIssueReferTime(updateLogs.getUpdatedOn(), removeJiraIssueLIst);
							removeIssueMap.computeIfPresent(updatedLog, (k, v) -> {
								List<JiraIssueReferTime> jiraIssueReferTimes = new ArrayList<>(v);
								jiraIssueReferTimes.addAll(jiraIssueReferTime);
								return jiraIssueReferTimes;
							});
							removeIssueMap.putIfAbsent(updatedLog, jiraIssueReferTime);
						}

					});
			createCompletedIssuesDateWiseMap(issueHistory, completedReleaseMap, devCompletedReleaseMap, releaseIssue,
					basicProjConfigId);

		});
	}

	private List<JiraIssueReferTime> createJiraIssueReferTime(LocalDateTime updatedOn, List<JiraIssue> jiraIssueList) {
		return jiraIssueList.stream().map(jiraIssue -> new JiraIssueReferTime(jiraIssue, updatedOn)).toList();
	}

	/**
	 * Create Completed Issue Date Wise
	 *
	 * @param issueHistory
	 *            Issue History
	 * @param completedIssues
	 *            Map<LocalDate, List<JiraIssue>>
	 * @param totalIssueList
	 *            List<JiraIssue>
	 */
	private void createCompletedIssuesDateWiseMap(JiraIssueCustomHistory issueHistory,
			Map<LocalDate, List<JiraIssueReferTime>> completedIssues, Map<LocalDate, List<JiraIssueReferTime>> devCompletedReleaseMap,
			List<JiraIssue> totalIssueList, String basicProjConfigId) {
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(new ObjectId(basicProjConfigId));
		List<JiraHistoryChangeLog> statusUpdateLog = issueHistory.getStatusUpdationLog();
		JiraIssueReleaseStatus jiraIssueReleaseStatus = getJiraIssueReleaseStatus();

		List<String> jiraReleaseDoneStatus = jiraIssueReleaseStatus.getClosedList().values().stream()
				.map(String::toLowerCase).collect(Collectors.toList());
		List<String> devDoneStatus = Optional.ofNullable(fieldMapping.getJiraDevDoneStatusKPI150())
				.orElse(new ArrayList<>()).stream().map(String::toLowerCase).collect(Collectors.toList());

		List<JiraHistoryChangeLog> qaCompletionStatusLog = statusUpdateLog.stream()
				.filter(statusLog -> jiraReleaseDoneStatus.contains(statusLog.getChangedTo().toLowerCase())
						|| jiraReleaseDoneStatus.contains(statusLog.getChangedFrom().toLowerCase()))
				.collect(Collectors.toList());
		List<JiraHistoryChangeLog> devCompletionStatusLog = statusUpdateLog.stream()
				.filter(statusLog -> devDoneStatus.contains(statusLog.getChangedTo().toLowerCase())
						|| devDoneStatus.contains(statusLog.getChangedFrom().toLowerCase()))
				.collect(Collectors.toList());
		// making QA completion date map
		if (CollectionUtils.isNotEmpty(qaCompletionStatusLog)) {
			final LocalDateTime updatedLog = getDoneDateBasedOnStatus(qaCompletionStatusLog, jiraReleaseDoneStatus,
					fieldMapping);
			List<JiraIssue> jiraIssueList = new ArrayList<>(getRespectiveJiraIssue(totalIssueList, issueHistory));
			List<JiraIssueReferTime> jiraIssueReferTime = createJiraIssueReferTime(updatedLog, jiraIssueList);
			completedIssues.computeIfPresent(updatedLog.toLocalDate(), (k, v) -> {
				List<JiraIssueReferTime> jiraIssueReferTimes = new ArrayList<>(v);
				jiraIssueReferTimes.addAll(jiraIssueReferTime);
				return jiraIssueReferTimes;
			});
			completedIssues.putIfAbsent(updatedLog.toLocalDate(), jiraIssueReferTime);
			completedIssues.remove(null);
		}
		// making dev completion date map
		if (CollectionUtils.isNotEmpty(devCompletionStatusLog)) {
			final LocalDateTime updatedLog = getDoneDateBasedOnStatus(devCompletionStatusLog, devDoneStatus, fieldMapping);
			List<JiraIssue> jiraIssueList = new ArrayList<>(getRespectiveJiraIssue(totalIssueList, issueHistory));
			List<JiraIssueReferTime> jiraIssueReferTime = createJiraIssueReferTime(updatedLog, jiraIssueList);
			devCompletedReleaseMap.computeIfPresent(updatedLog.toLocalDate(), (k, v) -> {
				List<JiraIssueReferTime> jiraIssueReferTimes = new ArrayList<>(v);
				jiraIssueReferTimes.addAll(jiraIssueReferTime);
				return jiraIssueReferTimes;
			});
			devCompletedReleaseMap.putIfAbsent(updatedLog.toLocalDate(), jiraIssueReferTime);
			devCompletedReleaseMap.remove(null);
		}
	}

	/**
	 * Method to find first close date of last close cycle
	 *
	 * @param statusUpdateLog
	 *            statusUpdateLog
	 * @param devOrQaDoneStatus
	 *            Dev or Qa Done Status
	 * @param fieldMapping
	 *            fieldMapping
	 * @return Map<String, LocalDate>
	 */
	private static LocalDateTime getDoneDateBasedOnStatus(List<JiraHistoryChangeLog> statusUpdateLog,
			List<String> devOrQaDoneStatus, FieldMapping fieldMapping) {
		Map<String, LocalDateTime> closedStatusDateMap = new HashMap<>();
		for (JiraHistoryChangeLog jiraHistoryChangeLog : statusUpdateLog) {
			LocalDateTime activityDate = jiraHistoryChangeLog.getUpdatedOn();
			// reopened scenario
			if (devOrQaDoneStatus.contains(jiraHistoryChangeLog.getChangedFrom().toLowerCase())
					&& jiraHistoryChangeLog.getChangedTo().equalsIgnoreCase(fieldMapping.getStoryFirstStatus())) {
				closedStatusDateMap.clear();
			}
			// first close date of last close cycle
			if (devOrQaDoneStatus.contains(jiraHistoryChangeLog.getChangedTo().toLowerCase())) {
				if (closedStatusDateMap.containsKey(jiraHistoryChangeLog.getChangedTo().toLowerCase())) {
					closedStatusDateMap.clear();
				}
				closedStatusDateMap.put(jiraHistoryChangeLog.getChangedTo().toLowerCase(), activityDate);
			}
		}
		// Getting the min date of closed status.
		return closedStatusDateMap.values().stream().filter(Objects::nonNull).min(LocalDateTime::compareTo).orElse(null);
	}

	/**
	 * Populate Release Wise Leaf Node Value
	 *
	 * @param latestRelease
	 *            List<Node>
	 * @param kpiElement
	 *            kpiElement
	 * @param kpiRequest
	 *            kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void releaseWiseLeafNodeValue(Node latestRelease, KpiElement kpiElement, // NOSONAR
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		String startDate = latestRelease.getReleaseFilter().getStartDate();
		String endDate = latestRelease.getReleaseFilter().getEndDate();
		String releaseState = Optional.ofNullable(latestRelease.getAccountHierarchy().getReleaseState()).orElse("");

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestRelease, null, null, kpiRequest);
		List<JiraIssue> releaseIssues = (List<JiraIssue>) resultMap.get(TOTAL_ISSUES);
		Map<LocalDate, List<JiraIssueReferTime>> completedReleaseMap = (Map<LocalDate, List<JiraIssueReferTime>>) resultMap
				.get(RELEASE_PROGRESS);
		Map<LocalDate, List<JiraIssueReferTime>> addedIssuesReferMap = (Map<LocalDate, List<JiraIssueReferTime>>) resultMap
				.get(ADDED_TO_RELEASE);
		Map<LocalDate, List<JiraIssueReferTime>> fullReleaseIssueReferMap = (Map<LocalDate, List<JiraIssueReferTime>>) resultMap
				.get(FULL_RELEASE);

		Map<LocalDate, List<JiraIssueReferTime>> removeIssueReferMap = (Map<LocalDate, List<JiraIssueReferTime>>) resultMap
				.get(REMOVED_FROM_RELEASE);
		Map<LocalDate, List<JiraIssueReferTime>> devCompletedIssueMap = (Map<LocalDate, List<JiraIssueReferTime>>) resultMap
				.get(DEV_COMPLETE_DATE_MAP);

		List<IterationKpiValue> iterationKpiValueList = new ArrayList<>();
		long range = 0;
		String duration;
		if (CollectionUtils.isNotEmpty(releaseIssues) && MapUtils.isNotEmpty(fullReleaseIssueReferMap)) {
			Object basicProjectConfigId = latestRelease.getProjectFilter().getBasicProjectConfigId();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			/*
			 * if start-time is absent, then the date at which issue was added and remained
			 * added in the entire release is considered to be the start date if end date is
			 * absent then it means that issue is unreleased, so till today we can consider
			 * as end date
			 */
			LocalDateTime startLocalDate = StringUtils.isEmpty(startDate)
					? fullReleaseIssueReferMap.keySet().stream().filter(Objects::nonNull)
							.map(LocalDate::atStartOfDay).min(LocalDateTime::compareTo).orElse(null)
					: LocalDateTime.parse(startDate);
			LocalDateTime endLocalDate = StringUtils.isEmpty(endDate) ? DateUtil.getTodayTime()
					: LocalDateTime.parse(endDate);
			// increment the startDate w.r.t Count of days field for plotting to start from
			// updated start date
			Map<String, Integer> startDateCountKPI150 = Optional.ofNullable(fieldMapping.getStartDateCountKPI150())
					.orElse(new HashMap<>());
			startLocalDate = Objects.requireNonNull(startLocalDate)
					.plusDays(startDateCountKPI150.getOrDefault(latestRelease.getId(), 0));
			Map<String, Long> durationRangeMap = getDurationRangeMap(startLocalDate, endLocalDate);
			duration = durationRangeMap.keySet().stream().findFirst().orElse("");
			range = durationRangeMap.values().stream().findFirst().orElse(0L);
			Boolean isPopulateByDevDone = ObjectUtils.defaultIfNull(fieldMapping.isPopulateByDevDoneKPI150(), false);
			Map<LocalDate, List<JiraIssueReferTime>> originalCompletedIssueMap = deepCopyMap(completedReleaseMap);
			Map<LocalDate, List<JiraIssueReferTime>> originalDevCompletedIssueMap = deepCopyMap(devCompletedIssueMap);
			Map<LocalDate, List<JiraIssueReferTime>> originalFullReleaseMap = deepCopyMap(fullReleaseIssueReferMap);
			completedReleaseMap = prepareIssueBeforeStartDate(completedReleaseMap, startLocalDate);
			fullReleaseIssueReferMap = prepareIssueBeforeStartDate(fullReleaseIssueReferMap, startLocalDate);
			devCompletedIssueMap = prepareIssueBeforeStartDate(devCompletedIssueMap, startLocalDate);
			// if populateByDev is enabled then burnUp Release Progress line will be
			// populated by
			// devCompletionDate (dev complete) else releaseCompletionMap (qa complete)
			Map<LocalDate, List<JiraIssueReferTime>> startDateAdjustedDoneReferMap = Boolean.TRUE.equals(isPopulateByDevDone)
					? devCompletedIssueMap
					: completedReleaseMap;
			Map<LocalDate, List<JiraIssueReferTime>> originalIssueDoneReferMap = Boolean.TRUE.equals(isPopulateByDevDone)
					? originalDevCompletedIssueMap
					: originalCompletedIssueMap;

			tempStartDate = startLocalDate;
			allReleaseTaggedIssue.clear();

			List<JiraIssue> overallIssues = new ArrayList<>();
			List<JiraIssue> overallCompletedIssues = new ArrayList<>();
			List<DataCountGroup> issueCountDataGroup = new ArrayList<>();
			List<DataCountGroup> issueSizeCountDataGroup = new ArrayList<>();
			Map<String, Object> predictionDataMap = new HashMap<>();

			// populating release predication date on the basis of latest closed selected
			// releases
			Map<String, Object> averageDataMap = new HashMap<>();
			ReleaseSpecification releaseSpecification = new ReleaseSpecification();
			if (CollectionUtils.isNotEmpty(fieldMapping.getReleaseListKPI150())) {
				averageDataMap = getAvgVelocity(fieldMapping, releaseSpecification);
			}
			//convert List<JiraIssueReferTime> to List<JiraIssue>
			Map<LocalDate,List<JiraIssue>> addedIssuesMap= convertReferMapToJiraIssue(addedIssuesReferMap);
			Map<LocalDate,List<JiraIssue>> removeIssueMap= convertReferMapToJiraIssue(removeIssueReferMap);
			Map<LocalDate,List<JiraIssue>> fullReleaseIssueMap= convertReferMapToJiraIssue(fullReleaseIssueReferMap);
			Map<LocalDate,List<JiraIssue>> originalIssueDoneMap= convertReferMapToJiraIssue(originalIssueDoneReferMap);
			fullReleaseIssueMap.forEach((k, v) -> allReleaseTaggedIssue.addAll(v));
			Map<LocalDate,List<JiraIssue>> startDateAdjustedDoneMap= convertReferMapToJiraIssue(startDateAdjustedDoneReferMap);


			// if no issue is closed & status is "Released" in a release prediction will not
			// be shown
			if (releaseState.equalsIgnoreCase(CommonConstant.RELEASED) || MapUtils.isEmpty(startDateAdjustedDoneMap)) {
				// populating only release scope vs progress
				for (long i = 0; i < range && !startLocalDate.isAfter(endLocalDate); i++) {
					DataCountGroup issueCount = new DataCountGroup();
					DataCountGroup issueSize = new DataCountGroup();
					CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateTimeForDataFiltering(startLocalDate,
							duration);
					Map<String, List<JiraIssue>> filterWiseGroupedMap = createFilterWiseGroupedMap(dateRange,
							addedIssuesMap, removeIssueMap, fullReleaseIssueMap, overallIssues,
							startDateAdjustedDoneMap, overallCompletedIssues);
					overallCompletedIssues = filterWiseGroupedMap.getOrDefault(OVERALL_COMPLETED, new ArrayList<>());
					overallIssues = filterWiseGroupedMap.getOrDefault(OVERALL_ISSUE, new ArrayList<>());
					String date = getRange(dateRange, duration);
					populateFilterWiseDataMap(filterWiseGroupedMap, issueCount, issueSize, date, duration, fieldMapping,
							predictionDataMap);
					startLocalDate = getNextRangeDate(duration, startLocalDate, endLocalDate);
					issueCountDataGroup.add(issueCount);
					issueSizeCountDataGroup.add(issueSize);
				}
			} else {

				// populating release scope vs release progress followed by its prediction on
				// avg completion rate
				if (CollectionUtils.isEmpty(fieldMapping.getReleaseListKPI150())) {
					averageDataMap = getAverageData(fieldMapping, startLocalDate.toLocalDate(), originalIssueDoneMap,
							releaseSpecification);
				}

				double avgIssueCount = (double) averageDataMap.getOrDefault(AVG_ISSUE_COUNT, 0d);
				double avgStoryPoint = (double) averageDataMap.getOrDefault(AVG_STORY_POINT, 0d);

				// finding the release scope and progress for which prediction need to be cal
				CustomDateRange dateRangeTillToday = new CustomDateRange();
				dateRangeTillToday.setStartDate(startLocalDate.toLocalDate());
				dateRangeTillToday.setEndDate(DateUtil.getTodayDate());

				List<JiraIssue> overallCompletedIssue = new ArrayList<>();
				Map<String, List<JiraIssue>> filterWiseGroupedMapTillNow = createFilterWiseGroupedMap(
						dateRangeTillToday, addedIssuesMap, removeIssueMap, fullReleaseIssueMap, allReleaseTaggedIssue,
						startDateAdjustedDoneMap, overallCompletedIssue);
				List<JiraIssue> releaseScopeToReach = filterWiseGroupedMapTillNow.getOrDefault(RELEASE_SCOPE,
						new ArrayList<>());
				List<JiraIssue> releaseProgressTillNow = filterWiseGroupedMapTillNow.getOrDefault(RELEASE_PROGRESS,
						new ArrayList<>());
				releaseScopeToReach.retainAll(allReleaseTaggedIssue);
				releaseProgressTillNow.retainAll(allReleaseTaggedIssue);
				// based on the value to reach & avg value finding the Prediction date
				final LocalDateTime predictionEndDate = calPredictionEndDate(releaseScopeToReach,
						releaseProgressTillNow, fieldMapping, avgIssueCount, avgStoryPoint);

				// using the prediction end date & release start date to cal x-axis duration &
				// range
				Map<String, Long> durationMapPrediction = getDurationRangeMap(startLocalDate, predictionEndDate);
				duration = durationMapPrediction.keySet().stream().findFirst().orElse("");
				range = durationMapPrediction.values().stream().findFirst().orElse(0L);

				// setting the initial value form which prediction population will start
				double issueCountPrediction = releaseProgressTillNow.size();
				double issueSizePrediction = roundingOff(getStoryPoint(releaseProgressTillNow, fieldMapping));
				predictionDataMap.put(ISSUE_COUNT_PREDICTION, issueCountPrediction);
				predictionDataMap.put(ISSUE_SIZE_PREDICTION, issueSizePrediction);
				boolean isPredictionBoundary = true; // used to check and add data point of release
				// scope,progress,prediction at boundary
				for (long i = 0; i <= range && !startLocalDate.isAfter(predictionEndDate); i++) {
					DataCountGroup issueCount = new DataCountGroup();
					DataCountGroup issueSize = new DataCountGroup();
					CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateTimeForDataFiltering(startLocalDate,
							duration);
					Map<String, List<JiraIssue>> filterWiseGroupedMap = createFilterWiseGroupedMap(dateRange,
							addedIssuesMap, removeIssueMap, fullReleaseIssueMap, overallIssues,
							startDateAdjustedDoneMap, overallCompletedIssues);
					overallCompletedIssues = filterWiseGroupedMap.getOrDefault(OVERALL_COMPLETED, new ArrayList<>());
					overallIssues = filterWiseGroupedMap.getOrDefault(OVERALL_ISSUE, new ArrayList<>());
					String date = getRange(dateRange, duration);
					// start populate the release prediction if startDate is greater than today
					isPredictionBoundary = getNextPredictionDataPt(predictionDataMap, dateRange, isPredictionBoundary,
							releaseScopeToReach, averageDataMap, fieldMapping);
					populateFilterWiseDataMap(filterWiseGroupedMap, issueCount, issueSize, date, duration, fieldMapping,
							predictionDataMap);
					startLocalDate = getNextRangeDate(duration, startLocalDate, predictionEndDate);
					if (CollectionUtils.isNotEmpty(issueCount.getValue())) {
						issueCountDataGroup.add(issueCount);
					}
					if (CollectionUtils.isNotEmpty(issueSize.getValue())) {
						issueSizeCountDataGroup.add(issueSize);
					}
				}
			}
			populateExcelDataObject(requestTrackerId, excelData, releaseIssues, originalFullReleaseMap,
					originalCompletedIssueMap, originalDevCompletedIssueMap, fieldMapping);
			createExcelDataAndTrendValueList(kpiElement, excelData, iterationKpiValueList, issueCountDataGroup,
					issueSizeCountDataGroup, releaseSpecification);

		}
		kpiElement.setTrendValueList(iterationKpiValueList);
	}

	private Map<LocalDate, List<JiraIssue>> convertReferMapToJiraIssue(Map<LocalDate, List<JiraIssueReferTime>> addedIssuesReferMap) {
		Map<LocalDate, List<JiraIssue>> mpp= new HashMap<>();
		addedIssuesReferMap.forEach((key, value) -> {
            List<JiraIssue> jiraIssues = new ArrayList<>();
            for (JiraIssueReferTime jiraIssueReferTime : value) {
                jiraIssues.add(jiraIssueReferTime.getJiraIssue());
            }
            mpp.put(key, jiraIssues);
        });
		return mpp;
	}

	/**
	 * Method used to get successive data point of prediction
	 *
	 * @param predictionDataMap
	 *            Map<String, Object>
	 * @param dateRange
	 *            Date Range for start & end Date
	 * @param isPredictionBoundary
	 *            Flag of start of prediction
	 * @param releaseScopeToReach
	 *            List<JiraIssue>
	 * @param averageDataMap
	 *            Map<String, Object>
	 * @param fieldMapping
	 *            fieldMapping
	 * @return prediction boundary flag
	 */
	private boolean getNextPredictionDataPt(Map<String, Object> predictionDataMap, CustomDateRange dateRange,
			boolean isPredictionBoundary, List<JiraIssue> releaseScopeToReach, Map<String, Object> averageDataMap,
			FieldMapping fieldMapping) {
		double issueSizePrediction;
		double issueCountPrediction;
		double avgIssueCount = (double) averageDataMap.getOrDefault(AVG_ISSUE_COUNT, 0d);
		double avgStoryPoint = (double) averageDataMap.getOrDefault(AVG_STORY_POINT, 0d);
		final int issueCountToReach = releaseScopeToReach.size();
		final Double storyPointToReach = getStoryPoint(releaseScopeToReach, fieldMapping);
		// flag to start population of prediction from today onwards
		final boolean isStartPrediction = DateUtil.isWithinDateTimeRange(DateUtil.getTodayTime(),
				dateRange.getStartDateTime(), dateRange.getEndDateTime())
				|| dateRange.getStartDateTime().isAfter(DateUtil.getTodayTime());

		if (isStartPrediction && MapUtils.isNotEmpty(averageDataMap)) {
			issueCountPrediction = (double) predictionDataMap.get(ISSUE_COUNT_PREDICTION);
			issueSizePrediction = (double) predictionDataMap.get(ISSUE_SIZE_PREDICTION);
			// checking if predication values crossed the release scope
			predictionDataMap.put(IS_ISSUE_COUNT_ACHIEVED,
					issueCountPrediction >= issueCountToReach || avgIssueCount == 0);
			predictionDataMap.put(IS_STORY_POINT_ACHIEVED,
					issueSizePrediction >= storyPointToReach || avgStoryPoint == 0);
			// working days between intervals namely: [days, week or month]
			long daysInterval = (long) CommonUtils.getWorkingDays(dateRange.getStartDate(), dateRange.getEndDate()) + 1;
			if (!isPredictionBoundary) {
				// cal the next issueCount/Sp for prediction data
				issueCountPrediction = roundingOff(issueCountPrediction + (avgIssueCount * daysInterval));
				issueSizePrediction = roundingOff(issueSizePrediction + (avgStoryPoint * daysInterval));
			}

			predictionDataMap.put(ISSUE_COUNT_PREDICTION,
					roundingOff(Math.min(issueCountPrediction, issueCountToReach)));
			predictionDataMap.put(ISSUE_SIZE_PREDICTION, roundingOff(Math.min(issueSizePrediction, storyPointToReach)));
			predictionDataMap.put(SHOW_PREDICTION, true);
			predictionDataMap.put(IS_PREDICTION_BOUNDARY, isPredictionBoundary);
			isPredictionBoundary = false;
		}
		return isPredictionBoundary;
	}

	/**
	 * Based on Average Closure Rate & Release Scope to Reach cal prediction End
	 * Date
	 *
	 * @param releaseScopeToReach
	 *            List<JiraIssue>
	 * @param releaseProgressTillNow
	 *            List<JiraIssue>
	 * @param fieldMapping
	 *            fieldMapping
	 * @param avgIssueCount
	 *            avg Issue Count
	 * @param avgStoryPoint
	 *            avg SP
	 * @return Prediction End Date
	 */
	private LocalDateTime calPredictionEndDate(List<JiraIssue> releaseScopeToReach,
			List<JiraIssue> releaseProgressTillNow, FieldMapping fieldMapping, double avgIssueCount,
			double avgStoryPoint) {
		// finding the prediction end date
		long timeRequiredForIssueCount = 0;
		long timeRequiredForSp = 0;
		int remainingIssues = releaseScopeToReach.size() - releaseProgressTillNow.size();
		double remainingSp = getStoryPoint(releaseScopeToReach, fieldMapping)
				- getStoryPoint(releaseProgressTillNow, fieldMapping);
		if (avgIssueCount != 0) {
			timeRequiredForIssueCount = (long) Math.ceil(remainingIssues / avgIssueCount);
		}
		if (avgStoryPoint != 0) {
			timeRequiredForSp = (long) Math.ceil(remainingSp / avgStoryPoint);
		}
		return CommonUtils.getWorkingDayAfterAdditionofDays(DateUtil.getTodayTime(),
				(int) Math.max(timeRequiredForSp, timeRequiredForIssueCount));
	}

	/**
	 * Method to Calculate the Average Closure Rate
	 *
	 * @param fieldMapping
	 *            fieldMapping
	 * @param startLocalDate
	 *            startDate
	 * @param completedReleaseMap
	 *            Map<LocalDate, List<JiraIssue>>
	 * @param releaseSpecification
	 * @return Map of Avg Issue Count, Story Point
	 */
	private Map<String, Object> getAverageData(FieldMapping fieldMapping, LocalDate startLocalDate,
			Map<LocalDate, List<JiraIssue>> completedReleaseMap, ReleaseSpecification releaseSpecification) {
		Map<String, Object> averageDataMap = new HashMap<>();
		double avgIssueCount;
		double avgStoryPoint;
		int countOfDaysTillToday = 0;

		LocalDate currentDate = startLocalDate;
		List<JiraIssue> completedIssuesTillTodayList = new ArrayList<>();
		// completed issue between prediction start date and today both inclusive
		while (DateUtil.isWithinDateRange(currentDate, startLocalDate, LocalDate.now())) {
			completedIssuesTillTodayList.addAll(completedReleaseMap.getOrDefault(currentDate, new ArrayList<>()));
			if (currentDate.getDayOfWeek() != DayOfWeek.SATURDAY && currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
				countOfDaysTillToday++;
			}
			currentDate = currentDate.plusDays(1);
		}
		// out of all completed, what all issues were completed & still tagged to
		// release
		completedIssuesTillTodayList.retainAll(allReleaseTaggedIssue);
		// calculate the avg issue count and story point
		if (countOfDaysTillToday != 0 && CollectionUtils.isNotEmpty(completedIssuesTillTodayList)) {
			avgIssueCount = (double) completedIssuesTillTodayList.size() / countOfDaysTillToday;
			avgStoryPoint = getStoryPoint(completedIssuesTillTodayList, fieldMapping) / countOfDaysTillToday;
			averageDataMap.put(AVG_ISSUE_COUNT, roundingOff(avgIssueCount));
			averageDataMap.put(AVG_STORY_POINT, roundingOff(avgStoryPoint));

			releaseSpecification.setReleaseIssueCount(completedIssuesTillTodayList.size());
			releaseSpecification.setReleaseStoryPoint(getStoryPoint(completedIssuesTillTodayList, fieldMapping));
			releaseSpecification.setReleaseIssueCountVelocity(roundingOff(avgIssueCount));
			releaseSpecification.setReleaseStoryPointVelocity(roundingOff(avgStoryPoint));
			releaseSpecification.setReleaseDuration(countOfDaysTillToday);
		}

		return averageDataMap;
	}

	/**
	 * Method for setting Trend value & Excel Data
	 *
	 * @param kpiElement
	 *            kpiElement
	 * @param excelData
	 *            List<KPIExcelData>
	 * @param iterationKpiValueList
	 *            List<IterationKpiValue>
	 * @param issueCountDataGroup
	 *            List<DataCountGroup>
	 * @param issueSizeCountDataGroup
	 *            List<DataCountGroup>
	 * @param releaseSpecification
	 *            releaseSpecification
	 */
	private void createExcelDataAndTrendValueList(KpiElement kpiElement, List<KPIExcelData> excelData,
			List<IterationKpiValue> iterationKpiValueList, List<DataCountGroup> issueCountDataGroup,
			List<DataCountGroup> issueSizeCountDataGroup, ReleaseSpecification releaseSpecification) {
		if (CollectionUtils.isNotEmpty(issueCountDataGroup)) {
			String issueCountAvgVelocity = AVERAGE_VELOCITY_IS + releaseSpecification.getReleaseIssueCountVelocity()
					+ " (" + releaseSpecification.getReleaseIssueCount() + ISSUE_COUNT_IN
					+ releaseSpecification.getReleaseDuration() + DAYS;
			String storyPointAvgVelocity = AVERAGE_VELOCITY_IS + releaseSpecification.getReleaseStoryPointVelocity()
					+ " (" + releaseSpecification.getReleaseStoryPoint() + SPS_IN
					+ releaseSpecification.getReleaseDuration() + DAYS;
			Map<String, Object> additionalInfoIssueMap = new HashMap<>();

			additionalInfoIssueMap.put(IS_XAXIS_GAP_REQUIRED, true);
			additionalInfoIssueMap.put(CUSTOMISED_GROUP, RELEASE_PREDICTION);
			additionalInfoIssueMap.put(TOTAL_AVG_VELOCITY, issueCountAvgVelocity);

			Map<String, Object> additionalInfoSPMap = new HashMap<>();
			additionalInfoSPMap.put(IS_XAXIS_GAP_REQUIRED, true);
			additionalInfoSPMap.put(CUSTOMISED_GROUP, RELEASE_PREDICTION);
			additionalInfoSPMap.put(TOTAL_AVG_VELOCITY, storyPointAvgVelocity);

			IterationKpiValue kpiValueIssueCount = new IterationKpiValue();
			kpiValueIssueCount.setDataGroup(issueCountDataGroup);
			kpiValueIssueCount.setFilter1(ISSUE_COUNT);
			kpiValueIssueCount.setAdditionalInfo(additionalInfoIssueMap);
			IterationKpiValue kpiValueSizeCount = new IterationKpiValue();
			kpiValueSizeCount.setDataGroup(issueSizeCountDataGroup);
			kpiValueSizeCount.setFilter1(STORY_POINT);
			kpiValueSizeCount.setAdditionalInfo(additionalInfoSPMap);
			iterationKpiValueList.add(kpiValueSizeCount);
			iterationKpiValueList.add(kpiValueIssueCount);

			kpiElement.setModalHeads(KPIExcelColumn.RELEASE_BURNUP.getColumns());
			kpiElement.setExcelColumns(KPIExcelColumn.RELEASE_BURNUP.getColumns());
			kpiElement.setExcelData(excelData);
		}
	}

	/**
	 * issue completed/tagged before the release version but happened to be present
	 * in selected release then issues to be present on the first day of release
	 * start date
	 */
	private Map<LocalDate, List<JiraIssueReferTime>> prepareIssueBeforeStartDate(
			Map<LocalDate, List<JiraIssueReferTime>> completedReleaseMap, LocalDateTime startLocalDate) {
		Map<LocalDate, List<JiraIssueReferTime>> rangedCompletedMap = new HashMap<>();
		completedReleaseMap.forEach((date, issues) -> {
			if (!date.atStartOfDay().isAfter(startLocalDate)) {
				rangedCompletedMap.computeIfPresent(startLocalDate.toLocalDate(), (key, value) -> {
					List<JiraIssueReferTime> jiraIssueReferTimes = new ArrayList<>(value);
					jiraIssueReferTimes.addAll(issues);
					return jiraIssueReferTimes;
				});
				rangedCompletedMap.putIfAbsent(startLocalDate.toLocalDate(), issues);
			} else {
				rangedCompletedMap.put(date, issues);
			}
		});
		return rangedCompletedMap;
	}

	/**
	 * DeepClone the map
	 *
	 * @param originalMap
	 * @return
	 */
	public static Map<LocalDate, List<JiraIssueReferTime>> deepCopyMap(Map<LocalDate, List<JiraIssueReferTime>> originalMap) {
		return originalMap.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> new ArrayList<>(entry.getValue())));
	}

	/**
	 * Method for calculation x-axis duration & range
	 *
	 * @param startLocalDate
	 *            startDate
	 * @param endLocalDate
	 *            endDate
	 * @return Map<String, Long>
	 */
	private Map<String, Long> getDurationRangeMap(LocalDateTime startLocalDate, LocalDateTime endLocalDate) {
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
	 * Method for population of Release Scope,Progress & Prediction
	 *
	 * @param filterWiseGroupedMap
	 *            Map<String, List<JiraIssue>>
	 * @param issueCount
	 *            DataCountGroup of Issue Count
	 * @param issueSize
	 *            DataCountGroup of Issue Size
	 * @param date
	 *            Date of x-axis
	 * @param duration
	 *            Duration
	 * @param fieldMapping
	 *            fieldMapping
	 * @param predictionDataMap
	 *            Map<String, Object>
	 */
	private void populateFilterWiseDataMap(Map<String, List<JiraIssue>> filterWiseGroupedMap, DataCountGroup issueCount,
			DataCountGroup issueSize, String date, String duration, FieldMapping fieldMapping,
			Map<String, Object> predictionDataMap) {
		List<DataCount> issueCountDataList = new ArrayList<>();
		List<DataCount> issueSizeDataList = new ArrayList<>();

		List<JiraIssue> overallIssues = filterWiseGroupedMap.getOrDefault(RELEASE_SCOPE, new ArrayList<>());
		List<JiraIssue> completedIssues = filterWiseGroupedMap.getOrDefault(RELEASE_PROGRESS, new ArrayList<>());

		double predictionIssueCount = (double) predictionDataMap.getOrDefault(ISSUE_COUNT_PREDICTION, 0d);
		double predictionIssueSize = (double) predictionDataMap.getOrDefault(ISSUE_SIZE_PREDICTION, 0d);
		boolean showPrediction = (boolean) predictionDataMap.getOrDefault(SHOW_PREDICTION, false);
		boolean isPredictionBoundary = (boolean) predictionDataMap.getOrDefault(IS_PREDICTION_BOUNDARY, false);
		boolean isIssueCountAchieved = (boolean) predictionDataMap.getOrDefault(IS_ISSUE_COUNT_ACHIEVED, false);
		boolean isStoryPointAchieved = (boolean) predictionDataMap.getOrDefault(IS_STORY_POINT_ACHIEVED, false);

		if (!showPrediction) {
			createDataCount((long) overallIssues.size(), LINE_GRAPH_TYPE, RELEASE_SCOPE, issueCountDataList,
					CommonConstant.SOLID_LINE_TYPE);
			createDataCount((long) completedIssues.size(), LINE_GRAPH_TYPE, RELEASE_PROGRESS, issueCountDataList,
					CommonConstant.SOLID_LINE_TYPE);
		} else if (isPredictionBoundary) { // populating release Progress & prediction when boundary is reached
			createDataCount((long) overallIssues.size(), LINE_GRAPH_TYPE, RELEASE_SCOPE, issueCountDataList,
					CommonConstant.SOLID_LINE_TYPE);
			createDataCount((long) completedIssues.size(), LINE_GRAPH_TYPE, RELEASE_PROGRESS, issueCountDataList,
					CommonConstant.SOLID_LINE_TYPE);
			createDataCount(predictionIssueCount, LINE_GRAPH_TYPE, RELEASE_PREDICTION, issueCountDataList,
					CommonConstant.DOTTED_LINE_TYPE);
		} else if (!isIssueCountAchieved) {
			createDataCount((long) overallIssues.size(), LINE_GRAPH_TYPE, RELEASE_SCOPE, issueCountDataList,
					CommonConstant.SOLID_LINE_TYPE);
			createDataCount(predictionIssueCount, LINE_GRAPH_TYPE, RELEASE_PREDICTION, issueCountDataList,
					CommonConstant.DOTTED_LINE_TYPE);

		}
		issueCount.setFilter(date);
		issueCount.setDuration(duration);
		issueCount.setValue(issueCountDataList);

		if (!showPrediction) {
			createDataCount(getStoryPoint(overallIssues, fieldMapping), LINE_GRAPH_TYPE, RELEASE_SCOPE,
					issueSizeDataList, CommonConstant.SOLID_LINE_TYPE);
			createDataCount(getStoryPoint(completedIssues, fieldMapping), LINE_GRAPH_TYPE, RELEASE_PROGRESS,
					issueSizeDataList, CommonConstant.SOLID_LINE_TYPE);
		} else if (isPredictionBoundary) {// populating release Progress & prediction when boundary is reached
			createDataCount(getStoryPoint(overallIssues, fieldMapping), LINE_GRAPH_TYPE, RELEASE_SCOPE,
					issueSizeDataList, CommonConstant.SOLID_LINE_TYPE);
			createDataCount(getStoryPoint(completedIssues, fieldMapping), LINE_GRAPH_TYPE, RELEASE_PROGRESS,
					issueSizeDataList, CommonConstant.SOLID_LINE_TYPE);
			createDataCount(predictionIssueSize, LINE_GRAPH_TYPE, RELEASE_PREDICTION, issueSizeDataList,
					CommonConstant.DOTTED_LINE_TYPE);
		} else if (!isStoryPointAchieved) {
			createDataCount(getStoryPoint(overallIssues, fieldMapping), LINE_GRAPH_TYPE, RELEASE_SCOPE,
					issueSizeDataList, CommonConstant.SOLID_LINE_TYPE);
			createDataCount(predictionIssueSize, LINE_GRAPH_TYPE, RELEASE_PREDICTION, issueSizeDataList,
					CommonConstant.DOTTED_LINE_TYPE);
		}

		issueSize.setFilter(date);
		issueSize.setDuration(duration);
		issueSize.setValue(issueSizeDataList);

	}

	/**
	 * Method to Populate the DataCount Object
	 *
	 * @param value
	 *            value
	 * @param graphType
	 *            graphType
	 * @param label
	 *            Label
	 * @param issueCountDataList
	 *            List<DataCount>
	 * @param lineCategory
	 *            Line Type
	 */
	private void createDataCount(Object value, String graphType, String label, List<DataCount> issueCountDataList,
			String lineCategory) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setKpiGroup(label);
		dataCount.setValue(value);
		dataCount.setGraphType(graphType);
		dataCount.setLineCategory(lineCategory);
		issueCountDataList.add(dataCount);
	}

	/**
	 * Method to get Release Scope,Progress in a DateRange
	 *
	 * @param dateRange
	 *            CustomDateRange
	 * @param addedIssuesMap
	 *            Map<LocalDate, List<JiraIssue>>
	 * @param removeIssueMap
	 *            Map<LocalDate, List<JiraIssue>>
	 * @param fullReleaseIssueMap
	 *            Map<LocalDate, List<JiraIssue>>
	 * @param overallIssues
	 *            List<JiraIssue>
	 * @param completedReleaseMap
	 *            Map<LocalDate, List<JiraIssue>>
	 * @param overallCompletedIssues
	 *            List<JiraIssue>
	 * @return Map<String, List < JiraIssue>>
	 */
	@SuppressWarnings("unchecked")
	private Map<String, List<JiraIssue>> createFilterWiseGroupedMap(CustomDateRange dateRange,
			Map<LocalDate, List<JiraIssue>> addedIssuesMap, Map<LocalDate, List<JiraIssue>> removeIssueMap,
			Map<LocalDate, List<JiraIssue>> fullReleaseIssueMap, List<JiraIssue> overallIssues,
			Map<LocalDate, List<JiraIssue>> completedReleaseMap, List<JiraIssue> overallCompletedIssues) {
		Map<String, List<JiraIssue>> groupedMap = new HashMap<>();
		List<JiraIssue> defaultIssues = new ArrayList<>();
		List<JiraIssue> allAddedIssues = new ArrayList<>();
		List<JiraIssue> removedIssues = new ArrayList<>();
		List<JiraIssue> completedIssues = new ArrayList<>();

		for (LocalDate currentDate = dateRange.getStartDate(); DateUtil.isWithinDateRange(currentDate,
				dateRange.getStartDate(), dateRange.getEndDate()); currentDate = currentDate.plusDays(1)) {
			if (currentDate.isEqual(tempStartDate.toLocalDate())) {

				defaultIssues.addAll(addedIssuesMap.getOrDefault(currentDate, new ArrayList<>()));
				defaultIssues.removeAll(removeIssueMap.getOrDefault(currentDate, new ArrayList<>()));
				defaultIssues.addAll(fullReleaseIssueMap.getOrDefault(currentDate, new ArrayList<>()));// defaultMap
			} else {
				defaultIssues.addAll(fullReleaseIssueMap.getOrDefault(currentDate, new ArrayList<>()));// defaultMap
				allAddedIssues.addAll(addedIssuesMap.getOrDefault(currentDate, new ArrayList<>()));
				removedIssues.addAll(removeIssueMap.getOrDefault(currentDate, new ArrayList<>()));
			}
			completedIssues.addAll(completedReleaseMap.getOrDefault(currentDate, new ArrayList<>()));
		}

		// if on same day issue is added to release and removed from release, then on
		// same day delete that issue from
		// removed
		List<JiraIssue> commonIssues = (List<JiraIssue>) CollectionUtils.intersection(allAddedIssues, removedIssues);
		removedIssues.removeAll(commonIssues);
		allAddedIssues.removeAll(commonIssues);
		overallIssues.removeAll(commonIssues);
		List<JiraIssue> commonIssuesRemoved = (List<JiraIssue>) CollectionUtils.intersection(overallIssues,
				removedIssues);
		// issues removed and added on same day
		List<JiraIssue> removedThenAdded = (List<JiraIssue>) CollectionUtils.intersection(commonIssues, defaultIssues);
		allAddedIssues.addAll(removedThenAdded);
		allAddedIssues.removeAll(overallIssues);
		groupedMap.put(SCOPE_REMOVED, commonIssuesRemoved);
		groupedMap.put(SCOPE_ADDED, allAddedIssues);

		overallIssues.addAll(defaultIssues);
		overallIssues.addAll(allAddedIssues);
		overallIssues.removeAll(removedIssues);
		overallIssues = overallIssues.stream().distinct().collect(Collectors.toList());

		// issues which were completed within Release duration, but were out of selected
		// release when completed
		overallCompletedIssues.addAll(completedIssues);
		List<JiraIssue> allCompletedIssuesOutOfOverall = new ArrayList<>(overallCompletedIssues);
		// out of the overallIssues, what all issues were completed
		allCompletedIssuesOutOfOverall.retainAll(overallIssues);
		groupedMap.put(RELEASE_SCOPE, overallIssues);
		groupedMap.put(RELEASE_PROGRESS, allCompletedIssuesOutOfOverall);
		groupedMap.put(OVERALL_COMPLETED, overallCompletedIssues.stream().distinct().collect(Collectors.toList()));
		groupedMap.put(OVERALL_ISSUE, overallIssues.stream().distinct().collect(Collectors.toList()));
		return groupedMap;
	}

	/**
	 * Method to Get Date on basis of range for x-axis representation
	 *
	 * @param dateRange
	 *            CustomDateRange
	 * @param range
	 *            range
	 * @return x-axis representation value
	 */
	private String getRange(CustomDateRange dateRange, String range) {
		String date = null;
		if (range.equalsIgnoreCase(CommonConstant.MONTH)) {
			date = DateUtil.tranformUTCLocalTimeToZFormat(dateRange.getStartDateTime());
		} else if (range.equalsIgnoreCase(CommonConstant.WEEK)) {
			LocalDateTime endDate = dateRange.getEndDateTime();
			while (!endDate.getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
				endDate = endDate.minusDays(1);
			}
			date = DateUtil.tranformUTCLocalTimeToZFormat(dateRange.getStartDateTime()) + " to "
					+ DateUtil.tranformUTCLocalTimeToZFormat(endDate);

		} else {
			date = DateUtil.tranformUTCLocalTimeToZFormat(dateRange.getStartDateTime());
		}
		return date;
	}

	/**
	 * Method to Populate Excel Data Object
	 *
	 * @param requestTrackerId
	 *            requestTrackerId
	 * @param excelData
	 *            excelData
	 * @param jiraIssueList
	 *            jiraIssueList
	 * @param issueReleaseTagMap
	 *            issueReleaseTagMap
	 * @param completedReleaseMap
	 *            completedReleaseMap
	 * @param devCompletedIssueMap
	 *            devCompletedIssueMap
	 * @param fieldMapping
	 *            fieldMapping
	 */
	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> jiraIssueList, Map<LocalDate, List<JiraIssueReferTime>> issueReleaseTagMap,
			Map<LocalDate, List<JiraIssueReferTime>> completedReleaseMap, Map<LocalDate, List<JiraIssueReferTime>> devCompletedIssueMap,
			FieldMapping fieldMapping) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& CollectionUtils.isNotEmpty(jiraIssueList)) {
			jiraIssueList.retainAll(allReleaseTaggedIssue);
			final Map<String, LocalDateTime> issueWiseReleaseTagDateMap = getJiraIssueWiseDateMap(issueReleaseTagMap);
			final Map<String, LocalDateTime> issueWiseCompleteDateMap = getJiraIssueWiseDateMap(completedReleaseMap);
			final Map<String, LocalDateTime> issueWiseDevCompleteDateMap = getJiraIssueWiseDateMap(devCompletedIssueMap);

			KPIExcelUtility.populateReleaseBurnUpExcelData(jiraIssueList, issueWiseReleaseTagDateMap,
					issueWiseCompleteDateMap, issueWiseDevCompleteDateMap, excelData, fieldMapping);
		}
	}

	/**
	 * Convert to JiraIssueWiseDateMap
	 *
	 * @param dateWiseJiraIssueMap
	 *            dateWiseJiraIssueMap
	 * @return Map<String, LocalDate>
	 */
	private Map<String, LocalDateTime> getJiraIssueWiseDateMap(Map<LocalDate, List<JiraIssueReferTime>> dateWiseJiraIssueMap) {
		return dateWiseJiraIssueMap.entrySet().stream()
				.flatMap(entry -> entry.getValue().stream()
						.map(issue -> new AbstractMap.SimpleEntry<>(issue.getJiraIssue().getNumber(), issue.getRefferTime())))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existing, replacement) -> existing));
	}

	/**
	 * Get JiraIssue for respective CustomHistory
	 *
	 * @param totalIssueList
	 *            List<JiraIssue>
	 * @param issueHistory
	 *            issueHistory
	 * @return List<JiraIssue>
	 */
	private List<JiraIssue> getRespectiveJiraIssue(List<JiraIssue> totalIssueList,
			JiraIssueCustomHistory issueHistory) {
		return totalIssueList.stream()
				.filter(jiraIssue -> jiraIssue.getNumber().equalsIgnoreCase(issueHistory.getStoryID()))
				.collect(Collectors.toList());
	}

	/**
	 * Get Next Date of population based on duration
	 *
	 * @param duration
	 *            duration
	 * @param currentDate
	 *            currDate
	 * @return LocalDate
	 */
	private LocalDateTime getNextRangeDate(String duration, LocalDateTime currentDate, LocalDateTime endLocalDate) {
		if (duration.equalsIgnoreCase(CommonConstant.MONTH)) {
			currentDate = currentDate.plusMonths(1);
		} else if (duration.equalsIgnoreCase(CommonConstant.WEEK)) {
			LocalDateTime monday = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
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
	 *
	 * @param fieldMapping
	 *            fieldMapping
	 * @param releaseSpecification
	 *            releaseSpecification
	 * @return averageDataMap
	 */
	public Map<String, Object> getAvgVelocity(FieldMapping fieldMapping, ReleaseSpecification releaseSpecification) {

		Map<String, Object> averageDataMap = new HashMap<>();
		String regex = "^(.*?)\\s*\\(duration\\s*(\\d+\\.\\d+)\\s*days\\)$";
		Pattern pattern = Pattern.compile(regex);
		List<String> releaseNames = new ArrayList<>();
		double totalDurations = 0.0;

		for (String release : fieldMapping.getReleaseListKPI150()) {
			Matcher matcher = pattern.matcher(release);
			if (matcher.find()) {
				String releaseName = matcher.group(1).trim();
				double duration = Double.parseDouble(matcher.group(2));
				totalDurations = duration + totalDurations;
				releaseNames.add(releaseName);
			}
		}

		List<JiraIssue> jiraIssues = jiraIssueRepository.findByBasicProjectConfigIdAndReleaseVersionsReleaseNameIn(
				fieldMapping.getBasicProjectConfigId().toString(), releaseNames);
		getAverageData(fieldMapping, releaseSpecification, averageDataMap, totalDurations, jiraIssues);

		return averageDataMap;
	}

	/**
	 * 
	 * @param fieldMapping
	 *            fieldMapping
	 * @param releaseSpecification
	 *            releaseSpecification
	 * @param averageDataMap
	 *            averageDataMap
	 * @param totalDurations
	 *            totalDurations
	 * @param jiraIssues
	 *            jiraIssues
	 */
	private void getAverageData(FieldMapping fieldMapping, ReleaseSpecification releaseSpecification,
			Map<String, Object> averageDataMap, double totalDurations, List<JiraIssue> jiraIssues) {
		if (totalDurations != 0 && CollectionUtils.isNotEmpty(jiraIssues)) {
			int releaseIssuesSize = jiraIssues.size();
			double releaseTicketsEstimate = getStoryPoint(jiraIssues, fieldMapping);
			double issueCountVelocity = releaseIssuesSize / totalDurations;
			double issuesEstimateVelocity = releaseTicketsEstimate / totalDurations;

			releaseSpecification.setReleaseIssueCount(releaseIssuesSize);
			releaseSpecification.setReleaseStoryPoint(releaseTicketsEstimate);
			releaseSpecification.setReleaseIssueCountVelocity(roundingOff(issueCountVelocity));
			releaseSpecification.setReleaseStoryPointVelocity(roundingOff(issuesEstimateVelocity));
			releaseSpecification.setReleaseDuration(totalDurations);
			averageDataMap.put(AVG_ISSUE_COUNT, roundingOff(issueCountVelocity));
			averageDataMap.put(AVG_STORY_POINT, roundingOff(issuesEstimateVelocity));

		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getQualifierType() {
		return KPICode.RELEASE_BURNUP.name();
	}

}
