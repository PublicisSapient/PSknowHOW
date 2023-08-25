package com.publicissapient.kpidashboard.jira.listener;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.jira.processor.JiraIssueAccountHierarchyProcessor;
import com.publicissapient.kpidashboard.jira.processor.JiraIssueAssigneeProcessor;
import com.publicissapient.kpidashboard.jira.processor.JiraIssueHistoryProcessor;
import com.publicissapient.kpidashboard.jira.processor.JiraIssueProcessor;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JiraIssueStepListener implements StepExecutionListener {

	@Autowired
	private JiraIssueProcessor jiraIssueProcessor;

	@Autowired
	private JiraIssueHistoryProcessor jiraIssueHistoryProcessor;

	@Autowired
	private JiraIssueAccountHierarchyProcessor jiraIssueAccountHierarchyProcessor;

	@Autowired
	private JiraIssueAssigneeProcessor jiraIssueAssigneeProcessor;

	@Override
	public void beforeStep(StepExecution stepExecution) {

	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		log.info("cleaning all objects of Jira processors");
		jiraIssueProcessor.cleanAllObjects();
		jiraIssueHistoryProcessor.cleanAllObjects();
		jiraIssueAccountHierarchyProcessor.cleanAllObjects();
		jiraIssueAssigneeProcessor.cleanAllObjects();
		return stepExecution.getExitStatus();
	}
}
