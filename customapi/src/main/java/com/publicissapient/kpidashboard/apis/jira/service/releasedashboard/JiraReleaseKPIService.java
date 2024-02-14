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
package com.publicissapient.kpidashboard.apis.jira.service.releasedashboard;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.jira.service.NonTrendKPIService;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * All Jira NonTrend KPIs service have to implement this class {@link NonTrendKPIService}
 *
 * @author purgupta2
 */
public abstract class JiraReleaseKPIService implements NonTrendKPIService {

    @Autowired
    private CacheService cacheService;
    @Autowired
    private JiraReleaseServiceR jiraService;

    /**
     * Returns API Request tracker Id to be used for logging/debugging and using it
     * for maintaining any sort of cache.
     *
     * @return Scrum Request Tracker Id
     */
    public String getRequestTrackerId() {
        return cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name());
    }

    public double roundingOff(double value) {
        return (double) Math.round(value * 100) / 100;
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

    public JiraIssueReleaseStatus getJiraIssueReleaseStatus() {
        return jiraService.getJiraIssueReleaseForProject();
    }

    public List<String> getReleaseList() {
        return jiraService.getReleaseList();
    }

    public List<JiraIssueCustomHistory> getJiraIssuesCustomHistoryFromBaseClass() {
        return jiraService.getJiraIssuesCustomHistoryForCurrentSprint();
    }

}

