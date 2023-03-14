package com.publicissapient.kpidashboard.apis.jira.scrum.service;

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
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueSprint;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DefectReopenRateServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefectReopenRateServiceImpl.class);
  private static final String SEARCH_BY_PRIORITY = "Filter by priority";
  private static final String DEFECT_REOPEN_RATE = "Defect Reopen Rate";
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
  public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
    LOGGER.info("DEFECT-REOPEN-RATE Kpi {}", kpiRequest.getRequestTrackerId());
    DataCount trendValue = new DataCount();
    treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k,v) -> {
      if(Filters.getFilter(k) == Filters.PROJECT) {
        projectWiseLeafNodeValues(v, trendValue, kpiElement, kpiRequest);
      }
    });
    return kpiElement;
  }

  private void projectWiseLeafNodeValues(List<Node> projectList, DataCount trendValue, KpiElement kpiElement, KpiRequest kpiRequest) {
    Map<String, Object> kpiResultDbMap = fetchKPIDataFromDb(projectList, null, null, kpiRequest);
    List<JiraIssue> totalDefects = (List<JiraIssue>) kpiResultDbMap.get(TOTAL_JIRA_ISSUE);
    List<JiraIssueCustomHistory> reopenJiraHistory = (List<JiraIssueCustomHistory>) kpiResultDbMap.get(JIRA_REOPEN_HISTORY);
    Map<String, List<String>> closedStatusMap = (Map<String, List<String>>) kpiResultDbMap.get(PROJECT_CLOSED_STATUS_MAP);
    Map<String, JiraIssue> storyWisePriority = totalDefects.stream().collect(Collectors
        .toMap(JiraIssue::getNumber, jiraIssue -> jiraIssue));
    Map<String, List<JiraIssue>> priorityWiseTotalStory = totalDefects.stream().collect(Collectors.groupingBy(JiraIssue::getPriority));
    Map<String, Pair<DateTime, DateTime>> storyCloseReopenTime = new HashMap<>();
    List<IterationKpiModalValue> modalValues = new ArrayList<>();
    reopenJiraHistory.forEach(jiraIssueCustomHistory -> {
      List<String> closedStatusList = closedStatusMap.get(jiraIssueCustomHistory.getBasicProjectConfigId());
      JiraIssue jiraIssue = storyWisePriority.get(jiraIssueCustomHistory.getStoryID());
      List<JiraIssueSprint> sprintDetails = jiraIssueCustomHistory.getStorySprintDetails();
      Optional<JiraIssueSprint> closedHistoryOptional = sprintDetails.stream().filter(sprintDetail -> closedStatusList
          .contains(sprintDetail.getFromStatus())).findFirst();
      if (closedHistoryOptional.isPresent()) {
        JiraIssueSprint closedHistory = closedHistoryOptional.get();
        Optional<JiraIssueSprint> reopenHistoryOptional = sprintDetails.stream().filter( sprintDetail -> sprintDetail
            .getActivityDate().isAfter(closedHistory.getActivityDate()) &&
            !closedStatusList.contains(sprintDetail.getFromStatus())).findFirst();
        if (reopenHistoryOptional.isPresent()) {
          JiraIssueSprint reopenHistory = reopenHistoryOptional.get();
          IterationKpiModalValue iterationModal = getIterationKpiModal(jiraIssue, closedHistory, reopenHistory);
          modalValues.add(iterationModal);
          storyCloseReopenTime.put(jiraIssueCustomHistory.getStoryID(), Pair.of(closedHistory.getActivityDate(),
              reopenHistory.getActivityDate()));
        }
      }
    });
    Map<String, List<IterationKpiModalValue>> modalMap = modalValues.stream().collect(Collectors
        .groupingBy(IterationKpiModalValue::getPriority));

    populateTrendValue(trendValue, kpiElement, modalMap, priorityWiseTotalStory, storyCloseReopenTime);

  }

  private IterationKpiModalValue getIterationKpiModal(JiraIssue issue, JiraIssueSprint closedHistory,
                                                      JiraIssueSprint reopenHistory) {
    Long closedTimeMillis = closedHistory.getActivityDate().getMillis();
    Long reopenTimeMillis = reopenHistory.getActivityDate().getMillis();
    LocalDateTime closedTime = DateUtil.convertMillisToLocalDateTime(closedTimeMillis);
    LocalDateTime reopenTime = DateUtil.convertMillisToLocalDateTime(reopenTimeMillis);
    String duration = String.valueOf(TimeUnit.DAYS.convert(reopenTimeMillis - closedTimeMillis,
        TimeUnit.MILLISECONDS));
    return IterationKpiModalValue.builder().issueId(issue.getNumber()).issueURL(issue.getUrl())
        .description(issue.getName()).priority(issue.getPriority()).issueStatus(issue.getStatus())
        .closedDate(DateUtil.stringToLocalDate(closedTime.toString(), DateUtil.TIME_FORMAT).toString())
        .reopenDate(DateUtil.stringToLocalDate(reopenTime.toString(), DateUtil.TIME_FORMAT).toString())
        .durationToReopen(duration + "d")
        .build();
  }


  private void populateTrendValue(DataCount trendValue, KpiElement kpiElement, Map<String, List<IterationKpiModalValue>> modalmap,
                                  Map<String, List<JiraIssue>> priorityWiseStoryList, Map<String, Pair<DateTime, DateTime>> storyCloseReopenTime) {

    List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
    modalmap.forEach((priority, modalValues) -> {
      List<JiraIssue> totalDefects = priorityWiseStoryList.get(priority);
      IterationKpiValue kpiValue = getIterationKpiValue(storyCloseReopenTime, modalValues, totalDefects.size(), priority);
      iterationKpiValues.add(kpiValue);
    });
    List<IterationKpiModalValue> reopenIssueList = modalmap.values().stream().flatMap(List::stream).collect(Collectors.toList());
    Integer totalDefects = priorityWiseStoryList.values().stream().flatMap(List::stream).collect(Collectors.toList()).size();
    IterationKpiValue kpiValue = getIterationKpiValue(storyCloseReopenTime, reopenIssueList, totalDefects, OVERALL);
    iterationKpiValues.add(kpiValue);
    IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, modalmap.keySet());
    IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, null);
    kpiElement.setFilters(iterationKpiFilters);
    trendValue.setValue(iterationKpiValues);
    kpiElement.setModalHeads(KPIExcelColumn.DEFECT_REOPEN_RATE.getColumns());
    kpiElement.setTrendValueList(trendValue);
  }

  private IterationKpiValue getIterationKpiValue(Map<String, Pair<DateTime, DateTime>> storyCloseReopenTime,
                                                 List<IterationKpiModalValue> reopenIssueList, Integer totalDefects, String kpiLabel) {
    reopenIssueList.sort((issue1, issue2) -> LocalDate.parse(issue2.getReopenDate())
        .compareTo(LocalDate.parse(issue1.getReopenDate())));
    Double overAllReopenRate = totalDefects != 0 ? (double)reopenIssueList.size() / totalDefects : 0;
    BigDecimal bdOverallRate= BigDecimal.valueOf(overAllReopenRate * 100).setScale(2, RoundingMode.HALF_DOWN);
    Double averageTimeToReopen = calculateAverage(reopenIssueList, storyCloseReopenTime);
    return populateKpiValue(kpiLabel, bdOverallRate.doubleValue(), averageTimeToReopen, reopenIssueList);
  }

  private Double calculateAverage(List<IterationKpiModalValue> modalValues, Map<String, Pair<DateTime, DateTime>> storyCloseReopenTime) {

    Double totalDuration = modalValues.stream().map(modalValue -> diff(storyCloseReopenTime.get(modalValue.getIssueId())))
        .reduce(0d, Double::sum);
    return totalDuration / modalValues.size();
  }

  private double diff(Pair<DateTime, DateTime> closeReopenTime) {
    // reopentime - closedTime convert to minutes
    return (double) TimeUnit.DAYS
        .convert(closeReopenTime.getRight().getMillis() - closeReopenTime.getLeft().getMillis(),
            TimeUnit.MILLISECONDS);
  }

  private IterationKpiValue populateKpiValue(String kpiValueLable, double overallRate, double average,
                                                 List<IterationKpiModalValue> modalValues) {
    List<IterationKpiData> kpiDataList = new ArrayList<>();
    IterationKpiData overallReopenRate = IterationKpiData.builder().label(DEFECT_REOPEN_RATE).value(overallRate)
        .unit("%").modalValues(modalValues).build();
    IterationKpiData overallAverage = IterationKpiData.builder().label(AVERAGE_TIME_REOPEN).value(average)
        .unit("d").build();
    kpiDataList.add(overallReopenRate);
    kpiDataList.add(overallAverage);
    return new IterationKpiValue(kpiValueLable, null, kpiDataList);
  }


  /**
   * Calculates KPI Metrics
   *
   * @param stringObjectMap type of db object
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
      List<String> defectList = new ArrayList<>();
      defectList.add(CommonConstant.BUG);
      mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
          CommonUtils.convertToPatternList(defectList));
      uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
    });
    mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
        basicProjectConfigIds.stream().map(ObjectId::toString).distinct().collect(Collectors.toList()));
    List<JiraIssue> jiraIssueList = jiraIssueRepository.findIssuesByFilterAndProjectMapFilter(mapOfFilters,
        uniqueProjectMap );
    resultMap.put(TOTAL_JIRA_ISSUE, jiraIssueList);

    List<String> notClosedJiraIssueNumbers = new ArrayList<>();
    basicProjectConfigIds.forEach(basicProjectConfigObjectId -> {
      Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
      FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigObjectId);
      List<String> closedStatusList = (List<String>) CollectionUtils.emptyIfNull(fieldMapping.getJiraTicketClosedStatus());
      closedStatusListBasicConfigMap.put(basicProjectConfigObjectId.toString(), closedStatusList);
      notClosedJiraIssueNumbers.addAll(jiraIssueList.stream().filter(jiraIssue ->
          basicProjectConfigObjectId.toString().equals(jiraIssue.getBasicProjectConfigId())
              && !closedStatusList.contains(jiraIssue.getStatus()))
          .map(JiraIssue::getNumber).collect(Collectors.toList()));
      List<String> defectList = new ArrayList<>();
      defectList.add(CommonConstant.BUG);
      mapOfProjectFilters.put(JiraFeatureHistory.STORY_TYPE.getFieldValueInFeature(),
          CommonUtils.convertToPatternList(defectList));
      mapOfProjectFilters.put("storySprintDetails.story.fromStatus", CommonUtils.convertToPatternList(closedStatusList));
      uniqueProjectMap.put(basicProjectConfigObjectId.toString(), mapOfProjectFilters);
    });

    mapOfFiltersForHistory.put(JiraFeatureHistory.STORY_ID.getFieldValueInFeature(), notClosedJiraIssueNumbers);
    // we get all the data that are once closed and now in open state.
    List<JiraIssueCustomHistory> jiraReopenIssueCustomHistories = jiraIssueCustomHistoryRepository
        .findByFilterAndFromStatusMap(mapOfFiltersForHistory, uniqueProjectMap);
    resultMap.put(PROJECT_CLOSED_STATUS_MAP, closedStatusListBasicConfigMap);
    resultMap.put(JIRA_REOPEN_HISTORY, jiraReopenIssueCustomHistories);
    return resultMap;
  }

}
