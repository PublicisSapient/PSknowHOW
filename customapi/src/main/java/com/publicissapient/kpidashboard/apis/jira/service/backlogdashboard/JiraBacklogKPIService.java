/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package com.publicissapient.kpidashboard.apis.jira.service.backlogdashboard;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.ToolsKPIService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.jira.service.NonTrendKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * All Jira NonTrend KPIs service have to implement this class {@link NonTrendKPIService}
 *
 * @author purgupta2
 */
public abstract class JiraBacklogKPIService<R, S> extends ToolsKPIService<R, S> implements NonTrendKPIService {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private JiraBacklogServiceR jiraService;

    /**
     * Returns API Request tracker Id to be used for logging/debugging and using it
     * for maintaining any sort of cache.
     *
     * @return Scrum Request Tracker Id
     */
    public String getRequestTrackerId() {
        return cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name());
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

    public List<JiraIssueCustomHistory> getJiraIssuesCustomHistoryFromBaseClass() {
        return jiraService.getJiraIssuesCustomHistoryForCurrentSprint();
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

}
