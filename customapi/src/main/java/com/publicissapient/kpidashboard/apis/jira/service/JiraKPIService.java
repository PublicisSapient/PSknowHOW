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
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.IterationStatus;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import org.apache.commons.lang3.StringUtils;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.ApplicationKPIService;
import com.publicissapient.kpidashboard.apis.common.service.ToolsKPIService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

/**
 * This class is extention of ApplicationKPIService. All Jira KPIs service have to
 * implement this class {@link ApplicationKPIService}
 *
 * @param <R> KPIs calculated value type
 * @param <S> Maturity Value Type not applicable in every case
 * @param <T> Bind DB data with type
 * @author tauakram
 */
public abstract class JiraKPIService<R, S, T> extends ToolsKPIService<R, S> implements ApplicationKPIService<R, S, T> {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    @Autowired
    private CacheService cacheService;

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
     * @param kpiElement           KpiElement
     * @param requestTrackerId     request id
     * @param validationDataKey    validation data key
     * @param validationDataMap    validation data map
     * @param storyIdList          story id list
     * @param sprintWiseDefectList sprints defect list
     * @param storyPointList       the story point list
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

        //adjust for starting and ending on a Sunday:
        return daysWithoutWeekends + (startW == DayOfWeek.SUNDAY ? 1 : 0) + (endW == DayOfWeek.SUNDAY ? 1 : 0);
    }

    public void populateIterationStatusData(List<IterationKpiModalValue> overAllmodalValues, List<IterationKpiModalValue> modalValues, IterationStatus iterationStatus) {
        IterationKpiModalValue iterationKpiModalVal = new IterationKpiModalValue();
        iterationKpiModalVal.setIssueId(iterationStatus.getIssueId());
        iterationKpiModalVal.setIssueURL(iterationStatus.getUrl());
        iterationKpiModalVal.setIssueType(iterationStatus.getTypeName());
        iterationKpiModalVal.setPriority(iterationStatus.getPriority());
        iterationKpiModalVal.setDescription(iterationStatus.getIssueDescription());
        iterationKpiModalVal.setIssueStatus(iterationStatus.getIssueStatus());
        iterationKpiModalVal.setDueDate(DateUtil.stringToLocalDate(iterationStatus.getDueDate(),DateUtil.TIME_FORMAT_WITH_SEC).toString());
        if (iterationStatus.getRemainingEstimateMinutes() != null)
            iterationKpiModalVal.setRemainingTime(iterationStatus.getRemainingEstimateMinutes());
        else
            iterationKpiModalVal.setRemainingTime(0);
        iterationKpiModalVal.setDelay(iterationStatus.getDelay());
        modalValues.add(iterationKpiModalVal);
        overAllmodalValues.add(iterationKpiModalVal);
    }

	public void populateIterationData(List<IterationKpiModalValue> overAllmodalValues, List<IterationKpiModalValue>
			modalValues, JiraIssue jiraIssue, boolean estimationFlag, FieldMapping fieldMapping) {
		int originalEstimate = 0;
		int loggedTime = 0;
		IterationKpiModalValue iterationKpiModalValue = new IterationKpiModalValue();
		iterationKpiModalValue.setIssueId(jiraIssue.getNumber());
		iterationKpiModalValue.setIssueURL(jiraIssue.getUrl());
		iterationKpiModalValue.setDescription(jiraIssue.getName());
		iterationKpiModalValue.setIssueStatus(jiraIssue.getStatus());
		iterationKpiModalValue.setIssueType(jiraIssue.getTypeName());
		if (estimationFlag) {
			if (null != jiraIssue.getStoryPoints() && StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria()) &&
					fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
				iterationKpiModalValue.setIssueSize(jiraIssue.getStoryPoints().toString());
			}
			if (null!=jiraIssue.getOriginalEstimateMinutes() && StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria()) &&
					fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.ACTUAL_ESTIMATION)) {
				originalEstimate = jiraIssue.getOriginalEstimateMinutes()/60;
				iterationKpiModalValue.setIssueSize(originalEstimate+" hrs");
			}
		} else {
			iterationKpiModalValue.setIssueSize(Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0).toString());
			if(null!=jiraIssue.getOriginalEstimateMinutes()){
				originalEstimate = jiraIssue.getOriginalEstimateMinutes()/60;
				iterationKpiModalValue.setOriginalEstimateMinutes(String.valueOf(originalEstimate+" hrs"));
			}
			else
				iterationKpiModalValue.setOriginalEstimateMinutes(String.valueOf(originalEstimate)+" hrs");
		}
		if(jiraIssue.getRemainingEstimateMinutes() != null) {
			iterationKpiModalValue.setRemainingTime(jiraIssue.getRemainingEstimateMinutes()/60);
		}
		loggedTime = jiraIssue.getTimeSpentInMinutes()/60;
		iterationKpiModalValue.setTimeSpentInMinutes(String.valueOf(loggedTime+" hrs"));
		modalValues.add(iterationKpiModalValue);
		overAllmodalValues.add(iterationKpiModalValue);
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

	public void populateIterationDataForWorkCompleted(List<IterationKpiModalValue> overAllmodalValues,
			List<IterationKpiModalValue> modalValues, JiraIssue jiraIssue, FieldMapping fieldMapping,
			Map<String, Object> actualCompletionData, long delay) {
		int originalEstimate = 0;
		IterationKpiModalValue iterationKpiModalValue = new IterationKpiModalValue();
		iterationKpiModalValue.setIssueId(jiraIssue.getNumber());
		iterationKpiModalValue.setIssueURL(jiraIssue.getUrl());
		iterationKpiModalValue.setDescription(jiraIssue.getName());
		iterationKpiModalValue.setIssueStatus(jiraIssue.getStatus());
		iterationKpiModalValue.setIssueType(jiraIssue.getTypeName());
		if (null != jiraIssue.getStoryPoints() && StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			iterationKpiModalValue.setIssueSize(jiraIssue.getStoryPoints().toString());
		}
		if (null != jiraIssue.getOriginalEstimateMinutes()
				&& StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.ACTUAL_ESTIMATION)) {
			originalEstimate = jiraIssue.getOriginalEstimateMinutes() / 60;
			iterationKpiModalValue.setIssueSize(originalEstimate + " hrs");
		}
		// Original Estimate in days
		if (null != jiraIssue.getOriginalEstimateMinutes()) {
			iterationKpiModalValue
					.setOriginalEstimateMinutes(CommonUtils.convertIntoDays(jiraIssue.getOriginalEstimateMinutes()));
		}
		if (jiraIssue.getDueDate() != null)
			iterationKpiModalValue.setDueDate(jiraIssue.getDueDate().substring(0, jiraIssue.getDueDate().indexOf('T')));
		if (actualCompletionData.get("actualCompleteDate") != null)
			iterationKpiModalValue.setActualCompletionDate(actualCompletionData.get("actualCompleteDate").toString());
		if (jiraIssue.getOriginalEstimateMinutes() != null && actualCompletionData.get("actualCompletionDays") != "-") {
			iterationKpiModalValue.setDelay(String.valueOf(delay));
		} else {
			iterationKpiModalValue.setDelay(" - ");
		}
		modalValues.add(iterationKpiModalValue);
		overAllmodalValues.add(iterationKpiModalValue);
	}
}
