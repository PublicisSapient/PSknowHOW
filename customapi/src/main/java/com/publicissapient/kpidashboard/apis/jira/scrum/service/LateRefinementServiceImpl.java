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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard.JiraIterationKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.util.IterationKpiHelper;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

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
public class LateRefinementServiceImpl extends JiraIterationKPIService {
	public static final String UNCHECKED = "unchecked";
	public static final String DATE = "date";
	public static final String FULL_SPRINT_ISSUES = "Full Sprint Issues";
	private static final String SPRINT = "sprint";
	private static final String ISSUES = "issues";
	private static final String LATE_REFINED = "Late Refined";
	private static final String LEGEND = "Unrefined scope";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Autowired
	private ConfigHelperService configHelperService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getQualifierType() {
		return KPICode.LATE_REFINEMENT.name();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node sprintNode)
			throws ApplicationException {
		sprintWiseLeafNodeValue(sprintNode, kpiElement, kpiRequest);
		return kpiElement;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, final String startDate, final String endDate,
			final KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
        if (leafNode == null)
            return resultListMap;

        log.info("Late Refinement -> Requested sprint : {}", leafNode.getName());

        SprintDetails sprintDetails = getSprintDetailsFromBaseClass();
        if (sprintDetails == null)
            return resultListMap;

        FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
                .get(leafNode.getProjectFilter().getBasicProjectConfigId());


		List<JiraIssueCustomHistory> totalHistoryList = getJiraIssuesCustomHistoryFromBaseClass();
		List<JiraIssue> totalJiraIssueList = getJiraIssuesFromBaseClass();

        // filter by typeName and DOR statuses as these both are mandatory fields
        if (CollectionUtils.isNotEmpty(fieldMapping.getJiraIssueTypeNamesKPI187())
                && CollectionUtils.isNotEmpty(fieldMapping.getJiraStatusKPI187())) {
            Set<String> typeName = getTypeNames(fieldMapping);
            totalJiraIssueList = totalJiraIssueList.stream()
                    .filter(jiraIssue -> typeName.contains(jiraIssue.getTypeName().trim().toLowerCase())).toList();
            Set<String> jiraIssueNumber = totalJiraIssueList.stream().map(JiraIssue::getNumber)
                    .collect(Collectors.toSet());
            totalHistoryList = totalHistoryList.stream()
                    .filter(history -> jiraIssueNumber.contains(history.getStoryID())).toList();


			// there is no need to modify sprintdetails, as there is no check for the
			// completion status in the kpi
			List<String> allIssues = new ArrayList<>();
			List<String> notCompleted = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
					CommonConstant.NOT_COMPLETED_ISSUES);
			List<String> completed = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
					CommonConstant.COMPLETED_ISSUES);
			allIssues.addAll(notCompleted);
			allIssues.addAll(completed);
			allIssues.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
					CommonConstant.ADDED_ISSUES));
			allIssues.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
					CommonConstant.PUNTED_ISSUES));

            if (CollectionUtils.isNotEmpty(allIssues)) {
                Set<SprintIssue> sprintIssues = new HashSet<>();
                sprintIssues.addAll(checkNullList(sprintDetails.getTotalIssues()));
                sprintIssues.addAll(checkNullList(sprintDetails.getPuntedIssues()));
                Set<JiraIssue> totalIssueList = KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(
                        sprintDetails, sprintIssues,
                        IterationKpiHelper.getFilteredJiraIssue(allIssues, totalJiraIssueList));
                List<JiraIssueCustomHistory> allIssuesHistory = IterationKpiHelper
                        .getFilteredJiraIssueHistory(allIssues, totalHistoryList);
                Map<String, JiraIssueCustomHistory> historyMap = new HashMap<>();
                Map<LocalDate, List<JiraIssue>> fullSprintIssues = new HashMap<>();
                Map<LocalDate, List<JiraIssue>> addedIssues = new HashMap<>();
                Map<LocalDate, List<JiraIssue>> removedIssues = new HashMap<>();
                Map<LocalDate, List<JiraIssue>> lateRefined = createLateRefinedMap(sprintDetails);
                allIssuesHistory.forEach(issueHistory -> {
                    historyMap.put(issueHistory.getStoryID(), issueHistory);
                    if (CollectionUtils.isNotEmpty(issueHistory.getSprintUpdationLog())) {
                        List<JiraHistoryChangeLog> sprintUpdationLog = issueHistory.getSprintUpdationLog();
                        List<JiraHistoryChangeLog> statusUpdationLog = issueHistory.getStatusUpdationLog();
                        Collections.sort(sprintUpdationLog, Comparator.comparing(getGetUpdatedOn()));
                        Collections.sort(statusUpdationLog, Comparator.comparing(JiraHistoryChangeLog::getUpdatedOn));
                        createAddedandRemovedIssueDateWiseMap(sprintDetails, totalIssueList, addedIssues, removedIssues,
                                fullSprintIssues, issueHistory, sprintUpdationLog);
                        createDateWiseRefinementMap(totalIssueList, issueHistory, statusUpdationLog, fieldMapping,
                                lateRefined, sprintDetails);
                    }
                });

				resultListMap.put(FULL_SPRINT_ISSUES, fullSprintIssues);
				resultListMap.put(CommonConstant.PUNTED_ISSUES, removedIssues);
				resultListMap.put(CommonConstant.ADDED_ISSUES, addedIssues);
				resultListMap.put(SPRINT, sprintDetails);
				resultListMap.put(ISSUES, totalIssueList);
				resultListMap.put(LATE_REFINED, lateRefined);
			}
		}

		return resultListMap;
	}

	private static Set<String> getTypeNames(FieldMapping fieldMapping) {
		return fieldMapping.getJiraIssueTypeNamesKPI187().stream()
				.flatMap(name -> "Defect".equalsIgnoreCase(name)
						? Stream.of("defect", NormalizedJira.DEFECT_TYPE.getValue().toLowerCase())
						: Stream.of(name.trim().toLowerCase()))
				.collect(Collectors.toSet());
	}

	private static Function<JiraHistoryChangeLog, LocalDateTime> getGetUpdatedOn() {
		return JiraHistoryChangeLog::getUpdatedOn;
	}

	private Map<LocalDate, List<JiraIssue>> createLateRefinedMap(SprintDetails sprintDetails) {
		LocalDate startDate = DateUtil.convertingStringToLocalDateTime(sprintDetails.getStartDate(),
				DateUtil.TIME_FORMAT).toLocalDate();
		LocalDate sprintEndDate = DateUtil.convertingStringToLocalDateTime(sprintDetails.getEndDate(),
				DateUtil.TIME_FORMAT).toLocalDate();
		Map<LocalDate, List<JiraIssue>> lateRefinementMap = new HashMap<>();
		while (!startDate.isAfter(sprintEndDate)) {
			lateRefinementMap.put(startDate, new ArrayList<>());
			startDate = startDate.plusDays(1);
		}
		return lateRefinementMap;

	}

	private void createDateWiseRefinementMap(Set<JiraIssue> totalIssueList, JiraIssueCustomHistory issueHistory,
			List<JiraHistoryChangeLog> statusUpdationLog, FieldMapping fieldMapping,
			Map<LocalDate, List<JiraIssue>> lateRefined, SprintDetails sprintDetails) {
		LocalDateTime startDate = DateUtil.stringToLocalDateTime(sprintDetails.getStartDate(), DateUtil.TIME_FORMAT_WITH_SEC);
		final LocalDateTime currentDate = DateUtil.getTodayTime();


		// Get relevant issues
		List<JiraIssue> jiraIssueList = new ArrayList<>(getRespectiveJiraIssue(totalIssueList, issueHistory));
		if (jiraIssueList.isEmpty()) {
			return;
		}
		// Handle refined status case
		LocalDate maxDate = calculateMaxDateForRefinedStatus(statusUpdationLog, fieldMapping);
		if (DateUtil.isWithinDateTimeRange(maxDate.atStartOfDay(), startDate, currentDate)) {
			updateLateRefinedMap(startDate.toLocalDate(), maxDate, lateRefined, jiraIssueList);
		}

	}

	/**
	 * Calculates the maximum date for refined status based on status update logs
	 * 
	 * @param statusUpdationLog
	 *            List of status update logs
	 * @param fieldMapping
	 *            Field mapping configuration
	 * @return LocalDate representing the maximum date
	 */
	private LocalDate calculateMaxDateForRefinedStatus(List<JiraHistoryChangeLog> statusUpdationLog,
			FieldMapping fieldMapping) {
		Set<String> statusBeforeDOR = fieldMapping.getJiraStatusKPI187().stream()
				.map(status -> status.trim().toLowerCase()).collect(Collectors.toSet());

		boolean startDateFound = false;
		LocalDate maxDate = DateUtil.getTodayDate();

		for (JiraHistoryChangeLog updateLog : statusUpdationLog) {
			String changedStatus = updateLog.getChangedTo().trim().toLowerCase();
			if (!startDateFound && statusBeforeDOR.contains(changedStatus)) {
				startDateFound = true;
			} else if (startDateFound && !statusBeforeDOR.contains(changedStatus)) {
				maxDate = updateLog.getUpdatedOn().toLocalDate().minusDays(1);
				break;
			}
		}

		return maxDate;
	}

	private void updateLateRefinedMap(LocalDate startDate, LocalDate maxDate,
			Map<LocalDate, List<JiraIssue>> lateRefined, List<JiraIssue> jiraIssueList) {
		while (!startDate.isAfter(maxDate)) {
			lateRefined.computeIfPresent(startDate, (k, v) -> {
				v.addAll(jiraIssueList);
				return v;
			});
			startDate = startDate.plusDays(1);
		}
	}

	private Set<SprintIssue> checkNullList(Set<SprintIssue> totalIssues) {
		return CollectionUtils.isNotEmpty(totalIssues) ? totalIssues : new HashSet<>();
	}

	/*
	 * if last index of sprintUpdateLog is selected sprint then it means, the issue
	 * was present throughout put that issue in full-Sprint map and that list will
	 * be the base list. while all the other dynamics within sprint will be stored
	 * in AddedIssuesMap within sprint duration if an issue is removed then to be
	 * maintained in removedIssues map
	 */
	private void createAddedandRemovedIssueDateWiseMap(SprintDetails sprintDetails, Set<JiraIssue> totalIssueList,
			Map<LocalDate, List<JiraIssue>> addedIssuesMap, Map<LocalDate, List<JiraIssue>> removedIssues,
			Map<LocalDate, List<JiraIssue>> fullSprintMap, JiraIssueCustomHistory issueHistory,
			List<JiraHistoryChangeLog> sprintUpdationLog) {
		LocalDateTime startDateTime = DateUtil.stringToLocalDateTime(sprintDetails.getStartDate(), DateUtil.TIME_FORMAT_WITH_SEC);
		LocalDateTime sprintEndTime = DateUtil.getTodayTime();
		List<JiraIssue> jiraIssueList = new ArrayList<>(getRespectiveJiraIssue(totalIssueList, issueHistory));
		int lastIndex = sprintUpdationLog.size() - 1;
		sprintUpdationLog.stream()
				.filter(updateLogs -> updateLogs.getChangedTo().equalsIgnoreCase(sprintDetails.getSprintName())
						|| (updateLogs.getChangedFrom().equalsIgnoreCase(sprintDetails.getSprintName()) && DateUtil
								.isWithinDateTimeRange(updateLogs.getUpdatedOn(), startDateTime, sprintEndTime)))
				.forEach(updateLogs -> {
					LocalDate startLocalDate = startDateTime.toLocalDate();
					if (updateLogs.getChangedTo().equalsIgnoreCase(sprintDetails.getSprintName())) {
						if (sprintUpdationLog.get(lastIndex).getUpdatedOn()
								.isBefore(startDateTime.plusDays(1))) {
							fullSprintMap.computeIfPresent(startLocalDate, (k, v) -> {
								v.addAll(jiraIssueList);
								return v;
							});
							fullSprintMap.putIfAbsent(startLocalDate, jiraIssueList);
						}
						LocalDateTime updatedLog = updateLogs.getUpdatedOn().isBefore(startDateTime.plusDays(1))
								? startDateTime
								: limitDateInSprint(updateLogs.getUpdatedOn(), sprintEndTime);
						addedIssuesMap.computeIfPresent(updatedLog.toLocalDate(), (k, v) -> {
							v.addAll(jiraIssueList);
							return v;
						});
						addedIssuesMap.putIfAbsent(updatedLog.toLocalDate(), jiraIssueList);
					}

					if (updateLogs.getChangedFrom().equalsIgnoreCase(sprintDetails.getSprintName()) && DateUtil
							.isWithinDateTimeRange(updateLogs.getUpdatedOn(), startDateTime, sprintEndTime)) {
						List<JiraIssue> removeJiraIssueLIst = new ArrayList<>(jiraIssueList);
						LocalDate updatedLog = updateLogs.getUpdatedOn().toLocalDate();
						removedIssues.computeIfPresent(updatedLog, (k, v) -> {
							v.addAll(removeJiraIssueLIst);
							return v;
						});
						removedIssues.putIfAbsent(updatedLog, removeJiraIssueLIst);
					}
				});
	}

	private Set<JiraIssue> getRespectiveJiraIssue(Set<JiraIssue> totalIssueList, JiraIssueCustomHistory issueHistory) {
		return totalIssueList.stream()
				.filter(jiraIssue -> jiraIssue.getNumber().equalsIgnoreCase(issueHistory.getStoryID()))
				.collect(Collectors.toSet());
	}
	/*
	 * if for closed sprint updation is happening after sprint end date time then it
	 * would be counted under the last day of sprint
	 */

	private LocalDateTime limitDateInSprint(LocalDateTime updatedLog, LocalDateTime sprintEndDate) {
		if (Objects.nonNull(updatedLog) && updatedLog.isAfter(sprintEndDate.minusDays(1))) {
			return sprintEndDate;
		} else {
			return updatedLog;
		}
	}

	/**
	 * This method populates KPI value to sprint leaf nodes. It also gives the trend
	 * analysis at sprint wise.
	 *
	 * @param sprintLeafNode
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings(UNCHECKED)
	private void sprintWiseLeafNodeValue(Node sprintLeafNode, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();
		Object basicProjectConfigId = sprintLeafNode.getAccountHierarchy().getBasicProjectConfigId();
		String sprintName = sprintLeafNode.getAccountHierarchy().getNodeName();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNode, null, null, kpiRequest);
		SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT);
		if (ObjectUtils.isNotEmpty(sprintDetails)) {
			Map<LocalDate, List<JiraIssue>> fullSprintIssuesMap = (Map<LocalDate, List<JiraIssue>>) resultMap
					.get(FULL_SPRINT_ISSUES);
			Map<LocalDate, List<JiraIssue>> removedIssuesMap = (Map<LocalDate, List<JiraIssue>>) resultMap
					.get(CommonConstant.PUNTED_ISSUES);
			Map<LocalDate, List<JiraIssue>> addedIssuesMap = (Map<LocalDate, List<JiraIssue>>) resultMap
					.get(CommonConstant.ADDED_ISSUES);

			Map<LocalDate, List<JiraIssue>> lateRefinedDateWiseMap = (Map<LocalDate, List<JiraIssue>>) resultMap
					.get(LATE_REFINED);

			log.info("Late Refinement -> request id : {} total jira Issues : {}", requestTrackerId,
					fullSprintIssuesMap.size());
			LocalDateTime sprintStartDate = DateUtil.stringToLocalDateTime(sprintDetails.getStartDate(),
					DateUtil.TIME_FORMAT_WITH_SEC);
			LocalDateTime sprintEndDate = DateUtil.getTodayTime().plusDays(1);

			LocalDate maximumRemovalDate = removedIssuesMap.keySet().stream().filter(Objects::nonNull)
					.min(LocalDate::compareTo).orElse(null);

			List<DataCountGroup> currentSprintDataCountGroup = new ArrayList<>();
			List<JiraIssue> processedAllIssues = new ArrayList<>();
			List<KPIExcelData> excelDataList = new ArrayList<>();
			for (LocalDateTime date = sprintStartDate; date.toLocalDate().isBefore(sprintEndDate.toLocalDate()); date = date.plusDays(1)) {
				DataCountGroup currentSprint = new DataCountGroup();
				List<DataCount> dataCountList = new ArrayList<>();
				List<JiraIssue> overall = calculateOverallScopeDayWise(fullSprintIssuesMap, removedIssuesMap,
						addedIssuesMap, processedAllIssues, date.toLocalDate(), sprintDetails, maximumRemovalDate);
				List<JiraIssue> unRefinedData = calculateLateRefinementPercenatge(overall, lateRefinedDateWiseMap, date.toLocalDate(),
						dataCountList, sprintName);
				createExcelData(unRefinedData, overall, date, sprintName, excelDataList, fieldMapping,
						requestTrackerId);
				currentSprint.setFilter(DateUtil.tranformUTCLocalTimeToZFormat(date));
				currentSprint.setValue(dataCountList);
				currentSprintDataCountGroup.add(currentSprint);
			}

			IterationKpiValue iterationKpiValue = new IterationKpiValue();
			iterationKpiValue.setDataGroup(currentSprintDataCountGroup);
			iterationKpiValue.setFilter1("OVERALL");

			List<IterationKpiValue> iterationKpiValueList = new ArrayList<>();
			iterationKpiValueList.add(iterationKpiValue);

			kpiElement.setModalHeads(KPIExcelColumn.LATE_REFINEMENT.getColumns());
			kpiElement.setExcelColumns(KPIExcelColumn.LATE_REFINEMENT.getColumns());
			kpiElement.setExcelData(excelDataList);

			kpiElement.setTrendValueList(iterationKpiValueList);
		}
	}

	private List<JiraIssue> calculateLateRefinementPercenatge(List<JiraIssue> overall,
			Map<LocalDate, List<JiraIssue>> lateRefinedDateWiseMap, LocalDate date, List<DataCount> dataCountList,
			String sprintName) {
		Double defaultPercentage = 0.0D;
		List<JiraIssue> jiraIssueList = lateRefinedDateWiseMap.getOrDefault(date, new ArrayList<>());
		List<JiraIssue> commonIssues = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(jiraIssueList)) {
			commonIssues = (List<JiraIssue>) CollectionUtils.intersection(jiraIssueList, overall);
			defaultPercentage = roundingOff(((double) commonIssues.size() / overall.size()) * 100);
		}
		dataCountList.add(getDataCountObject(defaultPercentage, commonIssues, overall, date.toString(), sprintName));

		return commonIssues;
	}

	private void createExcelData(List<JiraIssue> commonIssues, List<JiraIssue> overall, LocalDateTime date, String sprintName,
			List<KPIExcelData> excelData, FieldMapping fieldMapping, String requestId) {
		if (requestId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateLateRefinementExcel(overall, commonIssues, date, sprintName, excelData,
					fieldMapping);
		}
	}

	private List<JiraIssue> calculateOverallScopeDayWise(Map<LocalDate, List<JiraIssue>> allIssues,
			Map<LocalDate, List<JiraIssue>> removedIssues, Map<LocalDate, List<JiraIssue>> addedIssues,
			List<JiraIssue> processedIssues, LocalDate date, SprintDetails sprintDetails,
			LocalDate maximumRemovalDate) {
		List<JiraIssue> allIssuesOrDefault = allIssues.getOrDefault(date, new ArrayList<>());
		List<JiraIssue> removedJiraIssues = removedIssues.getOrDefault(date, new ArrayList<>());
		List<JiraIssue> addedJiraIssues = addedIssues.getOrDefault(date, new ArrayList<>());
		Set<String> puntedIssues = checkNullList(sprintDetails.getPuntedIssues()).stream().map(SprintIssue::getNumber)
				.collect(Collectors.toSet());
		removeExtraTransitionOnSprintEndDate(sprintDetails, maximumRemovalDate, removedJiraIssues,
				sprintDetails.getTotalIssues().stream().map(SprintIssue::getNumber).toList());

		// if an issue is present in both on the same day, then whatever in the punted
		// issues those should be removed once and for all
		List<JiraIssue> commonIssues = (List<JiraIssue>) CollectionUtils.intersection(removedJiraIssues,
				addedJiraIssues);
		// if punted issues are present in commonIssues
		// remove from both, else just remove from added
		if (CollectionUtils.isNotEmpty(commonIssues)) {
			commonIssues.stream().forEach(issue -> {
				if (puntedIssues.contains(issue.getNumber())) {
					removedJiraIssues.removeIf(jira -> issue.getNumber().equalsIgnoreCase(jira.getNumber()));
					addedJiraIssues.removeIf(jira -> issue.getNumber().equalsIgnoreCase(jira.getNumber()));
					allIssuesOrDefault.removeIf(jira -> issue.getNumber().equalsIgnoreCase(jira.getNumber()));
				} else {
					// if not in punted then not to be removed from remove issues
					removedJiraIssues.removeIf(jira -> issue.getNumber().equalsIgnoreCase(jira.getNumber()));
				}
			});
		}
		processedIssues.addAll(allIssuesOrDefault);
		processedIssues.addAll(addedJiraIssues);
		processedIssues.removeAll(removedJiraIssues);
		return processedIssues.stream().distinct().toList();

	}

	/*
	 * if on the last day of sprint closure, some issues get closed after the sprint
	 * end time but sprint report has fixed them in not completed segment, then
	 * those has to be removed from completed Issue Map
	 */
	private void removeExtraTransitionOnSprintEndDate(SprintDetails sprintDetails, LocalDate maximumRemovalDate,
			List<JiraIssue> baseIssues, List<String> issuesToBeRemoved) {
		if (sprintDetails.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_CLOSED)
				&& ObjectUtils.isNotEmpty(maximumRemovalDate)) {
			baseIssues.removeIf(issue -> issuesToBeRemoved.contains(issue.getNumber()));
		}
	}

	private DataCount getDataCountObject(Double value, List<JiraIssue> lateIssues, List<JiraIssue> totalIssues,
			String date, String sprintID) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSProjectName(sprintID);
		dataCount.setKpiGroup(LEGEND);
		dataCount.setValue(value);
		Map<String, Object> hoverMap = new LinkedHashMap<>();
		hoverMap.put("Date", date);
		hoverMap.put("Number of Unrefined Items", lateIssues.size());
		hoverMap.put("Size of Unrefined Items", getTotalSum(lateIssues));
		hoverMap.put("Total Items", totalIssues.size());
		hoverMap.put("Size of Total Items", getTotalSum(totalIssues));
		dataCount.setHoverValue(hoverMap);

		return dataCount;
	}

	private double getTotalSum(List<JiraIssue> totalJiraIssue) {
		return roundingOff(totalJiraIssue.stream().filter(jiraIssue -> Objects.nonNull(jiraIssue.getStoryPoints()))
				.mapToDouble(JiraIssue::getStoryPoints).sum());

	}

}
