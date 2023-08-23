package com.publicissapient.kpidashboard.jira.listener;

import com.publicissapient.kpidashboard.jira.processor.KanbanJiraIssueAccountHierarchyProcessor;
import com.publicissapient.kpidashboard.jira.processor.KanbanJiraIssueAssigneeProcessor;
import com.publicissapient.kpidashboard.jira.processor.KanbanJiraIssueHistoryProcessor;
import com.publicissapient.kpidashboard.jira.processor.KanbanJiraIssueProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KanbanJiraIssueStepListener implements StepExecutionListener {

    @Autowired
    private KanbanJiraIssueProcessor kanbanJiraIssueProcessor;
    @Autowired
    private KanbanJiraIssueAssigneeProcessor kanbanJiraIssueAssigneeProcessor;
    @Autowired
    private KanbanJiraIssueHistoryProcessor kanbanJiraIssueHistoryProcessor;
    @Autowired
    private KanbanJiraIssueAccountHierarchyProcessor kanbanJiraIssueAccountHierarchyProcessor;

    @Override
    public void beforeStep(StepExecution stepExecution) {

    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("cleaning all objects of Jira processors");
        kanbanJiraIssueProcessor.cleanAllObjects();
        kanbanJiraIssueHistoryProcessor.cleanAllObjects();
        kanbanJiraIssueAccountHierarchyProcessor.cleanAllObjects();
        kanbanJiraIssueAssigneeProcessor.cleanAllObjects();
        return stepExecution.getExitStatus();
    }
}

