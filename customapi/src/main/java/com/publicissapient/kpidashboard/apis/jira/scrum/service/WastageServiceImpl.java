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
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueSprint;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.publicissapient.kpidashboard.common.constant.CommonConstant.OVERALL;

@Component
public class WastageServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WastageServiceImpl.class);
	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String SEARCH_BY_PRIORITY = "Filter by priority";

	public static final String UNCHECKED = "unchecked";
	private static final String ISSUES = "issues";
	private static final String ISSUES_CUSTOM_HISTORY = "issues custom history";
	private static final String SPRINT_DETAILS = "sprint details";
	private static final String BLOCKED_TIME = "Block Time";
	private static final String WAITING_TIME = "Waiting Time";
	private static final String WASTAGE = "Wastage";
	private static final String HOURS = "Hours";
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

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
		List<String> basicProjectConfigIds = new ArrayList<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);

		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			basicProjectConfigIds.add(basicProjectConfigId.toString());
		});

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
							.findByStoryIDInAndBasicProjectConfigIdIn(totalIssues, basicProjectConfigIds);
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
			List<Integer> overAllIssueCount = Arrays.asList(0);
			List<Integer> overAllBlockedTime = Arrays.asList(0);
			List<Integer> overAllWaitedTime = Arrays.asList(0);
			List<Integer> overAllWastedTime = Arrays.asList(0);
			List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(latestSprint.getProjectFilter().getBasicProjectConfigId());

			typeAndPriorityWiseIssues.forEach((issueType, priorityWiseIssue) -> {
				priorityWiseIssue.forEach((priority, issues) -> {
					issueTypes.add(issueType);
					priorities.add(priority);
					List<IterationKpiModalValue> modalValues = new ArrayList<>();
					int issueCount = 0;
					int blockedTime = 0;
					int waitedTime = 0;
					for (JiraIssue jiraIssue : issues) {
						populateIterationData(overAllmodalValues, modalValues, jiraIssue, blockedTime, waitedTime);
						issueCount = issueCount + 1;
						overAllIssueCount.set(0, overAllIssueCount.get(0) + 1);
						JiraIssueCustomHistory issueCustomHistory = allIssueHistory.stream()
								.filter(jiraIssueCustomHistory -> jiraIssueCustomHistory.getStoryID()
										.equals(jiraIssue.getNumber()))
								.findFirst().orElse(new JiraIssueCustomHistory());

						List<Integer> waitedTimeAndBlockedTime = calculateWaitAndBlockTime(issueCustomHistory,
								sprintDetail);
						waitedTime = waitedTimeAndBlockedTime.get(0);
						blockedTime = waitedTimeAndBlockedTime.get(1);
						if (waitedTime != 0) {
							waitedTime += waitedTime;
							overAllWaitedTime.set(0, overAllWaitedTime.get(0) + waitedTime);
						}
						if (blockedTime != 0) {
							blockedTime += blockedTime;
							overAllBlockedTime.set(0, overAllBlockedTime.get(0) + blockedTime);
						}
					}
					List<IterationKpiData> data = new ArrayList<>();
					IterationKpiData wastage = new IterationKpiData(WASTAGE, Double.valueOf((waitedTime+blockedTime)), null, null,
							HOURS, modalValues);
					IterationKpiData blocked = new IterationKpiData(BLOCKED_TIME, Double.valueOf(blockedTime), null,
							null, HOURS, null);
					IterationKpiData waited = new IterationKpiData(WAITING_TIME, Double.valueOf(waitedTime), null, null,
							HOURS, null);
					data.add(wastage);
					data.add(blocked);
					data.add(waited);
					IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, priority, data);
					iterationKpiValues.add(iterationKpiValue);
				});

		});
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
	 * Calculate the waitTime and BlockTime
	 *
	 * @param issueCustomHistory
	 * @param sprintDetail
	 * @return List<Integer>
	 */
	List<Integer> calculateWaitAndBlockTime(JiraIssueCustomHistory issueCustomHistory, SprintDetails sprintDetail) {
		List<JiraIssueSprint> storySprintDetails = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(issueCustomHistory.getStorySprintDetails())) {
			storySprintDetails = issueCustomHistory.getStorySprintDetails().stream()
					.filter(jiraIssueSprint -> jiraIssueSprint.getSprintId().equals(sprintDetail.getSprintName()))
					.collect(Collectors.toList());
		}
		DateTime sprintEndDate = DateUtil.stringToDateTime(sprintDetail.getEndDate(), DATE_TIME_FORMAT);
		int blockedTime = 0;
		int waitedTime = 0;
		for (int i = 0; i < storySprintDetails.size(); i++) {
			JiraIssueSprint entry = storySprintDetails.get(i);
			DateTime entryActivityDate = entry.getActivityDate();
			// On hold and ready for testing will come from fieldMappingStatus...
			blockedTime = calculateTime(entry, Arrays.asList("Open"), storySprintDetails, i, entryActivityDate,
					sprintEndDate, blockedTime);
			waitedTime = calculateTime(entry, Arrays.asList("In Investigation"), storySprintDetails, i,
					entryActivityDate, sprintEndDate, waitedTime);
		}
		return Arrays.asList(waitedTime, blockedTime);
	}

	private int calculateTime(JiraIssueSprint entry, List<String> fieldMappingStatus,
			List<JiraIssueSprint> storySprintDetails, int index, DateTime entryActivityDate, DateTime sprintEndDate,
			int time) {
		if (CollectionUtils.isNotEmpty(fieldMappingStatus) && fieldMappingStatus.contains(entry.getFromStatus())) {
			Hours hours;
			if (storySprintDetails.size() == index + 1) {
				hours = Hours.hoursBetween(entryActivityDate, sprintEndDate);
			} else {
				JiraIssueSprint nextEntry = storySprintDetails.get(index + 1);
				DateTime nextEntryActivityDate = nextEntry.getActivityDate();
				hours = nextEntryActivityDate.isBefore(sprintEndDate)
						? Hours.hoursBetween(entryActivityDate, nextEntryActivityDate)
						: Hours.hoursBetween(entryActivityDate, sprintEndDate);
			}
			time += hours.getHours();
		}
		return time;
	}

	public void populateIterationData(List<IterationKpiModalValue> overAllmodalValues,
			List<IterationKpiModalValue> modalValues, JiraIssue jiraIssue, int blockedTime, int waitTime) {
		IterationKpiModalValue iterationKpiModalValue = new IterationKpiModalValue();
		iterationKpiModalValue.setIssueId(jiraIssue.getNumber());
		iterationKpiModalValue.setIssueURL(jiraIssue.getUrl());
		iterationKpiModalValue.setDescription(jiraIssue.getName());
		iterationKpiModalValue.setIssueStatus(jiraIssue.getStatus());
		iterationKpiModalValue.setIssueType(jiraIssue.getTypeName());
		iterationKpiModalValue.setIssueSize(jiraIssue.getStoryPoints());
		iterationKpiModalValue.setIssuePriority(jiraIssue.getPriority());
		iterationKpiModalValue.setBlockedTime(String.valueOf(blockedTime + " hrs"));
		iterationKpiModalValue.setWaitTime(String.valueOf(waitTime + " hrs"));
		iterationKpiModalValue.setWastage(String.valueOf(blockedTime + waitTime + " hrs"));
		modalValues.add(iterationKpiModalValue);
		overAllmodalValues.add(iterationKpiModalValue);
	}

}
