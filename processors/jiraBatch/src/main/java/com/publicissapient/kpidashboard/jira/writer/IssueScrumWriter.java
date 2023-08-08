package com.publicissapient.kpidashboard.jira.writer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IssueScrumWriter implements ItemWriter<CompositeResult> {

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Override
	public void write(List<? extends CompositeResult> compositeResults) throws Exception {
		List<JiraIssue> jiraIssues = new ArrayList<>();
		List<JiraIssueCustomHistory> jiraHistoryItems = new ArrayList<>();
		for (CompositeResult compositeResult : compositeResults) {
			jiraIssues.add(compositeResult.getJiraIssue());
			if (null != compositeResult.getJiraIssueCustomHistory()) {
				jiraHistoryItems.add(compositeResult.getJiraIssueCustomHistory());
			}
		}
		writeJiraItem(jiraIssues);
		writeJiraHistory(jiraHistoryItems);
	}

	public void writeJiraItem(List<JiraIssue> jiraItems) {
		log.info("Writing issues to Jira_Issue Collection");
		jiraIssueRepository.saveAll(jiraItems);
	}

	public void writeJiraHistory(List<JiraIssueCustomHistory> jiraHistoryItems) {
		log.info("Writing issues to Jira_Issue_custom_history Collection");
		jiraIssueCustomHistoryRepository.saveAll(jiraHistoryItems);
	}
}
