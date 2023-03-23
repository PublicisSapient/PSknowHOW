package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.AtomicDouble;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.BacklogService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.jira.service.SprintVelocityServiceHelper;
import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFilters;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFiltersOptions;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.IssueDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueSprint;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;

/**
 * Jira service class to fetch backlog readiness kpi details
 * 
 * @author dhachuda
 *
 */
@Component
public class BacklogReadinessEfficiencyServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BacklogReadinessEfficiencyServiceImpl.class);

	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String SEARCH_BY_PRIORITY = "Filter by priority";
	public static final String UNCHECKED = "unchecked";
	private static final String ISSUES = "issues";
	private static final String STORYPOINTS = "Story Point";
	private static final String OVERALL = "Overall";
	private static final String SP = "SP";

	@Autowired
	private BacklogService backlogService;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraHistoryRepo;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private SprintVelocityServiceHelper velocityServiceHelper;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		LOGGER.info("Backlog readiness efficiency service {}", kpiRequest.getRequestTrackerId());

		DataCount trendValue = new DataCount();
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			Filters filters = Filters.getFilter(k);
			if (filters == Filters.SPRINT) {
				getAverageSprintCapacity(v, trendValue, kpiRequest);
				projectWiseLeafNodeValue(v, trendValue, kpiElement, kpiRequest);
			}
		});
		return kpiElement;
	}

	@Override
	public String getQualifierType() {
		return KPICode.BACKLOG_READINESS_EFFICIENCY.name();
	}

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return null;
	}

	/**
	 * Fetches the data from the backlog where the story have completed the grooming
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put(ISSUES, new ArrayList<>(
				backlogService.getBackLogStory(leafNodeList.get(0).getProjectFilter().getBasicProjectConfigId())));
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
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
				.get(latestSprint.getProjectFilter().getBasicProjectConfigId());

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		if (CollectionUtils.isNotEmpty(allIssues)) {
			LOGGER.info("Backlog items ready for development -> request id : {} total jira Issues : {}",
					requestTrackerId, allIssues.size());
			List<String> issueNumbers = allIssues.stream().map(JiraIssue::getNumber).collect(Collectors.toList());
			List<JiraIssueCustomHistory> historyForIssues = jiraHistoryRepo.findByStoryIDIn(issueNumbers);

			Map<String, Map<String, List<JiraIssue>>> typeAndPriorityWiseIssues = allIssues.stream().collect(
					Collectors.groupingBy(JiraIssue::getTypeName, Collectors.groupingBy(JiraIssue::getPriority)));

			Set<String> issueTypes = new HashSet<>();
			Set<String> priorities = new HashSet<>();
			List<IterationKpiValue> iterationKpiValues = trendValue.getValue() != null
					? (List<IterationKpiValue>) trendValue.getValue()
					: new ArrayList<>();
			List<Integer> overAllIssueCount = Arrays.asList(0);
			List<Double> overAllStoryPoints = Arrays.asList(0.0);
			AtomicLong overAllCycleTime = new AtomicLong(0);

			typeAndPriorityWiseIssues.forEach((issueType, priorityWiseIssue) -> {
				priorityWiseIssue.forEach((priority, issues) -> {
					issueTypes.add(issueType);
					priorities.add(priority);
					List<IterationKpiModalValue> modalValues = new ArrayList<>();
					int issueCount = 0;
					Double storyPoint = 0.0;
					long cycleTime = 0;
					for (JiraIssue jiraIssue : issues) {
						issueCount = issueCount + 1;
						overAllIssueCount.set(0, overAllIssueCount.get(0) + 1);
						AtomicLong difference = getActivityCycleTime(fieldMapping.getReadyForDevelopmentStatus(),
								historyForIssues, jiraIssue);
						cycleTime = cycleTime + difference.get();
						overAllCycleTime.set(overAllCycleTime.get() + difference.get());
						if (null != jiraIssue.getStoryPoints()) {
							storyPoint = storyPoint + jiraIssue.getStoryPoints();
							overAllStoryPoints.set(0, overAllStoryPoints.get(0) + jiraIssue.getStoryPoints());
						}
					}
					List<IterationKpiData> data = new ArrayList<>();
					IterationKpiData issuesForDevelopment = new IterationKpiData(ISSUES, Double.valueOf(issueCount),
							Double.valueOf(issueCount), null, "", modalValues);
					IterationKpiData averageCycleTime = new IterationKpiData("Cycle time",
							cycleTime / Double.valueOf(issueCount), null, null, "", modalValues);
					IterationKpiData storyPoints = new IterationKpiData(STORYPOINTS, storyPoint, null, null, SP, null);
					data.add(issuesForDevelopment);
					data.add(storyPoints);
					data.add(averageCycleTime);
					IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, priority, data);
					iterationKpiValues.add(iterationKpiValue);
				});

			});
			List<IterationKpiData> data = new ArrayList<>();

			IterationKpiData overAllIssues = new IterationKpiData(ISSUES, Double.valueOf(overAllIssueCount.get(0)),
					null, null, "", null);
			IterationKpiData overSp = new IterationKpiData(STORYPOINTS, overAllStoryPoints.get(0), null, null, SP,
					null);
			IterationKpiValue iterationKpiValue = iterationKpiValues.get(0);
			IterationKpiData backLogStrength = new IterationKpiData("Backlog Strength",
					overAllStoryPoints.get(0) / iterationKpiValue.getData().get(0).getValue(), null, null, "", null);
			IterationKpiData averageOverAllCycleTime = new IterationKpiData("Cycle time",
					overAllCycleTime.get() / Double.valueOf(overAllIssueCount.get(0)), null, null, "", null);
			data.add(overAllIssues);
			data.add(overSp);
			data.add(backLogStrength);
			data.add(averageOverAllCycleTime);
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
			iterationKpiValues.add(overAllIterationKpiValue);

			// Create kpi level filters
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypes);
			IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, priorities);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
			// Modal Heads Options
			trendValue.setValue(iterationKpiValues);
			kpiElement.setFilters(iterationKpiFilters);
			kpiElement.setModalHeads(KPIExcelColumn.BACKLOG_READINESS_EFFICIENCY.getColumns());
			kpiElement.setTrendValueList(trendValue);
		}
	}

	private AtomicLong getActivityCycleTime(String status, List<JiraIssueCustomHistory> historyForIssues,
			JiraIssue jiraIssue) {
		Optional<JiraIssueCustomHistory> jiraCustomHistory = historyForIssues.stream()
				.filter(history -> history.getStoryID().equals(jiraIssue.getNumber())).findAny();
		AtomicLong difference = new AtomicLong(0);
		if (jiraCustomHistory.isPresent()) {

			Optional<JiraIssueSprint> sprint = jiraCustomHistory.get().getStorySprintDetails().stream()
					.filter(sprintDetails -> sprintDetails.getStatus().equals(status)).findFirst();
			if (sprint.isPresent()) {
				DateTime createdDate = new DateTime(jiraCustomHistory.get().getCreatedDate(), DateTimeZone.UTC);
				DateTime changedDate = new DateTime(sprint.get().getActivityDate(), DateTimeZone.UTC);
				difference.set(difference.get() + new Duration(changedDate, createdDate).getStandardDays());

			} else {
				DateTime createdDate = new DateTime(jiraCustomHistory.get().getCreatedDate(), DateTimeZone.UTC);
				DateTime changedDate = new DateTime(
						jiraCustomHistory.get().getStorySprintDetails().get(0).getActivityDate(), DateTimeZone.UTC);
				difference.set(difference.get() + new Duration(changedDate, createdDate).getStandardDays());
			}
			;

		}
		return difference;
	}

	private void getAverageSprintCapacity(List<Node> sprintLeafNodeList, DataCount trendValue, KpiRequest kpiRequest) {
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		Collections.reverse(sprintLeafNodeList);
		List<Node> sprintForStregthCalculation = sprintLeafNodeList.stream()
				.limit(customApiConfig.getSprintCountForBackLogStrength()).collect(Collectors.toList());

		Map<String, Object> sprintVelocityStoryMap = kpiHelperService
				.fetchSprintVelocityDataFromDb(sprintForStregthCalculation, kpiRequest);

		List<JiraIssue> allJiraIssue = (List<JiraIssue>) sprintVelocityStoryMap.get("sprintVelocityKey");

		Map<Pair<String, String>, List<JiraIssue>> sprintWiseIssues = new HashMap<>();

		List<SprintDetails> sprintDetails = (List<SprintDetails>) sprintVelocityStoryMap
				.get("sprintWiseSprintDetailMap");
		Map<Pair<String, String>, Set<IssueDetails>> currentSprintLeafVelocityMap = new HashMap<>();
		velocityServiceHelper.getSprintForProject(allJiraIssue, sprintWiseIssues, sprintDetails,
				currentSprintLeafVelocityMap);
		AtomicDouble storyPoint = new AtomicDouble();
		sprintForStregthCalculation.forEach(node -> {
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), node.getSprintFilter().getId());
			double sprintVelocityForCurrentLeaf = velocityServiceHelper.calculateSprintVelocityValue(
					currentSprintLeafVelocityMap, currentNodeIdentifier, sprintWiseIssues);
			storyPoint.set(storyPoint.doubleValue() + sprintVelocityForCurrentLeaf);
		});
		List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
		List<IterationKpiData> data = new ArrayList<>();
		IterationKpiData overAllSprintVelocity = new IterationKpiData(ISSUES,
				Double.valueOf(storyPoint.get() / customApiConfig.getSprintCountForBackLogStrength()), null, null, "",
				null);
		data.add(overAllSprintVelocity);
		IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
		iterationKpiValues.add(overAllIterationKpiValue);
		trendValue.setValue(iterationKpiValues);
	}

}
