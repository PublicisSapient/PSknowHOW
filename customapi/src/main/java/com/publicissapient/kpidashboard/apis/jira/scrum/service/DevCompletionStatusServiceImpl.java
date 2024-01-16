package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
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
import com.publicissapient.kpidashboard.apis.util.IterationKpiHelper;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DevCompletionStatusServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	public static final String UNCHECKED = "unchecked";
	public static final String ISSUE_CUSTOM_HISTORY = "issues custom history";
	public static final String ACTUAL_COMPLETE_DATE = "actualCompleteDate";
	public static final String COMPLETED = "Completed";
	public static final String PLANNED_COMPLETION = "Planned Completion";
	public static final String DEV_COMPLETION = "Dev Completion";
	public static final String ACTUAL_START_DATE = "actualStartDate";
	public static final String ACTUAL_COMPLETION_DATA = "actualCompletionData";
	public static final String ISSUE_DELAY = "issueDelay";
	public static final String DEV_COMPLETION_DATE = "devCompletionDate";
	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String SEARCH_BY_PRIORITY = "Filter by priority";
	private static final String ISSUES = "issues";
	private static final String DELAY = "Delay";
	private static final String OVERALL = "Overall";
	private static final String SPRINT_DETAILS = "sprintDetails";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	@Autowired
	private ConfigHelperService configHelperService;

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
	public String getQualifierType() {
		return KPICode.DEV_COMPLETED_STATUS.name();
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
			log.info("Dev Completed Status -> Requested sprint : {}", leafNode.getName());
			SprintDetails dbSprintDetail = getSprintDetailsFromBaseClass();
			SprintDetails sprintDetails;
			if (null != dbSprintDetail) {
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
						.get(leafNode.getProjectFilter().getBasicProjectConfigId());
				// to modify sprintdetails on the basis of configuration for the project
				List<JiraIssueCustomHistory> totalHistoryList = getJiraIssuesCustomHistoryFromBaseClass();
				List<JiraIssue> totalJiraIssueList = getJiraIssuesFromBaseClass();
				Set<String> issueList = totalJiraIssueList.stream().map(JiraIssue::getNumber)
						.collect(Collectors.toSet());

				sprintDetails = IterationKpiHelper.transformIterSprintdetail(totalHistoryList, issueList,
						dbSprintDetail, fieldMapping.getJiraIterationIssuetypeKPI145(),
						fieldMapping.getJiraIterationCompletionStatusKPI145(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> totalIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.TOTAL_ISSUES);
				List<String> completedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.COMPLETED_ISSUES);
				if (CollectionUtils.isNotEmpty(totalIssues)) {
					List<JiraIssue> filteredJiraIssue = IterationKpiHelper.getFilteredJiraIssue(totalIssues,
							totalJiraIssueList);
					List<JiraIssueCustomHistory> issueHistoryList = IterationKpiHelper
							.getFilteredJiraIssueHistory(totalIssues, totalHistoryList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getTotalIssues(), filteredJiraIssue);
					resultListMap.put(ISSUES, new ArrayList<>(filtersIssuesList));
					resultListMap.put(ISSUE_CUSTOM_HISTORY, new ArrayList<>(issueHistoryList));
					resultListMap.put(SPRINT_DETAILS, sprintDetails);
				}
				resultListMap.put(COMPLETED, new ArrayList<>(completedIssues));
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
	@SuppressWarnings("java:S3776")
	private void projectWiseLeafNodeValue(List<Node> sprintLeafNodeList, DataCount trendValue, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);
		Object basicProjectConfigId = Objects.requireNonNull(latestSprint).getProjectFilter().getBasicProjectConfigId();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);

		SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT_DETAILS);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		// Filtering out the issues with dev due date.
		List<JiraIssue> allIssuesWithDevDueDate = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(allIssues)) {
			allIssuesWithDevDueDate = allIssues.stream()
					.filter(jiraIssue -> StringUtils.isNotBlank(jiraIssue.getDevDueDate()))
					.collect(Collectors.toList());
		}

		List<JiraIssueCustomHistory> allIssueHistories = (List<JiraIssueCustomHistory>) resultMap
				.get(ISSUE_CUSTOM_HISTORY);
		if (CollectionUtils.isNotEmpty(allIssuesWithDevDueDate)) {
			log.info("Dev Completed Status -> request id : {} total jira Issues : {}", requestTrackerId,
					allIssuesWithDevDueDate.size());

			Map<JiraIssue, String> completedIssueMap = createComplteIssuesWithCompletionDate(allIssueHistories,
					allIssuesWithDevDueDate, fieldMapping);
			Map<JiraIssue, String> devCompletdIssues = completedIssueMap.entrySet().stream()
					.filter(entry -> !entry.getValue().equalsIgnoreCase(Constant.DASH))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			// Creating map of modal Objects
			Map<String, IterationKpiModalValue> modalObjectMap = KpiDataHelper
					.createMapOfModalObject(allIssuesWithDevDueDate);

			Map<String, Map<String, List<JiraIssue>>> typeAndPriorityWiseIssues = allIssuesWithDevDueDate.stream()
					.collect(Collectors.groupingBy(JiraIssue::getTypeName,
							Collectors.groupingBy(JiraIssue::getPriority)));
			Set<String> issueTypes = new HashSet<>();
			Set<String> priorities = new HashSet<>();
			List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
			List<Integer> overAllIssueCountActual = Arrays.asList(0);
			List<Double> overAllStoryPointsActual = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimateActual = Arrays.asList(0.0);
			List<Integer> overAllIssueCountPlanned = Arrays.asList(0);
			List<Double> overAllStoryPointsPlanned = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimatePlanned = Arrays.asList(0.0);
			List<Integer> overallDelay = Arrays.asList(0);
			List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
			// For markerInfo

			Map<String, String> markerInfo = new HashMap<>();
			markerInfo.put(Constant.GREEN, "Issue finished earlier than planned are marked in Green");
			typeAndPriorityWiseIssues
					.forEach((issueType, priorityWiseIssue) -> priorityWiseIssue.forEach((priority, issues) -> {
						issueTypes.add(issueType);
						priorities.add(priority);
						List<IterationKpiModalValue> modalValues = new ArrayList<>();
						int issueCountActual = 0;
						Double storyPointActual = 0.0;
						Double originalEstimateActual = 0.0;
						int issueCountPlanned = 0;
						Double storyPointPlanned = 0.0;
						Double originalEstimatePlanned = 0.0;
						int delay = 0;
						for (JiraIssue jiraIssue : issues) {
							Map<String, Object> jiraIssueData = jiraIssueCalculation(fieldMapping, sprintDetails,
									allIssueHistories, jiraIssue, completedIssueMap);
							Map<String, Object> actualCompletionData = (Map<String, Object>) jiraIssueData
									.get(ACTUAL_COMPLETION_DATA);
							if (SprintDetails.SPRINT_STATE_ACTIVE.equalsIgnoreCase(sprintDetails.getState())) {
								// Checking if devdueDate is < today date for active sprint
								if (DateUtil.stringToLocalDate(jiraIssue.getDevDueDate(), DateUtil.TIME_FORMAT_WITH_SEC)
										.isBefore(LocalDate.now())) {
									issueCountPlanned = issueCountPlanned + 1;
									overAllIssueCountPlanned.set(0, overAllIssueCountPlanned.get(0) + 1);

									storyPointPlanned = KpiDataHelper.getStoryPoint(overAllStoryPointsPlanned,
											storyPointPlanned, jiraIssue);
									originalEstimatePlanned = KpiDataHelper.getOriginalEstimate(
											overAllOriginalEstimatePlanned, originalEstimatePlanned, jiraIssue);
									if (!jiraIssueData.get(ISSUE_DELAY).equals(Constant.DASH)) {
										int jiraIssueDelay = (int) jiraIssueData.get(ISSUE_DELAY);
										delay += KpiDataHelper.getDelayInMinutes(jiraIssueDelay);
										overallDelay.set(0,
												overallDelay.get(0) + KpiDataHelper.getDelayInMinutes(jiraIssueDelay));
									}
									KPIExcelUtility.populateIterationKPI(overAllmodalValues, modalValues, jiraIssue,
											fieldMapping, modalObjectMap);
									setKpiSpecificData(modalObjectMap, jiraIssue, jiraIssueData, actualCompletionData);

									// Calculating actual work status for only completed issues
									if (devCompletdIssues.containsKey(jiraIssue)) {
										issueCountActual = issueCountActual + 1;
										overAllIssueCountActual.set(0, overAllIssueCountActual.get(0) + 1);

										storyPointActual = KpiDataHelper.getStoryPoint(overAllStoryPointsActual,
												storyPointActual, jiraIssue);
										originalEstimateActual = KpiDataHelper.getOriginalEstimate(
												overAllOriginalEstimateActual, originalEstimateActual, jiraIssue);

										delay = getDelay(fieldMapping, modalObjectMap, overallDelay, overAllmodalValues,
												modalValues, delay, jiraIssue, jiraIssueData, actualCompletionData);
									}
								}
							} else {
								// Checking if devdueDate is <= sprint End Date for closed sprint
								if (DateUtil.stringToLocalDate(jiraIssue.getDevDueDate(), DateUtil.TIME_FORMAT_WITH_SEC)
										.isBefore(DateUtil.stringToLocalDate(sprintDetails.getEndDate(),
												DateUtil.TIME_FORMAT_WITH_SEC).plusDays(1))) {
									issueCountPlanned = issueCountPlanned + 1;
									overAllIssueCountPlanned.set(0, overAllIssueCountPlanned.get(0) + 1);

									storyPointPlanned = KpiDataHelper.getStoryPoint(overAllStoryPointsPlanned,
											storyPointPlanned, jiraIssue);
									originalEstimatePlanned = KpiDataHelper.getOriginalEstimate(
											overAllOriginalEstimatePlanned, originalEstimatePlanned, jiraIssue);
									if (!jiraIssueData.get(ISSUE_DELAY).equals(Constant.DASH)) {
										int jiraIssueDelay = (int) jiraIssueData.get(ISSUE_DELAY);
										delay += KpiDataHelper.getDelayInMinutes(jiraIssueDelay);
										overallDelay.set(0,
												overallDelay.get(0) + KpiDataHelper.getDelayInMinutes(jiraIssueDelay));
									}
									KPIExcelUtility.populateIterationKPI(overAllmodalValues, modalValues, jiraIssue,
											fieldMapping, modalObjectMap);
									setKpiSpecificData(modalObjectMap, jiraIssue, jiraIssueData, actualCompletionData);

									// Calculating actual work status for only completed issues
									if (devCompletdIssues.containsKey(jiraIssue)) {
										issueCountActual = issueCountActual + 1;
										overAllIssueCountActual.set(0, overAllIssueCountActual.get(0) + 1);

										storyPointActual = KpiDataHelper.getStoryPoint(overAllStoryPointsActual,
												storyPointActual, jiraIssue);
										originalEstimateActual = KpiDataHelper.getOriginalEstimate(
												overAllOriginalEstimateActual, originalEstimateActual, jiraIssue);

										delay = getDelay(fieldMapping, modalObjectMap, overallDelay, overAllmodalValues,
												modalValues, delay, jiraIssue, jiraIssueData, actualCompletionData);
									}
								}
							}
						}
						List<IterationKpiData> data = new ArrayList<>();
						IterationKpiData issueCountsPlanned;
						IterationKpiData issueCountsActual;
						IterationKpiData delayed;
						issueCountsPlanned = createIterationKpiData(PLANNED_COMPLETION, fieldMapping, issueCountPlanned,
								storyPointPlanned, originalEstimatePlanned, modalValues);
						issueCountsActual = createIterationKpiData(DEV_COMPLETION, fieldMapping, issueCountActual,
								storyPointActual, originalEstimateActual, null);
						delayed = new IterationKpiData(DELAY, (double) (delay), null, null, CommonConstant.DAY, null);
						data.add(issueCountsPlanned);
						data.add(issueCountsActual);
						data.add(delayed);
						IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, priority, data,
								Arrays.asList("marker"), markerInfo);
						iterationKpiValues.add(iterationKpiValue);
					}));
			List<IterationKpiData> data = new ArrayList<>();
			IterationKpiData overAllIssueCountsPlanned;
			IterationKpiData overAllIssueCountsActual;
			IterationKpiData overAllDelay;
			overAllIssueCountsPlanned = createIterationKpiData(PLANNED_COMPLETION, fieldMapping,
					overAllIssueCountPlanned.get(0), overAllStoryPointsPlanned.get(0),
					overAllOriginalEstimatePlanned.get(0), overAllmodalValues);
			overAllIssueCountsActual = createIterationKpiData(DEV_COMPLETION, fieldMapping,
					overAllIssueCountActual.get(0), overAllStoryPointsActual.get(0),
					overAllOriginalEstimateActual.get(0), null);
			overAllDelay = new IterationKpiData(DELAY, (double) (overallDelay.get(0)), null, null, CommonConstant.DAY,
					null);
			data.add(overAllIssueCountsPlanned);
			data.add(overAllIssueCountsActual);
			data.add(overAllDelay);
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data,
					Arrays.asList("marker"), markerInfo);
			iterationKpiValues.add(overAllIterationKpiValue);

			// Create kpi level filters
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypes);
			IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, priorities);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
			trendValue.setValue(iterationKpiValues);
			kpiElement.setFilters(iterationKpiFilters);
			kpiElement.setSprint(latestSprint.getName());
			kpiElement.setExcelColumnInfo(KPIExcelColumn.DEV_COMPLETION_STATUS.getKpiExcelColumnInfo());
			kpiElement.setTrendValueList(trendValue);
		}
	}

	@SuppressWarnings("java:S107")
	private int getDelay(FieldMapping fieldMapping, Map<String, IterationKpiModalValue> modalObjectMap,
			List<Integer> overallDelay, List<IterationKpiModalValue> overAllmodalValues,
			List<IterationKpiModalValue> modalValues, int delay, JiraIssue jiraIssue, Map<String, Object> jiraIssueData,
			Map<String, Object> actualCompletionData) {
		if (DateUtil.stringToLocalDate(jiraIssue.getDevDueDate(), DateUtil.TIME_FORMAT_WITH_SEC)
				.isAfter(LocalDate.now().minusDays(1))) {
			if (!jiraIssueData.get(ISSUE_DELAY).equals(Constant.DASH)) {
				int jiraIssueDelay = (int) jiraIssueData.get(ISSUE_DELAY);
				delay += KpiDataHelper.getDelayInMinutes(jiraIssueDelay);
				overallDelay.set(0, overallDelay.get(0) + KpiDataHelper.getDelayInMinutes(jiraIssueDelay));
			}
			KPIExcelUtility.populateIterationKPI(overAllmodalValues, modalValues, jiraIssue, fieldMapping,
					modalObjectMap);
			setKpiSpecificData(modalObjectMap, jiraIssue, jiraIssueData, actualCompletionData);
		}
		return delay;
	}

	private Map<JiraIssue, String> createComplteIssuesWithCompletionDate(List<JiraIssueCustomHistory> allIssueHistories,
			List<JiraIssue> allIssuesWithDevDueDate, FieldMapping fieldMapping) {
		Map<JiraIssue, String> compltedIssues = new HashMap<>();
		if (CollectionUtils.isNotEmpty(allIssueHistories)) {
			allIssuesWithDevDueDate.forEach(jiraIssue -> {
				JiraIssueCustomHistory issueCustomHistory = allIssueHistories.stream().filter(
						jiraIssueCustomHistory -> jiraIssueCustomHistory.getStoryID().equals(jiraIssue.getNumber()))
						.findFirst().orElse(new JiraIssueCustomHistory());
				String devCompletionDate = getDevCompletionDate(issueCustomHistory,
						fieldMapping.getJiraDevDoneStatusKPI145());
				compltedIssues.putIfAbsent(jiraIssue, devCompletionDate);
			});
		}
		return compltedIssues;

	}

	/**
	 * Method to calculate story start and completed date
	 *
	 * @param issueCustomHistory
	 * @param sprintDetail
	 * @param fieldMapping
	 * @return
	 */
	private Map<String, Object> calStartAndEndDate(JiraIssueCustomHistory issueCustomHistory,
			SprintDetails sprintDetail, FieldMapping fieldMapping) {
		List<String> inProgressStatuses = new ArrayList<>();
		List<JiraHistoryChangeLog> filterStorySprintDetails = new ArrayList<>();

		LocalDate sprintStartDate = LocalDate.parse(sprintDetail.getStartDate().split("T")[0], DATE_TIME_FORMATTER);
		LocalDate sprintEndDate = LocalDate.parse(sprintDetail.getEndDate().split("T")[0], DATE_TIME_FORMATTER);
		Map<String, Object> resultList = new HashMap<>();

		// filtering storySprintDetails lies in between sprintStart and sprintEnd
		filterStorySprintDetails = getFilterStorySprintDetails(issueCustomHistory, filterStorySprintDetails,
				sprintStartDate, sprintEndDate);

		// Creating the set of completed status
		Set<String> closedStatus = sprintDetail.getCompletedIssues().stream().map(SprintIssue::getStatus)
				.collect(Collectors.toSet());

		// sorting the story history on basis of activityDate
		filterStorySprintDetails.sort(Comparator.comparing(JiraHistoryChangeLog::getUpdatedOn));

		// Getting inProgress Status
		if (null != fieldMapping && CollectionUtils.isNotEmpty(fieldMapping.getJiraStatusForInProgressKPI145())) {
			inProgressStatuses = fieldMapping.getJiraStatusForInProgressKPI145();
		}
		LocalDate startDate = null;
		LocalDate endDate;
		boolean isStartDateFound = false;

		Map<String, LocalDate> closedStatusDateMap = new HashMap<>();
		for (JiraHistoryChangeLog storySprintDetail : filterStorySprintDetails) {
			LocalDate activityLocalDate = LocalDate.parse(storySprintDetail.getUpdatedOn().toString().split("T")[0],
					DATE_TIME_FORMATTER);

			if (inProgressStatuses.contains(storySprintDetail.getChangedTo()) && !isStartDateFound) {
				startDate = activityLocalDate;
				isStartDateFound = true;
			}

			if (closedStatus.contains(storySprintDetail.getChangedTo())) {
				if (closedStatusDateMap.containsKey(storySprintDetail.getChangedTo())) {
					closedStatusDateMap.clear();
				}
				closedStatusDateMap.put(storySprintDetail.getChangedTo(), activityLocalDate);
			}
		}
		// Getting the min date of closed status.
		endDate = closedStatusDateMap.values().stream().filter(Objects::nonNull).min(LocalDate::compareTo).orElse(null);
		resultList.put(ACTUAL_START_DATE, startDate);
		resultList.put(ACTUAL_COMPLETE_DATE, endDate);
		return resultList;
	}

	// Filtering the history which happened inside the sprint on basis of activity
	// date
	private List<JiraHistoryChangeLog> getFilterStorySprintDetails(JiraIssueCustomHistory issueCustomHistory,
			List<JiraHistoryChangeLog> filterStorySprintDetails, LocalDate sprintStartDate, LocalDate sprintEndDate) {
		if (CollectionUtils.isNotEmpty(issueCustomHistory.getStatusUpdationLog())) {
			filterStorySprintDetails = issueCustomHistory.getStatusUpdationLog().stream()
					.filter(jiraIssueSprint -> DateUtil.isWithinDateRange(LocalDate
							.parse(jiraIssueSprint.getUpdatedOn().toString().split("T")[0], DATE_TIME_FORMATTER),
							sprintStartDate, sprintEndDate))
					.collect(Collectors.toList());
		}
		return filterStorySprintDetails;
	}

	/**
	 * To calculate delay, devCompletion date
	 * 
	 * @param fieldMapping
	 * @param sprintDetails
	 * @param allIssueHistories
	 * @param jiraIssue
	 * @param completedIssueMap
	 * @return
	 */
	private Map<String, Object> jiraIssueCalculation(FieldMapping fieldMapping, SprintDetails sprintDetails,
			List<JiraIssueCustomHistory> allIssueHistories, JiraIssue jiraIssue,
			Map<JiraIssue, String> completedIssueMap) {
		int jiraIssueDelay = 0;
		Map<String, Object> resultList = new HashMap<>();

		JiraIssueCustomHistory issueCustomHistory = allIssueHistories.stream()
				.filter(jiraIssueCustomHistory -> jiraIssueCustomHistory.getStoryID().equals(jiraIssue.getNumber()))
				.findFirst().orElse(new JiraIssueCustomHistory());
		String devCompletionDate = completedIssueMap.getOrDefault(jiraIssue, "-");
		// calling function for call actual completion days
		Map<String, Object> actualCompletionData = calStartAndEndDate(issueCustomHistory, sprintDetails, fieldMapping);

		if (StringUtils.isNotEmpty(devCompletionDate) && !devCompletionDate.equalsIgnoreCase("-")
				&& jiraIssue.getDevDueDate() != null) {
			LocalDate devCompletedDate = LocalDate.parse(devCompletionDate);
			jiraIssueDelay = getDelay(
					DateUtil.stringToLocalDate(jiraIssue.getDevDueDate(), DateUtil.TIME_FORMAT_WITH_SEC),
					devCompletedDate);
			resultList.put(ISSUE_DELAY, jiraIssueDelay);
		} else {
			resultList.put(ISSUE_DELAY, Constant.DASH);
		}
		resultList.put(DEV_COMPLETION_DATE, devCompletionDate);
		resultList.put(ACTUAL_COMPLETION_DATA, actualCompletionData);
		return resultList;
	}

	private int getDelay(LocalDate dueDate, LocalDate completedDate) {
		int potentialDelays = CommonUtils.getWorkingDays(dueDate, completedDate);
		return (dueDate.isAfter(completedDate)) ? potentialDelays * (-1) : potentialDelays;
	}

	private void setKpiSpecificData(Map<String, IterationKpiModalValue> modalObjectMap, JiraIssue jiraIssue,
			Map<String, Object> jiraIssueData, Map<String, Object> actualCompletionData) {
		IterationKpiModalValue jiraIssueModalObject = modalObjectMap.get(jiraIssue.getNumber());
		String markerValue = Constant.BLANK;
		jiraIssueModalObject.setDevCompletionDate((String) jiraIssueData.get(DEV_COMPLETION_DATE));
		if (actualCompletionData.get(ACTUAL_COMPLETE_DATE) != null)
			jiraIssueModalObject.setActualCompletionDate(actualCompletionData.get(ACTUAL_COMPLETE_DATE).toString());
		else
			jiraIssueModalObject.setActualCompletionDate(" - ");
		if (actualCompletionData.get(ACTUAL_START_DATE) != null) {
			jiraIssueModalObject.setActualStartDate(actualCompletionData.get(ACTUAL_START_DATE).toString());
		} else
			jiraIssueModalObject.setActualStartDate(" - ");
		if (!jiraIssueData.get(ISSUE_DELAY).equals(Constant.DASH)) {
			jiraIssueModalObject.setDelayInDays(String.valueOf(jiraIssueData.get(ISSUE_DELAY)) + "d");
		} else {
			jiraIssueModalObject.setDelayInDays(" - ");
		}
		if (DateUtil.stringToLocalDate(jiraIssue.getDevDueDate(), DateUtil.TIME_FORMAT_WITH_SEC)
				.isAfter(LocalDate.now().minusDays(1))) {
			markerValue = Constant.GREEN;
		}
		jiraIssueModalObject.setMarker(markerValue);
	}

}
