package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.JiraFeatureHistory;
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
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueSprint;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

@Component
public class DefectReopenRateServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefectReopenRateServiceImpl.class);
	private static final String SEARCH_BY_PRIORITY = "Filter by priority";
	private static final String DEFECT_REOPEN_RATE = "Defect Reopen Rate";
	private static final String REOPEN_BY_TOTAL_DEFECTS = "Reopen/Total Defects";
	private static final String AVERAGE_TIME_REOPEN = "Average Time to Reopen";
	private static final String OVERALL = "Overall";
	private static final String TOTAL_JIRA_ISSUE = "TOTAL_JIRA_ISSUE";
	private static final String PROJECT_CLOSED_STATUS_MAP = "PROJECT_CLOSED_STATUS_MAP";
	private static final String JIRA_REOPEN_HISTORY = "JIRA_REOPEN_HISTORY";

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	/**
	 * Gets qualifier type
	 *
	 * @return qualifier type
	 */
	@Override
	public String getQualifierType() {
		return KPICode.DEFECT_REOPEN_RATE.name();
	}

	/**
	 * Gets Kpi data based on kpi request
	 *
	 * @param kpiRequest
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @return kpi data
	 * @throws ApplicationException
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		LOGGER.info("DEFECT-REOPEN-RATE Kpi {}", kpiRequest.getRequestTrackerId());
		DataCount trendValue = new DataCount();
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.PROJECT) {
				projectWiseLeafNodeValues(v, trendValue, kpiElement, kpiRequest);
			}
		});
		return kpiElement;
	}

	/**
	 * Update Kpi data based on kpi request
	 *
	 * @param projectList
	 * @param trendValue
	 * @param kpiElement
	 * @param kpiRequest
	 * 
	 */

	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValues(List<Node> projectList, DataCount trendValue, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		Map<String, Object> kpiResultDbMap = fetchKPIDataFromDb(projectList, null, null, kpiRequest);
		List<JiraIssue> totalDefects = (List<JiraIssue>) kpiResultDbMap.get(TOTAL_JIRA_ISSUE);
		List<JiraIssueCustomHistory> reopenJiraHistory = (List<JiraIssueCustomHistory>) kpiResultDbMap
				.get(JIRA_REOPEN_HISTORY);
		Map<String, List<String>> closedStatusMap = (Map<String, List<String>>) kpiResultDbMap
				.get(PROJECT_CLOSED_STATUS_MAP);
		Map<String, JiraIssue> storyWisePriority = totalDefects.stream()
				.collect(Collectors.toMap(JiraIssue::getNumber, jiraIssue -> jiraIssue));
		Map<String, List<JiraIssue>> priorityWiseTotalStory = totalDefects.stream()
				.collect(Collectors.groupingBy(JiraIssue::getPriority));
		Map<String, Pair<DateTime, DateTime>> storyCloseReopenTime = new HashMap<>();
		List<IterationKpiModalValue> modalValues = new ArrayList<>();
		reopenJiraHistory.forEach(jiraIssueCustomHistory -> {
			List<String> closedStatusList = closedStatusMap.get(jiraIssueCustomHistory.getBasicProjectConfigId());
			JiraIssue jiraIssue = storyWisePriority.get(jiraIssueCustomHistory.getStoryID());
			List<JiraIssueSprint> issueHistoryList = jiraIssueCustomHistory.getStorySprintDetails();
			Optional<JiraIssueSprint> closedHistoryOptional = issueHistoryList.stream()
					.filter(issueHistory -> closedStatusList.contains(issueHistory.getFromStatus())).findFirst();
			if (closedHistoryOptional.isPresent()) {
				JiraIssueSprint closedHistory = closedHistoryOptional.get();
				Optional<JiraIssueSprint> reopenHistoryOptional = issueHistoryList.stream()
						.filter(sprintDetail -> sprintDetail.getActivityDate().isAfter(closedHistory.getActivityDate())
								&& !closedStatusList.contains(sprintDetail.getFromStatus()))
						.findFirst();
				if (reopenHistoryOptional.isPresent()) {
					JiraIssueSprint reopenHistory = reopenHistoryOptional.get();
					IterationKpiModalValue iterationModal = getIterationKpiModal(jiraIssue, closedHistory,
							reopenHistory);
					modalValues.add(iterationModal);
					storyCloseReopenTime.put(jiraIssueCustomHistory.getStoryID(),
							Pair.of(closedHistory.getActivityDate(), reopenHistory.getActivityDate()));
				}
			}
		});
		Map<String, List<IterationKpiModalValue>> modalMap = modalValues.stream()
				.collect(Collectors.groupingBy(IterationKpiModalValue::getPriority));

		populateTrendValue(trendValue, kpiElement, modalMap, priorityWiseTotalStory, storyCloseReopenTime);

	}

	/**
	 * @param issue
	 * @param closedHistory
	 * @param reopenHistory
	 * @return IterationKpiModalValue
	 */
	private IterationKpiModalValue getIterationKpiModal(JiraIssue issue, JiraIssueSprint closedHistory,
			JiraIssueSprint reopenHistory) {
		Long closedTimeMillis = closedHistory.getActivityDate().getMillis();
		Long reopenTimeMillis = reopenHistory.getActivityDate().getMillis();
		LocalDateTime closedTime = DateUtil.convertMillisToLocalDateTime(closedTimeMillis);
		LocalDateTime reopenTime = DateUtil.convertMillisToLocalDateTime(reopenTimeMillis);
		String duration = String
				.valueOf(TimeUnit.DAYS.convert(reopenTimeMillis - closedTimeMillis, TimeUnit.MILLISECONDS));
		return IterationKpiModalValue.builder().issueId(issue.getNumber()).issueURL(issue.getUrl())
				.description(issue.getName()).priority(issue.getPriority()).issueStatus(issue.getStatus())
				.closedDate(DateUtil.stringToLocalDate(closedTime.toString(), DateUtil.TIME_FORMAT).toString())
				.reopenDate(DateUtil.stringToLocalDate(reopenTime.toString(), DateUtil.TIME_FORMAT).toString())
				.durationToReopen(duration + "d").build();
	}

	/**
	 * Populate trendValue.
	 * 
	 * @param trendValue
	 * @param kpiElement
	 * @param modalmap
	 * @param priorityWiseStoryList
	 * @param storyCloseReopenTime
	 */
	private void populateTrendValue(DataCount trendValue, KpiElement kpiElement,
			Map<String, List<IterationKpiModalValue>> modalmap, Map<String, List<JiraIssue>> priorityWiseStoryList,
			Map<String, Pair<DateTime, DateTime>> storyCloseReopenTime) {

		List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
		modalmap.forEach((priority, modalValues) -> {
			List<JiraIssue> totalDefects = priorityWiseStoryList.get(priority);
			IterationKpiValue kpiValue = getIterationKpiValue(storyCloseReopenTime, modalValues, totalDefects.size(),
					priority);
			iterationKpiValues.add(kpiValue);
		});
		List<IterationKpiModalValue> reopenIssueList = modalmap.values().stream().flatMap(List::stream)
				.collect(Collectors.toList());
		Integer totalDefects = priorityWiseStoryList.values().stream().flatMap(List::stream)
				.collect(Collectors.toList()).size();
		IterationKpiValue kpiValue = getIterationKpiValue(storyCloseReopenTime, reopenIssueList, totalDefects, OVERALL);
		iterationKpiValues.add(kpiValue);
		Set<String> filterOption = new HashSet<>(modalmap.keySet());
		filterOption.add(OVERALL);
		IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, filterOption);
		IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, null);
		kpiElement.setFilters(iterationKpiFilters);
		trendValue.setValue(iterationKpiValues);
		kpiElement.setModalHeads(KPIExcelColumn.DEFECT_REOPEN_RATE.getColumns());
		kpiElement.setTrendValueList(trendValue);
	}

	/**
	 * 
	 * @param storyCloseReopenTime
	 * @param reopenIssueList
	 * @param totalDefects
	 * @param kpiLabel
	 * @return IterationKpiValue.
	 */
	private IterationKpiValue getIterationKpiValue(Map<String, Pair<DateTime, DateTime>> storyCloseReopenTime,
			List<IterationKpiModalValue> reopenIssueList, Integer totalDefects, String kpiLabel) {
		reopenIssueList.sort((issue1, issue2) -> LocalDate.parse(issue2.getReopenDate())
				.compareTo(LocalDate.parse(issue1.getReopenDate())));
		Double overAllReopenRate = totalDefects != 0 ? (double) reopenIssueList.size() / totalDefects : 0;
		BigDecimal bdOverallRate = BigDecimal.valueOf(overAllReopenRate * 100).setScale(2, RoundingMode.HALF_DOWN);
		Double averageTimeToReopen = calculateAverage(reopenIssueList, storyCloseReopenTime);
		IterationKpiData reopenRateKpi = IterationKpiData.builder().label(DEFECT_REOPEN_RATE)
				.value(bdOverallRate.doubleValue()).unit("%").modalValues(reopenIssueList).build();
		IterationKpiData averageKpi = IterationKpiData.builder().label(AVERAGE_TIME_REOPEN)
				.value(averageTimeToReopen).unit("d").build();
		IterationKpiData reopenedByTotalKpi = IterationKpiData.builder().label(REOPEN_BY_TOTAL_DEFECTS)
				.value(Double.valueOf(reopenIssueList.size())).value1(Double.valueOf(totalDefects)).build();
		return new IterationKpiValue(kpiLabel, null, Arrays.asList(reopenedByTotalKpi, reopenRateKpi, averageKpi));
	}

	/**
	 * @param modalValues
	 * @param storyCloseReopenTime
	 * @return Calculated Average.
	 */
	private Double calculateAverage(List<IterationKpiModalValue> modalValues,
			Map<String, Pair<DateTime, DateTime>> storyCloseReopenTime) {
		if (modalValues.isEmpty()) {
			return 0d;
		}
		Double totalDuration = modalValues.stream()
				.map(modalValue -> diff(storyCloseReopenTime.get(modalValue.getIssueId()))).reduce(0d, Double::sum);
		return totalDuration / modalValues.size();
	}

	/**
	 * 
	 * @param closeReopenTime
	 * @return Difference between Reopentime and Closedtime.
	 */
	private double diff(Pair<DateTime, DateTime> closeReopenTime) {
		// reopentime - closedTime convert to minutes
		return (double) TimeUnit.DAYS.convert(
				closeReopenTime.getRight().getMillis() - closeReopenTime.getLeft().getMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * Calculates KPI Metrics
	 *
	 * @param stringObjectMap
	 *            type of db object
	 * @return KPI value
	 */
	@Override
	public Double calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	/**
	 * Fetches KPI Data from DB
	 *
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @param kpiRequest
	 * @return
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, List<String>> mapOfFiltersForHistory = new LinkedHashMap<>();

		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMapNotIn = new HashMap<>();
		List<ObjectId> basicProjectConfigIds = new ArrayList<>();
		Map<String, List<String>> closedStatusListBasicConfigMap = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			Map<String, Object> mapOfProjectFiltersNotin = new LinkedHashMap<>();
			basicProjectConfigIds.add(basicProjectConfigId);
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			List<String> defectTypeList = new ArrayList<>(fieldMapping.getJiradefecttype());
			defectTypeList.add(NormalizedJira.DEFECT_TYPE.getValue());
			List<String> defectList = defectTypeList.stream().filter(Objects::nonNull).distinct()
					.collect(Collectors.toList());
			List<String> closedStatusList = (List<String>) CollectionUtils
					.emptyIfNull(fieldMapping.getJiraDefectClosedStatus());
			mapOfProjectFiltersNotin.put(JiraFeature.STATUS.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(closedStatusList));
			mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(defectList));
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
			uniqueProjectMapNotIn.put(basicProjectConfigId.toString(), mapOfProjectFiltersNotin);
		});
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().map(ObjectId::toString).distinct().collect(Collectors.toList()));
		List<JiraIssue> jiraIssueList = jiraIssueRepository.findIssuesByFilterAndProjectMapFilter(mapOfFilters,
				uniqueProjectMap, uniqueProjectMapNotIn);
		resultMap.put(TOTAL_JIRA_ISSUE, jiraIssueList);

		List<String> notClosedJiraIssueNumbers = jiraIssueList.stream().map(JiraIssue::getNumber)
				.collect(Collectors.toList());
		basicProjectConfigIds.forEach(basicProjectConfigObjectId -> {
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigObjectId);
			List<String> closedStatusList = (List<String>) CollectionUtils
					.emptyIfNull(fieldMapping.getJiraDefectClosedStatus());
			closedStatusListBasicConfigMap.put(basicProjectConfigObjectId.toString(), closedStatusList);
			List<String> defectTypeList = new ArrayList<>(fieldMapping.getJiradefecttype());
			defectTypeList.add(NormalizedJira.DEFECT_TYPE.getValue());
			List<String> defectList = defectTypeList.stream().filter(Objects::nonNull).distinct()
					.collect(Collectors.toList());
			mapOfProjectFilters.put(JiraFeatureHistory.STORY_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(defectList));
			mapOfProjectFilters.put("storySprintDetails.story.fromStatus",
					CommonUtils.convertToPatternList(closedStatusList));
			uniqueProjectMap.put(basicProjectConfigObjectId.toString(), mapOfProjectFilters);
		});
		mapOfFiltersForHistory.put(JiraFeatureHistory.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().map(ObjectId::toString).distinct().collect(Collectors.toList()));
		mapOfFiltersForHistory.put(JiraFeatureHistory.STORY_ID.getFieldValueInFeature(), notClosedJiraIssueNumbers);
		// we get all the data that are once closed and now in open state.
		List<JiraIssueCustomHistory> jiraReopenIssueCustomHistories = jiraIssueCustomHistoryRepository
				.findByFilterAndFromStatusMap(mapOfFiltersForHistory, uniqueProjectMap);
		resultMap.put(PROJECT_CLOSED_STATUS_MAP, closedStatusListBasicConfigMap);
		resultMap.put(JIRA_REOPEN_HISTORY, jiraReopenIssueCustomHistories);
		return resultMap;
	}

}
