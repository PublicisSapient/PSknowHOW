package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.*;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.jira.client.jiraissue.JiraIssueClientUtil;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CreateKanbanJiraIssueHistoryImpl implements CreateKanbanJiraIssueHistory{

    @Autowired
    private KanbanJiraIssueHistoryRepository kanbanIssueHistoryRepo;

    @Autowired
    private JiraCommonService jiraCommonService;


    @Override
    public KanbanIssueCustomHistory createKanbanIssueCustomHistory(ProjectConfFieldMapping projectConfig, KanbanJiraIssue jiraIssue, Issue issue,
                                                             FieldMapping fieldMapping){
        KanbanIssueCustomHistory jiraIssueHistory=getKanbanIssueCustomHistory(projectConfig,issue);
        setJiraIssueHistory(jiraIssueHistory,jiraIssue,issue,fieldMapping);

        return jiraIssueHistory;
    }

    private KanbanIssueCustomHistory getKanbanIssueCustomHistory(ProjectConfFieldMapping projectConfig, Issue issue) {
        KanbanIssueCustomHistory jiraIssueHistory = jiraCommonService.findOneKanbanIssueCustomHistory(issue.getKey(),
                projectConfig.getBasicProjectConfigId().toString());
        if (jiraIssueHistory == null) {
            jiraIssueHistory = new KanbanIssueCustomHistory();
        }
        return jiraIssueHistory;
    }

    public void setJiraIssueHistory(KanbanIssueCustomHistory jiraIssueHistory, KanbanJiraIssue jiraIssue, Issue issue,
                                    FieldMapping fieldMapping) {

        jiraIssueHistory.setProjectID(jiraIssue.getProjectName());
        jiraIssueHistory.setProjectComponentId(jiraIssue.getProjectID());
        jiraIssueHistory.setProjectKey(jiraIssue.getProjectKey());
        jiraIssueHistory.setProjectName(jiraIssue.getProjectName());
        jiraIssueHistory.setPriority(jiraIssue.getPriority());
        jiraIssueHistory.setRootCauseList(jiraIssue.getRootCauseList());
        jiraIssueHistory.setStoryType(jiraIssue.getTypeName());
        jiraIssueHistory.setAdditionalFilters(jiraIssue.getAdditionalFilters());
        jiraIssueHistory.setUrl(jiraIssue.getUrl());
        jiraIssueHistory.setDescription(jiraIssue.getName());
        // This method is not setup method. write it to keep
        // custom history
        processJiraIssueHistory(jiraIssueHistory, jiraIssue, issue, fieldMapping);
        jiraIssueHistory.setBasicProjectConfigId(jiraIssue.getBasicProjectConfigId());
    }

    private void processJiraIssueHistory(KanbanIssueCustomHistory jiraIssueCustomHistory, KanbanJiraIssue jiraIssue,
                                         Issue issue, FieldMapping fieldMapping) {
        List<ChangelogGroup> changeLogList = JiraIssueClientUtil.sortChangeLogGroup(issue);
        List<ChangelogGroup> modChangeLogList = new ArrayList<>();
        for (ChangelogGroup changeLog : changeLogList) {
            List<ChangelogItem> changeLogCollection = Lists.newArrayList(changeLog.getItems().iterator());
            ChangelogGroup grp = new ChangelogGroup(changeLog.getAuthor(), changeLog.getCreated(), changeLogCollection);
            modChangeLogList.add(grp);
        }

        if (null != jiraIssue.getDevicePlatform()) {
            jiraIssueCustomHistory.setDevicePlatform(jiraIssue.getDevicePlatform());
        }

        if (null == jiraIssueCustomHistory.getStoryID()) {
            addStoryHistory(jiraIssueCustomHistory, jiraIssue, issue, modChangeLogList, fieldMapping);
        } else {
            addHistoryInJiraIssue(jiraIssueCustomHistory, jiraIssue, modChangeLogList);
        }

    }

    private void addStoryHistory(KanbanIssueCustomHistory jiraIssueCustomHistory, KanbanJiraIssue jiraIssue,
                                 Issue issue, List<ChangelogGroup> changeLogList, FieldMapping fieldMapping) {
        List<KanbanIssueHistory> kanbanIssueHistoryList = getChangeLog(jiraIssue, changeLogList,
                issue.getCreationDate(), fieldMapping);
        jiraIssueCustomHistory.setStoryID(jiraIssue.getNumber());
        jiraIssueCustomHistory.setHistoryDetails(kanbanIssueHistoryList);
        jiraIssueCustomHistory.setCreatedDate(issue.getCreationDate().toString());
        // estimate
        jiraIssueCustomHistory.setEstimate(jiraIssue.getEstimate());
        jiraIssueCustomHistory.setBufferedEstimateTime(jiraIssue.getBufferedEstimateTime());
        if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())) {
            jiraIssueCustomHistory.setDefectStoryID(jiraIssue.getDefectStoryID());
        }

    }

    private void addHistoryInJiraIssue(KanbanIssueCustomHistory jiraIssueCustomHistory, KanbanJiraIssue jiraIssue,
                                       List<ChangelogGroup> changeLogList) {
        if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())) {
            jiraIssueCustomHistory.setDefectStoryID(jiraIssue.getDefectStoryID());
        }
        createKanbanIssueHistory(jiraIssueCustomHistory, changeLogList);
        jiraIssueCustomHistory.setEstimate(jiraIssue.getEstimate());
    }

    private void createKanbanIssueHistory(KanbanIssueCustomHistory jiraIssueCustomHistory,
                                          List<ChangelogGroup> changeLogList) {
        List<KanbanIssueHistory> issueHistoryList = new ArrayList<>();
        for (ChangelogGroup history : changeLogList) {
            for (ChangelogItem changelogItem : history.getItems()) {
                if (changelogItem.getField().equalsIgnoreCase(JiraConstants.STATUS)) {
                    KanbanIssueHistory kanbanIssueHistory = new KanbanIssueHistory();
                    kanbanIssueHistory.setStatus(changelogItem.getToString());
                    kanbanIssueHistory.setActivityDate(history.getCreated().toString());
                    issueHistoryList.add(kanbanIssueHistory);

                }
            }
            jiraIssueCustomHistory.setHistoryDetails(issueHistoryList);
        }

    }

    private List<KanbanIssueHistory> getChangeLog(KanbanJiraIssue jiraIssue, List<ChangelogGroup> changeLogList,
                                                  DateTime issueCreatedDate, FieldMapping fieldMapping) {
        List<KanbanIssueHistory> historyDetails = new ArrayList<>();
        // creating first entry of issue
        if (null != issueCreatedDate) {
            KanbanIssueHistory kanbanHistory = new KanbanIssueHistory();
            kanbanHistory.setActivityDate(issueCreatedDate.toString());
            kanbanHistory.setStatus(fieldMapping.getStoryFirstStatus());
            historyDetails.add(kanbanHistory);
        }
        if (CollectionUtils.isNotEmpty(changeLogList)) {
            for (ChangelogGroup history : changeLogList) {
                historyDetails.addAll(getIssueHistory(jiraIssue, history));
            }
        }
        return historyDetails;
    }

    private List<KanbanIssueHistory> getIssueHistory(KanbanJiraIssue jiraIssue, ChangelogGroup history) {
        List<KanbanIssueHistory> historyDetails = new ArrayList<>();
        for (ChangelogItem changelogItem : history.getItems()) {
            if (changelogItem.getField().equalsIgnoreCase(JiraConstants.TEST_AUTOMATED)) {
                if (changelogItem.getToString().equalsIgnoreCase(JiraConstants.YES)) {
                    jiraIssue.setTestAutomatedDate(JiraProcessorUtil
                            .getFormattedDate(JiraProcessorUtil.deodeUTF8String(history.getCreated().toString())));
                } else {
                    jiraIssue.setTestAutomatedDate("");
                }
            }

            if (changelogItem.getField().equalsIgnoreCase(JiraConstants.STATUS)) {
                KanbanIssueHistory kanbanHistory = new KanbanIssueHistory();
                kanbanHistory.setActivityDate(history.getCreated().toString());
                kanbanHistory.setStatus(changelogItem.getToString());
                historyDetails.add(kanbanHistory);
            }
        }
        return historyDetails;

    }
}
