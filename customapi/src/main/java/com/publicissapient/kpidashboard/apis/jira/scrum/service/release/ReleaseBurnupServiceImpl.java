package com.publicissapient.kpidashboard.apis.jira.scrum.service.release;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.CommonServiceImpl;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReleaseBurnupServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final String TOTAL_ISSUES = "totalIssues";
	private static final String ADDED_TO_RELEASE = "addedToRelease";
	private static final String FULL_RELEASE = "fullRelease";
	private static final String REMOVED_FROM_RELEASE = "removedFromRelease";
	private static final String ISSUE_COUNT = "Issue Count";
	private static final String STORY_POINT = "Story Point";
	private static final String SCOPE_REMOVED = "Scope Removed";
	private static final String SCOPE_ADDED = "Scope Added";
	private static final String RELEASE_SCOPE = "Release Scope";
	private static final int DAYS_RANGE = 15;
	private static final String RELEASE_PROGRESS = "Release Progress";
	private static final String LINE_GRAPH_TYPE = "line";
	private static final String BAR_GRAPH_TYPE = "bar";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CommonServiceImpl commonService;

	private LocalDate tempStartDate = null;

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			log.info("Release BurnUp -> Requested sprint : {}", leafNode.getName());

			List<String> releaseList = getReleaseList();
			if (CollectionUtils.isNotEmpty(releaseList)) {
				List<JiraIssueCustomHistory> allIssuesHistory = getJiraIssuesCustomHistoryFromBaseClass();
				List<JiraIssue> releaseIssues = jiraIssueRepository.findByNumberInAndBasicProjectConfigId(
						allIssuesHistory.stream().map(JiraIssueCustomHistory::getStoryID).collect(Collectors.toList()),
						leafNode.getProjectFilter().getBasicProjectConfigId().toString());

				Map<LocalDate, List<JiraIssue>> addedIssuesMap = new HashMap<>();
				Map<LocalDate, List<JiraIssue>> removeIssueMap = new HashMap<>();
				Map<LocalDate, List<JiraIssue>> fullReleaseMap = new HashMap<>();
				Map<LocalDate, List<JiraIssue>> completedReleaseMap = new HashMap<>();
				dateWiseLogs(allIssuesHistory, releaseList.stream().findFirst().orElse(null), releaseIssues,
						addedIssuesMap, removeIssueMap, fullReleaseMap, completedReleaseMap);
				resultListMap.put(FULL_RELEASE, fullReleaseMap);
				resultListMap.put(ADDED_TO_RELEASE, addedIssuesMap);
				resultListMap.put(REMOVED_FROM_RELEASE, removeIssueMap);
				resultListMap.put(TOTAL_ISSUES, releaseIssues);
				resultListMap.put(RELEASE_PROGRESS, completedReleaseMap);
			}

		}
		return resultListMap;
	}

	private void dateWiseLogs(List<JiraIssueCustomHistory> allIssuesHistory, String releaseName,
			List<JiraIssue> releaseIssue, Map<LocalDate, List<JiraIssue>> addedIssuesMap,
			Map<LocalDate, List<JiraIssue>> removeIssueMap, Map<LocalDate, List<JiraIssue>> fullReleaseMap,
			Map<LocalDate, List<JiraIssue>> completedReleaseMap) {

		releaseName = releaseName != null ? releaseName : "";
		String finalReleaseName = releaseName.toLowerCase();
		allIssuesHistory.forEach(issueHistory -> {
			List<JiraHistoryChangeLog> fixVersionUpdationLog = issueHistory.getFixVersionUpdationLog();
			Collections.sort(fixVersionUpdationLog, Comparator.comparing(JiraHistoryChangeLog::getUpdatedOn));
			int lastIndex = fixVersionUpdationLog.size() - 1;
			fixVersionUpdationLog.stream()
					.filter(updateLogs -> updateLogs.getChangedTo().toLowerCase().contains(finalReleaseName)
							|| updateLogs.getChangedFrom().toLowerCase().contains(finalReleaseName))
					.forEach(updateLogs -> {
						List<JiraIssue> jiraIssueList = getRespectiveJiraIssue(releaseIssue, issueHistory);
						LocalDate updatedLog;
						if (updateLogs.getChangedTo().toLowerCase().contains(finalReleaseName)) {
							if (fixVersionUpdationLog.get(lastIndex).getChangedTo().toLowerCase()
									.contains(finalReleaseName)) {
								updatedLog = fixVersionUpdationLog.get(lastIndex).getUpdatedOn().toLocalDate();
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
			createCompletedIssuesDateWiseMap(issueHistory, completedReleaseMap, releaseIssue);

		});
	}

	private void createCompletedIssuesDateWiseMap(JiraIssueCustomHistory issueHistory,
			Map<LocalDate, List<JiraIssue>> completedIssues, List<JiraIssue> totalIssueList) {
		List<JiraHistoryChangeLog> statusUpdationLog = issueHistory.getStatusUpdationLog();
		JiraIssueReleaseStatus jiraIssueReleaseStatus = getJiraIssueReleaseStatus();
		statusUpdationLog = statusUpdationLog.stream()
				.filter(log -> jiraIssueReleaseStatus.getClosedList().containsValue(log.getChangedTo())
						|| jiraIssueReleaseStatus.getClosedList().containsValue(log.getChangedFrom()))
				.collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(statusUpdationLog)) {
			Map<String, LocalDate> closedStatusDateMap = new HashMap<>();
			for (JiraHistoryChangeLog jiraHistoryChangeLog : statusUpdationLog) {
				LocalDate activityDate = LocalDate.parse(jiraHistoryChangeLog.getUpdatedOn().toString().split("T")[0],
						DATE_TIME_FORMATTER);
				if (jiraIssueReleaseStatus.getClosedList().containsValue(jiraHistoryChangeLog.getChangedTo())) {
					if (closedStatusDateMap.containsKey(jiraHistoryChangeLog.getChangedTo())) {
						closedStatusDateMap.clear();
					}
					closedStatusDateMap.put(jiraHistoryChangeLog.getChangedTo(), activityDate);
				}
			}
			// Getting the min date of closed status.
			LocalDate updatedLog = closedStatusDateMap.values().stream().filter(Objects::nonNull)
					.min(LocalDate::compareTo).orElse(null);
			List<JiraIssue> jiraIssueList = new ArrayList<>(getRespectiveJiraIssue(totalIssueList, issueHistory));
			LocalDate finalUpdatedLog = updatedLog;
			jiraIssueList.forEach(issue -> issue.setUpdateDate(ObjectUtils.isEmpty(finalUpdatedLog)
					? LocalDate.parse(issue.getUpdateDate().split("T")[0], DATE_TIME_FORMATTER).toString()
					: finalUpdatedLog.toString()));
			completedIssues.computeIfPresent(updatedLog, (k, v) -> {
				v.addAll(jiraIssueList);
				return v;
			});
			completedIssues.putIfAbsent(updatedLog, jiraIssueList);
		}
	}

	@Override
	public String getQualifierType() {
		return KPICode.RELEASE_BURNUP.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.RELEASE) {
				releaseWiseLeafNodeValue(v, kpiElement, kpiRequest);
			}
		});
		log.info("ReleaseProgressServiceImpl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	private void releaseWiseLeafNodeValue(List<Node> releaseLeafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		List<Node> latestReleaseNode = new ArrayList<>();
		Node latestRelease = releaseLeafNodeList.get(0);

		if (latestRelease != null) {
			String startDate = latestRelease.getReleaseFilter().getStartDate();
			String endDate = latestRelease.getReleaseFilter().getEndDate();

			Optional.ofNullable(latestRelease).ifPresent(latestReleaseNode::add);

			Map<String, Object> resultMap = fetchKPIDataFromDb(latestReleaseNode, null, null, kpiRequest);
			List<JiraIssue> releaseIssues = (List<JiraIssue>) resultMap.get(TOTAL_ISSUES);
			Map<LocalDate, List<JiraIssue>> completedReleaseMap = (Map<LocalDate, List<JiraIssue>>) resultMap
					.get(RELEASE_PROGRESS);
			Map<LocalDate, List<JiraIssue>> addedIssuesMap = (Map<LocalDate, List<JiraIssue>>) resultMap
					.get(ADDED_TO_RELEASE);
			Map<LocalDate, List<JiraIssue>> fullReleaseIssueMap = (Map<LocalDate, List<JiraIssue>>) resultMap
					.get(FULL_RELEASE);

			Map<LocalDate, List<JiraIssue>> removeIssueMap = (Map<LocalDate, List<JiraIssue>>) resultMap
					.get(REMOVED_FROM_RELEASE);

			List<IterationKpiValue> iterationKpiValueList = new ArrayList<>();
			long range = 0;
			String duration;
			if (CollectionUtils.isNotEmpty(releaseIssues) && MapUtils.isNotEmpty(fullReleaseIssueMap)) {
				Object basicProjectConfigId = latestRelease.getProjectFilter().getBasicProjectConfigId();
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
				/*
				 * if starttime is absent, then the date at which issue was added and remained
				 * added in the entire relase is considered to be the start date if end date is
				 * absent then it means that issue is unreleased, so till today we can consider
				 * as end date
				 */
				LocalDate startLocalDate = StringUtils.isEmpty(startDate)
						? fullReleaseIssueMap.keySet().stream().filter(Objects::nonNull).min(LocalDate::compareTo)
								.orElse(null)
						: LocalDate.parse(startDate.split("T")[0], DATE_TIME_FORMATTER);
				LocalDate endLocalDate = StringUtils.isEmpty(endDate) ? LocalDate.now()
						: LocalDate.parse(endDate.split("T")[0], DATE_TIME_FORMATTER);

				Map<String, Long> durationRangeMap = getDurationRangeMap(startLocalDate, endLocalDate);
				duration = durationRangeMap.keySet().stream().findFirst().orElse("");
				range = durationRangeMap.values().stream().findFirst().orElse(0L);
				completedReleaseMap = prepareIssueBeforeStartDate(completedReleaseMap, startLocalDate);
				fullReleaseIssueMap = prepareIssueBeforeStartDate(fullReleaseIssueMap, startLocalDate);

				tempStartDate = LocalDate.parse(startLocalDate.toString());
				List<JiraIssue> overallIssues = new ArrayList<>();
				List<JiraIssue> overallCompletedIssues = new ArrayList<>();
				List<DataCountGroup> issueCountDataGroup = new ArrayList<>();
				List<DataCountGroup> issueSizeCountDataGroup = new ArrayList<>();
				for (int i = 0; i < range && !startLocalDate.isAfter(endLocalDate); i++) {
					DataCountGroup issueCount = new DataCountGroup();
					DataCountGroup issueSize = new DataCountGroup();
					CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(startLocalDate,
							duration);
					Map<String, List<JiraIssue>> filterWiseGroupedMap = createFilterWiseGroupedMap(dateRange,
							addedIssuesMap, removeIssueMap, fullReleaseIssueMap, overallIssues, completedReleaseMap,
							overallCompletedIssues);
					overallCompletedIssues = filterWiseGroupedMap.getOrDefault("OVERALL COMPLETED", new ArrayList<>());
					overallIssues = filterWiseGroupedMap.getOrDefault("OVERALL ISSUE", new ArrayList<>());
					String date = getRange(dateRange, duration);
					populateFilterWiseDataMap(filterWiseGroupedMap, issueCount, issueSize, date, duration,
							fieldMapping);
					startLocalDate = getNextRangeDate(duration, startLocalDate, endLocalDate);
					issueCountDataGroup.add(issueCount);
					issueSizeCountDataGroup.add(issueSize);
				}
				populateExcelDataObject(requestTrackerId, excelData, releaseIssues, fieldMapping);
				createExcelDataAndTrendValueList(kpiElement, excelData, iterationKpiValueList, issueCountDataGroup,
						issueSizeCountDataGroup);

			}
			kpiElement.setTrendValueList(iterationKpiValueList);
		}
	}

	private void createExcelDataAndTrendValueList(KpiElement kpiElement, List<KPIExcelData> excelData,
			List<IterationKpiValue> iterationKpiValueList, List<DataCountGroup> issueCountDataGroup,
			List<DataCountGroup> issueSizeCountDataGroup) {
		if (CollectionUtils.isNotEmpty(issueCountDataGroup)) {
			IterationKpiValue kpiValueIssueCount = new IterationKpiValue();
			kpiValueIssueCount.setDataGroup(issueCountDataGroup);
			kpiValueIssueCount.setFilter1(ISSUE_COUNT);
			IterationKpiValue kpiValueSizeCount = new IterationKpiValue();
			kpiValueSizeCount.setDataGroup(issueSizeCountDataGroup);
			kpiValueSizeCount.setFilter1(STORY_POINT);
			iterationKpiValueList.add(kpiValueSizeCount);
			iterationKpiValueList.add(kpiValueIssueCount);

			kpiElement.setModalHeads(KPIExcelColumn.RELEASE_BURNUP.getColumns());
			kpiElement.setExcelColumns(KPIExcelColumn.RELEASE_BURNUP.getColumns());
			kpiElement.setExcelData(excelData);
		}
	}

	/**
	 * issue completed/added before the release version but happened to be present in
	 * selected release then issues to be present on the first day of release start
	 * date
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

	private Map<String, Long> getDurationRangeMap(LocalDate startLocalDate, LocalDate endLocalDate) {
		Map<String, Long> map = new HashMap<>();
		long range;
		String duration;
		// added+1 to add the end date as well
		if (ChronoUnit.DAYS.between(Objects.requireNonNull(startLocalDate), endLocalDate) + 1 > 15) {
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

	private void populateFilterWiseDataMap(Map<String, List<JiraIssue>> filterWiseGroupedMap, DataCountGroup issueCount,
			DataCountGroup issueSize, String date, String duration, FieldMapping fieldMapping) {
		List<DataCount> issueCountDataList = new ArrayList<>();
		List<DataCount> issueSizeDataList = new ArrayList<>();

		List<JiraIssue> overallIssues = filterWiseGroupedMap.getOrDefault(RELEASE_SCOPE, new ArrayList<>());
		List<JiraIssue> issuesAdded = filterWiseGroupedMap.getOrDefault(SCOPE_ADDED, new ArrayList<>());
		List<JiraIssue> issuesRemoved = filterWiseGroupedMap.getOrDefault(SCOPE_REMOVED, new ArrayList<>());
		List<JiraIssue> completedIssues = filterWiseGroupedMap.getOrDefault(RELEASE_PROGRESS, new ArrayList<>());
		// do not change the order, as it will impact the UI coloration
		createDataCount((long) issuesAdded.size(), BAR_GRAPH_TYPE, SCOPE_ADDED, issueCountDataList, issuesAdded);
		createDataCount((long) issuesRemoved.size(), BAR_GRAPH_TYPE, SCOPE_REMOVED, issueCountDataList, issuesRemoved);
		createDataCount((long) overallIssues.size(), LINE_GRAPH_TYPE, RELEASE_SCOPE, issueCountDataList, overallIssues);
		createDataCount((long) completedIssues.size(), LINE_GRAPH_TYPE, RELEASE_PROGRESS, issueCountDataList,
				completedIssues);
		issueCount.setFilter(date);
		issueCount.setDuration(duration);
		issueCount.setValue(issueCountDataList);

		createDataCount(getStoryPoint(issuesAdded, fieldMapping), BAR_GRAPH_TYPE, SCOPE_ADDED, issueSizeDataList,
				issuesAdded);
		createDataCount(getStoryPoint(issuesRemoved, fieldMapping), BAR_GRAPH_TYPE, SCOPE_REMOVED, issueSizeDataList,
				issuesRemoved);
		createDataCount(getStoryPoint(overallIssues, fieldMapping), LINE_GRAPH_TYPE, RELEASE_SCOPE, issueSizeDataList,
				overallIssues);
		createDataCount(getStoryPoint(completedIssues, fieldMapping), LINE_GRAPH_TYPE, RELEASE_PROGRESS,
				issueSizeDataList, completedIssues);
		issueSize.setFilter(date);
		issueSize.setDuration(duration);
		issueSize.setValue(issueSizeDataList);
	}

	private Map<String, Object> createHoverMap(List<JiraIssue> issuesAdded) {
		Map<String, List<JiraIssue>> projectWiseJiraIssues = issuesAdded.stream()
				.collect(Collectors.groupingBy(JiraIssue::getTypeName));
		Map<String, Object> hoverMap = new HashMap<>();
		projectWiseJiraIssues.forEach((k, v) -> hoverMap.put(k, (long) v.size()));
		return hoverMap;
	}

	Double getStoryPoint(List<JiraIssue> jiraIssueList, FieldMapping fieldMapping) {
		Double ticketEstimate = 0.0d;
		if (CollectionUtils.isNotEmpty(jiraIssueList)) {
			if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
					&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
				ticketEstimate = jiraIssueList.stream()
						.mapToDouble(ji -> Optional.ofNullable(ji.getStoryPoints()).orElse(0.0d)).sum();
			} else {
				double totalOriginalEstimate = jiraIssueList.stream()
						.mapToDouble(jiraIssue -> Optional.ofNullable(jiraIssue.getOriginalEstimateMinutes()).orElse(0))
						.sum();
				double inHours = totalOriginalEstimate / 60;
				ticketEstimate = inHours / fieldMapping.getStoryPointToHourMapping();
			}
		}
		return roundingOff(ticketEstimate);

	}

	private DataCount createDataCount(Object value, String graphType, String label, List<DataCount> issueCountDataList,
			List<JiraIssue> orDefault) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setKpiGroup(label);
		dataCount.setValue(value);
		dataCount.setGraphType(graphType);
		if (graphType.equalsIgnoreCase(BAR_GRAPH_TYPE)) {
			dataCount.setHoverValue(createHoverMap(orDefault));
		}
		issueCountDataList.add(dataCount);
		return dataCount;
	}

	private Map<String, List<JiraIssue>> createFilterWiseGroupedMap(CustomDateRange dateRange,
			Map<LocalDate, List<JiraIssue>> addedIssuesMap, Map<LocalDate, List<JiraIssue>> removeIssueMap,
			Map<LocalDate, List<JiraIssue>> fullReleaseIssueMap, List<JiraIssue> overallIssues,
			Map<LocalDate, List<JiraIssue>> completedReleaseMap, List<JiraIssue> overallCompletedIssues) {
		Map<String, List<JiraIssue>> groupedMap = new HashMap<>();
		List<JiraIssue> defaultIssues = new ArrayList<>();
		List<JiraIssue> allAddedIssues = new ArrayList<>();
		List<JiraIssue> removedIssues = new ArrayList<>();
		List<JiraIssue> completedIssues = new ArrayList<>();

		for (LocalDate currentDate = dateRange.getStartDate(); currentDate.compareTo(dateRange.getStartDate()) >= 0
				&& dateRange.getEndDate().compareTo(currentDate) >= 0; currentDate = currentDate.plusDays(1)) {
			if (currentDate.isEqual(tempStartDate)) {
				defaultIssues.addAll(fullReleaseIssueMap.getOrDefault(currentDate, new ArrayList<>()));// defaultMap
				defaultIssues.addAll(addedIssuesMap.getOrDefault(currentDate, new ArrayList<>()));
				defaultIssues.removeAll(removeIssueMap.getOrDefault(currentDate, new ArrayList<>()));
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
		groupedMap.put("OVERALL COMPLETED", overallCompletedIssues.stream().distinct().collect(Collectors.toList()));
		groupedMap.put("OVERALL ISSUE", overallIssues.stream().distinct().collect(Collectors.toList()));

		return groupedMap;
	}

	private String getRange(CustomDateRange dateRange, String range) {
		String date = null;
		if (range.equalsIgnoreCase(CommonConstant.WEEK)) {
			LocalDate endDate = dateRange.getEndDate();
			while (!endDate.getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
				endDate = endDate.minusDays(1);
			}
			date = DateUtil.dateTimeConverter(dateRange.getStartDate().toString(), DateUtil.DATE_FORMAT,
					DateUtil.DISPLAY_DATE_FORMAT) + " to "
					+ DateUtil.dateTimeConverter(endDate.toString(), DateUtil.DATE_FORMAT,
							DateUtil.DISPLAY_DATE_FORMAT);
		} else {
			date = dateRange.getStartDate().toString();
		}
		return date;
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> jiraIssueList, FieldMapping fieldMapping) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& CollectionUtils.isNotEmpty(jiraIssueList)) {
			KPIExcelUtility.populateReleaseDefectRelatedExcelData(jiraIssueList, excelData, fieldMapping);
		}
	}

	private List<JiraIssue> getRespectiveJiraIssue(List<JiraIssue> totalIssueList,
			JiraIssueCustomHistory issueHistory) {
		return totalIssueList.stream()
				.filter(jiraIssue -> jiraIssue.getNumber().equalsIgnoreCase(issueHistory.getStoryID()))
				.collect(Collectors.toList());
	}

	private LocalDate getNextRangeDate(String duration, LocalDate currentDate, LocalDate endLocalDate) {
		if (duration.equalsIgnoreCase(CommonConstant.WEEK)) {
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

}
