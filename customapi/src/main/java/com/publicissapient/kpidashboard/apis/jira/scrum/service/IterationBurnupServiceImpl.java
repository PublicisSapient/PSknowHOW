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

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static com.publicissapient.kpidashboard.apis.util.KpiDataHelper.sprintWiseDelayCalculation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.IterationPotentialDelay;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.util.DateUtil;

/**
 * Iteration Burnups KPI gives a graphical representation of no. of issues
 * planned to be closed each day of iteration, actual count of issues closed day
 * wise and the predicted Iteration Burnups for the remaining days of the
 * iteration. {@link JiraKPIService}
 *
 * @author tauakram
 */
@Component
@Slf4j
public class IterationBurnupServiceImpl extends JiraKPIService<Map<String, Long>, List<Object>, Map<String, Object>> {
	private static final String SPRINT = "sprint";
	public static final String UNCHECKED = "unchecked";
	public static final String TOTAL_ISSUE = "total issue";
	public static final String COMPLETED_ISSUE = "completed issue";
	public static final String PLANNED_COMPLETION = "Planned Completion";
	public static final String OVERALL_SCOPE = "Overall Scope";
	public static final String ACTUAL_COMPLETION = "Completion Till Date";
	public static final String PREDICTED_COMPLETION = "Predicted Completion";
	public static final String DATE = "date";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	@Autowired
	private ConfigHelperService configHelperService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getQualifierType() {
		return KPICode.ITERATION_BURNUP.name();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		List<DataCount> trendValueList = new ArrayList<>();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

			if (Filters.getFilter(k) == Filters.SPRINT) {
				sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);
			}

		});

		return kpiElement;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Long> calculateKPIMetrics(Map<String, Object> objectMap) {
		return new HashMap<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(final List<Node> leafNodeList, final String startDate,
			final String endDate, final KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			log.info("Iteration Burnup -> Requested sprint : {}", leafNode.getName());
			SprintDetails sprintDetails = getSprintDetailsFromBaseClass();
			if (null != sprintDetails) {
				List<String> allIssues = new ArrayList<>();
				allIssues.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.TOTAL_ISSUES));
				allIssues.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.ADDED_ISSUES));
				allIssues.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.PUNTED_ISSUES));

				if (CollectionUtils.isNotEmpty(allIssues)) {
					allIssues = allIssues.stream().distinct().collect(Collectors.toList());
					List<JiraIssue> totalIssueList = getJiraIssuesFromBaseClass(allIssues);
					List<JiraIssueCustomHistory> allIssuesHistory = getJiraIssuesCustomHistoryFromBaseClass(allIssues);
					Map<LocalDate, Set<JiraIssue>> totalIssues = new HashMap<>();
					Map<LocalDate, Set<JiraIssue>> addedIssues = new HashMap<>();
					Map<LocalDate, Set<JiraIssue>> removedIssues = new HashMap<>();
					Map<LocalDate, Set<JiraIssue>> completedIssues = new HashMap<>();
					allIssuesHistory.forEach(issueHistory -> {
						if (CollectionUtils.isNotEmpty(issueHistory.getSprintUpdationLog())) {
							List<JiraHistoryChangeLog> sprintUpdationLog = issueHistory.getSprintUpdationLog();
							Collections.sort(sprintUpdationLog,
									Comparator.comparing(JiraHistoryChangeLog::getUpdatedOn));
							createTotalIssuesDateWiseMap(sprintDetails, totalIssueList, totalIssues, issueHistory,
									sprintUpdationLog);
							createAddedIssueDateWiseMap(sprintDetails, totalIssueList, addedIssues, issueHistory,
									sprintUpdationLog);
							createPuntedIssueDateWiseMap(sprintDetails, totalIssueList, removedIssues, issueHistory,
									sprintUpdationLog);
						}
						createCompletedIssuesDateWiseMap(sprintDetails, totalIssueList, completedIssues, issueHistory);

					});
					resultListMap.put(CommonConstant.TOTAL_ISSUES, totalIssues);
					resultListMap.put(CommonConstant.PUNTED_ISSUES, removedIssues);
					resultListMap.put(CommonConstant.ADDED_ISSUES, addedIssues);
					resultListMap.put(CommonConstant.COMPLETED_ISSUES, completedIssues);
					resultListMap.put(SPRINT, sprintDetails);
				}
			}
		}
		return resultListMap;
	}

	private Map<String, Pair<LocalDate, List<JiraIssue>>> createFilterMap(Map<LocalDate, List<JiraIssue>> totalIssues) {
		Map<String, Pair<LocalDate, List<JiraIssue>>> returnMap = new HashMap<>();
		totalIssues.forEach((k, v) -> {
			Map<String, List<JiraIssue>> collect = v.stream().collect(Collectors.groupingBy(JiraIssue::getTypeName));
			collect.forEach((type, v1) -> returnMap.put(type, Pair.of(k, v1)));

		});
		return returnMap;
	}

	private void createPuntedIssueDateWiseMap(SprintDetails sprintDetails, List<JiraIssue> totalIssueList,
			Map<LocalDate, Set<JiraIssue>> removedIssues, JiraIssueCustomHistory issueHistory,
			List<JiraHistoryChangeLog> withInSprintLogs) {
		LocalDate sprintStartDate = LocalDate.parse(sprintDetails.getStartDate().split("\\.")[0], DATE_TIME_FORMATTER);
		LocalDate sprintEndDate = LocalDate.parse(sprintDetails.getEndDate().split("\\.")[0], DATE_TIME_FORMATTER);
		withInSprintLogs.stream()
				.filter(updateLogs -> updateLogs.getChangedFrom().equalsIgnoreCase(sprintDetails.getSprintName())
						&& DateUtil.isWithinDateRange(updateLogs.getUpdatedOn().toLocalDate(), sprintStartDate,
								sprintEndDate))
				.forEach(updateLogs -> {
					Set<JiraIssue> jiraIssueList = getRespectiveJiraIssue(totalIssueList, issueHistory);
					LocalDate updatedLog = updateLogs.getUpdatedOn().toLocalDate();
					removedIssues.computeIfPresent(updatedLog, (k, v) -> {
						v.addAll(jiraIssueList);
						return v;
					});
					removedIssues.putIfAbsent(updatedLog, jiraIssueList);
				});
	}

	private void createAddedIssueDateWiseMap(SprintDetails sprintDetails, List<JiraIssue> totalIssueList,
			Map<LocalDate, Set<JiraIssue>> addedIssues, JiraIssueCustomHistory issueHistory,
			List<JiraHistoryChangeLog> withInSprintLogs) {
		LocalDate startDate = LocalDate.parse(sprintDetails.getStartDate().split("\\.")[0], DATE_TIME_FORMATTER);
		withInSprintLogs.stream()
				.filter(updateLogs -> updateLogs.getChangedTo().equalsIgnoreCase(sprintDetails.getSprintName()))
				.forEach(updateLogs -> {
					Set<JiraIssue> jiraIssueList = getRespectiveJiraIssue(totalIssueList, issueHistory);
					LocalDate updatedLog = updateLogs.getUpdatedOn().toLocalDate().isBefore(startDate.plusDays(1))
							? startDate
							: updateLogs.getUpdatedOn().toLocalDate();
					addedIssues.computeIfPresent(updatedLog, (k, v) -> {
						v.addAll(jiraIssueList);
						return v;
					});
					addedIssues.putIfAbsent(updatedLog, jiraIssueList);
				});
	}

	private void createTotalIssuesDateWiseMap(SprintDetails sprintDetails, List<JiraIssue> totalIssueList,
			Map<LocalDate, Set<JiraIssue>> totalIssues, JiraIssueCustomHistory issueHistory,
			List<JiraHistoryChangeLog> sprintUpdationLog) {
		int lastIndex = sprintUpdationLog.size() - 1;
		if (sprintUpdationLog.get(lastIndex).getUpdatedOn().toLocalDate().isBefore(
				LocalDate.parse(sprintDetails.getStartDate().split("\\.")[0], DATE_TIME_FORMATTER).plusDays(1))
				&& sprintUpdationLog.get(lastIndex).getChangedTo().equalsIgnoreCase(sprintDetails.getSprintName())) {
			LocalDate startDate = LocalDate.parse(sprintDetails.getStartDate().split("\\.")[0], DATE_TIME_FORMATTER);
			Set<JiraIssue> jiraIssueList = getRespectiveJiraIssue(totalIssueList, issueHistory);
			totalIssues.computeIfPresent(startDate, (k, v) -> {
				v.addAll(jiraIssueList);
				return v;
			});
			totalIssues.putIfAbsent(startDate, jiraIssueList);
		}
	}

	private Set<JiraIssue> getRespectiveJiraIssue(List<JiraIssue> totalIssueList, JiraIssueCustomHistory issueHistory) {
		return totalIssueList.stream()
				.filter(jiraIssue -> jiraIssue.getNumber().equalsIgnoreCase(issueHistory.getStoryID()))
				.collect(Collectors.toSet());
	}

	private void createCompletedIssuesDateWiseMap(SprintDetails sprintDetails, List<JiraIssue> totalIssueList,
			Map<LocalDate, Set<JiraIssue>> completedIssues, JiraIssueCustomHistory issueHistory) {
		if (CollectionUtils.isNotEmpty(issueHistory.getStatusUpdationLog())) {
			LocalDate sprintStartDate = LocalDate.parse(sprintDetails.getStartDate().split("\\.")[0],
					DATE_TIME_FORMATTER);
			LocalDate sprintEndDate = LocalDate.parse(sprintDetails.getEndDate().split("\\.")[0], DATE_TIME_FORMATTER);

			List<JiraHistoryChangeLog> statusUpdationLog = issueHistory.getStatusUpdationLog();
			Map<String, LocalDate> closedStatusDateMap = new HashMap<>();
			Set<String> closedStatus = sprintDetails.getCompletedIssues().stream().map(SprintIssue::getStatus)
					.collect(Collectors.toSet());
			for (JiraHistoryChangeLog jiraHistoryChangeLog : statusUpdationLog) {
				if (closedStatus.contains(jiraHistoryChangeLog.getChangedTo())) {
					LocalDate activityDate = LocalDate
							.parse(jiraHistoryChangeLog.getUpdatedOn().toString().split("\\.")[0], DATE_TIME_FORMATTER);
					if (DateUtil.isWithinDateRange(activityDate, sprintStartDate, sprintEndDate)) {
						if (closedStatusDateMap.containsKey(jiraHistoryChangeLog.getChangedTo())) {
							closedStatusDateMap.clear();
						}
						closedStatusDateMap.put(jiraHistoryChangeLog.getChangedTo(), activityDate);
					}
				}
			}
			// Getting the min date of closed status.
			LocalDate updatedLog = closedStatusDateMap.values().stream().filter(Objects::nonNull)
					.min(LocalDate::compareTo).orElse(null);
			Set<JiraIssue> jiraIssueList = getRespectiveJiraIssue(totalIssueList, issueHistory);
			completedIssues.computeIfPresent(updatedLog, (k, v) -> {
				v.addAll(jiraIssueList);
				return v;
			});
			completedIssues.putIfAbsent(updatedLog, jiraIssueList);
		}
	}

	/**
	 * This method populates KPI value to sprint leaf nodes. It also gives the trend
	 * analysis at sprint wise.
	 *
	 * @param mapTmp
	 *            node id map
	 * @param sprintLeafNodeList
	 *            sprint nodes list
	 * @param trendValueList
	 *            list to hold trend nodes data
	 * @param kpiElement
	 *            the KpiElement
	 * @param kpiRequest
	 *            the KpiRequest
	 */
	@SuppressWarnings(UNCHECKED)
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);
		Object basicProjectConfigId = latestSprint.getProjectFilter().getBasicProjectConfigId();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		Map<LocalDate, Set<JiraIssue>> allIssues = (Map<LocalDate, Set<JiraIssue>>) resultMap
				.get(CommonConstant.TOTAL_ISSUES);
		Map<LocalDate, Set<JiraIssue>> removedIssues = (Map<LocalDate, Set<JiraIssue>>) resultMap
				.get(CommonConstant.PUNTED_ISSUES);
		Map<LocalDate, Set<JiraIssue>> addedIssues = (Map<LocalDate, Set<JiraIssue>>) resultMap
				.get(CommonConstant.ADDED_ISSUES);
		Map<LocalDate, Set<JiraIssue>> completedIssueMap = (Map<LocalDate, Set<JiraIssue>>) resultMap
				.get(CommonConstant.COMPLETED_ISSUES);
		SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT);
		if (MapUtils.isNotEmpty(allIssues)) {
			log.info("Iteration Burnups -> request id : {} total jira Issues : {}", requestTrackerId, allIssues.size());
			List<String> totalIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
					CommonConstant.TOTAL_ISSUES);
			List<JiraIssue> totalIssueList = getJiraIssuesFromBaseClass(totalIssues);
			List<String> notCompletedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
					CommonConstant.NOT_COMPLETED_ISSUES);

			List<IterationPotentialDelay> iterationPotentialDelayList = calculatePotentialDelay(sprintDetails,
					totalIssueList, fieldMapping);
			Map<String, IterationPotentialDelay> issueWiseDelay = iterationPotentialDelayList.stream()
					.collect(Collectors.toMap(IterationPotentialDelay::getIssueId, Function.identity(), (e1, e2) -> e2,
							LinkedHashMap::new));
			Set<JiraIssue> allPotentialDelayIssue = totalIssueList.stream()
					.filter(i -> issueWiseDelay.containsKey(i.getNumber())).collect(Collectors.toSet());
			Map<LocalDate, List<JiraIssue>> potentialDelay = allPotentialDelayIssue.stream()
					.filter(f -> StringUtils.isNotBlank(issueWiseDelay.get(f.getNumber()).getPredictedCompletedDate()))
					.collect(Collectors.groupingBy(
							f -> LocalDate.parse(issueWiseDelay.get(f.getNumber()).getPredictedCompletedDate())));

			LocalDate sprintStartDate = DateUtil.stringToLocalDate(sprintDetails.getStartDate(),
					DateUtil.TIME_FORMAT_WITH_SEC);
			LocalDate sprintEndDate = DateUtil
					.stringToLocalDate(sprintDetails.getEndDate(), DateUtil.TIME_FORMAT_WITH_SEC).plusDays(1);

			List<DataCountGroup> dataCountGroups = new ArrayList<>();
			Set<JiraIssue> processedAllIssues = new HashSet<>();
			Set<JiraIssue> processedPlannedIssues = new HashSet<>();
			Set<JiraIssue> processCompletedIssues = new HashSet<>();
			Set<JiraIssue> pcdIssues = new HashSet<>();
			for (LocalDate date = sprintStartDate; date.isBefore(sprintEndDate); date = date.plusDays(1)) {
				DataCountGroup dataCountGroup = new DataCountGroup();
				List<DataCount> dataCountList = new ArrayList<>();
				Long dueDateWiseTypeCountMap = filterDataBasedOnDateAndTypeWise(allIssues, removedIssues, addedIssues,
						processedAllIssues, date);
				Long plannedDateWiseTypeCount = getDueDateWisedIssues(processedAllIssues, date, processedPlannedIssues,
						"DUE_DATE");
				Set<JiraIssue> completedIssues = completedIssueMap.getOrDefault(date, new HashSet<>());
				completedIssues.retainAll(processedAllIssues);
				Long closedDateWiseCount = getDueDateWisedIssues(completedIssues, date, processCompletedIssues,
						"updateDate");
				List<JiraIssue> orDefault = potentialDelay.getOrDefault(date, new ArrayList<>());
				pcdIssues.addAll(orDefault);
				dataCountList.add(getDataCountObject(dueDateWiseTypeCountMap, latestSprint.getId(), OVERALL_SCOPE));
				dataCountList
						.add(getDataCountObject(plannedDateWiseTypeCount, latestSprint.getId(), PLANNED_COMPLETION));
				dataCountList.add(getDataCountObject(closedDateWiseCount, latestSprint.getId(), ACTUAL_COMPLETION));
				dataCountList
						.add(getDataCountObject((long) pcdIssues.size(), latestSprint.getId(), PREDICTED_COMPLETION));
				dataCountGroup.setFilter(date.toString());
				dataCountGroup.setValue(dataCountList);
				dataCountGroups.add(dataCountGroup);

			}

			IterationKpiValue iterationKpiValue = new IterationKpiValue();
			iterationKpiValue.setDataGroup(dataCountGroups);
			iterationKpiValue.setFilter1("OVERALL");
			iterationKpiValue.setAdditionalGroup(Arrays.asList("Gap"));
			List<IterationKpiValue> iterationKpiValueList = new ArrayList<>();
			iterationKpiValueList.add(iterationKpiValue);
			kpiElement.setTrendValueList(iterationKpiValueList);
		}
	}

	private Long getDueDateWisedIssues(Set<JiraIssue> processedAllIssues, LocalDate date,
			Set<JiraIssue> processedPlannedIssues, String due_date) {
		if (due_date.equalsIgnoreCase("DUE_DATE")) {
			processedPlannedIssues.addAll(processedAllIssues.stream()
					.filter(f -> StringUtils.isNotBlank(f.getDueDate())
							&& DateUtil.stringToLocalDate(f.getDueDate(), DateUtil.TIME_FORMAT_WITH_SEC).isEqual(date))
					.collect(Collectors.toSet()));

		} else if (due_date.equalsIgnoreCase("updateDate")) {
			processedPlannedIssues.addAll(processedAllIssues.stream()
					.filter(f -> StringUtils.isNotBlank(f.getUpdateDate()) &&
							LocalDate.parse(f.getUpdateDate().split("\\.")[0], DATE_TIME_FORMATTER).isEqual(date))
					.collect(Collectors.toSet()));
		}
		return (long) processedPlannedIssues.size();
	}

	private Long filterDataBasedOnDateAndTypeWise(Map<LocalDate, Set<JiraIssue>> allIssues,
			Map<LocalDate, Set<JiraIssue>> removedIssues, Map<LocalDate, Set<JiraIssue>> addedIssues,
			Set<JiraIssue> processedIssues, LocalDate date) {
		Set<JiraIssue> allIssuesOrDefault = allIssues.getOrDefault(date, new HashSet<>());
		Set<JiraIssue> removedJiraIssues = removedIssues.getOrDefault(date, new HashSet<>());
		Set<JiraIssue> addedJiraIssues = addedIssues.getOrDefault(date, new HashSet<>());
		// if an issue is present in both on the same day, then that should be nullified
		removedJiraIssues.removeAll(addedJiraIssues);

		allIssuesOrDefault.addAll(processedIssues);
		allIssuesOrDefault.addAll(addedJiraIssues);
		allIssuesOrDefault.removeAll(removedJiraIssues);
		processedIssues.addAll(allIssuesOrDefault);
		return (long) allIssuesOrDefault.size();

	}

	/**
	 * Used to create typeWiseCountMap
	 * 
	 * @param typeWiseAndDateMap
	 * @param issueTypeList
	 * @param date
	 * @return
	 */
	private Map<String, Long> filterDataBasedOnDateAndTypeWise(
			Map<String, Map<String, List<JiraIssue>>> typeWiseAndDateMap, Set<String> issueTypeList, LocalDate date) {
		Map<String, Long> typeMap = new HashMap<>();
		issueTypeList.forEach(type -> {
			Set<JiraIssue> ids = typeWiseAndDateMap.getOrDefault(type, new HashMap<>())
					.getOrDefault(date.toString(), new ArrayList<>()).stream().filter(Objects::nonNull)
					.collect(Collectors.toSet());
			typeMap.put(type, Long.valueOf(ids.size()));
		});
		issueTypeList.forEach(type -> typeMap.computeIfAbsent(type, val -> 0L));
		return typeMap;
	}

	@Override
	public Map<String, Long> calculateKpiValue(List<Map<String, Long>> valueList, String kpiName) {
		return calculateKpiValueForMap(valueList, kpiName);
	}

	/**
	 * with assignees criteria calculating potential delay for inprogress and open
	 * issues and without assignees calculating potential delay for inprogress
	 * stories
	 * 
	 * @param sprintDetails
	 * @param allIssues
	 * @param fieldMapping
	 * @return
	 */
	private List<IterationPotentialDelay> calculatePotentialDelay(SprintDetails sprintDetails,
			List<JiraIssue> allIssues, FieldMapping fieldMapping) {
		List<IterationPotentialDelay> iterationPotentialDelayList = new ArrayList<>();
		Map<String, List<JiraIssue>> assigneeWiseJiraIssue = allIssues.stream()
				.filter(jiraIssue -> jiraIssue.getAssigneeId() != null)
				.collect(Collectors.groupingBy(JiraIssue::getAssigneeId));

		if (MapUtils.isNotEmpty(assigneeWiseJiraIssue)) {
			assigneeWiseJiraIssue.forEach((assignee, jiraIssues) -> {
				List<JiraIssue> inProgressIssues = new ArrayList<>();
				List<JiraIssue> openIssues = new ArrayList<>();
				KpiDataHelper.arrangeJiraIssueList(fieldMapping, jiraIssues, inProgressIssues, openIssues);
				iterationPotentialDelayList
						.addAll(sprintWiseDelayCalculation(inProgressIssues, openIssues, sprintDetails));
			});
		}

		if (CollectionUtils.isNotEmpty(fieldMapping.getJiraStatusForInProgress())) {
			List<JiraIssue> inProgressIssues = allIssues.stream()
					.filter(jiraIssue -> (jiraIssue.getAssigneeId() == null)
							&& StringUtils.isNotEmpty(jiraIssue.getDueDate())
							&& (fieldMapping.getJiraStatusForInProgress().contains(jiraIssue.getStatus())))
					.collect(Collectors.toList());

			List<JiraIssue> openIssues = new ArrayList<>();
			iterationPotentialDelayList.addAll(sprintWiseDelayCalculation(inProgressIssues, openIssues, sprintDetails));
		}
		return iterationPotentialDelayList;
	}

	private DataCount getDataCountObject(Long value, String projectName, String label) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSProjectName(projectName);
		dataCount.setLineCategory(label);
		dataCount.setValue(value);
		return dataCount;
	}

}
