package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
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
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DefectReopenRateServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String SEARCH_BY_PRIORITY = "Filter by priority";
	private static final String DEFECT_REOPEN_RATE = "Reopen Rate";
	private static final String REOPEN_BY_CLOSED_DEFECTS = "Reopened /Total Closed";
	private static final String AVERAGE_TIME_REOPEN = "Avg. Time to Reopen";
	private static final String OVERALL = "Overall";
	private static final String TOTAL_JIRA_DEFECTS = "TOTAL_JIRA_DEFECTS";
	private static final String PROJECT_CLOSED_STATUS_MAP = "PROJECT_CLOSED_STATUS_MAP";
	private static final String JIRA_REOPEN_HISTORY = "JIRA_REOPEN_HISTORY";
	private static final String TIME_FORMAT_WITH_SEC = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private KpiHelperService kpiHelperService;

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
		log.info("DEFECT-REOPEN-RATE Kpi {}", kpiRequest.getRequestTrackerId());
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
	 */

	@SuppressWarnings("java:S3776")
	private void projectWiseLeafNodeValues(List<Node> projectList, DataCount trendValue, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		Node leafNode = projectList.stream().findFirst().orElse(null);
		if (leafNode != null) {
			Map<String, Object> kpiResultDbMap = fetchKPIDataFromDb(projectList, null, null, kpiRequest);
			List<JiraIssue> totalDefects = (List<JiraIssue>) kpiResultDbMap.get(TOTAL_JIRA_DEFECTS);
			List<JiraIssueCustomHistory> reopenJiraHistory = (List<JiraIssueCustomHistory>) kpiResultDbMap
					.get(JIRA_REOPEN_HISTORY);
			Map<String, List<String>> closedStatusMap = (Map<String, List<String>>) kpiResultDbMap
					.get(PROJECT_CLOSED_STATUS_MAP);
			boolean closedStatusConfigEmpty = closedStatusMap.values().stream().filter(Objects::nonNull)
					.allMatch(List::isEmpty);
			if (closedStatusConfigEmpty) {
				return;
			}
			Map<String, List<JiraIssue>> priorityWiseTotalStory = totalDefects.stream()
					.collect(Collectors.groupingBy(JiraIssue::getPriority));
			Map<String, JiraIssueCustomHistory> reopenJiraHistoryMap = reopenJiraHistory.stream()
					.collect(Collectors.toMap(JiraIssueCustomHistory::getStoryID, Function.identity()));
			Set<String> filters = new LinkedHashSet<>();
			filters.add(OVERALL);
			List<IterationKpiModalValue> overAllModalValues = new ArrayList<>();
			List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
			List<Double> overAllDuration = Arrays.asList(0.0);
			List<String> closedStatusList = closedStatusMap
					.getOrDefault(leafNode.getProjectFilter().getBasicProjectConfigId().toString(), new ArrayList<>());
			priorityWiseTotalStory.forEach((priority, jiraIssueList) -> {
				List<IterationKpiModalValue> modalValues = new ArrayList<>();
				filters.add(priority);
				List<Double> totalDuration = Arrays.asList(0.0);
				jiraIssueList.forEach(jiraIssue -> {
					if (reopenJiraHistoryMap.containsKey(jiraIssue.getNumber())) {
						JiraIssueCustomHistory jiraIssueCustomHistory = reopenJiraHistoryMap.get(jiraIssue.getNumber());
						List<JiraHistoryChangeLog> issueHistoryList = jiraIssueCustomHistory.getStatusUpdationLog();
						Optional<JiraHistoryChangeLog> closedHistoryOptional = issueHistoryList.stream()
								.filter(Objects::nonNull)
								.filter(issueHistory -> closedStatusList.contains(issueHistory.getChangedTo()))
								.findFirst();
						if (closedHistoryOptional.isPresent()) {
							JiraHistoryChangeLog closedHistory = closedHistoryOptional.get();
							Optional<JiraHistoryChangeLog> reopenHistoryOptional = issueHistoryList.stream().filter(
									sprintDetail -> sprintDetail.getUpdatedOn().isAfter(closedHistory.getUpdatedOn())
											&& !closedStatusList.contains(sprintDetail.getChangedTo()))
									.findFirst();
							if (reopenHistoryOptional.isPresent()) {
								JiraHistoryChangeLog reopenHistory = reopenHistoryOptional.get();
								LocalDateTime closedTime = closedHistory.getUpdatedOn();
								LocalDateTime reopenTime = reopenHistory.getUpdatedOn();
								DateTime closedDate = DateUtil.convertLocalDateTimeToDateTime(closedTime);
								DateTime reopenDate = DateUtil.convertLocalDateTimeToDateTime(reopenTime);
								IterationKpiModalValue iterationModal = crateIterationKpiModal(jiraIssue, closedDate,
										reopenDate);
								modalValues.add(iterationModal);
								double duration = Double
										.parseDouble(KpiDataHelper.calWeekHours(closedDate, reopenDate));
								totalDuration.set(0, totalDuration.get(0) + duration);
							}
						}
					}
				});
				addToIterationKpiValues(iterationKpiValues, priority, jiraIssueList, modalValues, totalDuration
				);
				overAllModalValues.addAll(modalValues);
				overAllDuration.set(0, overAllDuration.get(0) + totalDuration.get(0));

			});
			addToIterationKpiValues(iterationKpiValues, OVERALL, totalDefects, overAllModalValues, overAllDuration
			);
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, filters);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, null);
			kpiElement.setFilters(iterationKpiFilters);
			trendValue.setValue(iterationKpiValues);
			kpiElement.setModalHeads(KPIExcelColumn.DEFECT_REOPEN_RATE.getColumns());
			kpiElement.setTrendValueList(trendValue);
		}
	}

	/**
	 * Update iterationKpiValues.
	 *
	 * @param iterationKpiValues
	 * @param priority
	 * @param jiraIssueList
	 * @param modalValues
	 * @param totalDuration
	 */
	private void addToIterationKpiValues(List<IterationKpiValue> iterationKpiValues, String priority,
			List<JiraIssue> jiraIssueList, List<IterationKpiModalValue> modalValues, List<Double> totalDuration) {
		double averageTimeToReopen = totalDuration.get(0) > 0 && !modalValues.isEmpty()
				? Math.ceil(totalDuration.get(0) / modalValues.size())
				: 0;
		IterationKpiData reopenRateKpi = createReopenRateIterationData(modalValues, jiraIssueList.size());
		IterationKpiData reopenedByTotalKpi = IterationKpiData.builder().label(REOPEN_BY_CLOSED_DEFECTS)
				.value(Double.valueOf(modalValues.size())).value1(Double.valueOf(jiraIssueList.size())).build();
		IterationKpiData averageKpi = IterationKpiData.builder().label(AVERAGE_TIME_REOPEN).value(averageTimeToReopen)
				.unit("Hrs").build();
		iterationKpiValues.add(
				new IterationKpiValue(priority, null, Arrays.asList(reopenedByTotalKpi, reopenRateKpi, averageKpi)));
	}

	/**
	 * create IterationKPIModel.
	 *
	 * @param issue
	 * @param closedHistory
	 * @param reopenHistory
	 * @return IterationKpiModalValue
	 */
	private IterationKpiModalValue crateIterationKpiModal(JiraIssue issue, DateTime closedHistory,
			DateTime reopenHistory) {

		String duration = KpiDataHelper.calWeekHours(closedHistory, reopenHistory);

		return IterationKpiModalValue.builder().issueId(issue.getNumber()).issueURL(issue.getUrl())
				.description(issue.getName()).priority(issue.getPriority()).issueStatus(issue.getStatus())
				.closedDate(DateUtil.dateTimeConverter(closedHistory, TIME_FORMAT_WITH_SEC))
				.reopenDate(DateUtil.dateTimeConverter(reopenHistory, TIME_FORMAT_WITH_SEC))
				.durationToReopen(duration + "Hrs").build();
	}

	/**
	 * create ReopenRateIterationData.
	 *
	 * @param reopenIssueList
	 * @param totalDefects
	 * @return IterationKpiData.
	 */
	private IterationKpiData createReopenRateIterationData(List<IterationKpiModalValue> reopenIssueList,
			Integer totalDefects) {
		reopenIssueList.sort(
				(issue1, issue2) -> DateUtil.stringToLocalDate(issue2.getReopenDate(), DateUtil.DISPLAY_DATE_FORMAT)
						.compareTo(DateUtil.stringToLocalDate(issue1.getReopenDate(), DateUtil.DISPLAY_DATE_FORMAT)));
		Double overAllReopenRate = totalDefects != 0 ? (double) reopenIssueList.size() / totalDefects : 0;
		BigDecimal bdOverallRate = BigDecimal.valueOf(overAllReopenRate * 100).setScale(2, RoundingMode.HALF_DOWN);
		return IterationKpiData.builder().label(DEFECT_REOPEN_RATE).value(bdOverallRate.doubleValue()).unit("%")
				.modalValues(reopenIssueList).build();
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
		List<ObjectId> basicProjectConfigIds = new ArrayList<>();
		Map<String, List<String>> closedStatusListBasicConfigMap = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			basicProjectConfigIds.add(basicProjectConfigId);
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			List<String> defectTypeList = new ArrayList<>();
			if (fieldMapping.getJiradefecttype() != null) {
				defectTypeList.addAll(fieldMapping.getJiradefecttype());
			}
			defectTypeList.add(NormalizedJira.DEFECT_TYPE.getValue());
			List<String> defectList = defectTypeList.stream().filter(Objects::nonNull).distinct()
					.collect(Collectors.toList());
			mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(defectList));
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
		});
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().map(ObjectId::toString).distinct().collect(Collectors.toList()));
		List<JiraIssue> jiraIssues = jiraIssueRepository.findIssuesByFilterAndProjectMapFilter(mapOfFilters,
				uniqueProjectMap);

		List<String> jiraDefectID = jiraIssues.stream().map(JiraIssue::getNumber).collect(Collectors.toList());
		basicProjectConfigIds.forEach(basicProjectConfigObjectId -> {
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigObjectId);
			List<String> closedStatusList = (List<String>) CollectionUtils
					.emptyIfNull(fieldMapping.getJiraDefectClosedStatusKPI137());
			closedStatusListBasicConfigMap.put(basicProjectConfigObjectId.toString(), closedStatusList);
			mapOfProjectFilters.put("statusUpdationLog.story.changedTo",
					CommonUtils.convertToPatternList(closedStatusList));
			uniqueProjectMap.put(basicProjectConfigObjectId.toString(), mapOfProjectFilters);
		});
		mapOfFiltersForHistory.put(JiraFeatureHistory.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().map(ObjectId::toString).distinct().collect(Collectors.toList()));
		mapOfFiltersForHistory.put(JiraFeatureHistory.STORY_ID.getFieldValueInFeature(), jiraDefectID);
		// we get all the data that are once closed
		List<JiraIssueCustomHistory> jiraReopenIssueCustomHistories = jiraIssueCustomHistoryRepository
				.findByFilterAndFromStatusMap(mapOfFiltersForHistory, uniqueProjectMap);
		List<String> jiraHistoryDefectID = jiraReopenIssueCustomHistories.stream()
				.map(JiraIssueCustomHistory::getStoryID).collect(Collectors.toList());
		List<JiraIssue> totalJiraDefect = jiraIssues.stream()
				.filter(jiraIssue -> jiraHistoryDefectID.contains(jiraIssue.getNumber())).collect(Collectors.toList());
		resultMap.put(TOTAL_JIRA_DEFECTS, totalJiraDefect);
		resultMap.put(PROJECT_CLOSED_STATUS_MAP, closedStatusListBasicConfigMap);
		resultMap.put(JIRA_REOPEN_HISTORY, jiraReopenIssueCustomHistories);
		return resultMap;
	}

}
