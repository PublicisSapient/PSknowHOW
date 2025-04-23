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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.publicissapient.kpidashboard.apis.util.IterationKpiHelper;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard.JiraIterationKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFilters;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFiltersOptions;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.util.IterationKpiHelper;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IterationCommitmentServiceImpl extends JiraIterationKPIService {

	public static final String UNCHECKED = "unchecked";
	public static final String OVERALL_COMMITMENT = "Overall Commitment";
	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String SEARCH_BY_PRIORITY = "Filter by status";
	private static final String PUNTED_ISSUES = "puntedIssues";
	private static final String ADDED_ISSUES = "addedIssues";
	private static final String EXCLUDE_ADDED_ISSUES = "excludeAddedIssues";
	private static final String SCOPE_ADDED = "Sprint scope added";
	private static final String SCOPE_REMOVED = "Sprint scope removed";
	private static final String SCOPE_CHANGE = "Scope Change";
	private static final String INITIAL_COMMITMENT = "Initial Commitment";
	private static final String OVERALL = "Overall";
	private static final String JIRA_ISSUE_HISTORY = "jiraIssueHistory";
	private static final String SPRINT_DETAILS = "sprintDetails";

	@Autowired
	private ConfigHelperService configHelperService;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node sprintNode)
			throws ApplicationException {
		DataCount trendValue = new DataCount();
		projectWiseLeafNodeValue(sprintNode, trendValue, kpiElement, kpiRequest);
		return kpiElement;
	}

	@Override
	public String getQualifierType() {
		return KPICode.ITERATION_COMMITMENT.name();
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		if (null != leafNode) {
			log.info("Scope Change -> Requested sprint : {}", leafNode.getName());
			SprintDetails dbSprintDetail = getSprintDetailsFromBaseClass();
			SprintDetails sprintDetails;
			if (null != dbSprintDetail) {
				resultListMap.put(SPRINT_DETAILS, dbSprintDetail);
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
						.get(leafNode.getProjectFilter().getBasicProjectConfigId());
				// to modify sprintdetails on the basis of configuration for the project
				List<JiraIssueCustomHistory> totalHistoryList = getJiraIssuesCustomHistoryFromBaseClass();
				List<JiraIssue> totalJiraIssueList = getJiraIssuesFromBaseClass();
				Set<String> issueList = totalJiraIssueList.stream().map(JiraIssue::getNumber)
						.collect(Collectors.toSet());
				resultListMap.put(JIRA_ISSUE_HISTORY, totalHistoryList);

				sprintDetails = IterationKpiHelper.transformIterSprintdetail(totalHistoryList, issueList, dbSprintDetail,
						fieldMapping.getJiraIterationIssuetypeKPI120(),
						fieldMapping.getJiraIterationCompletionStatusKPI120(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> puntedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.PUNTED_ISSUES);
				Set<String> addedIssues = sprintDetails.getAddedIssues();
				List<String> completeAndIncompleteIssues = Stream
						.of(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
								CommonConstant.COMPLETED_ISSUES),
								KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
										CommonConstant.NOT_COMPLETED_ISSUES))
						.flatMap(Collection::stream).collect(Collectors.toList());
				// Adding issues which were added before sprint start and later removed form
				// sprint or dropped.
				completeAndIncompleteIssues.addAll(puntedIssues);
				if (CollectionUtils.isNotEmpty(puntedIssues)) {
					List<JiraIssue> filteredPuntedIssueList = IterationKpiHelper.getFilteredJiraIssue(puntedIssues, totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getPuntedIssues(), filteredPuntedIssueList);
					resultListMap.put(PUNTED_ISSUES, new ArrayList<>(filtersIssuesList));
				}
				if (CollectionUtils.isNotEmpty(addedIssues)) {
					List<JiraIssue> filterAddedIssueList = IterationKpiHelper.getFilteredJiraIssue(new ArrayList<>(addedIssues),
							totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails, new HashSet<>(),
									filterAddedIssueList);
					resultListMap.put(ADDED_ISSUES, new ArrayList<>(filtersIssuesList));
					completeAndIncompleteIssues.removeAll(new ArrayList<>(addedIssues));
				}
				if (CollectionUtils.isNotEmpty(completeAndIncompleteIssues)) {
					List<JiraIssue> filteredJiraIssue = IterationKpiHelper.getFilteredJiraIssue(
							new ArrayList<>(completeAndIncompleteIssues), totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails, new HashSet<>(),
									filteredJiraIssue);
					resultListMap.put(EXCLUDE_ADDED_ISSUES, new ArrayList<>(filtersIssuesList));
				}
			}
		}
		return resultListMap;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint level.
	 * 
	 * @param sprintLeafNode
	 * @param trendValue
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(Node sprintLeafNode, DataCount trendValue, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		Object basicProjectConfigId = sprintLeafNode.getProjectFilter().getBasicProjectConfigId();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNode, null, null, kpiRequest);
		List<JiraIssue> puntedIssues = (List<JiraIssue>) resultMap.get(PUNTED_ISSUES);
		List<JiraIssue> addedIssues = (List<JiraIssue>) resultMap.get(ADDED_ISSUES);
		List<JiraIssue> initialIssues = (List<JiraIssue>) resultMap.get(EXCLUDE_ADDED_ISSUES);
		List<JiraIssueCustomHistory> issueHistory = (List<JiraIssueCustomHistory>) resultMap.get(JIRA_ISSUE_HISTORY);
		SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT_DETAILS);
		List<JiraIssue> totalIssues = new ArrayList<>();
		List<IterationKpiModalValue> overAllAddmodalValues = new ArrayList<>();
		List<IterationKpiModalValue> overAllRemovedmodalValues = new ArrayList<>();
		List<IterationKpiModalValue> overAllInitialmodalValues = new ArrayList<>();
		List<IterationKpiModalValue> overAllTotalmodalValues = new ArrayList<>();
		List<IterationKpiModalValue> overAllScopeChangemodalValues = new ArrayList<>();

		List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
		List<IterationKpiData> data = new ArrayList<>();
		// for totalIssue adding initialIssues + addedIssues - puntedIssues
		if (CollectionUtils.isNotEmpty(initialIssues)) {
			totalIssues.addAll(initialIssues);
		}
		if (CollectionUtils.isNotEmpty(addedIssues)) {
			totalIssues.addAll(addedIssues);
		}
		Set<String> issueTypes = totalIssues.stream().map(JiraIssue::getTypeName).collect(Collectors.toSet());
		Set<String> statuses = totalIssues.stream().map(JiraIssue::getStatus).collect(Collectors.toSet());
		if (CollectionUtils.isNotEmpty(puntedIssues)) {
			totalIssues.removeAll(puntedIssues);
		}

		List<JiraIssue> scopeChangeIssues = Stream
				.concat(Stream.concat(CollectionUtils.emptyIfNull(initialIssues).stream(),
						CollectionUtils.emptyIfNull(addedIssues).stream()),
						Stream.concat(CollectionUtils.emptyIfNull(puntedIssues).stream(),
								CollectionUtils.emptyIfNull(totalIssues).stream()))
				.filter(j -> Objects.nonNull(j.getLabels()) && Objects.nonNull(fieldMapping.getJiraLabelsKPI120())
						&& j.getLabels().stream().anyMatch(fieldMapping.getJiraLabelsKPI120()::contains))
				.distinct().collect(Collectors.toList());

		Map<String, JiraIssueCustomHistory> issuesByNumber = issueHistory.stream()
				.collect(Collectors.toMap(JiraIssueCustomHistory::getStoryID, issue -> issue, (existing, replacement) -> existing));

		if (CollectionUtils.isNotEmpty(totalIssues)) {
			log.info("Scope Change -> request id : {} total jira Issues : {}", requestTrackerId, totalIssues.size());
			List<Integer> overAllTotalIssueCount = Arrays.asList(0);
			List<Double> overAllTotalIssueSp = Arrays.asList(0.0);
			List<Double> overAllTotalOriginalEstimate = Arrays.asList(0.0);
			Map<JiraIssue, JiraIssueCustomHistory> totalJiraIssueMap = new HashMap<>();
			totalIssues.stream().filter(
					issue -> StringUtils.isNotEmpty(issue.getNumber()) && issuesByNumber.containsKey(issue.getNumber()))
					.forEach(issue -> totalJiraIssueMap.put(issue, issuesByNumber.get(issue.getNumber())));

			setScopeChange(iterationKpiValues, overAllTotalIssueCount,
					overAllTotalIssueSp, overAllTotalmodalValues, OVERALL_COMMITMENT, fieldMapping,
					overAllTotalOriginalEstimate, totalJiraIssueMap, sprintDetails, null);
			IterationKpiData overAllTotalCount = setIterationKpiData(fieldMapping, overAllTotalIssueCount,
					overAllTotalIssueSp, overAllTotalOriginalEstimate, overAllTotalmodalValues, OVERALL_COMMITMENT, null);
			data.add(overAllTotalCount);
		}

		if (CollectionUtils.isNotEmpty(initialIssues)) {
			log.info("Scope Change -> request id : {} initial jira Issues : {}", requestTrackerId,
					initialIssues.size());
			List<Integer> overAllInitialIssueCount = Arrays.asList(0);
			List<Double> overAllInitialIssueSp = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimate = Arrays.asList(0.0);
			Map<JiraIssue, JiraIssueCustomHistory> initialJiraIssueMap = new HashMap<>();
			initialIssues.stream().filter(
							issue -> StringUtils.isNotEmpty(issue.getNumber()) && issuesByNumber.containsKey(issue.getNumber()))
					.forEach(issue -> initialJiraIssueMap.put(issue, issuesByNumber.get(issue.getNumber())));

			setScopeChange(iterationKpiValues, overAllInitialIssueCount,
					overAllInitialIssueSp, overAllInitialmodalValues, INITIAL_COMMITMENT, fieldMapping,
					overAllOriginalEstimate, initialJiraIssueMap, sprintDetails, null);
			IterationKpiData overAllInitialCount = setIterationKpiData(fieldMapping, overAllInitialIssueCount,
					overAllInitialIssueSp, overAllOriginalEstimate, overAllInitialmodalValues, INITIAL_COMMITMENT, null);
			data.add(overAllInitialCount);
		}

		if (CollectionUtils.isNotEmpty(addedIssues)) {
			log.info("Scope Change -> request id : {} added jira Issues : {}", requestTrackerId, addedIssues.size());
			List<Integer> overAllAddedIssueCount = Arrays.asList(0);
			List<Double> overAllAddedIssueSp = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimate = Arrays.asList(0.0);
			Map<Long, Pair<Integer, Double>> dayWiseScopeUpdate = new HashMap<>();
			Map<JiraIssue, JiraIssueCustomHistory> addedJiraIssueMap = new HashMap<>();
			addedIssues.stream().filter(
							issue -> StringUtils.isNotEmpty(issue.getNumber()) && issuesByNumber.containsKey(issue.getNumber()))
					.forEach(issue -> addedJiraIssueMap.put(issue, issuesByNumber.get(issue.getNumber())));
			setScopeChange(iterationKpiValues, overAllAddedIssueCount,
					overAllAddedIssueSp, overAllAddmodalValues, SCOPE_ADDED, fieldMapping, overAllOriginalEstimate,
					addedJiraIssueMap, sprintDetails, dayWiseScopeUpdate);
			IterationKpiData overAllAddedCount = setIterationKpiData(fieldMapping, overAllAddedIssueCount,
					overAllAddedIssueSp, overAllOriginalEstimate, overAllAddmodalValues, SCOPE_ADDED, dayWiseScopeUpdate);

			data.add(overAllAddedCount);
		}

		if (CollectionUtils.isNotEmpty(puntedIssues)) {
			log.info("Scope Change -> request id : {} punted jira Issues : {}", requestTrackerId, puntedIssues.size());
			List<Integer> overAllPunIssueCount = Arrays.asList(0);
			List<Double> overAllPunIssueSp = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimate = Arrays.asList(0.0);
			Map<Long, Pair<Integer, Double>> dayWiseScopeUpdate = new HashMap<>();
			Map<JiraIssue, JiraIssueCustomHistory> puntedJiraIssueMap = new HashMap<>();
			puntedIssues.stream().filter(
							issue -> StringUtils.isNotEmpty(issue.getNumber()) && issuesByNumber.containsKey(issue.getNumber()))
					.forEach(issue -> puntedJiraIssueMap.put(issue, issuesByNumber.get(issue.getNumber())));
			setScopeChange(iterationKpiValues, overAllPunIssueCount,
					overAllPunIssueSp, overAllRemovedmodalValues, SCOPE_REMOVED, fieldMapping, overAllOriginalEstimate,
					puntedJiraIssueMap, sprintDetails, dayWiseScopeUpdate);
			IterationKpiData overAllPuntedCount = setIterationKpiData(fieldMapping, overAllPunIssueCount,
					overAllPunIssueSp, overAllOriginalEstimate, overAllRemovedmodalValues, SCOPE_REMOVED, dayWiseScopeUpdate);
			data.add(overAllPuntedCount);
		}

		if (CollectionUtils.isNotEmpty(scopeChangeIssues)) {
			log.info("Scope Change -> request id : {} scope change labels jira Issues : {}", requestTrackerId, scopeChangeIssues.size());
			List<Integer> overAllScopeChangeLabelsCount = Arrays.asList(0);
			List<Double> overAllScopeChangeLabelsSp = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimate = Arrays.asList(0.0);
			Map<Long, Pair<Integer, Double>> dayWiseScopeUpdate = new HashMap<>();
			Map<JiraIssue, JiraIssueCustomHistory> scopeChangeJiraIssueMap = new HashMap<>();
			scopeChangeIssues.stream().filter(
					issue -> StringUtils.isNotEmpty(issue.getNumber()) && issuesByNumber.containsKey(issue.getNumber()))
					.forEach(issue -> scopeChangeJiraIssueMap.put(issue, issuesByNumber.get(issue.getNumber())));
			setScopeChange(iterationKpiValues, overAllScopeChangeLabelsCount, overAllScopeChangeLabelsSp,
					overAllScopeChangemodalValues, SCOPE_CHANGE, fieldMapping, overAllOriginalEstimate,
					scopeChangeJiraIssueMap, sprintDetails, dayWiseScopeUpdate);
			IterationKpiData overAllScopeChangeLabels = setIterationKpiData(fieldMapping, overAllScopeChangeLabelsCount,
					overAllScopeChangeLabelsSp, overAllOriginalEstimate, overAllScopeChangemodalValues, SCOPE_CHANGE,
					dayWiseScopeUpdate);
			data.add(overAllScopeChangeLabels);
		}

		if (CollectionUtils.isNotEmpty(data)) {
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
			iterationKpiValues.add(overAllIterationKpiValue);

			// Create kpi level filters
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypes);
			IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, statuses);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
			trendValue.setValue(iterationKpiValues);
			kpiElement.setFilters(iterationKpiFilters);
			kpiElement.setSprint(sprintLeafNode.getName());
			kpiElement.setModalHeads(KPIExcelColumn.ITERATION_COMMITMENT.getColumns());
		}
		kpiElement.setTrendValueList(trendValue);
	}

	private void setScopeChange(List<IterationKpiValue> iterationKpiValues, List<Integer> overAllIssueCount,
			List<Double> overAllIssueSp, List<IterationKpiModalValue> overAllmodalValues, String label,
			FieldMapping fieldMapping, List<Double> overAllOriginalEstimate,
			Map<JiraIssue, JiraIssueCustomHistory> jiraIssueMap, SprintDetails sprintDetails,
			Map<Long, Pair<Integer, Double>> overallDayWiseScopeUpdate) {

		List<JiraIssue> allIssues = new ArrayList<>(jiraIssueMap.keySet());
		Map<String, Map<String, List<JiraIssue>>> typeAndStatusWiseIssues = allIssues.stream()
				.collect(Collectors.groupingBy(JiraIssue::getTypeName, Collectors.groupingBy(JiraIssue::getStatus)));

		// Creating map of modal Objects
		Map<String, IterationKpiModalValue> modalObjectMap = KpiDataHelper.createMapOfModalObject(allIssues);
		typeAndStatusWiseIssues.forEach((issueType, statusWiseIssue) -> statusWiseIssue.forEach((status, issues) -> {
			List<IterationKpiModalValue> modalValues = new ArrayList<>();
			int issueCount = 0;
			double storyPoints = 0;
			Double originalEstimate = 0.0;
			Map<Long, Pair<Integer, Double>> dayWiseScopeUpdate = new HashMap<>();
			for (JiraIssue jiraIssue : issues) {
				setDayWiseScopeChange(jiraIssue, jiraIssueMap.get(jiraIssue), sprintDetails, label,
						dayWiseScopeUpdate, fieldMapping);
				KPIExcelUtility.populateIterationKPI(overAllmodalValues, modalValues, jiraIssue, fieldMapping,
						modalObjectMap);
				issueCount = issueCount + 1;
				if (null != jiraIssue.getStoryPoints()) {
					storyPoints = storyPoints + jiraIssue.getStoryPoints();
					overAllIssueSp.set(0, overAllIssueSp.get(0) + jiraIssue.getStoryPoints());
				}
				if (null != jiraIssue.getOriginalEstimateMinutes()) {
					originalEstimate = originalEstimate + jiraIssue.getOriginalEstimateMinutes();
					overAllOriginalEstimate.set(0,
							overAllOriginalEstimate.get(0) + jiraIssue.getOriginalEstimateMinutes());
				}
				overAllIssueCount.set(0, overAllIssueCount.get(0) + 1);
			}
			List<IterationKpiData> data = new ArrayList<>();
			IterationKpiData issueCounts;
			List<String> additionalInfo = getKpiDataExpressions(overallDayWiseScopeUpdate, dayWiseScopeUpdate, fieldMapping);

			if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
					&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
				issueCounts = new IterationKpiData(label, (double) issueCount, roundingOff(storyPoints),
						"", CommonConstant.SP, modalValues, additionalInfo);
			} else {
				issueCounts = new IterationKpiData(label, (double) issueCount, roundingOff(originalEstimate), "",
						CommonConstant.DAY, modalValues, additionalInfo);
			}
			data.add(issueCounts);
			IterationKpiValue matchingObject = iterationKpiValues.stream()
					.filter(p -> p.getFilter1().equals(issueType) && p.getFilter2().equals(status)).findAny()
					.orElse(null);
			if (null == matchingObject) {
				IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, status, data);
				iterationKpiValues.add(iterationKpiValue);
			} else {
				matchingObject.getData().addAll(data);
			}
		}));
	}

	/**
	 * Updates the day-wise scope change for a given Jira issue based on its history
	 * and sprint details.
	 *
	 * @param jiraIssue
	 *            The Jira issue for which the scope change is being calculated.
	 * @param issueCustomHistory
	 *            The custom history of the Jira issue, containing sprint update
	 *            logs.
	 * @param sprintDetails
	 *            The details of the sprint, including its name, start date, and end
	 *            date.
	 * @param label
	 *            The label indicating whether the scope is being added or removed.
	 *            Possible values: SCOPE_ADDED, SCOPE_REMOVED.
     * @param fieldMapping
 	 *            field mapping configuration, used to determine the labels
	 * @param dayWiseScopeUpdate
	 *            A map where the key represents a time duration (in days) and the
	 *            value is a pair containing the count of issues and the total story
	 *            points for that duration.
	 */
	private void setDayWiseScopeChange(JiraIssue jiraIssue, JiraIssueCustomHistory issueCustomHistory,
			SprintDetails sprintDetails, String label, Map<Long, Pair<Integer, Double>> dayWiseScopeUpdate,
			FieldMapping fieldMapping) {
		List<JiraHistoryChangeLog> sprintUpdationLog = new ArrayList<>();
		LocalDate sprintStartDate = DateUtil.stringToLocalDate(sprintDetails.getStartDate(),
				DateUtil.TIME_FORMAT_WITH_SEC_ZONE);
		LocalDate sprintEndDate = DateUtil.stringToLocalDate(
				sprintDetails.getCompleteDate() != null ? sprintDetails.getCompleteDate() : sprintDetails.getEndDate(),
				DateUtil.TIME_FORMAT_WITH_SEC_ZONE);
		long sprintDuration = ChronoUnit.DAYS.between(sprintStartDate, sprintEndDate);
		long quarterSprint = (long) Math.ceil(0.25 * sprintDuration);
		long halfSprint = (long) Math.ceil(0.5 * sprintDuration);
		long threeQuarterSprint = (long) Math.ceil(0.75 * sprintDuration);
		if (issueCustomHistory != null) {
			sprintUpdationLog = switch (label) {
			case SCOPE_ADDED -> issueCustomHistory.getSprintUpdationLog().stream()
					.filter(updationLog -> updationLog.getChangedTo().equalsIgnoreCase(sprintDetails.getSprintName()))
					.toList();
			case SCOPE_REMOVED -> issueCustomHistory.getSprintUpdationLog().stream()
					.filter(updationLog -> updationLog.getChangedFrom().equalsIgnoreCase(sprintDetails.getSprintName()))
					.toList();
			case SCOPE_CHANGE -> issueCustomHistory.getLabelUpdationLog().stream()
					.filter(updationLog -> fieldMapping.getJiraLabelsKPI120().contains(updationLog.getChangedTo()))
					.toList();
			default -> sprintUpdationLog;
			};
		}

		if (CollectionUtils.isNotEmpty(sprintUpdationLog)) {
			sprintUpdationLog.forEach(updationLog -> {
				LocalDate sprintDate = updationLog.getUpdatedOn().toLocalDate();
				if (sprintDate.isAfter(sprintEndDate.minusDays(quarterSprint)) && sprintDate.isBefore(sprintEndDate)) {
					updateScopeCounts(dayWiseScopeUpdate, quarterSprint, jiraIssue, fieldMapping);
				} else if (sprintDate.isAfter(sprintEndDate.minusDays(halfSprint))
						&& sprintDate.isBefore(sprintEndDate)) {
					updateScopeCounts(dayWiseScopeUpdate, halfSprint, jiraIssue, fieldMapping);
				} else if (sprintDate.isAfter(sprintStartDate.plusDays(threeQuarterSprint))
						&& sprintDate.isBefore(sprintEndDate)) {
					updateScopeCounts(dayWiseScopeUpdate, threeQuarterSprint, jiraIssue, fieldMapping);
				}
			});
		}
	}

	/**
	 * Updates the day-wise scope change map with the count of issues and total
	 * story points.
	 *
	 * @param dayWiseScopeUpdate
	 *            A map where the key represents a time duration (in days) and the
	 *            value is a pair containing the count of issues and the total story
	 *            points for that duration.
	 * @param timeKey
	 *            The time key (in days) for which the scope counts are being
	 *            updated.
	 * @param jiraIssue
	 *            The Jira issue containing the story points or original estimate
	 * @param fieldMapping
	 *            The field mapping configuration, used to determine the estimation
	 *            criteria.
	 */
	private void updateScopeCounts(Map<Long, Pair<Integer, Double>> dayWiseScopeUpdate, Long timeKey,
			JiraIssue jiraIssue, FieldMapping fieldMapping) {

		Pair<Integer, Double> currentValue = dayWiseScopeUpdate.getOrDefault(timeKey, Pair.of(0, 0.0));
		double storyPoint = 0.0d;
		if(StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			storyPoint = jiraIssue.getStoryPoints() != null ? jiraIssue.getStoryPoints() : storyPoint;
		} else {
			storyPoint = jiraIssue.getOriginalEstimateMinutes() != null ? jiraIssue.getOriginalEstimateMinutes()
					: storyPoint;
		}
		int updatedCount = currentValue.getLeft() + 1;
		double updatedPoints = currentValue.getRight() + storyPoint;
		dayWiseScopeUpdate.put(timeKey, Pair.of(updatedCount, updatedPoints));
	}

	/**
	 * Generates a list of formatted KPI data expressions based on day-wise scope
	 * updates.
	 *
	 * @param overallDayWiseScopeUpdate
	 *            A map where the key represents a time duration (in days) and the
	 *            value is a pair of count and sp
	 * @param dayWiseScopeUpdate
	 *            A map where the key represents a time duration (in days) and the
	 * 	 *            value is a pair of count and sp
	 * @param fieldMapping
	 *            The field mapping configuration, used to determine the estimation
	 *            criteria
	 * @return A list of formatted strings representing the KPI data expressions for
	 *         each time duration
	 */
	private List<String> getKpiDataExpressions(Map<Long, Pair<Integer, Double>> overallDayWiseScopeUpdate,
			Map<Long, Pair<Integer, Double>> dayWiseScopeUpdate, FieldMapping fieldMapping) {
		List<String> additionalInfo = new ArrayList<>();
		if (overallDayWiseScopeUpdate != null) {
			dayWiseScopeUpdate.forEach((duration, countPair) -> {
				Pair<Integer, Double> overallValue = overallDayWiseScopeUpdate.getOrDefault(duration, Pair.of(0, 0.0));
				overallDayWiseScopeUpdate.put(duration, Pair.of(overallValue.getLeft() + countPair.getLeft(),
						overallValue.getRight() + countPair.getRight()));
				String format = StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)
								? "Last %d days: %d/%,.1f SP"
								: "Last %d days: %d/%,.1f days";
				additionalInfo.add(String.format(format, duration, countPair.getLeft(), countPair.getRight()));
			});
		}
		return additionalInfo;
	}

	private IterationKpiData setIterationKpiData(FieldMapping fieldMapping, List<Integer> overAllIssueCount,
			List<Double> overAllIssueSp, List<Double> overAllOriginalEstimate,
			List<IterationKpiModalValue> overAllModalValues, String kpiLabel, Map<Long, Pair<Integer, Double>> dayWiseScopeUpdate) {
		List<String> additionalInfo = new ArrayList<>();
		if (dayWiseScopeUpdate != null) {
			dayWiseScopeUpdate.forEach((duration, countPair) -> {
				String format = StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)
						? "Last %d days: %d/%,.1f SP"
						: "Last %d days: %d/%,.1f days";
				additionalInfo.add(String.format(format, duration, countPair.getLeft(), countPair.getRight()));
			});
		}
		if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			return new IterationKpiData(kpiLabel, Double.valueOf(overAllIssueCount.get(0)),
					roundingOff(overAllIssueSp.get(0)), "", CommonConstant.SP, overAllModalValues, additionalInfo);
		} else {
			return new IterationKpiData(kpiLabel, Double.valueOf(overAllIssueCount.get(0)),
					roundingOff(overAllOriginalEstimate.get(0)), "", CommonConstant.DAY, overAllModalValues,
					additionalInfo);
		}
	}

}
