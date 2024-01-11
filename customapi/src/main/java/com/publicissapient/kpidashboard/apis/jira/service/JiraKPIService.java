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

package com.publicissapient.kpidashboard.apis.jira.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import com.publicissapient.kpidashboard.apis.common.service.ApplicationKPIService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.ToolsKPIService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.model.jira.IterationStatus;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import com.publicissapient.kpidashboard.common.util.DateUtil;

/**
 * This class is extention of ApplicationKPIService. All Jira KPIs service have
 * to implement this class {@link ApplicationKPIService}
 *
 * @param <R>
 *            KPIs calculated value type
 * @param <S>
 *            Maturity Value Type not applicable in every case
 * @param <T>
 *            Bind DB data with type
 * @author tauakram
 */
public abstract class JiraKPIService<R, S, T> extends ToolsKPIService<R, S> implements ApplicationKPIService<R, S, T> {

	public static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String BASIC_PROJECT_CONFIG_ID = "basicProjectConfigId";
	@Autowired
	private CacheService cacheService;
	@Autowired
	private JiraServiceR jiraService;

	/**
	 * Gets qualifier type
	 *
	 * @return qualifier type
	 */
	public abstract String getQualifierType();

	/**
	 * Gets Kpi data based on kpi request
	 *
	 * @param kpiRequest
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @return kpi data
	 * @throws ApplicationException
	 */
	public abstract KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException;

