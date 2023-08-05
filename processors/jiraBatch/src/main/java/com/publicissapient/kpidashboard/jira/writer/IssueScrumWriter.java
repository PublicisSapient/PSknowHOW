package com.publicissapient.kpidashboard.jira.writer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IssueScrumWriter implements ItemWriter<CompositeResult> {

	@Override
	public void write(List<? extends CompositeResult> compositeResults) throws Exception {
		log.info("Writing data to DB");
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
		for (JiraIssue jiraItem : jiraItems) {
			System.out.println(jiraItem);
		}
	}

	public void writeJiraHistory(List<JiraIssueCustomHistory> jiraHistoryItems) {
		for (JiraIssueCustomHistory historyItem : jiraHistoryItems) {
			System.out.println(historyItem);
		}
	}
}
