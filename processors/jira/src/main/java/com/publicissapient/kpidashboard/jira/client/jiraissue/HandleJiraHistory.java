package com.publicissapient.kpidashboard.jira.client.jiraissue;

import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class HandleJiraHistory {

    private List<JiraHistoryChangeLog> getJiraFieldChangeLog(List<ChangelogGroup> changeLogList, FieldMapping fieldMapping,String jiraField) {

        List<JiraHistoryChangeLog> fieldHistoryLog = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(changeLogList)) {
            for (ChangelogGroup history : changeLogList) {
                history.getItems().forEach(item -> {
                    if (item.getField().trim().equalsIgnoreCase(jiraField.trim())) {
                        JiraHistoryChangeLog jiraHistoryChangeLog = new JiraHistoryChangeLog();
                        jiraHistoryChangeLog.setChangedFrom(handleStr(item.getFromString()));
                        jiraHistoryChangeLog.setChangedTo(handleStr(item.getToString()));
                        jiraHistoryChangeLog.setUpdatedOn(history.getCreated());
                        fieldHistoryLog.add(jiraHistoryChangeLog);
                    }
                });
            }
        }
        return fieldHistoryLog;
    }

    private String handleStr (String str)
    {
        return str!=null?str:"";
    }

    public void setJiraIssueCustomHistoryUpdationLog(JiraIssueCustomHistory jiraIssueCustomHistory, List<ChangelogGroup> changeLogList, FieldMapping fieldMapping)
    {
        List<JiraHistoryChangeLog> statusChangeLog = getJiraFieldChangeLog(changeLogList, fieldMapping, JiraConstants.STATUS);
        List<JiraHistoryChangeLog> assigneeChangeLog = getJiraFieldChangeLog(changeLogList, fieldMapping, JiraConstants.ASSIGNEE);
        List<JiraHistoryChangeLog> priorityChangeLog = getJiraFieldChangeLog(changeLogList, fieldMapping, JiraConstants.PRIORITY);
        List<JiraHistoryChangeLog> fixVersionChangeLog = getJiraFieldChangeLog(changeLogList, fieldMapping, JiraConstants.FIXVERSION);
        List<JiraHistoryChangeLog> labelsChangeLog = getJiraFieldChangeLog(changeLogList, fieldMapping, JiraConstants.LABELS);
        jiraIssueCustomHistory.setStatusUpdationLog(statusChangeLog);
        jiraIssueCustomHistory.setAssigneeUpdationLog(assigneeChangeLog);
        jiraIssueCustomHistory.setPriorityUpdationLog(priorityChangeLog);
        jiraIssueCustomHistory.setFixVersionUpdationLog(fixVersionChangeLog);
        jiraIssueCustomHistory.setLabelUpdationLog(labelsChangeLog);
    }
}


