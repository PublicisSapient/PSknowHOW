package com.publicissapient.kpidashboard.apis.jira.service.releasedashboard;

import com.publicissapient.kpidashboard.apis.common.service.ApplicationKPIService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public abstract class JiraReleaseKPIService<R, S, T> implements ApplicationKPIService<R, S, T> {

    public static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String BASIC_PROJECT_CONFIG_ID = "basicProjectConfigId";
    @Autowired
    private CacheService cacheService;
    @Autowired
    private JiraReleaseServiceR jiraService;

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
     * @param filteredAccountData
     * @return kpi data
     * @throws ApplicationException
     */
    public abstract KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
                                          Node filteredAccountData) throws ApplicationException;

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

}

