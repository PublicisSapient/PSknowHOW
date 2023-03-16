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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.*;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.*;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Daily Closures KPI gives a graphical representation of no. of issues planned to be closed each
 * day of iteration, actual count of issues closed day wise and
 * the predicted daily closures for the remaining days of the iteration.
 * {@link JiraKPIService}
 *
 * @author tauakram
 */
@Component
@Slf4j
public class DailyClosureServiceImpl extends JiraKPIService<Map<String, Long>, List<Object>, Map<String, Object>> {
	private static final String SPRINT = "sprint";
	public static final String UNCHECKED = "unchecked";
	public static final String TOTAL_ISSUE = "total issue";
	public static final String COMPLETED_ISSUE = "completed issue";
	public static final String ISSUES_PLANNED_TO_BE_CLOSED = "Issues planned to be closed";
	public static final String ISSUES_PREDICTED_TO_BE_CLOSED = "Issues predicted to be closed";
	public static final String ISSUES_CLOSED = "Issues closed";
	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	public static final String DATE = "date";

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueHistoryRepository;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getQualifierType() {
		return KPICode.DAILY_CLOSURES.name();
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
			log.info("Daily Closure -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId().toString();
			String sprintId = leafNode.getSprintFilter().getId();
			SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintId);
			if (null != sprintDetails) {
				List<String> totalIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.TOTAL_ISSUES);
				List<String> completedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.COMPLETED_ISSUES);
				if (CollectionUtils.isNotEmpty(totalIssues)) {
					List<JiraIssue> totalIssueList = jiraIssueRepository
							.findByNumberInAndBasicProjectConfigId(totalIssues, basicProjectConfigId);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getTotalIssues(), totalIssueList);
					resultListMap.put(TOTAL_ISSUE, new ArrayList<>(filtersIssuesList));
					resultListMap.put(SPRINT, sprintDetails);
				}
				if (CollectionUtils.isNotEmpty(completedIssues)) {
					List<JiraIssue> completedIssueList = jiraIssueRepository
							.findByNumberInAndBasicProjectConfigId(completedIssues, basicProjectConfigId);
					Set<JiraIssue> filtersCompletedIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getCompletedIssues(), completedIssueList);
					List<JiraIssueCustomHistory> completedJiraIssuesHistory = jiraIssueHistoryRepository
							.findByStoryIDInAndBasicProjectConfigIdIn(completedIssues,
									Arrays.asList(basicProjectConfigId));
					Map<String, String> activityMap = getClosedDate(completedJiraIssuesHistory, sprintDetails);
					filtersCompletedIssuesList.forEach(issue -> issue
							.setUpdateDate(activityMap.getOrDefault(issue.getNumber(), issue.getUpdateDate())));
					resultListMap.put(COMPLETED_ISSUE, new ArrayList<>(filtersCompletedIssuesList));
				}
			}
		}
		return resultListMap;
	}

	private Map<String, String> getClosedDate(List<JiraIssueCustomHistory> completedJiraIssuesHistory,
			SprintDetails sprintDetails) {
		Map<String, String> closedDateMap = new HashMap<>();
		completedJiraIssuesHistory.stream().forEach(jiraIssueCustomHistory -> {
			List<JiraIssueSprint> storySprintDetail = jiraIssueCustomHistory.getStorySprintDetails();
			SprintIssue sprintIssue = sprintDetails.getCompletedIssues().stream()
					.filter(s -> s.getNumber().equals(jiraIssueCustomHistory.getStoryID())).findFirst().get();
			if (CollectionUtils.isNotEmpty(storySprintDetail)) {
				for (int i = storySprintDetail.size() - 1; i >= 0; i--) {
					if (storySprintDetail.get(i).getFromStatus().equalsIgnoreCase(sprintIssue.getStatus())) {
						DateTime dateValue = DateTime.parse(storySprintDetail.get(i).getActivityDate().toString());
						DateTime startDateValue = DateTime.parse(sprintDetails.getStartDate());
						DateTime endDateValue = DateTime.parse(sprintDetails.getEndDate());
						if (dateValue.isAfter(startDateValue) && dateValue.isBefore(endDateValue)) {
							closedDateMap.put(jiraIssueCustomHistory.getStoryID(),
									storySprintDetail.get(i).getActivityDate().toString());
							break;
						}
					}
				}
			}
		});
		return closedDateMap;
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
		List<KPIExcelData> excelDataList = new ArrayList<>();

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);
		Object basicProjectConfigId = latestSprint.getProjectFilter().getBasicProjectConfigId();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(TOTAL_ISSUE);
		List<JiraIssue> allCompletedIssue = (List<JiraIssue>) resultMap.get(COMPLETED_ISSUE);
		SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT);
		if (CollectionUtils.isNotEmpty(allIssues)) {
			log.info("Daily Closures -> request id : {} total jira Issues : {}", requestTrackerId, allIssues.size());

			Map<String, Map<String, List<JiraIssue>>> typeWiseAndDueDateMap = allIssues.stream()
					.filter(f -> StringUtils.isNotBlank(f.getDueDate()))
					.collect(Collectors.groupingBy(JiraIssue::getTypeName, Collectors.groupingBy(f -> DateUtil
							.stringToLocalDate(f.getDueDate(), DateUtil.TIME_FORMAT_WITH_SEC).toString())));

			Map<String, Map<String, List<JiraIssue>>> typeWiseAndCompletedDateMap = new HashMap<>();
			if (CollectionUtils.isNotEmpty(allCompletedIssue)) {
				typeWiseAndCompletedDateMap = allCompletedIssue.stream()
						.filter(f -> StringUtils.isNotBlank(f.getUpdateDate()))
						.collect(Collectors.groupingBy(JiraIssue::getTypeName, Collectors.groupingBy(f -> LocalDate.parse
								(f.getUpdateDate().split("\\.")[0], DateTimeFormatter.ofPattern(DateUtil.TIME_FORMAT)).toString())));
			}
			List<IterationPotentialDelay> iterationPotentialDelayList = calculatePotentialDelay(sprintDetails,
					allIssues, fieldMapping);
			Map<String, IterationPotentialDelay> issueWiseDelay = iterationPotentialDelayList.stream()
					.collect(Collectors.toMap(IterationPotentialDelay::getIssueId, Function.identity(), (e1, e2) -> e2,
							LinkedHashMap::new));
			List<JiraIssue> allPotentialDelayIssue = allIssues.stream()
					.filter(i -> issueWiseDelay.containsKey(i.getNumber())).collect(Collectors.toList());
			Map<String, Map<String, List<JiraIssue>>> typeWiseAndPredictedDateMap = allPotentialDelayIssue.stream()
					.filter(f -> StringUtils.isNotBlank(issueWiseDelay.get(f.getNumber()).getPredictedCompletedDate()))
					.collect(Collectors.groupingBy(JiraIssue::getTypeName,
							Collectors.groupingBy(f -> issueWiseDelay.get(f.getNumber()).getPredictedCompletedDate())));

			LocalDate sprintStartDate = DateUtil.stringToLocalDate(sprintDetails.getStartDate(),
					DateUtil.TIME_FORMAT_WITH_SEC);
			LocalDate sprintEndDate = DateUtil
					.stringToLocalDate(sprintDetails.getEndDate(), DateUtil.TIME_FORMAT_WITH_SEC).plusDays(1);

			Set<String> issueTypeList = allIssues.stream().map(JiraIssue::getTypeName).collect(Collectors.toSet());
			Map<String, List<DataCount>> dataCountMap = new HashMap<>();
			for (LocalDate date = sprintStartDate; date.isBefore(sprintEndDate); date = date.plusDays(1)) {
				Map<String, Long> dueDateWiseTypeCountMap = filterDataBasedOnDateAndTypeWise(typeWiseAndDueDateMap,
						issueTypeList, date);
				Map<String, Long> predictedDateWiseTypeCountMap = filterDataBasedOnDateAndTypeWise(
						typeWiseAndPredictedDateMap, issueTypeList, date);
				Map<String, Long> completedDateWiseTypeCountMap = filterDataBasedOnDateAndTypeWise(
						typeWiseAndCompletedDateMap, issueTypeList, date);
				populateFilterWiseDataMap(dueDateWiseTypeCountMap, issueTypeList, dataCountMap, latestSprint.getId(),
						date.toString(), ISSUES_PLANNED_TO_BE_CLOSED);
				populateFilterWiseDataMap(predictedDateWiseTypeCountMap, issueTypeList, dataCountMap,
						latestSprint.getId(), date.toString(), ISSUES_PREDICTED_TO_BE_CLOSED);
				populateFilterWiseDataMap(completedDateWiseTypeCountMap, issueTypeList, dataCountMap,
						latestSprint.getId(), date.toString(), ISSUES_CLOSED);

			}
			List<DataCountGroup> dataCountGroups = new ArrayList<>();
			dataCountMap.forEach((issueType, typeWiseDc) -> {
				DataCountGroup dataCountGroup = new DataCountGroup();
				dataCountGroup.setFilter(issueType);
				dataCountGroup.setValue(typeWiseDc);
				dataCountGroups.add(dataCountGroup);
			});
			if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
				KPIExcelUtility.populateDailyClosureExcelData(excelDataList, allIssues, fieldMapping, issueWiseDelay,
						allCompletedIssue);
			}
			kpiElement.setTrendValueList(dataCountGroups);
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypeList);
			kpiElement.setFilters(new IterationKpiFilters(filter1, null));

		}
		excelDataList = reverseSortExcelDataList(excelDataList);
		kpiElement.setExcelData(excelDataList);
		kpiElement.setExcelColumns(KPIExcelColumn.DAILY_CLOSURES.getColumns());
	}

	/**
	 * Used to create typeWiseCountMap
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
	 * with assignees criteria calculating potential delay for inprogress and open issues and
	 * without assignees calculating potential delay for inprogress stories
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

	/**
	 *  Used to populate filter wise data
	 * @param typeCountMap
	 * @param typeList
	 * @param filterWiseDataMap
	 * @param projectNodeId
	 * @param date
	 * @param label
	 */
	private void populateFilterWiseDataMap(Map<String, Long> typeCountMap, Set<String> typeList,
			Map<String, List<DataCount>> filterWiseDataMap, String projectNodeId, String date, String label) {
		String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));
		Map<String, Long> finalMap = new HashMap<>();
		Map<String, Object> hoverValueMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(typeList)) {
			typeList.forEach(type -> {
				Long typeWiseCount = typeCountMap.getOrDefault(type, 0L);
				finalMap.put(org.apache.commons.lang.StringUtils.capitalize(type), typeWiseCount);
				hoverValueMap.put(org.apache.commons.lang.StringUtils.capitalize(type), typeWiseCount.intValue());
			});
			Long overAllCount = finalMap.values().stream().mapToLong(val -> val).sum();
			finalMap.put(CommonConstant.OVERALL, overAllCount);
		}

		finalMap.forEach((type, value) -> {
			DataCount dcObj = getDataCountObject(value, projectName, date, type, hoverValueMap, label);
			filterWiseDataMap.computeIfAbsent(type, k -> new ArrayList<>()).add(dcObj);
		});

	}

	/**
	 * particulate date format given as per date type
	 * @param value
	 * @param projectName
	 * @param date
	 * @param status
	 * @param overAllHoverValueMap
	 * @param label
	 * @return
	 */

	private DataCount getDataCountObject(Long value, String projectName, String date, String status,
			Map<String, Object> overAllHoverValueMap, String label) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSProjectName(projectName);
		dataCount.setDate(date);
		dataCount.setSubFilter(label);
		dataCount.setKpiGroup(status);
		dataCount.setGroupBy(DATE);
		Map<String, Object> hoverValueMap = new HashMap<>();
		if (status.equalsIgnoreCase(CommonConstant.OVERALL)) {
			dataCount.setHoverValue(overAllHoverValueMap);
		} else {
			hoverValueMap.put(status, value.intValue());
			dataCount.setHoverValue(hoverValueMap);
		}
		dataCount.setValue(value);
		return dataCount;
	}
	private List<KPIExcelData> reverseSortExcelDataList(List<KPIExcelData> excelDateList) {
		List<KPIExcelData> sortedExcelData = new ArrayList<>();
		sortedExcelData.addAll(org.apache.commons.collections4.CollectionUtils.emptyIfNull(excelDateList).stream()
				.filter(k -> StringUtils.isNotEmpty(k.getDueDate())
						&& !k.getDueDate().equalsIgnoreCase("-"))
				.sorted(Comparator.comparing(KPIExcelData::getDueDate))
				.collect(Collectors.toList()));
		sortedExcelData.addAll(org.apache.commons.collections4.CollectionUtils.emptyIfNull(excelDateList).stream()
				.filter(k -> StringUtils.isEmpty(k.getDueDate())
						|| k.getDueDate().equalsIgnoreCase("-"))
				.collect(Collectors.toList()));
		return sortedExcelData;

	}
}
