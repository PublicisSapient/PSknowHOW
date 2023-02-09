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
import org.joda.time.DateTime;
import org.joda.time.Minutes;
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
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);

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
							.findByStoryIDInAndBasicProjectConfigIdIn(totalIssues,
									Collections.singletonList(basicProjectConfigId));
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
			List<Integer> overAllBlockedTime = Arrays.asList(0);
			List<Integer> overAllWaitedTime = Arrays.asList(0);
			List<Integer> overAllWastedTime = Arrays.asList(0);
			List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(latestSprint.getProjectFilter().getBasicProjectConfigId());

			List<List<String>> fetchBlockAndWaitStatus = filedMappingExist(fieldMapping);

			List<String> blockedStatusList = fetchBlockAndWaitStatus.get(0);
			List<String> waitStatusList = fetchBlockAndWaitStatus.get(1);
			typeAndPriorityWiseIssues.forEach((issueType, priorityWiseIssue) -> {
				priorityWiseIssue.forEach((priority, issues) -> {
					issueTypes.add(issueType);
					priorities.add(priority);
					List<IterationKpiModalValue> modalValues = new ArrayList<>();
					int blockedTime = 0;
					int waitedTime = 0;
					for (JiraIssue jiraIssue : issues) {
						int jiraIssueWaitedTime = 0;
						int jiraIssueBlockedTime = 0;
						JiraIssueCustomHistory issueCustomHistory = allIssueHistory.stream()
								.filter(jiraIssueCustomHistory -> jiraIssueCustomHistory.getStoryID()
										.equals(jiraIssue.getNumber()))
								.findFirst().orElse(new JiraIssueCustomHistory());

						List<Integer> waitedTimeAndBlockedTime = calculateWaitAndBlockTime(issueCustomHistory,
								sprintDetail, blockedStatusList, waitStatusList);
						jiraIssueWaitedTime = waitedTimeAndBlockedTime.get(0);
						jiraIssueBlockedTime = waitedTimeAndBlockedTime.get(1);
						if (jiraIssueWaitedTime != 0) {
							waitedTime += jiraIssueWaitedTime;
							overAllWaitedTime.set(0, overAllWaitedTime.get(0) + jiraIssueWaitedTime);
						}
						if (jiraIssueBlockedTime != 0) {
							blockedTime += jiraIssueBlockedTime;
							overAllBlockedTime.set(0, overAllBlockedTime.get(0) + jiraIssueBlockedTime);
						}
						populateIterationData(overAllmodalValues, modalValues, jiraIssue, jiraIssueBlockedTime,
								jiraIssueWaitedTime);
					}
					List<IterationKpiData> data = new ArrayList<>();
					IterationKpiData wastage = new IterationKpiData(WASTAGE, Double.valueOf((waitedTime + blockedTime)),
							null, null, HOURS, modalValues);
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

	private List<List<String>> filedMappingExist(FieldMapping fieldMapping) {
		List<String> blockedStatus = new ArrayList<>();
		List<String> waitStatus = new ArrayList<>();
		if (null != fieldMapping) {
			if (CollectionUtils.isNotEmpty(fieldMapping.getJiraBlockedStatus()))
				blockedStatus = fieldMapping.getJiraBlockedStatus();

			if (CollectionUtils.isNotEmpty(fieldMapping.getJiraWaitStatus()))
				waitStatus = fieldMapping.getJiraWaitStatus();
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
			List<String> blockedStatusList, List<String> waitStatusList) {
		List<JiraIssueSprint> filterStorySprintDetails = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(issueCustomHistory.getStorySprintDetails())) {
			filterStorySprintDetails = issueCustomHistory.getStorySprintDetails().stream()
					.filter(jiraIssueSprint -> jiraIssueSprint.getSprintId().equals(sprintDetail.getSprintName()))
					.collect(Collectors.toList());
		}
		int blockedTime = 0;
		int waitedTime = 0;
		for (int i = 0; i < filterStorySprintDetails.size(); i++) {
			JiraIssueSprint entry = filterStorySprintDetails.get(i);

			blockedTime = calculateBlockAndWaitTimeBasedOnFieldmapping(entry, blockedStatusList,
					filterStorySprintDetails, i, sprintDetail, blockedTime);
			waitedTime = calculateBlockAndWaitTimeBasedOnFieldmapping(entry, waitStatusList, filterStorySprintDetails,
					i, sprintDetail, waitedTime);
		}
		return Arrays.asList(waitedTime, blockedTime);
	}

	private int calculateBlockAndWaitTimeBasedOnFieldmapping(JiraIssueSprint entry, List<String> fieldMappingStatus,
			List<JiraIssueSprint> storySprintDetails, int index, SprintDetails sprintDetails, int time) {
		DateTime sprintStartDate = DateUtil.stringToDateTime(sprintDetails.getStartDate(), DATE_TIME_FORMAT);
		DateTime sprintEndDate = DateUtil.stringToDateTime(sprintDetails.getEndDate(), DATE_TIME_FORMAT);
		DateTime entryActivityDate = entry.getActivityDate();
		if (CollectionUtils.isNotEmpty(fieldMappingStatus) && fieldMappingStatus.contains(entry.getFromStatus())) {
			Minutes minutes = null;
			if (storySprintDetails.size() == index + 1) {
				if (entryActivityDate.isAfter(sprintStartDate)) {
					minutes = Minutes.minutesBetween(entryActivityDate, sprintEndDate);
				} else {
					if (Objects.equals(sprintDetails.getState(), SprintDetails.SPRINT_STATE_ACTIVE)) {
						DateTime currDate = DateTime.now();
						minutes = Minutes.minutesBetween(sprintStartDate, currDate);
					} else {
						minutes = Minutes.minutesBetween(sprintStartDate, sprintEndDate);
					}
				}
			} else {
				JiraIssueSprint nextEntry = storySprintDetails.get(index + 1);
				DateTime nextEntryActivityDate = nextEntry.getActivityDate();
				if (!(entryActivityDate.isBefore(sprintStartDate) && nextEntryActivityDate.isBefore(sprintStartDate))
						&& !(entryActivityDate.isAfter(sprintEndDate)
								&& nextEntryActivityDate.isAfter(sprintEndDate))) {
					if (nextEntryActivityDate.isBefore(sprintEndDate)) {
						if (entryActivityDate.isAfter(sprintStartDate)) {
							minutes = Minutes.minutesBetween(entryActivityDate, nextEntryActivityDate);
						} else {
							minutes = Minutes.minutesBetween(sprintStartDate, nextEntryActivityDate);
						}
					} else {
						if (entryActivityDate.isAfter(sprintStartDate)) {
							minutes = Minutes.minutesBetween(entryActivityDate, sprintEndDate);
						} else {
							minutes = Minutes.minutesBetween(sprintStartDate, sprintEndDate);
						}
					}
				}
			}
			if (minutes != null)
				time += minutes.getMinutes();
		}
		return time;
	}

	public void populateIterationData(List<IterationKpiModalValue> overAllmodalValues,
			List<IterationKpiModalValue> modalValues, JiraIssue jiraIssue, int blockedTime, int waitTime) {
		int wastageTime = blockedTime + waitTime;
		IterationKpiModalValue iterationKpiModalValue = new IterationKpiModalValue();
		iterationKpiModalValue.setIssueId(jiraIssue.getNumber());
		iterationKpiModalValue.setIssueURL(jiraIssue.getUrl());
		iterationKpiModalValue.setDescription(jiraIssue.getName());
		iterationKpiModalValue.setIssueStatus(jiraIssue.getStatus());
		iterationKpiModalValue.setIssueType(jiraIssue.getTypeName());
		iterationKpiModalValue.setIssueSize(jiraIssue.getStoryPoints());
		iterationKpiModalValue.setIssuePriority(jiraIssue.getPriority());
		if ((blockedTime != 0)) {
			iterationKpiModalValue.setBlockedTime(String.valueOf(blockedTime / 60 + " hrs"));
		} else {
			iterationKpiModalValue.setBlockedTime(blockedTime + " hrs");
		}
		if ((waitTime != 0)) {
			iterationKpiModalValue.setWaitTime(String.valueOf(waitTime / 60 + " hrs"));
		} else {
			iterationKpiModalValue.setWaitTime(waitTime + " hrs");
		}
		if ((wastageTime != 0)) {
			iterationKpiModalValue.setWastage(String.valueOf(wastageTime / 60 + " hrs"));
		} else {
			iterationKpiModalValue.setWastage(String.valueOf(wastageTime + " hrs"));
		}
		modalValues.add(iterationKpiModalValue);
		overAllmodalValues.add(iterationKpiModalValue);
	}

}
