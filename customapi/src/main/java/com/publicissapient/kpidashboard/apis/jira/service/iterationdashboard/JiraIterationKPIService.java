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

package com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.jira.service.NonTrendKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.IterationStatus;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is extention of ApplicationKPIService. All Jira KPIs service have
 * to implement this class {@link NonTrendKPIService}
 */
public abstract class JiraIterationKPIService implements NonTrendKPIService {

    @Autowired
    private CacheService cacheService;


    @Autowired
    private JiraIterationServiceR jiraIterationServiceR;

    /**
     * Returns API Request tracker Id to be used for logging/debugging and using it
     * for maintaining any sort of cache.
     *
     * @return Scrum Request Tracker Id
     */
    public String getRequestTrackerId() {
        return cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name());
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
            sprintDetails = (SprintDetails) jiraIterationServiceR.getCurrentSprintDetails().clone();
        } catch (CloneNotSupportedException e) {
            sprintDetails = null;
        }
        return sprintDetails;
    }

    public List<JiraIssue> getJiraIssuesFromBaseClass(List<String> numbersList) {
        return jiraIterationServiceR.getJiraIssuesForCurrentSprint().stream()
                .filter(jiraIssue -> numbersList.contains(jiraIssue.getNumber())).collect(Collectors.toList());
    }

    public List<JiraIssueCustomHistory> getJiraIssuesCustomHistoryFromBaseClass(List<String> numbersList) {
        return jiraIterationServiceR.getJiraIssuesCustomHistoryForCurrentSprint().stream()
                .filter(jiraIssueCustomHistory -> numbersList.contains(jiraIssueCustomHistory.getStoryID()))
                .collect(Collectors.toList());
    }

    public List<JiraIssueCustomHistory> getJiraIssuesCustomHistoryFromBaseClass() {
        return jiraIterationServiceR.getJiraIssuesCustomHistoryForCurrentSprint();
    }

    public List<JiraIssue> getJiraIssuesFromBaseClass() {
        return jiraIterationServiceR.getJiraIssuesForCurrentSprint();
    }

}