	/**
	 * Returns API Request tracker Id to be used for logging/debugging and using it
	 * for maintaining any sort of cache.
	 *
	 * @return Scrum Request Tracker Id
	 */
	public String getRequestTrackerId() {
		return cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name());
	}

	/**
	 * Returns API Request tracker Id to be used for logging/debugging and using it
	 * for maintaining any sort of cache.
	 *
	 * @return Kanban Request Tracker Id
	 */
	public String getKanbanRequestTrackerId() {
		return cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRAKANBAN.name());
	}

	/**
	 * This method populates KPI Element with Validation data. It will be triggered
	 * only for request originated to get Excel data.
	 *
	 * @param kpiElement
	 *            KpiElement
	 * @param requestTrackerId
	 *            request id
	 * @param validationDataKey
	 *            validation data key
	 * @param validationDataMap
	 *            validation data map
	 * @param storyIdList
	 *            story id list
	 * @param sprintWiseDefectList
	 *            sprints defect list
	 * @param storyPointList
	 *            the story point list
	 */
	public void populateValidationDataObject(KpiElement kpiElement, String requestTrackerId, String validationDataKey,
			Map<String, ValidationData> validationDataMap, List<String> storyIdList,
			List<JiraIssue> sprintWiseDefectList, List<String> storyPointList) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			ValidationData validationData = new ValidationData();
			validationData.setStoryKeyList(storyIdList);
			validationData.setStoryPointList(storyPointList);
			validationData.setDefectKeyList(
					sprintWiseDefectList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			validationDataMap.put(validationDataKey, validationData);
			kpiElement.setMapOfSprintAndData(validationDataMap);
		}
	}

	public Map<String, Double> getLastNMonth(int count) {
		Map<String, Double> lastNMonth = new LinkedHashMap<>();
		DateTime currentDate = DateTime.now();
		String currentDateStr = currentDate.getYear() + Constant.DASH + currentDate.getMonthOfYear();
		lastNMonth.put(currentDateStr, 0.0);
		DateTime lastMonth = DateTime.now();
		for (int i = 1; i < count; i++) {
			lastMonth = lastMonth.minusMonths(1);
			String lastMonthStr = lastMonth.getYear() + Constant.DASH + lastMonth.getMonthOfYear();
			lastNMonth.put(lastMonthStr, 0.0);

		}
		return lastNMonth;
	}

	public long calcWeekDays(final LocalDate start, final LocalDate end) {
		final DayOfWeek startW = start.getDayOfWeek();
		final DayOfWeek endW = end.getDayOfWeek();

		final long days = ChronoUnit.DAYS.between(start, end);
		final long daysWithoutWeekends = days - 2 * ((days + startW.getValue()) / 7);

		// adjust for starting and ending on a Sunday:
		return daysWithoutWeekends + (startW == DayOfWeek.SUNDAY ? 1 : 0) + (endW == DayOfWeek.SUNDAY ? 1 : 0);
	}

	public void populateIterationStatusData(List<IterationKpiModalValue> overAllmodalValues,
			List<IterationKpiModalValue> modalValues, IterationStatus iterationStatus) {
		IterationKpiModalValue iterationKpiModalVal = new IterationKpiModalValue();
		iterationKpiModalVal.setIssueId(iterationStatus.getIssueId());
		iterationKpiModalVal.setIssueURL(iterationStatus.getUrl());
		iterationKpiModalVal.setIssueType(iterationStatus.getTypeName());
		iterationKpiModalVal.setPriority(iterationStatus.getPriority());
		iterationKpiModalVal.setDescription(iterationStatus.getIssueDescription());
		iterationKpiModalVal.setIssueStatus(iterationStatus.getIssueStatus());
		iterationKpiModalVal.setDueDate(DateUtil.dateTimeConverter(iterationStatus.getDueDate(),
				DateUtil.TIME_FORMAT_WITH_SEC, DateUtil.DISPLAY_DATE_FORMAT));
		if (iterationStatus.getRemainingEstimateMinutes() != null)
			iterationKpiModalVal.setRemainingTime(iterationStatus.getRemainingEstimateMinutes());
		else
			iterationKpiModalVal.setRemainingTime(0);
		iterationKpiModalVal.setDelay(iterationStatus.getDelay());
		modalValues.add(iterationKpiModalVal);
		overAllmodalValues.add(iterationKpiModalVal);
	}

	public void populateIterationDataForTestWithoutStory(List<IterationKpiModalValue> overAllModalValues,
			TestCaseDetails testCaseDetails) {
		IterationKpiModalValue iterationKpiModalValue = new IterationKpiModalValue();
		iterationKpiModalValue.setIssueId(testCaseDetails.getNumber());
		iterationKpiModalValue.setDescription(testCaseDetails.getName());
		overAllModalValues.add(iterationKpiModalValue);
	}

	public void populateIterationDataForDefectWithoutStory(List<IterationKpiModalValue> overAllModalValues,
			JiraIssue jiraIssue) {

		IterationKpiModalValue iterationKpiModalValue = new IterationKpiModalValue();
		iterationKpiModalValue.setIssueId(jiraIssue.getNumber());
		iterationKpiModalValue.setIssueURL(jiraIssue.getUrl());
		iterationKpiModalValue.setDescription(jiraIssue.getName());
		overAllModalValues.add(iterationKpiModalValue);
	}

	public String getDevCompletionDate(JiraIssueCustomHistory issueCustomHistory, List<String> fieldMapping) {
		String devCompleteDate = Constant.DASH;
		List<JiraHistoryChangeLog> filterStatusUpdationLog = issueCustomHistory.getStatusUpdationLog();
		if (null != fieldMapping && CollectionUtils.isNotEmpty(fieldMapping)) {
			devCompleteDate = filterStatusUpdationLog.stream()
					.filter(jiraHistoryChangeLog -> fieldMapping.contains(jiraHistoryChangeLog.getChangedTo())
							&& jiraHistoryChangeLog.getUpdatedOn() != null)
					.findFirst()
					.map(jiraHistoryChangeLog -> LocalDate
							.parse(jiraHistoryChangeLog.getUpdatedOn().toString().split("T")[0],
									DateTimeFormatter.ofPattern(DateUtil.DATE_FORMAT))
							.toString())
					.orElse(devCompleteDate);
		}
		return devCompleteDate;
	}

	// Filtering the history which happened inside the sprint on basis of activity
	// date
	public List<JiraHistoryChangeLog> getInSprintStatusLogs(List<JiraHistoryChangeLog> issueHistoryLogs,
			LocalDate sprintStartDate, LocalDate sprintEndDate) {
		List<JiraHistoryChangeLog> filterStatusUpdationLogs = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(issueHistoryLogs)) {
			filterStatusUpdationLogs = issueHistoryLogs.stream()
					.filter(jiraIssueSprint -> DateUtil.isWithinDateRange(
							LocalDate.parse(jiraIssueSprint.getUpdatedOn().toString().split("T")[0].concat("T00:00:00"),
									DateTimeFormatter.ofPattern(TIME_FORMAT)),
							sprintStartDate, sprintEndDate))
					.collect(Collectors.toList());
		}
		return filterStatusUpdationLogs;
	}
	/**
	 * to maintain values upto 2 places of decimal
	 * 
	 * @param value
	 * @return
	 */
	public double roundingOff(double value) {
		return (double) Math.round(value * 100) / 100;
	}

	/**
	 * For Assigning IterationKPiData
	 * 
	 * @param label
	 * @param fieldMapping
	 * @param issueCount
	 * @param storyPoint
	 * @param originalEstimate
	 * @param modalvalue
	 * @return
	 */
	public IterationKpiData createIterationKpiData(String label, FieldMapping fieldMapping, Integer issueCount,
			Double storyPoint, Double originalEstimate, List<IterationKpiModalValue> modalvalue) {
		IterationKpiData iterationKpiData;
		if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			iterationKpiData = new IterationKpiData(label, Double.valueOf(issueCount), roundingOff(storyPoint), null,
					"", CommonConstant.SP, modalvalue);
		} else {
			iterationKpiData = new IterationKpiData(label, Double.valueOf(issueCount), roundingOff(originalEstimate),
					null, "", CommonConstant.DAY, modalvalue);
		}
		return iterationKpiData;
	}

	public SprintDetails getSprintDetailsFromBaseClass() {
		SprintDetails sprintDetails;
		try {
			sprintDetails = (SprintDetails) jiraService.getCurrentSprintDetails().clone();
		} catch (CloneNotSupportedException e) {
			sprintDetails = null;
		}
		return sprintDetails;
	}

	public List<JiraIssue> getJiraIssuesFromBaseClass(List<String> numbersList) {
		return jiraService.getJiraIssuesForCurrentSprint().stream()
				.filter(jiraIssue -> numbersList.contains(jiraIssue.getNumber())).collect(Collectors.toList());
	}

	public List<JiraIssueCustomHistory> getJiraIssuesCustomHistoryFromBaseClass(List<String> numbersList) {
		return jiraService.getJiraIssuesCustomHistoryForCurrentSprint().stream()
				.filter(jiraIssueCustomHistory -> numbersList.contains(jiraIssueCustomHistory.getStoryID()))
				.collect(Collectors.toList());
	}

	public List<JiraIssueCustomHistory> getJiraIssuesCustomHistoryFromBaseClass() {
		return jiraService.getJiraIssuesCustomHistoryForCurrentSprint();
	}

	public List<JiraIssue> getBaseReleaseJiraIssues() {
		return jiraService.getJiraIssuesForSelectedRelease();
	}

	public Set<JiraIssue> getBaseReleaseSubTask() {
		return jiraService.getSubTaskDefects();
	}

	public List<JiraIssue> getFilteredReleaseJiraIssuesFromBaseClass(FieldMapping fieldMapping) {
		List<JiraIssue> filteredJiraIssue = new ArrayList<>();
		List<JiraIssue> subtaskDefects = new ArrayList<>();
		List<String> defectType = new ArrayList<>();
		List<JiraIssue> jiraIssuesForCurrentRelease = jiraService.getJiraIssuesForSelectedRelease();
		Set<JiraIssue> defectsList = jiraService.getSubTaskDefects();
		if (CollectionUtils.isNotEmpty(defectsList)) {
			subtaskDefects.addAll(defectsList);
			subtaskDefects.removeIf(jiraIssuesForCurrentRelease::contains);
			filteredJiraIssue = subtaskDefects;
		}

		if (fieldMapping != null && CollectionUtils.isNotEmpty(fieldMapping.getJiradefecttype())
				&& CollectionUtils.isNotEmpty(jiraIssuesForCurrentRelease)) {
			defectType.add(NormalizedJira.DEFECT_TYPE.getValue());
			defectType.addAll(fieldMapping.getJiradefecttype());
			List<JiraIssue> finalFilteredJiraIssue = filteredJiraIssue;
			finalFilteredJiraIssue.addAll(jiraIssuesForCurrentRelease.stream()
					.filter(jiraIssue -> defectType.contains(jiraIssue.getTypeName())).collect(Collectors.toList()));
		} else
			filteredJiraIssue = jiraIssuesForCurrentRelease;
		return filteredJiraIssue;
	}

	public List<JiraIssue> getBackLogJiraIssuesFromBaseClass() {
		return jiraService.getJiraIssuesForCurrentSprint();
	}

	public JiraIssueReleaseStatus getJiraIssueReleaseStatus() {
		return jiraService.getJiraIssueReleaseForProject();
	}

	public void populateBackLogData(List<IterationKpiModalValue> overAllmodalValues,
			List<IterationKpiModalValue> modalValues, JiraIssue jiraIssue) {
		IterationKpiModalValue iterationKpiModalValue = new IterationKpiModalValue();
		iterationKpiModalValue.setIssueType(jiraIssue.getTypeName());
		iterationKpiModalValue.setIssueURL(jiraIssue.getUrl());
		iterationKpiModalValue.setIssueId(jiraIssue.getNumber());
		iterationKpiModalValue.setDescription(jiraIssue.getName());
		iterationKpiModalValue.setPriority(jiraIssue.getPriority());
		iterationKpiModalValue.setIssueSize(Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0).toString());
		overAllmodalValues.add(iterationKpiModalValue);
		modalValues.add(iterationKpiModalValue);
	}

	public List<String> getReleaseList() {
		return jiraService.getReleaseList();
	}

	public List<JiraIssue> getJiraIssuesFromBaseClass() {
		return jiraService.getJiraIssuesForCurrentSprint();
	}

}
