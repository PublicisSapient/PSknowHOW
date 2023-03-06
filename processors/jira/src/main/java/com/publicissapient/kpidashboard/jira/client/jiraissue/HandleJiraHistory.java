package com.publicissapient.kpidashboard.jira.client.jiraissue;

import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.*;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
public class HandleJiraHistory {

    private List<JiraHistoryChangeLog> getJiraFieldChangeLog(List<ChangelogGroup> changeLogList, String jiraField, JiraIssue jiraIssue) {

        log.info("In getJiraFieldChangeLog for field : " + jiraField);
        List<JiraHistoryChangeLog> fieldHistoryLog = new ArrayList<>();
        // creating first entry of issue
        if (null != jiraIssue.getCreatedDate() && jiraField.equalsIgnoreCase(JiraConstants.STATUS)) {
            JiraHistoryChangeLog jiraHistoryChangeLog = new JiraHistoryChangeLog();
            jiraHistoryChangeLog.setChangedFrom("");
            jiraHistoryChangeLog.setChangedTo(CommonConstant.OPEN);
            jiraHistoryChangeLog.setUpdatedOn(LocalDateTime.parse(JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(jiraIssue.getCreatedDate()))));
            fieldHistoryLog.add(jiraHistoryChangeLog);
        }

        if (CollectionUtils.isNotEmpty(changeLogList)) {
            for (ChangelogGroup history : changeLogList) {
                history.getItems().forEach(item -> {
                    if (item.getField().trim().equalsIgnoreCase(jiraField.trim())) {
                        JiraHistoryChangeLog jiraHistoryChangeLog = new JiraHistoryChangeLog();
                        jiraHistoryChangeLog.setChangedFrom(handleStr(item.getFromString()));
                        jiraHistoryChangeLog.setChangedTo(handleStr(item.getToString()));
                        jiraHistoryChangeLog.setUpdatedOn(LocalDateTime.parse(JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(history.getCreated()))));
                        fieldHistoryLog.add(jiraHistoryChangeLog);
                    }
                });
            }
        }

        //Merging Fix Version object based on updation Time
        if (jiraField.trim().equalsIgnoreCase(JiraConstants.FIXVERSION) && ObjectUtils.isNotEmpty(fieldHistoryLog)) {
            List<JiraHistoryChangeLog> fieldHistoryLogTemp = new ArrayList<>(fieldHistoryLog);
            JiraHistoryChangeLog prevHistoryChangeLog = fieldHistoryLog.get(0);
            fieldHistoryLog.clear();
            for (int i = 1; i < fieldHistoryLogTemp.size(); i++) {
                if (prevHistoryChangeLog.getUpdatedOn().equals(fieldHistoryLogTemp.get(i).getUpdatedOn())) {
                    JiraHistoryChangeLog currHistoryChangeLog = fieldHistoryLogTemp.get(i);
                    currHistoryChangeLog.setChangedFrom(concatStr(prevHistoryChangeLog.getChangedFrom(), currHistoryChangeLog.getChangedFrom()));
                    currHistoryChangeLog.setChangedTo(concatStr(prevHistoryChangeLog.getChangedTo(), currHistoryChangeLog.getChangedTo()));
                    fieldHistoryLogTemp.set(i, currHistoryChangeLog);
                    prevHistoryChangeLog = currHistoryChangeLog;
                } else {
                    fieldHistoryLog.add(prevHistoryChangeLog);
                    prevHistoryChangeLog = fieldHistoryLogTemp.get(i);
                }
            }
            fieldHistoryLog.add(prevHistoryChangeLog);
        }


        return fieldHistoryLog;
    }

    private List<JiraHistoryChangeLog> getCustomFieldChangeLog(List<ChangelogGroup> changeLogList, String jiraField, String jiraCustomField, Map<String, IssueField> fields, JiraIssue jiraIssue) {

        List<JiraHistoryChangeLog> fieldHistoryLog;
        String field = jiraField;
        if (StringUtils.isNotEmpty(jiraCustomField.trim()) && ObjectUtils.isNotEmpty(fields.get(jiraCustomField.trim()))) {
            field = fields.get(jiraCustomField.trim()).getName();
            log.info("In getCustomFieldChangeLog for custom field : " + field + " and fieldId : " + jiraCustomField);
        }
        fieldHistoryLog = getJiraFieldChangeLog(changeLogList, field.trim(), jiraIssue);
        return fieldHistoryLog;
    }


    private String handleStr(String str) {
        return str != null ? str : "";
    }

    private String concatStr(String str1, String str2) {
        if (StringUtils.isEmpty(str1)) return str2;
        if (StringUtils.isEmpty(str2)) return str1;
        String str3 = str1.concat(",");
        return str3.concat(str2);

    }

    public void setJiraIssueCustomHistoryUpdationLog(JiraIssueCustomHistory jiraIssueCustomHistory, List<ChangelogGroup> changeLogList, FieldMapping fieldMapping, JiraIssue jiraIssue, Map<String, IssueField> fields) {
        log.info("In setJiraIssueCustomHistoryUpdationLog");
        List<JiraHistoryChangeLog> statusChangeLog = getJiraFieldChangeLog(changeLogList, JiraConstants.STATUS, jiraIssue);
        List<JiraHistoryChangeLog> assigneeChangeLog = getJiraFieldChangeLog(changeLogList, JiraConstants.ASSIGNEE, jiraIssue);
        List<JiraHistoryChangeLog> priorityChangeLog = getJiraFieldChangeLog(changeLogList, JiraConstants.PRIORITY, jiraIssue);
        List<JiraHistoryChangeLog> fixVersionChangeLog = getJiraFieldChangeLog(changeLogList, JiraConstants.FIXVERSION, jiraIssue);
        List<JiraHistoryChangeLog> labelsChangeLog = getJiraFieldChangeLog(changeLogList, JiraConstants.LABELS, jiraIssue);
        List<JiraHistoryChangeLog> dueDateChangeLog = getCustomFieldChangeLog(changeLogList, JiraConstants.DUEDATE, handleStr(fieldMapping.getJiraDueDateCustomField()), fields, jiraIssue);
        List<JiraHistoryChangeLog> sprintChangeLog = getCustomFieldChangeLog(changeLogList, JiraConstants.SPRINT, handleStr(fieldMapping.getSprintName()), fields, jiraIssue);
        jiraIssueCustomHistory.setStatusUpdationLog(statusChangeLog);
        jiraIssueCustomHistory.setAssigneeUpdationLog(assigneeChangeLog);
        jiraIssueCustomHistory.setPriorityUpdationLog(priorityChangeLog);
        jiraIssueCustomHistory.setFixVersionUpdationLog(fixVersionChangeLog);
        jiraIssueCustomHistory.setLabelUpdationLog(labelsChangeLog);
        jiraIssueCustomHistory.setDueDateUpdationLog(dueDateChangeLog);
        jiraIssueCustomHistory.setSprintUpdationLog(sprintChangeLog);
    }
}


