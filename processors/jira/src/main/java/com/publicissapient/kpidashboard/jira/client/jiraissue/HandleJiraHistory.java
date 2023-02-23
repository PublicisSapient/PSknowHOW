package com.publicissapient.kpidashboard.jira.client.jiraissue;

import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class HandleJiraHistory {

    public List<JiraHistoryChangeLog> getStatusChangeLog(List<ChangelogGroup> changeLogList, FieldMapping fieldMapping) {

        List<JiraHistoryChangeLog> statusHistory = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(changeLogList)) {
            for (ChangelogGroup history : changeLogList) {
                history.getItems().forEach(item -> {

                    if (item.getField().trim().equalsIgnoreCase("status")) {
                        JiraHistoryChangeLog jiraHistoryChangeLog = new JiraHistoryChangeLog();
                        jiraHistoryChangeLog.setChangedFrom(item.getFromString());
                        jiraHistoryChangeLog.setChangedTo(item.getToString());
                        jiraHistoryChangeLog.setUpdatedOn(history.getCreated());
                        statusHistory.add(jiraHistoryChangeLog);
                    }
                });
            }
        }
        return statusHistory;
    }
}
